package com.ozner.cup.Device.WaterPurifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ozner.WaterPurifier.WaterPurifierManager;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.BuildConfig;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.OznerDeviceSettings;
import com.ozner.cup.Device.Adapter.FoundDevcieAdapter;
import com.ozner.cup.R;
import com.ozner.cup.Utils.LCLogUtils;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;
import com.ozner.wifi.WifiPair;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 *
 */
public class MatchWaterPuriferActivity extends BaseActivity {
    private static final String TAG = "MatchWater";

    @InjectView(R.id.title)
    TextView tv_title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.tv_state)
    TextView tvState;
    @InjectView(R.id.rv_found_devices)
    RecyclerView rvFoundDevices;
    @InjectView(R.id.llay_found_device)
    LinearLayout llayFoundDevice;
    @InjectView(R.id.iv_match_loading)
    ImageView ivMatchLoading;
    @InjectView(R.id.tv_match_notice)
    TextView tvMatchNotice;
    @InjectView(R.id.tv_match_type)
    TextView tvMatchType;
    @InjectView(R.id.tv_selectedWifi)
    TextView tvSelectedWifi;
    @InjectView(R.id.iv_passwordImg)
    ImageView ivPasswordImg;
    @InjectView(R.id.et_password)
    EditText etPassword;
    @InjectView(R.id.cb_remPass)
    CheckBox cbRemPass;
    @InjectView(R.id.btn_next_step)
    Button btnNextStep;
    @InjectView(R.id.llay_input_wifiInfo)
    LinearLayout llayInputWifiInfo;
    @InjectView(R.id.iv_image1)
    ImageView ivImage1;
    @InjectView(R.id.iv_image2)
    ImageView ivImage2;
    @InjectView(R.id.iv_image3)
    ImageView ivImage3;
    @InjectView(R.id.iv_image4)
    ImageView ivImage4;
    @InjectView(R.id.iv_image5)
    ImageView ivImage5;
    @InjectView(R.id.tv_matchingTips)
    TextView tvMatchingTips;
    @InjectView(R.id.llay_wifi_Connecting)
    LinearLayout llayWifiConnecting;
    @InjectView(R.id.tv_notice_Bottom)
    TextView tvNoticeBottom;
    @InjectView(R.id.btn_rematch)
    Button btnRematch;
    @InjectView(R.id.llay_match_fail)
    LinearLayout llayMatchFail;
    @InjectView(R.id.et_device_name)
    EditText etDeviceName;
    @InjectView(R.id.et_device_position)
    EditText etDevicePosition;
    @InjectView(R.id.iv_place_icon)
    ImageView ivPlaceIcon;
    @InjectView(R.id.btn_match_success)
    Button btnMatchSuccess;
    @InjectView(R.id.llay_input_deviceInfo)
    LinearLayout llayInputDeviceInfo;
    @InjectView(R.id.llay_match_succ_holder)
    LinearLayout llayMatchSuccHolder;

    AnimationDrawable anim1, anim2, anim3, anim4, anim5;
    @InjectView(R.id.ib_moreLeft)
    ImageButton ibMoreLeft;
    private boolean isRemPass = true;
    private boolean isShowPass = false;
    private WifiManager wifiManager;
    private WifiPair wifiPair;
    private WifiPairImp pairImp;
    private FoundDevcieAdapter mDevAdpater;
    private BaseDeviceIO selDeviceIo;
    Monitor monitor;
    private String mUserid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_water_purifer);
        ButterKnife.inject(this);
        monitor = new Monitor();
        mUserid = OznerPreference.GetValue(this, OznerPreference.UserId, "");
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        this.registerReceiver(monitor, filter);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        initActionBar();
        initStaticValue();
        initFoundDeviceView();
        initAnimation();

        try {
            pairImp = new WifiPairImp();
            wifiPair = new WifiPair(this, pairImp);
        } catch (Exception ex) {
            ex.printStackTrace();
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "onCreate_Ex: " + ex.getMessage());
            }
        }

        readyMatchDevice();
    }


    /**
     * 初始化actionBar
     */
    private void initActionBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        tv_title.setText(R.string.match_device);
    }

    /**
     * 初始化RecyleView
     */
    private void initFoundDeviceView() {
        mDevAdpater = new FoundDevcieAdapter(this, R.drawable.match_water_selected, R.drawable.match_water_unselect);
//        mDevAdpater.setOnItemClickListener(new FoundDevcieAdapter.ClientClickListener() {
//            @Override
//            public void onItemClick(int position, BaseDeviceIO deviceIO) {
//                Log.e(TAG, "onItemClick: " + position);
//                selDeviceIo = deviceIO;
//                showInputDeviceInfo();
//            }
//        });
//        filladapterData();
        rvFoundDevices.setAdapter(mDevAdpater);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvFoundDevices.setLayoutManager(linearLayoutManager);
    }


    @OnClick({R.id.iv_passwordImg, R.id.cb_remPass, R.id.btn_next_step, R.id.btn_rematch, R.id.btn_match_success})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_passwordImg:
                isShowPass = !isShowPass;

                etPassword.setInputType(isShowPass
                        ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                break;
            case R.id.cb_remPass:
                isRemPass = cbRemPass.isChecked();
                break;
            case R.id.btn_next_step:
                startMatch();
                break;
            case R.id.btn_rematch:
                readyMatchDevice();
                break;
            case R.id.btn_match_success:
                saveDevice();
                break;
        }
    }

    /**
     * 开始配网操作
     */
    private void startMatch() {
        if (tvSelectedWifi.getText().length() > 0) {
//            if (etPassword.getText().length() > 0) {
                if (isRemPass) {
                    remPassWord(tvSelectedWifi.getText().toString(), etPassword.getText().toString());
                } else {
                    clearPassWord(tvSelectedWifi.getText().toString());
                }
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "开始配网: ssid:" + tvSelectedWifi.getText().toString() + " ,password:" + etPassword.getText().toString());
                }
                //开始配网
                try {
                    wifiPair.pair(tvSelectedWifi.getText().toString(), etPassword.getText().toString());
                    showMatchDevice();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Log.e(TAG, "matchDevice_Ex: " + ex.getMessage());
                    showMatchFail();
                }
