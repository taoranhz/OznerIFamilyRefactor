package com.ozner.cup.DBHelper;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.json.JSONObject;

/**
 * Created by ozner_67 on 2017/1/6.
 * 邮箱：xinde.zhang@cftcn.com
 */

@Entity
public class OznerDeviceSettings {
    /**
     * {
     * "SysId": "9fb4b842-29f2-41e6-bfb3-7857cbfe8511",
     * "Mac": "37:16:12:24:03:65",
     * "Name": "Ozner Cup",
     * "DeviceType": "CP001",
     * "DeviceAddress": "我的杯子",
     * "Settings": null,
     * "UserId": "02729c03-e049-4e80-8c1b-7aaa337dfe73",
     * "Weight": null,
     * "CreateBy": "02729c03-e049-4e80-8c1b-7aaa337dfe73",
     * "createTime": "\/Date(1449119292500)\/",
     * "ModifyBy": "02729c03-e049-4e80-8c1b-7aaa337dfe73",
     * "ModifyTime": "\/Date(1449119292503)\/"
     * }
     */
    //保存时间
    @Id
    private String createTime;
    //设备Mac
    private String mac;
    //设备名字
    private String name;
    //设备类型
    private String devcieType;
    //设备使用位置
    private String devicePosition;
    //设备设置
    private String Settings;
    //用户userid
    private String userId;

    //同步网络状态；0-未同步，1-已同步
    private int status;


    @Generated(hash = 1570120336)
    public OznerDeviceSettings(String createTime, String mac, String name,
                               String devcieType, String devicePosition, String Settings,
                               String userId, int status) {
        this.createTime = createTime;
        this.mac = mac;
        this.name = name;
        this.devcieType = devcieType;
        this.devicePosition = devicePosition;
        this.Settings = Settings;
        this.userId = userId;
        this.status = status;
    }

    @Generated(hash = 2028783457)
    public OznerDeviceSettings() {
    }


    /**
     * 读取app绑定设备字段
     *
     * @param key
     *
     * @return
     */
    public Object getAppData(String key) {
        JSONObject jsonObject = null;
        if (Settings != null && Settings.length() > 0) {
            try {
                jsonObject = new JSONObject(Settings);
                return jsonObject.get(key);
            } catch (Exception ex) {
                jsonObject = null;
            }
        }
        return null;
    }

    /**
     * 保存app绑定设备字段
     *
     * @param key
     * @param value
     */
    public void setAppData(String key, Object value) {
        JSONObject jsonObject = null;
        if (Settings != null && Settings.length() > 0) {
            try {
                jsonObject = new JSONObject(Settings);
            } catch (Exception ex) {
                ex.printStackTrace();
                jsonObject = null;
            }
        }

        if (jsonObject == null) {
            jsonObject = new JSONObject();
        }
        try {
            jsonObject.put(key, value);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Settings = jsonObject.toString();
    }


    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

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

    public String getDevcieType() {
        return devcieType;
    }

    public void setDevcieType(String devcieType) {
        this.devcieType = devcieType;
    }

    public String getDevicePosition() {
        return devicePosition;
    }

    public void setDevicePosition(String devicePosition) {
        this.devicePosition = devicePosition;
    }

    public String getSettings() {
        return Settings;
    }

    public void setSettings(String settings) {
        Settings = settings;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("OnzerDeviceSettings:")
                .append("\nMac:").append(mac)
                .append("\nName:").append(name)
                .append("\nType:").append(devcieType)
                .append("\nUserId:").append(userId)
                .append("\nDevicePos:").append(devicePosition)
                .append("\nstatus:").append(status)
                .append("\nSettings:").append(Settings);
        return strBuilder.toString();
    }
}
