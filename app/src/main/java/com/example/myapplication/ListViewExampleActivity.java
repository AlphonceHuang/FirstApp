package com.example.myapplication;

import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.example.myapplication.Util.showToastIns;

public class ListViewExampleActivity extends AppCompatActivity {

    private final String TAG="Alan";
    private ListView listView;
    private boolean multiLine=false;
    private Bundle bundle;
    private Menu optionMenu;
    private List<Map<String, Object>> listItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view_example);

        bundle = this.getIntent().getExtras();
        InitialListView();
    }

    protected void onResume(){
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_listview3, menu);
        optionMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.w(TAG, "ListViewExampleActivity:onOptionsItemSelected:");
        super.onOptionsItemSelected(item);

        switch(item.getItemId()) {
            case R.id.executeItem:
                showResult();
                break;
            case R.id.selectAllItem:
                selectAllItem();
                break;
            case R.id.clearAllItem:
                clearAllItem();
                break;
            case R.id.multiChoiceItem:
                multiLine=!item.isChecked();
                item.setChecked(multiLine);
                if (multiLine){    // 有複選
                    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    optionMenu.findItem(R.id.selectAllItem).setVisible(true);
                    optionMenu.findItem(R.id.clearAllItem).setVisible(true);
                }else { // 單選
                    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    optionMenu.findItem(R.id.selectAllItem).setVisible(false);
                    optionMenu.findItem(R.id.clearAllItem).setVisible(false);
                }
                clearAllItem();
                break;
        }
        return true;
    }

    // 每次打開 Option menu 都會再執行一次
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // 設定option menu裡面的check item
        Log.w(TAG, "ListViewExampleActivity:onPrepareOptionsMenu");
        if (!multiLine) {   // 沒有複選功能
            menu.findItem(R.id.selectAllItem).setVisible(false);
            menu.findItem(R.id.clearAllItem).setVisible(false);
        }
        return true;
    }

    private void InitialListView(){
        listView = findViewById(R.id.lsvEx);

        String style=null;
        if (bundle != null) {
            style = bundle.getString("LIST_STYLE");
        }

        if (multiLine){    // 有複選
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        }else { // 單選
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }

        if (Objects.equals(style, "simple_list_item_2") ||
                Objects.equals(style, "simple_list_item_activated_2") ||
                Objects.equals(style, "simple_expandable_list_item_2") ||
                Objects.equals(style, "two_line_list_item")){

            ArrayList<HashMap<String,String>> listA = new ArrayList<>();
            int length = getResources().getStringArray(R.array.array_Places).length;
            //把資料加入ArrayList中
            for(int i=0; i<length; i++){
                HashMap<String,String> item = new HashMap<>();
                item.put( "food", getResources().getStringArray(R.array.array_Food)[i]);
                item.put( "place",getResources().getStringArray(R.array.array_Places)[i]);
                listA.add( item );
            }
            //新增SimpleAdapter
            SimpleAdapter adapter;
            //==============================================================================
            // android.R.layout.simple_list_item_activated_2
            // ==============================================================================
            if (Objects.equals(style, "simple_list_item_activated_2")){
                adapter = new SimpleAdapter(
                        this,
                        listA,
                        android.R.layout.simple_list_item_activated_2,
                        new String[]{"food", "place"},
                        new int[]{android.R.id.text1, android.R.id.text2});
            }
            //==============================================================================
            // android.R.layout.simple_expandable_list_item_2
            // ==============================================================================
            else if (Objects.equals(style, "simple_expandable_list_item_2")){
                adapter = new SimpleAdapter(
                        this,
                        listA,
                        android.R.layout.simple_expandable_list_item_2,
                        new String[]{"food", "place"},
                        new int[]{android.R.id.text1, android.R.id.text2});
            }
            //==============================================================================
            // android.R.layout.two_line_list_item
            // ==============================================================================
            else if (Objects.equals(style, "two_line_list_item")){
                adapter = new SimpleAdapter(
                        this,
                        listA,
                        android.R.layout.two_line_list_item,
                        new String[]{"food", "place"},
                        new int[]{android.R.id.text1, android.R.id.text2});
            }
            //==============================================================================
            // android.R.layout.simple_list_item_2
            // ==============================================================================
            else{
                adapter = new SimpleAdapter(
                        this,
                        listA,
                        android.R.layout.simple_list_item_2,
                        new String[]{"food", "place"},
                        new int[]{android.R.id.text1, android.R.id.text2});
            }
            listView.setAdapter( adapter );
        }
        //==============================================================================
        // android.R.layout.simple_list_item_activated_1
        // ==============================================================================
        else if (Objects.equals(style, "simple_list_item_activated_1")){
            ArrayAdapter<String> ListAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_activated_1);
            for (String s : getResources().getStringArray(R.array.array_Places)) {
                ListAdapter.add(s);
            }
            listView.setAdapter(ListAdapter);
        }
        //==============================================================================
        // android.R.layout.simple_list_item_checked
        // ==============================================================================
        else if (Objects.equals(style, "simple_list_item_checked")){
            ArrayAdapter<String> ListAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_checked);
            for (String s : getResources().getStringArray(R.array.array_Places)) {
                ListAdapter.add(s);
            }
            listView.setAdapter(ListAdapter);
        }
        //==============================================================================
        // android.R.layout.simple_list_item_multiple_choice
        // ==============================================================================
        else if (Objects.equals(style, "simple_list_item_multiple_choice")){
            ArrayAdapter<String> ListAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_multiple_choice);
            for (String s : getResources().getStringArray(R.array.array_Places)) {
                ListAdapter.add(s);
            }
            listView.setAdapter(ListAdapter);
        }
        //==============================================================================
        // android.R.layout.simple_list_item_single_choice
        // ==============================================================================
        else if (Objects.equals(style, "simple_list_item_single_choice")){
            ArrayAdapter<String> ListAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_single_choice);
            for (String s : getResources().getStringArray(R.array.array_Places)) {
                ListAdapter.add(s);
            }
            listView.setAdapter(ListAdapter);
        }
        //==============================================================================
        // android.R.layout.simple_selectable_list_item
        // ==============================================================================
        else if (Objects.equals(style, "simple_selectable_list_item")){
            ArrayAdapter<String> ListAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_selectable_list_item);
            for (String s : getResources().getStringArray(R.array.array_Places)) {
                ListAdapter.add(s);
            }
            listView.setAdapter(ListAdapter);
        }
        //==============================================================================
        // android.R.layout.simple_expandable_list_item_1
        // ==============================================================================
        else if (Objects.equals(style, "simple_expandable_list_item_1")){

            ArrayAdapter<String> ListAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_expandable_list_item_1);
            for (String s : getResources().getStringArray(R.array.array_Places)) {
                ListAdapter.add(s);
            }
            listView.setAdapter(ListAdapter);
        }
        //==============================================================================
        // android.R.layout.test_list_item
        // ==============================================================================
        else if (Objects.equals(style, "test_list_item")){
            ArrayAdapter<String> ListAdapter = new ArrayAdapter<>(this,
                    android.R.layout.test_list_item);
            for (String s : getResources().getStringArray(R.array.array_Places)) {
                ListAdapter.add(s);
            }
            listView.setAdapter(ListAdapter);
        }
        //==============================================================================
        // android.R.layout.activity_list_item
        // 因為有兩個物件，adapter方式跟two line的方式相同
        // ==============================================================================
        else if (Objects.equals(style, "activity_list_item")){
            ArrayList<HashMap<String,Object>> listA = new ArrayList<>();
            int length = getResources().getStringArray(R.array.array_Places).length;
            TypedArray regionIconList = getResources().obtainTypedArray(R.array.array_icon);

            //把資料加入ArrayList中
            for(int i=0; i<length; i++){
                HashMap<String,Object> item = new HashMap<>();
                item.put( "icon", regionIconList.getResourceId(i, 0));
                item.put( "place",getResources().getStringArray(R.array.array_Places)[i]);
                listA.add( item );
            }
            //新增SimpleAdapter
            SimpleAdapter adapter;
            adapter = new SimpleAdapter(
                    this,
                    listA,
                    android.R.layout.activity_list_item,
                    new String[]{"icon", "place"},
                    new int[]{android.R.id.icon, android.R.id.text1});
            listView.setAdapter( adapter );
            /* android.R.layout.activity_list_item.xml

            <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:paddingTop="1dip"
                android:paddingBottom="1dip"
                android:paddingStart="8dip"
                android:paddingEnd="8dip">

                <ImageView android:id="@+id/icon"
                    android:layout_width="24dip"
                    android:layout_height="24dip"/>

                <TextView android:id="@android:id/text1"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center_horizontal"
                    android:paddingStart="?android:attr/listPreferredItemPaddingStart" />
            </LinearLayout>
             */
        }
        //==============================================================================
        // android.R.layout.preference_category
        // ==============================================================================
        else if (Objects.equals(style, "preference_category")){
            ArrayAdapter<String> ListAdapter = new ArrayAdapter<>(this,
                    android.R.layout.preference_category);
            for (String s : getResources().getStringArray(R.array.array_Places)) {
                ListAdapter.add(s);
            }
            listView.setAdapter(ListAdapter);
        }
        //==============================================================================
        // android.R.layout.select_dialog_item
        // ==============================================================================
        else if (Objects.equals(style, "select_dialog_item")){
            ArrayAdapter<String> ListAdapter = new ArrayAdapter<>(this,
                    android.R.layout.select_dialog_item);
            for (String s : getResources().getStringArray(R.array.array_Places)) {
                ListAdapter.add(s);
            }
            listView.setAdapter(ListAdapter);
        }
        //==============================================================================
        // android.R.layout.select_dialog_multichoice
        // ==============================================================================
        else if (Objects.equals(style, "select_dialog_multichoice")){
            ArrayAdapter<String> ListAdapter = new ArrayAdapter<>(this,
                    android.R.layout.select_dialog_multichoice);
            for (String s : getResources().getStringArray(R.array.array_Places)) {
                ListAdapter.add(s);
            }
            listView.setAdapter(ListAdapter);
        }
        //==============================================================================
        // android.R.layout.select_dialog_singlechoice
        // ==============================================================================
        else if (Objects.equals(style, "select_dialog_singlechoice")){
            ArrayAdapter<String> ListAdapter = new ArrayAdapter<>(this,
                    android.R.layout.select_dialog_singlechoice);
            for (String s : getResources().getStringArray(R.array.array_Places)) {
                ListAdapter.add(s);
            }
            listView.setAdapter(ListAdapter);
        }
        //==============================================================================
        // android.R.layout.simple_dropdown_item_1line
        // ==============================================================================
        else if (Objects.equals(style, "simple_dropdown_item_1line")){
            // 單行顯示，跑馬燈要怎麼開始跑??
            ArrayAdapter<String> ListAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line);
            for (String s : getResources().getStringArray(R.array.array_Places)) {
                ListAdapter.add(s);
            }
            listView.setAdapter(ListAdapter);
        }
        //==============================================================================
        // R.layout.list_view_item
        // ==============================================================================
        else if (Objects.equals(style, "list_view_item")){
            ArrayList<HashMap<String,Object>> listA = new ArrayList<>();
            TypedArray regionIconList = getResources().obtainTypedArray(R.array.array_icon);
            //TypedArray regionPlaceList = getResources().obtainTypedArray(R.array.array_Places);
            //TypedArray regionFoodList = getResources().obtainTypedArray(R.array.array_Food);
            //Log.w(TAG, "size="+regionPlaceList.length());

            //把資料加入ArrayList中
            for(int i=0; i<getResources().getStringArray(R.array.array_Places).length; i++){
                HashMap<String,Object> item = new HashMap<>();
                item.put( "icon", regionIconList.getResourceId(i, 0));
                item.put( "title",getResources().getStringArray(R.array.array_Places)[i]);
                item.put( "subtitle",getResources().getStringArray(R.array.array_Food)[i]);
                listA.add( item );
            }
            //新增SimpleAdapter
            SimpleAdapter adapter = new SimpleAdapter(
                    this,
                    listA,
                    R.layout.list_view_item,
                    new String[]{"icon", "title", "subtitle"},
                    new int[]{R.id.imgView, R.id.txtView, R.id.txtView1});
            //ListAdapter adapter = new ListAdapter(ListViewExampleActivity.this, itemList);
            listView.setAdapter( adapter );
        }
        //==============================================================================
        // android.R.layout.simple_list_item_1
        // ==============================================================================
        else{
            ArrayAdapter<String> ListAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1);
            for (String s : getResources().getStringArray(R.array.array_Places)) {
                ListAdapter.add(s);
            }
            listView.setAdapter(ListAdapter);
        }

        // 按下Event
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // 短按動作
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Log.w(TAG, "按了:"+i);
                if (listView.isItemChecked(i)) {
                    showToastIns(getApplicationContext(), "你點選了:" +
                            adapterView.getItemAtPosition(i).toString(), Toast.LENGTH_SHORT);
                }else{
                    showToastIns(getApplicationContext(), "你取消了:" +
                            adapterView.getItemAtPosition(i).toString(), Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private void showResult(){
        StringBuilder selectedData = new StringBuilder();
        int selectedCount = 0;
        for(int i=0;i< listView.getAdapter().getCount();i++){
            if (listView.isItemChecked(i)){
                selectedData.append(getResources().getStringArray(R.array.array_Places)[i]).append(",");
                selectedCount ++;
            }
        }
        showToastIns(this, "總共選取"+selectedCount+"個項目..."+selectedData, Toast.LENGTH_LONG);
    }

    private void clearAllItem(){
        for(int i=0;i< listView.getAdapter().getCount();i++){
            if (listView.isItemChecked(i)){
                listView.setItemChecked(i, false);
            }
        }
        showToastIns(this, "全部取消", Toast.LENGTH_LONG);
    }

    private void selectAllItem(){
        for(int i=0;i< listView.getAdapter().getCount();i++){
            if (!listView.isItemChecked(i)){
                listView.setItemChecked(i, true);
            }
        }
        showToastIns(this, "全部選取", Toast.LENGTH_LONG);
    }
}
