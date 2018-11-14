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
import android.widget.ImageView;

public class ControlFragment extends Fragment {

    private static final int COMMAND_LEFT = 1;
    private static final int COMMAND_RIGHT = 2;
    private static final int COMMAND_UP = 3;
    private static final int COMMAND_DOWN = 4;

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

        getView().findViewById(R.id.buttonStepLeft).setOnClickListener(new CommandClickListener(COMMAND_LEFT));
        getView().findViewById(R.id.buttonStepLeft).setOnTouchListener(new ButtonTouchListener());
        getView().findViewById(R.id.buttonStepRight).setOnClickListener(new CommandClickListener(COMMAND_RIGHT));
        getView().findViewById(R.id.buttonStepRight).setOnTouchListener(new ButtonTouchListener());
        getView().findViewById(R.id.buttonStepUp).setOnClickListener(new CommandClickListener(COMMAND_UP));
        getView().findViewById(R.id.buttonStepUp).setOnTouchListener(new ButtonTouchListener());
        getView().findViewById(R.id.buttonStepDown).setOnClickListener(new CommandClickListener(COMMAND_DOWN));
        getView().findViewById(R.id.buttonStepDown).setOnTouchListener(new ButtonTouchListener());
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

    private class ButtonTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                Log.d("Lisko", "DOWN");
                v.setBackgroundResource(R.drawable.button_square_pressed);
                return true;
            }
            else if(event.getAction() == MotionEvent.ACTION_UP) {
                Log.d("Lisko", "UP");
                v.setBackgroundResource(R.drawable.button_square);
                return false;
            }
            return false;
        }
    }
}
