package com.xuwd.threadtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
                Message msg=jThread.getHandler().obtainMessage();
                msg.what=1;
                Bundle bundle=new Bundle();
                String curThread=Thread.currentThread().getName();
                bundle.putString("thread",curThread);
                bundle.putString("message","Main to sub");
                msg.obj=bundle;
                jThread.getHandler().handleMessage(msg);
                Toast.makeText(getBaseContext(),"Y",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnSubToMain:
                jThread.sendMessage("Aha");
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
            //Looper looper=Looper.myLooper();
            Looper.prepare();

            Message msg=mainHandler.obtainMessage();
            msg.what=1;
            Bundle bundle=new Bundle();
            String curThread=Thread.currentThread().getName();
            bundle.putString("thread",curThread);
            bundle.putString("message","in run()");
            msg.obj=bundle;
            mainHandler.sendMessage(msg);

            subHandler=new SubHandler();

            sendMessage("CCC");
        }

        public void sendMessage(String ms){
            Message msg=mainHandler.obtainMessage();
            msg.what=1;
            Bundle bundle=new Bundle();
            String curThread=Thread.currentThread().getName();
            bundle.putString("thread",curThread);
            bundle.putString("message","in sendMesdsage"+ms);
            msg.obj=bundle;
            mainHandler.sendMessage(msg);
        }
    }

    class SubHandler extends Handler{
        public SubHandler(){
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what){
                case 1:
                    String str="SubHandler Thread ("+Thread.currentThread().getName()+") receive a message from: [";

                    Bundle bundle=(Bundle) msg.obj;
                    str+=bundle.getString("thread")+"] with information: ";
                    str+=bundle.getString("message");
                    Log.d("AAA", str);
            }
            //super.handleMessage(msg);
        }
    }

    class MainHandler extends Handler{
        public MainHandler(){
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what){
                case 1:
                    String str="MainHandler Thread ("+Thread.currentThread().getName()+") receive a message from: [";

                    Bundle bundle=(Bundle) msg.obj;
                    str+=bundle.getString("thread")+"] with information: ";
                    str+=bundle.getString("message");
                    Log.d("AAA", str);
            }
            //super.handleMessage(msg);
        }
    }
}
