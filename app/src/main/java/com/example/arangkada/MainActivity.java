package com.example.arangkada;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.arangkada.database.DatabaseHelper;
import com.example.arangkada.services.TrackingService;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper db;

    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                double distance = intent.getDoubleExtra("distance", 0);

                // update UI here (TextView later)
            }
        };

        registerReceiver(
                receiver,
                new IntentFilter("LOCATION_UPDATE"),
                Context.RECEIVER_NOT_EXPORTED
        );

        // START GPS SERVICE
        Intent startIntent = new Intent(this, TrackingService.class);
        startService(startIntent);

        // STOP  THE GPS SERVICE
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }
}