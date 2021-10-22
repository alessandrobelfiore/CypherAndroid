/*
package com.example.cypher00;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class WifiConnect extends AppCompatActivity implements View.OnClickListener {
    // WIFI PROVA
    private WifiP2pManager manager;
    private WifiManager man;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;
    private WifiP2pDeviceList deviceList;
    private Button wifiOff;
    private Button readyBtn;
    TextView status;
    private Button refreshButton;
    private ListView listView;
    private boolean amIHost;
    private boolean amIReady;
    private boolean isHeReady;
    private InetAddress connectedTo;
    private Integer matchTime;

    // to set
    // byte[] gridBytes
    // to send to GameTab

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
/*


    static final int MESSAGE_READ = 0;
    static final byte READY_MESSAGE = 1;
    static final byte NOTIFY = 2;
    static final byte GIVE_UP = 3;
    static final byte RESULT = 4;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_connect);

        wifiOff = findViewById(R.id.wifi_off);
        status = findViewById(R.id.status);
        refreshButton = findViewById(R.id.discover_peers);
        listView = findViewById(R.id.list_peers);
        readyBtn = findViewById(R.id.ready_btn);
        wifiOff.setOnClickListener(this);
        refreshButton.setOnClickListener(this);
        readyBtn.setOnClickListener(this);

        // WIFI PROVA
        man = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new Receiver(manager, channel, this);
        // WIFI PROVA
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
            switch(msg.what) {
                case MESSAGE_READ:
                    byte[] readBuff = (byte[]) msg.obj;
                    switch(readBuff[0]) {
                        case READY_MESSAGE:
                            isHeReady = true;
                            break;
                        case NOTIFY:
                            String tempMsg = new String(readBuff, 1, msg.arg1 - 1);
                            matchTime = Integer.valueOf(tempMsg);
                            break;
                        case GIVE_UP:
                            break;
                        case RESULT:
                            tempMsg = new String(readBuff, 1, msg.arg1 - 1);
                            matchTime = Integer.valueOf(tempMsg);
                            break;
                        default:
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
                    deviceNames[i] = device.deviceName;
                    devices[i] = device;
                    i ++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNames);
                listView.setAdapter(adapter);
            }

            if (peers.size() == 0) {
                Toast.makeText(getApplicationContext(), "No devices ..", Toast.LENGTH_SHORT).show();
            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            final InetAddress groupOwnerAddress = info.groupOwnerAddress;

            if (info.groupFormed && info.isGroupOwner) {
                amIHost = true;
                status.setText("HOST");
                serverClass = new ServerClass();
                serverClass.start();
            }
            else if (info.groupFormed) {
                amIHost = false;
                status.setText("CLIENT");
                // TODO
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
            }
            else {
                man.setWifiEnabled(true);
                status.setText("ON");
            }
        } else if (v.getId() == R.id.discover_peers) {
            findPeers();
        } else if (v.getId() == R.id.ready_btn) {
            // TODO rendere attivo solo quando connectedTo è diverso da null ?
            if (connectedTo != null) {
                if (!amIReady) {
                    amIReady = true;
                    sendReceive.sendReady();
                }
            }
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
                        readyBtn.setClickable(true);
                        Toast.makeText(getApplicationContext(), "Connected to" + device.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        readyBtn.setClickable(false);
                        Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // wifi attivo
    public void findPeers() {
        if (!man.isWifiEnabled()) {
            Toast.makeText(getApplicationContext(), "Turn ON wifi to find opponents!", Toast.LENGTH_LONG).show();
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
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }
    // unregister the broadcast receiver
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public class ServerClass extends Thread {
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                // TODO
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
                socket.connect(new InetSocketAddress(hostAdd, 8888), 500);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class SendReceive extends Thread {
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

         public SendReceive(Socket sock) {
            socket = sock;
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (socket != null) {
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {

                        // TODO smistare messaggi
                        handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void sendReady() {
            try {
                outputStream.write("ready".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendGrid(PieceGrid grid) {
            int diff = grid.getDim();
            byte[] bytes = PieceGrid.fromGridToByteBuffer(grid);

            try {
                outputStream.write(diff);
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
*/