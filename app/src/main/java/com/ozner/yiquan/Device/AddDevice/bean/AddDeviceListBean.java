package com.ozner.yiquan.Device.AddDevice.bean;

/**
 * Created by ozner_67 on 2016/11/7.
 * 邮箱：xinde.zhang@cftcn.com
 * <p>
 * 选择设备配对列表Entity
 */

public class AddDeviceListBean {
    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getConnectType() {
        return connectType;
    }

    public void setConnectType(String connectType) {
        this.connectType = connectType;
    }

    public int getConnectResId() {
        return connectResId;
    }

    public void setConnectResId(int connectResId) {
        this.connectResId = connectResId;
    }

    public int getDeviceIconResId() {
        return deviceIconResId;
    }

    public void setDeviceIconResId(int deviceIconResId) {
        this.deviceIconResId = deviceIconResId;
    }

    private String deviceType;
    private String connectType;
    private int connectResId;
    private int deviceIconResId;
}
