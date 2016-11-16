package com.ozner.cup.LoginWelcom.Model;

import com.google.gson.JsonObject;

import rx.functions.Action1;

/**
 * Created by ozner_67 on 2016/11/2.
 * 邮箱：xinde.zhang@cftcn.com
 */

public interface ILoginModel {
    void getVerifyCode(String phone, Action1<JsonObject> onNext);

    void getVoiceVerifyCode(String phone, Action1<JsonObject> onNext);

    void Login(String phone, String verifyCode, String miei, String devicename, Action1<JsonObject> onNext);

    void reLogin(String usertoken, Action1<JsonObject> onNext);
}
