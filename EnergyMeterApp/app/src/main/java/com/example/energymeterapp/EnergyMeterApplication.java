package com.example.energymeterapp;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

// This class is run once when the application starts.
public class EnergyMeterApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Get the app's shared preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get the saved dark mode setting, defaulting to 'off' (false)
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);

        // Apply the theme based on the saved setting
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
