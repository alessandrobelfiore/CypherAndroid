//package com.example.cypher00;
//
//import android.animation.ObjectAnimator;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.res.Resources;
//import android.graphics.drawable.Drawable;
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;
//import android.os.Bundle;
//import android.preference.PreferenceManager;
//import android.support.constraint.ConstraintLayout;
//import android.support.v7.app.AppCompatActivity;
//import android.util.DisplayMetrics;
//import android.util.Log;
//import android.view.View;
//import android.view.animation.AccelerateDecelerateInterpolator;
//import android.widget.Button;
//import android.widget.TableLayout;
//import android.widget.TableRow;
//import android.widget.TextView;
//
//import java.util.ArrayList;
//
//import static com.example.cypher00.MainActivity.MULTI_PLAYER_CLIENT;
//import static com.example.cypher00.MainActivity.MULTI_PLAYER_HOST;
//import static com.example.cypher00.MainActivity.SINGLE_PLAYER;
//import static com.example.cypher00.SelectModeTab.MODE_KEY;
//
//public class GameTab extends AppCompatActivity implements View.OnClickListener, SensorEventListener {
//
//    private Chronometer timer;
//    private Button backToMenu;
//    private TextView victoryAlert;
//    private TableLayout table;
//    private int difficulty;
//    private float pitchAngle;
//    private float rollAngle;
//    private SensorManager sensorManager;
//    private Sensor rvSensor;
//    private long lastAnim = 0;
//    private PieceGrid grid;
//    private ConstraintLayout layout;
//
//    public static final String DIFFICULTY_KEY = "DIFFICULTY_KEY";
//    public static final String PITCH_KEY = "PITCH_KEY";
//    public static final String ROLL_KEY = "ROLL_KEY";
//    public static final String GRID_BITS_KEY = "GRID_BITS_KEY";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_game_tab);
//        Intent intent = getIntent();
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
//        rollAngle = pref.getFloat(ROLL_KEY, 0f);
//        pitchAngle = pref.getFloat(PITCH_KEY, 0f);
//        difficulty = intent.getIntExtra(DIFFICULTY_KEY, 3);
//
//        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//        rvSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
//
//        table = findViewById(R.id.table1);
//        timer = findViewById(R.id.timer);
//        victoryAlert = findViewById(R.id.victory_alert);
//        layout = findViewById(R.id.game_layout);
//        backToMenu = findViewById(R.id.back_to_menu);
//
//        backToMenu.setOnClickListener(this);
//
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        int dim = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) / difficulty;
//
//        if (savedInstanceState == null) {
//            // checks if is in MULTI PLAYER MODE
//            int mode = intent.getIntExtra(MODE_KEY, SINGLE_PLAYER);
//            if (mode == SINGLE_PLAYER) {
//                grid = new PieceGrid(difficulty, difficulty - 1, layout, dim, this);
//                grid.generateGrid();
//            } else if (mode == MULTI_PLAYER_CLIENT || mode == MULTI_PLAYER_HOST) {
//                int[] bits  = intent.getIntArrayExtra(GRID_BITS_KEY);
//                ArrayList<ArrayList<Piece>> grid = new ArrayList<>();
//                for (int i = 0; i < difficulty; i++) {
//                    grid.add(new ArrayList<Piece>());
//                    for (int j = 0; j < difficulty; j++) {
//                        grid.get(i).add(new Piece(bits[i * difficulty + j]));
//                    }
//                }
//                this.grid = new PieceGrid(grid, difficulty - 1, layout, dim);
//            }
//            // TODO Set ncovers
//            // checkVictory() ? ; TODO Needed?
//        } else {
//            difficulty = savedInstanceState.getInt("GRID_DIM");
//            int[] bits = savedInstanceState.getIntArray("GRID_BITS");
//            ArrayList<ArrayList<Piece>> grid = new ArrayList<>();
//            for (int i = 0; i < difficulty; i++) {
//                grid.add(new ArrayList<Piece>());
//                for (int j = 0; j < difficulty; j++) {
//                    assert bits != null; // TODO necessary for debug?
//                    grid.get(i).add(new Piece(bits[i * difficulty + j]));
//                }
//            }
//            // TODO set ncovers
//            this.grid = new PieceGrid(grid, difficulty - 1, layout, dim);
//        }
//        for (int i = 0; i < difficulty; i++) {
//            TableRow row = new TableRow(this);
//            table.addView(row);
//            Resources res = getResources();
//            for (int j = 0; j < difficulty; j++) {
//                PuzzlePiece piece = new PuzzlePiece(this);
//                piece.setCoordinates(i, j);
//                setImageFromBitmask(grid.getB(i, j), res, piece);
//                piece.setLayoutParams(new TableRow.LayoutParams(dim, dim));
//                piece.setOnClickListener(this);
//                row.addView(piece);
//                piece.requestLayout();
//            }
//        }
//        timer.start();
//    }
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putInt("GRID_DIM", difficulty);
//        int[] bits = PieceGrid.fromGridToBits(this.grid);
//        /*int[] bits = new int[difficulty * difficulty];
//        for (int i = 0; i < difficulty; i++) {
//            for (int j = 0; j < difficulty; j++) {
//                bits[i * difficulty + j] = grid.getB(i, j);
//            }
//        } */
//        outState.putIntArray("GRID_BITS", bits);
//    }
//
//    @Override
//    public void onClick(View v) {
//        if (v.getId() == R.id.back_to_menu) {
//            Intent intent = new Intent(this, MainActivity.class);
//            startActivity(intent);
//        } else if (v instanceof PuzzlePiece) {
//            grid.rotate(((PuzzlePiece) v).getI(), ((PuzzlePiece) v).getJ());
//            rotatePiece((PuzzlePiece) v);
//            if (checkVictory()) {
//                timer.stop();
//                victoryAlert.setText("VICTORY!");
//                stopGame();
//                // pop escape buttons
//
//            } //else Log.d("HALP", "NOOOO");
//        }
//        else if (v instanceof Cover) {
//            //Log.d("HALP", "YEEEEEES");
//            ((Cover) v).block();
//        }
//    }
//
//    //UTILITY METHODS
//    private void setImageFromBitmask(int bitmask, Resources res, PuzzlePiece piece) {
//        Drawable jDrawable = null;
//        switch (bitmask) {
//            case 0b0000:
//                jDrawable = res.getDrawable(R.drawable.ic_piece_0000);
//                break;
//            case 0b0001:
//                jDrawable = res.getDrawable(R.drawable.ic_piece_0001);
//                break;
//            case 0b0010:
//                jDrawable = res.getDrawable(R.drawable.ic_piece_0010);
//                break;
//            case 0b0100:
//                jDrawable = res.getDrawable(R.drawable.ic_piece_0100);
//                break;
//            case 0b1000:
//                jDrawable = res.getDrawable(R.drawable.ic_piece_1000);
//                break;
//            case 0b1010:
//                jDrawable = res.getDrawable(R.drawable.ic_piece_1010);
//                break;
//            case 0b0101:
//                jDrawable = res.getDrawable(R.drawable.ic_piece_0101);
//                break;
//            case 0b0011:
//                jDrawable = res.getDrawable(R.drawable.ic_piece_0011);
//                break;
//            case 0b0110:
//                jDrawable = res.getDrawable(R.drawable.ic_piece_0110);
//                break;
//            case 0b1100:
//                jDrawable = res.getDrawable(R.drawable.ic_piece_1100);
//                break;
//            case 0b1001:
//                jDrawable = res.getDrawable(R.drawable.ic_piece_1001);
//                break;
//            case 0b0111:
//                jDrawable = res.getDrawable(R.drawable.ic_piece_0111);
//                break;
//            case 0b1011:
//                jDrawable = res.getDrawable(R.drawable.ic_piece_1011);
//                break;
//            case 0b1101:
//                jDrawable = res.getDrawable(R.drawable.ic_piece_1101);
//                break;
//            case 0b1110:
//                jDrawable = res.getDrawable(R.drawable.ic_piece_1110);
//                break;
//            case 0b1111:
//                jDrawable = res.getDrawable(R.drawable.ic_piece_1111);
//                break;
//            default:
//                break;
//        }
//        piece.setBackground(jDrawable);
//    }
//
//    private void rotatePiece(PuzzlePiece p) {
//        ObjectAnimator animator;
//        animator = ObjectAnimator.ofFloat(p, "rotation", p.getCurrentRotation(), p.getCurrentRotation() + 90f);
//        animator.setDuration(133);
//        animator.setInterpolator(new AccelerateDecelerateInterpolator());
//        animator.start();
//        p.increaseRotation(90f);
//    }
//
//    private void stopGame() {
//        this.grid.removeCovers();
//            for (int i = 0; i < this.difficulty; i++) {
//                TableRow row = (TableRow) table.getChildAt(i);
//                for (int j = 0; j < this.difficulty; j++)
//                    row.getChildAt(j).setOnClickListener(null);
//            }
//    }
//
//    private boolean checkVictory() {
//        for (int i = 0; i < difficulty; i++)
//            for (int j = 0; j < difficulty; j++) {
//                if (i == 0 && grid.has(i, j, Piece.Direction.NORTH)) return false;
//                if (j == 0 && grid.has(i, j, Piece.Direction.WEST)) return false;
//                if (i == (difficulty - 1) && grid.has(i, j, Piece.Direction.SOUTH)) return false;
//                if (j == (difficulty - 1) && grid.has(i, j, Piece.Direction.EAST)) return false;
//                if (i != 0 && grid.has(i, j, Piece.Direction.NORTH) && !grid.has(i -1, j, Piece.Direction.SOUTH)) return false;
//                if (j != 0 && grid.has(i, j, Piece.Direction.WEST) && !grid.has(i, j -1, Piece.Direction.EAST)) return false;
//                if (i != (difficulty - 1) && grid.has(i, j, Piece.Direction.SOUTH) && !grid.has(i + 1, j, Piece.Direction.NORTH)) return false;
//                if (j != (difficulty - 1) && grid.has(i, j, Piece.Direction.EAST)&& !grid.has(i, j + 1, Piece.Direction.WEST)) return false;
//            }
//        /*if (i !== 0 && GS.squares[i][j].n && !GS.squares[i - 1][j].s) return;
//        if (j !== 0 && GS.squares[i][j].w && !GS.squares[i][j - 1].e) return;
//        if (i !== GS.rows - 1 && GS.squares[i][j].s && !GS.squares[i + 1][j].n) return;
//        if (j !== GS.columns - 1 && GS.squares[i][j].e && !GS.squares[i][j + 1].w) return; */
//        // TODO checkLights
//        return true;
//    }
//    // TODO
//    /*private boolean checkVictory(int i, int j) {
//        return true;
//    }*/
//
//
//    // OVERRIDES
//
//    @Override
//    public final void onSensorChanged(SensorEvent sensorEvent) {
//        float[] rotationMatrix = new float[16];
//        SensorManager.getRotationMatrixFromVector(
//                rotationMatrix, sensorEvent.values);
//
//        // Convert to orientations
//        float[] orientations = new float[3];
//        SensorManager.getOrientation(rotationMatrix, orientations);
//        for (int i = 0; i < 3; i++) {
//            orientations[i] = (float) (Math.toDegrees(orientations[i]));
//        }
//        // customize angle rotations
//        if (orientations[2] - rollAngle < -15) {
//            if (lastAnim == 0) {
//                grid.slideCovers(Piece.Direction.WEST);
//                lastAnim = System.currentTimeMillis();
//            } else if (System.currentTimeMillis() - lastAnim >= 500) {
//                grid.slideCovers(Piece.Direction.WEST);
//                lastAnim = System.currentTimeMillis();
//            }
//        } else if (orientations[2] - rollAngle > 15) {
//            if (lastAnim == 0) {
//                grid.slideCovers(Piece.Direction.EAST);
//                lastAnim = System.currentTimeMillis();
//            } else if (System.currentTimeMillis() - lastAnim >= 500) {
//                grid.slideCovers(Piece.Direction.EAST);
//                lastAnim = System.currentTimeMillis();
//            }
//        } else if (orientations[1] - pitchAngle < -10) {
//            if (lastAnim == 0) {
//                grid.slideCovers(Piece.Direction.SOUTH);
//                lastAnim = System.currentTimeMillis();
//            } else if (System.currentTimeMillis() - lastAnim >= 500) {
//                grid.slideCovers(Piece.Direction.SOUTH);
//                lastAnim = System.currentTimeMillis();
//            }
//        } else if (orientations[1] - pitchAngle > 10) {
//            if (lastAnim == 0) {
//                grid.slideCovers(Piece.Direction.NORTH);
//                lastAnim = System.currentTimeMillis();
//            } else if (System.currentTimeMillis() - lastAnim >= 500) {
//                grid.slideCovers(Piece.Direction.NORTH);
//                lastAnim = System.currentTimeMillis();
//            }
//        }
//    }
//
//    @Override
//    public final void onAccuracyChanged(Sensor sensor, int i) {
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
//        rollAngle = pref.getFloat(ROLL_KEY, 0f);
//        Log.d("HALP!", String.valueOf(rollAngle));
//        pitchAngle = pref.getFloat(PITCH_KEY, 0f);
//        Log.d("HALP!", String.valueOf(pitchAngle));
//        sensorManager.registerListener(this, rvSensor, SensorManager.SENSOR_DELAY_NORMAL);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        sensorManager.unregisterListener(this);
//    }
//
//}
