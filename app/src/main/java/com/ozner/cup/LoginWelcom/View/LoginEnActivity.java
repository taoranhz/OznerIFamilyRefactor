package com.ozner.cup.LoginWelcom.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.LoginWelcom.Presenter.LoginEnPresenter;
import com.ozner.cup.Main.MainActivity;
import com.ozner.cup.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class LoginEnActivity extends BaseActivity implements ILoginEnView {

    @InjectView(R.id.et_email)
    EditText etEmail;
    @InjectView(R.id.et_password)
    EditText etPassword;
    @InjectView(R.id.tv_wrong)
    TextView tvWrong;

    private LoginEnPresenter loginEnPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_en);
        ButterKnife.inject(this);
        loginEnPresenter = new LoginEnPresenter(this, this);
    }

    @OnClick({R.id.tv_reset, R.id.btn_login, R.id.tv_register})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_reset:
                break;
            case R.id.btn_login:
                tvWrong.setText("");
                loginEnPresenter.login();
                break;
            case R.id.tv_register:
                break;
        }
    }

    @Override
    public String getEmail() {
        return etEmail.getText().toString().trim();
    }

    @Override
    public String getPassword() {
        return etPassword.getText().toString().trim();
    }

    @Override
    public void showErrMsg(String msg) {
        tvWrong.setText(msg);
    }

    @Override
    public void showErrMsg(int msgStrId) {
        tvWrong.setText(getString(msgStrId));
    }

    @Override
    public void showToastMsg(String msg) {
        showToastCenter(msg);
    }

    @Override
    public void loginSuccess() {
        startActivity(new Intent(this, MainActivity.class));
        this.finish();
    }
}
