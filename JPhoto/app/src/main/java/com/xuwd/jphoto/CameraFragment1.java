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
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import java.util.Collections;


/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends Fragment {

    private ImageView mImageView;
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
            Toast.makeText(getContext(), "TextutrView: On", Toast.LENGTH_SHORT).show();
//            mImageReader = ImageReader.newInstance(640, 480, ImageFormat.YUV_420_888, 1);
            mImageReader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 1);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, null);

            //!!!!!!!!!!!!!!!!!!!!!!!!!! Joint of framework !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!//
            linkCamera(width, height);
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

            //int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            //Toast.makeText(getContext(),"Rot:"+rotation,Toast.LENGTH_SHORT).show();
        }
    };
    //**************************** ImageReader ****************************//
    private ImageReader mImageReader;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
//            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
            Image image = reader.acquireLatestImage();
            if (image == null) {
                return;
            }
            int pixelStride,rowStride,rowPadding,width,height;
            Image.Plane[] planes = image.getPlanes();
            ByteBuffer buffer = planes[0].getBuffer();

            byte[] bytes = new byte[buffer.remaining()];
            Bitmap temp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            Bitmap bmp = Bitmap.createBitmap(640,480,temp.getConfig());
/*
            width = image.getWidth();
            height = image.getHeight();

            pixelStride = planes[0].getPixelStride();
            rowStride = planes[0].getRowStride();
            rowPadding = rowStride - pixelStride * width;
            int bmpWidth=width + rowPadding / pixelStride;

//            Bitmap bmp = Bitmap.createBitmap(bmpWidth,height, Bitmap.Config.ALPHA_8);
            bmp.copyPixelsFromBuffer(buffer);
*/
            mImageView.setImageBitmap(bmp);

            image.close();
//            Toast.makeText(getContext(),"--- "+i,Toast.LENGTH_SHORT).show();
        }


    };

    //**************************** CameraFragment ****************************//
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

        final Button btnPreview = view.findViewById(R.id.btnPreview);
        btnPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPreviewing){
                    closePreviewSession();
                    btnPreview.setText(R.string.previewStart);
                }else{
                    setPreviewSession();
                    btnPreview.setText(R.string.previewClose);
                }
                mPreviewing=!mPreviewing;
            }
        });


        mTextureView=view.findViewById(R.id.textureView);
        mTextureView.setSurfaceTextureListener(surfaceTextureListener);
    }

    //**************************** CameraDevice ****************************//
    private Boolean mPreviewing=false;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mPreviewSession;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCharacteristics mCharacteristics;
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;//!!!!!!!! Joint of framework !!!!!!
//            mCameraOpenCloseLock.release();
//            configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
            setPreviewSession();
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

    @SuppressLint("MissingPermission")
    private void linkCamera(int width, int height) {
        Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) {
            return;
        }

        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId =  manager.getCameraIdList()[0];
            mCharacteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = mCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            int mSensorOrientation = mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
//            characteristics
//            Size mPreviewSize = getPreferredPreviewSize(map.getOutputSizes(SurfaceTexture.class), width, height);
            manager.openCamera(cameraId, mStateCallback, null);
/*            if (map == null) {
                throw new RuntimeException("Cannot get available preview/video sizes");
            }
*/
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setPreviewSession() {
        if (null == mCameraDevice || !mTextureView.isAvailable()) {
            return;
        }
        try {
            closePreviewSession();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mTextureWidth,mTextureHeight);
            Surface previewSurface = new Surface(texture);
            Surface imageSurface = mImageReader.getSurface();

            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewBuilder.addTarget(previewSurface);
            mPreviewBuilder.addTarget(imageSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface,imageSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mPreviewSession = session;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Activity activity = getActivity();
                            if (null != activity) {
                                Toast.makeText(activity, "CaptureSession Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
//            HandlerThread thread = new HandlerThread("CameraPreview");
//            thread.start();
            mPreviewing=true;
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

}
