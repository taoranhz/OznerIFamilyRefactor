package com.ozner.cup.Command;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ozner_67 on 2016/11/3.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class OznerPreference {
    public final static String Ozner = "ozner";
    public final static String IsLogin = "islogin";
    public final static String UserId = "UserId";

    public final static String ISBDBind = "isBdBind";//设备id是否已经绑定
    public final static String BDDeivceID = "bd_deviceid";//百度推送设备id
    public final static String IsFirstStart = "is_first_start";//是否是第一次启动
    public final static String UserToken = "usertoken";

    private static SharedPreferences Init(Context context) {
        if (context != null)
            return context.getSharedPreferences(Ozner, Context.MODE_PRIVATE);
        else return null;
    }

    private static SharedPreferences.Editor InitEditor(Context context) {
        if (context != null)
            return context.getSharedPreferences(Ozner, Context.MODE_PRIVATE).edit();
        else
            return null;
    }

    public static String getUserToken(Context myContext) {
        if (myContext != null) {
            SharedPreferences ozner = Init(myContext);
            String usertoken = ozner.getString(UserToken, null);
            return usertoken;
        } else
            return null;
    }

    public static boolean IsLogin(Context myContext) {
        SharedPreferences ozner = Init(myContext);
        Boolean islogin = ozner.getBoolean(IsLogin, false);
        return islogin;
    }

    public static void setIsLogin(Context myContext, Boolean islogin) {
        SharedPreferences.Editor ozner = InitEditor(myContext);
        ozner.putBoolean(IsLogin, islogin);
        ozner.commit();
    }

    public static void setUserToken(Context myContext, String userToken) {
        SharedPreferences.Editor ozner = InitEditor(myContext);
        ozner.putString(UserToken, userToken);
        ozner.commit();
    }

    public static void SetValue(Context mycontex, String key, String value) {
        SharedPreferences.Editor ozner = InitEditor(mycontex);
        ozner.putString(key, value);
        ozner.commit();
    }

    public static String GetValue(Context mycontext, String key, String value) {
        SharedPreferences ozner = Init(mycontext);
        return ozner.getString(key, value);
    }
}
