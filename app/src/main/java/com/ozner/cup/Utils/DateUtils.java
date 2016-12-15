package com.ozner.cup.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ozner_67 on 2016/11/23.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class DateUtils {
    private static final long Default_Close_Time = 30000;//30s
    static SimpleDateFormat shortFormt = new SimpleDateFormat("HH:mm");

    public static String hourMinFormt(Date date) {
        return shortFormt.format(date);
    }

    /**
     * 两个时间间隔在30秒内
     * @param prevtime
     * @param currtime
     *
     * @return
     */
    public static boolean isCloseEnough(long prevtime, long currtime) {
        return currtime - prevtime < Default_Close_Time;
    }
}
