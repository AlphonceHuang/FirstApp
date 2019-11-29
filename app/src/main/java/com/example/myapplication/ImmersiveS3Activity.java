package com.example.myapplication;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class ImmersiveS3Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_immersive_s3);

        if (Build.VERSION.SDK_INT >= 21) {
            // 將LAYOUT擴展到status bar
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            // 全透明status bar
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        // 隱藏action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        // 註解
        TextView firstLine=findViewById(R.id.immersive3_text);
        String str=getString(R.string.firstLine)+"\n"+
                "因為有SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN，所以第一行字會跟status bar重疊";
        firstLine.setText(str);
    }
}
