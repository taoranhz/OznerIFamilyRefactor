package com.ozner.cup.HttpHelper;


import com.google.gson.JsonObject;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by ozner_67 on 2016/11/1.
 * 邮箱：xinde.zhang@cftcn.com
 */

public interface OznerHttpService {

    /**
     * 获取手机验证码
     *
     * @param phone
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerServer/GetPhoneCode")
    Observable<JsonObject> getPhoneCode(@Field("phone") String phone);

    /**
     * 获取语音验证码
     *
     * @param phone
     *
     * @return
     */
    @FormUrlEncoded
    @POST("Oznerserver/GetVoicePhoneCode")
    Observable<JsonObject> getVoicePhoneCode(@Field("phone") String phone);

    /**
     * 用户登录
     *
     * @param phone
     * @param password
     * @param miei
     * @param devicename
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerServer/Login")
    Observable<JsonObject> login(@Field("UserName") String phone, @Field("PassWord") String password
            , @Field("miei") String miei, @Field("devicename") String devicename);


    /**
     * 获取用户信息
     * <p>
     * 注：和获取头像信息联合使用完成UserInfo对象
     *
     * @param usertoken
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerServer/GetUserInfo")
    Observable<JsonObject> getUserInfo(@Field("usertoken") String usertoken);

    /**
     * 获取用户头像信息和积分信息
     * <p>
     * 注：和获取用户信息联合使用完成UserInfo对象
     *
     * @param usertoken
     * @param mobile
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerServer/GetUserNickImage")
    Observable<JsonObject> getUserNickImage(@Field("usertoken") String usertoken, @Field("jsonmobile") String mobile);


    /**
     * 获取净水器属性信息
     *
     * @param mac
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerServer/GetMachineType")
    Observable<JsonObject> getMachineType(@Field("type") String mac);

    /**
     * 获取水机滤芯信息
     *
     * @param mac
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerDevice/GetMachineLifeOutTime")
    Observable<JsonObject> getWaterFilterInfo(@Field("mac") String mac);


    /**
     * 获取水探头滤芯信息
     *
     * @param usertoken
     * @param mac
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerDevice/FilterService")
    Observable<JsonObject> getTapFilterInfo(@Field("usertoken") String usertoken, @Field("mac") String mac);


    /**
     * 获取室外天气信息
     *
     * @return
     */
    @GET("OznerServer/GetWeather")
    Observable<JsonObject> getWeather();

