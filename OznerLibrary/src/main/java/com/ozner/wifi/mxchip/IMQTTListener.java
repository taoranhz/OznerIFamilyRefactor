package com.ozner.wifi.mxchip;

/**
 * Created by ozner_67 on 2017/5/22.
 * 邮箱：xinde.zhang@cftcn.com
 */

public interface IMQTTListener {
    void onConnected(SMQTTProxy proxy);

    void onDisconnected(SMQTTProxy proxy);

    void onPublish(SMQTTProxy proxy, String topic, byte[] data);
}
