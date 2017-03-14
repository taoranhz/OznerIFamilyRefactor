package com.ozner.cup.LoginWelcom.View;

/**
 * Created by ozner_67 on 2017/3/14.
 * 邮箱：xinde.zhang@cftcn.com
 */

public interface ILoginEnView {
    String getEmail();

    String getPassword();

    void showErrMsg(String msg);

    void showErrMsg(int msgStrId);

    void showToastMsg(String msg);

    void loginSuccess();
}
