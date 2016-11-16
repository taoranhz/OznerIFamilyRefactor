package com.ozner.cup.HttpHelper;


import com.google.gson.JsonObject;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
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


    @FormUrlEncoded
    @POST("OznerServer/GetMatchineType")
    Observable<JsonObject> getMachineType(@Field("usertoken") String usertoken, @Field("type") String mac);
}
