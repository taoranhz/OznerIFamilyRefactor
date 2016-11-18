package com.ozner.cup.Device.AirPurifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ozner.AirPurifier.AirPurifier_MXChip;
import com.ozner.cup.Device.DeviceFragment;
import com.ozner.cup.HttpHelper.NetState;
import com.ozner.cup.Main.MainActivity;
import com.ozner.cup.R;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


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
    @InjectView(R.id.tv_air_quality)
    TextView tvAirQuality;
    @InjectView(R.id.tv_outPM25)
    TextView tvOutPM25;
    @InjectView(R.id.rlay_air_outside)
    RelativeLayout rlayAirOutside;
    private AirPurifier_MXChip mVerAirPurifier;
    AirPurifierMonitor airMonitor;


    private int oldVoc, oldPM, oldTemp, oldHum;
    private String deviceNewName = "";

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

        try {
            Bundle bundle = getArguments();
            mVerAirPurifier = (AirPurifier_MXChip) OznerDeviceManager.Instance().getDevice(bundle.getString(DeviceAddress));
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
        return view;
    }

    @Override
    public void setDevice(OznerDevice device) {

        oldVoc = oldPM = oldTemp = oldHum = 0;
        deviceNewName = "";
//        initColor();
        if (mVerAirPurifier != null) {
            if (mVerAirPurifier.Address() != device.Address()) {
                mVerAirPurifier.release();
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
        if (isThisAdd() && airMonitor != null) {
            getContext().unregisterReceiver(airMonitor);
        }
    }

    @Override
    public void onStart() {
        Log.e(TAG, "onStart: ");
        registerMonitor();
        super.onStart();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop: ");
        releaseMonitor();
        super.onStop();
    }


    private boolean isThisAdd() {
        return AirVerPurifierFragment.this.isAdded() && !AirVerPurifierFragment.this.isRemoving() && !AirVerPurifierFragment.this.isDetached();
    }

    @Override
    public void onAttach(Context context) {
        try {
            if (isThisAdd())
                ((MainActivity) context).setCustomTitle(R.string.air_purifier);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "onAttach_Ex: " + ex.getMessage());
        }
        super.onAttach(context);
    }


    @Override
    public void onResume() {
        try {
            initBgColor();
        } catch (Exception ex) {
            Log.e(TAG, "onResume_Ex:" + ex.getMessage());
        }
        refreshUIData();
        super.onResume();
    }

    @OnClick({R.id.rlay_filterStatus, R.id.iv_purifierSetBtn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rlay_filterStatus:
                break;
            case R.id.iv_purifierSetBtn:
                break;
        }
    }

    /**
     * 设置状态栏颜色
     */
    private void setBarColor(int resId) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = ((MainActivity) getActivity()).getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(getContext(), resId));
        }
    }

    /**
     * 设置主界面toolbar背景色
     *
     * @param resId
     */
    private void setToolbarColor(int resId) {
        ((MainActivity) getActivity()).setToolBarColor(resId);
    }

    /**
     * 初始化背景色
     */
    private void initBgColor() {
        setBarColor(R.color.air_good_bg);
        setToolbarColor(R.color.air_good_bg);
        llayTop.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.air_good_bg));
    }

    @Override
    protected void refreshUIData() {
//        Log.e(TAG, "refreshUIData: ");
        try {
            if (mVerAirPurifier != null && isThisAdd()) {
                //设置设备名字
                if (!deviceNewName.equals(mVerAirPurifier.getName())) {
                    deviceNewName = mVerAirPurifier.getName();
                    ((MainActivity) getActivity()).setCustomTitle(mVerAirPurifier.getName());
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
            Log.e(TAG, "refreshSensorData: " + mVerAirPurifier.sensor().toString());
            showConnectState();
            showPM25(mVerAirPurifier.sensor().PM25());
            showTemp(mVerAirPurifier.sensor().Temperature());
            showVOCState(mVerAirPurifier.sensor().VOC());
            showHumidity(mVerAirPurifier.sensor().Humidity());
        } else {
            llayDeviceConnectTip.setVisibility(View.VISIBLE);
            tvDeviceConnectTips.setText(R.string.device_disConnect);
            showDeviceDisConn();
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
        if (oldPM != pm25) {
            oldPM = pm25;
            if (pm25 > 0 && pm25 < 1000) {
                if (pm25 < 75) {
                    tvPmState.setText(R.string.excellent);
                    setBarColor(R.color.air_good_bg);
                    setToolbarColor(R.color.air_good_bg);
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
        }
    }

    /**
     * 设置温度
     *
     * @param temp
     */
    private void showTemp(int temp) {
        if (oldTemp != temp) {
            oldTemp = temp;
            if (65535 != temp) {
                tvAirTemp.setText(temp + "℃");
            } else {
                tvAirTemp.setText("-");
            }
        }
    }

    /**
     * 设置湿度
     *
     * @param hum
     */
    private void showHumidity(int hum) {
        if (oldHum != hum) {
            oldHum = hum;
            if (65535 != hum) {
                tvAirShiDu.setText(hum + "%");
            } else {
                tvAirShiDu.setText("-");
            }
        }
    }

    /**
     * 设置VOC状态
     *
     * @param voc
     */
    private void showVOCState(int voc) {
        if (oldVoc != voc) {
            oldVoc = voc;
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
        }
    }

    @Override
    public void onDetach() {
//        if (isDetached()) {
        setToolbarColor(R.color.colorAccent);
        setBarColor(R.color.colorAccent);
//        }
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
