package com.example.myapplication;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import static com.example.myapplication.Util.getRecycleViewStyle;


public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder>{

    private final String TAG="Alan";
    private ArrayList<String> mData;
    private RecycleViewAdapter.OnItemClickListener onItemClickListener;

    RecycleViewAdapter(ArrayList<String> data) {
        this.mData = data;
    }

    ArrayList<String> getListData(){
        return mData;
    }

    public void updateData(ArrayList<String> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    public void addNewItem(String str){
        if (mData == null){
            mData = new ArrayList<>();
        }

        mData.add(0, str);
        notifyItemInserted(0);
    }

    public void deleteItem() {
        if(mData == null || mData.isEmpty()) {
            return;
        }
        mData.remove(0);
        notifyItemRemoved(0);
    }

    public void setOnItemClickListener(RecycleViewAdapter.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 实例化展示的view
        View v;
        if (getRecycleViewStyle()==1) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_item_horizontal, parent, false);
        }else if (getRecycleViewStyle()==2){
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_grid_item, parent, false);
        }else if (getRecycleViewStyle()==3 || getRecycleViewStyle()==4){
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_staggergrid_item, parent, false);
        }else{
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_item_vertical, parent, false);
        }

        // 实例化viewholder
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final int itemId=position;

        // 綁定數據
        holder.mTitle.setText(mData.get(position));

        // 設定短按觸發
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.w(TAG, "item:"+mData.get(itemId));

                if (onItemClickListener !=null){
                    int pos = holder.getLayoutPosition();
                    //Log.w(TAG, "pos:"+pos);
                    onItemClickListener.onItemClick(holder.itemView, pos);
                }
            }
        });
        // 設定長按觸發
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(onItemClickListener != null) {
                    int pos = holder.getLayoutPosition();
                    onItemClickListener.onItemLongClick(holder.itemView, pos);
                }
                return true;
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

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

}
