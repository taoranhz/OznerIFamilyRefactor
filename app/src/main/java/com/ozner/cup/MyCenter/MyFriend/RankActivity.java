package com.ozner.cup.MyCenter.MyFriend;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Base.CommonAdapter;
import com.ozner.cup.Base.CommonViewHolder;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Bean.RankType;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.R;
import com.ozner.cup.Utils.LCLogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class RankActivity extends BaseActivity {
    private static final String TAG = "RankActivity";

    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.lv_rank)
    ListView lvRank;
    @InjectView(R.id.tv_data_empty)
    TextView tvDataEmpty;
    private String mRankType;
    private RankAdapter mAdapter;
    private String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);
        ButterKnife.inject(this);
        initToolBar();
        try {
            userid = UserDataPreference.GetUserData(this, UserDataPreference.UserId, "");
            mRankType = getIntent().getStringExtra(Contacts.PARMS_RANK_TYPE);
            initTitle(mRankType);
            lvRank.setEmptyView(tvDataEmpty);
            if (!mRankType.equals(RankType.CupVolumType)) {
                mAdapter = new RankAdapter(this, R.layout.center_rank_tds_item);
            } else {
                mAdapter = new RankAdapter(this, R.layout.center_rank_volume_item);
            }
            lvRank.setAdapter(mAdapter);
            LCLogUtils.E(TAG, "mRankType:" + mRankType);
        } catch (Exception ex) {
            LCLogUtils.E(TAG, "onCreate_Ex:" + ex.getMessage());
        }

        initTestData();
    }

    /**
     * 初始化ToolBar
     */
    private void initToolBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
    }

    private void initTestData() {
        Random random = new Random();
        List<CenterRankItem> dataList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            CenterRankItem item = new CenterRankItem();
            item.setUserid(userid);
            item.setIcon("http://d.hiphotos.baidu.com/zhidao/pic/item/bf096b63f6246b60474b7d5fe8f81a4c510fa250.jpg");
            item.setIsLike(i % 2);
            item.setLikeCount(i * 4 + 3);
//            item.setNickname("凌晨 " + i);
            item.setMobile("13166398575");
            item.setRank(i + 1);
            item.setVolume(random.nextInt(400));
            dataList.add(item);
        }
        mAdapter.loadData(dataList);
    }

    /**
     * 加载标题
     *
     * @param type
     */
    private void initTitle(String type) {
        switch (type) {
            case RankType.CupType:
                title.setText(R.string.cup_tds_rank);
                break;
            case RankType.CupVolumType:
                title.setText(R.string.cup_volume_rank);
                break;
            case RankType.WaterType:
                title.setText(R.string.water_purifier_tds_rank);
                break;
            case RankType.TapType:
                title.setText(R.string.tap_tds_rank);
                break;
            default:
                title.setText(R.string.cup_tds_rank);
                break;
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

    class RankAdapter extends CommonAdapter<CenterRankItem> {

        public RankAdapter(Context context, int itemLayoutId) {
            super(context, itemLayoutId);
        }

        @Override
        public void convert(CommonViewHolder holder, CenterRankItem item, int position) {
            holder.setText(R.id.tv_rankValue, String.valueOf(item.getRank()));
            switch (item.getRank()) {
                case 1:
                    holder.setTextColor(R.id.tv_rankValue, ContextCompat.getColor(mContext, R.color.faq_text_blue));
                    holder.setImageResource(R.id.iv_crown, R.drawable.crown_1);
                    holder.getView(R.id.iv_crown).setVisibility(View.VISIBLE);
                    break;
                case 2:
                    holder.setTextColor(R.id.tv_rankValue, ContextCompat.getColor(mContext, R.color.faq_text_blue));
                    holder.setImageResource(R.id.iv_crown, R.drawable.crown_2);
                    holder.getView(R.id.iv_crown).setVisibility(View.VISIBLE);
                    break;
                case 3:
                    holder.setTextColor(R.id.tv_rankValue, ContextCompat.getColor(mContext, R.color.faq_text_blue));
                    holder.setImageResource(R.id.iv_crown, R.drawable.crown_3);
                    holder.getView(R.id.iv_crown).setVisibility(View.VISIBLE);
                    break;
                default:
                    holder.setTextColor(R.id.tv_rankValue, Color.GRAY);
                    holder.getView(R.id.iv_crown).setVisibility(View.GONE);
                    break;
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

            if (item.getNickname() != null && !item.getNickname().isEmpty()) {
                holder.setText(R.id.tv_name, item.getNickname());
            } else {
                holder.setText(R.id.tv_name, item.getMobile());
            }

            holder.setText(R.id.tv_value, String.valueOf(item.getVolume()));
//            holder.setText(R.id.pb_Value, String.valueOf(item.getVolume() / 4));
            ((ProgressBar)holder.getView(R.id.pb_Value)).setProgress(item.getVolume()/4);
            holder.setText(R.id.tv_lickNum, String.valueOf(item.getLikeCount()));
            if (item.getIsLike() == 1) {
                holder.setImageResource(R.id.iv_likeImg, R.drawable.heart_red);
            } else {
                holder.setImageResource(R.id.iv_likeImg, R.drawable.heart_gray);
            }

        }
    }
}
