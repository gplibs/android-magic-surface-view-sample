package com.gplibs.magic_surface_view_sample.common;

public class RandomNumber {

    float mMin;
    float mMax;
    float[] mNumbers;

    public RandomNumber(int count, float min, float max) {
        mNumbers = new float[count];
        mMin = min;
        mMax = max;
    }

    public float get(int index) {
        if (mNumbers[index] == 0) {
            mNumbers[index] = mMin + (float) Math.random() * (mMax - mMin);
        }
        return mNumbers[index];
    }
}
