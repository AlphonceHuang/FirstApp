package com.example.myapplication;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.myapplication.Util.FROM_IMAGE_ACTIVITY;
import static com.example.myapplication.Util.FROM_MUSIC_ACTIVITY;
import static com.example.myapplication.Util.FROM_RECORD_ACTIVITY;
import static com.example.myapplication.Util.FROM_VIDEO_ACTIVITY;
import static com.example.myapplication.Util.getFromWhichActivity;

public class FileManagerActivity extends ListActivity {
    private static final String TAG="Alan";

    private String path;
    private int from_where;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_manager);

        Button confirm_Btn = findViewById(R.id.okButton);
        confirm_Btn.setOnClickListener(fileBtnEvent);

        from_where = getFromWhichActivity();
        Log.w(TAG, "FileManagerActivity:onCreate:"+from_where);

        // Use the current directory as title
        //path = "/"; // 取根目錄
        path = Environment.getExternalStorageDirectory().getPath();

        if (getIntent().hasExtra("path")) {
            path = getIntent().getStringExtra("path");
        }
        setTitle(path);

        // Read all files sorted into the values-array
        List<String> values = new ArrayList<>();
        File dir = new File(path);
        if (!dir.canRead()) {
            setTitle(getTitle() + " (inaccessible)");
        }
        String[] list = dir.list();
        if (list != null) {
            for (String file : list) {
                if (!file.startsWith(".")) {
                    values.add(file);
                }
            }
        }
        Collections.sort(values);

        // Put the data into the list
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);
        setListAdapter(adapter);
    }

    @Override
    protected void onResume(){
        Log.w(TAG, "FileManagerActivity:onResume");
        super.onResume();
    }

    @Override
    protected void onDestroy(){
        Log.w(TAG, "FileManagerActivity:onDestroy");
        super.onDestroy();
    }

    private View.OnClickListener fileBtnEvent = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            Intent intent;

            if (view.getId()==R.id.okButton){
                Log.w(TAG, "from where:"+from_where);
                switch(from_where){
                    case FROM_IMAGE_ACTIVITY:
                        intent = new Intent(FileManagerActivity.this, ImageActivity.class);
                        intent.putExtra("image_path", path);
                        startActivity(intent);
                        break;
                    case FROM_VIDEO_ACTIVITY:
                        intent = new Intent(FileManagerActivity.this, VideoActivity.class);
                        intent.putExtra("video_path", path);
                        startActivity(intent);
                        break;
                    case FROM_MUSIC_ACTIVITY:
                        Log.w(TAG, "FileManagerActivity: path="+path);
                        intent = new Intent(FileManagerActivity.this, VideoActivity.class);
                        intent.putExtra("music_path", path);
                        startActivity(intent);
                        break;
                    case FROM_RECORD_ACTIVITY:
                        intent = new Intent(FileManagerActivity.this, VideoActivity.class);
                        intent.putExtra("rec_path", path);
                        startActivity(intent);
                        break;
                }
            }
        }
    };

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String filename = (String) getListAdapter().getItem(position);
        if (path.endsWith(File.separator)) {
            filename = path + filename;
        } else {
            filename = path + File.separator + filename;
        }

        //Log.w("Alan", "onListItemClick:"+filename);
        if (new File(filename).isDirectory()) {
            Intent intent = new Intent(this, FileManagerActivity.class);
            intent.putExtra("path", filename);
            startActivity(intent);  // 跳至目錄內

            //Toast.makeText(this, filename, Toast.LENGTH_LONG).show();
        } //else {
            //Toast.makeText(this, filename, Toast.LENGTH_LONG).show();
        //}
    }
}
