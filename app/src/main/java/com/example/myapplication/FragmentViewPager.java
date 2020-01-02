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
    //private static final String TAG="Alan";

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

        int style = getArguments().getInt("style");
        //Log.w(TAG, "FragmentViewPager style="+style);

        if (style ==1) {

            Bitmap bp = BitmapFactory.decodeFile(getArguments().getString("path"));
            imageView.setImageBitmap(bp);

        }else {

            //Bitmap bp = BitmapFactory.decodeResource(this.getResources(), getArguments().getInt("img"));
            //Drawable drawbale = getResources().getDrawable(getArguments().getInt("img"));

            // 使用setBackgroundResource時比例會因為background的關系而被拉伸
            //imageView.setBackgroundResource(getArguments().getInt("img"));

            // 使用setImageResource，則維持原圖比例
            imageView.setImageResource(getArguments().getInt("img"));

            // 這個比例就對了
            //DrawableUtils.UseBitmap(v.getContext(), imageView, getArguments().getInt("img"));

            // 圓角
            //imageView.setImageBitmap(DrawableUtils.SetRoundCornerBitmap(bp, 60));

            // 縮放
            //imageView.setImageBitmap(DrawableUtils.ZoomBitmap(bp, 450, 450));

            // 倒影
            //imageView.setImageBitmap(DrawableUtils.CreateReflectionImageWithOrigin(bp));

            // drawable進行縮放
            //imageView.setImageDrawable(DrawableUtils.ZoomDrawable(drawbale, 1500, 1500));
        }
        return v;
    }

    public static FragmentViewPager newInstance(int style, String text, int image) {

        FragmentViewPager f = new FragmentViewPager();

        Bundle b = new Bundle();
        b.putInt("style", style);
        b.putString("text", text);  // text string
        b.putInt("img", image); // image resource id

        f.setArguments(b);
        return f;
    }

    public static FragmentViewPager newInstance1(int style, String text, String path){
        FragmentViewPager f = new FragmentViewPager();

        Bundle b = new Bundle();
        b.putInt("style", style);
        b.putString("text", text);  // text string
        b.putString("path", path);  // image path

        f.setArguments(b);
        return f;
    }
}
