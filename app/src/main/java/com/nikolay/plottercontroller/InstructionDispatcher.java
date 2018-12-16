package com.nikolay.plottercontroller;

import android.util.Log;

import com.nikolay.plottercontroller.bluetooth.BluetoothCommands;
import com.nikolay.plottercontroller.services.StartConnectionService;

public class InstructionDispatcher {

    /**
     *
     * @param buttonId the Id of the button clicked
     * @param value send -1 to use default value
     * @param instructionIndex
     */
    public static boolean sendInstruction(int buttonId, int value, int instructionIndex) {
        int command = getCommand(buttonId);
        if(value == -1) {
            value = getValue(buttonId);
        }
        //Log.d("Lisko", value + "");
        //Log.d("Lisko", command + "");
        //Log.d("Lisko", instructionIndex + "");

        return StartConnectionService.sendInstruction(command, value, instructionIndex);
    }

    public static int getCommand(int buttonId) {
        switch(buttonId) {
            case R.id.buttonStepDown :
            case R.id.buttonRevDown : {
                return BluetoothCommands.COMMAND_DOWN;
            }
            case R.id.buttonStepUp :
            case R.id.buttonRevUp : {
                return BluetoothCommands.COMMAND_UP;
            }
            case R.id.buttonStepLeft :
            case R.id.buttonRevLeft : {
                return BluetoothCommands.COMMAND_LEFT;
            }
            case R.id.buttonStepRight :
            case R.id.buttonRevRight : {
                return BluetoothCommands.COMMAND_RIGHT;
            }
            case R.id.buttonDraw : {
                return BluetoothCommands.COMMAND_DOT;
            }
//            case R.id.buttonStop : {
//                return BluetoothCommands.COMMAND_STOP;
//            }
            default : {
                return -1;
            }
        }
    }

    private static int getValue(int buttonId) {
        switch(buttonId) {
            case R.id.buttonStepDown : {
                return BluetoothCommands.VALUE_DOWN;
            }
            case R.id.buttonRevDown : {
                return BluetoothCommands.ROTATION_BYJ;
            }
            case R.id.buttonStepUp : {
                return BluetoothCommands.VALUE_UP;
            }
            case R.id.buttonRevUp : {
                return BluetoothCommands.ROTATION_BYJ;
            }
            case R.id.buttonStepLeft : {
                return BluetoothCommands.VALUE_LEFT;
            }
            case R.id.buttonRevLeft : {
                return BluetoothCommands.ROTATION_NEMA;
            }
            case R.id.buttonStepRight : {
                return BluetoothCommands.VALUE_RIGHT;
            }
            case R.id.buttonRevRight : {
                return BluetoothCommands.ROTATION_NEMA;
            }
            case R.id.buttonDraw : {
                return 0;
            }
//            case R.id.buttonStop : {
//                return 0;
//            }
            default : {
                return 0;
            }
        }
    }
}
