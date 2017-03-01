package com.ozner.yiquan.MyCenter.MyFriend.bean;

/**
 * Created by ozner_67 on 2017/1/3.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class VerifyMessageItem {
    //主键ID
    public String ID;
    //发送者手机号
    public String Mobile;
    //接收者手机号
    public String FriendMobile;
    //请求的消息内容
    public String RequestContent;
    //状态 1正在申请，2已经是好友
    public int Status;
    public int Disabled;
    //创建者
    public String CreateBy;
    //创建时间
    public String CreateTime;
    //昵称
    public String Nickname;
    public String Icon;
    public String OtherMobile;
    public int Score;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getMobile() {
        return Mobile;
    }

    public void setMobile(String mobile) {
        Mobile = mobile;
    }

    public String getFriendMobile() {
        return FriendMobile;
    }

    public void setFriendMobile(String friendMobile) {
        FriendMobile = friendMobile;
    }

    public String getRequestContent() {
        return RequestContent;
    }

    public void setRequestContent(String requestContent) {
        RequestContent = requestContent;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    public int getDisabled() {
        return Disabled;
    }

    public void setDisabled(int disabled) {
        Disabled = disabled;
    }

    public String getCreateBy() {
        return CreateBy;
    }

    public void setCreateBy(String createBy) {
        CreateBy = createBy;
    }

    public String getCreateTime() {
        return CreateTime;
    }

    public void setCreateTime(String createTime) {
        CreateTime = createTime;
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

    public String getOtherMobile() {
        return OtherMobile;
    }

    public void setOtherMobile(String otherMobile) {
        OtherMobile = otherMobile;
    }

    public int getScore() {
        return Score;
    }

    public void setScore(int score) {
        Score = score;
    }
}
