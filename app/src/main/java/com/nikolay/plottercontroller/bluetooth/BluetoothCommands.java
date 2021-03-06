package com.nikolay.plottercontroller.bluetooth;

public class BluetoothCommands {
    public static final int COMMAND_UP =    1;
    public static final int COMMAND_DOWN =  2;
    public static final int COMMAND_LEFT =  3;
    public static final int COMMAND_RIGHT = 4;
    public static final int COMMAND_DOT =   5;
    public static final int COMMAND_STOP =  6;

    public static final int VALUE_UP =      1; // 1 degree rotation is 5.68 steps
    public static final int VALUE_DOWN =    1; // 1 degree rotation is 5.68 steps
    public static final int VALUE_LEFT =    1; // used with quarter steps
    public static final int VALUE_RIGHT =   1; // user with quarter steps

    public static final int ROTATION_BYJ =  75;
    public static final int ROTATION_NEMA = 75;
}
