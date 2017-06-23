package com.ozner.cup.Device.ROWaterPurifier;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.kayvannj.permission_utils.Func;
import com.github.kayvannj.permission_utils.PermissionUtil;
import com.ozner.WaterPurifier.WaterPurifierManager;
import com.ozner.WaterPurifier.WaterPurifier_RO_BLE;
import com.ozner.bluetooth.BluetoothIO;
import com.ozner.bluetooth.BluetoothScan;
import com.ozner.cup.Base.BaseActivity;
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

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 *
 */
public class MatchROWaterPuriferActivity extends BaseActivity {
    private static final String TAG = "MatchROWater";

    @InjectView(R.id.title)
    TextView tv_title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.ib_moreLeft)
    ImageButton ibMoreLeft;
    @InjectView(R.id.iv_match_loading)
    ImageView ivMatchLoading;
    @InjectView(R.id.iv_match_icon)
    ImageView ivMatchIcon;
    @InjectView(R.id.tv_match_notice)
    TextView tvMatchNotice;
    @InjectView(R.id.tv_match_type)
    TextView tvMatchType;
    @InjectView(R.id.rv_found_devices)
    RecyclerView rvFoundDevices;
    @InjectView(R.id.llay_found_device)
    LinearLayout llayFoundDevice;
    @InjectView(R.id.et_device_name)
    EditText etDeviceName;
    @InjectView(R.id.et_device_position)
    EditText etDevicePosition;
    @InjectView(R.id.iv_place_icon)
    ImageView ivPlaceIcon;
    @InjectView(R.id.tv_succes_holder)
    TextView tvSuccesHolder;
    @InjectView(R.id.llay_match_fail)
    LinearLayout llayMatchFail;
    @InjectView(R.id.llay_inputInfo)
    LinearLayout llayInputInfo;
    @InjectView(R.id.tv_notice_Bottom)
    TextView tvNoticeBottom;

    private boolean isSearching = true;
    private boolean isShowFound = false;
    TimerCount timerCount;
    private FoundDevcieAdapter mDevAdpater;
    private BaseDeviceIO selDeviceIo;
    Monitor monitor;
    private String mUserid;
    private PermissionUtil.PermissionRequestObject perReqResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_ro);
        ButterKnife.inject(this);
        mUserid = OznerPreference.GetValue(this, OznerPreference.UserId, "");
        initActionBar();
        initNormalInfo();
        initFoundDeviceView();
        beginMatch();
    }

    /**
     * 检查位置权限，并开始配对
     */
    private void beginMatch() {
        perReqResult = PermissionUtil.with(this).request(Manifest.permission.ACCESS_COARSE_LOCATION)
                .onAllGranted(new Func() {
                    @Override
                    protected void call() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                Toast.makeText(MatchROWaterPuriferActivity.this,"ro水机beginMatch------startFindDevice",Toast.LENGTH_SHORT).show();
                                startFindDevice();
                            }
                        });
                    }
                }).onAnyDenied(new Func() {
                    @Override
                    protected void call() {
                        showToastCenter(R.string.blue_need_pos);
                        MatchROWaterPuriferActivity.this.finish();
                    }
                }).ask(2);
    }


    /**
     * 配对界面初始化
     */
    private void startFindDevice() {
        llayInputInfo.setVisibility(View.GONE);
        llayMatchFail.setVisibility(View.GONE);
        tv_title.setText(R.string.match_device);
        llayFoundDevice.setVisibility(View.INVISIBLE);
        tvMatchType.setVisibility(View.VISIBLE);
        tvMatchType.setText(getString(R.string.matching_bluetooth));
//        tvMatchNotice.setText(getString(R.string.match_notice_tap));
        tvMatchNotice.setText(R.string.reverse_ro);
        ivMatchIcon.setImageResource(R.drawable.ropurifier_match);
        ivMatchLoading.setImageResource(R.drawable.match_loading);
        ivMatchLoading.setVisibility(View.VISIBLE);
        ivMatchIcon.setVisibility(View.VISIBLE);
        tvSuccesHolder.setVisibility(View.VISIBLE);

        //开始旋转动画
        startRotate();
        registerBlueReceiver();
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
     * 初始化基本信息
     */
    private void initNormalInfo() {
        etDeviceName.setHint(R.string.inpu_water_name);
    }

    /**
     * 初始化RecyleView
     */
    private void initFoundDeviceView() {
        mDevAdpater = new FoundDevcieAdapter(this, R.drawable.rowater_purifier_selected, R.drawable.rowater_purifier_small);
        mDevAdpater.setOnItemClickListener(new FoundDevcieAdapter.ClientClickListener() {
            @Override
            public void onItemClick(int position, BaseDeviceIO deviceIO) {
                Log.e(TAG, "onItemClick: " + position);
                selDeviceIo = deviceIO;
                showEditDeviceInfo();
            }
        });
        rvFoundDevices.setAdapter(mDevAdpater);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvFoundDevices.setLayoutManager(linearLayoutManager);
    }


    @OnClick({R.id.btn_rematch, R.id.btn_match_success})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_rematch:
                startFindDevice();
                break;
            case R.id.btn_match_success:
                if (selDeviceIo != null) {
                    saveDevice(selDeviceIo);
                } else {
                    Toast.makeText(this, getString(R.string.device_disConnect), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * 保存设备
     *
     * @param deviceIo
     */
    private void saveDevice(BaseDeviceIO deviceIo) {
        try {
            OznerDevice device = OznerDeviceManager.Instance().getDevice(deviceIo);
            if (device != null && WaterPurifierManager.IsWaterPurifier(device.Type())) {
                OznerDeviceManager.Instance().save(device);
                UserDataPreference.SetUserData(this, UserDataPreference.SelMac, device.Address());//保存选中的设备mac
                if (etDeviceName.getText().length() > 0) {
                    device.Setting().name(etDeviceName.getText().toString().trim());
                } else {
                    device.Setting().name(getString(R.string.water_ropurifier));
                }
                device.updateSettings();

                saveDeviceToDB(mUserid, device);
            } else {
                Toast.makeText(this, getString(R.string.device_disConnect), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            this.finish();
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
     * 显示输入信息界面
     */
    private void showEditDeviceInfo() {
//        isEditShow = true;
        stopRotate();
        tv_title.setText(R.string.match_successed);
        llayMatchFail.setVisibility(View.GONE);
        ivMatchIcon.setVisibility(View.GONE);
        tvSuccesHolder.setVisibility(View.GONE);
        tvMatchType.setVisibility(View.INVISIBLE);
        tvMatchNotice.setVisibility(View.INVISIBLE);
        llayFoundDevice.setVisibility(View.VISIBLE);
        llayInputInfo.setVisibility(View.VISIBLE);
        ivMatchLoading.setImageResource(R.drawable.match_device_successed);
    }

    /**
     * 注册蓝牙监听
     */
    private void registerBlueReceiver() {
        if (monitor == null) {
//            Toast.makeText(MatchROWaterPuriferActivity.this,"ro水机配对monitor为空",Toast.LENGTH_LONG).show();
            monitor = new Monitor();
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothScan.ACTION_SCANNER_FOUND);
            loadFoundDevices();
            this.registerReceiver(monitor, filter);
        } else {
//            Toast.makeText(MatchROWaterPuriferActivity.this,"ro水机配对monitor不为空",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 注销蓝牙监听
     */
    private void unRegisterBlueReceiver() {
        if (monitor != null) {
            this.unregisterReceiver(monitor);
            monitor = null;
        }
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

    @Override
    protected void onDestroy() {
        unRegisterBlueReceiver();
        System.gc();
        super.onDestroy();
    }

    /**
     * 开始旋转
     */
    private void startRotate() {
        StartTime();
        RotateAnimation animation = new RotateAnimation(0f, -360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(3000);
        animation.setRepeatCount(9);
        animation.setRepeatCount(-1);
        LinearInterpolator li = new LinearInterpolator();
        animation.setInterpolator(li);
        animation.setFillAfter(false);
        ivMatchLoading.startAnimation(animation);
    }

    /**
     * 结束旋转
     */
    private void stopRotate() {
        StopTime();
        if (ivMatchLoading != null) {
            {
                Animation s = ivMatchLoading.getAnimation();
                if (s != null) {
                    s.cancel();
                }
            }
        }
    }

    /**
     * 开始计时器
     */
    public void StartTime() {
        isSearching = true;
        if (timerCount == null) {
            timerCount = new TimerCount(30000, 1000);
            timerCount.start();
        } else {
            StopTime();
            StartTime();
        }
    }

    /**
     * 停止计时器
     */
    public void StopTime() {
        isSearching = false;
        if (timerCount != null) {
            timerCount.cancel();
            timerCount = null;
        }
    }

    /**
     * 广播监听
     */
    class Monitor extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadFoundDevices();
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

    /**
     * 加载搜索到的设备
     */
    private void loadFoundDevices() {
        try {
//            mDevAdpater.clear();
            if (OznerDeviceManager.Instance() != null) {
                BaseDeviceIO[] deviceIOs = null;
                try {
                    deviceIOs = OznerDeviceManager.Instance().getNotBindDevices();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    deviceIOs = null;
                }
                if (deviceIOs != null) {
                    for (BaseDeviceIO device : deviceIOs) {
                        //添加ro水机
                        if ("Ozner RO".equals(device.getType()) || "RO Comml".equals(device.getType())) {
                            if (device instanceof BluetoothIO) {
                                BluetoothIO bluetoothIO = (BluetoothIO) device;
                                if (WaterPurifier_RO_BLE.isBindMode(bluetoothIO)) {
                                    if (!mDevAdpater.hasDevice(device)) {
                                        mDevAdpater.addItem(device);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (mDevAdpater.getItemCount() > 0) {
                if (isSearching) {
                    int deviceCount = mDevAdpater.getItemCount();
                    if (deviceCount > 3) {
                        deviceCount = 3;
                    }
                    mDevAdpater.setItemWidth(getRVWidth() / deviceCount);

                    if (!isShowFound) {
                        showFoundDevice();
                    }
                }
            } else {
                if (!isSearching) {
                    showNoFoundDevice();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "loadFoundDevices_Ex: " + ex.getMessage());
            showNoFoundDevice();
        }
    }

    /**
     * 显示搜索到设备
     */
    private void showFoundDevice() {
        stopRotate();
        isShowFound = true;
        ivMatchIcon.setVisibility(View.GONE);
        llayMatchFail.setVisibility(View.GONE);
        llayInputInfo.setVisibility(View.GONE);
        llayFoundDevice.setVisibility(View.VISIBLE);
        tvMatchNotice.setVisibility(View.INVISIBLE);
        tvMatchType.setVisibility(View.INVISIBLE);
        ivMatchLoading.setImageResource(R.drawable.rowater_purifier_selected);
        llayFoundDevice.setVisibility(View.VISIBLE);
        tvSuccesHolder.setVisibility(View.VISIBLE);
    }

    /**
     * 显示搜索失败界面
     */
    private void showNoFoundDevice() {
        stopRotate();
        isShowFound = false;
        tvSuccesHolder.setVisibility(View.GONE);
        ivMatchIcon.setVisibility(View.GONE);
        llayInputInfo.setVisibility(View.GONE);
        llayFoundDevice.setVisibility(View.INVISIBLE);
        tv_title.setText(R.string.match_failed);
        tvMatchType.setVisibility(View.VISIBLE);
        tvMatchNotice.setVisibility(View.VISIBLE);
        tvMatchType.setText(getString(R.string.rematch));
        tvMatchNotice.setText(getString(R.string.no_success));
        llayMatchFail.setVisibility(View.VISIBLE);
        ivMatchLoading.setImageResource(R.drawable.match_device_failed);
        tvNoticeBottom.setText(R.string.reverse_ro);
    }


    class TimerCount extends CountDownTimer {
        public TimerCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            if (!MatchROWaterPuriferActivity.this.isFinishing() && !MatchROWaterPuriferActivity.this.isDestroyed()) {
                if (mDevAdpater.getItemCount() > 0) {
                    if (!isShowFound)
                        showFoundDevice();
                } else {
                    showNoFoundDevice();
                    unRegisterBlueReceiver();
                }
            }
        }
    }
}
