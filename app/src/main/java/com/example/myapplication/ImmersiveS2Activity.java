package com.example.myapplication;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import static com.example.myapplication.StorageUtil.savePic;
import static com.example.myapplication.Util.showToastIns;
import static com.example.myapplication.Util.takeScreenShot;

public class ImmersiveS2Activity extends AppCompatActivity {
    private static final String TAG="Alan";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_immersive_s2);

        // 隱藏status bar
        //View decorView = getWindow().getDecorView();
        //int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
        //decorView.setSystemUiVisibility(option);

        // 隱藏title bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();
    }

    @Override
    protected void onResume(){
        super.onResume();
        // 隱藏status bar
        View decorView = getWindow().getDecorView();
        int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(option);
    }
}
