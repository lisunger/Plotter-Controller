package com.nikolay.plottercontroller;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // 00001101-0000-1000-8000-00805f9b34fb
    private static final String TAG = "Lisko";
    private static final String HC05_MAC_ADDRESS = "00:18:E4:00:78:F9";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION_LOCATION = 2;

    private Button mButtonScan;

    private boolean mScanning = false;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mHc05device;
    UUID mUuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    /* Listens to bluetooth turn on/off */
    BroadcastReceiver mBluetoothStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            switch(action) {
                case BluetoothStateChangeReceiver.ACTION_BLUETOOTH_STARTED : {
                    Log.d(TAG, "You start me up");
                    break;
                }
                case BluetoothStateChangeReceiver.ACTION_BLUETOOTH_STOPPED : {
                    Log.d(TAG, "Don't stop me now!");
                    break;
                }
            }
        }
    };

    /* Listens to bluetooth scanning and finding devices */
    BroadcastReceiver mDeviceFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch(action) {
                case BluetoothDevice.ACTION_FOUND : {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(device.getAddress().equals(HC05_MAC_ADDRESS)) {
                        // TODO connect devices
                        Log.d(TAG, "Found HC-05!");
                        mHc05device = device;
                        connectToHc05();
                    }
                    break;
                }
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED : {
                    Log.d(TAG, "Scan started");
                    mScanning = true;
                    mButtonScan.setText("STOP");
                    break;
                }
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED : {
                    Log.d(TAG, "Scan stopped");
                    mScanning = false;
                    mButtonScan.setText("SCAN");
                    break;
                }
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED : {
                    Log.d(TAG, action);
                    // TODO connect
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, REQUEST_PERMISSION_LOCATION);

        mButtonScan = findViewById(R.id.buttonScan);
        if(mScanning) {
            mButtonScan.setText("STOP");
        }
        else {
            mButtonScan.setText("SCAN");
        }
        mButtonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mBluetoothAdapter.startDiscovery();

                if(mScanning) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                else {
                    mBluetoothAdapter.startDiscovery();
                }
            }
        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            Log.d(TAG, "Bluetooth not supported");
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show();
            finish();
        }

        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
        }

        // Set up a broadcast receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothStateChangeReceiver.ACTION_BLUETOOTH_STARTED);
        intentFilter.addAction(BluetoothStateChangeReceiver.ACTION_BLUETOOTH_STOPPED);
        registerReceiver(mBluetoothStateBroadcastReceiver, intentFilter);

        // Set up a broadcast receiver
        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mDeviceFoundReceiver, intentFilter);

        // See list of paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                if(device.getAddress().equals(HC05_MAC_ADDRESS)) {

                }
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d(TAG, deviceName);
                Log.d(TAG, deviceHardwareAddress);
            }
        }
        //
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        switch(requestCode) {
            case REQUEST_ENABLE_BT : {
                if(resultCode == RESULT_OK) {
                    Log.d(TAG, "Bluetooth started");
                }
                else {
                    Log.d(TAG, "Bluetooth canceled");
                    finish();
                }
                break;
            }
            case REQUEST_PERMISSION_LOCATION : {
                if(resultCode == RESULT_OK) {
                    Log.d(TAG, "Permission granted");
                }
                else if(resultCode == RESULT_CANCELED) {
                    Log.d(TAG, "Permission refused");
                    finish();
                }
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroyed");
        unregisterReceiver(mBluetoothStateBroadcastReceiver);
        unregisterReceiver(mDeviceFoundReceiver);
    }

    private void connectToHc05() {
        mBluetoothAdapter.cancelDiscovery();
        try {
            BluetoothSocket socket = mHc05device.createRfcommSocketToServiceRecord(mUuid);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
