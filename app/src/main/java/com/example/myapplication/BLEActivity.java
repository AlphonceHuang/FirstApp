package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.example.myapplication.Util.isLocationEnable;
import static com.example.myapplication.Util.showToastIns;
import static com.example.myapplication.Util.toHex;

public class BLEActivity extends AppCompatActivity {

    private static final String TAG="Alan";
    //private boolean UUID_LIST_DEBUG=false;  // debug用，印出所有UUID


    private Button BLE_Start_Button;
    private Button BLE_Direct_Button;
    private Button BLE_Clean_Button;
    private TextView BLE_StatusText;
    private TextView BLE_DirectMACText;
    private boolean mScanning;

    private ListView listView;
    private ArrayAdapter<String> deviceName;

    private ArrayList<BluetoothDevice> mBluetoothDevices=new ArrayList<>();
    private BluetoothLeScanner mLEScanner;

    private Handler searchHandler;
    private Handler connectHandler;
    private static final long SCAN_PERIOD = 10000; //10 seconds
    private static final long CONNECT_PERIOD = 30000;   // 30s, connect to device max time
    private int REQUEST_CODE_LOCATION_SETTINGS=2;

    //public static BLEActivity instance=null;

    SharedPreferences mem_FilterSetting;
    SharedPreferences mem_SaveMacAddr;
    SharedPreferences mem_DirectConn;
    SharedPreferences mem_AutoScan;
    private static BluetoothDevice mBluetoothDevice;    // 目前連接上的device
    private ProgressDialog connectingDialog;

    //SparseArray<byte[]> mandufacturerData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);

        Log.w(TAG, "BLEActivity:onCreate");

        //instance=this;
        searchHandler = new Handler(); //該Handler用來搜尋Devices 10秒後，自動停止搜尋
        connectHandler = new Handler();

        // memory
        mem_FilterSetting = getSharedPreferences("BLE_ScanFilter", MODE_PRIVATE);
        mem_SaveMacAddr = getSharedPreferences("BLE_MAC_ADDR", MODE_PRIVATE);
        mem_DirectConn = getSharedPreferences("BLE_DirectConn", MODE_PRIVATE);
        mem_AutoScan = getSharedPreferences("AUTO_SCAN", MODE_PRIVATE);

        InitialViewer();    // 初始化 UI
        RequestPermission();    // 確認取得權限

        // bind service
        Intent gattServiceIntent = new Intent(BLEActivity.this, LeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    //=================================================================
    // Bind service
    //=================================================================
    public static LeService mBluetoothLEService;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLEService = ((LeService.LocalBinder) service).getService();
            if (!mBluetoothLEService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            InitialBLEScanner();    // 初始化Bluetooth Adapter
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLEService = null;
        }
    };

    private void InitialViewer(){
        // Button
        BLE_Start_Button = findViewById(R.id.BLE_StartBtn);
        BLE_Start_Button.setOnClickListener(BLE_TT);
        Button BLE_Stop_Button = findViewById(R.id.BLE_StopBtn);
        BLE_Stop_Button.setOnClickListener(BLE_TT);
        BLE_Direct_Button = findViewById(R.id.BLE_DirectBtn);
        BLE_Direct_Button.setOnClickListener(BLE_TT);
        BLE_Clean_Button = findViewById(R.id.BLE_CleanBtn);
        BLE_Clean_Button.setOnClickListener(BLE_TT);

        // Switch
        Switch BLE_FilterSwitch = findViewById(R.id.BLE_FilterSwitch);
        BLE_FilterSwitch.setOnCheckedChangeListener(BLE_SW);
        BLE_FilterSwitch.setChecked(FilterEnableGet());
        Switch BLE_DirectSwitch = findViewById(R.id.BLE_DirconSW);
        BLE_DirectSwitch.setOnCheckedChangeListener(BLE_SW);
        BLE_DirectSwitch.setChecked(DirectConnectGet());

        // Text
        BLE_StatusText = findViewById(R.id.BLE_status);
        BLE_StatusText.setText(getText(R.string.suggest_empty));    // 預設為空
        BLE_DirectMACText = findViewById(R.id.BLE_DirectMAC);
        BLE_DirectMACText.setText(SavedMACAddrGet());   // 顯示儲存的MAC

        // List
        listView = findViewById(R.id.BLE_List);
        deviceName = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1);
        listView.setOnItemClickListener(ListItem);
        listView.setOnItemLongClickListener(ListItem1);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.w(TAG, "BLEActivity: on resume");

