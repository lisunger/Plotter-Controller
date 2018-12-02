package com.nikolay.plottercontroller.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.nikolay.plottercontroller.Instruction;
import com.nikolay.plottercontroller.Sequence;
import com.nikolay.plottercontroller.activities.ControlFragment;

import java.util.ResourceBundle;

public class ExecuteSequenceService extends IntentService {

    private static final String EXTRA_SEQUENCE = "extraSequence";
    public static final String ACTION_SEQUENCE_STARTED = "com.nikolay.plottercontroller.action.SEQUENCE_STARTED";
    public static final String ACTION_SEQUENCE_FINISHED = "com.nikolay.plottercontroller.action.SEQUENCE_FINISHED";

    private static boolean commandChannelOpen = true;

    public ExecuteSequenceService() {
        super("ExecuteSequenceService");
    }

    public static void executeSequence(Context context, Sequence sequence) {
        Intent intent = new Intent(context, ExecuteSequenceService.class);
        intent.putExtra(EXTRA_SEQUENCE, sequence);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final Sequence sequence = intent.getExtras().getParcelable(EXTRA_SEQUENCE);
            Log.d("Lisko", sequence.toString());

            Intent broadcastStart = new Intent(ACTION_SEQUENCE_STARTED);
            sendBroadcast(broadcastStart);

            int sleep = 20;
            for(Instruction i : sequence.getInstructions()) {
                // TODO
                while (!isCommandChannelOpen()) {
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                setCommandChannelOpen(false);
                Log.d("Lisko", i.toString());
                StartConnectionService.sendInstruction((int)(Math.random()*3 +1), i.getSteps(), i.getInstructionIndex());
            }

            Intent broadcastFinish = new Intent(ACTION_SEQUENCE_FINISHED);
            sendBroadcast(broadcastFinish);
        }
    }

    public static synchronized boolean isCommandChannelOpen() {
        return commandChannelOpen;
    }

    public static synchronized void setCommandChannelOpen(boolean open) {
        commandChannelOpen = open;
    }
}
