package com.ozner.cup.Chat.ChatHttpUtils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ozner.cup.Utils.SecurityUtils;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.client.config.RequestConfig;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.util.EntityUtils;

/**
 * Created by ozner_67 on 2016/12/15.
 * 邮箱：xinde.zhang@cftcn.com
 */
public class FuckChatHttpClient {
    private static final String TAG = "FuckChatHttpClient";
    private static final int DEFAULT_READ_TIMEOUT = 10000;//默认超时时间10s
    private static final int HANDLER_TOKEN_RESULT = 1;
    private static final int HANDLER_USERINFO_RESULT = 2;
    private static final int HANDLER_LOGIN_RESULT = 3;
    private String mMobile, mDeviceid, mToken, mCustomerId;
    private FuckChatHttpListener chatListener;

    public interface FuckChatHttpListener {
        void onLoginSuccess(String kfid, String kfname);

        void onFail(int code, String msg);
    }

    public interface SendMessageListener {
        void onSuccess(long messageTime);

        void onFail(long messageTime, int errCode, String errMsg);
    }

    public FuckChatHttpClient() {

    }


    public void setChatHttpListener(FuckChatHttpListener listener) {
        this.chatListener = listener;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_TOKEN_RESULT:
                    String tokenResult = (String) msg.obj;
                    Log.e(TAG, "HANDLER_TOKEN_RESULT: " + tokenResult);
                    try {
                        if (tokenResult != null) {
                            JSONObject jsonObject = new JSONObject(tokenResult);
                            int code = jsonObject.getInt("code");
                            if (0 == code) {
                                JSONObject result = jsonObject.getJSONObject("result");
                                if (result != null) {
                                    mToken = result.getString("access_token");
                                    getUserInfo(mToken, mMobile);
                                }
                            } else {
                                if (chatListener != null) {
                                    chatListener.onFail(code, "token:" + jsonObject.getString("msg"));
                                }
                            }
                        } else {
                            if (chatListener != null) {
                                chatListener.onFail(-1, "咨询:获取token失败");
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (chatListener != null) {
                            chatListener.onFail(-1, e.getMessage());
                        }
                        Log.e(TAG, "HANDLER_TOKEN_RESULT_Ex: " + e.getMessage());
                    }
                    break;
                case HANDLER_USERINFO_RESULT:
                    try {
                        String userInfoResult = (String) msg.obj;
                        if (userInfoResult != null) {
                            JSONObject userJson = new JSONObject(userInfoResult);
                            int code = userJson.getInt("code");
                            if (code == 0) {
                                JSONObject resJson = userJson.getJSONObject("result");
                                int count = resJson.getInt("count");
                                if (count > 0) {
                                    JSONObject userInfoJson = resJson.getJSONArray("list").optJSONObject(0);
                                    Log.e(TAG, "customerid: " + userInfoJson.getString("customer_id"));
                                    mCustomerId = userInfoJson.getString("customer_id");
                                    chatLogin(mToken, mCustomerId, mDeviceid);
                                } else {
                                    if (chatListener != null) {
                                        chatListener.onFail(-1, "咨询:用户信息为空");
                                    }
                                }
                            } else {
                                if (chatListener != null) {
                                    chatListener.onFail(code, ChatErrDecoder.getInstance().getErrMsg(code));
                                }
                            }
                        } else {
                            if (chatListener != null) {
                                chatListener.onFail(-1, "咨询:获取用户信息失败");
                            }
                        }
                        Log.e(TAG, "ChatUserInfo: " + userInfoResult);
                    } catch (Exception ex) {
                        if (chatListener != null) {
                            chatListener.onFail(-1, ex.getMessage());
                        }
                        Log.e(TAG, "HANDLER_USERINFO_RESULT_Ex: " + ex.getMessage());
                    }
                    break;
                case HANDLER_LOGIN_RESULT:
                    try {
                        String loginResult = (String) msg.obj;
                        if (loginResult != null) {
                            JSONObject loginJson = new JSONObject(loginResult);
                            int code = loginJson.getInt("code");
                            if (code == 0) {
                                JSONObject resJson = loginJson.getJSONObject("result");
                                if (chatListener != null) {
                                    chatListener.onLoginSuccess(resJson.getString("kfid"), resJson.getString("kfname"));
                                }
                            } else {
                                if (chatListener != null) {
                                    chatListener.onFail(code, ChatErrDecoder.getInstance().getErrMsg(code));
                                }
                            }
                        } else {
                            if (chatListener != null) {
                                chatListener.onFail(-1, "咨询:登录失败");
                            }
                        }
                        Log.e(TAG, "HANDLER_LOGIN_RESULT: " + loginResult);
//                        JSONObject loginJson = new JSONObject(loginResult);

                    } catch (Exception ex) {
                        Log.e(TAG, "HANDLER_LOGIN_RESULT_Ex: " + ex.getMessage());
                        if (chatListener != null) {
                            chatListener.onFail(-1, ex.getMessage());
                        }
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };


    public void getAccessToken(String mobile, String deviceid) {
        this.mMobile = mobile;
        this.mDeviceid = deviceid;

        String signStr = String.format("appid=%s&appsecret=%s", ChatHttpBean.ChatAppid, ChatHttpBean.ChatAppsecret);
        String sign = SecurityUtils.Md5(signStr);
        StringBuffer queryParms = new StringBuffer();
        queryParms.append(String.format("%s=%s", "appid", ChatHttpBean.ChatAppid))
                .append("&")
                .append(String.format("%s=%s", "appsecret", ChatHttpBean.ChatAppsecret))
                .append("&")
                .append(String.format("%s=%s", "sign", sign));

        final String queryUrl = String.format("%s/%s?%s", ChatHttpBean.ChatBaseUrl, ChatHttpBean.TokenActionUrl, queryParms.toString());
        new Thread(new Runnable() {
            @Override
            public void run() {
                String result = basePostString(queryUrl, "");
                mHandler.removeMessages(HANDLER_TOKEN_RESULT);
                Message message = mHandler.obtainMessage(HANDLER_TOKEN_RESULT);
                message.obj = result;
                mHandler.sendMessage(message);
            }
        }).start();
    }

    /**
     * 获取用户信息
     *
     * @param accesstoken
     * @param mobile
     */
    private void getUserInfo(String accesstoken, String mobile) {
        try {
            String queryParams = getSysQueryParams(accesstoken);
            final String queryUrl = String.format("%s/%s?%s", ChatHttpBean.ChatBaseUrl, ChatHttpBean.UserInfoActionUrl, queryParams);
            final JSONObject paramsJson = new JSONObject();
            paramsJson.put("mobile", mobile);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String result = basePostString(queryUrl, paramsJson.toString());
                    mHandler.removeMessages(HANDLER_USERINFO_RESULT);
                    Message message = mHandler.obtainMessage(HANDLER_USERINFO_RESULT);
                    message.obj = result;
                    mHandler.sendMessage(message);
                }
            }).start();
        } catch (Exception ex) {
            if (chatListener != null) {
                chatListener.onFail(-1, ex.getMessage());
            }
            Log.e(TAG, "getUserInfo_ex: " + ex.getMessage());
        }
    }

