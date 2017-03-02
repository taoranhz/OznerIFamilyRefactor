package com.ozner.cup.UIView;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;


/**
 * Created by ozner_67 on 2016/11/30.
 */
public class UIXVolumeChartView extends UIXChartView {

    public UIXVolumeChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    float ani_value = 0;


    @Override
    protected void init() {
        super.init();
        //控制侧边数值的变化
        valueTag.put(50, "50");
        valueTag.put(200, "200");
        valueTag.put(400, "400\nml");
    }

    /**
     * 清除左侧标签
     */
    public void clearTag() {
        valueTag.clear();
        this.invalidate();
    }

    /**
     * 添加标签
     *
     * @param key
     * @param value
     */
    public void putTag(int key, String value) {
        valueTag.put(key, value);
    }

    @Override
    public void onAnimationStart(Animator animation) {
        if (getStep() == 0) {
            ani_value = 0;
            this.invalidate();
        }
        super.onAnimationStart(animation);
    }

    @Override
    public Animator[] getAnimation(int step) {
        ArrayList<ValueAnimator> animator = new ArrayList<>();
        if (adapter == null) return null;
        if (step == 0) {
            float sum = 0;
            for (int i = 0; i < adapter.count(); i++) {
                sum += adapter.getValue(i);
            }
            ValueAnimator animatory = ValueAnimator.ofFloat(0, sum);
            animatory.setDuration(1200);
            animatory.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    ani_value = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            animator.add(animatory);

        }
        return animator.toArray(new Animator[0]);
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


    private void drawBar(Canvas canvas) {
        if (adapter == null) return;
        float width = dpToPx(5);
        int count = adapter.count();
        int maxValue = adapter.getMax();
        float x = 0;
        float y = 0;
        int startColor = 0xff5c87ef;
        int endColor = 0xff89b3ff;
        Paint barPaint = new Paint();
        barPaint.setAntiAlias(true);
        barPaint.setStrokeCap(Paint.Cap.ROUND);
        barPaint.setStrokeWidth(width);
        barPaint.setColor(Color.BLUE);
        barPaint.setStyle(Paint.Style.FILL);
        float sum = 0;
//        Log.e("tag", "maxValue:" + maxValue);
        for (int i = 0; i < count; i++) {
            float value = adapter.getValue(i);
//            float value = 710;
            if (isAnnmatorRuning()) {
                if (sum >= ani_value)
                    break;
                if (value > ani_value - sum) {
                    value = ani_value - sum;
                }
                if (value > maxValue) {
                    value = maxValue;
                }
            }
            if (value > (maxValue + 10)) {
                value = maxValue + 10;
            }

            x = getPostionByIndex(i);
            y = getPostionByValue(value);
            barPaint.setShader(new LinearGradient(x, valueRect.bottom, x + width, valueRect.top,
                    startColor, endColor, Shader.TileMode.MIRROR));

            canvas.drawLine(x, y, x, getPostionByValue(0), barPaint);
            sum += value;
        }
        //canvas.drawPath(linePath,barPaint);
    }

    @Override
    protected void drawValue(Canvas canvas) {
        drawBar(canvas);

    }
}
