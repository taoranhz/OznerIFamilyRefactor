package com.ozner.cup.LoginWelcom.View;

/**
 * Created by ozner_67 on 2017/3/15.
 * 邮箱：xinde.zhang@cftcn.com
 */

public interface IResetPwdView {
    String getEmail();

    String getNewPwd();

    String getConfirmPwd();

    String getVerifyCode();

    void showToastMsg(String msg);

    void reqCodeSuccess();

    void onSuccess();

}