//            } else {
//                showToastCenter("请输入密码");
//            }
        } else {
            showToastCenter(R.string.not_connect_Wifi);
        }
    }

    /**
     * 保存设备信息
     */
    private void saveDevice() {
        if (selDeviceIo != null) {
            try {
                Log.e(TAG, "saveDevice:sleDeviceIo_info: " + selDeviceIo.getType() + " , " + selDeviceIo.name);
                OznerDevice device = OznerDeviceManager.Instance().getDevice(selDeviceIo);
                if (device != null && WaterPurifierManager.IsWaterPurifier(device.Type())) {
                    Log.e(TAG, "saveDevice: " + device.Type());
                    OznerDeviceManager.Instance().save(device);
                    UserDataPreference.SetUserData(this, UserDataPreference.SelMac, device.Address());//保存选中的设备mac
                    if (etDeviceName.getText().length() > 0) {
                        device.Setting().name(etDeviceName.getText().toString().trim());
                    } else {
                        device.Setting().name(getString(R.string.water_purifier));
                    }
                    device.updateSettings();
                    saveDeviceToDB(mUserid, device);
                } else {
                    if (BuildConfig.DEBUG)
                        Log.e(TAG, "saveDevice: devcie is null");
                    showToastCenter(R.string.device_disConnect);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showToastCenter(ex.getMessage());
                if (BuildConfig.DEBUG)
                    Log.e(TAG, "saveDevice_Ex: " + ex.getMessage());
            } finally {
                this.finish();
            }
        } else {
            if (BuildConfig.DEBUG)
                Log.e(TAG, "saveDevice: selDeviceIo is null");
            showToastCenter(R.string.device_disConnect);
        }
    }

    private void saveDeviceToDB(String userid, OznerDevice device) {
        try {
            OznerDeviceSettings oznerSetting = DBManager.getInstance(this).getDeviceSettings(userid, device.Address());
            if (oznerSetting != null) {
                DBManager.getInstance(this).deleteDeviceSettings(userid, device.Address());
                oznerSetting = null;
            }
            oznerSetting = new OznerDeviceSettings();
            oznerSetting.setCreateTime(String.valueOf(new Date().getTime()));
            oznerSetting.setUserId(userid);
            oznerSetting.setMac(device.Address());
            oznerSetting.setName(device.Setting().name());
            oznerSetting.setDevicePosition(etDevicePosition.getText().toString().trim());
            oznerSetting.setStatus(0);
            oznerSetting.setDevcieType(device.Type());
            DBManager.getInstance(this).updateDeviceSettings(oznerSetting);
        } catch (Exception ex) {
            ex.printStackTrace();
            LCLogUtils.E(TAG, "saveDeviceToDB_Ex:" + ex.getMessage());
        }
    }


    /**
     * 初始化wifi信息
     *
     * @return true:成功，false:失败
     */
    private boolean initWifiInfo() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                String ssid = wifiInfo.getSSID().replace("\"", "");
                tvSelectedWifi.setText(ssid);
                etPassword.setText(getPassWord(ssid));
                return true;
            } else {
                tvSelectedWifi.setText("");
                return false;
            }
        } else {
            tvSelectedWifi.setText("");
            return false;
        }
    }

    /**
     * 记住密码
     *
     * @param ssid
     * @param password
     */
    private void remPassWord(String ssid, String password) {
        OznerPreference.SetValue(this, ssid, password);
    }

    /**
     * 清除wifi密码
     *
     * @param ssid
     */
    private void clearPassWord(String ssid) {
        OznerPreference.SetValue(this, ssid, "");
    }

    /**
     * 获取保存的wifi密码
     *
     * @param ssid
     *
     * @return
     */
    private String getPassWord(String ssid) {
        return OznerPreference.GetValue(this, ssid, "");
    }

    /**
     * 初始化配网动画
     */
    private void initAnimation() {
        anim1 = (AnimationDrawable) ivImage1.getDrawable();
        anim2 = (AnimationDrawable) ivImage2.getDrawable();
        anim3 = (AnimationDrawable) ivImage3.getDrawable();
        anim4 = (AnimationDrawable) ivImage4.getDrawable();
        anim5 = (AnimationDrawable) ivImage5.getDrawable();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return true;
    }


    /**
     * 将通用的布局显示内容初始化为水机配网信息
     */
    private void initStaticValue() {
        tvNoticeBottom.setText(R.string.Purifier_CheckToReMatch);
        etDeviceName.setHint(R.string.inpu_water_name);
    }


    /**
     * 输入密码准备配网
     */
    private void readyMatchDevice() {
        tv_title.setText(R.string.match_device);
        initWifiInfo();
        llayWifiConnecting.setVisibility(View.GONE);
        llayMatchFail.setVisibility(View.GONE);
        llayInputDeviceInfo.setVisibility(View.GONE);
        tvMatchType.setVisibility(View.VISIBLE);
        tvMatchNotice.setVisibility(View.VISIBLE);
        tvMatchNotice.setText(R.string.connect_wlan);
        tvMatchType.setText(R.string.select_useful_wifi);
        llayFoundDevice.setVisibility(View.INVISIBLE);
        llayInputWifiInfo.setVisibility(View.VISIBLE);
        if (!this.isDestroyed())
            Glide.with(this).load(R.drawable.match_device_purifier_wifi).into(ivMatchLoading);
    }

    /**
     * 开始配网
     */
    private void showMatchDevice() {
        llayMatchSuccHolder.setVisibility(View.GONE);
        llayInputWifiInfo.setVisibility(View.GONE);
        llayMatchFail.setVisibility(View.GONE);
        llayInputDeviceInfo.setVisibility(View.GONE);
        tvMatchNotice.setVisibility(View.INVISIBLE);
        tvMatchType.setVisibility(View.INVISIBLE);
        llayFoundDevice.setVisibility(View.INVISIBLE);
        llayWifiConnecting.setVisibility(View.VISIBLE);
        startMatchAnim();
    }

    /**
     * 显示配网失败
     */
    private void showMatchFail() {
        stopMatchAnim();
        llayMatchSuccHolder.setVisibility(View.GONE);
        tv_title.setText(R.string.match_failed);
        llayWifiConnecting.setVisibility(View.GONE);
        llayInputWifiInfo.setVisibility(View.GONE);
        llayInputDeviceInfo.setVisibility(View.GONE);

        tvMatchType.setText(R.string.rematch);
        tvMatchNotice.setText(R.string.no_success);
        tvMatchNotice.setVisibility(View.VISIBLE);
        tvMatchType.setVisibility(View.VISIBLE);

        llayFoundDevice.setVisibility(View.INVISIBLE);
        llayMatchFail.setVisibility(View.VISIBLE);
        try {
            if (!this.isDestroyed())
                Glide.with(this).load(R.drawable.match_device_failed).into(ivMatchLoading);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "showMatchFail_ex: " + ex.getMessage());
        }

    }

    /**
     * 显示搜索到设备
     */
    private void showFoundDevice() {
        stopMatchAnim();
        mDevAdpater.setItemWidth(getRVWidth());
        tv_title.setText(R.string.match_successed);
        llayWifiConnecting.setVisibility(View.GONE);
        llayInputWifiInfo.setVisibility(View.GONE);
        llayMatchFail.setVisibility(View.GONE);
        llayInputDeviceInfo.setVisibility(View.VISIBLE);

        tvMatchNotice.setVisibility(View.INVISIBLE);
        tvMatchType.setVisibility(View.INVISIBLE);
        etDevicePosition.setText(R.string.pos_home);
//        llayMatchSuccHolder.setVisibility(View.VISIBLE);

        llayMatchSuccHolder.setVisibility(View.GONE);
        llayFoundDevice.setVisibility(View.VISIBLE);
        if (!this.isDestroyed())
            Glide.with(this).load(R.drawable.match_device_successed).into(ivMatchLoading);
    }

    /**
     * 显示填写设备信息，并完成配网
     */
    private void showInputDeviceInfo() {
        llayMatchSuccHolder.setVisibility(View.GONE);
        tv_title.setText(R.string.match_successed);
        llayWifiConnecting.setVisibility(View.GONE);
        llayInputWifiInfo.setVisibility(View.GONE);
        llayMatchFail.setVisibility(View.GONE);

        tvMatchNotice.setVisibility(View.INVISIBLE);
        tvMatchType.setVisibility(View.INVISIBLE);

        llayFoundDevice.setVisibility(View.VISIBLE);
        llayInputDeviceInfo.setVisibility(View.VISIBLE);
        if (!this.isDestroyed())
            Glide.with(this).load(R.drawable.match_device_successed).into(ivMatchLoading);

    }

    /**
     * 开始配网动画
     */
    private void startMatchAnim() {
        anim1.start();
        anim2.start();
        anim3.start();
        anim4.start();
        anim5.start();
    }

    /**
     * 结束配网动画
     */
    private void stopMatchAnim() {
        if (anim1 != null && anim1.isRunning()) {
            anim1.stop();
        }
        if (anim2 != null && anim2.isRunning()) {
            anim2.stop();
        }
        if (anim3 != null && anim3.isRunning()) {
            anim3.stop();
        }
        if (anim4 != null && anim4.isRunning()) {
            anim1.stop();
        }
        if (anim5 != null && anim5.isRunning()) {
            anim5.stop();
        }
    }

    /**
     * 释放动画资源
     */
    private void relealeAnim() {
        try {
            stopMatchAnim();
            anim1 = null;
            anim2 = null;
            anim3 = null;
            anim4 = null;
            anim5 = null;

//            Observable.create(new OnSubscribe<ImageView>() {
//                @Override
//                public void call(Subscriber<? super ImageView> subscriber) {
//                    subscriber.onNext(ivImage1);
//                    subscriber.onNext(ivImage2);
//                    subscriber.onNext(ivImage3);
//                    subscriber.onNext(ivImage4);
//                    subscriber.onNext(ivImage5);
//                }
//            }).subscribe(new Action1() {
//                @Override
//                public void call(Object o) {
//                    ImageView ivImage = (ImageView) o;
//                    AnimationDrawable bitmapDrawable = (AnimationDrawable) ivImage.getDrawable();
//                    bitmapDrawable.
//                    if (bitmapDrawable != null) {
//                        if (!bitmapDrawable.getBitmap().isRecycled()) {
//                            bitmapDrawable.getBitmap().recycle();
//                        }
//                    }
//                }
//            });
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "relealeAnim: " + ex.getMessage());
        }
    }


    /**
     * 获取RecyleView的宽度
     * 用来计算每一个item应有的宽度
     *
     * @return
     */
    private int getRVWidth() {
        int margin = dip2px(this, 20);
        int imageWidth = 2 * getMeasuredWidth(ibMoreLeft)[0];
        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        return screenWidth - imageWidth - margin;
    }

    @Override
    protected void onDestroy() {
        relealeAnim();
        try {
            this.unregisterReceiver(monitor);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        super.onDestroy();
    }

    class Monitor extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                initWifiInfo();
            }
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                initWifiInfo();
            }
        }
    }


    /**
     * 更新配网提示信息
     *
     * @param tips
     */
    private void updateConnectTips(final String tips) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvMatchingTips.setText(tips);
            }
        });
    }

    /**
     * 更新配网提示信息
     *
     * @param resId
     */
    private void updateConnectTips(final int resId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvMatchingTips.setText(resId);
            }
        });
    }

    class WifiPairImp implements WifiPair.WifiPairCallback {

        @Override
        public void onStartPairAyla() {
            updateConnectTips(R.string.Purifier_SendConfig);
        }

        @Override
        public void onStartPariMxChip() {
            updateConnectTips(R.string.Purifier_SendConfig);
        }

        @Override
        public void onSendConfiguration() {
            updateConnectTips(R.string.Purifier_SendConfig);
        }

        @Override
        public void onActivateDevice() {
            updateConnectTips(R.string.Purifier_WaitRegist);
        }

        @Override
        public void onWaitConnectWifi() {
            updateConnectTips(R.string.Purifier_WaitRestart);
        }

        @Override
        public void onPairComplete(final BaseDeviceIO io) {
            selDeviceIo = io;
            if (io != null) {
                Log.e(TAG, "onPairComplete: " + io.getAddress() + " , type:" + io.getType());
            } else {
                Log.e(TAG, "onPairComplete: io is null");
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (io != null) {
//                        mDevAdpater.clear();
                        if (!mDevAdpater.hasDevice(io)) {
                            mDevAdpater.addItem(io);
                            mDevAdpater.setDefaultClick(0);
                        }
                        showFoundDevice();
                    } else {
                        showMatchFail();
                    }
                }
            });
        }

        @Override
        public void onPairFailure(Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showMatchFail();
                }
            });
        }
    }
}
