package com.ozner.cup.Device.ROWaterPurifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ozner.WaterPurifier.WaterPurifier_RO_BLE;
import com.ozner.cup.Base.WebActivity;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.CupRecord;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.OznerDeviceSettings;
import com.ozner.cup.DBHelper.UserInfo;
import com.ozner.cup.DBHelper.WaterPurifierAttr;
import com.ozner.cup.Device.DeviceFragment;
import com.ozner.cup.Device.TDSSensorManager;
import com.ozner.cup.Device.WaterPurifier.WaterNetInfoManager;
import com.ozner.cup.Device.WaterPurifier.WaterTDSActivity;
import com.ozner.cup.Main.MainActivity;
import com.ozner.cup.R;
import com.ozner.cup.UIView.PurifierDetailProgress;
import com.ozner.cup.UIView.UIZSeekBar;
import com.ozner.cup.Utils.WeChatUrlUtil;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OperateCallback;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.ozner.cup.R.id.llay_tds_detail;


/**
 * Created by ozner_67 on 2017/6/21.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class RoCommlFragment extends DeviceFragment {
    private static final String TAG = "RoCommlFragment";
    private static final int TextSize = 40;
    private static final int NumSize = 50;
    private static final String CUSTOMER_TEMP = "comml_customer_temp";
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.iv_filter_icon)
    ImageView ivFilterIcon;
    @InjectView(R.id.tv_filter_value)
    TextView tvFilterValue;
    @InjectView(R.id.tv_filter_tips)
    TextView tvFilterTips;
    @InjectView(R.id.rlay_filter)
    RelativeLayout rlayFilter;
    @InjectView(R.id.iv_setting)
    ImageView ivSetting;
    @InjectView(R.id.iv_deviceConnectIcon)
    ImageView ivDeviceConnectIcon;
    @InjectView(R.id.tv_deviceConnectTips)
    TextView tvDeviceConnectTips;
    @InjectView(R.id.llay_deviceConnectTip)
    LinearLayout llayDeviceConnectTip;
    @InjectView(R.id.waterProgress)
    PurifierDetailProgress waterProgress;
    @InjectView(R.id.iv_tdsStateIcon)
    ImageView ivTdsStateIcon;
    @InjectView(R.id.tv_tdsStateText)
    TextView tvTdsStateText;
    @InjectView(R.id.llay_tdsState)
    LinearLayout llayTdsState;
    @InjectView(R.id.tv_preValue)
    TextView tvPreValue;
    @InjectView(R.id.tv_spec)
    TextView tvSpec;
    @InjectView(R.id.tv_afterValue)
    TextView tvAfterValue;
    @InjectView(R.id.lay_tdsValue)
    LinearLayout layTdsValue;
    @InjectView(R.id.tv_tdsTips)
    TextView tvTdsTips;
    @InjectView(R.id.llay_tdsTips)
    LinearLayout llayTdsTips;
    @InjectView(llay_tds_detail)
    LinearLayout llayTdsDetail;
    @InjectView(R.id.tv_cleanValue)
    TextView tvCleanValue;
    @InjectView(R.id.tv_unit)
    TextView tvUnit;
    @InjectView(R.id.tv_belowTemp)
    TextView tvBelowTemp;
    @InjectView(R.id.tv_middleTemp)
    TextView tvMiddleTemp;
    @InjectView(R.id.tv_highTemp)
    TextView tvHighTemp;
    @InjectView(R.id.tv_customer)
    TextView tvCustomer;
    @InjectView(R.id.uizSeekBar)
    UIZSeekBar uizSeekBar;

    private WaterPurifier_RO_BLE mWaterPurifer;
    private ROMonitor mMonitor;
    private int oldPreValue, oldThenValue;
    WaterNetInfoManager waterNetInfoManager;
    WaterPurifierAttr purifierAttr;
    private String dsn = "";
    private TDSSensorManager tdsSensorManager;
    private String mUserid;
    private OznerDeviceSettings oznerSetting;
    Handler mHandler = new Handler();

    private void saveCustomerValue(int value) {
        Log.e(TAG, "saveCustomerValue: " + value);
        UserDataPreference.SetUserData(getContext(), CUSTOMER_TEMP, String.valueOf(value));
    }

    private int getCustomerValue() {
        String value = UserDataPreference.GetUserData(getContext(), CUSTOMER_TEMP, "40");
        Log.e(TAG, "getCustomerValue: " + value);
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return 40;
        }
    }

    /**
     * 实例化Fragment
     *
     * @param mac
     *
     * @return
     */
    public static RoCommlFragment newInstance(String mac) {
        RoCommlFragment fragment = new RoCommlFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DeviceAddress, mac);
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mUserid = OznerPreference.GetValue(getContext(), OznerPreference.UserId, "");
        tdsSensorManager = new TDSSensorManager(getContext());
        try {
            Bundle bundle = getArguments();
            mWaterPurifer = (WaterPurifier_RO_BLE) OznerDeviceManager.Instance().getDevice(bundle.getString(DeviceAddress));
//            initWaterAttrInfo(mWaterPurifer.Address());
            oldPreValue = oldThenValue = 0;
            oznerSetting = DBManager.getInstance(getContext()).getDeviceSettings(mUserid, mWaterPurifer.Address());
            refreshUIData();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "onCreate_Ex: " + ex.getMessage());
        }
        initAnimation();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ro_comml, container, false);
        ButterKnife.inject(this, view);
        uizSeekBar.setOnTouchListener(new UIZSeekBarTouckListener());
        llayTdsDetail.setEnabled(false);
