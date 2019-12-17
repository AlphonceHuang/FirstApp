package com.example.myapplication;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class ImageViewPagerActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view_pager);

        ViewPager pager = findViewById(R.id.viewPager);
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i==1){
                return FragmentViewPager.newInstance(getString(R.string.bee), R.drawable.bee);
            }else if (i==2){
                return FragmentViewPager.newInstance(getString(R.string.burger), R.drawable.burger);
            }else if (i==0){
                return FragmentViewPager.newInstance(getString(R.string.apple), R.drawable.apple);
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
