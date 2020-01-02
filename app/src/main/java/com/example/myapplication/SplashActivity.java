package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import static com.example.myapplication.Util.showToastIns;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG="Alan";
    private EditText name, password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
/*
        Thread myThread=new Thread(){//创建子线程
            @Override
            public void run() {
                try{
                    sleep(3000);//使程序休眠五秒
                    Intent it=new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(it);
                    finish();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        myThread.start();
 */
        name=findViewById(R.id.editNameText);
        password=findViewById(R.id.editPWDText);
    }

    boolean checkPassword(){
        String inputName=name.getText().toString();
        String inputPassword=password.getText().toString();

        Log.w(TAG, "Name:"+inputName+"\n"+"Password:"+inputPassword);
        return true;
    }

    public void entermain(View view){
        if (checkPassword()) {
            Intent it = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(it);
            finish();
        }else{
            showToastIns(getApplicationContext(), getString(R.string.cal_fail), Toast.LENGTH_LONG);
        }
    }
}
