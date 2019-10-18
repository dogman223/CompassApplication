package com.example.compassapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // define the display assembly compass picture
    private ImageView image_compass;

    private float[] rMat = new float[9];
    private float[] orientation = new float[9];

    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer= new float[3];

    private boolean haveSensor = false, haveSensor2 = false;

    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    // device sensor manager
    private SensorManager mSensorManager;

    private Sensor mRotationV, mAccelerometer, mMagnetometer;

    private int mAzimuth;

    private TextView txt_azimuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // compass image
        image_compass = findViewById(R.id.imageViewCompass);

        // TextView that will tell the user what degree is he heading
        txt_azimuth = findViewById(R.id.tvHeading);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            mAzimuth = (int) ((Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360);
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        }
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastMagnetometerSet && mLastAccelerometerSet) {
            mSensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(rMat, orientation);
            mAzimuth = (int) ((Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360);
        }

        mAzimuth = Math.round(mAzimuth);
        image_compass.setRotation(-mAzimuth);

        String where = "NO";

        if (mAzimuth >= 350 || mAzimuth <= 10)
            where = "N";
        if (mAzimuth < 350 && mAzimuth > 280)
            where = "NW";
        if (mAzimuth <= 280 && mAzimuth > 260)
            where = "W";
        if (mAzimuth <= 260 && mAzimuth > 190)
            where = "SW";
        if (mAzimuth <= 190 && mAzimuth > 170)
            where = "S";
        if (mAzimuth <= 170 && mAzimuth > 100)
            where = "SE";
        if (mAzimuth <= 100 && mAzimuth > 80)
            where = "E";
        if (mAzimuth <= 80 && mAzimuth > 10)
            where = "NE";

        txt_azimuth.setText(mAzimuth + "° " + where);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

    public void start(){
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)==null){
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)==null || mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)==null){
                noSensorAlert();
            }
            else {
                mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

                haveSensor = mSensorManager.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_UI);
                haveSensor2 = mSensorManager.registerListener(this,mMagnetometer,SensorManager.SENSOR_DELAY_UI);
            }
        }
        else {
            mRotationV = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            haveSensor = mSensorManager.registerListener(this,mRotationV,SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void noSensorAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Your device doesn't support the compass.")
        .setCancelable(false)
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
    }

    public void stop(){
        if (haveSensor && haveSensor2){
            mSensorManager.unregisterListener(this,mAccelerometer);
            mSensorManager.unregisterListener(this,mMagnetometer);
        }else if (haveSensor){
            mSensorManager.unregisterListener(this,mRotationV);
        }
    }
}