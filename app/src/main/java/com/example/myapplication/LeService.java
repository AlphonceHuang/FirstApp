package com.example.myapplication;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.example.myapplication.Util.showToastIns;

public class LeService extends Service {
    private static final String TAG="Alan";
    private BluetoothGatt mBluetoothGatt;
    IBinder mBinder = new LocalBinder();
    public static BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    public static final int STATE_DISCONNECT = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    private static int mConnectionState = STATE_DISCONNECT;
    private String bluetoothAddress;
    private HashMap<String, Map<String, BluetoothGattCharacteristic>> servicesMap = new HashMap<>();
    HashMap<String, BluetoothGattCharacteristic> charMap = new HashMap<>();
    private boolean UUID_LIST_DEBUG=false;

    public static String m_ModelName;
    public static String m_Manufacturer;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.myapplication.LeService.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.myapplication.LeService.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_CONNECTFAIL =
            "com.example.myapplication.LeService.ACTION_GATT_CONNECTFAIL";
    public final static String ACTION_GATT_MODEL_NAME =
            "com.example.myapplication.LeService.ACTION_GATT_MODEL_NAME";
    public final static String ACTION_GATT_MANUFACTURER =
            "com.example.myapplication.LeService.ACTION_GATT_MANUFACTURER";
    public final static String ACTION_GATT_NOTIFY_FINISH =
            "com.example.myapplication.LeService.ACTION_GATT_NOTIFY_FINISH";
    public final static String ACTION_GATT_RX_STRING =
            "com.example.myapplication.LeService.ACTION_GATT_RX_STRING";

