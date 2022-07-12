package com.example.cypher00;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

import static com.example.cypher00.KeysUtils.GET_MATCHES;
import static com.example.cypher00.KeysUtils.MESSAGE_CODE;

/**
 * Gets and shows the match history from the database
 */
public class MatchHistory extends AppCompatActivity {

//    private ListView listView;
    private List<Match> matches;
    private RecyclerView recyclerView;

//    public static final byte GET_MATCHES = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_history);

        Intent serviceIntent = new Intent(getApplicationContext(), MessengerService.class);
        serviceIntent.putExtra(MESSAGE_CODE, GET_MATCHES);
        Objects.requireNonNull(getApplicationContext()).startService(serviceIntent);
        recyclerView = findViewById(R.id.match_list_view);

        MessengerService.setGetMatchesListener(new MessengerService.onGetMatchesListener() {
            @Override
            public void onGetMatches(List<Match> list) {
                matches = list;
                Log.d("REC", String.valueOf(matches.size()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MyAdapter adapter = new MyAdapter(matches);
                        recyclerView.setHasFixedSize(true);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        recyclerView.setAdapter(adapter);
                    }
                });
            }
        });
        // debug

//        db = AppDatabase.getInstance(this);
//        db.matchDao().deleteAll();
//        matches = db.matchDao().getAll();
    }
}

