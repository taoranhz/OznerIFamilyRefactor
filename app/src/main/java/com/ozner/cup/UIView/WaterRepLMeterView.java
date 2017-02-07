package com.ozner.cup.UIView;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;

/**
 * Created by mengdongya on 2016/3/15.
 */
public class WaterReplMeterView extends WaterReplMeterBaseView {
    float ani_y_rate = 0;
    float ani_x_rate = 0;
    int ani_sharp_alpha = 0;
    // UIZChartAdapter adapter, adapter2;
    int[] data, data2;

    public WaterReplMeterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setMonthData(int[] predata, int[] afterdata) {
        setData(predata, afterdata, UIZChartAdapter.ViewMode.Month);
    }

    public void setWeekData(int[] predata, int[] afterdata) {
        setData(predata, afterdata, UIZChartAdapter.ViewMode.Week);
    }

    public void setData(int[] predata, int[] afterdata, final UIZChartAdapter.ViewMode mode) {
        this.data = predata;
        this.data2 = afterdata;
        UIZChartAdapter adapter = new UIZChartAdapter() {

            @Override
            public int count() {
                return data.length;
            }

            @Override
            public int getValue(int Index) {
                return data[Index];
            }

            @Override
            public int getMax() {
                return 100;
            }

            @Override
            public int getMin() {
                return 20;
            }

            @Override
            public ViewMode getViewMode() {
                return mode;
            }
        };

        UIZChartAdapter adapter2 = new UIZChartAdapter() {
            @Override
            public int count() {
                return data2.length;
            }

            @Override
            public int getValue(int Index) {
                return data2[Index];
            }

            @Override
            public int getMax() {
                return 100;
            }

            @Override
            public int getMin() {
                return 20;
            }

            @Override
            public ViewMode getViewMode() {
                return mode;
            }
        };

        setAdapter(adapter, adapter2);
    }

    @Override
    protected void setAdapter(UIZChartAdapter adapter, UIZChartAdapter adapter2) {
        this.adapter = adapter;
        this.adapter2 = adapter2;
        adapter.setAdapterListener(new UIZChartAdapter.AdapterListener() {
            @Override
            public void onUpdate(UIZChartAdapter adapter) {
                update();
            }
        });
        adapter2.setAdapterListener(new UIZChartAdapter.AdapterListener() {
            @Override
            public void onUpdate(UIZChartAdapter adapter) {
                update();
            }
        });
        update();
    }

    @Override
    protected void init() {
        super.init();
        valueTag.put(20, "20");
        valueTag.put(30, "");
        valueTag.put(40, "40");
        valueTag.put(60, "60");
        valueTag.put(80, "80");
        valueTag.put(100, "100");
    }

    @Override
    public void onAnimationStart(Animator animation) {
        if (getStep() == 0) {
            ani_y_rate = 0;
            ani_sharp_alpha = 0;
            ani_x_rate = x_rate;
            this.invalidate();
        }
        super.onAnimationStart(animation);
    }

