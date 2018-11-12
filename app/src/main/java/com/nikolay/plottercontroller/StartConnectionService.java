package com.nikolay.plottercontroller;

import android.app.IntentService;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class StartConnectionService extends IntentService {

    private static final String TAG = "Lisko";
    public static final String ACTION_HC05_CONNECTED = "com.nikolay.plottercontroller.action.CONNECTED";
    public static final String ACTION_HC05_DISCONNECTED = "com.nikolay.plottercontroller.action.DISCONNECTED";
    private static BluetoothSocket mBluetoothSocket;

    public StartConnectionService() {
        super("StartConnectionService");
    }

    public static void startBluetoothConnection(Context context, BluetoothSocket bluetoothSocket) {
        mBluetoothSocket = bluetoothSocket;
        Intent intent = new Intent(context, StartConnectionService.class);
        context.startService(intent);
    }

    public static boolean isConnected() {
        if(mBluetoothSocket == null) {
            return false;
        } else {
            return mBluetoothSocket.isConnected();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            connectToHc05();
        }
        // Do nothing while connection is active
        while(mBluetoothSocket.isConnected());

        //When connection breaks, send broadcast and turn off
        Intent broadcast = new Intent(ACTION_HC05_DISCONNECTED);
        sendBroadcast(broadcast);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mBluetoothSocket != null) {
            try {
                mBluetoothSocket.close();
            } catch (IOException e) {
                Log.d(TAG, "Cannot close connection");
                e.printStackTrace();
            }
        }
        Intent broadcast = new Intent(ACTION_HC05_DISCONNECTED);
        sendBroadcast(broadcast);
        Log.d(TAG, "StartConnectionService destroyed");
    }

    private void connectToHc05() {
        try {
            if(mBluetoothSocket != null) {
                mBluetoothSocket.connect();
                Log.d(TAG, "Connection successful");
                // Send broadcast that the connection is established
                Intent broadcast = new Intent(ACTION_HC05_CONNECTED);
                sendBroadcast(broadcast);
            }
        } catch (IOException e) {
            Log.d(TAG, "Connection failed");
            e.printStackTrace();
        }
    }

    public static void writeMessage(String message) {
        try {
            InputStream readStream = mBluetoothSocket.getInputStream();
            OutputStream writeStream = mBluetoothSocket.getOutputStream();
            ByteBuffer b = ByteBuffer.allocate(4);
            b.putInt(4121994);
            byte[] result = b.array();

            writeStream.write("niki".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeMessage(int command) {
        try {
            OutputStream writeStream = mBluetoothSocket.getOutputStream();
            writeStream.write(command);
        } catch (IOException e) {
            Log.d("Lisko", "Could not send command");
            e.printStackTrace();
        }
    }
}
