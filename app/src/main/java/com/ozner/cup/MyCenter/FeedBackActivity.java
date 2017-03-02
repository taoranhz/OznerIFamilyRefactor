package com.ozner.cup.MyCenter;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.HttpHelper.ApiException;
import com.ozner.cup.HttpHelper.HttpMethods;
import com.ozner.cup.HttpHelper.OznerHttpResult;
import com.ozner.cup.HttpHelper.ProgressSubscriber;
import com.ozner.cup.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class FeedBackActivity extends BaseActivity {

    @InjectView(R.id.et_adviseText)
    EditText etAdviseText;
    @InjectView(R.id.tv_length)
    TextView tvLength;
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    private String userid, userToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_back);
        ButterKnife.inject(this);
        initToolBar();
        try {
            userid = UserDataPreference.GetUserData(this, UserDataPreference.UserId, null);
            userToken = OznerPreference.getUserToken(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        etAdviseText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                tvLength.setText(String.valueOf(s.length()));
            }
        });
    }

    private void initToolBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        title.setText(R.string.adsive);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setBackgroundColor(Color.WHITE);
        title.setTextColor(Color.BLACK);
    }

    @OnClick(R.id.btn_Submit)
    public void onClick() {
        if (userid != null && !userid.isEmpty()) {
            if (etAdviseText.getText().length() > 0) {
                submitOptions(etAdviseText.getText().toString().trim());
            } else {
                showToastCenter(R.string.input_advise);
            }
        } else {
            showToastCenter(R.string.need_login_first);
        }
    }

    /**
     * 提交意见
     *
     * @param message
     */
    private void submitOptions(String message) {
        HttpMethods.getInstance().submitOption(OznerPreference.getUserToken(this), message,
                new ProgressSubscriber<JsonObject>(this, getString(R.string.submiting), false, new OznerHttpResult<JsonObject>() {
                    @Override
                    public void onError(Throwable e) {
                        showToastCenter(e.getMessage());
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        if (jsonObject != null) {
                            if (jsonObject.get("state").getAsInt() > 0) {
                                Toast toast = new Toast(FeedBackActivity.this);
                                View layout = LayoutInflater.from(FeedBackActivity.this).inflate(R.layout.advise_result_toast, null);
                                toast.setDuration(Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.setView(layout);
                                toast.show();
                                FeedBackActivity.this.finish();
                            } else {
                                if (jsonObject.get("state").getAsInt() == -10006
                                        || jsonObject.get("state").getAsInt() == -10007) {
                                    BaseActivity.reLogin(FeedBackActivity.this);
                                } else {
                                    showToastCenter(getString(ApiException.getErrResId(jsonObject.get("state").getAsInt())));
                                }
                            }
                        } else {
                            showToastCenter(R.string.submit_fail);
                        }
                    }
                }));
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
