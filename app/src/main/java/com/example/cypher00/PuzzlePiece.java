package com.example.cypher00;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * View class that contains the coordinates of the piece, and its current rotation
 */
public class PuzzlePiece extends View {

    private float rotation;
    private int i_coordinate;
    private int j_coordinate;

    public PuzzlePiece(Context context) {
        super(context);
        init();
    }

    public PuzzlePiece(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public PuzzlePiece(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        rotation = 0.f;
    }

    public float getCurrentRotation() {
        return this.rotation;
    }

    public void increaseRotation(float increase) {
        this.rotation = this.rotation + increase;
    }

    public void setCoordinates(int i, int j) {
        this.i_coordinate = i;
        this.j_coordinate = j;
    }

    public int getI() {
        return i_coordinate;
    }

    public int getJ() {
        return j_coordinate;
    }

}
