package com.example.myapplication;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static com.example.myapplication.SpeechToTextService.isServiceRnning;
import static com.example.myapplication.Util.showToastIns;

public class SpeechToText3Activity extends AppCompatActivity {

    Button speak, stop;
    static TextView textView;   // 給service用所以用static
    private static final String TAG = "Alan";
    ProgressDialog dialog;
    //int code;
    boolean isEndOfSpeech = false;
    boolean serviceconneted;

    static final Integer LOCATION = 0x1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_to_text3);
        //Log.w(TAG, "SpeechToText3Activity:onCreate");

        speak = findViewById(R.id.speak);
        stop = findViewById(R.id.stop);
        textView = findViewById(R.id.write);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 確認service啟動狀態決定按鈕是否反灰
        //Log.w(TAG, "isServiceRnning:"+isServiceRnning);
        if (isServiceRnning){
            stop.setEnabled(true);
            speak.setEnabled(false);
        }else {
            stop.setEnabled(false);
            speak.setEnabled(true);
        }

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SpeechToText3Activity.this, SpeechToTextService.class);
                stopService(i);
                showToastIns(SpeechToText3Activity.this, "stop speaking", Toast.LENGTH_SHORT);
                stop.setEnabled(false);
                speak.setEnabled(true);
            }
        });
        SpeechRecognizer sr = SpeechRecognizer.createSpeechRecognizer(SpeechToText3Activity.this);
        sr.setRecognitionListener(new Listner());

        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    askForPermission();
                }
                Intent i = new Intent(SpeechToText3Activity.this, SpeechToTextService.class);
                bindService(i, connection, BIND_AUTO_CREATE);
                startService(i);
                showToastIns(SpeechToText3Activity.this, "Start Speaking", Toast.LENGTH_SHORT);
                speak.setEnabled(false);
                stop.setEnabled(true);
            }
        });
    }

    @Override
    protected void onDestroy(){
        Log.w(TAG, "SpeechToText3Activity: on Destroy");
        super.onDestroy();
        unbindService(connection);
    }

    class Listner implements RecognitionListener {

        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "ReadyForSpeech");
        }

        @Override
        public void onBeginningOfSpeech() {
            Log.d(TAG, "beginSpeech");

        }

        @Override
        public void onRmsChanged(float rmsdB) {
            Log.d(TAG, "onrms");

        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "onbuffer");

        }

        @Override
        public void onEndOfSpeech() {
            isEndOfSpeech = true;

        }

        @Override
        public void onError(int error) {
            Log.d(TAG, "error " + error);
            if (!isEndOfSpeech) {
                return;
            }
            showToastIns(SpeechToText3Activity.this, "Try agine", Toast.LENGTH_SHORT);
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            assert data != null;
            String word = (String) data.get(data.size() - 1);
            textView.setText(word);
            dialog.dismiss();
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            ArrayList data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            assert data != null;
            String word = (String) data.get(data.size() - 1);
            textView.setText(word);
        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    }

    private void askForPermission() {
        if (ContextCompat.checkSelfPermission(SpeechToText3Activity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(SpeechToText3Activity.this, Manifest.permission.RECORD_AUDIO)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(SpeechToText3Activity.this, new String[]{Manifest.permission.RECORD_AUDIO}, SpeechToText3Activity.LOCATION);

            } else {
                ActivityCompat.requestPermissions(SpeechToText3Activity.this, new String[]{Manifest.permission.RECORD_AUDIO}, SpeechToText3Activity.LOCATION);
            }
        } else {
            showToastIns(this, "" + Manifest.permission.RECORD_AUDIO + " is already granted.", Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                showToastIns(this, "Permission denied-1", Toast.LENGTH_SHORT);
            }
        } else {
            showToastIns(this, "Permission denied", Toast.LENGTH_SHORT);
        }
    }

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            Log.d(TAG, "service connected");

            Messenger mServiceMessenger = new Messenger(service);
            Message msg = new Message();
            msg.what = SpeechToTextService.MSG_RECOGNIZER_START_LISTENING;
            try {
                mServiceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            serviceconneted = false;
            Log.d(TAG, "service disconnetd");
        }
    };
}
