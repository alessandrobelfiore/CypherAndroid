package com.example.cypher00;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Objects;

import static android.content.Context.SENSOR_SERVICE;
import static com.example.cypher00.KeysUtils.ANIM_KEY;
import static com.example.cypher00.KeysUtils.COVERS_KEY;
import static com.example.cypher00.KeysUtils.COVERS_NO;
import static com.example.cypher00.KeysUtils.COVERS_SOME;
import static com.example.cypher00.KeysUtils.COVERS_YES;
import static com.example.cypher00.KeysUtils.DIFFICULTY_KEY;
import static com.example.cypher00.KeysUtils.DIFFICULTY_EASY;
import static com.example.cypher00.KeysUtils.DIFFICULTY_HARD;
import static com.example.cypher00.KeysUtils.DIFFICULTY_MEDIUM;
import static com.example.cypher00.KeysUtils.GRID_BYTES_KEY;
import static com.example.cypher00.KeysUtils.INSERT_DB;
import static com.example.cypher00.KeysUtils.MESSAGE_CODE;
import static com.example.cypher00.KeysUtils.MESSAGE_READ;
import static com.example.cypher00.KeysUtils.MODE_KEY;
import static com.example.cypher00.KeysUtils.MULTI_PLAYER_CLIENT;
import static com.example.cypher00.KeysUtils.MULTI_PLAYER_HOST;
import static com.example.cypher00.KeysUtils.MY_MATCH_TIME;
import static com.example.cypher00.KeysUtils.NOTIFY;
import static com.example.cypher00.KeysUtils.OPPONENT_KEY;
import static com.example.cypher00.KeysUtils.PITCH_KEY;
import static com.example.cypher00.KeysUtils.PLAY_AGAIN;
import static com.example.cypher00.KeysUtils.PLAY_AGAIN_KEY;
import static com.example.cypher00.KeysUtils.READY;
import static com.example.cypher00.KeysUtils.RESULT;
import static com.example.cypher00.KeysUtils.ROLL_KEY;
import static com.example.cypher00.KeysUtils.SINGLE_PLAYER;
import static com.example.cypher00.KeysUtils.SYNCHRONY_KEY;
import static com.example.cypher00.KeysUtils.WINNER_KEY;
import static com.example.cypher00.KeysUtils.WINNER_MATCH_TIME;

public class GameFragment extends Fragment implements View.OnClickListener, SensorEventListener {

    // Views
    private ConstraintLayout layout;
    private TableLayout table;
    private PieceGrid grid;
    private Chronometer timer;
    private Button backToMenu;
    private Button playAgain;
    private TextView victoryAlert;

    // Objects
    private SensorManager sensorManager;
    private Sensor rvSensor;
    private mReceiver rec;
    private String opponentName;
    private MediaPlayer victorySound;

    // Primitives
    private int dimension;
    private int difficulty;
    private int mode;
    private int nCovers;
    private float pitchAngle;
    private float rollAngle;
    private long lastAnim = 0;
    private long matchTime;
    private long enemyMatchTime;
    private long winnerMatchTime;
    private long animTime = 133;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_game_tab, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("TESTS",String.valueOf(isSynchronySet()));
        Bundle extras = getArguments();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        animTime = Long.parseLong(Objects.requireNonNull(pref.getString(ANIM_KEY, "133")));
        rollAngle = pref.getFloat(ROLL_KEY, 0f);
        pitchAngle = pref.getFloat(PITCH_KEY, 0f);
        difficulty = extras.getInt(DIFFICULTY_KEY, DIFFICULTY_EASY);
        int covers = extras.getInt(COVERS_KEY, COVERS_NO);
        Log.d("RESTART", "CREATION  " + covers);
        dimension = getDimensionFromDifficulty(difficulty);
        nCovers = getNCoversFromCoverKey(covers);
        opponentName = extras.getString(OPPONENT_KEY);
        matchTime = enemyMatchTime = winnerMatchTime = -1;

        sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        rvSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        table = view.findViewById(R.id.table1);
        timer = view.findViewById(R.id.timer);
        victoryAlert = view.findViewById(R.id.victory_alert);
        layout = view.findViewById(R.id.game_layout);
        backToMenu = view.findViewById(R.id.back_to_menu);
        playAgain = view.findViewById(R.id.play_again);

        playAgain.setOnClickListener(this);
        backToMenu.setOnClickListener(this);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int borderSize = 64;
        int coverSize = Math.min(displayMetrics.widthPixels - borderSize, displayMetrics.heightPixels - borderSize) / dimension;

        if (savedInstanceState == null) {
            // checks if is in MULTI PLAYER MODE
            Log.d("RESTART", "FIRST GAME " + nCovers);
            mode = extras.getInt(MODE_KEY, SINGLE_PLAYER);
            if (mode == SINGLE_PLAYER) {
                grid = new PieceGrid(dimension, nCovers, layout, coverSize, this);
                grid.generateGrid();
            } else if (mode == MULTI_PLAYER_CLIENT || mode == MULTI_PLAYER_HOST) {
                byte[] bytes = extras.getByteArray(GRID_BYTES_KEY);
                ArrayList<ArrayList<Piece>> pieces = new ArrayList<>();
                for (int i = 0; i < dimension; i++) {
                    pieces.add(new ArrayList<Piece>());
                    for (int j = 0; j < dimension; j++) {
                        assert bytes != null;
                        pieces.get(i).add(new Piece(bytes[i * dimension + j]));
                    }
                }
                this.grid = new PieceGrid(pieces, nCovers, layout, coverSize, this);
//                Log.d("GRIDDIM", String.valueOf(this.grid.getDim()));
            }
        } else {
            dimension = savedInstanceState.getInt("GRID_DIM");
            int[] bits = savedInstanceState.getIntArray("GRID_BITS");
//            Log.d("RESTART", "grid bits length " + bits.length);
//            //debug
//            StringBuilder builder = new StringBuilder();
//            for (int b : bits) {
//                builder.append('|');
//                builder.append(b);
//            }
//            builder.append('|');
//            Log.d("RESTART", "ON RESTART: " + builder.toString());
            ArrayList<ArrayList<Piece>> pieces = new ArrayList<>();
            for (int i = 0; i < dimension; i++) {
                pieces.add(new ArrayList<Piece>());
                for (int j = 0; j < dimension; j++) {
                    pieces.get(i).add(new Piece(bits[i * dimension + j]));
                }
            }
            this.grid = new PieceGrid(pieces, nCovers, layout, coverSize, this);
        }
        for (int i = 0; i < dimension; i++) {
            TableRow row = new TableRow(getContext());
            table.addView(row);
            Resources res = getResources();
            for (int j = 0; j < dimension; j++) {
                PuzzlePiece piece = new PuzzlePiece(getContext());
                piece.setCoordinates(i, j);
                setImageFromBitmask(grid.getB(i, j), res, piece);
                piece.setLayoutParams(new TableRow.LayoutParams(coverSize, coverSize));
                piece.setOnClickListener(this);
                row.addView(piece);
                piece.requestLayout();
            }
        }
        timer.start();
        if (mode != SINGLE_PLAYER) {
            Socket socket = MessengerService.getSocket();
            if (rec == null) {
                rec = new mReceiver(socket);
                rec.start();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("GRID_DIM", dimension);
        int[] bits = PieceGrid.fromGridToBits(this.grid);
        Log.d("RESTART", "grid bits length " + bits.length);
//        // debug
//        StringBuilder builder = new StringBuilder();
//        for (int b : bits) {
//            builder.append('|');
//            builder.append(b);
//        }
//        builder.append('|');
//        Log.d("RESTART", "ON SAVE: " + builder.toString());
        outState.putIntArray("GRID_BITS", bits);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back_to_menu) {
            Intent intent = new Intent(getContext(), MainActivity.class);
            startActivity(intent);
            this.getActivity().finish();
        } else if (v instanceof PuzzlePiece) {
            grid.rotate(((PuzzlePiece) v).getI(), ((PuzzlePiece) v).getJ());
            rotatePiece((PuzzlePiece) v);
            if (checkVictory()) {
                stopGame();
                // pop escape buttons
            }
        } else if (v instanceof Cover) {
            ((Cover) v).block();
        } else if(v.getId() == R.id.play_again) {
            if (mode == SINGLE_PLAYER) {
                GameFragment gf = new GameFragment();
                gf.setArguments(getArguments());
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, gf).commit();
            } else {
                ConnectionFragment cf = new ConnectionFragment();
                Bundle extras = getArguments();
                extras.putInt(PLAY_AGAIN_KEY, 1);
                cf.setArguments(extras);
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, cf).commit();
            }
        }
    }

    //UTILITY METHODS

    /**
     * Sets the background image of the piece based on its logical bitmask
     * @param bitmask the bitmask to be interpreted
     * @param res the resources
     * @param piece the piece to be given the background
     */
    public void setImageFromBitmask(int bitmask, Resources res, PuzzlePiece piece) {
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

    /**
     * Interprets the difficulty keys into a dimension of the grid
     * @param difficulty the difficulty key to interpret
     * @return the dimension of the grid, expressed as number of columns/rows
     */
    static int getDimensionFromDifficulty(int difficulty) {
        switch(difficulty) {
            case DIFFICULTY_EASY: return 4;
            case DIFFICULTY_MEDIUM : return 5;
            case DIFFICULTY_HARD : return 6;
            default : return 4;
        }
    }

    /**
     * Generate the number of covers to create, based on the cover key and dimension of the grid
     * @param covers the covers key to interpret
     * @return the number of covers to generate
     */
    private int getNCoversFromCoverKey(int covers) {
        switch(covers) {
            case COVERS_NO: return 0;
            case COVERS_SOME : return dimension;
            case COVERS_YES : return (dimension * dimension) - 1;
            default : return 0;
        }
    }

    /**
     * Animates the piece doing a rotation on his center
     * @param p the piece to rotate
     */
    public void rotatePiece(PuzzlePiece p) {
        ObjectAnimator animator;
        animator = ObjectAnimator.ofFloat(p, "rotation", p.getCurrentRotation(), p.getCurrentRotation() + 90f);
        animator.setDuration(animTime);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
        p.increaseRotation(90f);
    }

    /**
     * Stops the ongoing game, removes the onClick Listeners,
     *      in singlePlayer displays the result,
     *      in MultiPlayer displays the result if possible,
     *      else sends a message to the other player
     */
    private void stopGame() {
        this.grid.removeCovers();
        for (int i = 0; i < this.dimension; i++) {
            TableRow row = (TableRow) table.getChildAt(i);
            for (int j = 0; j < this.dimension; j++)
                row.getChildAt(j).setOnClickListener(null);
        }
        matchTime = timer.getTime();
        timer.stop();
        if (mode == SINGLE_PLAYER) {
            winnerMatchTime = matchTime;
            victory();
            // first notify, enemy hasn't finished
        } else if (enemyMatchTime == -1) {
//            Log.d("TEST", "PRIMA NOTIFY " + String.valueOf(enemyMatchTime));
            sendMessage(NOTIFY);
            // I won, not synchronized, HIS NOTIFY ALREADY ARRIVED
            //  but his timer started early
        } else if (enemyMatchTime > matchTime) {
//            Log.d("TEST", "SECONDA NOTIFY, PERDENTE  " + String.valueOf(enemyMatchTime));
            winnerMatchTime = matchTime;
            sendMessage(RESULT);
            victory();
            // prompted by onEnemyWinner Listener
        } else if (matchTime > enemyMatchTime) {
//            Log.d("TEST", "onENEMYLISTENER, HO PERSO " + String.valueOf(enemyMatchTime));
            winnerMatchTime = enemyMatchTime;
            sendMessage(RESULT);
            defeat();
        } else {
            Log.d("TEST", "DRAW..for real?");
        }
    }

    /**
     * Checks if the grid has been resolved
     * @return true if the grid is resolved
     *          false else
     */
    private boolean checkVictory() {
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++) {
                if (i == 0 && grid.has(i, j, Piece.Direction.NORTH)) return false;
                if (j == 0 && grid.has(i, j, Piece.Direction.WEST)) return false;
                if (i == (dimension - 1) && grid.has(i, j, Piece.Direction.SOUTH)) return false;
                if (j == (dimension - 1) && grid.has(i, j, Piece.Direction.EAST)) return false;
                if (i != 0 && grid.has(i, j, Piece.Direction.NORTH) && !grid.has(i - 1, j, Piece.Direction.SOUTH))
                    return false;
                if (j != 0 && grid.has(i, j, Piece.Direction.WEST) && !grid.has(i, j - 1, Piece.Direction.EAST))
                    return false;
                if (i != (dimension - 1) && grid.has(i, j, Piece.Direction.SOUTH) && !grid.has(i + 1, j, Piece.Direction.NORTH))
                    return false;
                if (j != (dimension - 1) && grid.has(i, j, Piece.Direction.EAST) && !grid.has(i, j + 1, Piece.Direction.WEST))
                    return false;
            }
        /*if (i !== 0 && GS.squares[i][j].n && !GS.squares[i - 1][j].s) return;
        if (j !== 0 && GS.squares[i][j].w && !GS.squares[i][j - 1].e) return;
        if (i !== GS.rows - 1 && GS.squares[i][j].s && !GS.squares[i + 1][j].n) return;
        if (j !== GS.columns - 1 && GS.squares[i][j].e && !GS.squares[i][j + 1].w) return; */
        // TODO Perfectionism (LIGHTS)
        return true;
    }
    // TODO Perfectionism
    /*private boolean checkVictory(int i, int j) {
        return true;
    }*/

    /**
     * Given a message code, creates an intent that contains the info to be sent to the service
     * @param code the code that identifies the message to be sent
     */
    private void sendMessage(byte code) {
        Intent serviceIntent = new Intent(getContext(), MessengerService.class);
        serviceIntent.putExtra(MESSAGE_CODE, code);
        switch (code) {
            case NOTIFY:
                serviceIntent.putExtra(MY_MATCH_TIME, matchTime);
                break;
            case RESULT:
                serviceIntent.putExtra(WINNER_MATCH_TIME, winnerMatchTime);
                break;
            case INSERT_DB:
                serviceIntent.putExtra(WINNER_MATCH_TIME, winnerMatchTime);
                serviceIntent.putExtra(DIFFICULTY_KEY, difficulty);
                if (mode == SINGLE_PLAYER) {
                    serviceIntent.putExtra(OPPONENT_KEY, getString(R.string.training));
                    serviceIntent.putExtra(WINNER_KEY, "WIN");
                } else if (winnerMatchTime == matchTime) {
                    serviceIntent.putExtra(WINNER_KEY, "WIN");
                    serviceIntent.putExtra(OPPONENT_KEY, opponentName);
                } else {
                    serviceIntent.putExtra(WINNER_KEY, "LOSE");
                    serviceIntent.putExtra(OPPONENT_KEY, opponentName);
                }
//                Log.d("DAO", "MANDO INSERT");
//                Log.d("DAO", "matchTime: " + winnerMatchTime);
                break;
            default:
                Log.d("TEST", "Receiver GameFrag corrupted msg");
                break;
        }
        Objects.requireNonNull(getContext()).startService(serviceIntent);
    }

    /**
     * Thread class used to delegate receiving messages to the handler
     */
    public class mReceiver extends Thread {
        private Socket socket;
        private InputStream inputStream;

        mReceiver(Socket sock) {
            socket = sock;
            try {
                inputStream = socket.getInputStream();
                socket.setSoTimeout(9);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            Log.d("HALP", "THREAD RECEIVER CREATO");
            while (!this.isInterrupted()) {
                byte[] buffer = new byte[1024];
                int bytes;
                while (socket != null && !this.isInterrupted()) {
                    try {
                        bytes = inputStream.read(buffer);
                        if (bytes > 0) {
                            handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                        }
                    } catch (SocketTimeoutException e) {
                        Log.d("HALP", "TIMMEOUT in gf");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        void stopThread() {
            this.interrupt();
        }
    }

    // Handler receiver (ThreadHandler?)
    /**
     * Respectively to the message received:
     *      notify: sets onEnemyWinnerListener and, if the method is called, sends RESULT
     *      result: displays the result
     */
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    byte[] readBuff = (byte[]) msg.obj;
                    StringBuilder builder = new StringBuilder();
                    for (byte b : readBuff) {
                        builder.append('|');
                        builder.append(b);
                    }
                    builder.append('|');
                    Log.d("TEST", "IN GAME: " + builder.toString());
                    switch (readBuff[0]) {
                        case READY:
                            break;
                        case NOTIFY:
                            byte[] time = new byte[msg.arg1 - 1];
                            System.arraycopy(readBuff, 1, time, 0, msg.arg1 - 1);
//                            Log.d("TEST", "msg.arg1 in notify: " + String.valueOf(msg.arg1));
                            enemyMatchTime = ByteUtils.bytesToLong(time);
//                            Log.d("TEST", "enemyMatchTime: " + String.valueOf(enemyMatchTime));
//                            Log.d("TEST", "ARRIVATA NOTIFY " + String.valueOf(enemyMatchTime));
                            // I AM STILL PLAYING
                            if (matchTime == -1 && timer.getTime() != -1) {
                                timer.setEnemyMatchTime(enemyMatchTime);
                                timer.setOnEnemyWinnerListener(new Chronometer.OnEnemyWinnerListener() {
                                    @Override
                                    public void onEnemyWinner() {
                                        if (isSynchronySet()) {
                                            stopGame();
                                        } else {
//                                        Log.d("TEST", "onENEMYLISTENER, HO PERSO " + String.valueOf(enemyMatchTime));
                                            winnerMatchTime = enemyMatchTime;
                                            sendMessage(RESULT);
                                        }
//                                    defeat();
                                    }
                                });
                            }
                            // I have already FINISHED
                            else if (matchTime != -1 && timer.getTime() == -1) {
                                if (enemyMatchTime <= matchTime) {
//                                    Log.d("TEST", "SECONDA NOTIFY; VINCENTE " + String.valueOf(enemyMatchTime));
                                    winnerMatchTime = enemyMatchTime;
                                    sendMessage(RESULT);
                                    defeat();
                                } else {
//                                    Log.d("TEST", "SECONDA NOTIFY, PERDENTE " + String.valueOf(enemyMatchTime));
                                    winnerMatchTime = matchTime;
                                    sendMessage(RESULT);
                                    victory();
                                }
                            }
                            break;
                        case RESULT:
                            time = new byte[msg.arg1 - 1];
//                            Log.d("TEST", "msg.arg1 in result: " + String.valueOf(msg.arg1));
                            System.arraycopy(readBuff, 1, time, 0, msg.arg1 - 1);
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

    /**
     * Displays the victory screen and saves the match in the database
     */
    private void victory() {
        victoryAlert.setText(getContext().getString(R.string.victory));
        victoryAlert.setVisibility(View.VISIBLE);
        victorySound = MediaPlayer.create(getContext(), R.raw.win);
        victorySound.start();
        sendMessage(INSERT_DB);
        if (mode != SINGLE_PLAYER) rec.stopThread();
    }

    /**
     * Displays the defeat screen and saves the match in the database
     */
    private void defeat() {
        victoryAlert.setText(getContext().getString(R.string.defeat));
        sendMessage(INSERT_DB);
        if (mode != SINGLE_PLAYER) rec.stopThread();
    }

    /**
     * @return true if the MultiPlayer Synchrony is set,
     *         false else
     */
    private boolean isSynchronySet() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        return pref.getBoolean(SYNCHRONY_KEY, true);
    }

    // OVERRIDES

    /**
     * Checks if the sensor values are too different from the base ones,
     *      in that case slides the covers able to do so, at a given frequency
     * @param sensorEvent the event fired by the sensor
     */
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
//        Log.d("HALP!", String.valueOf(rollAngle));
        pitchAngle = pref.getFloat(PITCH_KEY, 0f);
//        Log.d("HALP!", String.valueOf(pitchAngle));
        sensorManager.registerListener(this, rvSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

}
