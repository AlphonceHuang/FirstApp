package com.example.myapplication;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class Tutorial1Activity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "Alan";

    private CameraBridgeViewBase mOpenCvCameraView;
    SharedPreferences mem_Image_threshold;
    private int threshold;
    private TextView threshold_txt;

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
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        //return inputFrame.rgba(); // 彩色
        //return inputFrame.gray();   // 灰階

        Mat rgbMat = inputFrame.rgba();
        Mat grayMat = rgbMat.clone();
        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY, 2);
        Imgproc.threshold(grayMat, grayMat, threshold, 255, Imgproc.THRESH_BINARY);// 大於thresh的都設定為max，小於的設定0
        //Core.bitwise_not(grayMat, grayMat);   // 反色 --- 適用於白點黑圖
        return grayMat;
    }

    private void setImageThrehold(int data){
        SharedPreferences.Editor editor;
        editor = mem_Image_threshold.edit(); //獲取編輯器
        editor.putInt("IMAGE_THREHOLD", data);
        editor.apply();
        editor.commit();    //提交
    }

}
