package com.ozner.cup.Device.Cup;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ozner.cup.Cup;
import com.ozner.cup.CupRecord;
import com.ozner.cup.Device.DeviceFragment;
import com.ozner.cup.Device.Tap.SetupTapActivity;
import com.ozner.cup.Main.MainActivity;
import com.ozner.cup.R;
import com.ozner.cup.UIView.TdsDetailProgress;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.ozner.cup.R.id.tv_tdsRank;
import static com.ozner.cup.R.string.bad;

public class CupFragment extends DeviceFragment {
    private static final String TAG = "CupFragment";
    private static final int TextSize = 45;
    private static final int NumSize = 60;
    @InjectView(R.id.iv_battery_icon)
    ImageView ivBatteryIcon;
    @InjectView(R.id.tv_battery_value)
    TextView tvBatteryValue;
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
    @InjectView(R.id.tv_tdsValue)
    TextView tvTdsValue;
    @InjectView(tv_tdsRank)
    TextView tvTdsRank;
    @InjectView(R.id.iv_volumIcon)
    ImageView ivVolumIcon;
    @InjectView(R.id.tv_VolumValue)
    TextView tvVolumValue;
    @InjectView(R.id.iv_tempIcon)
    ImageView ivTempIcon;
    @InjectView(R.id.tv_tempState)
    TextView tvTempState;
    @InjectView(R.id.tv_tempNotice)
    TextView tvTempNotice;
    @InjectView(R.id.tv_water_goal)
    TextView tvWaterGoal;
    @InjectView(R.id.llay_tds_detail)
    LinearLayout llayTdsDetail;
    @InjectView(R.id.tv_volumTips)
    TextView tvVolumTips;
    @InjectView(R.id.tv_goalTips)
    TextView tvGoalTips;
    @InjectView(R.id.tv_tempTips)
    TextView tvTempTips;

    private Cup mCup;
    private int oldTdsValue;
    private CupMonitor mMonitor;
    Calendar recordCal = Calendar.getInstance();


