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
//                .map(new ResultTransFunc1())
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
//                .map(new ResultTransFunc1())
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
//                .map(new ResultTransFunc1())
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
     * 获取用户头像信息和积分信息
     * <p>
     * 注：和获取用户信息联合使用完成UserInfo对象
     *
     * @param usertoken
     * @param mobile
     * @param subscriber
     */
    public void getUserNickImage(String usertoken, String mobile, Subscriber<JsonObject> subscriber) {
        oznerHttpService.getUserNickImage(usertoken, mobile)
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

    /**
     * 绑定百度推送设备
     *
     * @param usertoken
     * @param deviceId
     * @param subscriber
     */
    public void updateUserInfoBD(String usertoken, String deviceId, Subscriber<JsonObject> subscriber) {
        oznerHttpService.updateUserInfoBD(usertoken, "5", deviceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .map(new ResultTransFunc1())
                .subscribe(subscriber);
    }

    /**
     * 获取未读排名通知
     * 注意此接口获取的是未读通知，调用后会统一标示已读。因此第一调用后把数据保存到本地供再次展示和其他操作
     *
     * @param usertoken
     * @param subscriber
     */
    public void getRankNotify(String usertoken, Subscriber<JsonObject> subscriber) {
        oznerHttpService.getRankNotify(usertoken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .map(new ResultTransFunc1())
                .subscribe(subscriber);
    }

    /**
     * 获取赞我的人
     *
     * @param usertoken
     * @param type
     *
     * @return
     */
    public void getWhoLikeMe(String usertoken, String type, Subscriber<JsonObject> subscriber) {
        oznerHttpService.getWhoLikeMe(usertoken, type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .map(new ResultTransFunc1())
                .subscribe(subscriber);
    }

    /**
     * 对其他用户点赞
     *
     * @param usertoken
     * @param likeuserid
     * @param type
     * @param subscriber
     */
    public void likeOtherUser(String usertoken, String likeuserid, String type, Subscriber<JsonObject> subscriber) {
        oznerHttpService.likeOtherUser(usertoken, likeuserid, type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .map(new ResultTransFunc1())
                .subscribe(subscriber);
    }

    /**
     * 获取朋友列表
     *
     * @param usertoken
     * @param subscriber
     */
    public void getFriendList(String usertoken, Subscriber<JsonObject> subscriber) {
        oznerHttpService.getFriendList(usertoken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .map(new ResultTransFunc1())
                .subscribe(subscriber);
    }

    /**
     * 获取历史留言
     *
     * @param usertoken
     * @param otherUserid
     * @param subscriber
     */
    public void getHistoryMessage(String usertoken, String otherUserid, Subscriber<JsonObject> subscriber) {
        oznerHttpService.getHistoryMessage(usertoken, otherUserid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .map(new ResultTransFunc1())
                .subscribe(subscriber);
    }

    /**
     * 留言
     *
     * @param usertoken
     * @param otherUserid
     * @param msg
     * @param subscriber
     */
    public void leaveMessage(String usertoken, String otherUserid, String msg, Subscriber<JsonObject> subscriber) {
        oznerHttpService.leaveMessage(usertoken, otherUserid, msg)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .map(new ResultTransFunc1())
                .subscribe(subscriber);
    }

    /**
     * 获取验证消息列表
     *
     * @param usertoken
     * @param subscriber
     */
    public void getVerifyMessage(String usertoken, Subscriber<JsonObject> subscriber) {
        oznerHttpService.getVerifyMessage(usertoken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .map(new ResultTransFunc1())
                .subscribe(subscriber);
    }

    /**
     * 接受验证请求
     *
     * @param usertoken
     * @param id
     * @param subscriber
     */
    public void acceptUserVerify(String usertoken, String id, Subscriber<JsonObject> subscriber) {
        oznerHttpService.acceptUserVerify(usertoken, id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .map(new ResultTransFunc1())
                .subscribe(subscriber);
    }

    /**
     * 发送添加验证信息
     *
     * @param usertoken
     * @param mobile
     * @param content
     * @param subscriber
     */
    public void addFriend(String usertoken, String mobile, String content, Subscriber<JsonObject> subscriber) {
        oznerHttpService.addFriend(usertoken, mobile, content)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .map(new ResultTransFunc1())
                .subscribe(subscriber);
    }

    /**
     * 提交意见
     *
     * @param usertoken
     * @param message
     * @param subscriber
     */
    public void submitOption(String usertoken, String message, Subscriber<JsonObject> subscriber) {
        oznerHttpService.submitOption(usertoken, message)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .map(new ResultTransFunc1())
                .subscribe(subscriber);
    }

    /**
     * 获取最新版本号
     *
     * @param os
     * @param subscriber
     */
    public void getNewVersion(String os, Subscriber<JsonObject> subscriber) {
        oznerHttpService.getNewVersion(os)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .map(new ResultTransFunc1())
                .subscribe(subscriber);
    }

    /**
     * 上传补水仪检测数据
     *
     * @param usertoken
     * @param mac        补水仪设备地址
     * @param oilValue   油性值
     * @param moisValue  水分值
     * @param action     检测部位，取值范围如下：
     *                   脸部：FaceSkinValue,
     *                   手部：HandSkinValue,
     *                   眼部：EyesSkinValue,
     *                   颈部：NeckSkinValue
     * @param subscriber
     */
    public void updateBuShuiYiNumber(String usertoken, String mac, String oilValue, String moisValue, String action, Subscriber<JsonObject> subscriber) {
        oznerHttpService.updateBuShuiYiNumber(usertoken, mac, oilValue, moisValue, action)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .map(new ResultTransFunc1())
                .subscribe(subscriber);
    }


    /**
     * 获取补水仪检测历史检测数据
     *
     * @param usertoken
     * @param mac        补水仪mac地址
     * @param action     检测位置参数，取值范围如下：(为空时获取所有部位数据)
     *                   脸部：FaceSkinValue,
     *                   手部：HandSkinValue,
     *                   眼部：EyesSkinValue,
     *                   颈部：NeckSkinValue
     * @param subscriber
     */
    public void getBuShuiFenBu(String usertoken, String mac, String action, Subscriber<JsonObject> subscriber) {
        oznerHttpService.getBuShuiFenBu(usertoken, mac, action)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .map(new ResultTransFunc1())
                .subscribe(subscriber);
    }


    /**
     * 肤质查询页检测总次数
     *
     * @param usertoken
     * @param mac
     * @param subscriber
     */
    public void getTimesCountBuShui(String usertoken, String mac, Subscriber<JsonObject> subscriber) {
        oznerHttpService.getTimesCountBuShui(usertoken, mac)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .map(new ResultTransFunc1())
                .subscribe(subscriber);
    }


    /**
     * 更新滤芯服务时间
     *
     * @param usertoken
     * @param mac
     * @param deivcetype
     * @param code       二维码值
     * @param subscriber
     */
    public void reNewFilterTime(String usertoken, String mac, String deivcetype, String code, Subscriber<JsonObject> subscriber) {
        oznerHttpService.reNewFilterTime(usertoken, mac, deivcetype, code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .map(new ResultTransFunc1())
                .subscribe(subscriber);
    }

    /**
     * 邮箱登录
     *
     * @param email
     * @param pass
     * @param miei
     * @param devicename
     * @param subscriber
     */
    public void emailLogin(String email, String pass, String miei, String devicename, Subscriber<JsonObject> subscriber) {
        oznerHttpService.emailLogin(email, pass, miei, devicename)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 获取邮箱验证码
     *
     * @param email
     * @param subscriber
     */
    public void getEmailCode(String email, Subscriber<JsonObject> subscriber) {
        oznerHttpService.getEmailCode(email)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 重置密码
     *
     * @param email
     * @param password
     * @param code
     * @param subscriber
     */
    public void resetPassword(String email, String password, String code, Subscriber<JsonObject> subscriber) {
        oznerHttpService.resetPassword(email, password, code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    /**
     * 注册邮箱账号
     *
     * @param email
     * @param password
     * @param code
     * @param subscriber
     */
    public void signUpMail(String email, String password, String code, Subscriber<JsonObject> subscriber) {
        oznerHttpService.signUpMail(email, password, code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }
}
