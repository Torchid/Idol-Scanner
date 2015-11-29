package com.direyorkie.idolscanner;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

public class ReceiverActivity extends AppCompatActivity {

    private final IntentFilter intentFilter = new IntentFilter();
    private WiFiDirectBroadcastReceiver mReceiver;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    private final String TAG = AppCompatActivity.class.getSimpleName();

    TextView connMsgText, receiveMsgText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        connMsgText = (TextView) findViewById(R.id.connection_msg);
        receiveMsgText = (TextView) findViewById(R.id.receiver_msg);

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
    public void onResume() {
        super.onResume();
        if(mReceiver == null) {
            mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
            registerReceiver(mReceiver, intentFilter);
        }
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //...
            }

            @Override
            public void onFailure(int reasonCode) {
                //...
            }
        });




    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(mReceiver);
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    public void connect() {
        //obtain a peer from the WifiP2pDeviceList
        WifiP2pDevice device;
        WifiP2pConfig config = new WifiP2pConfig();
        //config.deviceAddress = device.deviceAddress;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                //success logic
            }

            @Override
            public void onFailure(int reason) {
                //failure logic
            }
        });
    }

    public void writeMsg(String msg) {
        connMsgText.setText(msg);
    }

}

