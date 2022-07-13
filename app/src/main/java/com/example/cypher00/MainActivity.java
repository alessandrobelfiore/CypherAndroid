package com.example.cypher00;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import static com.example.cypher00.KeysUtils.MODE_KEY;
import static com.example.cypher00.KeysUtils.MULTI_PLAYER_HOST;
import static com.example.cypher00.KeysUtils.SINGLE_PLAYER_TRAINING;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

   /* private Button optionsBtn;
    private Button newGameSingle;
    private Button newGameMulti;
    private Button matchHistoryBtn;*/
    private ImageView optionsBtn;
    private ImageView newGameSingle;
    private ImageView newGameMulti;
    private boolean sensorSetting;
    private TableLayout table;
    private PieceGrid grid;
    private SharedPreferences pref;
    private ConstraintLayout layout;
    private int[][] titleBitmask;

    private GameFragment gf;

    public static final String SENSOR_SET = "SENSOR_SET";

    static final int SELECT_MODE_TAB = 0;
    static final int SET_SENSOR_TAB = 1;
    static final int OPTION_TAB = 2;
    static final int MATCH_HISTORY_TAB = 3;
    static final int SINGLE_PLAYER_MODES_TAB = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gf = new GameFragment();

        /*optionsBtn = findViewById(R.id.prefs);
        newGameSingle = findViewById(R.id.new_game_single);
        newGameMulti = findViewById(R.id.new_game_multi);
        matchHistoryBtn = findViewById(R.id.history);*/
        optionsBtn = findViewById(R.id.options_image);
        newGameSingle = findViewById(R.id.singleplayer_image);
        newGameMulti = findViewById(R.id.multiplayer_image);

        table = findViewById(R.id.tableLayout);
        layout = findViewById(R.id.main_menu_layout);

        optionsBtn.setOnClickListener(this);
        newGameSingle.setOnClickListener(this);
        newGameMulti.setOnClickListener(this);
        /*matchHistoryBtn.setOnClickListener(this);*/

        titleBitmask = Constants.titleBitmask;
        createTitle();
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        MessengerService.setOnSocketClosedListener(new MessengerService.onSocketClosedListener() {
            @Override
            public void onSocketClosed() {
                Toast.makeText(getApplicationContext(), "Connection lost!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onClick(View v) {
        if (v.getId() == R.id.singleplayer_image) {
            sensorSetting = pref.getBoolean(SENSOR_SET, false);
            if (sensorSetting)
                switchActivity(SINGLE_PLAYER_MODES_TAB, SINGLE_PLAYER_TRAINING);
            else switchActivity(SET_SENSOR_TAB, SINGLE_PLAYER_TRAINING);
        }
        else if (v.getId() == R.id.multiplayer_image) {
            sensorSetting = pref.getBoolean(SENSOR_SET, false);
            if (sensorSetting)
                switchActivity(SELECT_MODE_TAB, MULTI_PLAYER_HOST);
            else switchActivity(SET_SENSOR_TAB, MULTI_PLAYER_HOST);
        }
        else if (v.getId() == R.id.options_image) {
            switchActivity(OPTION_TAB, 0);
        }
        // database currently hidden TODO
        /*else if (v.getId() == R.id.history) {
            switchActivity(MATCH_HISTORY_TAB, 0);
        }*/
        else if (v instanceof PuzzlePiece) {
            grid.rotate(((PuzzlePiece) v).getI(), ((PuzzlePiece) v).getJ());
            gf.rotatePiece((PuzzlePiece) v);
        }
    }

    /**
     * Switch to the given activity in the chosen mode
     * @param tab the code that identifies the activity to switch to
     * @param mode the code that identifies the chosen mode
     */
    public void switchActivity(int tab, int mode) {
        switch (tab) {
            case SINGLE_PLAYER_MODES_TAB: {
                Intent intent = new Intent(this, SelectSinglePlayerModeTab.class);
                startActivity(intent);
                break;
            }
            case SELECT_MODE_TAB: {
                Intent intent = new Intent(this, SelectDifficultyTab.class);
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

    /**
     *  Creates the interactive title with the correct pieces
     */
    private void createTitle()
    {
        int rows = 3;
        int columns = 11;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int coverSize = Math.min(displayMetrics.widthPixels - 64, displayMetrics.heightPixels - 64) / columns;

        grid = new PieceGrid(rows, columns);
        grid.generateGridFromBitmask(titleBitmask);
        // TODO encapsulate in a method
        for (int i = 0; i < rows; i++) {
            TableRow row = new TableRow(this);
            table.addView(row);
            Resources res = getResources();
            for (int j = 0; j < columns; j++) {
                PuzzlePiece piece = new PuzzlePiece(this);
                piece.setCoordinates(i, j);
                gf.setImageFromBitmask(grid.getB(i, j), res, piece);
                piece.setLayoutParams(new TableRow.LayoutParams(coverSize, coverSize));
                piece.setOnClickListener(this);
                row.addView(piece);
                piece.requestLayout();
            }
        }
    }
}
