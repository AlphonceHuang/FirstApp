package com.example.myapplication;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SpeechToTextActivity extends AppCompatActivity {

    private static final String TAG="Alan";
    private TextView speechText;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_to_text);

        Button start_Btn = findViewById(R.id.start_button);
        start_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "請說～");
                try{
                    startActivityForResult(intent,VOICE_RECOGNITION_REQUEST_CODE);
                }catch (ActivityNotFoundException a){
                    Toast.makeText(getApplicationContext(),"Intent problem", Toast.LENGTH_SHORT).show();
                }
            }
        });

        speechText=findViewById(R.id.toText);
        speechText.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == VOICE_RECOGNITION_REQUEST_CODE){
            if(resultCode == RESULT_OK && data != null){
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                //speechText.setText(result.get(0));    // 只列出最佳結果
                StringBuilder all = new StringBuilder();
                for (String r : result) {   // 將所有結果列出來
                    all.append(r).append("\n");
                }
                speechText.setText(all.toString());

                if (result.get(0).equals("打開第二頁"))
                {
                    Log.w(TAG, "open second page.");
                    Intent intent = new Intent(SpeechToTextActivity.this,SpeechToText2Activity.class);
                    startActivity(intent);
                }
            }
        }
    }
}
