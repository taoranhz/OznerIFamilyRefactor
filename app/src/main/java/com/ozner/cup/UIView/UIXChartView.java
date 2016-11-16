package com.ozner.cup.UIView;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;

import com.ozner.cup.CupRecord;
import com.ozner.ui.library.UIXBaseView;

import java.util.LinkedHashMap;
import java.util.Random;

/**
 * Created by taoran on 15/11/25.
 */
public abstract class UIXChartView extends UIXBaseView {
    private final static int tagColor = 0xff94979a;
    private final static int lineColor = 0xffd7d7d7;
    protected LinkedHashMap<Integer, String> valueTag = new LinkedHashMap<>();
    private TextPaint tagPaint = new TextPaint();
    private Paint dayPaint = new Paint();
    protected RectF valueRect = new RectF();
    protected RectF gridRect = new RectF();
    protected float x_rate = 0;
    protected float y_rate = 0;
    ChartAdapter adapter = new ChartAdapter() {
        int[] data = new int[20];

        @Override
        protected void init() {
            Random random = new Random();
            for (int i = 0; i < data.length; i++) {
                data[i] = random.nextInt(CupRecord.TDS_Bad_Value * 2 - CupRecord.TDS_Good_Value);
            }
        }

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
            return CupRecord.TDS_Bad_Value * 2 - CupRecord.TDS_Good_Value;
        }

        @Override
        public ViewMode getViewMode() {
            return ViewMode.Day;
        }
    };

    public void setAdapter(ChartAdapter adapter) {
        this.adapter = adapter;
        adapter.setAdapterListener(new ChartAdapter.AdapterListener() {
            @Override
            public void onUpdate(ChartAdapter adapter) {
                update();
            }
        });
        update();
    }


    protected void init() {
        tagPaint.setTextAlign(Paint.Align.LEFT);
        tagPaint.setColor(tagColor);
        tagPaint.setAntiAlias(true);
        tagPaint.setTextSize(spToPx(11));

        dayPaint.setTextAlign(Paint.Align.CENTER);
        dayPaint.setColor(tagColor);
        dayPaint.setAntiAlias(true);
        dayPaint.setTextSize(spToPx(11));
    }

    private void update() {
        if (adapter == null) return;

        int maxValue = adapter.getMax();
        //dpToPx控制侧边数值和统计图的间距
        float x = tagPaint.measureText(String.valueOf(valueTag.get(adapter))) + dpToPx(30);

        float y = 0;
        gridRect = new RectF(x, dpToPx(8), getMeasuredWidth() - dpToPx(6), getMeasuredHeight() - dpToPx(12));
        valueRect = new RectF(gridRect.left + dpToPx(2), gridRect.top + dpToPx(3),
                gridRect.right - dpToPx(6), gridRect.bottom - dpToPx(3));

        y_rate = valueRect.height() / maxValue;
        x_rate = valueRect.width() / (adapter.getMaxCount() - 1);
        this.invalidate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        update();
    }

    public UIXChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public Animator[] getAnimation(int step) {
        return null;
    }

    @Override
    public int getAnimationCount() {
        return 0;
    }

    protected float getPostionByValue(float value) {
        return valueRect.bottom - value * y_rate;
    }

    protected float getPostionByIndex(float index) {
        return valueRect.left + (index) * x_rate;
    }


    protected void drawGrid(Canvas canvas) {
        if (adapter == null) return;
        float y = 0;
        float x = 0;
        int maxValue = adapter.getMax();


        Paint linePaint = new Paint();
        linePaint.setColor(lineColor);
        linePaint.setStyle(Paint.Style.STROKE);
//        linePaint.setPathEffect(new DashPathEffect(new float[]{5, 5, 5, 5}, 0));
        linePaint.setStrokeWidth(dpToPx(1));
        canvas.drawLine(dpToPx(2), gridRect.bottom, getWidth() - dpToPx(2), gridRect.bottom, linePaint);
        Path path = new Path();
        float fontYSeek = dpToPx(3);
        for (Integer value : valueTag.keySet()) {
            y = getPostionByValue(value);
            if (value == maxValue) {
                canvas.drawLine(gridRect.left, y, gridRect.right, y, linePaint);
            } else {
                path.moveTo(gridRect.left, y);
                path.lineTo(gridRect.right, y);
            }

            StaticLayout layout = new StaticLayout(valueTag.get(value), tagPaint, (int) gridRect.left, Layout.Alignment.ALIGN_CENTER, 1.0F, 0.0F, false);

            canvas.save();
            canvas.translate(0, y - layout.getLineBottom(0) / 2.0f);
            layout.draw(canvas);
            canvas.restore();
            //canvas.drawText(valueTag.get(value),0,y+fontYSeek,tagPaint);
        }
        canvas.drawPath(path, linePaint);

        int seek = 1;
        int maxXValue = adapter.getMaxCount() - 1;
        switch (adapter.getViewMode()) {
            case Day:
                seek = 4;
                break;
            case Week:
                seek = 1;
                break;
            case Month:
                seek = 5;
                break;
        }

        int p = 0;
        while (true) {
            x = getPostionByIndex(p);
            String text = adapter.getPostionText(p);
            canvas.drawText(text, x, this.getHeight() - dpToPx(1), dayPaint);
            p += seek;
            if (p >= maxXValue) {
                x = getPostionByIndex(maxXValue);
                text = adapter.getPostionText(maxXValue);
                canvas.drawText(text, x, this.getHeight() - dpToPx(1), dayPaint);
                break;
            }
        }

    }


    protected abstract void drawValue(Canvas canvas);

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawGrid(canvas);
        drawValue(canvas);


    }


}
