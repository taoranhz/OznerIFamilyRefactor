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
}
