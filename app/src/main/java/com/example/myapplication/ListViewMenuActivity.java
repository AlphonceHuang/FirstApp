package com.example.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ListViewMenuActivity extends AppCompatActivity {

    private final String TAG="Alan";
    //private boolean multi_choice=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view_menu);

        InitialListView();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_listview3, menu);
        return true;
    }
    // 每次打開 Option menu 都會再執行一次
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // 設定option menu裡面的check item
        menu.findItem(R.id.multiChoiceItem).setChecked(multi_choice);
        menu.findItem(R.id.selectAllItem).setVisible(false);
        menu.findItem(R.id.clearAllItem).setVisible(false);
        menu.findItem(R.id.executeItem).setVisible(false);
        InitialListView();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.multiChoiceItem){
            multi_choice=!item.isChecked();
            item.setChecked(multi_choice);
        }
        return true;
    }*/

    private void InitialListView(){

        ListView listView=findViewById(R.id.ListMenu);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this, android.R.layout.test_list_item);
        for (String s: getResources().getStringArray(R.array.listViewOneLineMenu)){
                listAdapter.add(s);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String target=adapterView.getItemAtPosition(i).toString();
                Log.w(TAG, "選了:"+target);

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("LIST_STYLE", target);
                //bundle.putString("MULTI_CHOICE", String.valueOf(multi_choice));// 轉成字串
                intent.setClass(ListViewMenuActivity.this, ListViewExampleActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        listView.setAdapter(listAdapter);
    }
}
