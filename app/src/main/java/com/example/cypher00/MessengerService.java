package com.example.cypher00;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;


import static com.example.cypher00.KeysUtils.COVERS_KEY;
import static com.example.cypher00.KeysUtils.COVERS_NO;
import static com.example.cypher00.KeysUtils.DIFFICULTY_EASY;
import static com.example.cypher00.KeysUtils.DIFFICULTY_KEY;
import static com.example.cypher00.KeysUtils.GET_MATCHES;
import static com.example.cypher00.KeysUtils.GRID_BYTES_KEY;
import static com.example.cypher00.KeysUtils.INSERT_DB;
import static com.example.cypher00.KeysUtils.MESSAGE_CODE;
import static com.example.cypher00.KeysUtils.MY_DEVICE_NAME_KEY;
import static com.example.cypher00.KeysUtils.MY_MATCH_TIME;
import static com.example.cypher00.KeysUtils.NOTIFY;
import static com.example.cypher00.KeysUtils.OPPONENT_KEY;
import static com.example.cypher00.KeysUtils.PASS_GRID;
import static com.example.cypher00.KeysUtils.READY;
import static com.example.cypher00.KeysUtils.RESULT;
import static com.example.cypher00.KeysUtils.WINNER_KEY;
import static com.example.cypher00.KeysUtils.WINNER_MATCH_TIME;

public class MessengerService extends IntentService {

    public static Socket socket;
    private OutputStream outputStream;
    private AppDatabase db;
    private static onGetMatchesListener mListener = null;
    private static onSocketClosedListener ListenerSocket = null;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public MessengerService(String name) {
        super(name);
    }

    public MessengerService() {
        super("STRING");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        /*man = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(getApplicationContext(), getMainLooper(), null);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        receiver = new Receiver(manager, channel, getApplicationContext());
        registerReceiver(receiver, intentFilter);*/
    }

    /**
     * Obtain a message code from the intent and acts respectively:
     *      insert_db = insert the given match into the database
     *      get_matches = returns via callback all the matches in the database
     *      ready = sends ready code via socket
     *      notify = sends notify code plus the timer given via socket
     *      result = sends result code plus the timer given via socket
     *      pass_grid = sends pass_grid code plus the grid bytes via socket
     *
     * @param intent the intent used to start the service
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        byte code = intent != null ? intent.getByteExtra(MESSAGE_CODE, (byte) 0b0) : 0;
        if (code == INSERT_DB) {
            db = AppDatabase.getInstance(getApplicationContext());
            String opponent = intent.getStringExtra(OPPONENT_KEY);
            String winner = intent.getStringExtra(WINNER_KEY);
            int difficulty = intent.getIntExtra(DIFFICULTY_KEY, DIFFICULTY_EASY);
            long time = intent.getLongExtra(WINNER_MATCH_TIME, 0);
            Match newMatch = new Match(opponent, winner, difficulty, time);
            db.matchDao().insertAll(newMatch);
            return;
        } else if (code == GET_MATCHES) {
            db = AppDatabase.getInstance(this);
            List<Match> matches = db.matchDao().getAll();
            if (mListener != null) mListener.onGetMatches(matches);
        }
        if (socket != null) {
            try {
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                if (ListenerSocket != null) ListenerSocket.onSocketClosed();
                //TODO intent?
                return;
            }
        }
        switch (code) {
            case READY:
                try {
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String myDeviceName = pref.getString(MY_DEVICE_NAME_KEY, "");
                    byte[] b = myDeviceName.getBytes();
                    byte[] toSend = new byte[1 + Long.SIZE / Byte.SIZE + b.length];
                    toSend[0] = READY;
                    System.arraycopy(ByteUtils.longToBytes(b.length), 0, toSend, 1, Long.SIZE / Byte.SIZE);
                    System.arraycopy(b, 0, toSend, 1 + Long.SIZE / Byte.SIZE, b.length);
                    outputStream.write(toSend);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case NOTIFY:
                try {
                    long matchTime = intent.getLongExtra(MY_MATCH_TIME, 0);
//                    Log.d("TEST", "matchTime in notify: " + String.valueOf(matchTime));
                    byte[] b = ByteUtils.longToBytes(matchTime);
                    byte[] toSend = new byte[1 + b.length];
                    toSend[0] = NOTIFY;
                    System.arraycopy(b, 0, toSend, 1, b.length);
                    outputStream.write(toSend);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case RESULT:
                try {
                    long winnerTime = intent.getLongExtra(WINNER_MATCH_TIME, 0);
//                    Log.d("TEST", "matchTime in result: " + String.valueOf(winnerTime));
                    byte[] b = ByteUtils.longToBytes(winnerTime);
                    byte[] toSend = new byte[1 + b.length];
                    toSend[0] = RESULT;
                    System.arraycopy(b, 0, toSend, 1, b.length);
                    outputStream.write(toSend);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case PASS_GRID:
                try {
                    byte[] bytes = intent.getByteArrayExtra(GRID_BYTES_KEY);
                    int difficulty = intent.getIntExtra(DIFFICULTY_KEY, DIFFICULTY_EASY);
                    int covers = intent.getIntExtra(COVERS_KEY, COVERS_NO);
                    byte[] toSend = new byte[3 + bytes.length];
                    toSend[0] = PASS_GRID;
                    toSend[1] = (byte) difficulty;
                    toSend[2] = (byte) covers;
                    System.arraycopy(bytes, 0, toSend, 3, bytes.length);
                    outputStream.write(toSend);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                Log.d("TEST", "MESSAGE CORRUPTED");
                break;
        }
    }

    /**
     * Static function used to set the current Socket to use
     * @param mSocket the socket in use
     */
    public static void setSocket(Socket mSocket) {
//        Log.d("TEST", "SOCKET SET");
        socket = mSocket;
    }

    /**
     * Gets the socket in use
     * @return the socket in use
     */
    public static Socket getSocket() {
        return socket;
    }

    public interface onGetMatchesListener {
        void onGetMatches(List<Match> matches);
    }

    public static void setGetMatchesListener(onGetMatchesListener listener) {
        mListener = listener;
    }

    public interface onSocketClosedListener {
        void onSocketClosed();
    }

    public static void setOnSocketClosedListener(onSocketClosedListener listener) {
        ListenerSocket = listener;
    }
}
