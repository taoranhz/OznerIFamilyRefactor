package com.ozner.yiquan.MyCenter.MyFriend.bean;

/**
 * Created by ozner_67 on 2016/12/27.
 * 邮箱：xinde.zhang@cftcn.com
 *
 * 个人中心tds排名数据
 */

public class CenterRankItem {
    private int rank;
    private String userid;
    private int volume;
    private String Nickname;
    private String Icon;
    private String Score;
    private int isLike;
    private int LikeCount;

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
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

    public int getIsLike() {
        return isLike;
    }

    public void setIsLike(int isLike) {
        this.isLike = isLike;
    }

    public int getLikeCount() {
        return LikeCount;
    }

    public void setLikeCount(int likeCount) {
        this.LikeCount = likeCount;
    }

    public String getScore() {
        return Score;
    }

    public void setScore(String score) {
        Score = score;
    }
}
