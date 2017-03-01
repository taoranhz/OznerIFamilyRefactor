package com.ozner.yiquan.Utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.UUID;

/**
 * Created by ozner_67 on 2016/11/3.
 * 邮箱：xinde.zhang@cftcn.com
 *
 * 手机信息工具
 */

public class MobileInfoUtil {
    /**
     * 获取手机mac地址<br/>
     * 错误返回12个0
     */
    public static String getImie(Context context) {
        // 获取mac地址：
        try {
            TelephonyManager telephonemanage = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return telephonemanage.getDeviceId();
        } catch (Exception ex) {
            return UUID.randomUUID().toString();
        }
    }

    /**
     * 检测当的网络（WLAN、3G/2G）状态
     * @param context Context
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected())
            {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED)
                {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }


    public static String getLocalPhoneNumbers(final Activity activity) {
        try {
            PackageManager pm = activity.getPackageManager();
            boolean permission = (PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.READ_CONTACTS", activity.getPackageName()));
            Log.e("tag", "ReadContacts:" + pm.checkPermission("android.permission.READ_CONTACTS", activity.getPackageName()));
            if (permission) {
                StringBuilder sb = new StringBuilder();
                ContentResolver resolver = activity.getContentResolver();
                Uri uri = Uri.parse("content://com.android.contacts/contacts");
                Cursor cursor = resolver.query(uri, new String[]{"_id"}, null, null, null);
                while (cursor.moveToNext()) {
                    int contractID = cursor.getInt(0);
                    uri = Uri.parse("content://com.android.contacts/contacts/" + contractID + "/data");
                    Cursor cursor1 = resolver.query(uri, new String[]{"mimetype", "data1", "data2"}, null, null, null);
                    while (cursor1.moveToNext()) {
                        String data1 = cursor1.getString(cursor1.getColumnIndex("data1"));
                        String mimeType = cursor1.getString(cursor1.getColumnIndex("mimetype"));

                        if ("vnd.android.cursor.item/phone_v2".equals(mimeType)) { //手机
                            sb.append(data1.replace(" ", "") + ",");
                        }
                    }
                    cursor1.close();
                }
                cursor.close();

                if (sb != null && sb.toString().length() > 0) {
                    return sb.toString().substring(0, sb.toString().length() - 1);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
