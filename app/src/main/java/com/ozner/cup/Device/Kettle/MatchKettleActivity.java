package com.ozner.cup.Device.Kettle;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
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
import com.ozner.Kettle.Kettle;
import com.ozner.Kettle.KettleMgr;
import com.ozner.bluetooth.BluetoothIO;
import com.ozner.bluetooth.BluetoothScan;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.CupManager;
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
 * description: MatchKettleActivity
 * autour: ozner_67
 * date: 2017/7/12 16:55
 * e-mail: xinde.zhang@cftcn.com
 */
public class MatchKettleActivity extends BaseActivity {
    private static final String TAG = "MatchKettleActivity";
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.tv_state)
    TextView tvState;
    @InjectView(R.id.ib_moreLeft)
    ImageButton ibMoreLeft;
    @InjectView(R.id.rv_found_devices)
    RecyclerView rvFoundDevices;
    @InjectView(R.id.ib_moreRight)
    ImageButton ibMoreRight;
    @InjectView(R.id.llay_found_device)
    LinearLayout llayFoundDevice;
    @InjectView(R.id.iv_match_loading)
    ImageView ivMatchLoading;
    @InjectView(R.id.iv_match_icon)
    ImageView ivMatchIcon;
    @InjectView(R.id.tv_match_notice)
    TextView tvMatchNotice;
    @InjectView(R.id.tv_match_type)
    TextView tvMatchType;
    @InjectView(R.id.tv_notice_Bottom)
    TextView tvNoticeBottom;
    @InjectView(R.id.llay_match_fail)
    LinearLayout llayMatchFail;
    @InjectView(R.id.et_device_name)
    EditText etDeviceName;
    @InjectView(R.id.et_device_position)
    EditText etDevicePosition;
    @InjectView(R.id.iv_place_icon)
    ImageView ivPlaceIcon;
    @InjectView(R.id.llay_inputInfo)
    LinearLayout llayInputInfo;
    @InjectView(R.id.tv_succes_holder)
    TextView tvSuccesHolder;

    private Monitor mMonitor;
    private boolean isSearching = true;
    private boolean isShowFound = false;
    private FoundDevcieAdapter mDevAdpater;
    TimerCount timerCount;
    private BaseDeviceIO selDeviceIo;
    private String mUserid;
    private PermissionUtil.PermissionRequestObject perReqResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_kettle);
        ButterKnife.inject(this);
        mUserid = OznerPreference.GetValue(this, OznerPreference.UserId, "");
        initActionBar();
        initNormalInfo();
        initFoundDeviceView();
//        startFindDevice();
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
                                startFindDevice();
                            }
                        });
                    }
                }).onAnyDenied(new Func() {
                    @Override
                    protected void call() {
                        showToastCenter(R.string.blue_need_pos);
                        MatchKettleActivity.this.finish();
                    }
                }).ask(2);
    }


    /**
     * 初始化actionBar
     */
    private void initActionBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        title.setText(R.string.match_device);
    }

    /**
     * 初始化基本信息
     */
    private void initNormalInfo() {
        etDeviceName.setHint(R.string.input_cup_name);
        tvMatchNotice.setText(R.string.reverse_cup);
        etDevicePosition.setText(R.string.cup_mine);
    }


    /**
     * 初始化RecyleView
     */
    private void initFoundDeviceView() {
        mDevAdpater = new FoundDevcieAdapter(this, R.drawable.found_cup_selected, R.drawable.found_cup_unselected);
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
                        //只添加 水杯
                        if (KettleMgr.isKettle(device.getType())) {
                            if (device instanceof BluetoothIO) {
                                BluetoothIO bluetoothIO = (BluetoothIO) device;
                                //检查水杯处于start模式
                                if (Kettle.isBindMode(bluetoothIO))
                                    if (!mDevAdpater.hasDevice(device))
                                        mDevAdpater.addItem(device);
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
                    showToastCenter(R.string.device_disConnect);
//                    Toast.makeText(this, getString(R.string.device_disConnect), Toast.LENGTH_SHORT).show();
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
            if (device != null && CupManager.IsCup(device.Type())) {
                OznerDeviceManager.Instance().save(device);
                UserDataPreference.SetUserData(this, UserDataPreference.SelMac, device.Address());//保存选中的设备mac
                if (etDeviceName.getText().length() > 0) {
                    device.Setting().name(etDeviceName.getText().toString().trim());
                } else {
                    device.Setting().name(getString(R.string.smart_glass));
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return true;
    }


    /**
     * 配对界面初始化
     */
    private void startFindDevice() {
        llayInputInfo.setVisibility(View.GONE);
        llayMatchFail.setVisibility(View.GONE);
        title.setText(R.string.match_device);
        llayFoundDevice.setVisibility(View.INVISIBLE);
        tvMatchType.setVisibility(View.VISIBLE);
        tvMatchType.setText(getString(R.string.matching_bluetooth));
//        tvMatchNotice.setText(getString(R.string.match_notice_tap));
        tvMatchNotice.setText(R.string.reverse_cup);
        ivMatchIcon.setImageResource(R.drawable.match_device_cup);
        ivMatchLoading.setImageResource(R.drawable.match_loading);
        ivMatchLoading.setVisibility(View.VISIBLE);
        ivMatchIcon.setVisibility(View.VISIBLE);
        tvSuccesHolder.setVisibility(View.VISIBLE);

        startRotate();
        registerBlueReceiver();
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
        ivMatchLoading.setImageResource(R.drawable.found_cup_selected);
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
        title.setText(R.string.match_failed);
        tvMatchType.setVisibility(View.VISIBLE);
        tvMatchNotice.setVisibility(View.VISIBLE);
        tvMatchType.setText(getString(R.string.rematch));
        tvMatchNotice.setText(getString(R.string.no_success));
        llayMatchFail.setVisibility(View.VISIBLE);
        ivMatchLoading.setImageResource(R.drawable.match_device_failed);
    }

    /**
     * 显示输入信息界面
     */
    private void showEditDeviceInfo() {
//        isEditShow = true;
        stopRotate();
        title.setText(R.string.match_successed);
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
     * 注册蓝牙监听
     */
    private void registerBlueReceiver() {
        if (mMonitor == null) {
            mMonitor = new Monitor();
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothScan.ACTION_SCANNER_FOUND);
            this.registerReceiver(mMonitor, filter);
        }
    }

    /**
     * 注销蓝牙监听
     */
    private void unRegisterBlueReceiver() {
        if (mMonitor != null) {
            this.unregisterReceiver(mMonitor);
            mMonitor = null;
        }
    }

    @Override
    protected void onDestroy() {
        unRegisterBlueReceiver();
        System.gc();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (perReqResult != null) {
            perReqResult.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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


    class TimerCount extends CountDownTimer {
        public TimerCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            if (!MatchKettleActivity.this.isFinishing() && !MatchKettleActivity.this.isDestroyed()) {
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
