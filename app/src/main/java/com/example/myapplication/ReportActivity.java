package com.example.myapplication;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

import static com.example.myapplication.R.array.pop_item;
import static com.example.myapplication.Util.showToastIns;

public class ReportActivity extends AppCompatActivity {
    private static final String TAG = "Alan";
    private TextView report_result;
    private TextView report_suggest;
    private double BMI;
    private boolean checkResult;
    private TextView input_height;
    private TextView input_weight;
    private TextView ChangeText;

    final int font_text_10 = 1;
    final int font_text_12 = 2;
    final int font_text_14 = 3;
    final int font_text_16 = 4;
    final int font_text_18 = 5;
    final int color_text_1 = 6;
    final int color_text_2 = 7;
    final int color_text_3 = 8;

    final int float_menu_group = 90;
    private static final int ACTIVITY_RECORD_AUDIO = 1009;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        //Log.w(TAG, "ReportActivity onCreate:"+MainActivity.mHowGoP2);

        initialView();

        //if (MainActivity.mHowGoP2 == 0)    // 由按下"看報告"跳轉而來，需顯示報告內容
        //    showResult();
        //else if (MainActivity.mHowGoP2 == -1)   // not from MainActivity
            showBMIreport();

        registerForContextMenu(findViewById(R.id.report_layout));   // 註冊context menu 浮動選單
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar!=null)
            mActionBar.setBackgroundDrawable(getDrawable(R.drawable.background_test));
    }

    //========================================================================
    // 長按任何空白區域，跳出浮動選單
    //========================================================================
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        String[] a = getResources().getStringArray(pop_item);

        //參數1:群組id, 參數2:itemId, 參數3:item順序, 參數4:item名稱
        for (int i = 0; i < a.length; i++)
            menu.add(float_menu_group, Menu.FIRST + i, Menu.NONE, a[i]);

        super.onCreateContextMenu(menu, view, menuInfo);
    }

    public boolean onContextItemSelected(MenuItem item) {
        String[] a = getResources().getStringArray(pop_item);

        if (item.getGroupId() == float_menu_group) {
            /*
            showToastIns(getApplicationContext(),
                    item.getTitle(),
                    Toast.LENGTH_SHORT);*/
            int index = item.getItemId();
            //Log.w(TAG, "按下"+getString(R.string.item_text)+(index));

            switch (index) {
                case Menu.FIRST:    // 1
                    showToastIns(getApplicationContext(), a[0], Toast.LENGTH_SHORT,
                            Gravity.CENTER, 0, 0);
                    break;
                case Menu.FIRST + 1:  // 2
                    showToastIns(getApplicationContext(), a[1], Toast.LENGTH_SHORT,
                            Gravity.TOP, 0, 0);
                    break;
                case Menu.FIRST + 2:  // 3
                    showToastIns(getApplicationContext(), a[2], Toast.LENGTH_SHORT,
                            Gravity.BOTTOM, 0, 0);
                    break;
                case Menu.FIRST + 3:  // 4
                    showToastIns(getApplicationContext(), a[3], Toast.LENGTH_SHORT,
                            Gravity.START, 0, 0);
                    break;
                case Menu.FIRST + 4:  // 5
                    showToastIns(getApplicationContext(), a[4], Toast.LENGTH_SHORT,
                            Gravity.END, 0, 0);
                    break;
                case Menu.FIRST + 5:  // 6
                    showToastIns(getApplicationContext(), a[5], Toast.LENGTH_SHORT,
                            Gravity.FILL_HORIZONTAL, 0, 0);
                    break;
                case Menu.FIRST + 6:  // 7
                    showToastIns(getApplicationContext(), a[6], Toast.LENGTH_SHORT,
                            Gravity.TOP | Gravity.START, 0, 0);
                    break;
                case Menu.FIRST + 7:  // 8
                    Toast.makeText(getApplicationContext(), a[7], Toast.LENGTH_SHORT).show();
                    break;
                case Menu.FIRST + 8:  // 9
                    showToastIns(getApplicationContext(), a[8], Toast.LENGTH_SHORT,
                            Gravity.NO_GRAVITY, 0, 0);
                    break;
                case Menu.FIRST + 9:  // 10
                    DisplayMetrics metrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(metrics);
                    //Log.w(TAG, "metrics.heightPixels="+metrics.heightPixels);
                    showToastIns(getApplicationContext(), a[9], Toast.LENGTH_SHORT,
                            Gravity.BOTTOM, 0, metrics.heightPixels / 9); // 類似預設位置
                    break;
            }
        }
        return super.onContextItemSelected(item);
    }

    protected void onStart() {
        super.onStart();
        Log.w(TAG, "report on start");
    }

    protected void onResume() {
        super.onResume();
        Log.w(TAG, "report on resume");
    }

    protected void onPause() {
        super.onPause();
        Log.w(TAG, "report on pause");
    }

    protected void onStop() {
        super.onStop();
        Log.w(TAG, "report on stop");
    }

    protected void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "report on destroy");
    }

    private void initialView() {
        //TextView titleText = findViewById(R.id.reportTitle);
        Button button_back = findViewById(R.id.reportBackbutton);
        report_result = findViewById(R.id.reportResult);
        report_suggest = findViewById(R.id.reportSuggest);
        input_height = findViewById(R.id.height);
        input_weight = findViewById(R.id.weight);
        Button button_toast = findViewById(R.id.report_toast);
        Button goto_ListPage = findViewById(R.id.tolistviewpage);
        ChangeText = findViewById(R.id.changeText);
        Button goto_prevPage = findViewById(R.id.prev_page);
        Button goto_nextPage = findViewById(R.id.report_nextpage);

        button_back.setOnClickListener(BMIReportBtn);
        button_toast.setOnClickListener(BMIReportBtn);
        goto_prevPage.setOnClickListener(BMIReportBtn);
        goto_nextPage.setOnClickListener(BMIReportBtn);
        goto_ListPage.setOnClickListener(BMIReportBtn); // 註冊短按
        goto_ListPage.setOnLongClickListener(BMIReportBtn1);    // 註冊長按

        //TitleText.setTextSize(30);

        ChangeText.setTextColor(Color.GREEN);   // default color
        ChangeText.setTextSize(30); // default size
    }
