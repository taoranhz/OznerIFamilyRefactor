package com.ozner.cup.UIView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.ozner.cup.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by xinde on 2015/12/5.
 * <p>
 * 滤芯使用进度
 */
public class FilterProgressView extends View {
    private static final String TAG = "FilterProgressView";
    private static int startColor = 0xff3387f9;
    private static int centerColor = 0xff6b3ee1;
    private static int endColor = 0xfff83636;
    private static int textColor = 0xff3387f9;
    private static int lineColor = 0xffc9c9c9;
    private Paint textPaint, bgPaint, valuePaint;
    private int textSizeYear = 14;
    private int textSizeMonthDay = 16;
    private int linwidth = 5;
    private float textWidthMonth = 0;
    private float textWidthYear = 0;
    private float textHeightMonth = 0;
    private float textHeightYear = 0;
    private float value = 0;
    private Date startTime, endTime;
    private int warranty = 30;//保修期 /天
    private GregorianCalendar gc = new GregorianCalendar();
    private SimpleDateFormat yearFormat;
    private SimpleDateFormat monthDayFormat;
    private Bitmap thumb = null;
    private boolean isShowTime = true;//是否显示时间

    public FilterProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.FilterProgress);
        isShowTime = array.getBoolean(R.styleable.FilterProgress_IsShowTime, true);
        array.recycle();
        init();
    }

    //更新保修时间
    public void update(float days) {
        this.value = days;
        this.invalidate();
    }

    //更新保修时间
    public void update(Date date) {
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(date);
        dateCal.set(Calendar.HOUR_OF_DAY, 0);
        dateCal.set(Calendar.MINUTE, 0);
        dateCal.set(Calendar.SECOND, 0);
        dateCal.set(Calendar.MILLISECOND, 0);
        this.value = (int) ((dateCal.getTimeInMillis() - startTime.getTime()) / (24 * 3600 * 1000));
        Log.w(TAG, TAG + "update: date,value=" + value);
        this.invalidate();
    }

    public void setThumb(int id) {
        this.thumb = BitmapFactory.decodeResource(getResources(), id);
        this.invalidate();
    }

    public void setShowTime(boolean isShow) {
        this.isShowTime = isShow;
        this.invalidate();
    }

    public float getValue() {
        return this.value;
    }

    public float getWarranty() {
        return this.warranty;
    }

    public void initTime(Date proTime, Date stopTime) {
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(proTime);
        dateCal.set(Calendar.HOUR_OF_DAY, 0);
        dateCal.set(Calendar.MINUTE, 0);
        dateCal.set(Calendar.SECOND, 0);
        dateCal.set(Calendar.MILLISECOND, 0);
        this.startTime = dateCal.getTime();
        dateCal.setTime(stopTime);
        dateCal.set(Calendar.HOUR_OF_DAY, 0);
        dateCal.set(Calendar.MINUTE, 0);
        dateCal.set(Calendar.SECOND, 0);
        dateCal.set(Calendar.MILLISECOND, 0);
        this.endTime = dateCal.getTime();
        this.warranty = (int) ((endTime.getTime() - startTime.getTime()) / (24 * 60 * 60 * 1000));
        Calendar curCal = Calendar.getInstance();
        curCal.set(Calendar.HOUR_OF_DAY, 0);
        curCal.set(Calendar.MINUTE, 0);
        curCal.set(Calendar.SECOND, 0);
        curCal.set(Calendar.MILLISECOND, 0);
        this.value = (int) ((curCal.getTimeInMillis() - startTime.getTime()) / (24 * 3600 * 1000));
        Log.w(TAG, TAG + "initTime: initTime:value" + value);
        this.invalidate();
    }

    public void initTime(Date startTime, int warranty) {
        this.warranty = warranty;
        this.startTime = startTime;
        gc.setTime(startTime);
        gc.add(Calendar.DAY_OF_MONTH, warranty);
        this.endTime = gc.getTime();
        this.invalidate();
    }

    private void init() {
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(textColor);

        textPaint.setTextSize(dpToPx(textSizeMonthDay));
        textWidthMonth = textPaint.measureText("08-08");
        textHeightMonth = dpToPx(16);
        textPaint.setTextSize(dpToPx(textSizeYear));
        textWidthYear = textPaint.measureText("2015");
        textHeightYear = dpToPx(18);
        endTime = new Date(System.currentTimeMillis());

        gc.setTime(endTime);
        gc.add(Calendar.DAY_OF_MONTH, -warranty);
        startTime = gc.getTime();

        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setStrokeWidth(dpToPx(linwidth));
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeCap(Paint.Cap.ROUND);
        bgPaint.setTextSize(dpToPx(textSizeMonthDay));

        valuePaint = new Paint();
        valuePaint.setAntiAlias(true);
        valuePaint.setStrokeWidth(dpToPx(linwidth));
        valuePaint.setStyle(Paint.Style.STROKE);
        valuePaint.setStrokeCap(Paint.Cap.ROUND);
        valuePaint.setColor(lineColor);

        yearFormat = new SimpleDateFormat("yyyy");
        monthDayFormat = new SimpleDateFormat("MM-dd");

        value = warranty;

    }

    private void drawInitDate(Canvas canvas, RectF rect, Date startTime, Date endTime) {
        if (isShowTime) {
            String year1 = yearFormat.format(startTime);
            String monthDay1 = monthDayFormat.format(startTime);
            String year2 = yearFormat.format(endTime);
            String monthDay2 = monthDayFormat.format(endTime);

            float offset = Math.abs(textWidthMonth - textWidthYear) / 2;
            float heightOffset = textHeightMonth + textHeightYear;


            textPaint.setTextSize(dpToPx(textSizeYear));
            canvas.drawText(year1, rect.left + offset, rect.top + dpToPx(linwidth + 40) + heightOffset, textPaint);
            canvas.drawText(year2, rect.right - textWidthYear - offset, rect.top + dpToPx(linwidth + 40) + heightOffset, textPaint);

            textPaint.setTextSize(dpToPx(textSizeMonthDay));
            canvas.drawText(monthDay1, rect.left, rect.top + dpToPx(linwidth + 55) + heightOffset, textPaint);
            canvas.drawText(monthDay2, rect.right - textWidthMonth, rect.top + dpToPx(linwidth + 55) + heightOffset, textPaint);
        }
    }

    private void drawVlueLine(Canvas canvas, RectF rect, float value) {
        float textWidth = Math.max(textWidthMonth, textWidthYear);
        float offset = Math.max(valuePaint.getStrokeWidth() / 2, textWidth / 2);
        float heightOffset = textHeightMonth + textHeightYear;
        float lineLenght = rect.width() - offset * 2;
        float valueWidth = (value / warranty) * lineLenght;
        float remain = lineLenght - valueWidth;
        if (value < 0 || value > warranty) {
            valueWidth = lineLenght;
            remain = 0;
        }
        //绘制进度
        canvas.drawLine(rect.left + offset, rect.top + dpToPx(25) + heightOffset, rect.right - offset - remain, rect.top + dpToPx(25) + heightOffset, valuePaint);

        //绘制滑动块
        if (null != thumb) {
            thumb = Bitmap.createScaledBitmap(thumb, (int) dpToPx(10), (int) dpToPx(14), true);
            canvas.drawBitmap(thumb, rect.left + offset + valueWidth - thumb.getWidth() / 2, rect.top + dpToPx(5) + heightOffset, textPaint);
        } else {
            Path path = new Path();
            path.moveTo(rect.left + offset + valueWidth, rect.top + linwidth + dpToPx(18) + heightOffset);
            path.lineTo(rect.left + offset + valueWidth - dpToPx(5), rect.top + linwidth + dpToPx(6) + heightOffset);
            path.lineTo(rect.left + offset + valueWidth + dpToPx(5), rect.top + linwidth + dpToPx(6) + heightOffset);
            path.lineTo(rect.left + offset + valueWidth, rect.top + linwidth + dpToPx(18) + heightOffset);
            path.close();
            canvas.drawPath(path, textPaint);
        }

        if (isShowTime) {
            //绘制滑动时间
            gc.setTime(startTime);
            gc.add(Calendar.DAY_OF_MONTH, (int) value);
            Date nowDate = gc.getTime();
            String year = yearFormat.format(nowDate);
            String monthDay = monthDayFormat.format(nowDate);

            textPaint.setTextSize(dpToPx(textSizeYear));
            canvas.drawText(year, rect.left + offset + valueWidth - textWidthYear / 2, rect.top + dpToPx(linwidth + 15), textPaint);
            textPaint.setTextSize(dpToPx(textSizeMonthDay));
            canvas.drawText(monthDay, rect.left + offset + valueWidth - textWidthMonth / 2, rect.top + dpToPx(linwidth + 30), textPaint);
        }
    }

    private void drawBackgroundLine(Canvas canvas, RectF rect) {

        bgPaint.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.top,
                new int[]{startColor, centerColor, endColor}, null, Shader.TileMode.REPEAT));
        float offset = Math.max(bgPaint.getStrokeWidth() / 2, Math.max(textWidthMonth, textWidthYear) / 2);
        float heightOffset = textHeightMonth + textHeightYear;
        canvas.drawLine(rect.left + offset, rect.top + dpToPx(25) + heightOffset, rect.right - offset, rect.top + dpToPx(25) + heightOffset, bgPaint);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF rect = new RectF(0, 0, this.getWidth(), this.getHeight());

        drawBackgroundLine(canvas, rect);
        drawInitDate(canvas, rect, startTime, endTime);
        drawVlueLine(canvas, rect, value);
    }

    protected float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    protected float spToPx(float spValue) {
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
