package com.hiteshjangid.lenden;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.hiteshjangid.lenden.databinding.ActivityMainBinding;
import com.hiteshjangid.lenden.util.AppSettingsManager;

public class MainActivity extends AppCompatActivity {

    private Context context = this;
    private SQLiteDatabase sqLiteDatabase;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeDatabase();
        setupUI();
        setupNavigationListeners();
        setupBackPressedHandler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBudgetDisplay();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppSettingsManager.closeDatabase(sqLiteDatabase);
    }

    private void initializeDatabase() {
        try {
            sqLiteDatabase = AppSettingsManager.getDatabase(this);
            createTablesIfNotExist();
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Failed to initialize database: " + e.getMessage());
            finish();
        }
    }

    private void createTablesIfNotExist() {
        if (sqLiteDatabase == null) return;

        String createBudgetTable = "CREATE TABLE IF NOT EXISTS BudgetInfo (" +
                "budget_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "month INTEGER NOT NULL, " +
                "year INTEGER NOT NULL, " +
                "budget_amount INTEGER DEFAULT 0, " +
                "target_saving INTEGER DEFAULT 0, " +
                "current_budget INTEGER DEFAULT 0, " +
                "total_saving INTEGER DEFAULT 0, " +
                "income_money INTEGER DEFAULT 0, " +
                "expense_money INTEGER DEFAULT 0, " +
                "UNIQUE(month, year) ON CONFLICT REPLACE);";

        try {
            sqLiteDatabase.execSQL(createBudgetTable);
            
            // Create performance indexes for frequently queried columns
            sqLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS idx_expenses_month_year_status ON expenses(month, year, status)");
            sqLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS idx_incomes_month_year_status ON incomes(month, year, status)");
            sqLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS idx_budget_month_year ON BudgetInfo(month, year)");
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Failed to create database tables: " + e.getMessage());
        }
    }

    private void setupUI() {
        updateBudgetDisplay();
    }

    private void updateBudgetDisplay() {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return;

        int selectedMonth = AppSettingsManager.getSelectedMonthForDatabase(this);
        int selectedYear = AppSettingsManager.getSelectedYear(this);
        String monthName = AppSettingsManager.getSelectedMonthName(this);

        synchronizeBudgetData(selectedMonth, selectedYear);

        String baseText = getString(R.string.available_budget);
        String headerText = baseText + " (" + monthName + " - " + selectedYear + ") :";

        int currentBudget = getCurrentBudget(selectedMonth, selectedYear);
        String displayText = headerText + " " + currentBudget + " ₹";
        binding.availableBudget.setText(displayText);
    }

    private void synchronizeBudgetData(int month, int year) {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return;

        try {
            int totalIncome = calculateActualTotal("incomes");
            int totalExpenses = calculateActualTotal("expenses");

            Cursor cursor = sqLiteDatabase.rawQuery(
                "SELECT budget_amount FROM BudgetInfo WHERE month = ? AND year = ?",
                new String[]{String.valueOf(month), String.valueOf(year)});
            
            if (cursor.moveToFirst()) {
                int budgetAmount = cursor.getInt(0);
                                // CONSISTENT FORMULA: current_budget = budget_amount + income - expense
                // This represents available budget including accumulated income
                int currentBudget = budgetAmount + totalIncome - totalExpenses;
                int totalSaving = totalIncome - totalExpenses;

                ContentValues values = new ContentValues();
                values.put("current_budget", currentBudget);
                values.put("total_saving", totalSaving);
                values.put("income_money", totalIncome);
                values.put("expense_money", totalExpenses);

                sqLiteDatabase.update("BudgetInfo", values, "month = ? AND year = ?",
                        new String[]{String.valueOf(month), String.valueOf(year)});
            }
            cursor.close();
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error synchronizing budget data: " + e.getMessage());
        }
    }
    
    private int calculateActualTotal(String tableName) {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return 0;

        Cursor cursor = null;
        try {
            int selectedMonth = AppSettingsManager.getSelectedMonthForDatabase(this);
            int selectedYear = AppSettingsManager.getSelectedYear(this);
            cursor = sqLiteDatabase.rawQuery(
                "SELECT SUM(CAST(amount AS INTEGER)) FROM " + tableName + " WHERE month = ? AND year = ? AND status = 0",
                new String[]{String.valueOf(selectedMonth), String.valueOf(selectedYear)});
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error calculating total from " + tableName + ": " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(cursor);
        }
        return 0;
    }

    private int getTotalFromTable(String tableName, int month, int year) {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return 0;

        // Whitelist allowed table names to prevent SQL injection
        if (!tableName.equals("expenses") && !tableName.equals("incomes")) {
            AppSettingsManager.showToast(this, "Invalid table name");
            return 0;
        }

        Cursor cursor = null;
        try {
            // Safe: tableName is now validated against whitelist
            cursor = sqLiteDatabase.rawQuery(
                "SELECT SUM(CAST(amount AS INTEGER)) FROM " + tableName + " WHERE month = ? AND year = ? AND status = 0",
                new String[]{String.valueOf(month), String.valueOf(year)});
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error calculating total from " + tableName + ": " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(cursor);
        }
        return 0;
    }

    private int getCurrentBudget(int month, int year) {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return 0;

        int currentBudget = 0;
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery(
                "SELECT current_budget FROM BudgetInfo WHERE month = ? AND year = ?",
                new String[]{String.valueOf(month), String.valueOf(year)}
            );
            if (cursor.moveToFirst()) {
                currentBudget = cursor.getInt(0);
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error getting budget: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(cursor);
        }
        return currentBudget;
    }

    private void setupNavigationListeners() {
        // Summary card
        binding.summaryCard.setOnClickListener(v -> navigateWithAnimation(v, SummaryActivity.class));

        // Navigation cards
        binding.budgetNavCard.setOnClickListener(v -> navigateWithAnimation(v, BudgetActivity.class));
        binding.resetNavCard.setOnClickListener(v -> navigateWithAnimation(v, ResetActivity.class));
        binding.expensesNavCard.setOnClickListener(v -> navigateWithAnimation(v, ExpenseActivity.class));
        binding.incomeNavCard.setOnClickListener(v -> navigateWithAnimation(v, IncomeActivity.class));
        binding.reportsNavCard.setOnClickListener(v -> navigateWithAnimation(v, ReportsActivity.class));
        binding.incomeReportsNavCard.setOnClickListener(v -> navigateWithAnimation(v, ReportsIncomeActivity.class));
        binding.settingsButton.setOnClickListener(v -> navigateWithAnimation(v, AppSettingsActivity.class));
    }

    private void navigateWithAnimation(@NonNull View view, @NonNull Class<?> targetActivity) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction(() -> {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start();
                startActivity(new Intent(getApplicationContext(), targetActivity));
            })
            .start();
    }

    private void setupBackPressedHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Exit")
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dialog.dismiss();
                        finish(); // Properly exit the app
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                        // Stay in the app
                    })
                    .setOnCancelListener(dialog -> {
                        // Stay in the app
                    })
                    .setCancelable(true)
                    .show();
            }
        });
    }
}
