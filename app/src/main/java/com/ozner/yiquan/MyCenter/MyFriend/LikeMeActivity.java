package com.ozner.yiquan.MyCenter.MyFriend;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.ozner.yiquan.Base.BaseActivity;
import com.ozner.yiquan.Base.CommonAdapter;
import com.ozner.yiquan.Base.CommonViewHolder;
import com.ozner.yiquan.Bean.Contacts;
import com.ozner.yiquan.Bean.RankType;
import com.ozner.yiquan.Command.UserDataPreference;
import com.ozner.yiquan.MyCenter.MyFriend.bean.LikeMeItem;
import com.ozner.yiquan.R;
import com.ozner.yiquan.Utils.DateUtils;
import com.ozner.yiquan.Utils.LCLogUtils;

import java.util.Calendar;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LikeMeActivity extends BaseActivity {
    private static final String TAG = "LikeMeActivity";

    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.lv_rank)
    ListView lvRank;
    @InjectView(R.id.tv_data_empty)
    TextView tvDataEmpty;
    String mType;
    LikeMeAdapter mAdapter;
    private String userid;
    private FriendInfoManager infoManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_me);
        ButterKnife.inject(this);
        initToolBar();
        mAdapter = new LikeMeAdapter(this, R.layout.like_me_item);
        lvRank.setEmptyView(tvDataEmpty);
        lvRank.setAdapter(mAdapter);
        infoManager = new FriendInfoManager(this, null);
        try {
            userid = UserDataPreference.GetUserData(this, UserDataPreference.UserId, "");
            mType = getIntent().getStringExtra(Contacts.PARMS_RANK_TYPE);
            LCLogUtils.E(TAG, "mType:" + mType);
        } catch (Exception ex) {
            mType = RankType.CupType;
            LCLogUtils.E(TAG, "onCreate_Ex: " + ex.getMessage());
        }

//        initTestData();
        infoManager.getWhoLikeMe(mType, new FriendInfoManager.LikeMeListener() {
            @Override
            public void onSuccess(List<LikeMeItem> result) {
                mAdapter.loadData(result);
            }

            @Override
            public void onFail(String msg) {
                LCLogUtils.E(TAG, msg);
            }
        });

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


    class LikeMeAdapter extends CommonAdapter<LikeMeItem> {
        Calendar cal;

        public LikeMeAdapter(Context context, int itemLayoutId) {
            super(context, itemLayoutId);
            cal = Calendar.getInstance();
        }

        @Override
        public void convert(CommonViewHolder holder, LikeMeItem item, int position) {
            if (item.getNickname() != null && !item.getNickname().isEmpty()) {
                holder.setText(R.id.tv_name, item.getNickname());
            } else {
                holder.setText(R.id.tv_name, item.getMobile());
            }
            try {
                long timeInMills = DateUtils.formatDateFromString(item.getLiketime());
                cal.setTimeInMillis(timeInMills);
                int month = cal.get(Calendar.MONTH) + 1;
                int day = cal.get(Calendar.DAY_OF_MONTH);
                holder.setText(R.id.tv_time, String.format(mContext.getString(R.string.montyDayTime), month, day));
            } catch (Exception ex) {
                holder.setText(R.id.tv_time, R.string.unknown);
            }


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
        }
    }

}
