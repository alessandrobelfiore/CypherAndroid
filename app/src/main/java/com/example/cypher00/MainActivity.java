package com.example.cypher00;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import static com.example.cypher00.SelectModeTab.MODE_KEY;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button optionsBtn;
    private Button newGameSingle;
    private Button newGameMulti;
    private Button matchHistoryBtn;
    private boolean sensorSetting;

    private Button test;

    public static final String SENSOR_SET = "SENSOR_SET";

    static final int SELECT_MODE_TAB = 0;
    static final int SET_SENSOR_TAB = 1;
    static final int OPTION_TAB = 2;
    static final int MATCH_HISTORY_TAB = 3;

    static final int SINGLE_PLAYER = 0;
    static final int MULTI_PLAYER_CLIENT = 1;
    static final int MULTI_PLAYER_HOST = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        optionsBtn = findViewById(R.id.prefs);
        newGameSingle = findViewById(R.id.new_game_single);
        newGameMulti = findViewById(R.id.new_game_multi);
        matchHistoryBtn = findViewById(R.id.history);
        test = findViewById(R.id.test);
        optionsBtn.setOnClickListener(this);
        newGameSingle.setOnClickListener(this);
        newGameMulti.setOnClickListener(this);
        matchHistoryBtn.setOnClickListener(this);
        test.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.new_game_single) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            sensorSetting = pref.getBoolean(SENSOR_SET, false);
            if (sensorSetting)
                switchActivity(SELECT_MODE_TAB, SINGLE_PLAYER);
            else switchActivity(SET_SENSOR_TAB, SINGLE_PLAYER);
        }
        else if (v.getId() == R.id.new_game_multi) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            sensorSetting = pref.getBoolean(SENSOR_SET, false);
            if (sensorSetting)
                switchActivity(SELECT_MODE_TAB, MULTI_PLAYER_HOST);
            else switchActivity(SET_SENSOR_TAB, MULTI_PLAYER_HOST);
        }
        else if (v.getId() == R.id.prefs) {
            switchActivity(OPTION_TAB, 0);
        }
        else if (v.getId() == R.id.history) {
            switchActivity(MATCH_HISTORY_TAB, 0);
        }
//        else if (v.getId() == R.id.test) {
//            Intent intent = new Intent(this, TestSocketActivity.class);
//            startActivity(intent);
//        }
    }

    public void switchActivity(int tab, int mode) {
        switch (tab) {
            case SELECT_MODE_TAB: {
                Intent intent = new Intent(this, SelectModeTab.class);
                intent.putExtra(MODE_KEY, mode);
                startActivity(intent);
                break;
            }
            case SET_SENSOR_TAB: {
                Intent intent = new Intent(this, SetSensor.class);
                intent.putExtra(MODE_KEY, mode);
                startActivity(intent);
                break;
            }
            case OPTION_TAB: {
                Intent intent = new Intent(this, TestPrefs.class);
                startActivity(intent);
                break;
            }
            case MATCH_HISTORY_TAB: {
                Intent intent = new Intent(this, MatchHistory.class);
                startActivity(intent);
                break;
            }

        }
    }



}
