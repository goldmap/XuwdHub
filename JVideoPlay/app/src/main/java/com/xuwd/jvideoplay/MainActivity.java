package com.xuwd.jvideoplay;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends JActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPermission();

        Button btnFileMan=findViewById(R.id.btnFileMan);
        btnFileMan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!hasPermissions(VIDEO_PERMISSIONS)){
                    return;
                }
                Intent intent=new Intent(getBaseContext(),FileManActivity.class);
                startActivity(intent);
            }
        });
    }



}
