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

public class ListView1Activity extends AppCompatActivity {

    private static final String TAG="Alan";

    // 對應到ListAdapter.java
    private static final String ITEM_ICON = "ITEM_ICON";
    private static final String ITEM_TITLE = "ITEM_TITLE";
    private static final String ITEM_TITLE1 = "ITEM_SUBTITME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view1);

        Log.w(TAG, "ListView1Activity:onCreate");


        //宣告 ListView 元件
        //private TextView mTxtR;
        ListView lsv1_main = findViewById(R.id.lsv1_main);
        lsv1_main.setOnItemClickListener(listViewOnItemClickListener);

        //定義 ListView 每個 Item 的資料
        List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();

        // icon由res/value/string裡面取出
        TypedArray regionIconList = getResources().obtainTypedArray(R.array.kitty_icon);
        TypedArray regionTitleList = getResources().obtainTypedArray(R.array.region1_title_list);
        TypedArray regionSubTitleList = getResources().obtainTypedArray(R.array.region1_subtitle_list);

        // 將圖片及文字放入item中
        for (int i = 0; i < regionTitleList.length(); i++)
        {
            Map<String, Object> item = new HashMap<String, Object>();

            // 用array的方式填入字串
            //item.put(ITEM_TITLE, regionList[i]);

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
        // 使用ListAdapter1.java
        ListAdapter mListAdapter = new ListAdapter(ListView1Activity.this, itemList);

        //設定 ListView 的 Adapter
        lsv1_main.setAdapter(mListAdapter);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    private AdapterView.OnItemClickListener listViewOnItemClickListener
            = new AdapterView.OnItemClickListener()
    {
        Intent intent;

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            Log.w(TAG,"onItemClick position:"+position+", id:"+id);

            switch(position){
                case 0:
                    intent = new Intent(ListView1Activity.this, ImmersiveS1Activity.class);
                    startActivity(intent);
                    break;
                case 1:
                    intent = new Intent(ListView1Activity.this, ImmersiveS2Activity.class);
                    startActivity(intent);
                    break;
                case 2:
                    intent = new Intent(ListView1Activity.this, ImmersiveS3Activity.class);
                    startActivity(intent);
                    break;
                case 3:
                    intent = new Intent(ListView1Activity.this, ImmersiveS4Activity.class);
                    startActivity(intent);
                    break;
                case 4:
                    intent = new Intent(ListView1Activity.this, ImmersiveS5Activity.class);
                    startActivity(intent);
                    break;
                case 5:
                    intent = new Intent(ListView1Activity.this, ImmersiveS6Activity.class);
                    startActivity(intent);
                    break;
                case 6:
                    intent = new Intent(ListView1Activity.this, ImmersiveS7Activity.class);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    };
}
