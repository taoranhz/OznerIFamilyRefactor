package com.ozner.cup.Device.WaterPurifier;

import android.graphics.Color;

/**
 * Created by ozner_67 on 2017/5/26.
 * 邮箱：xinde.zhang@cftcn.com
 * <p>
 * 获取相应水温颜色值工具
 */

public class WPTempColorUtil {
    private static final String TAG = "WPTempColorUtil";
    public static final int LowColor = 0xff0196ff;//起点颜色
    public static final int MiddleColor = 0xffb159ed;//中间颜色
    public static final int HighColor = 0xffef3e6e;//终点颜色

    public static int getColor(int temp) {
        int rRed, rGreen, rBlue;
        float fbegin = 0.0f, fend = 0.0f;
        float step = 0.0f;

        int startColor = LowColor;
        int endColor = MiddleColor;
        if (temp < 1) {
            return LowColor;
        }
        if (temp > 99) {
            return HighColor;
        }

        if (temp >= 50) {
            startColor = MiddleColor;
            endColor = HighColor;
            temp -= 50;
        }


        //计算红色分量
        fbegin = (float) Color.red(startColor);
        fend = (float) Color.red(endColor);
        step = (fend - fbegin) / 50;
        rRed = (int) (fbegin + step * temp);

        //计算红绿色分量
        fbegin = (float) Color.green(startColor);
        fend = (float) Color.green(endColor);
        step = (fend - fbegin) / 50;
        rGreen = (int) (fbegin + step * temp);

        //计算蓝色分量
        fbegin = (float) Color.blue(startColor);
        fend = (float) Color.blue(endColor);
        step = (fend - fbegin) / 50;
        rBlue = (int) (fbegin + step * temp);
        return Color.argb(0xff, rRed, rGreen, rBlue);
    }
}
