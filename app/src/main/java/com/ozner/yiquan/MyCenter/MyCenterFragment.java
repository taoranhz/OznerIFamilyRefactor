package com.ozner.yiquan.MyCenter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.ozner.yiquan.Base.BaseFragment;
import com.ozner.yiquan.Base.WebActivity;
import com.ozner.yiquan.Bean.Contacts;
import com.ozner.yiquan.Command.CenterNotification;
import com.ozner.yiquan.Command.OznerPreference;
import com.ozner.yiquan.Command.UserDataPreference;
import com.ozner.yiquan.DBHelper.DBManager;
import com.ozner.yiquan.DBHelper.UserInfo;
import com.ozner.yiquan.LoginWelcom.View.LoginActivity;
import com.ozner.yiquan.Main.MainActivity;
import com.ozner.yiquan.Main.UserInfoManager;
import com.ozner.yiquan.MyCenter.MyFriend.MyFriendsActivity;
import com.ozner.yiquan.MyCenter.Settings.SettingsActivity;
import com.ozner.yiquan.R;
import com.ozner.yiquan.Utils.LCLogUtils;
import com.ozner.yiquan.Utils.WeChatUrlUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.ozner.yiquan.R.id.tv_myMoney;

/**
 * Created by ozner_67 on 2016/11/1.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class MyCenterFragment extends BaseFragment {
    private static final String TAG = "MyCenterFragment";

    @InjectView(R.id.iv_headImg)
    ImageView ivHeadImg;
    @InjectView(R.id.tv_name)
    TextView tvName;
    @InjectView(R.id.tv_grade)
    TextView tvGrade;
    @InjectView(R.id.tv_myDevice)
    TextView tvMyDevice;
    @InjectView(tv_myMoney)
    TextView tvMyMoney;
    @InjectView(R.id.tv_newFriendNum)
    TextView tvNewFriendNum;

    private UserInfoManager userInfoManager;
    private String userid;
    private UserInfo mUserInfo;
    private String userToken;

    public MyCenterFragment() {
        // Required empty public constructor
    }

    /**
     * 实例化Fragment
     *
     * @param bundle
     *
     * @return
     */
    public static MyCenterFragment newInstance(Bundle bundle) {
        MyCenterFragment fragment = new MyCenterFragment();
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        userToken = OznerPreference.getUserToken(getContext());
        userInfoManager = new UserInfoManager(getContext());
        userid = UserDataPreference.GetUserData(getContext(), UserDataPreference.UserId, "");
        mUserInfo = DBManager.getInstance(getContext()).getUserInfo(userid);
        userInfoManager.loadUserNickImage(mUserInfo, new UserInfoManager.LoadUserInfoListener() {
            @Override
            public void onSuccess(UserInfo userInfo) {
                Log.e(TAG, "onSuccess: " + mUserInfo.toString());
                mUserInfo = userInfo;
                if (isResumed()) {
                    showUserInfo(mUserInfo);
                }
            }

            @Override
            public void onFail(String msg) {
                Log.e(TAG, "onFail: " + msg);
            }
        });
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_center, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (mUserInfo != null) {
            showUserInfo(mUserInfo);
        }
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * 刷新我的消息记录
     */
    private void refreshCenterMsgCount() {
        int centerNotify = CenterNotification.getCenterNotifyState(getContext());
        if (centerNotify > 0) {
            tvNewFriendNum.setVisibility(View.VISIBLE);
        } else {
            tvNewFriendNum.setVisibility(View.GONE);
        }
        ((MainActivity) getActivity()).setNewCenterMsgTip(centerNotify);
    }

    public void showUserInfo(UserInfo userinfo) {
        if (isAdded()) {
            if (userinfo.getNickname() != null && !mUserInfo.getNickname().isEmpty()) {
                tvName.setText(mUserInfo.getNickname());
            }
            if (userinfo.getGradeName() != null && !mUserInfo.getGradeName().isEmpty()) {
                String gradeName = mUserInfo.getGradeName();
                if (gradeName.contains("会员")) {
                    int index = gradeName.indexOf("会员");
                    gradeName = gradeName.substring(0, index);
                }
                gradeName += getString(R.string.act_member);
//                tvGrade.setVisibility(View.VISIBLE);
                tvGrade.setText(gradeName);
            }
            LCLogUtils.E(TAG, "score:" + userinfo.getScore());
            if (userinfo.getScore() != null)
                tvMyMoney.setText(String.valueOf(userinfo.getScore()));
            else
                tvMyMoney.setText("0");

            Glide.with(getContext()).load(mUserInfo.getHeadimg()).asBitmap().placeholder(R.drawable.icon_default_headimage).centerCrop().into(new BitmapImageViewTarget(ivHeadImg) {
                @Override
                protected void setResource(Bitmap resource) {
                    if (MyCenterFragment.this.isAdded()) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        ivHeadImg.setImageDrawable(circularBitmapDrawable);
                    }
                }
            });
        }
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        System.gc();
        super.onDetach();
    }

    @Override
    public void onResume() {
        try {
            setBarColor(R.color.cup_detail_bg);
            tvMyDevice.setText(String.valueOf(DBManager.getInstance(getContext()).getDeviceSettingList(userid).size()));
            refreshCenterMsgCount();
        } catch (Exception ex) {

        }
        super.onResume();
    }

    /**
     * 设置状态栏颜色
     */
    protected void setBarColor(int resId) {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = ((MainActivity) getActivity()).getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(getContext(), resId));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @OnClick({R.id.llay_headImg, R.id.llay_myDevice, R.id.llay_myBurse, R.id.rlay_my_order, R.id.rlay_redbag, R.id.rlay_my_ticket, R.id.rlay_my_friend, R.id.rlay_report, R.id.rlay_feedback, R.id.rlay_settings})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.llay_headImg:
                if (userid == null || userid.isEmpty()) {
                    startActivity(new Intent(getContext(), LoginActivity.class));
                    getActivity().finish();
                }
                break;
            case R.id.llay_myDevice:
                startActivity(new Intent(getContext(), MyDeviceActivity.class));
                break;
            case R.id.llay_myBurse:
                Intent burseIntent = new Intent(getContext(), WebActivity.class);
                String burseUrl = WeChatUrlUtil.formatMyMoneyUrl(mUserInfo.getMobile(), userToken, "zh", "zh");
                LCLogUtils.E(TAG, "burseUrl:" + burseUrl);
                burseIntent.putExtra(Contacts.PARMS_URL, burseUrl);
                startActivity(burseIntent);
                break;
            case R.id.rlay_my_order:
                Intent orderIntent = new Intent(getContext(), WebActivity.class);
                String orderUrl = WeChatUrlUtil.formatMyOrderUrl(mUserInfo.getMobile(), userToken, "zh", "zh");
                LCLogUtils.E(TAG, "OrderUrl:" + orderUrl);
                orderIntent.putExtra(Contacts.PARMS_URL, orderUrl);
                startActivity(orderIntent);
                break;
            case R.id.rlay_redbag:
                Intent redBagIntnet = new Intent(getContext(), WebActivity.class);
                String redPacUrl = WeChatUrlUtil.formatRedPacUrl(mUserInfo.getMobile(), userToken, "zh", "zh");
                LCLogUtils.E(TAG, "redPacUrl:" + redPacUrl);
                redBagIntnet.putExtra(Contacts.PARMS_URL, redPacUrl);
                startActivity(redBagIntnet);
                break;
            case R.id.rlay_my_ticket:
                Intent ticketIntent = new Intent(getContext(), WebActivity.class);
                String myTicketUrl = WeChatUrlUtil.formatMyTicketUrl(mUserInfo.getMobile(), userToken, "zh", "zh");
                LCLogUtils.E(TAG, "myTicketUrl:" + myTicketUrl);
                ticketIntent.putExtra(Contacts.PARMS_URL, myTicketUrl);
                startActivity(ticketIntent);
                break;
            case R.id.rlay_my_friend:
                startActivity(new Intent(getContext(), MyFriendsActivity.class));
                break;
            case R.id.rlay_report:
                Intent reportIntent = new Intent(getContext(), WebActivity.class);
                String reportUrl = String.format(Contacts.Water_Analysis, mUserInfo.getMobile());
                reportIntent.putExtra(Contacts.PARMS_URL, reportUrl);
                startActivity(reportIntent);
                break;
            case R.id.rlay_feedback://我要提意见
                startActivity(new Intent(getContext(), FeedBackActivity.class));
                break;
            case R.id.rlay_settings:
                startActivity(new Intent(getContext(), SettingsActivity.class));
                break;
        }
    }

}