/*
    private void showResult() {
        Bundle bundle = this.getIntent().getExtras();

        Log.w(TAG, Objects.requireNonNull(Objects.requireNonNull(bundle).getString("KEY_HEIGHT")));
        Log.w(TAG, Objects.requireNonNull(bundle.getString("KEY_WEIGHT")));
        Log.w(TAG, Objects.requireNonNull(bundle.getString("KEY_BMI")));
        Log.w(TAG, Objects.requireNonNull(bundle.getString("KEY_RESULT")));

        if (Integer.parseInt(Objects.requireNonNull(bundle.getString("KEY_RESULT"))) == 1) {
            DecimalFormat nf = new DecimalFormat("0.00");

            String showHeight = getText(R.string.length) + ":" + bundle.getString("KEY_HEIGHT") + getText(R.string.cm);
            String showWeight = getText(R.string.weight) + ":" + bundle.getString("KEY_WEIGHT") + getText(R.string.kg);
            input_height.setText(showHeight);
            input_weight.setText(showWeight);

            //int height=Integer.parseInt(Objects.requireNonNull(bundle.getString("KEY_HEIGHT")));

            BMI = Double.parseDouble(Objects.requireNonNull(bundle.getString("KEY_BMI")));
            String showResult = getText(R.string.cal_result) + nf.format(BMI);
            report_result.setText(showResult);

            if (BMI > 25) //太重了
                report_suggest.setText(R.string.suggest_over);
            else if (BMI < 20) //太輕了
                report_suggest.setText(R.string.suggest_light);
            else //剛剛好
                report_suggest.setText(R.string.suggest_good);

            checkResult = true;
        } else {
            checkResult = false;
            Log.w(TAG, "輸入錯誤");
            report_result.setText(R.string.cal_fail);
        }
    }
 */

    private void showBMIreport(){
        DecimalFormat nf = new DecimalFormat("0.00");
        GlobalVariable gv = (GlobalVariable)getApplicationContext();

        BMI = gv.getPerson_BMI();
        String showHeight = getText(R.string.length) + ":" + gv.getPerson_height();
        String showWeight = getText(R.string.weight) + ":" + gv.getPerson_weight();
        input_height.setText(showHeight);
        input_weight.setText(showWeight);

        String showResult = getText(R.string.cal_result) + nf.format(BMI);
        report_result.setText(showResult);
        checkResult=false;

        if (BMI > 25) //太重了
            report_suggest.setText(R.string.suggest_over);
        else if (BMI < 20) //太輕了
            report_suggest.setText(R.string.suggest_light);
        else //剛剛好
            report_suggest.setText(R.string.suggest_good);
    }

    //========================================================================
    // button event
    //========================================================================
    private Button.OnClickListener BMIReportBtn = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.reportBackbutton: // 回前頁
                    Log.w(TAG, "按下離開");
                    showExitDialog();
                    break;

                case R.id.report_toast: // show toast button
                    Log.w(TAG, "使用mainActivity裡面的function");
                    MainActivity.instance.ShowImageToast(); // 調用MainActivity裡的function
                    break;

                case R.id.tolistviewpage:
                    Log.w(TAG, "短按下:跳至列表頁面");
                    //Intent intent1=new Intent(ReportActivity.this, Oe_ListViewActivity.class);
                    //startActivity(intent1);
                    showToastIns(getApplicationContext(), getText(R.string.short_press), Toast.LENGTH_SHORT);
                    break;

                case R.id.prev_page:
                    Log.w(TAG, "使用finish回上一頁");
                    ReportActivity.this.finish();   // 不傳值回去
                    break;

                case R.id.report_nextpage:
                    Log.w(TAG, "Report的下一頁");
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        Log.w(TAG, "MainActivity:Get RECORD_AUDIO permission success.");
                        Intent OptionIntent = new Intent(ReportActivity.this, VideoActivity.class);
                        startActivity(OptionIntent);
                    } else {
                        Log.w(TAG, "MainActivity:Get RECORD_AUDIO permission fail.");
                        ActivityCompat.requestPermissions(ReportActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, ACTIVITY_RECORD_AUDIO);
                    }
                    //Intent intent1 = new Intent(ReportActivity.this,VideoActivity.class);
                    //startActivity(intent1);
                    break;
            }
        }
    };

    /*
        private void CloseMenuBundleIntent(){
            // 用bundle的方式傳值
            DecimalFormat nf = new DecimalFormat("0.00");
            Bundle msg = new Bundle();
            Intent intent = new Intent();

            if (checkResult==true)
            {
                msg.putString("LAST_DATA", nf.format(BMI));
                intent.putExtras(msg);
                setResult(RESULT_OK, intent);
            }
            else
                setResult(RESULT_CANCELED, intent);
        }
    */
    // 顯示離開確認對話框
    private void showExitDialog() {
        AlertDialog.Builder ShowInfo = new AlertDialog.Builder(ReportActivity.this);

        ShowInfo.setTitle(R.string.back);
        ShowInfo.setMessage(R.string.about_detail);
        ShowInfo.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.w(TAG, "Dialog:按下確定");
                CloseMenuEasyIntent();  // 將結果帶去另一個activity
            }
        });
        ShowInfo.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.w(TAG, "Dialog:按下取消");
            }
        });
        ShowInfo.show();
    }

    private void CloseMenuEasyIntent() {
        // 簡單Intent方式回傳值
        DecimalFormat nf = new DecimalFormat("0.00");
        Intent intent = new Intent();

        if (checkResult) {   // 有值可回傳
            intent.putExtra("LAST_DATA", nf.format(BMI));
            setResult(RESULT_OK, intent);   // 因前面是用startActivityForResult，所以必須使用這個回傳result
        } else {
            setResult(RESULT_CANCELED, intent);
        }
        ReportActivity.this.finish();   // 回至上一個Activity
    }

    //========================================================================
    // long press button event
    //========================================================================
    private Button.OnLongClickListener BMIReportBtn1 = new Button.OnLongClickListener() {

        @Override
        public boolean onLongClick(View view) {
            if (view.getId() == R.id.tolistviewpage) {
                Log.w(TAG, "長按下:跳至列表頁面");
                showToastIns(getApplicationContext(), getText(R.string.long_press), Toast.LENGTH_SHORT);
            }
            return true;    // 只會觸發長按
            //return false: // 如果長按，會先觸發長按，放開後再觸發短按
        }
    };


    //========================================================================
    // 增加右上角選單列
    //========================================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 報告頁項目一   字體大小
        //               字體顏色
        // 報告頁項目二
        // 報告頁項目三
        // 字體大小      字體10
        //              字體12
        //              字體14
        //              字體16
        //              字體18
        // 字體顏色
        //              顏色:黑
        //              顏色:黃
        //              顏色:紅

        // Layout方式取得items
        getMenuInflater().inflate(R.menu.menu_report, menu);
        // 第一個item裡面的submenu是用Layout方式取得

        // 用手寫方式增加有submenu的item
        SubMenu fontMenu = menu.addSubMenu(getText(R.string.report_font_size));
        SubMenu colorMenu = menu.addSubMenu(getText(R.string.report_font_color));

        // 手動方式增加item裡的submenu
        fontMenu.add(0, font_text_10, 0, getText(R.string.report_font_10));
        fontMenu.add(0, font_text_12, 0, getText(R.string.report_font_12));
        fontMenu.add(0, font_text_14, 0, getText(R.string.report_font_14));
        fontMenu.add(0, font_text_16, 0, getText(R.string.report_font_16));
        fontMenu.add(0, font_text_18, 0, getText(R.string.report_font_18));

        // 增加有submenu的item
        colorMenu.add(0, color_text_1, 0, getText(R.string.report_fontcolor_black));
        colorMenu.add(0, color_text_2, 0, getText(R.string.report_fontcolor_yellow));
        colorMenu.add(0, color_text_3, 0, getText(R.string.report_fontcolor_red));
        //colorMenu.setIcon(R.drawable.cat);
        colorMenu.setHeaderTitle("字體颜色選擇"); // submenu的標頭，如果沒設定就用item的字串

        return true;
    }

    //========================================================================
    // Optoma menu選到的動作
    //========================================================================
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.report_menu_item1:
            case R.id.report_menu_item2:
            case R.id.report_menu_item3:
                showToastIns(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT);
                break;

            case font_text_10:  // 因為是手動增加，使用fontMenu.add裡的字串
            case R.id.report_item1_subitem11:
                showToastIns(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT);
                ChangeText.setTextSize(10 * 2);
                break;
            case font_text_12:
            case R.id.report_item1_subitem12:
                showToastIns(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT);
                ChangeText.setTextSize(12 * 2);
                break;
            case font_text_14:
            case R.id.report_item1_subitem13:
                showToastIns(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT);
                ChangeText.setTextSize(14 * 2);
                break;
            case font_text_16:
            case R.id.report_item1_subitem14:
                showToastIns(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT);
                ChangeText.setTextSize(16 * 2);
                break;
            case font_text_18:
            case R.id.report_item1_subitem15:
                showToastIns(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT);
                ChangeText.setTextSize(18 * 2);
                break;
            case color_text_1:
            case R.id.report_item1_subitem21:
                showToastIns(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT);
                ChangeText.setTextColor(Color.BLACK);
                break;
            case color_text_2:
            case R.id.report_item1_subitem22:
                showToastIns(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT);
                ChangeText.setTextColor(Color.YELLOW);
                break;
            case color_text_3:
            case R.id.report_item1_subitem23:
                showToastIns(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT);
                ChangeText.setTextColor(Color.RED);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //========================================================================
    // 下面不會執行到?? 為何??
    //========================================================================
    @Override
    public void onOptionsMenuClosed(Menu menu) //當 Menu 關閉時觸發
    {
        //showToastIns(getApplicationContext(), "關閉了選項選單", Toast.LENGTH_SHORT);
        Log.w(TAG, "關閉選單");
        super.onOptionsMenuClosed(menu);
    }

    //========================================================================
    // 使用xml裡android:onClick實現動作
    //========================================================================
    public void clickLargeTextSize(View view) {
        ChangeText.setTextSize(20 * 2);
    }

    public void clickSmallTextSize(View view) {
        ChangeText.setTextSize(14 * 2);
    }

    //=======================================================================
    // 要求系統權限回應
    //=======================================================================
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults) {

        //Log.w(TAG, "requestCode:" + requestCode + ",length" + permissions.length);

        for (int i = 0, len = permissions.length; i < len; i++) {
            //String permission = permissions[i];
            //Log.w(TAG, "permis:" + permissions[i]);

            if (requestCode == ACTIVITY_RECORD_AUDIO) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Intent intentVideo = new Intent(ReportActivity.this, VideoActivity.class);
                    startActivity(intentVideo);
                } else {
                    Log.w(TAG, "Audio Record Permission not granted.");
                    showToastIns(getApplicationContext(), "沒有錄音權限", Toast.LENGTH_LONG);
                }
            }
        }
    }
}