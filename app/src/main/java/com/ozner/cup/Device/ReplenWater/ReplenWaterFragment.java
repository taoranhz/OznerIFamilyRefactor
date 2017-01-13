package com.ozner.cup.Device.ReplenWater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.ozner.WaterReplenishmentMeter.WaterReplenishmentMeter;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.OznerDeviceSettings;
import com.ozner.cup.Device.DeviceFragment;
import com.ozner.cup.Main.MainActivity;
import com.ozner.cup.R;
import com.ozner.cup.Utils.LCLogUtils;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class ReplenWaterFragment extends DeviceFragment {
    private static final String TAG = "ReplenWaterFragment";
    @InjectView(R.id.tv_connectState)
    TextView tvConnectState;
    @InjectView(R.id.tv_replen_click_tips)
    TextView tvReplenClickTips;
    @InjectView(R.id.iv_clickImg)
    ImageView ivClickImg;
    @InjectView(R.id.iv_battery_icon)
    ImageView ivBatteryIcon;
    @InjectView(R.id.tv_battery_value)
    TextView tvBatteryValue;
    @InjectView(R.id.iv_inTesting)
    ImageView ivInTesting;
    @InjectView(R.id.tv_inTesting)
    TextView tvInTesting;
    @InjectView(R.id.tv_skin_state)
    TextView tvSkinState;
    @InjectView(R.id.tv_skin_value)
    TextView tvSkinValue;
    @InjectView(R.id.llay_skin_value)
    LinearLayout llaySkinValue;
    @InjectView(R.id.tv_last_value)
    TextView tvLastValue;
    @InjectView(R.id.tv_skin_average)
    TextView tvSkinAverage;
    @InjectView(R.id.tv_skin_notice)
    TextView tvSkinNotice;
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.rlay_inTest)
    RelativeLayout rlayInTest;
    @InjectView(R.id.tv_null_skin_btn)
    TextView tvNullSkinBtn;
    @InjectView(R.id.llay_skin_detail)
    LinearLayout llaySkinDetail;
    @InjectView(R.id.tv_lowPowerTip)
    TextView tvLowPowerTip;

    private WaterReplenishmentMeter replenWater;
    private OznerDeviceSettings oznerSetting;
    private String mUserid;
    private ReplenMonitor mMonitor;
    private boolean isWaitTest = false;
    private int gender = 0;

    private List<RectF> manClikArea = new ArrayList<>();
    private List<RectF> womenClickArea = new ArrayList<>();

    public static DeviceFragment newInstance(String mac) {
        ReplenWaterFragment fragment = new ReplenWaterFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DeviceAddress, mac);
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }


    @Override
    public void setDevice(OznerDevice device) {
        oznerSetting = DBManager.getInstance(getContext()).getDeviceSettings(mUserid, device.Address());
        if (oznerSetting != null) {
            gender = (int) oznerSetting.getAppData(Contacts.DEV_REPLEN_GENDER);
        }
        if (replenWater != null) {
            if (replenWater.Address() != device.Address()) {
                replenWater.release();
                replenWater = null;
                replenWater = (WaterReplenishmentMeter) device;
            }
        } else {
            replenWater = (WaterReplenishmentMeter) device;
        }
        refreshUIData();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        initAnimation();
        mUserid = UserDataPreference.GetUserData(getContext(), UserDataPreference.UserId, "");
        try {
            Bundle bundle = getArguments();
            replenWater = (WaterReplenishmentMeter) OznerDeviceManager.Instance().getDevice(bundle.getString(DeviceAddress));
            oznerSetting = DBManager.getInstance(getContext()).getDeviceSettings(mUserid, replenWater.Address());
            if (oznerSetting != null) {
                gender = (int) oznerSetting.getAppData(Contacts.DEV_REPLEN_GENDER);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "onCreate_Ex: " + ex.getMessage());
        }

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_replen_water, container, false);
        ButterKnife.inject(this, view);
        toolbar.setTitle("");
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);

        ivClickImg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (gender == 0) {
                        if (womenClickArea.size() == 0) {
                            initWomenClickArea(v);
                        }
                    } else {
                        if (manClikArea.size() == 0) {
                            initManClickArea(v);
                        }
                    }

                    if (isWaitTest) {
                        resetView();
                    } else {
                        showWaitTest(generateClickPos(event.getX(), event.getY()));
                    }
                }
                return true;
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).initActionBarToggle(toolbar);
        resetView();
