package com.example.cypher00;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.cypher00.ConnectionFragment.MESSAGE_CODE;
import static com.example.cypher00.ConnectionFragment.MESSAGE_READ;
import static com.example.cypher00.ConnectionFragment.OK;
import static com.example.cypher00.ConnectionFragment.READY;
import static com.example.cypher00.GameFragment.DIFFICULTY_KEY;
import static com.example.cypher00.GameFragment.GRID_BITS_KEY;
import static com.example.cypher00.GameFragment.GRID_BYTES_KEY;
import static java.security.AccessController.getContext;

public class ConnectionFragment extends Fragment implements View.OnClickListener {
    private WifiP2pManager manager;
    private WifiManager man;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;
//    private WifiP2pDeviceList deviceList;
    private Button wifiOff;
    private Button readyBtn;
    TextView status;
    private Button refreshButton;
    private ListView listView;
    private boolean amIHost;
    private boolean amIReady;
    private boolean isHeReady;
    private InetAddress connectedTo;
    private ConstraintLayout layout;

    private Button deleteGroups;
    // TODO SOCKET DEBUG
//    private EditText insertSocket;
//    private Button createServer;
//    private TextView serverAddress;

    // TODO grid
    private int difficulty;
    private ArrayList<ArrayList<Piece>> pieces;


    List<WifiP2pDevice> peers = new ArrayList<>();
    String[] deviceNames;
    WifiP2pDevice[] devices;

    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

/*STATES:
    PLAYING;
    NOTIFYING;
    CLOSED;
 */

    public static final String MESSAGE_CODE = "MESSAGE_CODE";

    static final int MESSAGE_READ = 0;
    static final byte READY = 1;
    static final byte NOTIFY = 2;
    static final byte RESULT = 4;
    static final byte OK = 5;
    static final byte PASS_GRID = 6;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_wifi_connect, null);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle extras = getArguments();
        difficulty = extras.getInt(DIFFICULTY_KEY, 3);

        wifiOff = view.findViewById(R.id.wifi_off);
        status = view.findViewById(R.id.status);
        refreshButton = view.findViewById(R.id.discover_peers);
        listView = view.findViewById(R.id.list_peers);
        readyBtn = view.findViewById(R.id.ready_btn);
        layout = view.findViewById(R.id.connection_layout);

        deleteGroups = view.findViewById(R.id.hard_connect);
        deleteGroups.setOnClickListener(this);
        // TODO
//        insertSocket = view.findViewById(R.id.insert_socket);
//        createServer = view.findViewById(R.id.create_server);
//        serverAddress = view.findViewById(R.id.server_addr);
//        createServer.setOnClickListener(this);

        wifiOff.setOnClickListener(this);
        refreshButton.setOnClickListener(this);
        readyBtn.setOnClickListener(this);

        readyBtn.setClickable(false);
