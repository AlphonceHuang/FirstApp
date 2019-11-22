package com.example.myapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class SpeechToTextService extends Service {

    private static final String TAG="Alan";

    //Use audio manageer services
    static protected AudioManager mAudioManager;
    protected SpeechRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;
    protected final Messenger mServerMessenger = new Messenger(new IncomingHandler(this));

    //To turn screen On uitll lock phone
    //PowerManager.WakeLock wakeLock;
    protected boolean mIsListening;
    protected volatile boolean mIsCountDownOn;
    static boolean mIsStreamSolo;

    static final int MSG_RECOGNIZER_START_LISTENING = 1;
    static final int MSG_RECOGNIZER_CANCEL = 2;
    static int result = 0;
    //static int identify = 0;
    String Currentdata = null;//, newcurrent = null;
    public static final String CHANNEL_ID = "SpeechToTextServiceChannel";
    static boolean isServiceRnning=false;

    @Override
    public void onCreate() {
        super.onCreate();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());

        Log.w(TAG, "SpeechToTextService: oncreate");
        isServiceRnning=true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);

        Log.w(TAG, "SpeechToTextService: onStartCommand");
        createNotificationChannel();

        // 狀態列的通知
        Intent notificationIntent = new Intent(this, SpeechToText3Activity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SpeechToText Service")
                .setContentText("這個是語音轉文字的功能")
                .setSmallIcon(R.drawable.delta_office)
                .setContentIntent(pendingIntent)
                .build();

        //AndroidManifest.xml需增加
        //<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
        startForeground(1, notification);   // 啟用FOREGROUND_SERVICE
        return START_NOT_STICKY;
    }

    // 啟用FOREGROUND_SERVICE，畫面關掉時仍在處理
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public SpeechToTextService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mServerMessenger.getBinder();
    }

    protected static class IncomingHandler extends Handler {
        private WeakReference<SpeechToTextService> mtarget;

        public IncomingHandler(SpeechToTextService speechToTextService) {
            mtarget = new WeakReference<SpeechToTextService>(speechToTextService);
        }

        @Override
        public void handleMessage(Message msg) {
            final SpeechToTextService target = mtarget.get();

            switch (msg.what) {
                case MSG_RECOGNIZER_START_LISTENING:

                    //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        // turn off beep sound
                        if (!mIsStreamSolo) {
                            // 關閉媒體及通知音效
                            Log.w(TAG, "關閉聲音");
                            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                            mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
                            mIsStreamSolo = true;
                        }
                    //}
                    if (!target.mIsListening) {
                        target.mSpeechRecognizer.startListening(target.mSpeechRecognizerIntent);
                        target.mIsListening = true;
                        Log.d(TAG, "message start listening");
                    }
                    break;

                case MSG_RECOGNIZER_CANCEL:
                    if (mIsStreamSolo) {
                        mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                        mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
                        mIsStreamSolo = false;
                    }
                    target.mSpeechRecognizer.cancel();
                    target.mIsListening = false;
                    Log.d(TAG, "message canceled recognizer");
                    break;
            }
        }
    }

    // Count down timer for Jelly Bean work around
    protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(2000, 2000) {

        @Override
        public void onTick(long millisUntilFinished) {
            // TODO Auto-generated method stub
            Log.w(TAG, "CountDownTimer: onTick");
        }

        @Override
        public void onFinish() {
            Log.w(TAG, "CountDownTimer: onFinish");
            mIsCountDownOn = false;
            Message message = Message.obtain(null, MSG_RECOGNIZER_CANCEL);
            try {
                mServerMessenger.send(message);
                message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
                mServerMessenger.send(message);
            } catch (RemoteException e) {

            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "SpeechToTextService: onDestroy");

        if (mIsCountDownOn) {
            mNoSpeechCountDown.cancel();
        }
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
        }
        stopForeground(true);   // 關閉FOREGROUND_SERVICE
        isServiceRnning=false;
        if (mIsStreamSolo) {
            Log.w(TAG, "開啟聲音");
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
            mIsStreamSolo = false;
        }
    }

    protected class SpeechRecognitionListener implements RecognitionListener {

        @Override
        public void onBeginningOfSpeech() {
            // speech input will be processed, so there is no need for count down anymore
            if (mIsCountDownOn) {
                mIsCountDownOn = false;
                mNoSpeechCountDown.cancel();
            }
            Log.d(TAG, "onBeginingOfSpeech");
        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech");
        }

        @Override
        public void onError(int error) {
            if (mIsCountDownOn) {
                mIsCountDownOn = false;
                mNoSpeechCountDown.cancel();
            }
            mIsListening = false;
            Message message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
            try {
                mServerMessenger.send(message);
            } catch (RemoteException e) {

            }
        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }

        @Override
        public void onPartialResults(Bundle partialResults) {
/*
            Log.i(TAG, "onPartialResults");

            ArrayList data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            assert data != null;
            String word = (String) data.get(data.size() - 1);
            if (Currentdata == null) {
                SpeechToText3Activity.textView.setText(word);
            } else {
                String temp = Currentdata + " " + word;
                SpeechToText3Activity.textView.setText(temp);
                SpeechToText3Activity.textView.setSelection(SpeechToText3Activity.textView.getText().length());
            }

            newcurrent = SpeechToText3Activity.textView.getText().toString();
            identify = 1;
            Log.d(TAG, "" + word);*/
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mIsCountDownOn = true;
                mNoSpeechCountDown.start();
            //}
            Log.d(TAG, "onReadyForSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onResults(Bundle results) {

            Log.d(TAG, "onResults");

            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            assert data != null;

            //for (int i=0; i<data.size(); i++){
            //    Log.w(TAG, ""+data.get(i)+"\n");
            //}

            //String word = (String) data.get(data.size() - 1);
            String word = (String) data.get(0);

            if (result == 0) {
                //Log.w(TAG, "result=0"+word);
                SpeechToText3Activity.textView.setText(word);
                Currentdata = SpeechToText3Activity.textView.getText().toString();
            } else if (result == 1) {
                //Log.w(TAG, "result=1"+word);
                if (Currentdata != null) {
                    String temp = Currentdata + "\n" + word;
                    SpeechToText3Activity.textView.setText(temp);
                    //SpeechToText3Activity.textView.setSelection(SpeechToText3Activity.textView.getText().length());
                }
            }
            Currentdata = SpeechToText3Activity.textView.getText().toString();

            Log.d(TAG, "" + Currentdata);

            if (mIsListening) {
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
            }
            result = 1;
        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

    }
}
