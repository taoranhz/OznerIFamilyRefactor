package com.ozner.cup.Device.Tap;

import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.ozner.cup.CupRecord;
import com.ozner.cup.Device.DeviceFragment;
import com.ozner.cup.Main.MainActivity;
import com.ozner.cup.R;
import com.ozner.cup.UIView.ChartAdapter;
import com.ozner.cup.UIView.TapTDSChartView;
import com.ozner.cup.UIView.TdsDetailProgress;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;
import com.ozner.tap.Tap;
import com.ozner.tap.TapRecord;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.ozner.cup.R.string.bad;

/**
 * Created by ozner_67 on 2016/11/4.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class TapFragment extends DeviceFragment {
    private static final String TAG = "TapFragment";
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
    private Tap mTap;
    private TapMonitor tapMonitor;
//    private RotateAnimation rotateAnimation;
    private int oldTdsValue;
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

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        initAnimation();
        try {
            Bundle bundle = getArguments();
            mTap = (Tap) OznerDeviceManager.Instance().getDevice(bundle.getString(DeviceAddress));
            oldTdsValue = 0;
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
        return view;
    }

//    /**
//     * 初始化动画
//     */
//    private void initAnimation() {
//        rotateAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//        rotateAnimation.setRepeatCount(-1);
//        LinearInterpolator li = new LinearInterpolator();
//        rotateAnimation.setInterpolator(li);
//        rotateAnimation.setFillAfter(false);
//        rotateAnimation.setDuration(1000);
//    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        tdsChartView.setAdapter(recordAdapter);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        refreshUIData();
        super.onResume();
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
            ((MainActivity) getActivity()).setCustomTitle(mTap.getName());
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
            tvTdsRank.setVisibility(View.GONE);
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
                if (oldTdsValue != tdsValue) {
                    tvTdsValue.setTextSize(NumSize);
                    final ValueAnimator animator = ValueAnimator.ofInt(oldTdsValue, tdsValue);
                    animator.setDuration(500);
                    animator.setInterpolator(new LinearInterpolator());//线性效果变化
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            Integer value = (Integer) animator.getAnimatedValue();
                            tvTdsValue.setText("" + value);

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
                }
            } else {
                tvTdsState.setText(R.string.state_null);
                ivTdsState.setVisibility(View.GONE);
                tvTdsValue.setText(R.string.no_data);
                tvTdsValue.setTextSize(TextSize);
                tvTdsRank.setVisibility(View.GONE);
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
    public void onAttach(Context context) {
        try {
            if (TapFragment.this.isAdded() && !TapFragment.this.isRemoving() && !TapFragment.this.isDetached())
                ((MainActivity) context).setCustomTitle(getString(R.string.water_probe));
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "onAttach_Ex: " + ex.getMessage());
        }
        super.onAttach(context);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
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

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == Activity.RESULT_OK) {
//            switch (requestCode) {
//                case SetUpRequestCode:
//
//                    refreshUIData();
//                    break;
//            }
//        }
//    }

    class TapMonitor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshUIData();
        }
    }
}
