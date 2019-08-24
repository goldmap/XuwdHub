package com.xuwd.jscan.JCamera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
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
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.Semaphore;

public class JCamera {
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_LOCK = 1;
    private static final int STATE_WAITING_PRECAPTURE = 2;
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;
    private static final int STATE_PICTURE_TAKEN = 4;
    private int mState = STATE_PREVIEW;
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
            if (map == null) {
                throw new RuntimeException("Cannot get available preview/video sizes");
            }

            Size[] yuvImageSize=map.getOutputSizes(ImageFormat.YUV_420_888);
            int yuvIamgeWidth = yuvImageSize[0].getWidth();
            int yuvIamgeHeight = yuvImageSize[0].getHeight();
            mImageReader = ImageReader.newInstance(yuvIamgeWidth, yuvIamgeHeight, ImageFormat.YUV_420_888, 2);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener);

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
    TimerTask timerTask=new TimerTask() {
        @Override
        public void run() {
        }
    };


    //*****************通过Session启动各种功能****************************//
    private void lockFocus(){
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), captureRequestCallback,mBackgroundHandler);
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
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
//            process(partialResult);
        }
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
//            Toast.makeText(getContext(),"mState:"+mState,Toast.LENGTH_SHORT).show();
            if(mState!=0)
                process(result);
        }

        private void process(CaptureResult result) {
//            Toast.makeText(getContext(),"mState"+mState,Toast.LENGTH_SHORT).show();
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
                            runPrecaptureSequence();
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

    private void captureStillPicture() {
        try {
            // capturePictureBuilder is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder capturePictureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            capturePictureBuilder.addTarget(mImageReader.getSurface());

            // Use the same AE and AF modes as the preview.
            capturePictureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            capturePictureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            //******************自用回调**********************/
            CameraCaptureSession.CaptureCallback capturePictureCallback
                    = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    unlockFocus();
                }
            };

            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();
            mCaptureSession.capture(capturePictureBuilder.build(), capturePictureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private ImageReader mImageReader;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            //子线程抛出，是对的
            //         mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
            Image image = reader.acquireNextImage();

            int pixelStride,rowStride,rowPadding,width,height;
            ByteBuffer buffer;
            width = image.getWidth();
            height = image.getHeight();

            //设置mImageReader = ImageReader.newInstance(imgWidth, imgHeight, ImageFormat.JPEG, 2);OK!
            if(image.getFormat()== ImageFormat.JPEG){
                Image.Plane[] planes = image.getPlanes();
                buffer = planes[0].getBuffer();
                buffer.rewind();
                byte[] bytes=new byte[buffer.remaining()];
                buffer.get(bytes);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                mBmp=bmp;
            }//mImageReader = ImageReader.newInstance(imgWidth, imgHeight, ImageFormat.YUV_420_888, 2)
            else if(image.getFormat()==ImageFormat.YUV_420_888){
                byte[] yuvBytes = ImageUtil.getBytesFromImage(image,ImageUtil.YUV420P);
                //yuvBytes=rotateGraph(yuvBytes,width,height);
             /*这个机制失败，有待分析
                YuvImage yuvImage = new YuvImage(yuvBytes,ImageFormat.YUV_420_888,width,height,null);
                if(yuvImage!=null){
                    ByteArrayOutputStream stream=new ByteArrayOutputStream();
                    yuvImage.compressToJpeg(new Rect(0,0,width,height),80,stream);
                    Bitmap cmp =BitmapFactory.decodeByteArray(stream.toByteArray(),0,stream.size());
                    mBmp=cmp;
                }
                */
                int rgb[]=ImageUtil.decodeYUVtoRGB(yuvBytes, width, height);
                Bitmap cmp = Bitmap.createBitmap(rgb,0,width,width,height, Bitmap.Config.ARGB_8888);
                mBmp=cmp;
            }
//            mImageView.setImageBitmap(bmp);  //子线程不能操作UI

            image.close();
        }

    };

}
