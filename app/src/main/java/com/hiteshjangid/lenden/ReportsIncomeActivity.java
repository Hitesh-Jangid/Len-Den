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
import android.text.TextUtils;
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
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.hiteshjangid.lenden.databinding.ActivityIncomeReportsBinding;
import com.hiteshjangid.lenden.util.AppSettingsManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;

public class ReportsIncomeActivity extends AppCompatActivity {
    private Context context = this;
    private LinearLayout tableLayout;
    private SQLiteDatabase sqLiteDatabase;
    private HashSet<Integer> deletedRowIds = new HashSet<>();
    private int selectedmonth, selectedyear;
    private ActivityIncomeReportsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityIncomeReportsBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            initializeDatabase();
            loadIncomeReports();
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error initializing activity: " + e.getMessage());
            finish();
        }
    }

    private void initializeDatabase() {
        try {
            selectedmonth = AppSettingsManager.getSelectedMonthForDatabase(this);
            selectedyear = AppSettingsManager.getSelectedYear(this);
            String monthName = AppSettingsManager.getSelectedMonthName(this);
            sqLiteDatabase = AppSettingsManager.getDatabase(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database: " + e.getMessage());
        }
    }

    private void loadIncomeReports() {
        Cursor incomeCursor = null;
        try {
            tableLayout = binding.reportTableLayout;
            String query = "SELECT income_id, date, type, amount, note, status FROM incomes WHERE month = ? AND year = ?";
            incomeCursor = sqLiteDatabase.rawQuery(query, new String[]{String.valueOf(selectedmonth), String.valueOf(selectedyear)});

            if (incomeCursor != null && incomeCursor.moveToFirst()) {
                do {
                    populateDataRow(incomeCursor);
                } while (incomeCursor.moveToNext());
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error loading income reports: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(incomeCursor);
        }
    }

    private void populateDataRow(Cursor cursor) {
        TableRow row = null;
        try {
            row = createTableRow();
            
            int idIndex = cursor.getColumnIndex("income_id");
            int dateIndex = cursor.getColumnIndex("date");
            int typeIndex = cursor.getColumnIndex("type");
            int amountIndex = cursor.getColumnIndex("amount");
            int noteIndex = cursor.getColumnIndex("note");
            int statusIndex = cursor.getColumnIndex("status");
            
            if (idIndex == -1 || dateIndex == -1 || typeIndex == -1 || 
                amountIndex == -1 || noteIndex == -1 || statusIndex == -1) {
                return;
            }
            
            @SuppressLint("Range") int id = cursor.getInt(idIndex);
            @SuppressLint("Range") String date = cursor.getString(dateIndex);
            @SuppressLint("Range") String type = cursor.getString(typeIndex);
            @SuppressLint("Range") int amount = cursor.getInt(amountIndex);
            @SuppressLint("Range") String note = cursor.getString(noteIndex);
            @SuppressLint("Range") int status = cursor.getInt(statusIndex);

            
            final TableRow finalRow = row;
            final int finalId = id;
            final int finalStatus = status;

            TextView tvId = createTextView(String.valueOf(id), false);
            TextView tvDate = createTextView(date, false);
            TextView tvType = createTextView(type, false);
            TextView tvAmount = createTextView(String.valueOf(amount), false);
            TextView tvNote = createTextView(note, false);

            // Set layout parameters for each TextView
            tvId.setLayoutParams(createLayoutParams());
            tvDate.setLayoutParams(createLayoutParams());
            tvType.setLayoutParams(createLayoutParams());
            tvAmount.setLayoutParams(createLayoutParams());
            tvNote.setLayoutParams(createLayoutParams());

            // Add TextViews to the row
            row.addView(tvId);
            row.addView(tvDate);
            row.addView(tvType);
            row.addView(tvAmount);
            row.addView(tvNote);

            // Set long click listener
            row.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showOptionsDialog(finalRow, finalId, finalStatus);
                    return true;
                }
            });

            if (status == 1) {
                markRowAsDeleted(row);
            }

            tableLayout.addView(row);
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error populating data row: " + e.getMessage());
            if (row != null && tableLayout != null) {
                tableLayout.removeView(row);
            }
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
            builder.setItems(new String[]{"Update", "Delete"}, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        updateIncome(id, row);
                    } else if (which == 1) {
                        deleteRow(id, row);
                    }
                }
            });
        } else if (status == 1) {
            builder.setItems(new String[]{"Update", "Undo"}, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        updateIncome(id, row);
                    } else if (which == 1) {
                        undoDelete(id, row);
                    }
                }
            });
        }

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void updateIncome(int id, TableRow row) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = getLayoutInflater().inflate(R.layout.popup_updateincome, null);
        builder.setView(dialogView);

        EditText amountInput = dialogView.findViewById(R.id.amount_edittext);
        EditText dateInput = dialogView.findViewById(R.id.date_edittext);
        EditText noteInput = dialogView.findViewById(R.id.note_edittext);
        AutoCompleteTextView typeSpinner = dialogView.findViewById(R.id.type_spinner);

        if (amountInput == null || dateInput == null || noteInput == null || typeSpinner == null) {
            AppSettingsManager.showToast(this, "Dialog layout error");
            return;
        }

        setupTypeSpinner(typeSpinner);
        loadExistingIncomeData(id, amountInput, dateInput, noteInput, typeSpinner);

        final AlertDialog alertDialog = builder.create();

        Button updateButton = dialogView.findViewById(R.id.update_button);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);

        if (updateButton != null) {
            updateButton.setOnClickListener(v -> {
                if (validateAndUpdateIncome(id, row, amountInput, dateInput, noteInput, typeSpinner)) {
                    TotalIncome();
                    alertDialog.dismiss();
                }
            });
        }
        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> alertDialog.dismiss());
        }

        AppSettingsManager.setupDatePicker(dateInput, this);
        AppSettingsManager.setupAmountInput(amountInput);

        alertDialog.show();
    }

    private void setupTypeSpinner(AutoCompleteTextView typeSpinner) {
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT type_name FROM type ORDER BY type_name", null);
            ArrayList<String> types = new ArrayList<>();
            while (cursor.moveToNext()) {
                types.add(cursor.getString(0));
            }

            if (!types.isEmpty()) {
                ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                        context, android.R.layout.simple_dropdown_item_1line, types
                );
                typeSpinner.setAdapter(typeAdapter);
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error loading types: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(cursor);
        }
    }

    private void loadExistingIncomeData(int id, EditText amountInput, EditText dateInput,
                                      EditText noteInput, AutoCompleteTextView typeSpinner) {
        Cursor incomeCursor = null;
        try {
            incomeCursor = sqLiteDatabase.rawQuery("SELECT date, type, amount, note FROM incomes WHERE income_id=?",
                    new String[]{String.valueOf(id)});
            if (incomeCursor.moveToFirst()) {
                int dateIndex = incomeCursor.getColumnIndex("date");
                int amountIndex = incomeCursor.getColumnIndex("amount");
                int noteIndex = incomeCursor.getColumnIndex("note");
                int typeIndex = incomeCursor.getColumnIndex("type");
                
                if (dateIndex == -1 || amountIndex == -1 || noteIndex == -1 || typeIndex == -1) {
                    return; // Column not found, abort to prevent crash
                }
                
                @SuppressLint("Range") String date = incomeCursor.getString(dateIndex);
                @SuppressLint("Range") int amount = incomeCursor.getInt(amountIndex);
                @SuppressLint("Range") String note = incomeCursor.getString(noteIndex);
                @SuppressLint("Range") String type = incomeCursor.getString(typeIndex);

                dateInput.setText(date);
                amountInput.setText(String.valueOf(amount));
                noteInput.setText(note);
                typeSpinner.setText(type);
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error loading income data: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(incomeCursor);
        }
    }

    private boolean validateAndUpdateIncome(int id, TableRow row, EditText amountInput, EditText dateInput,
                                          EditText noteInput, AutoCompleteTextView typeSpinner) {
        try {
            String updatedAmount = amountInput.getText().toString().trim();
            String updatedDate = dateInput.getText().toString().trim();
            String updatedNote = noteInput.getText().toString().trim();
            String updatedType = typeSpinner.getText().toString().trim();

            if (TextUtils.isEmpty(updatedAmount)) {
                AppSettingsManager.showToast(this, "Please enter an amount");
                return false;
            }

            if (TextUtils.isEmpty(updatedDate)) {
                AppSettingsManager.showToast(this, "Please select a date");
                return false;
            }

            if (TextUtils.isEmpty(updatedType)) {
                AppSettingsManager.showToast(this, "Please select a type");
                return false;
            }

            double amountValue;
            try {
                amountValue = Double.parseDouble(updatedAmount);
                if (amountValue <= 0) {
                    AppSettingsManager.showToast(this, "Amount must be greater than 0");
                    return false;
                }
            } catch (NumberFormatException e) {
                AppSettingsManager.showToast(this, "Please enter a valid amount");
                return false;
            }

            // Update the database
            ContentValues values = new ContentValues();
            values.put("amount", amountValue);
            values.put("date", updatedDate);
            values.put("note", updatedNote);
            values.put("type", updatedType);

            int rowsAffected = sqLiteDatabase.update("incomes", values, "income_id=?",
                    new String[]{String.valueOf(id)});

            if (rowsAffected > 0) {
                // Update the TableRow with the new data
                // Validate and safely access table row children to prevent IndexOutOfBoundsException
                if (row.getChildCount() < 5) {
                    AppSettingsManager.showToast(this, "Invalid row structure");
                    return false;
                }
                
                TextView tvAmount = (TextView) row.getChildAt(3); // amount column
                TextView tvDate = (TextView) row.getChildAt(1);   // date column  
                TextView tvNote = (TextView) row.getChildAt(4);   // note column
                TextView tvType = (TextView) row.getChildAt(2);   // type column

                tvAmount.setText(String.valueOf(amountValue));
                tvDate.setText(updatedDate);
                tvNote.setText(updatedNote);
                tvType.setText(updatedType);

                AppSettingsManager.showToast(this, "Income updated successfully");
                return true;
            } else {
                AppSettingsManager.showToast(this, "Failed to update income");
                return false;
            }

        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error updating income: " + e.getMessage());
            return false;
        }
    }

    @SuppressLint("Range")
    private void TotalIncome() {
        Cursor monthYearCursor = null;
        Cursor cursor = null;
        Cursor budgetCursor = null;

        try {
            monthYearCursor = sqLiteDatabase.rawQuery("SELECT DISTINCT month, year FROM incomes", null);

            if (monthYearCursor != null && monthYearCursor.moveToFirst()) {
                do {
                    int monthIndex = monthYearCursor.getColumnIndex("month");
                    int yearIndex = monthYearCursor.getColumnIndex("year");
                    
                    if (monthIndex == -1 || yearIndex == -1) {
                        continue; // Skip this record if columns not found
                    }
                    
                    int month = monthYearCursor.getInt(monthIndex);
                    int year = monthYearCursor.getInt(yearIndex);

                    String totalIncomeQuery = "SELECT SUM(CASE WHEN status = 0 THEN amount ELSE 0 END) FROM incomes WHERE month = ? AND year = ?";
                    AppSettingsManager.closeCursor(cursor); // Close previous cursor before reuse
                    cursor = sqLiteDatabase.rawQuery(totalIncomeQuery, new String[]{String.valueOf(month), String.valueOf(year)});

                    if (cursor != null && cursor.moveToFirst()) {
                        int totalAmount = cursor.getInt(0);

                        ContentValues values = new ContentValues();
                        values.put("income_money", totalAmount);

                        // Fetch budget_amount and expense_money to update current_budget and total_saving
                        AppSettingsManager.closeCursor(budgetCursor); // Close previous cursor before reuse
                        budgetCursor = sqLiteDatabase.rawQuery("SELECT budget_amount, expense_money FROM BudgetInfo WHERE month = ? AND year = ?",
                                new String[]{String.valueOf(month), String.valueOf(year)});
                        if (budgetCursor != null && budgetCursor.moveToFirst()) {
                            int budgetAmount = budgetCursor.getInt(0);
                            int expenseMoney = budgetCursor.getInt(1);
                            // CORRECTED FORMULA: current_budget = budget_amount + income - expense
                            values.put("current_budget", budgetAmount + totalAmount - expenseMoney);
                            // CORRECTED FORMULA: total_saving = income - expense
                            values.put("total_saving", totalAmount - expenseMoney);
                        }

                        sqLiteDatabase.update("BudgetInfo", values, "month = ? AND year = ?",
                                new String[]{String.valueOf(month), String.valueOf(year)});
                    }
                } while (monthYearCursor.moveToNext());
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error calculating total income: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(monthYearCursor);
            AppSettingsManager.closeCursor(cursor);
            AppSettingsManager.closeCursor(budgetCursor);
        }
    }

    private void deleteRow(final int id, final TableRow row) {
        AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(this);
        confirmBuilder.setTitle("Delete Entry");
        confirmBuilder.setMessage("Are you sure you want to delete this entry?");

        confirmBuilder.setPositiveButton("Delete", (dialog, which) -> {
            try {
                ContentValues values = new ContentValues();
                values.put("status", 1);
                int rowsAffected = sqLiteDatabase.update("incomes", values, "income_id=? AND month=? AND year=?",
                        new String[]{String.valueOf(id), String.valueOf(selectedmonth), String.valueOf(selectedyear)});

                if (rowsAffected > 0) {
                    TotalIncome();
                    markRowAsDeleted(row);
                    AppSettingsManager.showToast(this, "Entry deleted successfully");
                } else {
                    AppSettingsManager.showToast(this, "Failed to delete entry");
                }
            } catch (Exception e) {
                AppSettingsManager.showToast(this, "Error deleting entry: " + e.getMessage());
            }
        });

        confirmBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        confirmBuilder.create().show();
    }

    private void undoDelete(int id, final TableRow row) {
        AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(this);
        confirmBuilder.setTitle("Undo Delete");
        confirmBuilder.setMessage("Are you sure you want to undo the deletion of this entry?");

        confirmBuilder.setPositiveButton("Undo", (dialog, which) -> {
            try {
                ContentValues values = new ContentValues();
                values.put("status", 0);
                int rowsAffected = sqLiteDatabase.update("incomes", values, "income_id=? AND month=? AND year=?",
                        new String[]{String.valueOf(id), String.valueOf(selectedmonth), String.valueOf(selectedyear)});

                if (rowsAffected > 0) {
                    restoreRowAppearance(row);
                    TotalIncome();
                    AppSettingsManager.showToast(this, "Entry restored successfully");
                } else {
                    AppSettingsManager.showToast(this, "Failed to restore entry");
                }
            } catch (Exception e) {
                AppSettingsManager.showToast(this, "Error restoring entry: " + e.getMessage());
            }
        });

        confirmBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        confirmBuilder.create().show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppSettingsManager.closeDatabase(sqLiteDatabase);
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
