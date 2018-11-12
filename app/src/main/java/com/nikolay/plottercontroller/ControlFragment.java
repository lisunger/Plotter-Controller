package com.nikolay.plottercontroller;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class ControlFragment extends Fragment {

    private static final int COMMAND_LEFT = 1;
    private static final int COMMAND_RIGHT = 2;
    private static final int COMMAND_UP = 3;
    private static final int COMMAND_DOWN = 4;

    private Button mButtonLeft;
    private Button mButtonRight;
    private Button mButtonUp;
    private Button mButtonDown;

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

        mButtonLeft = getView().findViewById(R.id.buttonLeft);
        mButtonRight = getView().findViewById(R.id.buttonRight);
        mButtonUp = getView().findViewById(R.id.buttonUp);
        mButtonDown = getView().findViewById(R.id.buttonDown);

        mButtonLeft.setOnClickListener(new CommandClickListener(COMMAND_LEFT));
        mButtonRight.setOnClickListener(new CommandClickListener(COMMAND_RIGHT));
        mButtonUp.setOnClickListener(new CommandClickListener(COMMAND_UP));
        mButtonDown.setOnClickListener(new CommandClickListener(COMMAND_DOWN));
    }

    private class CommandClickListener implements View.OnClickListener {

        private int command = -1;

        private CommandClickListener(int command) {
            this.command = command;
        }

        @Override
        public void onClick(View v) {
            StartConnectionService.writeMessage(this.command);
        }
    }
}
