package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static com.example.myapplication.Util.showToastIns;

public class GattActivity extends AppCompatActivity {

    private static final String TAG="Alan";

    private TextView m_DeviceName;
    private TextView m_DeviceAddr;
    private TextView m_DeviceStatus;
    private EditText m_GattText_Input;
    private TextView mMsg;
    private TextView m_DeviceInfo;
    private TextView m_ManufacturerInfo;
    private TextView m_ModelNamtInfo;

    //public static GattActivity Gatt_instance=null;

    private ArrayList<CharSequence> mLogBuf;

    private ProgressDialog myDialog;
    private static String dev_name = null;

    private Handler serviceHandler;
    private boolean mGetService=false;  // 判斷是否正在取得service
    private static final long SCAN_PERIOD = 20000; //20 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gatt);

        Log.w(TAG, "GattActivity:onCreate");
        serviceHandler = new Handler();

        //Gatt_instance = this;
        InitialView();
        GetInputInformation();
    }

    @Override
    protected void onResume()
    {
        Log.w(TAG, "GattActivity: on resume");

        super.onResume();
        registerReceiver(mGattUpdateReceiver, GattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        Log.w(TAG, "GattActivity:onPause");

        super.onPause();
        BLEActivity.closeGatt();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy(){
        Log.w(TAG, "GattActivity:onDestroy");

        super.onDestroy();
        serviceHandler.removeCallbacksAndMessages(null);
    }

    private void InitialView(){
        m_DeviceName = findViewById(R.id.d_name);
        m_DeviceAddr = findViewById(R.id.d_addr);
        m_DeviceStatus = findViewById(R.id.Gatt_Status);
        Button m_GattBtn_Disconnect = findViewById(R.id.Gatt_Disconnect);
        Button m_GattBtn_Send = findViewById(R.id.Gatt_Send);
        m_GattText_Input = findViewById(R.id.Gatt_InputText);
        mMsg = findViewById(R.id.Gatt_SendText);
        Button m_GattBtn_Clear = findViewById(R.id.Gatt_Clear);
        m_DeviceInfo = findViewById(R.id.Gatt_infoText);
        m_ManufacturerInfo = findViewById(R.id.Gatt_ManufacturerText);
        m_ModelNamtInfo = findViewById(R.id.Gatt_ModelNameText);

        m_GattBtn_Disconnect.setOnClickListener(GATT_Event);
        m_GattBtn_Send.setOnClickListener(GATT_Event);
        m_GattBtn_Clear.setOnClickListener(GATT_Event);

        UpdateStatus(GattState.GATT_CONNECTING);    // 顯示"連線中"

        // 先清除console
        mLogBuf = new ArrayList<>();
        mLogBuf.clear();

        // console的捲動方式
        mMsg.setMovementMethod(ScrollingMovementMethod.getInstance());
        registerForContextMenu(mMsg);

        // 隱藏小鍵盤
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    //---------------------------------------------------------------
    // 更新狀態文字
    //---------------------------------------------------------------
    public void UpdateStatus(GattState status){
        Log.w(TAG, "UpdateStatus:"+status);

        if (status==GattState.GATT_DISCONNECT){
            m_DeviceStatus.setText(getString(R.string.disconnect));
        }
        else if (status == GattState.GATT_CONNECTING){
            m_DeviceStatus.setText(getString(R.string.connecting));
        }
        else if (status==GattState.GATT_CONNECTED) {    // 連線上
            if (!BLEActivity.mBluetoothLEService.GetModelName()){
                m_DeviceInfo.setText(getString(R.string.without_premission));
            }
            mGetService=false;
            serviceHandler.removeCallbacksAndMessages(null);//避免時間繼續計算
            myDialog.dismiss();
            m_DeviceStatus.setText(getString(R.string.connect));
        }
        else if (status==GattState.GATT_CONNECT_FAIL){
            m_DeviceStatus.setText(getString(R.string.connect_fail));
        }
        else if (status==GattState.GATT_GET_MODEL_NAME) {   // 得到model name
            if (!BLEActivity.mBluetoothLEService.GetManufacturer()) {
                m_DeviceInfo.setText(getString(R.string.without_premission));
            }else{  // 更新字串
                String InfoString1 = getString(R.string.model_Name)+LeService.m_ModelName;
                m_ModelNamtInfo.setText(InfoString1);
            }
        }
        else if (status==GattState.GATT_GET_MANUFACTURER){    // 得到manufacturer後，更新字串
            String InfoString = getString(R.string.manufacturer)+LeService.m_Manufacturer;
            m_ManufacturerInfo.setText(InfoString);
        }
    }

    //---------------------------------------------------------------
    // 取得連線的device name及MAC address
    //---------------------------------------------------------------
    private void GetInputInformation(){
        Bundle bundle = this.getIntent().getExtras();
        String dev_addr;
        if (bundle != null) {
            dev_name = bundle.getString("DEVICE_NAME");
            dev_addr = bundle.getString("DEVICE_ADDRESS");
        }else{
            dev_name=null;
            dev_addr =null;
        }
        String strDeviceName=getString(R.string.paired_device_name)+dev_name;
        String strMACAddress=getString(R.string.paired_device_address)+ dev_addr;

        Log.w(TAG, "Connecting... "+dev_name+"="+ dev_addr);

        if (dev_name==null){
            dev_name="";
        }
        else {
            m_DeviceName.setText(strDeviceName);
        }
        m_DeviceAddr.setText(strMACAddress);
        mGetService=true;
        ShowSearchServiceDialog();
        GettingServiceTime();   // 設定timeout時間
    }

    private void ShowSearchServiceDialog(){
        // 顯示對話框
        myDialog = new ProgressDialog(this);
        myDialog.setMessage(dev_name+"\n"+
                getString(R.string.getting_service));
        myDialog.setCancelable(false);

        //取消按鈕
        myDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myDialog.dismiss();
                        finish();   // 回至上一頁
                    }
                });
        //myDialog.getWindow().setGravity(Gravity.BOTTOM); // 置於下方
        myDialog.show();    // 開始顯示
    }

    //=================================================================
    // 嘗試連線device的時間
    //=================================================================
    private void GettingServiceTime() {

        serviceHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mGetService) {
                    mGetService=false;

                    Log.w(TAG, "Timeout, stop query services.");

                    if (myDialog.isShowing())
                        myDialog.dismiss();

                    showToastIns(getBaseContext(),getString(R.string.no_device), Toast.LENGTH_SHORT);
                    serviceHandler.removeCallbacksAndMessages(null);
                    UpdateStatus(GattState.GATT_CONNECT_FAIL);
                }
            }
        }, SCAN_PERIOD);
    }

    //------------------------------------------------------------
    // 按鈕 Event
    //------------------------------------------------------------
    private View.OnClickListener GATT_Event = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.Gatt_Disconnect:
                    Log.w(TAG, "Press "+getString(R.string.disconnect));
                    GattActivity.this.finish(); // close GATT 放在onPause()
                    break;
                case R.id.Gatt_Send:
                    Log.w(TAG, "Press "+getString(R.string.send));
                    CharSequence cs=m_GattText_Input.getText();
                    BLEActivity.mBluetoothLEService.sendMessage(cs.toString());
                    appendMsg("send:",cs);
                    m_GattText_Input.setText(getString(R.string.suggest_empty));    // 清空輸入欄位
                    break;
                case R.id.Gatt_Clear:
                    Log.w(TAG, "Press "+getString(R.string.clear_btn));
                    m_GattText_Input.setText(R.string.suggest_empty);
                    ClearMessage();
                    break;
            }
        }
    };

    /*
    //=======================================================================
    // 監控EditText是否有輸入字，決定"送出"按鈕是否反灰
    //=======================================================================
    private TextWatcher InputTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (TextUtils.isEmpty(m_GattText_Input.getText())){
                Log.w(TAG, "empty");
                m_GattBtn_Send.setEnabled(false);
            }else{
                m_GattBtn_Send.setEnabled(true);
                Log.w(TAG, "not empty");
            }
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };
*/
    //------------------------------------------------------------
    // 清空console文字，包含buffer
    //------------------------------------------------------------
    private void ClearMessage() {
        StringBuffer sb = new StringBuffer();
        sb.append("");
        mMsg.setText(sb);
        mLogBuf.clear();
    }

    //------------------------------------------------------------
    // 當 CHR_ISSC_TRANS_TX 收到資料時，更新console文字
    //------------------------------------------------------------
    public void appendMsg(CharSequence prefix, CharSequence msg) {
        StringBuffer sb = new StringBuffer();
        sb.append(prefix);
        sb.append(msg);
        sb.append("\n");
        mLogBuf.add(sb);

        // we don't want to display too many lines
        if (mLogBuf.size() > 13) {
            mLogBuf.remove(0);
        }

        StringBuffer text = new StringBuffer();
        for (int i = 0; i < mLogBuf.size(); i++) {
            text.append(mLogBuf.get(i));
        }
        mMsg.setText(text);

        final int amount = mMsg.getLayout().getLineTop(
                mMsg.getLineCount())
                - mMsg.getHeight();
        if (amount > 0) {
            mMsg.scrollTo(0, amount);
        }
    }

    //------------------------------------------------------------
    // 取得 Intent Filter
    //------------------------------------------------------------
    private static IntentFilter GattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(LeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(LeService.ACTION_GATT_MODEL_NAME);
        intentFilter.addAction(LeService.ACTION_GATT_MANUFACTURER);
        intentFilter.addAction(LeService.ACTION_GATT_NOTIFY_FINISH);
        intentFilter.addAction(LeService.ACTION_GATT_RX_STRING);
        return intentFilter;
    }

    //------------------------------------------------------------
    // Broadcast 接收器
    //------------------------------------------------------------
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            Log.w(TAG, "GattActivity: BroadcastReceiver:"+action);

            if (action != null) {
                switch(action){
                    case LeService.ACTION_GATT_CONNECTED:
                    case LeService.ACTION_GATT_NOTIFY_FINISH:
                        UpdateStatus(GattState.GATT_CONNECTED);
                        break;
                    case LeService.ACTION_GATT_DISCONNECTED:
                        UpdateStatus(GattState.GATT_DISCONNECT);
                        showToastIns(getBaseContext(),getString(R.string.disconnect), Toast.LENGTH_LONG);
                        if (myDialog.isShowing()) {
                            myDialog.dismiss();
                            serviceHandler.removeCallbacksAndMessages(null);
                        }
                        break;
                    case LeService.ACTION_GATT_CONNECTFAIL:
                        Log.w(TAG, "connect fail.");
                        finish();   // 連接失敗，回至上一頁
                        break;
                    case LeService.ACTION_GATT_MODEL_NAME:
                        UpdateStatus(GattState.GATT_GET_MODEL_NAME);
                        break;
                    case LeService.ACTION_GATT_MANUFACTURER:
                        UpdateStatus(GattState.GATT_GET_MANUFACTURER);
                        break;
                    case LeService.ACTION_GATT_RX_STRING:
                        appendMsg("Receive:", intent.getStringExtra("RX_STRING"));
                        break;
                    default:
                        break;
                }
            }else{
                Log.w(TAG, "action is null.");
            }
        }
    };
}
