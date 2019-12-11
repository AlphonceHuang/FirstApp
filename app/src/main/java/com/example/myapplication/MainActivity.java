package com.example.myapplication;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static android.support.v7.app.AlertDialog.Builder;
import static android.support.v7.app.AlertDialog.OnClickListener;
import static com.example.myapplication.StorageUtil.savePic;
import static com.example.myapplication.Util.FROM_FILE_BROWSER;
import static com.example.myapplication.Util.setFromWhichActivity;
import static com.example.myapplication.Util.setRecycleViewStyle;
import static com.example.myapplication.Util.showToastIns;
import static com.example.myapplication.Util.takeScreenShot;
import static com.example.myapplication.sRecycleViewStyle.*;


// 1. 過class方式觸發元件
//public class MainActivity extends AppCompatActivity{

// 2. implements就直接寫在主程式MainActivity上面
//public class MainActivity extends AppCompatActivity implements OnTouchListener {

public class MainActivity extends AppCompatActivity{

    private static final String TAG="Alan";
    private EditText num_height;
    private EditText num_weight;
    private TextView num_result;
    private TextView result_suggest;
    private TextView last_result;
    private Button exe_button;
    private Button exe_seeReport;
    private Button exe_BLE;
    private Button exe_DirectConnect;
    private ImageView ScreenShotImg;

    private Switch sw_AutoScan;

    private ImageView MyImageView;
    private ImageView MyImageView1;
    private double BMI, I_Height, I_Weight;

    private static final int CUSTOM_DIALOG_DELAY_TIME = 10000;  // 10s

    private static final int ACTIVITY_REPORT = 1000;
    private static final int ACTIVITY_BLUETOOTH = 1001;
    private static final int ACTIVITY_CAMERA = 1002;
    private static final int ACTIVITY_READ_STORAGE = 1003;
    //private static final int ACTIVITY_INTERNET = 1004;
    //private static final int ACTIVITY_VIBRATE = 1005;
    private static final int ACTIVITY_CAMERA2 = 1006;
    //private static final int ACTIVITY_CAMERA3 = 1007;
    private static final int ACTIVITY_WRITE_STORAGE = 1008;
    private static final int ACTIVITY_RECORD_AUDIO = 1009;
    private static final int ACTIVITY_SPEECHTOTEXT = 1010;
    private static final int ACTIVITY_SPEECHTOTEXT2 = 1011;
    private static final int ACTIVITY_SPEECHTOTEXT3 = 1012;
    private static final int ACTIVITY_FILE_MANAGER = 1013;

    public static MainActivity instance=null;

    private int mImageCounter=0;
    private ProgressDialog PDialog = null;
    private ProgressDialog PDialog_H = null;

    SharedPreferences mem_DirectConn;
    SharedPreferences mem_SaveMacAddr;
    SharedPreferences mem_AutoScanSetting;
    SharedPreferences mem_ScreenCap;

    private static String savedMacAddr;
    private int count=0;
    private boolean ongoing=false;

    ArrayAdapter<CharSequence> nAdapter;

    private boolean isCustomerDialogShow=false;

    // USB相關
    private UsbManager mUsbManager = null;
    IntentFilter filterAttached_and_Detached = null;
    private static final String ACTION_USB_PERMISSION = "android.list_usb_otg.USB_PERMISSION";

    public final static String ACTION_TELEPHONE =
            "com.example.myapplication.MainActivity.ACTION_TELEPHONE";
    public final static String ACTION_IMAGE_CAPTURE =
            "com.example.myapplication.MainActivity.ACTION_IMAGE_CAPTURE";
    public final static String ACTION_NETWORK_CAHNGE =
            "android.net.conn.CONNECTIVITY_CHANGE";
    public final static String ACTION_MY_BROADCAST = "com.example.myapplication.MY_BROADCAST";

