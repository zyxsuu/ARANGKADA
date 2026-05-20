package com.example.arangkada;

import android.content.Intent;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

    private SharedPreferences sharedPrefs;

    private TextView tvFullName;
    private TextView tvProfileModel;
    private TextView tvProfilePlate;
    private TextView tvProfileTarget;
    private TextView tvProfileGoal;
    private TextView tvProfilePhone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        sharedPrefs = getSharedPreferences("ArangkadaPrefs", Context.MODE_PRIVATE);

        // Bind TextViews
        tvFullName = findViewById(R.id.tvFullName);
        tvProfileModel = findViewById(R.id.tvProfileModel);
        tvProfilePlate = findViewById(R.id.tvProfilePlate);
        tvProfileTarget = findViewById(R.id.tvProfileTarget);
        tvProfileGoal = findViewById(R.id.tvProfileGoal);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);

        // Load data on startup
        loadProfileData();

        // Button clicks
        findViewById(R.id.btnEditProfile).setOnClickListener(v -> showEditProfileDialog());
        findViewById(R.id.btnLogOut).setOnClickListener(v -> logout());

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_profile);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_reports) {
                startActivity(new Intent(getApplicationContext(), ReportsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_map) {
                startActivity(new Intent(getApplicationContext(), MapActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_maintenance) {
                startActivity(new Intent(getApplicationContext(), MaintenanceActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    private void loadProfileData() {
        tvFullName.setText(sharedPrefs.getString("user_name", "Jaisen Josh Laparan"));
        tvProfileModel.setText(sharedPrefs.getString("user_model", "Honda Click 125i"));
        tvProfilePlate.setText(sharedPrefs.getString("user_plate", "ABC 1234"));
        
        String target = "₱" + sharedPrefs.getString("user_target", "1,200");
        tvProfileTarget.setText(target);

        tvProfileGoal.setText(sharedPrefs.getString("user_goal", "PC Set"));
        tvProfilePhone.setText(sharedPrefs.getString("user_phone", "09123456789"));
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Profile");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText inputName = new EditText(this);
        inputName.setHint("Full Name");
        inputName.setText(sharedPrefs.getString("user_name", ""));
        layout.addView(inputName);

        final EditText inputModel = new EditText(this);
        inputModel.setHint("Motorcycle Model");
        inputModel.setText(sharedPrefs.getString("user_model", ""));
        layout.addView(inputModel);

        final EditText inputPlate = new EditText(this);
        inputPlate.setHint("Plate Number");
        inputPlate.setText(sharedPrefs.getString("user_plate", ""));
        layout.addView(inputPlate);

        final EditText inputTarget = new EditText(this);
        inputTarget.setHint("Daily Target (e.g. 1000)");
        inputTarget.setText(sharedPrefs.getString("user_target", ""));
        layout.addView(inputTarget);
        
        final EditText inputGoal = new EditText(this);
        inputGoal.setHint("Savings Goal (e.g. Vacation)");
        inputGoal.setText(sharedPrefs.getString("user_goal", ""));
        layout.addView(inputGoal);

        final EditText inputPhone = new EditText(this);
        inputPhone.setHint("Phone Number");
        inputPhone.setText(sharedPrefs.getString("user_phone", ""));
        layout.addView(inputPhone);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString("user_name", inputName.getText().toString());
            editor.putString("user_model", inputModel.getText().toString());
            editor.putString("user_plate", inputPlate.getText().toString());
            editor.putString("user_target", inputTarget.getText().toString());
            editor.putString("user_goal", inputGoal.getText().toString());
            editor.putString("user_phone", inputPhone.getText().toString());
            editor.apply();

            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
            loadProfileData(); // refresh UI
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Optional: sharedPrefs.edit().clear().apply(); if you want to wipe on logout
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    // Clear backstack so hitting back button doesn't return to profile
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
