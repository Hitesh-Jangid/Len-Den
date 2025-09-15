package com.hiteshjangid.lenden.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public class AppSettingsManager {
    
        
    public static class ValidationLimits {
        public static final int MAX_CATEGORY_LENGTH = 50;
        public static final int MAX_TYPE_LENGTH = 50;
        public static final int MAX_AMOUNT_LENGTH = 20;
        public static final int MAX_NOTE_LENGTH = 200;
        public static final int MAX_CATEGORY_LENGTH_EXPENSE = 100;
        public static final int MAX_NOTE_LENGTH_EXPENSE = 500;
        public static final double MAX_AMOUNT_VALUE = 999999999.0;
        public static final double MIN_AMOUNT_VALUE = 0.0;
        public static final int MIN_YEAR = 1900;
        public static final int MAX_YEAR = 2100;
        public static final int MIN_MONTH = 1;
        public static final int MAX_MONTH = 12;
        public static final int MIN_DAY = 1;
        public static final int MAX_DAY = 31;
    }
    
    // Centralized error messages to eliminate duplicates
    public static final class ErrorMessages {
        public static final String INVALID_CATEGORY = "Please enter a valid category (max %d characters)";
        public static final String INVALID_TYPE = "Please select a valid type (max %d characters)";
        public static final String INVALID_AMOUNT_EMPTY = "Please enter amount";
        public static final String INVALID_AMOUNT_LENGTH = "Please enter a valid amount (max %d characters)";
        public static final String INVALID_AMOUNT_RANGE = "Amount must be between %.0f and %.0f";
        public static final String INVALID_DATE_EMPTY = "Please select date";
        public static final String INVALID_DATE_FORMAT = "Please enter a valid date in DD-MM-YYYY format";
        public static final String INVALID_NOTE_LENGTH = "Note too long (max %d characters)";
        public static final String TYPE_NAME_EMPTY = "Please enter a type name";
    }

    private static final String PREF_NAME = "AppSettings";
    private static final String DB_NAME = "Len-Den_Database";

    // Database helper methods with improved error handling
    @NonNull
    public static SQLiteDatabase getDatabase(@NonNull Context context) {
        try {
            SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
            if (db == null) {
                throw new SQLiteException("Failed to open database");
            }
            return db;
        } catch (SQLiteException e) {
            showToast(context, "Database error: " + e.getMessage());
            throw e;
        }
    }

    public static void closeDatabase(@Nullable SQLiteDatabase database) {
        if (database != null && database.isOpen()) {
            try {
                database.close();
            } catch (Exception e) {
                // Log error but don't crash
            }
        }
    }

    public static void showToast(@NonNull Context context, @NonNull String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    // Safe cursor operations with enhanced null checking
    public static void closeCursor(@Nullable Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            try {
                cursor.close();
            } catch (Exception e) {
                // Log error but don't crash - cursor might already be closed
            }
        }
    }

    // Safe database query helper to prevent SQL injection
    public static Cursor safeQuery(@NonNull SQLiteDatabase db, @NonNull String query, @Nullable String[] selectionArgs) {
        if (db == null || !db.isOpen()) {
            return null;
        }
        try {
            return db.rawQuery(query, selectionArgs);
        } catch (Exception e) {
            return null;
        }
    }

    // Safe integer parsing with fallback
    public static int safeParseInt(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // Safe double parsing with fallback
    public static double safeParseDouble(String value, double defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // Month handling with validation
    public static int getSelectedMonth(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int month = sharedPreferences.getInt("selectedMonth", Calendar.getInstance().get(Calendar.MONTH));
        return Math.max(0, Math.min(11, month)); // Ensure valid range
    }

    @NonNull
    public static String getSelectedMonthName(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String monthName = sharedPreferences.getString("selectedMonthName", "");
        if (monthName.isEmpty()) {
            monthName = getMonthName(getSelectedMonth(context));
            saveSelectedMonthAndYear(context, getSelectedMonth(context), getSelectedYear(context));
        }
        return monthName;
    }

    public static int getSelectedYear(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int year = sharedPreferences.getInt("selectedYear", Calendar.getInstance().get(Calendar.YEAR));
        return Math.max(2020, Math.min(2030, year)); // Reasonable year range
    }

    public static int getSelectedMonthForDatabase(@NonNull Context context) {
        return getSelectedMonth(context) + 1; // Convert 0-based to 1-based for database
    }

    public static void saveSelectedMonthAndYearForDatabase(@NonNull Context context, int newSelectedMonth, int newSelectedYear) {
        // newSelectedMonth should be 1-based from database, convert to 0-based for storage
        saveSelectedMonthAndYear(context, newSelectedMonth - 1, newSelectedYear);
    }

    public static void saveSelectedMonthAndYear(@NonNull Context context, int newSelectedMonth, int newSelectedYear) {
        // Validate inputs
        newSelectedMonth = Math.max(0, Math.min(11, newSelectedMonth));
        newSelectedYear = Math.max(2020, Math.min(2030, newSelectedYear));

        String newSelectedMonthName = getMonthName(newSelectedMonth);

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("selectedMonth", newSelectedMonth);
        editor.putInt("selectedYear", newSelectedYear);
        editor.putString("selectedMonthName", newSelectedMonthName);
        editor.apply();
    }

    @NonNull
    private static String getMonthName(int month) {
        DateFormatSymbols symbols = new DateFormatSymbols();
        String[] months = symbols.getMonths();
        return (month >= 0 && month < months.length) ? months[month] : "Unknown";
    }

    // Utility method for safe database operations
    public static boolean executeInTransaction(@NonNull SQLiteDatabase db, @NonNull Runnable operation) {
        if (db == null || !db.isOpen()) return false;
        
        db.beginTransaction();
        try {
            operation.run();
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (db.inTransaction()) {
                    db.endTransaction();
                }
            } catch (Exception e) {
                // Transaction might already be ended
            }
        }
    }

    // Enhanced transaction method with error handling
    public static boolean executeInTransactionWithCallback(@NonNull SQLiteDatabase db, 
                                                         @NonNull Runnable operation, 
                                                         @NonNull Context context,
                                                         @NonNull String errorMessage) {
        if (db == null || !db.isOpen()) {
            showToast(context, "Database not available");
            return false;
        }
        
        db.beginTransaction();
        try {
            operation.run();
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            showToast(context, errorMessage + ": " + e.getMessage());
            return false;
        } finally {
            try {
                if (db.inTransaction()) {
                    db.endTransaction();
                }
            } catch (Exception e) {
                // Transaction might already be ended
            }
        }
    }

    // CENTRALIZED budget update logic to eliminate duplication
    public static void updateBudgetWithTransaction(@NonNull SQLiteDatabase db, @NonNull Context context,
                                                 int month, int year, int totalIncome, int totalExpense) {
        executeInTransactionWithCallback(db, () -> {
            ContentValues values = new ContentValues();
            values.put("income_money", totalIncome);
            values.put("expense_money", totalExpense);

            Cursor cursor = db.rawQuery("SELECT budget_amount FROM BudgetInfo WHERE month = ? AND year = ?",
                    new String[]{String.valueOf(month), String.valueOf(year)});
            
            if (cursor.moveToFirst()) {
                int budgetAmount = cursor.getInt(0);
                // CONSISTENT FORMULA: current_budget = budget_amount + income - expense
                values.put("current_budget", budgetAmount + totalIncome - totalExpense);
                // CONSISTENT FORMULA: total_saving = income - expense
                values.put("total_saving", totalIncome - totalExpense);
            }
            cursor.close();

            int result = db.update("BudgetInfo", values, "month = ? AND year = ?",
                    new String[]{String.valueOf(month), String.valueOf(year)});
            
            if (result == 0) {
                throw new RuntimeException("No budget entry found to update");
            }
        }, context, "Error updating budget");
    }

    // Centralized expense ID generation to prevent logical conflicts
    public static int generateUniqueExpenseId(SQLiteDatabase db, int year, int month) {
        if (db == null || !db.isOpen()) return 1;
        
        Cursor cursor = null;
        try {
            // Generate base ID using consistent formula
            int lastTwoDigits = year % 100;
            int baseId = (month * 100000) + (lastTwoDigits * 1000) + 1;
            
            // Get maximum existing ID for this month/year
            cursor = db.rawQuery("SELECT COALESCE(MAX(e_id), 0) FROM expenses WHERE year=? AND month=?",
                    new String[]{String.valueOf(year), String.valueOf(month)});
            
            int maxId = baseId;
            if (cursor.moveToFirst()) {
                int dbMaxId = cursor.getInt(0);
                if (dbMaxId >= baseId) {
                    maxId = dbMaxId + 1;
                }
            }
            
            // Double-check uniqueness to be absolutely safe
            closeCursor(cursor);
            cursor = db.rawQuery("SELECT COUNT(*) FROM expenses WHERE e_id=? AND year=? AND month=?",
                    new String[]{String.valueOf(maxId), String.valueOf(year), String.valueOf(month)});
            
            while (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                maxId++;
                closeCursor(cursor);
                cursor = db.rawQuery("SELECT COUNT(*) FROM expenses WHERE e_id=? AND year=? AND month=?",
                        new String[]{String.valueOf(maxId), String.valueOf(year), String.valueOf(month)});
            }
            
            return maxId;
        } catch (Exception e) {
            // Return safe fallback
            int lastTwoDigits = year % 100;
            return (month * 100000) + (lastTwoDigits * 1000) + (int)(System.currentTimeMillis() % 1000);
        } finally {
            closeCursor(cursor);
        }
    }

    // Centralized income ID generation with consistent format  
    public static int generateUniqueIncomeId(SQLiteDatabase db, int year, int month) {
        if (db == null || !db.isOpen()) return 1;
        
        Cursor cursor = null;
        try {
            // Generate base ID using consistent formula (similar to expense but offset to prevent conflicts)
            int lastTwoDigits = year % 100;
            int baseId = (month * 200000) + (lastTwoDigits * 2000) + 1; // Different multipliers to prevent conflicts with expenses
            
            // Get maximum existing ID for this month/year
            cursor = db.rawQuery("SELECT COALESCE(MAX(income_id), 0) FROM incomes WHERE year=? AND month=?",
                    new String[]{String.valueOf(year), String.valueOf(month)});
            
            int maxId = baseId;
            if (cursor.moveToFirst()) {
                int dbMaxId = cursor.getInt(0);
                if (dbMaxId >= baseId) {
                    maxId = dbMaxId + 1;
                }
            }
            
            // Double-check uniqueness to be absolutely safe
            closeCursor(cursor);
            cursor = db.rawQuery("SELECT COUNT(*) FROM incomes WHERE income_id=? AND year=? AND month=?",
                    new String[]{String.valueOf(maxId), String.valueOf(year), String.valueOf(month)});
            
            while (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                maxId++;
                closeCursor(cursor);
                cursor = db.rawQuery("SELECT COUNT(*) FROM incomes WHERE income_id=? AND year=? AND month=?",
                        new String[]{String.valueOf(maxId), String.valueOf(year), String.valueOf(month)});
            }
            
            return maxId;
        } catch (Exception e) {
            // Return safe fallback
            int lastTwoDigits = year % 100;
            return (month * 200000) + (lastTwoDigits * 2000) + (int)(System.currentTimeMillis() % 1000);
        } finally {
            closeCursor(cursor);
        }
    }

    // Centralized UI helper methods to prevent duplication across activities
    public static void setupDatePicker(android.widget.EditText dateInput, android.content.Context context) {
        dateInput.setOnClickListener(v -> showDatePickerDialog(dateInput, context));
        dateInput.setFocusable(false);
        dateInput.setClickable(true);
    }

    public static void setupAmountInput(android.widget.EditText amountInput) {
        amountInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    public static void showDatePickerDialog(android.widget.EditText dateInput, android.content.Context context) {
        final java.util.Calendar calendar = java.util.Calendar.getInstance();
        int year = calendar.get(java.util.Calendar.YEAR);
        int month = calendar.get(java.util.Calendar.MONTH);
        int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(context,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = String.format(java.util.Locale.getDefault(), "%02d-%02d-%04d", dayOfMonth, monthOfYear + 1, year1);
                    dateInput.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    // Centralized input sanitization to prevent SQL injection
    public static String sanitizeInput(String input) {
        if (input == null) return "";
        return input.trim()
                .replace("'", "''")  // Escape single quotes for SQL
                .replace("\n", " ")   // Replace newlines with spaces
                .replace("\r", " ")   // Replace carriage returns
                .replaceAll("\\s+", " ");  // Replace multiple whitespaces with single space
    }

    // Centralized validation methods to eliminate duplicate logic
    public static boolean validateCategory(String category, Context context, boolean isExpense) {
        int maxLength = isExpense ? ValidationLimits.MAX_CATEGORY_LENGTH_EXPENSE : ValidationLimits.MAX_CATEGORY_LENGTH;
        if (category.isEmpty() || category.length() > maxLength) {
            showToast(context, String.format(ErrorMessages.INVALID_CATEGORY, maxLength));
            return false;
        }
        return true;
    }

    public static boolean validateType(String type, Context context) {
        if (type.isEmpty() || type.length() > ValidationLimits.MAX_TYPE_LENGTH) {
            showToast(context, String.format(ErrorMessages.INVALID_TYPE, ValidationLimits.MAX_TYPE_LENGTH));
            return false;
        }
        return true;
    }

    public static boolean validateAmount(String amount, Context context) {
        if (amount.isEmpty()) {
            showToast(context, ErrorMessages.INVALID_AMOUNT_EMPTY);
            return false;
        }
        if (amount.length() > ValidationLimits.MAX_AMOUNT_LENGTH) {
            showToast(context, String.format(ErrorMessages.INVALID_AMOUNT_LENGTH, ValidationLimits.MAX_AMOUNT_LENGTH));
            return false;
        }
        
        double amountValue = safeParseDouble(amount, -1);
        if (amountValue <= ValidationLimits.MIN_AMOUNT_VALUE || amountValue > ValidationLimits.MAX_AMOUNT_VALUE) {
            showToast(context, String.format(ErrorMessages.INVALID_AMOUNT_RANGE, ValidationLimits.MIN_AMOUNT_VALUE, ValidationLimits.MAX_AMOUNT_VALUE));
            return false;
        }
        return true;
    }

    public static boolean validateDate(String date, Context context) {
        if (date.isEmpty()) {
            showToast(context, ErrorMessages.INVALID_DATE_EMPTY);
            return false;
        }
        
        if (!isValidDateFormat(date)) {
            showToast(context, ErrorMessages.INVALID_DATE_FORMAT);
            return false;
        }
        return true;
    }

    public static boolean validateNote(String note, Context context, boolean isExpense) {
        int maxLength = isExpense ? ValidationLimits.MAX_NOTE_LENGTH_EXPENSE : ValidationLimits.MAX_NOTE_LENGTH;
        if (note.length() > maxLength) {
            showToast(context, String.format(ErrorMessages.INVALID_NOTE_LENGTH, maxLength));
            return false;
        }
        return true;
    }

    public static boolean isValidDateFormat(String date) {
        if (date == null || date.trim().isEmpty()) return false;
        
        String[] parts = date.split("-");
        if (parts.length != 3) return false;
        
        try {
            int day = safeParseInt(parts[0], -1);
            int month = safeParseInt(parts[1], -1);
            int year = safeParseInt(parts[2], -1);
            
            if (day < ValidationLimits.MIN_DAY || day > ValidationLimits.MAX_DAY || 
                month < ValidationLimits.MIN_MONTH || month > ValidationLimits.MAX_MONTH || 
                year < ValidationLimits.MIN_YEAR || year > ValidationLimits.MAX_YEAR) {
                return false;
            }
            
            return isValidDayForMonth(day, month, year);
        } catch (Exception e) {
            return false;
        }
    }
    
    
    private static boolean isValidDayForMonth(int day, int month, int year) {
        int[] daysInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        
        if (month == 2 && isLeapYear(year)) {
            return day >= 1 && day <= 29;
        }
        
        return day >= 1 && day <= daysInMonth[month - 1];
    }
    
    private static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }
}
