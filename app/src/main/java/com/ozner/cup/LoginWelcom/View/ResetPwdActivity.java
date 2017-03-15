package com.ozner.cup.LoginWelcom.View;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.LoginWelcom.Presenter.SiginUpPresenter;
import com.ozner.cup.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ResetPwdActivity extends BaseActivity implements IResetPwdView {
    private static final String TAG = "ResetPwdActivity";

    @InjectView(R.id.et_email)
    EditText etEmail;
    @InjectView(R.id.et_verCode)
    EditText etVerCode;
    @InjectView(R.id.tv_verifyCode)
    TextView tvVerifyCode;
    @InjectView(R.id.et_new_password)
    EditText etNewPassword;
    @InjectView(R.id.et_confirm_password)
    EditText etConfirmPassword;

    private SiginUpPresenter siginUpPresenter;
    private CountDownTimer countTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pwd);
        ButterKnife.inject(this);
        siginUpPresenter = new SiginUpPresenter(this, this);
        countTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvVerifyCode.setText(String.format("%ds", millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                tvVerifyCode.setText(R.string.get_verify_code);
                tvVerifyCode.setEnabled(true);
            }
        };
    }

    @OnClick({R.id.ib_back, R.id.tv_verifyCode, R.id.btn_sign_up})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_back:
                this.finish();
                break;
            case R.id.tv_verifyCode:
//                startTimer();
                siginUpPresenter.getVerifyCode();
                break;
            case R.id.btn_sign_up:
                siginUpPresenter.resetPassword();
//                stopTimer();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        stopTimer();
        super.onDestroy();
    }

    /**
     * 开始计时
     */
    private void startTimer() {
        tvVerifyCode.setEnabled(false);
        countTimer.start();
    }

    /**
     * 结束计时
     */
    private void stopTimer() {
        countTimer.onFinish();
        countTimer.cancel();
    }

    @Override
    public String getEmail() {
        return etEmail.getText().toString().trim();
    }

    @Override
    public String getNewPwd() {
        return etNewPassword.getText().toString().trim();
    }

    @Override
    public String getConfirmPwd() {
        return etConfirmPassword.getText().toString().trim();
    }

    @Override
    public String getVerifyCode() {
        return etVerCode.getText().toString().trim();
    }

    @Override
    public void showToastMsg(String msg) {
        showToastCenter(msg);
    }

    @Override
    public void reqCodeSuccess() {
        startTimer();
    }

    @Override
    public void onSuccess() {
        new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT)
                .setMessage(R.string.reset_pwd_success)
                .setPositiveButton(R.string.ensure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent resIntent = new Intent();
                        resIntent.putExtra("email",etEmail.getText().toString().trim());
                        setResult(RESULT_OK,resIntent);
                        ResetPwdActivity.this.finish();
                    }
                }).show();
    }
}
