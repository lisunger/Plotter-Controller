package com.nikolay.plottercontroller;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Sequence {

    private List<Instruction> instructions;

    public Sequence(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    @Override
    public String toString() {
        return "Set of " + this.instructions.size() + " instructions";
    }
}
