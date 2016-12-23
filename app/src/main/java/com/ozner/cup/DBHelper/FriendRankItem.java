package com.ozner.cup.DBHelper;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by ozner_67 on 2016/12/23.
 * 邮箱：xinde.zhang@cftcn.com
 */
@Entity
public class FriendRankItem {

    /**
     * id : 1
     * rank : 1
     * max : 256
     * likenumaber : 10
     * userid : d6cd332e-6c7b-482f-be77-06cd2570d54c
     * vuserid : d6cd332e-6c7b-482f-be77-06cd2570d54c
     * type : SC001
     * notify : 1
     * notime : /Date(1451232000000)/
     * Nickname : null
     * Icon : null
     * Score : null
     */
    @Id
    private String id;
    private String rank;
    private String max;
    private String likenumaber;
    @Id
    private String userid;
    private String vuserid;
    private String type;
    private String notify;
    private long notime;
    private String Nickname;
    private String Icon;
    private String Score;

    @Generated(hash = 468449123)
    public FriendRankItem(String id, String rank, String max, String likenumaber,
            String userid, String vuserid, String type, String notify, long notime,
            String Nickname, String Icon, String Score) {
        this.id = id;
        this.rank = rank;
        this.max = max;
        this.likenumaber = likenumaber;
        this.userid = userid;
        this.vuserid = vuserid;
        this.type = type;
        this.notify = notify;
        this.notime = notime;
        this.Nickname = Nickname;
        this.Icon = Icon;
        this.Score = Score;
    }

    @Generated(hash = 579252413)
    public FriendRankItem() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public String getLikenumaber() {
        return likenumaber;
    }

    public void setLikenumaber(String likenumaber) {
        this.likenumaber = likenumaber;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getVuserid() {
        return vuserid;
    }

    public void setVuserid(String vuserid) {
        this.vuserid = vuserid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNotify() {
        return notify;
    }

    public void setNotify(String notify) {
        this.notify = notify;
    }

    public long getNotime() {
        return notime;
    }

    public void setNotime(String notime) {
        try {
            this.notime = Long.parseLong(notime.replace("/Date(", "").replace(")/", ""));
        } catch (Exception ex) {
            this.notime = 0;
        }
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

    public void setNotime(long notime) {
        this.notime = notime;
    }
}
