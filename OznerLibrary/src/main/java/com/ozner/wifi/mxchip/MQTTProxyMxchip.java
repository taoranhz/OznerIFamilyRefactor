package com.ozner.wifi.mxchip;

import android.content.Context;

/**
 * Created by ozner_67 on 2017/5/22.
 * 邮箱：xinde.zhang@cftcn.com
 *
 * 庆科1.0 代理
 */

public class MQTTProxyMxchip extends SMQTTProxy {
    private final static String host_mxch = "tcp://api.easylink.io:1883";
    public MQTTProxyMxchip(Context context) {
        super(context);
        init(host_mxch,"ProxyMxchip");
    }
}
