package com.example.myapplication;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GridViewAdapter extends BaseAdapter {

    private LayoutInflater mLayInf;
    private List<Map<String, Object>> mItemList;
    private int mStyle=0;

    GridViewAdapter(Context context, List<Map<String, Object>> itemList, int Style){
        mLayInf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mItemList = itemList;
        mStyle = Style;
    }

    @Override
    public int getCount() {
        //取得 ListView 列表 Item 的數量
        return mItemList.size();
    }

    @Override
    public Object getItem(int i) {
        //取得 ListView 列表於 position 位置上的 Item
        return i;
    }

    @Override
    public long getItemId(int i) {
        //取得 ListView 列表於 position 位置上的 Item 的 ID
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        //設定與回傳 convertView 作為顯示在這個 position 位置的 Item 的 View。
        if (view ==null){
            view = mLayInf.inflate(R.layout.grid_view_item, viewGroup, false);
        }

        if (view !=null) {
            ImageView imgView = view.findViewById(R.id.gridImage);
            TextView txtView = view.findViewById(R.id.gridText);

            if (mStyle==2) {
                String path = Objects.requireNonNull(mItemList.get(i).get("ITEM_ICON1")).toString();
                imgView.setImageBitmap(BitmapFactory.decodeFile(path));
            }else{
                imgView.setImageResource(Integer.valueOf(Objects.requireNonNull(mItemList.get(i).get("ITEM_ICON1")).toString()));
            }
            txtView.setText(Objects.requireNonNull(mItemList.get(i).get("ITEM_TITLE1")).toString());

            // 設定這個才會有跑馬燈效果
            txtView.setSelected(true);
        }

        return view;
    }
}
