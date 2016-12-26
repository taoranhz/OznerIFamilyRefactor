package com.ozner.cup.MyCenter;

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
import com.ozner.cup.Base.BaseFragment;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.UserInfo;
import com.ozner.cup.Main.MainActivity;
import com.ozner.cup.Main.UserInfoManager;
import com.ozner.cup.MyCenter.MyFriend.MyFriendsActivity;
import com.ozner.cup.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

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
    @InjectView(R.id.tv_myMoney)
    TextView tvMyMoney;

    UserInfoManager userInfoManager;
    String userid;
    UserInfo mUserInfo;

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
                tvGrade.setVisibility(View.VISIBLE);
                tvGrade.setText(gradeName);
            }
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
//            setToolbarColor(R.color.colorAccent);
//            ((MainActivity) getActivity()).setCustomTitle(R.string.mine);

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

                break;
            case R.id.llay_myDevice:
                break;
            case R.id.llay_myBurse:
                break;
            case R.id.rlay_my_order:
                break;
            case R.id.rlay_redbag:
                break;
            case R.id.rlay_my_ticket:
                break;
            case R.id.rlay_my_friend:
                startActivity(new Intent(getContext(), MyFriendsActivity.class));
                break;
            case R.id.rlay_report:
                break;
            case R.id.rlay_feedback:
                break;
            case R.id.rlay_settings:
                break;
        }
    }

}
