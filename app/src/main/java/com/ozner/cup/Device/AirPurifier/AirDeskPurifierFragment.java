package com.ozner.cup.Device.AirPurifier;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ozner.AirPurifier.AirPurifier_Bluetooth;
import com.ozner.AirPurifier.AirPurifier_MXChip;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.OznerDeviceSettings;
import com.ozner.cup.Device.AirPurifier.bean.NetWeather;
import com.ozner.cup.Device.DeviceFragment;
import com.ozner.cup.Main.MainActivity;
import com.ozner.cup.R;
import com.ozner.cup.UIView.CProessbarView;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OperateCallback;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.ozner.cup.R.id.rlay_filterStatus;
import static com.ozner.device.BaseDeviceIO.ConnectStatus.Connected;


public class AirDeskPurifierFragment extends DeviceFragment {
    private static final String TAG = "AirDeskPurifier";
    private static final int CHANGE_SPEED = 0X01;
    private static final int FILTER_MAX_WORK_TIME = 60000;
    @InjectView(R.id.tv_pmState)
    TextView tvPmState;
    @InjectView(R.id.tv_pmValue)
    TextView tvPmValue;
    @InjectView(R.id.tv_airTemp)
    TextView tvAirTemp;
    @InjectView(R.id.tv_airShiDu)
    TextView tvAirShiDu;
    @InjectView(R.id.tv_deskAnionTips)
    TextView tvDeskAnionTips;
    @InjectView(R.id.iv_filterState)
    ImageView ivFilterState;
    @InjectView(R.id.tv_filiteState)
    TextView tvFiliteState;
    @InjectView(R.id.tv_filterValue)
    TextView tvFilterValue;
    @InjectView(rlay_filterStatus)
    RelativeLayout rlayFilterStatus;
    @InjectView(R.id.iv_purifierSetBtn)
    ImageView ivPurifierSetBtn;
    @InjectView(R.id.tv_address)
    TextView tvAddress;
    @InjectView(R.id.tv_air_outdoor)
    TextView tvAirOutdoor;
    @InjectView(R.id.tv_air_quality)
    TextView tvAirQuality;
    @InjectView(R.id.tv_outPM25)
    TextView tvOutPM25;
    @InjectView(R.id.tv_air_pm)
    TextView tvAirPm;
    @InjectView(R.id.rlay_air_outside)
    RelativeLayout rlayAirOutside;
    @InjectView(R.id.llay_top)
    LinearLayout llayTop;
    @InjectView(R.id.iv_deviceConnectIcon)
    ImageView ivDeviceConnectIcon;
    @InjectView(R.id.tv_deviceConnectTips)
    TextView tvDeviceConnectTips;
    @InjectView(R.id.llay_deviceConnectTip)
    LinearLayout llayDeviceConnectTip;
    @InjectView(R.id.cproessbarView)
    CProessbarView cproessbarView;
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    private AirPurifier_Bluetooth mDeskAirPurifier;
    private AirPurifierMonitor airMonitor;
    private String deviceNewName = "";
    private int oldSpeed = 0;
    private NetWeather netWeather;
    AirPurifierPresenter airPresenter;
    private String mUserid;
    private OznerDeviceSettings oznerSetting;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (CHANGE_SPEED == msg.what) {
                int present = msg.arg1;
                if (isThisAdd() && mDeskAirPurifier != null
                        && mDeskAirPurifier.connectStatus() == Connected) {

                    if (present > 1) {
                        if (!mDeskAirPurifier.status().Power()) {
                            mDeskAirPurifier.status().setPower(true, new OperateCallback<Void>() {
                                @Override
                                public void onSuccess(Void var1) {
                                    Log.e(TAG, "onSuccess_power_true: ");
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            refreshUIData();
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(Throwable var1) {
                                    Log.e(TAG, "onFailure_power_true: ");
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            refreshUIData();
                                        }
                                    });
                                }
                            });
                        }

                        mDeskAirPurifier.status().setRPM((byte) present, new OperateCallback<Void>() {
                            @Override
                            public void onSuccess(Void var1) {
                                Log.e(TAG, "onSuccess_RPM: ");
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        refreshUIData();
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Throwable var1) {
                                Log.e(TAG, "onFailure_RPM: ");
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        refreshUIData();
                                    }
                                });
                                showCenterToast(R.string.send_status_fail);
                            }
                        });

                    } else {
                        mDeskAirPurifier.status().setPower(false, new OperateCallback<Void>() {
                            @Override
                            public void onSuccess(Void var1) {
                                Log.e(TAG, "onSuccess_power_false: ");
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        refreshUIData();
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Throwable var1) {
                                Log.e(TAG, "onFailure_power_false: ");

                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        refreshUIData();
                                    }
                                });
                                showCenterToast(R.string.send_status_fail);
                            }
                        });
                    }

                } else {
                    showCenterToast(R.string.device_disConnect);
                }
            }
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
        AirDeskPurifierFragment fragment = new AirDeskPurifierFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DeviceAddress, mac);
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mUserid = UserDataPreference.GetUserData(getContext(), UserDataPreference.UserId, "");
        airPresenter = new AirPurifierPresenter(getContext());
        getOutDoorInfo();
        initAnimation();
        try {
            Bundle bundle = getArguments();
            mDeskAirPurifier = (AirPurifier_Bluetooth) OznerDeviceManager.Instance().getDevice(bundle.getString(DeviceAddress));
            oznerSetting = DBManager.getInstance(getContext()).getDeviceSettings(mUserid, mDeskAirPurifier.Address());
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "onCreate_Ex: " + ex.getMessage());
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_air_desk_purifier, container, false);
        ButterKnife.inject(this, view);
        toolbar.setTitle("");
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        cproessbarView.setOnValueChangeListener(new ValueChangeListener());
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).initActionBarToggle(toolbar);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void setDevice(OznerDevice device) {
        oznerSetting = DBManager.getInstance(getContext()).getDeviceSettings(mUserid, mDeskAirPurifier.Address());
//        refreshMainOutDoorInfo();
        getOutDoorInfo();
        deviceNewName = "";
        if (mDeskAirPurifier != null) {
            if (mDeskAirPurifier.Address() != device.Address()) {
                mDeskAirPurifier = null;
                mDeskAirPurifier = (AirPurifier_Bluetooth) device;
                refreshUIData();
                showFilterStatus(mDeskAirPurifier.sensor().FilterStatus().workTime);
            }
        } else {
            mDeskAirPurifier = (AirPurifier_Bluetooth) device;
            refreshUIData();
            showFilterStatus(mDeskAirPurifier.sensor().FilterStatus().workTime);
        }
    }

    @OnClick({R.id.iv_purifierSetBtn, R.id.rlay_filterStatus, R.id.llay_center_detail, R.id.rlay_air_outside})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_purifierSetBtn:
                if (mDeskAirPurifier != null) {
                    Intent setupIntent = new Intent(getContext(), SetUpAirVerActivity.class);
                    setupIntent.putExtra(Contacts.PARMS_MAC, mDeskAirPurifier.Address());
                    startActivity(setupIntent);
                } else {
                    showCenterToast(R.string.Not_found_device);
                }
                break;
            case R.id.llay_center_detail:
            case R.id.rlay_filterStatus:
                if (mDeskAirPurifier != null) {
                    Intent filterIntent = new Intent(getContext(), AirDeskFilterActivity.class);
                    filterIntent.putExtra(Contacts.PARMS_MAC, mDeskAirPurifier.Address());
                    startActivity(filterIntent);
                } else {
                    showCenterToast(R.string.Not_found_device);
                }
                break;
            case R.id.rlay_air_outside:
                showOutDoorInfo();
                break;
        }
    }

    /**
     * 获取室外天气信息
     */
    private void getOutDoorInfo() {
        try {
            if (netWeather == null) {
                airPresenter.getWeatherOutSide(new AirPurifierPresenter.NetWeatherResult() {
                    @Override
                    public void onResult(NetWeather weather) {
                        netWeather = weather;
                        if (isThisAdd()) {
                            refreshMainOutDoorInfo();
                        }
                        Log.e(TAG, "getWeatherOutSide_onResult: " + weather.toString());
                    }
                });
            } else {
                refreshMainOutDoorInfo();
            }
        } catch (Exception ex) {
            Log.e(TAG, "setDevice_Ex: " + ex.getMessage());
        }
    }

    /**
     * 主页显示室外
     */
    private void refreshMainOutDoorInfo() {
        try {
            if (netWeather != null && isThisAdd()) {
                tvOutPM25.setText(netWeather.getPm25());
                tvAddress.setText(netWeather.getCity());
                switch (netWeather.getQlty()) {
                    case "优":
                        tvAirQuality.setText(R.string.excellent);
                        break;
                    case "良":
                        tvAirQuality.setText(R.string.good);
                        break;
                    case "差":
                        tvAirQuality.setText(R.string.bads);
                        break;
                    default:
                        tvAirQuality.setText(R.string.state_null);
                        break;
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "refreshMainOutDoorInfo_Ex: " + ex.getMessage());
        }
    }


    /**
     * 显示室外信息
     */
    private void showOutDoorInfo() {
        final Dialog airDialog = new Dialog(getContext(), R.style.SelectPicBaseStyle);
        airDialog.setContentView(R.layout.air_outside_details);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        WindowManager.LayoutParams lp2 = airDialog.getWindow().getAttributes();
        lp2.width = display.getWidth();
        Window window2 = airDialog.getWindow();
        window2.setGravity(Gravity.BOTTOM);
        window2.setAttributes(lp2);
        window2.setWindowAnimations(R.style.SelectPicAnimationStyle);
        airDialog.findViewById(R.id.tv_air_know).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                airDialog.cancel();
            }
        });
        if (netWeather != null) {
            ((TextView) airDialog.findViewById(R.id.tv_outside_city)).setText(netWeather.getCity());
            ((TextView) airDialog.findViewById(R.id.tv_outside_pm)).setText(netWeather.getPm25());
            ((TextView) airDialog.findViewById(R.id.tv_outside_aqi)).setText(netWeather.getAqi());
            ((TextView) airDialog.findViewById(R.id.tv_outside_temp)).setText(netWeather.getTmp());
            ((TextView) airDialog.findViewById(R.id.tv_airOutside_humidity)).setText(netWeather.getHum());
//            ((TextView) airDialog.findViewById(R.id.tv_outside_data)).setText(netWeather.getWeatherform());
            ((TextView) airDialog.findViewById(R.id.tv_outside_data)).setText(String.format("%s  %s",netWeather.getWeatherform(),netWeather.getUpdateLocTime()));
        }
        airDialog.show();
    }


    @Override
    protected void refreshUIData() {
        try {
            if (isThisAdd() && mDeskAirPurifier != null) {
                showDeviceName();
                refreshConnectState();
                refreshSensorData();
                setUISwitch(mDeskAirPurifier.status().RPM());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "refreshUIData_Ex: " + ex.getMessage());
        }
    }

    /**
     * 刷新设备连接状态
     */
    private void refreshConnectState() {
        if (mDeskAirPurifier != null) {
            if (ivDeviceConnectIcon.getAnimation() == null) {
                ivDeviceConnectIcon.setAnimation(rotateAnimation);
            }

            if (mDeskAirPurifier.connectStatus() == BaseDeviceIO.ConnectStatus.Connecting) {
                llayDeviceConnectTip.setVisibility(View.VISIBLE);
                tvDeviceConnectTips.setText(R.string.device_connecting);
                ivDeviceConnectIcon.setImageResource(R.drawable.data_loading);
                if (ivDeviceConnectIcon.getAnimation() != null && !ivDeviceConnectIcon.getAnimation().hasStarted()) {
                    ivDeviceConnectIcon.getAnimation().start();
                }
            } else if (mDeskAirPurifier.connectStatus() == Connected) {
                llayDeviceConnectTip.setVisibility(View.INVISIBLE);
                if (ivDeviceConnectIcon.getAnimation() != null) {
                    ivDeviceConnectIcon.getAnimation().cancel();
                }
            } else if (mDeskAirPurifier.connectStatus() == BaseDeviceIO.ConnectStatus.Disconnect) {
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
     * 设置设备名字
     */
    private void showDeviceName() {
        if (!deviceNewName.equals(mDeskAirPurifier.getName())) {
            deviceNewName = mDeskAirPurifier.getName();

//            ((MainActivity) getActivity()).setCustomTitle(mDeskAirPurifier.getName());
        }
    }

    /**
     * 刷新传感器数据
     */
    private void refreshSensorData() {
        if (mDeskAirPurifier != null) {
            showPM25(mDeskAirPurifier.sensor().PM25());
            showTemp(mDeskAirPurifier.sensor().Temperature());
            showHumidity(mDeskAirPurifier.sensor().Humidity());
        }
    }


    /**
     * 设置开关值
     *
     * @param present
     */
    private void setUISwitch(int present) {
        oldSpeed = present;
        if (mDeskAirPurifier != null && mDeskAirPurifier.status().Power()) {
            cproessbarView.updateValue(oldSpeed);
        } else {
            cproessbarView.updateValue(0);
        }
    }

    /**
     * 设置PM2.5
     *
     * @param pm25
     */
    private void showPM25(int pm25) {
        if (mDeskAirPurifier.connectStatus() == Connected) {
            if (mDeskAirPurifier.status().Power()) {
                tvDeskAnionTips.setVisibility(View.VISIBLE);
                if (pm25 > 0 && pm25 < 1000) {
                    if (pm25 < 75) {
                        tvPmState.setText(R.string.excellent);
                        setBarColor(R.color.air_good_bg);
                        setToolbarColor(R.color.air_good_bg);
//                        toolbar.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.air_good_bg));
                        llayTop.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.air_good_bg));
                    } else if (pm25 >= 75 && pm25 < 150) {
                        tvPmState.setText(R.string.good);
                        setBarColor(R.color.air_soso_bg);
                        setToolbarColor(R.color.air_soso_bg);
                        llayTop.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.air_soso_bg));
                    } else if (pm25 >= 150) {
                        tvPmState.setText(R.string.bads);
                        setBarColor(R.color.air_bad_bg);
                        setToolbarColor(R.color.air_bad_bg);
                        llayTop.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.air_bad_bg));
                    }
                    tvPmValue.setAlpha(1.0f);
                    tvPmValue.setText(String.valueOf(pm25));
                } else {
                    tvPmValue.setText(R.string.no_data);
                    tvPmState.setText("-");
                }
            } else {
                showDeviceClose();
            }
        } else {
            showDeviceDisConn();
        }
    }

    /**
     * 显示设备关机
     */
    private void showDeviceClose() {
        tvDeskAnionTips.setVisibility(View.INVISIBLE);
        tvPmState.setText(R.string.state_null);
        tvPmValue.setText(R.string.device_close);
        tvPmValue.setAlpha(0.6f);
        cproessbarView.updateValue(0);
    }

    /**
     * 显示设备连接已断开
     */
    private void showDeviceDisConn() {
        tvPmValue.setText(R.string.device_dis_connect);
        tvPmValue.setAlpha(0.6f);
        tvPmState.setText(R.string.state_null);
        tvAirTemp.setText("-");
        tvAirShiDu.setText("-");
        cproessbarView.updateValue(0);
        tvFilterValue.setText("0%");
    }

    /**
     * 设置温度
     *
     * @param temp
     */
    private void showTemp(int temp) {
//        if (oldTemp != temp) {
        if (65535 != temp) {
            tvAirTemp.setText(temp + "℃");
        } else {
            tvAirTemp.setText("-");
        }
//        }
    }

    /**
     * 设置湿度
     *
     * @param hum
     */
    private void showHumidity(int hum) {
//        if (oldHum != hum) {
        if (65535 != hum) {
            tvAirShiDu.setText(hum + "%");
        } else {
            tvAirShiDu.setText("-");
        }
//        }
    }

    /**
     * 显示滤芯状态
     */
    private void showFilterStatus(float workTime) {
        try {
            if (isThisAdd()) {
                float maxWorkTime = mDeskAirPurifier.sensor().FilterStatus().maxWorkTime;
                if (maxWorkTime <= 0) {
                    maxWorkTime = FILTER_MAX_WORK_TIME;
                }
                if (workTime > maxWorkTime) {
                    workTime = maxWorkTime;
                }

                int ret = 0;
                if (maxWorkTime != 0)
                    ret = Math.round((1 - (workTime / maxWorkTime)) * 100);
                tvFilterValue.setText(String.format("%d%%", ret));
            }
        } catch (Exception ex) {
            Log.e(TAG, "showFilterStatus_Ex: " + ex.getMessage());
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
    public void onResume() {
        try {
            if (oznerSetting != null) {
                title.setText(oznerSetting.getName());
            } else {
                title.setText(R.string.air_purifier);
            }
            initBgColor();
        } catch (Exception ex) {
            Log.e(TAG, "onResume_Ex:" + ex.getMessage());
        }
        refreshUIData();
        refreshMainOutDoorInfo();
        if (mDeskAirPurifier != null)
            showFilterStatus(mDeskAirPurifier.sensor().FilterStatus().workTime);
        super.onResume();
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


    /**
     * 初始化背景色
     */
    private void initBgColor() {
        setBarColor(R.color.air_good_bg);
        setToolbarColor(R.color.air_good_bg);
        llayTop.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.air_good_bg));
    }

    /**
     * 注册广播接收器
     */
    private void registerMonitor() {
        airMonitor = new AirPurifierMonitor();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        filter.addAction(OznerDeviceManager.ACTION_OZNER_MANAGER_DEVICE_CHANGE);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_CONNECTING);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_DISCONNECTED);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_CONNECTED);

        filter.addAction(AirPurifier_MXChip.ACTION_AIR_PURIFIER_SENSOR_CHANGED);
        filter.addAction(AirPurifier_MXChip.ACTION_AIR_PURIFIER_STATUS_CHANGED);
        getContext().registerReceiver(airMonitor, filter);
    }

    /**
     * 注销广播接收器
     */
    private void releaseMonitor() {
        if (isThisAdd() && airMonitor != null) {
            getContext().unregisterReceiver(airMonitor);
        }
    }

    private boolean isThisAdd() {
        return AirDeskPurifierFragment.this.isAdded() && !AirDeskPurifierFragment.this.isRemoving() && !AirDeskPurifierFragment.this.isDetached();
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


    class ValueChangeListener implements CProessbarView.ValueChangeListener {

        @Override
        public void onValueChange(final int present) {
            try {
                mHandler.removeMessages(CHANGE_SPEED);

                Message message = mHandler.obtainMessage();
                message.what = CHANGE_SPEED;
                message.arg1 = present;
                mHandler.sendMessageDelayed(message, 500);
            } catch (Exception ex) {
                Log.e(TAG, "ValueChange_Ex: " + ex.getMessage());
            }
        }
    }

    class AirPurifierMonitor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BaseDeviceIO.ACTION_DEVICE_CONNECTED)
                    || intent.getAction().equals(BaseDeviceIO.ACTION_DEVICE_CONNECTING)
                    || intent.getAction().equals(BaseDeviceIO.ACTION_DEVICE_DISCONNECTED)) {
                showFilterStatus(mDeskAirPurifier.sensor().FilterStatus().workTime);
            }
            refreshUIData();
        }
    }
}
