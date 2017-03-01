package com.ozner.yiquan.MyCenter.MyFriend.bean;

/**
 * Created by ozner_67 on 2016/12/29.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class LikeMeItem {

    /**
     * id : 5
     * userid : fbc92bf0-5d98-4a5a-badd-070364aa9a10
     * likeuserid : d6cd332e-6c7b-482f-be77-06cd2570d54c
     * devicetype : SC001TDS
     * liketime : /Date(1451543945833)/
     * Mobile : null
     * Nickname : null
     * Icon : null
     * Score : null
     */

    private int id;
    private String userid;
    private String likeuserid;
    private String devicetype;
    private String liketime;
    private String Mobile;
    private String Nickname;
    private String Icon;
    private String Score;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getLikeuserid() {
        return likeuserid;
    }

    public void setLikeuserid(String likeuserid) {
        this.likeuserid = likeuserid;
    }

    public String getDevicetype() {
        return devicetype;
    }

    public void setDevicetype(String devicetype) {
        this.devicetype = devicetype;
    }

    public String getLiketime() {
        return liketime;
    }

    public void setLiketime(String liketime) {
        this.liketime = liketime;
    }

    public String getMobile() {
        return Mobile;
    }

    public void setMobile(String Mobile) {
        this.Mobile = Mobile;
    }

    public String getNickname() {
        return Nickname;
    }

    public void setNickname(String Nickname) {
        this.Nickname = Nickname;
    }

    public String getIcon() {
        return Icon;
    }

    public void setIcon(String Icon) {
        this.Icon = Icon;
    }

    public String getScore() {
        return Score;
    }

    public void setScore(String Score) {
        this.Score = Score;
    }
}
