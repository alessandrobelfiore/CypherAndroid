package com.example.cypher00;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import static com.example.cypher00.ConnectionFragment.MESSAGE_CODE;
import static com.example.cypher00.ConnectionFragment.NOTIFY;
import static com.example.cypher00.ConnectionFragment.PASS_GRID;
import static com.example.cypher00.ConnectionFragment.READY;
import static com.example.cypher00.ConnectionFragment.RESULT;
import static com.example.cypher00.GameFragment.DIFFICULTY_KEY;
import static com.example.cypher00.GameFragment.GRID_BYTES_KEY;
import static com.example.cypher00.GameFragment.MY_MATCH_TIME;
import static com.example.cypher00.GameFragment.WINNER_MATCH_TIME;

public class MessengerService extends IntentService {

    public static Socket socket;
    private OutputStream outputStream;


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

    @Override
    protected void onHandleIntent(Intent intent) {
        byte code = intent.getByteExtra(MESSAGE_CODE, (byte) 0b0);
        if (socket != null) {
            try {
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }}
        switch(code) {
//            case OK:
//                try {
//                    outputStream.write(OK);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                break;
            case READY:
                try {
                    outputStream.write(READY);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case NOTIFY:
                try {
                    long matchTime = intent.getLongExtra(MY_MATCH_TIME, 0);
                    Log.d("TEST", "matchTime in notify: "+ String.valueOf(matchTime));
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
                    Log.d("TEST", "matchTime in result: "+ String.valueOf(winnerTime));
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
                    int difficulty = intent.getIntExtra(DIFFICULTY_KEY, 3);
                    byte[] toSend = new byte[2 + bytes.length];
                    toSend[0] = PASS_GRID;
                    toSend[1] = (byte) difficulty;
                    System.arraycopy(bytes, 0, toSend, 2, bytes.length);
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

    public static void setSocket(Socket mSocket) {
        Log.d("TEST", "SOCKET SET");
        socket = mSocket;
    }

    // TODO JUST FOR DEBUG
    public static Socket getSocket() {
        return socket;
    }
}
