package com.nikolay.plottercontroller.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.nikolay.plottercontroller.Instruction;
import com.nikolay.plottercontroller.R;
import com.nikolay.plottercontroller.Sequence;
import com.nikolay.plottercontroller.activities.ControlFragment;
import com.nikolay.plottercontroller.activities.MainActivity;
import com.nikolay.plottercontroller.bitmap.Dither;
import com.nikolay.plottercontroller.bluetooth.BluetoothUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StartConnectionService extends IntentService {

    private static final String TAG = "Lisko";
    public static final String ACTION_HC05_CONNECTED =      "com.nikolay.plottercontroller.action.CONNECTED";
    public static final String ACTION_HC05_DISCONNECTED =   "com.nikolay.plottercontroller.action.DISCONNECTED";
    public static final String ACTION_HC05_RESPONSE =       "com.nikolay.plottercontroller.action.RESPONSE";
    private static BluetoothSocket mBluetoothSocket;

    public static final String ACTION_SEQUENCE_STARTED = "com.nikolay.plottercontroller.action.SEQUENCE_STARTED";
    public static final String ACTION_SEQUENCE_FINISHED = "com.nikolay.plottercontroller.action.SEQUENCE_FINISHED";
    private boolean mCommandChannelOpen = true;

    private static final String EXTRA_INSTRUCTION_INDEX = "instructionIndex";

    private int mInstructionIndex = 0;
    private int sequenceIndex = 0;
    private boolean executingSequence = false;
    private Sequence sequence;


    private BroadcastReceiver mCommandReadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case StartConnectionService.ACTION_HC05_RESPONSE: {
                    int index = intent.getIntExtra(EXTRA_INSTRUCTION_INDEX, -1);
                    if (true || (index == (mInstructionIndex - 1))) { //command has been executed
                        mCommandChannelOpen = true;
                        mInstructionIndex++;

                        if(executingSequence) {
                            sequenceIndex++;
                            executeSequenceStep(sequenceIndex);
                        }
                    } else {
                        // TODO command not executed correctly?
                    }
                    break;
                }
            }

        }
    };

    IBinder mBinder = new LocalBinder();

    public StartConnectionService() {
        super("StartConnectionService");
    }

    public boolean isConnected() {
        if(mBluetoothSocket == null) {
            return false;
        } else {
            return mBluetoothSocket.isConnected();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        BluetoothUtils.registerCommandReadReceiver(this, mCommandReadReceiver);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        startForeground(1, buildForegroundNotification());

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
                while(readStream.available() < 4) { Thread.sleep(1); }
                byte[] value = new byte[4];
                readStream.read(value, 0, 4);

                int instructionIndex = 0;
                instructionIndex |= (value[0] & 0xff) << 24;
                instructionIndex |= (value[1] & 0xff) << 16;
                instructionIndex |= (value[2] & 0xff) << 8;
                instructionIndex |=  value[3] & 0xff;
                Intent broadcast = new Intent(ACTION_HC05_RESPONSE);
                broadcast.putExtra(EXTRA_INSTRUCTION_INDEX, instructionIndex);
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

        }

        //When connection breaks, send broadcast and turn off
        disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mCommandReadReceiver);

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
        Toast.makeText(this, "StartConnectionService destroyed", Toast.LENGTH_SHORT).show();
    }

   @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
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
    private boolean sendInstruction(int command, int value, int instructionIndex) {
        mCommandChannelOpen = false;
        try {
            OutputStream writeStream = mBluetoothSocket.getOutputStream();

            // instruction beginning
            writeStream.write(new byte[] {'n', 'i', 'k', 'i'});

            // command
            writeStream.write(Integer.valueOf(command).byteValue());

            //value (first two bytes only)
            writeStream.write((value >> 8) & 0b11111111);
            writeStream.write(value  & 0b11111111);

            // index
            writeStream.write((instructionIndex >> 24) & 0b11111111);
            writeStream.write((instructionIndex >> 16) & 0b11111111);
            writeStream.write((instructionIndex >> 8) & 0b11111111);
            writeStream.write(instructionIndex  & 0b11111111);

            return true;
        } catch (IOException e) {
            Log.d("Lisko", "Could not send command");
            // TODO scan if connected device is no longer there (powered off)
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendInstruction(int command, int value) {
        return sendInstruction(command, value, getNextInstructionIndex());
    }

    public void startSequence(Context context, int imageId) {

            this.sequence = Dither.getSequenceFromImage(context, imageId);
            this.sequenceIndex = 0;
            this.executingSequence = true;
            Log.d("Lisko", sequence.getInstructions().size() + " instructions");
            Log.d("Lisko", "Start time:\t" + System.currentTimeMillis());
            Toast.makeText(this, String.valueOf(sequence.getInstructions().size()), Toast.LENGTH_LONG).show();
            executeSequenceStep(sequenceIndex);
    }

    private void executeSequenceStep(int step) {
        if(step < sequence.getInstructions().size()) {
            mCommandChannelOpen = false;
            Instruction i = sequence.getInstructions().get(step);
            //Log.d("Lisko", String.format(">>%.2f", (double) step / sequence.getInstructions().size()));
            sendInstruction(i.getCommandId(), i.getSteps(), getNextInstructionIndex());
        }
        else {
            executingSequence = false;
            Log.d("Lisko", "End time:\t" + System.currentTimeMillis());
        }
    }

    private Notification buildForegroundNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new Notification.Builder(this)
                        .setContentTitle("Connected to Plotter")
                        .setContentText("Hello")
                        .setSmallIcon(R.drawable.pen)
                        .setContentIntent(pendingIntent)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setTicker("Ticker text")
                        .build();
        return notification;
    }

    public boolean isCommandChannelOpen() {
        return mCommandChannelOpen && !executingSequence;
    }

    public int getNextInstructionIndex() {
        mInstructionIndex++;
        return mInstructionIndex - 1;
    }

    public static BluetoothSocket getBluetoothSocket() {
        return mBluetoothSocket;
    }

    public static void setBluetoothSocket(BluetoothSocket bluetoothSocket) {
        mBluetoothSocket = bluetoothSocket;
    }

    public class LocalBinder extends Binder {
        public StartConnectionService getService() {
            return StartConnectionService.this;
        }
    }
}
