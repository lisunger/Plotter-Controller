package com.nikolay.plottercontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // 00001101-0000-1000-8000-00805f9b34fb
    public static final String TAG = "Lisko";
    public static final String HC05_MAC_ADDRESS = "00:18:E4:00:78:F9";
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_PERMISSION_LOCATION = 2;

    private boolean mScanning = false;
    private boolean mConnected = false;
    private Fragment mActiveFragment;
    private Menu mMenu;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mHc05device = null;
    private BluetoothSocket mBluetoothSocket = null;
    private UUID mUuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    /* Listens to bluetooth turn on/off */
    BroadcastReceiver mBluetoothStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if(action != null) {
                switch (action) {
                    case BluetoothStateChangeReceiver.ACTION_BLUETOOTH_STARTED: {
                        Log.d(TAG, "You start me up");
                        break;
                    }
                    case BluetoothStateChangeReceiver.ACTION_BLUETOOTH_STOPPED: {
                        Log.d(TAG, "Don't stop me now!");
                        break;
                    }
                }
            }
        }
    };

    /* Listens to bluetooth scanning and finding devices */
    BroadcastReceiver mDeviceFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action != null) {
                switch (action) {
                    case BluetoothDevice.ACTION_FOUND: { // Device found
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (device.getAddress().equals(HC05_MAC_ADDRESS)) {
                            Log.d(TAG, "Found HC-05!");
                            mHc05device = device;
                            connectToHc05();
                        }
                        break;
                    }
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED: { // Scan started
                        Log.d(TAG, "Scan started");
                        mScanning = true;
                        ((ScanFragment) mActiveFragment).startScanning();
                        break;
                    }
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED: { // Scan stopped
                        Log.d(TAG, "Scan stopped");
                        //TODO hide progressBar, show message
                        mScanning = false;
                        break;
                    }
                    case BluetoothDevice.ACTION_BOND_STATE_CHANGED: { // A device paired/unpaired
                        Log.d(TAG, action);
                        int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                        if (bondState == BluetoothDevice.BOND_NONE) {
                            Log.d(TAG, "NO bond");
                        }
                        if (bondState == BluetoothDevice.BOND_BONDING) {
                            Log.d(TAG, "BONDING bond");
                        }
                        if (bondState == BluetoothDevice.BOND_BONDED) {
                            Log.d(TAG, "BONDED bond");
                        }
                        // TODO connect

                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        BluetoothUtils.requestLocationPermission(this);

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

        BluetoothUtils.registerBluetoothStateReceiver(this, mBluetoothStateBroadcastReceiver);
        BluetoothUtils.registerBluetoothDeviceReceiver(this, mDeviceFoundReceiver);

        BluetoothUtils.displayPairedDevices(mBluetoothAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mScanning = mBluetoothAdapter.isDiscovering();

        /* If null:
         *   - the application is opened for the first time;
         *   - or no scan has been started;
         *   - or no device has been located;
         */
        if(mConnected) {
            setFragment(new ControlFragment());
        }
        else {
            setFragment(new ScanFragment());
        }
        if(mMenu != null) {
            setMenuScan();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_scan : {
                Log.d(TAG, "Should I scan or should I go now?");
                if(!mScanning) {
                    mBluetoothAdapter.startDiscovery();
                }
                break;
            }
        }
        return super.onOptionsItemSelected(item);
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

    private void setFragment(Fragment fragment) {
        mActiveFragment = fragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mActiveFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        //transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setMenuScan() {
        MenuItem item = mMenu.findItem(R.id.label_scan);
        if(item == null) return;

        if(mConnected) {
            item.setEnabled(false);
        }
        else {
            item.setEnabled(true);
        }
    }

    private void connectToHc05() {
        mBluetoothAdapter.cancelDiscovery();
        try {
            mBluetoothSocket = mHc05device.createRfcommSocketToServiceRecord(mUuid);

            // TODO da sledi dali vrazkata ne se e razpadnala!!!!
            StartConnectionService.startBluetoothConnection(this, mBluetoothSocket);

            //mBluetoothSocket.connect();
            //TODO get this value from receiver
            mConnected = true;

            setFragment(new ControlFragment());
            findViewById(R.id.menu_scan).setVisibility(View.GONE);
            findViewById(R.id.menu_scan).setEnabled(false);

        } catch (IOException e) {
            e.printStackTrace();
            mConnected = false;
        }
    }
}
