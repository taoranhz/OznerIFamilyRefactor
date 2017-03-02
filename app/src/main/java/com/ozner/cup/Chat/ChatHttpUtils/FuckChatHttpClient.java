package com.ozner.cup.Chat.ChatHttpUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ozner.cup.Bean.OznerBroadcastAction;
import com.ozner.cup.Chat.EaseUI.model.MessageDirect;
import com.ozner.cup.Chat.EaseUI.utils.MessageCreator;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.EMMessage;
import com.ozner.cup.Utils.LCLogUtils;
import com.ozner.cup.Utils.SecurityUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.client.config.RequestConfig;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.util.EntityUtils;

/**
 * Created by ozner_67 on 2016/12/15.
 * 邮箱：xinde.zhang@cftcn.com
 */
public class FuckChatHttpClient {
    public static final int DEFAULT_PAGESIZE = 30;//默认历史记录pagesize
    public static final String GET_COUNT = "getcount";
    private static final String TAG = "FuckChatHttpClient";
    private static final int DEFAULT_READ_TIMEOUT = 10000;//默认超时时间10s
    private static final int HANDLER_TOKEN_RESULT = 1;
    private static final int HANDLER_USERINFO_RESULT = 2;
    private static final int HANDLER_LOGIN_RESULT = 3;
    private String mMobile, mDeviceid, mToken, mCustomerId, mKfId;
    private FuckChatHttpListener chatListener;

    public interface FuckChatHttpListener {
        void onLoginSuccess(String kfid, String kfname);

        void onFail(int code, String msg);
    }

    public interface SendMessageListener {
        void onSuccess(long messageTime);

        void onFail(long messageTime, int errCode, String errMsg);
    }

    public interface UploadImageListener {
        void onSuccess(long messageTime, String imgUrl);

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
                                mKfId = resJson.getString("kfid");
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


