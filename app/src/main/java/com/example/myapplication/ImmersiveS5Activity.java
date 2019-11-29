package com.example.myapplication;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class ImmersiveS5Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_immersive_s5);

        if (Build.VERSION.SDK_INT >= 21) {
            // 全透明status bar及Navi bar
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION // navi bar
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // status bar
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        // 隱藏action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        TextView line1=findViewById(R.id.immersive5_text1);
        String str=getString(R.string.firstLine)+"\n"+
                "因為有SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN，所以第一行字會跟status bar重疊";
        line1.setText(str);
        TextView line2=findViewById(R.id.immersive5_text2);
        str = "因為有SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION，所以最後一行字會跟navi bar重疊";
        line2.setText(str);
    }
}
