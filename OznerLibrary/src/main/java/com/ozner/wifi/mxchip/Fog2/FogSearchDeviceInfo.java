package com.ozner.wifi.mxchip.Fog2;

import org.json.JSONObject;

/**
 * Created by ozner_67 on 2017/5/16.
 * 邮箱：xinde.zhang@cftcn.com
 *
 * 庆科2.0 搜索到的设备信息
 */

public class FogSearchDeviceInfo {
    /**
     * Name : EMW3162 Module#4E0374
     * IP : 172.20.10.5
     * Port : 8002
     * MAC : C8:93:46:4E:03:74
     * Firmware Rev : FOG_V2_EMW3162@005
     * FogProductId : 737bc5a2-f345-11e6-9d95-00163e103941
     * IsHaveSuperUser : false
     * MICO OS Rev : 31621002.050
     * Model : EMW3162
     * Protocol : fog2.6.0
     */

    public String Name;
    public String IP;
    public String Port;
    public String MAC;
    public String Firmware_Rev;
    public String FogProductId;
    public boolean IsHaveSuperUser;
    public String MICOOS_Rev;
    public String Model;
    public String Protocol;
    public String deviceId = null;

    public void fromJSONObject(JSONObject jsonObject) {
        try {
            if (jsonObject != null) {
                Name = jsonObject.getString("Name");
                IP = jsonObject.getString("IP");
                Port = jsonObject.getString("Port");
                MAC = jsonObject.getString("MAC");
                Firmware_Rev = jsonObject.getString("Firmware Rev");
                FogProductId = jsonObject.getString("FogProductId");
                IsHaveSuperUser = jsonObject.getBoolean("IsHaveSuperUser");
                MICOOS_Rev = jsonObject.getString("MICO OS Rev");
                Model = jsonObject.getString("Model");
                Protocol = jsonObject.getString("Protocol");
            }
        } catch (Exception ex) {
            System.out.print(ex.getMessage());
        }
    }

}
