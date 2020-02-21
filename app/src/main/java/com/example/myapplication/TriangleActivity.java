package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.example.myapplication.StorageUtil.getBitmapFromSDCard;
import static com.example.myapplication.StorageUtil.saveMattoBitmapFile;
import static com.example.myapplication.Util.FROM_TRIANGLE_ACTIVITY;
import static com.example.myapplication.Util.getFromWhichActivity;
import static com.example.myapplication.Util.setFromWhichActivity;
import static com.example.myapplication.Util.showToastIns;
import static java.lang.Math.abs;

public class TriangleActivity extends AppCompatActivity {
    private static final String TAG = "Alan";

    private GlobalVariable gv;
    private ImageView file1Image, file2Image, resultImage, proImage1, proImage2;
    private TextView file1text, file2text, file1Detail, file2Detail;
    private TextView finalDetail, removeDetail;

    SharedPreferences mem_Image1, mem_Image2, mem_showProcess;
    private boolean bShowProcess;

    private List<Point> Image1Points = new ArrayList<>();
    private List<Point> Image2Points = new ArrayList<>();
    private List<List<Point>> pointBB = new ArrayList<>();
    private List<Point> RemovedPoints = new ArrayList<>();
    //private List<Point> RemoveImage1Points = new ArrayList<>();

    private int Image_threshold=200;
    private TextView Num_threshold;
    SharedPreferences mem_Image_threshold;
    private SeekBar threshold_Bar;

    private int point_tolerance=5;
    private TextView Num_pointTolerance;
    SharedPreferences mem_Point_Tolerance;
    private SeekBar PointTolerance_SeekBar;

    private int approx_tolerance=50;
    private TextView Num_approxTolerance;
    SharedPreferences mem_approx_Tolerance;
    private SeekBar approxTolerance_SeekBar;

    private int imagecase=0;
    //private final int ONE_TRIANGLE=1;
    //private final int TWO_TRIANGLE=2;
    //private final int TWO_TRIANGLE_OVERLAP=3;
    //private final int TWO_TRIANGLE_MATCH=4;
    private final int SOURCE_IMAGE_ERROR=5;
    //private final int TARGET_IMAGE_ERROR=6;
    private final int PROCESS_IMAGE=7;

