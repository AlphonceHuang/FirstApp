package com.example.myapplication;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.example.myapplication.Util.showToastIns;

public class ListView2Activity extends AppCompatActivity {

    private String[] title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view2);

        ListView listView=findViewById(R.id.listview2);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showToastIns(getApplicationContext(), "按下了:"+title[i], Toast.LENGTH_LONG);
            }
        });
        MyBaseAdapter adapter=new MyBaseAdapter(this, getData(), R.layout.list_view_item);
        listView.setAdapter(adapter);
    }

    private List<MyBaseAdapterData> getData(){

        List<MyBaseAdapterData> datas = new ArrayList<>();

        // 方法一：text 由res/values/strings.xml裡的String-array取出

        title=getResources().getStringArray(R.array.array_Places);
        String[] subtitle=getResources().getStringArray(R.array.array_Food);

        // icon由res/values/array.xml裡的inteter-array取出
        TypedArray iconArray = getResources().obtainTypedArray(R.array.kitty_itmes);
        int[] ids = new int[iconArray.length()];
        for (int i=0; i<ids.length; i++){
            ids[i]=iconArray.getResourceId(i, 0);
        }
        iconArray.recycle();

        // 方法二: 字串也是用TypedArray方式取得
        /*
        TypedArray titleArray = getResources().obtainTypedArray(R.array.array_Places);
        for (int i=0; i<titleArray.length(); i++){
            title[i]=getString(titleArray.getResourceId(i, 0));
        }

        TypedArray subtitleArray = getResources().obtainTypedArray(R.array.array_Food);
        for (int i=0; i<subtitleArray.length(); i++){
            subtitle[i]=getString(subtitleArray.getResourceId(i, 0));
        }
        titleArray.recycle();
        subtitleArray.recycle();
        */

        // 將圖片及文字放入item中
        for (int i = 0; i < title.length; i++)
        {
            datas.add(new MyBaseAdapterData(ids[i], title[i], subtitle[i]));
        }
        return datas;
    }
}
