package com.ozner.cup.MyCenter.MyFriend.bean;

/**
 * Created by ozner_67 on 2016/12/29.
 * 邮箱：xinde.zhang@cftcn.com
 *
 * 好友列表实体
 */

public class FriendItem {
    private int Id;
    private String Mobile;
    private String FriendMobile;
    private String RequestContent;
    private int Status;
    private int Disabled;
    private String CreateBy;
    private String CreateTime;
    private String ModifyBy;
    private String ModifyTime;
    private String Nickname;
    private String Icon;
    private int Score;
    private int MessageCount;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
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

    public String getModifyBy() {
        return ModifyBy;
    }

    public void setModifyBy(String modifyBy) {
        ModifyBy = modifyBy;
    }

    public String getModifyTime() {
        return ModifyTime;
    }

    public void setModifyTime(String modifyTime) {
        ModifyTime = modifyTime;
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

    public int getScore() {
        return Score;
    }

    public void setScore(int score) {
        Score = score;
    }

    public int getMessageCount() {
        return MessageCount;
    }

    public void setMessageCount(int messageCount) {
        MessageCount = messageCount;
    }
}