    /**
     * 初始化登录
     *
     * @param mobile
     * @param deviceid
     */
    public void initChat(String mobile, String deviceid) {
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
     * 咨询获取历史记录
     *
     * @param context
     * @param userid
     * @param page    页数
     */
    public void chatGetHistoryMsg(final Context context, final String userid, final int page) {
        try {
            String queyParams = getSysQueryParams(mToken);
            final JSONObject paramsJson = new JSONObject();
            paramsJson.put("customer_id", mCustomerId);
            paramsJson.put("pagesize", String.valueOf(DEFAULT_PAGESIZE));
            paramsJson.put("page", String.valueOf(page));
            final String queryUrl = String.format("%s/%s?%s", ChatHttpBean.ChatBaseUrl, ChatHttpBean.HistoryActionUrl, queyParams);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String result = basePostString(queryUrl, paramsJson.toString());
                    LCLogUtils.E(TAG, "chatGetHistoryMsg_result: " + result);
                    try {
                        if (result != null && result != "") {
                            JSONObject resJson = new JSONObject(result);
                            int code = resJson.getInt("code");
                            if (0 == code) {
                                JSONObject resJo = resJson.getJSONObject("result");
                                int totalCount = resJo.getInt("count");
                                int historyCount = Integer.parseInt(UserDataPreference.GetUserData(context, UserDataPreference.ChatHistoryCount, "-1"));
                                //当本地历史记录总数为-1时，表示未获取过历史消息，
                                // 如果大于等于0表示已经不是第一次获取历史消息，就不需要再保存历史消息总数，
                                // 否则会造成信息重复
                                if (historyCount < 0) {
                                    UserDataPreference.SetUserData(context, UserDataPreference.ChatHistoryCount, String.valueOf(totalCount));
                                }
                                int curMsgCount = DBManager.getInstance(context).getAllChatMessage(userid).size();
                                if (totalCount > 0) {
                                    //保存本次获取历史记录的页码，为下次获取历史记录准备
                                    UserDataPreference.SetUserData(context, UserDataPreference.ChatCurPage, String.valueOf(page));
                                    JSONArray jsonArray = resJo.getJSONArray("list");
                                    int getCount = jsonArray.length();
                                    for (int i = 0; i < getCount; i++) {
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        //修正为毫秒级时间戳
                                        long addtime = jsonObject.getLong("_add_timestamp") * 1000;
                                        String message = jsonObject.getString("_message");
                                        int oper = jsonObject.getInt("_oper");
                                        int direct = 1;
                                        if (oper == 1) {
                                            direct = MessageDirect.SEND;
                                        } else if (oper == 2) {
                                            direct = MessageDirect.RECEIVE;
                                        }
                                        EMMessage chatMsg = MessageCreator.transMsgNetToLocal(userid, message, direct, addtime);
                                        DBManager.getInstance(context).updateEMMessage(chatMsg);
                                    }
                                    if (curMsgCount > 0) {
                                        Intent hisIntent = new Intent(OznerBroadcastAction.OBA_OBTAIN_CHAT_HISTORY);
                                        hisIntent.putExtra(GET_COUNT, getCount);
                                        context.sendBroadcast(hisIntent);
                                    } else {
                                        context.sendBroadcast(new Intent(OznerBroadcastAction.OBA_RECEIVE_CHAT_MSG));
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "chatGetHistoryMsg_run_ex: " + ex.getMessage());
                    }
                }
            }).start();
        } catch (Exception ex) {
            LCLogUtils.E(TAG, "chatGetHistoryMsg_Ex:" + ex.getMessage());
        }
    }

    /**
     * 咨询上传图片
     *
     * @param msgTime
     * @param imgPath
     * @param imageListener
     */
    public void chatUploadImage(final long msgTime, final String imgPath, final UploadImageListener imageListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String queyParams = getSysQueryParams(mToken);
                    Log.e(TAG, "chatUploadImage:queyParams: " + queyParams);
                    String queryurl = String.format("%s/%s?%s", ChatHttpBean.ChatBaseUrl, ChatHttpBean.UploadImgActionUrl, queyParams);
                    Log.e(TAG, "chatUploadImage: queryurl:" + queryurl);
                    RequestConfig requestConfig = RequestConfig.custom()
                            .setConnectionRequestTimeout(DEFAULT_READ_TIMEOUT)
                            .setSocketTimeout(DEFAULT_READ_TIMEOUT)
                            .build();
                    //声明HttpClient对象
                    CloseableHttpClient httpclient = HttpClients.createDefault();
                    HttpPost httpPost = new HttpPost(queryurl);
                    httpPost.setConfig(requestConfig);
                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    builder.addBinaryBody("photo", new File(imgPath));
                    httpPost.setEntity(builder.build());

                    CloseableHttpResponse response2 = httpclient.execute(httpPost);
                    try {
                        if (response2.getStatusLine().getStatusCode() == 200) {
                            HttpEntity entity2 = response2.getEntity();
                            String strResult = EntityUtils.toString(entity2);
                            Log.e(TAG, "chatUploadImage: strResult:" + strResult);
                            try {
                                if (strResult != null) {
                                    JSONObject resJson = new JSONObject(strResult);
                                    int code = resJson.getInt("code");
                                    if (0 == code) {
                                        JSONObject resultJson = resJson.getJSONObject("result");
                                        String imgUrl = "";
                                        if (resultJson != null) {
                                            imgUrl = resultJson.getString("picpath");
                                        }
                                        if (imageListener != null)
                                            imageListener.onSuccess(msgTime, imgUrl);
                                    } else {
                                        if (imageListener != null) {
                                            imageListener.onFail(msgTime, code, resJson.getString("msg"));
                                        }
                                    }
                                } else {
                                    if (imageListener != null) {
                                        imageListener.onFail(msgTime, -1, "发送失败");
                                    }
                                }
                            } catch (Exception ex) {
                                if (imageListener != null) {
                                    imageListener.onFail(msgTime, -1, ex.getMessage());
                                }
                                Log.e(TAG, "run_ex: " + ex.getMessage());
                            }
                        }
                    } catch (Exception ex) {
                    } finally {
                        response2.close();
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
//            return null;
                }
            }
        }).start();
//        return null;
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
}
