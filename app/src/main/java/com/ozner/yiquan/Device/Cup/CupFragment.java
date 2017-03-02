package com.ozner.yiquan.Device.Cup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ozner.yiquan.Bean.Contacts;
import com.ozner.yiquan.Command.UserDataPreference;
import com.ozner.cup.Cup;
import com.ozner.cup.CupRecord;
import com.ozner.yiquan.DBHelper.DBManager;
import com.ozner.yiquan.DBHelper.OznerDeviceSettings;
import com.ozner.yiquan.Device.DeviceFragment;
import com.ozner.yiquan.Device.TDSSensorManager;
import com.ozner.yiquan.Main.MainActivity;
import com.ozner.yiquan.MyCenter.Settings.MeasurementUnit;
import com.ozner.yiquan.R;
import com.ozner.yiquan.UIView.TdsDetailProgress;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


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
    @InjectView(R.id.tv_tdsRank)
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
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    private Cup mCup;
    private int oldTdsValue, oldVolumeValue;
    private CupMonitor mMonitor;
    Calendar recordCal = Calendar.getInstance();
    private TDSSensorManager tdsSensroManager;
    private int beatPer = 0;
    private int volumeRank = 0;
    private OznerDeviceSettings oznerSetting;
    private String mUserid;


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
        Log.e(TAG, "setDevice: " + device.Address());
        oznerSetting = DBManager.getInstance(getContext()).getDeviceSettings(mUserid, device.Address());
        if (mCup != null) {
            if (!mCup.Address().equals(device.Address())) {
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
        mUserid = UserDataPreference.GetUserData(getContext(), UserDataPreference.UserId, "");
        tdsSensroManager = new TDSSensorManager(getContext());
        initAnimation();
        try {
            Bundle bundle = getArguments();
            mCup = (Cup) OznerDeviceManager.Instance().getDevice(bundle.getString(DeviceAddress));
            oldTdsValue = 0;
            oldVolumeValue = 0;
            oznerSetting = DBManager.getInstance(getContext()).getDeviceSettings(mUserid, mCup.Address());
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
        toolbar.setTitle("");
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).initActionBarToggle(toolbar);
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * 设置toolbar背景色
     *
     * @param resId
     */
    protected void setToolbarColor(int resId) {
        if (isThisAdd())
            toolbar.setBackgroundColor(ContextCompat.getColor(getContext(), resId));
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
        try {
            setBarColor(R.color.cup_detail_bg);
            setToolbarColor(R.color.cup_detail_bg);
        } catch (Exception ex) {
            Log.e(TAG, "onResume_Ex: " + ex.getMessage());
        }
        title.setText(getString(R.string.smart_glass));
        initRecordCal();
        refreshUIData();
        super.onResume();
    }

//
//    @Override
//    public void onAttach(Context context) {
//        Log.e(TAG, "onAttach: ");
//        try {
//            if (isThisAdd())
//                title.setText(getString(R.string.smart_glass));
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            Log.e(TAG, "onAttach_Ex: " + ex.getMessage());
//        }
//        super.onAttach(context);
//    }

    private boolean isThisAdd() {
        return CupFragment.this.isAdded() && !CupFragment.this.isRemoving();
    }

    /**
     * 刷新UI数据
     */
    @Override
    protected void refreshUIData() {
        if (isThisAdd() && mCup != null) {
//            Log.e(TAG, "refreshUIData: " + mCup.Sensor().toString());
            if (oznerSetting != null && oznerSetting.getName() != null && !oznerSetting.getName().isEmpty()) {
                title.setText(oznerSetting.getName());
            } else {
                title.setText(mCup.getName());
            }
            refreshConnectState();
            refreshWaterGoal();
            showWaterTarget();
            if (mCup.connectStatus() == BaseDeviceIO.ConnectStatus.Connected) {
                refreshSensorData();
            } else {
                showNoData();
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
//            Log.e(TAG, "refreshSensorData: " + mCup.toString());
            showTdsState(mCup.Sensor().TDSFix);
            showPowerState();
            showWaterTemp();
        }
    }

    /**
     * 刷新饮水目标
     */
    private void refreshWaterGoal() {
        if (mCup != null) {
            int waterGoal = 2000;// = (int) mCup.Setting().get(Contacts.DEV_USER_WATER_GOAL, -1);
//            if (-1 == waterGoal) {
//                waterGoal = 2000;
//            }
            try {
                if (oznerSetting != null && oznerSetting.getAppData(Contacts.DEV_USER_WATER_GOAL) != null) {
                    waterGoal = Integer.parseInt((String) oznerSetting.getAppData(Contacts.DEV_USER_WATER_GOAL));
                }
            } catch (Exception ex) {
            }
            int unit = Integer.parseInt(UserDataPreference.GetUserData(getContext(),
                    UserDataPreference.VolUnit, String.valueOf(MeasurementUnit.VolumUnit.ML)));
            switch (unit) {
                case MeasurementUnit.VolumUnit.ML:
                    tvWaterGoal.setText(waterGoal + "ml");
                    break;
                case MeasurementUnit.VolumUnit.OZ:
                    tvWaterGoal.setText(String.format("%.2foz", waterGoal / MeasurementUnit.OZ_TO_G));
                    break;
                case MeasurementUnit.VolumUnit.DL:
                    tvWaterGoal.setText(String.format("%.2fdl", waterGoal / 100.f));
                    break;
                default:
                    tvWaterGoal.setText(waterGoal + "ml");
                    break;
            }
        }
    }

    /**
     * 显示饮水量
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

                if (oldVolumeValue != record.Volume) {
                    oldVolumeValue = record.Volume;
                    updateVolumeSensor(String.valueOf(record.Volume));
                }
            }
        }
    }

    /**
     * 更新当天饮水量获取好友内排名
     */
    private void updateVolumeSensor(final String volume) {
        if (mCup != null) {
            tdsSensroManager = new TDSSensorManager(getContext());
            tdsSensroManager.updateVolumeSensor(mCup.Address(), mCup.Type(), volume, new TDSSensorManager.TDSListener() {
                @Override
                public void onSuccess(int result) {
                    volumeRank = result;
                }

                @Override
                public void onFail(String msg) {
                    Log.e(TAG, "loadTdsFriendRank_onFail: " + msg);
                }
            });
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
            tvTdsRank.setVisibility(View.VISIBLE);
            tvTdsRank.setText(String.format(getString(R.string.beat_users), beatPer));

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
                tvTdsState.setText(R.string.bad);
            }

            //数字跑马灯
            if (tdsValue != 0) {
                tvTdsValue.setTextSize(NumSize);
                tvTdsValue.setText(String.valueOf(tdsValue));
                if (tdsValue > 250) {
                    tdsDetailProgress.update(100);
                } else {
                    tdsDetailProgress.update((tdsValue << 1) / 5);
                }
                if (oldTdsValue != tdsValue) {
                    oldTdsValue = tdsValue;
                    // TODO: 2016/12/8 updateTDSSensor
                    updateTdsSensor(tdsValue);
                }
            } else {
                showNoData();
            }
        }
    }

    /**
     * 上传TDS获取排名
     *
     * @param tds
     */
    private void updateTdsSensor(int tds) {
        if (tdsSensroManager != null && mCup != null) {
            tdsSensroManager.updateTds(mCup.Address(), mCup.Type(), String.valueOf(tds), null, null, new TDSSensorManager.TDSListener() {
                @Override
                public void onSuccess(int result) {
                    try {
                        beatPer = result;
                        refreshUIData();
                    } catch (Exception ex) {
                        Log.e(TAG, "onSuccess_Ex: " + ex.getMessage());
                    }
                }

                @Override
                public void onFail(String msg) {
                    Log.e(TAG, "onFail: " + msg);
                }
            });
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
        ivBatteryIcon.setImageResource(R.drawable.battery0);
        tvBatteryValue.setText(R.string.state_null);
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

    @Override
    public void onDetach() {
        System.gc();
        super.onDetach();
    }

    @OnClick({R.id.iv_setting, R.id.llay_tds_detail, R.id.rlay_water_volum, R.id.rlay_water_temp})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_setting:
                if (mCup != null) {
                    Intent setIntent = new Intent(getContext(), SetUpCupActivity.class);
                    setIntent.putExtra(Contacts.PARMS_MAC, mCup.Address());
                    startActivity(setIntent);
                } else {
                    Toast.makeText(getContext(), R.string.Not_found_device, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.llay_tds_detail:
                if (mCup != null) {
                    Intent tdsIntent = new Intent(getContext(), CupTDSActivity.class);
                    tdsIntent.putExtra(Contacts.PARMS_MAC, mCup.Address());
                    startActivity(tdsIntent);
                } else {
                    Toast.makeText(getContext(), R.string.Not_found_device, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.rlay_water_volum://饮水量详情
                if (mCup != null) {
                    Intent volumIntent = new Intent(getContext(), CupVolumActivity.class);
                    volumIntent.putExtra(Contacts.PARMS_MAC, mCup.Address());
                    volumIntent.putExtra(Contacts.PARMS_RANK, volumeRank);
                    startActivity(volumIntent);
                } else {
                    Toast.makeText(getContext(), R.string.Not_found_device, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.rlay_water_temp://水温详情
                if (mCup != null) {
                    Intent tempIntent = new Intent(getContext(), CupTempActivity.class);
                    tempIntent.putExtra(Contacts.PARMS_MAC, mCup.Address());
                    startActivity(tempIntent);
                } else {
                    Toast.makeText(getContext(), R.string.Not_found_device, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    class CupMonitor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
//            Log.e(TAG, "onReceive: " + mCup.toString());
            refreshUIData();
        }
    }
}
