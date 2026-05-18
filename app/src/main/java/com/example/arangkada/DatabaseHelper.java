package com.example.arangkada;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "RiderTrackerDB";
    private static final int DATABASE_VERSION = 1;

    // TABLE NAMES
    public static final String TABLE_RIDER = "rider_settings";
    public static final String TABLE_SHIFT = "shifts";
    public static final String TABLE_MAINTENANCE = "maintenance_logs";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // RIDER SETTINGS TABLE
        String createRiderTable =
                "CREATE TABLE " + TABLE_RIDER + "(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "rider_name TEXT," +
                        "motorcycle_model TEXT," +
                        "odometer REAL," +
                        "savings_percentage REAL" +
                        ")";

        // SHIFT HISTORY TABLE
        String createShiftTable =
                "CREATE TABLE " + TABLE_SHIFT + "(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "date TEXT," +
                        "start_time TEXT," +
                        "end_time TEXT," +
                        "distance REAL," +
                        "gross_earnings REAL," +
                        "gas_expense REAL," +
                        "liters_fuel REAL," +
                        "net_profit REAL," +
                        "fuel_efficiency REAL" +
                        ")";

        // MAINTENANCE TABLE
        String createMaintenanceTable =
                "CREATE TABLE " + TABLE_MAINTENANCE + "(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "maintenance_type TEXT," +
                        "mileage REAL," +
                        "date TEXT," +
                        "notes TEXT" +
                        ")";

        db.execSQL(createRiderTable);
        db.execSQL(createShiftTable);
        db.execSQL(createMaintenanceTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion,
                          int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RIDER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHIFT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MAINTENANCE);

        onCreate(db);

    }
    public boolean insertShift(
            String date,
            String startTime,
            String endTime,
            double distance,
            double grossEarnings,
            double gasExpense,
            double litersFuel,
            double netProfit,
            double fuelEfficiency
    ) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("date", date);
        values.put("start_time", startTime);
        values.put("end_time", endTime);
        values.put("distance", distance);
        values.put("gross_earnings", grossEarnings);
        values.put("gas_expense", gasExpense);
        values.put("liters_fuel", litersFuel);
        values.put("net_profit", netProfit);
        values.put("fuel_efficiency", fuelEfficiency);

        long result = db.insert(TABLE_SHIFT,
                null,
                values);

        return result != -1;
    }
}