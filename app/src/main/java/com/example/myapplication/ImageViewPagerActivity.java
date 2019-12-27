package com.example.myapplication;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.io.File;

import static com.example.myapplication.StorageUtil.getImages;


public class ImageViewPagerActivity extends AppCompatActivity {

    private TypedArray IconList;
    private String[] IconString;
    private File[] files;
    private int ViewPagerStyle=0;

    private static final String TAG="Alan";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view_pager);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            ViewPagerStyle = bundle.getInt("VIEWPAGE_STYLE");
        }
        //Log.w(TAG, "ViewPagerStyle:"+ViewPagerStyle);

        ViewPager pager = findViewById(R.id.viewPager);
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (ViewPagerStyle!=1) {
            IconList.recycle();
        }
    }

    //private File[] getImages(String path){
    //    File folder= new File(path);
    //    if (folder.isDirectory()){
    //        File[] fs=folder.listFiles(imageFilter);
    //        return fs;
    //    }
    //    return null;
    //}

    private class MyPagerAdapter extends FragmentPagerAdapter {

        MyPagerAdapter(FragmentManager fm) {
            super(fm);

            if (ViewPagerStyle==1) {
                String folder = Environment.getExternalStorageDirectory() + "/DCIM/100ANDRO/";
                files = getImages(folder);
            }else {
                IconString = getResources().getStringArray(R.array.array_icon_name);
                IconList = getResources().obtainTypedArray(R.array.array_icon);
            }
        }

        @Override
        public Fragment getItem(int i) {
            //Log.w(TAG, "getItem:"+ViewPagerStyle);

            if (ViewPagerStyle==1)
                return FragmentViewPager.newInstance1(ViewPagerStyle, files[i].getName(), files[i].getPath());
            else
                return FragmentViewPager.newInstance(ViewPagerStyle, IconString[i], IconList.getResourceId(i, 0));
        }

        @Override
        public int getCount() {
            if (ViewPagerStyle==1){
                return files.length;
            }else {
                return IconString.length;
            }
        }
    }
}
