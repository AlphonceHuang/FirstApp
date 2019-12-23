package com.example.myapplication;
/*
    Author: Alan Huang
*/

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MyExpandableListViewAdapter extends BaseExpandableListAdapter {
    private static final String TAG="Alan";

    private Context mContext = null;
    private List<String> mGroupList = null;
    private List<List<MyBaseAdapterData>> mItemList = null;

    MyExpandableListViewAdapter(Context context, List<String>grouplist, List<List<MyBaseAdapterData>>itemList){
        this.mContext=context;
        this.mGroupList=grouplist;
        this.mItemList=itemList;
    }

    @Override
    public int getGroupCount() {
        return mGroupList.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return mItemList.get(i).size();
    }

    @Override
    public Object getGroup(int i) {
        return mGroupList.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return mItemList.get(i).get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        GroupHolder groupHolder=null;
        if (view == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.expendlist_group, null);
            groupHolder=new GroupHolder();
            groupHolder.groupNameTv=view.findViewById(R.id.groupname_tv);
            groupHolder.groupImg=view.findViewById(R.id.group_img);
            view.setTag(groupHolder);
        }else{
            groupHolder=(GroupHolder) view.getTag();
        }

        //Log.w(TAG, "getGroupView: i="+i+", b="+b);

        if (b){
            groupHolder.groupImg.setImageResource(R.drawable.down); // 展開
        }else{
            groupHolder.groupImg.setImageResource(R.drawable.right); // 未展開
        }
        groupHolder.groupNameTv.setText(mGroupList.get(i));

        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        ItemHolder itemHolder=null;
        if (view ==null){
            view = LayoutInflater.from(mContext).inflate(R.layout.expendlist_item, null);
            itemHolder=new ItemHolder();
            itemHolder.nameTv = view.findViewById(R.id.itemname_tv);
            itemHolder.iconImg=view.findViewById(R.id.icon_img);
            itemHolder.info=view.findViewById(R.id.info_tv);
            view.setTag(itemHolder);

        }else{
            itemHolder=(ItemHolder) view.getTag();
        }
        itemHolder.nameTv.setText(mItemList.get(i).get(i1).getItemTitle());
        //Log.w(TAG, "adapter:"+mItemList.get(i).get(i1).getItemTitle());
        itemHolder.info.setText(mItemList.get(i).get(i1).getItemSubtitle());
        itemHolder.iconImg.setImageResource(mItemList.get(i).get(i1).getItemIcon());

        itemHolder.nameTv.setSelected(true);
        itemHolder.info.setSelected(true);

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    class GroupHolder {
        TextView groupNameTv;
        ImageView groupImg;
    }

    class ItemHolder {
        ImageView iconImg;
        TextView nameTv;
        TextView info;
    }
}
