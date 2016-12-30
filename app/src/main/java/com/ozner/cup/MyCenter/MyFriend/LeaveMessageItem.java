package com.ozner.cup.MyCenter.MyFriend;

/**
 * Created by ozner_67 on 2016/12/29.
 * 邮箱：xinde.zhang@cftcn.com
 * <p>
 * 好友留言实体
 */

public class LeaveMessageItem {
    private int id;
    private String senduserid;
    private String recvuserid;
    private String message;
    private String stime;
    private String Mobile;
    private String Nickname;
    private String Icon;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSenduserid() {
        return senduserid;
    }

    public void setSenduserid(String senduserid) {
        this.senduserid = senduserid;
    }

    public String getRecvuserid() {
        return recvuserid;
    }

    public void setRecvuserid(String recvuserid) {
        this.recvuserid = recvuserid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStime() {
        return stime;
    }

    public void setStime(String stime) {
        this.stime = stime;
    }

    public String getMobile() {
        return Mobile;
    }

    public void setMobile(String mobile) {
        Mobile = mobile;
    }

    public String getNickname() {
        return Nickname;
    }

    public void setNickname(String nickname) {
        Nickname = nickname;
    }

    public String getIcon() {
        return Icon;
    }

    public void setIcon(String icon) {
        Icon = icon;
    }
}