    //------------------------------------------------------------------------------------------
    // USB Broadcast Receiver
    //------------------------------------------------------------------------------------------
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.w(TAG, "mUsbReceiver"+action);

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if(device != null){
                        Log.w(TAG,"DEATTCHED-" + device);
                    }
                }
            }

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                synchronized (this) {
                    UsbDevice device =  intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                        if (device != null) {
                            Log.w(TAG, "ATTACHED-" + device);
                        }
                    } else {
                        PendingIntent mPermissionIntent;
                        mPermissionIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_ONE_SHOT);
                        mUsbManager.requestPermission(device, mPermissionIntent);
                    }
                }
            }

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                        if(device != null){
                            Log.w(TAG,"PERMISSION-" + device);
                        }
                    }
                    else{
                        Log.w(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };
    //------------------------------------------------------------------------------------------

    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            //Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };
    private List<String> mMissPermissions = new ArrayList<>();
    private static final int REQUEST_CODE = 1014;

    private void checkAndRequestPermissions() {
        mMissPermissions.clear();
        for (String permission : REQUIRED_PERMISSION_LIST) {
            int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                mMissPermissions.add(permission);
            }
        }
        // check permissions has granted
        if (mMissPermissions.isEmpty()) {
            Log.w(TAG, "所有權限完成");
        } else {
            ActivityCompat.requestPermissions(this,
                    mMissPermissions.toArray(new String[mMissPermissions.size()]),
                    REQUEST_CODE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.w(TAG, "MainActivity: on Create");

        instance = this;    // 這裡的function可以被其他class調用

        if (savedInstanceState!=null){
            mImageCounter = savedInstanceState.getInt("mImageCounter");
        }
        Log.w(TAG, "mImageCounter:"+mImageCounter);

        //ActionBar mActionBar = getActionBar(); //取得Activity的ActionBar
        //assert mActionBar != null;
        //mActionBar.setDisplayShowTitleEnabled(false); //false : 隱藏程式標題


        //------------------------------------------------------------------------------------------
        // 註冊context menu 浮動選單 (長按空白處出現的選單)
        //------------------------------------------------------------------------------------------
        registerForContextMenu(findViewById(R.id.mainmenu_layout));

        //------------------------------------------------------------------------------------------
        // USB 裝置
        //------------------------------------------------------------------------------------------
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        filterAttached_and_Detached = new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        filterAttached_and_Detached.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filterAttached_and_Detached.addAction(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filterAttached_and_Detached);
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Log.w(TAG, deviceList.size()+" USB device(s) found");
        for (UsbDevice device : deviceList.values()) {
            Log.w(TAG, "" + device);
        }
        //------------------------------------------------------------------------------------------

        mem_DirectConn = getSharedPreferences("BLE_DirectConn", MODE_PRIVATE);
        mem_AutoScanSetting = getSharedPreferences("AUTO_SCAN", MODE_PRIVATE);
        mem_ScreenCap = getSharedPreferences("SCREENCAP", MODE_PRIVATE);

        checkAndRequestPermissions();   // 取得REQUIRED_PERMISSION_LIST裡的權限
        initView();
        clearInputData();
        ShowAnimation();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        Log.w(TAG, "MainActivity:onSaveInstanceState:"+mImageCounter);
        savedInstanceState.putInt("mImageCounter", mImageCounter);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mImageCounter = savedInstanceState.getInt("mImageCounter");
        Log.w(TAG, "MainActivity:onRestoreInstanceState:"+mImageCounter);
    }


    protected void onStart()
    {
        super.onStart();
        Log.w(TAG, "MainActivity: on start");
    }

    protected void onResume()
    {
        super.onResume();
        Log.w(TAG, "MainActivity: on resume");

        // 動態註冊廣播
        registerReceiver(mMainUpdateReceiver, MainActivityIntentFilter());

        mem_SaveMacAddr = getSharedPreferences("BLE_MAC_ADDR", MODE_PRIVATE);
        savedMacAddr=mem_SaveMacAddr.getString("BLE_MAC_ADDR", "");
        if (Objects.requireNonNull(savedMacAddr).length()==0) {
            exe_DirectConnect.setEnabled(false);    // 如果沒有儲存的MAC，"一鍵連線"按鈕不可按
        }else{
            exe_DirectConnect.setEnabled(true);
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);   // hide keyboard
    }

    protected void onPause()
    {
        super.onPause();
        Log.w(TAG, "MainActivity: on pause");
        unregisterReceiver(mMainUpdateReceiver);    // 狀態列通知欄廣播
    }

    protected void onStop()
    {
        super.onStop();
        Log.w(TAG, "MainActivity: on stop");
    }

    protected void onDestroy()
    {
        super.onDestroy();
        Log.w(TAG, "MainActivity: on destroy");

        // USB
        unregisterReceiver(mUsbReceiver);
    }

    //==============================================================================
    //==============================================================================
    //==============================================================================
    @SuppressLint("ClickableViewAccessibility")
    private void initView()
    {
        num_height =  findViewById(R.id.height);
        num_weight =  findViewById(R.id.weight);
        num_result =  findViewById(R.id.result);
        last_result = findViewById(R.id.lastResult);

        MyImageView =  findViewById(R.id.imageView);
        MyImageView1 =  findViewById(R.id.imageView3);
        ImageView myImageView2 = findViewById(R.id.imageView2);
        MyImageView.setOnTouchListener(mOnTouchListener);
        MyImageView1.setOnTouchListener(mOnTouchListener);
        myImageView2.setOnTouchListener(mOnTouchListener);

        exe_button =  findViewById(R.id.button);
        exe_button.setOnClickListener(calcBMI);
        Button exe_clearButton = findViewById(R.id.clearbutton);
        exe_clearButton.setOnClickListener(calcBMI);
        exe_seeReport = findViewById(R.id.seereport);
        exe_seeReport.setOnClickListener(calcBMI);
        Button exe_Notify = findViewById(R.id.notifybtn);
        exe_Notify.setOnClickListener(calcBMI);
        Button exe_NextPage = findViewById(R.id.nextpagebtn);
        exe_NextPage.setOnClickListener(calcBMI);
        Button exe_showImage = findViewById(R.id.showimage);
        exe_showImage.setOnClickListener(calcBMI);
        exe_BLE = findViewById(R.id.ble_btn);
        exe_BLE.setOnClickListener(calcBMI);

        Button exe_Dialog = findViewById(R.id.DialogBtn);
        exe_Dialog.setOnClickListener(calcBMI);
        exe_Dialog.setSelected(true);// 跑馬燈啟動

        exe_DirectConnect = findViewById(R.id.DirectCon);
        exe_DirectConnect.setOnClickListener(calcBMI);

        Button exe_immersive = findViewById(R.id.immersiveBtn);
        exe_immersive.setOnClickListener(calcBMI);

        Button exe_Snacker = findViewById(R.id.snackbatBtn);
        exe_Snacker.setOnClickListener(calcBMI);
        exe_Snacker.setSelected(true);// 跑馬燈啟動

        Button exe_ScreenShot = findViewById(R.id.screencapBtn);
        exe_ScreenShot.setOnClickListener(calcBMI);
        ScreenShotImg = findViewById(R.id.screencapImg);
        ScreenShotImg.setVisibility(View.GONE);

        Button exe_customer = findViewById(R.id.CustomeDialogShowBtn);
        exe_customer.setOnClickListener(calcBMI);
        exe_customer.setSelected(true); // 跑馬燈啟動

        Button exe_customer2 = findViewById(R.id.CameraBtn);
        exe_customer2.setOnClickListener(calcBMI);
        exe_customer2.setSelected(true);    // 跑馬燈啟動

        // 動作定義在activity_main.xml的android:onClick，所以此處不需再做OnClickListener
        Button exe_HProgress = findViewById(R.id.progress_hor);
        exe_HProgress.setSelected(true);    // 跑馬燈啟動

        sw_AutoScan = findViewById(R.id.AutoScanSwitch);
        sw_AutoScan.setOnCheckedChangeListener(MainSW);
        sw_AutoScan.setChecked(AutoScanGet());

        result_suggest = findViewById(R.id.suggest);
        result_suggest.setText(getString(R.string.suggest_empty)); // 預設空白字串

        Button exe_Location = findViewById(R.id.locationButton);
        exe_Location.setOnClickListener(calcBMI);

        Button exe_Sensor = findViewById(R.id.sensorbutton);
        exe_Sensor.setOnClickListener(calcBMI);

        Button exe_Battery = findViewById(R.id.batteryBtn);
        exe_Battery.setOnClickListener(calcBMI);

        Button exe_ListView = findViewById(R.id.listviewB);
        exe_ListView.setOnClickListener(calcBMI);
        exe_ListView.setSelected(true);

        //------------------------------------------------
        // 監聽EditText的狀態
        //------------------------------------------------
        num_height.addTextChangedListener(InputWatcher);
        num_weight.addTextChangedListener(InputWatcher);

        //------------------------------------------------
        // Spinner (下拉式選單)
        //------------------------------------------------
        Spinner lunch = findViewById(R.id.spinner);
        nAdapter = ArrayAdapter.createFromResource(
                //this, R.array.lunch, android.R.layout.simple_spinner_item );
                this, R.array.lunch, R.layout.myspinner_layout );   //使用layout來客制化spinner布局

        //加入這個選項之間比較寬鬆
        //nAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nAdapter.setDropDownViewResource(R.layout.myspinner_layout);
        lunch.setAdapter(nAdapter);
        //設置選到之後的動作
        lunch.setOnItemSelectedListener(mItem);
        //lunch.setSelected(true);

        //------------------------------------------------
        // 跑馬燈文字，必須要將setSelected設為true才會開始跑
        //------------------------------------------------
        // TextView
        TextView runningText = findViewById(R.id.runningText);
        //runningText.setSelected(true);
        runningText.requestFocus();
        //------------------------------------------------
    }

    //=======================================================================
    // 顯示動畫 (菊花樣式)  @drawable/amin_pgbar.xml
    //=======================================================================
    private void ShowAnimation(){
        // 使用 AnimationDrawable，不可在image view屬性裡面加入任何圖片，不然會無法顯示動畫
        // 也就是xml裡的此圖app:srcCompat="@drawable/delta" 不能有這行
        if (MyImageView1.getDrawable()==null) {
            MyImageView1.setImageResource(R.drawable.amin_pgbar);   // 菊花樣式

            AnimationDrawable frameAnimation = (AnimationDrawable) MyImageView1.getDrawable();
            frameAnimation.start(); // 開始動畫
            //frameAnimation.stop();    // 停止動畫
        }
    }

    //=======================================================================
    // 監控EditText是否有輸入字，決定"計算"按鈕是否反灰
    //=======================================================================
    private TextWatcher InputWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (TextUtils.isEmpty(num_height.getText()) || TextUtils.isEmpty(num_weight.getText())){
                exe_button.setEnabled(false);
                exe_seeReport.setEnabled(false);
            }else{
                exe_button.setEnabled(true);
                exe_seeReport.setEnabled(true);
            }
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
/*
    // 1. 透過class方式觸發元件
    private final class MyTouchListener implements OnTouchListener{
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageView t_v = (ImageView) v;
            if (t_v == MyImageView)
            {
                Toast.makeText(getApplicationContext(), "發出一個Toast short...", Toast.LENGTH_SHORT).show();
            }
            else if (t_v == MyImageView1)
            {
                Toast.makeText(getApplicationContext(), "發出一個Toast long...", Toast.LENGTH_LONG).show();
            }
            return false;
        }
    };
*/
/*
    // 2. implements就直接寫在主程式MainActivity上面
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView t_v = (ImageView) v;
        if (t_v == MyImageView) {
            Toast.makeText(getApplicationContext(), "發出一個Toast short...", Toast.LENGTH_SHORT).show();
        } else if (t_v == MyImageView1) {
            Toast.makeText(getApplicationContext(), "發出一個Toast long...", Toast.LENGTH_LONG).show();
        }
        return false;
    }
*/
/*
    private void setListener() {
        //呼叫一個新的class
        MyImageView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView t_v = (ImageView) v;
                if (t_v == MyImageView) {
                    Toast.makeText(getApplicationContext(), "發出一個Toast short...", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }
*/

    //=======================================================================
    // User Layout as Toast (用Layout方式顯示客制化Toast)
    //=======================================================================
    public void ShowImageToast()
    {
        Log.w(TAG, "ShowImageToast()");
        //把xml的資源轉成view
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_test, (ViewGroup) findViewById(R.id.toastlayoutt));
        //透過 inflater跟View方式來取得元件的控制權

        //layout.setBackgroundColor(Color.BLUE);  // 背景顏色，如果不定義就照layout設計

        // 字串樣式
        //TextView text = layout.findViewById(R.id.toasttext); // 取得layout字串的位置
        //text.setText(getString(R.string.imageToast));   // 字串
        //text.setTextSize(24);   // 字型大小
        // 透明度，紅，綠，藍
        //text.setTextColor(Color.argb(0xFF, 0x80, 0x80, 0x00));

        // 字串已被上方設定，makeText無論設定什麼字都沒用了
        Toast toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG);

        // 位置
        toast.setGravity(Gravity.CENTER,0,500);
        toast.setView(layout);
        toast.show();
    }

    //=======================================================================
    // Touch Event
    //=======================================================================
    private View.OnTouchListener mOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            if (motionEvent.getAction()==MotionEvent.ACTION_DOWN) {
                //Log.w(TAG, "按下圖片");
                switch(view.getId())
                {
                    case R.id.imageView:
                        Log.w(TAG, "Press Image 1");

                        if(count<100)
                            count++;
                        else
                            count=0;
                        showToastIns(getApplicationContext(), "發出一個Toast short[DOWN]:"+count, Toast.LENGTH_SHORT);
                        break;

                    case R.id.imageView2:
                        Log.w(TAG, "Press Image 2");
                        ShowImageToast();
                        break;

                    case R.id.imageView3:
                        Log.w(TAG, "CustomerToast");
                        CustomerToast(1, "CustomerToast");
                        CustomerToast(0, "客制化Toast");
                        /*
                        //showToastIns(getApplicationContext(), getString(R.string.multiToast), Toast.LENGTH_SHORT);
                        Toast toast=Toast.makeText(getApplicationContext(), "這是用動畫方式做成的image",
                                Toast.LENGTH_SHORT);

                        View view1=toast.getView();

                        // 自定義背景顏色 會造成四邊圓角不見
                        view1.setBackgroundColor(Color.argb(0xa0, 0xff, 0x00, 0x00));

                        TextView message=view1.findViewById(android.R.id.message);
                        message.setTextColor(Color.YELLOW);   // toast文字顏色

                        // 文字部份使用round_corner_toast.xml的設計
                        //message.setBackground(getDrawable(R.drawable.round_corner_toast));

                        toast.show();*/
                        break;
                }
            }
            //else if (motionEvent.getAction()==MotionEvent.ACTION_MOVE) {
                //Log.w(TAG, "移動");
            //}
            else if (motionEvent.getAction()==MotionEvent.ACTION_UP) {
                view.performClick();    // 加入此行解決warning

                //Log.w(TAG, "抬起");
                if (view.getId()==R.id.imageView)
                    showToastIns(getApplicationContext(), "發出一個Toast short[UP]:"+count, Toast.LENGTH_SHORT);
            }
            return true;    // false: can listen "ACTION_DOWN"
                            // true: can listen "ACTION_DOWN", "ACTION_MOVE" and "ACTION_UP"
        }
    };

    //=======================================================================
    // Spinner item event (下拉式選單Event)
    //=======================================================================
    private AdapterView.OnItemSelectedListener mItem = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            //showToastIns(getApplicationContext(), nAdapter.getItem(i), Toast.LENGTH_LONG);
            Log.w(TAG, "Spinner選了:"+nAdapter.getItem(i)+", i:"+i);

            switch(i){
                case 0:
                    break;
                case 1:
                    //Intent intentImage = new Intent(MainActivity.this,ListView1Activity.class);
                    //startActivity(intentImage);
                    break;
                case 2:
                    //Intent intentMusic = new Intent(MainActivity.this,ProgressBarActivity.class);
                    //startActivity(intentMusic);
                    break;
                case 3:
                    //Intent intentVideo = new Intent(MainActivity.this,VideoActivity.class);
                    //startActivity(intentVideo);
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            Log.w(TAG, "沒有選擇");
        }
    };

    //=======================================================================
    // Button event
    //=======================================================================
    private View.OnClickListener calcBMI = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent myIntent;

            switch(v.getId())
            {
                case R.id.button:
                    Log.w(TAG, "Press calculate.");
                    DecimalFormat nf = new DecimalFormat("0.00");

                    //判斷是否有輸入
                    //if (TextUtils.isEmpty(num_height.getText().toString()) ||
                    //    TextUtils.isEmpty(num_weight.getText().toString()))
                    //{
                    //     Toast.makeText(getApplicationContext(), R.string.cal_fail, Toast.LENGTH_SHORT).show();
                    //     num_result.setText(getText(R.string.cal_fail));    // 輸入錯誤
                    //}
                    //else

                    // 使用Exception的方式判斷是否有輸入
                    /*
                    try {

                        //身高
                        I_Height = Double.parseDouble(num_height.getText().toString()) / 100;
                        //體重
                        I_Weight = Double.parseDouble(num_weight.getText().toString());
                        // 計算出BMI值
                        BMI = I_Weight / (I_Height * I_Height);

                        //結果 (Your BMI is:)
                        num_result.setText(getText(R.string.cal_result) + nf.format(BMI));

                        //建議
                        if (BMI > 25) //太重了
                            result_suggest.setText(R.string.suggest_over);
                        else if (BMI < 20) //太輕了
                            result_suggest.setText(R.string.suggest_light);
                        else //剛剛好
                            result_suggest.setText(R.string.suggest_good);

                        // 隱藏軟鍵盤
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);   // 強制隱藏
                            //imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);   // toggle方式
                        }
                    } catch (Exception obj)   // 使用try...catch的方式判斷是否有輸入
                    {
                        Toast.makeText(getApplicationContext(), R.string.cal_fail, Toast.LENGTH_SHORT).show();
                        result_suggest.setText(getText(R.string.cal_fail));    // 輸入錯誤
                    }*/
                    if (CalCulate_BMI())
                    {
                        String strRes=getString(R.string.cal_result) + nf.format(BMI);
                        num_result.setText(strRes);

                        if (BMI > 25) //太重了
                        {
                            result_suggest.setText(R.string.suggest_over);
                            result_suggest.setTextColor(Color.RED);
                        }
                        else if (BMI < 20) //太輕了
                        {
                            result_suggest.setText(R.string.suggest_light);
                            result_suggest.setTextColor(Color.BLUE);
                        }
                        else //剛剛好
                        {
                            result_suggest.setText(R.string.suggest_good);
                            result_suggest.setTextColor(Color.GREEN);
                        }

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);   // 強制隱藏
                        }
                    }else{
                        showToastIns(getApplicationContext(), getString(R.string.cal_fail), Toast.LENGTH_SHORT);
                        result_suggest.setText(getString(R.string.suggest_empty));
                        num_result.setText(getString(R.string.cal_result)); // 清除後面的數字
                    }
                    clearInputData();
                    num_height.requestFocus();  // focus在身高
                    break;

                case R.id.clearbutton:
                    //Log.w(TAG, "Press CLEAR");
                    showToastIns(getApplicationContext(), getString(R.string.clear_btn), Toast.LENGTH_SHORT);
                    clearAllData();
                    //MyImageView.setVisibility(View.INVISIBLE);  // 隱藏圖片
                    MyImageView.setVisibility(View.GONE);   // 刪除圖片，包含位置，後面的會往前移動
                    break;

                case R.id.seereport:
                    Log.w(TAG, "Press REPORT，use startActivityForResult");
                    mHowGoP2=0;

                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    intent.setClass(MainActivity.this, ReportActivity.class);

                    if (CalCulate_BMI()) {    // 輸入正確值
                        Log.w(TAG, "Input correct.");
                        bundle.putString("KEY_HEIGHT", String.valueOf(I_Height));// 轉成字串
                        bundle.putString("KEY_WEIGHT", String.valueOf(I_Weight));// 轉成字串
                        bundle.putString("KEY_BMI", String.valueOf(BMI));   // 轉成字串
                        bundle.putString("KEY_RESULT", String.valueOf(1));
                        intent.putExtras(bundle);
                        startActivityForResult(intent, ACTIVITY_REPORT);    // 需將結果回傳的start activity
                    }
                    else
                    {
                        Log.w(TAG, "Input incorrect.");
                        bundle.putString("KEY_HEIGHT", String.valueOf(0));// 轉成字串
                        bundle.putString("KEY_WEIGHT", String.valueOf(0));// 轉成字串
                        bundle.putString("KEY_BMI", String.valueOf(0));   // 轉成字串
                        bundle.putString("KEY_RESULT", String.valueOf(0));
                        intent.putExtras(bundle);
                        startActivityForResult(intent, ACTIVITY_REPORT);    // 需將結果回傳的start activity
                    }
                    break;

                case R.id.notifybtn:
                    Log.w(TAG, "Press NOTIFY");
                    Notify();
                    break;

                case R.id.nextpagebtn:
                    Log.w(TAG, "NEXT，use startActivity");
                    myIntent = new Intent(MainActivity.this,ReportActivity.class);
                    startActivity(myIntent);
                    mHowGoP2=1;
                    break;

                // 按鈕更換圖片
                case R.id.showimage:
                    Log.w(TAG, "Change pic="+ (mImageCounter%5));
                    MyImageView.setVisibility(View.VISIBLE);  // 顯示圖片
                    sendBroadcast(new Intent(ACTION_MY_BROADCAST));

                    switch(mImageCounter%5)
                    {
                        default:
                        case 0:
                            MyImageView.setImageResource(R.drawable.apple);
                            break;
                        case 1:
                            MyImageView.setImageResource(R.drawable.bee);
                            break;
                        case 2:
                            MyImageView.setImageResource(R.drawable.birthdaycake);
                            break;
                        case 3:
                            MyImageView.setImageResource(R.drawable.burger);
                            break;
                        case 4:
                            MyImageView.setImageResource(R.drawable.icecream);
                            break;
                    }
                    mImageCounter++;
                    Log.w(TAG, "mImageCounter:"+mImageCounter);
                    break;

                case R.id.ble_btn:
                    Log.w(TAG, "Press BLE");

                    // 判斷是否支援Bluetooth及Bluetooth是否打開
                    BluetoothManager mBluetoothManager = (BluetoothManager) getBaseContext().getSystemService(Context.BLUETOOTH_SERVICE);
                    assert mBluetoothManager != null;
                    BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
                    if(mBluetoothAdapter ==null){
                        showToastIns(getApplicationContext(), "不支援藍芽", Toast.LENGTH_SHORT);
                        //finish();   //如果==null，利用finish()取消程式
                        exe_BLE.setEnabled(false);
                        return;
                    }
                    else {
                        if (!mBluetoothAdapter.isEnabled()) {
                            myIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(myIntent, ACTIVITY_BLUETOOTH);
                            Log.w(TAG, "Ask open Bluetooth");
                        } else{

                            // MAC是空的，且direct connect設為on
                            if (savedMacAddr.length()==0 && mem_DirectConn.getBoolean("BLE_DirectConn", true)){

                                SharedPreferences.Editor editor = mem_DirectConn.edit(); //獲取編輯器
                                editor.putBoolean("BLE_DirectConn", false); // 取消direct connect flag
                                editor.apply();
                                editor.commit();    //提交
                            }

                            // 跳轉頁面
                            myIntent = new Intent(MainActivity.this, BLEActivity.class);
                            startActivity(myIntent);
                        }
                    }
                    break;

                case R.id.DialogBtn:    // 進度條(圈)
                    circleProgress();
                    break;

                case R.id.CustomeDialogShowBtn:
                    custom_Progress();
                    break;

                case R.id.CameraBtn:
                    // 取得 Camera 權限
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                        Log.w(TAG, "CameraActivity:Get CAMERA permission success.");

                        myIntent = new Intent(MainActivity.this, ListViewActivity.class);
                        startActivity(myIntent);
                    }else{
                        Log.w(TAG, "CameraActivity:Get CAMERA permission fail.");
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, ACTIVITY_CAMERA);
                    }

                    //Intent intent4 = new Intent(MainActivity.this, CameraActivity.class);
                    //startActivity(intent4);
                    break;

                case R.id.DirectCon:
                    // 判斷是否支援Bluetooth及Bluetooth是否打開
                    mBluetoothManager = (BluetoothManager) getBaseContext().getSystemService(Context.BLUETOOTH_SERVICE);
                    assert mBluetoothManager != null;
                    mBluetoothAdapter = mBluetoothManager.getAdapter();
                    if(mBluetoothAdapter ==null){
                        showToastIns(getApplicationContext(), "不支援藍芽", Toast.LENGTH_SHORT);
                        //finish();   //如果==null，利用finish()取消程式
                        exe_BLE.setEnabled(false);
                        return;
                    }
                    else {
                        if (!mBluetoothAdapter.isEnabled()) {
                            myIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(myIntent, ACTIVITY_BLUETOOTH);
                            Log.w(TAG, "Ask open Bluetooth");
                        } else {
                            //Log.w(TAG, "Direct connect.");
                            if (savedMacAddr.length() != 0) {
                                SharedPreferences.Editor editor = mem_DirectConn.edit(); //獲取編輯器
                                editor.putBoolean("BLE_DirectConn", true);
                                editor.apply();
                                editor.commit();    //提交
                            }
                            myIntent = new Intent(MainActivity.this, BLEActivity.class);
                            startActivity(myIntent);
                        }
                    }
                    break;

                case R.id.snackbatBtn:
                    View rootView = findViewById(R.id.mainmenu_layout);

                    Snackbar sb = Snackbar.make(rootView, getString(R.string.down_notification), Snackbar.LENGTH_LONG);
                    /*
                    sb.setAction(getString(R.string.cancel), new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            // 按下右邊文字後的行為
                            showToastIns(getApplicationContext(), getString(R.string.cancel), Toast.LENGTH_SHORT);
                        }
                    });*/
                    sb.setAction(getString(R.string.filemanager), new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            // 按下右邊文字後的行為
                            setFromWhichActivity(FROM_FILE_BROWSER);
                            Intent intentAudioRecord = new Intent(MainActivity.this, FileManagerActivity.class);
                            startActivity(intentAudioRecord);
                        }
                    });

                    View vv=sb.getView();
                    vv.setBackgroundColor(Color.BLUE);  // 背景顏色
                    TextView Title=vv.findViewById(android.support.design.R.id.snackbar_text);
                    Title.setTextColor(Color.parseColor("#90CAF9"));  // 標頭文字顏色

                    sb.setActionTextColor(Color.YELLOW);    // 右邊文字顏色
                    sb.setDuration(5000);   // 持續時間, 如果沒定義就照.make裡面的時間設定, 跟Toast一樣
                    sb.show();
                    break;

                case R.id.screencapBtn:
                    // 截圖
                    Bitmap bm=takeScreenShot(MainActivity.this);
                    ScreenShotImg.setImageBitmap(bm);
                    ScreenShotImg.setVisibility(View.VISIBLE);

                    // 將截圖儲存
                    File f = new File(Environment.getExternalStorageDirectory(), "ScreenCap");
                    Log.w(TAG,"path:" + f.getAbsolutePath());
                    if (!f.exists()) {
                        boolean isCreat=f.mkdir();  // 有回傳值，為解決warning才加入isCreat
                        if (!isCreat){
                            Log.w(TAG, "無法建立資料夾");
                        }else{
                            Log.w(TAG, "ScreenCap建立完成");
                        }
                    }
                    String path1 = "cap_"+ScreenCapIndexGet()+".jpg";
                    File n = new File(f, path1);
                    savePic(bm, n.getAbsolutePath());
                    ScreenCapIndexSet(ScreenCapIndexGet()+1);
                    showToastIns(getApplicationContext(), "儲存至"+n.getAbsolutePath(), Toast.LENGTH_SHORT);
                    break;

                case R.id.immersiveBtn:
                    myIntent = new Intent(MainActivity.this, ListView1Activity.class);
                    startActivity(myIntent);
                    break;

                case R.id.locationButton:
                    myIntent = new Intent(MainActivity.this, LocationActivity.class);
                    startActivity(myIntent);
                    break;

                case R.id.sensorbutton:
                    myIntent = new Intent(MainActivity.this, SensorActivity.class);
                    startActivity(myIntent);
                    break;
                    
                case R.id.batteryBtn:
                    myIntent = new Intent(MainActivity.this, BattaryActivity.class);
                    startActivity(myIntent);
                    break;

                case R.id.listviewB:
                    myIntent = new Intent(MainActivity.this, ListViewMenuActivity.class);
                    startActivity(myIntent);
                    break;
            }
        }
    };

    //=================================================================
    // 顯示客制化Dialog，使用xml "Layout\progress_bar_dialog.xml"
    // CustomProgress.java
    //=================================================================
    CustomProgress customProgress = CustomProgress.getInstance();
    private Thread customThread;

    public void custom_Progress() {
        customProgress.showProgress(this, "", false);
        isCustomerDialogShow = true;

        customThread = new Thread() {
            @Override
            public void run() {
                if (isCustomerDialogShow) {
                    try {
                        sleep(CUSTOM_DIALOG_DELAY_TIME);    // 顯示時間
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        customProgress.hideProgress();
                        isCustomerDialogShow = false;
                    }
                } else {
                    customProgress.hideProgress();
                }
            }
        };
        customThread.start();
    }

    //=================================================================
    // 顯示客制化Dialog上面的button event
    // 後面一定要加View才能用，不然會當掉
    // onButton1 / onButton2定義在progress_bar_dialog.xml裡
    //=================================================================
    public void onButton1(View view){
        //Log.w(TAG, "onButton1:" +a.isInterrupted());
        customProgress.hideProgress();
        isCustomerDialogShow=false;

        if (!customThread.isInterrupted()){
            customThread.interrupt();  // 必須要中斷Thread，不然下次執行時間會不正確
        }

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "MainActivity:Get RECORD_AUDIO permission success.");
            Intent intentSpeech = new Intent(MainActivity.this, SpeechToTextActivity.class);
            startActivity(intentSpeech);
        } else {
            Log.w(TAG, "MainActivity:Get RECORD_AUDIO permission fail.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, ACTIVITY_SPEECHTOTEXT);
        }
    }

    public void onButton2(View view){
        //Log.w(TAG, "onButton2" );
        customProgress.hideProgress();
        isCustomerDialogShow=false;

        if (!customThread.isInterrupted()){
            customThread.interrupt();  // 必須要中斷Thread，不然下次執行時間會不正確
        }

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "MainActivity:Get RECORD_AUDIO permission success.");
            Intent intentSpeech = new Intent(MainActivity.this, SpeechToText2Activity.class);
            startActivity(intentSpeech);
        } else {
            Log.w(TAG, "MainActivity:Get RECORD_AUDIO permission fail.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, ACTIVITY_SPEECHTOTEXT2);
        }
    }

    public void onButton3(View view){
        Log.w(TAG, "onButton3" );
        customProgress.hideProgress();
        isCustomerDialogShow=false;

        if (!customThread.isInterrupted()){
            customThread.interrupt();  // 必須要中斷Thread，不然下次執行時間會不正確
        }

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "MainActivity:Get RECORD_AUDIO permission success.");
            Intent intentSpeech = new Intent(MainActivity.this, SpeechToText3Activity.class);
            startActivity(intentSpeech);
        } else {
            Log.w(TAG, "MainActivity:Get RECORD_AUDIO permission fail.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, ACTIVITY_SPEECHTOTEXT3);
        }
    }

    public void onExit(View view){
        Log.w(TAG, "onExit" );
        customProgress.hideProgress();
        isCustomerDialogShow=false;
        if (!customThread.isInterrupted()){
            customThread.interrupt();  // 必須要中斷Thread，不然下次執行時間會不正確
        }
    }
    //=================================================================



    //=================================================================
    // 自定義Toast (按下菊花動畫後的 Toast)
    //=================================================================
    Toast toast;
    private void CustomerToast(int style, String str){
        //showToastIns(getApplicationContext(), getString(R.string.multiToast), Toast.LENGTH_SHORT);
        toast=Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT);

        View view1 = toast.getView();

        TextView message = view1.findViewById(android.R.id.message);
        message.setTextColor(Color.YELLOW);   // toast文字顏色


        if (style==1) {
            // 文字部份使用round_corner_toast.xml的設計，但toast外圍仍然維持黑色半透明
            message.setBackground(getDrawable(R.drawable.round_corner_toast));
        }
        else {
            // 自定義背景顏色 會造成四邊圓角不見
            view1.setBackgroundColor(Color.argb(0xa0, 0xff, 0x00, 0x00));
        }

        toast.show();
    }

    //=================================================================
    // 水平 progress bar
    //=================================================================
    public void download(View view){
        ongoing=true;
        PDialog_H=new ProgressDialog(this);
        PDialog_H.setTitle("進度條測試");
        PDialog_H.setMessage("這是水平進度條");
        PDialog_H.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        PDialog_H.setIndeterminate(false);  // 如果true，就會一直循環不會停
        PDialog_H.setProgress(0);
        PDialog_H.setCancelable(false); //按空白處不會被取消

        PDialog_H.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ongoing=false;
                    }
                });
        PDialog_H.show();

        final int totalProgressTime = 100;
        final Thread t = new Thread() {
            @Override
            public void run() {
                    int jumpTime = 0;

                    while (jumpTime < totalProgressTime) {
                        try {
                            sleep(200);
                            jumpTime += 5;
                            PDialog_H.setProgress(jumpTime);
                            Log.w(TAG, "pro:" + jumpTime);
                            if (!ongoing) { // 按下取消後，關閉dialog
                                Log.w(TAG, "stop");
                                PDialog_H.dismiss();
                                break;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            ongoing=false;
                        }
                    }
                    PDialog_H.dismiss();
                    ongoing=false;
            }
        };
        t.start();
    }

    //=================================================================
    // 轉圈 progress bar
    //=================================================================
    public void circleProgress(){
        PDialog = new ProgressDialog(this);
        PDialog.setTitle("進度條測試");
        PDialog.setMessage("這是轉圈進度條");
        PDialog.setIndeterminate(true); // 一直轉圈
        PDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // 沒寫也是spinning
        PDialog.setCancelable(false);   // 按別地方無法被取消
        PDialog.show();

        new Thread(){
            public void run(){
                try{
                    sleep(10000);   // 持續時間
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                finally{
                    PDialog.dismiss();
                }
            }
        }.start();
    }

    //=================================================================
    // Switch Event
    //=================================================================
    private CompoundButton.OnCheckedChangeListener MainSW= new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (R.id.AutoScanSwitch==compoundButton.getId()) {
                AutoScanSet(b);
            }
        }
    };

    //=================================================================
    // AutoScan 儲存/讀取
    //=================================================================
    private boolean AutoScanGet(){
        return mem_AutoScanSetting.getBoolean("AUTO_SCAN", true);
    }

    private void AutoScanSet(boolean bEnable){

        SharedPreferences.Editor editor = mem_AutoScanSetting.edit(); //獲取編輯器
        editor.putBoolean("AUTO_SCAN", bEnable);
        editor.apply();
        editor.commit();    //提交
    }

    //=================================================================
    // Screencap index 儲存/讀取
    //=================================================================
    private int ScreenCapIndexGet(){
        int aa=0;
        return mem_ScreenCap.getInt("SCREENCAP", aa);
    }

    private void ScreenCapIndexSet(int aa){

        SharedPreferences.Editor editor = mem_ScreenCap.edit(); //獲取編輯器
        editor.putInt("SCREENCAP", aa);
        editor.apply();
        editor.commit();    //提交
    }

    public static int mHowGoP2=0;// 0: 按"看報告", 1:按"下一頁"
    //public int HowToGoPage2(){
    //    return mHowGoP2;    // 0: 按"看報告", 1:按"下一頁"
    //}

    private boolean CalCulate_BMI()
    {
        try{
            double temp;
            //身高
            I_Height = Double.parseDouble(num_height.getText().toString());
            temp=I_Height/100;
            //體重
            I_Weight = Double.parseDouble(num_weight.getText().toString());
            // 計算出BMI值
            BMI = I_Weight / (temp * temp);
            return true;
        }catch (Exception obj)
        {
            return false;
        }
    }

    // onActivityResult函式與startActivityForResult函式是共生的關係，
    // startActivityForResult函式負責呼叫其他Activity，
    // 而onActivityResult函式來處理被呼叫的Activity所傳回的資訊
    // 當被呼叫的Activity完成工作時，就會通知負責呼叫的Activity，
    // 負責呼叫的Activity會使用onActivityResult函式來處理被呼叫的Activity所傳回的訊息
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);

        Log.w(TAG, "requestCode:"+requestCode+",resultCode:"+resultCode);

        if(resultCode == RESULT_OK && requestCode == ACTIVITY_REPORT)
        {
            Bundle bundle = intent.getExtras();
            String strResult;

            if (bundle != null) {
                strResult = getString(R.string.last_result)+bundle.getString("LAST_DATA");
            }else{
                strResult = getString(R.string.last_result);
            }
            last_result.setText(strResult);
        }

        else if (requestCode == ACTIVITY_BLUETOOTH){
            Log.w(TAG, "ACTIVITY_BLUETOOTH");
            if (resultCode == RESULT_OK) {
                Log.w(TAG, "按了開啟bluetooth後，跳轉頁面");

                Intent intent2 = new Intent(MainActivity.this, BLEActivity.class);
                startActivity(intent2);
            }
            else if (resultCode == RESULT_CANCELED){
                Log.w(TAG, "按了取消");
            }
        }
        else if (requestCode == ACTIVITY_FILE_MANAGER && resultCode == RESULT_OK){
            String PathHolder = Objects.requireNonNull(intent.getData()).getPath();
            Toast.makeText(MainActivity.this, PathHolder , Toast.LENGTH_LONG).show();
        }
        clearAllKeepLastData(); // 只保留 "上次測試結果" 項目，其餘清空
        num_height.requestFocus();  // focus在身高
    }

    //=======================================================================
    // 要求系統權限回應
    //=======================================================================
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults){

        Log.w(TAG, "requestCode:"+requestCode+",length"+permissions.length);

        for (int i = 0, len = permissions.length; i < len; i++) {
            //String permission = permissions[i];
            Log.w(TAG, "permis:"+permissions[i]);

            if (requestCode == ACTIVITY_CAMERA) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED){

                    Intent intent4 = new Intent(MainActivity.this, ListViewActivity.class);
                    startActivity(intent4);
                }else{
                    showToastIns(getApplicationContext(), "沒有攝影機的權限-1", Toast.LENGTH_LONG);
                }
            }else if (requestCode==ACTIVITY_READ_STORAGE){
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    Log.w(TAG, "Read Storage Permission not granted.");
                    showToastIns(getApplicationContext(), "沒有讀取外部儲存空間的權限，將無法正常使用此App", Toast.LENGTH_LONG);
                    finish();
                }
            }else if (requestCode==ACTIVITY_WRITE_STORAGE){
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    Log.w(TAG, "Write Storage Permission not granted.");
                    showToastIns(getApplicationContext(), "沒有寫入外部儲存空間的權限，將無法正常使用此App", Toast.LENGTH_LONG);
                    //finish();
                }
            }
            else if (requestCode==ACTIVITY_CAMERA2){
                if (grantResults[i] ==PackageManager.PERMISSION_GRANTED) {
                    Intent intentCamera2 = new Intent(MainActivity.this, ListViewActivity.class);
                    startActivity(intentCamera2);
                }else {
                    showToastIns(getApplicationContext(), "沒有攝影機的權限-2", Toast.LENGTH_LONG);
                }
            }else if (requestCode==ACTIVITY_RECORD_AUDIO){
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    Intent intentVideo = new Intent(MainActivity.this,VideoActivity.class);
                    startActivity(intentVideo);
                }else {
                    Log.w(TAG, "Audio Record Permission not granted.");
                    showToastIns(getApplicationContext(), "沒有錄音權限-1", Toast.LENGTH_LONG);
                }
            }else if (requestCode==ACTIVITY_SPEECHTOTEXT){
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    Intent intentVideo = new Intent(MainActivity.this,SpeechToTextActivity.class);
                    startActivity(intentVideo);
                }else {
                    Log.w(TAG, "Audio Record Permission not granted.");
                    showToastIns(getApplicationContext(), "沒有錄音權限-2", Toast.LENGTH_LONG);
                }
            }else if (requestCode==ACTIVITY_SPEECHTOTEXT2){
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    Intent intentVideo = new Intent(MainActivity.this,SpeechToText2Activity.class);
                    startActivity(intentVideo);
                }else {
                    Log.w(TAG, "Audio Record Permission not granted.");
                    showToastIns(getApplicationContext(), "沒有錄音權限-3", Toast.LENGTH_LONG);
                }
            }else if (requestCode==ACTIVITY_SPEECHTOTEXT3){
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    Intent intentVideo = new Intent(MainActivity.this,SpeechToText3Activity.class);
                    startActivity(intentVideo);
                }else {
                    Log.w(TAG, "Audio Record Permission not granted.");
                    showToastIns(getApplicationContext(), "沒有錄音權限-4", Toast.LENGTH_LONG);
                }
            }else if (requestCode==REQUEST_CODE){
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    showToastIns(getApplicationContext(), "拿到『儲存空間』權限了", Toast.LENGTH_LONG);
                }else{
                    showToastIns(getApplicationContext(), "沒有拿到權限", Toast.LENGTH_LONG);
                }
            }
            /*else if (requestCode==ACTIVITY_CAMERA3){
                if (grantResults[i] ==PackageManager.PERMISSION_GRANTED) {
                    Intent intentCamera2 = new Intent(MainActivity.this, Camera3Activity.class);
                    startActivity(intentCamera2);
                }else {
                    showToastIns(getApplicationContext(), "沒有攝影機的權限-3", Toast.LENGTH_LONG);
                }
            }*/
        }
    }

    private void clearInputData()
    {
        num_height.setText(""); // 清空欄位
        num_weight.setText(""); // 清空欄位
    }
    private void clearAllData()
    {
        num_height.setText(""); // 清空欄位
        num_weight.setText(""); // 清空欄位
        num_result.setText(getText(R.string.cal_result)); // 清除後面的數字
        result_suggest.setText("");   // 清空
        last_result.setText(getString(R.string.last_result));
    }
    private  void clearAllKeepLastData()
    {
        num_height.setText(""); // 清空欄位
        num_weight.setText(""); // 清空欄位
        num_result.setText(getText(R.string.cal_result)); // 清除後面的數字
        result_suggest.setText("");   // 清空
    }


    // 按下選單項目，出現對話框，關於一
    private void OpenDialogFunction()
    {
/*
        // 一次寫完的做法
        new Builder(this)
                //.setTitle("關於這支程式")
                //.setMessage("這是計算BMI程式")
                //.setTitle(R.string.about_app)   // 用string方式實現多國語言
                //.setMessage(R.string.about_detail)
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener()  // 靠右，紅字
                //.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener()   // 靠左，紅字
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()    // 靠右，紅字
                {
                    public void onClick(DialogInterface dialog, int which){
                        // empty
                    }
                }).show();


 */

        // 分散寫完
        Builder mmBuilder = new Builder(this);

        mmBuilder.setTitle(R.string.about_app); // 標題
        //mmBuilder.setMessage(R.string.about_detail);    // 內容
        mmBuilder.setMessage(checkCamera2Support());
        mmBuilder.setPositiveButton(android.R.string.ok, new OnClickListener()  // 確認按鈕
                {
                    public void onClick(DialogInterface dialog, int which){
                        showToastIns(getApplicationContext(), "按下確定", Toast.LENGTH_SHORT);
                        //checkCamera2Support();
                    }
                }
        );
        mmBuilder.setNegativeButton(android.R.string.cancel, new OnClickListener()  // 取消按鈕
                {
                    public void onClick(DialogInterface dialog, int which){
                        showToastIns(getApplicationContext(), getString(android.R.string.cancel), Toast.LENGTH_SHORT);
                    }
                }
        );
        mmBuilder.show();   // 放最後，顯示出來
    }

    // 關於二
    private void showInfoDialog()
    {
        Builder ShowInfo=new Builder(MainActivity.this);

        ShowInfo.setTitle(R.string.about_app1);
        ShowInfo.setMessage(R.string.about_BLE);
        ShowInfo.setPositiveButton(android.R.string.ok, new OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                Log.w(TAG, "Dialog: Press OK");
            }
        });
        ShowInfo.setNegativeButton(android.R.string.cancel, new OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                Log.w(TAG, "Dialog: Press Cancel");
            }

        });


        ShowInfo.show();
    }

    // 檔案總管
    private void FileManagerDialog()
    {
        Builder ShowInfo=new Builder(MainActivity.this);

        ShowInfo.setTitle(R.string.filemanager);
        ShowInfo.setMessage(R.string.fileman_select);
        ShowInfo.setPositiveButton(R.string.default_fileman, new OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                //Log.w(TAG, "Dialog: Press OK");
                setFromWhichActivity(FROM_FILE_BROWSER);

                Intent myIntent = new Intent(Intent.ACTION_GET_CONTENT);
                myIntent.setType("*/*");
                startActivityForResult(myIntent, ACTIVITY_FILE_MANAGER);
            }
        });
        ShowInfo.setNegativeButton(R.string.oem_fileman, new OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                //Log.w(TAG, "Dialog: Press Cancel");
                setFromWhichActivity(FROM_FILE_BROWSER);

                Intent myIntent = new Intent(MainActivity.this, FileManagerActivity.class);
                startActivity(myIntent);
            }
        });
        ShowInfo.show();
    }

    //======================================================================
    // 創建右上角 Option menu
    //======================================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        Log.w(TAG, "onCreateOptionsMenu...");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // 每次打開 Option menu 都會再執行一次
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.w(TAG, "onPrepareOptionsMenu...");
        // 設定option menu裡面的check item
        menu.findItem(R.id.AutoScanItem).setChecked(AutoScanGet());
        menu.findItem(R.id.DirectConnectItem).setChecked(mem_DirectConn.getBoolean("BLE_DirectConn", true));
        return true;
    }

    // Option menu item event
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Bundle OptionBundle;
        Intent OptionIntent;

        switch(id)
        {
            case R.id.settingsItem:
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "MainActivity:Get RECORD_AUDIO permission success.");
                    OptionIntent = new Intent(MainActivity.this,VideoActivity.class);
                    startActivity(OptionIntent);
                } else {
                    Log.w(TAG, "MainActivity:Get RECORD_AUDIO permission fail.");
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, ACTIVITY_RECORD_AUDIO);
                }
                break;
            case R.id.savedMacItem:
                showToastIns(getApplicationContext(), mem_SaveMacAddr.getString("BLE_MAC_ADDR", ""), Toast.LENGTH_LONG);
                break;
            case R.id.ImageItem:
                //showToastIns(getApplicationContext(), "按下搜尋了", Toast.LENGTH_SHORT);
                OptionIntent = new Intent(MainActivity.this,ImageActivity.class);
                startActivity(OptionIntent);
                break;
            case R.id.about1Item:
                OpenDialogFunction();
                break;
            case R.id.about2Item:
                showInfoDialog();
                break;
            case R.id.AutoScanItem:
                //Log.w(TAG, "按下auto scan");
                if (item.isChecked()) {
                    item.setChecked(false);
                    AutoScanSet(false);
                    sw_AutoScan.setChecked(false);  // update switch
                }
                else {
                    item.setChecked(true);
                    AutoScanSet(true);
                    sw_AutoScan.setChecked(true);   // update switch
                }
                //m_ret=true;
                break;
            case R.id.DirectConnectItem:
                SharedPreferences.Editor editor = mem_DirectConn.edit();

                if (item.isChecked()) {
                    item.setChecked(false);
                    editor.putBoolean("BLE_DirectConn", false);
                }else{
                    item.setChecked(true);
                    editor.putBoolean("BLE_DirectConn", true);
                }
                editor.apply();
                editor.commit();
                break;
