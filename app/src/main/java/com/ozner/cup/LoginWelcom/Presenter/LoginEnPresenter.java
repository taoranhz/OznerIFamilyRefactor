package com.ozner.cup.LoginWelcom.Presenter;

import android.content.Context;

import com.google.gson.JsonObject;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.UserInfo;
import com.ozner.cup.HttpHelper.ApiException;
import com.ozner.cup.HttpHelper.HttpMethods;
import com.ozner.cup.HttpHelper.OznerHttpResult;
import com.ozner.cup.HttpHelper.ProgressSubscriber;
import com.ozner.cup.LoginWelcom.View.ILoginEnView;
import com.ozner.cup.R;
import com.ozner.cup.Utils.LCLogUtils;
import com.ozner.cup.Utils.MobileInfoUtil;
import com.ozner.device.OznerDeviceManager;

import java.lang.ref.WeakReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ozner_67 on 2017/3/14.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class LoginEnPresenter {
    private static final String TAG = "LoginEnPresenter";
    private ILoginEnView loginView;
    private WeakReference<Context> mContext;

    public LoginEnPresenter(Context context, ILoginEnView loginView) {
        mContext = new WeakReference<Context>(context);
        this.loginView = loginView;
    }

    public void login() {
        if (null == loginView)
            return;
        if (loginView.getEmail().length() > 0) {
            if (loginView.getPassword().length() > 0) {
                if (isEmail(loginView.getEmail())) {
                    loginHttp(loginView.getEmail(), loginView.getPassword());
                } else {
                    loginView.showErrMsg(R.string.valid_email);
                }
            } else {
                loginView.showErrMsg(R.string.input_password);
//                loginView.showToastMsg(mContext.get().getString(R.string.input_password));
            }
        } else {
            loginView.showErrMsg(R.string.input_email);
//            loginView.showToastMsg(mContext.get().getString(R.string.input_email));
        }
    }

    /**
     * 登录请求
     *
     * @param email
     * @param pass
     */
    private void loginHttp(String email, String pass) {
        loginView.showErrMsg("");
        if (!isNetAvailable()) {
            LCLogUtils.E(TAG, "login: 网络中断");
            loginView.showErrMsg(R.string.err_net_outline);
            return;
        }

        HttpMethods.getInstance().emailLogin(email, pass
                , MobileInfoUtil.getImie(mContext.get())
                , android.os.Build.MANUFACTURER
                , new ProgressSubscriber<JsonObject>(mContext.get(), mContext.get().getString(R.string.logining), true, new OznerHttpResult<JsonObject>() {
                    @Override
                    public void onError(Throwable e) {
                        LCLogUtils.E(TAG, "login_onError: " + e.getMessage());
                        if (e.getMessage().isEmpty()) {
                            loginView.showErrMsg(R.string.Code_Login_Error);
                        } else {
                            loginView.showErrMsg(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        LCLogUtils.E(TAG, "login_result:" + jsonObject.toString());
                        if (jsonObject.get("state").getAsInt() > 0) {
                            String usertoken = jsonObject.get("usertoken").getAsString();
                            String userid = jsonObject.get("userid").getAsString();
                            OznerPreference.setUserToken(mContext.get(), usertoken);
                            OznerPreference.setIsLogin(mContext.get(), true);
                            OznerPreference.setLoginEmail(mContext.get(), true);
                            UserDataPreference.SetUserData(mContext.get(), UserDataPreference.UserId, userid);
                            OznerDeviceManager.Instance().setOwner(userid, usertoken);
                            try {
                                UserInfo userInfo = DBManager.getInstance(mContext.get()).getUserInfo(jsonObject.get("userid").getAsString());
                                if (userInfo == null) {
                                    userInfo = new UserInfo();
                                    userInfo.setUserId(jsonObject.get("userid").getAsString());
                                }
                                userInfo.setEmail(loginView.getEmail());
                                DBManager.getInstance(mContext.get()).updateUserInfo(userInfo);
                            } catch (Exception ex) {
                                LCLogUtils.E(TAG, "login_En_Ex:" + ex.getMessage());
                            }
                            loginView.loginSuccess();
                        } else {
                            String title = mContext.get().getString(ApiException.getErrResId(jsonObject.get("state").getAsInt()));
                            String titleErrMsg = title != null ? title : "";
                            String subErrmsg = !jsonObject.get("msg").isJsonNull() ? ":" + jsonObject.get("msg").getAsString() : "";
                            loginView.showToastMsg(titleErrMsg + subErrmsg);
                            loginView.showErrMsg(ApiException.getErrResId(jsonObject.get("state").getAsInt()));
                        }
                    }
                }));
    }

    /**
     * 判断邮箱是否合法
     *
     * @param email
     *
     * @return
     */
    public static boolean isEmail(String email) {
        if (null == email || "".equals(email)) return false;
        //Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}"); //简单匹配
        Pattern p = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");//复杂匹配
        Matcher m = p.matcher(email);
        return m.matches();
    }

    private boolean isNetAvailable() {
        if (!MobileInfoUtil.isNetworkAvailable(mContext.get())) {
            return false;
        }
        return true;
    }
}
