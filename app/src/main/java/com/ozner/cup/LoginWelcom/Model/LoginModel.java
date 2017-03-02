package com.ozner.cup.LoginWelcom.Model;

import android.content.Context;

import com.google.gson.JsonObject;
import com.ozner.cup.HttpHelper.HttpMethods;
import com.ozner.cup.HttpHelper.OznerHttpResult;
import com.ozner.cup.HttpHelper.ProgressSubscriber;
import com.ozner.cup.R;

import java.lang.ref.WeakReference;

/**
 * Created by ozner_67 on 2016/11/2.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class LoginModel implements ILoginModel {
    private static final String TAG = "LoginModel";
    private WeakReference<Context> loginContext;

    public LoginModel(Context context) {
        loginContext = new WeakReference<Context>(context);
    }

    @Override
    public void getVerifyCode(String phone, OznerHttpResult httpResult) {
        HttpMethods.getInstance().getPhoneCode(phone
                , new ProgressSubscriber<JsonObject>(loginContext.get()
                        , loginContext.get().getString(R.string.verify_code_requesting)
                        , false
                        , httpResult));
    }

    @Override
    public void getVoiceVerifyCode(String phone, OznerHttpResult httpResult) {
        HttpMethods.getInstance().getVoiceVerifyCode(phone
                , new ProgressSubscriber<JsonObject>(loginContext.get()
                        , loginContext.get().getString(R.string.verify_code_requesting)
                        , false
                        , httpResult));
    }

    @Override
    public void Login(String phone, String verifyCode, String miei, String devicename, OznerHttpResult httpResult) {
        HttpMethods.getInstance().login(phone, verifyCode, miei, devicename
                , new ProgressSubscriber<JsonObject>(loginContext.get()
                        , loginContext.get().getString(R.string.logining)
                        , false
                        , httpResult));
    }


    @Override
    public void reLogin(String usertoken, OznerHttpResult httpResult) {

    }
}
