package com.xuwd.jscan.JCamera;

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
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.Semaphore;

public class JCamera {
    private TextureView mTextureView;
    private Activity mActivity;
    //private HandlerThread mBackgroundThread;
    //private Handler mBackgroundHandler;
    public JCamera(TextureView textureView, Activity activity){
        this.mTextureView=textureView;
        this.mActivity=activity;
    }

    @SuppressLint("MissingPermission")
    //1：设置参数、启动相机
    public void start() {
        CameraManager manager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];

            // Choose the sizes for camera preview and video recording
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            //mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            if (map == null) {
                throw new RuntimeException("Cannot get available preview/video sizes");
            }
           //mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
           // mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),mTextureWidth,mTextureHeight, mVideoSize);

//            int orientation = getResources().getConfiguration().orientation;
/*            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            } else {
                mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            }
            configureTransform(mTextureWidth,mTextureHeight);
*/
//            manager.openCamera(cameraId, cameraStateCallback, mBackgroundHandler);
            //流程抛给回调函数cameraStateCallback
            manager.openCamera(cameraId, cameraStateCallback, null);
        }   catch (CameraAccessException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.");
        }
    }

    //********************************* CameraDevice *****************************************//
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest.Builder mPreviewRequestBuilder;

    private CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;  //[KeyJoint]:mCameraDevice的句柄
            mCameraOpenCloseLock.release();

            //configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
            //此时配置Recorder应该合适
            //setUpMediaRecorder();

            //2：建立相机与画面的通道，并创建对话机制, 待成熟后启动对话，流程抛给回调函数sessionStateCallback
            try {
                SurfaceTexture texture = mTextureView.getSurfaceTexture();
                assert texture != null;
                //texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                Surface textureSurface = new Surface(texture);
//                Surface imageSurface = mImageReader.getSurface();

                //这个动作不是建立会话的必要，但要生成并关联两个Surface，所以顺便addTarget
                mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                mPreviewRequestBuilder.addTarget(textureSurface);

                //创建相机设备的会话，抛出去，待其回调函数sessionStateCallback冒泡
                mCameraDevice.createCaptureSession(Arrays.asList(textureSurface),sessionStateCallback, null);
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
            Activity activity = mActivity;
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
            //3：会话机制ok,启动会话“浏览”；
            updatePreview();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Activity activity = mActivity;
            if (null != activity) {
                Toast.makeText(activity, "CaptureSession Failed", Toast.LENGTH_SHORT).show();
            }
        }
    };

    //*****************通过Session启动各种功能****************************//
    //4：“浏览”会话具体内容。浏览成功，意味着相机的数据会被投向画面TextureView
    private void updatePreview() {
        try {
            //投射通道在之前已经顺便建立： mPreviewRequestBuilder.addTarget(textureSurface);
            // Auto focus & flash should be continuous for camera preview.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

            //启动预览请求----浏览无事可处理，不需要设置回调函数---------------！！！！
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null,null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void close() {
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
