package com.example.energymeterapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

//  FINAL, CORRECTED AND ENCAPSULATED VERSION
public class AnalysisResult {
    @SerializedName("text_summary")
    private String textSummary;
    @SerializedName("chart_data")
    private List<String> chartData;
    @SerializedName("chart_title")
    private String chartTitle;

    /**
     * Default constructor for robust object creation, particularly with GSON.
     */
    public AnalysisResult() {
        // GSON can use this to instantiate the object.
    }

    // --- Getter Methods ---

    public String getTextSummary() {
        return textSummary;
    }

    public List<String> getChartData() {
        return chartData;
    }

    public String getChartTitle() {
        return chartTitle;
    }
}
