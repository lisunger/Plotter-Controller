package com.nikolay.plottercontroller.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.nikolay.plottercontroller.Instruction;
import com.nikolay.plottercontroller.InstructionDispatcher;
import com.nikolay.plottercontroller.R;
import com.nikolay.plottercontroller.Sequence;
import com.nikolay.plottercontroller.bluetooth.BluetoothCommands;

import java.util.ArrayList;
import java.util.List;

public class Dither {

    public static Sequence getSequenceFromImage(Context context, int imageId, int commandIndex) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.fox, options);
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
                sequence.add(new Instruction(InstructionDispatcher.getCommand(R.id.buttonDraw), -1, commandIndex++));
            }
            if((i % width) == (width - 1)) { // last pixel on the row
                sequence.add(new Instruction(InstructionDispatcher.getCommand(R.id.buttonStepUp), BluetoothCommands.VALUE_UP, commandIndex++));
                sequence.add(new Instruction(InstructionDispatcher.getCommand(R.id.buttonStepLeft), (width - 1) * BluetoothCommands.VALUE_LEFT, commandIndex++));
            }
            else {
                sequence.add(new Instruction(InstructionDispatcher.getCommand(R.id.buttonStepRight), BluetoothCommands.VALUE_RIGHT, commandIndex++));
            }
        }
        return new Sequence(sequence);
    }
}
