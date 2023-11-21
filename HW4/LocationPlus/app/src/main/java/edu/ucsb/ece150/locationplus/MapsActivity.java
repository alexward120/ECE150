package edu.ucsb.ece150.locationplus;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback {

    private Geofence mGeofence;
    private GeofencingClient mGeofencingClient;
    private PendingIntent mPendingIntent = null;

    private GnssStatus.Callback mGnssStatusCallback;
    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private Marker currentLocationMarker;
    private boolean autoRecenter = true;
    private boolean startup = false;
    private float desiredZoomLevel = 16.0f;
    private ViewFlipper viewFlipper;
    private Button mapViewButton;
    private Button satelliteViewButton;

    private Toolbar mToolbar;
    private ArrayAdapter adapter;
    private ArrayList<Satellite> satelliteList;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Setup view flipper
        viewFlipper = findViewById(R.id.view_flipper);
        mapViewButton = findViewById(R.id.map_view_button);
        satelliteViewButton = findViewById(R.id.satellite_button);
        mapViewButton.setOnClickListener(v -> viewFlipper.setDisplayedChild(0));
        satelliteViewButton.setOnClickListener(v -> viewFlipper.setDisplayedChild(1));

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
                // [TODO] Implement behavior when the satellite status is updated
                super.onSatelliteStatusChanged(status);
                satelliteList.clear();
                int satelliteCount = status.getSatelliteCount();

                for (int i = 0; i < satelliteCount; i++) {
                    satelliteList.add(new Satellite(status, i));
                }
            }
        };

        // [TODO] Additional setup for viewing satellite information (lists, adapters, etc.)
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
        mToolbar = (Toolbar) findViewById(R.id.appToolbar);
        setSupportActionBar(mToolbar);
        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.action_toggle_auto_center);
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
        // [TODO] Implement behavior when Google Maps is ready

        // [TODO] In addition, add a listener for long clicks (which is the starting point for
        // creating a Geofence for the destination and listening for transitions that indicate
        // arrival)
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO [DONE] Implement behavior when a location update is received
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
            }
            else {
                currentLocationMarker.setPosition(latLng);
                currentLocationMarker.setTitle(result);
            }
            if (!startup) {
                startup = true;
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
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    private GeofencingRequest getGeofenceRequest() {
        // [TODO] Set the initial trigger (i.e. what should be triggered if the user is already
        // inside the Geofence when it is created)

        return new GeofencingRequest.Builder()
                //.setInitialTrigger()  <--  Add triggers here
                .addGeofence(mGeofence)
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
                    Manifest.permission.INTERNET
            }, 1);
        }


        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        mLocationManager.registerGnssStatusCallback(mGnssStatusCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // [TODO] Data recovery
    }

    @Override
    protected void onPause() {
        super.onPause();

        // [TODO] Data saving
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStop() {
        super.onStop();

        mLocationManager.removeUpdates(this);
        mLocationManager.unregisterGnssStatusCallback(mGnssStatusCallback);
    }
}
