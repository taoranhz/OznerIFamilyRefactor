package com.ozner.yiquan.MyCenter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;

import com.ozner.yiquan.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MyCenterActivity extends AppCompatActivity {

    @InjectView(R.id.ib_back)
    ImageButton ibBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_center);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.ib_back)
    public void onClick() {
        this.finish();
    }
}
