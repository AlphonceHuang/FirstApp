package com.example.myapplication;

import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.myapplication.StorageUtil.getRealPathFromURI;
import static com.example.myapplication.StorageUtil.imageFilter;
import static com.example.myapplication.Util.getRecycleViewStyle;
import static com.example.myapplication.Util.showToastIns;
import static com.example.myapplication.sRecycleViewStyle.*;

public class RecycleViewImgActivity extends AppCompatActivity implements View.OnClickListener{
    protected final String TAG="Alan";

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TypedArray IconList, TitleList, SubTitleList;
    private List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();
    private int newItemCount=0;

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
            case sCardView_Stagger_Vertical_Bitmap:
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
        if (getRecycleViewStyle()!=sCardView_Stagger_Vertical_Bitmap) {
            IconList.recycle();
            TitleList.recycle();
            SubTitleList.recycle();
        }
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
            case sCardView_Stagger_Vertical_Bitmap:
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

        //final List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();
/*
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

 */
        if (getRecycleViewStyle()==sCardView_Stagger_Vertical_Bitmap){
            mAdapter = new RecycleViewImgAdapter(getBitmapData());
        }else{
            mAdapter = new RecycleViewImgAdapter(getData());
        }

        ((RecycleViewImgAdapter) mAdapter).setOnItemClickListener(new RecycleViewImgAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                //Map<String, Object> item = new HashMap<>();
                //item = itemList.get(position);
                //Set<String> get = item.keySet();
                //Log.w(TAG, ""+item.get("ITEM_TITLE1"));

                showToastIns(RecycleViewImgActivity.this,"click: " +
                        itemList.get(position).get("ITEM_TITLE1"), Toast.LENGTH_SHORT);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                showToastIns(RecycleViewImgActivity.this,"long click: " +
                        itemList.get(position).get("ITEM_TITLE1"), Toast.LENGTH_SHORT);
                showPopupMenu(view, position);
            }
        });
    }

    private List<Map<String, Object>> getBitmapData() {
        String folder = Environment.getExternalStorageDirectory() + "/DCIM/100ANDRO/";
        File[] files = getImages(folder);
        if (files != null){
            for (File file : files) {
                Map<String, Object> item = new HashMap<>();
                item.put("ITEM_TITLE1", file.getName());
                //item.put("ITEM_TITLE1", str[i]);
                item.put("ITEM_ICON1", getRealPathFromURI(Uri.fromFile(file)));
                itemList.add(item);
            }
        }
        return itemList;
    }
    // 將取得的檔案放至array
    private File[] getImages(String path){
        File folder= new File(path);
        if (folder.isDirectory()){
            File[] fs=folder.listFiles(imageFilter);
            return fs;
        }
        return null;
    }


    private List<Map<String, Object>> getData() {
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
        return itemList;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_recycleview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.addItem:
                newItemCount++;
                ((RecycleViewImgAdapter) mAdapter).addNewItem(getString(R.string.new_item)+newItemCount, Integer.toString(newItemCount), 0);
                mLayoutManager.scrollToPosition(0);
                break;
            case R.id.deleteItem:
                if (newItemCount>0)
                    newItemCount--;
                ((RecycleViewImgAdapter) mAdapter).deleteItem(0);
                mLayoutManager.scrollToPosition(0);
                break;
        }
        // 更新itemList資料
        itemList = ((RecycleViewImgAdapter) mAdapter).getListData();
        ((RecycleViewImgAdapter) mAdapter).updateData(itemList);
        return super.onOptionsItemSelected(item);
    }

    private void showPopupMenu(View view, final int position){
        PopupMenu popupMenu = new PopupMenu(RecycleViewImgActivity.this, view);

        // 強制顯示Icon
        try {
            Method method = popupMenu.getMenu().getClass().getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            method.setAccessible(true);
            method.invoke(popupMenu.getMenu(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*  // 下面這種方法也可以用
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        */

        popupMenu.getMenuInflater().inflate(R.menu.menu_recycleview, popupMenu.getMenu());

        //final PopupWindow popup = new PopupWindow(view.getContext());
        //popup.setBackgroundDrawable(new BitmapDrawable());
        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()){
                    case R.id.addItem:
                        Log.w(TAG, "press add:"+position);
                        newItemCount++;
                        ((RecycleViewImgAdapter) mAdapter).addNewItem(getString(R.string.new_item)+newItemCount, Integer.toString(newItemCount), position);

                        // 更新資料
                        itemList = ((RecycleViewImgAdapter) mAdapter).getListData();
                        ((RecycleViewImgAdapter) mAdapter).updateData(itemList);
                        break;
                    case R.id.deleteItem:
                        Log.w(TAG, "press del:"+position);
                        ((RecycleViewImgAdapter) mAdapter).deleteItem(position);

                        // 更新資料
                        itemList = ((RecycleViewImgAdapter) mAdapter).getListData();
                        ((RecycleViewImgAdapter) mAdapter).updateData(itemList);
                        break;
                }
                return true;
            }
        });
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                // 控件消失时的事件
            }
        });
    }
}
