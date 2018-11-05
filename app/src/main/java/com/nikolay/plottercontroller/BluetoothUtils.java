package com.nikolay.plottercontroller;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;

import java.util.Set;

public class BluetoothUtils {

    private static final String TAG = "Lisko";

    public static void requestLocationPermission(Activity context) {
        context.requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, MainActivity.REQUEST_PERMISSION_LOCATION);
    }

    public static void registerBluetoothStateReceiver(Context context, BroadcastReceiver broadcastReceiver) {
        // Set up a broadcast receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothStateChangeReceiver.ACTION_BLUETOOTH_STARTED);
        intentFilter.addAction(BluetoothStateChangeReceiver.ACTION_BLUETOOTH_STOPPED);
        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    public static void registerBluetoothDeviceReceiver(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    public static void registerConnectionStateReceiver(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(StartConnectionService.ACTION_HC05_CONNECTED);
        intentFilter.addAction(StartConnectionService.ACTION_HC05_DISCONNECTED);
        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    public static void displayPairedDevices(Context context, BluetoothAdapter bluetoothAdapter) {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                if(device.getAddress().equals(MainActivity.HC05_MAC_ADDRESS)) {
                    //TODO something...
                }
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d(TAG, deviceName);
                Log.d(TAG, deviceHardwareAddress);
            }
        }
    }
}
