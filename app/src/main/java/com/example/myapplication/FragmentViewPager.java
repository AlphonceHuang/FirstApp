package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class FragmentViewPager extends Fragment {
    private static final String TAG="Alan";
    private int style=0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_main, container, false);

        TextView tv =  v.findViewById(R.id.fragment_title);
        if (getArguments() != null) {
            tv.setText(getArguments().getString("text"));
        }
        //Log.w("Alan", "onCreateView");

        // R.layout.fragment_main不能先放圖片，不然會被重疊
        ImageView imageView = v.findViewById(R.id.fragment_image);

        style = getArguments().getInt("style");
        //Log.w(TAG, "FragmentViewPager style="+style);

        if (style==1) {
            Bitmap bp = BitmapFactory.decodeFile(getArguments().getString("path"));
            imageView.setImageBitmap(bp);
        }else {
            imageView.setBackgroundResource(getArguments().getInt("img"));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        return v;
    }

    public static FragmentViewPager newInstance(int style, String text, int image) {

        FragmentViewPager f = new FragmentViewPager();

        Bundle b = new Bundle();
        b.putInt("style", style);
        b.putString("text", text);
        b.putInt("img", image);

        f.setArguments(b);
        return f;
    }

    public static FragmentViewPager newInstance1(int style, String text, String path){
        FragmentViewPager f = new FragmentViewPager();

        Bundle b = new Bundle();
        b.putInt("style", style);
        b.putString("text", text);
        b.putString("path", path);

        f.setArguments(b);
        return f;
    }
}
