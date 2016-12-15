package com.ozner.cup.Chat.ChatHttpHelper;

import com.google.gson.JsonObject;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by ozner_67 on 2016/12/14.
 * 邮箱：xinde.zhang@cftcn.com
 * <p>
 * 咨询部分网络请求
 */

public interface ChatService {
    /**
     * 咨询获取accesstoken
     *
     * @param appid
     * @param appsecret
     * @param sign
     *
     * @return
     */
    @GET("api/token.ashx")
    Observable<JsonObject> getAccesstoken(@Query("appid") String appid, @Query("appsecret") String appsecret, @Query("sign") String sign);


    /**
     * @param mobile
     * @param accesstoken
     * @param sign
     *
     * @return
     */
    @GET("api/member.ashx")
    Observable<JsonObject> getChatUserInfo(@Query("mobile") String mobile, @Query("access_token") String accesstoken, @Query("sign") String sign);


    /**
     * 咨询登录
     *
     * @param access_token
     *
     * @return
     */
    @GET("api/customerlogin.ashx")
    Observable<JsonObject> chatLogin(@Query("access_token") String access_token);


}
