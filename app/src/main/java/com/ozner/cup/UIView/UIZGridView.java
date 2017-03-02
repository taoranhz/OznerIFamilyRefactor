package com.ozner.cup.UIView;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Created by xinde on 2015/12/7.
 */
public class UIZGridView extends GridView {


    public UIZGridView(Context context) {
        super(context);
    }

    public UIZGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UIZGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
