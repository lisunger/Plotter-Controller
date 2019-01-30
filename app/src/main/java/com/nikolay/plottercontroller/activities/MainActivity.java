package com.nikolay.plottercontroller.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.nikolay.plottercontroller.R;
import com.nikolay.plottercontroller.bluetooth.BluetoothStateChangeReceiver;
import com.nikolay.plottercontroller.bluetooth.BluetoothUtils;
import com.nikolay.plottercontroller.services.StartConnectionService;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements ControlFragment.ServiceConnectionActivity {

    // 00001101-0000-1000-8000-00805f9b34fb
    public static final String TAG = "Lisko";
    public static final String HC05_MAC_ADDRESS = "00:18:E4:00:78:F9";
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_PERMISSION_LOCATION = 2;

    private boolean mScanning = false;
    private boolean mConnected = false;
    private Fragment mActiveFragment;
    private Menu mMenu;
    private boolean mScanStartedByApp = false;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mHc05device = null;
    private BluetoothSocket mBluetoothSocket = null;
    private UUID mUuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private StartConnectionService mService;

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StartConnectionService.LocalBinder binder = ((StartConnectionService.LocalBinder)service);
            mService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) { }
    };

    /* Listens to bluetooth turn on/off */
    BroadcastReceiver mBluetoothStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case BluetoothStateChangeReceiver.ACTION_BLUETOOTH_STARTED: {
                        Log.d(TAG, "You start me up");
                        break;
                    }
                    case BluetoothStateChangeReceiver.ACTION_BLUETOOTH_STOPPED: {
                        Log.d(TAG, "Don't stop me now!");
                        stopService(new Intent(context, StartConnectionService.class));
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
            if (action != null) {
                switch (action) {
                    case BluetoothDevice.ACTION_FOUND: { // Device found
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (device.getAddress().equals(HC05_MAC_ADDRESS)) {
                            Log.d(TAG, "Found HC-05!");
                            mHc05device = device;
                            ((ScanFragment) mActiveFragment).setTextConnecting();
                            connectToHc05();
                        }
                        break;
                    }
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED: { // Scan started
                        /* I want to connect to the module only if I clicked the icon,
                         * not when the user turns on the bluetooth (automatic discovery starts)
                         */
                        if(mScanStartedByApp) {
                            Log.d(TAG, "Scan started");
                            mScanning = true;
                            ((ScanFragment) mActiveFragment).startScanning();
                        }
                        else {
                            mBluetoothAdapter.cancelDiscovery();
                        }
                        break;
                    }
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED: { // Scan stopped
                        Log.d(TAG, "Scan stopped");
                        /* Two cases - search stopped with/without finding the device
                            if found, it automatically changes framgent, no need to do anything
                            if not found - hide progressBar, show message
                         */
                        mScanning = false;
                        mScanStartedByApp = false;
                        if (mActiveFragment instanceof ScanFragment) {
                            ((ScanFragment) mActiveFragment).stopScanning();
                        }
                        break;
                    }
                    case BluetoothDevice.ACTION_BOND_STATE_CHANGED: { /* A device paired/unpaired */ }
                }
            }
        }
    };

    BroadcastReceiver mConnectionStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case StartConnectionService.ACTION_HC05_CONNECTED: {
                    mConnected = true;
                    setMenu();
                    setControlFragment();
                    //StartConnectionService.setCommandChannelOpen(true);
                    break;
                }
                case StartConnectionService.ACTION_HC05_DISCONNECTED: {
                    mConnected = false;
                    setScanFragment();
                    break;
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
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "Bluetooth not supported");
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show();
            finish();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
        }

        BluetoothUtils.registerBluetoothStateReceiver(this, mBluetoothStateBroadcastReceiver);
        BluetoothUtils.registerBluetoothDeviceReceiver(this, mDeviceFoundReceiver);
        BluetoothUtils.registerConnectionStateReceiver(this, mConnectionStateReceiver);

    }

    @Override
    protected void onResume() {
        super.onResume();

        mBluetoothAdapter.cancelDiscovery();

        Intent intent = new Intent(this, StartConnectionService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        mConnected = (mService != null) && (mService.isConnected());
        if (mConnected) {
            setControlFragment();
        } else {
            setScanFragment();
        }
        if (mMenu != null) {
            setMenu();
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
        switch (item.getItemId()) {
            case R.id.menu_scan: {
                if(mBluetoothAdapter.isEnabled()) {
                    Log.d(TAG, "Should I scan or should I go now?");
                    if (!mScanning) {
                        mScanStartedByApp = true;
                        mBluetoothAdapter.startDiscovery();
                    }
                }
                else {
                    Toast.makeText(this, "Enable bluetooth before you scan", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.menu_disconnect: {
                Log.d(TAG, "Bluetooth unplugged");
                this.unbindService(mConnection);
                stopService(new Intent(this, StartConnectionService.class));
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        switch (requestCode) {
            case REQUEST_ENABLE_BT: {
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "Bluetooth started");
                } else {
                    Log.d(TAG, "Bluetooth canceled");
                    finish();
                }
                break;
            }
            case REQUEST_PERMISSION_LOCATION: {
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "Permission granted");
                } else if (resultCode == RESULT_CANCELED) {
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
        unregisterReceiver(mBluetoothStateBroadcastReceiver);
        unregisterReceiver(mDeviceFoundReceiver);
        unregisterReceiver(mConnectionStateReceiver);
        Log.d(TAG, "MainActivity Destroyed");
        Toast.makeText(this, "MainActivity destroyed", Toast.LENGTH_SHORT).show();
    }

    private void setScanFragment() {
        mActiveFragment = new ScanFragment();
        changeFragment();
        if(mMenu != null) {
            setMenu();
        }
    }

    private void setControlFragment() {
        mActiveFragment = new ControlFragment();
        changeFragment();
        if(mMenu != null) {
            setMenu();
        }
    }

    private void changeFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mActiveFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    private void setMenu() {
        MenuItem itemScan = mMenu.findItem(R.id.menu_scan);
        MenuItem itemDisconnect = mMenu.findItem(R.id.menu_disconnect);

        if (mActiveFragment instanceof ControlFragment) {
            itemScan.setEnabled(false);
            itemScan.setVisible(false);
            itemDisconnect.setEnabled(true);
            itemDisconnect.setVisible(true);
        } else if(mActiveFragment instanceof ScanFragment){
            itemScan.setEnabled(true);
            itemScan.setVisible(true);
            itemDisconnect.setEnabled(false);
            itemDisconnect.setVisible(false);
        }
    }

    private void connectToHc05() {
        mBluetoothAdapter.cancelDiscovery();

        try {
            mBluetoothSocket = mHc05device.createRfcommSocketToServiceRecord(mUuid);
            mService.setBluetoothSocket(mBluetoothSocket);
            Intent intent = new Intent(this, StartConnectionService.class);
            startService(intent);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        } catch (IOException e) {
            Log.d(TAG, "Cannot open socket.");
            e.printStackTrace();
            mConnected = false;
        }
    }

    @Override
    public boolean isCommandChannelOpen() {
        return mService.isCommandChannelOpen();
    }

    @Override
    public void startSequence(Context context, int imageId) {
        mService.startSequence(context, imageId);
    }

    @Override
    public boolean sendInstruction(int command, int value) {
        return mService.sendInstruction(command, value);
    }
}