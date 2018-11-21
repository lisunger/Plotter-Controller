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
    public static final String ACTION_HC05_CONNECTED =      "com.nikolay.plottercontroller.action.CONNECTED";
    public static final String ACTION_HC05_DISCONNECTED =   "com.nikolay.plottercontroller.action.DISCONNECTED";
    public static final String ACTION_HC05_RESPONSE =       "com.nikolay.plottercontroller.action.RESPONSE";
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
        InputStream readStream = null;
        try {
            readStream = mBluetoothSocket.getInputStream();
        } catch (IOException e) {
            Log.d("Lisko", "Could not open input stream");
            disconnect();
            e.printStackTrace();
        }
        // Do nothing while connection is active
        while(mBluetoothSocket.isConnected()) {
            try {
                while(readStream.available() < 4) { Thread.sleep(500); }
                byte[] value = new byte[4];
                readStream.read(value, 0, 4);
                Intent broadcast = new Intent(ACTION_HC05_RESPONSE);
                broadcast.putExtra(ControlFragment., value);
                sendBroadcast(broadcast);

            } catch (IOException e) {
                Log.d("Lisko", "Could not open input stream");
                disconnect();
                e.printStackTrace();
            } catch (InterruptedException e) {
                Log.d("Lisko", "Bluetooth connection error");
                disconnect();
                e.printStackTrace();
            }

        };

        //When connection breaks, send broadcast and turn off
        disconnect();
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

    private void disconnect() {
        Intent broadcast = new Intent(ACTION_HC05_DISCONNECTED);
        sendBroadcast(broadcast);
        stopSelf();
    }

    /**
     * @return true if the command has been sent, false otherwise
     */
    public static boolean sendCommand(int command, int value, int index) {
        try {

            OutputStream writeStream = mBluetoothSocket.getOutputStream();

            // instruction beginning
            writeStream.write(new byte[] {'n', 'i', 'k', 'i'});

            // command
            writeStream.write(Integer.valueOf(command).byteValue());

            //value (first two bytes only)
            writeStream.write(value  & 0b11111111);
            writeStream.write((value >> 8) & 0b11111111);

            // index
            writeStream.write(index  & 0b11111111);
            writeStream.write((index >> 8) & 0b11111111);
            writeStream.write((index >> 16) & 0b11111111);
            writeStream.write((index >> 24) & 0b11111111);

            return true;
        } catch (IOException e) {
            Log.d("Lisko", "Could not send command");
            // TODO scan if connected device is no longer there (powered off)
            e.printStackTrace();
            return false;
        }
    }
}
