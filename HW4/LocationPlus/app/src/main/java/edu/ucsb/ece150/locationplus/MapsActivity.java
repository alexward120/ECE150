package edu.ucsb.ece150.locationplus;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback {

    private Geofence mGeofence;
    private final String GEOFENCING_REQUEST_ID = "Target Destination";
    private final List<String> GEOFENCE_REQUEST_LIST = Collections.unmodifiableList(Arrays.asList(GEOFENCING_REQUEST_ID));
    private Marker destination;
    private Circle geofenceCircle;
    private GeofencingClient mGeofencingClient;
    private PendingIntent mPendingIntent = null;

    private GnssStatus.Callback mGnssStatusCallback;
    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private Marker currentLocationMarker;
    private boolean autoRecenter = true;
    private boolean startup = false;
    private ViewFlipper viewFlipper;

    private FloatingActionButton fab;
    private ArrayAdapter adapter;
    private ArrayList<Satellite> satelliteList;
    private double latitudeFence;
    private double longitudeFence;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Setup view flipper
        viewFlipper = findViewById(R.id.view_flipper);
        Button mapViewButton = findViewById(R.id.map_view_button);
        ImageButton satelliteViewButton = findViewById(R.id.satelliteButton);
        mapViewButton.setOnClickListener(v -> viewFlipper.setDisplayedChild(0));
        satelliteViewButton.setOnClickListener(v -> viewFlipper.setDisplayedChild(1));
        fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setTitle("Confirm Deletion");
            String detailedMessage = "Are you sure you want to remove the geofence?";
            builder.setMessage(detailedMessage);
            builder.setPositiveButton("YES", (dialog, which) -> {
                removeGeofence();
                fab.setVisibility(View.INVISIBLE);
                dialog.dismiss();
            });
            builder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());
            builder.create().show();
        });
        //Setup satellite list
        satelliteList = new ArrayList<>();


        // Set up Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Set up Geofencing Client
        mGeofencingClient = LocationServices.getGeofencingClient(MapsActivity.this);

        // Set up Satellite List
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mGnssStatusCallback = new GnssStatus.Callback() {
            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                super.onSatelliteStatusChanged(status);
                satelliteList.clear();
                int satelliteCount = status.getSatelliteCount();

                for (int i = 0; i < satelliteCount; i++) {
                    satelliteList.add(new Satellite(status, i));
                }
            }
        };
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, satelliteList);
        ListView cardList = findViewById(R.id.cardList);
        cardList.setAdapter(adapter);
        cardList.setOnItemClickListener((parent, view, position, id) -> {
            Satellite selectedSatellite = (Satellite) adapter.getItem(position);
            if (selectedSatellite != null) {
                showSatelliteInfoDialog(selectedSatellite);
            }
        });

        // Set up Toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.appToolbar);
        setSupportActionBar(mToolbar);
        ToggleButton toggleButton = findViewById(R.id.action_toggle_auto_center);
        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> autoRecenter = isChecked);
    }

    private void showSatelliteInfoDialog(Satellite satellite) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Satellite Information")
                .setTitle(satellite.toString())
                .setMessage(satellite.showInfo())
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (latitudeFence != 0 && longitudeFence != 0) {
            LatLng destinationLoc = new LatLng(latitudeFence, longitudeFence);
            createGeofence(destinationLoc);
        }
        //  In addition, add a listener for long clicks (which is the starting point for
        // creating a Geofence for the destination and listening for transitions that indicate
        // arrival)
        mMap.setOnMapLongClickListener(latLng -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setTitle("Confirm Destination");
            String detailedMessage = "Set Position (" + latLng.latitude + "°, " + latLng.longitude + "°) as destination?";
            builder.setMessage(detailedMessage);
            builder.setPositiveButton("YES", (dialog, which) -> {
                createGeofence(latLng);
                dialog.dismiss();
            });
            builder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());
            builder.create().show();
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Geocoder geocoder = new Geocoder(getApplicationContext());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            assert addresses != null;
            String result = addresses.get(0).getAddressLine(0);
            LatLng latLng = new LatLng(latitude, longitude);
            if (currentLocationMarker == null) {
                currentLocationMarker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(result)
                        .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap(R.drawable.dot, 30, 30))));
            } else {
                currentLocationMarker.setPosition(latLng);
                currentLocationMarker.setTitle(result);
            }
            if (!startup) {
                startup = true;
                float desiredZoomLevel = 16.0f;
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, desiredZoomLevel));
            }
            if (autoRecenter && startup) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Resize the bitmap to the specified width and height
    private Bitmap resizeBitmap(int resourceId, int width, int height) {
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), resourceId);
        return Bitmap.createScaledBitmap(originalBitmap, width, height, false);
    }

    /*
     * The following three methods onProviderDisabled(), onProviderEnabled(), and onStatusChanged()
     * do not need to be implemented -- they must be here because this Activity implements
     * LocationListener.
     *
     * You may use them if you need to.
     */
    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    private void createGeofence(LatLng latLng) {
        fab.setVisibility(View.VISIBLE);
        removeGeofence();
        latitudeFence = latLng.latitude;
        longitudeFence = latLng.longitude;
        destination = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Destination")
                .snippet("Your destination"));
        float radius = 100.0f;
        long expiration = Geofence.NEVER_EXPIRE;
        int transitionTypes = Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL;
        int loiteringDelay = 1000;
        mGeofence = new Geofence.Builder()
                .setRequestId(GEOFENCING_REQUEST_ID)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(expiration)
                .setTransitionTypes(transitionTypes)
                .setLoiteringDelay(loiteringDelay)
                .build();
        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 123);
        }
        geofenceCircle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(radius)
                .strokeColor(Color.RED)
                .strokeWidth(2f)
                .fillColor(Color.argb(70, 255, 0, 0)));
        mGeofencingClient.addGeofences(getGeofenceRequest(mGeofence), getGeofencePendingIntent())
                .addOnSuccessListener(this, unused -> Log.d("geofence", "Geofence added successfully"))
                .addOnFailureListener(this, e -> Log.d("geofence", "Geofence failed to add"));
    }

    private GeofencingRequest getGeofenceRequest(Geofence geoFence) {
        // [TODO] Set the initial trigger (i.e. what should be triggered if the user is already
        // inside the Geofence when it is created)
        int initialTrigger = GeofencingRequest.INITIAL_TRIGGER_ENTER;

        return new GeofencingRequest.Builder()
                .setInitialTrigger(initialTrigger) // <--  Add triggers here
                .addGeofence(geoFence)
                .build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if(mPendingIntent != null)
            return mPendingIntent;

        Intent intent = new Intent(MapsActivity.this, GeofenceBroadcastReceiver.class);

        mPendingIntent = PendingIntent.getBroadcast(MapsActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mPendingIntent;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() throws SecurityException {
        super.onStart();

        // TODO [DONE] Ensure that necessary permissions are granted (look in AndroidManifest.xml to
        // see what permissions are needed for this app)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NOTIFICATION_POLICY
            }, 1);
        }


        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        mLocationManager.registerGnssStatusCallback(mGnssStatusCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("locSet", Context.MODE_PRIVATE);
        double defaultValue = 0;
        latitudeFence = Double.parseDouble(sharedPreferences.getString("latitudeSave", String.valueOf(defaultValue)));
        longitudeFence = Double.parseDouble(sharedPreferences.getString("longitudeSave", String.valueOf(defaultValue)));
        Log.d("geofenceTriggeredrun", "onResume: is not run");
        if (getIntent().getBooleanExtra("GeofenceTriggered", false)) {
            Log.d("geofenceTriggeredrun", "onResume: is run");
            getIntent().removeExtra("GeofenceTriggered");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
            removeGeofence();
        }
    }

    private void removeGeofence() {
        if (latitudeFence != 0 && longitudeFence != 0){
            latitudeFence = 0;
            longitudeFence = 0;
        }
        if (geofenceCircle != null) {
            geofenceCircle.remove(); // Remove the existing circle if any
        }
        // Remove the marker
        if (destination != null) {
            destination.remove(); // Remove the existing marker if any
            destination = null;
        }
        // Remove the geofence
        if (mGeofence != null) {
            mGeofencingClient.removeGeofences(GEOFENCE_REQUEST_LIST)
                    .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Geofence removed successfully
                            // ...
                            mGeofence = null;
                            Log.d("removeGeofence", "geoFence removed successfully");
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("removeGeofence", "onFailure: geofence remove failed");
                            // Failed to remove geofence
                            // ...
                        }
                    });
            mGeofence = null; // Set mGeofence to null to indicate that there's no active geofence
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = getSharedPreferences("locSet", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("latitudeSave", String.valueOf(latitudeFence));
        editor.putString("longitudeSave", String.valueOf(longitudeFence));
        editor.apply();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStop() {
        super.onStop();

        mLocationManager.removeUpdates(this);
        mLocationManager.unregisterGnssStatusCallback(mGnssStatusCallback);
    }
    @Override
    public void onBackPressed() {
        if (viewFlipper.getDisplayedChild() == 1) {
            viewFlipper.setDisplayedChild(0);
        }
    }
}
