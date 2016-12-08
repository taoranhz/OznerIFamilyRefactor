package com.ozner.cup.HttpHelper;

import android.util.Log;

import com.google.gson.JsonObject;
import com.ozner.cup.Bean.Contacts;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by ozner_67 on 2016/11/1.
 * 邮箱：xinde.zhang@cftcn.com
 * <p>
 * 网络请求工具
 */
public class HttpMethods {
    private static final String TAG = "HttpMethods";

    private static final int DEFAULT_TIMEOUT = 15;//默认超时时间15s

    private Retrofit retrofit;
    private OznerHttpService oznerHttpService;

    private static HttpMethods ourInstance = new HttpMethods();

    public static HttpMethods getInstance() {
        return ourInstance;
    }

    public OznerHttpService getHttpService() {
        return oznerHttpService;
    }

    private HttpMethods() {
        //创建OkHttpClient并设置超时时间
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

        retrofit = new Retrofit.Builder()
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(Contacts.HttpBaseUrl)
                .build();
        oznerHttpService = retrofit.create(OznerHttpService.class);
    }

    /**
     * 返回结果预处理
     */
    class ResultTransFunc1 implements Func1<JsonObject, JsonObject> {

        @Override
        public JsonObject call(JsonObject jsonObject) {
            Log.e(TAG, "result: " + jsonObject.toString());
            if (jsonObject.get("state").getAsInt() <= 0) {
                throw new ApiException(jsonObject.get("state").getAsInt());
            }
            return jsonObject;
        }
    }


    /**
     * 获取验证码
     *
     * @param phone
     * @param subscriber
     */
    public void getPhoneCode(String phone, Subscriber<JsonObject> subscriber) {
        oznerHttpService.getPhoneCode(phone)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new ResultTransFunc1())
                .subscribe(subscriber);
    }

    /**
     * 获取语音验证码
     *
     * @param phone
     * @param subscriber
     */
    public void getVoiceVerifyCode(String phone, Subscriber<JsonObject> subscriber) {
        oznerHttpService.getVoicePhoneCode(phone)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new ResultTransFunc1())
                .subscribe(subscriber);
    }

    /**
     * 登录
     *
     * @param phone
     * @param verifyCode
     * @param miei
     * @param deviceName
     * @param subscriber
     */
    public void login(String phone, String verifyCode, String miei, String deviceName, Subscriber<JsonObject> subscriber) {
        oznerHttpService.login(phone, verifyCode, miei, deviceName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new ResultTransFunc1())
                .subscribe(subscriber);
    }

    /**
     * 获取用户信息
     * <p>
     * 注：和获取头像信息联合使用完成UserInfo对象
     *
     * @param usertoken
     * @param subscriber
     */
    public void getUserInfo(String usertoken, Subscriber<JsonObject> subscriber) {
        oznerHttpService.getUserInfo(usertoken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .map(new ResultTransFunc1())
                .subscribe(subscriber);
    }

    /**
     * 获取净水器属性信息
     *
     * @param mac
     * @param subscriber
     */
    public void getMatchineType(String mac, Subscriber<JsonObject> subscriber) {
        oznerHttpService.getMachineType(mac)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 获取净水器滤芯信息
     *
     * @param mac
     * @param subscriber
     */
    public void getWaterFilterInfo(String mac, Subscriber<JsonObject> subscriber) {
        oznerHttpService.getWaterFilterInfo(mac)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }


    /**
     * 获取室外天气信息
     *
     * @param subscriber
     */
    public void getWeatherOutSide(Subscriber<JsonObject> subscriber) {
        oznerHttpService.getWeather()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 获取水探头滤芯信息
     *
     * @param usertoken
     * @param mac
     * @param subscriber
     */
    public void getTapFilterInfo(String usertoken, String mac, Subscriber<JsonObject> subscriber) {
        oznerHttpService.getTapFilterInfo(usertoken, mac)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .map(new ResultTransFunc1())
                .subscribe(subscriber);
    }


    /**
     * @param usertoken
     * @param mac
     * @param type
     * @param tds
     * @param beforetds  水机必须字段 TDS 净化前的值 针对水机
     * @param dsn        Ayla 设备标识 String
     * @param subscriber
     */
    public void updateTDSSensor(String usertoken, String mac, String type, String tds
            , String beforetds, String dsn, Subscriber<JsonObject> subscriber) {
        oznerHttpService.updateTDSSensor(usertoken, mac, type, tds, beforetds, dsn)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .map(new ResultTransFunc1())
                .subscribe(subscriber);
    }

    /**
     * 获取朋友圈TDS排名
     *
     * @param usertoken
     * @param type
     * @param subscriber
     */
    public void getTdsFriendRank(String usertoken, String type, Subscriber<JsonObject> subscriber) {
        oznerHttpService.getTdsFriendRank(usertoken, type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .map(new ResultTransFunc1())
                .subscribe(subscriber);
    }

    /**
     * 获取朋友圈饮水量排名
     *
     * @param usertoken
     * @param subscriber
     */
    public void getVolumeFriendRank(String usertoken, Subscriber<JsonObject> subscriber) {
        oznerHttpService.getVolumeFriendRank(usertoken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .map(new ResultTransFunc1())
                .subscribe(subscriber);
    }

    /**
     * 更新当天饮水量获取好友内排名
     *
     * @param usertoken
     * @param mac
     * @param type
     * @param volume
     * @param subscriber
     */
    public void updateVolumeSensor(String usertoken, String mac, String type, String volume, Subscriber<JsonObject> subscriber) {
        oznerHttpService.updateVolumeSensor(usertoken, mac, type, volume)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .map(new ResultTransFunc1())
                .subscribe(subscriber);
    }

}
