package com.hiteshjangid.lenden;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.hiteshjangid.lenden.databinding.ActivityFullscreenBinding;

public class FullscreenActivity extends AppCompatActivity {

    private ActivityFullscreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullscreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Hide the ActionBar (Toolbar)
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Modern back press handling
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitDialog();
            }
        });

        binding.dummyButton.setOnClickListener(v -> {
            showToast("Welcome To Len-den");
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finishAffinity();
        });
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> finishAffinity())
                .setNegativeButton("No", (dialog, which) -> dialog.cancel())
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