    private int addPointCase=0;
    private final int ADD_IMAGE2_BOTTOM=1;
    private final int ADD_IMAGE2_TOP=2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triangle);

        Button file1Btn = findViewById(R.id.select1Button);
        file1Btn.setOnClickListener(BTN);
        Button file2Btn = findViewById(R.id.select2Button);
        file2Btn.setOnClickListener(BTN);
        Button startBtn = findViewById(R.id.startButton);
        startBtn.setOnClickListener(BTN);

        gv = (GlobalVariable)getApplicationContext();

        file1Image=findViewById(R.id.select1image);
        file2Image=findViewById(R.id.select2image);
        resultImage=findViewById(R.id.resultImage);
        proImage1=findViewById(R.id.progressImage);
        proImage2=findViewById(R.id.progressImage2);
        resultImage.setVisibility(View.GONE);
        proImage1.setVisibility(View.GONE);
        proImage2.setVisibility(View.GONE);


        file1text=findViewById(R.id.select1path);
        file2text=findViewById(R.id.select2path);
        file1text.setSelected(true);
        file2text.setSelected(true);

        file1Detail=findViewById(R.id.select1Detail);
        file2Detail=findViewById(R.id.select2Detail);
        finalDetail=findViewById(R.id.finalresultText);
        removeDetail=findViewById(R.id.removeText);
        file1Detail.setText("");
        file2Detail.setText("");
        finalDetail.setText("");
        removeDetail.setText("");
        file1Detail.setMovementMethod(new ScrollingMovementMethod());
        file2Detail.setMovementMethod(new ScrollingMovementMethod());
        finalDetail.setMovementMethod(new ScrollingMovementMethod());
        removeDetail.setMovementMethod(new ScrollingMovementMethod());

        mem_Image1 = getSharedPreferences("IMAGE_SAVE1", MODE_PRIVATE);
        mem_Image2 = getSharedPreferences("IMAGE_SAVE2", MODE_PRIVATE);
        mem_showProcess = getSharedPreferences("SHOW_PROCESS", MODE_PRIVATE);

        mem_Image_threshold = getSharedPreferences("IMAGE_THREHOLD", MODE_PRIVATE);
        threshold_Bar = findViewById(R.id.Threhold_seek);
        Num_threshold=findViewById(R.id.Threhold_Num);

        mem_Point_Tolerance = getSharedPreferences("POINT_TOLERANCE", MODE_PRIVATE);
        PointTolerance_SeekBar = findViewById(R.id.pointTolerance_seek);
        Num_pointTolerance=findViewById(R.id.pointToleranceNum);

        mem_approx_Tolerance = getSharedPreferences("APPROX_TOLERANCE", MODE_PRIVATE);
        approxTolerance_SeekBar = findViewById(R.id.Tolerance_seek);
        Num_approxTolerance=findViewById(R.id.ToleranceNum);

    }

    private SeekBar.OnSeekBarChangeListener thresholdOnSeekBarChange = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            switch(seekBar.getId()){
                case R.id.Threhold_seek:
                    Log.w(TAG, "threshold調至:"+i);
                    Image_threshold=i;
                    //setImageThrehold(Image_threshold);
                    Num_threshold.setText(String.valueOf(Image_threshold));
                    break;

                case R.id.pointTolerance_seek:
                    Log.w(TAG, "point tolerance調至:"+i);
                    point_tolerance=i;
                    //setPointTolerance(point_tolerance);
                    Num_pointTolerance.setText(String.valueOf(point_tolerance));
                    break;

                case R.id.Tolerance_seek:
                    approx_tolerance=i;
                    //setApproxTolerance(approx_tolerance);
                    String aproxString=(approx_tolerance/10)+"."+(approx_tolerance%10);
                    Num_approxTolerance.setText(aproxString);
                    Log.w(TAG, "approx tolerance調至:"+aproxString);
                    break;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            switch(seekBar.getId()){
                case R.id.Threhold_seek:
                    setImageThrehold(Image_threshold);
                    break;
                case R.id.pointTolerance_seek:
                    setPointTolerance(point_tolerance);
                    break;
                case R.id.Tolerance_seek:
                    setApproxTolerance(approx_tolerance);
                    break;
            }
        }
    };

    private int getImageThrehold(){
        return mem_Image_threshold.getInt("IMAGE_THREHOLD", 200);
    }

    private void setImageThrehold(int data){
        SharedPreferences.Editor editor;
        editor = mem_Image_threshold.edit(); //獲取編輯器
        editor.putInt("IMAGE_THREHOLD", data);
        editor.apply();
        editor.commit();    //提交
    }

    private int getPointTolerance(){
        return mem_Point_Tolerance.getInt("POINT_TOLERANCE", 5);
    }

    private void setPointTolerance(int data){
        SharedPreferences.Editor editor;
        editor = mem_Point_Tolerance.edit(); //獲取編輯器
        editor.putInt("POINT_TOLERANCE", data);
        editor.apply();
        editor.commit();    //提交
    }

    private int getApproxTolerance(){
        return mem_approx_Tolerance.getInt("APPROX_TOLERANCE", 50);
    }

    private void setApproxTolerance(int data){
        SharedPreferences.Editor editor;
        editor = mem_approx_Tolerance.edit(); //獲取編輯器
        editor.putInt("APPROX_TOLERANCE", data);
        editor.apply();
        editor.commit();    //提交
    }

    private String getImagePath(int index){
        if (index==1)
            return mem_Image1.getString("IMAGE_SAVE1", "");
        else if (index==2)
            return mem_Image2.getString("IMAGE_SAVE2", "");
        else
            return null;
    }

    private void setImagePath(int index, String path){
        SharedPreferences.Editor editor;
        if (index==1) {
            editor = mem_Image1.edit(); //獲取編輯器
            editor.putString("IMAGE_SAVE1", path);
            editor.apply();
            editor.commit();    //提交
        }else if (index==2){
            editor = mem_Image2.edit(); //獲取編輯器
            editor.putString("IMAGE_SAVE2", path);
            editor.apply();
            editor.commit();    //提交
        }
    }

    private Boolean getShowProcess(){
        return mem_showProcess.getBoolean("SHOW_PROCESS", true);
    }
    private void setShowProcess(Boolean b){
        bShowProcess=b;
        SharedPreferences.Editor editor;
        editor = mem_showProcess.edit(); //獲取編輯器
        editor.putBoolean("SHOW_PROCESS", b);
        editor.apply();
        editor.commit();    //提交
    }


    @Override
    protected void onResume() {
        Log.w(TAG, "TriangleActivity: onResume");
        // 加載OpenCV
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.w(TAG, "TriangleActivity:Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, loaderCallback);
        } else {
            Log.w(TAG, "TriangleActivity:OpenCV library found inside package. Using it!");
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        getFilePath();

        bShowProcess=getShowProcess();

        //Log.w(TAG, "1:"+getImagePath(1));
        //Log.w(TAG, "2:"+getImagePath(2));

        file1text.setText(getImagePath(1));
        file2text.setText(getImagePath(2));

        Bitmap bmp;
        if (getImagePath(1)==null){
            file1Image.setVisibility(View.INVISIBLE);
        }else{
            bmp =BitmapFactory.decodeFile(getImagePath(1));
            file1Image.setImageBitmap(bmp);
            file1Image.setVisibility(View.VISIBLE);
        }

        if (getImagePath(2)==null){
            file2Image.setVisibility(View.INVISIBLE);
        }else{
            bmp =BitmapFactory.decodeFile(getImagePath(2));
            file2Image.setImageBitmap(bmp);
            file2Image.setVisibility(View.VISIBLE);
        }

        Image_threshold = getImageThrehold();
        threshold_Bar.setMax(255);
        threshold_Bar.setProgress(Image_threshold);
        threshold_Bar.setOnSeekBarChangeListener(thresholdOnSeekBarChange);
        Num_threshold.setText(String.valueOf(Image_threshold));

        point_tolerance = getPointTolerance();
        PointTolerance_SeekBar.setMax(20);
        PointTolerance_SeekBar.setProgress(point_tolerance);
        PointTolerance_SeekBar.setOnSeekBarChangeListener(thresholdOnSeekBarChange);
        Num_pointTolerance.setText(String.valueOf(point_tolerance));

        approx_tolerance = getApproxTolerance();
        approxTolerance_SeekBar.setMax(200);
        approxTolerance_SeekBar.setProgress(approx_tolerance);
        approxTolerance_SeekBar.setOnSeekBarChangeListener(thresholdOnSeekBarChange);
        String aproxString=(approx_tolerance/10)+"."+(approx_tolerance%10);
        Num_approxTolerance.setText(aproxString);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 如果是由FileManagerActivity回來這，按back又會至FileManagerActivity,
            // 為避免這個情況，強制回至MainActivity，並且設定FLAG_ACTIVITY_CLEAR_TOP
            Log.w(TAG, "press back");
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.w(TAG, "TriangleActivity: onStop");
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.w(TAG, "TriangleActivity: onPause");
    }

    //-----------------------------------------------------
    // OpenCV loader callback
    //-----------------------------------------------------
    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == BaseLoaderCallback.SUCCESS) {
                showToastIns(getApplicationContext(), "OpenCV loaded successfully", Toast.LENGTH_SHORT);
            } else {
                super.onManagerConnected(status);
                showToastIns(getApplicationContext(), "OpenCV loaded fail", Toast.LENGTH_SHORT);
            }
        }
    };

    private View.OnClickListener BTN = new View.OnClickListener() {

        Intent intent = new Intent();

        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.select1Button:
                    gv.setTriangle_Target(1);
                    setFromWhichActivity(FROM_TRIANGLE_ACTIVITY);
                    intent=new Intent(TriangleActivity.this, FileManagerActivity.class);
                    startActivity(intent);
                    break;
                case R.id.select2Button:
                    gv.setTriangle_Target(2);
                    setFromWhichActivity(FROM_TRIANGLE_ACTIVITY);
                    intent=new Intent(TriangleActivity.this, FileManagerActivity.class);
                    startActivity(intent);
                    break;
                case R.id.startButton:
                    Image1Points.clear();
                    Image2Points.clear();
                    pointBB.clear();
                    imagecase=0;

                    int result = getImagePoints(1, BitmapFactory.decodeFile(getImagePath(1)));

                    if (result != SOURCE_IMAGE_ERROR) {
                        pointBB.clear();
                        getImagePoints(2, BitmapFactory.decodeFile(getImagePath(2)));

                        //Log.w(TAG, "2 result="+result);
                        //DrawFinalBitmap();
                    }else{
                        imagecase=SOURCE_IMAGE_ERROR;
                    }
                    DrawFinalBitmap();
                    DrawProcessBitmap(bShowProcess);
                    break;
            }
        }
    };

    private void getFilePath(){
        //Log.w(TAG, "getFilePath:"+getFromWhichActivity());
        //Log.w(TAG, "target:"+gv.getTriangle_Target());

        if (getFromWhichActivity()==FROM_TRIANGLE_ACTIVITY){
            Bundle bundle;
            bundle = this.getIntent().getExtras();

            if (gv.getTriangle_Target()==1 || gv.getTriangle_Target()==2) {
                if (bundle != null) {
                    setImagePath(gv.getTriangle_Target(), bundle.getString("FILEPATH"));
                    Log.w(TAG, "index="+gv.getTriangle_Target()+":"+bundle.getString("FILEPATH"));
                }else{
                    Log.w(TAG, "null");
                }
            }
        }
    }

    // 畫出比較完後的圖片
    private void DrawFinalBitmap(){
        Point newPoint = new Point();

        StringBuffer resultTxt = new StringBuffer();
        StringBuffer resultTxt1 = new StringBuffer();
        Bitmap resultBitmap =BitmapFactory.decodeFile(getImagePath(2));
        Mat rgbMat = new Mat();
        Utils.bitmapToMat(resultBitmap, rgbMat);

        Log.w(TAG, "imagecase:"+imagecase);
        RemovedPoints.clear();
        //RemoveImage1Points.clear();

        switch(imagecase){
            /*
            case TWO_TRIANGLE_MATCH:    // 完全重合
                Imgproc.circle(rgbMat, Image2Points.get(0), 15, new Scalar(0, 0, 255, 255), -1);
                Imgproc.putText(rgbMat, String.valueOf(Image2Points.get(0)), new Point(Image2Points.get(0).x + 20, Image2Points.get(0).y),
                        Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 255, 255), 2);
                Utils.matToBitmap(rgbMat, resultBitmap);
                resultImage.setImageBitmap(resultBitmap);
                resultImage.setVisibility(View.VISIBLE);

                resultTxt.append("兩個圖形重合\n");
                resultTxt.append(Image2Points.get(0));
                finalDetail.setText(resultTxt);
                break;
             */

            //case ONE_TRIANGLE:
            //    Log.w(TAG, "圖二中的三角形不是正確的");
            //    break;
/*
            case TWO_TRIANGLE: // 三角形分開
                Imgproc.circle(rgbMat, Image2Points.get(0), 15, new Scalar(0, 0, 255, 255), -1);
                //Imgproc.putText(rgbMat, "1:"+String.valueOf(Image2Points.get(0)), new Point(5,40),
                        //Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 255, 255), 2);
                Imgproc.putText(rgbMat, String.valueOf(1), new Point(Image2Points.get(0).x + 20, Image2Points.get(0).y),
                        Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 255, 255), 2);
                Imgproc.circle(rgbMat, Image2Points.get(3), 15, new Scalar(0, 0, 255, 255), -1);
                //Imgproc.putText(rgbMat, "2:"+String.valueOf(Image2Points.get(3)), new Point(5,80),
                        //Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 255, 255), 2);
                Imgproc.putText(rgbMat, String.valueOf(2), new Point(Image2Points.get(3).x + 20, Image2Points.get(3).y),
                        Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 255, 255), 2);

                Utils.matToBitmap(rgbMat, resultBitmap);
                resultImage.setImageBitmap(resultBitmap);
                resultImage.setVisibility(View.VISIBLE);

                resultTxt.append("兩個圖形最高點座標\n");
                resultTxt.append(Image2Points.get(0));
                resultTxt.append("\n");
                resultTxt.append(Image2Points.get(3));
                finalDetail.setText(resultTxt);
                break;
 */

            case PROCESS_IMAGE:
            //case TWO_TRIANGLE_OVERLAP: // 三角形重疊
                int removeCount=0;

                Log.w(TAG, "PROCESS_IMAGE");
                //int beforeRemovePointNum = Image2Points.size();
                boolean bDrawText=false;
                Log.w(TAG, "image1 size:"+Image1Points.size());
                Log.w(TAG, "image2 size:"+Image2Points.size());

                for (int i=0; i<Image2Points.size(); i++){
                    for (int j=0; j<Image1Points.size(); j++){
                        // 完全相同的比較
                        /*
                        if (Image2Points.get(i).equals(Image1Points.get(j))){
                            Image2Points.remove(Image2Points.get(i));
                            Log.w(TAG, "remove:"+i);
                            removeCount++;
                        }*/

                        //Log.w(TAG, "比較:"+Image2Points.get(i)+"和"+Image1Points.get(j));

                        // 有tolerance的比較
                        if (Check2PointSame(Image2Points.get(i), Image1Points.get(j)))
                        {
                            Log.w(TAG, "remove:"+i+"="+Image2Points.get(i));
                            RemovedPoints.add(Image2Points.get(i)); // 儲存移除的點座標
                            //RemoveImage1Points.add(Image1Points.get(j));
                            removeCount++;
                        }
                    }
                }

                // 將重覆的點移除
                for (int i=0; i<RemovedPoints.size(); i++) {
                    Image2Points.remove(RemovedPoints.get(i));
                }

                Log.w(TAG, "removeCount:"+removeCount);
                Log.w(TAG, "after remove duplicate points:"+Image2Points);

                // 移除的點
                for (int i=0; i<removeCount; i++) {
                    Imgproc.circle(rgbMat, RemovedPoints.get(i), 10, new Scalar(0, 255, 0, 255), -1);
                }
/*
                // 尋找重要點被移除條件
                if (Image2Points.size()!=0) {
                    double diffX, diffY;
                    int img1_index = Image1Points.size()-1;
                    int img2_index = Image2Points.size()-1;

                    for (int i = 0; i < removeCount; i++) {
                        if (Check2PointSame(RemovedPoints.get(i), Image1Points.get(0))) {

                            diffX = Image1Points.get(img1_index).x - Image2Points.get(img2_index).x;
                            diffY = Image1Points.get(img1_index).y - Image2Points.get(img2_index).y;
                            newPoint.x = Image1Points.get(0).x - diffX;
                            newPoint.y = Image1Points.get(0).y - diffY;
                            Log.w(TAG, "1. new point:" + newPoint);
                            addPointCase = ADD_IMAGE2_TOP;
                        }

                        if (Check2PointSame(RemovedPoints.get(i), Image1Points.get(img1_index))) {

                            diffX = Image1Points.get(0).x - Image2Points.get(0).x;
                            diffY = Image1Points.get(0).y - Image2Points.get(0).y;
                            newPoint.x = Image1Points.get(img1_index).x - diffX;
                            newPoint.y = Image1Points.get(img1_index).y - diffY;
                            Log.w(TAG, "2. new point:" + newPoint);
                            addPointCase = ADD_IMAGE2_BOTTOM;
                        }
                    }
                }
 */

                if (removeCount >= 3) { // 正確重合的話，會有3個點以上被移除
                    int img1low = Image1Points.size()-1;
                    int img2low = Image2Points.size()-1;

                    // 圖一的最高點
                    Imgproc.circle(rgbMat, Image1Points.get(0), 5, new Scalar(0, 0, 255, 255), -1);
                    Imgproc.putText(rgbMat, "1H", new Point(Image1Points.get(0).x + 20, Image1Points.get(0).y),
                            Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255, 255), 2);

                    // 圖一的最低點座標
                    Imgproc.circle(rgbMat, Image1Points.get(img1low), 5, new Scalar(0, 0, 255, 255), -1);
                    Imgproc.putText(rgbMat, "1L", new Point(Image1Points.get(img1low).x + 20, Image1Points.get(img1low).y),
                            Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255, 255), 2);

                    if (Image2Points.size()==0) {    // 圖二的4個點都被移除了
                        Log.w(TAG, "完全重合");
                    }else {

                        // 尋找重要點被移除條件
                        double diffX, diffY;
                        for (int i = 0; i < removeCount; i++) {
                            if (Check2PointSame(RemovedPoints.get(i), Image1Points.get(0))) {

                                diffX = Image1Points.get(img1low).x - Image2Points.get(img2low).x;
                                diffY = Image1Points.get(img1low).y - Image2Points.get(img2low).y;
                                newPoint.x = Image1Points.get(0).x - diffX;
                                newPoint.y = Image1Points.get(0).y - diffY;
                                Log.w(TAG, "1. new point:" + newPoint);
                                addPointCase = ADD_IMAGE2_TOP;
                            }

                            if (Check2PointSame(RemovedPoints.get(i), Image1Points.get(img1low))) {

                                diffX = Image1Points.get(0).x - Image2Points.get(0).x;
                                diffY = Image1Points.get(0).y - Image2Points.get(0).y;
                                newPoint.x = Image1Points.get(img1low).x - diffX;
                                newPoint.y = Image1Points.get(img1low).y - diffY;
                                Log.w(TAG, "2. new point:" + newPoint);
                                addPointCase = ADD_IMAGE2_BOTTOM;
                            }
                        }


                        for (int i = 0; i < Image2Points.size(); i++) {
                            // 剩下點裡的最高點
                            if (i == 0) {
                                if (addPointCase==ADD_IMAGE2_TOP){
                                    Imgproc.circle(rgbMat, newPoint, 5, new Scalar(255, 200, 0, 255), -1);
                                    Imgproc.putText(rgbMat, "2H", new Point(newPoint.x + 20, newPoint.y),
                                            Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255, 255), 2);
                                }else {
                                    Imgproc.circle(rgbMat, Image2Points.get(i), 5, new Scalar(0, 0, 255, 255), -1);
                                    Imgproc.putText(rgbMat, "2H", new Point(Image2Points.get(i).x + 20, Image2Points.get(i).y),
                                            Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255, 255), 2);
                                }
                            }
                            else if (i==img2low){  // 最低點
                                if (addPointCase == ADD_IMAGE2_BOTTOM){
                                    Imgproc.circle(rgbMat, newPoint, 5, new Scalar(255, 200, 0, 255), -1);
                                    Imgproc.putText(rgbMat, "2L", new Point(newPoint.x + 20, newPoint.y),
                                            Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255, 255), 2);
                                }else {
                                    Imgproc.circle(rgbMat, Image2Points.get(i), 5, new Scalar(0, 0, 255, 255), -1);
                                    Imgproc.putText(rgbMat, "2L", new Point(Image2Points.get(i).x + 20, Image2Points.get(i).y),
                                            Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 255, 255), 2);
                                }
                            }
                            else  // 其他點
                                Imgproc.circle(rgbMat, Image2Points.get(i), 5, new Scalar(255, 0, 0, 255), -1);
                        }
                        bDrawText=true;
                    }

                    Utils.matToBitmap(rgbMat, resultBitmap);
                    resultImage.setImageBitmap(resultBitmap);
                    resultImage.setVisibility(View.VISIBLE);

                    resultTxt.append("兩個圖形最高點座標\n");
                    resultTxt.append("1H=");
                    resultTxt.append(Image1Points.get(0));
                    resultTxt.append("\n");
                    if (bDrawText) {
                        resultTxt.append("2H=");
                        if (addPointCase==ADD_IMAGE2_TOP)
                            resultTxt.append(newPoint);
                        else
                            resultTxt.append(Image2Points.get(0));
                        resultTxt.append("\n");
                    }

                    resultTxt.append("兩個圖形最低點座標\n");
                    resultTxt.append("1L=");
                    resultTxt.append(Image1Points.get(img1low));
                    resultTxt.append("\n");
                    if (bDrawText) {
                        resultTxt.append("2L=");
                        if (addPointCase == ADD_IMAGE2_BOTTOM)
                            resultTxt.append(newPoint);
                        else
                            resultTxt.append(Image2Points.get(img2low));
                    }

                    finalDetail.setText(resultTxt);

                    resultTxt1.append("移除的點座標");
                    for (int i=0; i<removeCount; i++){
                        resultTxt1.append("\n");
                        resultTxt1.append(RemovedPoints.get(i));
                    }
                    removeDetail.setText(resultTxt1);

                }else {
                    showToastIns(getApplicationContext(), "目標影像錯誤", Toast.LENGTH_SHORT);
                    resultImage.setVisibility(View.GONE);
                    proImage1.setVisibility(View.GONE);
                    proImage2.setVisibility(View.GONE);
                    finalDetail.setText("");
                    removeDetail.setText("");
                }
                break;

            case SOURCE_IMAGE_ERROR:
                showToastIns(getApplicationContext(), "來源影像錯誤", Toast.LENGTH_SHORT);
                file2Detail.setText("圖一錯誤，不做處理");
                resultImage.setVisibility(View.GONE);
                proImage1.setVisibility(View.GONE);
                proImage2.setVisibility(View.GONE);
                finalDetail.setText("");
                removeDetail.setText("");
                break;