    /**
     * 上传TDS获取排名
     *
     * @param usertoken
     * @param mac
     * @param type
     * @param tds
     * @param beforetds 水机必须字段 TDS 净化前的值 针对水机
     * @param dsn       Ayla 设备标识 String
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerDevice/TDSSensor")
    Observable<JsonObject> updateTDSSensor(@Field("usertoken") String usertoken, @Field("mac") String mac
            , @Field("type") String type, @Field("tds") String tds
            , @Field("beforetds") String beforetds, @Field("dsn") String dsn);

    /**
     * 获取朋友圈TDS排名
     *
     * @param usertoken
     * @param type
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerDevice/TdsFriendRank")
    Observable<JsonObject> getTdsFriendRank(@Field("usertoken") String usertoken, @Field("type") String type);


    /**
     * 获取朋友圈饮水量排名
     *
     * @param usertoken
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerDevice/VolumeFriendRank")
    Observable<JsonObject> getVolumeFriendRank(@Field("usertoken") String usertoken);

    /**
     * 更新当天饮水量获取好友内排名
     *
     * @param usertoken
     * @param mac
     * @param type
     * @param volume
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerDevice/VolumeSensor")
    Observable<JsonObject> updateVolumeSensor(@Field("usertoken") String usertoken, @Field("mac") String mac
            , @Field("type") String type, @Field("volume") String volume);


    /**
     * 绑定百度推送设备id
     *
     * @param usertoken
     * @param channelId
     * @param deviceId
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerServer/UpdateUserInfo")
    Observable<JsonObject> updateUserInfoBD(@Field("usertoken") String usertoken, @Field("channel_id") String channelId, @Field("device_id") String deviceId);

    /**
     * 获取未读排名通知
     * 注意此接口获取的是未读通知，调用后会统一标示已读。因此第一调用后把数据保存到本地供再次展示和其他操作
     *
     * @param usertoken
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerDevice/GetRankNotify")
    Observable<JsonObject> getRankNotify(@Field("usertoken") String usertoken);

    /**
     * 获取赞我的人
     *
     * @param usertoken
     * @param type
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerDevice/WhoLikeMe")
    Observable<JsonObject> getWhoLikeMe(@Field("usertoken") String usertoken, @Field("type") String type);

    /**
     * 对其他用户点赞
     *
     * @param usertoken
     * @param likeUserid
     * @param type
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerDevice/LikeOtherUser")
    Observable<JsonObject> likeOtherUser(@Field("usertoken") String usertoken, @Field("likeuserid") String likeUserid, @Field("type") String type);

    /**
     * 获取朋友列表
     *
     * @param usertoken
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerServer/GetFriendList")
    Observable<JsonObject> getFriendList(@Field("usertoken") String usertoken);

    /**
     * 获取历史留言
     *
     * @param usertoken
     * @param otherUserid
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerDevice/GetHistoryMessage")
    Observable<JsonObject> getHistoryMessage(@Field("usertoken") String usertoken, @Field("otheruserid") String otherUserid);

    /**
     * 留言
     *
     * @param usertoken
     * @param otherUserid
     * @param leaveMsg
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerDevice/LeaveMessage")
    Observable<JsonObject> leaveMessage(@Field("usertoken") String usertoken, @Field("otheruserid") String otherUserid, @Field("message") String leaveMsg);

    /**
     * 获取验证消息列表
     *
     * @param usertoken
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerServer/GetUserVerifMessage")
    Observable<JsonObject> getVerifyMessage(@Field("usertoken") String usertoken);

    /**
     * 接受验证请求
     *
     * @param usertoken
     * @param id
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerServer/AcceptUserVerif")
    Observable<JsonObject> acceptUserVerify(@Field("usertoken") String usertoken, @Field("id") String id);

    /**
     * 发送添加验证信息
     *
     * @param usertoken
     * @param mobile
     * @param content
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerServer/AddFriend")
    Observable<JsonObject> addFriend(@Field("usertoken") String usertoken, @Field("mobile") String mobile, @Field("content") String content);

    /**
     * 提交意见
     *
     * @param usertoken
     * @param message
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerServer/SubmitOpinion")
    Observable<JsonObject> submitOption(@Field("usertoken") String usertoken, @Field("message") String message);

    /**
     * 获取最新版本号
     *
     * @param os
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerServer/GetNewVersion")
    Observable<JsonObject> getNewVersion(@Field("os") String os);


    /**
     * 上传补水仪检测数据
     *
     * @param usertoken
     * @param mac       补水仪设备地址
     * @param oilValue  油性值
     * @param moisValue 水分值
     * @param action    检测部位，取值范围如下：
     *                  脸部：FaceSkinValue,
     *                  手部：HandSkinValue,
     *                  眼部：EyesSkinValue,
     *                  颈部：NeckSkinValue
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerDevice/UpdateBuShuiYiNumber")
    Observable<JsonObject> updateBuShuiYiNumber(@Field("usertoken") String usertoken, @Field("mac") String mac
            , @Field("ynumber") String oilValue, @Field("snumber") String moisValue, @Field("action") String action);


    /**
     * 获取补水仪检测历史检测数据
     *
     * @param usertoken
     * @param mac       补水仪mac地址
     * @param action    检测位置参数，取值范围如下：(为空时获取所有部位数据)
     *                  脸部：FaceSkinValue,
     *                  手部：HandSkinValue,
     *                  眼部：EyesSkinValue,
     *                  颈部：NeckSkinValue
     *
     * @return 返回数据示例：{"state":1,"data":{"FaceSkinValue":{"week":[{"id":235,"userid":"b376615c-0718-43b4-9103-d3906d02d2b9","mac":"E2:68:46:AC:F7:88","ynumber":14.05,"snumber":32.6,"action":"FaceSkinValue","times":2,"updatetime":"/Date(1484755200000)/"},{"id":236,"userid":"b376615c-0718-43b4-9103-d3906d02d2b9","mac":"E2:68:46:AC:F7:88","ynumber":16.975,"snumber":38.475,"action":"FaceSkinValue","times":2,"updatetime":"/Date(1484841600000)/"}],"monty":[{"id":235,"userid":"b376615c-0718-43b4-9103-d3906d02d2b9","mac":"E2:68:46:AC:F7:88","ynumber":14.05,"snumber":32.6,"action":"FaceSkinValue","times":2,"updatetime":"/Date(1484755200000)/"},{"id":236,"userid":"b376615c-0718-43b4-9103-d3906d02d2b9","mac":"E2:68:46:AC:F7:88","ynumber":16.975,"snumber":38.475,"action":"FaceSkinValue","times":2,"updatetime":"/Date(1484841600000)/"}]}}}
     */
    @FormUrlEncoded
    @POST("OznerServer/GetBuShuiFenBu")
    Observable<JsonObject> getBuShuiFenBu(@Field("usertoken") String usertoken, @Field("mac") String mac, @Field("myaction") String action);


