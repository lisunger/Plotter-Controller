package com.nikolay.plottercontroller;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Sequence implements Parcelable {

    private List<Instruction> instructions;

    public Sequence(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public Sequence(Parcel p) {
        this.instructions = p.readArrayList(Instruction.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.instructions);
    }

    public static final Parcelable.Creator<Sequence> CREATOR = new Parcelable.Creator<Sequence>() {

        @Override
        public Sequence createFromParcel(Parcel source) {
            return new Sequence(source);
        }

        @Override
        public Sequence[] newArray(int size) {
            return new Sequence[size];
        }
    };

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
