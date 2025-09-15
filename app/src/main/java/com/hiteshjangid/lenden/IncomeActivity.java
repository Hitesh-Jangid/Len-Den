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

import com.hiteshjangid.lenden.databinding.ActivityIncomeBinding;
import com.hiteshjangid.lenden.util.AppSettingsManager;

import java.util.ArrayList;
import java.util.Calendar;

public class IncomeActivity extends AppCompatActivity {
    private Context context = this;
    private SQLiteDatabase sqLiteDatabase;
    private int selectedMonth;
    private int selectedYear;
    private ActivityIncomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIncomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeData();
        setupDatabase();
        setupUI();
        setupNavigationListeners();
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

        String createTypeTable = "CREATE TABLE IF NOT EXISTS type (" +
                "type_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "type_name TEXT UNIQUE NOT NULL);";

        String createIncomesTable = "CREATE TABLE IF NOT EXISTS incomes (" +
                "income_id INTEGER PRIMARY KEY, " +
                "month INTEGER NOT NULL, " +
                "year INTEGER NOT NULL, " +
                "date TEXT NOT NULL, " +
                "type TEXT NOT NULL, " +
                "amount INTEGER NOT NULL CHECK(amount > 0), " +
                "note TEXT, " +
                "status INTEGER DEFAULT 0);";

        try {
            sqLiteDatabase.execSQL(createTypeTable);
            sqLiteDatabase.execSQL(createIncomesTable);
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Failed to create tables: " + e.getMessage());
        }
    }

    private void setupUI() {
        updateIncomeHeader();
    }

    private void setupNavigationListeners() {
        binding.addIncome.setOnClickListener(v -> showAddIncomeDialog());
        binding.viewIncome.setOnClickListener(v -> navigateToReports());
        binding.addType.setOnClickListener(v -> showAddTypeDialog());
        binding.viewIncomeType.setOnClickListener(v -> showTypeListDialog());
    }

    private void navigateToReports() {
        Intent intent = new Intent(context, ReportsIncomeActivity.class);
        startActivity(intent);
    }

    private void updateIncomeHeader() {
        String monthName = AppSettingsManager.getSelectedMonthName(this);
        binding.incomeHeader.setText("Incomes\n(" + monthName + "-" + selectedYear + ")");
    }

    private void showAddIncomeDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.popup_addincome, null);

        final AutoCompleteTextView typeSpinner = dialogView.findViewById(R.id.type_spinner);
        final EditText amountInput = dialogView.findViewById(R.id.amount_edittext);
        final EditText dateInput = dialogView.findViewById(R.id.date_edittext);
        final EditText noteInput = dialogView.findViewById(R.id.note_edittext);
        final Button addButton = dialogView.findViewById(R.id.add_button);
        final Button cancelButton = dialogView.findViewById(R.id.cancel_button);

        if (typeSpinner == null || amountInput == null || dateInput == null || noteInput == null ||
            addButton == null || cancelButton == null) {
            AppSettingsManager.showToast(this, "Dialog layout error");
            return;
        }

        loadTypesIntoSpinner(typeSpinner);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MaterialAlertDialog_Rounded);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);

        addButton.setOnClickListener(v -> {
            if (validateAndAddIncome(typeSpinner, amountInput, dateInput, noteInput)) {
                alertDialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(v -> alertDialog.dismiss());

        AppSettingsManager.setupDatePicker(dateInput, this);
        AppSettingsManager.setupAmountInput(amountInput);

        alertDialog.show();
    }

    private void loadTypesIntoSpinner(AutoCompleteTextView typeSpinner) {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return;

        Cursor cursor = null;
        try {
            cursor = AppSettingsManager.safeQuery(sqLiteDatabase, "SELECT type_name FROM type ORDER BY type_name", null);
            ArrayList<String> types = new ArrayList<>();
            while (cursor.moveToNext()) {
                String typeName = cursor.getString(0);
                if (typeName != null && !typeName.trim().isEmpty()) {
                    types.add(AppSettingsManager.sanitizeInput(typeName.trim()));
                }
            }

            if (!types.isEmpty()) {
                ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                        context, android.R.layout.simple_dropdown_item_1line, types);
                typeSpinner.setAdapter(typeAdapter);
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error loading types: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(cursor);
        }
    }


    private boolean validateAndAddIncome(AutoCompleteTextView typeSpinner, EditText amountInput,
                                       EditText dateInput, EditText noteInput) {
        String type = AppSettingsManager.sanitizeInput(typeSpinner.getText().toString().trim());
        String amount = AppSettingsManager.sanitizeInput(amountInput.getText().toString().trim());
        String date = AppSettingsManager.sanitizeInput(dateInput.getText().toString().trim());
        String note = AppSettingsManager.sanitizeInput(noteInput.getText().toString().trim());

        if (!AppSettingsManager.validateCategory(type, this, true)) return false;
        if (!AppSettingsManager.validateAmount(amount, this)) return false;
        if (!AppSettingsManager.validateDate(date, this)) return false;
        if (!AppSettingsManager.validateNote(note, this, false)) return false;

        try {
            String[] dateParts = date.split("-");
            if (dateParts.length != 3) {
                AppSettingsManager.showToast(this, "Invalid date format - missing components");
                return false;
            }
            
            int year = Integer.parseInt(dateParts[2]);
            int month = Integer.parseInt(dateParts[1]);
            int newIId = getNewIncomeId(year, month, date);

            if (saveIncomeToDatabase(newIId, year, month, date, type, amount, note)) {
                AppSettingsManager.showToast(this, "Income added successfully");
                TotalIncome();
                return true;
            } else {
                AppSettingsManager.showToast(this, "Failed to save income");
                return false;
            }
        } catch (NumberFormatException e) {
            AppSettingsManager.showToast(this, "Please enter valid numbers");
            return false;
        }
    }

    private boolean saveIncomeToDatabase(int incomeId, int year, int month, String date, String type, String amount, String note) {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return false;

        return AppSettingsManager.executeInTransactionWithCallback(sqLiteDatabase, () -> {
            ContentValues contentValues = new ContentValues();
            contentValues.put("income_id", incomeId);
            contentValues.put("year", year);
            contentValues.put("month", month);
            contentValues.put("date", date);
            contentValues.put("type", type);
            contentValues.put("amount", amount);
            contentValues.put("note", note);
            contentValues.put("status", 0);

            long insertedId = sqLiteDatabase.insert("incomes", null, contentValues);
            if (insertedId == -1) {
                throw new RuntimeException("Failed to insert income");
            }
        }, this, "Error saving income");
    }

    private int getNewIncomeId(int year, int month, String date) {
        return AppSettingsManager.generateUniqueIncomeId(sqLiteDatabase, year, month);
    }

    private void showAddTypeDialog() {
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setHint("Enter type name");

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MaterialAlertDialog_Rounded);
        builder.setTitle("Add Income Type")
               .setView(input)
               .setPositiveButton("Add", (dialog, which) -> {
                   String typeName = input.getText().toString().trim();
                   if (typeName.isEmpty()) {
                       AppSettingsManager.showToast(this, "Please enter a type name");
                       return;
                   }
                   addTypeToDatabase(typeName);
               })
               .setNegativeButton("Cancel", null)
               .show();
    }

    private void addTypeToDatabase(String typeName) {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return;

        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT type_id FROM type WHERE type_name=?",
                    new String[]{typeName});

            if (cursor != null && !cursor.moveToFirst()) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("type_name", typeName);
                long result = sqLiteDatabase.insert("type", null, contentValues);
                if (result != -1) {
                    AppSettingsManager.showToast(this, "Type Added");
                } else {
                    AppSettingsManager.showToast(this, "Failed to add type");
                }
            } else {
                AppSettingsManager.showToast(this, "Type Already Exists!");
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error adding type: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(cursor);
        }
    }

    @SuppressLint("Range")
    private void TotalIncome() {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return;

        Cursor monthYearCursor = null;
        try {
            monthYearCursor = sqLiteDatabase.rawQuery("SELECT DISTINCT month, year FROM incomes", null);
            if (monthYearCursor.moveToFirst()) {
                do {
                    int monthIndex = monthYearCursor.getColumnIndex("month");
                    int yearIndex = monthYearCursor.getColumnIndex("year");
                    
                    if (monthIndex == -1 || yearIndex == -1) {
                        continue;
                    }
                    
                    int month = monthYearCursor.getInt(monthIndex);
                    int year = monthYearCursor.getInt(yearIndex);
                    updateBudgetWithIncome(month, year);
                } while (monthYearCursor.moveToNext());
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error calculating total income: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(monthYearCursor);
        }
    }

    private void updateBudgetWithIncome(int month, int year) {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) return;

        Cursor incomeCursor = null;
        Cursor expenseCursor = null;
        try {
            incomeCursor = sqLiteDatabase.rawQuery(
                    "SELECT SUM(CASE WHEN status = 0 THEN amount ELSE 0 END) FROM incomes WHERE month = ? AND year = ?",
                    new String[]{String.valueOf(month), String.valueOf(year)});
            
            expenseCursor = sqLiteDatabase.rawQuery(
                    "SELECT SUM(CASE WHEN status = 0 THEN amount ELSE 0 END) FROM expenses WHERE month = ? AND year = ?",
                    new String[]{String.valueOf(month), String.valueOf(year)});

            if (incomeCursor.moveToFirst() && expenseCursor.moveToFirst()) {
                int totalIncome = incomeCursor.getInt(0);
                int totalExpense = expenseCursor.getInt(0);
                
                AppSettingsManager.updateBudgetWithTransaction(sqLiteDatabase, this, month, year, totalIncome, totalExpense);
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error updating budget with income: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(incomeCursor);
            AppSettingsManager.closeCursor(expenseCursor);
        }
    }

    private void showTypeListDialog() {
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("SELECT type_name FROM type ORDER BY type_name", null);
            ArrayList<String> typeList = new ArrayList<>();

            while (cursor.moveToNext()) {
                typeList.add(cursor.getString(0));
            }

            if (!typeList.isEmpty()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, typeList);

                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MaterialAlertDialog_Rounded);
                builder.setTitle("Income Types")
                       .setAdapter(adapter, null)
                       .setPositiveButton("OK", null)
                       .show();
            } else {
                showNoTypesAvailableDialog();
            }
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error loading types: " + e.getMessage());
        } finally {
            AppSettingsManager.closeCursor(cursor);
        }
    }

    private void showNoTypesAvailableDialog() {
        new AlertDialog.Builder(context, R.style.MaterialAlertDialog_Rounded)
            .setTitle("No Types")
            .setMessage("Please add a type first!")
            .setPositiveButton("OK", null)
            .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                selectedMonth = data.getIntExtra("selectedMonth", 0);
                selectedYear = data.getIntExtra("selectedYear", 0);
                updateIncomeHeader();
            }
        }
    }
}
