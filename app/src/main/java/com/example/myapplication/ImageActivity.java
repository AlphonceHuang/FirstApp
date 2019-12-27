package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import static com.example.myapplication.StorageUtil.checkSDCard;
import static com.example.myapplication.StorageUtil.getImages;
import static com.example.myapplication.StorageUtil.getRealPathFromURI;
import static com.example.myapplication.Util.FROM_FILE_BROWSER;
import static com.example.myapplication.Util.FROM_IMAGE_ACTIVITY;
import static com.example.myapplication.Util.setFromWhichActivity;
import static org.opencv.core.CvType.CV_16U;
import static org.opencv.imgproc.Imgproc.cvtColor;


public class ImageActivity extends AppCompatActivity {

    private static final String TAG="Alan";
    private Button SDPreBTN;
    private Button SDNextBTN;
    private Button SDCalBTN;
    private ImageView SDImage;
    private TextView SDFilePath, SDResult;

    private File[] files;
    private int index;

    private String image_path;
    private static final String default_path=Environment.getExternalStorageDirectory() + "/DCIM/100ANDRO/";
    SharedPreferences mem_ImageDefaultPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        mem_ImageDefaultPath = getSharedPreferences("IMAGE_DEFAULT_PATH", MODE_PRIVATE);

        Intent intent = this.getIntent();//取得傳遞過來的資料
        image_path = intent.getStringExtra("image_path");
        Log.w(TAG, "ImageActivity:onCreate:"+image_path);

        if (image_path == null){    // 直接進入image activity會得到null
            image_path=mem_ImageDefaultPath.getString("IMAGE_DEFAULT_PATH", "");

            assert image_path != null;
            if (image_path.isEmpty()){  // 還沒有存路徑
                Log.w(TAG, "mem empty");
                image_path = default_path;
            }
        }else{
            Log.w(TAG, "save path to mem"+image_path);
            SharedPreferences.Editor editor = mem_ImageDefaultPath.edit(); //獲取編輯器
            editor.putString("IMAGE_DEFAULT_PATH", image_path);
            editor.apply();
            editor.commit();    //提交
        }

        SDPreBTN = findViewById(R.id.SDPrv_Btn);
        SDPreBTN.setOnClickListener(ImageBTN);

        SDNextBTN = findViewById(R.id.SDNext_Btn);
        SDNextBTN.setOnClickListener(ImageBTN);

        SDCalBTN = findViewById(R.id.SDCalBtn);
        SDCalBTN.setOnClickListener(ImageBTN);

        Button SDBrowser = findViewById(R.id.BrowserButton);
        SDBrowser.setOnClickListener(ImageBTN);

        SDImage = findViewById(R.id.SDImage);
        SDImage.setImageResource(R.drawable.projector); // 檔案不存在，顯示預設圖示

        SDFilePath = findViewById(R.id.SDfilepath);
        SDFilePath.setText("");

        SDResult = findViewById(R.id.SDResultText);
        //SDResult.setText("");

