package com.example.orientationassgn1;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //System sensor manager instance
    private SensorManager sensorManager;

    //Accelerometer & Magnetometer sensors
    private Sensor sensorAccelerometer;
    private Sensor sensorMagnetometer;

    //Store data from Accelerometer & Magnetometer
    private float[] accelerometerData = new float[3];
    private float[] magnetometerData = new float[3];

    private float xAccelerometer;
    private float yAccelerometer;
    private float zAccelerometer;

    private float xMagnetometer;
    private float yMagnetometer;
    private float zMagnetometer;


    //TextViews to display current sensor values
    private TextView textAzimuth;
    private TextView textPitch;
    private TextView textRoll;

    //ImageView drawables to display spots
    private ImageView spotTop;
    private ImageView spotBottom;
    private ImageView spotLeft;
    private ImageView spotRight;

    //System display, need this for determining rotation
    private Display display;

    private static final float ALPHA_VALUE = 0.25f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textAzimuth = (TextView) findViewById(R.id.value_azimuth);
        textPitch = (TextView) findViewById(R.id.value_pitch);
        textRoll = (TextView) findViewById(R.id.value_roll);
        spotTop = (ImageView) findViewById(R.id.spot_top);
        spotBottom = (ImageView) findViewById(R.id.spot_bottom);
        spotRight = (ImageView) findViewById(R.id.spot_right);
        spotLeft = (ImageView) findViewById(R.id.spot_left);

        //Get accelerometer and magnetometer sensors from the sensor manager
        //getDefaultSensor() method returns null if the sensor is not available on the device
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        //Get the display from the window manager (for rotation)
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
    }

    @Override
    protected void onStart(){

        super.onStart();

        if(sensorAccelerometer != null){
            sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if(sensorMagnetometer != null){
            sensorManager.registerListener(this, sensorMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    @Override
    protected void onStop(){

        super.onStop();

        sensorManager.unregisterListener(this);

    }

    protected float[] lowPass(float[] input, float[] output){

        if(output == null){
            return input;
        }

        for(int i = 0; i < input.length; i++){
            output[i] = output[i] + ALPHA_VALUE * (input[i] - output[i]);
        }

        return output;

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        int sensorType = event.sensor.getType();

        switch(sensorType){

            case Sensor.TYPE_ACCELEROMETER:
                accelerometerData = lowPass(event.values.clone(), accelerometerData);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magnetometerData = lowPass(event.values.clone(), magnetometerData);
                break;
            default:
                return;

        }

        //Compute Azimuth, pitch, roll values using formula
        xAccelerometer = accelerometerData[0];
        yAccelerometer = accelerometerData[1];
        zAccelerometer = accelerometerData[2];

        xMagnetometer = magnetometerData[0];
        yMagnetometer = magnetometerData[1];
        zMagnetometer = magnetometerData[2];



        float pitch = ((180 * (float)(Math.atan2(xAccelerometer, (float)(Math.sqrt(xAccelerometer*xAccelerometer + zAccelerometer*zAccelerometer)))))/(float)Math.PI) + 90;
        float roll = (180 * (float)(Math.atan2(yAccelerometer, (float)(Math.sqrt(yAccelerometer*yAccelerometer + zAccelerometer*zAccelerometer)))))/(float)Math.PI;

        float xAzimuth = (xMagnetometer * (float) Math.cos(pitch)) + (yMagnetometer * (float)Math.sin(pitch) * (float)Math.sin(roll)) + (zMagnetometer * (float)Math.cos(roll) * (float)Math.sin(pitch));
        float yAzimuth = (yMagnetometer * (float) Math.cos(roll)) - (zMagnetometer * (float)Math.sin(roll));

        float azimuth = ((180 * (float)Math.atan2(-yAzimuth, xAzimuth))/(float)Math.PI) + 180;


        textAzimuth.setText(getResources().getString(R.string.value_format, azimuth));
        textPitch.setText(getResources().getString(R.string.value_format, pitch));
        textRoll.setText(getResources().getString(R.string.value_format, roll));


        spotTop.setAlpha(0f);
        spotBottom.setAlpha(0f);
        spotLeft.setAlpha(0f);
        spotRight.setAlpha(0f);

        if(pitch > 0){
            spotBottom.setAlpha(pitch);
        }else{
            spotTop.setAlpha(Math.abs(pitch));
        }

        if(roll > 0){
            spotLeft.setAlpha(roll);
        }else{
            spotRight.setAlpha(Math.abs(roll));
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
