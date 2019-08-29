package com.xuwd.jimagetest.JCamera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Semaphore;

import static com.xuwd.jimagetest.JCamera.Camera2Utils.chooseOptimalSize;
import static com.xuwd.jimagetest.JCamera.Camera2Utils.chooseVideoSize;


public class JCamera {
    private static final int CAMERA_MSG_ERROR            = 0x001;
    private static final int CAMERA_MSG_SHUTTER          = 0x002;
    private static final int CAMERA_MSG_FOCUS            = 0x004;
    private static final int CAMERA_MSG_ZOOM             = 0x008;
    private static final int CAMERA_MSG_PREVIEW_FRAME    = 0x010;
    private static final int CAMERA_MSG_VIDEO_FRAME      = 0x020;
    private static final int CAMERA_MSG_POSTVIEW_FRAME   = 0x040;
    private static final int CAMERA_MSG_RAW_IMAGE        = 0x080;
    private static final int CAMERA_MSG_COMPRESSED_IMAGE = 0x100;
    private static final int CAMERA_MSG_RAW_IMAGE_NOTIFY = 0x200;
    private static final int CAMERA_MSG_PREVIEW_METADATA = 0x400;
    private static final int CAMERA_MSG_FOCUS_MOVE       = 0x800;

    private EventHandler mEventHandler;
    private PreviewCallback mPreviewCallback;

    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_LOCK = 1;
    private static final int STATE_WAITING_PRECAPTURE = 2;
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;
    private static final int STATE_PICTURE_TAKEN = 4;
    private int mState = STATE_PREVIEW;
    private AutoFitTextureView mTextureView;
    private Activity mActivity;
    private Bitmap mBmp;
    //private UpdateImage updateImage=null;

    private Size mPreviewSize;
    private Size mVideoSize;
    private int mSensorOrientation;
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    //private HandlerThread mBackgroundThread;
    //private Handler mBackgroundHandler;
    public JCamera(AutoFitTextureView textureView, Activity activity){
        mPreviewCallback=null;

        this.mTextureView=textureView;
        this.mActivity=activity;
    }

    private void initHandler(){
        Looper looper=Looper.myLooper();
        if(looper==null){
            looper=Looper.getMainLooper();
        }

        if(looper!=null){
            mEventHandler=new EventHandler(looper);
        }else{
            mEventHandler=null;
        }
    }

    public void setPreviewCallback(PreviewCallback previewCallback){
        this.mPreviewCallback=previewCallback;
    }

    @SuppressLint("MissingPermission")
    //1：设置参数、启动相机
    public void start() {
        int width=mTextureView.getWidth();
        int height=mTextureView.getHeight();
        initHandler();
        CameraManager manager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];

