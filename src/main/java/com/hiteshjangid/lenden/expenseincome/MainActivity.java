package com.hiteshjangid.lenden.expenseincome;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private SQLiteDatabase sqLiteDatabase;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        Calendar c = Calendar.getInstance();
        final int month = c.get(Calendar.MONTH);
        final int year = c.get(Calendar.YEAR);
        final String month_name = new DateFormatSymbols().getMonths()[month];

        sqLiteDatabase = openOrCreateDatabase("Len-Den", MODE_PRIVATE, null);
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS budget (b_id INTEGER PRIMARY KEY AUTOINCREMENT, b_month INTEGER, b_year INTEGER, b_amount INTEGER, b_cash INTEGER);");

        TextView tx = findViewById(R.id.available_budget);
        TextView tx1 = findViewById(R.id.balance);
        String temp = tx.getText().toString();
        temp = temp + "(" + month_name + " - " + year + ") :";
        tx.setText(temp);

        Cursor c1 = sqLiteDatabase.rawQuery("SELECT b_amount, b_cash FROM budget WHERE b_month = " + month + " AND b_year = " + year, null);
        if (c1.moveToFirst()) {
            int x = c1.getInt(0);
            int y = c1.getInt(1);
            int z = x - y;
            tx1.setText(String.valueOf(z) + " ₹");
        }

        TextView textView = findViewById(R.id.summary);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, SummaryActivity.class));
            }
        });

        ImageButton budget = findViewById(R.id.budget);
        ImageButton bills = findViewById(R.id.bills);
        ImageButton expenses = findViewById(R.id.expenses);
        ImageButton savings = findViewById(R.id.savings);
        ImageButton reports = findViewById(R.id.reports);

        budget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in1 = new Intent(getApplicationContext(), BudgetActivity.class);
                startActivity(in1);
            }
        });
        bills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in2 = new Intent(getApplicationContext(), ResetActivity.class);
                startActivity(in2);
            }
        });
        expenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in3 = new Intent(getApplicationContext(), ExpenseActivity.class);
                startActivity(in3);
            }
        });
        savings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in4 = new Intent(getApplicationContext(), SavingsActivity.class);
                startActivity(in4);
            }
        });
        reports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in5 = new Intent(getApplicationContext(), ReportsActivity.class);
                startActivity(in5);
            }
        });
    }
}
