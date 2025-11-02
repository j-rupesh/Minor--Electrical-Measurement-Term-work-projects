package com.example.energymeterapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private SwitchMaterial switchDarkMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupToolbar();

        // Initialize Views
        switchDarkMode = findViewById(R.id.switchDarkMode);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Setup Navigation
        setupNavigation(bottomNavigationView);

        // Setup Dark Mode Toggle
        setupDarkMode();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupNavigation(BottomNavigationView bottomNavigationView) {
        bottomNavigationView.setSelectedItemId(R.id.navigation_settings);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.navigation_settings) {
            return true; // Already on this screen
        }
        Intent intent = null;
        if (itemId == R.id.navigation_home) {
            intent = new Intent(this, MainActivity.class);
        } else if (itemId == R.id.navigation_ai_analyze) {
            intent = new Intent(this, AiAnalyzeActivity.class);
        } else if (itemId == R.id.navigation_analysis) {
            intent = new Intent(this, AnalysisActivity.class);
        } else if (itemId == R.id.navigation_all_data) {
            intent = new Intent(this, AllDataActivity.class);
        }

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
        return true;
    }

    private void setupDarkMode() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);

        switchDarkMode.setChecked(isDarkMode);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save the new state
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply();

            // Apply the theme
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }
}
