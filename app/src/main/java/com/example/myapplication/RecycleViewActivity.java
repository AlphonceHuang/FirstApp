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

        if (getRecycleViewStyle()==1) {
            setContentView(R.layout.activity_recycle_view_h);
        }else{
            setContentView(R.layout.activity_recycle_view_v);
        }

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
        if (getRecycleViewStyle()==1) {
            mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        }else if (getRecycleViewStyle()==2){
            mLayoutManager = new GridLayoutManager(this, 2);
        }else if (getRecycleViewStyle()==3) {
            mLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        }else if (getRecycleViewStyle()==4) {
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

        if (getRecycleViewStyle()==1)
            mRecyclerView.addItemDecoration(divider_Horizontal);
        else if (getRecycleViewStyle()==0)
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
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_listview3, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
*/
}
