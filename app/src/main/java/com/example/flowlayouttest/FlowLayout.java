package com.example.flowlayouttest;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class FlowLayout extends ViewGroup {
    private static final String TAG = "FlowLayout";
    private int mHorizontalSpacing = dp2px(16); // 每个item横向间距
    private int mVerticalSpacing = dp2px(8); // 行纵向间距

    private List<List<View>> allLines = new ArrayList<>(); // 记录所有的行，一行一行的存储，用于layout
    List<Integer> lineHeights = new ArrayList<>(); // 记录每一行的行高，用于layout

    public FlowLayout(Context context) {
        super(context);
    }
    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    private void clearMeasureParams(){
        allLines.clear();
        lineHeights.clear();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 多次执行onMeasure时，需要清空
        clearMeasureParams();

        // 用于保存每一行的子view
        List<View> lineViews = new ArrayList<>(); // 保存一行中的所有的view
        int lineWidthUsed = 0;  // 记录这行已经使用了多宽的size
        int lineHeight = 0;     // 一行view的行高

        int parentNeededWidth = 0;  // measure过程中，子View要求的父ViewGroup的宽
        int parentNeedHeight = 0;   // measure过程中，子View要求的父ViewGroup的高

        int paddingLeft = getPaddingLeft(); // 父容器的padding
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        int selfWidth = MeasureSpec.getSize(widthMeasureSpec); // ViewGroup解析的父亲给我的宽度
        int selfHeight = MeasureSpec.getSize(heightMeasureSpec); // ViewGroup解析的父亲给我的高度

        // 先度量子view
        int childCount = getChildCount(); // 父容器下有多少个直接子view的个数，不包含子元素内部包含的元素个数

        for (int i = 0; i < childCount; i++) {
            // 每个子view
            View childView = getChildAt(i);
            // 每个子view的LayoutParams
            LayoutParams childLP = childView.getLayoutParams();
            // 判断每个子view是否为GONE
            if (childView.getVisibility() != View.GONE){
                // 通过子view的LayoutParams，获取子view 的 MeasureSpec
                int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, paddingLeft + paddingRight, childLP.width);
                int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, paddingBottom + paddingTop, childLP.height);
                // 进行度量子view
                childView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                // 获取子view的度量宽/高
                int childMesauredWidth = childView.getMeasuredWidth();
                int childMeasuredHeight = childView.getMeasuredHeight();

                // 进行判断是否一行可以容得下，是否换行操作
                // 当前子view的宽 + 已经占用的子view的宽 + 间距 如果大于 父容器的宽，就换行
                if (childMesauredWidth + lineWidthUsed + mHorizontalSpacing > selfWidth){
                    // 一旦换行，我们就可以判断当前行需要的宽和高了，所以此时要记录下来
                    allLines.add(lineViews);
                    lineHeights.add(lineHeight);

                    parentNeedHeight = parentNeedHeight + lineHeight + mVerticalSpacing;
                    parentNeededWidth = Math.max(parentNeededWidth, lineWidthUsed+mHorizontalSpacing);

                    lineViews = new ArrayList<>();
                    lineWidthUsed = 0;
                    lineHeight = 0;
                }

                lineViews.add(childView);
                // 每行的宽高
                lineWidthUsed = lineWidthUsed + childMesauredWidth + mHorizontalSpacing;
                lineHeight = Math.max(lineHeight , childMeasuredHeight);

                // 处理最后一行数据
                if (i == childCount-1){
                    allLines.add(lineViews);
                    lineHeights.add(lineHeight);
                    parentNeedHeight = parentNeedHeight + lineHeight + mVerticalSpacing;
                    parentNeededWidth = Math.max(parentNeededWidth, lineWidthUsed+mHorizontalSpacing);
                }
            }
        }

        // 度量父容器
        // 根据子view的测量结果，来重新度量自己的viewGroup
        // 作为一个ViewGroup，它自己也是一个View,它的大小也需要根据它的父亲给它提供的宽高来度量
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int realWidth = (widthMode == MeasureSpec.EXACTLY) ? selfWidth : parentNeededWidth;
        int realHeight = (heightMode == MeasureSpec.EXACTLY) ? selfHeight : parentNeedHeight;

        setMeasuredDimension(realWidth, realHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 一共几行
        int lineCount = allLines.size();
        // padding
        int currentLeft = getPaddingLeft();
        int currentTop = getPaddingTop();

        // 一行行布局展示
        for (int i = 0; i < lineCount; i++) {
            // 获取每一行的子view
            List<View> lineViews = allLines.get(i);
            // 获取每一行的高
            Integer lineHeight = lineHeights.get(i);

            for (int j = 0; j < lineViews.size(); j++) {
                // 获取每一个子view
                View view = lineViews.get(j);
                int left = currentLeft;
                int top = currentTop;
                int right = left + view.getMeasuredWidth();
                int bottom = top +view.getMeasuredHeight();

                view.layout(left,top,right,bottom);

                currentLeft = right + mHorizontalSpacing;
            }
            currentTop = currentTop + lineHeight + mVerticalSpacing;
            currentLeft = getPaddingLeft();
        }
    }

    public static int dp2px(int dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }
}
