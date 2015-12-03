package com.direyorkie.idolscanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private final String INTENT_MSG = "123";
    private final String TAG = AppCompatActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void setupIdol(View v) {
        String buttonText = (String) ((Button) v).getText();
        String idolID = "";

        if(buttonText.equals(getString(R.string.idol_0))) {
            idolID = Constants.IDOL0;
        }
        else if (buttonText.equals(getString(R.string.idol_1))){
            idolID = Constants.IDOL1;
        }
        else {
            idolID = Constants.IDOL2;
        }

        Log.i(TAG, "Set up idol " + idolID);

        Intent scannerIntent = new Intent(this, ScannerActivity.class);
        scannerIntent.putExtra(INTENT_MSG, idolID);
        startActivity(scannerIntent);
    }

    public void setupReceiver(View v) {
        Log.i(TAG, "setup receiver");

        Intent receiverIntent = new Intent(this, ReceiverActivity.class);
        startActivity(receiverIntent);
    }

}
