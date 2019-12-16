package com.example.myapplication;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

import static com.example.myapplication.StorageUtil.checkSDCard;
import static com.example.myapplication.StorageUtil.imageFilter;

public class ScreenCapActivity extends AppCompatActivity {

    private ImageView capimg;
    private static final String TAG="Alan";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_cap);

        capimg=findViewById(R.id.capImg);
        if (readlastImg()) {
            capimg.setVisibility(View.VISIBLE);
        }else{
            capimg.setVisibility(View.INVISIBLE);
        }

    }

    private boolean readlastImg(){

        File[] files;
        String default_path=Environment.getExternalStorageDirectory() + "/ScreenCap/";

        if (checkSDCard()) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File cfolder=new File(default_path);
                if (cfolder.exists()) {
                    files = getImages(default_path);  // 將此路徑裡的相關檔案取出
                    if (files !=null) {
                        Log.w(TAG, "length:" + files.length);

                        capimg.setImageURI(Uri.fromFile(files[files.length-1]));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private File[] getImages(String path){
        File folder= new File(path);
        if (folder.isDirectory()){
            File[] fs=folder.listFiles(imageFilter);
            return fs;
        }
        return null;
    }
}
