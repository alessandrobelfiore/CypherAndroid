package com.example.cypher00;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;

import static com.example.cypher00.Chronometer.FORMAT;
import static com.example.cypher00.KeysUtils.DIFFICULTY_EASY;
import static com.example.cypher00.KeysUtils.DIFFICULTY_HARD;
import static com.example.cypher00.KeysUtils.DIFFICULTY_MEDIUM;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private List<Match> matches;
    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvOpponent;
        TextView tvWinner;
        TextView tvMatchTime;
        TextView tvDifficulty;
        MyViewHolder(View view) {
            super(view);
            tvOpponent = view.findViewById(R.id.opponent_mh);
            tvWinner = view.findViewById(R.id.winner_mh);
            tvMatchTime = view.findViewById(R.id.matchTime_mh);
            tvDifficulty = view.findViewById(R.id.difficulty_mh);
        }
    }

    public MyAdapter(List<Match> objects) {
        matches = objects;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.match_history_list_item_real, parent, false);
        return new MyViewHolder(listItem);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT);
        String opponent = matches.get(position).getOpponent();
        holder.tvOpponent.setText(opponent == null ? "Opponent" : opponent);
        if (matches.get(position).getWinner().equals("WIN")) {
            holder.tvWinner.setTextColor(Color.GREEN);
            holder.tvWinner.setText("W");
        } else {
            holder.tvWinner.setTextColor(Color.RED);
            holder.tvWinner.setText("L");
        }
        holder.tvMatchTime.setText(sdf.format(matches.get(position).getTime()));
        holder.tvDifficulty.setText(getDifficultyFromDifficultyKey(matches.get(position).getDifficulty(), holder.tvDifficulty));
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    /**
     * @param difficulty int, the difficulty code
     * @return a string representing the difficulty
     */
    private String getDifficultyFromDifficultyKey(int difficulty, View view) {
        switch(difficulty) {
            case DIFFICULTY_EASY: return view.getContext().getString(R.string.easy);
            case DIFFICULTY_MEDIUM : return  view.getContext().getString(R.string.normal);
            case DIFFICULTY_HARD : return  view.getContext().getString(R.string.hard);
            default : return  view.getContext().getString(R.string.easy);
        }
    }


}