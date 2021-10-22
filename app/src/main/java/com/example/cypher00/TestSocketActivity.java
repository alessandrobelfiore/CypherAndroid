//package com.example.cypher00;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
//import android.widget.Toast;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.Inet4Address;
//import java.net.Inet6Address;
//import java.net.InetAddress;
//import java.net.NetworkInterface;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.util.ArrayList;
//import java.util.Enumeration;
//
//import static com.example.cypher00.ConnectionFragment.MESSAGE_CODE;
//import static com.example.cypher00.ConnectionFragment.MESSAGE_READ;
//import static com.example.cypher00.ConnectionFragment.NOTIFY;
//import static com.example.cypher00.ConnectionFragment.OK;
//import static com.example.cypher00.ConnectionFragment.PASS_GRID;
//import static com.example.cypher00.ConnectionFragment.READY;
//import static com.example.cypher00.ConnectionFragment.RESULT;
//import static com.example.cypher00.GameFragment.DIFFICULTY_KEY;
//import static com.example.cypher00.GameFragment.GRID_BYTES_KEY;
//import static com.example.cypher00.MainActivity.MULTI_PLAYER_HOST;
//import static com.example.cypher00.SelectModeTab.MODE_KEY;
//
//public class TestSocketActivity extends AppCompatActivity {
//
//    ServerSocket serverSocket;
//    Socket socket;
//    SendReceive sendReceive;
//
//    boolean amIReady;
//    boolean amIHost;
//    boolean isHeReady;
//
//
//    public TestSocketActivity() {
//        super();
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        try {
//            InetAddress address = InetAddress.getByName(getLocalIpAddress());
//            // serverSocket = new ServerSocket(6665, 0, address);
//            new Thread() {
//                @Override
//                public void run() {
////                    try {
////                        Log.d("TEST",serverSocket.toString());
////                        // socket = serverSocket.accept();
////                        // socket.getOutputStream().write(OK);
////                        Log.d("TEST", String.valueOf(socket.isConnected()));
////                        sendReceive = new SendReceive(socket);
////                        // sendReceive.start();
////                    } catch (IOException e) {
////                        e.printStackTrace();
////                    }
////                    MessengerService.setSocket(socket);
//                }
//            }.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//         amIReady = true;
//         amIHost = true;
//         isHeReady = false;
//    }
//    private String getLocalIpAddress() throws IOException {
//        String resultIpv6 = "";
//        String resultIpv4 = "";
//
//        for (Enumeration en = NetworkInterface.getNetworkInterfaces();
//             en.hasMoreElements();) {
//
//            NetworkInterface intf = (NetworkInterface) en.nextElement();
//            for (Enumeration enumIpAddr = intf.getInetAddresses();
//                 enumIpAddr.hasMoreElements();) {
//
//                InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
//                if(!inetAddress.isLoopbackAddress()){
//                    if (inetAddress instanceof Inet4Address) {
//                        resultIpv4 = inetAddress.getHostAddress();
//                    } else if (inetAddress instanceof Inet6Address) {
//                        resultIpv6 = inetAddress.getHostAddress();
//                    }
//                }
//            }
//        }
//        return ((resultIpv4.length() > 0) ? resultIpv4 : resultIpv6);
//    }
//    // Receiver
//    public class SendReceive extends Thread {
//        private Socket socket;
//        private InputStream inputStream;
//        private OutputStream outputStream;
//
//        public SendReceive(Socket sock) {
//            socket = sock;
//            try {
//                Log.d("TEST", "ENTRO IN SENDRECEVICE");
//                inputStream = socket.getInputStream();
//                outputStream = socket.getOutputStream();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        public void run() {
//            byte[] buffer = new byte[1024];
//            int bytes;
//            while (socket != null) {
//                try {
//                    bytes = inputStream.read(buffer);
//                    if (bytes > 0) {
//                        handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//
//        public void sendReady() {
//            // SERVICE
//            Intent serviceIntent = new Intent(getApplicationContext(), MessengerService.class);
//            serviceIntent.putExtra(MESSAGE_CODE, READY);
//            // TODO DEBUG
//            startService(serviceIntent);
//        }
//        public void sendOK() {
//            // SERVICE
//            Intent serviceIntent = new Intent(getApplicationContext(), MessengerService.class);
//            serviceIntent.putExtra(MESSAGE_CODE, OK);
//            // TODO DEBUG
//            startService(serviceIntent);
//        }
//
//        public void sendGrid(ArrayList<ArrayList<Piece>> pieces) {
//            byte[] bytes = PieceGrid.fromGridToByteBuffer(pieces);
//            // SERVICE
//            Intent serviceIntent = new Intent(getApplicationContext(), MessengerService.class);
//            serviceIntent.putExtra(MESSAGE_CODE, PASS_GRID);
//            serviceIntent.putExtra(DIFFICULTY_KEY, 3);
//            // TODO COVERS
//            serviceIntent.putExtra(GRID_BYTES_KEY, bytes);
//            // TODO DEBUG
//            startService(serviceIntent);
//        }
//    }
//
//    Handler handler = new Handler(new Handler.Callback() {
//        @Override
//        public boolean handleMessage(Message msg) {
//            // TODO
//            switch (msg.what) {
//                case MESSAGE_READ:
//                    byte[] readBuff = (byte[]) msg.obj;
//                    switch (readBuff[0]) {
//                        case READY:
//                            isHeReady = true;
//                            if (amIReady && amIHost) {
//                                // send grid
//                                ArrayList<ArrayList<Piece>> pieces = PieceGrid.generateGridS(3);
//                                sendReceive.sendGrid(pieces);
//                                Toast.makeText(getApplicationContext(), "PASSO A GAME",Toast.LENGTH_SHORT).show();
//                                switchToGameFragment(pieces);
//                            }
//                            break;
//                        case NOTIFY:
//                            String tempMsg = new String(readBuff, 1, msg.arg1 - 1);
//                            break;
//                        case RESULT:
//                            tempMsg = new String(readBuff, 1, msg.arg1 - 1);
//                            break;
//                        case PASS_GRID:
//                            //Toast.makeText(getContext(), "PASSO A GAME",Toast.LENGTH_SHORT).show();
//                            byte[] bytes = new byte[readBuff.length - 1];
//                            System.arraycopy(readBuff, 1, bytes, 0, readBuff.length - 1);
//                            switchToGameFragment(bytes);
//                        default:
//                            break;
//                    }
//                    break;
//            }
//            return false;
//        }
//    });
//
//    private void switchToGameFragment(byte[] bytes) {
//        GameFragment gf = new GameFragment();
//        Bundle extras = new Bundle();
//        if (extras != null) {
//            extras.putByteArray(GRID_BYTES_KEY, bytes);
//        }
//        extras.putInt(DIFFICULTY_KEY, 3);
//        extras.putInt(MODE_KEY, MULTI_PLAYER_HOST);
//        gf.setArguments(extras);
//        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, gf).commit();
//    }
//
//    private void switchToGameFragment(ArrayList<ArrayList<Piece>> pieces) {
//        GameFragment gf = new GameFragment();
//        Bundle extras = new Bundle();
//        if (extras != null) {
//            extras.putByteArray(GRID_BYTES_KEY, PieceGrid.fromGridToByteBuffer(pieces));
//        }
//        extras.putInt(DIFFICULTY_KEY, 3);
//        extras.putInt(MODE_KEY, MULTI_PLAYER_HOST);
//        gf.setArguments(extras);
//        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, gf).commit();
//    }
//
//
//}
