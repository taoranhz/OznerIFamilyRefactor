package com.ozner.cup.Utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ozner_67 on 2016/11/23.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class DateUtils {
    private static final String TAG = "DateUtils";
    private static final long Default_Close_Time = 30000;//30s
    static SimpleDateFormat shortFormt = new SimpleDateFormat("HH:mm");

    public static String hourMinFormt(Date date) {
        return shortFormt.format(date);
    }

    /**
     * 两个时间间隔在30秒内
     *
     * @param prevtime
     * @param currtime
     *
     * @return
     */
    public static boolean isCloseEnough(long prevtime, long currtime) {
        return currtime - prevtime < Default_Close_Time;
    }

    /**
     * 将字符串毫秒日期转换成long毫秒日期
     *
     * @param strLongDate
     *
     * @return
     */
    public static long formatDateFromString(String strLongDate) {
        try {
            return Long.parseLong(strLongDate.replace("/Date(", "").replace(")/", ""));
        } catch (Exception ex) {
            Log.e(TAG, "formatDateFromString_Ex: " + ex.getMessage());
            return 0;
        }
    }
}
