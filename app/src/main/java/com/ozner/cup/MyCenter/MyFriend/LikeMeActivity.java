package com.ozner.cup.MyCenter.MyFriend;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LikeMeActivity extends BaseActivity {

    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_me);
        ButterKnife.inject(this);
        initToolBar();
    }

    /**
     * 初始化ToolBar
     */
    private void initToolBar() {
        toolbar.setTitle("");
        title.setText(R.string.like_me);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
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
