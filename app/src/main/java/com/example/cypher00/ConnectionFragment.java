package com.example.cypher00;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.example.cypher00.KeysUtils.COVERS_KEY;
import static com.example.cypher00.KeysUtils.COVERS_NO;
import static com.example.cypher00.KeysUtils.DIFFICULTY_EASY;
import static com.example.cypher00.KeysUtils.DIFFICULTY_KEY;
import static com.example.cypher00.KeysUtils.GRID_BYTES_KEY;
import static com.example.cypher00.KeysUtils.HOST_KEY;
import static com.example.cypher00.KeysUtils.MESSAGE_CODE;
import static com.example.cypher00.KeysUtils.MESSAGE_READ;
import static com.example.cypher00.KeysUtils.MY_DEVICE_NAME_KEY;
import static com.example.cypher00.KeysUtils.OPPONENT_KEY;
import static com.example.cypher00.KeysUtils.PASS_GRID;
import static com.example.cypher00.KeysUtils.PLAY_AGAIN_KEY;
import static com.example.cypher00.KeysUtils.READY;

public class ConnectionFragment extends Fragment implements View.OnClickListener {
    // Wi-fi
    private WifiP2pManager manager;
    private WifiManager man;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;
    private List<WifiP2pDevice> peers = new ArrayList<>();
    private String[] deviceNames;
    private WifiP2pDevice[] devices;

    // Views
    private Button readyBtn;
    private Button refreshButton;
    public TextView status;
    private ListView listView;
    private ConstraintLayout layout;

    // flags
    private boolean amIHost;
    private boolean amIReady;
    private boolean isHeReady;
    private int difficulty;
    private int dimension;
    private int covers;

    private String opponentName;
    private int playAgainSet;
    private InetAddress connectedTo;
    private ArrayList<ArrayList<Piece>> pieces;

