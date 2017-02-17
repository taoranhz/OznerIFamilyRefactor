package com.ozner.cup.Device.ReplenWater;

import android.app.Activity;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ozner.WaterReplenishmentMeter.WaterReplenishmentMeter;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.OznerDeviceSettings;
import com.ozner.cup.Device.DeviceFragment;
import com.ozner.cup.HttpHelper.HttpMethods;
import com.ozner.cup.HttpHelper.OznerHttpResult;
import com.ozner.cup.HttpHelper.ProgressSubscriber;
import com.ozner.cup.Main.MainActivity;
import com.ozner.cup.R;
import com.ozner.cup.Utils.LCLogUtils;
import com.ozner.cup.Utils.OznerFileImageHelper;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.ozner.cup.R.id.llay_skin_value;


public class ReplenWaterFragment extends DeviceFragment {
    private static final String TAG = "ReplenWaterFragment";
    private final int SetReqCode = 1;
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
    @InjectView(llay_skin_value)
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
    @InjectView(R.id.rlay_skin_value)
    RelativeLayout rlaySkinValue;

    private WaterReplenishmentMeter replenWater;
    private OznerDeviceSettings oznerSetting;
    private String mUserid;
    private String mUserToken;
    private ReplenMonitor mMonitor;
    private boolean isWaitTest = false;
    private int gender = 0;
    private int clickPos = -1;
    /**
     * 开始检测标记shouldCounter = 1，结束检测标记shouldCounter = 7
     * 当同时拥有开始检测标记和检测结果标记时，代表完成一次检测过程，可以计一次数
     * 也就是shouldCounter值为3时，进行计数，
     * 计数结束后将shouldCounter置为7，用来防止误判
     * 当shouldCounter为1或者7时，用来检测时间是否到5秒
     */
    private byte shouldCounter = 0;//是否应该检测计数
    private double lastFaceMoisValue = 0;
    private double lastEyeMoisValue = 0;
    private double lastHandMoisValue = 0;
    private double lastNeckMoisValue = 0;
    private float curFaceMoisValue = 0, curFaceOilValue = 0;
    private float curEyeMoisValue = 0, curEyeOilValue = 0;
    private float curHandMoisValue = 0, curHandOilValue = 0;
    private float curNeckMoisValue = 0, curNeckOilValue = 0;
    private int count = 0;
    private double totalValue = 0;

