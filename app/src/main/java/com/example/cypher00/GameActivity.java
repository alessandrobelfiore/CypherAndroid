package com.example.cypher00;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import java.net.Socket;

import static com.example.cypher00.GameFragment.DIFFICULTY_KEY;
import static com.example.cypher00.MainActivity.SINGLE_PLAYER;
import static com.example.cypher00.SelectModeTab.MODE_KEY;

public class GameActivity extends AppCompatActivity {

    private Socket socket;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Intent intent = getIntent();
        int mode = intent.getIntExtra(MODE_KEY, SINGLE_PLAYER);
        if (mode == SINGLE_PLAYER) {
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
}
