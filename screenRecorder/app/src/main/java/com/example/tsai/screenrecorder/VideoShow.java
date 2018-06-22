package com.example.tsai.screenrecorder;


import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.VideoView;


public class VideoShow extends AppCompatActivity {

    VideoView playList;
    private Button goBack;
    MediaController mController;
    ScrollView scroll_View;
    EditText mEdit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_show);

        goBack = findViewById(R.id.goBack);
        scroll_View = findViewById(R.id.scroll);
        playList = findViewById(R.id.playList);
        mEdit = findViewById(R.id.editText);

        //make the text in this activity not editable
        mEdit.setEnabled(false);

        //the keyboard will not come out when opening the activity
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //let the view start from the top instead of the bottom
        scroll_View.post(new Runnable() {
            @Override
            public void run() {
                scroll_View.fullScroll(ScrollView.FOCUS_UP);
            }
        });

        //when clicking the goBack button, the activity will be finished, which means go back to the last activity (home page)
        goBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });


        //use the media controller, so it can be more convenient when playing the video in this activity
        mController = new MediaController(this);

        //set the uri of the name of my video i wanna play
        Uri uri=Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.video_1529442656);

        playList.setVideoURI(uri);

        mController.setAnchorView(playList);

        playList.setMediaController(mController);
        playList.requestFocus();
        playList.start();

    }
}
