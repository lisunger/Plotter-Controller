package com.nikolay.plottercontroller.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.nikolay.plottercontroller.Instruction;
import com.nikolay.plottercontroller.R;
import com.nikolay.plottercontroller.Sequence;
import com.nikolay.plottercontroller.bluetooth.BluetoothCommands;

import java.util.ArrayList;
import java.util.List;

public class Dither {

    public static Sequence getSequenceFromImage(Context context, int imageId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imageId, options);
        //Log.d("Lisko", String.format("%d x %d", bitmap.getWidth(), bitmap.getHeight()));

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        List<Instruction> sequence = new ArrayList<Instruction>();
        // -16777216 = black; -1 = white
        for(int i = 0; i < width * height; i++) {
            int pixel = pixels[i];
            if(pixel == -16777216) {
                sequence.add(new Instruction(BluetoothCommands.COMMAND_DOT, -1));
            }
            if((i % width) == (width - 1)) { // last pixel on the row
                sequence.add(new Instruction(BluetoothCommands.COMMAND_UP, BluetoothCommands.VALUE_UP));
                sequence.add(new Instruction(BluetoothCommands.COMMAND_LEFT, (width - 1) * BluetoothCommands.VALUE_LEFT));
            }
            else {
                sequence.add(new Instruction(BluetoothCommands.COMMAND_RIGHT, BluetoothCommands.VALUE_RIGHT));
            }
        }

        List<Instruction> finalSequence = new ArrayList<Instruction>();
        int n = 0;
        for(int i = 0; i < sequence.size(); i++) {
            Instruction instruction = sequence.get(i);
            if(instruction.getCommandId() == BluetoothCommands.COMMAND_RIGHT) {
                while(sequence.get(i + n).getCommandId() == BluetoothCommands.COMMAND_RIGHT) {
                    n++;
                    if((i + n) >= sequence.size()) break;
                }
                finalSequence.add(new Instruction(BluetoothCommands.COMMAND_RIGHT, BluetoothCommands.VALUE_RIGHT * n));
                i += (n - 1);
                n = 0;
            }
            else {
                finalSequence.add(instruction);
            }
        }

        return new Sequence(finalSequence);
    }
}
