package com.example.myapplication;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.io.File;

import static com.example.myapplication.StorageUtil.checkSDCard;
import static com.example.myapplication.StorageUtil.imageFilter;
import static com.example.myapplication.Util.showToastIns;

public class ImageSwitcherActivity extends AppCompatActivity implements ViewSwitcher.ViewFactory, View.OnTouchListener {

    private static final String TAG="Alan";
    private ImageSwitcher mImageSwitcher;
    private File[] files;
    private int currentPosition;
    private float downX;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_switcher);

        mImageSwitcher = findViewById(R.id.imageswitcher);
        mImageSwitcher.setFactory(this);
        mImageSwitcher.setOnTouchListener(this);

        LinearLayout linearLayout = findViewById(R.id.viewGroup);

        String default_path= Environment.getExternalStorageDirectory() + "/ScreenCap/";

        if (checkSDCard()) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File cfolder=new File(default_path);
                if (cfolder.exists()) {
                    files = getImages(default_path);  // 將此路徑裡的相關檔案取出

                    if (files !=null) {
                        Log.w(TAG, "length:" + files.length);
                        ImageView[] tips = new ImageView[files.length];
                        for(int i=0; i<files.length; i++){
                            ImageView mImageView = new ImageView(this);
                            tips[i] = mImageView;
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                            layoutParams.rightMargin = 3;
                            layoutParams.leftMargin = 3;

                            linearLayout.addView(mImageView, layoutParams);
                        }
                        currentPosition = getIntent().getIntExtra("position", 0);
                        mImageSwitcher.setImageURI(Uri.fromFile(files[currentPosition]));
                    }
                }
            }
        }
    }

    private File[] getImages(String path){
        File folder= new File(path);
        if (folder.isDirectory()){
            File[] fs=folder.listFiles(imageFilter);
            return fs;
        }
        return null;
    }

    @Override
    public View makeView() {
        final ImageView i = new ImageView(this);
        i.setBackgroundColor(0xff000000);
        i.setScaleType(ImageView.ScaleType.CENTER_CROP);
        i.setLayoutParams(new ImageSwitcher.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        return i ;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:{
                //手指按下的X坐标
                downX = event.getX();
                break;
            }
            case MotionEvent.ACTION_UP:{
                float lastX = event.getX();
                //抬起的时候的X坐标大于按下的时候就显示上一张图片
                if(lastX > downX){
                    if(currentPosition > 0){
                        //设置动画，这里的动画比较简单，不明白的去网上看看相关内容
                        mImageSwitcher.setInAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.left_in));
                        mImageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.right_out));
                        currentPosition --;
                        mImageSwitcher.setImageURI(Uri.fromFile(files[currentPosition% files.length]));
                    }else{
                        showToastIns(getApplicationContext(), getString(R.string.first_image), Toast.LENGTH_SHORT);
                    }
                }

                if(lastX < downX){
                    if(currentPosition < files.length - 1){
                        mImageSwitcher.setInAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.right_in));
                        mImageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.left_out));
                        currentPosition ++ ;
                        mImageSwitcher.setImageURI(Uri.fromFile(files[currentPosition]));
                    }else{
                        showToastIns(getApplicationContext(), getString(R.string.last_image), Toast.LENGTH_SHORT);
                    }
                }
            }

            break;
        }
        return true;
    }
}
