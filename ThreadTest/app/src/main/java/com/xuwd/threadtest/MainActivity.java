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
    private MainHandler mainHandler;
    private JThread jThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView=findViewById(R.id.textView);
        Log.d("AAA", "main: "+Thread.currentThread().getId());

        Button btnMainToSub=findViewById(R.id.btnMainToSub);
        btnMainToSub.setOnClickListener(this);
        Button btnSubToMain=findViewById(R.id.btnSubToMain);
        btnSubToMain.setOnClickListener(this);

        mainHandler=new MainHandler();

        jThread=new JThread("J_Thread");
        jThread.start();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btnMainToSub:

                break;
            case R.id.btnSubToMain:
                break;
        }
    }

    class JThread extends Thread{
        String name;
        SubHandler subHandler;

        public JThread(String name){
            super();
            this.name=name;
        }

        public SubHandler getHandler(){
            return subHandler;
        }
        @Override
        public void run() {
            super.run();
            subHandler=new SubHandler();
        }
    }

    class SubHandler extends Handler{
        public SubHandler(){
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    }

    class MainHandler extends Handler{
        public MainHandler(){
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    }
}
