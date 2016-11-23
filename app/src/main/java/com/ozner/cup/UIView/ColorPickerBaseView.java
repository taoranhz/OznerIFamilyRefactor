package com.ozner.cup.UIView;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by xinde on 2015/12/1.
 */
public class ColorPickerBaseView extends View {

    protected int mInitialColor = 0xffff0000;

    public interface OnColorChangedListener {
        void colorChanged(int color);
    }

    protected OnColorChangedListener mListener = null;
    protected Paint mCirclePaint;// 渐变色环画笔
    protected Paint mCenterPaint;// 中间圆画笔
    protected int mHeight;// View高
    protected int mWidth;// View宽
    protected float mCircleRadius;// 色环半径(paint中部)
    protected float mCenterRadius;// 中心圆半径

    protected boolean mDownInCircle = false;// 按在渐变环上
    protected boolean mHighlightCenter;// 高亮
    protected boolean mlittleLightCenter;// 微亮

    protected final int[] mCircleColors = new int[]{0xFFFF0000, 0xFFFF00FF, 0xFF0000FF,
            0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000};// 渐变色环颜色

    public ColorPickerBaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected int ave(int s, int d, float p) {
        return s + Math.round(p * (d - s));
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        this.mListener = listener;
    }

    protected float dpToPx( float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    protected float spToPx( float spValue) {
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 坐标是否在色环上
     *
     * @param x         坐标
     * @param y         坐标
     * @param outRadius 色环外半径
     * @param inRadius  色环内半径
     * @return
     */
    protected boolean inColorCircle(float x, float y, float outRadius,
                                    float inRadius) {
        double outCircle = Math.PI * outRadius * outRadius;
        double inCircle = Math.PI * inRadius * inRadius;
        double fingerCircle = Math.PI * (x * x + y * y);
        return (fingerCircle < outCircle && fingerCircle > inCircle);
    }

    /**
     * 坐标是否在中心圆上
     *
     * @param x            坐标
     * @param y            坐标
     * @param centerRadius 圆半径
     * @return
     */
    protected boolean inCenter(float x, float y, float centerRadius) {
        double centerCircle = Math.PI * centerRadius * centerRadius;
        double fingerCircle = Math.PI * (x * x + y * y);
        return (fingerCircle < centerCircle);
    }

    /**
     * 获取圆环上颜色
     *
     * @param colors
     * @param unit
     * @return
     */
    protected int interpCircleColor(int colors[], float unit) {
        if (unit <= 0) {
            return colors[0];
        }
        if (unit >= 1) {
            return colors[colors.length - 1];
        }

        float p = unit * (colors.length - 1);
        int i = (int) p;
        p -= i;

        // now p is just the fractional part [0...1) and i is the index
        int c0 = colors[i];
        int c1 = colors[i + 1];
        int a = ave(Color.alpha(c0), Color.alpha(c1), p);
        int r = ave(Color.red(c0), Color.red(c1), p);
        int g = ave(Color.green(c0), Color.green(c1), p);
        int b = ave(Color.blue(c0), Color.blue(c1), p);

        return Color.argb(a, r, g, b);
    }

    /**
     * 当手指按下时。
     *
     * @param inCircle 是否在色环上。
     * @param inCenter 是否在圆圈中心上。
     */
    protected void onActionDown(boolean inCircle, boolean inCenter) {
        mDownInCircle = inCircle;
        mHighlightCenter = inCenter;
    }

    /**
     * 当手指移动时。
     *
     * @param x        手指位置与圆心横坐标的相对距离。
     * @param y        手指位置与圆心纵坐标的相对距离。
     * @param inCircle 是否在色环中。
     * @param inCenter 是否在圆圈中。
     */
    protected void onActionMove(float x, float y, boolean inCircle,
                                boolean inCenter) {
        if (mDownInCircle && inCircle) {// down按在渐变色环内, 且move也在渐变色环内
            final float angle = (float) Math.atan2(y, x);
            float unit = (float) (angle / (2 * Math.PI));
            if (unit < 0) {
                unit += 1;
            }
            // mCenterPaint.setColor(interpCircleColor(mCircleColors, unit));
            mInitialColor = interpCircleColor(mCircleColors, unit);
        }
        if ((mHighlightCenter && inCenter)
                || (mlittleLightCenter && inCenter)) {// 点击中心圆,
            // 当前移动在中心圆
            mHighlightCenter = true;
            mlittleLightCenter = false;
        } else if (mHighlightCenter || mlittleLightCenter) {// 点击在中心圆,
            // 当前移出中心圆
            mHighlightCenter = false;
            mlittleLightCenter = true;
        } else {
            mHighlightCenter = false;
            mlittleLightCenter = false;
        }
        invalidate();
    }

    public void SetCenterColor(int Color) {
        //mCenterPaint.setColor(Color);
        mInitialColor = Color;
        this.invalidate();
    }

    public int GetCenterColor() {
        return mCenterPaint.getColor();
    }

    /**
     * 当手指松开时。
     *
     * @param inCenter 是否在圆圈中。
     */
    protected void onActionUp(boolean inCenter) {

//        if (mHighlightCenter && inCenter) {// 点击在中心圆, 且当前启动在中心圆
//            if(this.mListener!=null)
            this.mListener.colorChanged(mCenterPaint.getColor());
//        }
        if (mDownInCircle) {
            mDownInCircle = false;
        }
        if (mHighlightCenter) {
            mHighlightCenter = false;
        }
        if (mlittleLightCenter) {
            mlittleLightCenter = false;
        }
        invalidate();
    }


}
