package com.ozner.cup.Chat.ChatHttpUtils;

/**
 * Created by ozner_67 on 2016/12/15.
 * 邮箱：xinde.zhang@cftcn.com
 *
 * 咨询接口
 */

public class ChatHttpBean {
    public static final String ChatAppid = "hzapi";
    public static final String ChatAppsecret = "8af0134asdffe12";

    public static final String ChatBaseUrl = "http://dkf.ozner.net";
    public static final String TokenActionUrl = "api/token.ashx";//获取token
    public static final String UserInfoActionUrl = "api/member.ashx";//获取用户信息
    public static final String LoginActionUrl = "api/customerlogin.ashx";//登录
    public static final String SendMsgActionUrl = "/api/customermsg.ashx";//用户发送信息
    public static final String HistoryActionUrl = "/api/historyrecord.ashx";//获取历史记录
    public static final String KillqueueActionUrl = "/api/cuskillqueue.ashx";//用户结束会话
    public static final String UploadImgActionUrl = "/api/uploadpic.ashx";//上传图片

}
