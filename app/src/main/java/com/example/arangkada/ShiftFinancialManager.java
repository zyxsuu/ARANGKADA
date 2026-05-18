package com.example.arangkada;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.arangkada.database.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ShiftFinancialManager.java
 * ==========================
 * Backend computations for the ARANGKADA motorcycle rider shift-tracking app.
 *
 * Builds ON TOP of the existing database schema created by DatabaseSQL_ARANGKADA.java.
 * Does NOT modify or overwrite any of the groupmate's table definitions or helper methods.
 *
 * Handles three core flows:
 *   1. Post-Shift Financial Logic    → Net Profit & Fuel Efficiency calculations
 *   2. Virtual Odometer Logic        → Lifetime mileage accumulation
 *   3. Mileage-Based Alerts          → Threshold-triggered maintenance notifications
 *
 * All calculations use double-precision floating point for maximum accuracy.
 *
 * USAGE EXAMPLE (drop-in ready):
 *   ShiftFinancialManager sfm = new ShiftFinancialManager(context);
 *   sfm.processShift(45.0, 1200.0, 250.0, 4.5); // distance, gross, gas, liters
 */
public class ShiftFinancialManager {

    // ──────────────────────────────────────────────────────────────────────────
    // MAINTENANCE THRESHOLDS (in kilometers)
    // ──────────────────────────────────────────────────────────────────────────
    private static final double OIL_CHANGE_INTERVAL_KM      = 1500.0;
    private static final double SPARK_PLUG_INTERVAL_KM      = 4000.0;
    private static final double BRAKE_CHAIN_INTERVAL_KM     = 10000.0;

    private final DatabaseHelper dbHelper;
    private final SimpleDateFormat dateFormatter;

    /**
     * Constructor – requires a valid Android Context (Activity or Application).
     *
     * @param context  Any context from which a database can be opened.
     */
    public ShiftFinancialManager(Context context) {
        this.dbHelper = new DatabaseHelper(context);
        this.dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    // ======================================================================
    //  1. POST-SHIFT FINANCIAL LOGIC
    // ======================================================================

    /**
     * Calculate Net Profit for a shift.
     *
     * Formula: Net Profit = Gross Earnings − Gas Expense
     *
     * @param grossEarnings  Total peso earnings from the shift.
     * @param gasExpense     Total peso amount spent on gas.
     * @return               Net profit as a double (may be negative if gas > earnings).
     */
    public double calculateNetProfit(double grossEarnings, double gasExpense) {
        return grossEarnings - gasExpense;
    }

    /**
     * Calculate Fuel Efficiency (kilometers per liter).
     *
     * Formula: Fuel Efficiency (Km/L) = Shift Kilometers ÷ Liters of Fuel Pumped
     *
     * @param distanceKm    Total distance travelled during the shift in kilometers.
     * @param litersFuel    Total liters of fuel pumped/consumed.
     * @return              Kilometers per liter. Returns 0.0 if litersFuel is zero
     *                      to avoid division-by-zero errors.
     */
    public double calculateFuelEfficiency(double distanceKm, double litersFuel) {
        // Guard against division by zero – if no fuel was logged, efficiency is 0.
        if (litersFuel == 0.0) {
            return 0.0;
        }
        return distanceKm / litersFuel;
    }

    /**
     * Full post-shift processing: calculates net profit and fuel efficiency,
     * then inserts a new row into the "shifts" table with all computed values.
     *
     * @param date           Shift date as "yyyy-MM-dd" (pass null to auto-use today).
     * @param startTime      Start time string (e.g. "08:00 AM").
     * @param endTime        End time string (e.g. "05:00 PM").
     * @param distanceKm     Total kilometers ridden during shift.
     * @param grossEarnings  Gross peso earnings for the shift.
     * @param gasExpense     Peso amount spent on gas.
     * @param litersFuel     Liters of fuel pumped.
     * @return               The row ID of the newly inserted shift, or -1 on failure.
     */
    public long insertShift(String date,
                            String startTime,
                            String endTime,
                            double distanceKm,
                            double grossEarnings,
                            double gasExpense,
                            double litersFuel) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Compute the derived fields
        double netProfit       = calculateNetProfit(grossEarnings, gasExpense);
        double fuelEfficiency  = calculateFuelEfficiency(distanceKm, litersFuel);

        // Auto-date if caller didn't provide one
        if (date == null || date.isEmpty()) {
            date = dateFormatter.format(new Date());
        }

        ContentValues values = new ContentValues();
        values.put("date",            date);
        values.put("start_time",      startTime);
        values.put("end_time",        endTime);
        values.put("distance",        distanceKm);
        values.put("gross_earnings",  grossEarnings);
        values.put("gas_expense",     gasExpense);
        values.put("liters_fuel",     litersFuel);
        values.put("net_profit",      netProfit);
        values.put("fuel_efficiency", fuelEfficiency);

        long newRowId = db.insert(DatabaseHelper.TABLE_SHIFT, null, values);
        db.close();

        return newRowId;
    }

