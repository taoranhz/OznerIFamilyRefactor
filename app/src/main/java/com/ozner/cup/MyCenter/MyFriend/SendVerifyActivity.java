package com.ozner.cup.MyCenter.MyFriend;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.gson.JsonObject;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.HttpHelper.ApiException;
import com.ozner.cup.HttpHelper.HttpMethods;
import com.ozner.cup.HttpHelper.OznerHttpResult;
import com.ozner.cup.HttpHelper.ProgressSubscriber;
import com.ozner.cup.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class SendVerifyActivity extends BaseActivity {
    private static final String TAG = "SendVerifyActivity";

    @InjectView(R.id.et_sendMsg)
    EditText etSendMsg;
    @InjectView(R.id.llay_cleanUp)
    LinearLayout llayCleanUp;

    private String sendMobile;
    private int clickPos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_verify);
        ButterKnife.inject(this);
        try {
            sendMobile = getIntent().getStringExtra(Contacts.PARMS_PHONE);
            clickPos = getIntent().getIntExtra(Contacts.PARMS_CLICK_POS, -1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        etSendMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    if (!llayCleanUp.isShown())
                        llayCleanUp.setVisibility(View.VISIBLE);
                } else {
                    llayCleanUp.setVisibility(View.GONE);
                }
            }
        });
    }

    @OnClick({R.id.llay_cancle, R.id.llay_send, R.id.llay_cleanUp})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.llay_cancle:
                this.finish();
                break;
            case R.id.llay_send:
                if (sendMobile != null && !sendMobile.isEmpty()) {
                    String verifyMsg = etSendMsg.getText().toString().trim();
                    sendMessage(sendMobile, verifyMsg);
                } else {
                    showToastCenter(R.string.Code_P_params_error);
                }
                break;
            case R.id.llay_cleanUp:
                etSendMsg.setText("");
                break;
        }
    }

    /**
     * 发送验证信息
     *
     * @param mobile
     * @param content
     */
    private void sendMessage(String mobile, String content) {
        HttpMethods.getInstance().addFriend(OznerPreference.getUserToken(this), mobile, content,
                new ProgressSubscriber<JsonObject>(this, getString(R.string.submiting), false, new OznerHttpResult<JsonObject>() {
                    @Override
                    public void onError(Throwable e) {
                        showToastCenter(e.getMessage());
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        if (jsonObject != null) {
                            if (jsonObject.get("state").getAsInt() > 0) {
                                Intent resIntent = new Intent();
                                resIntent.putExtra(Contacts.PARMS_CLICK_POS, clickPos);
                                resIntent.putExtra(Contacts.PARMS_PHONE, sendMobile);
                                setResult(RESULT_OK, resIntent);
                                finish();
                            } else {
                                if (jsonObject.get("state").getAsInt() == -10006
                                        || jsonObject.get("state").getAsInt() == -10007) {
                                    BaseActivity.reLogin(SendVerifyActivity.this);
                                } else {
                                    showToastCenter(getString(ApiException.getErrResId(jsonObject.get("state").getAsInt())));
                                }
                            }
                        } else {
                            showToastCenter(R.string.send_status_fail);
                        }
                    }
                }));
    }
}
