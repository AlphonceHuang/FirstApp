package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.location.LocationManager;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

class Util {

    private static Toast toast;
    private static Toast toast_1;
    private static int fromWhichActivity;

    static final int FROM_FILE_BROWSER=0;
    static final int FROM_IMAGE_ACTIVITY=1;
    static final int FROM_VIDEO_ACTIVITY=2;
    static final int FROM_MUSIC_ACTIVITY=3;
    static final int FROM_RECORD_ACTIVITY=4;

    static void showToastIns(Context context,
                             CharSequence content,
                             int time,
                             int gravity, int xOffset, int yOffset) {
            if (Build.VERSION.SDK_INT <= 23 && gravity != Gravity.NO_GRAVITY) {

                if (toast == null) {
                    toast = Toast.makeText(context,
                            content,
                            time);
                } else {
                    toast.setText(content); // 只更新字串
                }
                toast.setGravity(gravity, xOffset, yOffset);
                toast.show();

            } else{   // Android 9.0只能用原本的方式，會自動更新字串，不需用上面的方式
                Toast.makeText(context, content, time).show();
            }
    }

    static void showToastIns(Context context,
                             CharSequence content,
                             int time) {
        if (Build.VERSION.SDK_INT<=23) {
            if (toast_1 == null) {
                //Log.w("Alan", "null");
                toast_1 = Toast.makeText(context,
                        content,
                        time);
            } else {
                //Log.w("Alan", "update");
                toast_1.setText(content);
            }
            toast_1.show();
        }else   // Android 9.0只能用原本的方式，會自動更新字串，不需用上面的方式
        {
            Toast.makeText(context, content, time).show();
        }
    }

    //=====================================================================
    // 檢查 Location service 是否已開啟
    //=====================================================================
    static boolean isLocationEnable(Context context) {

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        return (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
    }

    //=====================================================================
    // 將byte轉成Hex字串
    //=====================================================================
    static String toHex(byte b){
        return (""+"0123456789ABCDEF".charAt(0xf&b>>4)+"0123456789ABCDEF".charAt(b&0xf));
    }

    //=====================================================================
    // 截取目前畫面
    //=====================================================================
    public static Bitmap takeScreenShot(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        //Find the screen dimensions to create bitmap in the same size.
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay().getHeight();

        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight);
        view.destroyDrawingCache();
        return b;
    }

    // 跳轉至file browser時設定按確認之後的activity
    static int getFromWhichActivity(){
        return fromWhichActivity;
    }
    static void setFromWhichActivity(int val){
        fromWhichActivity=val;
    }

}
