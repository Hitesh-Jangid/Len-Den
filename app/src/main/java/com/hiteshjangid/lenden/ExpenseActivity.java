package com.hiteshjangid.lenden;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.hiteshjangid.lenden.databinding.ActivityExpenseBinding;
import com.hiteshjangid.lenden.util.AppSettingsManager;

import java.util.ArrayList;
import java.util.Calendar;

public class ExpenseActivity extends AppCompatActivity {
    private Context context = this;
    private SQLiteDatabase sqLiteDatabase;
    private int selectedMonth;
    private int selectedYear;
    private ActivityExpenseBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeData();
        setupDatabase();
        setupUI();
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
    }

    private void setupDatabase() {
        try {
            sqLiteDatabase = AppSettingsManager.getDatabase(this);
            createTables();
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Database initialization failed: " + e.getMessage());
            finish();
        }
    }

    private void createTables() {
        if (sqLiteDatabase == null) return;

        String createCategoryTable = "CREATE TABLE IF NOT EXISTS category (" +
                "c_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "c_name TEXT UNIQUE NOT NULL);";

        String createExpensesTable = "CREATE TABLE IF NOT EXISTS expenses (" +
                "e_id INTEGER PRIMARY KEY, " +
                "month INTEGER NOT NULL, " +
                "year INTEGER NOT NULL, " +
                "date TEXT NOT NULL, " +
                "category TEXT NOT NULL, " +
                "amount INTEGER NOT NULL CHECK(amount > 0), " +
                "note TEXT, " +
                "status INTEGER DEFAULT 0);";

        try {
            sqLiteDatabase.execSQL(createCategoryTable);
            sqLiteDatabase.execSQL(createExpensesTable);
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Failed to create tables: " + e.getMessage());
        }
    }

    private void setupUI() {
        updateExpenseHeader();
    }

    private void setupClickListeners() {
        binding.addCategory.setOnClickListener(v -> showAddCategoryDialog());
        binding.addExpense.setOnClickListener(v -> showAddExpenseDialog());
        binding.viewCategory.setOnClickListener(v -> showCategoryListDialog());
        binding.viewExpense.setOnClickListener(v -> navigateToReports());
    }

    private void navigateToReports() {
        Intent intent = new Intent(context, ReportsActivity.class);
        startActivity(intent);
    }

    private void updateExpenseHeader() {
        String monthName = AppSettingsManager.getSelectedMonthName(this);
        binding.expenseHeader.setText("Expenses\n(" + monthName + "-" + selectedYear + ")");
    }

    private void showAddExpenseDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.popup_addexpense, null);

        final AutoCompleteTextView categorySpinner = dialogView.findViewById(R.id.category_spinner);
        final EditText amountInput = dialogView.findViewById(R.id.amount_edittext);
        final EditText dateInput = dialogView.findViewById(R.id.date_edittext);
        final EditText noteInput = dialogView.findViewById(R.id.note_edittext);
        final Button addButton = dialogView.findViewById(R.id.add_button);
        final Button cancelButton = dialogView.findViewById(R.id.cancel_button);

        if (categorySpinner == null || amountInput == null || dateInput == null || noteInput == null ||
            addButton == null || cancelButton == null) {
            AppSettingsManager.showToast(this, "Dialog layout error");
            return;
        }

        loadCategoriesIntoSpinner(categorySpinner);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MaterialAlertDialog_Rounded);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);

        addButton.setOnClickListener(v -> {
            if (validateAndAddExpense(categorySpinner, amountInput, dateInput, noteInput)) {
                alertDialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(v -> alertDialog.dismiss());

        AppSettingsManager.setupDatePicker(dateInput, this);
        AppSettingsManager.setupAmountInput(amountInput);

        alertDialog.show();
    }

    private boolean validateAndAddExpense(AutoCompleteTextView categorySpinner, EditText amountInput,
                                        EditText dateInput, EditText noteInput) {
        String category = AppSettingsManager.sanitizeInput(categorySpinner.getText().toString().trim());
        String amount = AppSettingsManager.sanitizeInput(amountInput.getText().toString().trim());
        String date = AppSettingsManager.sanitizeInput(dateInput.getText().toString().trim());
        String note = AppSettingsManager.sanitizeInput(noteInput.getText().toString().trim());

        // Use centralized validation methods
        if (!AppSettingsManager.validateCategory(category, this, true)) return false;
        if (!AppSettingsManager.validateAmount(amount, this)) return false;
        if (!AppSettingsManager.validateDate(date, this)) return false;
        if (!AppSettingsManager.validateNote(note, this, true)) return false;

        try {
            String[] dateParts = date.split("-");
            if (dateParts.length != 3) {
                AppSettingsManager.showToast(this, "Invalid date format - missing components");
                return false;
            }
            
            int year = Integer.parseInt(dateParts[2]);
            int month = Integer.parseInt(dateParts[1]);
            int newEId = getNewEId(year, month, date);

            if (saveExpenseToDatabase(newEId, year, month, date, category, amount, note)) {
                AppSettingsManager.showToast(this, "Expense added successfully");
                TotalExpense();
                return true;
            } else {
                AppSettingsManager.showToast(this, "Failed to save expense");
                return false;
            }
        } catch (NumberFormatException e) {
            AppSettingsManager.showToast(this, "Please enter valid numbers");
            return false;
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error: " + e.getMessage());
            return false;
        }
    }



    private void loadCategoriesIntoSpinner(@NonNull AutoCompleteTextView categorySpinner) {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return;

        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT c_name FROM category ORDER BY c_name", null);
            ArrayList<String> categories = new ArrayList<>();
            while (cursor.moveToNext()) {
                categories.add(cursor.getString(0));
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

    private void handleAddExpense(@NonNull AutoCompleteTextView categorySpinner, @NonNull EditText amountInput,
                                  @NonNull EditText dateInput, @NonNull EditText noteInput, @NonNull AlertDialog alertDialog) {
        String selectedCategory = categorySpinner.getText().toString().trim();
        String amount = amountInput.getText().toString().trim();
        String date = dateInput.getText().toString().trim();
        String note = noteInput.getText().toString().trim();

        if (selectedCategory.isEmpty()) {
            AppSettingsManager.showToast(this, "Please select or enter a category");
            return;
        }
        if (amount.isEmpty()) {
            AppSettingsManager.showToast(this, "Please enter an amount");
            return;
        }
        if (date.isEmpty()) {
            AppSettingsManager.showToast(this, "Please select a date");
            return;
        }

        double amountValue;
        try {
            amountValue = Double.parseDouble(amount);
            if (amountValue <= 0) {
                AppSettingsManager.showToast(this, "Amount must be greater than 0");
                return;
            }
        } catch (NumberFormatException e) {
            AppSettingsManager.showToast(this, "Please enter a valid amount");
            return;
        }

        // Get the year and month from the selected date
        String[] dateParts = date.split("-");
        if (dateParts.length != 3) {
            AppSettingsManager.showToast(this, "Invalid date format");
            return;
        }

        try {
            int year = Integer.parseInt(dateParts[2]);
            int month = Integer.parseInt(dateParts[1]);

            // Get the new e_id based on the year, month, and date
            int newEId = getNewEId(year, month, date);

            if (saveExpenseToDatabase(newEId, year, month, date, selectedCategory, amount, note)) {
                AppSettingsManager.showToast(this, "Expense Added. Expense ID: " + newEId);
                TotalExpense();
                alertDialog.dismiss();
            }
        } catch (NumberFormatException e) {
            AppSettingsManager.showToast(this, "Invalid date format");
        }
    }

    private boolean saveExpenseToDatabase(int eId, int year, int month, String date, String category, String amount, String note) {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return false;

        return AppSettingsManager.executeInTransactionWithCallback(sqLiteDatabase, () -> {
            ContentValues contentValues = new ContentValues();
            contentValues.put("e_id", eId);
            contentValues.put("year", year);
            contentValues.put("month", month);
            contentValues.put("date", date);
            contentValues.put("category", category);
            contentValues.put("amount", amount);
            contentValues.put("note", note);
            contentValues.put("status", 0);

            long insertedId = sqLiteDatabase.insert("expenses", null, contentValues);
            if (insertedId == -1) {
                throw new RuntimeException("Failed to insert expense");
            }
        }, this, "Error saving expense");
    }

    private int getNewEId(int year, int month, String date) {
        return AppSettingsManager.generateUniqueExpenseId(sqLiteDatabase, year, month);
    }



    private void showAddCategoryDialog() {
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setHint("Enter category name");

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MaterialAlertDialog_Rounded);
        builder.setTitle("Add Expense Category")
               .setView(input)
               .setPositiveButton("Add", (dialog, which) -> {
                   String categoryName = input.getText().toString().trim();
                   if (categoryName.isEmpty()) {
                       AppSettingsManager.showToast(this, "Please enter a category name");
                       return;
                   }
                   addCategoryToDatabase(categoryName);
               })
               .setNegativeButton("Cancel", null)
               .show();
    }

    private void addCategoryToDatabase(String categoryName) {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return;
        
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT c_id FROM category WHERE c_name=?",
                    new String[]{categoryName});
            if (cursor != null && cursor.moveToFirst()) {
                AppSettingsManager.showToast(this, "Category already exists!");
                return;
            }

            ContentValues values = new ContentValues();
            values.put("c_name", categoryName);
            long result = sqLiteDatabase.insert("category", null, values);
            if (result != -1) {
                AppSettingsManager.showToast(this, "Category added successfully");
            } else {
                AppSettingsManager.showToast(this, "Failed to add category");
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error adding category: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(cursor);
        }
    }

    @SuppressLint("Range")
    private void TotalExpense() {
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT DISTINCT month, year FROM expenses", null);
            if (cursor.moveToFirst()) {
                do {
                    int monthIndex = cursor.getColumnIndex("month");
                    int yearIndex = cursor.getColumnIndex("year");
                    
                    if (monthIndex == -1 || yearIndex == -1) {
                        continue; // Skip this record if columns not found
                    }
                    
                    int month = cursor.getInt(monthIndex);
                    int year = cursor.getInt(yearIndex);
                    updateBudgetWithExpense(month, year);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error calculating total expense: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(cursor);
        }
    }

    private void updateBudgetWithExpense(int month, int year) {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return;

        Cursor expenseCursor = null;
        Cursor incomeCursor = null;
        try {
            // Get total expense
            expenseCursor = sqLiteDatabase.rawQuery(
                    "SELECT SUM(CASE WHEN status = 0 THEN amount ELSE 0 END) FROM expenses WHERE month = ? AND year = ?",
                    new String[]{String.valueOf(month), String.valueOf(year)});
            
            // Get total income
            incomeCursor = sqLiteDatabase.rawQuery(
                    "SELECT SUM(CASE WHEN status = 0 THEN amount ELSE 0 END) FROM incomes WHERE month = ? AND year = ?",
                    new String[]{String.valueOf(month), String.valueOf(year)});

            if (expenseCursor.moveToFirst() && incomeCursor.moveToFirst()) {
                int totalExpense = expenseCursor.getInt(0);
                int totalIncome = incomeCursor.getInt(0);
                
                // Use centralized budget update logic
                AppSettingsManager.updateBudgetWithTransaction(sqLiteDatabase, this, month, year, totalIncome, totalExpense);
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error updating budget with expense: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(expenseCursor);
            AppSettingsManager.closeCursor(incomeCursor);
        }
    }

    private void showCategoryListDialog() {
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT c_name FROM category ORDER BY c_name", null);
            ArrayList<String> categoryList = new ArrayList<>();

            while (cursor.moveToNext()) {
                categoryList.add(cursor.getString(0));
            }

            if (!categoryList.isEmpty()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, categoryList);

                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MaterialAlertDialog_Rounded);
                builder.setTitle("Expense Categories")
                       .setAdapter(adapter, null)
                       .setPositiveButton("OK", null)
                       .show();
            } else {
                showNoCategoriesDialog();
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error loading categories: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(cursor);
        }
    }

    private void showNoCategoriesDialog() {
        new AlertDialog.Builder(context, R.style.MaterialAlertDialog_Rounded)
            .setTitle("No Categories")
            .setMessage("Please add a category first!")
            .setPositiveButton("OK", null)
            .show();
    }

    // Handle navigation back to this activity with updated month and year
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                selectedMonth = data.getIntExtra("selectedMonth", 0);
                selectedYear = data.getIntExtra("selectedYear", 0);
                updateExpenseHeader();
            }
        }
    }
}
