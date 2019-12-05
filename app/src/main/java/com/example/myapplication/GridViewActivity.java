package com.example.myapplication;

import android.content.res.TypedArray;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import static com.example.myapplication.Util.showToastIns;

public class GridViewActivity extends AppCompatActivity {

    private GridView gView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);

        gView=findViewById(R.id.grid);
        gView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        ArrayList<HashMap<String,Object>> listA = new ArrayList<>();
        TypedArray regionIconList = getResources().obtainTypedArray(R.array.array_icon);
        //把資料加入ArrayList中
        for(int i=0; i<getResources().getStringArray(R.array.array_Places).length; i++){
            HashMap<String,Object> item = new HashMap<>();
            item.put( "icon", regionIconList.getResourceId(i, 0));
            item.put( "title",getResources().getStringArray(R.array.array_Places)[i]);
            listA.add( item );
        }
        //新增SimpleAdapter
        SimpleAdapter adapter = new SimpleAdapter(
                this,
                listA,
                R.layout.grid_view_item,
                new String[]{"icon", "title"},
                new int[]{R.id.gridImage, R.id.gridText});
        gView.setAdapter( adapter );

        // 按下Event
        gView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // 短按動作
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.w("Alan", "按了:"+i);
                if (gView.isItemChecked(i)) {
                    showToastIns(getApplicationContext(), "你點選了:" +
                            adapterView.getItemAtPosition(i).toString(), Toast.LENGTH_SHORT);
                }else{
                    showToastIns(getApplicationContext(), "你取消了:" +
                            adapterView.getItemAtPosition(i).toString(), Toast.LENGTH_SHORT);
                }
            }
        });
    }
}
