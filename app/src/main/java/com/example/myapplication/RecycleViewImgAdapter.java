package com.example.myapplication;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;
import java.util.Objects;


public class RecycleViewImgAdapter extends RecyclerView.Adapter<RecycleViewImgAdapter.ViewHolder>{

    private final String TAG="Alan";
    private List<Map<String, Object>> mData;
    private RecycleViewImgAdapter.OnItemClickListener onItemClickListener;

    RecycleViewImgAdapter(List<Map<String, Object>> data) {
        this.mData = data;
    }

    List<Map<String, Object>> getListData(){
        return mData;
    }

    public void updateData(List<Map<String, Object>> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(RecycleViewImgAdapter.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 实例化展示的view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_image_item, parent, false);

        // 实例化viewholder
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // 綁定數據
        holder.mIcon.setImageResource(Integer.valueOf(Objects.requireNonNull(mData.get(position).get("ITEM_ICON1")).toString()));
        holder.mTitle.setText(Objects.requireNonNull(mData.get(position).get("ITEM_TITLE1")).toString());
        holder.mSubTitle.setText(Objects.requireNonNull(mData.get(position).get("ITEM_TITLE2")).toString());

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

        TextView mTitle, mSubTitle;
        ImageView mIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.txtView);
            mSubTitle = itemView.findViewById(R.id.txtView1);
            mIcon = itemView.findViewById(R.id.imgView);
            mTitle.setSelected(true); // 跑馬燈
            mSubTitle.setSelected(true);    // 跑馬燈
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

}

