package com.ozner.cup.Device.WaterPurifier;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ozner.WaterPurifier.WaterPurifier;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.WaterPurifierAttr;
import com.ozner.cup.Device.DeviceFragment;
import com.ozner.cup.R;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OperateCallback;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class WPLGFragment extends DeviceFragment {
    private static final String TAG = "WPLGFragment";
    private static final int NumSize = 60;
    private static final int TextSize = 40;

    @InjectView(R.id.iv_deviceConnectIcon)
    ImageView ivDeviceConnectIcon;
    @InjectView(R.id.tv_deviceConnectTips)
    TextView tvDeviceConnectTips;
    @InjectView(R.id.llay_deviceConnectTip)
    LinearLayout llayDeviceConnectTip;
    @InjectView(R.id.tv_tempValue)
    TextView tvTempValue;
    @InjectView(R.id.tv_tempUtil)
    TextView tvTempUtil;
    @InjectView(R.id.tv_tempDec)
    TextView tvTempDec;
    @InjectView(R.id.tv_powerswitch)
    TextView tvPowerswitch;
    @InjectView(R.id.iv_powerswitch)
    ImageView ivPowerswitch;
    @InjectView(R.id.rlay_powerswitch)
    RelativeLayout rlayPowerswitch;
    @InjectView(R.id.tv_hotswitch)
    TextView tvHotswitch;
    @InjectView(R.id.iv_hotswitch)
    ImageView ivHotswitch;
    @InjectView(R.id.rlay_hotswitch)
    RelativeLayout rlayHotswitch;
    @InjectView(R.id.tv_coolswitch)
    TextView tvCoolswitch;
    @InjectView(R.id.iv_coolswitch)
    ImageView ivCoolswitch;
    @InjectView(R.id.rlay_coolswitch)
    RelativeLayout rlayCoolswitch;

    private String address;
    private WaterPurifier mWaterPurifer;
    boolean isPowerOn = false;
    boolean isCoolOn = false;
    boolean isHotOn = false;
    Handler mHandler = new Handler();
    WaterPurifierMonitor waterMonitor;
    WaterPurifierAttr purifierAttr;
    CountDownTimer downTimer;

    public static WPLGFragment newInstance(String mac) {
        Bundle args = new Bundle();
        args.putString(DeviceFragment.DeviceAddress, mac);
        WPLGFragment fragment = new WPLGFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        initAnimation();
        try {
            Bundle bundle = getArguments();
            address = bundle.getString(DeviceFragment.DeviceAddress);
            mWaterPurifer = (WaterPurifier) OznerDeviceManager.Instance().getDevice(address);
            purifierAttr = DBManager.getInstance(getContext()).getWaterAttr(address);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "onCreate_ex: " + ex.getMessage());
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wplg, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void setDevice(OznerDevice device) {

    }

    @Override
    protected void refreshUIData() {
        if (isWaterPuriferAdd()) {
            if (mWaterPurifer != null && mWaterPurifer.connectStatus() == BaseDeviceIO.ConnectStatus.Connected) {
                Log.e(TAG, "refreshUIData: ");
                refreshConnectState();
                refreshSensorData();
                refreshSwitchState();
            } else {
                llayDeviceConnectTip.setVisibility(View.INVISIBLE);
                tvTempUtil.setVisibility(View.GONE);
                tvTempValue.setText(R.string.state_null);
                tvTempDec.setVisibility(View.GONE);
            }
        }
    }

    private boolean isStart = false;

    /**
     * 刷新传感器数据
     */
    private void refreshSensorData() {
//        refreshTemp(0);

    }

    /**
     * 刷新水温显示
     *
     * @param temp
     */
    private void refreshTemp(int temp) {
        if (temp < 0) {
            tvTempUtil.setVisibility(View.GONE);
            tvTempValue.setText(R.string.state_null);
            tvTempDec.setVisibility(View.GONE);
            tvTempValue.setTextSize(TextSize);
            return;
        } else {
            tvTempUtil.setVisibility(View.VISIBLE);
            tvTempDec.setVisibility(View.VISIBLE);
        }
        tvTempValue.setTextSize(NumSize);

        int tempColor = WPTempColorUtil.getColor(temp);
        tvTempValue.setText(String.valueOf(temp));
        tvTempValue.setTextColor(tempColor);
        tvTempUtil.setTextColor(tempColor);
    }

    /**
     * 刷新开关状态
     */
    private void refreshSwitchState() {
        if (mWaterPurifer != null) {
            isPowerOn = mWaterPurifer.status().Power();
            isHotOn = mWaterPurifer.status().Hot();
            isCoolOn = mWaterPurifer.status().Cool();
        } else {
            isPowerOn = false;
            isHotOn = false;
            isCoolOn = false;
        }

        switchPower(isPowerOn);
        switchHot(isHotOn);
        switchCool(isCoolOn);

    }

    @Override
    public void onResume() {
        registerMonitor();
        refreshUIData();
        super.onResume();
    }

    @Override
    public void onPause() {
        releaseMonitor();
        super.onPause();
    }

    //测试效果代码，最终删除
    @Override
    public void onStart() {
        if (downTimer == null) {
            downTimer = new CountDownTimer(100000, 200) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (isAdded())
                        refreshTemp((int) (millisUntilFinished / 1000));
                }

                @Override
                public void onFinish() {
                    downTimer.start();
                }
            };
            downTimer.start();
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        if (downTimer != null) {
            downTimer.cancel();
            downTimer = null;
        }
        super.onStop();
    }

    /**
     * 刷新连接状态
     */
    private void refreshConnectState() {
        if (mWaterPurifer != null) {
            Log.e(TAG, "refreshConnectState: " + mWaterPurifer.toString());
            if (ivDeviceConnectIcon.getAnimation() == null) {
                ivDeviceConnectIcon.setAnimation(rotateAnimation);
            }
            if (mWaterPurifer.connectStatus() == BaseDeviceIO.ConnectStatus.Connecting) {
                llayDeviceConnectTip.setVisibility(View.VISIBLE);
                tvDeviceConnectTips.setText(R.string.device_connecting);
                ivDeviceConnectIcon.setImageResource(R.drawable.data_loading);
                ivDeviceConnectIcon.getAnimation().start();
            } else if (mWaterPurifer.connectStatus() == BaseDeviceIO.ConnectStatus.Connected) {
                llayDeviceConnectTip.setVisibility(View.INVISIBLE);
                if (ivDeviceConnectIcon.getAnimation() != null) {
                    ivDeviceConnectIcon.getAnimation().cancel();
                }
            } else if (mWaterPurifer.connectStatus() == BaseDeviceIO.ConnectStatus.Disconnect) {
                llayDeviceConnectTip.setVisibility(View.VISIBLE);
                tvDeviceConnectTips.setText(R.string.device_unconnected);
                if (ivDeviceConnectIcon.getAnimation() != null) {
                    ivDeviceConnectIcon.getAnimation().cancel();
                }
                ivDeviceConnectIcon.setImageResource(R.drawable.data_load_fail);
            }
        }
    }


    @OnClick({R.id.rlay_powerswitch, R.id.rlay_hotswitch, R.id.rlay_coolswitch, R.id.iv_setting})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_setting:
                if (mWaterPurifer != null) {
                    Intent setupIntent = new Intent(getContext(), SetupWaterActivity.class);
                    setupIntent.putExtra(Contacts.PARMS_MAC, mWaterPurifer.Address());
                    if (purifierAttr != null) {
                        setupIntent.putExtra(Contacts.PARMS_URL, purifierAttr.getSmlinkurl());
                    }
                    startActivity(setupIntent);
                } else {
                    showCenterToast(R.string.Not_found_device);
                }
                break;
            case R.id.rlay_powerswitch:
                if (mWaterPurifer != null
                        && mWaterPurifer.connectStatus() == BaseDeviceIO.ConnectStatus.Connected
                        && !mWaterPurifer.isOffline()) {
                    isPowerOn = !mWaterPurifer.status().Power();
                    switchPower(isPowerOn);
                    mWaterPurifer.status().setPower(isPowerOn, new SwitchCallback());
                } else {
                    showCenterToast(R.string.device_disConnect);
                }
                break;
            case R.id.rlay_hotswitch:
                if (mWaterPurifer != null
                        && mWaterPurifer.connectStatus() == BaseDeviceIO.ConnectStatus.Connected
                        && !mWaterPurifer.isOffline()) {
                    if (isPowerOn) {
                        isHotOn = !mWaterPurifer.status().Hot();
                        switchHot(isHotOn);
                        mWaterPurifer.status().setHot(isHotOn, new SwitchCallback());
                    } else {
                        showCenterToast(R.string.please_open_power);
                    }
                } else {
                    showCenterToast(R.string.device_disConnect);
                }
                break;
            case R.id.rlay_coolswitch:
                if (mWaterPurifer != null
                        && mWaterPurifer.connectStatus() == BaseDeviceIO.ConnectStatus.Connected
                        && !mWaterPurifer.isOffline()) {
                    if (isPowerOn) {
                        isCoolOn = !mWaterPurifer.status().Cool();
                        switchCool(isCoolOn);
                        mWaterPurifer.status().setCool(isCoolOn, new SwitchCallback());
                    } else {
                        showCenterToast(R.string.please_open_power);
                    }
                } else {
                    showCenterToast(R.string.device_disConnect);
                }
                break;
        }
    }

    /**
     * 切换电源开关
     *
     * @param isOn
     */
    public void switchPower(boolean isOn) {
        if (!isOn && isCoolOn) {
            switchCool(isOn);
        }
        if (!isOn && isHotOn) {
            switchHot(isOn);
        }
        ivPowerswitch.setSelected(isOn);
        rlayPowerswitch.setSelected(isOn);
        tvPowerswitch.setSelected(isOn);
//        waterPurifier.status().setPower(isOn, this);
        isPowerOn = isOn;
    }

    /**
     * 切换制冷开关
     *
     * @param isOn
     */
    public void switchCool(boolean isOn) {
        rlayCoolswitch.setSelected(isOn);
        ivCoolswitch.setSelected(isOn);
        tvCoolswitch.setSelected(isOn);
        isCoolOn = isOn;
    }

    /**
     * 切换加热开关
     *
     * @param isOn
     */
    public void switchHot(boolean isOn) {
        rlayHotswitch.setSelected(isOn);
        ivHotswitch.setSelected(isOn);
        tvHotswitch.setSelected(isOn);
        isHotOn = isOn;
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

    /**
     * 是否已添加到View
     *
     * @return
     */
    private boolean isWaterPuriferAdd() {
        return !WPLGFragment.this.isRemoving() && !WPLGFragment.this.isDetached() && WPLGFragment.this.isAdded();
    }

    /**
     * 注册广播接收器
     */
    private void registerMonitor() {
        waterMonitor = new WaterPurifierMonitor();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WaterPurifier.ACTION_WATER_PURIFIER_STATUS_CHANGE);
        filter.addAction(OznerDeviceManager.ACTION_OZNER_MANAGER_DEVICE_CHANGE);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_CONNECTING);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_DISCONNECTED);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_CONNECTED);
        getContext().registerReceiver(waterMonitor, filter);
    }

    /**
     * 注销广播接收器
     */
    private void releaseMonitor() {
        if (isWaterPuriferAdd() && waterMonitor != null) {
            getContext().unregisterReceiver(waterMonitor);
        }
    }

    /**
     * 开关操作回调
     */
    class SwitchCallback implements OperateCallback<Void> {

        @Override
        public void onSuccess(Void var1) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isWaterPuriferAdd())
                        showCenterToast(R.string.send_status_success);
                    refreshUIData();
                }
            }, 300);
        }

        @Override
        public void onFailure(Throwable var1) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isWaterPuriferAdd())
                        showCenterToast(R.string.send_status_fail);
                    refreshUIData();
                }
            }, 300);
        }
    }

    class WaterPurifierMonitor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshUIData();
        }
    }
}
