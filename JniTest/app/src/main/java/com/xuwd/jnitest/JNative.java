package com.xuwd.jnitest;

import java.lang.annotation.Native;

public class JNative {
    static {
        System.loadLibrary("xuwd_lib");
    }
    public native String getStrFromJNI();
    public native String ffmpegInfo(String outUrl);

}
