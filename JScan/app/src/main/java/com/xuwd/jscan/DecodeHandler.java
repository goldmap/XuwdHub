/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xuwd.jscan;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.Hashtable;


final class DecodeHandler extends Handler {

    private static final String TAG = "AAA";

    private final ScanActivity activity;
    private final MultiFormatReader multiFormatReader;

    DecodeHandler(ScanActivity activity, Hashtable<DecodeHintType, Object> hints) {
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
        this.activity = activity;
    }

    @Override
    public void handleMessage(Message msg) {
        Log.d(TAG, "DecodeHandler got message");

        switch (msg.what) {

            case R.id.decode:
                Log.d(TAG, "DecodeHandler got message: R.id.decode, with size :" + msg.arg1 + "," + msg.arg2);
                decode((byte[]) msg.obj, msg.arg1, msg.arg2);
                break;
            case R.id.quit:
                Looper.myLooper().quit();
                break;
            case R.id.test:
                String str = "DecodeHandler handleMessage,thread id :" + Thread.currentThread().getId() + " and ";
                Log.d(TAG, str + "Test OK~");
                break;
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        Log.d(TAG, "decode start to deal :" + width + "," + height);

        long start = System.currentTimeMillis();
        Result rawResult = null;

        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            rawResult = multiFormatReader.decodeWithState(bitmap);
        } catch (ReaderException re) {
            // continue
        } finally {
            multiFormatReader.reset();
        }


        //向ScanActivity的发送消息（处理的结果）
        if (rawResult != null) {
            long end = System.currentTimeMillis();
            //Log.d(TAG, "Found barcode (" + (end - start) + " ms):\n" + rawResult.toString());
            Message message = Message.obtain(activity.getHandler(), R.id.decode_succeeded, rawResult);
            String str = rawResult.getText();
            Bundle bundle = new Bundle();
            //bundle.putParcelable(DecodeThread.BARCODE_BITMAP, source.renderCroppedGreyscaleBitmap());
            bundle.putString("QRCode",str);
            message.setData(bundle);
            Log.d(TAG, "decode OK and sengMessage to handler of ScanActivity: "+str);
            Toast.makeText(activity, str, Toast.LENGTH_SHORT).show();

            message.sendToTarget();
        } else {
            Message message = Message.obtain(activity.getHandler(), R.id.decode_failed);
            message.sendToTarget();
        }

    }

}
