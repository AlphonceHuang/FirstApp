package com.example.myapplication;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.myapplication.Util.showToastIns;

public class ExpendListActivity extends AppCompatActivity {
    //private static final String TAG="Alan";

    // 列表数据
    private List<String> mGroupNameList = null;
    private List<List<MyBaseAdapterData>> mItemNameList = null;
    private MyExpandableListViewAdapter mAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expend_list);

        // 獲得組件
        ExpandableListView mExpandableListView = findViewById(R.id.expendlist);
        mExpandableListView.setGroupIndicator(null);

        // 初始化數據
        initialData();

        mAdapter = new MyExpandableListViewAdapter(this, mGroupNameList, mItemNameList);
        mExpandableListView.setAdapter(mAdapter);

        // Group item click event
        mExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                showToastIns(ExpendListActivity.this, mAdapter.getGroup(i).toString(),Toast.LENGTH_SHORT);
                return mGroupNameList.get(i).isEmpty();
            }
        });

        // Child item click event
        mExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                showToastIns(ExpendListActivity.this,
                        mAdapter.getGroup(i) + ":" +  mItemNameList.get(i).get(i1).getItemTitle() ,Toast.LENGTH_SHORT);
                return false;
            }
        });
    }

    private void initialData(){
        mGroupNameList = new ArrayList<>();

        // Title group
        String[] groups = getResources().getStringArray(R.array.group_title);
        mGroupNameList.addAll(Arrays.asList(groups));

        List<MyBaseAdapterData> datas = new ArrayList<>();
        mItemNameList = new ArrayList<>();

        // Group1
        TypedArray regionIconList = getResources().obtainTypedArray(R.array.group_icons);
        int[] ids = new int[regionIconList.length()];
        for (int i=0; i<ids.length; i++){
            ids[i]=regionIconList.getResourceId(i, 0);
        }
        String[] title=getResources().getStringArray(R.array.group_king);
        String[] subtitle=getResources().getStringArray(R.array.group_king_info);

        for (int i=0; i<title.length; i++) {
            datas.add(new MyBaseAdapterData(ids[i], title[i], subtitle[i]));
        }
        mItemNameList.add(datas);


        // Group2
        datas = new ArrayList<>();
        regionIconList = getResources().obtainTypedArray(R.array.group_icons);
        ids = new int[regionIconList.length()];
        for (int i=0; i<ids.length; i++){
            ids[i]=regionIconList.getResourceId(i, 0);
        }
        title=getResources().getStringArray(R.array.group_chinese_star);
        subtitle=getResources().getStringArray(R.array.group_chinese_star_info);

        for (int i=0; i<title.length; i++) {
            datas.add(new MyBaseAdapterData(ids[i], title[i], subtitle[i]));
        }
        mItemNameList.add(datas);


        // Group3
        datas = new ArrayList<>();
        regionIconList = getResources().obtainTypedArray(R.array.group_icons);
        ids = new int[regionIconList.length()];
        for (int i=0; i<ids.length; i++){
            ids[i]=regionIconList.getResourceId(i, 0);
        }
        title=getResources().getStringArray(R.array.group_Foreigner_star);
        subtitle=getResources().getStringArray(R.array.group_Foreigner_star_info);

        for (int i=0; i<title.length; i++) {
            datas.add(new MyBaseAdapterData(ids[i], title[i], subtitle[i]));
        }
        mItemNameList.add(datas);


        // Group4
        datas = new ArrayList<>();
        regionIconList = getResources().obtainTypedArray(R.array.group_icons);
        ids = new int[regionIconList.length()];
        for (int i=0; i<ids.length; i++){
            ids[i]=regionIconList.getResourceId(i, 0);
        }
        title=getResources().getStringArray(R.array.group_political);
        subtitle=getResources().getStringArray(R.array.group_information);

        for (int i=0; i<title.length; i++) {
            datas.add(new MyBaseAdapterData(ids[i], title[i], subtitle[i]));
        }
        mItemNameList.add(datas);


        regionIconList.recycle();
    }
}
