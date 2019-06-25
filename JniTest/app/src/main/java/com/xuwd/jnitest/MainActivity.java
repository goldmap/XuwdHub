package com.xuwd.jnitest;

import android.app.Activity;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv=findViewById(R.id.textView);

        JNative jNative=new JNative();
        String str=jNative.getStrFromJNI();

        str += "\n"+jNative.ffmpegInfo("rtmp://203.195.210.150/live/123");
//        tv.setText(jNative.getStrFromJNI());
        tv.setText(str);
    }
}