/*
            case R.id.CameraItem:
                // 取得 Camera 權限
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    Log.w(TAG, "CameraActivity:Get CAMERA2 permission success.");

                    Intent intentCamera2 = new Intent(MainActivity.this, Camera2Activity.class);
                    startActivity(intentCamera2);
                }else{
                    Log.w(TAG, "CameraActivity:Get CAMERA2 permission fail.");
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, ACTIVITY_CAMERA2);
                }

                //Intent intentCamera2 = new Intent(MainActivity.this, Camera2Activity.class);
                //startActivity(intentCamera2);
                break;

            case R.id.Camera2Item:
                // 取得 Camera 權限
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    Log.w(TAG, "CameraActivity:Get CAMERA3 permission success.");

                    Intent intentCamera3 = new Intent(MainActivity.this, Camera3Activity.class);
                    startActivity(intentCamera3);
                }else{
                    Log.w(TAG, "CameraActivity:Get CAMERA3 permission fail.");
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, ACTIVITY_CAMERA3);
                }
                break;
*/
            case R.id.ListViewItem:
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    //Log.w(TAG, "CameraActivity:Get CAMERA permission success.");
                    OptionIntent = new Intent(MainActivity.this, ListViewActivity.class);
                    startActivity(OptionIntent);
                }else{
                    Log.w(TAG, "CameraActivity:Get CAMERA permission fail.");
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, ACTIVITY_CAMERA2);
                }
                break;

            case R.id.ListView1Item:
                OptionIntent = new Intent(MainActivity.this, ListView1Activity.class);
                startActivity(OptionIntent);
                break;

            case R.id.speechTextItem:
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "MainActivity:Get RECORD_AUDIO permission success.");
                    OptionIntent = new Intent(MainActivity.this, SpeechToTextActivity.class);
                    startActivity(OptionIntent);
                } else {
                    Log.w(TAG, "MainActivity:Get RECORD_AUDIO permission fail.");
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, ACTIVITY_SPEECHTOTEXT);
                }
                break;

            case R.id.speechText2Item:
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "MainActivity:Get RECORD_AUDIO permission success.");
                    OptionIntent = new Intent(MainActivity.this, SpeechToText2Activity.class);
                    startActivity(OptionIntent);
                } else {
                    Log.w(TAG, "MainActivity:Get RECORD_AUDIO permission fail.");
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, ACTIVITY_SPEECHTOTEXT2);
                }
                break;

            case R.id.FileManagerItem:
                //setFromWhichActivity(FROM_FILE_BROWSER);
                //Intent intentAudioRecord = new Intent(MainActivity.this, FileManagerActivity.class);
                //startActivity(intentAudioRecord);
                FileManagerDialog();
                break;

            case R.id.LocationItem:
                OptionIntent = new Intent(MainActivity.this, LocationActivity.class);
                startActivity(OptionIntent);
                break;

            case R.id.SensorItem:
                OptionIntent = new Intent(MainActivity.this, SensorActivity.class);
                startActivity(OptionIntent);
                break;

            case R.id.BatteryItem:
                OptionIntent = new Intent(MainActivity.this, BattaryActivity.class);
                startActivity(OptionIntent);
                break;

            case R.id.GridView1Item:
                OptionBundle = new Bundle();
                OptionBundle.putString("GRIDVIEW_STYLE", "1");

                OptionIntent = new Intent();
                OptionIntent.setClass(MainActivity.this, GridViewActivity.class);
                OptionIntent.putExtras(OptionBundle);
                startActivity(OptionIntent);
                break;

            case R.id.GridView2Item:
                OptionBundle = new Bundle();
                OptionBundle.putString("GRIDVIEW_STYLE", "2");

                OptionIntent = new Intent();
                OptionIntent.setClass(MainActivity.this, GridViewActivity.class);
                OptionIntent.putExtras(OptionBundle);
                startActivity(OptionIntent);
                break;

            case R.id.RecycleView_V_Item:
                OptionIntent = new Intent(MainActivity.this, RecycleViewActivity.class);
                setRecycleViewStyle(sLinear_Layout_Vertical);
                startActivity(OptionIntent);
                break;
            case R.id.RecycleView_H_Item:
                OptionIntent = new Intent(MainActivity.this, RecycleViewActivity.class);
                setRecycleViewStyle(sLinear_Layout_Horizontal);
                startActivity(OptionIntent);
                break;

            case R.id.RecycleView_Grid_Item:
                OptionIntent = new Intent(MainActivity.this, RecycleViewActivity.class);
                setRecycleViewStyle(sGrid_Layout);
                startActivity(OptionIntent);
                break;
            case R.id.RecycleView_StaggeredGridV_Item:
                OptionIntent = new Intent(MainActivity.this, RecycleViewActivity.class);
                setRecycleViewStyle(sStaggered_Grid_Vertical);
                startActivity(OptionIntent);
                break;
            case R.id.RecycleView_StaggeredGridH_Item:
                OptionIntent = new Intent(MainActivity.this, RecycleViewActivity.class);
                setRecycleViewStyle(sStaggered_Grid_Horizontal);
                startActivity(OptionIntent);
                break;
            case R.id.RecycleView_ImgV_Item:
                setRecycleViewStyle(sLinear_Layout_Vertical_Image);
                OptionIntent = new Intent(MainActivity.this, RecycleViewImgActivity.class);
                startActivity(OptionIntent);
                break;
            case R.id.RecycleView_ImgH_Item:
                setRecycleViewStyle(sLinear_Layout_Horizontal_Image);
                OptionIntent = new Intent(MainActivity.this, RecycleViewImgActivity.class);
                startActivity(OptionIntent);
                break;
            case R.id.RecycleView_ImgGrid_Item:
                setRecycleViewStyle(sGrid_Layout_Image);
                OptionIntent = new Intent(MainActivity.this, RecycleViewImgActivity.class);
                startActivity(OptionIntent);
                break;
            case R.id.RecycleView_StaggeredGrid_ImgV_Item:
                setRecycleViewStyle(sStaggered_Grid_Vertical_Image);
                OptionIntent = new Intent(MainActivity.this, RecycleViewImgActivity.class);
                startActivity(OptionIntent);
                break;
            case R.id.RecycleView_StaggeredGrid_ImgH_Item:
                setRecycleViewStyle(sStaggered_Grid_Horizontal_Image);
                OptionIntent = new Intent(MainActivity.this, RecycleViewImgActivity.class);
                startActivity(OptionIntent);
                break;
            default:
                //m_ret=true;
                break;
        }
        //if (m_ret==true)
            //return true;
        //else
            return super.onOptionsItemSelected(item);
    }

    //======================================================================
    // 長按空白處出現的 Context menu
    //======================================================================
    final int float_menu_group=91;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        String a=getString(R.string.item_text);

        //參數1:群組id, 參數2:itemId, 參數3:item順序, 參數4:item名稱
        for (int i=0; i<5; i++)
            menu.add(float_menu_group, Menu.FIRST+i, Menu.NONE, a+(i+1));

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    // Context menu 的 item event
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getGroupId() == float_menu_group) {
            /*
            showToastIns(getApplicationContext(),
                    item.getTitle(),
                    Toast.LENGTH_SHORT);*/
            int index = item.getItemId();
            Log.w(TAG, "按下" + getString(R.string.item_text) + (index));
        }
        return super.onContextItemSelected(item);
    }

    //======================================================================
    // 在通知欄顯示訊息
    //======================================================================
    private NotificationManager manager;
    private int counter=0;
    public void Notify()
    {
        // 取得service
        if (manager == null)
            manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        Log.w(TAG, String.valueOf(Build.VERSION.SDK_INT));   // 26, SONY_Z2=23

        String id = "channel_1";
        String name= "my_app";
        String description = "143";

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.delta);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,getIntent(),PendingIntent.FLAG_UPDATE_CURRENT);    // 跳至main activity

        // 方法一(直接)，但這個不會將通知移除
        Intent intent1 = new Intent(Intent.ACTION_DIAL);    // 打電話
        PendingIntent pendingIntent1 = PendingIntent.getActivity(this, 0,intent1,PendingIntent.FLAG_UPDATE_CURRENT);

        // 方法二(廣播方式)，可利用 manager.cancel() 將通知移除
        //Intent intent1 = new Intent();
        //intent1.setAction(MainActivity.ACTION_TELEPHONE);
        //PendingIntent pendingIntent1 = PendingIntent.getBroadcast(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent2 = new Intent(ACTION_IMAGE_CAPTURE);   // 拍照
        intent2.setAction(MainActivity.ACTION_IMAGE_CAPTURE);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(this, 0,intent2,PendingIntent.FLAG_UPDATE_CURRENT);

        //Intent intent3 = new Intent(MainActivity.this, BLEActivity.class);    // 跳轉頁面

        if(Build.VERSION.SDK_INT >= 26) // 目前我們是使用這裡的
        {
            //当sdk版本大于26
            NotificationChannel channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);

            channel.setDescription(description);
            //channel.enableLights(true);    // 閃光燈
            //channel.setLightColor(Color.BLUE);   // 燈顏色
            //channel.canShowBadge(); // 桌面圖示角標
            //channel.enableVibration(true);  // 允許震動
            channel.getAudioAttributes();   // 聲音提示
            //channel.setVibrationPattern(new long[]{100, 100, 200}); // 震動模式
            //channel.shouldShowLights(); //是否有燈光

            if (manager != null)
                manager.createNotificationChannel(channel);

            Notification notification = new Notification.Builder(getApplicationContext(), id)
                    .setColor(Color.RED)
                    .setCategory(Notification.CATEGORY_MESSAGE)
                    .setSmallIcon(R.mipmap.ic_launcher) // 小圖
                    .setContentTitle(getString(R.string.title_text))  // 標題
                    .setContentText(getString(R.string.about_detail)) // 內容
                    //.setProgress(0,0,true)  // 進度條，沒有確定的進度時，設0,0,true就會一直循環
                    .setSubText(getString(R.string.about_BLE))  // 第三行，跟setProgress相沖，只能選其中一個用
                    .setContentIntent(pendingIntent)    // //設置意圖
                    .setLargeIcon(bitmap)
                    .setAutoCancel(true)// 用戶點擊後自動消失
                    // 只出現字，沒有圖
                    .addAction(android.R.drawable.sym_action_call,getString(R.string.call), pendingIntent1) // 點擊通知欄按鈕後動作
                    .addAction(android.R.drawable.ic_menu_camera,getString(R.string.picture), pendingIntent2) // 點擊通知欄按鈕後動作//.setTicker("搞什麼東西")
                    // FullScreen是無效的
                    // 在Manifest.xml增加USE_FULL_SCREEN_INTENT權限後，Action的兩個字變成兩個按鈕
                    .setFullScreenIntent(pendingIntent, true)   //使用全畫面通知(跟下拉後出現的樣式一樣)，不是只有上方小圖示通知
                    .setChannelId(id)
                    //.setTicker("悬浮通知")    // 無效
                    //.setPriority(Notification.PRIORITY_MAX)   // 無效
                    //.setDefaults(Notification.DEFAULT_LIGHTS)
                    //.setPriority(Notification.PRIORITY_HIGH)
                    .build();

            // 方式一
            //NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
            //notificationManagerCompat.notify(1, notification);

            // 方式二
            manager.notify(counter, notification);
            //counter++;    // 用不同的id，就不會覆蓋之前的通知
        }
        else
        {
            //当sdk版本小于26 SONY Z2

            Notification notification = new NotificationCompat.Builder(this, id)
                    //.setStyle(new NotificationCompat.BigTextStyle().bigText("AAA")) //大型style
                    .setColor(Color.RED)    // 小圖顏色
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.title_text))
                    .setContentInfo(getString(R.string.cal_result))  // 右邊本文
                    .setVibrate(new long[] {0,300}) // 震動 delay 0ms - 震動300 - delay 500 - 震動700
                    //.setUsesChronometer(true)   // 通知來時開始計數，通常用在電話通話時間累加
                    //.setLights(0xffff00ff, 300, 0)
                    //.setProgress(0,0,true)  // 進度條，沒有確定的進度時，設0,0,true就會一直循環
                    .setSubText(getString(R.string.about_BLE))  // 第三行，跟setProgress相沖，只能選其中一個用
                    .addAction(android.R.drawable.sym_action_call,getString(R.string.call), pendingIntent1) // 點擊通知欄按鈕後動作
                    .addAction(android.R.drawable.ic_menu_camera,getString(R.string.picture), pendingIntent2) // 點擊通知欄按鈕後動作
                    .setContentIntent(pendingIntent)    // 點擊內容時的動作
                    .setSmallIcon(R.mipmap.ic_launcher) // 小圖
                    .setLargeIcon(bitmap)   // 大圖
                    .setAutoCancel(true)    // 用戶點擊後自動消失
                    .setDefaults(Notification.DEFAULT_ALL) // 沒反應
                    .setDefaults(Notification.DEFAULT_SOUND)    // 發出系統預設聲音
                    //.setFullScreenIntent(pendingIntent, true)   //使用全畫面通知(跟下拉後出現的樣式一樣)，不是只有上方小圖示通知
                    //.setNumber(10)
                    .build();

            manager.notify(1,notification);
        }
    }

    //=================================================================
    // 註冊Broadcast filter
    //=================================================================
    private static IntentFilter MainActivityIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.ACTION_TELEPHONE);
        intentFilter.addAction(MainActivity.ACTION_IMAGE_CAPTURE);
        intentFilter.addAction(MainActivity.ACTION_NETWORK_CAHNGE);
        return intentFilter;
    }

    //=================================================================
    // 接收廣播
    //=================================================================
    private final BroadcastReceiver mMainUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            Log.w(TAG, "MainActivity: BroadcastReceiver:"+action);
            assert action != null;
            Intent myIntent;

            switch(action){
                case ACTION_TELEPHONE:
                    myIntent =new Intent(Intent.ACTION_DIAL);
                    startActivity(myIntent);
                    manager.cancel(counter);  // 將通知關掉
                    break;
                case ACTION_IMAGE_CAPTURE:
                    myIntent =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivity(myIntent);
                    manager.cancel(counter);
                    break;
                case ACTION_NETWORK_CAHNGE:
                    ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo info = connectivityManager.getActiveNetworkInfo();
                    if(info != null && info.isAvailable()){
                        showToastIns(getApplicationContext(), "網路連上:"+info.getTypeName(), Toast.LENGTH_LONG);
                    }else {
                        showToastIns(getApplicationContext(), "沒有網路連線", Toast.LENGTH_LONG);
                    }
                    break;
                //case ACTION_MY_BROADCAST:
                //    showToastIns(getApplicationContext(), "收到我的廣播了", Toast.LENGTH_LONG);
                //    break;
            }
        }
    };

    //=================================================================
    // 檢查相機支援camera2 API的level
    //=================================================================
    private String[] camera_support_level = new String[20];
    private String[] camera_position = new String[10];

    public int allowCamera2Support(int cameraId) {
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        if (manager != null) {
            try {
                String cameraIdS = manager.getCameraIdList()[cameraId];
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraIdS);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null) {
                    switch (facing) {
                        case CameraCharacteristics.LENS_FACING_FRONT:   // 0
                            camera_position[cameraId] = "FRONT";
                            break;
                        case CameraCharacteristics.LENS_FACING_BACK:    // 1
                            camera_position[cameraId] = "BACK";
                            break;
                        case CameraCharacteristics.LENS_FACING_EXTERNAL:    // 2
                            camera_position[cameraId] = "EXTERNAL";
                            break;
                        default:
                            camera_position[cameraId] = "UNKNOW";
                            break;
                    }

                }

                int support = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                switch (support) {
                    case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
                        Log.d(TAG, "Camera " + cameraId + " has LEGACY Camera2 support");
                        break;
                    case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
                        Log.d(TAG, "Camera " + cameraId + " has LIMITED Camera2 support");
                        break;
                    case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
                        Log.d(TAG, "Camera " + cameraId + " has FULL Camera2 support");
                        break;
                    case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL:
                        Log.d(TAG, "Camera " + cameraId + " has EXTERNAL Camera2 support");
                        break;
                    case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_3:
                        Log.d(TAG, "Camera " + cameraId + " has LEVEL3 Camera2 support");
                        break;
                    default:
                        Log.d(TAG, "Camera " + cameraId + " has unknown Camera2 support?!");
                        break;
                }

                return support;// == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED || support == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL;
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        return 100;
    }

    //=================================================================
    // 相機支援的Level轉成字串
    //=================================================================
    private String checkCamera2Support() {
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
            int numberOfCameras = 0;
            CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);

            if (manager!=null) {
                try {
                    numberOfCameras = manager.getCameraIdList().length;
                } catch (CameraAccessException | AssertionError e) {
                    e.printStackTrace();
                }

                if (numberOfCameras == 0) {
                    Log.d(TAG, "0 cameras");
                    return "沒有找到相機";
                    //showToastIns(getApplicationContext(), "沒有找到相機", Toast.LENGTH_LONG);
                } else {
                    for (int i = 0; i < numberOfCameras; i++) {

                        switch (allowCamera2Support(i)) {
                            case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
                                camera_support_level[i] = "LEGACY";
                                break;
                            case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
                                camera_support_level[i] = "LIMIT";
                                break;
                            case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
                                camera_support_level[i] = "FULL";
                                break;
                            case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL:
                                camera_support_level[i] = "EXTERNAL";
                                break;
                            case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_3:
                                camera_support_level[i] = "LEVEL3";
                                break;
                            default:
                                camera_support_level[i] = "UNKNOW";
                                break;
                        }

                    }

                    String context = "";

                    if (numberOfCameras == 1) {
                        context = "相機0:" + camera_support_level[0] + "," + camera_position[0];
                    } else if (numberOfCameras == 2) {
                        context = "相機0: " + camera_support_level[0] + "," + camera_position[0] + "\n" +
                                "相機1: " + camera_support_level[1] + "," + camera_position[1];
                    } else if (numberOfCameras == 3) {
                        context = "相機0: " + camera_support_level[0] + "," + camera_position[0] + "\n" +
                                "相機1: " + camera_support_level[1] + "," + camera_position[1] + "\n" +
                                "相機2: " + camera_support_level[2] + "," + camera_position[2];
                    }

                    return "目前安裝了" + numberOfCameras + "台相機" + "\n" + context;
                    //showToastIns(getApplicationContext(), "目前安裝了"+numberOfCameras+"台相機"+"\n"+"\n"+
                    //                context,
                    //        Toast.LENGTH_LONG);
                }
            }
        }
        return null;
    }
}
