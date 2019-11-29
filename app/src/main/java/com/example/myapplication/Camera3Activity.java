package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import static com.example.myapplication.StorageUtil.checkSDCard;
import static com.example.myapplication.StorageUtil.getPhotoDefaultName;
import static com.example.myapplication.StorageUtil.imageFilter;
import static com.example.myapplication.Util.showToastIns;

public class Camera3Activity extends AppCompatActivity {
    private static final String TAG = "Alan";

    private static final SparseIntArray ORIENTATION = new SparseIntArray();
    static {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    private String mCameraId;
    private Size mPreviewSize;
    private Size mCaptureSize;
    private Handler mCameraHandler;
    private CameraDevice mCameraDevice;
    private TextureView mTextureView;
    private ImageReader mImageReader;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private CaptureRequest mCaptureRequest;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraManager manager;
    private Button CameraSWBtn;
    private static String savedPath;
    private ImageView lastImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //全屏無狀態欄
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_camera3);
        mTextureView = findViewById(R.id.textureView);
        mTextureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        mTextureView.setOnTouchListener(TouchEvent);

        // 按鈕
        CameraSWBtn = findViewById(R.id.cameraswitch);
        CameraSWBtn.setOnClickListener(BtnEvent);
        Button takePicBtn = findViewById(R.id.photoButton);
        takePicBtn.setOnClickListener(BtnEvent);

        //lastImage = new ImageView(this);
        lastImage = findViewById(R.id.camera3Cap);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 將status / navi bar透明化
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        //ActionBar actionBar = getSupportActionBar();
        //actionBar.hide();


        startCameraThread();

