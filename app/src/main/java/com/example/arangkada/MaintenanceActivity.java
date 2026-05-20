package com.example.arangkada;

import android.content.Intent;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.arangkada.database.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MaintenanceActivity extends AppCompatActivity {

    private ShiftFinancialManager shiftManager;
    private DatabaseHelper dbHelper;
    private double currentOdometer = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance);
        
        shiftManager = new ShiftFinancialManager(this);
        dbHelper = new DatabaseHelper(this);

        findViewById(R.id.btnLogMaintenance).setOnClickListener(v -> showLogMaintenanceDialog());
        
        TextView btnEditIntervals = findViewById(R.id.btnEditIntervals);
        if (btnEditIntervals != null) {
            btnEditIntervals.setOnClickListener(v -> showEditIntervalsDialog());
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_maintenance);

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
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshMaintenanceData();
    }

    private void refreshMaintenanceData() {
        currentOdometer = shiftManager.getCurrentOdometer();

        TextView tvOdometer = findViewById(R.id.tvCurrentOdometer);
        tvOdometer.setText(String.format(Locale.US, "%,.0f km", currentOdometer));
        
        android.content.SharedPreferences prefs = getSharedPreferences("ArangkadaPrefs", Context.MODE_PRIVATE);
        double oilInterval = Double.parseDouble(prefs.getString("interval_oil", "1500"));
        double sparkInterval = Double.parseDouble(prefs.getString("interval_spark", "4000"));
        double carbInterval = Double.parseDouble(prefs.getString("interval_carb", "4000"));
        double brakeInterval = Double.parseDouble(prefs.getString("interval_brake", "10000"));
        double chainInterval = Double.parseDouble(prefs.getString("interval_chain", "10000"));

        // Update interval cards using generic text matching method since IDs were not preserved correctly
        updateIntervalCardDynamic("Engine Oil Change", oilInterval, currentOdometer);
        updateIntervalCardDynamic("Spark Plug Check", sparkInterval, currentOdometer);
        updateIntervalCardDynamic("Carb / throttle cleaning", carbInterval, currentOdometer);
        updateIntervalCardDynamic("Brake pad check", brakeInterval, currentOdometer);
        updateIntervalCardDynamic("Chain / belt check", chainInterval, currentOdometer);

        // Load logs dynamically
        LinearLayout logsContainer = findViewById(R.id.llMaintenanceLogsContainer);
        logsContainer.removeAllViews();

        Cursor cursor = dbHelper.getAllMaintenanceLogs();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String type = cursor.getString(cursor.getColumnIndexOrThrow("maintenance_type"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                String notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
                double mileage = cursor.getDouble(cursor.getColumnIndexOrThrow("mileage"));

                View logView = LayoutInflater.from(this).inflate(R.layout.item_maintenance_log, logsContainer, false);
                
                TextView title = logView.findViewById(R.id.tvLogTitle);
                title.setText(type);
                
                TextView desc = logView.findViewById(R.id.tvLogDateDesc);
                desc.setText(date + " • " + notes);
                
                TextView mileageTv = logView.findViewById(R.id.tvLogMileage);
                mileageTv.setText(String.format(Locale.US, "%,.0f km", mileage));

                logsContainer.addView(logView);
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private void updateIntervalCardDynamic(String type, double interval, double currentOdo) {
        // A robust custom View traverser to find the right blocks since IDs weren't strictly preserved
        ViewGroup rootView = findViewById(android.R.id.content);
        List<TextView> allTextViews = getAllTextViews(rootView);

        TextView titleView = null;
        for (TextView tv : allTextViews) {
            if (tv.getText().toString().equals(type)) {
                titleView = tv;
                break;
            }
        }
        
        if (titleView != null && titleView.getParent() instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) titleView.getParent();
            if (parent.getChildCount() >= 3) {
                // The structure is fixed: Title(0), Subtitle(1), Badge(2)
                TextView tvSubtitle = (TextView) parent.getChildAt(1);
                TextView tvBadge = (TextView) parent.getChildAt(2);

                int multiplesPassed = (int) Math.floor(currentOdo / interval);
                double nextDue = (multiplesPassed + 1) * interval;
                double remaining = nextDue - currentOdo;
        
                tvSubtitle.setText(String.format(Locale.US, "Every %,.0f km • Due at %,.0f km", interval, nextDue));
                tvBadge.setText(String.format(Locale.US, "%,.0f km", remaining));
        
                // If due within 200km, turn badge orange, else green
                if (remaining <= 200.0) {
                    tvBadge.setBackgroundResource(R.drawable.bg_badge_outline_orange);
                    tvBadge.setTextColor(android.graphics.Color.parseColor("#E65100"));
                } else {
                    tvBadge.setBackgroundResource(R.drawable.bg_badge_outline_green);
                    tvBadge.setTextColor(android.graphics.Color.parseColor("#006400"));
                }
            }
        }
    }
    
    private List<TextView> getAllTextViews(ViewGroup root) {
        List<TextView> textViews = new java.util.ArrayList<>();
        int pt = root.getChildCount();
        for (int i = 0; i < pt; i++) {
            View child = root.getChildAt(i);
            if (child instanceof TextView) {
                textViews.add((TextView) child);
            } else if (child instanceof ViewGroup) {
                textViews.addAll(getAllTextViews((ViewGroup) child));
            }
        }
        return textViews;
    }

    private void showEditIntervalsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Service Intervals");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        
        android.content.SharedPreferences prefs = getSharedPreferences("ArangkadaPrefs", Context.MODE_PRIVATE);

        final EditText inOil = new EditText(this);
        inOil.setHint("Engine Oil (Default: 1500)");
        inOil.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inOil.setText(prefs.getString("interval_oil", "1500"));
        layout.addView(createLabeledWrapper("Engine Oil Change (km):", inOil));

        final EditText inSpark = new EditText(this);
        inSpark.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inSpark.setText(prefs.getString("interval_spark", "4000"));
        layout.addView(createLabeledWrapper("Spark Plugs (km):", inSpark));
        
        final EditText inCarb = new EditText(this);
        inCarb.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inCarb.setText(prefs.getString("interval_carb", "4000"));
        layout.addView(createLabeledWrapper("Carb / Throttle (km):", inCarb));
        
        final EditText inBrake = new EditText(this);
        inBrake.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inBrake.setText(prefs.getString("interval_brake", "10000"));
        layout.addView(createLabeledWrapper("Brake Pads (km):", inBrake));
        
        final EditText inChain = new EditText(this);
        inChain.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inChain.setText(prefs.getString("interval_chain", "10000"));
        layout.addView(createLabeledWrapper("Chain / Belt (km):", inChain));

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            try {
                // Ensure no completely bogus values go through
                double oilCheck = Double.parseDouble(inOil.getText().toString());
            } catch (Exception e) {
                Toast.makeText(MaintenanceActivity.this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
                return;
            }
            prefs.edit()
                 .putString("interval_oil", inOil.getText().toString())
                 .putString("interval_spark", inSpark.getText().toString())
                 .putString("interval_carb", inCarb.getText().toString())
                 .putString("interval_brake", inBrake.getText().toString())
                 .putString("interval_chain", inChain.getText().toString())
                 .apply();
            Toast.makeText(this, "Service intervals saved!", Toast.LENGTH_SHORT).show();
            refreshMaintenanceData();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
    
    private View createLabeledWrapper(String labelStr, View inputView) {
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setPadding(0, 5, 0, 15);
        TextView tv = new TextView(this);
        tv.setText(labelStr);
        tv.setTextSize(12f);
        wrapper.addView(tv);
        wrapper.addView(inputView);
        return wrapper;
    }

    private void showLogMaintenanceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Log Maintenance");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        String[] options = {"Engine Oil Change", "Spark Plug Check", "Carb / throttle cleaning", "Brake pad check", "Chain / belt check"};
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options);
        spinner.setAdapter(adapter);
        layout.addView(spinner);

        EditText inputNotes = new EditText(this);
        inputNotes.setHint("Notes (e.g. Honda Click)");
        layout.addView(inputNotes);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String selectedService = spinner.getSelectedItem().toString();
            String notes = inputNotes.getText().toString();
            String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("maintenance_type", selectedService);
            values.put("mileage", currentOdometer);
            values.put("date", date);
            values.put("notes", notes);
            db.insert(DatabaseHelper.TABLE_MAINTENANCE, null, values);
            db.close();

            Toast.makeText(this, "Maintenance logged!", Toast.LENGTH_SHORT).show();
            refreshMaintenanceData();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