        index=0;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        setFromWhichActivity(FROM_FILE_BROWSER);
    }

    @Override
    protected void onResume(){

        // 隱藏Title
        //ActionBar actionBar = getSupportActionBar();
        //actionBar.hide();

        //View decorView = getWindow().getDecorView();
        //int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;    // 圖示變成一個點
        //decorView.setSystemUiVisibility(uiOptions);


        // 下面用法可將status bar變全透明, android:fitsSystemWindows="true" 畫面才不會跟bar重疊
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); // 確認取消半透明設置。
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // 全螢幕顯示，status bar 不隱藏，activity 上方 layout 會被 status bar 覆蓋。
                |View.SYSTEM_UI_FLAG_LAYOUT_STABLE); // 配合其他 flag 使用，防止 system bar 改變後 layout 的變動。
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS); // 跟系統表示要渲染 system bar 背景。
        window.setStatusBarColor(Color.TRANSPARENT);

        // 下面方法更簡單，也是透明化，一樣要將android:fitsSystemWindows="true" 畫面才不會跟bar重疊
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); // 半透明
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);   // 半透明

        // 改用style的方式來透明status bar及navigation bar
        // styles.xml
        // <item name="android:windowTranslucentStatus">true</item>
        // <item name="android:windowTranslucentNavigation">true</item>
        // <item name="windowNoTitle">true</item>
        // 透明之後，內容要由status bar下方開始畫，所以這個activity的layout要加入
        // android:fitsSystemWindows="true" 畫面才不會跟bar重疊
        // 上述方法在android 9，Navigation bar仍無法透明

        // 加載OpenCV
        if (!OpenCVLoader.initDebug()) {
            Log.w(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, loaderCallback);
        } else {
            Log.w(TAG, "OpenCV library found inside package. Using it!");
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        // 檢查圖片路徑
        if (checkSDCard()){
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String folder = default_path;

                if (image_path!=null)
                    folder = image_path;
                Log.w(TAG, "load folder:"+folder);

                File cfolder=new File(folder);

                if (cfolder.exists()) { // 資料夾存在
                    files = getImages(folder);  // 將此路徑裡的相關檔案取出

                    if ((files != null ? files.length : 0) != 0) {
                        SDImage.setImageURI(Uri.fromFile(files[index]));
                        SDFilePath.setText(getRealPathFromURI(Uri.fromFile(files[index])));

                        if (files.length<2){
                            SDPreBTN.setEnabled(false);
                            SDNextBTN.setEnabled(false);
                        }
                    }else{
                        Log.w(TAG,"Without Image Files.");
                        ReadDataFail();
                        SDFilePath.setText(getString(R.string.file_not_exist));
                    }
                }
                else {
                    Log.w(TAG, "Folder not exist.");
                    ReadDataFail();
                    SDFilePath.setText(getString(R.string.folder_not_exist));
                }
            }else{
                Log.w(TAG, "MEDIA_MOUNTED fail.");
            }
        }else {
            Log.w(TAG, "SD card doesn't exist.");
        }

        super.onResume();
    }

    // 按back key
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 如果是由FileManagerActivity回來這，按back又會至FileManagerActivity,
            // 為避免這個情況，強制回至MainActivity，並且設定FLAG_ACTIVITY_CLEAR_TOP
            Log.w(TAG, "press back");
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            //VideoActivity.this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private void ReadDataFail(){
        SDPreBTN.setEnabled(false);
        SDNextBTN.setEnabled(false);
        SDCalBTN.setEnabled(false);
    }

    //-----------------------------------------------------
    // OpenCV loader callback
    //-----------------------------------------------------
    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == BaseLoaderCallback.SUCCESS) {
                Log.w(TAG, "加载成功");
            } else {
                super.onManagerConnected(status);
                Log.w(TAG, "加载失败");
            }
        }
    };

    //-----------------------------------------------------
    // 按鈕 Event
    //-----------------------------------------------------
    private View.OnClickListener ImageBTN= new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.SDPrv_Btn:
                    if (index>0){
                        index--;
                    }else{
                        index=files.length-1;
                    }
                    SDImage.setImageURI(Uri.fromFile(files[index]));
                    SDFilePath.setText(getRealPathFromURI(Uri.fromFile(files[index])));
                    break;

                case R.id.SDNext_Btn:
                    if (index < (files.length-1)) {
                        index++;
                    }else{
                        index=0;
                    }
                    SDImage.setImageURI(Uri.fromFile(files[index]));
                    SDFilePath.setText(getRealPathFromURI(Uri.fromFile(files[index])));
                    break;

                case R.id.SDCalBtn:
                    Bitmap bm=BitmapFactory.decodeFile(getRealPathFromURI(Uri.fromFile(files[index])));
                    String finish = getString(R.string.Laplacian)+getLaplacian(bm);
                    SDResult.setText(finish);
                    break;

                case R.id.BrowserButton:
                    setFromWhichActivity(FROM_IMAGE_ACTIVITY);
                    Intent intent = new Intent(ImageActivity.this, FileManagerActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };
/*
    //讀取SDCard圖片，型態為Bitmap
    private static Bitmap getBitmapFromSDCard(String path)
    {
        File mFile=new File(path);

        Log.w(TAG, "path="+path+",exist="+mFile.exists()+
                ",read="+mFile.canRead());

        if (mFile.exists()) {
            if (mFile.canRead()) {
                try{
                    return BitmapFactory.decodeFile(path);
                }
                catch (Exception e){
                    e.printStackTrace();
                    Log.w(TAG, "no file");
                    return null;
                }
            }else{
                Log.w(TAG, "file can't read.");
                return null;
            }
        }
        else {
            Log.w(TAG, "file isn't exist.");
            return null;
        }
    }
 */

    // 將取得的檔案放至array
    //private File[] getImages(String path){
    //    File folder= new File(path);
    //    if (folder.isDirectory()){
    //        return folder.listFiles(imageFilter);
    //    }
    //    return null;
    //}
/*
    // 過濾所列的檔案類型
    private FileFilter imageFilter=new FileFilter(){

        @Override
        public boolean accept(File file) {
            String filename=file.getName();

            return filename.endsWith("JPG") || filename.endsWith("BMP") ||
                    filename.endsWith("PNG") || filename.endsWith("JPEG");
        }
    };*/

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

    private double getLaplacian(Bitmap bmp) {

        Mat img = new Mat();
        //bitmap->mat
        Utils.bitmapToMat(bmp, img);

        //Mat areamat = img.submat(0,50, 0, 50);

        Mat imageGrey = new Mat();
        //cvtColor(areamat, imageGrey, Imgproc.COLOR_RGB2GRAY);
        cvtColor(img, imageGrey, Imgproc.COLOR_RGB2GRAY);

        Mat imageSobel = new Mat();

        Imgproc.Laplacian(imageGrey, imageSobel, CV_16U);    //拉普拉斯梯度
        //图像的平均灰度
        Log.w(TAG, "av:"+ Core.mean(imageSobel).val[0]);
        return Core.mean(imageSobel).val[0];
    }
}
