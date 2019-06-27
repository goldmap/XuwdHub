package com.xuwd.jlive;

import android.content.Context;

public class JFFmpeg {
    private JFFmpeg() {
    }

    private static class SingletonInstance {
        private static final JFFmpeg INSTANCE = new JFFmpeg();
    }

    public static JFFmpeg getInstance() {
        return SingletonInstance.INSTANCE;
    }

    static {
        System.loadLibrary("avutil");
//        System.loadLibrary("swresample");
        System.loadLibrary("avcodec");
        System.loadLibrary("avformat");
//        System.loadLibrary("swscale");
        System.loadLibrary("avfilter");
//        System.loadLibrary("avdevice");
        System.loadLibrary("JffmpegLib");
    }

    public native String getFFmpegInfo();
    public native int init(String url);
    public native int pushCameraData(byte[] buffer,int ylen,byte[] ubuffer,int ulen,byte[] vbuffer,int vlen);
    public native int close();
}
