package com.ozner.cup.MyCenter;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Command.UserDataPreference;
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
                break;
            case R.id.llay_about_ozner:
                startActivity(new Intent(this, AboutOznerActivity.class));
                break;
            case R.id.tv_logout:
                break;
        }
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
