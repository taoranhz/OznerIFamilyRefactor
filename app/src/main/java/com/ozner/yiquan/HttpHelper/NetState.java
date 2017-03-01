package com.ozner.yiquan.HttpHelper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by ozner_67 on 2016/11/17.
 * 邮箱：xinde.zhang@cftcn.com
 * <p>
 * 获取当前网络状态
 */

public class NetState {


    public enum State {
        CONNECTED,//连接网络
        NONE//未连接网络
    }

    /**
     * 判断当前网络状态
     *
     * @param context
     *
     * @return
     */
    public static State checkNetwork(Context context) {
        try {
            NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                return State.CONNECTED;
            } else {
                return State.NONE;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return State.NONE;
        }
    }
}
