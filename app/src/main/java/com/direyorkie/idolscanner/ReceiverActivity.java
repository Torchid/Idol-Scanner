package com.direyorkie.idolscanner;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ReceiverActivity extends ActivityParent {

    public TextView connMsgText, passMsgText;
    public final String COMBINATION = "RSTYX";

    public String lilithPass,
            asmodeusPass,
            mammonPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().hide();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        lilithPass = "";
        asmodeusPass = "";
        mammonPass = "";

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 10, 0);

        connMsgText = (TextView) findViewById(R.id.connection_msg);
        passMsgText = (TextView) findViewById(R.id.password_msg);

        passMsgText.setText("");

       // WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
       // String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

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
        MessageServerAsyncTask msgServerAsyncTask = new MessageServerAsyncTask(this, passMsgText);
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

    public class MessageServerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        private ReceiverActivity ra;
        TextView mPassMsgText;

        public MessageServerAsyncTask(Context context, TextView newPassMsgText) {
            this.context = context;
            this.mPassMsgText = newPassMsgText;
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
             //   setPassword(result);
               // context.getApplicationContext().
               // checkPasswords();
                setPassword(result);
            }
        }

        public void setPassword(String idolAndPass){
            String[] idolPass = idolAndPass.split(":");
            switch(idolPass[0]){
                case "lilith":
                    lilithPass = idolPass[1].trim();
//                    CharSequence text0 = "Set lilith to " + idolPass[1].trim();
//                    Toast.makeText(getApplicationContext(), text0, Toast.LENGTH_SHORT).show();
                    break;
                case "asmodeus":
                    asmodeusPass = idolPass[1].trim();
//                    CharSequence text1 = "Set asmodeus to " + idolPass[1].trim();
//                    Toast.makeText(getApplicationContext(), text1, Toast.LENGTH_SHORT).show();
                    break;
                case "mammon":
                    mammonPass = idolPass[1].trim();
//                    CharSequence text2 = "Set mammon to " + idolPass[1].trim();
//                    Toast.makeText(getApplicationContext(), text2, Toast.LENGTH_SHORT).show();
            }
            //connMsgText.setText(lilithPass + " " + asmodeusPass + " " + mammonPass);

            checkPasswords();

            String capitalizedHero = WordUtils.capitalize(idolPass[1]);
            CharSequence text = capitalizedHero + ": Aaaaaaaaaaaaaaaaaah!";
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            MediaPlayer mPlayer = MediaPlayer.create(context, R.raw.scream);
            mPlayer.start();
        }

        public void checkPasswords(){
            if(lilithPass.equals(getString(R.string.key0)) &&
                    asmodeusPass.equals(getString(R.string.key1)) &&
                    mammonPass.equals(getString(R.string.key2))) {
                    mPassMsgText.setText(COMBINATION);
            }

        }

    }

}