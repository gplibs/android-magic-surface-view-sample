package com.gplibs.magic_surface_view_sample.common;

public class Direction {
    public final static int LEFT = 0;
    public final static int TOP = 1;
    public final static int RIGHT = 2;
    public final static int BOTTOM = 3;

    public static boolean isVertical(int direction) {
        return direction == TOP || direction == BOTTOM;
    }
}
