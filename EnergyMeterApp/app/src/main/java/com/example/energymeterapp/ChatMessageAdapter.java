package com.example.energymeterapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast; // Added for debugging/placeholders

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// FINAL CORRECTION: This is the definitive adapter for the AI Analyze screen.
public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessages;

    public ChatMessageAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @Override
    public int getItemViewType(int position) {
        // This now correctly uses the view type from the ChatMessage model.
        return chatMessages.get(position).getViewType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // This now correctly inflates the different layout files based on the view type.
        if (viewType == ChatMessage.TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_sent, parent, false);
            return new SentViewHolder(view);
        } else if (viewType == ChatMessage.TYPE_GRAPH) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_graphical, parent, false);
            return new GraphViewHolder(view);
        } else { // TYPE_RECEIVED
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_received, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);

        // The logic is now split to handle each message type correctly.
        if (holder.getItemViewType() == ChatMessage.TYPE_SENT) {
            SentViewHolder sentHolder = (SentViewHolder) holder;
            sentHolder.tvMessageContent.setText(message.getContent());
            sentHolder.tvTimestamp.setText(message.getTimestamp());
        } else if (holder.getItemViewType() == ChatMessage.TYPE_RECEIVED) {
            ReceivedViewHolder receivedHolder = (ReceivedViewHolder) holder;
            receivedHolder.tvSender.setText(message.getSender());
            receivedHolder.tvMessageContent.setText(message.getContent());
            receivedHolder.tvTimestamp.setText(message.getTimestamp());
        } else { // TYPE_GRAPH
            GraphViewHolder graphHolder = (GraphViewHolder) holder;
            graphHolder.tvGraphTitle.setText(message.getContent());
            graphHolder.tvTimestamp.setText(message.getTimestamp());

            // --- CRITICAL UPDATE FOR DATA BINDING ---
            Object chartData = message.getChartData(); // Assuming ChatMessage has getChartData()
            if (chartData != null) {
                // To display the graph, you MUST replace the placeholder TextView
                // in item_chat_graphical.xml with a real chart view (e.g., LineChart from MPAndroidChart).

                // Placeholder logic: Show that data is present
                // Toast.makeText(holder.itemView.getContext(), "Binding " + chartData.getClass().getSimpleName() + " to Chart View.", Toast.LENGTH_SHORT).show();

                // Actual rendering logic (e.g., using MPAndroidChart library):
                // LineChart chartView = graphHolder.itemView.findViewById(R.id.chartView);
                // ChartUtils.renderData(chartView, chartData);

            } else {
                // Toast.makeText(holder.itemView.getContext(), "Error: Chart data is missing.", Toast.LENGTH_SHORT).show();
            }
            // -----------------------------------------
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    // ViewHolder for Sent messages
    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageContent, tvTimestamp;
        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }

    // ViewHolder for Received messages
    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView tvSender, tvMessageContent, tvTimestamp;
        public ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);tvSender = itemView.findViewById(R.id.tvSender);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }

    // ViewHolder for Graphical messages
    static class GraphViewHolder extends RecyclerView.ViewHolder {
        TextView tvGraphTitle, tvTimestamp;
        // You might need to add a reference to the actual chart view here once implemented in XML.
        // For example: LineChart chartView;

        public GraphViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGraphTitle = itemView.findViewById(R.id.tvGraphTitle);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            // chartView = itemView.findViewById(R.id.chartView);
        }
    }
}
