//package com.example.cypher00;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.graphics.Color;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.TextView;
//
//import java.text.SimpleDateFormat;
//import java.util.List;
//import java.util.Objects;
//
//import static com.example.cypher00.Chronometer.FORMAT;
//import static com.example.cypher00.KeysUtils.DIFFICULTY_EASY;
//import static com.example.cypher00.KeysUtils.DIFFICULTY_HARD;
//import static com.example.cypher00.KeysUtils.DIFFICULTY_MEDIUM;
//
//
//public class MyAdapter extends ArrayAdapter {
//
//    @SuppressWarnings("unchecked")
//    MyAdapter(Context context, int textViewResourceId, List<Match> objects) {
//        super(context, textViewResourceId, objects);
//    }
//
//    /**
//     *  Fills the given view with the match information
//     *
//     * @param position the match index
//     * @param view the view to fill in
//     * @param parent the listView parent
//     * @return view filled
//     */
//    @SuppressWarnings("NullableProblems")
//    @Override
//    public View getView(int position, View view, ViewGroup parent) {
//        LayoutInflater inflater = (LayoutInflater) getContext()
//                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//        view = inflater.inflate(R.layout.match_history_list_item, null);
//        TextView tvOpponent = view.findViewById(R.id.opponent_mh);
//        TextView tvWinner = view.findViewById(R.id.winner_mh);
//        TextView tvMatchTime = view.findViewById(R.id.matchTime_mh);
//        TextView tvDifficulty = view.findViewById(R.id.difficulty_mh);
//        Match m = (Match) getItem(position);
//
//        @SuppressLint("SimpleDateFormat")
//        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT);
//        tvOpponent.setText(Objects.requireNonNull(m).getOpponent());
//        if (m.getWinner().equals("WIN")) {
//            tvWinner.setTextColor(Color.GREEN);
//        } else tvWinner.setTextColor(Color.RED);
//        tvWinner.setText(m.getWinner());
//        tvMatchTime.setText(sdf.format(m.getTime()));
//        tvDifficulty.setText(getDifficultyFromDifficultyKey(m.getDifficulty()));
//        return view;
//    }
//
//    /**
//     * @param difficulty int, the difficulty code
//     * @return a string representing the difficulty
//     */
//    private String getDifficultyFromDifficultyKey(int difficulty) {
//        switch(difficulty) {
//            case DIFFICULTY_EASY: return getContext().getString(R.string.easy);
//            case DIFFICULTY_MEDIUM : return  getContext().getString(R.string.normal);
//            case DIFFICULTY_HARD : return  getContext().getString(R.string.hard);
//            default : return  getContext().getString(R.string.easy);
//        }
//    }
//
//
//}