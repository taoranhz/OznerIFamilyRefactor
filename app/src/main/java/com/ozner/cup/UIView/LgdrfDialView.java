package com.ozner.cup.UIView;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

/**
 * Created by ozner_67 on 2017/5/27.
 * 邮箱：xinde.zhang@cftcn.com
 * <p>
 * 水温指示表盘
 */

public class LgdrfDialView extends View {
    private static final String TAG = "LgdrfDialView";
    private final int START_COLOR = 0xff0196ff;//表盘起始颜色
    private final int MIDDLE_COLOR = 0xffb159ed;//中间颜色
    private final int END_COLOR = 0xffef3e6e;//结束颜色
    private final int ROTATE_STEP = 10;//两条线间隔角度
    private final int ANIM_DURIATION = 5000;//动画时间
    private final int START_ROTATE = -10;//起始角度
    private final int END_ROTATE = 200;//结束角度
    private int lineNum = 20;
    private final float LINE_WIDTH = 15f;
    float lineLenght = 3.0f;//线长，单位dp
    float radius;//刻度盘最外侧半径值
    private Paint linePaint;//刻度画笔
    private Paint shaderPaint;
    private RectF canvasRect;//画布范围
    Shader shader;
    private int curLineNum = 0;

    private ValueAnimator rotateAnim;


    public LgdrfDialView(Context context) {
        super(context);
        init();
    }

    public LgdrfDialView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LgdrfDialView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(LINE_WIDTH);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        shaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lineNum = (180 - START_ROTATE * 2) / ROTATE_STEP;
        if (START_ROTATE + lineNum * ROTATE_STEP < END_ROTATE) {
            lineNum += 1;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int defaultSize = 400;
        int canvasSize;
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        if (widthMode == View.MeasureSpec.EXACTLY) {
            canvasSize = widthSize;
        } else if (widthMode == View.MeasureSpec.AT_MOST) {
            canvasSize = Math.max(defaultSize, widthSize);
        } else if (widthMode == View.MeasureSpec.UNSPECIFIED) {
            canvasSize = defaultSize;
        } else {
            canvasSize = defaultSize;
        }
        setMeasuredDimension(canvasSize, (int) (canvasSize * 0.75f));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasRect = new RectF(0, 0, w, h);
        caclueProperties();
        startRotateAnima();
    }

    /**
     * 计算相关属性
     */
    private void caclueProperties() {
        //计算线长
        radius = canvasRect.width() / 2 * 0.8f;
        lineLenght = radius * 0.15f;
        shader = new LinearGradient(0, 0, canvasRect.right, 0, new int[]{START_COLOR, MIDDLE_COLOR, END_COLOR}
                , new float[]{0, 0.5f, 1}, Shader.TileMode.CLAMP);
    }

    /**
     * 绘制直线
     *
     * @param canvas
     */
    private void drawLine(Canvas canvas, float lineLen, float rotate) {
        canvas.rotate(-rotate, canvasRect.width() / 2, canvasRect.width() / 2);
        canvas.drawLine(canvasRect.width() / 2 + radius, canvasRect.width() / 2, canvasRect.width() / 2 + radius - lineLen, canvasRect.width() / 2, linePaint);
        canvas.rotate(rotate, canvasRect.width() / 2, canvasRect.width() / 2);
    }


    /**
     * 绘制表盘
     */
    private void drawDial(Canvas canvas) {
        Bitmap srcBm = makeSrc((int) canvasRect.width(), (int) canvasRect.height());
        BitmapShader bs = new BitmapShader(srcBm, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        ComposeShader cs = new ComposeShader(bs, shader, PorterDuff.Mode.SRC_ATOP);
        shaderPaint.setShader(cs);
        canvas.drawRect(new Rect(0, 0, (int) canvasRect.width(), (int) canvasRect.height()), shaderPaint);
    }

    private void startRotateAnima() {
        if (rotateAnim == null) {
            rotateAnim = ValueAnimator.ofInt(0, lineNum + 1);
            rotateAnim.setDuration(ANIM_DURIATION);
            rotateAnim.setInterpolator(new LinearInterpolator());
            rotateAnim.setRepeatCount(Animation.INFINITE);
            rotateAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    curLineNum = (int) animation.getAnimatedValue();
                    invalidate();
                }
            });
        }
        rotateAnim.start();
    }

    private void drawLines(Canvas canvas) {
        for (int i = 0; i <= curLineNum; i++) {
            if (i % 2 == 0) {
                drawLine(canvas, lineLenght, END_ROTATE - i * ROTATE_STEP);
            } else {
                drawLine(canvas, lineLenght * 0.65f, END_ROTATE - i * ROTATE_STEP);
            }
        }
    }

    /**
     * 创建刻度图像
     *
     * @param w
     * @param h
     *
     * @return
     */
    private Bitmap makeSrc(int w, int h) {
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        drawLines(c);
        return bm;

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawDial(canvas);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (rotateAnim != null) {
            rotateAnim.cancel();
            rotateAnim = null;
        }
    }
}
