package com.ozner.cup.Base;

import android.support.v4.app.Fragment;
import android.view.View;

import java.util.Locale;

/**
 * Created by ozner_67 on 2016/11/1.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class BaseFragment extends Fragment {
    /**
     * 获取控件宽度
     *
     * @param view
     *
     * @return
     */
    public int[] getMeasuredWidth(View view) {
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
        int[] size = new int[2];
        size[0] = view.getMeasuredWidth();
        size[1] = view.getMeasuredHeight();
        return size;
    }

    /**
     * 是否是英文环境
     * @return
     */
    public boolean isLanguageEn(){
        try {
            if (Locale.getDefault().getLanguage().endsWith("zh")) {
                return false;
            } else {
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return true;
        }
    }
}
