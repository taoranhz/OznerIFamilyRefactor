package com.ozner.cup.Device.Tap;

import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.CupRecord;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.OznerDeviceSettings;
import com.ozner.cup.Device.DeviceFragment;
import com.ozner.cup.HttpHelper.HttpMethods;
import com.ozner.cup.HttpHelper.ProgressSubscriber;
import com.ozner.cup.Main.MainActivity;
import com.ozner.cup.R;
import com.ozner.cup.UIView.ChartAdapter;
import com.ozner.cup.UIView.TapTDSChartView;
import com.ozner.cup.UIView.TdsDetailProgress;
import com.ozner.cup.Utils.MobileInfoUtil;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;
import com.ozner.tap.Tap;
import com.ozner.tap.TapRecord;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.functions.Action1;

import static com.ozner.cup.R.string.bad;

/**
 * Created by ozner_67 on 2016/11/4.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class TapFragment extends DeviceFragment {
    private static final String TAG = "TapFragment";
    private static final int INIT_WARRANTY = 30;// 默认有效期
    private static final int TextSize = 45;
    private static final int NumSize = 60;
    @InjectView(R.id.iv_battery_icon)
    ImageView ivBatteryIcon;
    @InjectView(R.id.tv_battery_value)
    TextView tvBatteryValue;
    @InjectView(R.id.iv_filter_icon)
    ImageView ivFilterIcon;
    @InjectView(R.id.tv_filter_value)
    TextView tvFilterValue;
    @InjectView(R.id.tv_filter_tips)
    TextView tvFilterTips;
    @InjectView(R.id.iv_setting)
    ImageView ivSetting;
    @InjectView(R.id.iv_deviceConnectIcon)
    ImageView ivDeviceConnectIcon;
    @InjectView(R.id.tv_deviceConnectTips)
    TextView tvDeviceConnectTips;
    @InjectView(R.id.llay_deviceConnectTip)
    LinearLayout llayDeviceConnectTip;
    @InjectView(R.id.tdsDetailProgress)
    TdsDetailProgress tdsDetailProgress;
    @InjectView(R.id.iv_tdsState)
    ImageView ivTdsState;
    @InjectView(R.id.tv_tdsState)
    TextView tvTdsState;
    @InjectView(R.id.tv_tdsRank)
    TextView tvTdsRank;
    @InjectView(R.id.rlay_filter)
    RelativeLayout rlayFilter;
    @InjectView(R.id.tv_tdsValue)
    TextView tvTdsValue;
    @InjectView(R.id.tv_tapHealthPre)
    TextView tvTapHealthPre;
    @InjectView(R.id.tv_tapGenericPre)
    TextView tvTapGenericPre;
    @InjectView(R.id.tv_tapBadPre)
    TextView tvTapBadPre;
    @InjectView(R.id.tdsChartView)
    TapTDSChartView tdsChartView;
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    private Tap mTap;
    private TapMonitor tapMonitor;
    //    private RotateAnimation rotateAnimation;
    private int oldTdsValue;
    private String mUserid;
    private OznerDeviceSettings oznerSetting;
    SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    int[] tdsDatas = new int[31];

    ChartAdapter recordAdapter = new ChartAdapter() {

        @Override
        public int count() {
            return tdsDatas.length;
        }

        @Override
        public int getValue(int Index) {
            return tdsDatas[Index];
        }

        @Override
        public int getMax() {
            return CupRecord.TDS_Bad_Value * 2 - CupRecord.TDS_Good_Value;
        }

        @Override
        public ViewMode getViewMode() {
            return ViewMode.Month;
        }
    };

    /**
     * 实例化Fragment
     *
     * @param mac
     *
     * @return
     */
    public static DeviceFragment newInstance(String mac) {
        TapFragment fragment = new TapFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DeviceAddress, mac);
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    /**
     * 设置设备
     *
     * @param device
     */

    @Override
    public void setDevice(OznerDevice device) {
        oznerSetting = DBManager.getInstance(getContext()).getDeviceSettings(mUserid, device.Address());
        if (mTap != null) {
            if (mTap.Address() != device.Address()) {
                mTap.release();
                mTap = null;
                mTap = (Tap) device;
                refreshUIData();
            }
        } else {
            mTap = (Tap) device;
            refreshUIData();
        }
        refreshTapFilterInfo();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mUserid = UserDataPreference.GetUserData(getContext(), UserDataPreference.UserId, "");
        initAnimation();
        try {
            Bundle bundle = getArguments();
            mTap = (Tap) OznerDeviceManager.Instance().getDevice(bundle.getString(DeviceAddress));
            oldTdsValue = 0;
            oznerSetting = DBManager.getInstance(getContext()).getDeviceSettings(mUserid, mTap.Address());
            refreshTapFilterInfo();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "onCreate_Ex: " + ex.getMessage());
        }
        super.onCreate(savedInstanceState);
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_tap, container, false);
        ButterKnife.inject(this, view);
        toolbar.setTitle("");
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).initActionBarToggle(toolbar);
        tdsChartView.setAdapter(recordAdapter);
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
            setBarColor(R.color.cup_detail_bg);
            setToolbarColor(R.color.cup_detail_bg);
        } catch (Exception ex) {

        }
        if (TapFragment.this.isAdded())
            title.setText(getString(R.string.water_probe));
        refreshUIData();
        super.onResume();
    }


    /**
     * 刷新水探头滤芯信息
     */
    private void refreshTapFilterInfo() {
        String startTimeStr = (String) mTap.Setting().get(Contacts.TAP_START_TIME, null);
        if (startTimeStr != null) {
            try {
                int useDay = (int) mTap.Setting().get(Contacts.TAP_FILTER_USEDAY, 0);
                Date startTime = dataFormat.parse(startTimeStr);
                long temUseDay = (Calendar.getInstance().getTimeInMillis() - startTime.getTime()) / (24 * 3600 * 1000);
                Log.e(TAG, "refreshTapFilterInfo: temUseDay:" + temUseDay);
                if (temUseDay != useDay) {
                    loadTapFilterFromNet();
                } else {
                    if (useDay < INIT_WARRANTY) {
                        int ret = (INIT_WARRANTY - useDay) / INIT_WARRANTY * 100;
                        showFilterInfo(ret);
                    } else {
                        showFilterInfo(0);
                    }
                }
            } catch (Exception ex) {
                Log.e(TAG, "refreshTapFilterInfo_Ex: " + ex.getMessage());
            }
        } else {
            loadTapFilterFromNet();
        }
    }

    /**
     * 从网络加载滤芯数据
     * <p>
     * 这里获取数据，需要在设备配对时，将设备同步到服务器
     */
    private void loadTapFilterFromNet() {
        Log.e(TAG, "loadTapFilterFromNet: mime:" + MobileInfoUtil.getImie(getContext()) + " ,deviceName:" + Build.MANUFACTURER);
        if (mTap != null) {
            Log.e(TAG, "loadTapFilterFromNet_Usertoken: " + OznerPreference.getUserToken(getContext()));
            Log.e(TAG, "loadTapFilterFromNet_Mac: " + mTap.Address());
            HttpMethods.getInstance().getTapFilterInfo(OznerPreference.getUserToken(getContext()), mTap.Address(), new ProgressSubscriber<JsonObject>(getContext(), new Action1<JsonObject>() {
                @Override
                public void call(JsonObject jsonObject) {
                    Log.e(TAG, "loadTapFilterFromNet: " + jsonObject.toString());
                }
            }));
        }
    }

    /**
     * 显示滤芯信息
     */
    private void showFilterInfo(int ret) {
        if (isAdded()) {
            if (ret > 100) ret = 100;
            if (ret < 0) ret = 0;
            if (ret == 0) {
                ivFilterIcon.setImageResource(R.drawable.filter_state0);
                tvFilterTips.setText(R.string.filter_need_change);
            } else if (ret > 0 && ret < 30) {
                ivFilterIcon.setImageResource(R.drawable.filter_state1);
                tvFilterTips.setText(R.string.filter_need_change);
            } else if (ret > 30 && ret < 60) {
                ivFilterIcon.setImageResource(R.drawable.filter_state2);
                tvFilterTips.setText(R.string.filter_status);
            } else if (ret > 60) {
                ivFilterIcon.setImageResource(R.drawable.filter_state3);
                tvFilterTips.setText(R.string.filter_status);
            }

            tvFilterValue.setText(String.format("%d%%", ret));
        }
    }


    /**
     * 刷新UI数据
     */
    @Override
    protected void refreshUIData() {
        if (TapFragment.this.isAdded() && !TapFragment.this.isRemoving() && !TapFragment.this.isDetached() && mTap != null) {
            // TODO: 2016/11/7 加载数据，并填充页面
            Log.e(TAG, "refreshUIData: ");
            refreshConnectState();
            refreshSensorData();
        }
    }

    /**
     * 刷新连接状态
     */
    private void refreshConnectState() {
        if (mTap != null) {
            Log.e(TAG, "refreshConnectState: " + mTap.toString());
            if (ivDeviceConnectIcon.getAnimation() == null) {
                ivDeviceConnectIcon.setAnimation(rotateAnimation);
            }
            if (mTap.connectStatus() == BaseDeviceIO.ConnectStatus.Connecting) {
                llayDeviceConnectTip.setVisibility(View.VISIBLE);
                tvDeviceConnectTips.setText(R.string.device_connecting);
                ivDeviceConnectIcon.setImageResource(R.drawable.data_loading);
                ivDeviceConnectIcon.getAnimation().start();
            } else if (mTap.connectStatus() == BaseDeviceIO.ConnectStatus.Connected) {
                llayDeviceConnectTip.setVisibility(View.INVISIBLE);
                if (ivDeviceConnectIcon.getAnimation() != null) {
                    ivDeviceConnectIcon.getAnimation().cancel();
                }
            } else if (mTap.connectStatus() == BaseDeviceIO.ConnectStatus.Disconnect) {
                llayDeviceConnectTip.setVisibility(View.VISIBLE);
                tvDeviceConnectTips.setText(R.string.device_unconnected);
                if (ivDeviceConnectIcon.getAnimation() != null) {
                    ivDeviceConnectIcon.getAnimation().cancel();
                }
                ivDeviceConnectIcon.setImageResource(R.drawable.data_load_fail);
            }
        }
    }

    /**
     * 刷新传感器数据
     */
    private void refreshSensorData() {
        if (mTap != null) {
            if (oznerSetting != null) {
                title.setText(oznerSetting.getName());
            } else {
                title.setText(mTap.getName());
            }
            showTdsState(mTap.Sensor().TDSFix);
            showPowerState();
            showTdsRecords();
        }
    }

    /**
     * 处理TDS显示相关
     *
     * @param tdsValue
     */
    private void showTdsState(int tdsValue) {
        if (tdsValue == 65535) {//传感器无数据
            tvTdsValue.setTextSize(TextSize);
            tvTdsValue.setText(R.string.no_data);
            ivTdsState.setVisibility(View.GONE);
            tvTdsState.setText(R.string.state_null);
            tvTdsRank.setVisibility(View.INVISIBLE);
        } else {
            //显示tds对应状态
            if (tdsValue > 0 && tdsValue <= CupRecord.TDS_Good_Value) {
                ivTdsState.setVisibility(View.VISIBLE);
                Glide.with(this).load(R.drawable.face_good).asBitmap().into(ivTdsState);
                tvTdsState.setText(R.string.health);
            } else if (tdsValue > CupRecord.TDS_Good_Value && tdsValue <= CupRecord.TDS_Bad_Value) {
                ivTdsState.setVisibility(View.VISIBLE);
                Glide.with(this).load(R.drawable.face_soso).asBitmap().into(ivTdsState);
                tvTdsState.setText(R.string.soso);
            } else if (tdsValue > CupRecord.TDS_Bad_Value) {
                ivTdsState.setVisibility(View.VISIBLE);
                Glide.with(this).load(R.drawable.face_bad).asBitmap().into(ivTdsState);
                tvTdsState.setText(bad);
            }

            //数字跑马灯
            if (tdsValue != 0) {
//                if (oldTdsValue != tdsValue) {
                tvTdsValue.setTextSize(NumSize);
                final ValueAnimator animator = ValueAnimator.ofInt(oldTdsValue, tdsValue);
                animator.setDuration(500);
                animator.setInterpolator(new LinearInterpolator());//线性效果变化
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        Integer value = (Integer) animator.getAnimatedValue();
                        try {
                            if (isAdded())
                                tvTdsValue.setText("" + value);
                        } catch (Exception ex) {
                            Log.e(TAG, "onAnimationUpdate_Ex: " + ex.getMessage());
                        }

                    }
                });
                animator.start();
                if (tdsValue > 250) {
                    tdsDetailProgress.update(100);
                } else {
//                    double s = (tdsValue / 250.00) * 100;
//                    tdsDetailProgress.update((int) s);
                    tdsDetailProgress.update((tdsValue << 1) / 5);
                }
                oldTdsValue = tdsValue;
