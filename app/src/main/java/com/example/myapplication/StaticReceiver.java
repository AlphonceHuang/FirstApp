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
        if (action.equals(ACTION_MY_BROADCAST)) {
            showToastIns(context, "收到我的廣播了", Toast.LENGTH_LONG);
        }
    }
}
