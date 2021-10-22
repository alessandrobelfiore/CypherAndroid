package com.example.cypher00;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class Cover extends View {

    protected float positionX;
    protected float positionY;
    protected float increment;
    protected float maxX = 1080f;
    private boolean blocked;

    public Cover(Context context) {
        super(context);
        init();
    }

    public Cover(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public Cover(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        positionX = 0.f;
        positionY = 0.f;
        increment = 0.f;
        blocked = false;
        //TODO ?
    }

    public float moveX(Piece.Direction direction) {
        // TODO bounds with other covers
        if (direction == Piece.Direction.EAST) {
            if (this.positionX + this.increment < this.maxX) {
                this.positionX = this.positionX + this.increment;
                return this.positionX;
            } else return this.positionX;
        } else if (direction == Piece.Direction.WEST) {
            if (this.positionX - this.increment >= 0f) {
                this.positionX = this.positionX - this.increment;
                return this.positionX;
            } else return this.positionX;
        }
        return this.positionX;
    }

    public float moveY(Piece.Direction direction) {
        if (direction == Piece.Direction.SOUTH) {
            if (this.positionY + this.increment < this.maxX) {
                this.positionY = this.positionY + this.increment;
                return this.positionY;
            } else return this.positionY;
        } else if (direction == Piece.Direction.NORTH) {
            if (this.positionY - this.increment >= 0f) {
                this.positionY = this.positionY - this.increment;
                return this.positionY;
            } else return this.positionY;
        }
        return this.positionY;
    }

    public void slideCover(Piece.Direction direction) {
        if (!isBlocked()) {
            if (direction == Piece.Direction.NORTH || direction == Piece.Direction.SOUTH) {
                Log.d("HALPMESRZLY", "n-s");
                ObjectAnimator animator;
                animator = ObjectAnimator.ofFloat(this, "translationY", moveY(direction));
                animator.setDuration(133);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.start();
            } else {
                Log.d("HALPMESRZLY", "w-e");
                ObjectAnimator animator;
                animator = ObjectAnimator.ofFloat(this, "translationX", moveX(direction));
                animator.setDuration(133);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.start();
            }
        }
    }

    public void setIncrement(float incr) {
        this.increment = incr;
    }

    public void setPositionX(float posX) {
        this.positionX = posX;
    }

    public void setPositionY(float posY) {
        this.positionY = posY;
    }

    public float getPositionX() {
        return this.positionX;
    }

    public float getPositionY() {
        return this.positionY;
    }

    public void block() {
        if (this.blocked == false) {
            this.blocked = true;
            Drawable d = this.getContext().getDrawable(R.drawable.cover_blocked);
            this.setBackground(d);
        }
        else {
            Drawable d = this.getContext().getDrawable(R.drawable.cover_black);
            this.setBackground(d);
            this.blocked = false;
        }
    }
    public boolean isBlocked() {
        return this.blocked;
    }
}
