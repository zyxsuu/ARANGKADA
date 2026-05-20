package com.example.arangkada;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.example.arangkada.database.DatabaseHelper;
import com.example.arangkada.services.TrackingService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper db;
    ShiftFinancialManager shiftManager;
    BroadcastReceiver receiver;

    private double liveDistance = 0;
    private boolean isShiftActive = false;
    
    // UI Elements
    private TextView tvLiveDistance, tvOdometer, tvOnShift;
    private Button btnEndShift, btnLiveMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        shiftManager = new ShiftFinancialManager(this);
        
        tvLiveDistance = findViewById(R.id.tvLiveDistance);
        tvOdometer = findViewById(R.id.tvOdometer);
        tvOnShift = findViewById(R.id.tvOnShift);
        btnEndShift = findViewById(R.id.btnEndShift);
        btnLiveMap = findViewById(R.id.btnLiveMap);
        
        btnEndShift.setOnClickListener(v -> {
            if (isShiftActive) {
                showEndShiftDialog();
            } else {
                startShift();
            }
        });

        btnLiveMap.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MapActivity.class));
        });

        android.widget.ImageView ivNotification = findViewById(R.id.ivNotification);
        if (ivNotification != null) {
            ivNotification.setOnClickListener(v -> 
                Toast.makeText(this, "No new notifications", Toast.LENGTH_SHORT).show()
            );
        }

        if (checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    1
            );
        }

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                liveDistance =
                        intent.getDoubleExtra(
                                "distance",
                                0
                        );

                if (tvLiveDistance != null) {
                    tvLiveDistance.setText(String.format(java.util.Locale.US, "%.2f km", liveDistance));
                }
            }
        };

        registerReceiver(
                receiver,
                new IntentFilter("LOCATION_UPDATE"),
                Context.RECEIVER_NOT_EXPORTED
        );

        setupBottomNavigation();
        updateShiftUI();
    }

    private void updateShiftUI() {
        if (isShiftActive) {
            btnEndShift.setText("End shift");
            btnEndShift.setBackgroundResource(R.drawable.bg_button_red);
            if (tvOnShift != null) tvOnShift.setText("On shift");
        } else {
            btnEndShift.setText("Start shift");
            btnEndShift.setBackgroundResource(R.drawable.bg_button_green);
            if (tvOnShift != null) tvOnShift.setText("Offline");
        }
    }
    
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNavigationView.OnItemSelectedListener navListener = item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
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
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        };

        bottomNav.setOnItemSelectedListener(navListener);
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDashboardStats();
    }

    private void updateDashboardStats() {
        if (tvOdometer != null) {
            double currentOdo = shiftManager.getCurrentOdometer();
            tvOdometer.setText(String.format(java.util.Locale.US, "%,.0f km", currentOdo));
        }

        // Fetch Shift summary stats
        TextView tvDashProfit = findViewById(R.id.tvNetProfit);
        TextView tvDashDist = findViewById(R.id.tvDistanceValue);
        TextView tvDashGas = findViewById(R.id.tvGasExpense);
        
        double totalEarnings = 0;
        double totalGas = 0;
        double totalDistance = 0;
        double totalLiters = 0;

        android.widget.LinearLayout recentShiftsContainer = findViewById(R.id.llRecentShifts);
        if (recentShiftsContainer != null) recentShiftsContainer.removeAllViews();

        android.database.Cursor cursor = db.getAllShifts();
        if (cursor != null && cursor.moveToFirst()) {
            int count = 0;
            do {
                double gross = cursor.getDouble(cursor.getColumnIndexOrThrow("gross_earnings"));
                double gas = cursor.getDouble(cursor.getColumnIndexOrThrow("gas_expense"));
                double dist = cursor.getDouble(cursor.getColumnIndexOrThrow("distance"));
                
                totalEarnings += gross;
                totalGas += gas;
                totalDistance += dist;
                totalLiters += cursor.getDouble(cursor.getColumnIndexOrThrow("liters_fuel"));
                
                // Add up to 3 recent shifts to the dashboard UI
                if (count < 3 && recentShiftsContainer != null) {
                    View shiftView = android.view.LayoutInflater.from(this).inflate(R.layout.item_shift_history, recentShiftsContainer, false);
                    
                    TextView tvDate = shiftView.findViewById(R.id.tvRepDate);
                    tvDate.setText(cursor.getString(cursor.getColumnIndexOrThrow("date")));
                    
                    TextView tvStats = shiftView.findViewById(R.id.tvRepStats);
                    double eff = cursor.getDouble(cursor.getColumnIndexOrThrow("fuel_efficiency"));
                    tvStats.setText(String.format(java.util.Locale.US, "%.1f km • %.1f Km/L", dist, eff));
                    
                    TextView tvProfit = shiftView.findViewById(R.id.tvRepProfit);
                    tvProfit.setText(String.format(java.util.Locale.US, "+ ₱%.0f", (gross - gas)));
                    
                    recentShiftsContainer.addView(shiftView);
                    count++;
                }

            } while (cursor.moveToNext());
            cursor.close();
        }

        if (tvDashProfit != null) {
            tvDashProfit.setText(String.format(java.util.Locale.US, "₱%,.0f", (totalEarnings - totalGas)));
        }
        if (tvDashDist != null) {
            tvDashDist.setText(String.format(java.util.Locale.US, "%,.0f", totalDistance));
        }
        if (tvDashGas != null) {
            tvDashGas.setText(String.format(java.util.Locale.US, "₱%,.0f", totalGas));
        }
        
        // Dynamically pull the user's setup name to populate the greeting
        android.content.SharedPreferences prefs = getSharedPreferences("ArangkadaPrefs", Context.MODE_PRIVATE);
        String name = prefs.getString("user_name", "Rider");
        
        TextView tvUserName = findViewById(R.id.tvUserName);
        if (tvUserName != null) {
            tvUserName.setText(name + "!");
        }

        // Auto-generate 2-letter Initials for the top right red circle bubble (Dashboard)
        android.widget.FrameLayout cvProfilePic = findViewById(R.id.cvProfilePic);
        if (cvProfilePic != null && cvProfilePic.getChildCount() > 0) {
            android.view.View child = cvProfilePic.getChildAt(0);
            if (child instanceof TextView) {
                String initials = "";
                String[] parts = name.split(" ");
                if (parts.length >= 2 && parts[1].length() > 0) {
                    initials = parts[0].substring(0,1) + parts[1].substring(0,1);
                } else if (name.length() > 0) {
                    initials = name.substring(0, Math.min(name.length(), 2));
                }
                ((TextView) child).setText(initials.toUpperCase());
            }
        }
        
        // Setup Sinking Fund math
        TextView tvSinkingGoalPercent = findViewById(R.id.tvSinkingGoalPercent);
        TextView tvFundName           = findViewById(R.id.tvFundName);
        TextView tvFundDeduction      = findViewById(R.id.tvFundDeduction);
        TextView tvFundSaved          = findViewById(R.id.tvFundSaved);
        TextView tvFundGoalAmount     = findViewById(R.id.tvFundGoalAmount);
        android.widget.ProgressBar pbSinkingFund = findViewById(R.id.pbSinkingFund);
        
        String goalName = prefs.getString("user_goal", "No Goal");
        String percentStr = prefs.getString("setup_sinking_fund", "0%");
        
        // Strip out any `%` signs to do math
        double deductionPercent = 0.0;
        try { deductionPercent = Double.parseDouble(percentStr.replace("%", "").trim()) / 100.0; } catch (Exception ignored) {}
        
        // In reality, this goal target value might be set in another screen. For now we will assume the profile text field `user_goal` handles strings like "PC Set" and we can't extract numbers from it natively if they typed words. 
        // We will mock the target goal amount at 10,000 for demonstration mechanics since the actual design field `user_goal` took text instead of numeric targets.
        double goalAmount = 10000.0; 
        
        // Fund math: calculate total lifetime netprofit mathematically, and apply the percentage slice.
        double lifetimeNet = (totalEarnings - totalGas);
        double totalSaved = lifetimeNet * deductionPercent;
        int pctComplete = (int) Math.min(100, Math.max(0, Math.round((totalSaved / goalAmount) * 100)));

        if (tvFundName != null) tvFundName.setText(goalName);
        if (tvFundDeduction != null) tvFundDeduction.setText(percentStr + " per shift");
        if (tvFundSaved != null) tvFundSaved.setText(String.format(java.util.Locale.US, "₱%,.0f saved", totalSaved));
        if (tvFundGoalAmount != null) tvFundGoalAmount.setText(String.format(java.util.Locale.US, "Goal: ₱%,.0f", goalAmount));
        if (tvSinkingGoalPercent != null) tvSinkingGoalPercent.setText(pctComplete + "% of goal");
        if (pbSinkingFund != null) pbSinkingFund.setProgress(pctComplete);
    }
    
    private void showEndShiftDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("End Shift");

        final android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText inputEarnings = new EditText(this);
        inputEarnings.setHint("Gross Earnings (e.g. 1500)");
        inputEarnings.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputEarnings);

        final EditText inputGas = new EditText(this);
        inputGas.setHint("Gas Expense (e.g. 200)");
        inputGas.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputGas);
        
        final EditText inputLiters = new EditText(this);
        inputLiters.setHint("Liters Pumped (e.g. 3.5)");
        inputLiters.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputLiters);

        builder.setView(layout);

        builder.setPositiveButton("Save Shift", (dialog, which) -> {
            try {
                double earnings = Double.parseDouble(inputEarnings.getText().toString());
                double gas = Double.parseDouble(inputGas.getText().toString());
                double liters = Double.parseDouble(inputLiters.getText().toString());
                
                endShift(earnings, gas, liters);
            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void startShift() {
        isShiftActive = true;
        updateShiftUI();

        TrackingService.currentDistance = 0;

        Intent startIntent =
                new Intent(
                        this,
                        TrackingService.class
                );

        startService(startIntent);
    }

    private void endShift(double grossEarnings, double gasExpense, double litersFuel) {

        Intent stopIntent =
                new Intent(
                        this,
                        TrackingService.class
                );

        stopService(stopIntent);

        double finalDistance =
                TrackingService.currentDistance;

        ShiftFinancialManager.ShiftResult result = shiftManager.processShift(
                finalDistance,
                grossEarnings,
                gasExpense,
                litersFuel
        );

        Toast.makeText(this, "Shift Saved! Net Profit: ₱" + result.netProfit, Toast.LENGTH_LONG).show();
        
        isShiftActive = false;
        liveDistance = 0;
        if (tvLiveDistance != null) tvLiveDistance.setText("--.-- km");
        
        // Refresh UI
        updateShiftUI();
        updateDashboardStats();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }
}