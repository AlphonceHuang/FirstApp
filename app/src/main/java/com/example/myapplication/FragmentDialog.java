package com.example.myapplication;
/*
    Author: Alan Huang
*/

import android.annotation.SuppressLint;
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

/* 啟動順序
FragmentDialog:onCreate
FragmentDialog:onCreateDialog
FragmentDialog:onCreateView
FragmentDialog:onViewCreated
FragmentDialog:onResume
FragmentDialog:onDestroyView
*/

public class FragmentDialog extends DialogFragment {

    private static final String TAG="Alan";
    private int AlertStyle=-1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(TAG, "FragmentDialog:onCreate");

        if (getArguments() != null) {
            AlertStyle = getArguments().getInt("AlertDialog");
        }
        Log.w(TAG, "AlertStyle="+AlertStyle);

        boolean setFullScreen = false;
        if (getArguments() != null) {
            setFullScreen = getArguments().getBoolean("fullScreen");
        }

        if (setFullScreen)
            setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Log.w(TAG, "FragmentDialog:onCreateDialog");

        if (AlertStyle==2) {    // 結果跟Simple Dialog Fragment一樣
            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
            LayoutInflater inflater = getActivity().getLayoutInflater();
            @SuppressLint("InflateParams") View view= inflater.inflate(R.layout.fragment_dialog, null);

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
            builder.setIcon(R.drawable.kitty010);
            builder.setTitle(R.string.about_app2);  // 關於(使用DialogFragment)
            builder.setMessage(R.string.about_detail);  // 這是一支計算BMI的小程式
            builder.setView(view);
            return builder.create();

        }else if (AlertStyle==1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
            builder.setIcon(R.drawable.kitty010);
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
            return builder.create();
        }
        return super.onCreateDialog(savedInstanceState);
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

        if (AlertStyle==0) {
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
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.w(TAG, "FragmentDialog:onResume");
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
