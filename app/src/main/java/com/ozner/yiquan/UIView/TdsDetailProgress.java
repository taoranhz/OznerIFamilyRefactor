package com.ozner.yiquan.UIView;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import com.ozner.ui.library.UIXBaseView;

/**
 * Created by xinde on 2015/11/20.
 */
public class TdsDetailProgress extends UIXBaseView {
    private final static int baseColor = 0xffe7ecf8;
    private final static int lineStartColor = 0xff3387f9;
    private final static int lineBetweenColor = 0xff6b3ee1;
    private final static int lineEndColor = 0xfff83636;

    private int _old_progrewwVal = 0;
    private int _progressVal = 0;
    private int _ani_progressVal = 0;
    private int _ani_alpha = 0;
    private int _ani_duration = 600;

    float _baseLineWidth = 0;
    float _valueLineWidth = 0;

    private void init() {
        _baseLineWidth = dpToPx(1);
        _valueLineWidth = dpToPx(8);
    }

    public int getProgressVal() {
        return _progressVal;
    }

    public void setProgressVal(int _progressVal) {
        this._progressVal = _progressVal;
        invalidate();
    }

    public void update(int progress) {
        if (progress > 100) {
            this._progressVal = 100;
        } else {
            this._progressVal = progress;
        }
        invalidate();
        startAnimation();
    }

    public TdsDetailProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public Animator[] getAnimation(int step) {
        ValueAnimator animator;
        animator = ValueAnimator.ofInt(_old_progrewwVal, _progressVal);
        animator.setDuration(_ani_duration);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animatorUpdateListener);
        return new Animator[]{animator};
    }

    ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            int value = (Integer) animation.getAnimatedValue();
            if (_old_progrewwVal < _progressVal) {
                _ani_progressVal = value < _progressVal ? value : _progressVal;
            } else {
                _ani_progressVal = value > _progressVal ? value : _progressVal;
            }
            invalidate();
        }
    };

    @Override
    public void onAnimationStart(Animator animation) {
        // if(getStep()==0){
        _ani_progressVal = 0;
        // }
        super.onAnimationStart(animation);
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        _old_progrewwVal = _progressVal;
        super.onAnimationEnd(animation);
    }

    @Override
    public int getAnimationCount() {
        return 1;
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if(event.getAction() == MotionEvent.ACTION_DOWN){
//            startAnimation();
//        }
//        return super.onTouchEvent(event);
//    }

    private void drawBackgroundLine(RectF rect, Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(baseColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(_baseLineWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);

//        canvas.drawArc(new RectF(rect.left + _valueLineWidth / 2, rect.top + _valueLineWidth / 2,
//                        rect.right - _valueLineWidth / 2, rect.bottom - _valueLineWidth / 2),
//                -180, 180, false, paint);
        canvas.drawArc(new RectF(rect.left + _valueLineWidth / 2, rect.top + _valueLineWidth / 2,
                        rect.right - _valueLineWidth / 2, rect.bottom + rect.height() - _valueLineWidth * 2),
                -180, 180, false, paint);
    }

    private void drawValueLine(RectF rect, Canvas canvas, int startColor, int endColor, float value) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(startColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(_valueLineWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.top,
                startColor, endColor, Shader.TileMode.REPEAT));
//        paint.setShader(new SweepGradient(rect.centerX(), rect.centerY(), new int[]{startColor, endColor}, new float[]{0.5f, 1.0f}));
//        canvas.drawArc(new RectF(rect.left + _valueLineWidth / 2, rect.top + _valueLineWidth / 2,
//                        rect.right - _valueLineWidth / 2, rect.bottom - _valueLineWidth / 2),
//                -180, 180 * (value / 100.0f), false, paint);
        canvas.drawArc(new RectF(rect.left + _valueLineWidth / 2, rect.top + _valueLineWidth / 2,
                        rect.right - _valueLineWidth / 2, rect.bottom + rect.height() - _valueLineWidth * 2),
                -180, 180 * (value / 100.0f), false, paint);
    }

    protected void onDrawBackground(Canvas canvas) {
        drawBackgroundLine(new RectF(0, 0, this.getWidth(), this.getHeight()), canvas);
    }

    protected void onDrawValue(Canvas canvas) {
        drawValueLine(new RectF(0, 0, this.getWidth(), this.getHeight()), canvas, lineStartColor, lineEndColor,
                isAnnmatorRuning() ? _ani_progressVal : _progressVal);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        onDrawBackground(canvas);
        onDrawValue(canvas);
    }
}