//        laly_buttons.setVisibility(View.INVISIBLE);
        toolbar.setTitle("");
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).initActionBarToggle(toolbar);
        selectCustomer();
        uizSeekBar.setProgress(getCustomerValue());
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void setDevice(OznerDevice device) {
        oznerSetting = DBManager.getInstance(getContext()).getDeviceSettings(mUserid, device.Address());
        oldPreValue = oldThenValue = 0;
//        initWaterAttrInfo(device.Address());

        if (mWaterPurifer != null) {
            if (mWaterPurifer.Address() != device.Address()) {
                mWaterPurifer = null;
                mWaterPurifer = (WaterPurifier_RO_BLE) device;
                refreshUIData();
            }
        } else {
            mWaterPurifer = (WaterPurifier_RO_BLE) device;
            refreshUIData();
        }
    }

    @Override
    protected void refreshUIData() {
        if (isWaterPuriferAdd()) {
            if (mWaterPurifer != null && mWaterPurifer.connectStatus() == BaseDeviceIO.ConnectStatus.Connected) {
                Log.e(TAG, "refreshUIData: ");
//                refreshConnectState();
                refreshSensorData();
                refreshSettingExtra();
                refreshFilterInfo();
            } else {
//                llayDeviceConnectTip.setVisibility(View.INVISIBLE);
                showDisConnect();
                tvPreValue.setText(R.string.state_null);
                tvAfterValue.setText(R.string.state_null);
                tvPreValue.setTextSize(TextSize);
                tvAfterValue.setTextSize(TextSize);
                ivTdsStateIcon.setVisibility(View.GONE);
                tvTdsStateText.setText(R.string.state_null);
                waterProgress.update(0, 0);
//                if (filter_median2 == 0) {
                tvFilterValue.setText(getResources().getString(R.string.state_null));
                tvFilterTips.setText(R.string.filter_status);
                rlayFilter.setEnabled(false);
//                }
            }
        }
    }

    /**
     * 刷新厨上式水芯片设置二信息
     */
    private void refreshSettingExtra() {
        //加载过滤水量
        int filterVolume = mWaterPurifer.waterInfo.FilterVolume;
        Log.e(TAG, "refreshSettingExtra: 过滤水量：" + filterVolume);
        if (filterVolume <= 0) {
            tvCleanValue.setText(R.string.state_null);
            tvUnit.setText("");
        } else if (filterVolume < 1000) {
            tvCleanValue.setText(String.format("%d", filterVolume));
            tvUnit.setText("ML");
        } else if (filterVolume < 1000000) {
            tvCleanValue.setText(String.format("%.2f", filterVolume / 1000.0f));
            tvUnit.setText("L");
        } else {
            tvCleanValue.setText(String.format("%.2", filterVolume / 1000000.0f));
            tvUnit.setText("m³");
        }

        enableTempBtn(mWaterPurifer.settingExtra().heatTemperature);
    }

    /**
     * 设置温度控制按钮
     * 默认温度调节范围40-99
     *
     * @param temp
     */
    private void enableTempBtn(int temp) {
        if (temp < 0 || temp > 100) {
            temp = 40;
        }
        switch (temp) {
            case 55://中低温
                selectBelow();
                break;
            case 85://中高温
                selectMiddle();
                break;
            case 99://高温
                selectHigh();
                break;
            default://自定义
                selectCustomer();
                break;
        }
        uizSeekBar.setProgress(temp);
    }

    private void selectBelow() {
        tvCustomer.setSelected(false);
        tvBelowTemp.setSelected(true);
        tvMiddleTemp.setSelected(false);
        tvHighTemp.setSelected(false);
    }

    private void selectMiddle() {
        tvCustomer.setSelected(false);
        tvBelowTemp.setSelected(false);
        tvMiddleTemp.setSelected(true);
        tvHighTemp.setSelected(false);
    }

    private void selectHigh() {
        tvCustomer.setSelected(false);
        tvBelowTemp.setSelected(false);
        tvMiddleTemp.setSelected(false);
        tvHighTemp.setSelected(true);
    }

    private void selectCustomer() {
        tvCustomer.setSelected(true);
        tvBelowTemp.setSelected(false);
        tvMiddleTemp.setSelected(false);
        tvHighTemp.setSelected(false);
    }

    @OnClick({R.id.rlay_filter, R.id.iv_setting, R.id.tv_belowTemp, R.id.tv_middleTemp, R.id.tv_highTemp, R.id.tv_customer, R.id.llay_tds_detail})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.rlay_filter:
                if (mWaterPurifer != null) {
//                    Log.e("trfit","rofitSSSSSSSSS");
                    Intent filterIntent = new Intent(getContext(), ROFilterStatusActivity.class);
                    filterIntent.putExtra(Contacts.PARMS_MAC, mWaterPurifer.Address());
                    filterIntent.putExtra("Fit_a", mWaterPurifer.filterInfo.Filter_A_Percentage + "");
                    filterIntent.putExtra("Fit_b", mWaterPurifer.filterInfo.Filter_B_Percentage + "");
                    filterIntent.putExtra("Fit_c", mWaterPurifer.filterInfo.Filter_C_Percentage + "");
                    startActivity(filterIntent);
                } else {
                    showCenterToast(R.string.Not_found_device);
                }
                break;
            case R.id.iv_setting:
                if (mWaterPurifer != null) {
                    Intent setupIntent = new Intent(getContext(), SetupROWaterActivity.class);
                    setupIntent.putExtra(Contacts.PARMS_MAC, mWaterPurifer.Address());
                    startActivity(setupIntent);
                } else {
                    showCenterToast(R.string.Not_found_device);
                }
                break;
            case R.id.llay_tds_detail:
                if (mWaterPurifer != null) {
                    Intent tdsIntent = new Intent(getContext(), WaterTDSActivity.class);
                    tdsIntent.putExtra(Contacts.PARMS_MAC, mWaterPurifer.Address());
                    startActivity(tdsIntent);
                } else {
                    showCenterToast(R.string.Not_found_device);
                }
                break;
            case R.id.tv_belowTemp:
                selectBelow();
                setHeatTemperature(55);
                break;
            case R.id.tv_middleTemp:
                selectMiddle();
                setHeatTemperature(85);
                break;
            case R.id.tv_highTemp:
                selectHigh();
                setHeatTemperature(99);
                break;
            case R.id.tv_customer:
                selectCustomer();
                setHeatTemperature(getCustomerValue());
                break;
        }
    }

    /**
     * 设置加热水温
     *
     * @param temp temp
     */
    private void setHeatTemperature(int temp) {
        uizSeekBar.setProgress(temp);
        if (mWaterPurifer != null && mWaterPurifer.connectStatus().equals(BaseDeviceIO.ConnectStatus.Connected)) {
            mWaterPurifer.setHeatTemperature(temp, new OperateCallback<Void>() {
                @Override
                public void onSuccess(Void var1) {
                    Log.e(TAG, "setHeatTemperature_onSuccess: ");
                }

                @Override
                public void onFailure(Throwable var1) {
                    Log.e(TAG, "setHeatTemperature_onFailure: ");

                }
            });
        } else {
            Toast.makeText(getContext(), "设备断开连接", Toast.LENGTH_SHORT).show();
        }
    }

    class UIZSeekBarTouckListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                tvCustomer.setSelected(true);
                tvBelowTemp.setSelected(false);
                tvMiddleTemp.setSelected(false);
                tvHighTemp.setSelected(false);
                saveCustomerValue(uizSeekBar.getProgress() + 40);
                setHeatTemperature(uizSeekBar.getProgress() + 40);
            }
            return false;
        }
    }

    /**
     * 刷新滤芯状态
     */
    private void refreshFilterInfo() {

        int filter = Math.min(mWaterPurifer.filterInfo.Filter_A_Percentage, mWaterPurifer.filterInfo.Filter_B_Percentage);
        filter = Math.min(mWaterPurifer.filterInfo.Filter_C_Percentage, filter);
        setFilterState(filter);
    }

