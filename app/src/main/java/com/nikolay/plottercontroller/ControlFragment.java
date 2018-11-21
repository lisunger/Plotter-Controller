package com.nikolay.plottercontroller;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ControlFragment extends Fragment {

    private Button mButton200;
    private Button mButton2048;
    private EditText mEditTextSteps;
    private CheckBox mCheckboxUseSteps;
    private boolean mUseSteps = false;
    private int mCommandIndex = 0;
    private boolean cCommandChannelOpen = true;

    private List<ControlFragment> mButtonsList = new ArrayList<>();

    public ControlFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_control, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initUi();
        setCommands();
    }

    private void initUi() {
        mButton200 = getView().findViewById(R.id.button200);
        mButton2048 = getView().findViewById(R.id.button2048);
        mEditTextSteps = getView().findViewById(R.id.editTextSteps);
        mCheckboxUseSteps = getView().findViewById(R.id.checkBoxUseSteps);

        toggleInput(false);

        mCheckboxUseSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUseSteps = mCheckboxUseSteps.isChecked();

                if(mUseSteps) {
                    toggleInput(true);
                }
                else {
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

    private void setCommands() {
        ((ControlButton) getView().findViewById(R.id.buttonStepLeft)).setCommand(BluetoothCommands.COMMAND_LEFT);
        ((ControlButton) getView().findViewById(R.id.buttonStepRight)).setCommand(BluetoothCommands.COMMAND_RIGHT);
        ((ControlButton) getView().findViewById(R.id.buttonStepUp)).setCommand(BluetoothCommands.COMMAND_UP);
        ((ControlButton) getView().findViewById(R.id.buttonStepDown)).setCommand(BluetoothCommands.COMMAND_DOWN);
        ((ControlButton) getView().findViewById(R.id.buttonRevLeft)).setCommand(BluetoothCommands.COMMAND_LEFT);
        ((ControlButton) getView().findViewById(R.id.buttonRevRight)).setCommand(BluetoothCommands.COMMAND_RIGHT);
        ((ControlButton) getView().findViewById(R.id.buttonRevUp)).setCommand(BluetoothCommands.COMMAND_UP);
        ((ControlButton) getView().findViewById(R.id.buttonRevDown)).setCommand(BluetoothCommands.COMMAND_DOWN);
        ((ControlButton) getView().findViewById(R.id.buttonDraw)).setCommand(BluetoothCommands.COMMAND_DOT);
        ((ControlButton) getView().findViewById(R.id.buttonStop)).setCommand(BluetoothCommands.COMMAND_STOP);

        ((ControlButton) getView().findViewById(R.id.buttonStepLeft)).setValue(BluetoothCommands.VALUE_LEFT);
        ((ControlButton) getView().findViewById(R.id.buttonStepRight)).setValue(BluetoothCommands.VALUE_RIGHT);
        ((ControlButton) getView().findViewById(R.id.buttonStepUp)).setValue(BluetoothCommands.VALUE_UP);
        ((ControlButton) getView().findViewById(R.id.buttonStepDown)).setValue(BluetoothCommands.VALUE_DOWN);
        ((ControlButton) getView().findViewById(R.id.buttonRevLeft)).setValue(BluetoothCommands.ROTATION_NEMA);
        ((ControlButton) getView().findViewById(R.id.buttonRevRight)).setValue(BluetoothCommands.ROTATION_NEMA);
        ((ControlButton) getView().findViewById(R.id.buttonRevUp)).setValue(BluetoothCommands.ROTATION_BYJ);
        ((ControlButton) getView().findViewById(R.id.buttonRevDown)).setValue(BluetoothCommands.ROTATION_BYJ);

        getView().findViewById(R.id.buttonStepLeft).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonStepRight).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonStepUp).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonStepDown).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonRevLeft).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonRevRight).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonRevUp).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonRevDown).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonDraw).setOnClickListener(new CommandClickListener());
        getView().findViewById(R.id.buttonStop).setOnClickListener(new CommandClickListener());
    }

    private void prepareNewCommand() {
        mCommandIndex++;
        cCommandChannelOpen = true;
    }

    private class CommandClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.d("Lisko", ((ControlButton)v).getCommand() + "");
            if(((ControlButton)v).getCommand() == -1) {
                Toast.makeText(getContext(), "No command set for this button", Toast.LENGTH_SHORT).show();
            }
            else {
                int steps = ((ControlButton) v).getValue();
                if(mUseSteps) {

                    if(mEditTextSteps.getText() == null || mEditTextSteps.getText().toString().trim().equals("")) {
                        mEditTextSteps.setError("Set number between 0 and 16 777 215");
                        return;
                    }

                    try {
                        steps = Integer.parseInt(mEditTextSteps.getText().toString());
                    } catch(NumberFormatException e) {
                        mEditTextSteps.setError("Set number between 0 and 65535");
                        return;
                    }
                    if(steps < 0 || steps > 65535) {
                        mEditTextSteps.setError("Set number between 0 and 65535");
                        return;
                    }
                }
                if(cCommandChannelOpen) {
                    boolean sent = StartConnectionService.sendCommand(((ControlButton) v).getCommand(), steps, mCommandIndex);
                    if(!sent) {
                        prepareNewCommand();
                    }
                }
            }
        }
    }

}
