package com.example.energymeterapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DataEntryAdapter extends RecyclerView.Adapter<DataEntryAdapter.DataEntryViewHolder> {

    private final List<DataEntry> dataEntries;

    public DataEntryAdapter(List<DataEntry> dataEntries) {
        this.dataEntries = dataEntries;
    }

    @NonNull
    @Override
    public DataEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data_entry, parent, false);
        return new DataEntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataEntryViewHolder holder, int position) {
        DataEntry entry = dataEntries.get(position);

        if (entry != null) {
            holder.tvTimestamp.setText(entry.getTimestamp() != null ? entry.getTimestamp() : "N/A");
            holder.tvVoltage.setText(entry.getVoltage() != null ? entry.getVoltage() : "N/A");
            holder.tvCurrent.setText(entry.getCurrent() != null ? entry.getCurrent() : "N/A");
            holder.tvPower.setText(entry.getPower() != null ? entry.getPower() : "N/A");
            holder.tvPF.setText(entry.getPowerFactor() != null ? entry.getPowerFactor() : "N/A");
            holder.tvEnergy.setText(entry.getEnergy() != null ? entry.getEnergy() : "N/A");
            holder.tvFrequency.setText(entry.getFrequency() != null ? entry.getFrequency() : "N/A");
        }
    }

    @Override
    public int getItemCount() {
        return dataEntries != null ? dataEntries.size() : 0;
    }

    public static class DataEntryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTimestamp, tvVoltage, tvCurrent, tvPower, tvPF, tvEnergy, tvFrequency;

        public DataEntryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvVoltage = itemView.findViewById(R.id.tvVoltage);
            tvCurrent = itemView.findViewById(R.id.tvCurrent);
            tvPower = itemView.findViewById(R.id.tvPower);
            tvPF = itemView.findViewById(R.id.tvPF);
            tvEnergy = itemView.findViewById(R.id.tvEnergy);
            tvFrequency = itemView.findViewById(R.id.tvFrequency);
        }
    }
}
