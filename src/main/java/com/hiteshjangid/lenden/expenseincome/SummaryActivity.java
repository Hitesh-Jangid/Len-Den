package com.hiteshjangid.lenden.expenseincome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public class SummaryActivity extends AppCompatActivity {
    final Context context = this;
    SQLiteDatabase sqLiteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        sqLiteDatabase = openOrCreateDatabase("Len-Den", MODE_PRIVATE, null);
        Calendar c1 = Calendar.getInstance();
        final int month = c1.get(Calendar.MONTH);
        final int year = c1.get(Calendar.YEAR);
        final String month_name = new DateFormatSymbols().getMonths()[month];

        TextView c_budget = findViewById(R.id.c_budget);
        TextView a_budget = findViewById(R.id.a_budget);
        TextView expenses = findViewById(R.id.expenses);
        TextView liabilities = findViewById(R.id.liabilities);
        TextView c_savings = findViewById(R.id.c_savings);
        TextView t_savings = findViewById(R.id.t_savings);

        // Retrieve budget information
        Cursor budgetCursor = sqLiteDatabase.rawQuery("SELECT b_amount, b_cash FROM budget WHERE b_month = ? AND b_year = ?", new String[]{String.valueOf(month), String.valueOf(year)});
        if (budgetCursor.moveToFirst()) {
            int amt = budgetCursor.getInt(0);
            c_budget.setText(String.valueOf(amt) + " ₹");
            int cash = budgetCursor.getInt(1);
            a_budget.setText(String.valueOf(cash) + " ₹");
        }
        budgetCursor.close();

        // Retrieve paid expenses
        Cursor paidExpensesCursor = sqLiteDatabase.rawQuery("SELECT SUM(e_amount) FROM expenses WHERE e_mark = 1 AND budget_id = (SELECT b_id FROM budget WHERE b_month = ? AND b_year = ?)", new String[]{String.valueOf(month), String.valueOf(year)});
        if (paidExpensesCursor.moveToFirst()) {
            int sum = paidExpensesCursor.getInt(0);
            expenses.setText(String.valueOf(sum) + " ₹");
        }
        paidExpensesCursor.close();

        // Retrieve unpaid expenses
        Cursor unpaidExpensesCursor = sqLiteDatabase.rawQuery("SELECT SUM(e_amount) FROM expenses WHERE e_mark = 0 AND budget_id = (SELECT b_id FROM budget WHERE b_month = ? AND b_year = ?)", new String[]{String.valueOf(month), String.valueOf(year)});
        if (unpaidExpensesCursor.moveToFirst()) {
            int sum = unpaidExpensesCursor.getInt(0);
            liabilities.setText(String.valueOf(sum) + " ₹");
        }
        unpaidExpensesCursor.close();

        // Retrieve liability details
        Cursor liabilityCursor = sqLiteDatabase.rawQuery("SELECT e_category_id, e_amount FROM expenses WHERE e_mark = 0 AND budget_id = (SELECT b_id FROM budget WHERE b_month = ? AND b_year = ?)", new String[]{String.valueOf(month), String.valueOf(year)});
        if (liabilityCursor.moveToFirst()) {
            do {
                int categoryId = liabilityCursor.getInt(0);
                int amount = liabilityCursor.getInt(1);
                Cursor categoryCursor = sqLiteDatabase.rawQuery("SELECT c_name FROM category WHERE c_id = ?", new String[]{String.valueOf(categoryId)});
                if (categoryCursor.moveToFirst()) {
                    String categoryName = categoryCursor.getString(0);
                    // Do something with the category name and amount
                }
                categoryCursor.close();
            } while (liabilityCursor.moveToNext());
        }
        liabilityCursor.close();

        // Retrieve savings information
        Cursor savingsCursor = sqLiteDatabase.rawQuery("SELECT s_target FROM savings WHERE budget_id = (SELECT b_id FROM budget WHERE b_month = ? AND b_year = ?)", new String[]{String.valueOf(month), String.valueOf(year)});
        if (savingsCursor.moveToFirst()) {
            int target = savingsCursor.getInt(0);
            budgetCursor = sqLiteDatabase.rawQuery("SELECT b_amount, b_cash FROM budget WHERE b_month = ? AND b_year = ?", new String[]{String.valueOf(month), String.valueOf(year)});
            if (budgetCursor.moveToFirst()) {
                int budget = budgetCursor.getInt(0);
                int cash = budgetCursor.getInt(1);
                int savings = budget - cash;
                c_savings.setText(String.valueOf(savings) + " ₹");
                t_savings.setText(String.valueOf(target) + " ₹");
            }
            budgetCursor.close();
        }
        savingsCursor.close();

        sqLiteDatabase.close();
    }
}
