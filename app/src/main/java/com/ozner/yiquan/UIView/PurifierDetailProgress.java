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
import android.view.MotionEvent;

import com.ozner.ui.library.UIXBaseView;

/**
 * Created by xinde on 2015/11/20.
 */
public class PurifierDetailProgress extends UIXBaseView {
    private final static int baseColor = 0xffe7ecf8;
    private final static int lineStartColor = 0xff3387f9;
    private final static int lineBetweenColor = 0xff6b3ee1;
    private final static int lineEndColor = 0xfff83636;

    private int _progressVal = 0;
    private int _innerProVal = 0;
    private int _ani_innerProVal = 0;
    private int _ani_progressVal = 0;
    private int _ani_alpha = 0;
    private int _ani_duration = 600;

    float _baseLineWidth = 0;
    float _valueLineWidth = 0;

    private void init() {
        _baseLineWidth = dpToPx(1);
        _valueLineWidth = dpToPx(8);
    }

    public int outerVal() {
        return this._progressVal;
    }

    public int innerVal() {
        return this._innerProVal;
    }
//    public void set_progressVal(int _progressVal) {
//        this._progressVal = _progressVal;
//    }

    public void update(int outerVal, int innerVal) {
        if (outerVal > 100) {
            _progressVal = 100;
        } else {
            _progressVal = outerVal;
        }
        if (innerVal > 100) {
            _innerProVal = 100;
        } else {
            _innerProVal = innerVal;
        }
        invalidate();
        startAnimation();
    }

//    public void startAnimator() {
//        startAnimation();
//    }

    public PurifierDetailProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public Animator[] getAnimation(int step) {
        ValueAnimator animator;
        switch (step) {
            case 0:
                animator = ValueAnimator.ofInt(0, _progressVal);
                animator.setDuration(_ani_duration);
                break;
            case 1:
                animator = ValueAnimator.ofInt(0, _innerProVal);
                animator.setDuration(_ani_duration);
                break;
            default:
                return null;
        }

        animator.addUpdateListener(animatorUpdateListener);
        return new Animator[]{animator};
    }

    ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            int value = (Integer) animation.getAnimatedValue();
            switch (getStep()) {
                case 0:
                    _ani_progressVal = value < _progressVal ? value : _progressVal;
                    break;
                case 1:
                    _ani_innerProVal = value < _innerProVal ? value : _innerProVal;
                    break;
            }

            invalidate();
        }
    };

    @Override
    public void onAnimationStart(Animator animation) {
        if (getStep() == 0) {
            _ani_progressVal = 0;
            _ani_innerProVal = 0;
        }
        super.onAnimationStart(animation);
    }

    @Override
    public int getAnimationCount() {
        return 2;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            startAnimation();
        }
        return super.onTouchEvent(event);
    }

    private void drawBackgroundLine(RectF rect, Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(baseColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(_baseLineWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);

        paint.setAlpha(125);
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

//        paint.setAlpha(2);

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

        float space = dpToPx(6);
        float offset = 0;
        int width = this.getWidth();
        int height = this.getHeight();
        int dx = 0;
        int dy = 0;
        if (width > height * 1.8f) {
            width = (int) (height * 1.8f);
            dx = (this.getWidth() - width) / 2;
        } else {
            height = (int) (width / 1.8f);
            dy = Math.abs((this.getHeight() - height) / 2);
        }
//        if (_progressVal > 0)
            drawBackgroundLine(new RectF(offset + dx, offset + dy, this.getWidth() - dx, this.getHeight() - dy), canvas);

//        offset += space + _valueLineWidth;
//        drawBackgroundLine(new RectF(offset + dx, offset + dy, this.getWidth() - offset - dx, this.getHeight() - dy), canvas);
//        if (_progressVal > 0)
//            drawBackgroundLine(new RectF(offset, offset, length, length * 0.5f), canvas);
//
//        offset += space + _valueLineWidth;
//        drawBackgroundLine(new RectF(offset, offset, length - offset, length * 0.5f), canvas);
    }

    protected void onDrawValue(Canvas canvas) {
        float space = dpToPx(6);
        float offset = 0;
        int width = this.getWidth();
        int height = this.getHeight();
        int dx = 0;
        int dy = 0;
        if (width > height * 1.8f) {
            width = (int) (height * 1.8f);
            dx = (this.getWidth() - width) / 2;
        } else {
            height = (int) (width / 1.8f);
            dy = Math.abs((this.getHeight() - height) / 2);
        }

        drawValueLine(new RectF(offset + dx, offset + dy, this.getWidth() - offset - dx, this.getHeight() - offset - dy), canvas, lineStartColor, lineEndColor,
                isAnnmatorRuning() ? _ani_progressVal : _progressVal);
        offset += space + _valueLineWidth;
        drawValueLine(new RectF(offset + dx, offset + dy, this.getWidth() - offset - dx, this.getHeight() - dy), canvas, lineStartColor, lineEndColor,
                isAnnmatorRuning() ? _ani_innerProVal : _innerProVal);
//        drawValueLine(new RectF(offset, offset, length - offset, length * 0.5f - offset), canvas, lineStartColor, lineEndColor,
//                isAnnmatorRuning() ? _ani_progressVal : _progressVal);
//        offset = space + _valueLineWidth;
//        drawValueLine(new RectF(offset, offset, length - offset, length * 0.5f), canvas, lineStartColor, lineEndColor,
//                isAnnmatorRuning() ? _ani_innerProVal : _innerProVal);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //白线的圆环
        onDrawBackground(canvas);
        //值的圆环
        onDrawValue(canvas);
    }
}
