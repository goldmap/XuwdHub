package com.xuwd.jvideo;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;

public class CameraVideoFragment extends Fragment {
    private Bitmap mBmp;
    //private final Activity parent = getActivity();;
    private String TAG="--------------->:";
    private Button mButtonVideo;
    private Boolean mIsRecordingVideo=false;
    private static final String FRAGMENT_DIALOG = "dialog";
    private static final int REQUEST_VIDEO_PERMISSIONS = 1;

    private Integer mSensorOrientation;
    private String mNextVideoAbsolutePath;
    private CameraCaptureSession mPreviewSession;
    private ImageView mImageView;

    private  MediaPlayer mMediaPlayer;

    private MediaRecorder mMediaRecorder;
    //**************************** MediaRecorder ****************************//

    private void setUpMediaRecorder()  {
        final Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        mNextVideoAbsolutePath = getVideoFilePath(getActivity());
        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);

        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        /*
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (mSensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                break;
        }*/
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
            if(image.getFormat()== ImageFormat.JPEG){
                Image.Plane[] planes = image.getPlanes();
                buffer = planes[0].getBuffer();
                buffer.rewind();
                byte[] bytes=new byte[buffer.remaining()];
                buffer.get(bytes);

                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                mBmp=bmp;
            }
            else if(image.getFormat()==ImageFormat.YUV_420_888){
                byte[] yuvBytes = ImageUtil.getBytesFromImage(image,ImageUtil.YUV420P);

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
//            mBmp= mTextureView.getBitmap();
            if(mBmp!=null)
                mImageView.setImageBitmap(mBmp);
        }

    };

    @SuppressLint("MissingPermission")
    private void igniteCamera() {
        final Activity activity = getActivity();
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
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            if (map == null) {
                throw new RuntimeException("Cannot get available preview/video sizes");
            }
            mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                    mTextureWidth,mTextureHeight, mVideoSize);

            int orientation = getResources().getConfiguration().orientation;
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
            initMediaPlayer(surfaceTexture);
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

    private void initMediaPlayer(SurfaceTexture surfaceTexture){
        if (mMediaPlayer==null){
            mMediaPlayer = new MediaPlayer();
        }
        mMediaPlayer.setSurface(new Surface(surfaceTexture));
    }
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

            configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
            //此时配置Recorder应该合适
            setUpMediaRecorder();

            //创建相机的对话机制, 创建会话（设置会话的回调函数），通过会话启动“会话请求”Request
            try {
                SurfaceTexture texture = mTextureView.getSurfaceTexture();
                assert texture != null;
                texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                Surface textureSurface = new Surface(texture);
//                Surface imageSurface = mImageReader.getSurface();
                Surface recorderSurface = mMediaRecorder.getSurface();


                //这个动作不是建立会话的必要，但要生成并关联两个Surface，所以顺便addTarget
                mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                mPreviewRequestBuilder.addTarget(textureSurface);
                mPreviewRequestBuilder.addTarget(recorderSurface);

                //创建相机设备的会话，抛出去，待其回调函数出声
                mCameraDevice.createCaptureSession(Arrays.asList(textureSurface,recorderSurface),
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
            mCaptureSession = session;////[KeyJoint]:mCaptureSession的句柄
            //会话OK，启动“浏览”；
            updatePreview();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Activity activity = getActivity();
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

     //****************************** CameraVideoFragment ***********************************//
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private TextView mVideoView;
    public static CameraVideoFragment newInstance() {
        return new CameraVideoFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera_video, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        //parent= getActivity();
        TextureView mVideoView=view.findViewById(R.id.videoView);
        mVideoView.setSurfaceTextureListener(videoSurfaceTextureListener);

        mTextureView = view.findViewById(R.id.textureView);
        mTextureView.setSurfaceTextureListener(surfaceTextureListener);
        mMediaRecorder = new MediaRecorder();

        mButtonVideo = view.findViewById(R.id.btnRecord);
        mButtonVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsRecordingVideo) {
                    stopRecordingVideo();
                } else {
                    startRecordingVideo();
                }
            }
        });

        Button mButtonPlay = view.findViewById(R.id.btnPlay);
        mButtonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playVideo();
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
            if (mPreviewSession != null) {
                mPreviewSession.close();
                mPreviewSession = null;
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

    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            return choices[0];
        }
    }
    private Size mPreviewSize;
    private Size mVideoSize;

    private static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        return choices[choices.length - 1];
    }
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    private String getVideoFilePath(Context context) {
        final File dir = context.getExternalFilesDir(null);
        return (dir == null ? "" : (dir.getAbsolutePath() + "/"))
                + System.currentTimeMillis() + ".mp4";
    }
    private void startRecordingVideo() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // UI
                mButtonVideo.setText(R.string.stop);
                mIsRecordingVideo = true;

                // Start recording
                mMediaRecorder.start();
            }
        });
    }

    private void stopRecordingVideo() {
        // UI
        mIsRecordingVideo = false;
        mButtonVideo.setText(R.string.record);
        // Stop recording
        mMediaRecorder.stop();
        mMediaRecorder.reset();

        Activity activity = getActivity();
        if (null != activity) {
            Toast.makeText(activity, "Video saved: " + mNextVideoAbsolutePath,
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Video saved: " + mNextVideoAbsolutePath);
        }
        //startPreview();
    }

    private void playVideo(){
        try {
            //使用手机本地视频
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(mNextVideoAbsolutePath);
            mMediaPlayer.prepare();

        } catch (Exception e) {
            e.printStackTrace();
        }
        mMediaPlayer.start();
    }
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
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
}
