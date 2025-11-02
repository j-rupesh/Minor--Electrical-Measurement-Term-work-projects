package com.example.energymeterapp;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "EnergyData")
public class EnergyData {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "voltage")
    private float voltage = 0f;

    @ColumnInfo(name = "current")
    private float current = 0f;

    @ColumnInfo(name = "power")
    private float power = 0f;

    @ColumnInfo(name = "energy")
    private float energy = 0f;

    @ColumnInfo(name = "pf")
    private float pf = 0f;

    @ColumnInfo(name = "timestamp")
    private long timestamp = System.currentTimeMillis();

    // --- Constructors ---
    public EnergyData() {}

    public EnergyData(float voltage, float current, float power, float energy, float pf, long timestamp) {
        this.voltage = voltage;
        this.current = current;
        this.power = power;
        this.energy = energy;
        this.pf = pf;
        this.timestamp = timestamp;
    }

    // --- Getters & Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public float getVoltage() { return voltage; }
    public void setVoltage(float voltage) { this.voltage = voltage; }

    public float getCurrent() { return current; }
    public void setCurrent(float current) { this.current = current; }

    public float getPower() { return power; }
    public void setPower(float power) { this.power = power; }

    public float getEnergy() { return energy; }
    public void setEnergy(float energy) { this.energy = energy; }

    public float getPf() { return pf; }
    public void setPf(float pf) { this.pf = pf; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    // --- Debugging ---
    @NonNull
    @Override
    public String toString() {
        return "EnergyData{" +
                "id=" + id +
                ", voltage=" + voltage +
                ", current=" + current +
                ", power=" + power +
                ", energy=" + energy +
                ", pf=" + pf +
                ", timestamp=" + timestamp +
                '}';
    }
}
