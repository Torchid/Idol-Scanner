package com.direyorkie.idolscanner;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ScannerActivity extends ActivityParent {

    private final String INTENT_MSG = "123";
    private final String TAG = AppCompatActivity.class.getSimpleName();
    private String idolID;

    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private TextView scanMsgText,
                     tagMsgText,
                     tagDataText,
                     sendMsgText;
    private int mCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Intent intent = getIntent();
        idolID = intent.getStringExtra(INTENT_MSG);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupContainerViews();

        scanMsgText.setText(R.string.scanning);

        setupNFCIntentFiltering();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null) mAdapter.disableForegroundDispatch(this);
    }

    private void setupContainerViews() {
        //Grab all container views from the layout
        scanMsgText = (TextView) findViewById(R.id.scan_msg);
        tagMsgText = (TextView) findViewById(R.id.tag_msg);
        tagDataText = (TextView) findViewById(R.id.tag_data);
        sendMsgText = (TextView) findViewById(R.id.send_msg);
    }

    private void setupNFCIntentFiltering() {
        mAdapter = NfcAdapter.getDefaultAdapter(this);

        // Create a generic PendingIntent that will be deliver to this activity. The NFC stack
        // will fill in the intent with the details of the discovered tag before delivering to
        // this activity.
        mPendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        //Tag tagFromIntent = pendingIntent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        //Setup an intent filter for all MIME based dispatches
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");    // Handles all MIME based dispatches.
        }
        catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }

        mFilters = new IntentFilter[] {ndef, };
        mTechLists = new String[][] { new String[] { NfcF.class.getName() } };
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);
        tagMsgText.setText("Discovered tag " + ++mCount + " with intent: " + intent);

        NdefMessage[] msgs = new NdefMessage[0];

        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMsgs != null) {
            msgs = new NdefMessage[rawMsgs.length];
            for (int i = 0; i < rawMsgs.length; i++) {
                msgs[i] = (NdefMessage) rawMsgs[i];
            }
        }

        //String tagMsg = readTag((Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG));
        if(msgs.length > 0) {
            NdefRecord[] records = msgs[0].getRecords();
            String msg = "";
            //tagDataText.setText(msgs[0].toString());
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        msg = readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }

            if(msg != "")
            {
                tagDataText.setText("KeyWord: " + msg);
                (new SendMsgAsync(this, idolID + ":" + msg)).execute();
            }

        }
    }

    private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

        byte[] payload = record.getPayload();

        // Get the Text Encoding
        //StandardCharsets textEncoding = (((payload[0] & 128) == 0) ? StandardCharSets.UTF-8 : StandardCharsets.);

        // Get the Language Code
        int languageCodeLength = payload[0] & 0063;

        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
        // e.g. "en"

        // Get the Text
        return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, StandardCharsets.UTF_8);
    }

    public class SendMsgAsync extends AsyncTask <Void, Void, Void> {

        private Context context;
        private String msgText;

        public SendMsgAsync(Context context, String msgText) {
            this.context = context;
            this.msgText = msgText;
        }

        protected Void doInBackground(Void... params) {
            String host = "172.16.1.77";
            int port = 8988;
            int len;
            Socket socket = new Socket();
            byte buf[]  = new byte[1024];
            //...
            try {
                /**
                 * Create a client socket with the host,
                 * port, and timeout information.
                 */
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), 500);

                /**
                 * Create a byte stream from a JPEG file and pipe it to the output stream
                 * of the socket. This data will be retrieved by the server device.
                 */
                OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream = new ByteArrayInputStream(msgText.getBytes(StandardCharsets.UTF_8));

                while ((len = inputStream.read(buf)) != -1) {
                    outputStream.write(buf, 0, len);
                }
                outputStream.close();
                inputStream.close();
            } catch (FileNotFoundException e) {
                //catch logic
            } catch (IOException e) {
                //catch logic
            }

            /**
             * Clean up any open sockets when done
             * transferring or if an exception occurred.
             */
            finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            //catch logic
                        }
                    }
                }
            }

            return null;
        }
    }

}
