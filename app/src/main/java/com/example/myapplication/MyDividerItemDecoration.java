package com.example.myapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

public class MyDividerItemDecoration extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider

    };
    public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;
    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;
    private Drawable mDivider;
    private int mOrientation;

    public MyDividerItemDecoration(Context context, int orientation) {
        // 获取默认主题的属性
        final TypedArray a = context.obtainStyledAttributes(ATTRS);

        mDivider = a.getDrawable(0);
        a.recycle();
        setOrientation(orientation);
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        // 绘制间隔
        if (mOrientation == VERTICAL_LIST) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    /*
    设置item的偏移量，偏移的部分用于填充间隔样式，在RecyclerView的onMesure()中会调用该方法
    */
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (mOrientation == VERTICAL_LIST) {
            outRect.set(50, 0, 50, mDivider.getIntrinsicHeight()+50);
        } else {
            outRect.set(0, 150, mDivider.getIntrinsicWidth()+50, 150);
        }
    }

    private void setOrientation(int orientation) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
            throw new IllegalArgumentException("invalid orientation");
        }
        mOrientation = orientation;
    }

    private void drawVertical(Canvas c, RecyclerView parent) {
        final int left = parent.getPaddingLeft()+50;
        final int right = parent.getWidth() - parent.getPaddingRight()+50;
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin +
                    Math.round(ViewCompat.getTranslationY(child)+50);
            final int bottom = top + mDivider.getIntrinsicHeight()-3;
            //Log.w("Alan", "left="+left+",top="+top+",right="+right+",bottom"+bottom);
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    private void drawHorizontal(Canvas c, RecyclerView parent) {
        final int top = parent.getPaddingTop()+150;
        final int bottom = parent.getHeight() - parent.getPaddingBottom()+150;
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int left = child.getRight() + params.rightMargin +
                    Math.round(ViewCompat.getTranslationX(child));
            final int right = left + mDivider.getIntrinsicHeight()-3;
            Log.w("Alan", "left="+left+",top="+top+",right="+right+",bottom"+bottom);
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}
