package com.nikolay.plottercontroller.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.nikolay.plottercontroller.Instruction;
import com.nikolay.plottercontroller.R;
import com.nikolay.plottercontroller.Sequence;
import com.nikolay.plottercontroller.bluetooth.BluetoothCommands;

import java.util.ArrayList;
import java.util.Collections;
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
                sequence.add(new Instruction(BluetoothCommands.COMMAND_UP, 1));
                sequence.add(new Instruction(BluetoothCommands.COMMAND_LEFT, (width - 1)));
            }
            else {
                sequence.add(new Instruction(BluetoothCommands.COMMAND_RIGHT, 1));
            }
        }

        // combine white pixels into one long movement
        List<Instruction> sequence2 = new ArrayList<Instruction>();
        int n = 0;
        for(int i = 0; i < sequence.size(); i++) {
            Instruction instruction = sequence.get(i);
            if(instruction.getCommandId() == BluetoothCommands.COMMAND_RIGHT) {
                while(sequence.get(i + n).getCommandId() == BluetoothCommands.COMMAND_RIGHT) {
                    n++;
                    if((i + n) >= sequence.size()) break;
                }
                sequence2.add(new Instruction(BluetoothCommands.COMMAND_RIGHT, n));
                i += (n - 1);
                n = 0;
            }
            else {
                sequence2.add(instruction);
            }
        }

        // make zig-zag movement
        sequence.clear();
        for(int i = 0; i < height; i++) {
            int index = sequence2.indexOf(new Instruction(BluetoothCommands.COMMAND_LEFT, (width - 1))) + 1;
            List<Instruction> row = sequence2.subList(0, index);

            if(i % 2 == 0) {
                //row.remove(row.size() - 1);
                sequence.addAll(row.subList(0, row.size() - 1));
            }
            else {
                Collections.reverse(row);
                for(int j = 2; j < row.size(); j++) {
                    Instruction instr = row.get(j);
                    if(instr.getCommandId() == BluetoothCommands.COMMAND_RIGHT) {
                        instr.setCommandId(BluetoothCommands.COMMAND_LEFT);
                    }
                    sequence.add(instr);
                }
                sequence.add(new Instruction(BluetoothCommands.COMMAND_UP, 1));
            }

            sequence2.subList(0, index).clear();
        }

        return new Sequence(sequence);
    }
}