    /**
     * 实例化Fragment
     *
     * @param mac
     *
     * @return
     */
    public static DeviceFragment newInstance(String mac) {
        Log.e(TAG, "newInstance: " + mac);
        CupFragment fragment = new CupFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DeviceAddress, mac);
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }


    @Override
    public void setDevice(OznerDevice device) {
        if (mCup != null) {
            if (mCup.Address() != device.Address()) {
                mCup.release();
                mCup = null;
                mCup = (Cup) device;
                refreshUIData();
            }
        } else {
            mCup = (Cup) device;
            refreshUIData();
        }
    }

    /**
     * 初始化查询时间
     */
    private void initRecordCal() {
        recordCal = Calendar.getInstance();
        recordCal.set(Calendar.HOUR_OF_DAY, 0);
        recordCal.set(Calendar.MINUTE, 0);
        recordCal.set(Calendar.SECOND, 0);
        recordCal.set(Calendar.MILLISECOND, 0);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        initAnimation();
        try {
            Bundle bundle = getArguments();
            mCup = (Cup) OznerDeviceManager.Instance().getDevice(bundle.getString(DeviceAddress));
            oldTdsValue = 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "onCreate_Ex: " + ex.getMessage());
        }
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cup, container, false);
        ButterKnife.inject(this, view);
        return view;
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
    public void onResume() {
        initRecordCal();
        refreshUIData();
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        Log.e(TAG, "onAttach: ");
        try {
            if (isThisAdd())
                ((MainActivity) context).setCustomTitle(getString(R.string.smart_glass));
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "onAttach_Ex: " + ex.getMessage());
        }
        super.onAttach(context);
    }

    private boolean isThisAdd() {
        return CupFragment.this.isAdded() && !CupFragment.this.isRemoving();
    }

    /**
     * 刷新UI数据
     */
    @Override
    protected void refreshUIData() {
        if (isThisAdd() && mCup != null) {
            Log.e(TAG, "refreshUIData: " + mCup.Sensor().toString());
            refreshConnectState();
            if (mCup.connectStatus() == BaseDeviceIO.ConnectStatus.Connected) {
                refreshSensorData();
            }
        }
    }

    /**
     * 刷新连接状态
     */
    private void refreshConnectState() {
        if (mCup != null) {
            if (ivDeviceConnectIcon.getAnimation() == null) {
                ivDeviceConnectIcon.setAnimation(rotateAnimation);
            }
            if (mCup.connectStatus() == BaseDeviceIO.ConnectStatus.Connecting) {
                llayDeviceConnectTip.setVisibility(View.VISIBLE);
                tvDeviceConnectTips.setText(R.string.device_connecting);
                ivDeviceConnectIcon.setImageResource(R.drawable.data_loading);
                ivDeviceConnectIcon.getAnimation().start();
            } else if (mCup.connectStatus() == BaseDeviceIO.ConnectStatus.Connected) {
                llayDeviceConnectTip.setVisibility(View.INVISIBLE);
                if (ivDeviceConnectIcon.getAnimation() != null) {
                    ivDeviceConnectIcon.getAnimation().cancel();
                }
            } else if (mCup.connectStatus() == BaseDeviceIO.ConnectStatus.Disconnect) {
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
        if (mCup != null) {
            Log.e(TAG, "refreshSensorData: " + mCup.toString());
            ((MainActivity) getActivity()).setCustomTitle(mCup.getName());
            showTdsState(mCup.Sensor().TDSFix);
            showPowerState();
            showWaterTarget();
            showWaterTemp();
        }
    }

    /**
     * 显示饮水目标
     */
    private void showWaterTarget() {
        if (mCup != null) {
            CupRecord record = mCup.Volume().getRecordByDate(recordCal.getTime());
            if (record != null) {
                tvVolumValue.setText(String.valueOf(record.Volume));
                if (record.Volume == 0) {
                    ivVolumIcon.setImageResource(R.drawable.watervolum0);
                } else if (record.Volume > 0 && record.Volume <= 1000) {
                    ivVolumIcon.setImageResource(R.drawable.watervolum30);
                } else if (record.Volume > 1000 && record.Volume <= 2000) {
                    ivVolumIcon.setImageResource(R.drawable.watervolum50);
                } else {
                    ivVolumIcon.setImageResource(R.drawable.watervolum80);
                }
            }
        }
    }

    /**
     * 显示水温
     */
    private void showWaterTemp() {
        if (mCup != null) {
            int temp = mCup.Sensor().TemperatureFix;

            if (temp > 0 && temp <= CupRecord.Temperature_Low_Value) {
                tvTempState.setText(R.string.temp_cool);
                tvTempNotice.setText(R.string.can_not_drink);
                ivTempIcon.setImageResource(R.drawable.temp_0);
            } else if (temp > CupRecord.Temperature_Low_Value && temp <= CupRecord.Temperature_High_Value) {
                tvTempState.setText(R.string.temp_moderation);
                tvTempNotice.setText(R.string.can_drink);
                ivTempIcon.setImageResource(R.drawable.temp_50);
            } else if (temp > CupRecord.Temperature_High_Value && temp <= 100) {
                tvTempState.setText(R.string.temp_hot);
                tvTempNotice.setText(R.string.can_not_drink);
                ivTempIcon.setImageResource(R.drawable.temp_100);
            } else {
                tvTempState.setText(R.string.state_null);
                tvTempNotice.setText(R.string.temp_null);
                ivTempIcon.setImageResource(R.drawable.temp_0);
            }
        }
    }

    /**
     * 处理TDS显示相关
     *
     * @param tdsValue
     */
    private void showTdsState(int tdsValue) {
//        if (tdsValue == 65535) {//传感器无数据
        if (tdsValue > 5000) {//传感器无数据
            showNoData();
        } else {
            int beat = 68;
            tvTdsRank.setText(String.format(getString(R.string.beat_users), beat));
            tvTdsRank.setVisibility(View.VISIBLE);
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
                    tvTdsValue.setText(String.valueOf(tdsValue));
                    if (tdsValue > 250) {
                        tdsDetailProgress.update(100);
                    } else {
                        tdsDetailProgress.update((tdsValue << 1) / 5);
                    }
                    oldTdsValue = tdsValue;
                }
            } else {
                showNoData();
            }
        }
    }

    /**
     * 显示无数据
     */
    private void showNoData() {
        tvTdsState.setText(R.string.state_null);
        ivTdsState.setVisibility(View.GONE);
        tvTdsValue.setText(R.string.no_data);
        tvTdsValue.setTextSize(TextSize);
        tvTdsRank.setVisibility(View.INVISIBLE);
        tdsDetailProgress.update(0);
    }

    /**
     * 显示电池状态
     */
    private void showPowerState() {
        if (mCup != null) {
            int batteryValue = Math.round(mCup.Sensor().getPower() * 100);
            Log.e(TAG, "showPowerState: batteryValue:" + batteryValue);
            //设置电池电量图标
            if (batteryValue == 100) {
                ivBatteryIcon.setImageResource(R.drawable.battery100);
            } else if (batteryValue >= 50 && batteryValue < 100) {
                ivBatteryIcon.setImageResource(R.drawable.battery70);
            } else if (batteryValue > 0 && batteryValue < 50) {
                ivBatteryIcon.setImageResource(R.drawable.battery30);
            } else if (batteryValue == 0) {
                ivBatteryIcon.setImageResource(R.drawable.battery0);
            }

            //设置电量值，并设置没电时的电量图标
            if (batteryValue >= 0 && batteryValue <= 100) {
                tvBatteryValue.setText(String.valueOf(batteryValue) + "%");
            } else {
                ivBatteryIcon.setImageResource(R.drawable.battery0);
                tvBatteryValue.setText(R.string.state_null);
            }
        }
    }

    /**
     * 注册广播监听
     */
    private void registerMonitor() {
        mMonitor = new CupMonitor();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Cup.ACTION_BLUETOOTHCUP_SENSOR);
        filter.addAction(Cup.ACTION_BLUETOOTHCUP_RECORD_COMPLETE);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_CONNECTED);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_CONNECTING);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_DISCONNECTED);
        filter.addAction(OznerDeviceManager.ACTION_OZNER_MANAGER_DEVICE_CHANGE);
        getContext().registerReceiver(mMonitor, filter);
    }

    /**
     * 注销广播监听
     */
    private void releaseMonitor() {
        if (isThisAdd()) {
            getContext().unregisterReceiver(mMonitor);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @OnClick({R.id.iv_setting, R.id.llay_tds_detail})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_setting:
                if (mCup != null) {
                    Intent setIntent = new Intent(getContext(), SetUpCupActivity.class);
                    setIntent.putExtra(SetupTapActivity.PARMS_MAC, mCup.Address());
                    startActivity(setIntent);
                } else {
                    Toast.makeText(getContext(), R.string.Not_found_device, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.llay_tds_detail:
                break;
        }
    }


    class CupMonitor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive: " + mCup.toString());
            refreshUIData();
        }
    }
}
