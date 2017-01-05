package com.ozner.cup.MyCenter.Settings;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.kayvannj.permission_utils.Func;
import com.github.kayvannj.permission_utils.PermissionUtil;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Base.WebActivity;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.MyCenter.OznerUpdateManager;
import com.ozner.cup.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class AboutOznerActivity extends BaseActivity {

    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.tv_version)
    TextView tvVersion;
    private OznerUpdateManager updateManager;
    private PermissionUtil.PermissionRequestObject perReqResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_ozner);
        ButterKnife.inject(this);
        initToolBar();
        updateManager = new OznerUpdateManager(this, true);
        tvVersion.setText(getVersion());
    }

    private void initToolBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        title.setText(R.string.about_ozner);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setBackgroundColor(Color.WHITE);
        title.setTextColor(Color.BLACK);
    }

    @OnClick({R.id.llay_checkVersion, R.id.llay_rate_ozner, R.id.tv_exceptions})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.llay_checkVersion:
                checkUpdate();
                break;
            case R.id.llay_rate_ozner:
                String rateStr = "market://details?id=" + getPackageName();
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(rateStr));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        showToastCenter("找不到应用，无法评分");
                    }
                } catch (Exception ex) {
                    showToastCenter(ex.getMessage());
                }
                break;
            case R.id.tv_exceptions:
                Intent exceptIntent = new Intent(this, WebActivity.class);
                exceptIntent.putExtra(Contacts.PARMS_URL, Contacts.exceptions_url);
                startActivity(exceptIntent);
                break;
        }
    }

    /**
     * 检查更新
     */
    private void checkUpdate() {
        perReqResult = PermissionUtil.with(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onAllGranted(new Func() {
                    @Override
                    protected void call() {
                        updateManager.checkUpdate();
                    }
                }).onAnyDenied(new Func() {
                    @Override
                    protected void call() {
                        showToastCenter(R.string.user_deny_write_storge);
                    }
                }).ask(1);
    }


    /**
     * 获取当前版本名称
     *
     * @return
     */
    private String getVersion() {
        try {
            PackageManager pgManager = getPackageManager();
            PackageInfo pgInfo = pgManager.getPackageInfo(getPackageName(), 0);
            return pgInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "1.0.0";
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (perReqResult != null) {
            perReqResult.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
