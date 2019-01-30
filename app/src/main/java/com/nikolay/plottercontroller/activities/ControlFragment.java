package com.nikolay.plottercontroller.activities;


import android.content.Context;
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
import com.nikolay.plottercontroller.R;
import com.nikolay.plottercontroller.bluetooth.BluetoothCommands;

public class ControlFragment extends Fragment {

    private Button mButton200;
    private Button mButton2048;
    private EditText mEditTextSteps;
    private CheckBox mCheckboxUseSteps;
    private TextView mTextView;
    private boolean mUseSteps = false;
    private ServiceConnectionActivity mService;


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

        mService = (ServiceConnectionActivity) getActivity();
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
        ((ControlButton)getView().findViewById(R.id.buttonStepLeft)).setCommand(BluetoothCommands.COMMAND_LEFT);
        ((ControlButton)getView().findViewById(R.id.buttonStepRight)).setCommand(BluetoothCommands.COMMAND_RIGHT);
        ((ControlButton)getView().findViewById(R.id.buttonStepUp)).setCommand(BluetoothCommands.COMMAND_UP);
        ((ControlButton)getView().findViewById(R.id.buttonStepDown)).setCommand(BluetoothCommands.COMMAND_DOWN);
        ((ControlButton)getView().findViewById(R.id.buttonRevLeft)).setCommand(BluetoothCommands.COMMAND_LEFT);
        ((ControlButton)getView().findViewById(R.id.buttonRevRight)).setCommand(BluetoothCommands.COMMAND_RIGHT);
        ((ControlButton)getView().findViewById(R.id.buttonRevUp)).setCommand(BluetoothCommands.COMMAND_UP);
        ((ControlButton)getView().findViewById(R.id.buttonRevDown)).setCommand(BluetoothCommands.COMMAND_DOWN);
        ((ControlButton)getView().findViewById(R.id.buttonDraw)).setCommand(BluetoothCommands.COMMAND_DOT);

        ((ControlButton)getView().findViewById(R.id.buttonStepLeft)).setSteps(BluetoothCommands.VALUE_LEFT);
        ((ControlButton)getView().findViewById(R.id.buttonStepRight)).setSteps(BluetoothCommands.VALUE_RIGHT);
        ((ControlButton)getView().findViewById(R.id.buttonStepUp)).setSteps(BluetoothCommands.VALUE_UP);
        ((ControlButton)getView().findViewById(R.id.buttonStepDown)).setSteps(BluetoothCommands.VALUE_DOWN);
        ((ControlButton)getView().findViewById(R.id.buttonRevLeft)).setSteps(BluetoothCommands.ROTATION_NEMA);
        ((ControlButton)getView().findViewById(R.id.buttonRevRight)).setSteps(BluetoothCommands.ROTATION_NEMA);
        ((ControlButton)getView().findViewById(R.id.buttonRevUp)).setSteps(BluetoothCommands.ROTATION_BYJ);
        ((ControlButton)getView().findViewById(R.id.buttonRevDown)).setSteps(BluetoothCommands.ROTATION_BYJ);
        ((ControlButton)getView().findViewById(R.id.buttonDraw)).setSteps(-1);

        getView().findViewById(R.id.buttonStepLeft).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonStepRight).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonStepUp).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonStepDown).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonRevLeft).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonRevRight).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonRevUp).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonRevDown).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonDraw).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonSequence).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mService.isCommandChannelOpen()) {
                    mService.startSequence(getContext(), R.drawable.sw);
                }
            }
        });
    }

    private class CommandClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (!mService.isCommandChannelOpen()) {
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
                    mEditTextSteps.setError("Set number between 0 and 32767");
                    return;
                }
                if (steps < 0 || steps > 32767) {
                    mEditTextSteps.setError("Set number between 0 and 32767");
                    return;
                }
            }
            else {
                steps = ((ControlButton) v).getSteps();
            }

            mService.sendInstruction(((ControlButton) v ).getCommand(), steps);
        }
    }

    interface ServiceConnectionActivity {
        boolean isCommandChannelOpen();
        void startSequence(Context context, int imageId);
        boolean sendInstruction(int command, int value);
    }
}
