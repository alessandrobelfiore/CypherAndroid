package com.example.cypher00;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Chronometer extends androidx.appcompat.widget.AppCompatTextView {

    public interface OnEnemyWinnerListener {
        void onEnemyWinner();
    }

    public static final String FORMAT = "mm:ss.SSS";
    private static final long delay = 20;

    private boolean isRunning = false;
    private long offset;
    private long enemyMatchTime = Long.MAX_VALUE;
    private OnEnemyWinnerListener listener = null;

    public Chronometer(Context context) {
        super(context);
        setText(context.getString(R.string.time_format));
    }

    public Chronometer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Chronometer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void start() {
        isRunning = true;
        offset = Calendar.getInstance().getTimeInMillis();
        final Handler h = new Handler();
        h.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                long time = Calendar.getInstance().getTimeInMillis() - offset;
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat sdf = new SimpleDateFormat(FORMAT);
                setText(sdf.format(time));
                if (enemyMatchTime < time) {
                    if (listener != null) listener.onEnemyWinner();
                }
                if (isRunning) h.postDelayed(this, delay);
            }
        }, delay);
    }

    public void stop() {
        isRunning = false;
    }

    /**
     * @return the time spent since the start of the chronometer if it's running,
     *          -1 if the chronometer is stopped
     */
    public long getTime() {
        if (isRunning)
            return Calendar.getInstance().getTimeInMillis() - offset;
        else return -1;
    }
    //
    public void setEnemyMatchTime(long enemyMatchTime) {
        this.enemyMatchTime = enemyMatchTime;
    }

    public OnEnemyWinnerListener getOnEnemyWinnerListener() {
        return listener;
    }

    public void setOnEnemyWinnerListener(OnEnemyWinnerListener listener) {
        this.listener = listener;
    }

}