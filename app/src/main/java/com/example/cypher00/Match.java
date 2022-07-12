package com.example.cypher00;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Match {

    @PrimaryKey(autoGenerate = true) private int _id;
    private String opponent;
    private String winner;
    private int difficulty;
    private long time;

    @Ignore
    public Match(int id, String opponent, String winner, int difficulty, long time) {
        this._id = id;
        this.opponent = opponent;
        this.winner = winner;
        this.difficulty = difficulty;
        this.time = time;
    }

    public Match(String opponent, String winner, int difficulty, long time) {
        this(0, opponent, winner, difficulty, time);
    }

    public String getOpponent() {
        return opponent;
    }

    public void setOpponent(String opponent) {
        this.opponent = opponent;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getId() {
        return _id;
    }

    public void setId(int _id) {
        this._id = _id;
    }
}
