package com.example.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class CustomProgress {

    private static CustomProgress customProgress = null;
    private Dialog mDialog;

    public static CustomProgress getInstance() {
        if (customProgress == null) {
            customProgress = new CustomProgress();
        }
        return customProgress;
    }

    public void showProgress(Context context, String message, boolean cancelable) {
        mDialog = new Dialog(context);

        // no tile for the dialog
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);  // 這個沒有也不影響
        mDialog.setContentView(R.layout.progress_bar_dialog);   // 使用此layout
        ProgressBar mProgressBar = mDialog.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);

        // 動畫
        ImageView mAnimation = mDialog.findViewById(R.id.progress_anime);
        if (mAnimation.getDrawable()==null)
            mAnimation.setImageResource(R.drawable.amin_pgbar);   // 菊花樣式
        AnimationDrawable frameAnimation = (AnimationDrawable) mAnimation.getDrawable();
        frameAnimation.start(); // 開始動畫

        //  mProgressBar.getIndeterminateDrawable().setColorFilter(context.getResources()
        // .getColor(R.color.material_blue_gray_500), PorterDuff.Mode.SRC_IN);



        // 如果需做用帶入文字，將下面打開，不然就只有使用layout裡面的TextView
        //TextView progressText = (TextView) mDialog.findViewById(R.id.progress_text);
        //progressText.setText("" + message);
        //progressText.setVisibility(View.INVISIBLE);

        // you can change or add this line according to your need
        mProgressBar.setIndeterminate(true);
        mDialog.setCancelable(cancelable);
        mDialog.setCanceledOnTouchOutside(cancelable);
        mDialog.show();
    }

    public void hideProgress() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
}
