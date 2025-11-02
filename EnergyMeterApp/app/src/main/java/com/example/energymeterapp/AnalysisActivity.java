package com.example.energymeterapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnalysisActivity extends AppCompatActivity {

    private LineChart analysisChart;
    private TextView tvChartTitle;
    private Button btnDay, btnWeek, btnMonth, btnYear;
    private Button btnPower, btnEnergy, btnPF, btnFrequency;

    private String currentPeriod = "Day";
    private final Set<String> selectedDataTypes = new HashSet<>(List.of("Power"));

    private final List<Entry> dayPowerData = new ArrayList<>();
    private final List<Entry> dayEnergyData = new ArrayList<>();
    private final List<Entry> dayPFData = new ArrayList<>();
    private final List<Entry> dayFrequencyData = new ArrayList<>();
    private final List<Entry> weekPowerData = new ArrayList<>();
    private final List<Entry> weekEnergyData = new ArrayList<>();
    private final List<Entry> weekPFData = new ArrayList<>();
    private final List<Entry> weekFrequencyData = new ArrayList<>();
    private final List<Entry> monthPowerData = new ArrayList<>();
    private final List<Entry> monthEnergyData = new ArrayList<>();
    private final List<Entry> monthPFData = new ArrayList<>();
    private final List<Entry> monthFrequencyData = new ArrayList<>();
    private final List<Entry> yearPowerData = new ArrayList<>();
    private final List<Entry> yearEnergyData = new ArrayList<>();
    private final List<Entry> yearPFData = new ArrayList<>();
    private final List<Entry> yearFrequencyData = new ArrayList<>();

    private static final Map<String, String> COLOR_MAP = Map.of(
            "Power", "#00AEEF",
            "Energy", "#FF9800",
            "PF", "#4CAF50",
            "Frequency", "#F44336"
    );
    private static final Map<String, String> UNIT_MAP = Map.of(
            "Power", "kW",
            "Energy", "kWh",
            "PF", "",
            "Frequency", "Hz"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Consumption Analysis");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        initViews();
        setupNavigation(); // Add this line
        setupChart();
        generateDummyData();
        setupListeners();

        updateDisplay();
    }

    private void initViews() {
        analysisChart = findViewById(R.id.analysisChart);
        tvChartTitle = findViewById(R.id.tvChartTitle);
        btnDay = findViewById(R.id.btnDay);
        btnWeek = findViewById(R.id.btnWeek);
        btnMonth = findViewById(R.id.btnMonth);
        btnYear = findViewById(R.id.btnYear);
        btnPower = findViewById(R.id.btnPower);
        btnEnergy = findViewById(R.id.btnEnergy);
        btnPF = findViewById(R.id.btnPF);
        btnFrequency = findViewById(R.id.btnFrequency);
    }

    private void setupNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_analysis);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_analysis) {
                return true; // Already on this screen
            }
            Intent intent = null;
            if (itemId == R.id.navigation_home) {
                intent = new Intent(getApplicationContext(), MainActivity.class);
            } else if (itemId == R.id.navigation_ai_analyze) {
                intent = new Intent(getApplicationContext(), AiAnalyzeActivity.class);
            } else if (itemId == R.id.navigation_all_data) {
                intent = new Intent(getApplicationContext(), AllDataActivity.class);
            } else if (itemId == R.id.navigation_settings) {
                intent = new Intent(getApplicationContext(), SettingsActivity.class);
            }

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            return true;
        });
    }

    private void setupListeners() {
        btnDay.setOnClickListener(v -> setPeriod("Day"));
        btnWeek.setOnClickListener(v -> setPeriod("Week"));
        btnMonth.setOnClickListener(v -> setPeriod("Month"));
        btnYear.setOnClickListener(v -> setPeriod("Year"));

        btnPower.setOnClickListener(v -> setDataType("Power"));
        btnEnergy.setOnClickListener(v -> setDataType("Energy"));
        btnPF.setOnClickListener(v -> setDataType("PF"));
        btnFrequency.setOnClickListener(v -> setDataType("Freq"));
    }

    private void setPeriod(String period) {
        currentPeriod = period;
        updateDisplay();
    }

    private void setDataType(String dataType) {
        String fullDataType = getFullDataType(dataType);
        if (selectedDataTypes.contains(fullDataType)) {
            if (selectedDataTypes.size() > 1) {
                selectedDataTypes.remove(fullDataType);
            } else {
                Toast.makeText(this, "Must select at least one data type.", Toast.LENGTH_SHORT).show();
            }
        } else {
            selectedDataTypes.add(fullDataType);
        }
        updateDisplay();
    }

    private String getFullDataType(String shortName) {
        if (shortName.equals("Freq")) {
            return "Frequency";
        }
        return shortName;
    }

    private void setupChart() {
        analysisChart.getDescription().setEnabled(false);
        analysisChart.setTouchEnabled(true);
        analysisChart.setDragEnabled(true);
        analysisChart.setScaleEnabled(true);
        analysisChart.setDrawGridBackground(false);
        int axisColor = Color.parseColor("#A0A0A0");
        analysisChart.getXAxis().setTextColor(axisColor);
        analysisChart.getAxisLeft().setTextColor(axisColor);
        analysisChart.getAxisRight().setEnabled(false);
        Legend legend = analysisChart.getLegend();
        legend.setTextColor(axisColor);
        legend.setWordWrapEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        analysisChart.getXAxis().setDrawGridLines(false);
        analysisChart.getAxisLeft().setDrawGridLines(true);
        analysisChart.getAxisLeft().setGridColor(Color.parseColor("#404040"));
    }

    private List<List<Entry>> getPeriodLists(String period) {
        switch (period) {
            case "Day":
                return List.of(dayPowerData, dayEnergyData, dayPFData, dayFrequencyData);
            case "Week":
                return List.of(weekPowerData, weekEnergyData, weekPFData, weekFrequencyData);
            case "Month":
                return List.of(monthPowerData, monthEnergyData, monthPFData, monthFrequencyData);
            case "Year":
                return List.of(yearPowerData, yearEnergyData, yearPFData, yearFrequencyData);
            default:
                return Collections.emptyList();
        }
    }

    private void generateDummyData() {
        int[] periods = {24, 7, 30, 12};
        List<List<List<Entry>>> allLists = List.of(
                List.of(dayPowerData, weekPowerData, monthPowerData, yearPowerData),
                List.of(dayEnergyData, weekEnergyData, monthEnergyData, yearEnergyData),
                List.of(dayPFData, weekPFData, monthPFData, yearPFData),
                List.of(dayFrequencyData, weekFrequencyData, monthFrequencyData, yearFrequencyData)
        );

        for (int p = 0; p < periods.length; p++) {
            int maxPoints = periods[p];
            for (int i = 0; i < maxPoints; i++) {
                allLists.get(0).get(p).add(new Entry(i, (float) (Math.random() * 40 + 10)));
                allLists.get(1).get(p).add(new Entry(i, (float) (Math.random() * 200 + 100)));
                allLists.get(2).get(p).add(new Entry(i, (float) (Math.random() * 0.2 + 0.8)));
                allLists.get(3).get(p).add(new Entry(i, (float) (Math.random() * 0.4 + 49.8)));
            }
        }
    }

    private void updateDisplay() {
        updatePeriodHighlight(currentPeriod, btnDay, btnWeek, btnMonth, btnYear);
        updateDataTypeHighlight(selectedDataTypes, btnPower, btnEnergy, btnPF, btnFrequency);

        List<ILineDataSet> dataSets = new ArrayList<>();
        List<List<Entry>> periodLists = getPeriodLists(currentPeriod);
        final Map<String, Integer> dataTypeIndexMap = Map.of("Power", 0, "Energy", 1, "PF", 2, "Frequency", 3);
        List<String> sortedSelectedTypes = new ArrayList<>(selectedDataTypes);
        Collections.sort(sortedSelectedTypes);

        StringBuilder titleBuilder = new StringBuilder(currentPeriod + " Trend: ");

        for (String dataType : sortedSelectedTypes) {
            int index = dataTypeIndexMap.get(dataType);
            List<Entry> entries = periodLists.get(index);
            String colorHex = COLOR_MAP.get(dataType);
            String unit = UNIT_MAP.get(dataType);
            LineDataSet dataSet = createDataSet(entries, dataType + " (" + unit + ")", colorHex);
            dataSets.add(dataSet);
            titleBuilder.append(dataType).append(", ");
        }

        if (titleBuilder.length() > (currentPeriod.length() + 8)) {
            tvChartTitle.setText(titleBuilder.substring(0, titleBuilder.length() - 2));
        } else {
            tvChartTitle.setText(currentPeriod + " Trend: (No Data Selected)");
        }

        if (!dataSets.isEmpty()) {
            LineData lineData = new LineData(dataSets);
            analysisChart.setData(lineData);
        } else {
            analysisChart.clear();
        }
        analysisChart.notifyDataSetChanged();
        analysisChart.invalidate();
    }

    private LineDataSet createDataSet(List<Entry> entries, String label, String colorHex) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        int color = Color.parseColor(colorHex);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setLineWidth(2.5f);
        return dataSet;
    }

    private void updatePeriodHighlight(String selected, Button... buttons) {
        int highlightColor = Color.parseColor("#55FFFFFF");
        int transparentColor = Color.TRANSPARENT;
        for (Button button : buttons) {
            if (button.getText().toString().equalsIgnoreCase(selected)) {
                button.setBackgroundColor(highlightColor);
            } else {
                button.setBackgroundColor(transparentColor);
            }
        }
    }

    private void updateDataTypeHighlight(Set<String> selected, Button... buttons) {
        int highlightColor = Color.parseColor("#8000AEEF");
        int transparentColor = Color.TRANSPARENT;
        for (Button button : buttons) {
            String dataType = getFullDataType(button.getText().toString());
            if (selected.contains(dataType)) {
                button.setBackgroundColor(highlightColor);
            } else {
                button.setBackgroundColor(transparentColor);
            }
        }
    }
}
