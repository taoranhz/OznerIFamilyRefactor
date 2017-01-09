package com.ozner.cup.LoginWelcom.Model;

import android.content.Context;

import com.google.gson.JsonObject;
import com.ozner.cup.HttpHelper.HttpMethods;
import com.ozner.cup.HttpHelper.ProgressSubscriber;
import com.ozner.cup.R;

import java.lang.ref.WeakReference;

import rx.functions.Action1;

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
    public void getVerifyCode(String phone, Action1<JsonObject> onNext) {
        HttpMethods.getInstance().getPhoneCode(phone
                , new ProgressSubscriber<JsonObject>(loginContext.get()
                        , loginContext.get().getString(R.string.verify_code_requesting)
                        , false
                        , onNext));
    }

    @Override
    public void getVoiceVerifyCode(String phone, Action1<JsonObject> onNext) {
        HttpMethods.getInstance().getVoiceVerifyCode(phone
                , new ProgressSubscriber<JsonObject>(loginContext.get()
                        , loginContext.get().getString(R.string.verify_code_requesting)
                        , false
                        , onNext));
    }

    @Override
    public void Login(String phone, String verifyCode, String miei, String devicename, Action1<JsonObject> onNext) {
        HttpMethods.getInstance().login(phone, verifyCode, miei, devicename
                , new ProgressSubscriber<JsonObject>(loginContext.get()
                        , loginContext.get().getString(R.string.logining)
                        , false
                        , onNext));
    }


    @Override
    public void reLogin(String usertoken, Action1<JsonObject> onNext) {

    }
}
