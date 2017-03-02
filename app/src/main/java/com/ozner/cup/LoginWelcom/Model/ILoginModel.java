package com.ozner.cup.LoginWelcom.Model;

import com.ozner.cup.HttpHelper.OznerHttpResult;

/**
 * Created by ozner_67 on 2016/11/2.
 * 邮箱：xinde.zhang@cftcn.com
 */

public interface ILoginModel {
    void getVerifyCode(String phone, OznerHttpResult httpResult);

    void getVoiceVerifyCode(String phone, OznerHttpResult httpResult);

    void Login(String phone, String verifyCode, String miei, String devicename, OznerHttpResult httpResult);

    void reLogin(String usertoken, OznerHttpResult httpResult);
}