//    /**
//     * 刷新连接状态
//     */
//    private void refreshConnectState() {
//        if (mWaterPurifer != null) {
//            Log.e(TAG, "refreshConnectState: " + mWaterPurifer.connectStatus());
//            if (ivDeviceConnectIcon.getAnimation() == null) {
//                ivDeviceConnectIcon.setAnimation(rotateAnimation);
//            }
//            if (mWaterPurifer.connectStatus() == BaseDeviceIO.ConnectStatus.Connecting) {
//                llayDeviceConnectTip.setVisibility(View.VISIBLE);
//                tvDeviceConnectTips.setText(R.string.device_connecting);
//                ivDeviceConnectIcon.setImageResource(R.drawable.data_loading);
//                ivDeviceConnectIcon.getAnimation().start();
//            } else if (mWaterPurifer.connectStatus() == BaseDeviceIO.ConnectStatus.Connected) {
//                llayDeviceConnectTip.setVisibility(View.INVISIBLE);
//                if (ivDeviceConnectIcon.getAnimation() != null) {
//                    ivDeviceConnectIcon.getAnimation().cancel();
//                }
//            } else if (mWaterPurifer.connectStatus() == BaseDeviceIO.ConnectStatus.Disconnect) {
//                llayDeviceConnectTip.setVisibility(View.VISIBLE);
//                tvDeviceConnectTips.setText(R.string.device_unconnected);
//                if (ivDeviceConnectIcon.getAnimation() != null) {
//                    ivDeviceConnectIcon.getAnimation().cancel();
//                }
//                ivDeviceConnectIcon.setImageResource(R.drawable.data_load_fail);
//            }
//        }
//    }

    private void showDisConnect() {
        llayDeviceConnectTip.setVisibility(View.VISIBLE);
        tvDeviceConnectTips.setText(R.string.device_unconnected);
        if (ivDeviceConnectIcon.getAnimation() != null) {
            ivDeviceConnectIcon.getAnimation().cancel();
        }
        ivDeviceConnectIcon.setImageResource(R.drawable.data_load_fail);
    }

    /**
     * 刷新传感器数据
     */
    private void refreshSensorData() {
        if (mWaterPurifer != null) {
            if (oznerSetting != null) {
                title.setText(oznerSetting.getName());
            } else {
                title.setText(mWaterPurifer.getName());
            }
            showTdsState();

        }
    }

    /**
     * 处理TDS显示相关
     */
    private void showTdsState() {
        int tdsPre, tdsThen;
//        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
//        Date time=mWaterPurifer.settingInfo.ExpireTime;
//        if(time!=null){
//            Calendar calst = Calendar.getInstance();;
//            Calendar caled = Calendar.getInstance();
//            calst.setTime(curDate);
//            caled.setTime(time);
//            //设置时间为0时
//            calst.set(java.util.Calendar.HOUR_OF_DAY, 0);
//            calst.set(java.util.Calendar.MINUTE, 0);
//            calst.set(java.util.Calendar.SECOND, 0);
//            caled.set(java.util.Calendar.HOUR_OF_DAY, 0);
//            caled.set(java.util.Calendar.MINUTE, 0);
//            caled.set(java.util.Calendar.SECOND, 0);
//            //得到两个日期相差的天数
//            int days = ((int) (caled.getTime().getTime() / 1000) - (int) (calst
//                    .getTime().getTime() / 1000)) / 3600 / 24;
//            Log.e("trtime",days+"========="+curDate+"@@@@@@@@@@@"+time);
//            if(days<0){
//                tv_ozner_days.setText(getString(R.string.recharge_days)+0+"天");
//            }else {
//                tv_ozner_days.setText(getString(R.string.recharge_days)+days+"天");
//            }
//        }
        //获取净化前后的TDS值
        if (mWaterPurifer.waterInfo.TDS1 > 0 && mWaterPurifer.waterInfo.TDS2 > 0
                && mWaterPurifer.waterInfo.TDS1 != 65535 && mWaterPurifer.waterInfo.TDS2 != 65535) {
            //TDS值比较大的作为净化前的值
            if (mWaterPurifer.waterInfo.TDS1 > mWaterPurifer.waterInfo.TDS2) {
                tdsPre = mWaterPurifer.waterInfo.TDS1;
                tdsThen = mWaterPurifer.waterInfo.TDS2;
            } else {
                tdsPre = mWaterPurifer.waterInfo.TDS2;
                tdsThen = mWaterPurifer.waterInfo.TDS1;
            }
        } else {
            //有任何一个不大于0或者有任何一个为65535，就全部置为0
            tdsPre = tdsThen = 0;
        }
        Log.e(TAG, "showTdsState: oldPre:" + oldPreValue + " , oldThen:" + oldThenValue);
        //只有当数据和上次不一样时才更新刷新
        if (oldPreValue != tdsPre || oldThenValue != tdsThen) {
            oldPreValue = tdsPre;
            oldThenValue = tdsThen;
            //净化前后的值都不为0，并且都不为65535
            if (tdsPre != 0 && !mWaterPurifer.isOffline()) {
                tvPreValue.setText(String.valueOf(tdsPre));
                tvAfterValue.setText(String.valueOf(tdsThen));
                tvPreValue.setTextSize(NumSize);
                tvAfterValue.setTextSize(NumSize);
                updateTdsSensor(String.valueOf(tdsThen), String.valueOf(tdsPre));
            } else {
                tvPreValue.setText(R.string.state_null);
                tvAfterValue.setText(R.string.state_null);
                tvPreValue.setTextSize(TextSize);
                tvAfterValue.setTextSize(TextSize);
            }
            // 根据净化后的tds显示状态
            showTdsStateTips(tdsThen);
            if (tdsPre > 250) {
                tdsPre = 250;
            }
            if (tdsThen > 250) {
                tdsThen = 250;
            }
            waterProgress.update(Math.round((tdsPre / 250f) * 100), Math.round((tdsThen / 250f) * 100));
        }
    }

    /**
     * 根据净化后的tds显示状态
     */
    private void showTdsStateTips(int thenTds) {
        if (thenTds > 0) {
            ivTdsStateIcon.setVisibility(View.VISIBLE);
            llayTdsTips.setVisibility(View.VISIBLE);
        } else {
            llayTdsTips.setVisibility(View.INVISIBLE);
        }
        if (thenTds == 0) {
            ivTdsStateIcon.setVisibility(View.GONE);
            tvTdsStateText.setText(R.string.state_null);
        } else if (thenTds > 0 && thenTds <= CupRecord.TDS_Good_Value) {
            if (!this.isDetached())
                Glide.with(this).load(R.drawable.face_good).into(ivTdsStateIcon);
            tvTdsStateText.setText(R.string.health);
        } else if (thenTds > CupRecord.TDS_Good_Value && thenTds < CupRecord.TDS_Bad_Value) {
            if (!this.isDetached()) {
                Glide.with(this).load(R.drawable.face_soso).into(ivTdsStateIcon);
            }
            tvTdsStateText.setText(R.string.soso);
        } else if (thenTds > CupRecord.TDS_Bad_Value) {
            if (!this.isDetached()) {
                Glide.with(this).load(R.drawable.face_bad).into(ivTdsStateIcon);
            }
            tvTdsStateText.setText(R.string.bad);
        }
    }

    /**
     * 上传TDS
     *
     * @param tds
     * @param beforeTds
     */
    private void updateTdsSensor(String tds, String beforeTds) {
        if (mWaterPurifer != null) {
            try {
//                if (mWaterPurifer.IO() instanceof AylaIO) {
//                    dsn = ((AylaIO) mWaterPurifer.IO()).DSN();
//                }
            } catch (Exception ex) {

            }
            tdsSensorManager.updateTds(mWaterPurifer.Address(), mWaterPurifer.Type(), tds, beforeTds, dsn, null);
        }
    }


    /**
     * 是否已添加到View
     *
     * @return
     */
    private boolean isWaterPuriferAdd() {
        return !RoCommlFragment.this.isRemoving() && !RoCommlFragment.this.isDetached() && RoCommlFragment.this.isAdded();
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

    /**
     * 注册广播监听
     */
    private void registerMonitor() {
        mMonitor = new ROMonitor();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_CONNECTED);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_CONNECTING);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_DISCONNECTED);
        filter.addAction(OznerDeviceManager.ACTION_OZNER_MANAGER_DEVICE_CHANGE);
        filter.addAction(OznerDevice.ACTION_DEVICE_UPDATE);
        getContext().registerReceiver(mMonitor, filter);
    }

    /**
     * 注销广播监听
     */
    private void releaseMonitor() {
        if (isWaterPuriferAdd()) {
            getContext().unregisterReceiver(mMonitor);
        }
    }


    /**
     * 显示滤芯百分比
     *
     * @param fitPre
     */
    private void setFilterState(final int fitPre) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isWaterPuriferAdd()) {
                    int tempPre = fitPre;
                    if (tempPre > 100) {
                        tempPre = 100;
                    }
                    if (tempPre < 0) {
                        tempPre = 0;
                    }
                    rlayFilter.setEnabled(true);
                    tvFilterValue.setText(tempPre + "%");
                    if (0 == tempPre) {
                        tvFilterTips.setText(R.string.filter_need_change);
                        ivFilterIcon.setImageResource(R.drawable.filter_state0);
                    } else if (tempPre > 0 && tempPre <= 10) {
                        tvFilterTips.setText(R.string.filter_need_change);
                        ivFilterIcon.setImageResource(R.drawable.filter_state1);
                        //ro水机一级页面的滤芯提醒
                        told();
                    } else if (tempPre > 10 && tempPre <= 60) {
                        tvFilterTips.setText(R.string.filter_status);
                        ivFilterIcon.setImageResource(R.drawable.filter_state2);
                    } else {
                        tvFilterTips.setText(R.string.filter_status);
                        ivFilterIcon.setImageResource(R.drawable.filter_state3);
                    }

                }
            }
        });
    }

    private void told() {
        new AlertDialog.Builder(getContext())
                .setMessage(getString(R.string.filter_need_change))
                .setPositiveButton(getString(R.string.buy_filter), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        buyFilter();
                        dialog.dismiss();
                    }
                }).setNegativeButton(getString(R.string.I_got_it), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show();
    }


    private void buyFilter() {
        String userId = OznerPreference.GetValue(getContext(), OznerPreference.UserId, "");
        if (userId != null) {
            UserInfo userInfo = DBManager.getInstance(getContext()).getUserInfo(userId);
            String usertoken = OznerPreference.getUserToken(getContext());
            String shopUrl = WeChatUrlUtil.getMallUrl(userInfo.getMobile(), usertoken, "zh", "zh");
            startWebActivity(shopUrl);
        }
    }

    private void startWebActivity(String url) {
        Intent filterIntent = new Intent(getActivity(), WebActivity.class);
        filterIntent.putExtra(Contacts.PARMS_URL, url);
        startActivity(filterIntent);
    }

    @Override
    public void onPause() {
        releaseMonitor();
        super.onPause();
    }


    @Override
    public void onResume() {
        try {
            setBarColor(R.color.cup_detail_bg);
            setToolbarColor(R.color.cup_detail_bg);
            if (oznerSetting != null) {
                title.setText(oznerSetting.getName());
            } else {
                title.setText(R.string.water_purifier);
            }
        } catch (Exception ex) {

        }
        registerMonitor();
        refreshUIData();
        super.onResume();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    /**
     * 刷新连接状态
     */
    private void refreshConnectState(String action) {
        Log.e(TAG, "refreshConnectState: " + action);
        if (mWaterPurifer != null && isWaterPuriferAdd()) {
            if (ivDeviceConnectIcon.getAnimation() == null) {
                ivDeviceConnectIcon.setAnimation(rotateAnimation);
            }
            switch (action) {
                case BaseDeviceIO.ACTION_DEVICE_CONNECTING:
                    llayDeviceConnectTip.setVisibility(View.VISIBLE);
                    tvDeviceConnectTips.setText(R.string.device_connecting);
                    ivDeviceConnectIcon.setImageResource(R.drawable.data_loading);
                    ivDeviceConnectIcon.getAnimation().start();
                    break;
                case BaseDeviceIO.ACTION_DEVICE_CONNECTED:
                    llayDeviceConnectTip.setVisibility(View.INVISIBLE);
                    if (ivDeviceConnectIcon.getAnimation() != null) {
                        ivDeviceConnectIcon.getAnimation().cancel();
                    }
                    break;
                case BaseDeviceIO.ACTION_DEVICE_DISCONNECTED:
                    llayDeviceConnectTip.setVisibility(View.VISIBLE);
                    tvDeviceConnectTips.setText(R.string.device_unconnected);
                    if (ivDeviceConnectIcon.getAnimation() != null) {
                        ivDeviceConnectIcon.getAnimation().cancel();
                    }
                    ivDeviceConnectIcon.setImageResource(R.drawable.data_load_fail);
                    break;
            }
        }
    }


    class ROMonitor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive: " + intent.getAction());
//            Toast.makeText(getActivity(),"onReceive: " + mWaterPurifer.toString(),Toast.LENGTH_LONG).show();
            refreshConnectState(intent.getAction());
            refreshUIData();
        }
    }
}