        BLE_DirectMACText.setText(SavedMACAddrGet());
        registerReceiver(mGattUpdateReceiver, GattUpdateIntentFilter());

        // 沒有儲存MAC，將"一鍵連線"及"清除MAC"按鈕反灰
        if (SavedMACAddrGet()==null){
            BLE_Direct_Button.setEnabled(false);
            BLE_Clean_Button.setEnabled(false);
        }else{
            BLE_Direct_Button.setEnabled(true);
            BLE_Clean_Button.setEnabled(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w(TAG, "BLEActivity:onPause");

        StopScan();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.w(TAG, "BLEActivity:onStop");

        StopScan();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.w(TAG, "BLEActivity:onDestroy");

        StopScan();
        closeGatt();
        deviceName.clear();

        unbindService(mServiceConnection);
        mBluetoothLEService = null;
    }

    //=================================================================
    // 註冊Broadcast filter
    //=================================================================
    private static IntentFilter GattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(LeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(LeService.ACTION_GATT_CONNECTFAIL);
        return intentFilter;
    }

    //=================================================================
    //這個Override Function是因為在onResume中使用了ActivityForResult，當使用者按了取消或確定鍵時，結果會
    //返回到此onActivvityResult中，在判別requestCode判別是否==RESULT_CANCELED，如果是則finish()程式
    //=================================================================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.w(TAG, "onActivityResult: requestCode:"+requestCode);

        int REQUEST_ENABLE_BT = 1;
        if(requestCode == REQUEST_ENABLE_BT && resultCode== Activity.RESULT_CANCELED){
            Log.w(TAG, "Bluetooth can't enable.");
            finish();
        }
        else if (requestCode == REQUEST_CODE_LOCATION_SETTINGS){
            if (isLocationEnable(this)) {
                Log.w(TAG, "Location already enabled.");
            } else {
                Log.w(TAG, "Location can't enable.");
            }
        }
    }

    //=================================================================
    // Filter值 儲存/讀取
    //=================================================================
    private boolean FilterEnableGet(){
        return mem_FilterSetting.getBoolean("BLE_ScanFilter", true);
    }

    private void FilterEnableSet(boolean bEnable){

        SharedPreferences.Editor editor = mem_FilterSetting.edit(); //獲取編輯器
        editor.putBoolean("BLE_ScanFilter", bEnable);
        editor.apply();
        editor.commit();    //提交
    }

    //=================================================================
    // MAC address 儲存/讀取/清除
    //=================================================================
    public String SavedMACAddrGet(){
        String strMAC=mem_SaveMacAddr.getString("BLE_MAC_ADDR", "");

        if (strMAC != null) {
            if (strMAC.length()!=0)
                return strMAC;
            else
                return null;
        }else
            return null;
    }
    private void SavedMACAddrSet(String address){
        Log.w(TAG, "儲存MAC");
        SharedPreferences.Editor editor = mem_SaveMacAddr.edit(); //獲取編輯器
        editor.putString("BLE_MAC_ADDR", address);
        editor.apply();
        editor.commit();    //提交
    }
    public void cleanMacAddr(){
        SharedPreferences.Editor editor = mem_SaveMacAddr.edit();
        editor.clear();
        editor.apply();
        editor.commit();
        BLE_DirectMACText.setText(getText(R.string.suggest_empty));
        BLE_Direct_Button.setEnabled(false);    // 反灰"一鍵連線"

        Log.w(TAG,"after clean get:"+SavedMACAddrGet());
    }

    //=================================================================
    // Direct connect 儲存/讀取
    //=================================================================
    private boolean DirectConnectGet(){
        return mem_DirectConn.getBoolean("BLE_DirectConn", true);
    }

    private void DirectConnectSet(boolean bEnable){

        SharedPreferences.Editor editor = mem_DirectConn.edit(); //獲取編輯器
        editor.putBoolean("BLE_DirectConn", bEnable);
        editor.apply();
        editor.commit();    //提交
    }