    private ServerClass serverClass;
    private ClientClass clientClass;
    private SendReceive sendReceive;

/*STATES:
    PLAYING;
    NOTIFYING;
    CLOSED;
 */

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_wifi_connect, null);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle extras = getArguments();

        if (extras.getInt(PLAY_AGAIN_KEY, 0) == 1) {
            playAgainSet = 1;
            setupPlayAgain(view);
        } else {
            difficulty = extras.getInt(DIFFICULTY_KEY, DIFFICULTY_EASY);
            covers = extras.getInt(COVERS_KEY, COVERS_NO);
            dimension = GameFragment.getDimensionFromDifficulty(difficulty);

            status = view.findViewById(R.id.status);
            refreshButton = view.findViewById(R.id.discover_peers);
            listView = view.findViewById(R.id.list_peers);
            readyBtn = view.findViewById(R.id.ready_btn);
            layout = view.findViewById(R.id.connection_layout);

            refreshButton.setOnClickListener(this);
            readyBtn.setOnClickListener(this);

            readyBtn.setEnabled(false);

            man = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            manager = (WifiP2pManager) getActivity().getSystemService(Context.WIFI_P2P_SERVICE);
            channel = manager.initialize(getContext(), getActivity().getMainLooper(), null);
            receiver = new Receiver(manager, channel, this);

            intentFilter = new IntentFilter();
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

            amIHost = false;
            amIReady = false;
            isHeReady = false;
            deletePersistentGroups();
        }
    }

    /**
     * Setups a simplified version of this fragment, used to play with the same settings,
     *      versus the same opponent
     * @param view parent view
     */
    private void setupPlayAgain(View view) {
        Bundle extras = getArguments();
        difficulty = extras.getInt(DIFFICULTY_KEY, DIFFICULTY_EASY);
        covers = extras.getInt(COVERS_KEY, COVERS_NO);
        dimension = GameFragment.getDimensionFromDifficulty(difficulty);

        status = view.findViewById(R.id.status);
        listView = view.findViewById(R.id.list_peers);
        refreshButton = view.findViewById(R.id.discover_peers);
        readyBtn = view.findViewById(R.id.ready_btn);

        listView.setVisibility(View.GONE);
        refreshButton.setOnClickListener(this);
        readyBtn.setOnClickListener(this);

        readyBtn.setEnabled(true);
        refreshButton.setEnabled(false);

        status.setText(getContext().getString(R.string.rematch));
        amIHost = extras.getBoolean(HOST_KEY);
        sendReceive = new SendReceive(MessengerService.getSocket());
        sendReceive.start();
        amIReady = false;
        isHeReady = false;

    }

    /**
     *  Respectively to the message received:
     *      ready: sets isHeReady, if I'm the host and I'm already ready, generate a grid, sends it
     *          to the opponent and switches to GameFragment with that grid
     *      pass_grid: switches to GameFragment with the given grid
     */
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    byte[] readBuff = (byte[]) msg.obj;
                    StringBuilder builder = new StringBuilder();
                    for (byte b : readBuff) {
                        builder.append('|');
                        builder.append(b);
                    }
                    builder.append('|');
                    Log.d("TEST", "IN CONNECTION: " + builder.toString());
                    switch (readBuff[0]) {
                        case READY:
                            isHeReady = true;
                            Toast.makeText(getContext(), getContext().getString(R.string.oppo_ready), Toast.LENGTH_SHORT).show();
                            if (opponentName == null) {
                                long length = ByteUtils.bytesToLong(Arrays.copyOfRange(readBuff, 1, Long.SIZE / Byte.SIZE + 1));
                                opponentName = new String(Arrays.copyOfRange(readBuff, 1 + Long.SIZE / Byte.SIZE, (int) (1 + Long.SIZE / Byte.SIZE + length))).trim();
                            }
                            if (amIReady && amIHost) {
                                pieces = PieceGrid.generateGridS(dimension);
                                sendReceive.sendGrid(pieces);
                                switchToGameFragment(pieces);
                            }
                            break;
                        case PASS_GRID:
                            difficulty = (int) readBuff[1];
                            covers = (int) readBuff[2];
                            Bundle extras = getArguments();
                            extras.putInt(DIFFICULTY_KEY, difficulty);
                            extras.putInt(COVERS_KEY, covers);
                            byte[] bytes = new byte[readBuff.length - 3];
                            System.arraycopy(readBuff, 3, bytes, 0, readBuff.length - 3);
                            switchToGameFragment(bytes);
                            break;
                        default:
                            Log.d("TEST", "WRONG RECEIVER");
                            break;
                    }
                    break;
            }
            return false;
        }
    });

    /**
     * Wifi Direct method to obtain available peers
     */
    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peersList) {
            if (!peersList.getDeviceList().equals(peers)) {
                peers.clear();
                peers.addAll(peersList.getDeviceList());

                deviceNames = new String[peersList.getDeviceList().size()];
                devices = new WifiP2pDevice[peersList.getDeviceList().size()];
                int i = 0;

                for (WifiP2pDevice device : peersList.getDeviceList()) {
                    deviceNames[i] = device.deviceName + " - " + getDeviceStatus(device.status);
                    devices[i] = device;
                    i++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.peer_item, deviceNames);
                listView.setAdapter(adapter);
            }

            if (peers.size() == 0) {
                readyBtn.post((new Runnable()
                {
                    @Override
                    public void run() {
                        setReadyClickable(false);
                    }
                }));
//                readyBtn.setClickable(false);
                Toast.makeText(getActivity().getApplicationContext(), getContext().getString(R.string.no_devices), Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * @param deviceStatus int code that identifies a status
     * @return a string explaining the status of the device
     */
    private static String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }

    /**
     * Creates the server or the client thread when a connection is established
     */
    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            final InetAddress groupOwnerAddress = info.groupOwnerAddress;
            Toast.makeText(getContext(), getContext().getString(R.string.connection_established), Toast.LENGTH_SHORT).show();

            if (info.groupFormed && info.isGroupOwner) {
                amIHost = true;
                Bundle extras = getArguments();
                extras.putBoolean(HOST_KEY, true);
                status.setText(getContext().getString(R.string.host));
                serverClass = new ServerClass();
                serverClass.start();
            } else if (info.groupFormed) {
                amIHost = false;
                Bundle extras = getArguments();
                extras.putBoolean(HOST_KEY, false);
                status.setText(getString(R.string.client));
                connectedTo = groupOwnerAddress;
                clientClass = new ClientClass(groupOwnerAddress);
                clientClass.start();
            }
        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.discover_peers) {
            findPeers();
        } else if (v.getId() == R.id.ready_btn) {
            if (v.isClickable()) {
                Log.d("CLICKABLE", String.valueOf(v.isClickable()));
                //if (connectedTo != null) {
                if (!amIReady) {
                    amIReady = true;
                }
                sendReceive.sendReady();
                // DEBUG
//                Toast.makeText(getContext(), "MANDO OK", Toast.LENGTH_SHORT).show();
                if (isHeReady && amIHost) {
                    pieces = PieceGrid.generateGridS(dimension);
                    sendReceive.sendGrid(pieces);
                    switchToGameFragment(pieces);
                }
                //}
            }

        }

        if (playAgainSet != 1)
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final WifiP2pDevice device = devices[position];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
//                        Toast.makeText(getActivity().getApplicationContext(), getContext().getString(R.string.connected_to) + device.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        readyBtn.post((new Runnable() {
                            @Override
                            public void run() {
                                setReadyClickable(false);
                            }
                        }));
                        Toast.makeText(getContext(), getContext().getString(R.string.connection_failed), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * Calls the discoverPeers WifiDirect method
     */
    private void findPeers() {
        if (!man.isWifiEnabled()) {
            Toast.makeText(getActivity().getApplicationContext(), getContext().getString(R.string.turn_wifi_on), Toast.LENGTH_LONG).show();
        }
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                status.setText(getContext().getString(R.string.looking_for_clients));
            }

            @Override
            public void onFailure(int reasonCode) {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (playAgainSet != 1)
        getActivity().registerReceiver(receiver, intentFilter);
    }

    /* unregister the broadcast receiver */
    @Override
    public void onPause() {
        super.onPause();
        if (playAgainSet != 1)
        getActivity().unregisterReceiver(receiver);
    }

    /**
     * Thread class used to initialize the server and wait for the client connection
     */
    public class ServerClass extends Thread {
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(6666);
                socket = serverSocket.accept();
                MessengerService.setSocket(socket);
                connectedTo = socket.getInetAddress();
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Thread class used to initialize the client and connect to the server
     */
    public class ClientClass extends Thread {
        Socket socket;
        String hostAdd;

        ClientClass(InetAddress hostAddress) {
            hostAdd = hostAddress.getHostAddress();
            socket = new Socket();
        }

        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAdd, 6666), 500);
                MessengerService.setSocket(socket);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Receiver

    /**
     * Thread class used to delegate receiving messages to the handler
     */
    public class SendReceive extends Thread {
        private Socket socket;
        private InputStream inputStream;

        SendReceive(Socket sock) {
            socket = sock;
            try {
                socket.setSoTimeout(10);
                inputStream = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            readyBtn.setClickable(true);
            readyBtn.post((new Runnable()
            {
                @Override
                public void run() {
                    setReadyClickable(true);
                }
            }));
        }

        public void run() {
            while (!this.isInterrupted()) {
                byte[] buffer = new byte[1024];
                int bytes;
                while (socket != null && !this.isInterrupted()) {
                    try {
                        bytes = inputStream.read(buffer);
                        if (bytes > 0) {
                            handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                        }
                    } catch (SocketTimeoutException e) {
                        Log.d("HALP", "timeout");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                socket.setSoTimeout(0);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        void sendReady() {
            Intent serviceIntent = new Intent(getContext(), MessengerService.class);
            serviceIntent.putExtra(MESSAGE_CODE, READY);
            Objects.requireNonNull(getContext()).startService(serviceIntent);
        }

        void sendGrid(ArrayList<ArrayList<Piece>> pieces) {
            byte[] bytes = PieceGrid.fromGridToByteArray(pieces);
            Intent serviceIntent = new Intent(getContext(), MessengerService.class);
            serviceIntent.putExtra(MESSAGE_CODE, PASS_GRID);
            serviceIntent.putExtra(DIFFICULTY_KEY, difficulty);
            serviceIntent.putExtra(COVERS_KEY, covers);
            serviceIntent.putExtra(GRID_BYTES_KEY, bytes);
            Objects.requireNonNull(getContext()).startService(serviceIntent);
        }

        void stopThread() {
            this.interrupt();
        }
    }

    private void setReadyClickable(boolean value) {
        readyBtn.setEnabled(value);
    }

    /**
     * Switches the current fragment with GameFragment passing the given grid
     * @param pieces ArrayList<ArrayList<Piece>> passed to GameFragment to initialize the correct grid
     */
    private void switchToGameFragment(ArrayList<ArrayList<Piece>> pieces) {
        GameFragment gf = new GameFragment();
        Bundle extras = getArguments();
        if (extras != null) {
            extras.putByteArray(GRID_BYTES_KEY, PieceGrid.fromGridToByteArray(pieces));
            extras.putString(OPPONENT_KEY, opponentName);
        }
        gf.setArguments(extras);
        handler.removeCallbacksAndMessages(null);
        sendReceive.stopThread();
        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, gf).commit();
    }
    /**
     * Switches the current fragment with GameFragment passing the given grid
     * @param bytes byte[] passed to GameFragment to initialize the correct grid
     */
    private void switchToGameFragment(byte[] bytes) {
        GameFragment gf = new GameFragment();
        Bundle extras = getArguments();
        if (extras != null) {
            extras.putByteArray(GRID_BYTES_KEY, bytes);
            extras.putString(OPPONENT_KEY, opponentName);
        }
        gf.setArguments(extras);
        handler.removeCallbacksAndMessages(null);
        sendReceive.stopThread();
        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, gf).commit();
    }

    // Delete any persistent group, hidden function

    /**
     * Utility function used to clear past history of WifiDirect groups
     */
    private void deletePersistentGroups() {
        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("deletePersistentGroup")) {
                    for (int netid = 0; netid < 32; netid++) {
                        methods[i].invoke(manager, channel, netid, null);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("main", "removeGroup onSuccess");
            }

            @Override
            public void onFailure(int reason) {
                Log.d("main", "removeGroup onFailure -" + reason);
            }
        });
    }
}


