package com.example.myapplication;
/*
    Author: Alan Huang
*/

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.Objects;

public class FragmentDialog extends DialogFragment {

    private static final String TAG="Alan";
    private int AlertStyle=0;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Log.w(TAG, "FragmentDialog:onCreateDialog");

        if (getArguments() != null) {
            if (getArguments().getInt("AlertDialog")==0) {
                return super.onCreateDialog(savedInstanceState);
            }else{
                if (getArguments().getInt("AlertDialog")==2)
                    AlertStyle=1;
                else
                    AlertStyle=0;
            }
        }

        Log.w(TAG, "AlertStyle="+AlertStyle);

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        if (AlertStyle==1) {    // 結果跟Simple Dialog Fragment一樣
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view= inflater.inflate(R.layout.fragment_dialog, null);

            Button btn=view.findViewById(R.id.btnDone);
            final EditText editText = view.findViewById(R.id.inEmail);

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 將email傳回activity
                    DialogListener dialogListener = (DialogListener) getActivity();

                    if (getArguments() != null && !TextUtils.isEmpty(getArguments().getString("email")))
                        editText.setText(getArguments().getString("email"));

                    if (dialogListener != null) {
                        dialogListener.onFinishEditDialog(editText.getText().toString());
                    }
                    dismiss();
                }
            });

            // 設定title及message可省略
            builder.setTitle(R.string.about_app2);  // 關於(使用DialogFragment)
            builder.setMessage(R.string.about_detail);  // 這是一支計算BMI的小程式
            builder.setView(view);
        }else {

            builder.setTitle(R.string.about_app2);  // 關於(使用DialogFragment)
            builder.setMessage(R.string.about_detail);  // 這是一支計算BMI的小程式
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dismiss();
                }
            });

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dismiss();
                }
            });
        }
        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.w(TAG, "FragmentDialog:onCreateView");
        return inflater.inflate(R.layout.fragment_dialog, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.w(TAG, "FragmentDialog:onViewCreated");

        final EditText editText = view.findViewById(R.id.inEmail);

        if (getArguments() != null && !TextUtils.isEmpty(getArguments().getString("email")))
            editText.setText(getArguments().getString("email"));

        Button btnDone = view.findViewById(R.id.btnDone);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.w(TAG, "Done onClick");

                DialogListener dialogListener = (DialogListener) getActivity();
                if (dialogListener != null) {
                    dialogListener.onFinishEditDialog(editText.getText().toString());
                }
                dismiss();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.w(TAG, "FragmentDialog:onResume");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(TAG, "FragmentDialog:onCreate");

        boolean setFullScreen = false;
        if (getArguments() != null) {
            setFullScreen = getArguments().getBoolean("fullScreen");
        }

        if (setFullScreen)
            setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.w(TAG, "FragmentDialog:onDestroyView");
    }

    public interface DialogListener {
        void onFinishEditDialog(String inputText);
    }
}
