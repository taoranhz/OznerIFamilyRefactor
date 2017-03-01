package com.ozner.yiquan.MyCenter.Settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ozner.yiquan.Base.BaseActivity;
import com.ozner.yiquan.Command.OznerPreference;
import com.ozner.yiquan.Command.UserDataPreference;
import com.ozner.yiquan.LoginWelcom.View.LoginActivity;
import com.ozner.yiquan.R;
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

    private boolean isAllowPushMsg = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.inject(this);
        initToolBar();

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
                        UserDataPreference.SetUserData(SettingsActivity.this,UserDataPreference.UserId,"");
                        OznerPreference.setUserToken(SettingsActivity.this,"");
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
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
