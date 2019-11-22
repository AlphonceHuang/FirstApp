package com.example.myapplication;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

public class ShowCamera extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "Alan";
    private SurfaceHolder holdMe;
    private Camera theCamera;


    public ShowCamera(Context context, Camera camera) {
        super(context);
        theCamera = camera;
        holdMe = getHolder();
        holdMe.addCallback(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        Log.w(TAG, "surfaceChanged");
        Camera.Parameters parameters = theCamera.getParameters();
        List<String> allFocus = parameters.getSupportedFocusModes();
        //Log.w(TAG,"allFocus:"+allFocus.size());
        //for (int i=0; i<allFocus.size(); i++)
        //    Log.w(TAG, "SUPPORT FOCUSE MODE:"+allFocus.get(i));

        // FOCUS_MODE_CONTINUOUS_PICTURE 會一直在PREVIEW的地方做AUTO FOCUS
        // AUTO是只做一次AUTO FOCUS
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        theCamera.setParameters(parameters);
        theCamera.startPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.w(TAG, "surfaceCreated");
        try   {
            theCamera.setDisplayOrientation(90);
            theCamera.setPreviewDisplay(holder);
            //theCamera.startPreview();
        } catch (IOException e) {
            theCamera.release();
            //Log.w("Alan", "surfaceCreated fail.");
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        Log.w(TAG, "surfaceDestroyed");
        //theCamera.stopPreview();//停止預覽 ---- 這個會造成app當掉
        //theCamera.release();//釋放相機資源
        //theCamera = null;
    }

}
