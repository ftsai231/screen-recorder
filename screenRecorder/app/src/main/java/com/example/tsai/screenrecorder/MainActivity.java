package com.example.tsai.screenrecorder;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;
import android.widget.VideoView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //"final" means that the values are constant and cannot be changed.
    //static final用来修饰成员变量和成员方法，可简单理解为“全局常量”！
    //对于变量，表示一旦给值就不可修改，并且通过类名可以访问。
    //对于方法，表示不可覆盖，并且可以通过类名直接访问。

    //The integer argument is a "request code" that identifies your request. When you receive the
    // result Intent, the callback provides the same request code so that your app can properly identify the
    // result and determine how to handle it.
    private static final int REQUEST_CODE = 1000;
    private static final int REQUEST_PERMISSION = 1001;
    private static final SparseIntArray ORIENTATION = new SparseIntArray();


    //mediaProjection:A token granting applications the ability to capture screen contents and/or record
    //system audio. The exact capabilities granted depend on the type of MediaProjection.
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    //Represents a virtual display. The content of a virtual display is rendered to a Surface that you must provide to createVirtualDisplay()
    private VirtualDisplay virtualDisplay;
    //MediaProjection.Callback:Callbacks for the projection session.
    private MediaProjectionCallback mediaProjectionCallback;
    private MediaRecorder mediaRecorder;

    private int mScreenDensity;
    private static int DISPLAY_WIDTH = 720;
    private static int DISPLAY_HEIGHT = 1280;

    //This is the block of code that will get invoked when your class is loaded by classloader
    //Classloader is a part of the Java Runtime Environment that dynamically loads Java classes into the Java Virtual Machine

    static {
        ORIENTATION.append(Surface.ROTATION_0,90);
        ORIENTATION.append(Surface.ROTATION_90,0);
        ORIENTATION.append(Surface.ROTATION_180,270);
        ORIENTATION.append(Surface.ROTATION_270,180);

    }

    //view
    private RelativeLayout rootLayout;
    private ToggleButton toggleButton;
    private VideoView videoView;
    private String videoUri="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics metrics = new DisplayMetrics();    //A structure describing general information about a display, such as its size, density, and font scaling
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;

        //Get Screen
        //DISPLAY_HEIGHT = metrics.heightPixels;
        //DISPLAY_WIDTH = metrics.widthPixels;

        mediaRecorder = new MediaRecorder();
        mediaProjectionManager = (MediaProjectionManager) getSystemService(getApplicationContext().MEDIA_PROJECTION_SERVICE);

        //view
        videoView = (VideoView) findViewById(R.id.videoView);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);

        //event
        toggleButton.setOnClickListener(new View.OnClickListener() {               //View.OnClickListener is actually OnClickListener itself
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        + ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED){
                    if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            ||
                            ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.RECORD_AUDIO)){
                        toggleButton.setChecked(false);
                        Snackbar.make(rootLayout, "Permissions", Snackbar.LENGTH_INDEFINITE)
                        .setAction("ENABLE", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                Manifest.permission.RECORD_AUDIO

                                        }, REQUEST_PERMISSION);
                            }
                        }).show();

                    }
                    else{
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.RECORD_AUDIO

                                }, REQUEST_PERMISSION);
                    }
                }
                else{
                    toggleScreenShare(v);
                    
                }
            }
        });
    }

    private void toggleScreenShare(View v) {
        if(((ToggleButton)v).isChecked()){          //isCheck():to check if android checkbox is checked within its onClick method (on vs. off)
            initRecorder();
            recordScreen();
        }
        else{
            mediaRecorder.stop();
            mediaRecorder.reset();
            stopRecordScreen();

            //play in video view
            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoURI(Uri.parse(videoUri));
            videoView.start();
        }
    }
//
//    public String getFilePath() {
//        final String directory = Environment.getExternalStorageDirectory() + File.separator + "Recordings";
//        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
//            Toast.makeText(this, "Failed to get External Storage", Toast.LENGTH_SHORT).show();
//            return null;
//        }
//        final File folder = new File(directory);
//        boolean success = true;
//        if (!folder.exists()) {
//            success = folder.mkdir();
//        }
//        String filePath;
//        if (success) {
//            String videoName = ("capture_" + getCurSysDate() + ".mp4");
//            filePath = directory + File.separator + videoName;
//        } else {
//            Toast.makeText(this, "Failed to create Recordings directory", Toast.LENGTH_SHORT).show();
//            return null;
//        }
//        return filePath;
//    }
//
//    public String getCurSysDate() {
//        return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
//    }


    private void initRecorder() {
        try{
            //setAudioSource: Sets the audio source to be used for recording
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
           // mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);


            videoUri = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + new StringBuilder("/EDMTRecord_").append(new SimpleDateFormat("dd-MM-yyyy-hh_mm_ss").format(new Date())).append("mp4").toString();

            mediaRecorder.setOutputFile(videoUri);
            mediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setVideoEncodingBitRate(512*1000);
            mediaRecorder.setVideoFrameRate(30);


            // getRotation: Returns the rotation of the screen from its "natural" orientation.
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATION.get(rotation + 90);
            mediaRecorder.setOrientationHint(orientation);
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void recordScreen() {
        if(mediaProjection==null){
            // startActivityForResult: Launch an activity for which you would like a result when it finished
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            return;
        }
        virtualDisplay = createVirtualDisplay();
        mediaRecorder.start();

    }

    //createVirtualDisplay: Creates a VirtualDisplay to capture the contents of the screen.
    private VirtualDisplay createVirtualDisplay() {
        return mediaProjection.createVirtualDisplay("MainActivity", DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder.getSurface(), null, null);
    }
    //getSurface: Gets the surface to record from when using SURFACE video source.

    //ctrl + o


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode != REQUEST_CODE){
            Toast.makeText(this, "Unk error", Toast.LENGTH_SHORT).show();
            return;
        }
        if(resultCode != RESULT_OK){
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            toggleButton.setChecked(false);
            return;
        }

        mediaProjectionCallback = new MediaProjectionCallback();
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
        mediaProjection.registerCallback(mediaProjectionCallback, null);
        virtualDisplay = createVirtualDisplay();
        mediaRecorder.start();
    }

    //MediaProjection.Callback:Callbacks for the projection session.
    private class MediaProjectionCallback extends MediaProjection.Callback{
        @Override
        public void onStop() {
            if(toggleButton.isChecked()){
                toggleButton.setChecked(false);
                mediaRecorder.stop();
                mediaRecorder.reset();
            }
            mediaProjection = null;
            stopRecordScreen();
            super.onStop();
        }
    }

    private void stopRecordScreen() {
        if(virtualDisplay == null){
            return;
        }
        virtualDisplay.release();   //Releases the virtual display and destroys its underlying surface.
        destroyMediaProjection();
    }

    private void destroyMediaProjection() {
        if(mediaProjection != null){
            mediaProjection.unregisterCallback(mediaProjectionCallback);
            //unregisterCallback: Unregisters the specified callback. If an update has already been posted you may still receive it after calling this method.
            mediaProjection.stop();
            mediaProjection = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_PERMISSION:
            {
                if((grantResults.length>0) && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)){
                    toggleScreenShare(toggleButton);
                }
                else
                {
                    toggleButton.setChecked(false);
                    Snackbar.make(rootLayout, "Permissions", Snackbar.LENGTH_INDEFINITE)
                            .setAction("ENABLE", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                    Manifest.permission.RECORD_AUDIO

                                            }, REQUEST_PERMISSION);
                                }
                            }).show();
                }
                return;
            }
        }
    }


}





