    //===========================================================
    //  初始化Bluetooth Adapter
    //===========================================================
    private void InitialBLEScanner(){
        deviceName.clear(); // 先清除list

        if(LeService.mBluetoothAdapter==null){
            showToastIns(getBaseContext(),"不支援藍芽",Toast.LENGTH_SHORT);
            finish();   //如果==null，利用finish()取消程式
        }
        else{
            mLEScanner = LeService.mBluetoothAdapter.getBluetoothLeScanner();
            if (DirectConnectGet() && SavedMACAddrGet().length()!=0){
                Log.w(TAG, "direct connect.");
                BLE_StatusText.setText(R.string.direct);
                DirectConnect();
            }
            else {
                if (mem_AutoScan.getBoolean("AUTO_SCAN", true)) {
                    Log.w(TAG, "Auto scan.");
                    StartScan();
                }else{
                    Log.w(TAG, "Wait.");
                }
            }
        }
    }

    //===========================================================
    // Device 連接至 Gatt
    //===========================================================
    public void connectToDevice(BluetoothDevice device)
    {
        Log.w(TAG, "connect to device:"+device);
        Log.w(TAG, "gatt status:"+ mBluetoothLEService.connectionStatus());

        mBluetoothLEService.clearServiceMap();  // 清除BLE service map

        //if (mBluetoothLEService.connectionStatus() != LeService.STATE_DISCONNECT)
            closeGatt();

        mBluetoothLEService.connect(device.getAddress());
        ShowConnectingDialog(device);   //顯示對話框
    }

    //===========================================================
    // 關閉 GATT service
    //===========================================================
    public static void closeGatt(){
        //Log.w(TAG, "Close GATT");
        //mBluetoothLEService.refreshGattCache(); // move to mBluetoothLEService.close()
        mBluetoothLEService.disconnect();
        mBluetoothLEService.close();
    }

/*
    //=================================================================
    // 印出 DescriptorsonServicesDiscovered
    //=================================================================
    private void Log_BluetoothGatttDescriptors(BluetoothGattCharacteristic characteristic){
        List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();

        //Log.w(TAG, characteristic.getUuid().toString().toUpperCase());

        if (descriptors.size()!=0){
            for (int k=0; k<descriptors.size(); k++)
                Log.w(TAG, "    descriptor:"+descriptors.get(k).getUuid().toString().toUpperCase());
        }
        else{
            Log.w(TAG, "    No descriptor");
        }
    }
    */

    //=================================================================
    // 由手機端送出字串
    //=================================================================
    //public void sendMessage(String value) {
        //mBluetoothLEService.sendMessage(value);
    //}

    //=================================================================
    // 開始尋找 Bluetooth device
    //=================================================================
    private void StartScan(){
        // 先清除list裡的東西
        deviceName.clear();
        mBluetoothDevices.clear();

        scanLeDevice(true); // 開始搜尋
        mScanning=true;
        BLE_Start_Button.setEnabled(false); //按鈕反灰
    }

    //=================================================================
    // 停止尋找 Bluetooth device
    //=================================================================
    private void StopScan(){
        if (mScanning) {
            scanLeDevice(false);
            BLE_Start_Button.setEnabled(true);
        }
    }

    //=================================================================
    // 裝置經由 LeService callback 確認連接上了，跳轉頁面
    //=================================================================
    private void ConnectDeviceSuccess(){
        connectingDialog.dismiss();
        mScanning=false;    // 清除flag
        connectHandler.removeCallbacksAndMessages(null);    // 清除時間計算

        //將device Name與address存到DEVICE_NAME與ADDRESS，以供GattActivity使用
        Intent goGATTIntent=new Intent(BLEActivity.this,GattActivity.class);
        goGATTIntent.putExtra("DEVICE_NAME",mBluetoothDevice.getName());
        goGATTIntent.putExtra("DEVICE_ADDRESS",mBluetoothDevice.getAddress());
        //goGATTIntent.putExtra("DEVICE_MANUFACTURER", getManufacturerId());
        //Log.w(TAG,"type:"+mBluetoothDevice.getType());
        startActivity(goGATTIntent);


    }

