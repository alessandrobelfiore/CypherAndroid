package com.example.cypher00;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class TestPrefs extends PreferenceActivity {

//    public static final String SYNCHRONY_KEY = "SYNCHRONY_KEY";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);

        Preference preference = findPreference("set_sensor");
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(TestPrefs.this, SetSensor.class);
                startActivity(intent);
                return true;
            }
        });
    }

}
