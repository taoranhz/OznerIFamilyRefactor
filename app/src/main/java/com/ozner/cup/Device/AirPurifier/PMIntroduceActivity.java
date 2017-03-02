package com.ozner.cup.Device.AirPurifier;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;

import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PMIntroduceActivity extends BaseActivity {
    private static final String TAG = "PMIntroduceActivity";
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pmintroduce);
        ButterKnife.inject(this);
        initToolBar();
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.cup_detail_bg));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.cup_detail_bg));
        }
    }


    /**
     * 初始化ToolBar
     */
    private void initToolBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        title.setText(R.string.what_is_pm25);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.cup_detail_bg));
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
