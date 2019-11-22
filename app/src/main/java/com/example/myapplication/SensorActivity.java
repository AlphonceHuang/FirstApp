package com.example.myapplication;
/*
TYPE_ACCELEROMETER     A constant describing an accelerometer sensor type. //三轴加速度感应器 返回三个坐标轴的加速度  单位m/s2
TYPE_ALL     A constant describing all sensor types.                     //用于列出所有感应器
TYPE_GRAVITY     A constant describing a gravity sensor type.                //重力感应器
TYPE_GYROSCOPE     A constant describing a gyroscope sensor type            //陀螺仪 可判断方向 返回三个坐标轴上的角度
TYPE_LIGHT     A constant describing an light sensor type.                 //光线感应器 单位 lux 勒克斯
TYPE_LINEAR_ACCELERATION     A constant describing a linear acceleration sensor type.  //线性加速度
TYPE_MAGNETIC_FIELD     A constant describing a magnetic field sensor type.               //磁场感应 返回三个坐标轴的数值  微特斯拉
TYPE_ORIENTATION     This constant is deprecated. use SensorManager.getOrientation() instead. //方向感应器 已过时 可以使用方法获得
TYPE_PRESSURE     A constant describing a pressure sensor type                             //压力感应器  单位 千帕斯卡
TYPE_PROXIMITY     A constant describing an proximity sensor type.                          //距离传感器
TYPE_ROTATION_VECTOR     A constant describing a rotation vector sensor type.             //翻转传感器
TYPE_TEMPERATURE     A constant describing a temperature sensor type                  //温度传感器 单位 摄氏度
*/

