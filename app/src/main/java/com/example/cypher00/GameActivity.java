package com.example.cypher00;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import static com.example.cypher00.KeysUtils.MODE_KEY;
import static com.example.cypher00.KeysUtils.SINGLE_PLAYER;

public class GameActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Intent intent = getIntent();
        int mode = intent.getIntExtra(MODE_KEY, SINGLE_PLAYER);
//        Log.d("TEST", "ENTRO IN GAMEACT " + mode);
        if (mode == SINGLE_PLAYER) {
//            Log.d("TEST", "ENTRO IN SINGLE, GAMEACT");
            GameFragment gf = new GameFragment();
            gf.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, gf).commit();
        }
        else {
            ConnectionFragment cf = new ConnectionFragment();
            cf.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, cf).commit();

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's instance
//        getSupportFragmentManager().putFragment(outState, "myFragmentName", gf);
    }
}
