package com.ozner.cup.Chat;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.EMMessage;
import com.ozner.cup.R;
import com.ozner.cup.Utils.TransitionHelper.TransitionsHeleper;
import com.ozner.cup.Utils.TransitionHelper.bean.InfoBean;
import com.ozner.cup.Utils.TransitionHelper.method.ColorShowMethod;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class BigImageDetailActivity extends BaseActivity {
    private static final String TAG = "BigImageDetail";
    public static final String PARAMS_MSG_ID = "params_msg_id";
    //    @InjectView(R.id.title)
//    TextView title;
//    @InjectView(R.id.toolbar)
//    Toolbar toolbar;
    @InjectView(R.id.iv_showImg)
    ImageView ivShowImg;
    private long msgId;
    private EMMessage emMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        setContentView(R.layout.activity_big_image_detail);
        ButterKnife.inject(this);
//        title.setText("确认图片");

        try {
            msgId = getIntent().getIntExtra(PARAMS_MSG_ID, 0);
            String userid = OznerPreference.GetValue(this, OznerPreference.UserId, "");
            if (msgId > 0 && !userid.isEmpty()) {
                emMessage = DBManager.getInstance(this).getChatMessage(userid, msgId);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "initView_Ex: " + ex.getMessage());
        }
        showTransAnim();
    }

    private void showTransAnim() {
        TransitionsHeleper.getInstance()
                .setShowMethod(new ColorShowMethod(R.color.translucent, R.color.translucent) {
                    @Override
                    public void loadCopyView(InfoBean bean, ImageView copyView) {
                        Glide.with(BigImageDetailActivity.this)
                                .load(bean.getImgUrl())
                                .fitCenter()
                                .into(copyView);
                    }

                    @Override
                    public void loadTargetView(InfoBean bean, ImageView targetView) {
                        Glide.with(BigImageDetailActivity.this)
                                .load(bean.getImgUrl())
                                .fitCenter()
                                .into(targetView);
                    }
                }).show(this, ivShowImg);
    }

    @OnClick(R.id.activity_big_image_detail)
    public void onClick() {
        this.finish();
    }
}
