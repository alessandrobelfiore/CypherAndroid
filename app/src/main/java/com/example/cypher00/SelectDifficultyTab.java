package com.example.cypher00;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.ChipGroup;

import static com.example.cypher00.KeysUtils.COVERS_KEY;
import static com.example.cypher00.KeysUtils.COVERS_NO;
import static com.example.cypher00.KeysUtils.COVERS_SOME;
import static com.example.cypher00.KeysUtils.COVERS_YES;
import static com.example.cypher00.KeysUtils.DIFFICULTY_EASY;
import static com.example.cypher00.KeysUtils.DIFFICULTY_HARD;
import static com.example.cypher00.KeysUtils.DIFFICULTY_KEY;
import static com.example.cypher00.KeysUtils.DIFFICULTY_MEDIUM;
import static com.example.cypher00.KeysUtils.MODE_KEY;
import static com.example.cypher00.KeysUtils.MULTI_PLAYER_HOST;
import static com.example.cypher00.KeysUtils.SINGLE_PLAYER;

public class SelectDifficultyTab extends AppCompatActivity implements View.OnClickListener {

    private int currentMode;
    private ChipGroup difficultyGroup;
    private ChipGroup coversGroup;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_mode);

        Intent intent = getIntent();
        currentMode = intent.getIntExtra(MODE_KEY, SINGLE_PLAYER);

        Button startBtn = findViewById(R.id.start_button);
        difficultyGroup = findViewById(R.id.difficultyGroup);
        coversGroup = findViewById(R.id.coversGroup);

        startBtn.setOnClickListener(this);
    }

    /**
     * Sets difficulty and cover number and switches to GameFragment if singlePlayer
     *      to ConnectionFragment otherwise
     */
    @Override
    public void onClick(View v) {
        int difficulty;
        int covers;
        int difficultyId = difficultyGroup.getCheckedChipId();
        int coversId = coversGroup.getCheckedChipId();
        switch(difficultyId) {
            case R.id.difficultyEasy : difficulty = DIFFICULTY_EASY; break;
            case R.id.difficultyMedium : difficulty = DIFFICULTY_MEDIUM; break;
            case R.id.difficultyHard : difficulty = DIFFICULTY_HARD; break;
            default : difficulty = DIFFICULTY_EASY;
        }
        switch(coversId) {
            case R.id.coversDeactivated : covers = COVERS_NO; break;
            case R.id.coversSome : covers = COVERS_SOME; break;
            case R.id.coversALot : covers = COVERS_YES; break;
            default : covers = COVERS_NO;
        }

        if (v.getId() == R.id.start_button) {
            if (currentMode == SINGLE_PLAYER) {
//                Log.d("TEST", "ENTRO IN SINGLE");
                Intent intent = new Intent(this, GameActivity.class);
                intent.putExtra(MODE_KEY, SINGLE_PLAYER);
                intent.putExtra(COVERS_KEY, covers);
                intent.putExtra(DIFFICULTY_KEY, difficulty);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, GameActivity.class);
                intent.putExtra(MODE_KEY, MULTI_PLAYER_HOST);
                intent.putExtra(COVERS_KEY, covers);
                intent.putExtra(DIFFICULTY_KEY, difficulty);
                startActivity(intent);
            }
        }
    }
}
