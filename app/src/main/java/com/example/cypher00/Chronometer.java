package com.example.cypher00;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Chronometer extends android.support.v7.widget.AppCompatTextView {

    private static final String FORMAT = "mm:ss.SSS";
    private static final long delay = 20;

    private boolean isRunning = false;
    private long offset;

    public Chronometer(Context context) {
        super(context);
        setText("00:00.000");
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
                if (isRunning) h.postDelayed(this, delay);
            }
        }, delay);
    }

    public void stop() {
        isRunning = false;
    }

    public long getTime() {
        if (isRunning)
            return Calendar.getInstance().getTimeInMillis() - offset;
        else return -1;
    }

}