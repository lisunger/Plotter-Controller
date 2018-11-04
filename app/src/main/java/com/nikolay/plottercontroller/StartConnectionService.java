package com.nikolay.plottercontroller;

import android.app.IntentService;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import java.io.IOException;

public class StartConnectionService extends IntentService {

    private static final String ACTION_CONNECT = "com.nikolay.plottercontroller.action.CONNECT";
    private static BluetoothSocket mBluetoothSocket;

    public StartConnectionService() {
        super("StartConnectionService");
    }

    public static void startBluetoothConnection(Context context, BluetoothSocket bluetoothSocket) {
        mBluetoothSocket = bluetoothSocket;
        Intent intent = new Intent(context, StartConnectionService.class);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            connectToHc05();
        }
    }

    private void connectToHc05() {
        try {
            if(mBluetoothSocket != null) {
                mBluetoothSocket.connect();
            }
        } catch (IOException e) {
            Log.d(MainActivity.TAG, "Connection failed");
            e.printStackTrace();
        }
    }
}
