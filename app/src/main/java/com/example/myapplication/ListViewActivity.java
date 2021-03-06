package com.example.myapplication;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListViewActivity extends AppCompatActivity {

    private static final String TAG="Alan";

    // 對應到ListAdapter.java
    private static final String ITEM_ICON = "ITEM_ICON";
    private static final String ITEM_TITLE = "ITEM_TITLE";
    private static final String ITEM_TITLE1 = "ITEM_SUBTITME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        Log.w(TAG, "ListViewActivity:onCreate");

        //宣告 ListView 元件
        ListView lsv_main = findViewById(R.id.lsv_main);
        lsv_main.setOnItemClickListener(listViewOnItemClickListener);

        //定義 ListView 每個 Item 的資料
        List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();

        // icon由res/value/string裡面取出
        TypedArray regionIconList = getResources().obtainTypedArray(R.array.region_icon_list);
        TypedArray regionTitleList = getResources().obtainTypedArray(R.array.region_title_list);
        TypedArray regionSubTitleList = getResources().obtainTypedArray(R.array.region_subtitle_list);

        // 將圖片及文字放入item中
        for (int i = 0; i < regionTitleList.length(); i++)
        {
            Map<String, Object> item = new HashMap<>();

            // 用res/values/string.xml裡的string-array來填入字串，如要多國語言，建議用這種
            item.put(ITEM_TITLE, getString(regionTitleList.getResourceId(i, 0)));
            item.put(ITEM_TITLE1, getString(regionSubTitleList.getResourceId(i, 0)));
            item.put(ITEM_ICON, regionIconList.getResourceId(i, 0));

            itemList.add(item);
        }
        regionIconList.recycle();
        regionTitleList.recycle();
        regionSubTitleList.recycle();

        // ListView 中所需之資料參數可透過修改 Adapter 的建構子傳入
        // 使用ListAdapter.java
        ListAdapter mListAdapter = new ListAdapter(ListViewActivity.this, itemList);

         //設定 ListView 的 Adapter
        lsv_main.setAdapter(mListAdapter);
    }

    private AdapterView.OnItemClickListener listViewOnItemClickListener
            = new AdapterView.OnItemClickListener()
    {
        Intent intentCamera;


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            Log.w(TAG,"onItemClick position:"+position+", id:"+id);
            Bundle bundle = new Bundle();
            Intent intent = new Intent();

            switch(position){
                case 0:
                    intentCamera = new Intent(ListViewActivity.this, CameraActivity.class);
                    startActivity(intentCamera);
                    break;
                case 1:
                    intentCamera = new Intent(ListViewActivity.this, Camera2Activity.class);
                    startActivity(intentCamera);
                    break;
                case 2:
                    intentCamera = new Intent(ListViewActivity.this, Camera3Activity.class);
                    startActivity(intentCamera);
                    break;
                case 3:
                    intentCamera = new Intent(ListViewActivity.this, OpenCVActivity.class);
                    startActivity(intentCamera);
                    break;
                case 4:
                    intentCamera = new Intent(ListViewActivity.this, ImageActivity.class);
                    startActivity(intentCamera);
                    break;
                case 5:
                    bundle.putString("CAMERA_FUNCTION", "1");
                    intent.setClass(ListViewActivity.this, OpenCVCameraActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);

                    //intentCamera = new Intent(ListViewActivity.this, OpenCVCameraActivity.class);
                    //startActivity(intentCamera);
                    break;
                case 6:
                    intentCamera = new Intent(ListViewActivity.this, Tutorial1Activity.class);
                    startActivity(intentCamera);
                    break;
                case 7:
                    intentCamera = new Intent(ListViewActivity.this, OpenCVCamera1Activity.class);
                    startActivity(intentCamera);
                    break;
                case 8:
                    bundle.putString("CAMERA_FUNCTION", "2");
                    intent.setClass(ListViewActivity.this, OpenCVCameraActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
                default:
                    break;
            }

            //Log.w(TAG, "getCount"+mListAdapter.getCount()); // item總數
            //Log.w(TAG, "getItem"+mListAdapter.getItem(position));   //目前的item，沒啥用
            //Log.w(TAG, "getItemId"+mListAdapter.getItemId(position));   // 目前的item id，沒啥用

            // 網路上抓下來有下面設定Text的部份，移除也沒差
            //TextView txtItemTitle = view.findViewById(R.id.txtView);
            //mTxtR.setText(txtItemTitle.getText());
        }
    };
}
