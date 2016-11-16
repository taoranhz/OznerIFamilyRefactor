package com.ozner.cup.DBHelper;

import android.util.Log;

import com.google.gson.JsonObject;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by ozner_67 on 2016/11/15.
 * 邮箱：xinde.zhang@cftcn.com
 */

@Entity
public class WaterPurifierAttr {
    private static final String TAG = "WaterPurifierAttr";

    /**
     * Mac:C8:93:46:43:2F:02
     * MachineType : A2S3(CRF)
     * Attr : cool:false,hot:true
     * Disabled : 0
     * CreateTime : /Date(1448208000000)/
     * ModifyTime : /Date(1448208000000)/
     * IsShowDueDay : 0
     * smlinkurl : http://cup.ozner.net/app/gyysj/gyysj.html
     * buylinkurl : http://www.oznerwater.com/lktnew/wap/shopping/confirmOrderFromQrcode.aspx?gid=69
     * tips : 不支持此功能哦
     * days : 10
     * boolshow : 1
     */

    /**
     * "state": 0,
     * "time": 2016/11/16 15:30:51,
     * "nowtime": 2016/11/16 15:30:51
     */

    @Id
    private String Mac;
    private String DeviceType;
    private String Attr;
    private boolean Disabled;
    private long CreateTime;
    private long ModifyTime;
    private boolean IsShowDueDay;
    private String smlinkurl;
    private String buylinkurl;
    private String tips;
    private int days;
    private boolean boolshow;
    private boolean hasCool;
    private boolean hasHot;

    private long filterTime;
    private long filterNowtime;


    public WaterPurifierAttr(String Mac, String DeviceType, String Attr, boolean Disabled, long CreateTime, long ModifyTime,
                             boolean IsShowDueDay, String smlinkurl, String buylinkurl, String tips, int days, boolean boolshow, boolean hasCool,
                             boolean hasHot) {
        this.Mac = Mac;
        this.DeviceType = DeviceType;
        this.Attr = Attr;
        this.Disabled = Disabled;
        this.CreateTime = CreateTime;
        this.ModifyTime = ModifyTime;
        this.IsShowDueDay = IsShowDueDay;
        this.smlinkurl = smlinkurl;
        this.buylinkurl = buylinkurl;
        this.tips = tips;
        this.days = days;
        this.boolshow = boolshow;
        this.hasCool = hasCool;
        this.hasHot = hasHot;
    }

    @Generated(hash = 1460846934)
    public WaterPurifierAttr() {
    }

    @Generated(hash = 918272366)
    public WaterPurifierAttr(String Mac, String DeviceType, String Attr, boolean Disabled, long CreateTime, long ModifyTime, boolean IsShowDueDay,
                             String smlinkurl, String buylinkurl, String tips, int days, boolean boolshow, boolean hasCool, boolean hasHot, long filterTime,
                             long filterNowtime) {
        this.Mac = Mac;
        this.DeviceType = DeviceType;
        this.Attr = Attr;
        this.Disabled = Disabled;
        this.CreateTime = CreateTime;
        this.ModifyTime = ModifyTime;
        this.IsShowDueDay = IsShowDueDay;
        this.smlinkurl = smlinkurl;
        this.buylinkurl = buylinkurl;
        this.tips = tips;
        this.days = days;
        this.boolshow = boolshow;
        this.hasCool = hasCool;
        this.hasHot = hasHot;
        this.filterTime = filterTime;
        this.filterNowtime = filterNowtime;
    }

    public String getMac() {
        return Mac;
    }

    public void setMac(String mac) {
        Mac = mac;
    }

    public boolean isShowDueDay() {
        return IsShowDueDay;
    }

    public void setShowDueDay(boolean showDueDay) {
        IsShowDueDay = showDueDay;
    }

    public String getDeviceType() {
        return DeviceType;
    }

    public void setDeviceType(String DeviceType) {
        this.DeviceType = DeviceType;
    }

    public String getAttr() {
        return Attr;
    }

    public void setAttr(String Attr) {
        this.Attr = Attr;
    }

    public boolean isDisabled() {
        return Disabled;
    }

