package com.example.cypher00;

import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class PieceGrid {

    private int dim;
    private ArrayList<ArrayList<Piece>> pieces;
    private Cover[][] covers;
    private int ncovers;
    private View.OnClickListener listener;

    public PieceGrid(int dim, int ncovers, ConstraintLayout layout, int coverSize, View.OnClickListener listener) {
        this.dim = dim;
        this.ncovers = ncovers;
        this.pieces = new ArrayList<>();
        covers = new Cover[dim][dim];
        Log.d("HALPSQUARED", String.valueOf(ncovers));

        for (int i = 0; i < ncovers; i++) {
            Log.d("HALPSQUARED", coverSize * (i % dim) + "," + coverSize * (i/dim));

            Cover cover = new Cover(layout.getContext());
            layout.addView(cover);
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) cover.getLayoutParams();
            params.height = coverSize;
            params.width = coverSize;
            params.topToTop = R.id.table1;
            params.startToStart = R.id.table1;
            cover.setLayoutParams(params);
            cover.setIncrement(coverSize);
            cover.setPositionX(coverSize * (i % dim));
            cover.setPositionY(coverSize * (int) (i / dim));
            cover.setTranslationX(coverSize * (i % dim));
            cover.setTranslationY(coverSize * (int) (i / dim));
            // cover.setOnClickListener((GameTab) layout.getContext());
            this.listener = listener;
            cover.setOnClickListener(listener);
            cover.invalidate();
            cover.setBackgroundResource(R.drawable.cover_black);
            covers[i / dim][i % dim] = cover;
        }
    }

    public PieceGrid(ArrayList<ArrayList<Piece>> pieces, int ncovers, ConstraintLayout layout, int coverSize, View.OnClickListener listener) {
        this.pieces = pieces;
        this.ncovers = ncovers;
        this.dim = pieces.size();
//        this.pieces = new ArrayList<>();
        covers = new Cover[dim][dim];
        Log.d("HALPSQUARED", String.valueOf(ncovers));

        for (int i = 0; i < ncovers; i++) {
            Log.d("HALPSQUARED", i % dim + "," + i/dim);

            Cover cover = new Cover(layout.getContext());
            layout.addView(cover);
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) cover.getLayoutParams();
            params.height = coverSize;
            params.width = coverSize;
            params.topToTop = R.id.table1;
            params.startToStart = R.id.table1;
            cover.setLayoutParams(params);
            cover.setIncrement(coverSize);
            cover.setPositionX(coverSize * (i % dim));
            cover.setPositionY(coverSize * (int) (i / dim));
            cover.setTranslationX(coverSize * (i % dim));
            cover.setTranslationY(coverSize * (int) (i / dim));
            //cover.setOnClickListener((GameTab) layout.getContext());
            this.listener = listener;
            cover.setOnClickListener(listener);
            cover.invalidate();
            cover.setBackgroundResource(R.drawable.cover_black);
            covers[i / dim][i % dim] = cover;
        }
    }

    public ArrayList<ArrayList<Piece>> generateGrid() {
        // allocate arrays
        for (int i = 0; i < this.dim; i++) {
            pieces.add(new ArrayList<Piece>());
            for (int j = 0; j < this.dim; j++){
                // first row
                if (i == 0) {
                    if (j == 0)
                        pieces.get(i).add(new Piece(0b0110));
                    else if (j == this.dim - 1)
                        pieces.get(i).add(new Piece(0b0011));
                    else
                        pieces.get(i).add(new Piece(0b0111));
                // last row
                } else if (i == this.dim - 1) {
                    if (j == 0)
                        pieces.get(i).add(new Piece(0b1100));
                    else if (j == this.dim - 1)
                        pieces.get(i).add(new Piece(0b1001));
                    else
                        pieces.get(i).add(new Piece(0b1101));
                // middle rows
                } else {
                    if (j == 0)
                        pieces.get(i).add(new Piece(0b1110));
                    else if (j == this.dim - 1)
                        pieces.get(i).add(new Piece(0b1011));
                    else
                        pieces.get(i).add(new Piece(0b1111));
                }
            }
        }
        // for every item in the matrix pick randomly which stick to remove
        //      and remove his neighbour
        for (int i = 0; i < this.dim; i++) {
            for (int j = 0; j < this.dim; j++) {
                tryToRemove(i, j, Piece.Direction.NORTH, 0.15f);
                tryToRemove(i, j, Piece.Direction.EAST, 0.15f);
                tryToRemove(i, j, Piece.Direction.SOUTH, 0.15f);
                tryToRemove(i, j, Piece.Direction.WEST, 0.15f);
            }
        }

        // for every item in the matrix rotate randomly a number of times
        //      between 0-3
        for (int i = 0; i < this.dim; i++) {
            for (int j = 0; j < this.dim; j++) {
                double seed = Math.random();
                if (seed <= 0.33) {
                    pieces.get(i).get(j).rotate();
                } else if (seed > 0.33 && seed <= 0.66) {
                    pieces.get(i).get(j).rotate(2);
                } else {
                    pieces.get(i).get(j).rotate(3);
                }
            }
        }
        return pieces;
    }

    public static ArrayList<ArrayList<Piece>> generateGridS(int dim) {
        ArrayList<ArrayList<Piece>> pieces = new ArrayList<>();
        // allocate arrays
        for (int i = 0; i < dim; i++) {
            pieces.add(new ArrayList<Piece>());
            for (int j = 0; j < dim; j++){
                // first row
                if (i == 0) {
                    if (j == 0)
                        pieces.get(i).add(new Piece(0b0110));
                    else if (j == dim - 1)
                        pieces.get(i).add(new Piece(0b0011));
                    else
                        pieces.get(i).add(new Piece(0b0111));
                    // last row
                } else if (i == dim - 1) {
                    if (j == 0)
                        pieces.get(i).add(new Piece(0b1100));
                    else if (j == dim - 1)
                        pieces.get(i).add(new Piece(0b1001));
                    else
                        pieces.get(i).add(new Piece(0b1101));
                    // middle rows
                } else {
                    if (j == 0)
                        pieces.get(i).add(new Piece(0b1110));
                    else if (j == dim - 1)
                        pieces.get(i).add(new Piece(0b1011));
                    else
                        pieces.get(i).add(new Piece(0b1111));
                }
            }
        }
        // for every item in the matrix pick randomly which stick to remove
        //      and remove his neighbour
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                tryToRemoveS(i, j, Piece.Direction.NORTH, 0.15f, pieces);
                tryToRemoveS(i, j, Piece.Direction.EAST, 0.15f, pieces);
                tryToRemoveS(i, j, Piece.Direction.SOUTH, 0.15f, pieces);
                tryToRemoveS(i, j, Piece.Direction.WEST, 0.15f, pieces);
            }
        }
        // for every item in the matrix rotate randomly a number of times
        //      between 0-3
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                double seed = Math.random();
                if (seed <= 0.33) {
                    pieces.get(i).get(j).rotate();
                } else if (seed > 0.33 && seed <= 0.66) {
                    pieces.get(i).get(j).rotate(2);
                } else {
                    pieces.get(i).get(j).rotate(3);
                }
            }
        }
        return pieces;
    }

    public void slideCovers(Piece.Direction direction) {
        switch (direction) {
            case NORTH:
                for (int i = 0; i < covers.length; i++) {
                    for (int j = 0; j < covers.length; j++) {
                        if (covers[i][j] != null && !covers[i][j].isBlocked()) {
                            if (i > 0 && covers[i - 1][j] == null) {
                                covers[i - 1][j] = covers[i][j];
                                covers[i][j].slideCover(Piece.Direction.NORTH);
                                covers[i][j] = null;
                            }
                        }
                    }
                }
                break;
            case EAST:
                for (int i = 0; i < covers.length; i++) {
                    for (int j = covers.length - 1; j >= 0; j--) {
                        if (covers[i][j] != null && !covers[i][j].isBlocked()) {
                            if (j < covers.length - 1 && covers[i][j + 1] == null) {
                                covers[i][j + 1] = covers[i][j];
                                covers[i][j].slideCover(Piece.Direction.EAST);
                                covers[i][j] = null;
                            }
                        }
                    }
                }
                break;
            case SOUTH:
                for (int i = covers.length - 1; i >= 0; i--) {
                    for (int j = 0; j < covers.length; j++) {
                        if (covers[i][j] != null && !covers[i][j].isBlocked()) {
                            if (i < covers.length - 1 && covers[i + 1][j] == null) {
                                covers[i + 1][j] = covers[i][j];
                                covers[i][j].slideCover(Piece.Direction.SOUTH);
                                covers[i][j] = null;
                            }
                        }
                    }
                }
                break;
            case WEST:
                for (int i = 0; i < covers.length; i++) {
                    for (int j = 0; j < covers.length; j++) {
                        if (covers[i][j] != null && !covers[i][j].isBlocked()) {
                            if (j > 0 && covers[i][j - 1] == null) {
                                covers[i][j - 1] = covers[i][j];
                                covers[i][j].slideCover(Piece.Direction.WEST);
                                covers[i][j] = null;
                            }
                        }
                    }
                }
                break;
        }
    }

    public boolean has(int i, int j, Piece.Direction direction) {
        return pieces.get(i).get(j).has(direction);
    }

    public void rotate(int i, int j) {
        pieces.get(i).get(j).rotate();
    }

    //try to remove "dir" stick with "probability" probability, if existent
    private void tryToRemove(int i, int j, Piece.Direction direction, float probability) {
        if (this.pieces.get(i).get(j).has(direction)) {
            if (Math.random() <= probability) {
                pieces.get(i).get(j).remove(direction);
                removeNeighbour(i, j, direction);
            }
        }
    }
    private static void tryToRemoveS(int i, int j, Piece.Direction direction, float probability,  ArrayList<ArrayList<Piece>> pieces) {
        if (pieces.get(i).get(j).has(direction)) {
            if (Math.random() <= probability) {
                pieces.get(i).get(j).remove(direction);
                removeNeighbourS(i, j, direction, pieces);
            }
        }
    }

    // gets the neighbour and removes it, if existent
    private int removeNeighbour(int i, int j, Piece.Direction direction) {
        switch (direction) {
            case NORTH:
                pieces.get(i - 1).get(j).remove(Piece.Direction.SOUTH);
                return 1;
            case EAST:
                pieces.get(i).get(j + 1).remove(Piece.Direction.WEST);
                return 1;
            case SOUTH:
                pieces.get(i + 1).get(j).remove(Piece.Direction.NORTH);
                return 1;
            case WEST:
                pieces.get(i).get(j - 1).remove(Piece.Direction.EAST);
                return 1;
            default: return -1;
        }

    }
    private static int removeNeighbourS(int i, int j, Piece.Direction direction, ArrayList<ArrayList<Piece>> pieces) {
        switch (direction) {
            case NORTH:
                pieces.get(i - 1).get(j).remove(Piece.Direction.SOUTH);
                return 1;
            case EAST:
                pieces.get(i).get(j + 1).remove(Piece.Direction.WEST);
                return 1;
            case SOUTH:
                pieces.get(i + 1).get(j).remove(Piece.Direction.NORTH);
                return 1;
            case WEST:
                pieces.get(i).get(j - 1).remove(Piece.Direction.EAST);
                return 1;
            default: return -1;
        }
    }

    public int getB(int i, int j) {
        return this.pieces.get(i).get(j).getBitmask();
    }

    private static byte getByte(int i, int j, ArrayList<ArrayList<Piece>> pieces) {
        return (byte) pieces.get(i).get(j).getBitmask();
    }

    public static int[] fromGridToBits(PieceGrid grid) {
        int difficulty = grid.getDim();
        int[] bits = new int[difficulty * difficulty];
        for (int i = 0; i < difficulty; i++) {
            for (int j = 0; j < difficulty; j++) {
                bits[i * difficulty + j] = grid.getB(i, j);
            }
        }
        return bits;
    }
    public static byte[] fromGridToByteBuffer(ArrayList<ArrayList<Piece>> pieces) {
        int difficulty = pieces.size();
        //int difficulty = grid.getDim();
        byte[] bytes = new byte[difficulty * difficulty];
        for (int i = 0; i < difficulty; i++) {
            for (int j = 0; j < difficulty; j++) {
                bytes[i * difficulty + j] = getByte(i, j, pieces);
                // TODO CLEAN
                //byte) pieces.get(i).get(j).getBitmask();
                        //grid.getByte(i, j);
            }
        }
        return bytes;
    }


    public void removeCovers() {
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < this.dim; j++) {
                if (covers[i][j] != null) {
                    covers[i][j].setOnClickListener(null);
                    covers[i][j].setVisibility(View.GONE);
                }
            }
        }
    }

    public int getDim() {
        return this.dim;
    }

}
