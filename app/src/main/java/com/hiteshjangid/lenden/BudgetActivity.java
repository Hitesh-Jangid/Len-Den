package com.hiteshjangid.lenden;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import com.hiteshjangid.lenden.databinding.ActivityBudgetBinding;
import com.hiteshjangid.lenden.util.AppSettingsManager;

public class BudgetActivity extends AppCompatActivity {
    private SQLiteDatabase sqLiteDatabase;
    private int selectedMonth, selectedYear;
    private String monthName;
    private Context context = this;
    private ActivityBudgetBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBudgetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeData();
        setupDatabase();
        setupClickListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppSettingsManager.closeDatabase(sqLiteDatabase);
    }

    private void initializeData() {
        selectedMonth = AppSettingsManager.getSelectedMonthForDatabase(this);
        selectedYear = AppSettingsManager.getSelectedYear(this);
        monthName = AppSettingsManager.getSelectedMonthName(this);
    }

    private void setupDatabase() {
        try {
            sqLiteDatabase = AppSettingsManager.getDatabase(this);
            createBudgetTable();
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Database initialization failed: " + e.getMessage());
            finish();
        }
    }

    private void createBudgetTable() {
        if (sqLiteDatabase == null) return;

        String createTable = "CREATE TABLE IF NOT EXISTS BudgetInfo (" +
                "entry_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "month INTEGER NOT NULL," +
                "year INTEGER NOT NULL," +
                "budget_amount INTEGER DEFAULT 0," +
                "target_saving INTEGER DEFAULT 0," +
                "current_budget INTEGER DEFAULT 0," +
                "total_saving INTEGER DEFAULT 0," +
                "income_money INTEGER DEFAULT 0," +
                "expense_money INTEGER DEFAULT 0," +
                "UNIQUE(month, year) ON CONFLICT REPLACE);";

        try {
            sqLiteDatabase.execSQL(createTable);
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Failed to create budget table: " + e.getMessage());
        }
    }

    private void setupClickListeners() {
        binding.createBudget.setOnClickListener(v -> handleBudgetAction(true));
        binding.updateBudget.setOnClickListener(v -> handleBudgetAction(false));
        binding.details.setOnClickListener(v -> showBudgetDetails());
    }

    private void handleBudgetAction(boolean isCreate) {
        if (!isBudgetValidForAction(isCreate)) {
            return;
        }
        showBudgetInputDialog(isCreate ? "Create Budget & Savings Target" : "Update Budget & Savings Target", isCreate ? "Create" : "Update");
    }

    private boolean isBudgetValidForAction(boolean isCreate) {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return false;

        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT budget_amount, target_saving FROM BudgetInfo WHERE month = ? AND year = ?",
                    new String[]{String.valueOf(selectedMonth), String.valueOf(selectedYear)});
            boolean hasBudget = false;
            boolean hasTarget = false;

            if (cursor.moveToFirst()) {
                hasBudget = cursor.getInt(0) > 0;
                hasTarget = cursor.getInt(1) > 0;
            }

            if (isCreate && hasBudget && hasTarget) {
                AppSettingsManager.showToast(this, "Budget & Savings Target already created for " + monthName + " " + selectedYear);
                return false;
            } else if (!isCreate && !hasBudget) {
                AppSettingsManager.showToast(this, "No Budget & Saving Target created for " + monthName + " " + selectedYear + ". Create first.");
                return false;
            } else if (!isCreate && !hasTarget) {
                return true;
            }
            return true;
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error checking budget status: " + e.getMessage());
            return false;
        } finally {
            AppSettingsManager.closeCursor(cursor);
        }
    }

    private void showBudgetInputDialog(String title, String positiveButtonLabel) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_set_update_budget, null);
        EditText budgetAmountInput = dialogView.findViewById(R.id.updated_budget_input);
        EditText targetSavingInput = dialogView.findViewById(R.id.updated_target_input);

        loadExistingBudgetData(budgetAmountInput, targetSavingInput);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(dialogView)
                .create();

        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        Button updateButton = dialogView.findViewById(R.id.update_button);

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        updateButton.setOnClickListener(v -> {
            if (validateAndSaveBudget(budgetAmountInput, targetSavingInput)) {
                updateCurrentBudgetAndSaving();
                AppSettingsManager.showToast(this, "Budget " + positiveButtonLabel.toLowerCase() + "d successfully for " + monthName + " " + selectedYear);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void loadExistingBudgetData(EditText budgetInput, EditText targetInput) {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return;

        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT budget_amount, target_saving FROM BudgetInfo WHERE month = ? AND year = ?",
                    new String[]{String.valueOf(selectedMonth), String.valueOf(selectedYear)});
            if (cursor.moveToFirst()) {
                budgetInput.setText(String.valueOf(cursor.getInt(0)));
                targetInput.setText(String.valueOf(cursor.getInt(1)));
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error loading existing budget: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(cursor);
        }
    }

    private boolean validateAndSaveBudget(EditText budgetInput, EditText targetInput) {
        String budgetText = budgetInput.getText().toString().trim();
        String targetText = targetInput.getText().toString().trim();

        if (TextUtils.isEmpty(budgetText)) {
            AppSettingsManager.showToast(this, "Please enter a budget amount");
            return false;
        }
        if (TextUtils.isEmpty(targetText)) {
            AppSettingsManager.showToast(this, "Please enter a target saving amount");
            return false;
        }

        int budgetAmount, targetSaving;
        try {
            budgetAmount = Integer.parseInt(budgetText);
            targetSaving = Integer.parseInt(targetText);
        } catch (NumberFormatException e) {
            AppSettingsManager.showToast(this, "Please enter valid numbers");
            return false;
        }

        if (budgetAmount <= 0) {
            AppSettingsManager.showToast(this, "Budget amount must be greater than 0");
            return false;
        }
        if (targetSaving < 0) {
            AppSettingsManager.showToast(this, "Target saving cannot be negative");
            return false;
        }
        if (targetSaving > budgetAmount) {
            AppSettingsManager.showToast(this, "Target saving cannot exceed budget amount");
            return false;
        }

        return saveBudgetToDatabase(budgetAmount, targetSaving);
    }

    private boolean saveBudgetToDatabase(int budgetAmount, int targetSaving) {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return false;

        return AppSettingsManager.executeInTransactionWithCallback(sqLiteDatabase, () -> {
            ContentValues values = new ContentValues();
            values.put("month", selectedMonth);
            values.put("year", selectedYear);
            values.put("budget_amount", budgetAmount);
            values.put("target_saving", targetSaving);
            values.put("current_budget", budgetAmount);
            values.put("total_saving", 0);
            values.put("income_money", 0);
            values.put("expense_money", 0);

            // Check if entry exists
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT COUNT(*) FROM BudgetInfo WHERE month = ? AND year = ?",
                    new String[]{String.valueOf(selectedMonth), String.valueOf(selectedYear)});
            cursor.moveToFirst();
            boolean exists = cursor.getInt(0) > 0;
            cursor.close();

            int result;
            if (exists) {
                result = sqLiteDatabase.update("BudgetInfo", values, "month = ? AND year = ?",
                        new String[]{String.valueOf(selectedMonth), String.valueOf(selectedYear)});
            } else {
                result = (int) sqLiteDatabase.insert("BudgetInfo", null, values);
            }
            
            if (result <= 0) {
                throw new RuntimeException("Failed to save budget");
            }
        }, this, "Error saving budget");
    }

    private void showBudgetDetails() {
        if (!hasBudgetData()) {
            AppSettingsManager.showToast(this, "No Budget & Saving Target created. Create first.");
            return;
        }

        updateCurrentBudgetAndSaving();
        displayBudgetInformation();
    }

    private boolean hasBudgetData() {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return false;

        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT COUNT(*) FROM BudgetInfo WHERE month = ? AND year = ? AND budget_amount > 0",
                    new String[]{String.valueOf(selectedMonth), String.valueOf(selectedYear)});
            cursor.moveToFirst();
            return cursor.getInt(0) > 0;
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error checking budget data: " + e.getMessage());
            return false;
        } finally {
            AppSettingsManager.closeCursor(cursor);
        }
    }

    @SuppressLint("Range")
    private void displayBudgetInformation() {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return;

        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT year, month, current_budget, budget_amount, target_saving, total_saving, income_money, expense_money FROM BudgetInfo WHERE month = ? AND year = ?",
                    new String[]{String.valueOf(selectedMonth), String.valueOf(selectedYear)});

            if (cursor.moveToFirst()) {
                // Validate all column indices to prevent crashes
                int yearIndex = cursor.getColumnIndex("year");
                int monthIndex = cursor.getColumnIndex("month");
                int currentBudgetIndex = cursor.getColumnIndex("current_budget");
                int budgetAmountIndex = cursor.getColumnIndex("budget_amount");
                int targetSavingIndex = cursor.getColumnIndex("target_saving");
                int totalSavingIndex = cursor.getColumnIndex("total_saving");
                int incomeMoneyIndex = cursor.getColumnIndex("income_money");
                int expenseMoneyIndex = cursor.getColumnIndex("expense_money");
                
                if (yearIndex == -1 || monthIndex == -1 || currentBudgetIndex == -1 || 
                    budgetAmountIndex == -1 || targetSavingIndex == -1 || 
                    totalSavingIndex == -1 || incomeMoneyIndex == -1 || expenseMoneyIndex == -1) {
                    return; // Column not found, abort to prevent crash
                }
                
                int year = cursor.getInt(yearIndex);
                int month = cursor.getInt(monthIndex);
                double currentBudget = cursor.getDouble(currentBudgetIndex);
                double monthlyBudget = cursor.getDouble(budgetAmountIndex);
                double targetSaving = cursor.getDouble(targetSavingIndex);
                double totalSaving = cursor.getDouble(totalSavingIndex);
                double incomeMoney = cursor.getDouble(incomeMoneyIndex);
                double expenseMoney = cursor.getDouble(expenseMoneyIndex);

                int textColor = (currentBudget <= 0) ? Color.RED : (totalSaving >= targetSaving) ? Color.GREEN : Color.RED;

                String message = "<b>Current Budget:</b> " + currentBudget + " ₹<br><br>" +
                        "<b>Monthly Budget:</b> " + monthlyBudget + " ₹<br><br>" +
                        "<b>Target Saving:</b> " + targetSaving + " ₹<br><br>" +
                        "<b>Current Saving:</b> <font color='" + textColor + "'>" + totalSaving + " ₹ </font><br><br>" +
                        "<b>Income Money:</b> " + incomeMoney + " ₹<br><br>" +
                        "<b>Total Expenses:</b> " + expenseMoney + " ₹";

                new AlertDialog.Builder(this)
                        .setTitle("Budget Information : " + monthName + " " + selectedYear)
                        .setMessage(HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY))
                        .setPositiveButton("OK", null)
                        .show();
            } else {
                AppSettingsManager.showToast(this, "No information available for the current month.");
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error displaying budget information: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(cursor);
        }
    }

    private void updateCurrentBudgetAndSaving() {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return;

        // Get actual income and expense totals from their respective tables
        int totalIncome = getTotalIncomeFromDatabase();
        int totalExpense = getTotalExpenseFromDatabase();
        
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT budget_amount, target_saving FROM BudgetInfo WHERE month = ? AND year = ?",
                    new String[]{String.valueOf(selectedMonth), String.valueOf(selectedYear)});
            if (cursor.moveToFirst()) {
                int budgetAmount = cursor.getInt(0);
                
                // Correct calculation: current_budget = initial_budget + income - expenses
                int currentBudget = budgetAmount + totalIncome - totalExpense;
                // Savings = income - expenses (can be negative if overspent)
                int totalSaving = totalIncome - totalExpense;

                ContentValues values = new ContentValues();
                values.put("current_budget", currentBudget);
                values.put("total_saving", totalSaving);
                values.put("income_money", totalIncome);
                values.put("expense_money", totalExpense);
                
                sqLiteDatabase.update("BudgetInfo", values, "month = ? AND year = ?",
                        new String[]{String.valueOf(selectedMonth), String.valueOf(selectedYear)});
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error updating budget: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(cursor);
        }
    }

    private int getTotalIncomeFromDatabase() {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return 0;
        
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery(
                "SELECT SUM(CAST(amount AS INTEGER)) FROM incomes WHERE month = ? AND year = ? AND status = 0",
                new String[]{String.valueOf(selectedMonth), String.valueOf(selectedYear)});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error calculating total income: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(cursor);
        }
        return 0;
    }

    private int getTotalExpenseFromDatabase() {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return 0;
        
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery(
                "SELECT SUM(CAST(amount AS INTEGER)) FROM expenses WHERE month = ? AND year = ? AND status = 0",
                new String[]{String.valueOf(selectedMonth), String.valueOf(selectedYear)});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error calculating total expense: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(cursor);
        }
        return 0;
    }

    private void showToast(String message) {
        AppSettingsManager.showToast(context, message);
    }
}