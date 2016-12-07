package com.ozner.cup.DBHelper;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by ozner_67 on 2016/12/7.
 * 邮箱：xinde.zhang@cftcn.com
 */

@Entity
public class UserInfo {
    /**
     * userId : b376615c-0718-43b4-9103-d3906d02d2b9
     * mobile : 13166398575
     * nickname : 遇见
     * headimg : http://wx.qlogo.cn/mmopen/KiclJTickB3NODplquOH5eXvu8m3dX1nkib4OVgHrPEWgE47SXJnOShODVCx3nd5kMhzPlL3mOKB74UyLWVnETByhzsdXHj6Kv6/0
     * GradeName : 银卡会员
     * Score : 0
     * Status : 0
     * device_id:4017995850221438505
     * channel_id:5
     * sex:男
     */

    @Id
    private String userId;
    private String mobile;
    private String nickname;
    private String headimg;
    private String GradeName;
    private String Score;
    private int Status;
    private String deviceId;
    private String channelId;
    private String sex;

    public UserInfo() {
    }

    @Generated(hash = 97911111)
    public UserInfo(String userId, String mobile, String nickname, String headimg, String GradeName, String Score, int Status, String deviceId,
            String channelId, String sex) {
        this.userId = userId;
        this.mobile = mobile;
        this.nickname = nickname;
        this.headimg = headimg;
        this.GradeName = GradeName;
        this.Score = Score;
        this.Status = Status;
        this.deviceId = deviceId;
        this.channelId = channelId;
        this.sex = sex;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getHeadimg() {
        return headimg;
    }

    public void setHeadimg(String headimg) {
        this.headimg = headimg;
    }

    public String getGradeName() {
        return GradeName;
    }

    public void setGradeName(String GradeName) {
        this.GradeName = GradeName;
    }

    public String getScore() {
        return Score;
    }

    public void setScore(String Score) {
        this.Score = Score;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int Status) {
        this.Status = Status;
    }

    @Override
    public String toString() {
        return
                "======================"
                        + "\nuserid:" + userId
                        + "\nnickName:" + nickname
                        + "\nmobile:" + mobile
                        + "\nheadimg:" + headimg
                        + "\nGradeName:" + GradeName
                        + "\nScore:" + Score
                        + "\nStatus:" + Status
                        + "\ndeviceId:" + deviceId
                        + "\nsex:" + sex
                        + "\n======================";
    }
}
