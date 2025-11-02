package com.example.energymeterapp;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AiAnalyzeActivity extends AppCompatActivity {

    private static final int FILE_PICKER_REQUEST_CODE = 102;
    private static final String SENDER_AI = "RT AI Assistant";
    private static final String SENDER_USER = "You";
    private static final String TYPING_MESSAGE_CONTENT = "AI is thinking...";

    private RecyclerView recyclerViewChat;
    private ChatMessageAdapter chatAdapter;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageButton btnAttachFile;
    private ProgressBar progressBar;
    private TextView tvAttachedFile;

    private final List<ChatMessage> chatMessages = new ArrayList<>();
    private Uri selectedFileUri = null;
    private ChatMessage typingMessage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_analyze);

        setupToolbar();
        setupNavigation();
        setupChatInterface();

        if (chatMessages.isEmpty()) {
            addMessage(SENDER_AI,
                    "Hello! I am your RT AI Energy Assistant. Tap '+' to upload a file and send a query.",
                    false, null);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.navigation_ai_analyze);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_ai_analyze) return true;

            Intent intent = null;
            if (id == R.id.navigation_home) intent = new Intent(this, MainActivity.class);
            else if (id == R.id.navigation_analysis) intent = new Intent(this, AnalysisActivity.class);
            else if (id == R.id.navigation_all_data) intent = new Intent(this, AllDataActivity.class);
            else if (id == R.id.navigation_settings) intent = new Intent(this, SettingsActivity.class);

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            return true;
        });
    }

    private void setupChatInterface() {
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnAttachFile = findViewById(R.id.btnAttachFile);
        progressBar = findViewById(R.id.progressBar);
        tvAttachedFile = findViewById(R.id.tvAttachedFile);

        chatAdapter = new ChatMessageAdapter(chatMessages);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);

        btnAttachFile.setOnClickListener(v -> openFilePicker());
        btnSend.setOnClickListener(v -> handleSendMessage());
    }

    private void handleSendMessage() {
        String userMessage = etMessage.getText().toString().trim();
        if (userMessage.isEmpty() && selectedFileUri == null) {
            Toast.makeText(this, "Please type a message or attach a file.", Toast.LENGTH_SHORT).show();
            return;
        }

        String displayContent = (selectedFileUri != null)
                ? "File attached (" + getFileName(selectedFileUri) + "). Query: " + userMessage
                : userMessage;

        addMessage(SENDER_USER, displayContent, true, null);
        etMessage.setText("");

        // Add "typing..." message safely
        typingMessage = addMessage(SENDER_AI, TYPING_MESSAGE_CONTENT, false, null);
        progressBar.setVisibility(View.VISIBLE);

        new AiResponseTask().execute(userMessage);
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"text/csv", "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(Intent.createChooser(intent, "Select Energy Data File"), FILE_PICKER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedFileUri = data.getData();
            String fileName = getFileName(selectedFileUri);
            tvAttachedFile.setText("Attached: " + fileName);
            tvAttachedFile.setVisibility(View.VISIBLE);
            addMessage(SENDER_AI, "File '" + fileName + "' is ready. Please send your query.", false, null);
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) result = cursor.getString(index);
                }
            } catch (Exception ignored) {}
        }
        if (result == null) result = uri.getLastPathSegment();
        return result;
    }

    private ChatMessage addMessage(String sender, String content, boolean isUser, Object chartData) {
        int type = isUser ? ChatMessage.TYPE_SENT : ChatMessage.TYPE_RECEIVED;
        // Check if chartData is a List, indicating a graphical message
        boolean isGraphical = chartData != null && chartData instanceof List;

        if (isGraphical) {
            type = ChatMessage.TYPE_GRAPH;
        }

        String timestamp = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        ChatMessage newMessage = new ChatMessage(sender, content, type, isGraphical, chartData, timestamp);

        chatMessages.add(newMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerViewChat.scrollToPosition(chatMessages.size() - 1);

        return newMessage;
    }

    private class AiResponseTask extends AsyncTask<String, Void, String> {
        private Object chartData = null;
        private String chartTitle = null; // To set the title dynamically

        @Override
        protected void onPreExecute() {
            btnSend.setEnabled(false);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                Thread.sleep(2000); // simulate AI processing delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            String query = strings[0];
            if (selectedFileUri != null) {
                // Call the new method to read/analyze the file and set chartData
                return readAndAnalyzeCsv(selectedFileUri, query); // CORRECTED: Now returns the analysis string
            } else {
                return "Please attach a file to get a detailed analysis.";
            }
        }

        /**
         * Simulates reading and analyzing the CSV data based on the user's query.
         * In a real implementation, this is where you would use a CSV library to process the file.
         */
        private String readAndAnalyzeCsv(Uri fileUri, String query) {
            // --- ACTUAL FILE READING SIMULATION ---
            StringBuilder fileContent = new StringBuilder();
            try (InputStream inputStream = getContentResolver().openInputStream(fileUri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                String line;
                int lineCount = 0;
                while ((line = reader.readLine()) != null && lineCount < 5) {
                    fileContent.append(line).append("\n");
                    lineCount++;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "Error reading file: " + e.getMessage();
            }
            // --- END FILE READING ---

            // --- ANALYSIS AND GRAPH DATA GENERATION ---
            String lowerCaseQuery = query.toLowerCase(Locale.ROOT);

            // If the query contains keywords for plotting, generate chartData
            if (lowerCaseQuery.contains("graph") || lowerCaseQuery.contains("trend") || lowerCaseQuery.contains("show")) {

                // *** SIMULATED CHART DATA GENERATION ***
                // In a real app, this would be List<Entry> or a similar structure
                // containing processed data (e.g., Time vs. Energy).
                List<String> mockDataPoints = new ArrayList<>();
                mockDataPoints.add("01:00, 50kWh");
                mockDataPoints.add("02:00, 65kWh");
                mockDataPoints.add("03:00, 80kWh");
                mockDataPoints.add("04:00, 120kWh");
                mockDataPoints.add("05:00, 95kWh");

                chartData = mockDataPoints; // This List<String> is now the structured data
                chartTitle = "Consumption Trend: " + query; // Set the chart title

                return "Analysis complete. I have processed the first few lines:\n" + fileContent.toString().trim() + "\nGenerating the " + (lowerCaseQuery.contains("trend") ? "hourly trend" : "requested graph") + " below.";
            }

            // CORRECTED: If no graph is requested, return text analysis based on the file content and query
            String analysisResult = analyzeQuery(query);
            return "File processed (" + fileContent.toString().trim() + "). " + analysisResult;
        }
        // ----------------------------------------------------


        @Override
        protected void onPostExecute(String result) {
            // Remove "typing..." safely
            if (typingMessage != null && chatMessages.contains(typingMessage)) {
                int index = chatMessages.indexOf(typingMessage);
                chatMessages.remove(index);
                chatAdapter.notifyItemRemoved(index);
                typingMessage = null;
            }

            // 1. Add the text response
            addMessage(SENDER_AI, result, false, null);

            // 2. Add the graphical response if data was generated
            if (chartData != null) {
                // CORRECTED: Use chartTitle for the content of the graphical message
                String title = chartTitle != null ? chartTitle : "Data Trend Analysis";
                addMessage(SENDER_AI, title, false, chartData);
            }

            progressBar.setVisibility(View.GONE);
            btnSend.setEnabled(true);

            // Reset file state after processing
            selectedFileUri = null;
            tvAttachedFile.setVisibility(View.GONE);
        }
    }

    private String analyzeQuery(String query) {
        query = query.toLowerCase(Locale.ROOT);
        if (query.contains("light used") || query.contains("consumption")) {
            return "ANALYSIS: Average light usage 18.2 kWh/day. Peak at 8 PM.";
        } else if (query.contains("peak")) {
            return "ANALYSIS: Peak consumption 150 kWh on Friday at 7 PM.";
        } else {
            return "ANALYSIS: Total entries processed. Power Factor avg 0.98. Ask a specific question like 'show light used' or 'show trend'.";
        }
    }
}