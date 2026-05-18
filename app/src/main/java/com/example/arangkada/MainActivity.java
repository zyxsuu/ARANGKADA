package com.example.arangkada;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DatabaseHelper db =
                new DatabaseHelper(this);

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