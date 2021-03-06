package com.ozner.cup.Utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

/**
 * Created by ozner_67 on 2016/12/27.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class LCLogUtils {
    public static boolean APP_DBG = false;//是否是debug模式

    public static void init(Context context) {
        APP_DBG = isApkdebugable(context);
    }

    public static boolean isApkdebugable(Context context) {
        try {
            ApplicationInfo appInfo = context.getApplicationInfo();
            return (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {

        }
        return false;
    }

    public static void E(String tag, String msg) {
        if (APP_DBG) {
            Log.e(tag, msg);
        }
    }

    public static void D(String tag, String msg) {
        if (APP_DBG) {
            Log.d(tag, msg);
        }
    }

    public static void I(String tag, String msg) {
        if (APP_DBG) {
            Log.i(tag, msg);
        }
    }

    public static void V(String tag, String msg) {
        if (APP_DBG) {
            Log.v(tag, msg);
        }
    }

    public static void W(String tag, String msg) {
        if (APP_DBG) {
            Log.w(tag, msg);
        }
    }
}
