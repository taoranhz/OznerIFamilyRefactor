package com.ozner.cup.Chat.ChatHttpHelper;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by ozner_67 on 2016/12/14.
 * 邮箱：xinde.zhang@cftcn.com
 * <p>
 * 咨询网络请求管理
 */

public class ChatOkManager {
    private static final String TAG = "ChatOkManager";
    private static final int HANDLER_TOKEN_RESULT = 1;
    private static final int HANDLER_USERINFO_RESULT = 2;
    private static final int HANDLER_LOGIN_RESULT = 3;
    private List<Call> httpCalls = new ArrayList<>();
    private ChatHttpListener chatListener;
    private WeakReference<Context> mContext;
    private String mMobile, mDeviceid;

    public interface ChatHttpListener {
        void onInitChatSuccess();

        void onUserInfoSuccess();

        void onFail(int code, String msg);
    }

    public ChatOkManager(Context context, ChatHttpListener listener) {
        this.mContext = new WeakReference<Context>(context);
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
                        JSONObject jsonObject = new JSONObject(tokenResult);
                        int code = jsonObject.getInt("code");
                        if (0 == code) {
                            if (chatListener != null) {
                                chatListener.onInitChatSuccess();
                            }
                            JSONObject result = jsonObject.getJSONObject("result");
                            if (result != null) {
                                String token = result.getString("access_token");
                                getUserInfo(token);
//                                ChatOkHttpClient.getInstance().getWeatherTest();
                            }
                        } else {
                            if (chatListener != null) {
                                chatListener.onFail(code, "token:" + jsonObject.getString("msg"));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "HANDLER_TOKEN_RESULT_Ex: " + e.getMessage());
                    }
                    break;
                case HANDLER_USERINFO_RESULT:
                    String result = (String) msg.obj;
                    Log.e(TAG, "ChatUserInfo: " + result);
                    break;
                case HANDLER_LOGIN_RESULT:
                    break;
            }
            super.handleMessage(msg);
        }
    };


    /**
     * 初始化咨询，获取token，获取用户信息，登录
     * @param mobile
     * @param devceid
     */
    public void initChat(String mobile, String devceid) {
        this.mMobile = mobile;
        this.mDeviceid = devceid;

        Call call = ChatOkHttpClient.getInstance().getAccessToken(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                httpCalls.remove(call);
                Log.d(TAG, "initChat_onFailure_Ex: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                mHandler.removeMessages(HANDLER_TOKEN_RESULT);
                Message message = mHandler.obtainMessage(HANDLER_TOKEN_RESULT);
                message.obj = response.body().string();
                mHandler.sendMessage(message);
                httpCalls.remove(call);

            }
        });
        if (call != null)
            httpCalls.add(call);
    }

    /**
     * 获取用户信息
     *
     * @param accesstoken
     */
    private void getUserInfo(String accesstoken) {
        Call call = ChatOkHttpClient.getInstance().getUserInfo(accesstoken, mMobile, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                httpCalls.remove(call);
                Log.e(TAG, "getUserInfo_onFailure_Ex: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e(TAG, "getUserInfo_onResponse: code:"+response.code());
                Log.e(TAG, "getUserInfo_onResponse: isSuccessful:"+response.isSuccessful());
                mHandler.removeMessages(HANDLER_USERINFO_RESULT);
                Message message = mHandler.obtainMessage(HANDLER_USERINFO_RESULT);
                message.obj = response.body().string();
                mHandler.sendMessage(message);
                httpCalls.remove(call);
            }
        });

        if (call != null)
            httpCalls.add(call);

    }


}
