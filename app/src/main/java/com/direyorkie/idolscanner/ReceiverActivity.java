package com.direyorkie.idolscanner;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ReceiverActivity extends ActivityParent {


    TextView connMsgText, passMsgText;

    private final String COMBINATION = "3456";

    String lilithPass = "",
            asmodeusPass = "",
            mammonPass = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        connMsgText = (TextView) findViewById(R.id.connection_msg);
        passMsgText = (TextView) findViewById(R.id.password_msg);

        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        //writeMsg(ip);
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
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
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
        MessageServerAsyncTask msgServerAsyncTask = new MessageServerAsyncTask(this, connMsgText, passMsgText);
        msgServerAsyncTask.execute();

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

    public class MessageServerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        TextView connMsgText, passMsgText;

        public MessageServerAsyncTask(Context context, TextView connMsgText, TextView passMsgText) {
            this.context = context;
            this.connMsgText = connMsgText;
            this.passMsgText = passMsgText;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {

                /**
                 * Create a server socket and wait for client connections. This
                 * call blocks until a connection is accepted from a client
                 */
                ServerSocket serverSocket = new ServerSocket(8988);
                Socket client = serverSocket.accept();

                /**
                 * If this code is reached, a client has connected and transferred data
                 * Save the input stream from the client as a JPEG file
                 */
                final File f = new File(Environment.getExternalStorageDirectory() + "/"
                        + context.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis()
                        + ".jpg");

                InputStream inputstream = client.getInputStream();
                byte[] data = new byte[100];
                inputstream.read(data);

                String msgFromClient = new String(data, "UTF-8");
                Log.i("MESSAGE FROM CLIENT", msgFromClient);
                serverSocket.close();
                return msgFromClient;
            } catch (IOException e) {
                return null;
            }
        }

        /**
         * Start activity that can handle the JPEG image
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                //passMsgText.setText("Message received: " + result);
                setPassword(result);
               // context.getApplicationContext().
                setPassword(result);
            }
        }

        public void setPassword(String idolAndPass){
            String[] idolPass = idolAndPass.split(":");
            switch(idolPass[0]){
                case "lilith":
                    lilithPass = idolPass[1].trim();
                    break;
                case "asmodeus":
                    asmodeusPass = idolPass[1].trim();
                    break;
                case "mammon":
                    mammonPass = idolPass[1].trim();
            }
            //connMsgText.setText(lilithPass + " " + asmodeusPass + " " + mammonPass);
            String capitalizedHero = WordUtils.capitalize(idolPass[1]);
            CharSequence text = capitalizedHero + ": Aaaaaaaaaaaaaaaaaah!";
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            checkPasswords();
        }

        public void checkPasswords(){
            if(lilithPass.equals(getString(R.string.key0)) &&
                    asmodeusPass.equals((getString(R.string.key1))) &&
                    mammonPass.equals(getString(R.string.key2))) {
                passMsgText.setText(COMBINATION);
            }

        }

    }
}