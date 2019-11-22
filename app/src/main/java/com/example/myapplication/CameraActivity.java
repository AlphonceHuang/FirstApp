package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import static com.example.myapplication.StorageUtil.checkSDCard;
import static com.example.myapplication.StorageUtil.getRealPathFromURI;
import static com.example.myapplication.StorageUtil.imageFilter;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "Alan";

    private Button btn_TakePicture;
    private ImageView CameraImage;
    private TextView ImagePath;
    private TextView Detail1;

    private static final int Image_Capture_Code = 1;
    private File[] files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Log.w(TAG, "CameraActivity:onCreate");

        InitialViews();
    }

    @Override
    protected void onResume(){
        Log.w(TAG, "CameraActivity:onResume");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        super.onResume();
    }

    private void UpdateImagePath(){
        if (checkSDCard()){
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String folder = Environment.getExternalStorageDirectory() + "/DCIM/100ANDRO/";
                files = getImages(folder);  // 將此路徑裡的相關檔案取出

                if ((files != null ? files.length : 0) != 0) {
                    CameraImage.setImageURI(Uri.fromFile(files[files.length-1]));
                    ImagePath.setText(getRealPathFromURI(Uri.fromFile(files[files.length-1])));
                }else{
                    Log.w(TAG,"Without Image Files.");
                    CameraImage.setImageResource(R.drawable.home);
                }
            }else{
                Log.w(TAG, "MEDIA_MOUNTED fail.");
                CameraImage.setImageResource(R.drawable.home);
            }
        }
        else
            CameraImage.setImageResource(R.drawable.home);
    }

    private void InitialViews(){
        CameraImage = findViewById(R.id.CameraImage);

        btn_TakePicture=findViewById(R.id.TakePictureBtn);
        btn_TakePicture.setOnClickListener(CameraBtnEvent);

        ImagePath=findViewById(R.id.CaptureImagePath);
        ImagePath.setSelected(true);    // 跑馬燈
        UpdateImagePath();

        Detail1=findViewById(R.id.CameraDetailText1);
        Detail1.setSelected(true);
    }
/*
    // 將 URI 轉成 path 字串回傳
    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(contentURI, null, null, null, null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
*/
    private View.OnClickListener CameraBtnEvent=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.TakePictureBtn:
                    Intent cInt = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cInt,Image_Capture_Code);
                    break;
                default:
                    break;
            }
        }
    };

    // 將取得的檔案放至array
    private File[] getImages(String path){
        File folder= new File(path);
        if (folder.isDirectory()){
            File[] fs=folder.listFiles(imageFilter);
            return fs;
        }
        return null;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Image_Capture_Code) {
            if (resultCode == RESULT_OK) {
                Bitmap bp = (Bitmap) data.getExtras().get("data");
                CameraImage.setImageBitmap(bp);

                // update image path text
                UpdateImagePath();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            }
        }
    }

}
