package com.ozner.yiquan.Main.Bean;

import com.ozner.device.OznerDevice;

/**
 * Created by ozner_67 on 2017/1/9.
 * 邮箱：xinde.zhang@cftcn.com
 * 侧边栏设备列表实体，为了扩展自定义属性
 */

public class LeftMenuDeviceItem {
    private String mac;
    private String name;
    private String usePos;
    private String type;
    private OznerDevice device;

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsePos() {
        return usePos;
    }

    public void setUsePos(String usePos) {
        this.usePos = usePos;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public OznerDevice getDevice() {
        return device;
    }

    public void setDevice(OznerDevice device) {
        this.device = device;
    }


}