    public void setDisabled(boolean Disabled) {
        this.Disabled = Disabled;
    }

    public long getCreateTime() {
        return CreateTime;
    }

    public void setCreateTime(String CreateTime) {
        try {
            this.CreateTime = Long.parseLong(CreateTime.replace("/Date(", "").replace(")/", ""));
        } catch (Exception ex) {
            ex.printStackTrace();
            this.CreateTime = 0;
        }
    }

    public long getModifyTime() {
        return ModifyTime;
    }

    public void setModifyTime(String ModifyTime) {
        try {
            this.ModifyTime = Long.parseLong(ModifyTime.replace("/Date(", "").replace(")/", ""));
        } catch (Exception ex) {
            ex.printStackTrace();
            this.ModifyTime = 0;
        }
    }

    public boolean isIsShowDueDay() {
        return IsShowDueDay;
    }

    public void setIsShowDueDay(boolean IsShowDueDay) {
        this.IsShowDueDay = IsShowDueDay;
    }

    public String getSmlinkurl() {
        return smlinkurl;
    }

    public void setSmlinkurl(String smlinkurl) {
        this.smlinkurl = smlinkurl;
    }

    public String getBuylinkurl() {
        return buylinkurl;
    }

    public void setBuylinkurl(String buylinkurl) {
        this.buylinkurl = buylinkurl;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public boolean isBoolshow() {
        return boolshow;
    }

    public void setBoolshow(boolean boolshow) {
        this.boolshow = boolshow;
    }

    public boolean getDisabled() {
        return this.Disabled;
    }

    public void setCreateTime(long CreateTime) {
        this.CreateTime = CreateTime;
    }

    public void setModifyTime(long ModifyTime) {
        this.ModifyTime = ModifyTime;
    }

    public boolean getIsShowDueDay() {
        return this.IsShowDueDay;
    }

    public boolean getBoolshow() {
        return this.boolshow;
    }

    public boolean isHasCool() {
        return hasCool;
    }

    public void setHasCool(boolean hasCool) {
        this.hasCool = hasCool;
        this.Attr = "cool:" + hasCool + ",hot:" + hasHot;
    }

    public boolean isHasHot() {
        return hasHot;
    }

    public void setHasHot(boolean hasHot) {
        this.hasHot = hasHot;
        this.Attr = "cool:" + hasCool + ",hot:" + hasHot;
    }


    public long getFilterTime() {
        return filterTime;
    }

    public void setFilterTime(long filterTime) {
        this.filterTime = filterTime;
    }

    public long getFilterNowtime() {
        return filterNowtime;
    }

    public void setFilterNowtime(long filterNowtime) {
        this.filterNowtime = filterNowtime;
    }

    public void fromJsonObject(String mac, JsonObject jsonObject) {
        try {
            setMac(mac);
            setDeviceType(jsonObject.get("MachineType").getAsString());
            setDisabled(jsonObject.get("Disabled").getAsBoolean());
            setCreateTime(jsonObject.get("CreateTime").getAsString());
            setModifyTime(jsonObject.get("ModifyTime").getAsString());
            setIsShowDueDay(jsonObject.get("IsShowDueDay").getAsBoolean());
            setSmlinkurl(jsonObject.get("smlinkurl").getAsString());
            setBuylinkurl(jsonObject.get("buylinkurl").getAsString());
            setTips(jsonObject.get("tips").getAsString());
            setDays(jsonObject.get("days").getAsInt());
            setBoolshow(jsonObject.get("boolshow").getAsBoolean());

            String attr = jsonObject.get("Attr").getAsString();
            setAttr(attr);
            setHasCool(Boolean.parseBoolean(attr.split(",")[0].split(":")[1]));
            setHasHot(Boolean.parseBoolean(attr.split(",")[1].split(":")[1]));
        } catch (Exception ex) {
            Log.e(TAG, "fromJsonObject_Ex: " + ex.getMessage());
        }
    }

    public boolean getHasCool() {
        return this.hasCool;
    }

    public boolean getHasHot() {
        return this.hasHot;
    }
}
