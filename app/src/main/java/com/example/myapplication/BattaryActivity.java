package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.myapplication.Util.showToastIns;


public class BattaryActivity extends AppCompatActivity {

    private static final String TAG="Alan";
    private BatteryManager manager;
    Handler handler;
    private int TIME = 5000;
    private boolean update=false;
    private Switch autoRef;
    SharedPreferences mem_AutoRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battary);

        manager = (BatteryManager) getSystemService(BATTERY_SERVICE);
        readBattery();

        mem_AutoRefresh = getSharedPreferences("BATTERY_AUTO_REFHRESH", MODE_PRIVATE);

        Button mRefresh = findViewById(R.id.BatteryMRefresh);
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readBattery();
            }
        });

        autoRef = findViewById(R.id.BatteryMAuto);
        autoRef.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                AutoRefresh(b);
            }
        });
        update = mem_AutoRefresh.getBoolean("BATTERY_AUTO_REFHRESH", true);
        autoRef.setChecked(update);

        handler = new Handler();
        handler.postDelayed(runnable, TIME);
    }

    @Override
    protected void onResume(){
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_BATTERY_OKAY);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(mBattery, filter);
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(mBattery);

        // 停止udpate電池
        handler.post(runnable);
        handler.removeCallbacks(runnable);
    }

    private void readBattery(){
        String string;

        // 當前電流
        TextView mCurrent = findViewById(R.id.BatteryMCurrent);
        int current = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
        string = getString(R.string.current)+":"+current/1000+"mA";
        mCurrent.setText(string);
        Log.w(TAG, "current:"+string);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.w(TAG, "status:"+manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS));
        }
        Log.w(TAG, "Remaining energy ="+manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)+"mWh");
        Log.w(TAG, "current avg:"+manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE));
        Log.w(TAG, "enegry counter:"+manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER));

        // 電量百分比
        TextView mPercentage=findViewById(R.id.BatteryMPercent);
        int percent=manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        string = getString(R.string.capacity)+":"+percent+"%";
        mPercentage.setText(string);

    }

    private final BroadcastReceiver mBattery = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            switch (action) {
                case Intent.ACTION_BATTERY_CHANGED:

                    String string;

                    TextView voltage = findViewById(R.id.BatteryVoltage);
                    int battery_voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
                    string = getString(R.string.voltage) + ":"+battery_voltage+"mV";
                    voltage.setText(string);

                    TextView temperature = findViewById(R.id.BatteryTemperature);
                    int battery_temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
                    string = getString(R.string.temperature) + ":" + battery_temp / 10 + "." + battery_temp % 10 + "℃";
                    temperature.setText(string);
                    if (battery_temp > 400) {
                        //temperature.setTextColor(Color.RED);
                        temperature.setTextColor(getResources().getColor(R.color.colorDarkRed));
                    } else {
                        //temperature.setTextColor(Color.parseColor("#FF1F6B22"));
                        temperature.setTextColor(getResources().getColor(R.color.colorDarkGreen));
                    }


                    TextView tech = findViewById(R.id.BatteryTech);
                    string = getString(R.string.battery_tech) + ":" + intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
                    tech.setText(string);

                    /*判斷充電狀態來源*/
                    TextView status = findViewById(R.id.BatteryStatus);
                    int battery_status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    switch (battery_status) {
                        case BatteryManager.BATTERY_STATUS_CHARGING:
                            string = getString(R.string.status) + ":" + getString(R.string.charging);
                            status.setTextColor(getResources().getColor(R.color.colorDarkRed));
                            break;
                        case BatteryManager.BATTERY_STATUS_DISCHARGING:
                            string = getString(R.string.status) + ":" + getString(R.string.discharging);
                            status.setTextColor(Color.BLUE);
                            break;
                        case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                            string = getString(R.string.status) + ":" + getString(R.string.notCharge);
                            status.setTextColor(Color.BLACK);
                            break;
                        case BatteryManager.BATTERY_STATUS_FULL:
                            string = getString(R.string.status) + ":" + getString(R.string.chargeFull);
                            //status.setTextColor(Color.GREEN);
                            status.setTextColor(getResources().getColor(R.color.colorDarkGreen));
                            break;
                        default:
                            string = getString(R.string.status) + ":" + getString(R.string.unknow);
                            break;
                    }
                    status.setText(string);

                    /*判斷健康狀態*/
                    TextView health = findViewById(R.id.BatteryHealth);
                    int battery_health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
                    switch (battery_health) {
                        case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                            string = getString(R.string.health) + ":" + getString(R.string.unknow);
                            health.setTextColor(Color.BLACK);
                            break;
                        case BatteryManager.BATTERY_HEALTH_GOOD:
                            string = getString(R.string.health) + ":" + getString(R.string.good);
                            health.setTextColor(getResources().getColor(R.color.colorDarkGreen));
                            break;
                        case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                            string = getString(R.string.health) + ":" + getString(R.string.overheat);
                            health.setTextColor(Color.RED);
                            break;
                        case BatteryManager.BATTERY_HEALTH_DEAD:
                            string = getString(R.string.health) + ":" + getString(R.string.dead);
                            health.setTextColor(Color.RED);
                            break;
                        case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                            string = getString(R.string.health) + ":" + getString(R.string.overVoltage);
                            health.setTextColor(Color.RED);
                            break;
                        case BatteryManager.BATTERY_HEALTH_COLD:
                            string = getString(R.string.health) + ":" + getString(R.string.cold);
                            health.setTextColor(Color.RED);
                            break;
                        case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                            string = getString(R.string.health) + ":" + getString(R.string.unknow_fail);
                            health.setTextColor(Color.RED);
                            break;
                    }
                    health.setText(string);
                    //int health_1 = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
                    //Log.w(TAG, "battery_health="+battery_health+"health1="+health_1);

                    TextView capacity = findViewById(R.id.BatteryPercent);
                    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    int levelPercent = (int) (((float) level / scale) * 100);
                    string = getString(R.string.capacity) + ":" + levelPercent + "%";
                    capacity.setText(string);
                    //Log.w(TAG, "level:" + level + ",scale:" + scale);

                    /*判斷電流來源*/
                    TextView pluged = findViewById(R.id.BatteryPlug);
                    int plug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                    //Log.w(TAG, "plug"+plug);
                    switch (plug) {
                        case BatteryManager.BATTERY_PLUGGED_AC:
                            string = getString(R.string.pluged) + ":" + getString(R.string.AC);
                            break;
                        case BatteryManager.BATTERY_PLUGGED_USB:
                            string = getString(R.string.pluged) + ":" + getString(R.string.usb);
                            break;
                        case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                            string = getString(R.string.pluged) + ":" + getString(R.string.wireless);
                            break;
                        default:
                            string = getString(R.string.pluged) + ":" + getString(R.string.not_connected);
                            break;
                    }
                    pluged.setText(string);

                    /*電池i-con圖*/
                    //int icon_small = intent.getIntExtra(BatteryManager.EXTRA_ICON_SMALL, 0);
                    //Log.w(TAG, "icon_small:"+icon_small);
                    //LevelListDrawable batteryLevel = (LevelListDrawable) getResources().getDrawable(icon_small);
                    //batteryLevel.setLevel(level);
                    //ImageView ivBattery.setBackgroundDrawable(batteryLevel);
                    break;
                case Intent.ACTION_BATTERY_LOW:
                    //Log.w(TAG, "battery low");
                    showToastIns(context, "BATTERY_LOW", Toast.LENGTH_LONG);
                    break;
                case Intent.ACTION_BATTERY_OKAY:
                    //Log.w(TAG, "battery ok");
                    showToastIns(context, "BATTERY_OKAY", Toast.LENGTH_LONG);
                    break;
                case Intent.ACTION_POWER_CONNECTED:
                    //Log.w(TAG, "POWER_CONNECTED");
                    showToastIns(context, "POWER_CONNECTED", Toast.LENGTH_LONG);
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    //Log.w(TAG, "POWER_DISCONNECTED");
                    showToastIns(context, "POWER_DISCONNECTED", Toast.LENGTH_LONG);
                    break;
            }
        }
    };

    private void AutoRefresh(boolean enable){
        update = enable;
        SharedPreferences.Editor editor = mem_AutoRefresh.edit(); //獲取編輯器
        editor.putBoolean("BATTERY_AUTO_REFHRESH", update);
        editor.apply();
        editor.commit();    //提交
    }

    // 每間隔時間執行一次
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                handler.postDelayed(this, TIME);    // 間隔時間

                if (update) {
                    readBattery();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}

