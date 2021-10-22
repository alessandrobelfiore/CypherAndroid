package com.example.cypher00;

import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

import static android.content.Context.SENSOR_SERVICE;
import static com.example.cypher00.ConnectionFragment.MESSAGE_CODE;
import static com.example.cypher00.ConnectionFragment.MESSAGE_READ;
import static com.example.cypher00.ConnectionFragment.NOTIFY;
import static com.example.cypher00.ConnectionFragment.READY;
import static com.example.cypher00.ConnectionFragment.RESULT;
import static com.example.cypher00.MainActivity.MULTI_PLAYER_CLIENT;
import static com.example.cypher00.MainActivity.MULTI_PLAYER_HOST;
import static com.example.cypher00.MainActivity.SINGLE_PLAYER;
import static com.example.cypher00.SelectModeTab.MODE_KEY;

public class GameFragment extends Fragment implements View.OnClickListener, SensorEventListener {

    private Chronometer timer;
    private Button backToMenu;
    private TextView victoryAlert;
    private TableLayout table;
    private int difficulty;
    private float pitchAngle;
    private float rollAngle;
    private SensorManager sensorManager;
    private Sensor rvSensor;
    private long lastAnim = 0;
    private PieceGrid grid;
    private ConstraintLayout layout;
    private long matchTime;
    private long enemyMatchTime;
    private long winnerMatchTime;
    private int mode;
    private SQLiteDatabase mDatabase;
//    private String DB_PATH =  getContext().getApplicationInfo().dataDir+"/databases/";
    // TODO
    private String opponentName = "PROVA";

    public static final String DIFFICULTY_KEY = "DIFFICULTY_KEY";
    public static final String PITCH_KEY = "PITCH_KEY";
    public static final String ROLL_KEY = "ROLL_KEY";
    public static final String GRID_BITS_KEY = "GRID_BITS_KEY";
    public static final String GRID_BYTES_KEY = "GRID_BYTES_KEY";
    public static final String MY_MATCH_TIME = "MY_MATCH_TIME";
    public static final String WINNER_MATCH_TIME = "WINNER_MATCH_TIME";

    public static final String MATCHES= "MATCHES";
    private SQLiteAdapter mySQLiteAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_game_tab, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle extras = getArguments();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        rollAngle = pref.getFloat(ROLL_KEY, 0f);
        pitchAngle = pref.getFloat(PITCH_KEY, 0f);
        difficulty = extras.getInt(DIFFICULTY_KEY, 3);
        Log.d("TEST", "test 2:" + (difficulty));
        matchTime = enemyMatchTime = winnerMatchTime = -1;
//        difficulty = 3;

        sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        rvSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        table = view.findViewById(R.id.table1);
        timer = view.findViewById(R.id.timer);
        victoryAlert = view.findViewById(R.id.victory_alert);
        layout = view.findViewById(R.id.game_layout);
        backToMenu = view.findViewById(R.id.back_to_menu);

        backToMenu.setOnClickListener(this);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int dim = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) / difficulty;

        if (savedInstanceState == null) {
            // checks if is in MULTI PLAYER MODE
            mode = extras.getInt(MODE_KEY, SINGLE_PLAYER);
            if (mode == SINGLE_PLAYER) {
                grid = new PieceGrid(difficulty, difficulty - 1, layout, dim, this);
                grid.generateGrid();
            } else if (mode == MULTI_PLAYER_CLIENT || mode == MULTI_PLAYER_HOST) {
                byte[] bytes = extras.getByteArray(GRID_BYTES_KEY);
                ArrayList<ArrayList<Piece>> pieces = new ArrayList<>();
                for (int i = 0; i < difficulty; i++) {
                    pieces.add(new ArrayList<Piece>());
                    for (int j = 0; j < difficulty; j++) {
                        assert bytes != null; // TODO necessary for debug?
                        pieces.get(i).add(new Piece(bytes[i * difficulty + j]));
                    }
                }
                this.grid = new PieceGrid(pieces, difficulty - 1, layout, dim, this);
                Log.d("GRIDDIM", String.valueOf(this.grid.getDim()));
            }
            // TODO Set ncovers
            // checkVictory() ? ; TODO Needed?
        } else {
            difficulty = savedInstanceState.getInt("GRID_DIM");
            int[] bits = savedInstanceState.getIntArray("GRID_BITS");
            ArrayList<ArrayList<Piece>> pieces = new ArrayList<>();
            for (int i = 0; i < difficulty; i++) {
                pieces.add(new ArrayList<Piece>());
                for (int j = 0; j < difficulty; j++) {
                    assert bits != null; // TODO necessary for debug?
                    pieces.get(i).add(new Piece(bits[i * difficulty + j]));
                }
            }
            // TODO set ncovers
            this.grid = new PieceGrid(pieces, difficulty - 1, layout, dim, this);
        }
        for (int i = 0; i < difficulty; i++) {
            TableRow row = new TableRow(getContext());
            table.addView(row);
            Resources res = getResources();
            for (int j = 0; j < difficulty; j++) {
                PuzzlePiece piece = new PuzzlePiece(getContext());
                piece.setCoordinates(i, j);
                setImageFromBitmask(grid.getB(i, j), res, piece);
                piece.setLayoutParams(new TableRow.LayoutParams(dim, dim));
                piece.setOnClickListener(this);
                row.addView(piece);
                piece.requestLayout();
            }
        }
        timer.start();
        if (mode != SINGLE_PLAYER) {
            Socket socket = MessengerService.getSocket();
            mReceiver rec = new mReceiver(socket);
        }
