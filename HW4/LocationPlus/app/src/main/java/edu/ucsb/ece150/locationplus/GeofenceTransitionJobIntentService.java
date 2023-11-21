package edu.ucsb.ece150.locationplus;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class GeofenceTransitionJobIntentService extends JobIntentService {
    private NotificationChannel mNotificationChannel;
    private NotificationManager mNotificationManager;
    private NotificationManagerCompat mNotificationManagerCompat;
    private final String CHANNEL_ID = "LocationPlus";
    private final String CHANNEL_NAME = "Geofence Notification Channel";
    private final int NOTIFICATION_ID = 1;
    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, GeofenceTransitionJobIntentService.class, 0, intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onHandleWork(Intent intent) {
//        double latitude = intent.getDoubleExtra("latitude", 0.0);
//        double longitude = intent.getDoubleExtra("longitude", 0.0);
//
//        Log.d("lat", "latitude: " + latitude);
//        Log.d("long", "longitude: " + longitude);
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent == null) {
            Log.d("geofencing", "geofencingEvent is null");
            return;
        }
        if (geofencingEvent.hasError()) {
            Log.e("Geofence", GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode()));
            return;
        }

        // [TODO] This is where you will handle detected Geofence transitions. If the user has
        // arrived at their destination (is within the Geofence), then
        // 1. Create a notification and display it
        // 2. Go back to the main activity (via Intent) to handle cleanup (Geofence removal, etc.)

        int transitionType = geofencingEvent.getGeofenceTransition();
        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER | transitionType == Geofence.GEOFENCE_TRANSITION_DWELL) {

            // The user has entered the Geofence (arrived at the destination)
            //sendNotification("You have arrived at your destination");
            mNotificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(mNotificationChannel);
            Log.d("TRANSITION TRIGGERED", "DWELL or ENTER triggered");
            Intent goBackToMapActivity = new Intent(getApplicationContext(), MapsActivity.class);
            goBackToMapActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            stackBuilder.addParentStack(MapsActivity.class);
            stackBuilder.addNextIntent(goBackToMapActivity);

            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, goBackToMapActivity, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Arrived at Destination")
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(false)
                    .setContentIntent(pendingIntent);
            mNotificationManagerCompat=NotificationManagerCompat.from(getApplicationContext());
            mNotificationManagerCompat.notify(NOTIFICATION_ID, notification.build());
            Log.d("notification", "notification should of sent ");

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent removeGeofenceIntent = new Intent(getApplicationContext(), MapsActivity.class);
                    removeGeofenceIntent.putExtra("GeofenceTriggered", true);
                    removeGeofenceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(removeGeofenceIntent);
                }
            }, 2000);

            //Intent mainActivityIntent = new Intent(this, MapsActivity.class);
            //mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //startActivity(mainActivityIntent);
        }
    }

    private void removeGeofence(String geofenceRequestID) {
        List<String> geofenceRequestIDs = new ArrayList<>();
        geofenceRequestIDs.add(geofenceRequestID);
        LocationServices.getGeofencingClient(this).removeGeofences(geofenceRequestIDs)
                .addOnSuccessListener(aVoid -> Log.d("Geofence", "Successfully removed Geofence"))
                .addOnFailureListener(e -> Log.d("Geofence", "Failed to remove Geofence"));
    }
}