    @Override
    public Animator[] getAnimation(int step) {
        ArrayList<ValueAnimator> animator = new ArrayList<ValueAnimator>();
        if (adapter == null || adapter2 == null) return null;
        if (step == 0) {
            ValueAnimator animatory = ValueAnimator.ofFloat(0, y_rate);
            animatory.setDuration(800);
            animatory.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    ani_y_rate = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            animator.add(animatory);

            ValueAnimator animator_a = ValueAnimator.ofInt(0, SharpAlpha);
            animator_a.setDuration(800);
            animator_a.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    ani_sharp_alpha = (int) animation.getAnimatedValue();
                    invalidate();
                }
            });
            animator.add(animator_a);
        }
        return animator.toArray(new Animator[0]);
        //return super.getAnimation(step);
    }

    @Override
    public int getAnimationCount() {
        return 1;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            this.startAnimation();
        }
        return super.onTouchEvent(event);
    }

    private final static int LineColor50 = 0xff5591fb;
    private final static int LineColor200 = 0xffa28aea;
    private final static int LineColor400 = 0xffec4756;

    private final static int SharpColor50 = Color.WHITE;
    private final static int SharpColor200 = 0xffeadbf0;
    private final static int SharpColor400 = 0xfff29599;
    private final static int SharpAlpha = 180;

    private final static int AfterColor = 0xff3f88f1;
    private final static int BeforeColor = 0xffba52a7;
    private final static int AfterShapeColor = 0x223f88f1;
    private final static int BeforeShapeColor = 0x22ba52a7;

    private float valueToPostion(float value) {
        return valueRect.bottom - value * (isAnnmatorRuning() ? ani_y_rate : y_rate);
    }

    private float indexToPostion(int index) {
        return valueRect.left + index * (isAnnmatorRuning() ? ani_x_rate : x_rate);
    }

    protected void drawLine(Canvas canvas, UIZChartAdapter adapter, int color) {
        if (adapter == null) return;
        int count = adapter.count();
        int maxValue = adapter.getMax();
        Path linePath = new Path();
        float x = 0;
        float y = 0;

        for (int i = 0; i < count; i++) {
            int value = adapter.getValue(i);
            if (value > maxValue) {
                value = maxValue;
            }
            x = indexToPostion(i);
            y = valueToPostion(value);
            if (i == 0) {
                linePath.moveTo(x, y);
                continue;
            }
            linePath.lineTo(x, y);
        }

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        //paint.setShader(getLineShare());
        paint.setColor(color);
        paint.setStrokeWidth(dpToPx(1));

        canvas.drawPath(linePath, paint);
    }


    private void drawSharp(Canvas canvas, UIZChartAdapter adapter, int index) {
        int count = 0;
        if(adapter!=null){
            count = adapter.count();
        }
//        int count = adapter.count();
        if (count <= 0) return;
        int maxValue = adapter.getMax();
        Path path = new Path();
        float x = 0;
        float y = 0;
        float firstY = 0;
        for (int i = 0; i < count; i++) {
            int value = adapter.getValue(i);
            if (value > maxValue) {
                value = maxValue;
            }
            x = indexToPostion(i);
            y = valueToPostion(value);
            if (i == 0) {
                firstY = y;
                path.moveTo(x, y);
            } else
                path.lineTo(x, y);

        }
        //以下三行是将画出的折线连成一个多边形
        path.lineTo(x, valueRect.bottom);//末点 到 横轴垂足的连线
        path.lineTo(valueRect.left, valueRect.bottom);//从上一个点到绘图区域左下角点的连线
        path.lineTo(valueRect.left, firstY);//绘图区域左下角点 到 第一个点的连线，完成封闭图形

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        switch (index) {
            case 0:
                paint.setShader(new LinearGradient(0, valueRect.bottom, 0, valueRect.top,
                        new int[]{SharpColor50, BeforeShapeColor},
                        new float[]{
                                //中间颜色起点
                                Math.abs(getPostionByValue((int) valueTag.keySet().toArray()[0]) - valueRect.bottom) / valueRect.height()
                                , Math.abs(getPostionByValue((int) valueTag.keySet().toArray()[2]) - valueRect.bottom) / valueRect.height()}
                        , Shader.TileMode.CLAMP));
                break;
            case 1:
                paint.setShader(new LinearGradient(0, valueRect.bottom, 0, valueRect.top,
                        new int[]{SharpColor50, AfterShapeColor},
                        new float[]{0f,
                                //中间颜色起点
                                Math.abs(getPostionByValue((int) valueTag.keySet().toArray()[0]) - valueRect.bottom) / valueRect.height()
                        }
                        , Shader.TileMode.CLAMP));
                break;
            default:
                paint.setShader(new LinearGradient(0, valueRect.bottom, 0, valueRect.top,
                        new int[]{SharpColor50, SharpColor200, SharpColor400},
                        new float[]{0f,
                                //中间颜色起点
                                Math.abs(getPostionByValue((int) valueTag.keySet().toArray()[0]) - valueRect.bottom) / valueRect.height()
                                , Math.abs(getPostionByValue((int) valueTag.keySet().toArray()[2]) - valueRect.bottom) / valueRect.height()}
                        , Shader.TileMode.CLAMP));
                break;
        }

//        paint.setShader(new LinearGradient(0, valueRect.bottom, 0, valueRect.top,
//                new int[]{SharpColor50, SharpColor200, SharpColor400},
//                new float[]{0f,
//                        //中间颜色起点
//                       Math.abs(getPostionByValue((int) valueTag.keySet().toArray()[0]) - valueRect.bottom) / valueRect.height()
//                        , Math.abs(getPostionByValue((int) valueTag.keySet().toArray()[2]) - valueRect.bottom) / valueRect.height()}
//                , Shader.TileMode.CLAMP));

        paint.setAlpha(isAnnmatorRuning() ? ani_sharp_alpha : SharpAlpha);
        canvas.drawPath(path, paint);

    }

    @Override
    protected void drawValue(Canvas canvas) {
        drawSharp(canvas, adapter, 0);
        drawSharp(canvas, adapter2, 1);
        drawLine(canvas, adapter, BeforeColor);
        drawLine(canvas, adapter2, AfterColor);
    }
}