    //=================================================================
    // List item click event (short press)
    //=================================================================
    private ListView.OnItemClickListener ListItem = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            StopScan();
            mBluetoothDevice=mBluetoothDevices.get(i);
            Log.w(TAG, "Press item Name:"+mBluetoothDevice.getName()+",MAC:"+mBluetoothDevice.getAddress());
            connectToDevice(mBluetoothDevice);  // 連接選擇的device
        }
    };

    //=================================================================
    // List item click event (long press)
    //=================================================================
    private ListView.OnItemLongClickListener ListItem1 = new ListView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            StopScan();
            mBluetoothDevice=mBluetoothDevices.get(i);

            Log.w(TAG, "Long press item="+i);

            //Log.w(TAG, "size="+mandufacturerData.size());
            /*
            for (int k = 0; k < mandufacturerData.size(); k++) {
                int key = mandufacturerData.indexOfKey(k);
                //mandufacturerData.k
                Log.w(TAG, "key:"+k+"---"+key);
            }*/
            return true;    // true: 放開時才不會觸發短按event
        }
    };

    //=================================================================
    // Switch Event
    //=================================================================
    private CompoundButton.OnCheckedChangeListener BLE_SW= new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            switch(compoundButton.getId()) {
                case R.id.BLE_FilterSwitch:
                    StopScan();    // filter狀態改變，停止search
                    FilterEnableSet(b);// 儲存
                    break;
                case R.id.BLE_DirconSW:
                    Log.w(TAG, "direct="+b);
                    DirectConnectSet(b);
                    break;
            }
        }
    };

    //=================================================================
    // Button event
    //=================================================================
    private View.OnClickListener BLE_TT=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.BLE_StartBtn:
                    Log.w(TAG, "Press START");
                    StartScan();
                    break;

                case R.id.BLE_StopBtn:
                    Log.w(TAG, "Press STOP");
                    if (mScanning && mBluetoothDevices.size()==0) {   // no device on list
                        showToastIns(getBaseContext(), getText(R.string.no_device), Toast.LENGTH_LONG);
                    }
                    StopScan();
                    break;

                case R.id.BLE_DirectBtn:
                    Log.w(TAG, "Press DIRECT");
                    StopScan();
                    DirectConnect();
                    break;

                case R.id.BLE_CleanBtn:
                    Log.w(TAG, "Press clean");
                    cleanMacAddr();
                    break;
            }
        }
    };


    //=================================================================
    // 掃描藍芽裝置
    //=================================================================
    public void scanLeDevice(boolean enable) {

        if (enable) {
            searchHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mScanning) {
                        StopScan();
                        Log.w(TAG, "Timeout, stop scan.");

                        AutoConnectDevice();    // 自動連線至first device
                    }
                }
            }, SCAN_PERIOD);    // 只搜描SCAN_PERIOD時間

            if (mLEScanner!=null) {
                BLE_StatusText.setText(getText(R.string.scanning));
                Log.w(TAG, "Scanning...");
                BLEDeviceScan(true, FilterEnableGet());
                mScanning=true;
            }
            else
                Log.w(TAG, "mLEScanner null");

        } else {
            BLEDeviceScan(false, false);
            mScanning=false;
            BLE_StatusText.setText(getString(R.string.stop));

            // 清除callback，避免下次按開始後postDelayed時間不正確
            searchHandler.removeCallbacksAndMessages(null);

            Log.w(TAG, "Stop scan.");
        }
    }

    //=====================================================================
    // Start/Stop BLE scanner
    //=====================================================================
    private void BLEDeviceScan(boolean enable, boolean scanFilterEnable) {
        Log.w(TAG, "BLEDeviceScan:"+enable);

        if (enable) {
            if (scanFilterEnable) {
                Log.w(TAG, "scan with filter");

                ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
                List<ScanFilter> filters_v2 = new ArrayList<>();
                int MANUFACTURER_ID = 13330;
                ScanFilter scanFilter = new ScanFilter.Builder()
                        //.setDeviceName("BM71_BLE")    // 用model name當filter
                        //.setServiceUuid(ParcelUuid.fromString(SERVICE_FILTER))  // 用service UUID當filter

                        // 使用manufacturer當filter
                        .setManufacturerData(MANUFACTURER_ID, new byte[] {})
                        .build();
                filters_v2.add(scanFilter);
                mLEScanner.startScan(filters_v2, settings, mScanCallback);
            }
            else {
                Log.w(TAG,"scan without filter");
                mLEScanner.startScan(mScanCallback);
            }
        }
        else {
            mLEScanner.stopScan(mScanCallback);
            mLEScanner.flushPendingScanResults(mScanCallback);  // clean up

        }
    }

    //=====================================================================
    // 1. 如果filter=on, 搜尋完直接連線在列表上的第一個裝置
    // 2. 如果filter=off, 由使用者選擇裝置連線
    //=====================================================================
    private void AutoConnectDevice(){

        //Log.w(TAG, "device number="+mBluetoothDevices.size());

        if (FilterEnableGet()) {
            if (mBluetoothDevices.size() != 0) {

                mBluetoothDevice = mBluetoothDevices.get(0);    // 直接連線至第一個找到的裝置
                Log.w(TAG, "Auto connect Name:" + mBluetoothDevice.getName() + ",MAC:" + mBluetoothDevice.getAddress());
                connectToDevice(mBluetoothDevice);  // 連接選擇的device
            } else {
                Log.w(TAG, "No device found.");
                showToastIns(getBaseContext(), getText(R.string.no_device), Toast.LENGTH_LONG);
            }
        }else{
            if (mBluetoothDevices.size() != 0) {
                Log.w(TAG, "Filter disabled will not auto connect.");
                showToastIns(getBaseContext(), getText(R.string.choose_to_connect), Toast.LENGTH_LONG);
            }else {
                Log.w(TAG, "No device found.");
                showToastIns(getBaseContext(), getText(R.string.no_device), Toast.LENGTH_LONG);
            }
        }
    }

    //=====================================================================
    // 連線儲存的MAC address
    //=====================================================================
    private void DirectConnect(){
        //String btMac="34:81:F4:17:56:7E";
        String btMac=SavedMACAddrGet();

        Log.w(TAG, "DirectConnect:"+btMac);

        if (btMac==null)   //　MAC如果是空的
        {
            Log.w(TAG, "no MAC to direct connect.");
            showToastIns(this, getText(R.string.no_device), Toast.LENGTH_LONG);
        }
        else {
            mScanning=true;
            mBluetoothDevice = LeService.mBluetoothAdapter.getRemoteDevice(btMac);
            connectToDevice(mBluetoothDevice);
        }
    }

    //=====================================================================
    // Scan Bluetooth Devices callback function
    //=====================================================================
    private ScanCallback mScanCallback = new ScanCallback()
    {
        //----------------------------------
        // 掃描device
        //----------------------------------
        @Override
        public void onScanResult(int callbackType, final ScanResult result)
        {
            super.onScanResult(callbackType, result);

            //BluetoothDevice device = result.getDevice();
            //Log.w(TAG, "scan name:" + device.getName()+"  MAC:"+device.getAddress());


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BluetoothDevice device = result.getDevice();

                    if (!mBluetoothDevices.contains(device)) {  // 檢查Array重覆的device
                        mBluetoothDevices.add(device);  // 未重覆的加入Array

                        // 將掃描到的 device 增加 list 裡
                        deviceName.add(device.getName()+"   "+device.getAddress()+"\n"+"RSSI:"+result.getRssi()+
                                "    Manufacturer ID:"+getManufacturer(result.getScanRecord()));
                        listView.setAdapter(deviceName);

                        Log.w(TAG, "Name:" + device.getName()+", MAC:"+device.getAddress());
                        //Log.w(TAG, "Result:"+result.toString());
                        //Log.w(TAG, "Manufacturer ID:"+getManufacturerId(result.getScanRecord()));
                    }
                }
            });
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results)
        {
            super.onBatchScanResults(results);
            /* Process a batch scan results */
            for (ScanResult sr : results)
            {
                Log.w(TAG, "Scan Item: "+ sr.toString());
            }
        }

        //----------------------------------
        // 掃描失敗
        //----------------------------------
        @Override
        public void onScanFailed(int errorCode){
            super.onScanFailed(errorCode);
            Log.w(TAG, "Error Code:"+errorCode);
            mScanning=false;
            BLE_StatusText.setText(getString(R.string.scan_fail));
        }
    };

    //=====================================================================
    // Get all kinds of permission
    //=====================================================================
    private void RequestPermission(){

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Get ACCESS_FINE_LOCATION/ACCESS_COARSE_LOCATION permission success.");
        } else {
            Log.w(TAG, "Get ACCESS_FINE_LOCATION/ACCESS_COARSE_LOCATION permission fail.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED)
        {
            Log.w(TAG, "Get BLUETOOTH permission success.");
        }else{
            Log.w(TAG, "Get BLUETOOTH permission fail.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, 1);
        }

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED)
        {
            Log.w(TAG, "Get BLUETOOTH_ADMIN permission success.");
        }else{
            Log.w(TAG, "Get BLUETOOTH_ADMIN permission fail.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, 1);
        }

        // 檢查手機硬體是否為BLE裝置
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showToastIns(this, getText(R.string.not_support), Toast.LENGTH_SHORT);
            Log.w(TAG, "Not support FEATURE_BLUETOOTH_LE");
            finish();
        }
        else
            Log.w(TAG, "Support FEATURE_BLUETOOTH_LE");

        if (!isLocationEnable(this))
        {
            Log.w(TAG, "LOCATION_SETTINGS not opened.");
            Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            this.startActivityForResult(locationIntent, REQUEST_CODE_LOCATION_SETTINGS);
            Log.w(TAG, "Get LOCATION_SETTINGS permission.");
        }
    }

    //=====================================================================
    // 顯示連線中的Dialog
    //=====================================================================
    public void ShowConnectingDialog(BluetoothDevice device){

        // 顯示對話框
        connectingDialog = new ProgressDialog(this);
        connectingDialog.setMessage(device.getName()+"\n"+
                device.getAddress()+"\n"+
                getText(R.string.connecting));
        connectingDialog.setCancelable(false);

        //取消按鈕
        connectingDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        connectingDialog.dismiss();
                        StopScan();
                        closeGatt();
                        //ShowNoFoundToast(); //----可正常Toast
                    }
                });
        connectingDialog.show();    // 開始顯示
        ConnectingDeviceTime();    // 設定最長嘗試連線時間

