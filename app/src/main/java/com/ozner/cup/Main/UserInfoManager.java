package com.ozner.cup.Main;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.UserInfo;
import com.ozner.cup.HttpHelper.ApiException;
import com.ozner.cup.HttpHelper.HttpMethods;
import com.ozner.cup.HttpHelper.ProgressSubscriber;

import java.lang.ref.WeakReference;

import rx.functions.Action1;

/**
 * Created by ozner_67 on 2016/12/7.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class UserInfoManager {
    private static final String TAG = "UserInfoManager";
    private WeakReference<Context> mContext;

    interface LoadUserInfoListener {
        void onSuccess(UserInfo userInfo);

        void onFail(String msg);
    }

    public UserInfoManager(Context context) {
        this.mContext = new WeakReference<Context>(context);
    }

    /**
     * 加载用户信息
     */
    public void loadUserInfo(final LoadUserInfoListener listener) {
        HttpMethods.getInstance().getUserInfo(OznerPreference.getUserToken(mContext.get())
                , new ProgressSubscriber<JsonObject>(mContext.get(), new Action1<JsonObject>() {
                    @Override
                    public void call(JsonObject jsonObject) {
                        try {
                            if (jsonObject != null) {
                                Log.e(TAG, "loadUserInfo: " + jsonObject.toString());
                                if (jsonObject.get("state").getAsInt() > 0) {
                                    //region  保存数据
                                    JsonObject data = jsonObject.get("userinfo").getAsJsonObject();
                                    UserInfo userInfo = new UserInfo();
                                    if (!data.get("UserId").isJsonNull())
                                        userInfo.setUserId(data.get("UserId").getAsString());
                                    if (!data.get("Nickname").isJsonNull())
                                        userInfo.setNickname(data.get("Nickname").getAsString());
                                    if (!data.get("Mobile").isJsonNull())
                                        userInfo.setMobile(data.get("Mobile").getAsString());
                                    if (!data.get("Icon").isJsonNull())
                                        userInfo.setHeadimg(data.get("Icon").getAsString());
                                    if (!data.get("device_id").isJsonNull())
                                        userInfo.setDeviceId(data.get("device_id").getAsString());
                                    if (!data.get("Sex").isJsonNull())
                                        userInfo.setSex(data.get("Sex").getAsString());
                                    if (!data.get("Score").isJsonNull())
                                        userInfo.setScore(data.get("Score").getAsString());
                                    if (userInfo != null) {
                                        DBManager.getInstance(mContext.get()).updateUserInfo(userInfo);
                                    }
                                    //endregion
                                    if (listener != null) {
                                        listener.onSuccess(userInfo);
                                    }
                                } else {
                                    if (listener != null) {
                                        listener.onFail(mContext.get().getString(ApiException.getErrResId(jsonObject.get("state").getAsInt())));
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            Log.e(TAG, "loadUserInfo_Ex: " + ex.getMessage());
                            if (listener != null) {
                                listener.onFail(ex.getMessage());
                            }
                        }
                    }
                }));
    }
}
