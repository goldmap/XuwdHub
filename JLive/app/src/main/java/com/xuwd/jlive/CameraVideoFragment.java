package com.xuwd.jlive;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.HandlerThread;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class CameraVideoFragment extends Fragment {
    boolean bNow=false;
    JFFmpeg jFFmpeg;
    boolean mIsLiving=false;
    TextView tv;
    Button btnLive;
    private Context mContext;
    private ImageView mImageView;
    private AutofitTextureView mTextureView;
    private int mTextureWidth,mTextureHeight;

    private CameraDevice mCameraDevice;
    private ImageReader mImageReader;
    private Bitmap mBitmap;
    Boolean bMap=false;
//    private Size mPreviewSize;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private static final int REQUEST_VIDEO_PERMISSIONS = 1;
    private static final String FRAGMENT_DIALOG = "dialog";
    private static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
    };

    public CameraVideoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera_video, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext=view.getContext();
        jFFmpeg =  JFFmpeg.getInstance();
        int i= jFFmpeg.init("rtmp://203.195.210.150/live/123");
        Toast.makeText(getContext(),"init:"+i,Toast.LENGTH_SHORT).show();

        mImageView=view.findViewById(R.id.testView);

        btnLive = view.findViewById(R.id.btnLive);
        btnLive.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(mIsLiving){
                    stopLive();
                }else{
                    startLive();
                }
            }
        });

        mTextureView=view.findViewById(R.id.textureView);
        mTextureView.setSurfaceTextureListener(surfaceTextureListener);
    }

    private TextureView.SurfaceTextureListener surfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                              int width, int height) {
            mTextureWidth=width;
            mTextureHeight=height;
            Toast.makeText(mContext,"textrue on", Toast.LENGTH_SHORT).show();
            //权限检测与设置
            if(!hasPermissions(VIDEO_PERMISSIONS)){
                confirmPermissions();
                return;
            }
            Toast.makeText(getContext(),"权限OK",Toast.LENGTH_SHORT).show();

            Toast.makeText(getContext(),"TextureView:"+mTextureWidth+","+mTextureHeight,Toast.LENGTH_SHORT).show();
            mImageReader = ImageReader.newInstance(640, 480, ImageFormat.YUV_420_888, 1);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);

            openCamera(mTextureWidth,mTextureHeight);
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
/*
            String str;
            if(bNow){
                bNow=false;
                str=">>>>";
            }else{
                bNow=true;
                str="----";
            }
*/
            mBitmap=mTextureView.getBitmap();
//            mImageView.setImageBitmap(bitmap);
        }
    };

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
//            if(!mIsLiving)
//                return;

//            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
            Image image = reader.acquireLatestImage();
            if (image == null) {
                return;
            }
            int width = image.getWidth();
            int height = image.getHeight();

            Image.Plane[] planes = image.getPlanes();
            ByteBuffer buffer = planes[0].getBuffer();

            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;

            int bmpWidth=width + rowPadding / pixelStride;
//            Bitmap bmp = Bitmap.createBitmap(bmpWidth*k,height*k, Bitmap.Config.ARGB_8888);
            Bitmap bmp = Bitmap.createBitmap(bmpWidth,height, Bitmap.Config.ALPHA_8);
            buffer.rewind();
            bmp.copyPixelsFromBuffer(buffer);

            mImageView.setImageBitmap(bmp);
//            mImageView.setImageBitmap(mBitmap);

