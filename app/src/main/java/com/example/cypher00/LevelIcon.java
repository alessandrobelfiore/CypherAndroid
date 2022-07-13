package com.example.cypher00;

import android.content.Context;
import android.util.AttributeSet;

/**
 * View class containing the information pertaining one level
 */
public class LevelIcon extends androidx.appcompat.widget.AppCompatImageButton {

    private int levelId;

    public LevelIcon(Context context) {
        super(context);
    }

    public LevelIcon(Context context, int id) {
        super(context);
        levelId = id;
    }

    public LevelIcon(Context context, AttributeSet attrs, int id) {
        super(context, attrs);
        levelId = id;
    }

    public int getLevelId() {
        return levelId;
    }
}
