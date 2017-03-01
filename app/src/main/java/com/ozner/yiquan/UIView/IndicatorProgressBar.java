package com.ozner.yiquan.UIView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by xinde on 2016/6/5.
 */
public class IndicatorProgressBar extends View {
    private final int thumbWidth = dpToPx(6);
    private static int startColor = 0xff3387f9;
    private static int centerColor = 0xff6b3ee1;
    private static int endColor = 0xfff83636;
    private static int textColor = 0xff3387f9;
    private static int lineColor = 0xffc9c9c9;
    private Paint textPaint, bgPaint, valuePaint;
    private int linwidth = 5;
    private int value = 0;
    private int maxValue = 100;
    private Bitmap thumb = null;

    public IndicatorProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setThumb(int id) {
        this.thumb = BitmapFactory.decodeResource(getResources(), id);
        this.invalidate();
    }

    public int getProgress() {
        return this.value;
    }

    public void setProgress(int progress) {
        if (progress < 0) {
            this.value = 0;
        } else if (progress > maxValue) {
            this.value = maxValue;
        } else {
            this.value = progress;
        }
        this.invalidate();
    }

    public void setMaxProgress(int max) {
        this.maxValue = max;
        this.invalidate();
    }

    public int getMaxProgress() {
        return this.maxValue;
    }

    private void init() {
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(textColor);

        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setStrokeWidth(dpToPx(linwidth));
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeCap(Paint.Cap.ROUND);

        valuePaint = new Paint();
        valuePaint.setAntiAlias(true);
        valuePaint.setStrokeWidth(dpToPx(linwidth));
        valuePaint.setStyle(Paint.Style.STROKE);
        valuePaint.setStrokeCap(Paint.Cap.ROUND);
        valuePaint.setColor(lineColor);
    }

    private void drawVlueLine(Canvas canvas, RectF rect, float value) {
        float offset = (valuePaint.getStrokeWidth() + thumbWidth) / 2;

        float lineLenght = rect.width() - offset * 2;
        float valueWidth = (value / maxValue) * lineLenght;
        float remain = lineLenght - valueWidth;
        if (value < 0 || value > maxValue) {
            valueWidth = lineLenght;
            remain = 0;
        }
        //绘制进度
        canvas.drawLine(rect.left + offset, rect.top + dpToPx(23), rect.right - offset - remain, rect.top + dpToPx(23), valuePaint);

        //绘制滑动块
        if (null != thumb) {
            thumb = Bitmap.createScaledBitmap(thumb, (int) dpToPx(10), (int) dpToPx(14), true);
            canvas.drawBitmap(thumb, rect.left + offset + valueWidth - thumb.getWidth() / 2, rect.top + dpToPx(5), textPaint);
        } else {
            Path path = new Path();
            path.moveTo(rect.left + offset + valueWidth, rect.top + dpToPx(18));
            path.lineTo(rect.left + offset + valueWidth - dpToPx(5), rect.top + dpToPx(6));
            path.lineTo(rect.left + offset + valueWidth + dpToPx(5), rect.top + dpToPx(6));
            path.lineTo(rect.left + offset + valueWidth, rect.top + dpToPx(18));
            path.close();
            canvas.drawPath(path, textPaint);
        }
    }

    private void drawBackgroundLine(Canvas canvas, RectF rect) {

        bgPaint.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.top,
                new int[]{startColor, centerColor, endColor}, null, Shader.TileMode.REPEAT));
        float offset = (valuePaint.getStrokeWidth() + thumbWidth) / 2;
        canvas.drawLine(rect.left + offset, rect.top + dpToPx(23), rect.right - offset, rect.top + dpToPx(23), bgPaint);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF rect = new RectF(0, 0, this.getWidth(), this.getHeight());
        drawBackgroundLine(canvas, rect);
        drawVlueLine(canvas, rect, value);
    }

    protected int dpToPx(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
