package com.xuwd.jimagetest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.xuwd.jimagetest.JCamera.AutoFitTextureView;
import com.xuwd.jimagetest.JCamera.ImageUtil;
import com.xuwd.jimagetest.JCamera.JCamera;

import static com.xuwd.jimagetest.ImageUtil.yuv420pToBitmap;

public class MainActivity extends JActivity {
    private Activity mActivity;
    private ImageView imgViewLeft;
    private ImageView imgViewRight;
    private Button btnLeft;
    private Bitmap mBmp;
    //**************************** TextureView ****************************//
    private AutoFitTextureView mTextureView;
    private int mTextureWidth,mTextureHeight;
    private JCamera mJCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity=this;
        initPermission();
        initView();
    }

    private void initView(){
        imgViewLeft=findViewById(R.id.imgViewLeft);
        imgViewRight=findViewById(R.id.imgViewRight);

        mTextureView=findViewById(R.id.textureView);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                mTextureWidth =width;
                mTextureHeight =height;
                //Log.d("AAA", "onSurfaceTextureAvailable:(mTextureWidth,mTextureHeight) "+mTextureWidth+","+mTextureHeight);
                //[KeyJoint]
                mJCamera=new JCamera(mTextureView,mActivity);
                mJCamera.setPreviewCallback(previewCallback);
                mJCamera.start();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                mBmp=mTextureView.getBitmap();
            }
        });

        btnLeft=findViewById(R.id.btnLeft);
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //imgViewLeft.setImageBitmap(mBmp);
                mJCamera.captureStillPicture();
            }
        });
    }

    JCamera.PreviewCallback previewCallback=new JCamera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data,int imageFormat,int width,int height) {
            Log.d("AAA", "previewCallback received data and show:"+imageFormat+","+width+":"+height);
            Bitmap bmp=null;
            byte[] rotatedData=null;
            int tmp;
            switch(imageFormat){
                case ImageFormat.JPEG:
                    //rotatedData = new byte[width*height];
                    //ImageUtil.rotateImage(data,width,height,rotatedData);

                    bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                    //bmp = BitmapFactory.decodeByteArray(rotatedData, 0, rotatedData.length);

                    break;
                case ImageFormat.YUV_420_888:
                    Log.d("AAA", "previewCallback switch in ImageFormat.YUV_420_888" );
                    rotatedData = new byte[data.length];

                    int rgb[]= ImageUtil.decodeYUVtoRGB(data, width, height);
                    //bmp = Bitmap.createBitmap(rgb,0,width,width,height, Bitmap.Config.RGBA_F16);

                    ImageUtil.rotateImage(data,width,height,rotatedData);

                    bmp=yuv420pToBitmap(rotatedData,width,height);

                    int rgb2[]= ImageUtil.decodeYUVtoRGB(rotatedData, width,height);
                    Bitmap bmp2 = Bitmap.createBitmap(rgb2,0,height,height,width, Bitmap.Config.RGBA_F16);
                    imgViewRight.setImageBitmap(bmp2);
                    break;
                default:
                    break;
            }
            if(bmp!=null){
                imgViewLeft.setImageBitmap(bmp);

                //PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, width, height,0,0,width,height,false);
                //BinaryBitmap cmp = new BinaryBitmap(new HybridBinarizer(source));
                //data=byte[] source;
                //bmp=BitmapFactory.decodeByteArray(source, 0, data.length);
                //imgViewRight.setImageBitmap(bmp);
            }
            else{
                Toast.makeText(mActivity, "bmp NULL", Toast.LENGTH_SHORT).show();
            }
            //Log.d("AAA", "ScanActivity send Message ok");
        }
    };

    private void decode(byte[] data, int width, int height) {
        //modify here
        final MultiFormatReader multiFormatReader = new MultiFormatReader();
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++)
                rotatedData[x * height + height - y - 1] = data[x + y * width];
        }
        int tmp = width; // Here we are swapping, that's the difference to #11
        width = height;
        height = tmp;

        Result rawResult = null;
        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(rotatedData, width, height, 0, 0, width, height, false);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            rawResult = multiFormatReader.decodeWithState(bitmap);
        } catch (ReaderException re) {
            // continue
        } finally {
            multiFormatReader.reset();
        }
    }
}