//                }
            } else {
                tvTdsState.setText(R.string.state_null);
                ivTdsState.setVisibility(View.GONE);
                tvTdsValue.setText(R.string.no_data);
                tvTdsValue.setTextSize(TextSize);
                tvTdsRank.setVisibility(View.INVISIBLE);
                tdsDetailProgress.update(0);
            }
        }
    }

    /**
     * 显示电池状态
     */
    private void showPowerState() {
        if (mTap != null) {
            int batteryValue = Math.round(mTap.Sensor().getPower() * 100);
            //设置电池电量图标
            if (batteryValue == 100) {
                ivBatteryIcon.setImageResource(R.drawable.battery100);
            } else if (batteryValue >= 50 && batteryValue < 100) {
                ivBatteryIcon.setImageResource(R.drawable.battery70);
            } else if (batteryValue > 0 && batteryValue < 50) {
                ivBatteryIcon.setImageResource(R.drawable.battery30);
            }

            //设置电量值，并设置没电时的电量图标
            if (batteryValue > 0) {
                tvBatteryValue.setText(String.valueOf(batteryValue) + "%");
            } else {
                ivBatteryIcon.setImageResource(R.drawable.battery0);
                tvBatteryValue.setText(R.string.state_null);
            }
        }
    }

    /**
     * 加载tds分布数据
     */
    private void showTdsRecords() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        TapRecord[] tapRecords = mTap.TapRecordList().getRecordsByDate(cal.getTime());
        int recordCount = 0;
        int good_count = 0;
        int nor_count = 0;
        int bad_count = 0;
        if (tapRecords != null && tapRecords.length > 0) {
            recordCount = tapRecords.length;
            for (int i = 0; i < recordCount; i++) {
                tdsDatas[tapRecords[i].time.getDate() - 1] = tapRecords[i].TDS;
                if (tapRecords[i].TDS > 0 && tapRecords[i].TDS < CupRecord.TDS_Good_Value) {
                    good_count++;
                } else if (tapRecords[i].TDS > CupRecord.TDS_Good_Value && tapRecords[i].TDS < CupRecord.TDS_Bad_Value) {
                    nor_count++;
                } else if (tapRecords[i].TDS > CupRecord.TDS_Bad_Value) {
                    bad_count++;
                }
            }
        }
        int good_pre = 0;
        int nor_pre = 0;
        int bad_pre = 0;
        if (recordCount > 0) {
            good_pre = good_count / recordCount * 100;
            nor_pre = nor_count / recordCount * 100;
            bad_pre = 100 - good_count - nor_count;
        }
        tvTapHealthPre.setText(getString(R.string.health) + "(" + good_pre + "%)");
        tvTapGenericPre.setText(getString(R.string.soso) + "(" + nor_pre + "%)");
        tvTapBadPre.setText(getString(R.string.bad) + "(" + bad_pre + "%)");
    }


    /**
     * 注册广播接收器
     */
    private void registerMonitor() {
        tapMonitor = new TapMonitor();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Tap.ACTION_BLUETOOTHTAP_SENSOR);
        filter.addAction(Tap.ACTION_BLUETOOTHTAP_RECORD_COMPLETE);
        filter.addAction(OznerDeviceManager.ACTION_OZNER_MANAGER_DEVICE_CHANGE);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_CONNECTING);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_DISCONNECTED);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_CONNECTED);
        getContext().registerReceiver(tapMonitor, filter);
    }

    /**
     * 注销广播接收器
     */
    private void releaseMonitor() {
        if (TapFragment.this.isAdded() && !TapFragment.this.isRemoving() && !TapFragment.this.isDetached() && tapMonitor != null) {
            getContext().unregisterReceiver(tapMonitor);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onDetach() {
        System.gc();
        super.onDetach();
    }

    @OnClick({R.id.rlay_filter, R.id.iv_setting, R.id.llay_tds_detail})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rlay_filter:
                break;
            case R.id.iv_setting:
                if (mTap != null) {
                    Intent setIntent = new Intent(getContext(), SetupTapActivity.class);
                    setIntent.putExtra(SetupTapActivity.PARMS_MAC, mTap.Address());
                    startActivity(setIntent);
                } else {
                    Toast.makeText(getContext(), R.string.Not_found_device, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.llay_tds_detail:
                break;
        }
    }

    class TapMonitor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshUIData();
        }
    }
}
