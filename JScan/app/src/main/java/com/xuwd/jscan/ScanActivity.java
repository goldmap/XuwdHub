package com.xuwd.jscan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.concurrent.Semaphore;

public class ScanActivity extends AppCompatActivity {
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private CameraCaptureSession mPreviewSession;
    private TextView mVideoView;
    private Size mPreviewSize;
    private Size mVideoSize;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

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

            //[KeyJoint]
            igniteCamera();
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
            /*mBmp= mTextureView.getBitmap();
            if(mBmp!=null)
                mImageView.setImageBitmap(mBmp);*/
        }

    };

    @SuppressLint("MissingPermission")
    private void igniteCamera() {
        final Activity activity = this;
        if (null == activity || activity.isFinishing()) {
            return;
        }

        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];

            // Choose the sizes for camera preview and video recording
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            //mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            if (map == null) {
                throw new RuntimeException("Cannot get available preview/video sizes");
            }
           // mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
           // mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),mTextureWidth,mTextureHeight, mVideoSize);

//            int orientation = getResources().getConfiguration().orientation;
/*            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            } else {
                mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            }
            configureTransform(mTextureWidth,mTextureHeight);
*/
            manager.openCamera(cameraId, cameraStateCallback, mBackgroundHandler);
        }   catch (CameraAccessException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.");
        }
    }

    private TextureView.SurfaceTextureListener videoSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {

        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    };


    //********************************* CameraDevice *****************************************//
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private Boolean mPreviewing=false;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CameraCharacteristics mCharacteristics;
    private CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;  //[KeyJoint]:mCameraDevice的句柄
            mCameraOpenCloseLock.release();

            //configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
            //此时配置Recorder应该合适
            //setUpMediaRecorder();

            //创建相机的对话机制, 创建会话（设置会话的回调函数），通过会话启动“会话请求”Request
            try {
                SurfaceTexture texture = mTextureView.getSurfaceTexture();
                assert texture != null;
                //texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                Surface textureSurface = new Surface(texture);
//                Surface imageSurface = mImageReader.getSurface();

                //这个动作不是建立会话的必要，但要生成并关联两个Surface，所以顺便addTarget
                mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                mPreviewRequestBuilder.addTarget(textureSurface);

                //创建相机设备的会话，抛出去，待其回调函数出声
                mCameraDevice.createCaptureSession(Arrays.asList(textureSurface),
                        sessionStateCallback, mBackgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getParent();
            if (null != activity) {
                activity.finish();
            }
        }
    };

    //**************************** CameraCapture.Session ****************************//
    CameraCaptureSession.StateCallback sessionStateCallback =  new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            mCaptureSession = session;////[KeyJoint]:mCaptureSession的句柄
            //会话OK，启动“浏览”；
            updatePreview();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Activity activity = getParent();
            if (null != activity) {
                Toast.makeText(activity, "CaptureSession Failed", Toast.LENGTH_SHORT).show();
            }
        }
    };

    //*****************通过Session启动各种功能****************************//
    private void updatePreview() {
        try {
            //投射通道在之前已经顺便建立： mPreviewRequestBuilder.addTarget(textureSurface);
            // Auto focus & flash should be continuous for camera preview.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

            //启动预览请求----浏览无事可处理，不需要设置回调函数---------------！！！！
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null,mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }

        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

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
}
