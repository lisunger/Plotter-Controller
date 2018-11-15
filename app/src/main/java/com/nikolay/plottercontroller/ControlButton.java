package com.nikolay.plottercontroller;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ControlButton extends android.support.v7.widget.AppCompatImageView {

    private int command = -1;
    private int value = 1;

    public ControlButton(Context context) {
        super(context);
        this.setOnTouchListener(new ButtonTouchListener());
    }

    public ControlButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnTouchListener(new ButtonTouchListener());
    }

    public int getCommand() {
        return this.command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    private class ButtonTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                v.setBackgroundResource(R.drawable.button_square_pressed);
                return true;
            }
            else if(event.getAction() == MotionEvent.ACTION_UP) {
                v.setBackgroundResource(R.drawable.button_square);
                v.performClick();
                return false;
            }
            return false;
        }
    }

}
