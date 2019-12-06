package com.example.myapplication;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.example.myapplication.Util.showToastIns;

public class GridViewActivity extends AppCompatActivity {

    private final String TAG="Alan";

    private GridView gView;
    private int gridview_style=0;
    private TypedArray regionIconList;
    private boolean multiChoice=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            if (Objects.equals(bundle.getString("GRIDVIEW_STYLE"), "1"))
                gridview_style = 1;
            else if (Objects.equals(bundle.getString("GRIDVIEW_STYLE"), "2"))
                gridview_style = 2;
            else {
                gridview_style = 0;
                showToastIns(getApplicationContext(), "設定錯誤，取消", Toast.LENGTH_LONG);
                finish();
            }
            Log.w(TAG, "gridview_style:"+gridview_style);
        }

        gView=findViewById(R.id.grid);
        gView.setOnItemClickListener(gridViewOnItemClickListener);

        Log.w(TAG, "onCreate: multiChoice="+multiChoice);

        if (multiChoice) {    // 有複選
            gView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        } else { // 單選
            gView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }

        switch(gridview_style){
            case 1:
                Default_InitialView();
                break;
            case 2:
                Custom_InitialView();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        regionIconList.recycle();
    }

    private void Default_InitialView(){
        ArrayList<HashMap<String,Object>> listA = new ArrayList<>();
        regionIconList = getResources().obtainTypedArray(R.array.array_icon);
        String[] titleStr=getResources().getStringArray(R.array.array_Places);

        //把資料加入ArrayList中
        for(int i=0; i<titleStr.length; i++){
            HashMap<String,Object> item = new HashMap<>();
            item.put( "icon", regionIconList.getResourceId(i, 0));
            item.put( "title",titleStr[i]);
            listA.add( item );
        }
        SimpleAdapter adapter = new SimpleAdapter(
                this,
                listA,
                R.layout.grid_view_item,
                new String[]{"icon", "title"},
                new int[]{R.id.gridImage, R.id.gridText});
        gView.setAdapter( adapter );
    }

    private void Custom_InitialView(){

        List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();

        regionIconList = getResources().obtainTypedArray(R.array.array_icon);
        String[] str=getResources().getStringArray(R.array.array_Places);

        for (int i = 0; i < str.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("ITEM_TITLE1", str[i]);
            item.put("ITEM_ICON1", regionIconList.getResourceId(i, 0));
            itemList.add(item);
        }
        GridViewAdapter mGridAdapter=new GridViewAdapter(GridViewActivity.this, itemList);
        gView.setAdapter(mGridAdapter);
    }

    //--------------------------------------------------------
    // 按下item
    //--------------------------------------------------------
    private AdapterView.OnItemClickListener gridViewOnItemClickListener
            = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Log.w(TAG, "按了:"+i);
            if (gView.isItemChecked(i)) {
                showToastIns(getApplicationContext(), "你點選了:" +
                        adapterView.getItemAtPosition(i).toString(), Toast.LENGTH_SHORT);
            }else{
                showToastIns(getApplicationContext(), "你取消了:" +
                        adapterView.getItemAtPosition(i).toString(), Toast.LENGTH_SHORT);
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_listview3, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.multiChoiceItem) {
            multiChoice = !item.isChecked();
            item.setChecked(multiChoice);
            if (multiChoice) {    // 有複選
                gView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            } else { // 單選
                gView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            }
        }

        Log.w(TAG, "onOptionsItemSelected: multiChoice="+multiChoice);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.selectAllItem).setVisible(false);
        menu.findItem(R.id.clearAllItem).setVisible(false);
        menu.findItem(R.id.executeItem).setVisible(false);
        menu.findItem(R.id.multiChoiceItem).setChecked(multiChoice);
        return true;
    }
}