//        showTesting();
//        showWaitTest();
//        showTestValue();
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * 设置toolbar背景色
     *
     * @param resId
     */
    protected void setToolbarColor(int resId) {
        if (isAdded())
            toolbar.setBackgroundColor(ContextCompat.getColor(getContext(), resId));
    }

    @Override
    public void onResume() {
        try {
            setBarColor(R.color.replen_blue_bg);
            setToolbarColor(R.color.replen_blue_bg);
        } catch (Exception ex) {

        }
        title.setText(getString(R.string.water_replen_meter));
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }


    @Override
    protected void refreshUIData() {
        showPowerState();
    }

    /**
     * 显示电池状态
     */
    private void showPowerState() {
        if (replenWater != null && isAdded()) {
            if (replenWater.status().power()) {
                int batteryValue = Math.round(replenWater.status().battery() * 100);
                Log.e(TAG, "showPowerState: batteryValue:" + batteryValue + " ,orgVlue;" + replenWater.status().battery());
                Log.e(TAG, "showPowerState:toString: " + replenWater.toString());
                //设置电池电量图标
                tvLowPowerTip.setVisibility(View.GONE);
                if (batteryValue == 100) {
                    ivBatteryIcon.setImageResource(R.drawable.battery100);
                } else if (batteryValue >= 50 && batteryValue < 100) {
                    ivBatteryIcon.setImageResource(R.drawable.battery70);
                } else if (batteryValue > 0 && batteryValue < 50) {
                    ivBatteryIcon.setImageResource(R.drawable.battery30);
                }
                if (batteryValue >= 0 && batteryValue < 15) {
                    tvLowPowerTip.setVisibility(View.VISIBLE);
                }

                //设置电量值，并设置没电时的电量图标
                if (batteryValue > 0) {
                    tvBatteryValue.setText(String.valueOf(batteryValue) + "%");
                } else {
                    ivBatteryIcon.setImageResource(R.drawable.battery0);
                    tvBatteryValue.setText(R.string.state_null);
                }
            }else {
                ivBatteryIcon.setImageResource(R.drawable.battery0);
                tvBatteryValue.setText(R.string.state_null);
                LCLogUtils.E(TAG,"showPowerState: off");
            }
        } else {
            LCLogUtils.E(TAG, "showPowerState: replen is null");
        }
    }

    /**
     * 初始化男士点击坐标区域
     *
     * @param clickView
     */
    private void initManClickArea(View clickView) {
//        int[] viewSize = getMeasuredWidth(clickView);
        manClikArea.clear();
        //点击脸部区域
        RectF faceRect = new RectF();
        faceRect.left = clickView.getWidth() * 0.33f;
        faceRect.top = clickView.getHeight() * 0.47f;
        faceRect.right = clickView.getWidth() * 0.44f;
        faceRect.bottom = clickView.getHeight() * 0.577f;
        manClikArea.add(faceRect);
        //点击眼部区域
        RectF eyeRect = new RectF();
        eyeRect.left = clickView.getWidth() * 0.528f;
        eyeRect.top = clickView.getHeight() * 0.423f;
        eyeRect.right = clickView.getWidth() * 0.619f;
        eyeRect.bottom = clickView.getHeight() * 0.513f;
        manClikArea.add(eyeRect);
        //点击手部区域
        RectF handRect = new RectF();
        handRect.left = clickView.getWidth() * 0.3f;
        handRect.top = clickView.getHeight() * 0.73f;
        handRect.right = clickView.getWidth() * 0.4f;
        handRect.bottom = clickView.getHeight() * 0.827f;
        manClikArea.add(handRect);
        //点击脖子区域
        RectF neckRect = new RectF();
        neckRect.left = clickView.getWidth() * 0.56f;
        neckRect.top = clickView.getHeight() * 0.637f;
        neckRect.right = clickView.getWidth() * 0.66f;
        neckRect.bottom = clickView.getHeight() * 0.73f;
        manClikArea.add(neckRect);
    }

    /**
     * 初始化女士点击坐标区域
     *
     * @param clickView
     */
    private void initWomenClickArea(View clickView) {
//        int[] viewSize = getMeasuredWidth(clickView);
        Log.e(TAG, "initWomenClickArea: sizeWidth:" + clickView.getWidth() + " ,sizeHight:" + clickView.getHeight());
        womenClickArea.clear();
        //点击脸部区域
        RectF faceRect = new RectF();
        faceRect.left = clickView.getWidth() * 0.328f;
        faceRect.top = clickView.getHeight() * 0.506f;
        faceRect.right = clickView.getWidth() * 0.421f;
        faceRect.bottom = clickView.getHeight() * 0.59f;
        womenClickArea.add(faceRect);
        //点击眼部区域
        RectF eyeRect = new RectF();
        eyeRect.left = clickView.getWidth() * 0.522f;
        eyeRect.top = clickView.getHeight() * 0.437f;
        eyeRect.right = clickView.getWidth() * 0.617f;
        eyeRect.bottom = clickView.getHeight() * 0.526f;
        womenClickArea.add(eyeRect);
        //点击手部区域
        RectF handRect = new RectF();
        handRect.left = clickView.getWidth() * 0.332f;
        handRect.top = clickView.getHeight() * 0.757f;
        handRect.right = clickView.getWidth() * 0.421f;
        handRect.bottom = clickView.getHeight() * 0.837f;
        womenClickArea.add(handRect);
        //点击脖子区域
        RectF neckRect = new RectF();
        neckRect.left = clickView.getWidth() * 0.506f;
        neckRect.top = clickView.getHeight() * 0.659f;
        neckRect.right = clickView.getWidth() * 0.589f;
        neckRect.bottom = clickView.getHeight() * 0.74f;
        womenClickArea.add(neckRect);
        Log.e(TAG, "initWomenClickArea: " + JSON.toJSONString(womenClickArea));
    }


    /**
     * 计算点击位置
     *
     * @param x
     * @param y
     *
     * @return
     */
    private int generateClickPos(float x, float y) {
        if ((int) oznerSetting.getAppData(Contacts.DEV_REPLEN_GENDER) == 0) {
            //女士
            for (int i = 0; i < womenClickArea.size(); i++) {
                if (womenClickArea.get(i).contains(x, y)) {
                    return i;
                }
            }
        } else {
            //男士
            for (int i = 0; i < manClikArea.size(); i++) {
                if (manClikArea.get(i).contains(x, y)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 重置页面到最初状态
     */
    private void resetView() {
        isWaitTest = false;
        if (ivInTesting.getAnimation() != null && ivInTesting.getAnimation().hasStarted()) {
            ivInTesting.getAnimation().cancel();
        }
        rlayInTest.setVisibility(View.GONE);
        llaySkinDetail.setVisibility(View.GONE);
        tvNullSkinBtn.setVisibility(View.VISIBLE);
        tvReplenClickTips.setVisibility(View.VISIBLE);

        if (gender == 0) {
            ivClickImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.img_women_click));
//            Glide.with(this).load(R.drawable.img_women_click)
//                    .asBitmap()
//                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
//                    .into(ivClickImg);

        } else {
            ivClickImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.img_man_click));
//            Glide.with(this).load(R.drawable.img_man_click)
//                    .asBitmap()
//                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
//                    .into(ivClickImg);
        }
    }

    /**
     * 显示正在检测
     */
    private void showInTesting() {
        if (ivInTesting.getAnimation() == null) {
            ivInTesting.setAnimation(rotateAnimation);
        }
        tvReplenClickTips.setVisibility(View.GONE);
        llaySkinDetail.setVisibility(View.GONE);
        llaySkinValue.setVisibility(View.GONE);
        tvNullSkinBtn.setVisibility(View.VISIBLE);
        rlayInTest.setVisibility(View.VISIBLE);
        ivInTesting.setVisibility(View.VISIBLE);
        tvInTesting.setVisibility(View.VISIBLE);
        tvInTesting.setText(R.string.in_test);
        if (ivInTesting.getAnimation() != null && !ivInTesting.getAnimation().hasStarted()) {
            ivInTesting.getAnimation().start();
        }
    }

    /**
     * 显示等待检测
     */
    private void showWaitTest(int clickPos) {
        if (clickPos >= 0 && clickPos < 4) {
            isWaitTest = true;
            if (ivInTesting.getAnimation() != null && ivInTesting.getAnimation().hasStarted()) {
                ivInTesting.getAnimation().cancel();
            }
            ivInTesting.setVisibility(View.GONE);
            llaySkinDetail.setVisibility(View.GONE);
            llaySkinValue.setVisibility(View.GONE);
            tvReplenClickTips.setVisibility(View.GONE);
            tvInTesting.setVisibility(View.VISIBLE);
            rlayInTest.setVisibility(View.VISIBLE);
            tvNullSkinBtn.setVisibility(View.VISIBLE);

            switch (clickPos) {
                case 0:
                    tvInTesting.setText(R.string.replen_test_face_tips);
                    if (gender == 0) {
//                        Glide.with(this).load(R.drawable.img_women_face_click_tip).asBitmap().diskCacheStrategy(DiskCacheStrategy.RESULT).into(ivClickImg);
                        ivClickImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.img_women_face_click_tip));
                    } else {
//                        Glide.with(this).load(R.drawable.img_man_face_click_tip).asBitmap().diskCacheStrategy(DiskCacheStrategy.RESULT).into(ivClickImg);
                        ivClickImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.img_man_face_click_tip));
                    }
                    break;
                case 1:
                    tvInTesting.setText(R.string.replen_test_eye_tips);
                    if (gender == 0) {
//                        Glide.with(this).load(R.drawable.img_women_eye_click_tip).asBitmap().diskCacheStrategy(DiskCacheStrategy.RESULT).into(ivClickImg);
                        ivClickImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.img_women_eye_click_tip));
                    } else {
//                        Glide.with(this).load(R.drawable.img_man_eye_click_tip).asBitmap().diskCacheStrategy(DiskCacheStrategy.RESULT).into(ivClickImg);
                        ivClickImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.img_man_eye_click_tip));
                    }
                    break;
                case 2:
                    tvInTesting.setText(R.string.replen_test_hand_tips);
                    if (gender == 0) {
//                        Glide.with(this).load(R.drawable.img_women_hand_click_tip).asBitmap().diskCacheStrategy(DiskCacheStrategy.RESULT).into(ivClickImg);
                        ivClickImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.img_women_hand_click_tip));
                    } else {
//                        Glide.with(this).load(R.drawable.img_man_hand_click_tip).asBitmap().diskCacheStrategy(DiskCacheStrategy.RESULT).into(ivClickImg);
                        ivClickImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.img_man_hand_click_tip));
                    }
                    break;
                case 3:
                    tvInTesting.setText(R.string.replen_test_neck_tips);
                    if (gender == 0) {
//                        Glide.with(this).load(R.drawable.img_women_neck_click_tips).asBitmap().diskCacheStrategy(DiskCacheStrategy.RESULT).into(ivClickImg);
                        ivClickImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.img_women_neck_click_tips));
                    } else {
//                        Glide.with(this).load(R.drawable.img_man_neck_click_tip).asBitmap().diskCacheStrategy(DiskCacheStrategy.RESULT).into(ivClickImg);
                        ivClickImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.img_man_neck_click_tip));
                    }
                    break;
            }
        }
    }

    /**
     * 显示检测结果
     */
    private void showTestValue() {
        if (ivInTesting.getAnimation() != null && ivInTesting.getAnimation().hasStarted()) {
            ivInTesting.getAnimation().cancel();
        }
        tvReplenClickTips.setVisibility(View.GONE);
        ivInTesting.setVisibility(View.GONE);
        tvInTesting.setVisibility(View.GONE);
        tvNullSkinBtn.setVisibility(View.GONE);
        rlayInTest.setVisibility(View.VISIBLE);
        llaySkinDetail.setVisibility(View.VISIBLE);
        llaySkinValue.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStart() {
        registerMonitor();
        super.onStart();
    }


    @Override
    public void onStop() {
        releaseMonitor();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @OnClick({R.id.iv_setting, R.id.tv_null_skin_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_setting:
                break;
            case R.id.tv_null_skin_btn:
                break;
        }
    }

    /**
     * 注册广播监听
     */
    private void registerMonitor() {
        mMonitor = new ReplenMonitor();
        IntentFilter filter = new IntentFilter();
        filter.addAction(OznerDevice.ACTION_DEVICE_UPDATE);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_CONNECTED);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_CONNECTING);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_DISCONNECTED);
        getContext().registerReceiver(mMonitor, filter);
    }

    /**
     * 注销广播监听
     */
    private void releaseMonitor() {
        try {
            if (!isDetached()) {
                getContext().unregisterReceiver(mMonitor);
            }
        } catch (Exception ex) {

        }
    }


    @Override
    public void onDetach() {
        System.gc();
        super.onDetach();
    }


    class ReplenMonitor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
//            Log.e(TAG, "onReceive: " + mCup.toString());
            refreshUIData();
        }
    }
}
