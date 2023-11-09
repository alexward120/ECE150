package edu.ucsb.ece150.stepitup;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Build;
import android.os.Bundle;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements SensorEventListener  {
    private final StepDetector mStepDetector = new StepDetector();
    private static float STEP_GOAL;
    private static float TOTAL_STEPS = 0;
    private static float builtStepCounter = 0;
    private static float goalsCompleted = 0;
    private static float stepGoalsFinalValue;
    private static double startTime = System.currentTimeMillis();
    private float stepsPerHour = 0;
    private long lastStepTime = 0;
    private static final int MIN_STEP_INTERVAL = 500;

    Handler handler = new Handler();
    Runnable thread = new Runnable() {
        @Override
        public void run() {
            long elaspedMillis = System.currentTimeMillis() - (long) startTime;
            stepsPerHour = 1000 * 60 * 60 * TOTAL_STEPS / (float) elaspedMillis;
            TextView stepsPHour = findViewById(R.id.stepshrval);
            stepsPHour.setText(String.valueOf(stepsPerHour));
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler.postDelayed(thread, 1000);
        // Setup button behavior
        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> {
            // Save step goal
            stepGoalsFinalValue = Float.parseFloat(((TextView) findViewById(R.id.StepGoal)).getText().toString());
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("Step_Goal_Final_Val", String.valueOf(stepGoalsFinalValue));
            editor.apply();
        });

        Button restartButton = findViewById(R.id.restartButton);
        restartButton.setOnClickListener(v -> {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            stepGoalsFinalValue = Float.parseFloat(sharedPref.getString("Step_Goal_Final_Val", "0")); // Default value
            ((TextView) findViewById(R.id.StepGoal)).setText(String.format("%.0f", stepGoalsFinalValue)); // Update UI
            STEP_GOAL = stepGoalsFinalValue;
        });

        // Initialize UI elements
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        TextView builtInStepCount = findViewById(R.id.BuiltInVal);
        String builtInStepCountString = sharedPref.getString("BuiltInStepCount", "0");
        builtStepCounter = Float.parseFloat(builtInStepCountString);
        builtInStepCount.setText(String.format("%.0f", builtStepCounter));

        TextView totalSteps = findViewById(R.id.totalStepVal);
        String totalStepsString = sharedPref.getString("TotalSteps", "0");
        TOTAL_STEPS = Float.parseFloat(totalStepsString);
        totalSteps.setText(String.format("%.0f", TOTAL_STEPS));

        TextView stepGoal = findViewById(R.id.StepGoal);
        String stepGoalString = sharedPref.getString("Step_Goal", "0"); // Default value
        STEP_GOAL = Float.parseFloat(stepGoalString);
        stepGoal.setText(String.format("%.0f", STEP_GOAL));

        TextView stepsPHour = findViewById(R.id.stepshrval);
        String stepsPerHourString = sharedPref.getString("StepsPerHour", "0");
        stepsPerHour = Float.parseFloat(stepsPerHourString);
        stepsPHour.setText(stepsPerHourString);

        TextView goalsComplete = findViewById(R.id.goalscompletedval);
        String goalsCompletedString = sharedPref.getString("GoalsCompleted", "0");
        goalsCompleted = Float.parseFloat(goalsCompletedString);
        goalsComplete.setText(String.format("%.0f", goalsCompleted));

        String startTimeString = sharedPref.getString("StartTime", "0");
        if (!startTimeString.equals("0")) {
            startTime = Double.parseDouble(startTimeString);
        }



        // Request ACTIVITY_RECOGNITION permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1);
            }
        }
        // Initialize accelerometer and step counter sensors
        SensorManager mSensorManager;
        Sensor mAccelerometer;
        Sensor mStepCounter;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (mAccelerometer != null) {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
        if (mStepCounter != null) {
            mSensorManager.registerListener(this, mStepCounter, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // Handle the raw data. Hint: Provide data to the step detector, call `handleStep` if step detected
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            long currentTime = System.currentTimeMillis();
            if (mStepDetector.detectStep(x, y, z) && currentTime - lastStepTime > MIN_STEP_INTERVAL) {
                handleStep();
                lastStepTime = currentTime;
            }
        }
        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            float steps = event.values[0];
            TextView builtInStepCount = findViewById(R.id.BuiltInVal);
            builtInStepCount.setText(String.format("%.0f", steps));
        }
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Unused
    }

    private void sendNotification(String text) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "my_channel";
            String description = "ECE150 - UCSB StepItUp";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("StepItUp")
                .setContentText(text)
                .setContentIntent(pendingIntent);

        notificationManager.notify(1, notificationBuilder.build());
    }

    private void handleStep() {
        TOTAL_STEPS++;

        TextView stepCount = findViewById(R.id.totalStepVal);
        stepCount.setText(String.format("%.0f", TOTAL_STEPS));
        STEP_GOAL--;
        if (STEP_GOAL > 0) {
            TextView totalSteps = findViewById(R.id.StepGoal);
            totalSteps.setText(String.format("%.0f", STEP_GOAL));
        } else {
            sendNotification("You have reached your step goal!");
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            STEP_GOAL = Float.parseFloat(sharedPref.getString("Step_Goal_Final_Val", "0")); // Default value
            ((TextView) findViewById(R.id.StepGoal)).setText(String.format("%.0f", STEP_GOAL)); // Update UI
            goalsCompleted++;
            TextView goalsComplete = findViewById(R.id.goalscompletedval);
            goalsComplete.setText(String.format("%.0f", goalsCompleted));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("BuiltInStepCount", String.valueOf(builtStepCounter));
        editor.putString("TotalSteps", String.valueOf(TOTAL_STEPS));
        editor.putString("Step_Goal", String.valueOf(STEP_GOAL));
        editor.putString("StepsPerHour", String.valueOf(stepsPerHour));
        editor.putString("GoalsCompleted", String.valueOf(goalsCompleted));
        editor.putString("StartTime", String.valueOf(startTime));
        editor.apply();
    }
}

