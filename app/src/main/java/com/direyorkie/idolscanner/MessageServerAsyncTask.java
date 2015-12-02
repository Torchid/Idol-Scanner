package com.direyorkie.idolscanner;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by rachel on 15-11-28.
 */
public class MessageServerAsyncTask extends AsyncTask <String, Void, String> {

    private Context context;
    private TextView statusText;

    public MessageServerAsyncTask(Context context, TextView statusText) {
        this.context = context;
        this.statusText = (TextView) statusText;
    }

    @Override
    protected String doInBackground(String[] params) {
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
            statusText.setText("Message received: " + result);
//            Intent intent = new Intent();
//            intent.setAction(android.content.Intent.ACTION_VIEW);
//            intent.setDataAndType(Uri.parse("file://" + result), "image/*");
//            context.startActivity(intent);
        }
    }


}