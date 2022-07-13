package com.example.cypher00;

import static com.example.cypher00.KeysUtils.LEVEL_ID_KEY;
import static com.example.cypher00.KeysUtils.MODE_KEY;
import static com.example.cypher00.KeysUtils.SINGLE_PLAYER_CAMPAIGN;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.appcompat.app.AppCompatActivity;

public class CampaignSelectLevel extends AppCompatActivity implements View.OnClickListener {

    private TableLayout table;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_level);

        table = findViewById(R.id.levels_table);
        createLevelsTable();
    }

    private void createLevelsTable() {
        int nLevels = Constants.levels.length;
        int columns = 4; // TODO dependant on size?
        int rows = (int) Math.ceil(nLevels / 4f);

        Log.d("LEVELS", "nLevels " +  nLevels);
        Log.d("LEVELS", "rows " +  rows);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int size = Math.min(displayMetrics.widthPixels - 64, displayMetrics.heightPixels - 64) / columns;
        Log.d("LEVELS", "size " +  size);

        for (int i = 0; i < rows; i++) {
            TableRow row = new TableRow(this);
            table.addView(row);
            Resources res = getResources();
            for (int j = 0; j < columns; j++) {
                if (((4 * i) + j) < nLevels)
                {
                    Log.d("LEVELS", "creating level " +  ((4 * i) + j));
                    LevelIcon level = new LevelIcon(this, ((4 * i) + j));
                    level.setImageDrawable(getDrawable(R.drawable.coin_1));
                    level.setLayoutParams(new TableRow.LayoutParams(size, size));
                    level.setOnClickListener(this);
                    row.addView(level);
                } else return;
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view instanceof LevelIcon) {
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra(MODE_KEY, SINGLE_PLAYER_CAMPAIGN);
            intent.putExtra(LEVEL_ID_KEY, ((LevelIcon) view).getLevelId());
            startActivity(intent);
        }
    }
}
