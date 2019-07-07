package com.xuwd.jvideoplay;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

public class MainActivity extends JActivity {
    private MediaPlayer mMediaPlayer;
    private TextureView mTextureView;
    private SurfaceTexture mSurfaceTexture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextureView=findViewById(R.id.textureView);
        mTextureView.setSurfaceTextureListener(surfaceTextureListener);

        initPermission();
        Button btnPlay=findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!hasPermissions(VIDEO_PERMISSIONS)){
                    return;
                }
                playVideo();
            }
        });
    }

    private TextureView.SurfaceTextureListener surfaceTextureListener=new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            mSurfaceTexture=surfaceTexture;
            initMediaPlayer();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    };

    public void playVideo(){
        mMediaPlayer.start();
    }

    private void initMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        mMediaPlayer.setSurface(new Surface(mSurfaceTexture));
        try {
            mMediaPlayer.setDataSource("h:/boen.mp4");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
