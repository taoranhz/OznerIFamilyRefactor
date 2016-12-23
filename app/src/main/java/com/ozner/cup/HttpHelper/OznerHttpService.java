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
}
