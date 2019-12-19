package com.example.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import static com.example.myapplication.Util.showToastIns;

public class CheckBoxRadioBiuttonActivity extends AppCompatActivity {

    private RadioButton rb1, rb2, rb3, rb4, rb5, rb6, rb7;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_box_radio_biutton);

        CheckBox city1 = findViewById(R.id.CB_city1);
        CheckBox city2 = findViewById(R.id.CB_city2);
        CheckBox city3 = findViewById(R.id.CB_city3);
        CheckBox city4 = findViewById(R.id.CB_city4);
        CheckBox city5 = findViewById(R.id.CB_city5);
        CheckBox city6 = findViewById(R.id.CB_city6);
        CheckBox city7 = findViewById(R.id.CB_city7);
        CheckBox city8 = findViewById(R.id.CB_city8);
        CheckBox city9 = findViewById(R.id.CB_city9);
        CheckBox city10 = findViewById(R.id.CB_city10);
        city1.setOnCheckedChangeListener(mCheckBox);
        city2.setOnCheckedChangeListener(mCheckBox);
        city3.setOnCheckedChangeListener(mCheckBox);
        city4.setOnCheckedChangeListener(mCheckBox);
        city5.setOnCheckedChangeListener(mCheckBox);
        city6.setOnCheckedChangeListener(mCheckBox);
        city7.setOnCheckedChangeListener(mCheckBox);
        city8.setOnCheckedChangeListener(mCheckBox);
        city9.setOnCheckedChangeListener(mCheckBox);
        city10.setOnCheckedChangeListener(mCheckBox);

        rb1=findViewById(R.id.radioButton1);
        rb2=findViewById(R.id.radioButton2);
        rb3=findViewById(R.id.radioButton3);
        rb4=findViewById(R.id.radioButton4);
        rb5=findViewById(R.id.radioButton5);
        rb6=findViewById(R.id.radioButton6);
        rb7=findViewById(R.id.radioButton7);
        RadioGroup rg=findViewById(R.id.RadioGroup);
        rg.setOnCheckedChangeListener(mRadioButton);
    }

    private CompoundButton.OnCheckedChangeListener mCheckBox = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (b){
                showToastIns(getApplicationContext(), "選擇:"+compoundButton.getText().toString(), Toast.LENGTH_LONG);
            }else{
                showToastIns(getApplicationContext(), "取消:"+compoundButton.getText().toString(), Toast.LENGTH_LONG);
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener mRadioButton = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            String food="";
            switch (i){
                case R.id.radioButton1:
                    food=rb1.getText().toString();
                    break;
                case R.id.radioButton2:
                    food=rb2.getText().toString();
                    break;
                case R.id.radioButton3:
                    food=rb3.getText().toString();
                    break;
                case R.id.radioButton4:
                    food=rb4.getText().toString();
                    break;
                case R.id.radioButton5:
                    food=rb5.getText().toString();
                    break;
                case R.id.radioButton6:
                    food=rb6.getText().toString();
                    break;
                case R.id.radioButton7:
                    food=rb7.getText().toString();
                    break;
            }
            showToastIns(getApplicationContext(), "選了:"+food, Toast.LENGTH_LONG);
        }
    };
}
