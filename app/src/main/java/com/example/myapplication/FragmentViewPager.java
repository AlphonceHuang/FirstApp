package com.example.myapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class FragmentViewPager extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_main, container, false);

        TextView tv =  v.findViewById(R.id.fragment_title);
        if (getArguments() != null) {
            tv.setText(getArguments().getString("text"));
        }

        // R.layout.fragment_main不能先放圖片，不然會被重疊
        ImageView imageView = v.findViewById(R.id.fragment_image);
        imageView.setBackgroundResource(getArguments().getInt("img"));

        return v;
    }

    public static FragmentViewPager newInstance(String text, int image) {

        FragmentViewPager f = new FragmentViewPager();

        Bundle b = new Bundle();
        b.putString("text", text);
        b.putInt("img", image);

        f.setArguments(b);
        return f;
    }
}
