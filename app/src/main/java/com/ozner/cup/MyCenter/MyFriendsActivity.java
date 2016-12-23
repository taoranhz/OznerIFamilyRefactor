package com.ozner.cup.MyCenter;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MyFriendsActivity extends BaseActivity {

    @InjectView(R.id.tv_friend_rank)
    TextView tvFriendRank;
    @InjectView(R.id.tv_rank_bottom)
    TextView tvRankBottom;
    @InjectView(R.id.tv_my_friend)
    TextView tvMyFriend;
    @InjectView(R.id.tv_friend_bottom)
    TextView tvFriendBottom;
    @InjectView(R.id.iv_newMsg)
    ImageView ivNewMsg;
    @InjectView(R.id.iv_newMsgTips)
    ImageView ivNewMsgTips;
    @InjectView(R.id.iv_add_friend)
    ImageView ivAddFriend;
    @InjectView(R.id.llay_friend_right)
    LinearLayout llayFriendRight;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.flay_container)
    FrameLayout flayContainer;
    FriendRankFragment rankFragment;
    FriendsFragment friendsFragment;
    FragmentTransaction framTrans;
    private boolean isRank = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_friends);
        ButterKnife.inject(this);
        framTrans = getSupportFragmentManager().beginTransaction();
        initView();
    }

    /**
     * 初始化ToolBar
     */
    private void initToolBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
    }

    /**
     * 初始化界面
     */
    private void initView() {
        initToolBar();
        setRankSelected();
    }

    @OnClick({R.id.llay_friend_rank, R.id.rlay_my_friend})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.llay_friend_rank:
                setRankSelected();
                break;
            case R.id.rlay_my_friend:
                setFriendSelected();
                break;
        }
    }


    /**
     * 选中好友排名
     */
    private void setRankSelected() {
        if (!isRank) {
            isRank = true;
            tvRankBottom.setVisibility(View.VISIBLE);
            tvFriendRank.setSelected(true);
            tvMyFriend.setSelected(false);
            tvFriendBottom.setVisibility(View.INVISIBLE);
            llayFriendRight.setVisibility(View.INVISIBLE);
            if (rankFragment == null) {
                rankFragment = FriendRankFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.flay_container, rankFragment).commitAllowingStateLoss();
        }
    }

    /**
     * 选中我的好友
     */
    private void setFriendSelected() {
        if (isRank) {
            isRank = false;
            tvRankBottom.setVisibility(View.INVISIBLE);
            tvFriendRank.setSelected(false);
            tvMyFriend.setSelected(true);
            tvFriendBottom.setVisibility(View.VISIBLE);
            llayFriendRight.setVisibility(View.VISIBLE);
            if (friendsFragment == null) {
                friendsFragment = FriendsFragment.newInstance(null);
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.flay_container, friendsFragment).commitAllowingStateLoss();
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
}