/*
            case TARGET_IMAGE_ERROR:
                showToastIns(getApplicationContext(), "目標影像錯誤", Toast.LENGTH_SHORT);
                resultImage.setVisibility(View.GONE);
                proImage1.setVisibility(View.GONE);
                proImage2.setVisibility(View.GONE);
                finalDetail.setText("");
                removeDetail.setText("");
                break;
 */

            default:
                break;
        }
    }

    //-----------------------------------------------------------------------
    //    先由 y 開始，由小到大排列，相同的 y 就以 x 由小到大排列
    //-----------------------------------------------------------------------
    private List<Point> SortPoints(List<Point> inputPoint) {
        List<Point> afterSort = new ArrayList<>();
        List<pointCompare> pp = new ArrayList<>();

        int num=inputPoint.size();
        for (int i=0; i<num; i++){
            pp.add(new pointCompare(inputPoint.get(i).x, inputPoint.get(i).y));
        }

        Collections.sort(pp, new Comparator<pointCompare>(){
            public int compare( pointCompare l1, pointCompare l2 )
            {
                int data = Double.compare(l1.pointY, l2.pointY);
                if (data==0)
                    return Double.compare(l1.pointX, l2.pointX);
                else
                    return data;
            }
        });
        afterSort.clear();
        for (int i=0; i<num; i++){
            afterSort.add(new Point(pp.get(i).pointX, pp.get(i).pointY));
        }
        return afterSort;
    }

    // 找圖片中的所有輪廓
    private int getImagePoints(int index, Bitmap resultBitmap){
        Log.w(TAG, "Start find image points.");

        int final_num=0;
        int total3=0;
        //int total7=0;
        StringBuffer sb = new StringBuffer();

        Mat rgbMat = new Mat();
        Utils.bitmapToMat(resultBitmap, rgbMat);

        Mat grayMat = new Mat(resultBitmap.getHeight(), resultBitmap.getWidth(),CvType.CV_8U, new Scalar(1));

        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY, 2);
        Imgproc.threshold(grayMat, grayMat, Image_threshold, 255, Imgproc.THRESH_BINARY);// 大於thresh的都設定為max，小於的設定0

        // 儲存處理後的圖一及圖二
        String savepath = Environment.getExternalStorageDirectory() + "/DCIM/";
        if (index ==1) {
            try {
                saveMattoBitmapFile(grayMat, "progress1.jpg", savepath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if (index ==2) {
            try {
                saveMattoBitmapFile(grayMat, "progress2.jpg", savepath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Core.bitwise_not(grayMat, grayMat);   // 反色 --- 適用於白底黑圖

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(grayMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        Log.w(TAG, "found="+contours.size());
        MatOfPoint temp_contour;

        for(int idx=0; idx<contours.size(); idx++)  // 找到的物件數
        {
            temp_contour=contours.get(idx);
            MatOfPoint2f new_mat=new MatOfPoint2f(temp_contour.toArray());
            MatOfPoint2f approxCurve_temp=new MatOfPoint2f();

            // 對圖像輪廓點進行多邊形擬合
            // int contourSize= (int) temp_contour.total();
            // epsilon = 原始曲线与近似曲线之间的最大距离
            // closed = 曲线是闭合的
            //Imgproc.approxPolyDP(new_mat,approxCurve_temp,contourSize*0.03,true);
            Imgproc.approxPolyDP(new_mat,approxCurve_temp,approx_tolerance/10,true);
            Log.w(TAG, "邊:"+approxCurve_temp.total());

            //Log.w(TAG, "final_num="+final_num);

            if (approxCurve_temp.total()>=4){// && approxCurve_temp.total()<=7) {  // 找到三邊形或七邊形

                // 在物件中間畫出數字index
                DrawNumber(rgbMat, temp_contour, final_num, new Scalar(255,0,255, 255));
                //Log.w(TAG, "final_num="+final_num);
                final_num++;

                int pointnum = (int)approxCurve_temp.total();

                // 畫外框
                //Imgproc.drawContours(rgbMat, contours, idx, new Scalar(0, 255, 0, 255), 2);

                pointBB.add(DrawPoint(pointnum, rgbMat, approxCurve_temp));

                if (pointnum==4)
                    total3++;
                //else
                //    total7++;
            }
        }

        for (int i=0; i<pointBB.size(); i++){
            Log.w(TAG, "pointBB="+i+"--"+pointBB.get(i));
        }

        // 判斷圖一
        if (index==1){
            if (total3==1 && pointBB.size()==1){
                Image1Points = pointBB.get(0);
                sb.append("找到1個四邊形");
                for (int i = 0; i < pointBB.get(0).size(); i++) {
                    sb.append("\n");
                    sb.append(pointBB.get(0).get(i));
                }
            }else{
                imagecase = SOURCE_IMAGE_ERROR;
                sb.append("找不到正確圖形");
            }
        }else if (index==2){
            // 將圖二所有座標存入Image2Points
            for (int i=0; i<pointBB.size(); i++){
                Image2Points.addAll(pointBB.get(i));
            }
            sb.append("所有座標");

            for (int i=0; i<pointBB.size(); i++){
                for (int j=0; j<pointBB.get(i).size(); j++){
                    sb.append("\n");
                    sb.append(pointBB.get(i).get(j));
                }
                sb.append("\n");
            }
            imagecase = PROCESS_IMAGE;
        }else{
            sb.append("錯誤判斷");
        }
        // 判斷圖二
        /*
        else if (index==2){
            // 一個七邊形
            if (total3==0 && total7==1){
                imagecase = TWO_TRIANGLE_OVERLAP;

                // 將圖二所有座標存入Image2Points
                Image2Points = pointBB.get(0);

                // 列出文字
                sb.append("找到1個多邊形");
                for (int i=0; i<pointBB.get(0).size(); i++) {
                    sb.append("\n");
                    sb.append(pointBB.get(0).get(i));
                }
            }
            // 一個三角形
            else if (total3==1 && total7==0){
                // 將圖二的一個三角形座標存入Image2Points
                Image2Points = pointBB.get(0);

                Log.w(TAG, "1="+Image1Points);
                Log.w(TAG, "2="+Image2Points);

                // 比對兩張圖的三角形是否重合
                //if (Image2Points.equals(Image1Points))    // 完全相同
                // 有tolerance
                for (int i=0; i<Image2Points.size(); i++) {
                    if (abs(Image2Points.get(i).x - Image1Points.get(i).x) < point_tolerance &&
                            abs(Image2Points.get(i).y - Image1Points.get(i).y) < point_tolerance) {
                        imagecase = TWO_TRIANGLE_MATCH;
                    }
                    else
                        imagecase = TARGET_IMAGE_ERROR;
                }

                // 列出文字
                sb.append("找到1個三角形，沒有多邊形");
                for (int j=0; j<pointBB.get(0).size(); j++){
                    sb.append("\n");
                    sb.append(pointBB.get(0).get(j));
                }
            }
            // 二個三角形
            else if ((total3>=2 && total7==0) || (total3==1 && total7>=1)){

                // 將兩個三角形座標存入
                for (int i=0; i<pointBB.size(); i++){
                    Image2Points.addAll(pointBB.get(i));
                }

                boolean bFindMatch=false;

                // 比對圖二中的三角形是否有符合圖一的三角形
                for (int i =0; i<pointBB.size(); i++){
                    if (pointBB.get(i).equals(Image1Points)){
                        Log.w(TAG, "第"+i+"個三角形相同");
                        //imagecase = TWO_TRIANGLE;
                        imagecase = TWO_TRIANGLE_OVERLAP;
                        bFindMatch=true;
                    }else{
                        if (!bFindMatch) {
                            Log.w(TAG, i+"沒有符合");
                            //imagecase = TARGET_IMAGE_ERROR;
                        }
                    }
                }

                // 列出文字
                if (total3>=2 && total7==0) {
                    sb.append("找到2個三角形");
                    for (int i = 0; i < total3; i++) {
                        sb.append("\n");
                        for (int j = 0; j < pointBB.get(i).size(); j++) {
                            sb.append(pointBB.get(i).get(j));
                            sb.append("\n");
                        }
                    }
                }else if (total3==1 && total7>=1){
                    sb.append("找到1個三角形，1個多邊形\n");
                    for (int j = 0; j < Image2Points.size(); j++) {
                        sb.append(Image2Points.get(j));
                        sb.append("\n");
                    }
                }
            }
            //else if (total3==1 && total7==1){  // 一個三角，一個多邊
            //    sb.append("所有點\n");
            //    for (int i=0; i<pointBB.size(); i++){
            //        Image2Points.addAll(pointBB.get(i));
            //    }
            //    for (int j = 0; j < Image2Points.size(); j++) {
            //        sb.append(Image2Points.get(j));
            //        sb.append("\n");
            //    }
            //    imagecase = TWO_TRIANGLE;
            //}


            else{
                imagecase = TARGET_IMAGE_ERROR;
                sb.append("找不到正確圖形\n");
                sb.append("三角形有");
                sb.append(total3);
                sb.append("個\n七邊形有");
                sb.append(total7);
                sb.append("個");
            }
        }*/

        // 各個物件的高/低座標
        for (int i=0; i<final_num; i++) {
            // 高點的座標
            Imgproc.putText(rgbMat, String.valueOf(pointBB.get(i).get(0)), new Point(pointBB.get(i).get(0).x + 20, pointBB.get(i).get(0).y),
                    Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 255, 255), 2);

            // 低點的座標
            //Log.w(TAG, "size="+pointBB.get(i).size());
            int lowpoint=pointBB.get(i).size()-1;
            Imgproc.putText(rgbMat, String.valueOf(pointBB.get(i).get(lowpoint)), new Point(pointBB.get(i).get(lowpoint).x + 20, pointBB.get(i).get(lowpoint).y),
                    Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 255, 255), 2);

        }


        // 更新圖一及圖二的結果，並且更新文字敘述
        Bitmap newBitMap = Bitmap.createBitmap(rgbMat.width(),rgbMat.height(),Bitmap.Config.ARGB_8888);
        if (index==1) {
            file1Detail.setText(sb);
            Utils.matToBitmap(rgbMat, newBitMap);
            file1Image.setImageBitmap(newBitMap);
        }
        else if (index==2) {
            file2Detail.setText(sb);
            Utils.matToBitmap(rgbMat, newBitMap);
            file2Image.setImageBitmap(newBitMap);
        }

        Log.w(TAG, "Image1Points:"+Image1Points);
        Log.w(TAG, "Image2Points:"+Image2Points);

        return imagecase;
    }

    // 在找到輪廓的中間畫面index數字
    private void DrawNumber(Mat mat, MatOfPoint mp, int index, Scalar color){
        int fontface = Core.FONT_HERSHEY_SIMPLEX;
        double scale = 1;
        int thickness = 3;
        int[] baseline = new int[1];
        String label=Integer.toString(index);
        Size text = Imgproc.getTextSize(label, fontface, scale, thickness, baseline);
        Rect r = Imgproc.boundingRect(mp);
        Point pt = new Point(r.x + ((r.width - text.width) / 2),r.y + ((r.height + text.height) / 2));
        Imgproc.putText(mat, label, pt, fontface, scale, color, thickness);
    }

    // 找出此輪廓的所有點，並且標示出來
    private List<Point> DrawPoint(int num, Mat rgbMat, MatOfPoint2f approxCurve) {
        double[] temp_double;
        List<Point> threePoint = new ArrayList<>();

        for (int i=0; i<num; i++){
            temp_double = approxCurve.get(i, 0);
            threePoint.add(new Point(temp_double[0], temp_double[1]));
        }
        Log.w(TAG, num+"個邊--各點座標為:"+threePoint);

        //if (draw){
            for (int i=0; i<num; i++){
                Imgproc.circle(rgbMat, threePoint.get(i), 15, new Scalar(255, 0, 0, 255), -1);
            }
        //}
        return SortPoints(threePoint);
    }

    public class pointCompare{

        private double pointX;
        private double pointY;

        pointCompare(double x, double y) {
            pointX = x;
            pointY = y;
        }
/*
        protected double getPointX() {
            return pointX;
        }

        protected double getPointY() {
            return pointY;
        }
*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        //Log.w(TAG, "onCreateOptionsMenu...");
        getMenuInflater().inflate(R.menu.menu_triangle, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //Log.w(TAG, "onPrepareOptionsMenu...");
        // 設定option menu裡面的check item
        menu.findItem(R.id.ShowProcessItem).setChecked(getShowProcess());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.PriviewItem) {
            Intent OptionIntent = new Intent(TriangleActivity.this,Tutorial1Activity.class);
            startActivity(OptionIntent);
        }else if (id==R.id.ShowProcessItem){
            if (item.isChecked()) {
                item.setChecked(false);
                setShowProcess(false);
            }else{
                item.setChecked(true);
                setShowProcess(true);
            }
        }
        return super.onOptionsItemSelected(item);
    }
/*
    private void saveMattoBitmapFile(Mat mat, String fileName, String path) throws IOException {

        Bitmap output = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, output);

        String subForder = path;
        File foder = new File(subForder);
        if (!foder.exists()) {
            foder.mkdirs();
        }
        File myCaptureFile = new File(subForder, fileName);
        if (!myCaptureFile.exists()) {
            myCaptureFile.createNewFile();
        }
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        output.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        bos.flush();
        bos.close();
    }

 */

    void DrawProcessBitmap(Boolean bDraw){
        //Log.w(TAG, "bDraw="+bDraw);
        if (bDraw) {
            proImage1.setImageBitmap(getBitmapFromSDCard("DCIM/progress1.jpg"));
            proImage1.setVisibility(View.VISIBLE);
            proImage2.setImageBitmap(getBitmapFromSDCard("DCIM/progress2.jpg"));
            proImage2.setVisibility(View.VISIBLE);
        }else{
            proImage1.setVisibility(View.GONE);
            proImage2.setVisibility(View.GONE);
        }
    }

    private boolean Check2PointSame(Point p1, Point p2){
        return (abs(p1.x - p2.x) < point_tolerance &&
                abs(p1.y - p2.y) < point_tolerance);
    }
/*
    private static Bitmap getBitmapFromSDCard(String file)
    {
        try{
            String sd = Environment.getExternalStorageDirectory().toString();
            //Log.w(TAG, "save path:"+sd + "/" + file);
            Bitmap bitmap = BitmapFactory.decodeFile(sd + "/" + file);
            return bitmap;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

 */
}
