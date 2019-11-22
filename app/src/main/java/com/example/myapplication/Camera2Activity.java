package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

import static com.example.myapplication.StorageUtil.checkSDCard;
import static com.example.myapplication.StorageUtil.getPhotoDefaultName;
import static com.example.myapplication.Util.showToastIns;

public class Camera2Activity extends AppCompatActivity {
    private static final String TAG = "Alan";

    private Camera cameraObject;
    private ImageView pic;

    //SharedPreferences mem_CapImageIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);

        // 改在AndroidManifest.xml裡設定轉向
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        // 拍攝下來的照片index
        //mem_CapImageIndex = getSharedPreferences("TAKEPICTURE_INDEX", MODE_PRIVATE);

        pic = findViewById(R.id.camera1Image);
        cameraObject = isCameraAvailiable();
        ShowCamera showCamera = new ShowCamera(this, cameraObject);
        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(showCamera);

        TextView detail = findViewById(R.id.camera1DetailText);
        detail.setSelected(true);   // 跑馬燈

        //c = Calendar.getInstance();
        Log.w(TAG, "Camera2Activity:onCreate");
    }

    @Override
    protected void onResume(){
        //setPreviewSize();

        //c = Calendar.getInstance();
        cameraObject.startPreview();

        super.onResume();
        Log.w(TAG, "Camera2Activity:onResume");
    }

    @Override
    protected void onDestroy(){
        cameraObject.release();
        cameraObject=null;

        super.onDestroy();
    }
/*
    private void setPreviewSize(){
        //DisplayMetrics dm = new DisplayMetrics();
        //getWindowManager().getDefaultDisplay().getMetrics(dm);
        //int w1  = dm.widthPixels;
        //int h1 = dm.heightPixels;
        //Log.w(TAG, "display:"+w1+","+h1);   // 1080, 1776

        int cameraWidth=cameraObject.getParameters().getPreviewSize().width;
        int cameraHeight=cameraObject.getParameters().getPreviewSize().height;
        Log.w(TAG, "display:"+cameraWidth+","+cameraHeight);
    }*/

    /*
    //=================================================================
    // picture index 儲存/讀取
    //=================================================================
    private int PictureIndexGet(){
        return mem_CapImageIndex.getInt("TAKEPICTURE_INDEX", 0);
    }

    private void PictureIndexSet(int index){

        SharedPreferences.Editor editor = mem_CapImageIndex.edit(); //獲取編輯器
        editor.putInt("TAKEPICTURE_INDEX", index);
        editor.apply();
        editor.commit();    //提交
    }
    */

    public static Camera isCameraAvailiable(){
        Camera object = null;
        try {
            object = Camera.open();
        }
        catch (Exception e){
            Log.w(TAG, "camera can't open");
        }
        return object;
    }

    private Camera.PictureCallback capturedIt = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data .length);
            if(bitmap==null){
                showToastIns(getApplicationContext(), "拍照失敗了", Toast.LENGTH_SHORT);
            }
            else
            {
                //showToastIns(getApplicationContext(), "拍好了", Toast.LENGTH_SHORT);
                pic.setImageBitmap(bitmap); // 顯示拍完的照片在ImageView
                pic.setRotation(90);    //手機直拍時，畫面呈現橫向，將其轉90度使其跟preview時一樣方向

                saveInSDCard(bitmap);
                cameraObject.startPreview();    // 重新開始preview

            }
            //cameraObject.release();
        }
    };

    public void snapIt(View view){
        //cameraObject.autoFocus(autoFocusCallback);
        cameraObject.takePicture(null, null, capturedIt);
    }
/*
    private final Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            //success表示对焦成功
            if (success){
                Log.i(TAG, "myAutoFocusCallback:success...");
                //myCamera.setOneShotPreviewCallback(null);
            } else {
                //未对焦成功
                Log.i(TAG, "myAutoFocusCallback: 失败了...");
            }
        }
    };*/

    private void saveInSDCard(Bitmap bitmap){
        //int index=0;
        if (checkSDCard()){
            //Log.w(TAG, "SD存在");
            try {
                File f = new File(Environment.getExternalStorageDirectory(), "/DCIM/AAA/");
                Log.w(TAG,"path:" + f.getAbsolutePath());
                if (!f.exists()) {
                    if (f.mkdir()){
                        Log.w(TAG, "/DCIM/AAA/建立成功");
                    }
                }
                //private Calendar c;
                String path1 = getPhotoDefaultName();
                File n = new File(f, path1);
                FileOutputStream bos = new FileOutputStream(n.getAbsolutePath());
                /* 文件转换 */
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                /* 调用flush()方法，更新BufferStream */
                bos.flush();
                /* 结束OutputStream */
                bos.close();
                showToastIns(getApplicationContext(), "保存成功:"+"\n"+n.getAbsolutePath(), Toast.LENGTH_SHORT);

            }catch (Exception e){
                e.printStackTrace();
                Log.w(TAG, "保存失敗.");
                showToastIns(getApplicationContext(), "保存失敗:"+"\n", Toast.LENGTH_SHORT);
            }
        }else{
            Log.w(TAG, "without SD card.");
        }
    }
}
