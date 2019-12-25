package com.example.myapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DialogFragmentActivity extends AppCompatActivity implements View.OnClickListener, FragmentDialog.DialogListener{

    Button btnEmbedDialogFragment, btnDialogFragment, btnDialogFragmentFullScreen, btnAlertDialogFragment;
    Button btnLayoutAlert;
    TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_fragment);

        textView = findViewById(R.id.textView);
        btnEmbedDialogFragment = findViewById(R.id.btnEmbedDialogFragment);
        btnDialogFragment = findViewById(R.id.btnDialogFragment);
        btnDialogFragmentFullScreen = findViewById(R.id.btnDialogFragmentFullScreen);
        btnAlertDialogFragment = findViewById(R.id.btnAlertDialogFragment);
        btnLayoutAlert = findViewById(R.id.btnLayoutAlertDialog);

        btnEmbedDialogFragment.setOnClickListener(this);
        btnDialogFragment.setOnClickListener(this);
        btnDialogFragmentFullScreen.setOnClickListener(this);
        btnAlertDialogFragment.setOnClickListener(this);
        btnLayoutAlert.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnEmbedDialogFragment:
                FragmentDialog dialogFragment = new FragmentDialog();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frameLayout, dialogFragment);   // 將fragment_dialog.xml的layout放在此activity layout的framelayout裡
                ft.commit();
                break;

            case R.id.btnDialogFragment:
                dialogFragment = new FragmentDialog();

                Bundle bundle = new Bundle();
                bundle.putInt("AlertDialog", 0);
                dialogFragment.setArguments(bundle);

                ft = getSupportFragmentManager().beginTransaction();
                Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                dialogFragment.show(ft, "dialog");
                break;

            case R.id.btnDialogFragmentFullScreen:
                dialogFragment = new FragmentDialog();

                bundle = new Bundle();
                bundle.putString("email", "Email Address");
                bundle.putBoolean("fullScreen", true);
                bundle.putInt("AlertDialog", 0);
                dialogFragment.setArguments(bundle);

                ft = getSupportFragmentManager().beginTransaction();
                prev = getSupportFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                dialogFragment.show(ft, "dialog");
                break;

            case R.id.btnAlertDialogFragment:
                dialogFragment = new FragmentDialog();

                bundle = new Bundle();
                bundle.putInt("AlertDialog", 1);
                dialogFragment.setArguments(bundle);

                ft = getSupportFragmentManager().beginTransaction();
                prev = getSupportFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                dialogFragment.show(ft, "dialog");
                break;

            case R.id.btnLayoutAlertDialog:
                dialogFragment = new FragmentDialog();

                bundle = new Bundle();
                bundle.putInt("AlertDialog", 2);
                dialogFragment.setArguments(bundle);

                ft = getSupportFragmentManager().beginTransaction();
                prev = getSupportFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                dialogFragment.show(ft, "dialog");
                break;
        }
    }

    @Override
    public void onFinishEditDialog(String inputText) {

        if (TextUtils.isEmpty(inputText)) {
            textView.setText("");
        } else {
            String str=getString(R.string.account)+":"+inputText;
            textView.setText(str);
        }
    }
}
