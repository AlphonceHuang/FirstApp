package com.example.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.ORB;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.Objects;

import static org.opencv.core.CvType.CV_16U;

public class OpenCVCameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private static final String  TAG = "Alan";
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mRgba;
    Handler handler = new Handler();
    private int TIME = 1000;
    private boolean mcalculate=false;
    private boolean featureDetect=false;
    private TextView cal_result;
    private double lap_number=0;
    Bundle bundle;
    private boolean Laplacian_method=false;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.w(TAG, "OpenCV loaded successfully");
                mOpenCvCameraView.enableView();

                //mOpenCvCameraView.setOnTouchListener(OpenCVCameraActivity.this);
            } else {
                super.onManagerConnected(status);
                Log.w(TAG, "OpenCV loaded fail");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.w(TAG, "OpenCVCameraActivity: onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        setContentView(R.layout.activity_open_cvcamera);
        cal_result=findViewById(R.id.lap_result);

        mOpenCvCameraView = findViewById(R.id.openCVCamera);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        cal_result.setText("");

        handler.postDelayed(runnable, TIME);
    }

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

        // 將status bar及navi bar透明
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); // 確認取消半透明設置。
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // 全螢幕顯示，status bar 不隱藏，activity 上方 layout 會被 status bar 覆蓋。
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE); // 配合其他 flag 使用，防止 system bar 改變後 layout 的變動。
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS); // 跟系統表示要渲染 system bar 背景。
        window.setStatusBarColor(Color.TRANSPARENT);


        if (!OpenCVLoader.initDebug()) {
            Log.w(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            Log.w(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        bundle = this.getIntent().getExtras();

        assert bundle != null;
        Log.w(TAG, "bundle="+bundle.getString("CAMERA_FUNCTION"));

        if (Objects.requireNonNull(bundle.getString("CAMERA_FUNCTION")).equals("1")) {
            Laplacian_method=true;
        }else if (Objects.requireNonNull(bundle.getString("CAMERA_FUNCTION")).equals("2")){
            featureDetect=true;
        }


    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        Log.w(TAG, "onCameraViewStarted");
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //Log.w(TAG, " "+getOrientation());
        if (getOrientation()==Surface.ROTATION_270)
            Core.flip(mRgba, mRgba,0);

        mRgba = inputFrame.rgba();

        if (mcalculate){

            Mat frame = new Mat();
            Imgproc.cvtColor(mRgba, frame, Imgproc.COLOR_RGB2GRAY);
            Mat imageSobel = new Mat();
            Imgproc.Laplacian(frame, imageSobel, CV_16U);    //拉普拉斯梯度
            //圖像平均灰度
            lap_number = Core.mean(imageSobel).val[0];
            Log.w(TAG, "Laplacian:"+ lap_number);
            mcalculate=false;
        }

        if (featureDetect)
        {
            ORB orb = ORB.create();

            // object
            String source_path = Environment.getExternalStorageDirectory().getPath() + "/DCIM/100ANDRO/source.jpg";
            Mat objectImage = Imgcodecs.imread(source_path, Imgcodecs.CV_LOAD_IMAGE_COLOR);
            Imgproc.cvtColor(objectImage, objectImage, Imgproc.COLOR_RGB2GRAY);
            MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint();
            Mat objectDescriptors = new Mat();
            orb.detect(objectImage, objectKeyPoints);
            orb.compute(objectImage, objectKeyPoints, objectDescriptors);

            // scene
            MatOfKeyPoint sceneKeyPoints = new MatOfKeyPoint();
            Mat sceneDescriptors = new Mat();
            Mat sceneImage = inputFrame.gray();
            orb.detect(sceneImage, sceneKeyPoints);
            orb.compute(sceneImage, sceneKeyPoints, sceneDescriptors);
            Features2d.drawKeypoints(sceneImage, sceneKeyPoints, mRgba, new Scalar(255, 0, 0), 0);

            //DescriptorMatcher descriptorMatcher=DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
            //MatOfDMatch matches = new MatOfDMatch();
            //descriptorMatcher.match(objectDescriptors, sceneDescriptors, matches);

            objectImage.release();
            sceneImage.release();
        }


        return mRgba;
    }

    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            try{
                if (Laplacian_method) {
                    mcalculate = true;
                    handler.postDelayed(this, TIME);
                    //Log.w(TAG, "------\r");
                    cal_result.setText(String.valueOf(lap_number));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    // 取得手機方向
    private int getOrientation(){
        int orientation = Surface.ROTATION_0;

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            Display display = wm.getDefaultDisplay();
            orientation = display.getOrientation();
        }
        return orientation;
/*
        if (orientation == Surface.ROTATION_0) {
            return "portrait";
        }else if (orientation == Surface.ROTATION_90) {
            return "landscape";
        } else if (orientation == Surface.ROTATION_180) {
            return "reverse portrait";
        } else return "reverse landscape";*/
    }
}
