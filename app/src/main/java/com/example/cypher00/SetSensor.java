package com.example.cypher00;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

import static com.example.cypher00.GameFragment.PITCH_KEY;
import static com.example.cypher00.GameFragment.ROLL_KEY;
import static com.example.cypher00.MainActivity.SENSOR_SET;

public class SetSensor extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

    private Button startButton;
    private Button doneButton;
    private SensorManager sensorManager;
    private Sensor rvSensor;
    private ArrayList<Float> measuresRoll;
    private ArrayList<Float> measuresPitch;
    private long duration = 1500;
    private long startTime;
    private float meanRoll;
    private float meanPitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_sensor);
        startButton = findViewById(R.id.button_start);
        doneButton = findViewById(R.id.button_done);
        startButton.setOnClickListener(this);
        doneButton.setOnClickListener(this);

        measuresRoll = new ArrayList();
        measuresPitch = new ArrayList();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        rvSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

    }
    public void onClick(View v) {
        if (v == startButton) {
            startMeasure();
            doneButton.setText("WAIT..");
        }
        if (v == doneButton) {
        }
    }
    @Override
    public final void onSensorChanged(SensorEvent sensorEvent) {
        float[] rotationMatrix = new float[16];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, sensorEvent.values);

        float[] orientations = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientations);

        for (int i = 0; i < 3; i++) {
            orientations[i] = (float) (Math.toDegrees(orientations[i]));
        }
        //Log.d("HALP!", String.valueOf(orientations[1]));
        measuresRoll.add(orientations[2]);
        measuresPitch.add(orientations[1]);
        if (System.currentTimeMillis() - startTime >= duration) {
            sensorManager.unregisterListener(this);
            meanPitch = calculateAverage(measuresPitch);
            //Log.d("HALP!", String.valueOf(meanPitch));
            meanRoll = calculateAverage(measuresRoll);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = pref.edit();
            editor.putFloat(ROLL_KEY, meanRoll);
            //Log.d("HALP!", String.valueOf(meanRoll));
            editor.putFloat(PITCH_KEY, meanPitch);
            editor.putBoolean(SENSOR_SET, true); // TODO ?
            doneButton.setText("DONE");
            editor.apply();
            Toast.makeText(this, "CALIBRATION FINISHED", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        //sensorManager.registerListener(this, rvSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private void startMeasure(){
        if (measuresPitch != null)
            measuresPitch.clear();
        if (measuresRoll != null)
            measuresRoll.clear();
        sensorManager.registerListener(this, rvSensor, SensorManager.SENSOR_DELAY_NORMAL);
        startTime = System.currentTimeMillis();
    }

    private float calculateAverage(ArrayList<Float> elem) {
        float sum = 0;
        if(!elem.isEmpty()) {
            for (int i=0; i < elem.size(); i++) {
                sum += elem.get(i);
            }
            return sum / elem.size();
        }
        return sum;
    }
}
