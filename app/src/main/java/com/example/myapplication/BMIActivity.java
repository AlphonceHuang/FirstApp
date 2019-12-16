package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

import static com.example.myapplication.Util.showToastIns;

public class BMIActivity extends AppCompatActivity {

    private EditText input_heigth, input_weight;
    private TextView result_text;
    private Button result_button, report_button;
    private double BMI;
    private static final String TAG="Alan";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi);

        input_heigth = findViewById(R.id.BMI_Height_ETxt);
        input_weight = findViewById(R.id.BMI_Weight_ETxt);
        result_text = findViewById(R.id.BMI_ResultText);
        result_text.setText("");

        result_button =findViewById(R.id.BMI_CalBtn);
        Button clear_button = findViewById(R.id.BMI_ClearBtn);
        report_button = findViewById(R.id.BMI_ReportBtn);

        result_button.setEnabled(false);
        report_button.setEnabled(false);

        //------------------------------------------------
        // 監聽EditText的狀態
        //------------------------------------------------
        input_heigth.addTextChangedListener(InputWatcher);
        input_weight.addTextChangedListener(InputWatcher);

        result_button.setOnClickListener(BMI_Button_Event);
        clear_button.setOnClickListener(BMI_Button_Event);
        report_button.setOnClickListener(BMI_Button_Event);

    }

    private View.OnClickListener BMI_Button_Event = new View.OnClickListener() {
        double height,weight;
        @Override
        public void onClick(View view) {

            switch (view.getId()){
                case R.id.BMI_CalBtn:
                    height = Double.parseDouble(input_heigth.getText().toString());
                    weight = Double.parseDouble(input_weight.getText().toString());

                    if (CalCulate_BMI(height,weight)){

                        DecimalFormat nf = new DecimalFormat("0.00");
                        String suggess;
                        if (BMI > 25) { //太重了
                            suggess = getString(R.string.suggest_over);
                            result_text.setTextColor(Color.RED);
                        }else if (BMI < 20){
                            suggess = getString(R.string.suggest_light);
                            result_text.setTextColor(Color.BLUE);
                        }else{
                            suggess = getString(R.string.suggest_good);
                            result_text.setTextColor(Color.GREEN);
                        }
                        String strRes=getString(R.string.cal_result) + " "+nf.format(BMI)+"\n"+suggess;
                        result_text.setText(strRes);
                    }else{
                        result_text.setText(R.string.cal_fail);
                        showToastIns(getApplicationContext(), getString(R.string.cal_fail), Toast.LENGTH_SHORT);
                    }

                    // 強制隱藏小鍵盤
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    break;

                case R.id.BMI_ClearBtn:
                    Log.w(TAG, "clear input");
                    input_heigth.setText("");
                    input_weight.setText("");
                    result_text.setText("");
                    break;

                case R.id.BMI_ReportBtn:
                    Log.w(TAG, "press report.");
                    height = Double.parseDouble(input_heigth.getText().toString());
                    weight = Double.parseDouble(input_weight.getText().toString());

                    Intent intent;

                    if (CalCulate_BMI(height,weight)) {    // 輸入正確值

                        // 儲存全域變數
                        GlobalVariable gv = (GlobalVariable)getApplicationContext();
                        gv.setPerson_height(height);
                        gv.setPerson_weight(weight);
                        gv.setPerson_BMI(BMI);

                        intent = new Intent(BMIActivity.this,ReportActivity.class);
                        startActivity(intent);
                    }
                    else{
                        result_text.setText(R.string.cal_fail);
                        showToastIns(getApplicationContext(), getString(R.string.cal_fail), Toast.LENGTH_SHORT);
                    }
                    break;
            }
        }
    };

    //=======================================================================
    // 監控EditText是否有輸入字，決定"計算"按鈕是否反灰
    //=======================================================================
    private TextWatcher InputWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (TextUtils.isEmpty(input_heigth.getText()) || TextUtils.isEmpty(input_weight.getText())) {
                result_button.setEnabled(false);
                report_button.setEnabled(false);
            }else {
                result_button.setEnabled(true);
                report_button.setEnabled(true);
            }
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private boolean CalCulate_BMI(double I_Height, double I_Weight)
    {
        try{
            double temp;
            // 計算出BMI值
            temp=I_Height/100;
            BMI = I_Weight / (temp * temp);
            return true;
        }catch (Exception obj){
            return false;
        }
    }

    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);

        if(resultCode == RESULT_OK && requestCode == ACTIVITY_REPORT)
        {
            Bundle bundle = intent.getExtras();
            String strResult;

            if (bundle != null) {
                strResult = getString(R.string.last_result)+bundle.getString("LAST_DATA");
            }else{
                strResult = getString(R.string.last_result);
            }
            Log.w(TAG, "last_result: "+strResult);
        }
    }*/
}
