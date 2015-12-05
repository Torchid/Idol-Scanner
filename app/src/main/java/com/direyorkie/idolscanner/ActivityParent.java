package com.direyorkie.idolscanner;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by rachel on 15-11-29.
 * This class contains all of the common elements needed in both the scanner and receiver
 * activities for making direct Wifi P2P connections.
 */
public class ActivityParent extends AppCompatActivity {

    protected final IntentFilter intentFilter = new IntentFilter();
    protected WiFiDirectBroadcastReceiver mReceiver;
    protected WifiP2pManager mManager;
    protected WifiP2pManager.Channel mChannel;

    protected final String TAG = ReceiverActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mReceiver == null) {
            mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
            registerReceiver(mReceiver, intentFilter);
        }

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //...
                Log.i(TAG, "Discovered peers.");
            }

            @Override
            public void onFailure(int reasonCode) {
                //...
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(mReceiver);
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    @Override
    public void onBackPressed() {
    }

    protected  void connect(WifiP2pDevice device) {}
    protected void prepareToConnect() {}

}
