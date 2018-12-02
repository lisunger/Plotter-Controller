package com.nikolay.plottercontroller;

import android.os.Parcel;
import android.os.Parcelable;

public class Instruction implements Parcelable {

    private int buttonId;
    private int steps;
    private int instructionIndex;

    public Instruction(int buttonId, int steps, int instructionIndex) {
        this.buttonId = buttonId;
        this.steps = steps;
        this.instructionIndex = instructionIndex;
    }

    protected Instruction(Parcel in) {
        buttonId = in.readInt();
        steps = in.readInt();
        instructionIndex = in.readInt();
    }

    public static final Creator<Instruction> CREATOR = new Creator<Instruction>() {
        @Override
        public Instruction createFromParcel(Parcel in) {
            return new Instruction(in);
        }

        @Override
        public Instruction[] newArray(int size) {
            return new Instruction[size];
        }
    };

    public int getButtonId() {
        return buttonId;
    }

    public void setButtonId(int buttonId) {
        this.buttonId = buttonId;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getInstructionIndex() {
        return instructionIndex;
    }

    public void setInstructionIndex(int instructionIndex) {
        this.instructionIndex = instructionIndex;
    }

    @Override
    public String toString() {
        return String.format("%5d: %2d, %2d", this.instructionIndex, this.buttonId, this.steps);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(buttonId);
        dest.writeInt(steps);
        dest.writeInt(instructionIndex);
    }


}
