package com.ozner.cup.Command;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.StringDef;

/**
 * Created by ozner_67 on 2016/11/3.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class UserDataPreference {
    public final static String UserId = "UserId";
    public final static String SelMac = "SelMac";
    public final static String TapPosSaveTag = "TapPosSave";//水探头
    public final static String CupPosSaveTag = "CupPosSave";//水杯
    public final static String WaterPosSaveTag = "WaterPosSave";//水机
    public final static String AirSaveTag = "AirPosSave";//空净


    @StringDef({TapPosSaveTag, CupPosSaveTag, WaterPosSaveTag, AirSaveTag})
    public @interface SaveTag {

    }


    public static SharedPreferences Init(Context context) {
        if (context != null) {
            String file = OznerPreference.GetValue(context, UserDataPreference.UserId, "Oznerser");
            return context.getSharedPreferences(file, Context.MODE_PRIVATE);
        } else
            return null;
    }

    public static String GetUserData(Context context, String key, String value) {
        if (context != null) {
            SharedPreferences sp = Init(context);
            String value2 = sp.getString(key, value);
            if (value2 != null) {
                if (value2.equals("null"))
                    return value;
                else
                    return value2;
            }
            return value2;
        } else {
            return value;
        }
    }

    public static void SetUserData(Context context, String key, String value) {
        if (context != null) {
            SharedPreferences sp = Init(context);
            SharedPreferences.Editor et = sp.edit();
            et.putString(key, value);
            et.commit();
        }
    }
}
