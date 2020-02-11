package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.myapplication.StorageUtil.saveMattoBitmapFile;
import static com.example.myapplication.Util.showToastIns;

public class Tutorial1Activity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "Alan";

    private CameraBridgeViewBase mOpenCvCameraView;
    SharedPreferences mem_Image_threshold;
    private int threshold;
    private TextView threshold_txt;

    private Mat mRgba, mPartical;
    private int rect_width=0, rect_high=0;
    private Mat mIntermediateMat;
    private Point rect_p1=new Point();


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully");
                mOpenCvCameraView.enableView();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    public Tutorial1Activity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial1_surface_view);

        mOpenCvCameraView = findViewById(R.id.tutorial1_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        mem_Image_threshold = getSharedPreferences("IMAGE_THREHOLD", MODE_PRIVATE);
        threshold=mem_Image_threshold.getInt("IMAGE_THREHOLD", 200);
        Log.w(TAG, "threshold="+threshold);

        SeekBar threshold_Bar = findViewById(R.id.seekBar2);
        threshold_Bar.setMax(255);
        threshold_Bar.setProgress(threshold);
        threshold_Bar.setOnSeekBarChangeListener(thresholdOnSeekBarChange);

        threshold_txt=findViewById(R.id.thre_text);
        threshold_txt.setText(String.valueOf(threshold));

        Button takePic=findViewById(R.id.takePicButton);
        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w(TAG, "take whole picture.");
                takePicture(0, mRgba);
            }
        });
        Button takePic1=findViewById(R.id.takePic1Button);
        takePic1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w(TAG, "take center picture.");
                takePicture(1, mPartical);
            }
        });

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();

            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // 有沒有此行沒差
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN    // 隱藏status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private SeekBar.OnSeekBarChangeListener thresholdOnSeekBarChange = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            Log.w(TAG, "threshold調至:"+i);
            threshold=i;
            threshold_txt.setText(String.valueOf(threshold));
            setImageThrehold(threshold);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);

        mIntermediateMat = new Mat();
        rect_width=width/2;
        rect_high=height/2;

        rect_p1.y = width/2-rect_width/2;
        rect_p1.x = height/2-rect_high/2;

        //Log.w(TAG, "width="+width+",height="+height);
        //Log.w(TAG, "rect_width="+rect_width+",rect_high="+rect_high);
        //Log.w(TAG, "p1.x="+rect_p1.x+",p1.y="+rect_p1.y);
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        Mat rgbaInnerWindow;

        int left = (int)rect_p1.x;
        int top = (int)rect_p1.y;
        int width = rect_width;
        int height = rect_high;

        // 畫框
        Point a0 = new Point(left, top);
        Point a3 = new Point(left + width, top + height);
        Imgproc.rectangle(mRgba, a0, a3, new Scalar(255, 0, 0), 4);


        Mat gray = inputFrame.gray();
        Mat grayInnerWindow = gray.submat(top, top + height, left, left + width);
        rgbaInnerWindow = mRgba.submat(top, top + height, left, left + width);
        //Imgproc.Sobel(grayInnerWindow, mIntermediateMat, CvType.CV_8U, 1, 1);
        //Core.convertScaleAbs(mIntermediateMat, mIntermediateMat, 10, 0);
        Imgproc.cvtColor(grayInnerWindow, mIntermediateMat, Imgproc.COLOR_GRAY2BGRA, 4);
        Imgproc.threshold(mIntermediateMat, rgbaInnerWindow, threshold, 255, Imgproc.THRESH_BINARY);
        mPartical = rgbaInnerWindow.clone();
        grayInnerWindow.release();
        rgbaInnerWindow.release();


        return mRgba;


        //Mat grayMat = rgbMat.clone();
        //Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY, 2);
        //Imgproc.threshold(grayMat, grayMat, threshold, 255, Imgproc.THRESH_BINARY);// 大於thresh的都設定為max，小於的設定0
        //Core.bitwise_not(grayMat, grayMat);   // 反色 --- 適用於白點黑圖
        //return grayMat;
    }

    private void setImageThrehold(int data){
        SharedPreferences.Editor editor;
        editor = mem_Image_threshold.edit(); //獲取編輯器
        editor.putInt("IMAGE_THREHOLD", data);
        editor.apply();
        editor.commit();    //提交
    }

    private void takePicture(int type, Mat inputMat){
        String savepath = Environment.getExternalStorageDirectory() + "/DCIM/";
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");//获取当前时间，进一步转化为字符串
        Date date =new Date();
        String str=format.format(date);
        String fileName;

        try {
            if (type==0)
                fileName = "Tri1_"+str+".jpg";
            else
                fileName = "Tri2_"+str+".jpg";
            saveMattoBitmapFile(inputMat, fileName, savepath);

            showToastIns(getApplicationContext(), "fileName:"+fileName, Toast.LENGTH_SHORT);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
