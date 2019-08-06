package com.xuwd.jvideoplay;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;

import java.io.IOException;

public class VideoPlayActivity extends AppCompatActivity {
    private MediaPlayer mMediaPlayer;
    private TextureView mTextureView;
    private SurfaceTexture mSurfaceTexture;

    String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);

        Intent intent=getIntent();
        filePath=intent.getStringExtra("filePath");

        mTextureView=findViewById(R.id.textureView);
        mTextureView.setSurfaceTextureListener(surfaceTextureListener);
    }

    private TextureView.SurfaceTextureListener surfaceTextureListener=new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            mSurfaceTexture=surfaceTexture;
            initMediaPlayer();
            playVideo(filePath);
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

    public void playVideo(String file){
        mMediaPlayer.reset();
        try {
            mMediaPlayer.setDataSource(file); ;
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMediaPlayer.start();
    }

    private void initMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            // mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        mMediaPlayer.setSurface(new Surface(mSurfaceTexture));
    }
}
