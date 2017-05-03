package com.ozner.cup.Device.AirPurifier;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntDef;
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

import com.ozner.AirPurifier.AirPurifier_MXChip;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.OznerDeviceSettings;
import com.ozner.cup.Device.AirPurifier.bean.NetWeather;
import com.ozner.cup.Device.DeviceFragment;
import com.ozner.cup.HttpHelper.NetState;
import com.ozner.cup.Main.MainActivity;
import com.ozner.cup.R;
import com.ozner.cup.Utils.LCLogUtils;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OperateCallback;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import java.util.Calendar;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static android.R.attr.mode;
import static com.ozner.cup.R.color.air_soso_bg;
import static com.ozner.cup.R.id.rlay_air_outside;
import static com.ozner.cup.R.id.tv_air_quality;


public class AirVerPurifierFragment extends DeviceFragment {
    private static final String TAG = "AirVerPurifier";
    @InjectView(R.id.tv_deviceConnectTips)
    TextView tvDeviceConnectTips;
    @InjectView(R.id.llay_deviceConnectTip)
    LinearLayout llayDeviceConnectTip;
    @InjectView(R.id.llay_top)
    LinearLayout llayTop;
    @InjectView(R.id.tv_pmState)
    TextView tvPmState;
    @InjectView(R.id.tv_pmValue)
    TextView tvPmValue;
    @InjectView(R.id.tv_airVOC)
    TextView tvAirVOC;
    @InjectView(R.id.tv_airTemp)
    TextView tvAirTemp;
    @InjectView(R.id.tv_airShiDu)
    TextView tvAirShiDu;
    @InjectView(R.id.iv_filterState)
    ImageView ivFilterState;
    @InjectView(R.id.tv_filiteState)
    TextView tvFiliteState;
    @InjectView(R.id.tv_filterValue)
    TextView tvFilterValue;
    @InjectView(R.id.rlay_filterStatus)
    RelativeLayout rlayFilterStatus;
    @InjectView(R.id.iv_purifierSetBtn)
    ImageView ivPurifierSetBtn;
    @InjectView(R.id.tv_address)
    TextView tvAddress;
    @InjectView(tv_air_quality)
    TextView tvAirQuality;
    @InjectView(R.id.tv_outPM25)
    TextView tvOutPM25;
    @InjectView(rlay_air_outside)
    RelativeLayout rlayAirOutside;
    @InjectView(R.id.tv_powerSwitch)
    TextView tvPowerSwitch;
    @InjectView(R.id.iv_poserSwitch)
    ImageView ivPoserSwitch;
    @InjectView(R.id.llay_Switch)
    LinearLayout llaySwitch;
    @InjectView(R.id.rlay_powerSwitch)
    RelativeLayout rlayPowerSwitch;
    @InjectView(R.id.llay_open)
    LinearLayout llayOpen;
    @InjectView(R.id.tv_modeSwitch)
    TextView tvModeSwitch;
    @InjectView(R.id.iv_modeSwitch)
    ImageView ivModeSwitch;
    @InjectView(R.id.rlay_modeSwitch)
    RelativeLayout rlayModeSwitch;
    @InjectView(R.id.llay_mode)
    LinearLayout llayMode;
    @InjectView(R.id.tv_lockSwitch)
    TextView tvLockSwitch;
    @InjectView(R.id.iv_lockSwitch)
    ImageView ivLockSwitch;
    @InjectView(R.id.rlay_lockSwitch)
    RelativeLayout rlayLockSwitch;
    @InjectView(R.id.llay_lock)
    LinearLayout llayLock;
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    private AirPurifier_MXChip mVerAirPurifier;
    AirPurifierMonitor airMonitor;
    private ProgressDialog progressDialog;
    private Handler mHandler = new Handler();
    AirPurifierPresenter airPresenter;
    private NetWeather netWeather;
    private String mUserid;
    private OznerDeviceSettings oznerSetting;

    /**
     * 定义模式开关注解，限定设置模式方法的参数
     */
    @IntDef({AirPurifier_MXChip.FAN_SPEED_AUTO, AirPurifier_MXChip.FAN_SPEED_POWER, AirPurifier_MXChip.FAN_SPEED_SILENT})
    public @interface AIR_Mode {

    }

