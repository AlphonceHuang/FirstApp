package com.example.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class ImmersiveS7Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_immersive_s7);

        TextView line1=findViewById(R.id.immersive7_text1);
        String str=getString(R.string.firstLine)+"\n"+
                "因為有SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN，所以第一行字會跟status bar重疊";
        line1.setText(str);
        TextView line2=findViewById(R.id.immersive7_text2);
        str = "因為有SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION，所以最後一行字會跟navi bar重疊";
        line2.setText(str);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();

            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // 有沒有此行沒差
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN    // 隱藏status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

/*
    @Override
    protected void onResume(){
        super.onResume();
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // 有沒有此行沒差
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN    // 隱藏status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

 */
}
