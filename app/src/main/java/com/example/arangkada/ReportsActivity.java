package com.example.arangkada;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.arangkada.database.DatabaseHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Locale;

public class ReportsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        dbHelper = new DatabaseHelper(this);

        userEmail = getSharedPreferences(
                "ArangkadaPrefs",
                MODE_PRIVATE
        ).getString(
                "user_email",
                "sample@gmail.com"
        );

        findViewById(R.id.btnExportCSV).setOnClickListener(v -> 
            Toast.makeText(this, "Exporting CSV... (Mock Download)", Toast.LENGTH_SHORT).show()
        );

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_reports);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_reports) {
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
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReportData();
    }

    private void loadReportData() {
        double totalEarnings = 0;
        double totalNetProfit = 0;
        double totalDistance = 0;
        double totalGas = 0;
        int shiftCount = 0;

        LinearLayout historyContainer = findViewById(R.id.llShiftHistoryContainer);
        historyContainer.removeAllViews();
        
        ArrayList<BarEntry> netProfitEntries = new ArrayList<>();
        ArrayList<Entry> efficiencyEntries = new ArrayList<>();

        Cursor cursor =
                dbHelper.getAllShifts(
                        userEmail
                );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                shiftCount++;
                
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                double dist = cursor.getDouble(cursor.getColumnIndexOrThrow("distance"));
                double gross = cursor.getDouble(cursor.getColumnIndexOrThrow("gross_earnings"));
                double gas = cursor.getDouble(cursor.getColumnIndexOrThrow("gas_expense"));
                double net = cursor.getDouble(cursor.getColumnIndexOrThrow("net_profit"));
                double eff = cursor.getDouble(cursor.getColumnIndexOrThrow("fuel_efficiency"));

                totalEarnings += gross;
                totalNetProfit += net;
                totalDistance += dist;
                totalGas += gas;
                
                // Add sequentially (oldest to newest would require reversing logic, but we'll feed raw count for now)
                netProfitEntries.add(new BarEntry(shiftCount, (float) net));
                efficiencyEntries.add(new Entry(shiftCount, (float) eff));

                View logView = LayoutInflater.from(this).inflate(R.layout.item_shift_history, historyContainer, false);
                
                TextView tvDate = logView.findViewById(R.id.tvRepDate);
                tvDate.setText(date);
                
                TextView tvStats = logView.findViewById(R.id.tvRepStats);
                tvStats.setText(String.format(Locale.US, "%,.1f km • %,.1f Km/L", dist, eff));
                
                TextView tvProfit = logView.findViewById(R.id.tvRepProfit);
                tvProfit.setText(String.format(Locale.US, "+ ₱%,.0f", net));
                
                TextView tvGas = logView.findViewById(R.id.tvRepGas);
                tvGas.setText(String.format(Locale.US, "Gas: ₱%,.0f", gas));

                historyContainer.addView(logView);
                
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        setupGraphs(netProfitEntries, efficiencyEntries);

        // Update top grids UI
        TextView tvTotalEarn = findViewById(R.id.tvTotalEarnings);
        TextView tvTotalNet = findViewById(R.id.tvTotalNetProfit);
        TextView tvTotalDist = findViewById(R.id.tvTotalDistance);
        TextView tvTotalGasUI = findViewById(R.id.tvTotalGas);
        
        TextView tvTotalShifts = findViewById(R.id.tvTotalShifts);

        tvTotalEarn.setText(String.format(Locale.US, "₱%,.0f", totalEarnings));
        tvTotalNet.setText(String.format(Locale.US, "₱%,.0f", totalNetProfit));
        tvTotalDist.setText(String.format(Locale.US, "%,.0f km", totalDistance));
        tvTotalGasUI.setText(String.format(Locale.US, "₱%,.0f", totalGas));
        
        tvTotalShifts.setText(shiftCount + " shifts");
    }

    private void setupGraphs(ArrayList<BarEntry> netProfitEntries, ArrayList<Entry> efficiencyEntries) {
        BarChart barChart = findViewById(R.id.barChart);
        LineChart lineChart = findViewById(R.id.lineChart);
        
        if (netProfitEntries.isEmpty()) {
            // Fake data to make default UI look nice if database is empty
            netProfitEntries.add(new BarEntry(1, 400));
            netProfitEntries.add(new BarEntry(2, 600));
            netProfitEntries.add(new BarEntry(3, 800));
            netProfitEntries.add(new BarEntry(4, 300));
            netProfitEntries.add(new BarEntry(5, 780));
            
            efficiencyEntries.add(new Entry(1, 35));
            efficiencyEntries.add(new Entry(2, 38));
            efficiencyEntries.add(new Entry(3, 37));
            efficiencyEntries.add(new Entry(4, 40));
            efficiencyEntries.add(new Entry(5, 42));
        }

        // --- BAR CHART SETUP ---
        if (barChart != null) {
            BarDataSet barDataSet = new BarDataSet(netProfitEntries, "Net Profit");
            barDataSet.setColor(Color.parseColor("#88B1E0"));
            barDataSet.setDrawValues(false);

            BarData barData = new BarData(barDataSet);
            barData.setBarWidth(0.6f);

            barChart.setData(barData);
            barChart.getDescription().setEnabled(false);
            barChart.getLegend().setEnabled(false);
            barChart.setDrawGridBackground(false);
            
            XAxis xAxis = barChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setDrawAxisLine(false);
            xAxis.setTextSize(10f);
            
            barChart.getAxisLeft().setDrawGridLines(false);
            barChart.getAxisRight().setEnabled(false);
            barChart.invalidate();
        }

        // --- LINE CHART SETUP ---
        if (lineChart != null) {
            LineDataSet lineDataSet = new LineDataSet(efficiencyEntries, "Fuel Efficiency");
            lineDataSet.setColor(Color.parseColor("#00B14F"));
            lineDataSet.setLineWidth(3f);
            lineDataSet.setDrawCircles(false);
            lineDataSet.setDrawValues(false);
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

            LineData lineData = new LineData(lineDataSet);

            lineChart.setData(lineData);
            lineChart.getDescription().setEnabled(false);
            lineChart.getLegend().setEnabled(false);
            
            XAxis lineXAxis = lineChart.getXAxis();
            lineXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            lineXAxis.setDrawGridLines(false);
            lineXAxis.setDrawAxisLine(false);
            lineXAxis.setTextSize(10f);

            lineChart.getAxisLeft().setDrawGridLines(false);
            lineChart.getAxisRight().setEnabled(false);
            lineChart.invalidate();
        }
    }
}