import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class SensorActivity extends AppCompatActivity{

    private static final String TAG="Alan";
    private TextView accelerometer_value, linear_accelero;
    private TextView gyroscopes_value, gyroscopes_value2;
    private TextView magnetometer_value, proximity_value, light_value, pressureValue;
    private TextView stepDet_val, stepCount_val, direct_val;
    private SensorManager mSesnorManager;
    private Sensor mAccelerometers, mGyroscopes, mMagnetometer, mProximitySensor, rotationVectorSensor;
    private Sensor mLightSensor, mPressure, mStepDetector, mStepCounter, mLinearAccelero;
    private Sensor mTemperature;
    private float timestamp;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private float[] angle = new float[3];
    //private final float[] deltaRotationVector = new float[4];
    //private boolean proximityEnable;
    private ImageView proximityImg;
    SharedPreferences mem_Sensor1SW;
    private int mDetector=0;

    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        mem_Sensor1SW = getSharedPreferences("SENSOR_SW1", MODE_PRIVATE);

        accelerometer_value=findViewById(R.id.accelero_value);
        linear_accelero = findViewById(R.id.Linearacceler_value);
        gyroscopes_value=findViewById(R.id.gyoscopes_value);
        gyroscopes_value2=findViewById(R.id.gyoscopes_value2);
        magnetometer_value=findViewById(R.id.magnetometer_value);
        proximity_value = findViewById(R.id.Proximity_value);
        //Switch mGyroscopeSW = findViewById(R.id.sensorsw1);
        proximityImg=findViewById(R.id.ProximityImage);
        light_value=findViewById(R.id.light_value);
        pressureValue=findViewById(R.id.pressure_value);
        stepDet_val=findViewById(R.id.stepDetText);
        stepCount_val=findViewById(R.id.stepCountText);
        direct_val = findViewById(R.id.wayText);

        //mGyroscopeSW.setOnCheckedChangeListener(SensorSW);
        //mGyroscopeSW.setChecked(getSensorSW1());

        accelerometer_value.setText("");
        linear_accelero.setText("");
        gyroscopes_value.setText("");
        gyroscopes_value2.setText("");
        magnetometer_value.setText("");
        proximity_value.setText("");
        light_value.setText("");
        pressureValue.setText("");
        stepDet_val.setText("");
        stepCount_val.setText("");
        direct_val.setText("");
        proximityImg.setVisibility(View.INVISIBLE);

        mSesnorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        allSensors();

        // TYPE_ACCELEROMETER 加速度(包含重力)
        mAccelerometers = mSesnorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (mAccelerometers==null){
            Log.w(TAG, "ACCELEROMETER can't work.");
            accelerometer_value.setText(getString(R.string.not_support));
        }

        // TYPE_LINEAR_ACCELERATION 線性加速度(不包含重力)
        mLinearAccelero = mSesnorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if (mLinearAccelero == null){
            Log.w(TAG, "LINEAR_ACCELERATION can't work.");
        }

        // 陀螺儀 (旋轉角速度)
        mGyroscopes = mSesnorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (mGyroscopes==null){
            Log.w(TAG, "GYROSCOPE can't work.");
            gyroscopes_value.setText(getString(R.string.not_support));
        }

        // 磁場感應器
        mMagnetometer = mSesnorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (mMagnetometer==null){
            Log.w(TAG, "MAGNETIC_FIELD can't work.");
            magnetometer_value.setText(getString(R.string.not_support));
        }

        // 距離傳感器
        mProximitySensor = mSesnorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (mProximitySensor==null){
            Log.w(TAG, "PROXIMITY can't work.");
            proximity_value.setText(getString(R.string.not_support));
        }

        // 方向感應器
        rotationVectorSensor = mSesnorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (rotationVectorSensor==null){
            Log.w(TAG, "ROTATION_VECTOR can't work.");
            gyroscopes_value2.setText(getString(R.string.not_support));
        }

        // 光線感應器
        mLightSensor = mSesnorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (mLightSensor==null){
            Log.w(TAG, "LIGHT can't work.");
            light_value.setText(getString(R.string.not_support));
        }

        // 壓力感測器(大氣壓力 / 高度計)
        mPressure = mSesnorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (mPressure==null){
            Log.w(TAG, "AMBIENT_TEMPERATURE can't work.");
            //light_value.setText(getString(R.string.not_support));
        }

        // 計步
        mStepDetector = mSesnorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (mStepDetector==null){
            Log.w(TAG, "STEP_DETECTOR can't work.");
        }
        mStepCounter = mSesnorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (mStepCounter==null){
            Log.w(TAG, "STEP_COUNTER can't work.");
        }

        mTemperature = mSesnorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);
        if (mTemperature == null){
            Log.w(TAG, "TEMPERATURE can't work.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*
        SENSOR_DELAY_FASTEST(0ms)
        SENSOR_DELAY_GAME(20ms)
        SENSOR_DELAY_NORMAL(200ms)
        SENSOR_DELAY_UI(60ms)
         */
        mSesnorManager.registerListener(mySensorEventListener, mAccelerometers, SensorManager.SENSOR_DELAY_NORMAL);
        mSesnorManager.registerListener(mySensorEventListener, mLinearAccelero, SensorManager.SENSOR_DELAY_NORMAL);
        mSesnorManager.registerListener(mySensorEventListener, mGyroscopes, SensorManager.SENSOR_DELAY_NORMAL);
        mSesnorManager.registerListener(mySensorEventListener, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSesnorManager.registerListener(mySensorEventListener, mProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSesnorManager.registerListener(mySensorEventListener, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSesnorManager.registerListener(mySensorEventListener, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSesnorManager.registerListener(mySensorEventListener, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
        mSesnorManager.registerListener(mySensorEventListener, mStepDetector, SensorManager.SENSOR_DELAY_FASTEST);
        mSesnorManager.registerListener(mySensorEventListener, mStepCounter, SensorManager.SENSOR_DELAY_FASTEST);
        mSesnorManager.registerListener(mySensorEventListener, mTemperature, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSesnorManager.unregisterListener(mySensorEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    SensorEventListener mySensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            String temp;

            if (sensorEvent.sensor.equals(mAccelerometers)){
                temp = "X:"+sensorEvent.values[0]+"\n"
                        +"Y:"+sensorEvent.values[1]+"\n"
                        +"Z:"+sensorEvent.values[2];
                accelerometer_value.setText(temp);
                accelerometerValues = sensorEvent.values;
                calculateOrientation();

            }else if (sensorEvent.sensor.equals(mGyroscopes)){
                //if (!getSensorSW1()){
                    if (timestamp != 0) {

                        final float dT = (sensorEvent.timestamp - timestamp) * NS2S;
                        angle[0] += sensorEvent.values[0] * dT;
                        angle[1] += sensorEvent.values[1] * dT;
                        angle[2] += sensorEvent.values[2] * dT;
                        float anglex = (float) Math.toDegrees(angle[0]);
                        float angley = (float) Math.toDegrees(angle[1]);
                        float anglez = (float) Math.toDegrees(angle[2]);
                        temp = "X:" + anglex + "\n"
                                + "Y:" + angley + "\n"
                                + "Z:" + anglez;
                        gyroscopes_value.setText(temp);

                        /*
                        // Calculate the angular speed of the sample
                        float omegaMagnitude = (float)sqrt(anglex*anglex + angley*angley + anglez*anglez);
                        // Normalize the rotation vector if it's big enough to get the axis
                        if (omegaMagnitude > EPSILON) {
                            anglex /= omegaMagnitude;
                            angley /= omegaMagnitude;
                            anglez /= omegaMagnitude;
                        }
                        // Integrate around this axis with the angular speed by the time step
                        // in order to get a delta rotation from this sample over the time step
                        // We will convert this axis-angle representation of the delta rotation
                        // into a quaternion before turning it into the rotation matrix.
                        float thetaOverTwo = omegaMagnitude * dT / 2.0f;
                        float sinThetaOverTwo = (float)sin(thetaOverTwo);
                        float cosThetaOverTwo = (float)cos(thetaOverTwo);
                        deltaRotationVector[0] = sinThetaOverTwo * anglex;
                        deltaRotationVector[1] = sinThetaOverTwo * angley;
                        deltaRotationVector[2] = sinThetaOverTwo * anglez;
                        deltaRotationVector[3] = cosThetaOverTwo;
                        Log.w(TAG, ""+
                                deltaRotationVector[0]+"\n"+
                                deltaRotationVector[1]+"\n"+
                                deltaRotationVector[2]+"\n"+
                                deltaRotationVector[3]);
                         */

                        if (sensorEvent.values[2] > 0.5f) { // anticlockwise
                            getWindow().getDecorView().setBackgroundColor(Color.BLUE);
                        } else if (sensorEvent.values[2] < -0.5f) { // clockwise
                            getWindow().getDecorView().setBackgroundColor(Color.YELLOW);
                        }else if (sensorEvent.values[0] > 0.5f){
                            getWindow().getDecorView().setBackgroundColor(Color.RED);
                        }else if (sensorEvent.values[0] < -0.5f){
                            getWindow().getDecorView().setBackgroundColor(Color.GREEN);
                        }else if (sensorEvent.values[1] > 0.5f){
                            getWindow().getDecorView().setBackgroundColor(Color.CYAN);
                        }else if (sensorEvent.values[1] < -0.5f){
                            getWindow().getDecorView().setBackgroundColor(Color.MAGENTA);
                        } else {
                            getWindow().getDecorView().setBackgroundColor(Color.WHITE);
                        }
                    }
                    timestamp = sensorEvent.timestamp;
                //}

            }else if (sensorEvent.sensor.equals(mMagnetometer)){
                temp = "X:"+sensorEvent.values[0]+"\n"
                        +"Y:"+sensorEvent.values[1]+"\n"
                        +"Z:"+sensorEvent.values[2];
                magnetometer_value.setText(temp);
                magneticFieldValues = sensorEvent.values;

            }else if (sensorEvent.sensor.equals(mProximitySensor)){
                if(sensorEvent.values[0] < mProximitySensor.getMaximumRange()) {
                    //Log.w(TAG, "有東西接近");
                    //getWindow().getDecorView().setBackgroundColor(Color.RED);
                    //proximityEnable=true;
                    proximityImg.setVisibility(View.VISIBLE);
                    //proximity_value.setText(String.valueOf(sensorEvent.values[0]));
                }else{
                    //getWindow().getDecorView().setBackgroundColor(Color.WHITE);
                    //proximityEnable=false;
                    proximityImg.setVisibility(View.INVISIBLE);
                }
                proximity_value.setText(String.valueOf(sensorEvent.values[0]));

            }else if (sensorEvent.sensor.equals(rotationVectorSensor)){
                //if (getSensorSW1()) {
                    float[] rotationMatrix = new float[16];
                    SensorManager.getRotationMatrixFromVector(
                            rotationMatrix, sensorEvent.values);

                    // Remap coordinate system
                    float[] remappedRotationMatrix = new float[16];
                    SensorManager.remapCoordinateSystem(rotationMatrix,
                            SensorManager.AXIS_X,
                            SensorManager.AXIS_Z,
                            remappedRotationMatrix);

                    // Convert to orientations
                    float[] orientations = new float[3];
                    SensorManager.getOrientation(remappedRotationMatrix, orientations);

                    for (int i = 0; i < 3; i++) {
                        orientations[i] = (float) (Math.toDegrees(orientations[i]));
                    }
                    temp = "X:" + orientations[0] + "\n"
                            + "Y:" + orientations[1] + "\n"
                            + "Z:" + orientations[2];
                    gyroscopes_value2.setText(temp);
                //}
            }else if (sensorEvent.sensor.equals(mLightSensor)){
                light_value.setText(String.valueOf(sensorEvent.values[0]));

            }else if (sensorEvent.sensor.equals(mPressure)){
                float sPa = sensorEvent.values[0];
                //计算海拔  sPa的单位 为hPa  而标准大气压的单位为kPa
                double height = (1 - Math.pow( (sPa / 1013.25),  (1 / 5.265))) * 44300;
                String Height="高度:"+height;
                pressureValue.setText(Height);

            }else if (sensorEvent.sensor.equals(mStepDetector)){
                if (sensorEvent.values[0]==1.0){
                    mDetector++;
                    String stepD = ", 此次: "+mDetector+"步";
                    stepDet_val.setText(stepD);
                }

            }else if (sensorEvent.sensor.equals(mStepCounter)){
                String stepC = "累計: "+sensorEvent.values[0]+"步";
                stepCount_val.setText(stepC);

            }else if (sensorEvent.sensor.equals(mLinearAccelero)) {
                temp = "X:"+sensorEvent.values[0]+"\n"
                        +"Y:"+sensorEvent.values[1]+"\n"
                        +"Z:"+sensorEvent.values[2];
                linear_accelero.setText(temp);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    /*
    private boolean getSensorSW1(){
        return mem_Sensor1SW.getBoolean("SENSOR_SW1", true);
    }
    private void setSensorSW1(boolean b){
        SharedPreferences.Editor editor = mem_Sensor1SW.edit(); //獲取編輯器
        editor.putBoolean("SENSOR_SW1", b); // 取消direct connect flag
        editor.apply();
        editor.commit();    //提交
    }

    //=================================================================
    // Switch Event
    //=================================================================
    private CompoundButton.OnCheckedChangeListener SensorSW= new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (R.id.sensorsw1==compoundButton.getId()) {
                setSensorSW1(b);
            }
        }
    };*/

    public void allSensors() {
        //从系统服务中获得传感器管理器
        //从传感器管理器中获得全部的传感器列表
        List<Sensor> allSensors = mSesnorManager.getSensorList(Sensor.TYPE_ALL);
        Log.i(TAG, "allsensors: " + allSensors.size());

        //显示每个传感器的具体信息
        for (Sensor sensor : allSensors) {
            String name = sensor.getName();
            String vendor = sensor.getVendor();
            int version = sensor.getVersion();
            Log.i(TAG, "name: " + name + ", vendor: " + vendor + ", version: " + version);
        }
    }

    private void calculateOrientation(){
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);
        // 要经过一次数据格式的转换，转换为度
        values[0] = (float) Math.toDegrees(values[0]);
        //Log.i(TAG, values[0]+"");
        //values[1] = (float) Math.toDegrees(values[1]);
        //values[2] = (float) Math.toDegrees(values[2]);

        if(values[0] >= -5 && values[0] < 5){
            //Log.i(TAG, "正北");
            direct_val.setText("指南針：正北");
        }
        else if(values[0] >= 5 && values[0] < 85){
            //Log.i(TAG, "东北");
            direct_val.setText("指南針：東北");
        }
        else if(values[0] >= 85 && values[0] <=95){
            //Log.i(TAG, "正东");
            direct_val.setText("指南針：正東");
        }
        else if(values[0] >= 95 && values[0] <175){
            //Log.i(TAG, "东南");
            direct_val.setText("指南針：東南");
        }
        else if((values[0] >= 175 && values[0] <= 180) || (values[0]) >= -180 && values[0] < -175){
            //Log.i(TAG, "正南");
            direct_val.setText("指南針：正南");
        }
        else if(values[0] >= -175 && values[0] <-95){
            //Log.i(TAG, "西南");
            direct_val.setText("指南針：西南");
        }
        else if(values[0] >= -95 && values[0] < -85){
            //Log.i(TAG, "正西");
            direct_val.setText("指南針：正西");
        }
        else if(values[0] >= -85 && values[0] <-5){
            //Log.i(TAG, "西北");
            direct_val.setText("指南針：西北");
        }
    }
}
