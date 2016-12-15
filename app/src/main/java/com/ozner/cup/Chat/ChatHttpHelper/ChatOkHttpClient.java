package com.ozner.cup.Chat.ChatHttpHelper;

import android.os.Build;
import android.util.Log;

import com.ozner.cup.Utils.SecurityUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by ozner_67 on 2016/12/14.
 * 邮箱：xinde.zhang@cftcn.com
 */
public class ChatOkHttpClient {
    private static final String TAG = "ChatOkHttpClient";
    private static final int DEFAULT_READ_TIMEOUT = 10;//默认超时时间15s
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


    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static OkHttpClient okhttpLCient;

    public interface GetResultListener {
        void onSuccess(String result);

        void onFail(String errMsg);
    }

    private static ChatOkHttpClient ourInstance = new ChatOkHttpClient();

    public static ChatOkHttpClient getInstance() {
        return ourInstance;
    }

    private ChatOkHttpClient() {
        okhttpLCient = new OkHttpClient.Builder()
//                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
//                .writeTimeout(DEFAULT_READ_TIMEOUT,TimeUnit.SECONDS)
                .connectTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
                .build();

    }

    /**
     * 获取token
     *
     * @param callback
     *
     * @return
     */
    public Call getAccessToken(Callback callback) {
        String signStr = String.format("appid=%s&appsecret=%s", ChatAppid, ChatAppsecret);
        String sign = SecurityUtils.Md5(signStr);
//        HashMap<String, String> sysParamsMap = new HashMap<>();
//        sysParamsMap.put("appid", ChatAppid);
//        sysParamsMap.put("appsecret", ChatAppsecret);
//        sysParamsMap.put("sign", sign);

        StringBuffer sysParams = new StringBuffer();
        sysParams.append(String.format("%s=%s", "appid", ChatAppid));
        sysParams.append("&");
        sysParams.append(String.format("%s=%s", "appsecret", ChatAppsecret));
        sysParams.append("&");
        sysParams.append(String.format("%s=%s", "sign", sign));
        try {
            String requestUrl = String.format("%s/%s?%s", ChatBaseUrl, TokenActionUrl, sysParams.toString());
            Log.e(TAG, "baseGetAsyn: requestUrl:" + requestUrl);
            Request request = new Request.Builder().url(requestUrl).build();
            Call call = okhttpLCient.newCall(request);
            call.enqueue(callback);
            return call;
        } catch (Exception ex) {
            Log.e(TAG, "baseGet_Ex: " + ex.getMessage());
        }
        return null;

//        return baseGetAsyn(TokenActionUrl, sysParamsMap, null, callback);
    }

    /**
     * 获取用户信息
     *
     * @param accesstoken
     * @param mobile
     * @param callback
     *
     * @return
     */
    public Call getUserInfo(String accesstoken, String mobile, Callback callback) {


//        String queryUrl = String.format("%s?access_token=%s&sign=%s&mobile=", UserInfoActionUrl, accesstoken, sign,mobile);
        HashMap<String, String> sysParamsMap = getSysParamsMap(accesstoken);
        HashMap<String, String> cusParamsMap = new HashMap<>();
        cusParamsMap.put("mobile", mobile);

        return baseGetAsyn(UserInfoActionUrl, sysParamsMap, cusParamsMap, callback);
    }


    /**
     * 咨询登录
     *
     * @param accesstoken
     * @param customid
     * @param deviceid
     * @param callback
     *
     * @return
     */
    public Call chatLogin(String accesstoken, String customid, String deviceid, Callback callback) {
        String signStr = String.format("access_token=%s&appid=%s&appsecret=%s", accesstoken, ChatAppid, ChatAppsecret);
        String sign = SecurityUtils.Md5(signStr);
        String queryUrl = String.format("%s?access_token=%s&sign=%s", LoginActionUrl, accesstoken, sign);
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("customer_id", customid);
        paramsMap.put("device_id", deviceid);
        paramsMap.put("ct_id", "1");
        paramsMap.put("channel_id", "5");
        return basePostFormAsyn(queryUrl, paramsMap, callback);
    }

    /**
     * 咨询发送信息
     *
     * @param customerid
     * @param deviceid
     * @param msg
     * @param accesstoken
     * @param callback
     *
     * @return
     */
    public Call chatSendMsg(String customerid, String deviceid, String msg, String accesstoken, Callback callback) {
        String signStr = String.format("access_token=%s&appid=%s&appsecret=%s", accesstoken, ChatAppid, ChatAppsecret);
        String sign = SecurityUtils.Md5(signStr);
        String queryUrl = String.format("%s?access_token=%s&sign=%s", SendMsgActionUrl, accesstoken, sign);
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("customer_id", customerid);
        paramsMap.put("device_id", deviceid);
        paramsMap.put("msg", msg);
        paramsMap.put("channel_id", "5");
        return basePostFormAsyn(queryUrl, paramsMap, callback);
    }

    /**
     * 获取用户全部历史信息
     *
     * @param accesstoken
     * @param customid
     * @param callback
     *
     * @return
     */
    public Call getHistoryRecord(String accesstoken, String customid, Callback callback) {
        String signStr = String.format("access_token=%s&appid=%s&appsecret=%s", accesstoken, ChatAppid, ChatAppsecret);
        String sign = SecurityUtils.Md5(signStr);
        String queryUrl = String.format("%s?access_token=%s&sign=%s", HistoryActionUrl, accesstoken, sign);
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("customer_id", customid);
        return basePostFormAsyn(queryUrl, paramsMap, callback);
    }