            // Choose the sizes for camera preview and video recording
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                throw new RuntimeException("Cannot get available preview/video sizes");
            }

            int[] imageFt={ImageFormat.JPEG,ImageFormat.YUV_420_888,ImageFormat.YV12};
            int testTp=1;

            Size maxSize = Collections.max(Arrays.asList(map.getOutputSizes(imageFt[testTp])), new CompareSizeByArea());

            mImageReader = ImageReader.newInstance(maxSize.getWidth(), maxSize.getHeight(),imageFt[testTp], 2);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener,null);

            mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),width, height, mVideoSize);

            //获取到屏幕的旋转角度，进一步判断是否，需要交换维度来获取预览大小
            int orientation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            } else {
                mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            }
            configureTransform(width, height);
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
                Surface textureSurface = new Surface(texture);
                Surface imageSurface = mImageReader.getSurface();

                //这个动作不是建立会话的必要，但要生成并关联两个Surface，所以顺便addTarget
                mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                mPreviewRequestBuilder.addTarget(textureSurface);

                //创建相机设备的会话，抛出去，待其回调函数sessionStateCallback冒泡
                mCameraDevice.createCaptureSession(Arrays.asList(textureSurface,imageSurface),sessionStateCallback, null);
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

    //*****************通过Session启动各种功能，在其回调函数中跟踪**********************//
    public void lockFocus(){
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), captureRequestCallback,null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unlockFocus() {
        updatePreview();
    }

    //用真机调试通过，afState=5
    private CameraCaptureSession.CaptureCallback captureRequestCallback
            = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,@NonNull CaptureRequest request,@NonNull CaptureResult partialResult) {
//            process(partialResult);
        }
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,@NonNull CaptureRequest request,@NonNull TotalCaptureResult result) {
            switch (mState) {
                case STATE_WAITING_LOCK: //简单可理解为待聚焦成功后调用capture
                {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    //Toast.makeText(getContext(),"af/aeState:"+afState+"/"+aeState,Toast.LENGTH_SHORT).show();

                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN == afState) {
                        // CONTROL_AE_STATE can be null on some devices
//                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            //runPrecaptureSequence();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
            }
        }
    };

    public void captureStillPicture() {
        try {
            // capturePictureBuilder is the CaptureRequest.Builder that we use to take a picture.
            CaptureRequest.Builder capturePictureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            capturePictureBuilder.addTarget(mImageReader.getSurface());

            // Use the same AE and AF modes as the preview.
            capturePictureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            capturePictureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            //******************自用回调**********************/
            CameraCaptureSession.CaptureCallback capturePictureCallback
                    = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,@NonNull TotalCaptureResult result) {
                    unlockFocus();
                }
            };

            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();
            mCaptureSession.capture(capturePictureBuilder.build(), capturePictureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.d("AAA", "captureStillPicture: ok");
    }

    private ImageReader mImageReader;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireNextImage();
            int pixelStride,rowStride,rowPadding,width,height;
            ByteBuffer buffer;
            width = image.getWidth();
            height = image.getHeight();

            byte[] bytes=null;
            int ff=0;
            //设置mImageReader = ImageReader.newInstance(imgWidth, imgHeight, ImageFormat.JPEG, 2);OK!
            switch(image.getFormat()){
                case ImageFormat.JPEG:
                    ff=1;
                    Image.Plane[] planes = image.getPlanes();
                    buffer = planes[0].getBuffer();
                    buffer.rewind();
                    bytes=new byte[buffer.remaining()];
                    buffer.get(bytes);
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    mBmp=bmp;
                    break;
                case ImageFormat.YUV_420_888:
                case ImageFormat.YUV_422_888:
                    ff=2;
                    //bytes = ImageUtil.getBytesFromImage(image,ImageUtil.YUV420SP);
                    bytes = ImageUtil.getBytesFromImage(image,1);
                    //int rgb[]=ImageUtil.decodeYUVtoRGB(bytes, width, height);
                    //Bitmap cmp = Bitmap.createBitmap(rgb,0,width,width,height, Bitmap.Config.ARGB_8888);
                    //mBmp=cmp;
                    break;
                case ImageFormat.YV12:
                    ff=3;
                    bytes = ImageUtil.getBytesFromImage(image,ImageUtil.YV12);
                    //int rgb[]=ImageUtil.decodeYUVtoRGB(bytes, width, height);
                    //Bitmap cmp = Bitmap.createBitmap(rgb,0,width,width,height, Bitmap.Config.ARGB_8888);
                    //mBmp=cmp;
                    break;
                default:
                    ff=3;
                    break;
            }
            if(bytes!=null){
                //updateImage(bytes);
            }
            image.close();
            Log.d("AAA", "mOnImageAvailableListener: ok");

            if(mPreviewCallback!=null){
                mPreviewCallback.onPreviewFrame(bytes,ff,width,height);
            }
        }

    };

    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = mActivity;
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }
    public interface PreviewCallback
    {
        void onPreviewFrame(byte[] data,int ff,int width,int height);
    };

    private class EventHandler extends Handler {

        public EventHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case CAMERA_MSG_PREVIEW_FRAME:

                    return;
            }
        }
    }
}