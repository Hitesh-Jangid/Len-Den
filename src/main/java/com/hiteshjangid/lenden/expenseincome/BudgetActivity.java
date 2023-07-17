package com.hiteshjangid.lenden.expenseincome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public class BudgetActivity extends AppCompatActivity {
    final Context context = this;
    SQLiteDatabase sqLiteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);
        sqLiteDatabase = openOrCreateDatabase("Len-Den", MODE_PRIVATE, null);
        Button createButton = findViewById(R.id.create_budget);
        Button updateButton = findViewById(R.id.update_budget);
        final Calendar c = Calendar.getInstance();
        final int month = c.get(Calendar.MONTH);
        final int year = c.get(Calendar.YEAR);
        final String month_name = new DateFormatSymbols().getMonths()[month];
        TextView tx = findViewById(R.id.budget_header);
        tx.setText("Budget\n(" + month_name + "-" + year + ")");

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor = sqLiteDatabase.rawQuery("SELECT b_month,b_year,b_amount FROM budget WHERE b_month = " + month + " AND b_year = " + year + ";", null);
                if (cursor == null || cursor.getCount() == 0) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setTitle("Create Budget");
                    alert.setMessage("Enter Your Amount");

                    final EditText input = new EditText(context);
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    alert.setView(input);

                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String amount = input.getText().toString();
                            Toast.makeText(context, "Budget Created", Toast.LENGTH_LONG).show();
                            sqLiteDatabase.execSQL("INSERT INTO budget(b_month,b_year,b_amount,b_cash) VALUES (" + month + "," + year + "," + amount + ",0);");
                        }
                    });

                    alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog alertDialog = alert.create();
                    alertDialog.show();
                } else {
                    cursor.moveToFirst();
                    int amount = cursor.getInt(2);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Budget");
                    builder.setMessage("Budget Already Created for " + month_name + " - " + year + " : " + amount + " ₹");
                    builder.setPositiveButton("Back", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                final int month = c.get(Calendar.MONTH);
                final int year = c.get(Calendar.YEAR);
                Cursor cursor = sqLiteDatabase.rawQuery("SELECT b_month,b_year,b_amount FROM budget WHERE b_month = " + month + " AND b_year = " + year + ";", null);
                if (cursor != null && cursor.getCount() > 0) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setTitle("Update Budget");
                    alert.setMessage("Enter Your Amount");

                    final EditText input = new EditText(context);
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    alert.setView(input);

                    alert.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String amount = input.getText().toString();
                            Toast.makeText(context, "Budget Updated", Toast.LENGTH_LONG).show();
                            sqLiteDatabase.execSQL("UPDATE budget SET b_amount = " + amount + ",b_cash=0 WHERE b_month = " + month + " AND b_year = " + year + ";");
                        }
                    });

                    alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog alertDialog = alert.create();
                    alertDialog.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Budget");
                    String month_name = new DateFormatSymbols().getMonths()[month];
                    builder.setMessage("Budget Not Available for " + month_name + " - " + year + "\nCreate New Budget First");
                    builder.setPositiveButton("Back", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });
    }
}
