package com.example.myapplication;

import android.content.res.TypedArray;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.myapplication.Util.getRecycleViewStyle;
import static com.example.myapplication.Util.showToastIns;
import static com.example.myapplication.sRecycleViewStyle.*;

public class RecycleViewImgActivity extends AppCompatActivity implements View.OnClickListener{
    protected final String TAG="Alan";

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TypedArray IconList, TitleList, SubTitleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle_view_img);

        // Title text
        String mTitle;
        switch(getRecycleViewStyle()){
            case sLinear_Layout_Vertical_Image:
                mTitle = getString(R.string.recycleView) + ":" + getString(R.string.vertical_img);
                break;
            case sLinear_Layout_Horizontal_Image:
                mTitle = getString(R.string.recycleView) + ":" + getString(R.string.horizontal_img);
                break;
            case sGrid_Layout_Image:
                mTitle = getString(R.string.recycleView) + ":" + getString(R.string.grid_img);
                break;
            case sStaggered_Grid_Vertical_Image:
                mTitle = getString(R.string.recycleView) + ":" + getString(R.string.stagger_VImg);
                break;
            case sStaggered_Grid_Horizontal_Image:
                mTitle = getString(R.string.recycleView) + ":" + getString(R.string.stagger_HImg);
                break;
            case sCardView_Linear_Vertical:
                mTitle = getString(R.string.recycleView) + " + " + getString(R.string.cardview_v);
                break;
            case sCardView_Linear_Horizontal:
                mTitle = getString(R.string.recycleView) + " + " + getString(R.string.cardview_h);
                break;
            case sCardView_Grid:
                mTitle = getString(R.string.recycleView) + " + " + getString(R.string.cardview_grid);
                break;
            case sCardView_Stagger_Vertical:
                mTitle = getString(R.string.recycleView) + " + " + getString(R.string.cardview_stagger_V);
                break;
            case sCardView_Stagger_Horizontal:
                mTitle = getString(R.string.recycleView) + " + " + getString(R.string.cardview_stagger_H);
                break;
            default:
                mTitle = getString(R.string.recycleView);
                break;
        }
        setTitle(mTitle);

        initData();
        initView();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        IconList.recycle();
        TitleList.recycle();
        SubTitleList.recycle();
    }

    @Override
    public void onClick(View view) {

    }

    private void initData() {

        switch(getRecycleViewStyle()){

            case sLinear_Layout_Horizontal_Image:
            case sCardView_Linear_Horizontal:
                mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                break;

            case sGrid_Layout_Image:
            case sCardView_Grid:
                mLayoutManager = new GridLayoutManager(this, 2);
                break;

            case sStaggered_Grid_Vertical_Image:
                mLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
                break;
            case sCardView_Stagger_Vertical:
                mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                break;

            case sStaggered_Grid_Horizontal_Image:
            case sCardView_Stagger_Horizontal:
                mLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.HORIZONTAL);
                break;

            case sLinear_Layout_Vertical_Image:
            case sCardView_Linear_Vertical:
            default:
                mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                break;
        }

        final List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();

        IconList = getResources().obtainTypedArray(R.array.array_icon);
        TitleList = getResources().obtainTypedArray(R.array.array_Food);
        SubTitleList = getResources().obtainTypedArray(R.array.array_Places);
        //final String[] str=getResources().getStringArray(R.array.array_Food);
        //String[] str1=getResources().getStringArray(R.array.array_Places);

        //for (int i = 0; i < str.length; i++) {
        for (int i = 0; i < TitleList.length(); i++) {
            Map<String, Object> item = new HashMap<>();
            //item.put("ITEM_TITLE1", str[i]);
            //item.put("ITEM_TITLE2", str1[i]);
            item.put("ITEM_TITLE1", getString(TitleList.getResourceId(i, 0)));
            item.put("ITEM_TITLE2", getString(SubTitleList.getResourceId(i, 0)));
            item.put("ITEM_ICON1", IconList.getResourceId(i, 0));
            itemList.add(item);
        }
        mAdapter = new RecycleViewImgAdapter(itemList);

        ((RecycleViewImgAdapter) mAdapter).setOnItemClickListener(new RecycleViewImgAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //showToastIns(RecycleViewImgActivity.this,"click: " + str[position], Toast.LENGTH_SHORT);
                showToastIns(RecycleViewImgActivity.this,"click: " + getString(TitleList.getResourceId(position, 0)), Toast.LENGTH_SHORT);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                //showToastIns(RecycleViewImgActivity.this,"long click: " + str[position], Toast.LENGTH_SHORT);
                showToastIns(RecycleViewImgActivity.this,"long click: " + getString(TitleList.getResourceId(position, 0)), Toast.LENGTH_SHORT);
            }
        });
    }

    private void initView() {
        RecyclerView mRecyclerView = findViewById(R.id.RecycleView);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        // 設置分隔線
        switch(getRecycleViewStyle()){
            case sLinear_Layout_Horizontal_Image:
            case sStaggered_Grid_Horizontal_Image:
            case sCardView_Linear_Horizontal:
            case sCardView_Stagger_Horizontal:
                mRecyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.HORIZONTAL));
                break;
            default:
                mRecyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL));
                break;
        }

        // 設置動畫
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }
}
