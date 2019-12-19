package com.example.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import static com.example.myapplication.Util.showToastIns;

public class CheckBoxRadioBiuttonActivity extends AppCompatActivity {

    private RadioButton rb1, rb2, rb3, rb4, rb5, rb6, rb7;
    private CheckBox city1, city2, city3, city4, city5, city6, city7, city8, city9, city10;
    private static final String TAG="Alan";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_box_radio_biutton);

        city1 = findViewById(R.id.CB_city1);
        city2 = findViewById(R.id.CB_city2);
        city3 = findViewById(R.id.CB_city3);
        city4 = findViewById(R.id.CB_city4);
        city5 = findViewById(R.id.CB_city5);
        city6 = findViewById(R.id.CB_city6);
        city7 = findViewById(R.id.CB_city7);
        city8 = findViewById(R.id.CB_city8);
        city9 = findViewById(R.id.CB_city9);
        city10 = findViewById(R.id.CB_city10);
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

        Button result = findViewById(R.id.CB_Result);
        result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showResult();
            }
        });
    }

    //---------------------------------------------------------------------
    // check box event
    //---------------------------------------------------------------------
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

    //---------------------------------------------------------------------
    // Radio button event
    //---------------------------------------------------------------------
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

    private void showResult(){
        StringBuilder selectedData = new StringBuilder();
        int selectedCount = 0;

        boolean[] itemselect=new boolean[]{false, false, false, false, false,
                                           false, false, false, false, false};
        itemselect[0]=city1.isChecked();
        itemselect[1]=city2.isChecked();
        itemselect[2]=city3.isChecked();
        itemselect[3]=city4.isChecked();
        itemselect[4]=city5.isChecked();
        itemselect[5]=city6.isChecked();
        itemselect[6]=city7.isChecked();
        itemselect[7]=city8.isChecked();
        itemselect[8]=city9.isChecked();
        itemselect[9]=city10.isChecked();

        for (int i=0; i<10; i++) {
            //Log.w(TAG, "item "+i+" :"+itemselect[i]);

            if (itemselect[i]) {
                selectedData.append(getResources().getStringArray(R.array.array_Places)[i]).append(",");
                selectedCount++;
            }
        }
        showToastIns(this, "總共選取"+selectedCount+"個項目..."+selectedData, Toast.LENGTH_LONG);
    }
}
