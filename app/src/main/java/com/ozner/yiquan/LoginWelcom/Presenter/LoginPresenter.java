package com.ozner.yiquan.LoginWelcom.Presenter;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ozner.yiquan.Command.OznerPreference;
import com.ozner.yiquan.Command.UserDataPreference;
import com.ozner.yiquan.DBHelper.DBManager;
import com.ozner.yiquan.DBHelper.UserInfo;
import com.ozner.yiquan.HttpHelper.ApiException;
import com.ozner.yiquan.HttpHelper.HttpMethods;
import com.ozner.yiquan.HttpHelper.OznerHttpResult;
import com.ozner.yiquan.HttpHelper.ProgressSubscriber;
import com.ozner.yiquan.LoginWelcom.Model.ILoginModel;
import com.ozner.yiquan.LoginWelcom.Model.LoginModel;
import com.ozner.yiquan.LoginWelcom.View.ILoginView;
import com.ozner.yiquan.R;
import com.ozner.yiquan.Utils.LCLogUtils;
import com.ozner.yiquan.Utils.MobileInfoUtil;

import java.lang.ref.WeakReference;

/**
 * Created by ozner_67 on 2016/11/2.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class LoginPresenter {
    private static final String TAG = "LoginPresenter";
    private ILoginView loginView;
    private ILoginModel loginModel;
    private WeakReference<Context> loginContext;

    public LoginPresenter(Context context, ILoginView loginView) {
        this.loginView = loginView;
        this.loginModel = new LoginModel(context);
        loginContext = new WeakReference<Context>(context);
    }

    private void showResultErrMsg(int errState, JsonElement subMsg) {
        String title = loginContext.get().getString(ApiException.getErrResId(errState));
        String titleErrMsg = title != null ? title : "";
        String subErrmsg = !subMsg.isJsonNull() ? subMsg.getAsString() : "";
        loginView.showResultErrMsg(titleErrMsg + ":" + subErrmsg);
    }

    /**
     * 获取手机验证码
     */
    public void getVerifyCode() {
        loginView.showErrMsg("");
        if (!isNetAvailable()) {
            return;
        }
        if (!loginView.getUserPhone().isEmpty() && loginView.getUserPhone().length() == 11) {
            loginModel.getVerifyCode(loginView.getUserPhone(), new OznerHttpResult<JsonObject>() {
                @Override
                public void onError(Throwable e) {
                    LCLogUtils.E(TAG, "getVerifyCode_onError: " + e.getMessage());
                    loginView.showErrMsg(e.getMessage());
                }

                @Override
                public void onNext(JsonObject jsonObject) {
                    LCLogUtils.E(TAG, "getVerifyCode: " + jsonObject.toString());
                    if (jsonObject.get("state").getAsInt() > 0) {
                        loginView.showErrMsg(loginContext.get().getString(R.string.tips_getcode));
                        loginView.beginCountdown();
                    } else {
                        showResultErrMsg(jsonObject.get("state").getAsInt(), jsonObject.get("msg"));
                        loginView.showErrMsg(ApiException.getErrResId(jsonObject.get("state").getAsInt()));
                    }
                }
            });
        } else {
            loginView.showErrMsg(R.string.err_miss_phone);
        }
    }

    /**
     * 登录
     */