        if (!mTextureView.isAvailable()) {
            mTextureView.setSurfaceTextureListener(mTextureListener);
        } else {
            startPreview();
        }
        //HideImageView();
        UpdateImageView();    // 顯示最後一次拍的照片
    }

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
    protected void onPause() {
        super.onPause();
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }

        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    private View.OnClickListener BtnEvent= new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.photoButton:
                    takePicture(view);
                    break;
                case R.id.cameraswitch:
                    switchCamera();
                    break;
            }
        }
    };

    private View.OnTouchListener TouchEvent = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int fingerX, fingerY;
            //int length = (int) (getResources().getDisplayMetrics().density * 80);

            if (motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                fingerX = (int) motionEvent.getX();
                fingerY = (int) motionEvent.getY();
                Log.w(TAG, "onTouch: x->" + fingerX + ",y->" + fingerY);
            }

            return true;
        }
    };


    private void startCameraThread() {
        HandlerThread mCameraThread = new HandlerThread("CameraThread");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());
    }

    private TextureView.SurfaceTextureListener mTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

            Log.w(TAG, "onSurfaceTextureAvailable: width"+width+",height:"+height);
            transformImage(width, height);  // 翻轉相機，重新計算textureView的長寬比

            //當SurefaceTexture可用的时候，設置相機參數
            setupCamera(width, height);
            //打開相機
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.w(TAG, "size change:"+width+","+height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.w(TAG, "onSurfaceTextureDestroyed");
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };


    private void setupCamera(int width, int height) {
        //Log.w(TAG, "setupCamera:width:"+width+",height:"+height);
        int Camera_Obj;

        //获取摄像头的管理者CameraManager
        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            //遍历所有摄像头
            for (String cameraId : manager.getCameraIdList()) {
                Log.w(TAG, "相機數量:"+manager.getCameraIdList().length);

                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                //獲取相機類型
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

                if (facing != null && facing.equals(CameraCharacteristics.LENS_FACING_EXTERNAL))
                    Log.w(TAG, "we have external camera.");

                //預設打開後相機
                Camera_Obj = CameraCharacteristics.LENS_FACING_FRONT;
                CameraSWBtn.setText(R.string.camera_back);  // 後鏡頭

                if (facing != null && facing == Camera_Obj) // 前相機
                    continue;

                //获取StreamConfigurationMap，它是管理摄像头支持的所有输出格式和尺寸
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                assert map != null;

                //根据TextureView的尺寸设置预览尺寸
                mPreviewSize = getOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height);

                assert mPreviewSize != null;
                Log.w(TAG, "preview:"+mPreviewSize.getHeight()+","+mPreviewSize.getWidth());

                //获取相机支持的最大拍照尺寸
                mCaptureSize = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new Comparator<Size>() {
                    @Override
                    public int compare(Size lhs, Size rhs) {
                        return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getHeight() * rhs.getWidth());
                    }
                });
                //此ImageReader用于拍照所需
                setupImageReader();
                mCameraId = cameraId;
                Log.w(TAG, "mCameraId:"+mCameraId);
                showToastIns(getApplicationContext(), "使用相機"+mCameraId, Toast.LENGTH_LONG);
                break;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    //在sizeMap裡找最符合比例的preview大小
    private Size getOptimalSize(Size[] sizeMap, int width, int height) {
        //List<Size> sizeList = new ArrayList<>();

        final double ASPECT_TOLERANCE = 0.2;
        Configuration configuration=getResources().getConfiguration();
        double targetRatio;

        if (configuration.orientation== Configuration.ORIENTATION_LANDSCAPE) {
            targetRatio = (double) width / height;
        }
        else {
            targetRatio = (double) height / width;
        }

        if (sizeMap == null)
            return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = height;

        //Log.w(TAG, "targetRatio="+targetRatio);

        for (Size option : sizeMap) {
            double ratio = (double) option.getWidth() / option.getHeight();
            //Log.w(TAG, "ratio="+ratio);

            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(option.getHeight() - targetHeight) < minDiff)
            {
                optimalSize = option;
                minDiff = Math.abs(option.getHeight() - targetHeight);
            }
        }

        if (optimalSize == null)
        {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizeMap) {
                if (Math.abs(size.getHeight() - targetHeight) < minDiff)
                {
                    optimalSize = size;
                    minDiff = Math.abs(size.getHeight() - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    private void openCamera() {
        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.openCamera(mCameraId, mStateCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.e(TAG, "onError: " + error);
            camera.close();
            mCameraDevice = null;
        }
    };

    private void startPreview() {
        SurfaceTexture mSurfaceTexture = mTextureView.getSurfaceTexture();
        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        //Log.w(TAG, "startPreview:"+mPreviewSize.getWidth()+","+mPreviewSize.getHeight());

        Surface previewSurface = new Surface(mSurfaceTexture);

        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            mCaptureRequestBuilder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        mCaptureRequest = mCaptureRequestBuilder.build();
                        mCameraCaptureSession = session;
                        mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //-----------------------------------------------------------------------------------------
    // 按下按鈕，拍攝照片
    //-----------------------------------------------------------------------------------------
    public void takePicture(View view) {
        lockFocus();
    }

    private void lockFocus() {
        try {
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            mCameraCaptureSession.capture(mCaptureRequestBuilder.build(), mCaptureCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            Log.w(TAG, "onCaptureProgressed");
            //mManualFocusEngaged=false;
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            Log.w(TAG, "onCaptureCompleted");
            capture();
            //UpdateImageView();
        }

    };

    private void capture() {
        try {
            final CaptureRequest.Builder mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            mCaptureBuilder.addTarget(mImageReader.getSurface());
            mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATION.get(rotation));
            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    Toast.makeText(getApplicationContext(), "Picture Saved:"+"\n"+savedPath, Toast.LENGTH_SHORT).show();
                    unLockFocus();
                }
            };
            mCameraCaptureSession.stopRepeating();  // 停止連續取景
            mCameraCaptureSession.capture(mCaptureBuilder.build(), CaptureCallback, null);  // 取得圖片
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unLockFocus() {
        try {
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setupImageReader() {
        //2代表ImageReader中最多可以获取两帧图像流
        mImageReader = ImageReader.newInstance(mCaptureSize.getWidth(), mCaptureSize.getHeight(),
                ImageFormat.JPEG, 2);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                //final Image image = reader.acquireNextImage();
                mCameraHandler.post(new imageSaver(reader.acquireNextImage()));
                Log.w(TAG, "setupImageReader:onImageAvailable");
                UpdateImageView();
                //lastImage.setImageResource(R.drawable.delta);
                //  lastImage.setVisibility(View.VISIBLE);
/*
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);//由缓冲区存入字节数组
                        final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        if (bitmap != null) {
                            Log.w(TAG, "show");
                            lastImage.setImageBitmap(bitmap);
                        }

                    }
                });
 */
            }
        }, mCameraHandler);
    }

    public static class imageSaver implements Runnable {

        private Image mImage;

        public imageSaver(Image image) {
            mImage = image;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            String path = Environment.getExternalStorageDirectory() + "/DCIM/CameraV2/";
            File mImageFile = new File(path);
            if (!mImageFile.exists()) {
                mImageFile.mkdir();
            }

            String fileName = path + getPhotoDefaultName();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(fileName);
                fos.write(data, 0, data.length);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                        savedPath=fileName;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //-----------------------------------------------------------------------------------------
    // 根據相機轉向，設定不同方向的 textureView
    //-----------------------------------------------------------------------------------------
    private void transformImage(int width, int height) {
        if (mTextureView == null) {

            return;
        } else try {
            {
                Matrix matrix = new Matrix();
                int rotation = getWindowManager().getDefaultDisplay().getRotation();
                RectF textureRectF = new RectF(0, 0, width, height);
                RectF previewRectF = new RectF(0, 0, mTextureView.getHeight(), mTextureView.getWidth());
                float centerX = textureRectF.centerX();
                float centerY = textureRectF.centerY();
                if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
                    previewRectF.offset(centerX - previewRectF.centerX(), centerY - previewRectF.centerY());
                    matrix.setRectToRect(textureRectF, previewRectF, Matrix.ScaleToFit.FILL);
                    float scale = Math.max((float) width / width, (float) height / width);
                    matrix.postScale(scale, scale, centerX, centerY);
                    matrix.postRotate(90 * (rotation - 2), centerX, centerY);
                }
                mTextureView.setTransform(matrix);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //-----------------------------------------------------------------------------------------
    // 取得最大尺寸
    //-----------------------------------------------------------------------------------------
    public static Size getMaxSize(Size... sizes) {
        if (sizes == null || sizes.length == 0) {
            throw new IllegalArgumentException("sizes was empty");
        }

        Size sz = sizes[0];
        for (Size size : sizes) {
            if (size.getWidth() * size.getHeight() > sz.getWidth() * sz.getHeight()) {
                sz = size;
            }
        }
        return sz;
    }

    //-----------------------------------------------------------------------------------------
    // 切換前後鏡頭
    //-----------------------------------------------------------------------------------------
    private void switchCamera(){
        try{
            for (String cameraId : manager.getCameraIdList()){
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                //获取StreamConfigurationMap，它是管理摄像头支持的所有输出格式和尺寸
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                assert map != null;
                Size maxSize = getMaxSize(map.getOutputSizes(SurfaceHolder.class));
                if (mCameraId.equals(String.valueOf(CameraCharacteristics.LENS_FACING_BACK)) &&
                    characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    //前鏡頭 --> 後鏡頭
                    mCaptureSize = maxSize;
                    mCameraId = String.valueOf(CameraCharacteristics.LENS_FACING_FRONT);
                    mCameraDevice.close();
                    openCamera();

                    CameraSWBtn.setText(R.string.camera_back);  // 後鏡頭
                    showToastIns(getApplicationContext(), "使用相機"+mCameraId, Toast.LENGTH_LONG);
                    break;
                } else if (mCameraId.equals(String.valueOf(CameraCharacteristics.LENS_FACING_FRONT)) &&
                    characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    //後鏡頭 --> 前鏡頭
                    mCaptureSize = maxSize;
                    mCameraId = String.valueOf(CameraCharacteristics.LENS_FACING_BACK);
                    mCameraDevice.close();
                    openCamera();

                    CameraSWBtn.setText(R.string.camera_front); // 前鏡頭
                    showToastIns(getApplicationContext(), "使用相機"+mCameraId, Toast.LENGTH_LONG);
                    break;
                }
            }
        }catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //-----------------------------------------------------------------------------------------
    // 隱藏右下角上次拍攝的圖
    //-----------------------------------------------------------------------------------------
    private void HideImageView(){
        lastImage.setVisibility(View.GONE);
    }

    //-----------------------------------------------------------------------------------------
    // 顯示右下角上次拍攝的圖
    //-----------------------------------------------------------------------------------------
    private void UpdateImageView(){
        if (checkSDCard()){
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String folder = Environment.getExternalStorageDirectory() + "/DCIM/CAMERAV2/";
                File[] cam2files = getImages(folder);  // 將此路徑裡的相關檔案取出

                if ((cam2files != null ? cam2files.length : 0) != 0) {
                    lastImage.setImageURI(Uri.fromFile(cam2files[cam2files.length-1]));
                    //lastImage.setRotation(270);
                    lastImage.setVisibility(View.VISIBLE);
                    Log.w(TAG,"顯示右下角.");
                }else{
                    Log.w(TAG,"Without Image Files.");
                }
            }else{
                Log.w(TAG, "MEDIA_MOUNTED fail.");
            }
        }else {
            Log.w(TAG, "SD card doesn't exist.");
        }

    }
}
