package com.ozner.wifi.mxchip;

import android.content.Context;

/**
 * Created by ozner_67 on 2017/5/22.
 * 邮箱：xinde.zhang@cftcn.com
 * 庆科2.0 代理
 */

public class MQTTProxyFog extends SMQTTProxy {
    private final static String host_fog = "tcp://211.136.146.211:1883";

    public MQTTProxyFog(Context context) {
        super(context);
        init(host_fog,"ProxyFog");
    }
}