/*      // 因為我們要在搜尋找不到裝置時顯示Toast，用這個方式會造成app當掉，改用另一個線程來設定時間
        // 就不使用包在Dialog裡的線程

        // 設定最長搜尋時間
        new Thread(){
            public void run(){
                try{
                    sleep(5000);    // CONNECT_PERIOD
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                finally{
                    //Log.w(TAG,"Connect to device timeout."+mScanning);
                    if (mScanning) {
                        mScanning=false;
                        connectingDialog.dismiss();
                        Log.w(TAG, "Can't connect to device.");
                        //ShowNoFoundToast();   //會造成app當掉
                    }
                }
            }
        }.start();*/
    }

    //=================================================================
    // 嘗試連線device的時間
    //=================================================================
    private void ConnectingDeviceTime() {

        connectHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mScanning) {
                    StopScan();
                    closeGatt();
                    Log.w(TAG, "Timeout, stop connect.");

                    if (connectingDialog.isShowing())
                        connectingDialog.dismiss();

                    showToastIns(getBaseContext(),getString(R.string.no_device),Toast.LENGTH_SHORT);
                    connectHandler.removeCallbacksAndMessages(null);
                }
            }
        }, CONNECT_PERIOD);    // 只搜描SCAN_PERIOD時間
    }

    //=================================================================
    // 接收廣播
    //=================================================================
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            Log.w(TAG, "BLEActivity: BroadcastReceiver:"+action);

            assert action != null;
            switch(action){
                case LeService.ACTION_GATT_CONNECTED:
                    SavedMACAddrSet(mBluetoothDevice.getAddress()); // 儲存
                    ConnectDeviceSuccess();   // 跳轉頁面
                    break;

                case LeService.ACTION_GATT_DISCONNECTED:
                    closeGatt();
                    connectingDialog.dismiss();
                    break;

                case LeService.ACTION_GATT_CONNECTFAIL:
                    Log.w(TAG,"連線失敗");
                    connectingDialog.dismiss();
                    connectHandler.removeCallbacksAndMessages(null);
                    break;
            }
        }
    };
/*
    //=================================================================
    // 解析 Manufacturer id (回傳int)
    //=================================================================
    private int getManufacturerId(ScanRecord scanRecord){

        byte[] a= new byte[0];
        int index=0;

        if (scanRecord != null) {
            a = scanRecord.getBytes();

            for(int i=0; i<a.length; i++) {
                if (a[i]==-1) {
                    index=i;
                    break;
                }
            }
        }
        return ((a[index+2]<<8)+a[index+1]);
    }
 */

    //=================================================================
    // 解析 Manufacturer id (回傳16進制)
    //=================================================================
    private StringBuffer getManufacturer(ScanRecord scanRecord){
        StringBuffer sb = new StringBuffer();
        sb.append("0x");
        byte[] a= new byte[0];
        int index=0;

        if (scanRecord != null) {
            a = scanRecord.getBytes();

            for(int i=0; i<a.length; i++) {
                if (a[i]==-1) {
                    index=i;
                    break;
                }
            }
        }
        sb.append(toHex(a[index+1]));
        sb.append(toHex(a[index+2]));
        return sb;
    }
}