    /**
     * 咨询登录
     *
     * @param token
     * @param customerid
     * @param deviceId
     */
    private void chatLogin(String token, String customerid, String deviceId) {
        try {
            String queryParams = getSysQueryParams(token);
            final String queryUrl = String.format("%s/%s?%s", ChatHttpBean.ChatBaseUrl, ChatHttpBean.LoginActionUrl, queryParams);
            final JSONObject paramsJson = new JSONObject();
            paramsJson.put("customer_id", customerid);
            paramsJson.put("device_id", deviceId);
            paramsJson.put("channel_id", "5");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String result = basePostString(queryUrl, paramsJson.toString());
                    mHandler.removeMessages(HANDLER_LOGIN_RESULT);
                    Message message = mHandler.obtainMessage(HANDLER_LOGIN_RESULT);
                    message.obj = result;
                    mHandler.sendMessage(message);
                }
            }).start();

        } catch (Exception ex) {
            Log.e(TAG, "chatLogin_Ex: " + ex.getMessage());
            if (chatListener != null) {
                chatListener.onFail(-1, ex.getMessage());
            }
        }
    }


    /**
     * 发送信息
     *
     * @param msgContent
     * @param msgTime        信息唯一性标识
     * @param messageListner
     */
    public void chatSendMessage(String msgContent, final long msgTime, final SendMessageListener messageListner) {
        try {
            String queyParams = getSysQueryParams(mToken);
            final JSONObject paramsJson = new JSONObject();
            paramsJson.put("device_id", mDeviceid);
            paramsJson.put("customer_id", mCustomerId);
            paramsJson.put("msg", msgContent);
            paramsJson.put("channel_id", "5");

            final String queryUrl = String.format("%s/%s?%s", ChatHttpBean.ChatBaseUrl, ChatHttpBean.SendMsgActionUrl, queyParams);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String result = basePostString(queryUrl, paramsJson.toString());
//                    Log.e(TAG, "chatSendMessage_result: " + result);
                    try {
                        if (result != null) {
                            JSONObject resJson = new JSONObject(result);
                            int code = resJson.getInt("code");
                            if (0 == code) {
                                messageListner.onSuccess(msgTime);
                            } else {
                                messageListner.onFail(msgTime, code, resJson.getString("msg"));
                            }
                        } else {
                            if (messageListner != null) {
                                messageListner.onFail(msgTime, -1, "发送失败");
                            }
                        }
                    } catch (Exception ex) {
                        if (messageListner != null) {
                            messageListner.onFail(msgTime, -1, ex.getMessage());
                        }
                        Log.e(TAG, "run_ex: " + ex.getMessage());
                    }
                }
            }).start();
        } catch (Exception ex) {
            Log.e(TAG, "chatSendMessage_Ex: " + ex.getMessage());
        }
    }


    /**
     * 获取系统查询参数
     *
     * @param token
     *
     * @return
     */
    private String getSysQueryParams(String token) {
        String signStr = String.format("access_token=%s&appid=%s&appsecret=%s", token, ChatHttpBean.ChatAppid, ChatHttpBean.ChatAppsecret);
        String sign = SecurityUtils.Md5(signStr);
        StringBuffer queryParams = new StringBuffer();
        queryParams.append(String.format("%s=%s", "access_token", token));
        queryParams.append("&");
        queryParams.append(String.format("%s=%s", "sign", sign));
        return queryParams.toString();
    }

    /**
     * 咨询基础post请求，只在咨询中使用
     *
     * @param queryurl
     * @param parm
     *
     * @return
     */
    private String basePostString(String queryurl, String parm) {
        try {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(DEFAULT_READ_TIMEOUT)
                    .setSocketTimeout(DEFAULT_READ_TIMEOUT)
                    .build();
            StringEntity parmentity = new StringEntity(parm, HTTP.UTF_8);
            //声明HttpClient对象
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(queryurl);
            httpPost.setConfig(requestConfig);
            httpPost.setEntity(parmentity);
            CloseableHttpResponse response2 = httpclient.execute(httpPost);
            try {
                if (response2.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity2 = response2.getEntity();
                    String strResult = EntityUtils.toString(entity2);
                    return strResult;
                }
            } finally {
                response2.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return null;
    }


//    /**
//     * @param actionUrl
//     * @param paramsMap
//     *
//     * @return
//     */
//    private String httpGet(String actionUrl, HashMap<String, String> paramsMap) {
//        try {
//            StringBuffer tempParams = new StringBuffer();
//            int pos = 0;
//            for (String key : paramsMap.keySet()) {
//                if (pos > 0) {
//                    tempParams.append("&");
//                }
//                tempParams.append(String.format("%s=%s", key, URLEncoder.encode(paramsMap.get(key), "utf-8")));
//                pos++;
//            }
//            String queryUrl = String.format("%s/%s?%s", ChatHttpBean.ChatBaseUrl, actionUrl, tempParams.toString());
//            Log.e(TAG, "httpGet: queryUrl:" + queryUrl);
//
//            RequestConfig requestConfig = RequestConfig.custom()
//                    .setSocketTimeout(DEFAULT_READ_TIMEOUT)
//                    .setConnectTimeout(DEFAULT_READ_TIMEOUT)
//                    .build();//设置请求和传输超时时间
//
//            //声明HttpClient对象
//            CloseableHttpClient httpclient = HttpClients.createDefault();
//            HttpGet httpGet = new HttpGet(queryUrl);
//            httpGet.setConfig(requestConfig);
//            CloseableHttpResponse response2 = httpclient.execute(httpGet);
//            try {
//                if (response2.getStatusLine().getStatusCode() == 200) {
//                    HttpEntity entity2 = response2.getEntity();
//                    String strResult = EntityUtils.toString(entity2);
//                    return strResult;
//                }
//
//            } finally {
//                response2.close();
//            }
//        } catch (Exception ex) {
//            Log.e(TAG, "httpGet_ex: " + ex.getMessage());
//        }
//        return null;
//    }


//    @Deprecated
//    private String httpPost(String url, String parm, RequestConfig requestConfig) {
//        try {
//            StringEntity parmentity = new StringEntity(parm, HTTP.UTF_8);
//            //声明HttpClient对象
//            CloseableHttpClient httpclient = HttpClients.createDefault();
//            HttpPost httpPost = new HttpPost(url);
//            httpPost.setConfig(requestConfig);
//            httpPost.setEntity(parmentity);
//            CloseableHttpResponse response2 = httpclient.execute(httpPost);
//            try {
//                if (response2.getStatusLine().getStatusCode() == 200) {
//                    HttpEntity entity2 = response2.getEntity();
//                    String strResult = EntityUtils.toString(entity2);
//                    return strResult;
//                }
//            } finally {
//                response2.close();
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return null;
//        }
//        return null;
//    }

}
