package com.example.tsai.screenrecorder;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.VideoView;

import java.io.File;


public class VideoShow extends AppCompatActivity {

    VideoView playList;
    private Button goBack;
    MediaController mController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_show);

//        ScrollView scrollView = (ScrollView) findViewById(R.id.)
//
//        scrollView.post(new Runnable() {
//            public void run() {
//                scrollView.fullScroll(View.FOCUS_DOWN);
//            }
//        });

        goBack = (Button) findViewById(R.id.goBack);

        goBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });













        playList = findViewById(R.id.playList);

        mController = new MediaController(this);


        Uri uri=Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.video_1529442656);

        playList.setVideoURI(uri);
        //playList.setVideoPath(path);

        mController.setAnchorView(playList);

        playList.setMediaController(mController);
        playList.requestFocus();
        playList.start();


    }
}
