package com.ozner.cup.MyCenter.Settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.LoginWelcom.View.LoginActivity;
import com.ozner.cup.LoginWelcom.View.LoginEnActivity;
import com.ozner.cup.R;
import com.zcw.togglebutton.ToggleButton;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class SettingsActivity extends BaseActivity {

    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.tb_allowPushMsg)
    ToggleButton tbAllowPushMsg;
    @InjectView(R.id.llay_pushMsg)
    LinearLayout llayPushMsg;
    @InjectView(R.id.tv_pushMsgLine)
    TextView tvPushMsgLine;

    private boolean isAllowPushMsg = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.inject(this);
        initToolBar();
        if (UserDataPreference.isLoginEmail(this)) {
            llayPushMsg.setVisibility(View.GONE);
            tvPushMsgLine.setVisibility(View.GONE);
        } else {
            isAllowPushMsg = Boolean.parseBoolean(UserDataPreference.GetUserData(this, UserDataPreference.IsAllowPushMessage, "true"));

            if (isAllowPushMsg) {
                tbAllowPushMsg.setToggleOn();
            } else {
                tbAllowPushMsg.setToggleOff();
            }
            tbAllowPushMsg.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
                @Override
                public void onToggle(boolean on) {
                    UserDataPreference.SetUserData(SettingsActivity.this, UserDataPreference.IsAllowPushMessage, String.valueOf(on));
                }
            });
        }
    }

    private void initToolBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        title.setText(R.string.setting);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setBackgroundColor(Color.WHITE);
        title.setTextColor(Color.BLACK);
    }

    @OnClick({R.id.llay_unit, R.id.llay_about_ozner, R.id.tv_logout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.llay_unit:
                startActivity(new Intent(this, UnitSettingsActivity.class));
                break;
            case R.id.llay_about_ozner:
                startActivity(new Intent(this, AboutOznerActivity.class));
                break;
            case R.id.tv_logout:
                logout();
                break;
        }
    }

    /**
     * 退出登录
     */
    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        builder.setTitle(R.string.logout)
                .setMessage(R.string.islogout)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OznerPreference.SetValue(SettingsActivity.this, OznerPreference.UserId, "");
                        OznerPreference.setUserToken(SettingsActivity.this, "");
                        if (isLanguageEn()) {
                            startActivity(new Intent(getApplicationContext(), LoginEnActivity.class));
                        } else {
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        }
                        finishAffinity();
                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return true;
    }

}
