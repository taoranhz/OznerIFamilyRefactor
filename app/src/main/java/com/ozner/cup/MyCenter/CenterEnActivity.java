package com.ozner.cup.MyCenter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.UserInfo;
import com.ozner.cup.MyCenter.Settings.SettingsActivity;
import com.ozner.cup.R;
import com.ozner.cup.Utils.LCLogUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class CenterEnActivity extends BaseActivity {
    private static final String TAG = "CenterEnActivity";
    @InjectView(R.id.iv_headImg)
    ImageView ivHeadImg;
    @InjectView(R.id.tv_name)
    TextView tvName;
    private UserInfo userInfo;
    private String mUserid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_center_en);
        ButterKnife.inject(this);
        try {
            mUserid = UserDataPreference.GetUserData(this, UserDataPreference.UserId, "");
            LCLogUtils.E(TAG,"userid:"+mUserid);
            userInfo = DBManager.getInstance(this).getUserInfo(mUserid);
            if (userInfo != null) {
                LCLogUtils.E(TAG,"userInfo:"+userInfo.toString());
                tvName.setText(userInfo.getEmail());
            }
        } catch (Exception ex) {
            LCLogUtils.E(TAG,"onCreate_Ex:"+ex.getMessage());
        }

    }

    @OnClick({R.id.rlay_my_device, R.id.rlay_feedback, R.id.rlay_settings, R.id.ib_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rlay_my_device:
                startActivity(new Intent(this, MyDeviceActivity.class));
                break;
            case R.id.rlay_feedback:
                startActivity(new Intent(this, FeedBackActivity.class));
                break;
            case R.id.rlay_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.ib_back:
                finish();
                break;
        }
    }
}
