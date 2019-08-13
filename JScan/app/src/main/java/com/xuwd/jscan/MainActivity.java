package com.xuwd.jscan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends JActivity {

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
                startActivity(intent);
            }
        });
    }
}



//https://blog.csdn.net/qq_17475155/article/details/51607141