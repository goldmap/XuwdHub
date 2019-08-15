package com.xuwd.jscan;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends JActivity {
    public static final int REQ_QR_CODE = 11002;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPermission();

        Button btnFileMan=findViewById(R.id.btnScan);
        btnFileMan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!hasPermissions(VIDEO_PERMISSIONS)){
                    return;
                }
                Intent intent=new Intent(getBaseContext(),ScanActivity.class);
                startActivityForResult(intent,REQ_QR_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQ_QR_CODE && resultCode==RESULT_OK){
            Bundle bundle=data.getExtras();
            String result=bundle.getString("ScanResult");
        }
    }
}



//https://blog.csdn.net/qq_17475155/article/details/51607141