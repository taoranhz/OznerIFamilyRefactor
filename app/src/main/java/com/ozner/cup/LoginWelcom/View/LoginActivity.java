package com.ozner.cup.LoginWelcom.View;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.LoginWelcom.Presenter.LoginPresenter;
import com.ozner.cup.Main.MainActivity;
import com.ozner.cup.R;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener, ILoginView {

    @InjectView(R.id.et_userphone)
    EditText etUserphone;
    @InjectView(R.id.et_password)
    EditText etPassword;
    @InjectView(R.id.btn_getcode)
    Button btnGetcode;
    @InjectView(R.id.tv_errMsg)
    TextView tvErrMsg;
    @InjectView(R.id.btn_login)
    Button btnLogin;
    @InjectView(R.id.cb_proctol)
    CheckBox cbProctol;
    @InjectView(R.id.tv_proctol)
    TextView tvProctol;
    @InjectView(R.id.btn_getVoiceCode)
    Button btnGetVoiceCode;

    private int timeIndex = 0;
    Timer timer = new Timer();
    private boolean isProctolChecked = true;
    private LoginPresenter loginPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);
        cbProctol.setOnCheckedChangeListener(this);
        loginPresenter = new LoginPresenter(this, this);
    }


    TimerTask task = new TimerTask() {

        @Override
        public void run() {
            // 需要做的事:发送消息
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    };

    /**
     * 倒计时开始
     */
    private void startWaitVerifyCode() {
        btnGetcode.setEnabled(false);

        if (timer == null)
            timer = new Timer();
        else {
            timer = null;
            timer = new Timer();
        }
        if (task != null) {
            task.cancel();
            task = new TimerTask() {

                @Override
                public void run() {
                    // 需要做的事:发送消息
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }
            };
        } else {
            task = new TimerTask() {

                @Override
                public void run() {
                    // 需要做的事:发送消息
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }
            };
        }
        timer.schedule(task, 0, 1000);
    }

    /**
     * 倒计时结束
     */
    private void stopWaitVerifyCode() {
        timeIndex = 0;
        task.cancel();
        timer = null;
        btnGetcode.setEnabled(true);
        btnGetcode.setText(getString(R.string.get_verify_code));
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (timeIndex < 60) {
                    timeIndex++;
                    String waitTime = String.valueOf(60 - timeIndex);
                    btnGetcode.setText(waitTime + "s");
                } else {
                    stopWaitVerifyCode();
                }
            }
            super.handleMessage(msg);
        }
    };

    @OnClick({R.id.btn_getcode, R.id.btn_login, R.id.tv_proctol, R.id.btn_getVoiceCode})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_getcode:
                loginPresenter.getVerifyCode();
                break;
            case R.id.btn_login:
                loginPresenter.login();
                break;
            case R.id.tv_proctol:
                // TODO: 2016/11/3 打开网页
                break;
            case R.id.btn_getVoiceCode:
                loginPresenter.getVoiceVerifyCode();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        isProctolChecked = isChecked;
    }

    @Override
    public boolean isCheckedProctol() {
        return isProctolChecked;
    }

    @Override
    public String getUserPhone() {
        return etUserphone.getText().toString().trim();
    }

    @Override
    public String getVerifyCode() {
        return etPassword.getText().toString().trim();
    }

    @Override
    public void showErrMsg(String errMsg) {
        tvErrMsg.setText(errMsg);
    }

    @Override
    public void showErrMsg(int errMsgResId) {
        tvErrMsg.setText(getString(errMsgResId));
    }

    @Override
    public void beginCountdown() {
        startWaitVerifyCode();
    }

    @Override
    public void showResultErrMsg(String errMsg) {
        Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void loginSuccess() {
        startActivity(new Intent(this, MainActivity.class));
        this.finish();
    }
}
