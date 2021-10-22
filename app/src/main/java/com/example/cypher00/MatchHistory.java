package com.example.cypher00;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.io.File;

import static com.example.cypher00.GameFragment.MATCHES;

public class MatchHistory extends AppCompatActivity {

    private ListView listView;
    private String[] oppoNames;
    private SQLiteDatabase mDatabase;
    private SQLiteAdapter mySQLiteAdapter;
//    private String DB_PATH =  getApplicationInfo().dataDir+"/databases/";
//    String path = this.getApplicationContext().getFilesDir().getAbsolutePath().replace("files", "databases") + File.separator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_history);

//        oppoNames = new String[10];
//        for (int i = 0; i < 10; i++) {
//            oppoNames[i] = "BEPPE";
//        }

//        String[] columns = new String[]{"opponent", "difficulty", "time", "winner"};
//        mDatabase =
//                SQLiteDatabase.openOrCreateDatabase( "mDatabase.db",null);
//        Cursor cur = mDatabase.query(MATCHES,
//                                    null,
//                                    null,
//                                    null,
//                                    null, null, null);



        /*
         *  Open the same SQLite database
         *  and read all it's content.
         */
        mySQLiteAdapter = new SQLiteAdapter(this);
        mySQLiteAdapter.openToRead();

        Cursor cursor = mySQLiteAdapter.queueAll();

//        String[] from = new String[]{SQLiteAdapter.KEY_CONTENT};
//        int[] to = new int[]{R.id.text};

//        SimpleCursorAdapter cursorAdapter =
//                new SimpleCursorAdapter(this, R.layout.row, cursor, from, to);

        MyCursorAdapter adp = new MyCursorAdapter(this, cursor);
        listView = findViewById(R.id.match_list_view);
        listView.setAdapter(adp);
        mySQLiteAdapter.close();

//        MyCursorAdapter adp = new MyCursorAdapter(this, cur);
//        listView.setAdapter(adp);

        cursor.close();
    }
}

