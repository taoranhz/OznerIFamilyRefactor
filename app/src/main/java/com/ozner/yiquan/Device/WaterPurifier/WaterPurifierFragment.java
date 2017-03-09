package com.ozner.yiquan.Device.WaterPurifier;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ozner.WaterPurifier.WaterPurifier;
import com.ozner.yiquan.Base.WebActivity;
import com.ozner.yiquan.Bean.Contacts;
import com.ozner.yiquan.Command.OznerPreference;
import com.ozner.yiquan.Command.UserDataPreference;
import com.ozner.cup.CupRecord;
import com.ozner.yiquan.DBHelper.DBManager;
import com.ozner.yiquan.DBHelper.OznerDeviceSettings;
import com.ozner.yiquan.DBHelper.UserInfo;
import com.ozner.yiquan.DBHelper.WaterPurifierAttr;
import com.ozner.yiquan.Device.DeviceFragment;
import com.ozner.yiquan.Device.FilterStatusActivity;
import com.ozner.yiquan.Device.TDSSensorManager;
import com.ozner.yiquan.Main.MainActivity;
import com.ozner.yiquan.R;
import com.ozner.yiquan.UIView.PurifierDetailProgress;
import com.ozner.yiquan.Utils.LCLogUtils;
import com.ozner.yiquan.Utils.WeChatUrlUtil;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OperateCallback;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import java.util.Calendar;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class WaterPurifierFragment extends DeviceFragment {
    private static final String TAG = "WaterPurifier";
    private static final int TextSize = 40;
    private static final int NumSize = 50;

    @InjectView(R.id.iv_deviceConnectIcon)
    ImageView ivDeviceConnectIcon;
    @InjectView(R.id.tv_deviceConnectTips)
    TextView tvDeviceConnectTips;
    @InjectView(R.id.llay_deviceConnectTip)
    LinearLayout llayDeviceConnectTip;
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
    @InjectView(R.id.tv_preValue)
    TextView tvPreValue;
    @InjectView(R.id.tv_afterValue)
    TextView tvAfterValue;
    @InjectView(R.id.lay_tdsValue)
    LinearLayout layTdsValue;
    @InjectView(R.id.tv_tdsTips)
    TextView tvTdsTips;
    @InjectView(R.id.waterProgress)
    PurifierDetailProgress waterProgress;
    @InjectView(R.id.iv_tdsStateIcon)
    ImageView ivTdsStateIcon;
    @InjectView(R.id.tv_tdsStateText)
    TextView tvTdsStateText;
    @InjectView(R.id.llay_tdsTips)
    LinearLayout llayTdsTips;
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
    @InjectView(R.id.rl_hot)
    RelativeLayout rlHot;
    @InjectView(R.id.tv_coolswitch)
    TextView tvCoolswitch;
    @InjectView(R.id.iv_coolswitch)
    ImageView ivCoolswitch;
    @InjectView(R.id.rlay_coolswitch)
    RelativeLayout rlayCoolswitch;
    @InjectView(R.id.rl_cool)
    RelativeLayout rlCool;

    boolean isPowerOn = false;
    boolean isCoolOn = false;
    boolean isHotOn = false;
    @InjectView(R.id.llay_tdsState)
    LinearLayout llayTdsState;
    @InjectView(R.id.tv_spec)
    TextView tvSpec;
    @InjectView(R.id.llay_tds_detail)
    LinearLayout llayTdsDetail;
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    private WaterPurifier mWaterPurifer;
    WaterPurifierMonitor waterMonitor;
    private int oldPreValue, oldThenValue;
    WaterNetInfoManager waterNetInfoManager;
    WaterPurifierAttr purifierAttr;
    private boolean hasHot = false;
    private boolean hasCool = false;
    private String dsn = "";
    private TDSSensorManager tdsSensorManager;
    private String mUserid;
    private OznerDeviceSettings oznerSetting;

    /**
     * 实例化Fragment
     *
     * @param mac
     *
     * @return
     */
    public static DeviceFragment newInstance(String mac) {
        WaterPurifierFragment fragment = new WaterPurifierFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DeviceAddress, mac);
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_water_purifier, container, false);
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
        if (isAdded())
            toolbar.setBackgroundColor(ContextCompat.getColor(getContext(), resId));
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mUserid = UserDataPreference.GetUserData(getContext(), UserDataPreference.UserId, "");
        tdsSensorManager = new TDSSensorManager(getContext());
        initAnimation();
        try {
            Bundle bundle = getArguments();
            mWaterPurifer = (WaterPurifier) OznerDeviceManager.Instance().getDevice(bundle.getString(DeviceAddress));

            initWaterAttrInfo(mWaterPurifer.Address());
            oldPreValue = oldThenValue = 0;
            oznerSetting = DBManager.getInstance(getContext()).getDeviceSettings(mUserid, mWaterPurifer.Address());
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "onCreate_Ex: " + ex.getMessage());
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setDevice(OznerDevice device) {
        oznerSetting = DBManager.getInstance(getContext()).getDeviceSettings(mUserid, device.Address());
        oldPreValue = oldThenValue = 0;
        initWaterAttrInfo(device.Address());

        if (mWaterPurifer != null) {
            if (mWaterPurifer.Address() != device.Address()) {
                mWaterPurifer = null;
                mWaterPurifer = (WaterPurifier) device;
                refreshUIData();
            }
        } else {
            mWaterPurifer = (WaterPurifier) device;
            refreshUIData();
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

    @OnClick({R.id.rlay_filter, R.id.iv_setting, R.id.llay_tds_detail, R.id.rlay_powerswitch, R.id.rlay_hotswitch, R.id.rlay_coolswitch})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rlay_filter:
                if (mWaterPurifer != null) {
                    Intent filterIntent = new Intent(getContext(), FilterStatusActivity.class);
                    filterIntent.putExtra(Contacts.PARMS_MAC, mWaterPurifer.Address());
                    filterIntent.putExtra(FilterStatusActivity.PARMS_DEVICE_TYPE, FilterStatusActivity.TYPE_WATER_FILTER);
                    startActivity(filterIntent);
                } else {
                    showCenterToast(R.string.Not_found_device);
                }
                break;
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
            case R.id.llay_tds_detail:
                if (mWaterPurifer != null) {
                    Intent tdsIntent = new Intent(getContext(), WaterTDSActivity.class);
                    tdsIntent.putExtra(Contacts.PARMS_MAC, mWaterPurifer.Address());
                    startActivity(tdsIntent);
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
                if (hasHot) {
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
                } else {
                    showCenterToast(R.string.not_support);
                }
                break;
            case R.id.rlay_coolswitch:
                if (hasCool) {
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
                } else {
                    showCenterToast(R.string.not_support);
                }
                break;
        }
    }


    /**
     * 是否已添加到View
     *
     * @return
     */
    private boolean isWaterPuriferAdd() {
        return !WaterPurifierFragment.this.isRemoving() && !WaterPurifierFragment.this.isDetached() && WaterPurifierFragment.this.isAdded();
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
        if (WaterPurifierFragment.this.isAdded() && !WaterPurifierFragment.this.isRemoving() && !WaterPurifierFragment.this.isDetached() && waterMonitor != null) {
            getContext().unregisterReceiver(waterMonitor);
        }
    }

    /**
     * 初始化净水器属性信息
     *
     * @param mac
     */
    private void initWaterAttrInfo(String mac) {
        try {
            purifierAttr = DBManager.getInstance(getContext()).getWaterAttr(mac);
            if (null == waterNetInfoManager) {
                waterNetInfoManager = new WaterNetInfoManager(getContext());
            }

            //获取设备属性
            if (purifierAttr != null && purifierAttr.getDeviceType() != null && !purifierAttr.getDeviceType().isEmpty()) {
                Log.e(TAG, "initWaterAttrInfo: " + purifierAttr.getDeviceType() + " ,hasHot:" + purifierAttr.getHasHot() + " ,hasCool:" + purifierAttr.getHasCool());
                refreshWaterSwitcher(purifierAttr);
            }
            waterNetInfoManager.getMatchineType(mac, new WaterNetInfoManager.IWaterAttr() {
                @Override
                public void onResult(WaterPurifierAttr attr) {
                    if (attr != null) {
                        refreshWaterSwitcher(attr);
                    }
                }
            });

            //获取滤芯信息
            if (purifierAttr != null && DateUtils.isToday(purifierAttr.getFilterNowtime())) {
                Log.e(TAG, "initWaterAttrInfo_filter: " + purifierAttr.getFilterNowtime());
                updateFilterInfoUI(purifierAttr);
            } else {
                LCLogUtils.E(TAG, "从网络加载滤芯信息");
                waterNetInfoManager.getWaterFilterInfo(mac, new WaterNetInfoManager.IWaterAttr() {
                    @Override
                    public void onResult(WaterPurifierAttr attr) {
                        if (attr != null) {
                            updateFilterInfoUI(attr);
                        }
                    }
                });
            }

        } catch (Exception ex) {
            Log.e(TAG, "initWaterAttrInfo_Ex: " + ex.getMessage());
        }
    }

    /**
     * 刷新净水器开关可用状态
     *
     * @param attr
     */
    private void refreshWaterSwitcher(WaterPurifierAttr attr) {
        hasCool = attr.getHasCool();
        hasHot = attr.getHasHot();
    }

    /**
     * 更新净水器滤芯状态
     *
     * @param attr
     */
    private void updateFilterInfoUI(final WaterPurifierAttr attr) {

        Calendar nowCal = Calendar.getInstance();
        nowCal.setTimeInMillis(attr.getFilterNowtime());
        Calendar endCal = Calendar.getInstance();
        endCal.setTimeInMillis(attr.getFilterTime());

        //显示滤芯到期提示
        if (attr.getFilterTime() + attr.getDays() * 24 * 3600 * 1000 < new Date().getTime()) {
            showChangeFilterTips();
        }

        if (endCal.getTimeInMillis() > nowCal.getTimeInMillis()) {
            float remainDay = (endCal.getTimeInMillis() - nowCal.getTimeInMillis()) / (1000.0f * 3600 * 24);
            int dayOfYear = nowCal.getActualMaximum(Calendar.DAY_OF_YEAR);
            Log.e(TAG, "dayOfYear: " + dayOfYear);
            float pre = (remainDay / dayOfYear) * 100;
            setFilterState((int) Math.ceil(pre));
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

                    tvFilterValue.setText(tempPre + "%");
                    if (0 == tempPre) {
                        tvFilterTips.setText(R.string.filter_need_change);
                        ivFilterIcon.setImageResource(R.drawable.filter_state0);
                    } else if (tempPre > 0 && tempPre <= 8) {
                        tvFilterTips.setText(R.string.filter_need_change);
                        ivFilterIcon.setImageResource(R.drawable.filter_state1);
                    } else if (tempPre > 8 && tempPre <= 60) {
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

    /**
     * 显示滤芯到期提醒
     */
    private void showChangeFilterTips() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builer = new AlertDialog.Builder(getContext(), AlertDialog.THEME_HOLO_LIGHT);
                builer.setMessage(R.string.filter_need_change);
                builer.setNegativeButton(R.string.Iknow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builer.setPositiveButton(R.string.buy_filter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO: 2016/11/16 购买滤芯逻辑处理
                        buyFilter();
                        dialog.cancel();
                    }
                });
                builer.show();
            }
        });
    }

    /**
     * 购买滤芯
     */
    private void buyFilter() {
        String userid = UserDataPreference.GetUserData(getContext(), UserDataPreference.UserId, null);
        if (userid != null) {
            UserInfo info = DBManager.getInstance(getContext()).getUserInfo(userid);
            if (info != null && info.getMobile() != null && !info.getMobile().isEmpty()) {
                String url = WeChatUrlUtil.formatTapShopUrl(info.getMobile(), OznerPreference.getUserToken(getContext()), "zh", "zh");
                if (purifierAttr != null && purifierAttr.getBuylinkurl() != null && !purifierAttr.getBuylinkurl().isEmpty()) {
                    url = purifierAttr.getBuylinkurl();
                }
                Intent buyFilterIntent = new Intent(getContext(), WebActivity.class);
                buyFilterIntent.putExtra(Contacts.PARMS_URL, url);
                startActivity(buyFilterIntent);
            }
        }
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
        refreshUIData();
        super.onResume();
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
                tvPreValue.setText(R.string.state_null);
                tvAfterValue.setText(R.string.state_null);
                tvPreValue.setTextSize(TextSize);
                tvAfterValue.setTextSize(TextSize);
                ivTdsStateIcon.setVisibility(View.GONE);
                tvTdsStateText.setText(R.string.state_null);
                waterProgress.update(0, 0);
            }
        }
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
        switchHot(isHotOn);
        switchCool(isCoolOn);
        switchPower(isPowerOn);

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
        //获取净化前后的TDS值
        if (mWaterPurifer.sensor().TDS1() > 0 && mWaterPurifer.sensor().TDS2() > 0
                && mWaterPurifer.sensor().TDS1() != 65535 && mWaterPurifer.sensor().TDS2() != 65535) {
            //TDS值比较大的作为净化前的值
            if (mWaterPurifer.sensor().TDS1() > mWaterPurifer.sensor().TDS2()) {
                tdsPre = mWaterPurifer.sensor().TDS1();
                tdsThen = mWaterPurifer.sensor().TDS2();
            } else {
                tdsPre = mWaterPurifer.sensor().TDS2();
                tdsThen = mWaterPurifer.sensor().TDS1();
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
            if (tdsPre != 0) {
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

    Handler mHandler = new Handler();

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
