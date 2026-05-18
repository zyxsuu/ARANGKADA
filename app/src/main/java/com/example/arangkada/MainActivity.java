package com.example.arangkada;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.arangkada.database.DatabaseHelper;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //THIS IS FOR THE BACKEND TESTING ONLY(JOSH)
        db = new DatabaseHelper(this);

        db.insertShift(
                "May 18",
                "8:00 AM",
                "4:00 PM",
                42.7,
                650,
                180,
                1.4,
                470,
                30.5
        );
    }
}