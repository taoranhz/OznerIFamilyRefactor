package com.ozner.yiquan.MyCenter.MyFriend;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.ozner.yiquan.Base.BaseActivity;
import com.ozner.yiquan.Base.CommonAdapter;
import com.ozner.yiquan.Base.CommonViewHolder;
import com.ozner.yiquan.Command.CenterNotification;
import com.ozner.yiquan.MyCenter.MyFriend.bean.VerifyMessageItem;
import com.ozner.yiquan.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DealVerifyActivity extends BaseActivity {

    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.lv_verifyMsg)
    ListView lvVerifyMsg;
    @InjectView(R.id.tv_no_msg)
    TextView tvNoMsg;
    VerifyAdapter mAdapter;
    FriendInfoManager infoManager;
    private List<VerifyMessageItem> verifyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal_verify);
        ButterKnife.inject(this);
        initToolBar();
        verifyList = new ArrayList<>();
        mAdapter = new VerifyAdapter(this, R.layout.verify_msg_item);
        lvVerifyMsg.setEmptyView(tvNoMsg);
        lvVerifyMsg.setAdapter(mAdapter);
//        initTestData();
        infoManager = new FriendInfoManager(this, null);
        infoManager.getVerifyMessage(new FriendInfoManager.LoadVerifyListener() {
            @Override
            public void onSuccess(List<VerifyMessageItem> result) {
                verifyList = result;
                mAdapter.loadData(verifyList);
            }

            @Override
            public void onFail(String msg) {
                Toast.makeText(DealVerifyActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 初始化ToolBar
     */
    private void initToolBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        title.setText(R.string.verify_msg);
        toolbar.setBackgroundColor(Color.WHITE);
        title.setTextColor(Color.BLACK);
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


    class VerifyAdapter extends CommonAdapter<VerifyMessageItem> {

        public VerifyAdapter(Context context, int itemLayoutId) {
            super(context, itemLayoutId);
        }

        @Override
        public void convert(CommonViewHolder holder, final VerifyMessageItem item, final int position) {
            if (item.getNickname() != null && !item.getNickname().isEmpty()) {
                holder.setText(R.id.tv_name, item.getNickname());
            } else {
                holder.setText(R.id.tv_name, item.getOtherMobile());
            }
            final StringBuilder verifyMsg = new StringBuilder(getString(R.string.verify_msg));
            verifyMsg.append(": ");
            verifyMsg.append(item.getRequestContent());
            holder.setText(R.id.tv_verifyMsg, verifyMsg.toString());

            final ImageView ivHeadImg = holder.getView(R.id.iv_headImg);
            Glide.with(mContext)
                    .load(item.getIcon())
                    .asBitmap()
                    .placeholder(R.drawable.icon_default_headimage)
                    .centerCrop()
                    .into(new BitmapImageViewTarget(ivHeadImg) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            ivHeadImg.setImageDrawable(circularBitmapDrawable);

                        }
                    });

            if (item.getStatus() == 2) {//已通过验证
                holder.setText(R.id.tv_btn_add, R.string.added);
                holder.getView(R.id.tv_btn_add).setEnabled(false);
            } else if (item.getStatus() == 1) {//等待验证
                holder.setText(R.id.tv_btn_add, R.string.append);
                holder.getView(R.id.tv_btn_add).setEnabled(true);
                holder.getView(R.id.tv_btn_add).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        infoManager.acceptUserVerify(item.getID(), new FriendInfoManager.LeaveMsgListener() {
                            @Override
                            public void onSuccess() {
                                verifyList.get(position).setStatus(2);
                                mAdapter.loadData(verifyList);
                                int waitNum = 0;
                                for (VerifyMessageItem item : verifyList) {
                                    if (item.getStatus() != 2) {
                                        waitNum++;
                                    }
                                }
                                if (waitNum > 0) {
                                    CenterNotification.setCenterNotify(DealVerifyActivity.this, CenterNotification.NewFriendVF);
                                } else {
                                    CenterNotification.resetCenterNotify(DealVerifyActivity.this, CenterNotification.DealNewFriendVF);
                                }
                            }

                            @Override
                            public void onFail(String msg) {
                                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }
    }
}
