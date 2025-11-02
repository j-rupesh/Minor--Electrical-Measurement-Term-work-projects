package com.example.energymeterapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.github.anastr.speedviewlib.AwesomeSpeedometer;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private static final int MAX_CHART_ENTRIES = 100;

    // --- Views ---
    private AwesomeSpeedometer gaugeVoltage, gaugeCurrent;
    private TextView tvPower, tvEnergy, tvPF, tvFrequency, tvConnectionStatus, tvTitle;
    private LineChart chartPower, chartEnergy, chartPF, chartFrequency;
    private EditText etEsp32Ip;
    private ImageButton btnRefreshIp;
    private Spinner spinnerSavedIps;
    private Button btnSetIp;
    private View statusIndicator;
    private BottomNavigationView bottomNavigationView;

    // --- Chart data ---
    private final ArrayList<Entry> powerEntries = new ArrayList<>();
    private final ArrayList<Entry> energyEntries = new ArrayList<>();
    private final ArrayList<Entry> pfEntries = new ArrayList<>();
    private final ArrayList<Entry> frequencyEntries = new ArrayList<>();
    private int time = 0;

    // --- State & Network ---
    private Handler handler;
    private RequestQueue requestQueue;
    private String esp32Url;
    private AlertDialog alertDialog;
    private AlertDialog loadingDialog;
    private boolean lastConnectionSuccessful = false;

    private final ArrayList<String> savedIpsList = new ArrayList<>();
    private ArrayAdapter<String> ipAdapter;

    private final Runnable dataFetcher = new Runnable() {
        @Override
        public void run() {
            fetchData();
            handler.postDelayed(this, 2000); // every 2 seconds
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupNavigation();

        handler = new Handler(Looper.getMainLooper());
        requestQueue = Volley.newRequestQueue(this);

        setupGauges();
        setupCharts();
        setupIpSpinner();
        loadEsp32Url();

        if (esp32Url != null) {
            String currentIp = esp32Url.replace("http://", "").replace("/data", "");
            etEsp32Ip.setText(currentIp);
            int position = ipAdapter.getPosition(currentIp);
            if (position >= 0) {
                spinnerSavedIps.setSelection(position);
            }
        }

        updateConnectionStatus(false, esp32Url == null);
        setupListeners();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        gaugeVoltage = findViewById(R.id.gaugeVoltage);
        gaugeCurrent = findViewById(R.id.gaugeCurrent);
        tvPower = findViewById(R.id.tvPower);
        tvEnergy = findViewById(R.id.tvEnergy);
        tvPF = findViewById(R.id.tvPF);
        tvFrequency = findViewById(R.id.tvFrequency);
        chartPower = findViewById(R.id.chartPower);
        chartEnergy = findViewById(R.id.chartEnergy);
        chartPF = findViewById(R.id.chartPF);
        chartFrequency = findViewById(R.id.chartFrequency);
        etEsp32Ip = findViewById(R.id.etEsp32Ip);
        btnSetIp = findViewById(R.id.btnSetIp);
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);
        statusIndicator = findViewById(R.id.statusIndicator);
        btnRefreshIp = findViewById(R.id.btnRefreshIp);
        spinnerSavedIps = findViewById(R.id.spinnerSavedIps);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.navigation_home) {
            return true; // Already on this screen
        }
        Intent intent = null;
        if (itemId == R.id.navigation_ai_analyze) {
            intent = new Intent(this, AiAnalyzeActivity.class);
        } else if (itemId == R.id.navigation_analysis) {
            intent = new Intent(this, AnalysisActivity.class);
        } else if (itemId == R.id.navigation_all_data) {
            intent = new Intent(this, AllDataActivity.class);
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

    private void setupIpSpinner() {
        loadSavedIps();
        ipAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, savedIpsList);
        spinnerSavedIps.setAdapter(ipAdapter);
    }

    private void setupListeners() {
        btnRefreshIp.setOnClickListener(v -> {
            String selectedIp = (String) spinnerSavedIps.getSelectedItem();
            if (selectedIp != null && !selectedIp.isEmpty()) {
                showLoadingDialog("Refreshing connection...");
                updateIp(selectedIp);
            } else {
                Toast.makeText(this, "No saved IP selected.", Toast.LENGTH_SHORT).show();
            }
        });

        btnSetIp.setOnClickListener(v -> {
            String ip = etEsp32Ip.getText().toString().trim();
            if (!ip.isEmpty()) {
                showLoadingDialog("Connecting to device...");
                saveAndSetIp(ip);
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            } else {
                Toast.makeText(this, "Please enter an IP address.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(dataFetcher);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(dataFetcher);
    }

    private void loadEsp32Url() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String ip = prefs.getString("esp32_ip_address", "");
        esp32Url = ip.isEmpty() ? null : "http://" + ip + "/data";
    }

    private void saveAndSetIp(String inputIp) {
        String ip = inputIp.trim();
        if (ip.isEmpty()) {
            Toast.makeText(this, "IP address cannot be empty.", Toast.LENGTH_SHORT).show();
            dismissLoadingDialog();
            return;
        }

        if (!ip.matches("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
            Toast.makeText(this, "Error: Invalid IP format.", Toast.LENGTH_LONG).show();
            dismissLoadingDialog();
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> ipSet = prefs.getStringSet("saved_ip_list", new HashSet<>());
        if (!ipSet.contains(ip)) {
            ipSet.add(ip);
            prefs.edit().putStringSet("saved_ip_list", ipSet).apply();
            savedIpsList.add(ip);
            ipAdapter.notifyDataSetChanged();
        }

        prefs.edit().putString("esp32_ip_address", ip).apply();
        int position = ipAdapter.getPosition(ip);
        if (position >= 0) {
            spinnerSavedIps.setSelection(position);
        }

        updateIp(ip);
    }

    private void loadSavedIps() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> ipSet = prefs.getStringSet("saved_ip_list", new HashSet<>());
        savedIpsList.clear();
        savedIpsList.addAll(ipSet);
    }

    private void updateIp(String ip) {
        if (!ip.matches("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
            Toast.makeText(this, "Enter valid IP.", Toast.LENGTH_SHORT).show();
            dismissLoadingDialog();
            return;
        }
        etEsp32Ip.setText(ip);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString("esp32_ip_address", ip).apply();
        loadEsp32Url();
        resetDataAndCharts();
        lastConnectionSuccessful = false;
        updateConnectionStatus(false, false);
        handler.removeCallbacks(dataFetcher);
        handler.post(dataFetcher);
        handler.postDelayed(this::dismissLoadingDialog, 4000);
        Toast.makeText(this, "Attempting to connect to: " + ip, Toast.LENGTH_SHORT).show();
    }

    private void resetDataAndCharts() {
        powerEntries.clear();
        energyEntries.clear();
        pfEntries.clear();
        frequencyEntries.clear();
        time = 0;
        gaugeVoltage.speedTo(0);
        gaugeCurrent.speedTo(0);
        tvPower.setText("P = 0.00 kW");
        tvEnergy.setText("E = 0.00 kWh");
        tvPF.setText("PF = 0.00");
        tvFrequency.setText("Freq = 0.0 Hz");
        updateChart(chartPower, powerEntries, "Power (kW)", "#00AEEF");
        updateChart(chartEnergy, energyEntries, "Energy (kWh)", "#FF9800");
        updateChart(chartPF, pfEntries, "Power Factor", "#4CAF50");
        updateChart(chartFrequency, frequencyEntries, "Frequency (Hz)", "#9C27B0");
    }

    private void setupGauges() {
        if (gaugeVoltage != null) {
            gaugeVoltage.setUnit("V");
            gaugeVoltage.setUnitTextSize(30f);
            gaugeVoltage.setSpeedTextSize(40f);
        }
        if (gaugeCurrent != null) {
            gaugeCurrent.setUnit("A");
            gaugeCurrent.setUnitTextSize(30f);
            gaugeCurrent.setSpeedTextSize(40f);
        }
    }

    private void setupCharts() {
        if (chartPower != null) setupChart(chartPower);
        if (chartEnergy != null) setupChart(chartEnergy);
        if (chartPF != null) setupChart(chartPF);
        if (chartFrequency != null) setupChart(chartFrequency);
    }

    private void setupChart(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        int axisColor = Color.parseColor("#A0A0A0");
        chart.getXAxis().setTextColor(axisColor);
        chart.getAxisLeft().setTextColor(axisColor);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setTextColor(axisColor);
    }

    private void fetchData() {
        if (esp32Url != null && !esp32Url.isEmpty()) {
            fetchDataFromNetwork();
        } else {
            if (!lastConnectionSuccessful) {
                updateConnectionStatus(false, true);
            }
            fetchDataFromAsset();
        }
    }

    private void fetchDataFromNetwork() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, esp32Url, null,
                response -> {
                    dismissLoadingDialog();
                    lastConnectionSuccessful = true;
                    try {
                        if (response.length() > 0) {
                            JSONObject latest = response.getJSONObject(0);
                            JSONArray phases = latest.getJSONArray("phases");
                            JSONObject phase1 = phases.getJSONObject(0);
                            JSONObject finalData = new JSONObject();
                            finalData.put("voltage", phase1.optDouble("voltage_v", 0.0));
                            finalData.put("current", phase1.optDouble("current_a", 0.0));
                            finalData.put("power", phase1.optDouble("power_kw", 0.0));
                            finalData.put("energy", latest.optDouble("energy_kwh_total", 0.0));
                            finalData.put("pf", phase1.optDouble("pf", 0.0));
                            finalData.put("frequency", latest.optDouble("frequency_hz", 0.0));
                            handleResponse(finalData, false);
                        } else {
                            updateConnectionStatus(true, false);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Network JSON parsing error", e);
                        lastConnectionSuccessful = false;
                        updateConnectionStatus(false, false);
                        fetchDataFromAsset();
                    }
                },
                error -> {
                    dismissLoadingDialog();
                    String errorMessage = getVolleyErrorReason(error);
                    Log.e(TAG, "Network error: " + errorMessage, error);
                    Toast.makeText(this, "Connection Failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    lastConnectionSuccessful = false;
                    updateConnectionStatus(false, false);
                    fetchDataFromAsset();
                });
        requestQueue.add(request);
    }

    private String getVolleyErrorReason(VolleyError error) {
        if (error instanceof TimeoutError || error.getCause() instanceof java.net.SocketTimeoutException) {
            return "Connection Timed Out. Check IP/Port.";
        } else if (error instanceof NoConnectionError) {
            return "No Connection. Check IP or Network/Firewall.";
        } else if (error.getMessage() != null && error.getMessage().contains("refused")) {
            return "Connection Refused. Server is unreachable.";
        } else {
            return "Unknown Error: " + error.toString();
        }
    }

    private void fetchDataFromAsset() {
        dismissLoadingDialog();
        try {
            InputStream is = getAssets().open("sample_data.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            JSONArray response = new JSONArray(new String(buffer, StandardCharsets.UTF_8));
            if (response.length() > 0) {
                JSONObject latest = response.getJSONObject(response.length() - 1);
                handleResponse(latest, true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading asset JSON", e);
            updateConnectionStatus(false, false);
        }
    }

    private void handleResponse(JSONObject response, boolean isLocal) {
        float voltage = (float) response.optDouble("voltage", 0.0);
        float current = (float) response.optDouble("current", 0.0);
        float power = (float) response.optDouble("power", 0.0);
        float energy = (float) response.optDouble("energy", 0.0);
        float pf = (float) response.optDouble("pf", 0.0);
        float frequency = (float) response.optDouble("frequency", 0.0);
        runOnUiThread(() -> {
            updateConnectionStatus(!isLocal && lastConnectionSuccessful, isLocal);
            gaugeVoltage.speedTo(voltage);
            gaugeCurrent.speedTo(current);
            tvPower.setText(String.format("P = %.2f kW", power));
            tvEnergy.setText(String.format("E = %.2f kWh", energy));
            tvPF.setText(String.format("PF = %.2f", pf));
            tvFrequency.setText(String.format("Freq = %.1f Hz", frequency));
            addEntry(powerEntries, power);
            addEntry(energyEntries, energy);
            addEntry(pfEntries, pf);
            addEntry(frequencyEntries, frequency);
            updateChart(chartPower, powerEntries, "Power (kW)", "#00AEEF");
            updateChart(chartEnergy, energyEntries, "Energy (kWh)", "#FF9800");
            updateChart(chartPF, pfEntries, "Power Factor", "#4CAF50");
            updateChart(chartFrequency, frequencyEntries, "Frequency (Hz)", "#9C27B0");
            time += 2;
            if (voltage > 250 || current > 10) showAlert(voltage, current);
        });
    }

    private void addEntry(ArrayList<Entry> entries, float value) {
        if (entries.size() >= MAX_CHART_ENTRIES) entries.remove(0);
        entries.add(new Entry(time, value));
    }

    private void updateChart(LineChart chart, ArrayList<Entry> entries, String label, String colorHex) {
        if (chart == null) return;
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(Color.parseColor(colorHex));
        dataSet.setCircleColor(Color.parseColor(colorHex));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setLineWidth(2.5f);
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.notifyDataSetChanged();
        chart.moveViewToX(lineData.getEntryCount() - 1);
        chart.invalidate();
    }

    private void updateConnectionStatus(boolean isConnected, boolean isLocal) {
        if (tvConnectionStatus == null || statusIndicator == null) return;
        if (isLocal) {
            tvConnectionStatus.setText("Status: LOCAL (Demo)");
            tvConnectionStatus.setTextColor(Color.parseColor("#FF9800"));
            statusIndicator.setBackgroundColor(Color.parseColor("#FF9800"));
        } else if (isConnected) {
            tvConnectionStatus.setText("Status: ONLINE");
            tvConnectionStatus.setTextColor(Color.parseColor("#4CAF50"));
            statusIndicator.setBackgroundColor(Color.parseColor("#4CAF50"));
        } else {
            tvConnectionStatus.setText("Status: OFFLINE");
            tvConnectionStatus.setTextColor(Color.parseColor("#F44336"));
            statusIndicator.setBackgroundColor(Color.parseColor("#F44336"));
        }
    }

    private void showAlert(float voltage, float current) {
        if (alertDialog != null && alertDialog.isShowing()) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Critical Alert!");
        StringBuilder msg = new StringBuilder();
        if (voltage > 250) msg.append(String.format("Voltage High: %.2f V\n", voltage));
        if (current > 10) msg.append(String.format("Current High: %.2f A\n", current));
        builder.setMessage(msg.toString());
        builder.setPositiveButton("OK", (dialog, which) -> alertDialog = null);
        runOnUiThread(() -> {
            alertDialog = builder.create();
            alertDialog.show();
        });
    }

    public void onSetIpClicked(View view) {
        String ip = etEsp32Ip.getText().toString().trim();
        if (!ip.isEmpty()) {
            showLoadingDialog("Connecting to device...");
            saveAndSetIp(ip);
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void showLoadingDialog(String message) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message + "\nPlease wait...").setCancelable(false);
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}
