package com.example.cypher00;

import static com.example.cypher00.KeysUtils.MODE_KEY;
import static com.example.cypher00.KeysUtils.SINGLE_PLAYER_TRAINING;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SelectSinglePlayerModeTab extends AppCompatActivity implements View.OnClickListener {

    private ImageView campaign;
    private ImageView training;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_mode_singleplayer);

        campaign = findViewById(R.id.campaign_button);
        training = findViewById(R.id.training_button);

        campaign.setOnClickListener(this);
        training.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == campaign.getId()) {
            Intent intent = new Intent(this, CampaignSelectLevel.class);
            startActivity(intent);
        } else if (v.getId() == training.getId()) {
            Intent intent = new Intent(this, SelectDifficultyTab.class);
            intent.putExtra(MODE_KEY, SINGLE_PLAYER_TRAINING);
            startActivity(intent);
        }
    }
}
