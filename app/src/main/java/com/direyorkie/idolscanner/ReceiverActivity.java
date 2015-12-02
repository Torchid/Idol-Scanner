package com.direyorkie.idolscanner;

import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.widget.TextView;
import android.widget.Toast;

public class ReceiverActivity extends ActivityParent {


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

        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        writeMsg(ip);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
    }


    protected void prepareToConnect() {
        mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                CharSequence text = "Create group successful!";
                int duration = Toast.LENGTH_SHORT;

                Toast.makeText(getApplicationContext(), text, duration).show();
            }

            @Override
            public void onFailure(int reason) {
                //failure logic
            }
        });
    }

    public void connect(WifiP2pDevice device) {
        //obtain a peer from the WifiP2pDeviceList
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;

//        //success logic
        MessageServerAsyncTask msgServerAsyncTask = new MessageServerAsyncTask(this, receiveMsgText);
        String parameters[] = {"params"};
        msgServerAsyncTask.execute(parameters);

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {

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

