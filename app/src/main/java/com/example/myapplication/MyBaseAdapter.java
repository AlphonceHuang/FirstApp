package com.example.myapplication;
/*
    Author: Alan Huang
*/

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MyBaseAdapter extends BaseAdapter {
    private Context mContext;
    private List<MyBaseAdapterData> mDatas;
    private int mListId;
    private boolean useBuildHolder=true;

    MyBaseAdapter(Context context, List<MyBaseAdapterData> datas, int listid){
        this.mContext=context;
        this.mDatas=datas;
        this.mListId=listid;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public MyBaseAdapterData getItem(int i) {
        return mDatas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (useBuildHolder) {   // use build holder method

            ViewHolder holder = new ViewHolder();
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                view = inflater.inflate(mListId, null);

                holder.icon = view.findViewById(R.id.imgView);
                holder.title = view.findViewById(R.id.txtView);
                holder.subtitle = view.findViewById(R.id.txtView1);

                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            MyBaseAdapterData data = getItem(i);
            if (data != null) {
                holder.icon.setImageResource(data.getItemIcon());
                holder.title.setText(data.getItemTitle());
                holder.subtitle.setText(data.getItemSubtitle());

                holder.title.setSelected(true);
                holder.subtitle.setSelected(true);
            }
        }else {

            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                view = inflater.inflate(mListId, null);
            }

            ImageView icon = view.findViewById(R.id.imgView);
            TextView title = view.findViewById(R.id.txtView);
            TextView subtitle = view.findViewById(R.id.txtView1);

            MyBaseAdapterData data = getItem(i);
            if (data != null) {
                icon.setImageResource(data.getItemIcon());
                title.setText(data.getItemTitle());
                subtitle.setText(data.getItemSubtitle());

                title.setSelected(true);
                subtitle.setSelected(true);
            }
        }
        return view;
    }

    private class ViewHolder{
        ImageView icon;
        TextView title;
        TextView subtitle;
    }
}