    private float oilTotal = 0;//油分总值，从网络获取
    private int timeTotal = 0;//油分总次数，从网络获取
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
        oilTotal = 0;
        timeTotal = 0;
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
//        loadBuShuiFenbu();
        refreshUIData();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        initAnimation();
        mUserToken = OznerPreference.getUserToken(getContext());
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
        loadBuShuiFenbu();
        loadTestCount();
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
        tvNullSkinBtn.setText(String.format(getString(R.string.replen_skin_null), getString(R.string.state_null)));

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
                        clickPos = generateClickPos(event.getX(), event.getY());
                        showWaitTest(clickPos);
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
            title.setText(oznerSetting.getName());
//            showSkinStatus();
        } catch (Exception ex) {

        }
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }


    @Override
    protected void refreshUIData() {
        refreshPowerState();
        refreshConnectState();
        refreshTestStatus();
    }

    /**
     * 刷新测试状态
     */
    private void refreshTestStatus() {
        if (replenWater != null) {
            if (isWaitTest) {
                if (replenWater.status().isTesting()) {
                    showInTesting();
                } else if (replenWater.status().testValue().moisture < 0.01) {
                    showWaitTest(clickPos);
                }
                if (replenWater.status().testValue().moisture > 0) {
                    showTestValue();
                }
            }
        }
    }

    /**
     * 刷新电池状态
     */
    private void refreshPowerState() {
        if (isAdded()) {
            if (replenWater != null) {
                if (replenWater.status().power()) {
                    int batteryValue = Math.round(replenWater.status().battery() * 100);
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
                } else {
                    ivBatteryIcon.setImageResource(R.drawable.battery0);
                    tvBatteryValue.setText(R.string.state_null);
//                    LCLogUtils.E(TAG, "refreshPowerState: off");
                }
            } else {
                LCLogUtils.E(TAG, "refreshPowerState: replen is null");
            }
        }
    }

    /**
     * 刷新设备连接状态
     */
    private void refreshConnectState() {
        if (isAdded()) {
            if (replenWater != null) {
                if (replenWater.connectStatus().equals(BaseDeviceIO.ConnectStatus.Connected)) {
                    tvConnectState.setVisibility(View.INVISIBLE);
                } else if (replenWater.connectStatus().equals(BaseDeviceIO.ConnectStatus.Connecting)) {
                    tvConnectState.setVisibility(View.VISIBLE);
                    tvConnectState.setText(R.string.device_connecting);
                } else if (replenWater.connectStatus().equals(BaseDeviceIO.ConnectStatus.Disconnect)) {
                    tvConnectState.setVisibility(View.VISIBLE);
                    tvConnectState.setText(R.string.device_unconnected);
                }
            } else {
                tvConnectState.setVisibility(View.VISIBLE);
                tvConnectState.setText(R.string.Not_found_device);
            }
        }
    }

    /**
     * 初始化男士点击坐标区域
     *
     * @param clickView
     */
    private void initManClickArea(View clickView) {
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
        curFaceMoisValue = 0;
        curEyeMoisValue = 0;
        curHandMoisValue = 0;
        curNeckMoisValue = 0;
        curFaceOilValue = 0;
        curEyeOilValue = 0;
        curHandOilValue = 0;
        curNeckOilValue = 0;
        shouldCounter = 0;
        isWaitTest = false;
        ivInTesting.clearAnimation();
        ivInTesting.setVisibility(View.GONE);
        rlayInTest.setVisibility(View.GONE);
        llaySkinDetail.setVisibility(View.GONE);
        tvNullSkinBtn.setVisibility(View.VISIBLE);
        tvReplenClickTips.setVisibility(View.VISIBLE);
//        tvNullSkinBtn.setText(String.format(getString(R.string.replen_skin_null), getString(R.string.state_null)));
//        showSkinStatus();
        rlayInTest.setBackground(OznerFileImageHelper.readBitDrawable(getContext(), R.drawable.replen_test_blue));

        if (gender == 0) {
            ivClickImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.img_women_click));
        } else {
            ivClickImg.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.img_man_click));
        }
    }

    /**
     * 显示正在检测
     */
    private void showInTesting() {
        if (isAdded()) {
            shouldCounter = 0;
            shouldCounter |= 0x01;//设置开始检测标记
            LCLogUtils.E(TAG, "showInTesting:shouldCounter: " + shouldCounter);
            rlayInTest.setBackground(OznerFileImageHelper.readBitDrawable(getContext(), R.drawable.replen_test_blue));
            tvReplenClickTips.setVisibility(View.GONE);
            llaySkinValue.setVisibility(View.GONE);
            tvNullSkinBtn.setVisibility(View.GONE);
            rlayInTest.setVisibility(View.VISIBLE);
            ivInTesting.setVisibility(View.VISIBLE);
            tvInTesting.setVisibility(View.VISIBLE);
            llaySkinDetail.setVisibility(View.VISIBLE);
            tvInTesting.setText(R.string.in_test);
            if (rotateAnimation != null)
                ivInTesting.startAnimation(rotateAnimation);
        }
    }

    /**
     * 显示等待检测
     */
    private void showWaitTest(int clickPos) {
        if (isAdded()) {
//            rlayInTest.setBackground(OznerFileImageHelper.readBitDrawable(getContext(), R.drawable.replen_test_blue));
            if (clickPos >= 0 && clickPos < 4) {
                isWaitTest = true;
                ivInTesting.clearAnimation();
                ivInTesting.setVisibility(View.GONE);
                llaySkinValue.setVisibility(View.GONE);
                tvNullSkinBtn.setVisibility(View.GONE);
                tvReplenClickTips.setVisibility(View.GONE);
                tvSkinNotice.setVisibility(View.GONE);
                tvInTesting.setVisibility(View.VISIBLE);
                rlayInTest.setVisibility(View.VISIBLE);
                llaySkinDetail.setVisibility(View.VISIBLE);
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
                        showFaceValue();
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
                        showEyeValue();
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
                        showHandValue();
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
                        showNeckValue();
                        break;
                }
            }
        }
    }

    /**
     * 显示检测结果
     */
    private void showTestValue() {
        if (isAdded()) {
            if (shouldCounter == 1)
                shouldCounter |= 0x02;//设置显示检测结果标记
            LCLogUtils.E(TAG, "showTestValue:shouldCounter: " + shouldCounter);
            ivInTesting.clearAnimation();
            tvReplenClickTips.setVisibility(View.GONE);
            ivInTesting.setVisibility(View.GONE);
            tvInTesting.setVisibility(View.GONE);
            tvNullSkinBtn.setVisibility(View.GONE);
            rlayInTest.setVisibility(View.VISIBLE);
            llaySkinDetail.setVisibility(View.VISIBLE);
            llaySkinValue.setVisibility(View.VISIBLE);
            LCLogUtils.E(TAG, "oznerSetting:" + oznerSetting.getSettings());

            if (replenWater != null) {
                switch (clickPos) {
                    case 0://脸
                        showFaceValue();
                        break;
                    case 1://眼
                        showEyeValue();
                        break;
                    case 2://手
                        showHandValue();
                        break;
                    case 3://颈部
                        showNeckValue();
                        break;
                }

            }
        }
    }

    /**
     * 显示脸部数据
     */
    private void showFaceValue() {
        try {
            totalValue = (double) oznerSetting.getAppData(Contacts.DEV_REPLEN_FACE_MOIS_TOTAL);
            count = (int) oznerSetting.getAppData(Contacts.DEV_REPLEN_FACE_TEST_COUNT);
        } catch (Exception ex) {
            count = 0;
            totalValue = 0;
        }
        if (curFaceMoisValue < 0.1) {
            try {
                lastFaceMoisValue = Double.parseDouble(String.valueOf(oznerSetting.getAppData(Contacts.DEV_REPLEN_FACE_LAST_MOIS)));
            } catch (Exception ex) {
                Log.e(TAG, "showFaceValue_Ex: " + ex.getMessage());
                lastFaceMoisValue = 0;
            }
        }
//        LCLogUtils.E(TAG, "face_count:" + count + " ,totalValue:" + totalValue);

        if (shouldCounter == 3) {//可以计数
            shouldCounter = 7;
            if (replenWater.status().testValue().moisture > 0) {
                curFaceMoisValue = replenWater.status().testValue().moisture;
            }
            if (replenWater.status().testValue().oil > 0) {
                curFaceOilValue = replenWater.status().testValue().oil;
            }
            try {
                lastFaceMoisValue = Double.parseDouble(String.valueOf(oznerSetting.getAppData(Contacts.DEV_REPLEN_FACE_LAST_MOIS)));
            } catch (Exception ex) {
                lastFaceMoisValue = 0;
            }
            count++;
            totalValue += curFaceMoisValue;
            oznerSetting.setAppData(Contacts.DEV_REPLEN_FACE_TEST_COUNT, count);
            oznerSetting.setAppData(Contacts.DEV_REPLEN_FACE_LAST_MOIS, curFaceMoisValue);
//            oznerSetting.setAppData(Contacts.DEV_REPLEN_FACE_LAST_OIL, curFaceOilValue);
            oznerSetting.setAppData(Contacts.DEV_REPLEN_FACE_MOIS_TOTAL, totalValue);
            DBManager.getInstance(getContext()).updateDeviceSettings(oznerSetting);

            if (curFaceMoisValue > 0) {
                updateBuShuiYiNumber(String.format("%.2f", curFaceOilValue), String.format("%.2f", curFaceMoisValue), ReplenFenBuAction.FaceSkinValue);
            }
        }
        LCLogUtils.E(TAG, "face_Mois:" + curFaceMoisValue + " ,Oil:" + curFaceOilValue + " ,shouldCounter:" + shouldCounter);
        if (curFaceMoisValue > 0.1) {
            tvSkinValue.setText(String.format("%.1f", curFaceMoisValue));
            llaySkinValue.setVisibility(View.VISIBLE);
            tvInTesting.setVisibility(View.GONE);
            tvSkinNotice.setVisibility(View.VISIBLE);
        } else {
            llaySkinValue.setVisibility(View.GONE);
            tvInTesting.setVisibility(View.VISIBLE);
            tvSkinNotice.setVisibility(View.GONE);
        }

        if (curFaceMoisValue > 0.1 && curFaceMoisValue <= 32) {
            rlayInTest.setBackground(OznerFileImageHelper.readBitDrawable(getContext(), R.drawable.replen_test_orange));
            tvSkinNotice.setText(R.string.replen_face_notice_dry);
            tvSkinState.setText(R.string.dry);
        } else if (curFaceMoisValue > 32 && curFaceMoisValue <= 42) {
            rlayInTest.setBackground(OznerFileImageHelper.readBitDrawable(getContext(), R.drawable.replen_test_blue));
            tvSkinNotice.setText(R.string.replen_face_notice_normal);
            tvSkinState.setText(R.string.normal);
        } else if (curFaceMoisValue > 42) {
            rlayInTest.setBackground(OznerFileImageHelper.readBitDrawable(getContext(), R.drawable.replen_test_blue));
            tvSkinNotice.setText(R.string.replen_face_notice_wetness);
            tvSkinState.setText(R.string.wetness);
        } else {
            rlayInTest.setBackground(OznerFileImageHelper.readBitDrawable(getContext(), R.drawable.replen_test_blue));
            if (!replenWater.status().isTesting()) {
                if (shouldCounter == 7 || shouldCounter == 1) {
                    if (curFaceMoisValue < 0.1 && curFaceMoisValue > -0.1) {
                        tvInTesting.setText(R.string.replen_test_less_time);
                    } else if (curFaceMoisValue < -0.1) {
                        tvInTesting.setText(R.string.replen_water_low);
                    }
                }
            }
        }


        if (lastFaceMoisValue > 0) {
            tvLastValue.setText(String.format("%.1f%%", lastFaceMoisValue));
        } else {
            tvLastValue.setText(R.string.state_null);
        }

        if (count > 0) {
            tvSkinAverage.setText(String.format(getString(R.string.replen_average_value), totalValue / count, count));
        } else {
            tvSkinAverage.setText(R.string.state_null);
        }
    }

    /**
     * 显示眼部数据
     */

    private void showEyeValue() {
        try {
            totalValue = (double) oznerSetting.getAppData(Contacts.DEV_REPLEN_EYE_MOIS_TOTAL);
            count = (int) oznerSetting.getAppData(Contacts.DEV_REPLEN_EYE_TEST_COUNT);
        } catch (Exception ex) {
            count = 0;
            totalValue = 0;
        }
        if (curEyeMoisValue < 0.1) {
            try {
                lastEyeMoisValue = Double.parseDouble(String.valueOf(oznerSetting.getAppData(Contacts.DEV_REPLEN_EYE_LAST_MOIS)));
            } catch (Exception ex) {
                lastEyeMoisValue = 0;
            }
        }
//        LCLogUtils.E(TAG, "eye_count:" + count + " ,totalValue:" + totalValue);
        if (shouldCounter == 3) {//可以计数
            shouldCounter = 7;
            if (replenWater.status().testValue().moisture > 0) {
                curEyeMoisValue = replenWater.status().testValue().moisture;
            }
            if (replenWater.status().testValue().oil > 0) {
                curEyeOilValue = replenWater.status().testValue().oil;
            }
            try {
                lastEyeMoisValue = Double.parseDouble(String.valueOf(oznerSetting.getAppData(Contacts.DEV_REPLEN_EYE_LAST_MOIS)));
            } catch (Exception ex) {
                lastFaceMoisValue = 0;
            }

            count++;
            totalValue += curEyeMoisValue;
            oznerSetting.setAppData(Contacts.DEV_REPLEN_EYE_TEST_COUNT, count);
            oznerSetting.setAppData(Contacts.DEV_REPLEN_EYE_LAST_MOIS, curEyeMoisValue);
//            oznerSetting.setAppData(Contacts.DEV_REPLEN_EYE_LAST_OIL, curEyeOilValue);
            oznerSetting.setAppData(Contacts.DEV_REPLEN_EYE_MOIS_TOTAL, totalValue);
            DBManager.getInstance(getContext()).updateDeviceSettings(oznerSetting);

            if (curEyeMoisValue > 0) {
                updateBuShuiYiNumber(String.format("%.2f", curEyeOilValue), String.format("%.2f", curEyeMoisValue), ReplenFenBuAction.EyesSkinValue);
            }
        }

        if (curEyeMoisValue > 0.1) {
            tvSkinValue.setText(String.format("%.1f", curEyeMoisValue));
            llaySkinValue.setVisibility(View.VISIBLE);
            tvInTesting.setVisibility(View.GONE);
            tvSkinNotice.setVisibility(View.VISIBLE);
        } else {
            llaySkinValue.setVisibility(View.GONE);
            tvInTesting.setVisibility(View.VISIBLE);
            tvSkinNotice.setVisibility(View.GONE);
        }


        if (curEyeMoisValue > 0.1 && curEyeMoisValue <= 35) {
            tvSkinState.setText(R.string.dry);
            tvSkinNotice.setText(R.string.replen_eye_notice_dry);
            rlayInTest.setBackground(OznerFileImageHelper.readBitDrawable(getContext(), R.drawable.replen_test_orange));
        } else if (curEyeMoisValue > 35 && curEyeMoisValue <= 45) {
            tvSkinState.setText(R.string.normal);
            tvSkinNotice.setText(R.string.replen_eye_notice_normal);
            rlayInTest.setBackground(OznerFileImageHelper.readBitDrawable(getContext(), R.drawable.replen_test_blue));
        } else if (curEyeMoisValue > 45) {
            tvSkinState.setText(R.string.wetness);
            tvSkinNotice.setText(R.string.replen_eye_notice_wetness);
            rlayInTest.setBackground(OznerFileImageHelper.readBitDrawable(getContext(), R.drawable.replen_test_blue));
        } else {
            rlayInTest.setBackground(OznerFileImageHelper.readBitDrawable(getContext(), R.drawable.replen_test_blue));
            if (shouldCounter == 7) {
                if (curEyeMoisValue < 0.1 && curEyeMoisValue > -0.1) {
                    tvInTesting.setText(R.string.replen_test_less_time);
                } else if (curEyeMoisValue < -0.1) {
                    tvInTesting.setText(R.string.replen_water_low);
                }
            }
        }

        if (lastEyeMoisValue > 0) {
            tvLastValue.setText(String.format("%.1f%%", lastEyeMoisValue));
        } else {
            tvLastValue.setText(R.string.state_null);
        }

        if (count > 0) {
            tvSkinAverage.setText(String.format(getString(R.string.replen_average_value), totalValue / count, count));
        } else {
            tvSkinAverage.setText(R.string.state_null);
        }
    }

    /**
     * 显示手部数据
     */
    private void showHandValue() {
        try {
            totalValue = (double) oznerSetting.getAppData(Contacts.DEV_REPLEN_HAND_MOIS_TOTAL);
            count = (int) oznerSetting.getAppData(Contacts.DEV_REPLEN_HAND_TEST_COUNT);
        } catch (Exception ex) {
            count = 0;
            totalValue = 0;
        }
        if (curHandMoisValue < 0.1) {
            try {
                lastHandMoisValue = Double.parseDouble(String.valueOf(oznerSetting.getAppData(Contacts.DEV_REPLEN_HAND_LAST_MOIS)));
            } catch (Exception ex) {
                lastHandMoisValue = 0;
            }
        }
//        LCLogUtils.E(TAG, "hand_count:" + count + " ,totalValue:" + totalValue + " ,shouldCounter:" + shouldCounter);
        if (shouldCounter == 3) {//可以计数
            shouldCounter = 7;
            if (replenWater.status().testValue().moisture > 0) {
                curHandMoisValue = replenWater.status().testValue().moisture;
            }
            if (replenWater.status().testValue().oil > 0) {
                curHandOilValue = replenWater.status().testValue().oil;
            }
            try {
                lastHandMoisValue = Double.parseDouble(String.valueOf(oznerSetting.getAppData(Contacts.DEV_REPLEN_HAND_LAST_MOIS)));
            } catch (Exception ex) {
                lastHandMoisValue = 0;
            }
            count++;
            totalValue += curHandMoisValue;
            totalValue = new BigDecimal(totalValue).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
            oznerSetting.setAppData(Contacts.DEV_REPLEN_HAND_TEST_COUNT, count);
            oznerSetting.setAppData(Contacts.DEV_REPLEN_HAND_LAST_MOIS, curHandMoisValue);
//            oznerSetting.setAppData(Contacts.DEV_REPLEN_HAND_LAST_OIL, curHandOilValue);
            oznerSetting.setAppData(Contacts.DEV_REPLEN_HAND_MOIS_TOTAL, totalValue);
            DBManager.getInstance(getContext()).updateDeviceSettings(oznerSetting);

            if (curHandMoisValue > 0) {
                updateBuShuiYiNumber(String.format("%.2f", curHandOilValue), String.format("%.2f", curHandMoisValue), ReplenFenBuAction.HandSkinValue);
            }
        }

        if (curHandMoisValue > 0.1) {
            tvSkinValue.setText(String.format("%.1f", curHandMoisValue));
            llaySkinValue.setVisibility(View.VISIBLE);
            tvInTesting.setVisibility(View.GONE);
            tvSkinNotice.setVisibility(View.VISIBLE);
        } else {
            llaySkinValue.setVisibility(View.GONE);
            tvInTesting.setVisibility(View.VISIBLE);
            tvSkinNotice.setVisibility(View.GONE);
        }


        if (curHandMoisValue > 0.1 && curHandMoisValue <= 30) {
            tvSkinState.setText(R.string.dry);
            tvSkinNotice.setText(R.string.replen_hand_notice_dry);
            rlayInTest.setBackground(OznerFileImageHelper.readBitDrawable(getContext(), R.drawable.replen_test_orange));
        } else if (curHandMoisValue > 30 && curHandMoisValue <= 38) {
            tvSkinState.setText(R.string.normal);
            tvSkinNotice.setText(R.string.replen_hand_notice_normal);
            rlayInTest.setBackground(OznerFileImageHelper.readBitDrawable(getContext(), R.drawable.replen_test_blue));
        } else if (curHandMoisValue > 38) {
            tvSkinState.setText(R.string.wetness);
            tvSkinNotice.setText(R.string.replen_hand_notice_wetness);
            rlayInTest.setBackground(OznerFileImageHelper.readBitDrawable(getContext(), R.drawable.replen_test_blue));
        } else {
            rlayInTest.setBackground(OznerFileImageHelper.readBitDrawable(getContext(), R.drawable.replen_test_blue));
            if (shouldCounter == 7) {
                if (curHandMoisValue < 0.1 && curHandMoisValue > -0.1) {
                    tvInTesting.setText(R.string.replen_test_less_time);
                } else if (curHandMoisValue < -0.1) {
                    tvInTesting.setText(R.string.replen_water_low);
                }
            }
        }

        if (lastHandMoisValue > 0) {
            tvLastValue.setText(String.format("%.1f%%", lastHandMoisValue));
        } else {
            tvLastValue.setText(R.string.state_null);
        }

        if (count > 0) {
            tvSkinAverage.setText(String.format(getString(R.string.replen_average_value), totalValue / count, count));
        } else {
            tvSkinAverage.setText(R.string.state_null);
        }
    }

    /**
     * 显示颈部数据
     */
    private void showNeckValue() {
        try {
            totalValue = (double) oznerSetting.getAppData(Contacts.DEV_REPLEN_NECK_MOIS_TOTAL);
            count = (int) oznerSetting.getAppData(Contacts.DEV_REPLEN_NECK_TEST_COUNT);
        } catch (Exception ex) {
            count = 0;
            totalValue = 0;
        }
        if (curNeckMoisValue < 0.1) {
            try {
                lastNeckMoisValue = Double.parseDouble(String.valueOf(oznerSetting.getAppData(Contacts.DEV_REPLEN_NECK_LAST_MOIS)));
            } catch (Exception ex) {
                lastNeckMoisValue = 0;
            }
        }
//        LCLogUtils.E(TAG, "neck_count:" + count + " ,totalValue:" + totalValue);

        if (shouldCounter == 3) {//可以计数
            shouldCounter = 7;
            if (replenWater.status().testValue().moisture > 0) {
                curNeckMoisValue = replenWater.status().testValue().moisture;
            }
            if (replenWater.status().testValue().oil > 0) {
                curNeckOilValue = replenWater.status().testValue().oil;
            }
            try {
                lastNeckMoisValue = Double.parseDouble(String.valueOf(oznerSetting.getAppData(Contacts.DEV_REPLEN_NECK_LAST_MOIS)));
            } catch (Exception ex) {
                lastNeckMoisValue = 0;
            }
            count++;
            totalValue += curNeckMoisValue;
            oznerSetting.setAppData(Contacts.DEV_REPLEN_NECK_TEST_COUNT, count);
            oznerSetting.setAppData(Contacts.DEV_REPLEN_NECK_LAST_MOIS, curNeckMoisValue);
//            oznerSetting.setAppData(Contacts.DEV_REPLEN_NECK_LAST_OIL, curNeckOilValue);
            oznerSetting.setAppData(Contacts.DEV_REPLEN_NECK_MOIS_TOTAL, totalValue);
            DBManager.getInstance(getContext()).updateDeviceSettings(oznerSetting);

            if (curNeckMoisValue > 0) {
                updateBuShuiYiNumber(String.format("%.2f", curNeckOilValue), String.format("%.2f", curNeckMoisValue), ReplenFenBuAction.NeckSkinValue);
            }
        }

        if (curNeckMoisValue > 0.1) {
            tvSkinValue.setText(String.format("%.1f", curNeckMoisValue));
            llaySkinValue.setVisibility(View.VISIBLE);
            tvInTesting.setVisibility(View.GONE);
            tvSkinNotice.setVisibility(View.VISIBLE);
        } else {
            llaySkinValue.setVisibility(View.GONE);
            tvInTesting.setVisibility(View.VISIBLE);
            tvSkinNotice.setVisibility(View.GONE);
        }

        if (curNeckMoisValue > 0.1 && curNeckMoisValue <= 35) {
            tvSkinState.setText(R.string.dry);
            tvSkinNotice.setText(R.string.replen_neck_notice_dry);
            rlayInTest.setBackground(OznerFileImageHelper.readBitDrawable(getContext(), R.drawable.replen_test_orange));
        } else if (curNeckMoisValue > 35 && curNeckMoisValue <= 45) {
            tvSkinState.setText(R.string.normal);
            tvSkinNotice.setText(R.string.replen_neck_notice_normal);
            rlayInTest.setBackground(OznerFileImageHelper.readBitDrawable(getContext(), R.drawable.replen_test_blue));
        } else if (curNeckMoisValue > 45) {
            tvSkinState.setText(R.string.wetness);
            tvSkinNotice.setText(R.string.replen_neck_notice_wetness);
            rlayInTest.setBackground(OznerFileImageHelper.readBitDrawable(getContext(), R.drawable.replen_test_blue));
        } else {
            rlayInTest.setBackground(OznerFileImageHelper.readBitDrawable(getContext(), R.drawable.replen_test_blue));
            if (shouldCounter == 7) {
                if (curNeckMoisValue < 0.1 && curNeckMoisValue > -0.1) {
                    tvInTesting.setText(R.string.replen_test_less_time);
                } else if (curNeckMoisValue < -0.1) {
                    tvInTesting.setText(R.string.replen_water_low);
                }
            }
        }

        if (lastNeckMoisValue > 0) {
            tvLastValue.setText(String.format("%.1f%%", lastNeckMoisValue));
        } else {
            tvLastValue.setText(R.string.state_null);
        }

        if (count > 0) {
            tvSkinAverage.setText(String.format(getString(R.string.replen_average_value), totalValue / count, count));
        } else {
            tvSkinAverage.setText(R.string.state_null);
        }
    }

    /**
     * 显示皮肤肤质
     */
    private void showSkinStatus() {
        try {
            float oilAverage = 0;
            if (timeTotal > 0) {
                oilAverage = oilTotal / timeTotal;
            }
            if (oilAverage > 0 && oilAverage <= 12) {
                tvNullSkinBtn.setText(String.format(getString(R.string.replen_skin_null), getString(R.string.replen_skin_oil)));
            } else if (oilAverage > 12 && oilAverage <= 20) {
                tvNullSkinBtn.setText(String.format(getString(R.string.replen_skin_null), getString(R.string.replen_skin_neture)));
            } else if (oilAverage > 20) {
                tvNullSkinBtn.setText(String.format(getString(R.string.replen_skin_null), getString(R.string.replen_skin_oil)));
            } else {
                tvNullSkinBtn.setText(String.format(getString(R.string.replen_skin_null), getString(R.string.state_null)));
            }
        } catch (Exception ex) {
            LCLogUtils.E(TAG, "showSkinStatus:" + ex.getMessage());
        }
    }

    /**
     * 上传补水仪检测数据
     *
     * @param oilValue  油分值
     * @param moisValue 水分值
     * @param action    检测部位
     */
    private void updateBuShuiYiNumber(String oilValue, String moisValue, final String action) {
        if (replenWater != null) {
            HttpMethods.getInstance().updateBuShuiYiNumber(mUserToken, replenWater.Address(), oilValue, moisValue, action,
                    new ProgressSubscriber<JsonObject>(getContext(), new OznerHttpResult<JsonObject>() {
                        @Override
                        public void onError(Throwable e) {
                            LCLogUtils.E(TAG, "updateBuShuiYiNumber_" + action + "_onError:" + e.getMessage());
                        }

                        @Override
                        public void onNext(JsonObject jsonObject) {
                            if (jsonObject.get("state").getAsInt() == -10006
                                    || jsonObject.get("state").getAsInt() == -10007) {
                                BaseActivity.reLogin(getActivity());
                            } else {
                                LCLogUtils.E(TAG, "updateBuShuiYiNumber_" + action + ":" + jsonObject.toString());
                            }
                        }
                    }));
        }
    }

    /**
     * 从网络加载检测次数
     */
    private void loadTestCount() {
        if (replenWater != null) {
            HttpMethods.getInstance().getTimesCountBuShui(mUserToken, replenWater.Address(),
                    new ProgressSubscriber<JsonObject>(getContext(), new OznerHttpResult<JsonObject>() {
                        @Override
                        public void onError(Throwable e) {
                            LCLogUtils.E(TAG, "loadTestCount_onError:" + e.getMessage());
                        }

                        @Override
                        public void onNext(JsonObject jsonObject) {
                            if (jsonObject != null) {
                                if (jsonObject.get("state").getAsInt() > 0) {
                                    JsonArray dataArray = jsonObject.getAsJsonArray("data");
                                    if (!dataArray.isJsonNull() && dataArray.size() > 0) {
                                        for (JsonElement element : dataArray) {
                                            JsonObject data = element.getAsJsonObject();
                                            switch (data.get("action").getAsString()) {
                                                case ReplenFenBuAction.FaceSkinValue:
                                                    if (oznerSetting != null) {
                                                        oznerSetting.setAppData(Contacts.DEV_REPLEN_FACE_TEST_COUNT, data.get("times").getAsInt());
                                                        DBManager.getInstance(getContext()).updateDeviceSettings(oznerSetting);
                                                    }
                                                    break;
                                                case ReplenFenBuAction.EyesSkinValue:
                                                    if (oznerSetting != null) {
                                                        oznerSetting.setAppData(Contacts.DEV_REPLEN_EYE_TEST_COUNT, data.get("times").getAsInt());
                                                        DBManager.getInstance(getContext()).updateDeviceSettings(oznerSetting);
                                                    }
                                                    break;
                                                case ReplenFenBuAction.HandSkinValue:
                                                    if (oznerSetting != null) {
                                                        oznerSetting.setAppData(Contacts.DEV_REPLEN_HAND_TEST_COUNT, data.get("times").getAsInt());
                                                        DBManager.getInstance(getContext()).updateDeviceSettings(oznerSetting);
                                                    }
                                                    break;
                                                case ReplenFenBuAction.NeckSkinValue:
                                                    if (oznerSetting != null) {
                                                        oznerSetting.setAppData(Contacts.DEV_REPLEN_NECK_TEST_COUNT, data.get("times").getAsInt());
                                                        DBManager.getInstance(getContext()).updateDeviceSettings(oznerSetting);
                                                    }
                                                    break;
                                            }
                                        }
                                    }
                                } else {
                                    if (jsonObject.get("state").getAsInt() == -10006
                                            || jsonObject.get("state").getAsInt() == -10007) {
                                        BaseActivity.reLogin(getActivity());
                                    }
                                }
                            }
                        }
                    }));
        }
    }


    /**
     * 加载补水仪历史检测数据
     * 目前只获取脸部数据
     */
    private void loadBuShuiFenbu() {
        LCLogUtils.E(TAG, "开始加载历史检测数据");
        if (replenWater != null) {
            HttpMethods.getInstance().getBuShuiFenBu(mUserToken, replenWater.Address(), ReplenFenBuAction.FaceSkinValue,
                    new ProgressSubscriber<JsonObject>(getContext(), new OznerHttpResult<JsonObject>() {
                        @Override
                        public void onError(Throwable e) {
                            LCLogUtils.E(TAG, "loadBuShuiFenbu_onError: " + e.getMessage());
                        }

                        @Override
                        public void onNext(JsonObject jsonObject) {
                            if (jsonObject != null) {
                                LCLogUtils.E(TAG, "loadBuShuiFenbu: " + jsonObject.toString());
                                int state = jsonObject.get("state").getAsInt();
                                if (state > 0) {
                                    JsonObject faceData = jsonObject.getAsJsonObject("data").getAsJsonObject("FaceSkinValue");
                                    if (!faceData.isJsonNull()) {
                                        JsonArray faceMonthArray = faceData.getAsJsonArray("monty");
                                        if (!faceMonthArray.isJsonNull()) {
                                            int arraySize = faceMonthArray.size();
                                            oilTotal = 0;
                                            timeTotal = 0;
                                            /**
                                             * 皮肤肤质只计算脸部数据
                                             *
                                             *数组中是每天测试的平均值以及每天测试的次数，测试的时候会上传当前测试的油性值，服务器计算平均值，并和当天测试次数一起存储；
                                             *所以计算月数据的时候需要把每天的值统计，然后求平均
                                             */
                                            for (int i = 0; i < arraySize; i++) {
                                                float oil = faceMonthArray.get(i).getAsJsonObject().get("ynumber").getAsFloat();
                                                int time = faceMonthArray.get(i).getAsJsonObject().get("times").getAsInt();
                                                oilTotal += oil * time;
                                                timeTotal += time;
                                            }

                                            showSkinStatus();
                                        }
                                    }
                                } else {
                                    if (state == -10006 || state == -10007) {
                                        BaseActivity.reLogin(getActivity());
                                    }
                                }
                            } else {
                                LCLogUtils.E(TAG, "结果为空");
                            }
                        }
                    }));
        }
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

    @OnClick({R.id.iv_setting, R.id.tv_null_skin_btn, R.id.llay_skin_detail})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_setting:
                if (replenWater != null) {
                    Intent setUpIntent = new Intent(getContext(), SetUpReplenActivity.class);
                    setUpIntent.putExtra(Contacts.PARMS_MAC, replenWater.Address());
                    startActivityForResult(setUpIntent, SetReqCode);
                } else {
                    showCenterToast(R.string.Not_found_device);
                }
                break;
            case R.id.tv_null_skin_btn:
                if (replenWater != null) {
                    Intent queryIntent = new Intent(getContext(), ReplenQueryActivity.class);
                    queryIntent.putExtra(Contacts.PARMS_MAC, replenWater.Address());
                    startActivity(queryIntent);
                } else {
                    showCenterToast(R.string.Not_found_device);
                }
                break;
            case R.id.llay_skin_detail:
                if (replenWater != null) {
                    Intent detailIntent = new Intent(getContext(), ReplenDetailActivity.class);
                    detailIntent.putExtra(Contacts.PARMS_MAC, replenWater.Address());
                    detailIntent.putExtra(Contacts.PARMS_CLICK_POS, clickPos);
                    startActivity(detailIntent);
                } else {
                    showCenterToast(R.string.Not_found_device);
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SetReqCode) {
                oznerSetting = DBManager.getInstance(getContext()).getDeviceSettings(mUserid, replenWater.Address());
                if (oznerSetting != null) {
                    gender = (int) oznerSetting.getAppData(Contacts.DEV_REPLEN_GENDER);
                }
                resetView();
            }
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
        LCLogUtils.E(TAG, "registerMonitor");
    }

    /**
     * 注销广播监听
     */
    private void releaseMonitor() {
        try {
//            if (!isDetached()) {
            getContext().unregisterReceiver(mMonitor);
//            }
            LCLogUtils.E(TAG, "releaseMonitor");
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
            Log.e(TAG, "onReceive: " + replenWater.toString() + " ,connectStatus:" + replenWater.connectStatus().toString());
            refreshUIData();
        }
    }
}