    public LeService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        disconnect();
        close();
        return super.onUnbind(intent);
    }

    public class LocalBinder extends Binder {
        LeService getService() {
            return LeService.this;
        }
    }

    //===========================================================
    // 初始化Bluetooth
    //===========================================================
    public boolean initialize() {
        mBluetoothManager = (BluetoothManager) getBaseContext().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter= mBluetoothManager.getAdapter();
        Log.w(TAG, "service initial");

        if (mBluetoothAdapter==null){
            showToastIns(getBaseContext(),"不支援藍芽", Toast.LENGTH_SHORT,0,0,0);
            return false;
        }
        return true;
    }

    public void clearServiceMap(){
        servicesMap.clear();
    }

    //===========================================================
    //  search services on device and save in map
    //===========================================================
    public void LookingForDeviceService(){

        if (mBluetoothGatt==null)
            return;

        List<BluetoothGattService> services = mBluetoothGatt.getServices();

        if (UUID_LIST_DEBUG)
            Log.w(TAG, "Service size="+services.size());

        for (int i = 0; i < services.size(); i++) {

            BluetoothGattService bluetoothGattService = services.get(i);
            String serviceUuid = bluetoothGattService.getUuid().toString().toUpperCase();
            List<BluetoothGattCharacteristic> characteristics = bluetoothGattService.getCharacteristics();

            if (UUID_LIST_DEBUG) {
                Log.w(TAG, "Service " + i + " UUID: " + serviceUuid.toUpperCase());    // 印出Service UUID
                Log.w(TAG, "characteristics size:"+characteristics.size());
            }

            for (int j = 0; j < characteristics.size(); j++) {
                charMap.put(characteristics.get(j).getUuid().toString().toUpperCase(), characteristics.get(j));

                if (UUID_LIST_DEBUG) {
                    Log.w(TAG, "  characteristic " + j + " UUID: " + characteristics.get(j).getUuid().toString().toUpperCase()); // 印出characteristic UUID
/*
                    List<BluetoothGattDescriptor> descriptors = characteristics.get(j).getDescriptors();

                    if (UUID_LIST_DEBUG)
                        Log.w(TAG, "descriptors size:"+descriptors.size());

                    for (int k=0; k<descriptors.size(); k++){
                        Log.w(TAG, "    descriptors "+k+" UUID: "+descriptors.get(j).getUuid().toString().toUpperCase());
                    }
 */
                }
            }
            servicesMap.put(serviceUuid, charMap);
        }
        LookingForCharacteristic();

/*  // 取出所有UUID
        for (Iterator it = charMap.keySet().iterator(); it.hasNext();)
        {
            Object key = it.next();
            Log.w(TAG, key+"="+ charMap.get(key));
        }
*/
        //取出Map裡Service的UUID
/*
        for (Iterator it = servicesMap.keySet().iterator(); it.hasNext();)
        {
            Object key = it.next();
            Log.w(TAG, key+":"+servicesMap.get(key));
        }
*/
    }

    //=================================================================
    // 搜尋 map中的serviceUUID 裡是否有 characterUUID
    //=================================================================
    public BluetoothGattCharacteristic getBluetoothGattCharacteristic(String serviceUUID, String characterUUID){
        if (null == mBluetoothGatt) {
            Log.e(TAG, "mGatt is null");
            return null;
        }

        // 在map裡面找 service是否存在
        Map<String, BluetoothGattCharacteristic> bluetoothGattCharacteristicMap = servicesMap.get(serviceUUID);
        if (null == bluetoothGattCharacteristicMap) {
            Log.w(TAG, "Not found the serviceUUID!");
            return null;
            //return false;
        }
        else {
            if (UUID_LIST_DEBUG) {
                Log.w(TAG, "Service Found:" + serviceUUID);
            }
        }

        // 在map裡找characteristic是否存在
        Set<Map.Entry<String, BluetoothGattCharacteristic>> entries = bluetoothGattCharacteristicMap.entrySet();
        BluetoothGattCharacteristic gattCharacteristic = null;
        for (Map.Entry<String, BluetoothGattCharacteristic> entry : entries) {
            if (characterUUID.equals(entry.getKey())) {
                gattCharacteristic = entry.getValue();
                if (UUID_LIST_DEBUG) {
                    Log.w(TAG, "  Characteristic UUID found:" + gattCharacteristic.getUuid().toString().toUpperCase());
                }
                break;
            }
        }
        return gattCharacteristic;
    }

    //=================================================================
    // 搜尋特定service裡的characteristic，如果有就設定通知
    // 如果找不到，就斷開GATT
    //=================================================================
    public void LookingForCharacteristic(){

        //Log.w(TAG, "LookingForCharacteristic");

        BluetoothGattCharacteristic NotificationCharacteristic =
                getBluetoothGattCharacteristic(sUUID.SERVICE_ISSC_PROPRIETARY, sUUID.CHR_ISSC_TRANS_TX);

        if (NotificationCharacteristic!=null) {
            //Log_BluetoothGatttDescriptors(NotificationCharacteristic);

            // 讓手機可接收字串，下面兩個都要enable, 如果只是傳送，下面可都不用設定
            enableCharacteristicNotification(NotificationCharacteristic, true);
            enableDescriptorNotification(NotificationCharacteristic, true);
        }else{
            Log.w(TAG, "can't find CHR_ISSC_TRANS_TX. Disconnect GATT");

            String intentDisconnect = ACTION_GATT_CONNECTFAIL;
            broadcastUpdate(intentDisconnect);

            disconnect();
            close();
        }

        /*
        NotificationCharacteristic=
            mBluetoothLEService.getBluetoothGattCharacteristic(SERVICE_ISSC_PROPRIETARY,CHR_ISSC_TRANS_RX);
            //getBluetoothGattCharacteristic(SERVICE_ISSC_PROPRIETARY,CHR_ISSC_TRANS_RX);
        if (NotificationCharacteristic!=null) {
            Log_BluetoothGatttDescriptors(NotificationCharacteristic);
            //enableCharacteristicNotification(gatt, NotificationCharacteristic, true);
            mBluetoothLEService.enableCharacteristicNotification(NotificationCharacteristic, true);
        }
        */
        /*  // 只需Tx,Rx，可不使用AirPatch
        NotificationCharacteristic=
            mBluetoothLEService.getBluetoothGattCharacteristic(sUUID.SERVICE_ISSC_PROPRIETARY,sUUID.CHR_AIR_PATCH);
            //getBluetoothGattCharacteristic(sUUID.SERVICE_ISSC_PROPRIETARY,sUUID.CHR_AIR_PATCH);
        if (NotificationCharacteristic!=null) {
            //Log_BluetoothGatttDescriptors(NotificationCharacteristic);
            //enableCharacteristicNotification(gatt, NotificationCharacteristic, true);
            enableAirPatch();
        }else{
            Log.w(TAG, "can't find CHR_AIR_PATCH.");
        }
        */
    }

    //=================================================================
    // Tx Description Notification 啟用/關閉
    //=================================================================
    public void enableDescriptorNotification(BluetoothGattCharacteristic characteristic, boolean enable) {
        boolean result;
        BluetoothGattDescriptor mWriteDescriptor =
                characteristic.getDescriptor(UUID.fromString(sUUID.DES_TRANS_TX));

        if(mWriteDescriptor == null) {
            Log.w(TAG, "mWriteDescriptor is null");
            return;
        }

        Log.w(TAG, "Descriptor permission:"+mWriteDescriptor.getPermissions());

        if (enable)
            result = mWriteDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        else
            result = mWriteDescriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);

        if (result) {
            if (!mBluetoothGatt.writeDescriptor(mWriteDescriptor)){
                Log.w(TAG, "write descriptor fail.");
            }
        }
        else
            Log.w(TAG, "can't set Descriptor value.");
    }

    //=================================================================
    // Notification 啟用/關閉
    //=================================================================
    public void enableCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                                 boolean enable){
        // 有notification的權限
        if (0!=(characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
            if (!mBluetoothGatt.setCharacteristicNotification(characteristic, enable)) {
                Log.w(TAG, "Notification [" + characteristic.getUuid() + "] fail. (" + enable + ")");
            }
        }
        else{
            Log.w(TAG, "UUID:"+characteristic.getUuid().toString().toUpperCase()+"沒有NOTIFY權限");
        }
    }

    //=================================================================
    // 由手機端送出字串
    //=================================================================
    public void sendMessage(String value) { // 1~20個字串
        //if (value.length()>0 && value.length()<21) {

            if (mBluetoothGatt == null) {
                Log.w(TAG, "sendMessage:" + value);
                return;
            }

            // 由UUID取得Service
            BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString(sUUID.SERVICE_ISSC_PROPRIETARY));
            if (mCustomService == null) {
                Log.w(TAG, "no SERVICE_ISSC_PROPRIETARY service");
                return;
            }

            // 由UUID取得Characteristic
            BluetoothGattCharacteristic mWriteCharacteristic =
                    mCustomService.getCharacteristic(UUID.fromString(sUUID.CHR_ISSC_TRANS_TX));
            if (mWriteCharacteristic == null) {
                Log.w(TAG, "no CHR_ISSC_TRANS_TX characteristic");
                return;
            }

            // TODO: need check this
            if ((mWriteCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0 ||
                    (mWriteCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                // send value
                mWriteCharacteristic.setValue(value);
                if (!mBluetoothGatt.writeCharacteristic(mWriteCharacteristic)) {
                    Log.w(TAG, "Failed to write characteristic");
                }
            }
        //}
    }

    //=====================================================================
    // 取得 characteristic 裡面的 Manufacturer 字串
    //=====================================================================
    public boolean GetManufacturer(){
        BluetoothGattCharacteristic characteristic=
                getBluetoothGattCharacteristic(sUUID.SERVICE_DEVICE_INFO, sUUID.CHR_MANUFACTURER);

        final int properties = characteristic != null ? characteristic.getProperties() : 0;
        if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) == 0){
            Log.w(TAG, "Without READ property.");
            return false;
        }
        mBluetoothGatt.readCharacteristic(characteristic);   // 回傳的資料會經由BluetoothGattCallback回傳
        return true;
    }

    //=====================================================================
    // 取得 characteristic 裡面的 model name 字串
    //=====================================================================
    public boolean GetModelName(){
        BluetoothGattCharacteristic characteristic=
                getBluetoothGattCharacteristic(sUUID.SERVICE_DEVICE_INFO, sUUID.CHR_MODELNAME);

        final int properties = characteristic != null ? characteristic.getProperties() : 0;
        if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) == 0){
            Log.w(TAG, "Without READ property.");
            return false;
        }
        mBluetoothGatt.readCharacteristic(characteristic);   // 回傳的資料會經由BluetoothGattCallback回傳
        return true;
    }

    //===========================================================
    // 關閉 GATT service
    //===========================================================
    public void close() {
        mConnectionState = STATE_DISCONNECT;

        if (mBluetoothGatt == null) {
            return;
        }
        Log.w(TAG, "GATT: close");
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        refreshGattCache();
    }

    //===========================================================
    // 連線裝置
    //===========================================================
    public boolean connect(String address) {
        //Try to use existing connection
        if (mBluetoothAdapter != null && address.equals(bluetoothAddress) && mBluetoothGatt != null) {
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);
        if (bluetoothDevice == null) {
            Log.w(TAG, "Device not found");
            return false;
        }

        mBluetoothGatt = bluetoothDevice.connectGatt(this, false, bluetoothGattCallback);
        bluetoothAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    //===========================================================
    // 中斷連線
    //===========================================================
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.d(TAG, "Bluetooth adapter not initialize");
            return;
        }
        mBluetoothGatt.disconnect();
        Log.w(TAG,"GATT: disconnect.");
        mConnectionState = STATE_DISCONNECT;
    }

    public int connectionStatus(){
        return mConnectionState;
    }

    //===========================================================
    // 清除藍芽緩存
    //===========================================================
    public void refreshGattCache() {
        boolean result = false;
        Log.w(TAG, "GATT: refresh");
        try {
            if (mBluetoothGatt != null) {
                Method refresh = BluetoothGatt.class.getMethod("refresh");
                if (refresh != null) {
                    refresh.setAccessible(true);
                    result = (boolean) refresh.invoke(mBluetoothGatt, new Object[0]);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "An exception occured while refreshing device");
        }
        //return result;
    }


    //=================================================================
    // GATT callback
    //=================================================================
    public final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback()
    {
        //-------------------------------------------------------
        // 連線狀態改變
        //-------------------------------------------------------
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            super.onConnectionStateChange(gatt, status, newState);
            Log.w(TAG, "status change:"+status+"----->"+"newState:"+newState);
            String intentAction;

            if (status==133){
                Log.w(TAG,"連接設備發生133，需重新連接設備");
                showToastIns(getBaseContext(),getString(R.string.connect_fail)+":"+133, Toast.LENGTH_SHORT,0,0,0);
                //mConnectionState = STATE_DISCONNECT;

                // 通知斷線了
                //intentAction = ACTION_GATT_CONNECTFAIL;
                //broadcastUpdate(intentAction);
                disconnect();
                //close();
                //return;
            }

            switch (newState)
            {
                case BluetoothProfile.STATE_CONNECTED:  // 2
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.w(TAG, "mainGattCallback:" + "CONNECTED");
                        gatt.discoverServices();

                        mConnectionState = STATE_CONNECTED;

                        intentAction = ACTION_GATT_CONNECTED;
                        broadcastUpdate(intentAction);
                    }
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:   // 0
                    Log.w(TAG,"mainGattCallback:"+ "DISCONNECTED");

                    //mConnectionState = STATE_DISCONNECT;
                    close();    // 每次disconnect必須要close GATT

                    intentAction = ACTION_GATT_DISCONNECTED;
                    broadcastUpdate(intentAction);
                    break;

                default:
                    Log.e(TAG,"mainGattCallback"+ "STATE_OTHER:"+newState);
                    break;
            }
        }

        //-------------------------------------------------------
        // 搜尋 GATT services
        //-------------------------------------------------------
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            Log.w(TAG, "----onServicesDiscovered:"+status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                LookingForDeviceService();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status){

            String intentAction;

            //Log.w(TAG,"onCharacteristicRead:"+ characteristic.getUuid().toString().toUpperCase()+":"+status);
            if (status== BluetoothGatt.GATT_SUCCESS) {
                if (characteristic.getUuid().toString().toUpperCase().equals(sUUID.CHR_MODELNAME)) {
                    m_ModelName = characteristic.getStringValue(0);

                    intentAction = ACTION_GATT_MODEL_NAME;
                    broadcastUpdate(intentAction);

                    GetManufacturer();  // 下一步取得制造商
                } else if (characteristic.getUuid().toString().toUpperCase().equals(sUUID.CHR_MANUFACTURER)) {
                    m_Manufacturer = characteristic.getStringValue(0);
                    intentAction = ACTION_GATT_MANUFACTURER;
                    broadcastUpdate(intentAction);
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status){
            // 由app送出data
            Log.w(TAG,"onCharacteristicWrite:"+ characteristic.getUuid().toString().toUpperCase()+",status:"+status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor,
                                      int status) {
            String intentAction;

            Log.w(TAG, "onDescriptorWrite:" + descriptor.getUuid());
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "status fail");
            }
            else {
                // enable Tx notify description之後，將GattActivity status改為"已連線"
                intentAction = ACTION_GATT_NOTIFY_FINISH;
                broadcastUpdate(intentAction);
                GetModelName(); // 下一步取得model name
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt,
                                     BluetoothGattDescriptor descriptor,
                                     int status){
            Log.w(TAG, "onDescriptorRead");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            String intentAction;

            Log.w(TAG, "onCharacteristicChanged: "+characteristic.getUuid().toString().toUpperCase());

            if (characteristic.getUuid().toString().toUpperCase().equals(sUUID.CHR_ISSC_TRANS_TX)) {

                // 將收到的字串更新至GattActivity下面console裡的字串
                final String data = characteristic.getStringValue(0);
                Log.w(TAG, "收到: " + data);

                intentAction = ACTION_GATT_RX_STRING;
                broadcastString(intentAction, data);
            }
        }
    };

    //=================================================================
    // 讀取Characteristic
    //=================================================================
    public String readCharacteristicValue(BluetoothGattCharacteristic characteristic) {
        return new String(characteristic.getValue());
    }

    //=================================================================
    // 發送廣播
    //=================================================================
    private void broadcastUpdate(final String action) {
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastString(final String action, String string){
        Intent intent = new Intent(action);
        Log.w(TAG, "broad:"+string);
        intent.putExtra("RX_STRING", string);
        sendBroadcast(intent);
    }
}
