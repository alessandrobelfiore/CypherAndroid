package com.example.cypher00;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.preference.PreferenceManager;
import android.widget.Toast;

import static com.example.cypher00.KeysUtils.MY_DEVICE_NAME_KEY;
import static com.example.cypher00.KeysUtils.SYNCHRONY_KEY;

/**
 * BroadcastReceiver class used to catch Wifi-Direct related events
 */
public class Receiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private ConnectionFragment mActivity;

    public Receiver() {
        super();
    }

    public Receiver(WifiP2pManager manager, WifiP2pManager.Channel channel, ConnectionFragment act) {
        this.mActivity = act;
        this.mManager = manager;
        this.mChannel = channel;
    }

    /**
     * Responds to the intent received requesting peers after a WIFI_P2P_PEERS_CHANGED_ACTION
     *      and requesting connection info after a WIFI_P2P_CONNECTION_CHANGED_ACTION
     * @param context the context
     * @param intent the intent received
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
//                Toast.makeText(context, mActivity.getString(R.string.wifi_on), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, mActivity.getString(R.string.wifi_off), Toast.LENGTH_SHORT).show();
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            if (mManager != null) {
                mManager.requestPeers(mChannel, mActivity.peerListListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            if (mManager == null) return;
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                mManager.requestConnectionInfo(mChannel, mActivity.connectionInfoListener);
            } else {
                mActivity.status.setText(mActivity.getString(R.string.device_disconnected));
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            if (device != null) {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
                pref.edit().putString(MY_DEVICE_NAME_KEY, device.deviceName).apply();
            }
        }
    }
}
