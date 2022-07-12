package com.example.cypher00;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static com.example.cypher00.GameFragment.DIFFICULTY_KEY;
import static com.example.cypher00.MainActivity.MULTI_PLAYER_HOST;
import static com.example.cypher00.MainActivity.SINGLE_PLAYER;

public class SelectModeTab extends AppCompatActivity implements View.OnClickListener {
    public static final String MODE_KEY = "MODE_KEY";

    private TextView diff;
    private int currentMode;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_mode);

        Intent intent = getIntent();
        currentMode = intent.getIntExtra(MODE_KEY, SINGLE_PLAYER);

        Button startBtn = findViewById(R.id.start_button);
        startBtn.setOnClickListener(this);
        diff = findViewById(R.id.editText);
        diff.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int difficulty;
        if (diff.getText() != null && diff.getText().length() != 0) {
            difficulty = Integer.parseInt(diff.getText().toString());
        } else difficulty = 3;

        if (v.getId() == R.id.start_button) {
            if (currentMode == SINGLE_PLAYER) {
                // Intent intent = new Intent(this, GameTab.class);
                Intent intent = new Intent(this, GameActivity.class);
                intent.putExtra(MODE_KEY, SINGLE_PLAYER);
                intent.putExtra(DIFFICULTY_KEY, difficulty);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, GameActivity.class);
                intent.putExtra(MODE_KEY, MULTI_PLAYER_HOST);
                intent.putExtra(DIFFICULTY_KEY, difficulty);
                startActivity(intent);
            }
        }
    }
}
