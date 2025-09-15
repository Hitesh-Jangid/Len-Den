package com.hiteshjangid.lenden;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hiteshjangid.lenden.databinding.ActivityAppSettingsBinding;
import com.hiteshjangid.lenden.util.AppSettingsManager;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AppSettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private AutoCompleteTextView monthSpinner;
    private AutoCompleteTextView yearSpinner;
    private List<String> formattedMonthNames;
    private List<String> years;
    private ActivityAppSettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityAppSettingsBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            initializeSettings();
            setupSpinners();
            loadCurrentSettings();
            setupClickListeners();
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error initializing settings activity: " + e.getMessage());
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear references to prevent memory leaks
        if (binding != null) {
            binding = null;
        }
        if (monthSpinner != null) {
            monthSpinner.setAdapter(null);
            monthSpinner = null;
        }
        if (yearSpinner != null) {
            yearSpinner.setAdapter(null);
            yearSpinner = null;
        }
        if (formattedMonthNames != null) {
            formattedMonthNames.clear();
            formattedMonthNames = null;
        }
        if (years != null) {
            years.clear();
            years = null;
        }
        sharedPreferences = null;
    }

    private void initializeSpinners() {
        try {
            formattedMonthNames = generateFormattedMonthNames();
            ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, formattedMonthNames);
            monthSpinner.setAdapter(monthAdapter);
            monthSpinner.setThreshold(1); // Show dropdown immediately when clicked

            years = generateYearList();
            ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, years);
            yearSpinner.setAdapter(yearAdapter);
            yearSpinner.setThreshold(1); // Show dropdown immediately when clicked
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error setting up spinners: " + e.getMessage());
        }
    }

    private List<String> generateFormattedMonthNames() {
        List<String> formattedMonthNames = new ArrayList<>();
        DateFormatSymbols symbols = new DateFormatSymbols();
        String[] monthNames = symbols.getMonths();
        for (int month = 0; month < 12; month++) {
            String formattedMonthName = String.format("%02d %s", month + 1, monthNames[month]);
            formattedMonthNames.add(formattedMonthName);
        }
        return formattedMonthNames;
    }

    private List<String> generateYearList() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        for (int year = currentYear - 10; year <= currentYear + 10; year++) {
            years.add(String.valueOf(year));
        }
        return years;
    }

    private void loadSelectedMonthAndYear() {
        try {
            int selectedMonth = sharedPreferences.getInt("selectedMonth", Calendar.getInstance().get(Calendar.MONTH));
            int selectedYear = sharedPreferences.getInt("selectedYear", Calendar.getInstance().get(Calendar.YEAR));

            // Validate month index
            if (selectedMonth >= 0 && selectedMonth < formattedMonthNames.size()) {
                monthSpinner.setText(formattedMonthNames.get(selectedMonth));
            } else {
                // Default to current month
                int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
                if (currentMonth >= 0 && currentMonth < formattedMonthNames.size()) {
                    monthSpinner.setText(formattedMonthNames.get(currentMonth));
                }
            }

            String yearString = String.valueOf(selectedYear);
            if (years.contains(yearString)) {
                yearSpinner.setText(yearString);
            } else {
                // Default to current year
                yearSpinner.setText(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error loading current settings: " + e.getMessage());
        }
    }

    private void saveSelectedMonthAndYear() {
        try {
            String selectedMonthText = monthSpinner.getText().toString().trim();
            String selectedYearText = yearSpinner.getText().toString().trim();

            // Enhanced validation with better error messages
            if (TextUtils.isEmpty(selectedMonthText)) {
                AppSettingsManager.showToast(this, "Please select a month from the dropdown");
                return;
            }

            if (TextUtils.isEmpty(selectedYearText)) {
                AppSettingsManager.showToast(this, "Please select a year from the dropdown");
                return;
            }

            int selectedMonth = formattedMonthNames.indexOf(selectedMonthText);
            if (selectedMonth == -1) {
                AppSettingsManager.showToast(this, "Invalid month selection. Please choose from the dropdown.");
                return;
            }

            int selectedYear;
            try {
                selectedYear = Integer.parseInt(selectedYearText);
                // Enhanced year validation
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                if (selectedYear < currentYear - 10 || selectedYear > currentYear + 10) {
                    AppSettingsManager.showToast(this, "Year must be between " + (currentYear - 10) + " and " + (currentYear + 10));
                    return;
                }
            } catch (NumberFormatException e) {
                AppSettingsManager.showToast(this, "Invalid year format. Please select from dropdown.");
                return;
            }

            String selectedMonthName = getMonthName(selectedMonth);

            // Validate month name before saving
            if (selectedMonthName.equals("January")) {
                // This is our fallback, ensure it's actually what we want
                DateFormatSymbols symbols = new DateFormatSymbols();
                String[] monthNames = symbols.getMonths();
                if (selectedMonth >= 0 && selectedMonth < monthNames.length) {
                    selectedMonthName = monthNames[selectedMonth];
                }
            }

            // Save settings with transaction-like behavior
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("selectedMonth", selectedMonth);
            editor.putInt("selectedYear", selectedYear);
            editor.putString("selectedMonthName", selectedMonthName);
            
            if (editor.commit()) { // Use commit for immediate write and error checking
                AppSettingsManager.showToast(this, "Settings saved successfully for " + selectedMonthName + " " + selectedYear);
                finish(); // Close activity after successful save
            } else {
                AppSettingsManager.showToast(this, "Failed to save settings. Please try again.");
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error saving settings: " + e.getMessage());
        }
    }

    private String getMonthName(int month) {
        try {
            DateFormatSymbols symbols = new DateFormatSymbols();
            String[] monthNames = symbols.getMonths();
            if (month >= 0 && month < monthNames.length) {
                return monthNames[month];
            }
            return "January"; // Default fallback
        } catch (Exception e) {
            return "January"; // Default fallback
        }
    }

    private void initializeSettings() {
        try {
            sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
            monthSpinner = binding.monthSpinner;
            yearSpinner = binding.yearSpinner;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize settings: " + e.getMessage());
        }
    }

    private void setupSpinners() {
        try {
            formattedMonthNames = generateFormattedMonthNames();
            ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, formattedMonthNames);
            monthSpinner.setAdapter(monthAdapter);
            monthSpinner.setThreshold(1); // Show dropdown immediately when clicked

            years = generateYearList();
            ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, years);
            yearSpinner.setAdapter(yearAdapter);
            yearSpinner.setThreshold(1); // Show dropdown immediately when clicked
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error setting up spinners: " + e.getMessage());
        }
    }

    private void loadCurrentSettings() {
        try {
            int selectedMonth = sharedPreferences.getInt("selectedMonth", Calendar.getInstance().get(Calendar.MONTH));
            int selectedYear = sharedPreferences.getInt("selectedYear", Calendar.getInstance().get(Calendar.YEAR));

            // Validate month index
            if (selectedMonth >= 0 && selectedMonth < formattedMonthNames.size()) {
                monthSpinner.setText(formattedMonthNames.get(selectedMonth));
            } else {
                // Default to current month
                int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
                if (currentMonth >= 0 && currentMonth < formattedMonthNames.size()) {
                    monthSpinner.setText(formattedMonthNames.get(currentMonth));
                }
            }

            String yearString = String.valueOf(selectedYear);
            if (years.contains(yearString)) {
                yearSpinner.setText(yearString);
            } else {
                // Default to current year
                yearSpinner.setText(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error loading current settings: " + e.getMessage());
        }
    }

    private void setupClickListeners() {
        try {
            binding.applySettingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveSelectedMonthAndYear();
                }
            });
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error setting up click listeners: " + e.getMessage());
        }
    }
}
