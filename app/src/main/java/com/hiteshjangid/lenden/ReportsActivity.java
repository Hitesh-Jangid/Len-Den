package com.hiteshjangid.lenden;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.hiteshjangid.lenden.databinding.ActivityReportsBinding;
import com.hiteshjangid.lenden.util.AppSettingsManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;

public class ReportsActivity extends AppCompatActivity {
    private Context context = this;
    private LinearLayout tableLayout;
    private SQLiteDatabase sqLiteDatabase;
    private int selectedmonth, selectedyear;
    private ActivityReportsBinding binding;
    private HashSet<Integer> deletedRowIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeData();
        setupDatabase();
        setupUI();
        loadExpenseData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppSettingsManager.closeDatabase(sqLiteDatabase);
    }

    private void initializeData() {
        selectedmonth = AppSettingsManager.getSelectedMonthForDatabase(this);
        selectedyear = AppSettingsManager.getSelectedYear(this);
    }

    private void setupDatabase() {
        try {
            sqLiteDatabase = AppSettingsManager.getDatabase(this);
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Database initialization failed: " + e.getMessage());
            finish();
        }
    }

    private void setupUI() {
        tableLayout = binding.reportTableLayout;
    }

    private void loadExpenseData() {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return;

        Cursor expenseCursor = null;
        try {
            expenseCursor = sqLiteDatabase.rawQuery("SELECT e_id, date, category, amount, note, status FROM expenses WHERE month = ? AND year = ?",
                    new String[]{String.valueOf(selectedmonth), String.valueOf(selectedyear)});
            if (expenseCursor.moveToFirst()) {
                do {
                    populateDataRow(expenseCursor);
                } while (expenseCursor.moveToNext());
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error loading expense data: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(expenseCursor);
        }
    }

    private void populateDataRow(@NonNull Cursor cursor) {
        try {
            TableRow row = createTableRow();
            
            int idIndex = cursor.getColumnIndex("e_id");
            int dateIndex = cursor.getColumnIndex("date");
            int categoryIndex = cursor.getColumnIndex("category");
            int amountIndex = cursor.getColumnIndex("amount");
            int noteIndex = cursor.getColumnIndex("note");
            int statusIndex = cursor.getColumnIndex("status");
            
            if (idIndex == -1 || dateIndex == -1 || categoryIndex == -1 || 
                amountIndex == -1 || noteIndex == -1 || statusIndex == -1) {
                return;
            }
            
            @SuppressLint("Range") int id = cursor.getInt(idIndex);
            @SuppressLint("Range") String date = cursor.getString(dateIndex);
            @SuppressLint("Range") String category = cursor.getString(categoryIndex);
            @SuppressLint("Range") int amount = cursor.getInt(amountIndex);
            @SuppressLint("Range") String note = cursor.getString(noteIndex);
            @SuppressLint("Range") int status = cursor.getInt(statusIndex);

            TextView idTextView = new TextView(this);
            TextView dateTextView = new TextView(this);
            TextView categoryTextView = new TextView(this);
            TextView amountTextView = new TextView(this);
            TextView noteTextView = new TextView(this);
            TextView statusTextView = new TextView(this);
            
            idTextView.setText(String.valueOf(id));
            dateTextView.setText(date);
            categoryTextView.setText(category);
            amountTextView.setText(String.valueOf(amount));
            noteTextView.setText(note);
            statusTextView.setText(String.valueOf(status));

            TextView[] textViews = {idTextView, dateTextView, categoryTextView, amountTextView, noteTextView, statusTextView};

            for (TextView textView : textViews) {
                TableRow.LayoutParams layoutParams = createLayoutParams();
                textView.setLayoutParams(layoutParams);
            }

            row.addView(idTextView);
            row.addView(dateTextView);
            row.addView(categoryTextView);
            row.addView(amountTextView);
            row.addView(noteTextView);
            row.addView(statusTextView);

            row.setOnLongClickListener(v -> {
                showOptionsDialog(row, id, status);
                return true;
            });

            if (status == 1) {
                markRowAsDeleted(row);
            }

            tableLayout.addView(row);
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error populating expense data: " + e.getMessage());
        }
    }

    private TableRow.LayoutParams createLayoutParams() {
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        layoutParams.weight = 1;
        layoutParams.gravity = Gravity.START;
        layoutParams.setMargins(8, 8, 8, 8);
        return layoutParams;
    }

    private TextView createTextView(String text, boolean isHeader) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setPadding(5, 5, 5, 5);
        textView.setGravity(isHeader ? Gravity.CENTER : Gravity.START);
        textView.setTextSize(12);

        if (isHeader) {
            textView.setTypeface(null, Typeface.BOLD);
        }

        return textView;
    }

    private TableRow createTableRow() {
        TableRow row = new TableRow(context);
        row.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));
        return row;
    }

    private void showOptionsDialog(final TableRow row, final int id, final int status) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Options");

        if (status == 0) {
            builder.setItems(new String[]{"Update", "Delete"}, (dialog, which) -> {
                if (which == 0) {
                    updateExpense(id, row);
                } else if (which == 1) {
                    deleteRow(id, row);
                }
            });
        } else if (status == 1) {
            builder.setItems(new String[]{"Update", "Undo"}, (dialog, which) -> {
                if (which == 0) {
                    updateExpense(id, row);
                } else if (which == 1) {
                    undoDelete(id, row);
                }
            });
        }

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void updateExpense(int id, TableRow row) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = getLayoutInflater().inflate(R.layout.popup_updateexpense, null);
        builder.setView(dialogView);

        EditText amountInput = dialogView.findViewById(R.id.amount_edittext);
        EditText dateInput = dialogView.findViewById(R.id.date_edittext);
        EditText noteInput = dialogView.findViewById(R.id.note_edittext);
        AutoCompleteTextView categorySpinner = dialogView.findViewById(R.id.category_spinner);

        if (amountInput == null || dateInput == null || noteInput == null || categorySpinner == null) {
            AppSettingsManager.showToast(this, "Dialog layout error");
            return;
        }

        setupCategorySpinner(categorySpinner);
        loadExistingExpenseData(id, amountInput, dateInput, noteInput, categorySpinner);

        final AlertDialog alertDialog = builder.create();

        Button updateButton = dialogView.findViewById(R.id.update_button);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);

        if (updateButton != null) {
            updateButton.setOnClickListener(v -> handleExpenseUpdate(id, amountInput, dateInput, noteInput, categorySpinner, alertDialog));
        }
        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> alertDialog.dismiss());
        }

        AppSettingsManager.setupDatePicker(dateInput, this);
        AppSettingsManager.setupAmountInput(amountInput);

        alertDialog.show();
    }

    private void setupCategorySpinner(@NonNull AutoCompleteTextView categorySpinner) {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return;

        Cursor cursor = null;
        try {
            cursor = AppSettingsManager.safeQuery(sqLiteDatabase, "SELECT c_name FROM category ORDER BY c_name", null);
            ArrayList<String> categories = new ArrayList<>();
            while (cursor.moveToNext()) {
                String categoryName = cursor.getString(0);
                if (categoryName != null && !categoryName.trim().isEmpty()) {
                    categories.add(AppSettingsManager.sanitizeInput(categoryName.trim()));
                }
            }

            if (!categories.isEmpty()) {
                ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                        context, android.R.layout.simple_dropdown_item_1line, categories
                );
                categorySpinner.setAdapter(categoryAdapter);
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error loading categories: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(cursor);
        }
    }

    private void loadExistingExpenseData(int id, @NonNull EditText amountInput, @NonNull EditText dateInput,
                                       @NonNull EditText noteInput, @NonNull AutoCompleteTextView categorySpinner) {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return;

        Cursor expenseCursor = null;
        try {
            expenseCursor = AppSettingsManager.safeQuery(sqLiteDatabase, "SELECT date, category, amount, note FROM expenses WHERE e_id=?",
                    new String[]{String.valueOf(id)});
            if (expenseCursor.moveToFirst()) {
                int dateIndex = expenseCursor.getColumnIndex("date");
                int amountIndex = expenseCursor.getColumnIndex("amount");
                int noteIndex = expenseCursor.getColumnIndex("note");
                int categoryIndex = expenseCursor.getColumnIndex("category");
                
                if (dateIndex == -1 || amountIndex == -1 || noteIndex == -1 || categoryIndex == -1) {
                    return; // Column not found, abort to prevent crash
                }
                
                @SuppressLint("Range") String date = expenseCursor.getString(dateIndex);
                @SuppressLint("Range") int amount = expenseCursor.getInt(amountIndex);
                @SuppressLint("Range") String note = expenseCursor.getString(noteIndex);
                @SuppressLint("Range") String category = expenseCursor.getString(categoryIndex);

                dateInput.setText(date != null ? AppSettingsManager.sanitizeInput(date) : "");
                amountInput.setText(String.valueOf(amount));
                noteInput.setText(note != null ? AppSettingsManager.sanitizeInput(note) : "");
                categorySpinner.setText(category != null ? AppSettingsManager.sanitizeInput(category) : "");
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error loading expense data: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(expenseCursor);
        }
    }

    private void handleExpenseUpdate(int id, @NonNull EditText amountInput, @NonNull EditText dateInput,
                                   @NonNull EditText noteInput, @NonNull AutoCompleteTextView categorySpinner,
                                   AlertDialog alertDialog) {
        String updatedAmount = AppSettingsManager.sanitizeInput(amountInput.getText().toString().trim());
        String updatedDate = AppSettingsManager.sanitizeInput(dateInput.getText().toString().trim());
        String updatedNote = AppSettingsManager.sanitizeInput(noteInput.getText().toString().trim());
        String updatedCategory = AppSettingsManager.sanitizeInput(categorySpinner.getText().toString().trim());

        // Enhanced input validation with length limits
        if (!validateExpenseInput(updatedCategory, updatedAmount, updatedDate, updatedNote)) {
            return;
        }

        try {
            double amountValue = AppSettingsManager.safeParseDouble(updatedAmount, -1);
            if (amountValue <= 0 || amountValue > 999999999) {
                AppSettingsManager.showToast(this, "Amount must be between 0 and 999,999,999");
                return;
            }
            
            String[] updatedDateParts = updatedDate.split("-");
            if (updatedDateParts.length != 3) {
                AppSettingsManager.showToast(this, "Invalid date format - missing components");
                return;
            }
            
            int updatedYear = AppSettingsManager.safeParseInt(updatedDateParts[2], -1);
            int updatedMonth = AppSettingsManager.safeParseInt(updatedDateParts[1], -1);

            if (updatedYear == -1 || updatedMonth == -1) {
                AppSettingsManager.showToast(this, "Invalid date format");
                return;
            }

            int selectedYear = AppSettingsManager.getSelectedYear(context);
            int selectedMonth = AppSettingsManager.getSelectedMonthForDatabase(context);

            if (updatedYear == selectedYear && updatedMonth == selectedMonth) {
                updateExistingExpense(id, updatedDate, updatedCategory, updatedAmount, updatedNote, alertDialog);
            } else {
                moveExpenseToNewMonth(id, updatedYear, updatedMonth, updatedDate, updatedCategory, updatedAmount, updatedNote, alertDialog);
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error processing update: " + e.getMessage());
        }
    }

    private boolean validateExpenseInput(String category, String amount, String date, String note) {
        // Use centralized validation methods
        if (!AppSettingsManager.validateCategory(AppSettingsManager.sanitizeInput(category), this, true)) return false;
        if (!AppSettingsManager.validateAmount(AppSettingsManager.sanitizeInput(amount), this)) return false;
        if (!AppSettingsManager.validateDate(AppSettingsManager.sanitizeInput(date), this)) return false;
        if (!AppSettingsManager.validateNote(AppSettingsManager.sanitizeInput(note), this, false)) return false;
        
        return true;
    }



    private void updateExistingExpense(int id, String date, String category, String amount,
                                     String note, AlertDialog alertDialog) {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return;

        try {
            int oldStatus = getExpenseStatus(id);

            ContentValues updateValues = new ContentValues();
            updateValues.put("date", date);
            updateValues.put("category", category);
            updateValues.put("amount", amount);
            updateValues.put("note", note);
            updateValues.put("status", oldStatus);

            int rowsAffected = sqLiteDatabase.update("expenses", updateValues, "e_id=?",
                    new String[]{String.valueOf(id)});
            if (rowsAffected > 0) {
                AppSettingsManager.showToast(this, "Expense updated successfully!");
                alertDialog.dismiss();
                TotalExpense();
            } else {
                AppSettingsManager.showToast(this, "Failed to update expense.");
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error updating expense: " + e.getMessage());
        }
    }

    private void moveExpenseToNewMonth(int id, int updatedYear, int updatedMonth, String updatedDate,
                                     String updatedCategory, String updatedAmount, String updatedNote,
                                     AlertDialog alertDialog) {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return;

        try {
            int oldStatus = getExpenseStatus(id);
            // Use centralized ID generation to prevent conflicts
            int newEId = AppSettingsManager.generateUniqueExpenseId(sqLiteDatabase, updatedYear, updatedMonth);

            ContentValues contentValues = new ContentValues();
            contentValues.put("e_id", newEId);
            contentValues.put("year", updatedYear);
            contentValues.put("month", updatedMonth);
            contentValues.put("date", updatedDate);
            contentValues.put("category", updatedCategory);
            contentValues.put("amount", updatedAmount);
            contentValues.put("note", updatedNote);
            contentValues.put("status", oldStatus);

            long insertedId = sqLiteDatabase.insert("expenses", null, contentValues);
            if (insertedId != -1) {
                deleteOldExpenseAndReorder(id);
                AppSettingsManager.showToast(this, "Expense updated and moved to new month!");
                alertDialog.dismiss();
                TotalExpense();
            } else {
                AppSettingsManager.showToast(this, "Failed to update expense.");
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error moving expense: " + e.getMessage());
        }
    }

    private int getExpenseStatus(int id) {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return 0;

        Cursor statusCursor = null;
        try {
            statusCursor = sqLiteDatabase.rawQuery("SELECT status FROM expenses WHERE e_id=?",
                    new String[]{String.valueOf(id)});
            if (statusCursor.moveToFirst()) {
                return statusCursor.getInt(0);
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error getting expense status: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(statusCursor);
        }
        return 0;
    }

    private void deleteOldExpenseAndReorder(int id) {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return;

        try {
            int rowsDeleted = sqLiteDatabase.delete("expenses", "e_id=?", new String[]{String.valueOf(id)});
            if (rowsDeleted > 0) {
                reorderExpenseIds();
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error deleting old expense: " + e.getMessage());
        }
    }

    private void reorderExpenseIds() {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return;

        Cursor eIdCursor = null;
        try {
            eIdCursor = sqLiteDatabase.rawQuery("SELECT e_id FROM expenses WHERE month=? AND year=?",
                    new String[]{String.valueOf(selectedmonth), String.valueOf(selectedyear)});

            ArrayList<Integer> eIds = new ArrayList<>();
            if (eIdCursor.moveToFirst()) {
                do {
                    int eIdIndex = eIdCursor.getColumnIndex("e_id");
                    if (eIdIndex == -1) {
                        continue; // Skip if column not found
                    }
                    @SuppressLint("Range") int eid = eIdCursor.getInt(eIdIndex);
                    eIds.add(eid);
                } while (eIdCursor.moveToNext());
            }

            int startingEId = getEid(selectedyear, selectedmonth) + 1;
            for (int i = 0; i < eIds.size(); i++) {
                int reEid = startingEId + i;
                sqLiteDatabase.execSQL("UPDATE expenses SET e_id=? WHERE e_id=?",
                        new String[]{String.valueOf(reEid), String.valueOf(eIds.get(i))});
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error reordering expense IDs: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(eIdCursor);
        }
    }

    private int getEid(int selectedYear, int selectedMonth) {
        int lastTwoDigits = selectedYear % 100;
        return (selectedMonth * 100000) + (lastTwoDigits * 1000);
    }



    @SuppressLint("Range")
    private void TotalExpense() {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return;

        Cursor monthYearCursor = null;
        try {
            monthYearCursor = sqLiteDatabase.rawQuery("SELECT DISTINCT month, year FROM expenses", null);

            if (monthYearCursor.moveToFirst()) {
                do {
                    int monthIndex = monthYearCursor.getColumnIndex("month");
                    int yearIndex = monthYearCursor.getColumnIndex("year");
                    
                    if (monthIndex == -1 || yearIndex == -1) {
                        continue; // Skip this record if columns not found
                    }
                    
                    int month = monthYearCursor.getInt(monthIndex);
                    int year = monthYearCursor.getInt(yearIndex);
                    updateBudgetInfoForMonth(month, year);
                } while (monthYearCursor.moveToNext());
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error calculating total expense: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(monthYearCursor);
        }
    }

    private void updateBudgetInfoForMonth(int month, int year) {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return;

        Cursor cursor = null;
        Cursor budgetCursor = null;
        Cursor incomeCursor = null;

        try {
            // Get total expense
            String totalExpenseQuery = "SELECT SUM(CASE WHEN status = 0 THEN amount ELSE 0 END) FROM expenses WHERE month = ? AND year = ?";
            cursor = sqLiteDatabase.rawQuery(totalExpenseQuery, new String[]{String.valueOf(month), String.valueOf(year)});

            // Get total income
            String totalIncomeQuery = "SELECT SUM(CASE WHEN status = 0 THEN amount ELSE 0 END) FROM incomes WHERE month = ? AND year = ?";
            incomeCursor = sqLiteDatabase.rawQuery(totalIncomeQuery, new String[]{String.valueOf(month), String.valueOf(year)});

            if (cursor.moveToFirst() && incomeCursor.moveToFirst()) {
                int totalExpense = cursor.getInt(0);
                int totalIncome = incomeCursor.getInt(0);

                ContentValues values = new ContentValues();
                values.put("expense_money", totalExpense);
                values.put("income_money", totalIncome);

                budgetCursor = sqLiteDatabase.rawQuery("SELECT budget_amount FROM BudgetInfo WHERE month = ? AND year = ?",
                        new String[]{String.valueOf(month), String.valueOf(year)});
                if (budgetCursor.moveToFirst()) {
                    int budgetAmount = budgetCursor.getInt(0);
                    // CORRECTED FORMULA: current_budget = budget_amount + income - expense
                    values.put("current_budget", budgetAmount + totalIncome - totalExpense);
                    // CORRECTED FORMULA: total_saving = income - expense
                    values.put("total_saving", totalIncome - totalExpense);
                }

                sqLiteDatabase.update("BudgetInfo", values, "month = ? AND year = ?",
                        new String[]{String.valueOf(month), String.valueOf(year)});
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error updating budget info: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(cursor);
            AppSettingsManager.closeCursor(budgetCursor);
            AppSettingsManager.closeCursor(incomeCursor);
        }
    }

    private void deleteRow(final int id, final TableRow row) {
        AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(this);
        confirmBuilder.setTitle("Delete Entry");
        confirmBuilder.setMessage("Are you sure you want to delete this entry?");

        confirmBuilder.setPositiveButton("Delete", (dialog, which) -> {
            if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return;

            try {
                ContentValues values = new ContentValues();
                values.put("status", 1);
                sqLiteDatabase.update("expenses", values, "e_id=? AND month=? AND year=?",
                        new String[]{String.valueOf(id), String.valueOf(selectedmonth), String.valueOf(selectedyear)});
                TotalExpense();
                markRowAsDeleted(row);
            } catch (Exception e) {
                AppSettingsManager.showToast(this, "Error deleting expense: " + e.getMessage());
            }
        });

        confirmBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        confirmBuilder.create().show();
    }

    private void undoDelete(int id, final TableRow row) {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return;

        try {
            ContentValues values = new ContentValues();
            values.put("status", 0);
            sqLiteDatabase.update("expenses", values, "e_id=? AND month=? AND year=?",
                    new String[]{String.valueOf(id), String.valueOf(selectedmonth), String.valueOf(selectedyear)});
            restoreRowAppearance(row);
            TotalExpense();
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error undoing delete: " + e.getMessage());
        }
    }

    private void markRowAsDeleted(TableRow row) {
        deletedRowIds.add(row.getId());
        for (int i = 0; i < row.getChildCount(); i++) {
            View child = row.getChildAt(i);
            if (child instanceof TextView) {
                TextView textView = (TextView) child;
                textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                textView.setTextColor(Color.GRAY); // You can adjust the color as needed
                textView.setGravity(Gravity.CENTER);
            }
        }
    }

    private void restoreRowAppearance(TableRow row) {
        deletedRowIds.remove(row.getId());
        for (int i = 0; i < row.getChildCount(); i++) {
            View child = row.getChildAt(i);
            if (child instanceof TextView) {
                TextView textView = (TextView) child;
                textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                textView.setTextColor(Color.BLACK); // Restore the normal color
                textView.setGravity(Gravity.START);
            }
        }
    }
}
