package com.example.cypher00;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class MyCursorAdapter extends CursorAdapter {
    public MyCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.match_history_list, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView tvOpponent = view.findViewById(R.id.opponent_mh);
        TextView tvWinner = view.findViewById(R.id.winner_mh);
        TextView tvMatchTime = view.findViewById(R.id.matchTime_mh);
        TextView tvDifficulty = view.findViewById(R.id.difficulty_mh);
        // Extract properties from cursor
        String opponent = cursor.getString(cursor.getColumnIndexOrThrow("opponent")) + "-";
        String winner = cursor.getString(cursor.getColumnIndexOrThrow("winner")) + "-";
        long matchTime =  cursor.getLong(cursor.getColumnIndexOrThrow("matchTime"));
        int difficulty = cursor.getInt(cursor.getColumnIndexOrThrow("difficulty"));
        // Populate fields with extracted properties
        tvOpponent.setText(opponent);
        tvWinner.setText(winner );
        tvMatchTime.setText(String.valueOf(matchTime));
        tvDifficulty.setText(String.valueOf(difficulty));
    }
}