//    public void login() {
//        loginView.showErrMsg("");
//        if (!isNetAvailable()) {
//            return;
//        }
//        if (!loginView.getUserPhone().isEmpty() && loginView.getUserPhone().length() == 11) {
//            if (!loginView.getVerifyCode().isEmpty()) {
//                if (loginView.isCheckedProctol()) {
//                    loginModel.Login(loginView.getUserPhone(), loginView.getVerifyCode()
//                            , MobileInfoUtil.getImie(loginContext.get())
//                            , Build.MANUFACTURER, new Action1<JsonObject>() {
//                                @Override
//                                public void call(JsonObject jsonObject) {
//                                    Log.e(TAG, "login_result: " + jsonObject.toString());
//                                    if (jsonObject.get("state").getAsInt() > 0) {
//                                        OznerPreference.setUserToken(loginContext.get(), jsonObject.get("usertoken").getAsString());
//                                        OznerPreference.setIsLogin(loginContext.get(), true);
//                                        UserDataPreference.SetUserData(loginContext.get(), UserDataPreference.UserId, jsonObject.get("userid").getAsString());
//                                        try {
//                                            UserInfo userInfo = DBManager.getInstance(loginContext.get()).getUserInfo(jsonObject.get("userid").getAsString());
//                                            if(userInfo==null){
//                                                userInfo = new UserInfo();
//                                                userInfo.setUserId(jsonObject.get("userid").getAsString());
//                                            }
//                                            userInfo.setMobile(loginView.getUserPhone());
//                                            DBManager.getInstance(loginContext.get()).updateUserInfo(userInfo);
//                                        } catch (Exception ex) {
//
//                                        }
//                                        loginView.loginSuccess();
//                                    } else {
//                                        try {
//                                            Log.e(TAG, "login: " + jsonObject.get("msg").getAsString());
//                                        } catch (Exception ex) {
//                                            Log.e(TAG, "login_Ex: " + ex.getMessage());
//                                        }
//                                        showResultErrMsg(jsonObject.get("state").getAsInt(), jsonObject.get("msg"));
//                                        loginView.showErrMsg(ApiException.getErrResId(jsonObject.get("state").getAsInt()));
//                                    }
//                                }
//                            });
//                } else {
//                    loginView.showErrMsg(R.string.err_miss_proctol);
//                }
//            } else {
//                loginView.showErrMsg(R.string.err_miss_verify_code);
//            }
//        } else {
//            loginView.showErrMsg(R.string.err_miss_phone);
//        }
//    }
    public void login() {
        loginView.showErrMsg("");
        if (!isNetAvailable()) {
            LCLogUtils.E(TAG, "login: 网络中断");
            loginView.showErrMsg(R.string.err_net_outline);
            return;
        }
        if (!loginView.getUserPhone().isEmpty() && loginView.getUserPhone().length() == 11) {
            if (!loginView.getVerifyCode().isEmpty()) {
                if (loginView.isCheckedProctol()) {
                    loginModel.Login(loginView.getUserPhone(), loginView.getVerifyCode()
                            , MobileInfoUtil.getImie(loginContext.get())
                            , Build.MANUFACTURER, new OznerHttpResult<JsonObject>() {
                                @Override
                                public void onError(Throwable e) {
                                    Log.e(TAG, "login_onError: " + e.getMessage());
                                    if (e.getMessage() == null || e.getMessage().isEmpty()) {
                                        loginView.showErrMsg(R.string.Code_Login_Error);
                                    } else {
                                        loginView.showErrMsg(e.getMessage());
                                    }
                                }

                                @Override
                                public void onNext(JsonObject jsonObject) {
                                    Log.e(TAG, "login_result: " + jsonObject.toString());
                                    if (jsonObject.get("state").getAsInt() > 0) {
                                        OznerPreference.setUserToken(loginContext.get(), jsonObject.get("usertoken").getAsString());
                                        OznerPreference.setIsLogin(loginContext.get(), true);
                                        UserDataPreference.SetUserData(loginContext.get(), UserDataPreference.UserId, jsonObject.get("userid").getAsString());
                                        try {
                                            UserInfo userInfo = DBManager.getInstance(loginContext.get()).getUserInfo(jsonObject.get("userid").getAsString());
                                            if (userInfo == null) {
                                                userInfo = new UserInfo();
                                                userInfo.setUserId(jsonObject.get("userid").getAsString());
                                            }
                                            userInfo.setMobile(loginView.getUserPhone());
                                            DBManager.getInstance(loginContext.get()).updateUserInfo(userInfo);
                                        } catch (Exception ex) {

                                        }
                                        loginView.loginSuccess();
                                    } else {
                                        try {
                                            Log.e(TAG, "login: " + jsonObject.get("msg").getAsString());
                                        } catch (Exception ex) {
                                            Log.e(TAG, "login_Ex: " + ex.getMessage());
                                        }
                                        showResultErrMsg(jsonObject.get("state").getAsInt(), jsonObject.get("msg"));
                                        loginView.showErrMsg(ApiException.getErrResId(jsonObject.get("state").getAsInt()));
                                    }
                                }
                            }

                    );
                } else {
                    loginView.showErrMsg(R.string.err_miss_proctol);
                }
            } else {
                loginView.showErrMsg(R.string.err_miss_verify_code);
            }
        } else {
            loginView.showErrMsg(R.string.err_miss_phone);
        }
    }

//    public void getVoiceVerifyCode() {
//        loginView.showErrMsg("");
//        if (!isNetAvailable()) {
//            return;
//        }
//        if (!loginView.getUserPhone().isEmpty() && loginView.getUserPhone().length() == 11) {
//            HttpMethods.getInstance().getVoiceVerifyCode(loginView.getUserPhone()
//                    , new ProgressSubscriber<JsonObject>(loginContext.get()
//                            , loginContext.get().getString(R.string.verify_code_requesting)
//                            , false
//                            , new Action1<JsonObject>() {
//                        @Override
//                        public void call(JsonObject jsonObject) {
//                            Log.e(TAG, "getVoiceVerifyCode: " + jsonObject.toString());
//                            if (jsonObject.get("state").getAsInt() > 0) {
//                                loginView.showErrMsg(loginContext.get().getString(R.string.tips_getvoice));
//                            } else {
//                                showResultErrMsg(jsonObject.get("state").getAsInt(), jsonObject.get("msg"));
//                                loginView.showErrMsg(ApiException.getErrResId(jsonObject.get("state").getAsInt()));
//                            }
//                        }
//                    }));
//        } else {
//            loginView.showErrMsg(R.string.err_miss_phone);
//        }
//    }

    public void getVoiceVerifyCode() {
        loginView.showErrMsg("");
        if (!isNetAvailable()) {
            LCLogUtils.E(TAG, "login: 网络中断");
            loginView.showErrMsg(R.string.err_net_outline);
            return;
        }
        if (!loginView.getUserPhone().isEmpty() && loginView.getUserPhone().length() == 11) {
            HttpMethods.getInstance().getVoiceVerifyCode(loginView.getUserPhone()
                    , new ProgressSubscriber<JsonObject>(loginContext.get()
                            , loginContext.get().getString(R.string.verify_code_requesting)
                            , false
                            , new OznerHttpResult<JsonObject>() {
                        @Override
                        public void onError(Throwable e) {
                            loginView.showErrMsg(e.getMessage());
                        }

                        @Override
                        public void onNext(JsonObject jsonObject) {
                            Log.e(TAG, "getVoiceVerifyCode: " + jsonObject.toString());
                            if (jsonObject.get("state").getAsInt() > 0) {
                                loginView.showErrMsg(loginContext.get().getString(R.string.tips_getvoice));
                            } else {
                                showResultErrMsg(jsonObject.get("state").getAsInt(), jsonObject.get("msg"));
                                loginView.showErrMsg(ApiException.getErrResId(jsonObject.get("state").getAsInt()));
                            }
                        }
                    }));
        } else {
            loginView.showErrMsg(R.string.err_miss_phone);
        }
    }


    private boolean isNetAvailable() {
        if (!MobileInfoUtil.isNetworkAvailable(loginContext.get())) {
            return false;
        }
        return true;
    }
}
