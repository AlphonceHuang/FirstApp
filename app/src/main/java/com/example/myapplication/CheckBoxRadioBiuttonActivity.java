package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import static com.example.myapplication.Util.showToastIns;

public class CheckBoxRadioBiuttonActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Alan";
    private RadioButton rb1, rb2, rb3, rb4, rb5, rb6, rb7;
    private AppCompatCheckBox city1, city2, city3;
    private CheckBox city4, city5, city6, city7, city8, city9, city10;
    private String food = "";
    private ImageView img;

    private ImageButton imgbtn;
    private boolean flag = false;
    private Bitmap stop, play;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_box_radio_biutton);

        stop = BitmapFactory.decodeResource(this.getResources(), R.drawable.kitty039);
        play = BitmapFactory.decodeResource(this.getResources(), R.drawable.kitty041);

        // Check box
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

        // Radio button
        rb1 = findViewById(R.id.radioButton1);
        rb2 = findViewById(R.id.radioButton2);
        rb3 = findViewById(R.id.radioButton3);
        rb4 = findViewById(R.id.radioButton4);
        rb5 = findViewById(R.id.radioButton5);
        rb6 = findViewById(R.id.radioButton6);
        rb7 = findViewById(R.id.radioButton7);
        RadioGroup rg = findViewById(R.id.RadioGroup);
        rg.setOnCheckedChangeListener(mRadioButton);

        Button result = findViewById(R.id.CB_Result);
        result.setOnClickListener(this);
        result.setPaintFlags(result.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG); // underline text

        /*
        result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showResult();
            }
        });*/

        // Image Button
        imgbtn = findViewById(R.id.imageButton);
        imgbtn.setImageResource(R.drawable.stop_1);
        imgbtn.setOnClickListener(this);
        flag = false;
        /*
        imgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag){
                    imgbtn.setImageResource(R.drawable.pause);
                    flag=false;
                    showToastIns(getApplicationContext(), getString(R.string.pause), Toast.LENGTH_LONG);

                }else{
                    imgbtn.setImageResource(R.drawable.playbutton);
                    flag=true;
                    showToastIns(getApplicationContext(), getString(R.string.play), Toast.LENGTH_LONG);
                }
            }
        });*/

        // Toggle Button
        ToggleButton tgb = findViewById(R.id.toggleButton);
        tgb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    showToastIns(getApplicationContext(), getString(R.string.start), Toast.LENGTH_LONG);
                } else {
                    showToastIns(getApplicationContext(), getString(R.string.stop), Toast.LENGTH_LONG);
                }
            }
        });

        // ImageView
        img = findViewById(R.id.imageViewTest);
        img.setImageBitmap(DrawableUtils.CreateReflectionImageWithOrigin(stop));
        img.setOnClickListener(this);

        // TextView
        TextView check = findViewById(R.id.checkboxTitle);
        check.setPaintFlags(check.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG); // underline text 1

        TextView radio = findViewById(R.id.RadioGroupTitle);
        radio.getPaint().setUnderlineText(true);    // underline text 2

        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(255);    // 設定最大值
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Log.w(TAG, "onProgressChanged:"+i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.w(TAG, "onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.w(TAG, "onStopTrackingTouch");
            }
        });

        SwitchCompat newSwitch=findViewById(R.id.switch1);
        newSwitch.setOnCheckedChangeListener(TestSW);
        Switch oldSwitch=findViewById(R.id.switch2);
        oldSwitch.setOnCheckedChangeListener(TestSW);
    }

    private CompoundButton.OnCheckedChangeListener TestSW= new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (R.id.switch1==compoundButton.getId()) {
                Log.w(TAG, "SwitchCompat:"+b);
            }else if (R.id.switch2 == compoundButton.getId()){
                Log.w(TAG, "Switch:"+b);
            }
        }
    };

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.imageButton:
            case R.id.imageViewTest:
                if (flag) {
                    imgbtn.setImageResource(R.drawable.stop_1);
                    img.setImageBitmap(DrawableUtils.CreateReflectionImageWithOrigin(stop));
                    flag = false;
                    showToastIns(getApplicationContext(), getString(R.string.pause), Toast.LENGTH_LONG);
                } else {
                    imgbtn.setImageResource(R.drawable.play_1);
                    img.setImageBitmap(DrawableUtils.CreateReflectionImageWithOrigin(play));
                    flag = true;
                    showToastIns(getApplicationContext(), getString(R.string.play), Toast.LENGTH_LONG);
                }
                break;
            case R.id.CB_Result:
                showResult();
                break;
        }

    }

    //---------------------------------------------------------------------
    // check box event
    //---------------------------------------------------------------------
    private CompoundButton.OnCheckedChangeListener mCheckBox = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (b) {
                showToastIns(getApplicationContext(), "選擇:" + compoundButton.getText(), Toast.LENGTH_LONG);
            } else {
                showToastIns(getApplicationContext(), "取消:" + compoundButton.getText(), Toast.LENGTH_LONG);
            }
            //Log.w(TAG, "compoundButton:"+compoundButton.getId());

        }
    };

    //---------------------------------------------------------------------
    // Radio button event
    //---------------------------------------------------------------------
    private RadioGroup.OnCheckedChangeListener mRadioButton = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {

            switch (i) {
                case R.id.radioButton1:
                    food = rb1.getText().toString();
                    break;
                case R.id.radioButton2:
                    food = rb2.getText().toString();
                    break;
                case R.id.radioButton3:
                    food = rb3.getText().toString();
                    break;
                case R.id.radioButton4:
                    food = rb4.getText().toString();
                    break;
                case R.id.radioButton5:
                    food = rb5.getText().toString();
                    break;
                case R.id.radioButton6:
                    food = rb6.getText().toString();
                    break;
                case R.id.radioButton7:
                    food = rb7.getText().toString();
                    break;
            }
            showToastIns(getApplicationContext(), "選了:" + food, Toast.LENGTH_LONG);
        }
    };

    private void showResult() {
        StringBuilder selectedData = new StringBuilder();
        int selectedCount = 0;

        boolean[] itemselect = new boolean[]{false, false, false, false, false,
                false, false, false, false, false};
        itemselect[0] = city1.isChecked();
        itemselect[1] = city2.isChecked();
        itemselect[2] = city3.isChecked();
        itemselect[3] = city4.isChecked();
        itemselect[4] = city5.isChecked();
        itemselect[5] = city6.isChecked();
        itemselect[6] = city7.isChecked();
        itemselect[7] = city8.isChecked();
        itemselect[8] = city9.isChecked();
        itemselect[9] = city10.isChecked();

        for (int i = 0; i < 10; i++) {
            //Log.w(TAG, "item "+i+" :"+itemselect[i]);

            if (itemselect[i]) {
                selectedData.append(getResources().getStringArray(R.array.array_Places)[i]).append(",");
                selectedCount++;
            }
        }
        selectedData.append(" 並且選擇吃").append(food);
        showToastIns(this, "總共選取" + selectedCount + "個項目..." + selectedData, Toast.LENGTH_LONG);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}