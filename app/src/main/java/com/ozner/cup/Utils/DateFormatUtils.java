package com.ozner.cup.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ozner_67 on 2016/11/23.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class DateFormatUtils {
    static SimpleDateFormat shortFormt = new SimpleDateFormat("HH:mm");

    public static String hourMinFormt(Date date) {
        return shortFormt.format(date);
    }
}
