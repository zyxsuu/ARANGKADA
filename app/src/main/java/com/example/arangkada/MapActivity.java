package com.example.arangkada;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.preference.PreferenceManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MapActivity extends AppCompatActivity {

    private MapView mMap;
    private MyLocationNewOverlay mLocationOverlay;
    private FusedLocationProviderClient fusedLocationClient;
    private BroadcastReceiver receiver;

    private TextView tvDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load OSMDroid configuration (required before setting content view)
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize OSMDroid MapView
        mMap = findViewById(R.id.mapFragment);
        mMap.setTileSource(TileSourceFactory.MAPNIK);
        mMap.setMultiTouchControls(true);
        mMap.getController().setZoom(15.0);

        // Add user location overlay
        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(ctx), mMap);
        mLocationOverlay.enableMyLocation();
        mMap.getOverlays().add(mLocationOverlay);

        findViewById(R.id.btnZoomIn).setOnClickListener(v -> {
            if (mMap != null) mMap.getController().zoomIn();
        });

        findViewById(R.id.btnZoomOut).setOnClickListener(v -> {
            if (mMap != null) mMap.getController().zoomOut();
        });

        setupLocationReceiver();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_map);

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

    private void setupLocationReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // When we receive fresh tracking updates from the service, we could animate map to it, but 
                // typically Maps APIs MyLocation layer seamlessly overrides anyway. We can hook Custom overlays here if needed.
            }
        };

        registerReceiver(
                receiver,
                new IntentFilter("LOCATION_UPDATE"),
                Context.RECEIVER_NOT_EXPORTED
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null) {
            mMap.onResume();
        }
        
        // Fetch location properly now that OSM view is up
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation().addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Location location = task.getResult();
                        GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        
                        // Add marker
                        Marker startMarker = new Marker(mMap);
                        startMarker.setPosition(startPoint);
                        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        startMarker.setTitle("Start of Shift");
                        mMap.getOverlays().add(startMarker);
                        
                        // Center camera
                        mMap.getController().setCenter(startPoint);
                    }
                });
            } else {
                 ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMap != null) {
            mMap.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }
}
