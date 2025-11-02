package com.example.energymeterapp;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Room Database definition for the Energy Meter Application.
 * Follows the Singleton pattern to prevent multiple database instances.
 */
@Database(entities = {EnergyData.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract EnergyDataDao energyDataDao();

    private static volatile AppDatabase INSTANCE;
    private static final String DATABASE_NAME = "energy_db";

    /**
     * Returns the singleton instance of the database.
     * Thread-safe, lazy initialization.
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DATABASE_NAME)
                            // Useful during development: auto-destroy DB on schema change
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
