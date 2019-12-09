package com.example.myapplication;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import static com.example.myapplication.Util.getRecycleViewHorizontal;


public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder>{

    private final String TAG="Alan";
    private ArrayList<String> mData;

    RecycleViewAdapter(ArrayList<String> data) {
        this.mData = data;
    }

    public void updateData(ArrayList<String> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 实例化展示的view
        View v;
        if (getRecycleViewHorizontal()) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_item_horizontal, parent, false);
        }
        else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_item_vertical, parent, false);
        }

        // 实例化viewholder
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int itemId=position;

        // 綁定數據
        holder.mTitle.setText(mData.get(position));

        // 設定觸發
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w(TAG, "item:"+mData.get(itemId));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView mTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.item_tv);
            mTitle.setSelected(true); // 跑馬燈
        }
    }

    public void addNewItem(){
        if (mData == null){
            mData = new ArrayList<>();
        }
        mData.add(0, "new Item");
        notifyItemInserted(0);
    }

    public void deleteItem() {
        if(mData == null || mData.isEmpty()) {
            return;
        }
        mData.remove(0);
        notifyItemRemoved(0);
    }
}
