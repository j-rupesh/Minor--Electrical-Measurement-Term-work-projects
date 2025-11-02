package com.example.energymeterapp;

// FINAL CORRECTION: This is the definitive ChatMessage model with the correct constructor AND the essential getChartData() method.
public class ChatMessage {
    public static final int TYPE_SENT = 1;
    public static final int TYPE_RECEIVED = 2;
    public static final int TYPE_GRAPH = 3;

    private final String sender;
    private final String content;
    private final int type;
    private final boolean isGraphical;
    private final Object chartData;
    private final String timestamp;

    // The 6-argument constructor required by AiAnalyzeActivity.
    public ChatMessage(String sender, String content, int type, boolean isGraphical, Object chartData, String timestamp) {
        this.sender = sender;
        this.content = content;
        this.type = type;
        this.isGraphical = isGraphical;
        this.chartData = chartData;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getViewType() {
        // If the message is graphical, it overrides the normal SENT/RECEIVED type.
        if (isGraphical) {
            return TYPE_GRAPH;
        }
        return type;
    }

    /**
     * Required by ChatMessageAdapter to bind the structured analysis data
     * to the chart view when the viewType is TYPE_GRAPH.
     */
    public Object getChartData() {
        return chartData;
    }

    // This method is kept for compatibility but getViewType() is preferred.
    public boolean isUser() {
        return this.type == TYPE_SENT;
    }
}