    /**
     * 显示等待框
     */
    private void showProgressDialog(String msg) {
        hideProgressDialog();
        progressDialog = ProgressDialog.show(getContext(), null, msg);
    }

    /**
     * 取消等待框
     */
    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
            progressDialog = null;
        }
    }

    /**
     * 实例化Fragment
     *
     * @param mac
     *
     * @return
     */
    public static DeviceFragment newInstance(String mac) {
        AirVerPurifierFragment fragment = new AirVerPurifierFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DeviceAddress, mac);
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mUserid = OznerPreference.GetValue(getContext(), OznerPreference.UserId, "");
        airPresenter = new AirPurifierPresenter(getContext());
        getOutDoorInfo();
        try {
            Bundle bundle = getArguments();
            mVerAirPurifier = (AirPurifier_MXChip) OznerDeviceManager.Instance().getDevice(bundle.getString(DeviceAddress));
            oznerSetting = DBManager.getInstance(getContext()).getDeviceSettings(mUserid, mVerAirPurifier.Address());
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
        View view = inflater.inflate(R.layout.fragment_air_ver_purifier, container, false);
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

    @Override
    public void setDevice(OznerDevice device) {
        oznerSetting = DBManager.getInstance(getContext()).getDeviceSettings(mUserid, device.Address());
//        refreshMainOutDoorInfo();
        getOutDoorInfo();
//        initColor();
        if (mVerAirPurifier != null) {
            if (mVerAirPurifier.Address() != device.Address()) {
                mVerAirPurifier = null;
                mVerAirPurifier = (AirPurifier_MXChip) device;
                refreshUIData();
            }
        } else {
            mVerAirPurifier = (AirPurifier_MXChip) device;
            refreshUIData();
        }
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
//        if (isThisAdd() && airMonitor != null) {
        getContext().unregisterReceiver(airMonitor);
//        }
    }

    @Override
    public void onStart() {
//        registerMonitor();
        super.onStart();
    }

    @Override
    public void onStop() {
//        releaseMonitor();
        super.onStop();
    }

    @Override
    public void onPause() {
        releaseMonitor();
        super.onPause();
    }

    private boolean isThisAdd() {
        return AirVerPurifierFragment.this.isAdded() && !AirVerPurifierFragment.this.isRemoving() && !AirVerPurifierFragment.this.isDetached();
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
        registerMonitor();
        refreshUIData();
        refreshMainOutDoorInfo();
        super.onResume();
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
                        if (weather != null)
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
                        tvAirQuality.setText(netWeather.getQlty());
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
//            String from = netWeather.getWeatherform() + "(" + netWeather.getUpdateLocTime()+")";
//            ((TextView) airDialog.findViewById(R.id.tv_outside_data)).setText(netWeather.getWeatherform());
            ((TextView) airDialog.findViewById(R.id.tv_outside_data)).setText(String.format("%s  %s", netWeather.getWeatherform(), netWeather.getUpdateLocTime()));
        }
        airDialog.show();
    }

    /**
     * 显示模式选择
     */
    private void showModePopWindow(int mode) {
        final Dialog airDialog = new Dialog(getContext(), R.style.SelectPicBaseStyle);
        airDialog.setContentView(R.layout.air_mode_pop_layout);
        airDialog.setCanceledOnTouchOutside(true);
        switch (mode) {
            case AirPurifier_MXChip.FAN_SPEED_AUTO:
                airDialog.findViewById(R.id.iv_AutoSwitch).setSelected(true);
                break;
            case AirPurifier_MXChip.FAN_SPEED_POWER:
                airDialog.findViewById(R.id.iv_StrongSwitch).setSelected(true);
                break;
            case AirPurifier_MXChip.FAN_SPEED_SILENT:
                airDialog.findViewById(R.id.iv_SlientSwitch).setSelected(true);
                break;
        }
        airDialog.findViewById(R.id.rlay_AutoSwitch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: Auto");
                airDialog.findViewById(R.id.iv_AutoSwitch).setSelected(true);
                airDialog.findViewById(R.id.iv_StrongSwitch).setSelected(false);
                airDialog.findViewById(R.id.iv_SlientSwitch).setSelected(false);
                setSpeedMode(AirPurifier_MXChip.FAN_SPEED_AUTO);
                airDialog.cancel();
            }
        });
        airDialog.findViewById(R.id.rlay_StrongSwitch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                airDialog.findViewById(R.id.iv_AutoSwitch).setSelected(false);
                airDialog.findViewById(R.id.iv_StrongSwitch).setSelected(true);
                airDialog.findViewById(R.id.iv_SlientSwitch).setSelected(false);
                setSpeedMode(AirPurifier_MXChip.FAN_SPEED_POWER);

                Log.e(TAG, "onClick: Strong");
                airDialog.cancel();
            }
        });
        airDialog.findViewById(R.id.rlay_SlientSwitch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: Slient");
                airDialog.findViewById(R.id.iv_AutoSwitch).setSelected(false);
                airDialog.findViewById(R.id.iv_StrongSwitch).setSelected(false);
                airDialog.findViewById(R.id.iv_SlientSwitch).setSelected(true);
                setSpeedMode(AirPurifier_MXChip.FAN_SPEED_SILENT);
                airDialog.cancel();
            }
        });

        WindowManager.LayoutParams lp2 = airDialog.getWindow().getAttributes();
        lp2.y = llayMode.getHeight();
        Window window2 = airDialog.getWindow();
        window2.setGravity(Gravity.CENTER);

        window2.setAttributes(lp2);
        window2.setWindowAnimations(R.style.SelectPicAnimationStyle);
        airDialog.show();
    }

    @OnClick({R.id.llay_center_detail, R.id.rlay_filterStatus,
            R.id.iv_purifierSetBtn, R.id.llay_open,
            R.id.llay_mode, R.id.llay_lock, R.id.rlay_air_outside})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.llay_center_detail:
            case R.id.rlay_filterStatus:
                if (mVerAirPurifier != null) {
                    Intent filterIntent = new Intent(getContext(), AirVerFilterActivity.class);
                    filterIntent.putExtra(Contacts.PARMS_MAC, mVerAirPurifier.Address());
                    startActivity(filterIntent);
                } else {
                    showCenterToast(R.string.Not_found_device);
                }
                break;
            case R.id.iv_purifierSetBtn:
                if (mVerAirPurifier != null) {
                    Intent setupIntent = new Intent(getContext(), SetUpAirVerActivity.class);
                    setupIntent.putExtra(Contacts.PARMS_MAC, mVerAirPurifier.Address());
                    startActivity(setupIntent);
                } else {
                    showCenterToast(R.string.Not_found_device);
                }
                break;
            case R.id.llay_open:
                Log.e(TAG, "onClick: llay_open");