//        readyBtn.setEnabled(false);

        man = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        manager = (WifiP2pManager) getActivity().getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(getContext(), getActivity().getMainLooper(), null);
        receiver = new Receiver(manager, channel, this);

        // TODO IMPLEMENT INTO SERVICE !
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        amIHost = false;
        amIReady = false;
        isHeReady = false;
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            // TODO
            switch (msg.what) {
                case MESSAGE_READ:
                    // byte[] readBuff = new byte[msg.arg1];
                    // System.arraycopy(msg.obj, msg.arg1, readBuff, 0, readBuff.length);
                    byte[] readBuff = (byte[]) msg.obj;
                    StringBuilder builder = new StringBuilder();
                    for (byte b : readBuff) {
                        builder.append('|');
                        builder.append(b);
                    }
                    builder.append('|');
                    Log.d("TEST", builder.toString());
                    switch (readBuff[0]) {
                        case READY:
                            isHeReady = true;
                            Toast.makeText(getContext(), "READY ARRIVED",Toast.LENGTH_SHORT).show();
                            if (amIReady && amIHost) {
                                pieces = PieceGrid.generateGridS(difficulty);
                                sendReceive.sendGrid(pieces);
                                switchToGameFragment(pieces);
                            }
                            break;
                        case PASS_GRID:
                            difficulty = (int) readBuff[1];
                            Bundle extras = getArguments();
                            extras.putInt(DIFFICULTY_KEY, difficulty);
                            byte[] bytes = new byte[readBuff.length - 2];
                            System.arraycopy(readBuff, 2, bytes, 0, readBuff.length - 2);
                            switchToGameFragment(bytes);
                        default:
                            Log.d("TEST", "WRONG RECEIVER");
                            break;
                    }
                    break;
            }
            return false;
        }
    });

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

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, deviceNames);
                listView.setAdapter(adapter);
            }

            if (peers.size() == 0) {
//                readyBtn.setEnabled(false);
//                readyBtn.setClickable(false);
                Toast.makeText(getActivity().getApplicationContext(), "No devices ..", Toast.LENGTH_SHORT).show();
            }
        }
    };

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

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            final InetAddress groupOwnerAddress = info.groupOwnerAddress;
            Toast.makeText(getContext(), "Someone connected to me", Toast.LENGTH_SHORT).show();

            if (info.groupFormed && info.isGroupOwner) {
                amIHost = true;
                status.setText("HOST");
                serverClass = new ServerClass();
                serverClass.start();
            } else if (info.groupFormed) {
                amIHost = false;
                status.setText("CLIENT");
                connectedTo = groupOwnerAddress;
                clientClass = new ClientClass(groupOwnerAddress);
                clientClass.start();
            }
        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.wifi_off) {
            if (man.isWifiEnabled()) {
                man.setWifiEnabled(false);
                status.setText("OFF");
            } else {
                man.setWifiEnabled(true);
                status.setText("ON");
            }
        } else if (v.getId() == R.id.discover_peers) {
            findPeers();
        } else if (v.getId() == R.id.ready_btn) {
            if (v.isClickable()) {
                Log.d("CLICKABLE", String.valueOf(v.isClickable()));
                //if (connectedTo != null) {
                    if (!amIReady) {
                        amIReady = true;
                    }
                sendReceive.sendReady();
                Toast.makeText(getContext(), "MANDO OK",Toast.LENGTH_SHORT).show();
                if (isHeReady && amIHost) {
                    pieces = PieceGrid.generateGridS(difficulty);
                        sendReceive.sendGrid(pieces);
                        switchToGameFragment(pieces);
                    }
                //}
            }
        } else if (v.getId() == R.id.hard_connect) {
                deletePersistentGroups();
        }


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final WifiP2pDevice device = devices[position];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getActivity().getApplicationContext(), "Connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
//                        readyBtn.setEnabled(false);
//                        readyBtn.setClickable(false);
                        Toast.makeText(getContext(), "Connection failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // wifi attivo
    public void findPeers() {
        if (!man.isWifiEnabled()) {
            Toast.makeText(getActivity().getApplicationContext(), "Turn ON wifi to find opponents!", Toast.LENGTH_LONG).show();
        }
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                status.setText("Looking for clients..");
            }

            @Override
            public void onFailure(int reasonCode) {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(receiver, intentFilter);
    }

    /* unregister the broadcast receiver */
    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

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

    public class ClientClass extends Thread {
        Socket socket;
        String hostAdd;

        public ClientClass(InetAddress hostAddress) {
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
    public class SendReceive extends Thread {
        private Socket socket;
        private InputStream inputStream;

        public SendReceive(Socket sock) {
            socket = sock;
            try {
                inputStream = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            readyBtn.setClickable(true);
//            readyBtn.setEnabled(true);
        }
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (socket != null) {
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        public void sendReady() {
            // SERVICE
            Intent serviceIntent = new Intent(getContext(), MessengerService.class);
            serviceIntent.putExtra(MESSAGE_CODE, READY);
            Objects.requireNonNull(getContext()).startService(serviceIntent);
        }
        public void sendGrid(ArrayList<ArrayList<Piece>> pieces) {
            byte[] bytes = PieceGrid.fromGridToByteBuffer(pieces);
            // SERVICE
            Intent serviceIntent = new Intent(getContext(), MessengerService.class);
            serviceIntent.putExtra(MESSAGE_CODE, PASS_GRID);
            serviceIntent.putExtra(DIFFICULTY_KEY, difficulty);
            // TODO COVERS ?
            serviceIntent.putExtra(GRID_BYTES_KEY, bytes);
            Objects.requireNonNull(getContext()).startService(serviceIntent);
        }
    }

    private void switchToGameFragment(ArrayList<ArrayList<Piece>> pieces) {
        GameFragment gf = new GameFragment();
        Bundle extras = getArguments();
        if (extras != null) {
            // TODO rename to ByteArray
            extras.putByteArray(GRID_BYTES_KEY, PieceGrid.fromGridToByteBuffer(pieces));
        }

        gf.setArguments(extras);
        handler.removeCallbacksAndMessages(null);
        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, gf).commit();
    }
    private void switchToGameFragment(byte[] bytes) {
        GameFragment gf = new GameFragment();
        Bundle extras = getArguments();
        if (extras != null) {
            extras.putByteArray(GRID_BYTES_KEY, bytes);
        }
        gf.setArguments(extras);
        handler.removeCallbacksAndMessages(null);
        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, gf).commit();
    }

    // Delete any persistent group, hidden function
    private void deletePersistentGroups(){
        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("deletePersistentGroup")) {
                    for (int netid = 0; netid < 32; netid++) {
                        methods[i].invoke(manager, channel, netid, null);
                    }
                }
            }
        } catch(Exception e) {
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

    // might be useful

//    DataOutputStream dos = new DataOutputStream(
//            new BufferedOutputStream(socket.getOutputStream()));
//    dos.writeLong(longValue);
//
//    DataInputStream dis = new DataInputStream(
//            new BufferedInputStream(socket.getInputStream()));
//    long longValue = dis.readLong();


}


