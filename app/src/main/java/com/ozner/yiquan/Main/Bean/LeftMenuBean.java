package com.ozner.yiquan.Main.Bean;

import java.io.Serializable;

/**
 * Created by ozner_67 on 2016/11/3.
 * 邮箱：xinde.zhang@cftcn.com
 * <p>
 * 侧边栏设备列表item对象
 */

public class LeftMenuBean implements Serializable {


    public int getDeviceTypeResId() {
        return deviceTypeResId;
    }

    public void setDeviceTypeResId(int deviceTypeResId) {
        this.deviceTypeResId = deviceTypeResId;
    }

    public String getConnectState() {
        return connectState;
    }

    public void setConnectState(String connectState) {
        this.connectState = connectState;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceDesc() {
        return deviceDesc;
    }

    public void setDeviceDesc(String deviceDesc) {
        this.deviceDesc = deviceDesc;
    }

    public int getDeviceiconResId() {
        return deviceiconResId;
    }

    public void setDeviceiconResId(int deviceiconResId) {
        this.deviceiconResId = deviceiconResId;
    }

    private int deviceiconResId;
    private int deviceTypeResId;
    private String connectState;
    private String deviceName;
    private String deviceDesc;
}
