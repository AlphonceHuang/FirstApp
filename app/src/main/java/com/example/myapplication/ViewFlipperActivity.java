package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import static java.lang.Math.min;

public class ViewFlipperActivity extends AppCompatActivity {

    private static final String TAG="Alan";

    private ViewFlipper vflp_help;
    private final static int MIN_MOVE = 200;   //最小距离
    private GestureDetector mDetector;

    private int[] iconId, strId;

    //private int[] resId = {R.drawable.kitty001,
    //        R.drawable.kitty002,
    //        R.drawable.kitty003,
    //        R.drawable.kitty004,
    //        R.drawable.kitty005};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_flipper);

        MyGestureListener mgListener = new MyGestureListener();
        //手势检测类
        mDetector = new GestureDetector(this, mgListener);

        vflp_help = findViewById(R.id.main_vf);

        // 取Array裡資料轉成int
        TypedArray iconArray = getResources().obtainTypedArray(R.array.kitty_icon);
        iconId = new int[iconArray.length()];
        for (int i=0; i<iconId.length; i++){
            iconId[i]=iconArray.getResourceId(i, 0);
        }
        iconArray.recycle();

        TypedArray strArray = getResources().obtainTypedArray(R.array.array_Food);
        strId = new int[strArray.length()];
        for (int i=0; i<strId.length; i++){
            strId[i]=strArray.getResourceId(i, 0);
        }
        strArray.recycle();

        int itemCount=min(iconId.length, strId.length);
        Log.w(TAG, "icon="+iconId.length+", text="+strId.length+", min="+itemCount);

        // 方法一：直接增加至view裡
        //for (int value : resId) {
        //    vflp_help.addView(getImageView(value));
        //}

        // 方法二：使用客制化layout，再一個一個加入view裡
        for (int value=0; value<itemCount; value++) {
            //View view = LayoutInflater.from(this).inflate(R.layout.fragment_main, null);
            //TextView tv_show = view.findViewById(R.id.fragment_title);
            //ImageView tv_showTwo = view.findViewById(R.id.fragment_image);

            //tv_show.setText(strId[value]);
            //tv_showTwo.setImageResource(iconId[value]);

            vflp_help.addView(getCustomView(value));
        }
    }

    private View getCustomView(int item){
        View view = LayoutInflater.from(this).inflate(R.layout.fragment_main, null);
        TextView tv_show = view.findViewById(R.id.fragment_title);
        ImageView tv_showTwo = view.findViewById(R.id.fragment_image);

        tv_show.setText(strId[item]);
        tv_showTwo.setImageResource(iconId[item]);
        return view;
    }

    private ImageView getImageView(int resId) {
        ImageView img = new ImageView(this);
        img.setBackgroundResource(resId);
        return img;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }

    private class MyGestureListener extends SimpleOnGestureListener{

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float v, float v1){
            if(e1.getX() - e2.getX() > MIN_MOVE){

                vflp_help.setInAnimation(ViewFlipperActivity.this ,R.anim.right_in);
                vflp_help.setOutAnimation(ViewFlipperActivity.this, R.anim.left_out);
                vflp_help.showNext();
            }else if(e2.getX() - e1.getX() > MIN_MOVE){

                vflp_help.setInAnimation(ViewFlipperActivity.this,R.anim.left_in);
                vflp_help.setOutAnimation(ViewFlipperActivity.this, R.anim.right_out);
                vflp_help.showPrevious();
            }
            return true;
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_flipperview, menu);

        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id==R.id.autochangeItem){
            if (item.isChecked()) {
                item.setChecked(false);
                if (vflp_help.isFlipping()) {
                    vflp_help.stopFlipping();   // 停止投影片播放
                }
            }else {
                item.setChecked(true);
                if (!vflp_help.isFlipping()) {
                    vflp_help.startFlipping();  // 開始投影片播放
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
