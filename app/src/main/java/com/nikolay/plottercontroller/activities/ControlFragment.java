package com.nikolay.plottercontroller.activities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.nikolay.plottercontroller.ControlButton;
import com.nikolay.plottercontroller.Instruction;
import com.nikolay.plottercontroller.InstructionDispatcher;
import com.nikolay.plottercontroller.R;
import com.nikolay.plottercontroller.Sequence;
import com.nikolay.plottercontroller.SequenceBuilder;
import com.nikolay.plottercontroller.bluetooth.BluetoothUtils;
import com.nikolay.plottercontroller.services.ExecuteSequenceService;
import com.nikolay.plottercontroller.services.StartConnectionService;

import java.util.ArrayList;
import java.util.List;

public class ControlFragment extends Fragment {
    public static final String EXTRA_INSTRUCTION_INDEX = "instructionIndex";

    private Button mButton200;
    private Button mButton2048;
    private EditText mEditTextSteps;
    private CheckBox mCheckboxUseSteps;
    private TextView mTextView;
    private boolean mUseSteps = false;
    private int mInstructionIndex = 941204;
    //private boolean mCommandChannelOpen = true;
    private boolean mIsRecording = false;
    private List<Instruction> mRecordedInstructions = new ArrayList<Instruction>();
    private boolean mIsExecuting = false;

    private BroadcastReceiver mCommandReadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case StartConnectionService.ACTION_HC05_RESPONSE: {
                    int index = intent.getIntExtra(EXTRA_INSTRUCTION_INDEX, -1);
                    if (index == mInstructionIndex) { //command has been executed
                        prepareNewCommand();
                    } else {
                        // TODO command not executed correctly?
                    }
                    break;
                }
                case ExecuteSequenceService.ACTION_SEQUENCE_STARTED: {
                    mIsExecuting = true;
                    break;
                }
                case ExecuteSequenceService.ACTION_SEQUENCE_FINISHED: {
                    mIsExecuting = false;
                    break;
                }
//                case ExecuteSequenceService.ACTION_COMMAND_STARTED: {
//                    mIsExecuting = true;
//                    break;
//                }
//                case ExecuteSequenceService.ACTION_COMMAND_FINISHED: {
//                    mIsExecuting = false;
//                    break;
//                }
            }

        }
    };

    public ControlFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_control, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initUi();
        setListeners();
        BluetoothUtils.registerCommandReadReceiver(getContext(), mCommandReadReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(mCommandReadReceiver);
    }

    private void initUi() {
        mButton200 = getView().findViewById(R.id.button200);
        mButton2048 = getView().findViewById(R.id.button2048);
        mEditTextSteps = getView().findViewById(R.id.editTextSteps);
        mCheckboxUseSteps = getView().findViewById(R.id.checkBoxUseSteps);
        mTextView = getView().findViewById(R.id.textViewLog);

        toggleInput(false);

        mCheckboxUseSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUseSteps = mCheckboxUseSteps.isChecked();

                if (mUseSteps) {
                    toggleInput(true);
                } else {
                    toggleInput(false);
                }
            }
        });

        mButton200.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditTextSteps.setText("200");
            }
        });

        mButton2048.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditTextSteps.setText("2048");
            }
        });
    }

    private void toggleInput(boolean enabled) {
        mButton200.setEnabled(enabled);
        mButton2048.setEnabled(enabled);
        mEditTextSteps.setEnabled(enabled);
    }

    private void setListeners() {
        getView().findViewById(R.id.buttonStepLeft).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonStepRight).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonStepUp).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonStepDown).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonRevLeft).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonRevRight).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonRevUp).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonRevDown).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonDraw).setOnClickListener(new CommandClickListener());
        //getView().findViewById(R.id.buttonStop).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonRecord).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsRecording) {
                    mIsRecording = false;
                    v.setBackgroundResource(R.drawable.button_square);
                    ((ControlButton) v).setImageResource(R.drawable.record);
                } else {
                    mIsRecording = true;
                    mRecordedInstructions.clear();
                    v.setBackgroundResource(R.drawable.button_square_red);
                    ((ControlButton) v).setImageResource(R.drawable.stop);
                }
            }
        });
        getView().findViewById(R.id.buttonPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
                for (Instruction i : mRecordedInstructions) {

                }
            }
        });
        getView().findViewById(R.id.buttonSequence).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // List<Instruction> sequence = SequenceBuilder.buildSquare(50, mInstructionIndex);
                Sequence sequence = SequenceBuilder.buildCheckerboard(9, mInstructionIndex);
                if (!mIsExecuting) {
                    // mIsExecuting = true;
                    //executeSequence(sequence);
                    ExecuteSequenceService.executeSequence(getContext(), sequence);
                }
            }
        });
    }

    private void prepareNewCommand() {
        mInstructionIndex++;
        ExecuteSequenceService.setCommandChannelOpen(true);
    }

    private void executeSequence(final List<Instruction> sequence) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                int sleep = 20;
                for (Instruction i : sequence) {
                    while (!ExecuteSequenceService.isCommandChannelOpen()) {
                        try {
                            Thread.sleep(sleep);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    ExecuteSequenceService.setCommandChannelOpen(false);
                    InstructionDispatcher.sendInstruction(i.getButtonId(), i.getSteps(), i.getInstructionIndex());
                }

                mIsExecuting = false;
            }
        });

        t.start();
    }

    private class CommandClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (mIsExecuting || !ExecuteSequenceService.isCommandChannelOpen()) {
                return;
            }

            int steps = -1;
            if (mUseSteps) {

                if (mEditTextSteps.getText() == null || mEditTextSteps.getText().toString().trim().equals("")) {
                    mEditTextSteps.setError("Set number between 0 and 32767");
                    return;
                }

                try {
                    steps = Integer.parseInt(mEditTextSteps.getText().toString());
                } catch (NumberFormatException e) {
                    mEditTextSteps.setError("Set number between 0 and 35767");
                    return;
                }
                if (steps < 0 || steps > 32767) {
                    mEditTextSteps.setError("Set number between 0 and 32767");
                    return;
                }
            }

            if (mIsRecording) {
                mRecordedInstructions.add(new Instruction(v.getId(), steps, mInstructionIndex));
                mInstructionIndex++;
            } else {
                ExecuteSequenceService.setCommandChannelOpen(false);
                boolean sent = InstructionDispatcher.sendInstruction(v.getId(), steps, mInstructionIndex);
                if (!sent) { // sending instruction failed
                    prepareNewCommand();
                }
            }

        }
    }

}
