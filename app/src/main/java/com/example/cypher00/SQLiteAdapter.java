package com.example.cypher00;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

import static com.example.cypher00.GameFragment.MATCHES;

public class SQLiteAdapter {

    public static final String MYDATABASE_NAME = "MY_DATABASE";
    public static final String MYDATABASE_TABLE = "MATCHES";
    public static final int MYDATABASE_VERSION = 1;
    public static final String KEY_ID = "_id";
    public static final String KEY_CONTENT = "Content";

    //create table MY_DATABASE (ID integer primary key, Content text not null);
    private static final String SCRIPT_CREATE_DATABASE =
            "create table " + MATCHES + " ("
                    + KEY_ID + " integer primary key autoincrement, "
                    + "opponent" + " text not null " +
                    ");";

    private SQLiteHelper sqLiteHelper;
    private SQLiteDatabase sqLiteDatabase;

    private Context context;

    public SQLiteAdapter(Context c){
        context = c;
    }

    public SQLiteAdapter openToRead() throws android.database.SQLException {
        sqLiteHelper = new SQLiteHelper(context, MYDATABASE_NAME, null, MYDATABASE_VERSION);
        sqLiteDatabase = sqLiteHelper.getReadableDatabase();
        return this;
    }

    public SQLiteAdapter openToWrite() throws android.database.SQLException {
        sqLiteHelper = new SQLiteHelper(context, MYDATABASE_NAME, null, MYDATABASE_VERSION);
        sqLiteDatabase = sqLiteHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        sqLiteHelper.close();
    }

    public long insert(String opponent, long matchTime, int difficulty, boolean amIWinner){
        ContentValues cv = new ContentValues();
        cv.put("opponent", opponent);
        cv.put("difficulty", difficulty);
        cv.put("time", matchTime);
        if (amIWinner) cv.put("winner", "WIN");
        else cv.put("winner", "LOSE");
        return sqLiteDatabase.insert(MATCHES, null, cv);
    }

    public long insert(long matchTime, int difficulty) {

        ContentValues cv = new ContentValues();
        cv.put("opponent", "SINGLEPLAYER");
        cv.put("difficulty", difficulty);
        cv.put("time", matchTime);
        cv.put("winner", "WIN");
        return sqLiteDatabase.insert(MATCHES, null, cv);
    }

    public int deleteAll(){
        return sqLiteDatabase.delete(MATCHES, null, null);
    }

    public Cursor queueAll(){
//        String[] columns = new String[]{KEY_ID, KEY_CONTENT};
        Cursor cursor = sqLiteDatabase.query(MATCHES, null,
                null, null, null, null, null);

        return cursor;
    }

    public class SQLiteHelper extends SQLiteOpenHelper {

        public SQLiteHelper(Context context, String name,
                            CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO Auto-generated method stub
            db.execSQL(SCRIPT_CREATE_DATABASE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub

        }

    }

}