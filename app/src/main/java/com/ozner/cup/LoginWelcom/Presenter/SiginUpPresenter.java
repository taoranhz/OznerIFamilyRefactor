package com.ozner.cup.LoginWelcom.Presenter;

import android.content.Context;

import com.google.gson.JsonObject;
import com.ozner.cup.HttpHelper.ApiException;
import com.ozner.cup.HttpHelper.HttpMethods;
import com.ozner.cup.HttpHelper.OznerHttpResult;
import com.ozner.cup.HttpHelper.ProgressSubscriber;
import com.ozner.cup.LoginWelcom.View.IResetPwdView;
import com.ozner.cup.R;
import com.ozner.cup.Utils.VerifyUtil;

import java.lang.ref.WeakReference;

/**
 * Created by ozner_67 on 2017/3/15.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class SiginUpPresenter {
    private WeakReference<Context> mContext;
    private IResetPwdView resetView;

    public SiginUpPresenter(Context context, IResetPwdView resetPwdView) {
        this.mContext = new WeakReference<Context>(context);
        this.resetView = resetPwdView;
    }


    /**
     * 获取验证码
     */
    public void getVerifyCode() {
        if (null == resetView)
            return;
        if (resetView.getEmail().length() <= 0) {
            resetView.showToastMsg(mContext.get().getString(R.string.input_email));
            return;
        }
        if (VerifyUtil.isEmail(resetView.getEmail())) {
            resetView.showToastMsg(mContext.get().getString(R.string.valid_email));
        }

        HttpMethods.getInstance().getEmailCode(resetView.getEmail()
                , new ProgressSubscriber<JsonObject>(mContext.get()
                        , mContext.get().getString(R.string.sendingEmailCode)
                        , true
                        , new OznerHttpResult<JsonObject>() {
                    @Override
                    public void onError(Throwable e) {
                        if (e.getMessage().length() > 0) {
                            resetView.showToastMsg(e.getMessage());
                        } else {
                            resetView.showToastMsg(mContext.get().getString(R.string.fail_to_get_code));
                        }
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        if (!jsonObject.isJsonNull()) {
                            int state = jsonObject.get("state").getAsInt();
                            if (state > 0) {
                                resetView.reqCodeSuccess();
                            } else {
                                String title = mContext.get().getString(ApiException.getErrResId(jsonObject.get("state").getAsInt()));
                                String titleErrMsg = title != null ? title : "";
                                String subErrmsg = !jsonObject.get("msg").isJsonNull() ? ":" + jsonObject.get("msg").getAsString() : "";
                                resetView.showToastMsg(titleErrMsg + subErrmsg);
                            }
                        } else {
                            resetView.showToastMsg(mContext.get().getString(R.string.err_net_outline));
                        }
                    }
                }));

    }


    /**
     * 重置密码
     */
    public void resetPassword() {
        if (!checkInputPass()) {
            return;
        }

        HttpMethods.getInstance().resetPassword(resetView.getEmail(), resetView.getNewPwd(), resetView.getVerifyCode(),
                new ProgressSubscriber<JsonObject>(mContext.get(), mContext.get().getString(R.string.submiting), true,
                        new OznerHttpResult<JsonObject>() {
                            @Override
                            public void onError(Throwable e) {
                                if (e.getMessage().length() > 0) {
                                    resetView.showToastMsg(e.getMessage());
                                } else {
                                    resetView.showToastMsg(mContext.get().getString(R.string.fail_to_get_code));
                                }
                            }

                            @Override
                            public void onNext(JsonObject jsonObject) {
                                if (!jsonObject.isJsonNull()) {
                                    int state = jsonObject.get("state").getAsInt();
                                    if (state > 0) {
                                        resetView.onSuccess();
                                    } else {
                                        String title = mContext.get().getString(ApiException.getErrResId(jsonObject.get("state").getAsInt()));
                                        String titleErrMsg = title != null ? title : "";
                                        String subErrmsg = !jsonObject.get("msg").isJsonNull() ? ":" + jsonObject.get("msg").getAsString() : "";
                                        resetView.showToastMsg(titleErrMsg + subErrmsg);
                                    }
                                } else {
                                    resetView.showToastMsg(mContext.get().getString(R.string.err_net_outline));
                                }
                            }
                        }));

    }

    /**
     * 邮箱注册
     */
    public void signUp() {
        if (!checkInputPass()) {
            return;
        }

        HttpMethods.getInstance().signUpMail(resetView.getEmail(),resetView.getNewPwd(), resetView.getVerifyCode(),
                new ProgressSubscriber<JsonObject>(mContext.get(), mContext.get().getString(R.string.submiting), true,
                        new OznerHttpResult<JsonObject>() {
                            @Override
                            public void onError(Throwable e) {
                                if (e.getMessage().length() > 0) {
                                    resetView.showToastMsg(e.getMessage());
                                } else {
                                    resetView.showToastMsg(mContext.get().getString(R.string.fail_to_get_code));
                                }
                            }

                            @Override
                            public void onNext(JsonObject jsonObject) {
                                if (!jsonObject.isJsonNull()) {
                                    int state = jsonObject.get("state").getAsInt();
                                    if (state > 0) {
                                        resetView.onSuccess();
                                    } else {
                                        String title = mContext.get().getString(ApiException.getErrResId(jsonObject.get("state").getAsInt()));
                                        String titleErrMsg = title != null ? title : "";
                                        String subErrmsg = !jsonObject.get("msg").isJsonNull() ? ":" + jsonObject.get("msg").getAsString() : "";
                                        resetView.showToastMsg(titleErrMsg + subErrmsg);
                                    }
                                } else {
                                    resetView.showToastMsg(mContext.get().getString(R.string.err_net_outline));
                                }
                            }
                        }));

    }

    /**
     * 检查注册和重置密码时的输入
     *
     * @return
     */
    private boolean checkInputPass() {
        if (resetView == null)
            return false;

        if (resetView.getEmail().length() <= 0) {
            resetView.showToastMsg(mContext.get().getString(R.string.input_email));
            return false;
        }

        if (!VerifyUtil.isEmail(resetView.getEmail())) {
            resetView.showToastMsg(mContext.get().getString(R.string.valid_email));
            return false;
        }
        if (resetView.getVerifyCode().length() <= 0) {
            resetView.showToastMsg(mContext.get().getString(R.string.input_verifycode));
            return false;
        }

        if (resetView.getNewPwd().length() <= 0) {
            resetView.showToastMsg(mContext.get().getString(R.string.input_password));
            return false;
        }

        if (!resetView.getNewPwd().trim().equals(resetView.getConfirmPwd().trim())) {
            resetView.showToastMsg(mContext.get().getString(R.string.different_pwd));
            return false;
        }

        return true;
    }

}
