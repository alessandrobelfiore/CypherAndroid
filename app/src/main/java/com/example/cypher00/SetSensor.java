package com.example.cypher00;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import static com.example.cypher00.KeysUtils.PITCH_KEY;
import static com.example.cypher00.KeysUtils.ROLL_KEY;
import static com.example.cypher00.MainActivity.SENSOR_SET;

public class SetSensor extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

    private Button startButton;
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
        startButton.setOnClickListener(this);

        measuresRoll = new ArrayList<>();
        measuresPitch = new ArrayList<>();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        rvSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }
    public void onClick(View v) {
        if (v == startButton) {
            startMeasure();
            startButton.setText(this.getString(R.string.wait));
        }
    }

    /**
     * Listens for sensor values for a determined timespan, then updates the preferences
     * @param sensorEvent the event fired by the sensor
     */
    @Override
    public final void onSensorChanged(SensorEvent sensorEvent) {
        float[] rotationMatrix = new float[16];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, sensorEvent.values);

        float[] orientations = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientations);

        for (int i = 0; i < 3; i++) {
            orientations[i] = (float) Math.toDegrees(orientations[i]);
        }
        //Log.d("HALP!", String.valueOf(orientations[1]));
        measuresRoll.add(orientations[2]);
        measuresPitch.add(orientations[1]);
        if (System.currentTimeMillis() - startTime >= duration) {
            sensorManager.unregisterListener(this);
            meanPitch = calculateAverage(measuresPitch);
            meanRoll = calculateAverage(measuresRoll);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = pref.edit();
            editor.putFloat(ROLL_KEY, meanRoll);
            editor.putFloat(PITCH_KEY, meanPitch);
            editor.putBoolean(SENSOR_SET, true);
            editor.apply();
            startButton.setText(this.getString(R.string.start));
            Toast.makeText(this, getApplicationContext().getString(R.string.calibration_end), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    /**
     * Clears past values and starts listening for new ones
     */
    private void startMeasure(){
        if (measuresPitch != null) measuresPitch.clear();
        if (measuresRoll != null) measuresRoll.clear();
        sensorManager.registerListener(this, rvSensor, SensorManager.SENSOR_DELAY_NORMAL);
        startTime = System.currentTimeMillis();
    }

    /**
     * Returns the average of a float array
     * @param elem array of float
     * @return the average
     */
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
