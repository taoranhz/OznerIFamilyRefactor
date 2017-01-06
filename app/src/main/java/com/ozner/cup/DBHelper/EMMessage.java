package com.ozner.cup.DBHelper;

import com.ozner.cup.Chat.EaseUI.model.MessageDirect;
import com.ozner.cup.Chat.EaseUI.model.MessageType;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by ozner_67 on 2016/12/12.
 * 邮箱：xinde.zhang@cftcn.com
 */

@Entity
public class EMMessage {
    //作为id
    @Id
    private long time;//消息时间
    private String userid;//用户id
    private String content;//消息内容
    private int status;//消息发送状态
    private int mType = MessageType.TXT;
    private int mDirect = MessageDirect.RECEIVE;//消息方向


    @Generated(hash = 269596009)
    public EMMessage(long time, String userid, String content, int status,
            int mType, int mDirect) {
        this.time = time;
        this.userid = userid;
        this.content = content;
        this.status = status;
        this.mType = mType;
        this.mDirect = mDirect;
    }

    @Generated(hash = 652031958)
    public EMMessage() {
    }


    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

//    public int getmType() {
//        return mType;
//    }

//    public void setmType(int mType) {
//        this.mType = mType;
//    }

//    public int getmDirect() {
//        return mDirect;
//    }
//
//    public void setmDirect(int mDirect) {
//        this.mDirect = mDirect;
//    }

    public int getMType() {
        return this.mType;
    }

    public void setMType(int mType) {
        this.mType = mType;
    }

    public int getMDirect() {
        return this.mDirect;
    }

    public void setMDirect(int mDirect) {
        this.mDirect = mDirect;
    }
}
