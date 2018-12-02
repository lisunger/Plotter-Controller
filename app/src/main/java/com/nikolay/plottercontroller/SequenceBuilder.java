package com.nikolay.plottercontroller;

import com.nikolay.plottercontroller.bluetooth.BluetoothCommands;

import java.util.ArrayList;
import java.util.List;

public class SequenceBuilder {

    public static Sequence buildSquare(int size, int commandIndex) {
        List<Instruction> sequence = new ArrayList<Instruction>();

        for(int i = 0; i < (size - 1); i++) {
            sequence.add(new Instruction(R.id.buttonDraw, -1, commandIndex++));
            sequence.add(new Instruction(R.id.buttonStepRight, BluetoothCommands.VALUE_RIGHT, commandIndex++));
        }
        for(int i = 0; i < (size - 1); i++) {
            sequence.add(new Instruction(R.id.buttonDraw, -1, commandIndex++));
            sequence.add(new Instruction(R.id.buttonStepUp, BluetoothCommands.VALUE_UP, commandIndex++));
        }
        for(int i = 0; i < (size - 1); i++) {
            sequence.add(new Instruction(R.id.buttonDraw, -1, commandIndex++));
            sequence.add(new Instruction(R.id.buttonStepLeft, BluetoothCommands.VALUE_LEFT, commandIndex++));
        }
        for(int i = 0; i < (size - 1); i++) {
            sequence.add(new Instruction(R.id.buttonDraw, -1, commandIndex++));
            sequence.add(new Instruction(R.id.buttonStepDown, BluetoothCommands.VALUE_DOWN, commandIndex++));
        }

        return new Sequence(sequence);
    }

    public static Sequence buildZigZag(int steps, int stepSize, int commandIndex) {
        List<Instruction> sequence = new ArrayList<Instruction>();

        sequence.add(new Instruction(R.id.buttonDraw, -1, commandIndex++));
        for(int i = 0; i < steps; i++) {
            for(int j = 0; j < (stepSize - 1); j++) {
                sequence.add(new Instruction(R.id.buttonStepUp, BluetoothCommands.VALUE_UP, commandIndex++));
                sequence.add(new Instruction(R.id.buttonDraw, -1, commandIndex++));
            }
            for(int j = 0; j < (stepSize - 1); j++) {
                sequence.add(new Instruction(R.id.buttonStepLeft, BluetoothCommands.VALUE_LEFT, commandIndex++));
                sequence.add(new Instruction(R.id.buttonDraw, -1, commandIndex++));
            }
        }

        sequence.add(new Instruction(R.id.buttonStepUp, BluetoothCommands.VALUE_UP * 4, commandIndex++));
        sequence.add(new Instruction(R.id.buttonStepRight, BluetoothCommands.VALUE_RIGHT * 4, commandIndex++));

        for(int i = 0; i < steps; i++) {
            for(int j = 0; j < (stepSize - 1); j++) {
                sequence.add(new Instruction(R.id.buttonDraw, -1, commandIndex++));
                sequence.add(new Instruction(R.id.buttonStepRight, BluetoothCommands.VALUE_RIGHT, commandIndex++));
            }
            for(int j = 0; j < (stepSize - 1); j++) {
                sequence.add(new Instruction(R.id.buttonDraw, -1, commandIndex++));
                sequence.add(new Instruction(R.id.buttonStepDown, BluetoothCommands.VALUE_DOWN, commandIndex++));
            }
        }

        return new Sequence(sequence);
    }

    public static Sequence buildCheckerboard(int size, int commandIndex) {
        List<Instruction> sequence = new ArrayList<Instruction>();
        int position = 0;
        int direction = R.id.buttonStepRight;


        for(int i = 0; i < Math.ceil(((double)size*size)/2); i++) {
            if(position >= 0 && position < size) {
                sequence.add(new Instruction(R.id.buttonDraw, -1, commandIndex++));
            }

            if(position < 0 || position >= size-1) {
                sequence.add(new Instruction(R.id.buttonStepUp, BluetoothCommands.VALUE_UP, commandIndex++));
                direction = (direction == R.id.buttonStepRight) ? R.id.buttonStepLeft : R.id.buttonStepRight;
                sequence.add(new Instruction(direction, BluetoothCommands.VALUE_RIGHT, commandIndex++));
                position = (direction == R.id.buttonStepRight) ? position+1 : position-1;
            }
            else {
                sequence.add(new Instruction(direction, BluetoothCommands.VALUE_RIGHT * 2, commandIndex++));
                position = (direction == R.id.buttonStepRight) ? position+2 : position-2;
            }
        }

        return new Sequence(sequence);
    }
}
