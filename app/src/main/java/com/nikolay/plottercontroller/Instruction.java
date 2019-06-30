package com.nikolay.plottercontroller;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

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
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(!(obj instanceof Instruction)) {
            return false;
        }

        Instruction i = (Instruction) obj;

        return this.getCommandId() == i.getCommandId() && this.getSteps() == i.getSteps();
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId, steps);
    }

    @Override
    public String toString() {
        return String.format("%5d: %2d, %2d", this.commandId, this.steps);
    }
}
