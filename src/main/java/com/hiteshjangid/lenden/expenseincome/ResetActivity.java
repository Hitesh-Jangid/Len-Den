package com.hiteshjangid.lenden.expenseincome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public class ResetActivity extends AppCompatActivity {
    final Context context = this;
    SQLiteDatabase sqLiteDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);
        Calendar c = Calendar.getInstance();
        final int month = c.get(Calendar.MONTH);
        final int year = c.get(Calendar.YEAR);
        sqLiteDatabase = openOrCreateDatabase("Len-den", MODE_PRIVATE, null);
        Button reset_expenses = (Button) findViewById(R.id.reset_expenses);
        Button reset_budget = (Button) findViewById(R.id.reset_budget);
        Button reset_liability = (Button) findViewById(R.id.reset_liabilities);
        Button reset_goals = (Button) findViewById(R.id.reset_goals);
        final String month_name = new DateFormatSymbols().getMonths()[month];
        TextView tx = (TextView)findViewById(R.id.reset_header);
        tx.setText("Reset Activities for ("+month_name+" "+year+"):");
        reset_expenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Reset Expenses");
                builder.setMessage("Are you Sure to reset Expenses for " + month_name + " " + year + " ? ");
                        builder.setCancelable(true);
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sqLiteDatabase.execSQL("update expenses set e_amount=0,e_mark=0 where e_mark=1 and budget_id in (select b_id from budget where b_month="+month+" and b_year="+year+");");
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        reset_budget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Reset Budget");
                builder.setMessage("Are you Sure to reset Budgets for " + month_name + " " + year + "?");
                builder.setCancelable(true);
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sqLiteDatabase.execSQL("update budget set b_cash=0,b_amount=0 where b_month="+month+" and b_year="+year+";");
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        reset_liability.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Reset Expenses");
                builder.setMessage("Are you Sure to reset Expenses for "+month_name+" "+year+"?");
                builder.setCancelable(true);
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sqLiteDatabase.execSQL("update expenses set e_amount=0 where e_mark=0 and budget_id in (select b_id from budget where b_month="+month+" and b_year="+year+");");
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        reset_goals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Reset Saving Goal");

                builder.setMessage("Are you Sure to reset Saving Goal for "+month_name+" "+year+"?");
                builder.setCancelable(true);
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sqLiteDatabase.execSQL("delete from savings where budget_id =(select b_id from budget where b_month="+month+" and b_year="+year+");");
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });


    }

}
