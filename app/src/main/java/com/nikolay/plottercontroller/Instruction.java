package com.nikolay.plottercontroller;

import android.os.Parcel;
import android.os.Parcelable;

public class Instruction {

    private int commandId;
    private int steps;

    public Instruction(int commandId, int steps) {
        this.commandId = commandId;
        this.steps = steps;
    }

    public int getCommandId() {
        return commandId;
    }

    public void setCommandId(int buttonId) {
        this.commandId = buttonId;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    @Override
    public String toString() {
        return String.format("%5d: %2d, %2d", this.commandId, this.steps);
    }
}
