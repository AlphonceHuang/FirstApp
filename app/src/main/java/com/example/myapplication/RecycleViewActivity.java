package com.example.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;


import static com.example.myapplication.Util.getRecycleViewHorizontal;

public class RecycleViewActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView.Adapter mAdapter;
    private boolean HorizontalStyle=false;
    private Button mAddItemBtn, mDelItemBtn;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getRecycleViewHorizontal()) {
            setContentView(R.layout.activity_recycle_view_h);
            HorizontalStyle = true;
        }else {
            setContentView(R.layout.activity_recycle_view_v);
            HorizontalStyle = false;
        }

        initData();
        initView();
        initAction();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.addButton) {
                ((RecycleViewAdapter) mAdapter).addNewItem();
                mLayoutManager.scrollToPosition(0);
        } else if (id == R.id.deleteButton) {
                ((RecycleViewAdapter) mAdapter).deleteItem();
                mLayoutManager.scrollToPosition(0);
        }
    }

    private void initData() {
        if (HorizontalStyle) {
            mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        }
        else {
            mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        }

        mAdapter = new RecycleViewAdapter(getData());
/*
        ((RecycleViewAdapter) mAdapter).setOnItemClickListener(new RecycleViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(RecycleViewActivity.this,"click " + position + " item", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View view, int position) {
                Toast.makeText(RecycleViewActivity.this,"long click " + position + " item", Toast.LENGTH_SHORT).show();
            }
        });*/
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
        if (HorizontalStyle)
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

        String[] str=getResources().getStringArray(R.array.array_Places);

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
