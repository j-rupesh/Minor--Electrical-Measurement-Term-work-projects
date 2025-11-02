package com.example.energymeterapp;

// FINAL CORRECTION: This class now matches the 7 fields of data used everywhere.
public class DataEntry {
    private final String timestamp;
    private final String voltage;
    private final String current;
    private final String power;
    private final String powerFactor;
    private final String energy;
    private final String frequency;

    // Constructor that accepts all 7 data fields.
    public DataEntry(String timestamp, String voltage, String current, String power, String powerFactor, String energy, String frequency) {
        this.timestamp = timestamp;
        this.voltage = voltage;
        this.current = current;
        this.power = power;
        this.powerFactor = powerFactor;
        this.energy = energy;
        this.frequency = frequency;
    }

    // Getter methods for all 7 fields.
    public String getTimestamp() {
        return timestamp;
    }

    public String getVoltage() {
        return voltage;
    }

    public String getCurrent() {
        return current;
    }

    public String getPower() {
        return power;
    }

    public String getPowerFactor() {
        return powerFactor;
    }

    public String getEnergy() {
        return energy;
    }

    public String getFrequency() {
        return frequency;
    }
}
