package com.ozner.cup.Utils;

import android.content.Context;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ozner_67 on 2017/3/15.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class VerifyUtil {
    /**
     * 判断邮箱是否合法
     *
     * @param email
     *
     * @return
     */
    public static boolean isEmail(String email) {
        if (null == email || "".equals(email)) return false;
//        Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}"); //简单匹配
        Pattern p = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");//复杂匹配
        Matcher m = p.matcher(email);
        return m.matches();
    }

    /**
     * 判断网络是否可用
     * @param context
     *
     * @return
     */
    public static boolean isNetAvailable(Context context) {
        if (!MobileInfoUtil.isNetworkAvailable(context)) {
            return false;
        }
        return true;
    }
}
