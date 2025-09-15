package com.hiteshjangid.lenden;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hiteshjangid.lenden.databinding.ActivityResetBinding;
import com.hiteshjangid.lenden.util.AppSettingsManager;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

public class ResetActivity extends AppCompatActivity {

    private SQLiteDatabase sqLiteDatabase;
    private Context context;
    private int selectedMonth, selectedYear;
    private String selectedMonthFormatted;
    private String month_name;
    private ActivityResetBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityResetBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            initializeDatabase();
            setupUI();
            setupClickListeners();
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error initializing reset activity: " + e.getMessage());
            finish();
        }
    }

    private void initializeDatabase() {
        try {
            context = this;
            sqLiteDatabase = AppSettingsManager.getDatabase(this);
            selectedMonth = AppSettingsManager.getSelectedMonthForDatabase(this);
            selectedYear = AppSettingsManager.getSelectedYear(this);
            month_name = AppSettingsManager.getSelectedMonthName(this);
            selectedMonthFormatted = String.format("%02d-%04d", selectedMonth, selectedYear);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database: " + e.getMessage());
        }
    }

    private void setupUI() {
        try {
            TextView tx = binding.resetHeader;
            tx.setText("Reset\n(" + month_name + "-" + selectedYear + ")");
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error setting up UI: " + e.getMessage());
        }
    }

    private void setupClickListeners() {
        try {
            binding.resetBudget.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showConfirmationDialog("Reset Budget",
                        "Are you sure you want to reset the budget data for " + month_name + " " + selectedYear + "?",
                        () -> {
                            if (resetBudget()) {
                                showToast("Budget data reset successfully.");
                            }
                        });
                }
            });

            binding.resetExpenses.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showConfirmationDialog("Reset Expenses",
                        "Are you sure you want to reset the expense data for " + month_name + " " + selectedYear + "?",
                        () -> {
                            if (resetExpenses()) {
                                showToast("Expense data for " + month_name + " " + selectedYear + " reset successfully.");
                            }
                        });
                }
            });

            binding.resetIncomes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showConfirmationDialog("Reset Incomes",
                        "Are you sure you want to reset the income data for " + month_name + " " + selectedYear + "?",
                        () -> {
                            if (resetIncomes()) {
                                showToast("Income data for " + month_name + " " + selectedYear + " reset successfully.");
                            }
                        });
                }
            });

            binding.resetSavings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showConfirmationDialog("Reset Savings",
                        "Are you sure you want to reset the savings data for " + month_name + " " + selectedYear + "?",
                        () -> {
                            if (resetSavings()) {
                                showToast("Savings data reset successfully.");
                            }
                        });
                }
            });

            binding.resetCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showConfirmationDialog("Reset Categories",
                        "Are you sure you want to reset the category data? This will delete all categories.",
                        () -> {
                            if (resetCategories()) {
                                showToast("Category data reset successfully.");
                            }
                        });
                }
            });

            binding.resetType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showConfirmationDialog("Reset Income Types",
                        "Are you sure you want to reset the income type data? This will delete all income types.",
                        () -> {
                            if (resetIncomeTypes()) {
                                showToast("Income type data reset successfully.");
                            }
                        });
                }
            });
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error setting up click listeners: " + e.getMessage());
        }
    }

    private void showConfirmationDialog(String title, String message, Runnable positiveAction) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        try {
                            positiveAction.run();
                        } catch (Exception e) {
                            AppSettingsManager.showToast(ResetActivity.this,
                                "Error performing operation: " + e.getMessage());
                        }
                        dialog.dismiss();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error showing confirmation dialog: " + e.getMessage());
        }
    }

    private boolean resetBudget() {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) {
            AppSettingsManager.showToast(this, "Database not available");
            return false;
        }
        
        try {
            sqLiteDatabase.beginTransaction();
            String query = "UPDATE BudgetInfo SET budget_amount = 0, current_budget = 0, target_saving = 0, total_saving = 0 WHERE month = ? AND year = ?";
            sqLiteDatabase.execSQL(query, new Object[]{selectedMonth, selectedYear});
            sqLiteDatabase.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error resetting budget: " + e.getMessage());
            return false;
        } finally {
            try {
                if (sqLiteDatabase.inTransaction()) {
                    sqLiteDatabase.endTransaction();
                }
            } catch (Exception e) {
                // Transaction might already be ended
            }
        }
    }

    private boolean resetExpenses() {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) {
            AppSettingsManager.showToast(this, "Database not available");
            return false;
        }
        
        try {
            sqLiteDatabase.beginTransaction();
            String selectedMonthFormatted = String.format(Locale.US, "%02d-%04d", selectedMonth, selectedYear);
            String likePattern = "%" + selectedMonthFormatted + "%";
            String query = "UPDATE expenses SET status = 1 WHERE date LIKE ?";
            sqLiteDatabase.execSQL(query, new Object[]{likePattern});
            sqLiteDatabase.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error resetting expenses: " + e.getMessage());
            return false;
        } finally {
            try {
                if (sqLiteDatabase.inTransaction()) {
                    sqLiteDatabase.endTransaction();
                }
            } catch (Exception e) {
                // Transaction might already be ended
            }
        }
    }

    private boolean resetIncomes() {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) {
            AppSettingsManager.showToast(this, "Database not available");
            return false;
        }
        
        try {
            sqLiteDatabase.beginTransaction();
            String selectedMonthFormatted = String.format(Locale.US, "%02d-%04d", selectedMonth, selectedYear);
            String likePattern = "%" + selectedMonthFormatted + "%";
            // Use soft delete by setting status = 1 instead of hard delete
            String query = "UPDATE incomes SET status = 1 WHERE date LIKE ?";
            sqLiteDatabase.execSQL(query, new Object[]{likePattern});
            sqLiteDatabase.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error resetting incomes: " + e.getMessage());
            return false;
        } finally {
            try {
                if (sqLiteDatabase.inTransaction()) {
                    sqLiteDatabase.endTransaction();
                }
            } catch (Exception e) {
                // Transaction might already be ended
            }
        }
    }

    private boolean resetSavings() {
        try {
            sqLiteDatabase.beginTransaction();
            String query = "UPDATE BudgetInfo SET target_saving = 0 WHERE month = ? AND year = ?";
            sqLiteDatabase.execSQL(query, new Object[]{selectedMonth, selectedYear});
            sqLiteDatabase.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error resetting savings: " + e.getMessage());
            return false;
        } finally {
            try {
                sqLiteDatabase.endTransaction();
            } catch (Exception e) {
                // Transaction might already be ended
            }
        }
    }

    private boolean resetCategories() {
        try {
            sqLiteDatabase.beginTransaction();
            // Drop and recreate the category table
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS category");
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS category (c_id INTEGER PRIMARY KEY AUTOINCREMENT, c_name TEXT)");
            sqLiteDatabase.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error resetting categories: " + e.getMessage());
            return false;
        } finally {
            try {
                sqLiteDatabase.endTransaction();
            } catch (Exception e) {
                // Transaction might already be ended
            }
        }
    }

    private boolean resetIncomeTypes() {
        try {
            sqLiteDatabase.beginTransaction();
            // Drop and recreate the type table
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS type");
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS type (type_id INTEGER PRIMARY KEY AUTOINCREMENT, type_name TEXT)");
            sqLiteDatabase.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            AppSettingsManager.showToast(this, "Error resetting income types: " + e.getMessage());
            return false;
        } finally {
            try {
                sqLiteDatabase.endTransaction();
            } catch (Exception e) {
                // Transaction might already be ended
            }
        }
    }

    private void showToast(String message) {
        try {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // Toast might fail if context is not available
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppSettingsManager.closeDatabase(sqLiteDatabase);
    }
}
