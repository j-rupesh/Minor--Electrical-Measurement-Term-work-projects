package com.example.energymeterapp;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AllDataActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "AllDataActivity";
    private static final int STORAGE_PERMISSION_CODE = 101;

    private RecyclerView recyclerView;
    private DataEntryAdapter dataAdapter;
    private Button btnDownloadCsv;

    private List<DataEntry> allDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_data);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("All Data Logs");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerView = findViewById(R.id.recyclerViewAllData);
        btnDownloadCsv = findViewById(R.id.btnDownloadCsv);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setupNavigation();
        loadDummyData();

        btnDownloadCsv.setOnClickListener(v -> requestStoragePermission());
    }

    private void setupNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_all_data);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.navigation_all_data) {
            return true; // Already on this screen
        }
        Intent intent = null;
        if (itemId == R.id.navigation_home) {
            intent = new Intent(this, MainActivity.class);
        } else if (itemId == R.id.navigation_ai_analyze) {
            intent = new Intent(this, AiAnalyzeActivity.class);
        } else if (itemId == R.id.navigation_analysis) {
            intent = new Intent(this, AnalysisActivity.class);
        } else if (itemId == R.id.navigation_settings) {
            intent = new Intent(this, SettingsActivity.class);
        }

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
        return true;
    }

    private void loadDummyData() {
        try {
            InputStream is = getAssets().open("sample_data.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            JSONArray jsonArray = new JSONArray(new String(buffer, StandardCharsets.UTF_8));

            allDataList.clear();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                allDataList.add(new DataEntry(
                        obj.optString("timestamp", "N/A"),
                        String.format("R: %.1f, Y: %.1f, B: %.1f", obj.optDouble("voltage", 0.0), obj.optDouble("voltage", 0.0), obj.optDouble("voltage", 0.0)),
                        String.format("R: %.2f, Y: %.2f, B: %.2f", obj.optDouble("current", 0.0), obj.optDouble("current", 0.0), obj.optDouble("current", 0.0)),
                        String.format("Active: %.2f, Reactive: %.2f", obj.optDouble("power", 0.0), obj.optDouble("power", 0.0) / 2),
                        String.format("Total: %.2f", obj.optDouble("pf", 0.0)),
                        String.format("%.2f kWh", obj.optDouble("energy", 0.0)),
                        String.format("%.1f Hz", obj.optDouble("frequency", 0.0))
                ));
            }
            dataAdapter = new DataEntryAdapter(allDataList);
            recyclerView.setAdapter(dataAdapter);

        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error loading data", e);
            Toast.makeText(this, "Could not load data from assets.", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        } else {
            showDateRangeDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showDateRangeDialog();
        } else {
            Toast.makeText(this, "Storage permission is required to download CSV.", Toast.LENGTH_LONG).show();
        }
    }

    private void showDateRangeDialog() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            generateAndDownloadCsv(allDataList);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setMessage("Select a date to download data for that period (dummy).");
        datePickerDialog.show();
    }

    private void generateAndDownloadCsv(List<DataEntry> dataToDownload) {
        StringBuilder csvData = new StringBuilder();
        csvData.append("Timestamp,Voltage,Current,Power,PowerFactor,Energy,Frequency\n");

        for (DataEntry entry : dataToDownload) {
            csvData.append(String.format("%s,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    entry.getTimestamp(),
                    entry.getVoltage(),
                    entry.getCurrent(),
                    entry.getPower(),
                    entry.getPowerFactor(),
                    entry.getEnergy(),
                    entry.getFrequency()
            ));
        }

        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, "EnergyData.csv");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(csvData.toString().getBytes(StandardCharsets.UTF_8));
            fos.close();
            Toast.makeText(this, "CSV saved to Downloads folder.", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e(TAG, "Error writing CSV file", e);
            Toast.makeText(this, "Failed to save CSV.", Toast.LENGTH_SHORT).show();
        }
    }
}
