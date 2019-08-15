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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView textView;
    private Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView=findViewById(R.id.textView);

        Button btnThread=findViewById(R.id.btnThread);
        btnThread.setOnClickListener(this);
        mHandler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                Log.d("AAA", "handleMessage: "+msg.arg1+","+msg.obj);
            }
        };
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btnThread:
                JThread thread=new JThread("AAA");
                thread.start();
                break;
        }
    }

    class JThread extends Thread{
        String name;
        JThread(String name){
            this.name=name;
        }

        @Override
        public void run() {
            super.run();
            Message msg=Message.obtain();
            for(int i=0;i<10;i++){
                msg.arg1=i;
                msg.obj=name;
                mHandler.sendMessageDelayed(msg,1000);
            }
        }
    }
}
