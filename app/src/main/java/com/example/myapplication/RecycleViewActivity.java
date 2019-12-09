package com.example.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;


import static com.example.myapplication.Util.getRecycleViewHorizontal;

public class RecycleViewActivity extends AppCompatActivity {
    private RecyclerView.Adapter mAdapter;
    private boolean HorizontalStyle=false;

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
    }

    private void initData() {
        mAdapter = new RecycleViewAdapter(getData());
    }

    private void initView() {
        RecyclerView mRecyclerView = findViewById(R.id.RecycleView);

        RecyclerView.LayoutManager mLayoutManager;
        if (HorizontalStyle) {
            mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);


        }
        else {
            mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        }

        // 設置 layout manager
        mRecyclerView.setLayoutManager(mLayoutManager);
        // 設置 adapter
        mRecyclerView.setAdapter(mAdapter);
        // 設置分隔線
        if (HorizontalStyle)
            mRecyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.HORIZONTAL));
        else
            mRecyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL));
    }

    private ArrayList<String> getData() {
        ArrayList<String> data = new ArrayList<>();

        String[] str=getResources().getStringArray(R.array.array_Places);

        for(int i=0; i<str.length; i++) {
            data.add(str[i]);
        }
        return data;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_listview3, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
