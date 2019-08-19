package com.xuwd.threadtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView textView;
    private Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView=findViewById(R.id.textView);
        Log.d("AAA", "main: "+Thread.currentThread().getId());

        /*
        Timer timer=new Timer();
        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                Log.d("AAA", " timer run:"+Thread.currentThread().getId());
            }
        };
        timer.schedule(timerTask,0,10000);*/

        Button btnThread=findViewById(R.id.btnThread);
        btnThread.setOnClickListener(this);

        mHandler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                Log.d("AAA", "handleMessage from: "+msg.obj+" ,"+msg.arg1+"thread:"+Thread.currentThread().getId());
            }
        };
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btnThread:
                JThread thread1=new JThread("BBB");
                JThread thread2=new JThread("CCC");
                thread1.start();
                thread2.start();
                break;
        }
    }

    class JThread extends Thread{
        String name;
        public JThread(String name){
            super();
            this.name=name;
        }

        @Override
        public void run() {
            super.run();
            Log.d("AAA", "thread: "+Thread.currentThread().getId());
            for(int i=0;i<100;i++){
//                Log.d("AAA", "run: "+i);
                Message msg=Message.obtain();
                msg.arg1=i;
                msg.obj=name;

                mHandler.sendMessage(msg);
            }
        }
    }
}