//                showCenterToast(R.string.about_tap);
                if (mVerAirPurifier != null) {
                    setPower(mVerAirPurifier.airStatus().Power());
                } else {
                    showCenterToast(R.string.Not_found_device);
                }
                break;
            case R.id.llay_mode:
                if (mVerAirPurifier != null) {
                    showModePopWindow(mVerAirPurifier.airStatus().speed());
                } else {
                    showCenterToast(R.string.Not_found_device);
                }
                break;
            case R.id.llay_lock:
                if (mVerAirPurifier != null) {
                    setLock(mVerAirPurifier.airStatus().Lock());
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
     * 设置toolbar背景色
     *
     * @param resId
     */
    protected void setToolbarColor(int resId) {
        if (isThisAdd()) {
            toolbar.setBackgroundColor(ContextCompat.getColor(getContext(), resId));
        }
    }


    /**
     * 设置电源
     *
     * @param nowPowerState 电源当前的状态
     */
    private void setPower(boolean nowPowerState) {
        if (mVerAirPurifier != null && mode != mVerAirPurifier.airStatus().speed()) {
            if (NetState.checkNetwork(getContext()) == NetState.State.CONNECTED) {
                if (!mVerAirPurifier.isOffline()) {
                    showProgressDialog(getString(R.string.command_sending));
                    mVerAirPurifier.airStatus().setPower(!nowPowerState, new OperateCallback<Void>() {
                        @Override
                        public void onSuccess(Void var1) {
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    hideProgressDialog();
                                    refreshUIData();
                                }
                            }, 300);
                        }

                        @Override
                        public void onFailure(Throwable var1) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    refreshUIData();
                                    hideProgressDialog();
                                    showCenterToast(R.string.send_status_fail);
                                }
                            });
                        }
                    });
                } else {
                    showCenterToast(R.string.device_no_net);
                }
            } else {
                showCenterToast(R.string.phone_nonet);
            }
        } else {
            showCenterToast(R.string.Not_found_device);
        }
        refreshUIData();
    }


    /**
     * 设置风速模式
     *
     * @param mode
     */
    private void setSpeedMode(int mode) {
        if (mVerAirPurifier != null) {
            if (mode != mVerAirPurifier.airStatus().speed()) {
                if (NetState.checkNetwork(getContext()) == NetState.State.CONNECTED) {
                    if (!mVerAirPurifier.isOffline()) {
                        if (mVerAirPurifier.airStatus().Power()) {
                            showProgressDialog(getString(R.string.command_sending));
                            mVerAirPurifier.airStatus().setSpeed(mode, new OperateCallback<Void>() {
                                @Override
                                public void onSuccess(Void var1) {
                                    mHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideProgressDialog();
                                            refreshUIData();
                                        }
                                    }, 300);
                                }

                                @Override
                                public void onFailure(Throwable var1) {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            refreshUIData();
                                            hideProgressDialog();
                                            showCenterToast(R.string.send_status_fail);
                                        }
                                    });
                                }
                            });
                        } else {
                            showCenterToast(R.string.please_open_power);
                        }
                    } else {
                        showCenterToast(R.string.device_no_net);
                    }
                } else {
                    showCenterToast(R.string.phone_nonet);
                }
            }
        } else {
            showCenterToast(R.string.Not_found_device);
        }
        refreshUIData();
    }

    /**
     * 设置童锁
     *
     * @param nowLockState 当前童锁状态
     */
    private void setLock(boolean nowLockState) {
        if (mVerAirPurifier != null && mode != mVerAirPurifier.airStatus().speed()) {
            if (NetState.checkNetwork(getContext()) == NetState.State.CONNECTED) {
                if (!mVerAirPurifier.isOffline()) {
                    if (mVerAirPurifier.airStatus().Power()) {
                        showProgressDialog(getString(R.string.command_sending));
                        mVerAirPurifier.airStatus().setLock(!nowLockState, new OperateCallback<Void>() {
                            @Override
                            public void onSuccess(Void var1) {
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        hideProgressDialog();
                                        refreshUIData();
                                    }
                                }, 300);
                            }

                            @Override
                            public void onFailure(Throwable var1) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        refreshUIData();
                                        hideProgressDialog();
                                        showCenterToast(R.string.send_status_fail);
                                    }
                                });
                            }
                        });
                    } else {
                        showCenterToast(R.string.please_open_power);
                    }
                } else {
                    showCenterToast(R.string.device_no_net);
                }
            } else {
                showCenterToast(R.string.phone_nonet);
            }
        } else {
            showCenterToast(R.string.Not_found_device);
        }
        refreshUIData();
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
     * 设置电源开关
     *
     * @param isOn
     */
    private void switchPower(boolean isOn) {
        rlayPowerSwitch.setSelected(isOn);
        tvPowerSwitch.setSelected(isOn);
        ivPoserSwitch.setSelected(isOn);
        if (!isOn) {
            switchMode(isOn, AirPurifier_MXChip.FAN_SPEED_AUTO);
            switchLock(isOn);
        }
    }


    /**
     * 设置模式开关
     *
     * @param isOn
     * @param mode
     */
    private void switchMode(boolean isOn, @AIR_Mode int mode) {
        rlayModeSwitch.setSelected(isOn);
        tvModeSwitch.setSelected(isOn);
        if (isOn) {
            switch (mode) {
                case AirPurifier_MXChip.FAN_SPEED_AUTO:
                    ivModeSwitch.setImageResource(R.drawable.air_auto_on);
                    break;
                case AirPurifier_MXChip.FAN_SPEED_POWER:
                    ivModeSwitch.setImageResource(R.drawable.air_strong_on);
                    break;
                case AirPurifier_MXChip.FAN_SPEED_SILENT:
                    ivModeSwitch.setImageResource(R.drawable.air_slient_on);
                    break;
            }
        } else {
            ivModeSwitch.setImageResource(R.drawable.air_auto_off);
        }
    }

    /**
     * 设置童锁开关
     *
     * @param isOn
     */
    private void switchLock(boolean isOn) {
        rlayLockSwitch.setSelected(isOn);
        ivLockSwitch.setSelected(isOn);
        tvLockSwitch.setSelected(isOn);
    }

    /**
     * 刷新UI数据，所有数据刷新从这里开始
     */
    @Override
    protected void refreshUIData() {
        try {
            if (mVerAirPurifier != null && isThisAdd()) {
                //设置设备名字
//                if (!deviceNewName.equals(mVerAirPurifier.getName())) {
//                    deviceNewName = mVerAirPurifier.getName();
//                    title.setText(mVerAirPurifier.getName());
//                }
                if (oznerSetting != null) {
                    title.setText(oznerSetting.getName());
                } else {
                    title.setText(mVerAirPurifier.getName());
                }
                refreshSensorData();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "refreshUIData_Ex: " + ex.getMessage());
        }
    }


    /**
     * 刷新传感器数据
     */
    private void refreshSensorData() {
        if (mVerAirPurifier != null) {
            Log.e("Air_sensor", "refreshSensorData: " + mVerAirPurifier.sensor().toString());
            showConnectState();
            showSwitcherState();
            showPM25(mVerAirPurifier.sensor().PM25());
            showTemp(mVerAirPurifier.sensor().Temperature());
            showVOCState(mVerAirPurifier.sensor().VOC());
            showHumidity(mVerAirPurifier.sensor().Humidity());
            showFilterStatus();
        } else {
            llayDeviceConnectTip.setVisibility(View.VISIBLE);
            tvDeviceConnectTips.setText(R.string.device_disConnect);
            showDeviceDisConn();
        }
    }

    /**
     * 显示滤芯状态
     */
    private void showFilterStatus() {
        try {
            if (mVerAirPurifier != null) {
                LCLogUtils.E(TAG, "空净类型：" + mVerAirPurifier.Type());
                int lvXin = 0;

                if (mVerAirPurifier.Type().equals("580c2783")) {//君融空净单独处理
                    int workTime = mVerAirPurifier.sensor().FilterStatus().workTime;
                    int maxTime = mVerAirPurifier.sensor().FilterStatus().maxWorkTime;
                    Log.e(TAG, "refreshFilter: workTime:" + workTime + " ,maxTime:" + maxTime);

                    float lvXinTemp = 0;
                    if (maxTime > 0)
                        lvXinTemp = 1 - (float) workTime / maxTime;
                    lvXinTemp = Math.min(1, lvXinTemp);
                    lvXinTemp = Math.max(0, lvXinTemp);
                    lvXin = (int) (lvXinTemp * 100);
                } else {
                    Date proDate = mVerAirPurifier.sensor().FilterStatus().lastTime;
                    Date stopDate = mVerAirPurifier.sensor().FilterStatus().stopTime;
                    long proMill = proDate.getTime();
                    long stopMill = stopDate.getTime();
                    long currentMill = Calendar.getInstance().getTimeInMillis();
                    long totalTime = (stopMill - proMill) / (24 * 3600 * 1000);
                    long useTime = (currentMill - proMill) / (24 * 3600 * 1000);
                    if (totalTime != 0) {
                        try {
                            Log.e(TAG, "showFilterStatus_remain: " + (totalTime - useTime) + " , totalTime:" + totalTime);
                            lvXin = Math.round((totalTime - useTime) * 100 / totalTime);
                            if (lvXin < 0 || lvXin > 100) {
                                lvXin = 0;
                            }
                        } catch (Exception ex) {
                            Log.e(TAG, "showFilterStatus_Ex: " + ex.getMessage());
                        }
                    } else {
                        lvXin = 0;
                    }
                }

                tvFilterValue.setText(String.format("%d%%", lvXin));
            }
        } catch (Exception ex) {
            Log.e(TAG, "showFilterStatus_Ex: " + ex.getMessage());
        }
    }

    /**
     * 显示设备连接已断开
     */
    private void showDeviceDisConn() {
        tvPmValue.setText(R.string.device_no_net);
        tvPmValue.setAlpha(0.6f);
        tvPmState.setText(R.string.state_null);
        tvAirTemp.setText("-");
        tvAirVOC.setText("-");
        tvAirShiDu.setText("-");
    }


    /**
     * 显示设备关机
     */
    private void showDeviceClose() {
        tvPmValue.setText(R.string.device_close);
        tvPmValue.setAlpha(0.6f);
    }

    /**
     * 刷新开关状态
     */
    private void showSwitcherState() {
        try {
            switchLock(mVerAirPurifier.airStatus().Lock());
            switch (mVerAirPurifier.airStatus().speed()) {
                case AirPurifier_MXChip.FAN_SPEED_AUTO:
                    switchMode(true, AirPurifier_MXChip.FAN_SPEED_AUTO);
                    break;
                case AirPurifier_MXChip.FAN_SPEED_POWER:
                    switchMode(true, AirPurifier_MXChip.FAN_SPEED_POWER);
                    break;
                case AirPurifier_MXChip.FAN_SPEED_SILENT:
                    switchMode(true, AirPurifier_MXChip.FAN_SPEED_SILENT);
                    break;
            }
            switchPower(mVerAirPurifier.airStatus().Power());
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "showSwitcherState_Ex: " + ex.getMessage());
        }
    }

    /**
     * 设置连接状态
     */
    private void showConnectState() {
        try {
            if (NetState.checkNetwork(getContext()) == NetState.State.CONNECTED) {
                if (mVerAirPurifier.isOffline()) {
                    llayDeviceConnectTip.setVisibility(View.VISIBLE);
                    tvDeviceConnectTips.setText(R.string.device_no_net);
                    showDeviceDisConn();
                } else {
                    llayDeviceConnectTip.setVisibility(View.INVISIBLE);
                    if (!mVerAirPurifier.airStatus().Power()) {
                        showDeviceClose();
                    }
                }

            } else {
                llayDeviceConnectTip.setVisibility(View.VISIBLE);
                tvDeviceConnectTips.setText(R.string.phone_nonet);
                showDeviceDisConn();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "showConnectState_Ex: " + ex.getMessage());
        }
    }

    /**
     * 设置PM2.5
     *
     * @param pm25
     */
    private void showPM25(int pm25) {
//        if (oldPM != pm25) {
        if (!mVerAirPurifier.isOffline() && mVerAirPurifier.airStatus().Power()) {
            if (pm25 > 0 && pm25 < 1000) {
                if (pm25 < 75) {
                    tvPmState.setText(R.string.excellent);
                    setBarColor(R.color.air_good_bg);
                    setToolbarColor(R.color.air_good_bg);
                    llayTop.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.air_good_bg));
                } else if (pm25 >= 75 && pm25 < 150) {
                    tvPmState.setText(R.string.good);
                    setBarColor(air_soso_bg);
                    setToolbarColor(air_soso_bg);
                    llayTop.setBackgroundColor(ContextCompat.getColor(getContext(), air_soso_bg));
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
        }
//        }
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
     * 设置VOC状态
     *
     * @param voc
     */
    private void showVOCState(int voc) {
//        if (oldVoc != voc) {
        if (65535 != voc) {
            switch (voc) {
                case -1:
                    tvAirVOC.setText(R.string.in_test);
                    break;
                case 0:
                    tvAirVOC.setText(R.string.excellent);
                    break;
                case 1:
                    tvAirVOC.setText(R.string.good);
                    break;
                case 2:
                    tvAirVOC.setText(R.string.ordinary);
                    break;
                case 3:
                    tvAirVOC.setText(R.string.bads);
                    break;
                default:
                    tvAirVOC.setText(R.string.state_null);
                    break;
            }
        } else {
            tvAirVOC.setText("-");
        }
//        }
    }


    @Override
    public void onDetach() {
//        if (!isDetached()) {
        try {
            setToolbarColor(R.color.colorAccent);
            setBarColor(R.color.colorAccent);
        } catch (Exception ex) {

        }
//        }
        System.gc();
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }


    class AirPurifierMonitor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshUIData();
        }
    }
}
