package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG="Alan";
    private EditText name, password;
    private static final String PWD="12345";

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
        CheckBox ck=findViewById(R.id.pwdcheckBox);
        ck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }else{
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                password.setSelection(password.length());  // 光標在最後
            }
        });
    }

    boolean checkPassword(){
        String inputName=name.getText().toString();
        String inputPassword=password.getText().toString();

        Log.w(TAG, "Name:"+inputName+"\n"+"Password:"+inputPassword);

        //return inputPassword.equals(PWD);
        return true;
    }

    public void entermain(View view){
        if (checkPassword()) {
            Intent it = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(it);
            finish();
        }else{
            //showToastIns(getApplicationContext(), getString(R.string.cal_fail), Toast.LENGTH_LONG);
            ShowIncorrectToast();
            name.setText("");
            password.setText("");
        }
    }

    public void ShowIncorrectToast()
    {
        //把xml的資源轉成view
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.incorrect_layout, (ViewGroup) findViewById(R.id.pwdincorrectLayout));
        Toast toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);

        // 位置
        toast.setGravity(Gravity.CENTER, 0,0);
        toast.setView(layout);
        toast.show();
    }
}
