package com.example.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;


import static com.example.myapplication.Util.getRecycleViewStyle;
import static com.example.myapplication.Util.showToastIns;
import static com.example.myapplication.sRecycleViewStyle.*;

public class RecycleViewActivity extends AppCompatActivity implements View.OnClickListener {

    protected final String TAG="Alan";

    private RecyclerView.Adapter mAdapter;
    private Button mAddItemBtn, mDelItemBtn;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<String> listArray;
    private int newItemCount=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 這裡分不同layout主要是因為scrollbars方向不同(Vertical & Horizontal)
        String mTitle=getString(R.string.recycleView);
        switch(getRecycleViewStyle()){
            case sLinear_Layout_Vertical:
                setContentView(R.layout.activity_recycle_view_v);
                mTitle = getString(R.string.recycleView)+":"+getString(R.string.vertical);
                break;
            case sLinear_Layout_Horizontal:
                setContentView(R.layout.activity_recycle_view_h);
                mTitle = getString(R.string.recycleView)+":"+getString(R.string.horizontal);
                break;
            case sGrid_Layout:
                setContentView(R.layout.activity_recycle_view_v);
                mTitle = getString(R.string.recycleView)+":"+getString(R.string.grid);
                break;
            case sStaggered_Grid_Vertical:
                setContentView(R.layout.activity_recycle_view_v);
                mTitle = getString(R.string.recycleView)+":"+getString(R.string.stagger_V);
                break;
            case sStaggered_Grid_Horizontal:
                setContentView(R.layout.activity_recycle_view_h);
                mTitle = getString(R.string.recycleView)+":"+getString(R.string.stagger_H);
                break;
            default:
                setContentView(R.layout.activity_recycle_view_v);
                break;
        }
        // 根據不同設計修改title名稱
        setTitle(mTitle);

        initData();
        initView();
        initAction();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.addButton) {
            newItemCount++;
            String str=getString(R.string.new_item)+newItemCount;
            ((RecycleViewAdapter) mAdapter).addNewItem(str);
            mLayoutManager.scrollToPosition(0);
        } else if (id == R.id.deleteButton) {
            ((RecycleViewAdapter) mAdapter).deleteItem();
            mLayoutManager.scrollToPosition(0);
            if (newItemCount>0)
                newItemCount--;
        }
        // 更新資料
        listArray = ((RecycleViewAdapter) mAdapter).getListData();
        ((RecycleViewAdapter) mAdapter).updateData(listArray);

    }

    private void initData() {
        if (getRecycleViewStyle()==sLinear_Layout_Horizontal) {
            mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        }else if (getRecycleViewStyle()==sGrid_Layout){
            mLayoutManager = new GridLayoutManager(this, 2);
        }else if (getRecycleViewStyle()==sStaggered_Grid_Vertical) {
            mLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        }else if (getRecycleViewStyle()==sStaggered_Grid_Horizontal) {
            mLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.HORIZONTAL);
        }else{
            mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        }

        listArray = getData();
        mAdapter = new RecycleViewAdapter(listArray);

        ((RecycleViewAdapter) mAdapter).setOnItemClickListener(new RecycleViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                showToastIns(RecycleViewActivity.this,"click " + listArray.get(position), Toast.LENGTH_SHORT);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                showToastIns(RecycleViewActivity.this,"long click " + listArray.get(position), Toast.LENGTH_SHORT);
            }
        });
    }

    private void initView() {
        RecyclerView mRecyclerView = findViewById(R.id.RecycleView);

        mAddItemBtn = findViewById(R.id.addButton);
        mDelItemBtn = findViewById(R.id.deleteButton);

        // 設置 layout manager
        mRecyclerView.setLayoutManager(mLayoutManager);

        // 設置 adapter
        mRecyclerView.setAdapter(mAdapter);

        // 設置分隔線
        MyDividerItemDecoration divider_Horizontal = new MyDividerItemDecoration(this, LinearLayoutManager.HORIZONTAL);
        MyDividerItemDecoration divider_Vertical = new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL);

        if (getRecycleViewStyle()==sLinear_Layout_Horizontal || getRecycleViewStyle()==sStaggered_Grid_Horizontal)
            mRecyclerView.addItemDecoration(divider_Horizontal);
        else
            mRecyclerView.addItemDecoration(divider_Vertical);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void initAction() {
        mAddItemBtn.setOnClickListener(this);
        mDelItemBtn.setOnClickListener(this);
    }

    private ArrayList<String> getData() {
        ArrayList<String> data = new ArrayList<>();

        String[] str=getResources().getStringArray(R.array.array_Food);

        for(int i=0; i<str.length; i++) {
            data.add(str[i]);
        }
        return data;
    }
}
