package com.ozner.cup.UIView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.ozner.cup.R;

/**
 * Created by C-sir@hotmail.com  on 2015/12/23.
 */
public class CProessbarView extends View implements View.OnTouchListener {
    private int screenW, screenH;
    private Paint mPaint;
    private int persent = -1;
    private float drawx;
    private int step = 50;
    private Boolean isfirst = true;

    public interface ValueChangeListener {
        void onValueChange(int present);
    }

    public CProessbarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    ValueChangeListener valueChangeListener;

    protected void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        this.setOnTouchListener(this);
    }

    public void setOnValueChangeListener(ValueChangeListener valueChangelistenr) {
        this.valueChangeListener = valueChangelistenr;
    }

    public void updateValue(int persent) {

        float r = this.getHeight() / 2;
        isfirst = false;
        this.persent = persent;
        drawx = (((this.persent / 100f) * (getWidth() - 2 * r)) + r);
        invalidate();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        /*
         * (x,y)点为发生事件时的点，它的坐标值为相对于该控件左上角的距离
         */
        float r = this.getHeight() / 2;
        int precent = (int) (((drawx - r) / (getWidth() - 2 * r)) * 100f);
        isfirst = false;
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN: // 按下
                drawx = x;
                invalidate();
//                if( proessbarValueChangeListener!=null)
//                proessbarValueChangeListener.ValueChange(precent);
                break;
            case MotionEvent.ACTION_MOVE: // 拖动
                drawx = x;
                invalidate();
//                if (valueChangeListener != null)
//                proessbarValueChangeListener.ValueChange(precent);
                    break;
            case MotionEvent.ACTION_UP: // 弹起
                drawx = x;
                invalidate();
                if (valueChangeListener != null) {
                    valueChangeListener.onValueChange(precent);
                }
                break;
        }
        /*
         * 注意：这里一定要返回true
         * 返回false和super.onTouchEvent(event)都会本监听只能检测到按下消息
         * 这是因为false和super.onTouchEvent(event)的处理都是告诉系统该控件不能处理这样的消息，
         * 最终系统会将这些事件交给它的父容器处理。
         */
        return true;
    }

    public void DrawVoidProess(Canvas canvas) {
        float nowdrawx = 0;
        float height = this.getHeight() / 4;
        float r = this.getHeight() / 2;
        nowdrawx += r;
        int spilt = 2;
        RectF process = new RectF(0, 0, 0, 0);
        float width = this.getWidth();
        float kkwidth = (width - (step - 1) * spilt - 2 * r) / step;
        int nowstep = (int) (persent / 100f * step);
        for (int i = 0; i < step; i++) {
            if (i <= nowstep)
                mPaint.setColor(GradientColor(i));
            else
                mPaint.setColor(Color.rgb(250, 250, 250));
            process = new RectF(nowdrawx, height, nowdrawx + kkwidth, getHeight() - height);
            nowdrawx += kkwidth + spilt;
            canvas.drawRect(process, mPaint);
        }
    }

    public void DrawProess(Canvas canvas) {
        float r = this.getHeight() / 2;
        float height = this.getHeight() / 4;
        float width = this.getWidth();
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.FILL);
//        Bitmap bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.processbgno);
//        RectF process=new RectF(0+r,height,width-r,this.getHeight()-height);
        // canvas.drawBitmap(bitmap,null,process,mPaint);
        DrawVoidProess(canvas);
//        process=new RectF(0+r,height,drawx,this.getHeight()-height);
//        bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.processbg);
//        Rect rect=new Rect(0+(int)r,(int)height,(int)drawx,(int)(this.getHeight()-height));
//        canvas.drawBitmap(bitmap,rect,process,mPaint);
    }

    public void DrawRadio(Canvas canvas) {
        float r = this.getHeight() / 2;
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(drawx, this.getHeight() / 2, this.getHeight() / 2 - 8, mPaint);//背景白
        mPaint.setColor(Color.rgb(41, 113, 211));
        mPaint.setStrokeWidth(2);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(drawx, this.getHeight() / 2, this.getHeight() / 2 - 8, mPaint);//边框蓝
        mPaint.setStrokeWidth(10);
        mPaint.setColor(Color.argb(60, 41, 113, 211));
        canvas.drawCircle(drawx, this.getHeight() / 2, this.getHeight() / 2 - 6, mPaint);//边框蓝
        mPaint.setColor(Color.rgb(41, 113, 211));
        String textvalue = String.valueOf(persent);
        String texttips = "低速";
        mPaint.setStrokeWidth(1);
        mPaint.setTextSize(32);
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        mPaint.setStyle(Paint.Style.FILL);
        float textwidth = mPaint.measureText(textvalue);
        canvas.drawText(textvalue, drawx - textwidth / 2, r, mPaint);
        if (persent <= 40) {
            texttips = getResources().getString(R.string.air_low_speed);
        } else if (persent <= 60) {
            texttips = getResources().getString(R.string.air_middle_speed);
        } else {
            texttips = getResources().getString(R.string.air_high_speed);
        }
        mPaint.setTextSize(19);
        textwidth = mPaint.measureText(texttips);
        canvas.drawText(texttips, drawx - textwidth / 2, r + fontMetrics.descent * 3, mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //调用View类中默认的测量方法
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        screenW = widthMeasureSpec;
        screenH = heightMeasureSpec;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float r = this.getHeight() / 2;

        if (drawx <= r)
            drawx = r;
        if (drawx >= getWidth() - r)
            drawx = getWidth() - r;
        persent = (int) (((drawx - r) / (getWidth() - 2 * r)) * 100f);
        DrawProess(canvas);
        if (!isfirst)
            DrawRadio(canvas);
//            Log.e("persent",drawx + "");

    }

    public int GradientColor(int i) {
        int oldr = 231, oldg = 239, oldb = 252;
        int newr = 121, newg = 159, newb = 245;
        int r = oldr + (newr - oldr) * i / step;
        int g = oldg + (newg - oldg) * i / step;
        int b = oldb + (newb - oldb) * i / step;
        return Color.rgb(r, g, b);
    }
}
