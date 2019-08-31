package com.xuwd.jimagetest;

public class NativeLibrary {
    static {
        System.loadLibrary("native_lib");
    }
    public static native void yuv420pToRGBA(byte[] yuv420p,int width,int height,byte[] rgba);
}
