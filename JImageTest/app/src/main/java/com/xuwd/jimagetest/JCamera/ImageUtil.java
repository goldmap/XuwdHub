package com.xuwd.jimagetest.JCamera;

import android.graphics.ImageFormat;
import android.media.Image;
import android.util.Log;

import java.nio.ByteBuffer;

public class ImageUtil {

    public static final int YUV420SP = 1;
    public static final int YV12 = 2;
    private static final String TAG = "ImageUtil";
    public static byte[] getBytesFromImage(Image image, int type) {
        final Image.Plane[] planes=image.getPlanes();
        int width = image.getWidth();
        int height=image.getHeight();
        byte[] yuvBytes=new byte[width*height* ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888)/8];
        int dstIndex=0;

        byte uBytes[] = new byte[width * height / 4];
        byte vBytes[] = new byte[width * height / 4];
        int uIndex = 0;
        int vIndex = 0;

        int pixelsStride,rowStride;
        for(int i=0;i<planes.length;i++){
            pixelsStride = planes[i].getPixelStride();
            rowStride = planes[i].getRowStride();
            ByteBuffer buffer=planes[i].getBuffer();

            byte[] bytes=new byte[buffer.capacity()];
            buffer.get(bytes);

            int srcIndex=0;
            if(i==0){
                for(int j=0;j<height;j++){
                    System.arraycopy(bytes,srcIndex,yuvBytes,dstIndex,width);
                    srcIndex+=rowStride;
                    dstIndex+=width;
                }
            }else if(i==1){
                for(int j=0;j<height/2;j++){
                    for(int k=0;k<width/2;k++){
                        uBytes[uIndex++]=bytes[srcIndex];
                        srcIndex+=pixelsStride;
                    }
                    if (pixelsStride == 2) {
                        srcIndex += rowStride - width;
                    } else if (pixelsStride == 1) {
                        srcIndex += rowStride - width / 2;
                    }
                }
            }else if (i == 2) {
                //根据pixelsStride取相应的数据
                for (int j = 0; j < height / 2; j++) {
                    for (int k = 0; k < width / 2; k++) {
                        vBytes[vIndex++] = bytes[srcIndex];
                        srcIndex += pixelsStride;
                    }
                    if (pixelsStride == 2) {
                        srcIndex += rowStride - width;
                    } else if (pixelsStride == 1) {
                        srcIndex += rowStride - width / 2;
                    }
                }
            }
        }
        image.close();

        Log.d("AAA", "getBytesFromImage get imageFormat: "+type);
        switch(type){
            case 0:
                System.arraycopy(uBytes,0,yuvBytes,dstIndex,uBytes.length);
                System.arraycopy(vBytes,0,yuvBytes,dstIndex+uBytes.length,vBytes.length);
                break;
            case 1:
                Log.d("AAA", "Yaa: ");
                for(int i=0;i<vBytes.length;i++){
                    yuvBytes[dstIndex++]=uBytes[i];
                    yuvBytes[dstIndex++]=vBytes[i];
                }
                break;
            case ImageFormat.YUV_420_888:
                Log.d("AAA", "Ya: ");
                for(int i=0;i<vBytes.length;i++){
                    yuvBytes[dstIndex++]=vBytes[i];
                    yuvBytes[dstIndex++]=uBytes[i];
                }
                break;
        }
        return yuvBytes;
    }

    public static int[] decodeYUVtoRGB(byte[] yuvBytes, int width, int height)
    {
        final int frameSize = width * height;
        int rgb[] = new int[frameSize];
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuvBytes[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuvBytes[uvp++]) - 128;
                    u = (0xff & yuvBytes[uvp++]) - 128;
                }
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);
                if (r < 0)
                    r = 0;
                else if (r > 262143)
                    r = 262143;
                if (g < 0)
                    g = 0;
                else if (g > 262143)
                    g = 262143;
                if (b < 0)
                    b = 0;
                else if (b > 262143)
                    b = 262143;
                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
                        | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
        return rgb;
    }
}
