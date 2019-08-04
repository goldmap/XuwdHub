package com.xuwd.jphoto;


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
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends Fragment {
    private Bitmap mBmp;
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_LOCK = 1;
    private static final int STATE_WAITING_PRECAPTURE = 2;
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;
    private static final int STATE_PICTURE_TAKEN = 4;
    private int mState = STATE_PREVIEW;
    private ImageView mImageView;

    //**************************** ImageReader ****************************//
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
            if(image.getFormat()==ImageFormat.JPEG){
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

    //**************************** TextureView ****************************//
    private TextureView mTextureView;
    private int mTextureWidth,mTextureHeight;
    private TextureView.SurfaceTextureListener surfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                              int width, int height) {
            mTextureWidth=width;
            mTextureHeight=height;

            //[KeyJoint]
            igniteCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                                                int width, int height) {
            Toast.makeText(getContext(), "TextutrViewSize: "+width+":"+height, Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
//            mBmp= mTextureView.getBitmap();
            if(mBmp!=null)
                mImageView.setImageBitmap(mBmp);
        }
    };

    @SuppressLint("MissingPermission")
    //触发某个相机，为其设置回调函数，该相机开始生命周期
    private void igniteCamera() {
        Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) {
            return;
        }

        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId =  manager.getCameraIdList()[0];
            CameraCharacteristics mCharacteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = mCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//            int mSensorOrientation = mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            Size[] yuv_imageSize = map.getOutputSizes(ImageFormat.YUV_420_888);
//            Size[] jpeg_imageSize = map.getOutputSizes(ImageFormat.JPEG);
//            Size[] raw_imageSize = map.getOutputSizes(ImageFormat.RAW_SENSOR);
//            Size[] priv_imageSize = map.getOutputSizes(TextureView.class);
            int imgWidth=yuv_imageSize[0].getWidth();
            int imgHeight=yuv_imageSize[0].getHeight();

//            mImageReader = ImageReader.newInstance(imgWidth, imgHeight, ImageFormat.JPEG, 2);
            mImageReader = ImageReader.newInstance(imgWidth, imgHeight, ImageFormat.YUV_420_888, 2);
//            mImageReader = ImageReader.newInstance(imageSize.getWidth(), imageSize.getHeight(), ImageFormat.JPEG, 2);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);

            try {
                if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                    throw new RuntimeException("Time out waiting to lock camera opening.");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //抛出Camera的回调函数cameraStateCallback，待其破土而出
            manager.openCamera(cameraId, cameraStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
     //**************************** CameraDevice ****************************//
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private Boolean mPreviewing=false;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;//!!!!!!!! Joint of framework !!!!!!
            mCameraOpenCloseLock.release();

            //建立物理相机和显示设备之间的通道，创建会话（设置会话的回调函数），通过会话启动“会话请求”Request
            try {
                SurfaceTexture texture = mTextureView.getSurfaceTexture();
                assert texture != null;
                texture.setDefaultBufferSize(mTextureWidth,mTextureHeight);
                Surface textureSurface = new Surface(texture);

                //建立投射通道
                mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                mPreviewRequestBuilder.addTarget(textureSurface);//textureSurface已经得到，顺便设置mPreviewRequestBuilder

                //创建会话，为什么要把ImageReader的surface关联进去？
                Surface imageSurface = mImageReader.getSurface();
                mCameraDevice.createCaptureSession(Arrays.asList(textureSurface,imageSurface), sessionStateCallback,mBackgroundHandler);
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
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }
    };


    //**************************** CameraCapture.Session ****************************//
    CameraCaptureSession.StateCallback sessionStateCallback =  new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            mCaptureSession = session;//[KeyJoint]:mCaptureSession的句柄
            //会话OK，启动“浏览”；浏览无事可处理，不需要设置回调函数
            try {
                //投射通道在之前已经顺便建立： mPreviewRequestBuilder.addTarget(textureSurface);
                // Auto focus & flash should be continuous for camera preview.
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

                //启动预览请求-------------------！！！！
                mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null,mBackgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Activity activity = getActivity();
            if (null != activity) {
                Toast.makeText(activity, "CaptureSession Failed", Toast.LENGTH_SHORT).show();
            }
        }
    };

    //*****************通过Session启动的Capture.Request 的共用回调***********************//
    //用真机调试通过，afState=5
    private CameraCaptureSession.CaptureCallback captureRequestCallback
            = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
//            Toast.makeText(getContext(),"mState"+mState,Toast.LENGTH_SHORT).show();
            switch (mState) {
                case STATE_PREVIEW: {
                    //改编的preview模式不调用这个回调函数，不经过这里;
                    // We have nothing to do when the camera preview is working normally.
                    break;
                }
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
    };

    //*****************通过Session启动各种功能****************************//
    private void lockFocus(){
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,CameraMetadata.CONTROL_AF_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), captureRequestCallback,mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), captureRequestCallback,mBackgroundHandler);
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null,mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * we get a response in {@link #captureRequestCallback} from {@link #lockFocus()}.
     */
    private void runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = STATE_WAITING_PRECAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), captureRequestCallback,mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void captureStillPicture() {
        try {
            final Activity activity = getActivity();
            if (null == activity || null == mCameraDevice) {
                return;
            }
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
                    Toast.makeText(getContext(),"phote takened: " ,Toast.LENGTH_SHORT).show();
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

    //**************************** CameraFragment ****************************//
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    public CameraFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mImageView=view.findViewById(R.id.imageView);

        Button btnExit=view.findViewById(R.id.btnExit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack();
            }
        });

        Button btnCapture=view.findViewById(R.id.btnCapture);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lockFocus();
            }
        });

        final Button btnPreview = view.findViewById(R.id.btnPreview);
        btnPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPreviewing){
                    btnPreview.setText(R.string.previewStart);
                    closeCamera();
                }else{
                    btnPreview.setText(R.string.previewClose);
                    igniteCamera();
                }
                mPreviewing=!mPreviewing;
            }
        });


        mTextureView=view.findViewById(R.id.textureView);
        mTextureView.setSurfaceTextureListener(surfaceTextureListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
    }

    @Override
    public void onPause() {
//        closeCamera();
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
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
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

    /**
     * Stops the background thread and its {@link Handler}.
     */
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