    /**
     * Convenience one-call method that processes a shift AND updates the
     * virtual odometer in a single step.
     *
     * @param distanceKm     Shift distance in kilometers.
     * @param grossEarnings  Gross earnings for the shift.
     * @param gasExpense     Gas expense in pesos.
     * @param litersFuel     Liters of fuel pumped.
     * @return               A ShiftResult object containing all computed values
     *                       and any triggered maintenance alerts.
     */
    public ShiftResult processShift(double distanceKm,
                                    double grossEarnings,
                                    double gasExpense,
                                    double litersFuel) {

        // 1. Insert shift record with computed financials
        long shiftId = insertShift(
                null,               // auto-date
                "00:00",            // placeholder – replace with actual GPS timestamps
                "00:00",
                distanceKm,
                grossEarnings,
                gasExpense,
                litersFuel
        );

        // 2. Update the virtual odometer
        double newOdometer = updateVirtualOdometer(distanceKm);

        // 3. Check for maintenance alerts based on the new mileage
        List<String> alerts = checkMaintenanceAlerts(newOdometer);

        // 4. Package everything into a result object
        ShiftResult result = new ShiftResult();
        result.shiftId        = shiftId;
        result.netProfit      = calculateNetProfit(grossEarnings, gasExpense);
        result.fuelEfficiency = calculateFuelEfficiency(distanceKm, litersFuel);
        result.updatedOdometer = newOdometer;
        result.alerts         = alerts;

        return result;
    }

    // ======================================================================
    //  2. VIRTUAL ODOMETER LOGIC
    // ======================================================================

    /**
     * Updates the running lifetime odometer mileage stored in the
     * "rider_settings" table.
     *
     * Reads the current odometer value from the first row of rider_settings,
     * adds the given shift distance, and writes the new total back.
     *
     * @param shiftDistanceKm  The kilometers ridden during the just-completed shift.
     * @return                 The updated lifetime odometer value.
     */
    public double updateVirtualOdometer(double shiftDistanceKm) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // ── Read current odometer from the rider_settings table ──
        double currentOdometer = 0.0;
        Cursor cursor = db.rawQuery(
                "SELECT " + DatabaseHelper.TABLE_RIDER + ".odometer " +
                        "FROM " + DatabaseHelper.TABLE_RIDER +
                        " LIMIT 1",
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            // Column index 0 because we only selected "odometer"
            currentOdometer = cursor.getDouble(0);
            cursor.close();
        } else {
            // No rider settings row exists yet – create one with initial values
            ContentValues initialRow = new ContentValues();
            initialRow.put("rider_name",         "Rider");
            initialRow.put("motorcycle_model",   "Unknown");
            initialRow.put("odometer",           0.0);
            initialRow.put("savings_percentage", 0.0);
            db.insert(DatabaseHelper.TABLE_RIDER, null, initialRow);
        }

        // ── Compute new odometer ──
        double newOdometer = currentOdometer + shiftDistanceKm;

        // ── Update the database ──
        ContentValues updatedValues = new ContentValues();
        updatedValues.put("odometer", newOdometer);
        db.update(DatabaseHelper.TABLE_RIDER, updatedValues, null, null);