    private void baseUrlPost(String actionUrl, HashMap<String, String> paramsMap) {
        try {
            StringBuffer tempParams = new StringBuffer();
            int pos = 0;
            for (String key : paramsMap.keySet()) {
                if (pos > 0) {
                    tempParams.append("&");
                }
                tempParams.append(String.format("%s=%s", key, URLEncoder.encode(paramsMap.get(key), "utf-8")));
                pos++;
            }
            URL url = new URL(String.format("%s/%s", ChatBaseUrl, actionUrl));
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            httpConn.setRequestMethod("GET");
            //设置请求属性
//            httpConn.setRequestProperty("Content-Type", "application/json");
//            httpConn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
//            httpConn.setRequestProperty("Charset", "UTF-8");
            httpConn.connect();
            //建立输入流，向指向的URL传入参数
            DataOutputStream dos = new DataOutputStream(httpConn.getOutputStream());
            dos.writeBytes(tempParams.toString());
            dos.flush();
            dos.close();
            //获得响应状态
            int resultCode = httpConn.getResponseCode();
            if (HttpURLConnection.HTTP_OK == resultCode) {
                StringBuffer sb = new StringBuffer();
                String readLine = new String();
                BufferedReader responseReader = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
                while ((readLine = responseReader.readLine()) != null) {
                    sb.append(readLine).append("\n");
                }
                responseReader.close();
                Log.e(TAG, "baseUrlPost: " + sb.toString());
            }

        } catch (Exception ex) {
            Log.e(TAG, "baseUrlPost_Ex: " + ex.getMessage());
        }
    }


    /**
     * 异步post请求
     *
     * @param actionUrl
     * @param paramsMap
     * @param callback
     *
     * @return
     */
    private Call basePostFormAsyn(String actionUrl, HashMap<String, String> paramsMap, Callback callback) {
        try {
            FormBody.Builder builder = new FormBody.Builder();
            for (String key : paramsMap.keySet()) {
                builder.add(key, paramsMap.get(key));
            }
            RequestBody formBody = builder.build();
            String requestUrl = String.format("%s/%s", ChatBaseUrl, actionUrl);
            Request request = new Request.Builder().url(requestUrl).put(formBody).build();
            Call call = okhttpLCient.newCall(request);
            call.enqueue(callback);

            return call;
        } catch (Exception ex) {
            Log.e(TAG, "basePostAsyn_Ex: " + ex.getMessage());
        }
        return null;
    }

    public void getWeatherTest() {
        Request request = new Request.Builder().url("http://app.ozner.net:888/OznerServer/GetWeather").build();
        Call call = okhttpLCient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "getWeatherTest_onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e(TAG, "getWeatherTest_onResponse: " + response.body().string());
            }
        });
    }

    /**
     * 获取系统级参数map
     *
     * @param token
     *
     * @return
     */
    private HashMap<String, String> getSysParamsMap(String token) {
        String signStr = String.format("access_token=%s&appid=%s&appsecret=%s", token, ChatAppid, ChatAppsecret);
        String sign = SecurityUtils.Md5(signStr);
        HashMap<String, String> sysParamsMap = new HashMap<>();
        sysParamsMap.put("access_token", token);
        sysParamsMap.put("sign", sign);
        return sysParamsMap;
    }


    /**
     * 基础get请求
     *
     * @param actionUrl
     * @param sysParamsMap
     * @param cusParamsMap
     * @param callback
     *
     * @return
     */
    private Call baseGetAsyn(String actionUrl, HashMap<String, String> sysParamsMap, HashMap<String, String> cusParamsMap, Callback callback) {
        StringBuffer sysParams = new StringBuffer();
        try {
            int pos = 0;
            for (String key : sysParamsMap.keySet()) {
                if (pos > 0) {
                    sysParams.append("&");
                }
                sysParams.append(String.format("%s=%s", key, URLEncoder.encode(sysParamsMap.get(key), "utf-8")));
                pos++;
            }
            JSONObject jsonObjedt = new JSONObject();
            if (cusParamsMap != null) {
                for (String key : cusParamsMap.keySet()) {
                    jsonObjedt.put(key, URLEncoder.encode(cusParamsMap.get(key), "utf-8"));
                }
            }
            String requestUrl = String.format("%s/%s?%s:", ChatBaseUrl, actionUrl, sysParams.toString(), jsonObjedt.toString());
            Log.e(TAG, "baseGetAsyn: requestUrl:" + requestUrl);
            Request request = new Request.Builder().url(requestUrl).build();
            Call call = okhttpLCient.newCall(request);
            call.enqueue(callback);
            return call;
        } catch (Exception ex) {
            Log.e(TAG, "baseGet_Ex: " + ex.getMessage());
        }
        return null;
    }


    /**
     * 统一为请求添加头信息
     *
     * @return
     */
    private Request.Builder addHeaders() {
        Request.Builder builder = new Request.Builder()
                .addHeader("Connection", "keep-alive")
                .addHeader("platform", "2")
                .addHeader("phoneModel", Build.MODEL)
                .addHeader("Content-Type", "application/json")
                .addHeader("systemVersion", Build.VERSION.RELEASE)
                .addHeader("appVersion", "3.2.0");
        return builder;
    }
}
