package com.nikolay.plottercontroller;

public class Instruction {

    private int buttonId;
    private int steps;
    private int instructionIndex;

    public Instruction(int buttonId, int steps, int instructionIndex) {
        this.buttonId = buttonId;
        this.steps = steps;
        this.instructionIndex = instructionIndex;
    }

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
}
