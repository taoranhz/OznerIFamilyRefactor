package com.ozner.cup.LoginWelcom.View;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.DBHelper.UserInfo;
import com.ozner.cup.Main.MainActivity;
import com.ozner.cup.Main.UserInfoManager;
import com.ozner.cup.R;

import butterknife.ButterKnife;

public class WelcomeActivity extends BaseActivity {
    private static final String TAG = "WelcomeActivity";
    private final int DEFAULT_WAIT_TIME = 1000;//启动页默认等待1秒
    private UserInfoManager userInfoManager;
    private String usertoken;
    private boolean isFirstStart = false;//是否第一次启动

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(getWindow().FEATURE_NO_TITLE);
        setContentView(R.layout.activity_welcome);
        ButterKnife.inject(this);
        usertoken = OznerPreference.getUserToken(this);
        try {
            isFirstStart = Boolean.parseBoolean(OznerPreference.GetValue(this, OznerPreference.IsFirstStart, "true"));
        } catch (Exception ex) {
            isFirstStart = false;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFirstStart) {
                    if (isLanguageEn()) {
                        startActivity(new Intent(WelcomeActivity.this, LoginEnActivity.class));
                    } else {
                        startActivity(new Intent(WelcomeActivity.this, GuideActivity.class));
                    }
                    WelcomeActivity.this.finish();
                } else {
                    userInfoManager = new UserInfoManager(WelcomeActivity.this);
                    if (usertoken != null && !usertoken.isEmpty()) {
                        checkLogin();
                    } else {
                        if (isLanguageEn()) {
                            startActivity(new Intent(WelcomeActivity.this, LoginEnActivity.class));
                        } else {
                            startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
                        }
                        WelcomeActivity.this.finish();
                    }
                }
            }
        }, DEFAULT_WAIT_TIME);

    }

    /**
     * 检查usertoken是否有效，有效就是已经登录，无效需要重新登录
     */
    private void checkLogin() {
        userInfoManager.loadUserInfo(new UserInfoManager.LoadUserInfoListener() {
            @Override
            public void onSuccess(UserInfo userInfo) {
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                WelcomeActivity.this.finish();
            }

            @Override
            public void onFail(String msg) {
                Log.e(TAG, "onFail: " + msg);
                if (isLanguageEn()) {
                    startActivity(new Intent(WelcomeActivity.this, LoginEnActivity.class));
                } else {
                    startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
                }
                WelcomeActivity.this.finish();
            }
        });
    }

}
