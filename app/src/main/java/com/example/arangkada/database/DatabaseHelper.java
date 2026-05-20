package com.example.arangkada.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ArangkadaDB";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_RIDER = "rider_settings";
    public static final String TABLE_SHIFT = "shifts";
    public static final String TABLE_MAINTENANCE = "maintenance_logs";

    public static final String SHIFT_ID = "id";
    public static final String SHIFT_DATE = "date";
    public static final String SHIFT_START = "start_time";
    public static final String SHIFT_END = "end_time";
    public static final String SHIFT_DISTANCE = "distance";
    public static final String SHIFT_GROSS = "gross_earnings";
    public static final String SHIFT_GAS = "gas_expense";
    public static final String SHIFT_LITERS = "liters_fuel";
    public static final String SHIFT_NET = "net_profit";
    public static final String SHIFT_EFFICIENCY = "fuel_efficiency";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createRiderTable =
                "CREATE TABLE " + TABLE_RIDER + "(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "rider_name TEXT," +
                        "motorcycle_model TEXT," +
                        "odometer REAL," +
                        "savings_percentage REAL" +
                        ")";

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
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

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

        values.put(SHIFT_DATE, date);
        values.put(SHIFT_START, startTime);
        values.put(SHIFT_END, endTime);
        values.put(SHIFT_DISTANCE, distance);
        values.put(SHIFT_GROSS, grossEarnings);
        values.put(SHIFT_GAS, gasExpense);
        values.put(SHIFT_LITERS, litersFuel);
        values.put(SHIFT_NET, netProfit);
        values.put(SHIFT_EFFICIENCY, fuelEfficiency);

        long result = db.insert(TABLE_SHIFT, null, values);

        return result != -1;
    }

    public boolean saveShiftMinimal(
            String date,
            String startTime,
            String endTime,
            double distance
    ) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(SHIFT_DATE, date);
        values.put(SHIFT_START, startTime);
        values.put(SHIFT_END, endTime);
        values.put(SHIFT_DISTANCE, distance);

        long result = db.insert(TABLE_SHIFT, null, values);

        return result != -1;
    }

    public Cursor getAllShifts() {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(
                TABLE_SHIFT,
                null,
                null,
                null,
                null,
                null,
                SHIFT_ID + " DESC"
        );
    }

    public Cursor getAllMaintenanceLogs() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_MAINTENANCE,
                null,
                null,
                null,
                null,
                null,
                "id DESC"
        );
    }
}
