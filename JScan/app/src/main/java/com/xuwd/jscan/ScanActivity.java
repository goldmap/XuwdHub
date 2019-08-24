package com.xuwd.jscan;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.SurfaceTexture;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.TextureView;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.xuwd.jscan.JCamera.JCamera;
import java.util.TimerTask;
import java.util.Vector;

public class ScanActivity extends AppCompatActivity {

    private ScanActivity mActivity;
    public ScanActivityHandler mScanActivityHandler;
    private DecodeThread mDecodeThread;
    private DecodeHandler mDecodeHandler;

    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        mActivity= this;
        mTextureView=findViewById(R.id.capture_preview);
        mTextureView.setSurfaceTextureListener(surfaceTextureListener);

        TranslateAnimation animation=new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation
                .RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                0.9f);
        animation.setDuration(4500);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.RESTART);
        ImageView scanLine = findViewById(R.id.capture_scan_line);
        scanLine.startAnimation(animation);

       mScanActivityHandler=new ScanActivityHandler(this,decodeFormats,characterSet);

       mDecodeThread=new DecodeThread(this,decodeFormats,characterSet);
       mDecodeThread.start();
       mDecodeHandler =mDecodeThread.getHandler();
    }

    public ScanActivityHandler getHandler(){
        return mScanActivityHandler;
    }

    @Override
    public void onResume() {
        super.onResume();
        decodeFormats = null;
        characterSet = null;
        //startBackgroundThread();
    }

    @Override
    public void onPause() {
        super.onPause();
        //closeCamera();
        //stopBackgroundThread();
        if(mDecodeHandler !=null){
            //mDecodeHandler.quitSynchronously();
            mDecodeHandler =null;
        }
    }

    //**************************** TextureView ****************************//
    private TextureView mTextureView;
    private int mTextureWidth,mTextureHeight;
    private TextureView.SurfaceTextureListener surfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                              int width, int height) {
            mTextureWidth =width;
            mTextureHeight =height;
            Log.d("AAA", "onSurfaceTextureAvailable:(mTextureWidth,mTextureHeight) "+mTextureWidth+","+mTextureHeight);
            //[KeyJoint]
            JCamera jCamera=new JCamera(mTextureView,mActivity);
            jCamera.start();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                                                int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            Message msg= mDecodeHandler.obtainMessage();
            msg.obj=mTextureView.getBitmap();
            msg.what= R.id.decode;
            msg.arg1=mTextureWidth;
            msg.arg2=mTextureHeight;
            mDecodeHandler.sendMessage(msg);
        }

    };

    @SuppressLint("MissingPermission")
    /*
    private void startBackgroundThread() {
       // mBackgroundThread = new HandlerThread("CameraBackground");
        //mBackgroundThread.start();
        //mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    */
    /*
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    */
    TimerTask timerTask=new TimerTask() {
        @Override
        public void run() {
        }
    };
}
