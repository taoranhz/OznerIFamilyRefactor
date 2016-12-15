package com.ozner.cup.Chat.ChatHttpHelper;

import android.util.Log;

import com.google.gson.JsonObject;
import com.ozner.cup.Utils.SecurityUtils;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by ozner_67 on 2016/12/14.
 * 邮箱：xinde.zhang@cftcn.com
 */
public class ChatHttpMethods {
    private static final String TAG = "ChatHttpMethods";
    public static final String ChatAppid = "hzapi";
    public static final String ChatAppsecret = "8af0134asdffe12";
    private static final String ChatBaseUrl = "http://dkf.ozner.net";
    private static HashMap<Integer, String> chatErrMap;

    private static final int DEFAULT_TIMEOUT = 10;//默认超时时间15s
    private Retrofit retrofit;
    private ChatService chatService;

    /**
     * 初始化咨询错误信息
     */
    private void initErrMap() {
        if (null == chatErrMap) {
            chatErrMap = new HashMap<>();
        } else {
            chatErrMap.clear();
        }
        chatErrMap.put(0, "正确");
        chatErrMap.put(1001, "账户信息有误");
        chatErrMap.put(1002, "access_token无效");
        chatErrMap.put(1003, "签名无效");
        chatErrMap.put(1004, "参数错误");
        chatErrMap.put(1005, "access_token过期");
        chatErrMap.put(1006, "操作错误");
        chatErrMap.put(1007, "无返回信息");
    }

    private static ChatHttpMethods ourInstance = new ChatHttpMethods();

    public static ChatHttpMethods getInstance() {
        return ourInstance;
    }

    private ChatHttpMethods() {
        initErrMap();
        //创建OkHttpClient并设置超时时间
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

        retrofit = new Retrofit.Builder()
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(ChatBaseUrl)
                .build();
        chatService = retrofit.create(ChatService.class);
    }

//    public void getAccesstoken(String)

    public interface ChatResultListener {
        void onSuccess();

        void onFail(int state, String msg);
    }

    /**
     * 初始化聊天环境，用户登录
     * 1、获取token
     * 2、获取用户信息
     * 3、登录
     */
    public void initChat(final String mobile, final ChatResultListener resultListener) {
        String signStr = String.format("appid=%s&appsecret=%s", ChatAppid, ChatAppsecret);
        String sign = SecurityUtils.Md5(signStr);
        chatService.getAccesstoken(ChatAppid, ChatAppsecret, sign)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<JsonObject, Observable<String>>() {
                    @Override
                    public Observable<String> call(JsonObject jsonObject) {
                        // TODO: 2016/12/14 处理access_token请求结果，并分发

                        if (jsonObject != null) {
                            Log.e(TAG, "getAccesstoken: " + jsonObject.toString());

                            int errCode = jsonObject.get("code").getAsInt();
                            String errMsg = jsonObject.get("msg").getAsString();

                            if (0 == errCode) {
                                if (!jsonObject.get("result").isJsonNull())
                                    return Observable.just(jsonObject.get("result").getAsJsonObject().get("access_token").getAsString());
                                else
                                    resultListener.onFail(-1, "结果为空");
                            } else {
                                resultListener.onFail(errCode, errMsg);
                            }
                        } else {
                            resultListener.onFail(-1, "获取token失败");
                        }
                        return null;
                    }
                })
                .flatMap(new Func1<String, Observable<JsonObject>>() {
                    @Override
                    public Observable<JsonObject> call(String accesstoken) {
                        // TODO: 2016/12/14 处理获取用户信息

                        String signStr = String.format("access_token=%s&appid=%s&appsecret=%s", accesstoken, ChatAppid, ChatAppsecret);
                        Log.e(TAG, "signStr: " + signStr + " , sign:" + SecurityUtils.Md5(signStr));

                        return chatService.getChatUserInfo(mobile, accesstoken, SecurityUtils.Md5(signStr));
                    }
                })
//                .flatMap(new Func1<JsonObject, Observable<JsonObject>>() {
//                    @Override
//                    public Observable<JsonObject> call(JsonObject jsonObject) {
//                        // TODO: 2016/12/14 处理用户登录
//                        return null;
//                    }
//                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JsonObject>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (resultListener != null)
                            resultListener.onFail(-1, e.getMessage());
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        Log.e(TAG, "getChatUserInfo_onNext: " + jsonObject.toString());
//                        if (jsonObject != null) {
//                            if (jsonObject.get("code").getAsInt() == 0) {
//                                if (resultListener != null) {
//                                    resultListener.onSuccess();
//                                }
//                            } else {
//                                if (resultListener != null) {
//                                    resultListener.onFail(jsonObject.get("code").getAsInt(), jsonObject.get("msg").getAsString());
//                                }
//                            }
//                        } else
//                            Log.e(TAG, "result: null ");
                    }
                });
    }
}
