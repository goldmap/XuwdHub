package com.xuwd.jweb;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static java.net.Proxy.Type.HTTP;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private WebView webView;
    final  Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==1){
                String str=(String) msg.obj;
                //textView.setText(str);
                //webView.setText
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView=findViewById(R.id.textView);
        webView=findViewById(R.id.webView);

        Button sendButton = (Button) findViewById(R.id.btnSend);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebViewTest();
            }
        });
    }

    public void test(){
        OkHttpClient okHttpClient=new OkHttpClient();
        Request request = new Request.Builder().url("https://www.wentier.com").build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                Log.d("AAA","lianjie shibai了");
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){//回调的方法执行在子线程。
                    String str=response.body().string();
                    Message msg=Message.obtain();
                    msg.what=1;
                    msg.obj=str;
                    mHandler.sendMessage(msg);
                }
                else{
                    Log.d("AAA","获取数据shibai了");
                }
            }
        });
    }

    public void WebViewTest(){
       webView.loadUrl("https://www.wentier.com/test");
    }

}