//          数据有效宽度，一般的图片 width <= rowStride，这也是导致byte[].length <= capacity的原因,所以我们只取width部分
            //此处用来装填最终的YUV数据，需要1.5倍的图片大小，因为Y U V 比例为 4:1:1
            byte[] yBytes = new byte[width * height];
            int dstIndex = 0;

            //临时存储uv数据的
            byte uBytes[] = new byte[width * height / 4];
            byte vBytes[] = new byte[width * height / 4];
            int uIndex = 0;
            int vIndex = 0;

            for (int i = 0; i < planes.length; i++) {
                pixelStride = planes[i].getPixelStride();
                rowStride = planes[i].getRowStride();

                buffer = planes[i].getBuffer();
                buffer.rewind();
                //如果pixelsStride==2，一般的Y的buffer长度=640*480，UV的长度=640*480/2-1
                //源数据的索引，y的数据是byte中连续的，u的数据是v向左移以为生成的，两者都是偶数位为有效数据
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);

                int srcIndex = 0;
                if (i == 0) {
                    //直接取出来所有Y的有效区域，也可以存储成一个临时的bytes，到下一步再copy
                    for (int j = 0; j < height; j++) {
                        System.arraycopy(bytes, srcIndex, yBytes, dstIndex, width);
                        srcIndex += rowStride;
                        dstIndex += width;
                    }
                } else if (i == 1) {
                    //根据pixelsStride取相应的数据
                    for (int j = 0; j < height / 2; j++) {
                        for (int k = 0; k < width / 2; k++) {
                            uBytes[uIndex++] = bytes[srcIndex];
                            srcIndex += pixelStride;
                        }
                        if (pixelStride == 2) {
                            srcIndex += rowStride - width;
                        } else if (pixelStride == 1) {
                            srcIndex += rowStride - width / 2;
                        }
                    }
                } else if (i == 2) {
                    //根据pixelsStride取相应的数据
                    for (int j = 0; j < height / 2; j++) {
                        for (int k = 0; k < width / 2; k++) {
                            vBytes[vIndex++] = bytes[srcIndex];
                            srcIndex += pixelStride;
                        }
                        if (pixelStride == 2) {
                            srcIndex += rowStride - width;
                        } else if (pixelStride == 1) {
                            srcIndex += rowStride - width / 2;
                        }
                    }
                }
            }
            Toast.makeText(getContext(),"... ",Toast.LENGTH_SHORT).show();
            int i=jFFmpeg.pushCameraData(yBytes, yBytes.length, uBytes, uBytes.length, vBytes, vBytes.length);

            image.close();
//            Toast.makeText(getContext(),"--- "+i,Toast.LENGTH_SHORT).show();
        }


    };


    @SuppressLint("MissingPermission")
    private void openCamera(int width, int height) {
        final Activity activity=getActivity();
        if(activity==null || activity.isFinishing()){
            return;
        }
//        setUpCameraOutputs(width, height);
//        configureTransform(width, height);
        CameraManager manager= (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId=manager.getCameraIdList()[0];
            CameraCharacteristics characteristics=manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map=characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if(map==null){
                throw new RuntimeException("Cannot get available preview/video sizes");
            }
            manager.openCamera(cameraId,mStateCallback,null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
//            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
//            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
//            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }
    };

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
//            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            texture.setDefaultBufferSize(mTextureWidth, mTextureHeight);

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);
            Surface imageSurface = mImageReader.getSurface();

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);
            mPreviewRequestBuilder.addTarget(imageSurface);

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(surface,imageSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    // The camera is already closed
                    if (null == mCameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    mCaptureSession = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Activity activity = getActivity();
                    if (null != activity) {
                        Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }, null );

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        try {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);;
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
//            mPreviewRequest = mPreviewRequestBuilder.build();
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void startLive(){
        btnLive.setText(R.string.live_stop);
        mIsLiving=true;
    }
    private void stopLive(){
        btnLive.setText(R.string.live_start);
//        jFFmpeg.close();
        mIsLiving=false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_VIDEO_PERMISSIONS) {
            if (grantResults.length == VIDEO_PERMISSIONS.length) {
                for (int i=0;i<grantResults.length;i++) {
                    int result=grantResults[i];
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        //ErrorDialog.newInstance(getString(R.string.permission_request)).show(getFragmentManager(), FRAGMENT_DIALOG);
                        new ConfirmationDialog().show(getFragmentManager(),"");
                    }
                    else{
                        String str=permissions[i]+"权限OK";
                        Toast.makeText(getContext(),str,Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                ErrorDialog.newInstance("设置错误").show(getFragmentManager(), FRAGMENT_DIALOG);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    private boolean hasPermissions(String[]permissions){
        for(String permission:permissions){
            if(ActivityCompat.checkSelfPermission(getActivity(),permission)!= PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;//所有的权限都开通，则返回true
    }
    private void confirmPermissions(){
        //坑点：有的权限在之前被赋予“不再询问”，无法用requesePemission函数喊醒，需要检查并提醒人工设置。
        if(noPrompt(VIDEO_PERMISSIONS)){
            Toast.makeText(getContext(),"需要通过手机【设置】取得权限",Toast.LENGTH_SHORT).show();
        }else {
            requestPermissions(VIDEO_PERMISSIONS,REQUEST_VIDEO_PERMISSIONS);
        }
    }
    private boolean noPrompt(String[] permissions){
        for(String permission:permissions){
            if(shouldShowRequestPermissionRationale(permission)){
                return true;
            }
        }
        return false;
    }

    public static class ConfirmationDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage("所需要的权限未获得，重新设置权限")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(VIDEO_PERMISSIONS,REQUEST_VIDEO_PERMISSIONS);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getContext(),"未取得必要权限，无法正常运行",Toast.LENGTH_SHORT).show();
                                }
                            })
                    .create();
        }

    }
    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
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
