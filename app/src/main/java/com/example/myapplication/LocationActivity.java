package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.example.myapplication.Util.showToastIns;

public class LocationActivity extends AppCompatActivity implements LocationListener {
    private static final String TAG = "Alan";
    private boolean getService = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        Log.w(TAG, "LocationActivity:onCreate");

        checkAndRequestPermissions();
    }

    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,

    };
    private List<String> mMissPermissions = new ArrayList<>();
    private static final int REQUEST_CODE = 2010;

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
            Log.w(TAG, "Location所有權限完成");
            //取得系統定位服務
            testLocationProvider();
        } else {
            ActivityCompat.requestPermissions(this,
                    mMissPermissions.toArray(new String[mMissPermissions.size()]),
                    REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults){
        for (int i = 0, len = permissions.length; i < len; i++) {
            if (requestCode == REQUEST_CODE){
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    Log.w(TAG, "---ok");
                    testLocationProvider();
                }else{
                    //Log.w(TAG, "---fail");
                    showToastIns(getApplicationContext(), "無GPS授權，無法使用此功能", Toast.LENGTH_LONG);
                    finish();
                }
            }
        }
    }

    private LocationManager lms;
    private String bestProvider = LocationManager.NETWORK_PROVIDER;
    Location bestLocation = null;
    private List<String> pNames = new ArrayList<>(); // 存放LocationProvider名称的集合

    private void locationServiceInitial() {
        lms = (LocationManager) getSystemService(LOCATION_SERVICE); //取得系統定位服務

        // 列出所有provider
        pNames.clear();
        pNames=lms.getAllProviders();
        TextView allPrividerText=findViewById(R.id.allProvider);
        allPrividerText.setText(getProvider());

        boolean gps = lms.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = lms.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (gps || network) {   // 有網路或GPS
            //Criteria criteria = new Criteria(); //資訊提供者選取標準
            //bestProvider = lms.getBestProvider(criteria, true); //選擇精準度最高的提供者
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "locationServiceInitial: permission fail.");
            }

            List<String> providers = lms.getProviders(true);

            for (String provider : providers) {
                bestProvider = provider;
                Location l = lms.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                    // Found best last known location: %s", l);
                    bestLocation = l;
                }
            }

            //Location location = lms.getLastKnownLocation(bestProvider);
            Log.w(TAG, "bestLocation:" + bestLocation);
            getLocation(bestLocation);
        }
    }

    private String getProvider(){
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string. all_provider));
        for (String s : pNames) {
            sb.append("\n").append(s);
        }
        return sb.toString();
    }

    private void testLocationProvider() {
        //取得系統定位服務
        //if (!isOpenGps()){
        LocationManager status = (LocationManager) (getApplicationContext().getSystemService(Context.LOCATION_SERVICE));
        if (status.isProviderEnabled(LocationManager.GPS_PROVIDER) || status.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //如果GPS或網路定位開啟，呼叫locationServiceInitial()更新位置
            getService = true;	//確認開啟定位服務
            locationServiceInitial();
        } else {
            Toast.makeText(this, getString(R.string.open_location), Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));	//開啟設定頁面
        }
    }

    private void getLocation(Location location) {   //將定位資訊顯示在畫面中
        if (location != null) {
            TextView longitude_txt = findViewById(R.id.longitude);
            TextView latitude_txt = findViewById(R.id.latitude);
            TextView provider_txt = findViewById(R.id.provider);

            double longitude = location.getLongitude(); //取得經度
            double latitude = location.getLatitude();   //取得緯度

            if (bestProvider.equals(LocationManager.NETWORK_PROVIDER))
                provider_txt.setText(getString(R.string.location_provide_network));
            else
                provider_txt.setText(getString(R.string.location_provide_gps));

            String temp = getString(R.string.longitude)+":"+longitude;
            longitude_txt.setText(temp);
            temp = getString(R.string.latitude)+":"+latitude;
            latitude_txt.setText(temp);
        } else {
            Toast.makeText(this, getString(R.string.can_not_location), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getService) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //checkAndRequestPermissions();
                Log.w(TAG, "onResume not ok.");
                return;
            }
            lms.requestLocationUpdates(bestProvider, 1000, 1, this);
            //lms.requestLocationUpdates(bestProvider, 0, 0, this);
            //服務提供者、更新頻率60000毫秒=1分鐘、最短距離、地點改變時呼叫物件
            Log.w(TAG, "on resume");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(getService) {
            lms.removeUpdates(this);    //離開頁面時停止更新
        }
    }

    @Override
    protected void onRestart() {	//從其它頁面跳回時
        super.onRestart();
        testLocationProvider();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.w(TAG, "onLocationChanged:"+location);
        //locationServiceInitial();
        getLocation(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
