/*
 * Copyright (C) 2008 ZXing authors
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
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BarcodeFormat;

import java.util.Vector;


/**
 * This class handles all the messaging which comprises the state machine for capture.
 */
public final class ScanActivityHandler extends Handler {
    private int mCount=0;
    private static final String TAG = "AAA";

    private final ScanActivity activity;
    private final DecodeThread decodeThread;
    private State state;

    private enum State {
        PREVIEW,
        SUCCESS,
        DONE
    }

    public ScanActivityHandler(ScanActivity activity, Vector<BarcodeFormat> decodeFormats,
                               String characterSet) {
        this.activity = activity;
        decodeThread = new DecodeThread(activity, decodeFormats, characterSet);
        decodeThread.start();
        state = State.SUCCESS;
        // Start ourselves capturing previews and decoding.

    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case R.id.auto_focus:
                //Log.d(TAG, "Got auto-focus message");
                // When one auto focus pass finishes, start another. This is the closest thing to
                // continuous AF. It does seem to hunt a bit, but I'm not sure what else to do.
                if (state == State.PREVIEW) {
                    //CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
                }
                break;
            case R.id.restart_preview:
                Log.d(TAG, "ScanActivityHanlder Got restart preview message");
                break;
            case R.id.decode_succeeded:
                Log.d(TAG, "ScanActivityHanlder Got decode succeeded message");
                state = State.SUCCESS;
                Bundle bundle = message.getData();
                Intent intent = new Intent();
                intent.putExtras(bundle);
                //Bitmap barcode = bundle == null ? null :(Bitmap) bundle.getParcelable(DecodeThread.BARCODE_BITMAP);

                activity.setResult(Activity.RESULT_OK, intent);
                quitSynchronously();
                activity.finish();
                break;
            case R.id.decode_failed:
                mCount++;
                Log.d(TAG, "ScanActivityHanlder Got decode failed message, count= "+mCount);
                if(mCount<=9){
                    state = State.PREVIEW;
                    activity.shot();
                    // We're decoding as fast as possible, so when one decode fails, start another.
                    //CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
                }else{
                    activity.setResult(Activity.RESULT_CANCELED, null);
                    quitSynchronously();
                    activity.finish();
                }
                break;
        }
    }

    public void quitSynchronously() {
        state = State.DONE;
        //CameraManager.get().stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
        quit.sendToTarget();
        try {
            decodeThread.join();
        } catch (InterruptedException e) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.decode_succeeded);
        removeMessages(R.id.decode_failed);
    }

}
