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
    private final int RESET_REQ_CODE = 1;
    private final int SIGNUP_REQ_CODE = 2;
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
                startActivityForResult(new Intent(this, ResetPwdActivity.class), RESET_REQ_CODE);
                break;
            case R.id.btn_login:
                tvWrong.setText("");
                loginEnPresenter.login();
                break;
            case R.id.tv_register:
                startActivityForResult(new Intent(this, SignUpActivity.class), SIGNUP_REQ_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
//            if (requestCode == RESET_REQ_CODE) {
            String email = data.getStringExtra("email");
            if (email != null) {
                etEmail.setText(email);
            }
//            }else if (requestCode == SIGNUP_REQ_CODE){
//                String email = data.getStringExtra("email");
//                if (email != null) {
//                    etEmail.setText(email);
//                }
//            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
