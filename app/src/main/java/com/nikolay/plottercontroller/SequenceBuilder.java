package com.nikolay.plottercontroller;

import com.nikolay.plottercontroller.bluetooth.BluetoothCommands;

import java.util.ArrayList;
import java.util.List;

public class SequenceBuilder {

    public static Sequence buildSquare(int size, int commandIndex) {
        List<Instruction> sequence = new ArrayList<Instruction>();

        for(int i = 0; i < (size - 1); i++) {
            sequence.add(new Instruction(BluetoothCommands.COMMAND_DOT, -1));
            sequence.add(new Instruction(BluetoothCommands.COMMAND_RIGHT, BluetoothCommands.VALUE_RIGHT));
        }
        for(int i = 0; i < (size - 1); i++) {
            sequence.add(new Instruction(BluetoothCommands.COMMAND_DOT, -1));
            sequence.add(new Instruction(BluetoothCommands.COMMAND_UP, BluetoothCommands.VALUE_UP));
        }
        for(int i = 0; i < (size - 1); i++) {
            sequence.add(new Instruction(BluetoothCommands.COMMAND_DOT, -1));
            sequence.add(new Instruction(BluetoothCommands.COMMAND_LEFT, BluetoothCommands.VALUE_LEFT));
        }
        for(int i = 0; i < (size - 1); i++) {
            sequence.add(new Instruction(BluetoothCommands.COMMAND_DOT, -1));
            sequence.add(new Instruction(BluetoothCommands.COMMAND_DOWN, BluetoothCommands.VALUE_DOWN));
        }

        return new Sequence(sequence);
    }

    public static Sequence buildZigZag(int steps, int stepSize, int commandIndex) {
        List<Instruction> sequence = new ArrayList<Instruction>();

        sequence.add(new Instruction(BluetoothCommands.COMMAND_DOT, -1));
        for(int i = 0; i < steps; i++) {
            for(int j = 0; j < (stepSize - 1); j++) {
                sequence.add(new Instruction(BluetoothCommands.COMMAND_UP, BluetoothCommands.VALUE_UP));
                sequence.add(new Instruction(BluetoothCommands.COMMAND_DOT, -1));
            }
            for(int j = 0; j < (stepSize - 1); j++) {
                sequence.add(new Instruction(BluetoothCommands.COMMAND_LEFT, BluetoothCommands.VALUE_LEFT));
                sequence.add(new Instruction(BluetoothCommands.COMMAND_DOT, -1));
            }
        }

        sequence.add(new Instruction(BluetoothCommands.COMMAND_UP, BluetoothCommands.VALUE_UP * 4));
        sequence.add(new Instruction(BluetoothCommands.COMMAND_RIGHT, BluetoothCommands.VALUE_RIGHT * 4));

        for(int i = 0; i < steps; i++) {
            for(int j = 0; j < (stepSize - 1); j++) {
                sequence.add(new Instruction(BluetoothCommands.COMMAND_DOT, -1));
                sequence.add(new Instruction(BluetoothCommands.COMMAND_RIGHT, BluetoothCommands.VALUE_RIGHT));
            }
            for(int j = 0; j < (stepSize - 1); j++) {
                sequence.add(new Instruction(BluetoothCommands.COMMAND_DOT, -1));
                sequence.add(new Instruction(BluetoothCommands.COMMAND_DOWN, BluetoothCommands.VALUE_DOWN));
            }
        }

        return new Sequence(sequence);
    }

    public static Sequence buildCheckerboard(int size, int commandIndex) {
        List<Instruction> sequence = new ArrayList<Instruction>();
        int position = 0;
        int direction = BluetoothCommands.COMMAND_RIGHT;


        for(int i = 0; i < Math.ceil(((double)size*size)/2); i++) {
            if(position >= 0 && position < size) {
                sequence.add(new Instruction(BluetoothCommands.COMMAND_DOT, -1));
            }

            if(position < 0 || position >= size-1) {
                sequence.add(new Instruction(BluetoothCommands.COMMAND_UP, BluetoothCommands.VALUE_UP));
                direction = (direction == BluetoothCommands.COMMAND_RIGHT) ? BluetoothCommands.COMMAND_LEFT : BluetoothCommands.COMMAND_RIGHT;
                sequence.add(new Instruction(direction, BluetoothCommands.VALUE_RIGHT));
                position = (direction == BluetoothCommands.COMMAND_RIGHT) ? position+1 : position-1;
            }
            else {
                sequence.add(new Instruction(direction, BluetoothCommands.VALUE_RIGHT * 2));
                position = (direction == BluetoothCommands.COMMAND_RIGHT) ? position+2 : position-2;
            }
        }

        return new Sequence(sequence);
    }

    public static Sequence buildLineDown(int size, int commandIndex) {
        List<Instruction> sequence = new ArrayList<Instruction>();

        for(int i = 0; i < size; i++) {
            sequence.add(new Instruction(BluetoothCommands.COMMAND_DOT, -1));
            sequence.add(new Instruction(BluetoothCommands.COMMAND_UP, BluetoothCommands.VALUE_UP));
        }

        return new Sequence(sequence);
    }
}
