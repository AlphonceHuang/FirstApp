package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ListAdapter extends BaseAdapter
{
    private LayoutInflater mLayInf;
    private List<Map<String, Object>> mItemList;
    ListAdapter(Context context, List<Map<String, Object>> itemList)
    {
        mLayInf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //mLayInf = LayoutInflater.from(context); //簡單寫法，跟上面功能一樣
        mItemList = itemList;
    }

    @Override
    public int getCount()
    {
        //取得 ListView 列表 Item 的數量
        return mItemList.size();
    }

    @Override
    public Object getItem(int position)
    {
        //取得 ListView 列表於 position 位置上的 Item
        return position;
    }

    @Override
    public long getItemId(int position)
    {
        //取得 ListView 列表於 position 位置上的 Item 的 ID
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        //設定與回傳 convertView 作為顯示在這個 position 位置的 Item 的 View。
        View v = mLayInf.inflate(R.layout.list_view_item, parent, false);

        ImageView imgView = v.findViewById(R.id.imgView);
        TextView txtView = v.findViewById(R.id.txtView);
        TextView txtView1 = v.findViewById(R.id.txtView1);

        imgView.setImageResource(Integer.valueOf(Objects.requireNonNull(mItemList.get(position).get("Item icon")).toString()));
        txtView.setText(Objects.requireNonNull(mItemList.get(position).get("Item title")).toString());
        txtView1.setText(Objects.requireNonNull(mItemList.get(position).get("Item title1")).toString());

        // 設定這個才會有跑馬燈效果
        txtView.setSelected(true);
        txtView1.setSelected(true);

        return v;
    }
}
