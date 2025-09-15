package com.hiteshjangid.lenden;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.hiteshjangid.lenden.databinding.ActivitySummaryBinding;
import com.hiteshjangid.lenden.util.AppSettingsManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;

public class SummaryActivity extends AppCompatActivity {

    private SQLiteDatabase sqLiteDatabase;
    private int selectedMonth;
    private int selectedYear;
    private TableLayout incomeTableLayout;
    private TableLayout expenseTableLayout;
    private ActivitySummaryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivitySummaryBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            initializeDatabase();
            loadSummaryData();
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error initializing summary activity: " + e.getMessage());
            finish();
        }
    }

    private void initializeDatabase() {
        try {
            sqLiteDatabase = AppSettingsManager.getDatabase(this);
            selectedMonth = AppSettingsManager.getSelectedMonthForDatabase(this);
            selectedYear = AppSettingsManager.getSelectedYear(this);
            
            incomeTableLayout = findViewById(R.id.income_table_layout);
            expenseTableLayout = findViewById(R.id.expense_table_layout);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database: " + e.getMessage());
        }
    }

    private void loadSummaryData() {
        try {
            synchronizeBudgetData();
            loadBudgetSummary();
            loadIncomeTable();
            loadExpenseTable();
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error loading summary data: " + e.getMessage());
        }
    }

    private void synchronizeBudgetData() {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return;

        Cursor cursor = null;
        try {
            int actualIncome = calculateActualTotal("incomes");
            int actualExpense = calculateActualTotal("expenses");

            cursor = sqLiteDatabase.rawQuery(
                "SELECT budget_amount FROM BudgetInfo WHERE month = ? AND year = ?",
                new String[]{String.valueOf(selectedMonth), String.valueOf(selectedYear)});
            
            if (cursor != null && cursor.moveToFirst()) {
                int budgetAmount = cursor.getInt(0);
                int currentBudget = budgetAmount + actualIncome - actualExpense;
                int totalSaving = actualIncome - actualExpense;

                sqLiteDatabase.execSQL(
                    "UPDATE BudgetInfo SET current_budget = ?, total_saving = ?, income_money = ?, expense_money = ? WHERE month = ? AND year = ?",
                    new Object[]{currentBudget, totalSaving, actualIncome, actualExpense, selectedMonth, selectedYear});
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error synchronizing budget data: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(cursor);
        }
    }

    private int calculateActualTotal(String tableName) {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return 0;

        Cursor cursor = null;
        try {
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

    private void loadBudgetSummary() {
        try {
            // Fetch budget-related information - all fields are INTEGER in database
            int currentBudget = getCurrentBudget();
            int budgetAmount = getBudgetAmount();
            int targetSaving = getTargetSaving();
            int totalSaving = getTotalSaving();
            int totalIncome = getTotalIncome();
            int totalExpense = getTotalExpense();

            // Set TextViews with summary data using integer values
            binding.currentBudgetText.setText("Current Budget: ₹ " + currentBudget);
            binding.budgetAmountText.setText("Budget Amount: ₹ " + budgetAmount);
            binding.targetSavingText.setText("Target Saving: ₹ " + targetSaving);
            binding.totalSavingText.setText("Total Saving: ₹ " + totalSaving);
            binding.totalIncomeText.setText("Total Income: ₹ " + totalIncome);
            binding.totalExpenseText.setText("Total Expense: ₹ " + totalExpense);
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error loading budget summary: " + e.getMessage());
        }
    }

    private void loadIncomeTable() {
        Cursor incomeCursor = null;
        try {
            incomeCursor = sqLiteDatabase.rawQuery(
                "SELECT type, SUM(amount) as totalAmount, MIN(date) as startDate, MAX(date) as endDate " +
                "FROM incomes WHERE status = 0 AND month = ? AND year = ? GROUP BY type",
                new String[]{String.valueOf(selectedMonth), String.valueOf(selectedYear)});

            if (incomeCursor != null && incomeCursor.moveToFirst()) {
                do {
                    String type = incomeCursor.getString(0);
                    int totalAmount = incomeCursor.getInt(1);
                    String startDate = incomeCursor.getString(2);
                    String endDate = incomeCursor.getString(3);

                    TableRow row = createRow(startDate, endDate, type, totalAmount);
                    incomeTableLayout.addView(row);

                } while (incomeCursor.moveToNext());
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error loading income table: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(incomeCursor);
        }
    }

    private void loadExpenseTable() {
        Cursor expenseCursor = null;
        try {
            expenseCursor = sqLiteDatabase.rawQuery(
                "SELECT category, SUM(amount) as totalAmount, MIN(date) as startDate, MAX(date) as endDate " +
                "FROM expenses WHERE status = 0 AND month = ? AND year = ? GROUP BY category",
                new String[]{String.valueOf(selectedMonth), String.valueOf(selectedYear)});

            if (expenseCursor != null && expenseCursor.moveToFirst()) {
                do {
                    String category = expenseCursor.getString(0);
                    int totalAmount = expenseCursor.getInt(1);
                    String startDate = expenseCursor.getString(2);
                    String endDate = expenseCursor.getString(3);

                    TableRow row = createRow(startDate, endDate, category, totalAmount);
                    expenseTableLayout.addView(row);

                } while (expenseCursor.moveToNext());
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error loading expense table: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(expenseCursor);
        }
    }

    private TableRow createRow(String startDate, String endDate, String typeOrCategory, int totalAmount) {
        try {
            TableRow row = new TableRow(this);

            TextView dateRangeTextView = new TextView(this);
            dateRangeTextView.setText(formatDate(startDate, endDate));
            dateRangeTextView.setPadding(8, 8, 8, 8);
            row.addView(dateRangeTextView);

            TextView typeOrCategoryTextView = new TextView(this);
            typeOrCategoryTextView.setText(typeOrCategory != null ? typeOrCategory : "N/A");
            typeOrCategoryTextView.setPadding(8, 8, 8, 8);
            row.addView(typeOrCategoryTextView);

            TextView amountTextView = new TextView(this);
            amountTextView.setText("₹ " + String.format("%.2f", (double) totalAmount));
            amountTextView.setPadding(8, 8, 8, 8);
            row.addView(amountTextView);

            return row;
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error creating table row: " + e.getMessage());
            return new TableRow(this); // Return empty row on error
        }
    }

    private String formatDate(String startDate, String endDate) {
        try {
            if (startDate == null || endDate == null || startDate.trim().isEmpty() || endDate.trim().isEmpty()) {
                return "N/A";
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            sdf.setLenient(false); // Strict date parsing to catch invalid dates
            
            Date parsedStartDate = sdf.parse(startDate.trim());
            Date parsedEndDate = sdf.parse(endDate.trim());

            if (parsedStartDate == null || parsedEndDate == null) {
                return startDate;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsedStartDate);

            int startDay = calendar.get(Calendar.DAY_OF_MONTH);
            calendar.setTime(parsedEndDate);
            int lastDay = calendar.get(Calendar.DAY_OF_MONTH);

            if (startDay == lastDay) {
                return sdf.format(parsedStartDate);
            } else {
                String monthName = new SimpleDateFormat("MM", Locale.US).format(parsedStartDate);
                return String.format(Locale.US, "%02d-%02d/%s/%04d", startDay, lastDay, monthName, calendar.get(Calendar.YEAR));
            }
        } catch (ParseException e) {
            return startDate != null ? startDate : "N/A";
        } catch (Exception e) {
            return "N/A";
        }
    }

    private int getCurrentBudget() {
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT current_budget FROM BudgetInfo WHERE month = ? AND year = ?",
                    new String[]{String.valueOf(selectedMonth), String.valueOf(selectedYear)});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } catch (Exception e) {
            return 0;
        } finally {
            AppSettingsManager.closeCursor(cursor);
        }
    }

    private int getBudgetAmount() {
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT budget_amount FROM BudgetInfo WHERE month = ? AND year = ?",
                    new String[]{String.valueOf(selectedMonth), String.valueOf(selectedYear)});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } catch (Exception e) {
            return 0;
        } finally {
            AppSettingsManager.closeCursor(cursor);
        }
    }

    private int getTargetSaving() {
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT target_saving FROM BudgetInfo WHERE month = ? AND year = ?",
                    new String[]{String.valueOf(selectedMonth), String.valueOf(selectedYear)});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } catch (Exception e) {
            return 0;
        } finally {
            AppSettingsManager.closeCursor(cursor);
        }
    }

    private int getTotalSaving() {
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT total_saving FROM BudgetInfo WHERE month = ? AND year = ?",
                    new String[]{String.valueOf(selectedMonth), String.valueOf(selectedYear)});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } catch (Exception e) {
            return 0;
        } finally {
            AppSettingsManager.closeCursor(cursor);
        }
    }

    private int getTotalIncome() {
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT SUM(amount) FROM incomes WHERE status = 0 AND month = ? AND year = ?",
                    new String[]{String.valueOf(selectedMonth), String.valueOf(selectedYear)});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } catch (Exception e) {
            return 0;
        } finally {
            AppSettingsManager.closeCursor(cursor);
        }
    }

    private int getTotalExpense() {
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT SUM(amount) FROM expenses WHERE status = 0 AND month = ? AND year = ?",
                    new String[]{String.valueOf(selectedMonth), String.valueOf(selectedYear)});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } catch (Exception e) {
            return 0;
        } finally {
            AppSettingsManager.closeCursor(cursor);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppSettingsManager.closeDatabase(sqLiteDatabase);
    }
}
