package com.nikolay.plottercontroller.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.nikolay.plottercontroller.Instruction;
import com.nikolay.plottercontroller.Sequence;
import com.nikolay.plottercontroller.activities.ControlFragment;
import com.nikolay.plottercontroller.bitmap.Dither;

import java.util.ResourceBundle;

public class ExecuteSequenceService extends IntentService {

    private static final String EXTRA_SEQUENCE = "extraSequence";
    private static final String EXTRA_IMAGEID = "extraImageId";
    private static final String EXTRA_INSTRUCTION_INDEX = "extraInstructionIndex";
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

    public static void executeSequence(Context context, int imageId, int instructionIndex) {
        Intent intent = new Intent(context, ExecuteSequenceService.class);
        intent.putExtra(EXTRA_IMAGEID, imageId);
        intent.putExtra(EXTRA_INSTRUCTION_INDEX, instructionIndex);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            int imageId = intent.getIntExtra(EXTRA_IMAGEID, -111111);
            int instructionIndex = intent.getIntExtra(EXTRA_INSTRUCTION_INDEX, -11111);
            // TODO check invalid values above
            final Sequence sequence = Dither.getSequenceFromImage(this, imageId, instructionIndex);
            //final Sequence sequence = intent.getExtras().getParcelable(EXTRA_SEQUENCE);
            Log.d("Lisko", sequence.getInstructions().size() + "instructions");

            Intent broadcastStart = new Intent(ACTION_SEQUENCE_STARTED);
            sendBroadcast(broadcastStart);

            int sleep = 20;
            for(int j = 0; j < sequence.getInstructions().size(); j++) {
                // TODO
                Instruction i = sequence.getInstructions().get(j);
                while (!isCommandChannelOpen()) {
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                setCommandChannelOpen(false);

                Log.d("Lisko", String.format(">>%.2f", (double) j / sequence.getInstructions().size()));
                StartConnectionService.sendInstruction(i.getButtonId(), i.getSteps(), i.getInstructionIndex());
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
