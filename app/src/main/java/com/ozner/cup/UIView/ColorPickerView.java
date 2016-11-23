package com.ozner.cup.UIView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by xinde on 2015/12/1.
 */
public class ColorPickerView extends ColorPickerBaseView {
    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    float clickX = 0;
    float clickY = 0;
    boolean isSmallCircle = false;
    final int GrayColor = 0xfffafafa;

    private void drawInit(Canvas canvas) {
        Shader s = new SweepGradient(0, 0, mCircleColors, null);
//        mHeight = this.getHeight();
//        mWidth = this.getWidth();
        //渐变色环初始化
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setShader(s);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(dpToPx(20));

        mCircleRadius = (float) (Math.min(mWidth, mHeight) / 2 * 0.8 - mCirclePaint.getStrokeWidth() * 0.5f);
        //中心圆参数初始化
        mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterPaint.setStrokeWidth(5);

        mCenterPaint.setColor(mInitialColor);
        mCenterRadius = (mCircleRadius - mCirclePaint.getStrokeWidth() / 2) * 0.6f;
        //移动中心
        canvas.translate(this.getWidth() / 2, this.getHeight() / 2);
    }

    private void drawClickCircle(Canvas canvas) {
        Point point = touchToDrawPoing(clickX, clickY);
        float radius = mCirclePaint.getStrokeWidth() / 2;
        Paint smallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        smallPaint.setStyle(Paint.Style.FILL);

        float offset = dpToPx(10);


        smallPaint.setColor(mInitialColor);
        //  canvas.drawOval(point.x - radius - offset, point.y - radius - offset, point.x + radius + offset, point.y + radius + offset, smallPaint);

        canvas.drawCircle(point.x - radius + offset, point.y - radius + offset, radius + offset, smallPaint);
        smallPaint.setColor(Color.WHITE);
        canvas.drawCircle(point.x - radius + offset, point.y - radius + offset, radius, smallPaint);
        //    canvas.drawOval(point.x - radius, point.y - radius, point.x + radius, point.y + radius, smallPaint);

    }

    public void DrawOval(Canvas canvas) {

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int defalutWidth = (int)dpToPx(200);
        int defaultHeight = (int)dpToPx(200);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width, height;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.max(defalutWidth, widthSize);
        } else if (widthMode == MeasureSpec.UNSPECIFIED) {
            width = defalutWidth;
        } else {
            width = defalutWidth;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(defaultHeight, heightSize);
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            height = defaultHeight;
        } else {
            height = defaultHeight;
        }

//        height = width / 2;

        setMeasuredDimension(width, height);
    }

    private Point touchToDrawPoing(double x, double y) {
        double degree = Math.atan2(-y, x);
        double radius = mCircleRadius;
        double centerX = radius * Math.cos(degree);
        double centerY = radius * Math.sin(degree);
        Point point = new Point();
        point.x = (int) centerX;
        point.y = (int) (y > 0 ? Math.abs(centerY) : -Math.abs(centerY));

        return point;
    }

    private void drawColorPicker(Canvas canvas) {
//        //移动中心
//        canvas.translate(this.getWidth()/2,this.getHeight()/2 - 50);

        Paint bgCenterPaint = new Paint(mCenterPaint);
        bgCenterPaint.setColor(GrayColor);
        canvas.drawCircle(0, 0, mCenterRadius + dpToPx(15), bgCenterPaint);

        //画中心圆
        canvas.drawCircle(0, 0, mCenterRadius, mCenterPaint);

        // 是否显示中心圆外的小圆环
        if (mHighlightCenter || mlittleLightCenter) {
            int c = mCenterPaint.getColor();
            mCenterPaint.setStyle(Paint.Style.STROKE);
            if (mHighlightCenter) {
                mCenterPaint.setAlpha(0xFF);
            } else if (mlittleLightCenter) {
                mCenterPaint.setAlpha(0x90);
            }
            canvas.drawCircle(0, 0,
                    mCenterRadius + mCenterPaint.getStrokeWidth(),
                    mCenterPaint);

            mCenterPaint.setStyle(Paint.Style.FILL);
            mCenterPaint.setColor(c);
        }

        // 画色环
        canvas.drawOval(new RectF(-mCircleRadius, -mCircleRadius,
                mCircleRadius, mCircleRadius), mCirclePaint);
        //画中心圆
        //canvas.drawOval(new RectF(0, 0, 50, 50), mCenterPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.drawColor(Color.WHITE);
        drawInit(canvas);
        drawColorPicker(canvas);
        if (mDownInCircle)
            drawClickCircle(canvas);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - mWidth / 2;
        float y = event.getY() - mHeight / 2 + 50;
        clickX = x;
        clickY = y;
        boolean inCircle = inColorCircle(x, y,
                mCircleRadius + mCirclePaint.getStrokeWidth() / 2,
                mCircleRadius - mCirclePaint.getStrokeWidth() / 2);
        boolean inCenter = inCenter(x, y, mCenterRadius);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onActionDown(inCircle, inCenter);
            case MotionEvent.ACTION_MOVE:
                onActionMove(x, y, inCircle, inCenter);
                break;
            case MotionEvent.ACTION_UP:
                onActionUp(inCenter);
                break;
        }

        return true;

    }
}