    /**
     * 肤质详情页检测总次数
     *
     * @param usertoken
     * @param mac
     *
     * @return 返回数据示例：{"state":3,"data":[{"id":138,"userid":"b376615c-0718-43b4-9103-d3906d02d2b9","mac":"E2:68:46:AC:F7:88","ynumber":null,"snumber":null,"times":4,"action":"FaceSkinValue","updatetime":"/Date(1484809092587)/"},{"id":139,"userid":"b376615c-0718-43b4-9103-d3906d02d2b9","mac":"E2:68:46:AC:F7:88","ynumber":null,"snumber":null,"times":1,"action":"EyesSkinValue","updatetime":"/Date(1484879958607)/"},{"id":140,"userid":"b376615c-0718-43b4-9103-d3906d02d2b9","mac":"E2:68:46:AC:F7:88","ynumber":null,"snumber":null,"times":1,"action":"NeckSkinValue","updatetime":"/Date(1484880218780)/"}]}
     */
    @FormUrlEncoded
    @POST("OznerDevice/GetTimesCountBuShui")
    Observable<JsonObject> getTimesCountBuShui(@Field("usertoken") String usertoken, @Field("mac") String mac);


    /**
     * 更新滤芯服务时间
     *
     * @param usertoken
     * @param mac
     * @param devicetype
     * @param code
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerDevice/RenewFilterTime")
    Observable<JsonObject> reNewFilterTime(@Field("usertoken") String usertoken, @Field("mac") String mac, @Field("devicetype") String devicetype, @Field("code") String code);


    /**
     * 邮箱登录
     *
     * @param email
     * @param password
     * @param miei
     * @param devicename
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerServer/MailLogin")
    Observable<JsonObject> emailLogin(@Field("username") String email, @Field("password") String password, @Field("miei") String miei, @Field("devicename") String devicename);

    /**
     * 获取邮箱验证码
     *
     * @param email
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerServer/GetEmailCode")
    Observable<JsonObject> getEmailCode(@Field("email") String email);

    /**
     * 重置密码
     * @param email
     * @param password
     * @param code
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerServer/ResetPassword")
    Observable<JsonObject> resetPassword(@Field("username") String email, @Field("password") String password, @Field("code") String code);

    /**
     * 注册邮箱账号
     * @param email
     * @param password
     * @param code
     *
     * @return
     */
    @FormUrlEncoded
    @POST("OznerServer/MailRegister")
    Observable<JsonObject> signUpMail(@Field("username") String email,@Field("password") String password,@Field("code") String code);
}