        db.close();
        return newOdometer;
    }

    /**
     * Reads the current virtual odometer without modifying anything.
     *
     * @return The lifetime odometer value stored in rider_settings, or 0.0 if not set.
     */
    public double getCurrentOdometer() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        double odometer = 0.0;

        Cursor cursor = db.rawQuery(
                "SELECT " + DatabaseHelper.TABLE_RIDER + ".odometer " +
                        "FROM " + DatabaseHelper.TABLE_RIDER +
                        " LIMIT 1",
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            odometer = cursor.getDouble(0);
            cursor.close();
        }

        db.close();
        return odometer;
    }

    // ======================================================================
    //  3. MILEAGE-BASED ALERTS (MAINTENANCE CHECKER)
    // ======================================================================

    /**
     * Evaluates the updated odometer against all maintenance thresholds and
     * returns a list of alert messages that should be triggered.
     *
     * Each alert is generated only ONCE per threshold window. We check the
     * maintenance_logs table to see if that specific alert was already logged
     * for the current threshold range (i.e. the odometer bracket that the
     * alert belongs to).
     *
     * @param updatedOdometer  The vehicle's total lifetime mileage after the shift.
     * @return                 A List of human-readable alert strings. Empty if none.
     */
    public List<String> checkMaintenanceAlerts(double updatedOdometer) {
        List<String> triggeredAlerts = new ArrayList<>();

        // ── Oil Change – every 1500 km ──
        if (isThresholdCrossed(updatedOdometer, OIL_CHANGE_INTERVAL_KM)) {
            if (!isAlertAlreadyLogged("Oil Change", updatedOdometer, OIL_CHANGE_INTERVAL_KM)) {
                String msg = "OIL CHANGE DUE — Engine oil must be replaced. " +
                        "(Threshold: every " + (int) OIL_CHANGE_INTERVAL_KM + " km)";
                triggeredAlerts.add(msg);
                logMaintenanceAlert("Oil Change", updatedOdometer, msg);
            }
        }

        // ── Spark Plug & Throttle Body – every 4000 km ──
        if (isThresholdCrossed(updatedOdometer, SPARK_PLUG_INTERVAL_KM)) {
            if (!isAlertAlreadyLogged("Spark Plug & Throttle Body",
                    updatedOdometer, SPARK_PLUG_INTERVAL_KM)) {
                String msg = "SPARK PLUG & THROTTLE BODY CHECK — Inspect spark plugs and " +
                        "clean throttle body. (Threshold: every " +
                        (int) SPARK_PLUG_INTERVAL_KM + " km)";
                triggeredAlerts.add(msg);
                logMaintenanceAlert("Spark Plug & Throttle Body", updatedOdometer, msg);
            }
        }

        // ── Brake Pad & Belt/Chain Replacement – every 10000 km ──
        if (isThresholdCrossed(updatedOdometer, BRAKE_CHAIN_INTERVAL_KM)) {
            if (!isAlertAlreadyLogged("Brake Pad & Belt/Chain",
                    updatedOdometer, BRAKE_CHAIN_INTERVAL_KM)) {
                String msg = "BRAKE PAD & BELT/CHAIN REPLACEMENT — Replace brake pads, " +
                        "belts, and drive chain. (Threshold: every " +
                        (int) BRAKE_CHAIN_INTERVAL_KM + " km)";
                triggeredAlerts.add(msg);
                logMaintenanceAlert("Brake Pad & Belt/Chain", updatedOdometer, msg);
            }
        }

        return triggeredAlerts;
    }

    /**
     * Determines whether a repeating threshold interval has been crossed.
     *
     * For an interval of N km, the threshold is crossed when the odometer
     * reaches or passes any multiple of N (N, 2N, 3N, …).
     *
     * @param currentOdometer  The current lifetime odometer reading.
     * @param intervalKm       The maintenance interval in kilometers.
     * @return                 true if the threshold has just been crossed or passed.
     */
    private boolean isThresholdCrossed(double currentOdometer, double intervalKm) {
        // Handle edge case where interval is zero or negative
        if (intervalKm <= 0.0) {
            return false;
        }
        // How many full intervals have been completed
        int previousIntervals = (int) Math.floor(currentOdometer / intervalKm);
        // If we've completed at least 1 interval, threshold is crossed
        return previousIntervals >= 1;
    }

    /**
     * Checks the maintenance_logs table to prevent duplicate alerts for the
     * same threshold window (e.g. don't spam "Oil Change" every time we open
     * the app if it was already logged for the current 1500 km bracket).
     *
     * @param maintenanceType  The type string (e.g. "Oil Change").
     * @param currentOdo       The current odometer reading.
     * @param intervalKm       The interval of this maintenance type.
     * @return                 true if an alert for this bracket already exists.
     */
    private boolean isAlertAlreadyLogged(String maintenanceType,
                                         double currentOdo,
                                         double intervalKm) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Determine which bracket this odometer belongs to.
        // E.g. for 1500 km interval, bracket = floor(odometer / 1500) * 1500
        int bracketNumber = (int) Math.floor(currentOdo / intervalKm);
        double bracketFloor = bracketNumber * intervalKm;

        // Look for a log whose mileage is within 10 km of this bracket
        double tolerance = 10.0;
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_MAINTENANCE +
                        " WHERE maintenance_type = ?" +
                        " AND mileage BETWEEN ? AND ?",
                new String[]{
                        maintenanceType,
                        String.valueOf(bracketFloor - tolerance),
                        String.valueOf(bracketFloor + tolerance)
                }
        );

        boolean alreadyLogged = false;
        if (cursor != null && cursor.moveToFirst()) {
            alreadyLogged = cursor.getInt(0) > 0;
            cursor.close();
        }

        db.close();
        return alreadyLogged;
    }

    /**
     * Inserts a maintenance alert into the maintenance_logs table so we
     * do not re-trigger the same alert for the same odometer bracket.
     *
     * @param type       The maintenance type string.
     * @param mileage    The odometer reading when the alert was triggered.
     * @param notes      The alert message / notes.
     */
    private void logMaintenanceAlert(String type, double mileage, String notes) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("maintenance_type", type);
        values.put("mileage",          mileage);
        values.put("date",             dateFormatter.format(new Date()));
        values.put("notes",            notes);

        db.insert(DatabaseHelper.TABLE_MAINTENANCE, null, values);
        db.close();
    }

    /**
     * Returns the next upcoming maintenance threshold that has NOT yet been
     * passed, based on the current odometer.
     *
     * Useful for showing the rider a "Next service at X km" indicator.
     *
     * @param currentOdometer  The current lifetime mileage.
     * @return                 The next threshold in km, or -1 if all are passed.
     */
    public double getNextMaintenanceMileage(double currentOdometer) {
        double[] thresholds = {
                OIL_CHANGE_INTERVAL_KM,
                SPARK_PLUG_INTERVAL_KM,
                BRAKE_CHAIN_INTERVAL_KM
        };

        for (double threshold : thresholds) {
            // Calculate the next multiple of this threshold that hasn't been reached
            if (currentOdometer < threshold) {
                return threshold;
            } else {
                // Already past the first occurrence; find the next multiple
                int multiplesPassed = (int) Math.floor(currentOdometer / threshold);
                double nextMilestone = (multiplesPassed + 1) * threshold;
                // Only return if it's within a reasonable upcoming window
                if (nextMilestone > currentOdometer) {
                    return nextMilestone;
                }
            }
        }

        // All thresholds are far in the past – nothing to suggest
        return -1.0;
    }

    // ======================================================================
    //  INNER CLASS: ShiftResult
    //  A lightweight POJO that bundles all outputs from processShift().
    // ======================================================================

    /**
     * Data container returned by {@link #processShift(double, double, double, double)}.
     *
     * Fields:
     *   shiftId         – SQLite row ID of the inserted shift record.
     *   netProfit        – Computed net profit (Gross − Gas).
     *   fuelEfficiency   – Computed km/L (Distance ÷ Liters).
     *   updatedOdometer  – Lifetime odometer after this shift.
     *   alerts           – List of maintenance alert strings triggered.
     */
    public static class ShiftResult {
        public long shiftId;
        public double netProfit;
        public double fuelEfficiency;
        public double updatedOdometer;
        public List<String> alerts;

        /**
         * Returns true if any maintenance alerts were triggered.
         */
        public boolean hasAlerts() {
            return alerts != null && !alerts.isEmpty();
        }
    }
}