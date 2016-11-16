package com.ozner.cup.DBHelper;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by ozner_67 on 2016/11/15.
 * 邮箱：xinde.zhang@cftcn.com
 */

@Entity
public class WaterPurifierAttr {

    /**
     * Mac:C8:93:46:43:2F:02
     * DeviceType : A2S3(CRF)
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

    @Generated(hash = 1502671221)
    public WaterPurifierAttr(String Mac, String DeviceType, String Attr, boolean Disabled,
            long CreateTime, long ModifyTime, boolean IsShowDueDay, String smlinkurl, String buylinkurl,
            String tips, int days, boolean boolshow) {
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
    }

    @Generated(hash = 1460846934)
    public WaterPurifierAttr() {
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
}
