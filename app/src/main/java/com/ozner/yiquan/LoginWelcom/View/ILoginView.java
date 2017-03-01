package com.ozner.yiquan.LoginWelcom.View;

/**
 * Created by ozner_67 on 2016/11/2.
 * 邮箱：xinde.zhang@cftcn.com
 */

public interface ILoginView {
    boolean isCheckedProctol();

    String getUserPhone();

    String getVerifyCode();

    void showErrMsg(String errMsg);

    void showErrMsg(int errMsgResId);

    void beginCountdown();

    void showResultErrMsg(String errMsg);

    void loginSuccess();
}