//        mDatabase =
//            SQLiteDatabase.openOrCreateDatabase("mDatabase.db",null);
        // TODO PROVA
        mySQLiteAdapter = new SQLiteAdapter(getActivity());


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("GRID_DIM", difficulty);
        int[] bits = PieceGrid.fromGridToBits(this.grid);
//        int[] bits = new int[difficulty * difficulty];
//        for (int i = 0; i < difficulty; i++) {
//            for (int j = 0; j < difficulty; j++) {
//                bits[i * difficulty + j] = grid.getB(i, j);
//            }
//        }
        outState.putIntArray("GRID_BITS", bits);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back_to_menu) {
            Intent intent = new Intent(getContext(), MainActivity.class);
            startActivity(intent);
        } else if (v instanceof PuzzlePiece) {
            grid.rotate(((PuzzlePiece) v).getI(), ((PuzzlePiece) v).getJ());
            rotatePiece((PuzzlePiece) v);
            if (checkVictory()) {
                stopGame();
                // pop escape buttons
            }
        }
        else if (v instanceof Cover) {
            ((Cover) v).block();
        }
    }

    //UTILITY METHODS
    private void setImageFromBitmask(int bitmask, Resources res, PuzzlePiece piece) {
        Drawable jDrawable = null;
        switch (bitmask) {
            case 0b0000:
                jDrawable = res.getDrawable(R.drawable.ic_piece_0000);
                break;
            case 0b0001:
                jDrawable = res.getDrawable(R.drawable.ic_piece_0001);
                break;
            case 0b0010:
                jDrawable = res.getDrawable(R.drawable.ic_piece_0010);
                break;
            case 0b0100:
                jDrawable = res.getDrawable(R.drawable.ic_piece_0100);
                break;
            case 0b1000:
                jDrawable = res.getDrawable(R.drawable.ic_piece_1000);
                break;
            case 0b1010:
                jDrawable = res.getDrawable(R.drawable.ic_piece_1010);
                break;
            case 0b0101:
                jDrawable = res.getDrawable(R.drawable.ic_piece_0101);
                break;
            case 0b0011:
                jDrawable = res.getDrawable(R.drawable.ic_piece_0011);
                break;
            case 0b0110:
                jDrawable = res.getDrawable(R.drawable.ic_piece_0110);
                break;
            case 0b1100:
                jDrawable = res.getDrawable(R.drawable.ic_piece_1100);
                break;
            case 0b1001:
                jDrawable = res.getDrawable(R.drawable.ic_piece_1001);
                break;
            case 0b0111:
                jDrawable = res.getDrawable(R.drawable.ic_piece_0111);
                break;
            case 0b1011:
                jDrawable = res.getDrawable(R.drawable.ic_piece_1011);
                break;
            case 0b1101:
                jDrawable = res.getDrawable(R.drawable.ic_piece_1101);
                break;
            case 0b1110:
                jDrawable = res.getDrawable(R.drawable.ic_piece_1110);
                break;
            case 0b1111:
                jDrawable = res.getDrawable(R.drawable.ic_piece_1111);
                break;
            default:
                break;
        }
        piece.setBackground(jDrawable);
    }

    private void rotatePiece(PuzzlePiece p) {
        ObjectAnimator animator;
        animator = ObjectAnimator.ofFloat(p, "rotation", p.getCurrentRotation(), p.getCurrentRotation() + 90f);
        animator.setDuration(133);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
        p.increaseRotation(90f);
    }

    private void stopGame() {
        this.grid.removeCovers();
        for (int i = 0; i < this.difficulty; i++) {
            TableRow row = (TableRow) table.getChildAt(i);
            for (int j = 0; j < this.difficulty; j++)
                row.getChildAt(j).setOnClickListener(null);
        }
            matchTime = timer.getTime();
            timer.stop();
        if (mode == SINGLE_PLAYER) {
            victoryAlert.setText("VICTORY!");
            winnerMatchTime = matchTime;
            // first notify, enemy hasn't finished
        } else if (enemyMatchTime == -1) {
            sendMessage(NOTIFY);
            // I won, not synchro
        } else if (enemyMatchTime > matchTime) {
            winnerMatchTime = matchTime;
            sendMessage(RESULT);
            // I lost, not synchro
        } else if (matchTime > enemyMatchTime) {
            winnerMatchTime = enemyMatchTime;
            sendMessage(RESULT);
        }
        else {
            Log.d("TEST", "DRAW..for real?");
        }
    }

    private boolean checkVictory() {
        for (int i = 0; i < difficulty; i++)
            for (int j = 0; j < difficulty; j++) {
                if (i == 0 && grid.has(i, j, Piece.Direction.NORTH)) return false;
                if (j == 0 && grid.has(i, j, Piece.Direction.WEST)) return false;
                if (i == (difficulty - 1) && grid.has(i, j, Piece.Direction.SOUTH)) return false;
                if (j == (difficulty - 1) && grid.has(i, j, Piece.Direction.EAST)) return false;
                if (i != 0 && grid.has(i, j, Piece.Direction.NORTH) && !grid.has(i -1, j, Piece.Direction.SOUTH)) return false;
                if (j != 0 && grid.has(i, j, Piece.Direction.WEST) && !grid.has(i, j -1, Piece.Direction.EAST)) return false;
                if (i != (difficulty - 1) && grid.has(i, j, Piece.Direction.SOUTH) && !grid.has(i + 1, j, Piece.Direction.NORTH)) return false;
                if (j != (difficulty - 1) && grid.has(i, j, Piece.Direction.EAST)&& !grid.has(i, j + 1, Piece.Direction.WEST)) return false;
            }
        /*if (i !== 0 && GS.squares[i][j].n && !GS.squares[i - 1][j].s) return;
        if (j !== 0 && GS.squares[i][j].w && !GS.squares[i][j - 1].e) return;
        if (i !== GS.rows - 1 && GS.squares[i][j].s && !GS.squares[i + 1][j].n) return;
        if (j !== GS.columns - 1 && GS.squares[i][j].e && !GS.squares[i][j + 1].w) return; */
        // TODO checkLights
        return true;
    }
    // TODO
    /*private boolean checkVictory(int i, int j) {
        return true;
    }*/

    private void sendMessage(byte code) {
        Intent serviceIntent = new Intent(getContext(), MessengerService.class);
        serviceIntent.putExtra(MESSAGE_CODE, code);
        switch(code) {
            case NOTIFY:
                serviceIntent.putExtra(MY_MATCH_TIME, matchTime);
                break;
            case RESULT:
                serviceIntent.putExtra(WINNER_MATCH_TIME, winnerMatchTime);
                break;
            default:
                Log.d("TEST", "Receiver GameFrag corrupted msg");
                break;
        }
        Objects.requireNonNull(getContext()).startService(serviceIntent);
    }

    // Receiver
    public class mReceiver extends Thread {
        private Socket socket;
        private InputStream inputStream;

        public mReceiver(Socket sock) {
            socket = sock;
            try {
                inputStream = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (socket != null) {
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // Handler receiver
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            // TODO
            switch (msg.what) {
                case MESSAGE_READ:
                    byte[] readBuff = (byte[]) msg.obj;
                    StringBuilder builder = new StringBuilder();
                    for (byte b : readBuff) {
                        builder.append('|');
                        builder.append(b);
                    }
                    builder.append('|');
                    Log.d("TEST", builder.toString());
                    switch (readBuff[0]) {
                        case READY:
                            break;
                        case NOTIFY:
                            byte[] time = new byte[msg.arg1 - 1];
                            System.arraycopy(readBuff, 1, time, 0, msg.arg1 - 1);
                            Log.d("TEST", "msg.arg1 in notify: " + String.valueOf(msg.arg1));
                            enemyMatchTime = ByteUtils.bytesToLong(time);
                            // ASINCRONO, I have already finished
                            if (matchTime != -1 && timer.getTime() == -1) {
                                if (enemyMatchTime <= matchTime) {
                                    defeat();
                                    winnerMatchTime = enemyMatchTime;
                                    sendMessage(RESULT);
                                } else {
                                    winnerMatchTime = matchTime;
                                    sendMessage(RESULT);
                                    victory();
                                }
                            }
                            // SINCRONO, I am still playing
                            if (enemyMatchTime <= timer.getTime() && timer.getTime() != -1) {
                                winnerMatchTime = enemyMatchTime;
                                sendMessage(RESULT);
                                defeat();
                            }
                            break;
                        case RESULT:
                            time = new byte[msg.arg1 - 1];
                            Log.d("TEST", "msg.arg1 in result: " + String.valueOf(msg.arg1));
                            System.arraycopy(readBuff, 1, time, 0, msg.arg1 - 1);
                            // PROVA LEGGERE SOLO I BYTES NECESSARY?
                            winnerMatchTime = ByteUtils.bytesToLong(time);
                            if (winnerMatchTime == matchTime) {
                                victory();
                            } else defeat();
                            break;
                        default:
                            break;
                    }
                    break;
            }
            return false;
        }
    });
    public void victory() {
        victoryAlert.setText("VICTORY!");
        if (mode == SINGLE_PLAYER) {
            //TODO
//            insertInDB(matchTime, difficulty);
//            mySQLiteAdapter.openToWrite();
//            mySQLiteAdapter.insert(matchTime, difficulty);
//            mySQLiteAdapter.close();

        } else {
            //TODO
//            insertInDB(opponentName, matchTime, difficulty, true);
//            mySQLiteAdapter.openToWrite();
//            mySQLiteAdapter.insert(opponentName, matchTime, difficulty, true);
//            mySQLiteAdapter.close();
        }
    }
    public void defeat() {
        victoryAlert.setText("DEFEAT!");
        insertInDB(opponentName, matchTime, difficulty, true);
    }
    // DATABASE
    public long insertInDB(String opponent, long matchTime, int difficulty, boolean amIWinner) {

        ContentValues cv = new ContentValues();
        cv.put("opponent", opponent);
        cv.put("difficulty", difficulty);
        cv.put("time", matchTime);
        if (amIWinner) cv.put("winner", "WIN");
        else cv.put("winner", "LOSE");
        return mDatabase.insert(MATCHES, null, cv);
    }
    public long insertInDB(long matchTime, int difficulty) {

        ContentValues cv = new ContentValues();
        cv.put("opponent", "SINGLEPLAYER");
        cv.put("difficulty", difficulty);
        cv.put("time", matchTime);
        cv.put("winner", "WIN");
        return mDatabase.insert(MATCHES, null, cv);
    }


    // OVERRIDES

    @Override
    public final void onSensorChanged(SensorEvent sensorEvent) {
        float[] rotationMatrix = new float[16];
        SensorManager.getRotationMatrixFromVector(
                rotationMatrix, sensorEvent.values);

        // Convert to orientations
        float[] orientations = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientations);
        for (int i = 0; i < 3; i++) {
            orientations[i] = (float) (Math.toDegrees(orientations[i]));
        }
        // customize angle rotations
        if (orientations[2] - rollAngle < -15) {
            if (lastAnim == 0) {
                grid.slideCovers(Piece.Direction.WEST);
                lastAnim = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - lastAnim >= 500) {
                grid.slideCovers(Piece.Direction.WEST);
                lastAnim = System.currentTimeMillis();
            }
        } else if (orientations[2] - rollAngle > 15) {
            if (lastAnim == 0) {
                grid.slideCovers(Piece.Direction.EAST);
                lastAnim = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - lastAnim >= 500) {
                grid.slideCovers(Piece.Direction.EAST);
                lastAnim = System.currentTimeMillis();
            }
        } else if (orientations[1] - pitchAngle < -10) {
            if (lastAnim == 0) {
                grid.slideCovers(Piece.Direction.SOUTH);
                lastAnim = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - lastAnim >= 500) {
                grid.slideCovers(Piece.Direction.SOUTH);
                lastAnim = System.currentTimeMillis();
            }
        } else if (orientations[1] - pitchAngle > 10) {
            if (lastAnim == 0) {
                grid.slideCovers(Piece.Direction.NORTH);
                lastAnim = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - lastAnim >= 500) {
                grid.slideCovers(Piece.Direction.NORTH);
                lastAnim = System.currentTimeMillis();
            }
        }
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        rollAngle = pref.getFloat(ROLL_KEY, 0f);
        Log.d("HALP!", String.valueOf(rollAngle));
        pitchAngle = pref.getFloat(PITCH_KEY, 0f);
        Log.d("HALP!", String.valueOf(pitchAngle));
        sensorManager.registerListener(this, rvSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

}
