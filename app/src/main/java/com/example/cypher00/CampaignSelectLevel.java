package com.example.cypher00;

import static com.example.cypher00.KeysUtils.LEVEL_ID_KEY;
import static com.example.cypher00.KeysUtils.MODE_KEY;
import static com.example.cypher00.KeysUtils.SINGLE_PLAYER_CAMPAIGN;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

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
                int levelId = ((4 * i) + j);
                if (levelId < nLevels)
                {
                    Log.d("LEVELS", "creating level " +  levelId);
                    LevelIcon level = new LevelIcon(this, levelId);
                    Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.level_open);
                    Bitmap bMapOpen = Bitmap.createScaledBitmap(bMap, size - 30, size - 30, true);
                    bMap = BitmapFactory.decodeResource(getResources(), R.drawable.level_closed);
                    Bitmap bMapClosed = Bitmap.createScaledBitmap(bMap, size - 30, size - 30, true);

                    level.setImageBitmap(checkAccessLevel(levelId) ? bMapOpen : bMapClosed);
                    level.setLayoutParams(new TableRow.LayoutParams(size, size));
                    //level.setBackgroundColor(Color.TRANSPARENT);
                    level.setOnClickListener(this);
                    row.addView(level);
                } else return;
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view instanceof LevelIcon) {
            int levelId = ((LevelIcon) view).getLevelId();
            if (checkAccessLevel(levelId))
            {
                Intent intent = new Intent(this, GameActivity.class);
                intent.putExtra(MODE_KEY, SINGLE_PLAYER_CAMPAIGN);
                intent.putExtra(LEVEL_ID_KEY, levelId);
                startActivity(intent);
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Obtain a Silver Medal in the preceding level to access it!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean checkAccessLevel(int levelId) {
        if (levelId == 0) return true;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean("isAccessible" + levelId, false);
    }
}
