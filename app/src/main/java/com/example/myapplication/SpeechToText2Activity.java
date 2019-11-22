package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class SpeechToText2Activity extends AppCompatActivity implements RecognitionListener {
    private TextView returnedText;
    private ProgressBar progressBar;
    private ToggleButton toggleButton;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String TAG = "Alan";
    SharedPreferences mem_BestAnsSetting;
    SharedPreferences mem_OpenActivityStart;
    SharedPreferences mem_ContinueRecognize;

    private boolean waitSpecialCommand=false;
    static protected AudioManager mAudioManager;
    private boolean startActivity=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_to_text2);

        returnedText = findViewById(R.id.SpeechResultText);
        progressBar = findViewById(R.id.VolumeLevelBar);
        toggleButton = findViewById(R.id.SpeechToggleButton);
        Switch ansSwitch = findViewById(R.id.BestResultSwitch);
        CheckBox openActive = findViewById(R.id.startOnActive);
        CheckBox aContinue = findViewById(R.id.ContinueReg);

        mem_BestAnsSetting = getSharedPreferences("BEST_ANS", MODE_PRIVATE);
        ansSwitch.setOnCheckedChangeListener(SpeechToTextAnsSW);
        ansSwitch.setChecked(BestAnswerGet());

        mem_OpenActivityStart = getSharedPreferences("OPEN_START", MODE_PRIVATE);
        openActive.setOnCheckedChangeListener(SpeechToTextAnsSW);
        openActive.setChecked(OpenActiveGet());

        mem_ContinueRecognize = getSharedPreferences("NON_STOP", MODE_PRIVATE);
        aContinue.setOnCheckedChangeListener(SpeechToTextAnsSW);
        aContinue.setChecked(NonStopGet());

        progressBar.setVisibility(View.INVISIBLE);



        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (OpenActiveGet()){
            startActivity=true;
            StartListing();
            toggleButton.setChecked(true);
        }

            toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.w(TAG, "isChecked:"+isChecked);
                    if (isChecked) {
                        startActivity=true;
                        StartListing();
                    } else {
                        StopListing();
                        //startActivity=false;
                        //progressBar.setIndeterminate(false);
                        //progressBar.setVisibility(View.INVISIBLE);
                        //speech.stopListening();
                    }
                }
            });


        MediaRecorder myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //MediaRecorder.AudioSource.REMOTE_SUBMIX
        //List<MicrophoneInfo> micList=new List<MicrophoneInfo>();
        //micList=myAudioRecorder.getActiveMicrophones();

    }

    private void Initial_VoiceService(){
        Log.w(TAG, "Initial_VoiceService");
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);    // 開啟語音活動
        //recognizerIntent = new Intent(RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SECURE, true);

        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.getDefault());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // API 23
        //    recognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
        //}
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);   // 使用語言模型在Web上搜尋
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);   // 返回的最大結果
    }

    private void StartListing(){
        if (startActivity) {
            Log.w(TAG, "StartListing");
            //waitSpecialCommand=false;

            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);

            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);
            toggleButton.setChecked(true);

            speech.startListening(recognizerIntent);
        }else{
            Log.w(TAG, "startActivity=false");
        }
    }

    private void StopListing(){

        startActivity=false;
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.INVISIBLE);
        speech.stopListening();
        toggleButton.setChecked(false);

        // 重新開啟聲音
        //Log.w(TAG, "開啟聲音");
        mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
    }
    
    @Override
    protected void onDestroy(){
        StopListing();
        if (speech != null)
            speech.destroy();
        super.onDestroy();
        Log.w(TAG, "SpeechToText2Activity: onDestroy");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Initial_VoiceService();
        Log.w(TAG, "SpeechToText2Activity: onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (speech != null) {
            StopListing();
            speech.destroy();
            Log.w(TAG, "SpeechToText2Activity: onPause");
        }
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        Log.i(TAG, "onReadyForSpeech");
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(TAG, "onBeginningOfSpeech");
        progressBar.setIndeterminate(false);
        progressBar.setMax(10);
    }

    @Override
    public void onRmsChanged(float v) {
        //Log.i(TAG, "onRmsChanged: " + v);
        progressBar.setProgress((int) v);
    }

    @Override
    public void onBufferReceived(byte[] bytes) {
        Log.i(TAG, "onBufferReceived: " + Arrays.toString(bytes));
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(TAG, "onEndOfSpeech");

        if (NonStopGet()) {
            StartListing();
        }else {
            progressBar.setIndeterminate(true);
            toggleButton.setChecked(false);
        }
    }

    @Override
    public void onResults(Bundle bundle) {
        Log.i(TAG, "onResults");
        ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        StringBuilder text = new StringBuilder();
        int num=1;

        if (matches!=null) {
            if (BestAnswerGet()) {
                for (String result : matches) {
                    text.append(num).append(": ").append(result).append("\n");
                    //text += num +": "+result + "\n";
                    num++;
                }
            } else {
                text = new StringBuilder(matches.get(0));

                String ret;
                ret = ProcessVoiceCommand(text.toString());
                if (!ret.isEmpty()) {
                    text.append("\n").append(ret);
                }
            }
        }
        returnedText.setText(text.toString());

        StartListing();
    }

    @Override
    public void onPartialResults(Bundle bundle) {
        Log.i(TAG, "onPartialResults");
    }

    @Override
    public void onEvent(int i, Bundle bundle) {
        Log.i(TAG, "onEvent");
    }

    @Override
    public void onError(int i) {
        String errorMessage = getErrorText(i);
        Log.d(TAG, "FAILED " + errorMessage);
        //returnedText.setText(errorMessage);

        if (NonStopGet()) {
            if (i==SpeechRecognizer.ERROR_NO_MATCH) {
                StartListing();
            }else if (i==SpeechRecognizer.ERROR_SPEECH_TIMEOUT){
                StopListing();
                Log.w(TAG, "timeout 重新啟動");
                startActivity=true;
                StartListing();
            }
        }else{
            progressBar.setIndeterminate(false);
            progressBar.setVisibility(View.INVISIBLE);
            toggleButton.setChecked(false);
            startActivity=false;
        }
/*
        if (errorMessage.equals("我聽不懂你在說什麼")){
            waitSpecialCommand=false;
            progressBar.setIndeterminate(false);
            progressBar.setVisibility(View.INVISIBLE);
            if (NonStopGet())
                StartListing();
        }else{
            toggleButton.setChecked(false);
        }*/
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "我聽不懂你在說什麼";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    //=================================================================
    // Switch Event
    //=================================================================
    private CompoundButton.OnCheckedChangeListener SpeechToTextAnsSW= new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (R.id.BestResultSwitch==compoundButton.getId()) {
                BestAnswerSet(b);
            }else if (R.id.startOnActive == compoundButton.getId()){
                OpenActiveSet(b);
            }else if (R.id.ContinueReg == compoundButton.getId()){
                NonStopSet(b);
            }
        }
    };

    //=================================================================
    // BEST_ANS 儲存/讀取
    //=================================================================
    private boolean BestAnswerGet(){
        return mem_BestAnsSetting.getBoolean("BEST_ANS", true);
    }

    private void BestAnswerSet(boolean bEnable){

        SharedPreferences.Editor editor = mem_BestAnsSetting.edit(); //獲取編輯器
        Log.w(TAG, "BEST_ANS:"+bEnable);
        editor.putBoolean("BEST_ANS", bEnable);
        editor.apply();
        editor.commit();    //提交
    }

    //=================================================================
    // OPEN_START 儲存/讀取
    //=================================================================
    private boolean OpenActiveGet(){
        return mem_OpenActivityStart.getBoolean("OPEN_START", true);
    }

    private void OpenActiveSet(boolean bEnable){

        SharedPreferences.Editor editor = mem_OpenActivityStart.edit(); //獲取編輯器
        Log.w(TAG, "OPEN_START:"+bEnable);
        editor.putBoolean("OPEN_START", bEnable);
        editor.apply();
        editor.commit();    //提交
    }

    //=================================================================
    // OPEN_START 儲存/讀取
    //=================================================================
    private boolean NonStopGet(){
        return mem_ContinueRecognize.getBoolean("NON_STOP", true);
    }

    private void NonStopSet(boolean bEnable){

        SharedPreferences.Editor editor = mem_ContinueRecognize.edit(); //獲取編輯器
        Log.w(TAG, "NON_STOP:"+bEnable);
        editor.putBoolean("NON_STOP", bEnable);
        editor.apply();
        editor.commit();    //提交
    }
	
	// 辨識關鍵字
    private String ProcessVoiceCommand(String command){
        String feedback="";
        if (command.equals("哈囉小黃")){
            waitSpecialCommand=true;
            feedback="請說";
            StartListing();
        }else{
            if (waitSpecialCommand){
                feedback=ProcessCommand(command);
            }
            waitSpecialCommand=false;
            if (NonStopGet())
                StartListing();
        }
        return feedback;
    }

    // 辨識指令
    private String ProcessCommand(String command){
        String ans="Ans:";
        Log.w(TAG, "Ans command:"+command);

        if (command.equals("關閉")){
            StopListing();
        }
        return ans;
    }
}
