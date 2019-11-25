package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import static com.example.myapplication.MainActivity.ACTION_MY_BROADCAST;
import static com.example.myapplication.Util.showToastIns;

public class StaticReceiver extends BroadcastReceiver {
    private static final String TAG="Alan";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        Log.w(TAG, "StaticReceiver: BroadcastReceiver:"+action);

        assert action != null;
        switch (action) {
            case ACTION_MY_BROADCAST:
                showToastIns(context, "收到我的廣播了", Toast.LENGTH_LONG);
                break;
            case "android.intent.action.ACTION_POWER_CONNECTED":
                showToastIns(context, "POWER_CONNECTED", Toast.LENGTH_LONG);
                //Log.w(TAG, "ACTION_POWER_CONNECTED");
                break;
            case "android.intent.action.ACTION_POWER_DISCONNECTED":
                //Log.w(TAG, "ACTION_POWER_DISCONNECTED");
                showToastIns(context, "POWER_DISCONNECTED", Toast.LENGTH_LONG);
                break;
            case "android.intent.action.BATTERY_LOW":
                showToastIns(context, "電量不足", Toast.LENGTH_LONG);
                break;
            case "android.intent.action.BATTERY_OKAY":
                showToastIns(context, "電量正常了", Toast.LENGTH_LONG);
                break;
        }
    }
}
