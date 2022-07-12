package com.example.cypher00;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Match.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance = null;

    public abstract MatchDao matchDao();

    public AppDatabase() {}

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = buildDatabase(context);
            return instance;
        }
        return instance;
    }

    private static AppDatabase buildDatabase(Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "cypher.db").allowMainThreadQueries().build();
    }

}
