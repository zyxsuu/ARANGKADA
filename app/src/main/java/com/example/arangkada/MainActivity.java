package com.example.arangkada;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.arangkada.database.DatabaseHelper;
import com.example.arangkada.services.TrackingService;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper db;
    BroadcastReceiver receiver;

    private double liveDistance = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);

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

                // update TextView later
            }
        };

        registerReceiver(
                receiver,
                new IntentFilter("LOCATION_UPDATE"),
                Context.RECEIVER_NOT_EXPORTED
        );

        // TEMPORARY TESTING
        startShift();

        // TEST END SHIFT AFTER A FEW SECONDS MANUALLY
        // endShift();
    }

    private void startShift() {

        TrackingService.currentDistance = 0;

        Intent startIntent =
                new Intent(
                        this,
                        TrackingService.class
                );

        startService(startIntent);
    }

    private void endShift() {

        Intent stopIntent =
                new Intent(
                        this,
                        TrackingService.class
                );

        stopService(stopIntent);

        double finalDistance =
                TrackingService.currentDistance;

        db.saveShiftMinimal(
                "May 20",
                "8:00 AM",
                "4:00 PM",
                finalDistance
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }
}