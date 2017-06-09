package com.ozner.cup.Device.AirPurifier;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ozner.AirPurifier.AirPurifier_Bluetooth;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Base.WebActivity;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Bean.OznerBroadcastAction;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.UserInfo;
import com.ozner.cup.R;
import com.ozner.cup.UIView.IndicatorProgressBar;
import com.ozner.cup.Utils.LCLogUtils;
import com.ozner.cup.Utils.WeChatUrlUtil;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OperateCallback;
import com.ozner.device.OznerDeviceManager;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.udesk.UdeskSDKManager;

import static com.ozner.cup.R.id.ll_en_no;

public class AirDeskFilterActivity extends BaseActivity {
    private static final String TAG = "AirDeskFilterActivity";
    private static final int FILTER_MAX_WORK_TIME = 60000;

    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.tv_pmQuestion)
    TextView tvPmQuestion;
    @InjectView(R.id.tv_pmValue)
    TextView tvPmValue;
    @InjectView(R.id.tv_filter_remind)
    TextView tvFilterRemind;
    @InjectView(R.id.filterProgress)
    IndicatorProgressBar filterProgress;
    @InjectView(R.id.tv_chat_btn)
    TextView tvChatBtn;
    @InjectView(R.id.tv_buy_purifier)
    TextView tvBuyPurifier;
    @InjectView(ll_en_no)
    LinearLayout llEnNo;

    AirPurifierMonitor airMonitor;
    private AirPurifier_Bluetooth mDeskAirPurifier;
    private UserInfo userInfo;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_desk_filter);
        ButterKnife.inject(this);
        initToolBar();
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.cup_detail_bg));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.cup_detail_bg));
        }
        String userid = OznerPreference.GetValue(this, OznerPreference.UserId, null);
        if (userid != null && !userid.isEmpty()) {
            userInfo = DBManager.getInstance(this).getUserInfo(userid);
        }
        if(UserDataPreference.isLoginEmail(this)){
            llEnNo.setVisibility(View.GONE);
        }

        filterProgress.setThumb(R.drawable.filter_status_thumb);
        filterProgress.setMaxProgress(100);

        try {
            String mac = getIntent().getStringExtra(Contacts.PARMS_MAC);
            Log.e(TAG, "onCreate: mac:" + mac);
            mDeskAirPurifier = (AirPurifier_Bluetooth) OznerDeviceManager.Instance().getDevice(mac);
            refreshUIData();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "onCreate_Ex: " + ex.getMessage());
        }
    }


    /**
     * 初始化ToolBar
     */
    private void initToolBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        title.setText(R.string.air_indoor_detail);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.cup_detail_bg));
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
     * 刷新UI数据
     */
    private void refreshUIData() {
        if (mDeskAirPurifier != null) {
            refreshPM25();
            showFilterStatus();
        }
    }


    @OnClick({R.id.tv_pmQuestion, R.id.tv_chat_btn, R.id.tv_buy_purifier, R.id.tv_resetFilter})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_pmQuestion:
                Intent pmIntent = new Intent(this, PMIntroduceActivity.class);
                startActivity(pmIntent);
                break;
            case R.id.tv_chat_btn:
                UdeskSDKManager.getInstance().toLanuchChatAcitvity(this);
//                sendBroadcast(new Intent(OznerBroadcastAction.OBA_SWITCH_CHAT));
//                this.finish();
                break;
            case R.id.tv_buy_purifier:
                if (userInfo != null && userInfo.getMobile() != null && !userInfo.getMobile().isEmpty()) {
                    String shopUrl = WeChatUrlUtil.formatKjShopUrl(userInfo.getMobile(), OznerPreference.getUserToken(this), "zh", "zh");
                    Intent shopIntent = new Intent(this, WebActivity.class);
                    shopIntent.putExtra(Contacts.PARMS_URL, shopUrl);
                    startActivity(shopIntent);
                } else {
                    showToastCenter(R.string.userinfo_miss);
                }
                break;
            case R.id.tv_resetFilter:
                new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT)
                        .setTitle(R.string.filter_reset_title)
                        .setMessage(R.string.filter_reset_tips)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        resetFilter();
                                    }
                                });
                            }
                        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
                break;
        }
    }


    /**
     * 重置滤芯
     */
    private void resetFilter() {
        if (mDeskAirPurifier != null) {
            if (mDeskAirPurifier.connectStatus() == BaseDeviceIO.ConnectStatus.Connected) {
                mDeskAirPurifier.ResetFilter(new OperateCallback<Void>() {
                    @Override
                    public void onSuccess(Void var1) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                refreshUIData();
                            }
                        }, 300);
                    }

                    @Override
                    public void onFailure(Throwable var1) {
                        showToastCenter(R.string.send_status_fail);
                    }
                });
            } else {
                showToastCenter(R.string.device_disConnect);
            }
        } else {
            showToastCenter(R.string.Not_found_device);
        }
    }

    /**
     * 刷新PM2.5
     */
    private void refreshPM25() {
        if (mDeskAirPurifier != null) {
            int pm25 = mDeskAirPurifier.sensor().PM25();
            if (pm25 == 65535) {
                tvPmValue.setText(R.string.air_disconnect);
            } else if (pm25 > 0 && pm25 < 1000) {
                tvPmValue.setText(String.valueOf(pm25));
            } else {
                tvPmValue.setText(R.string.state_null);
            }
        }
    }

    /**
     * 显示滤芯状态
     */
    private void showFilterStatus() {
        float workTime = mDeskAirPurifier.sensor().FilterStatus().workTime;
        Log.e(TAG, "showFilterStatus:workTime: " + workTime);
        float maxWorkTime = mDeskAirPurifier.sensor().FilterStatus().maxWorkTime;
        if (maxWorkTime <= 0) {
            maxWorkTime = FILTER_MAX_WORK_TIME;
        }
        if (workTime > maxWorkTime) {
            workTime = maxWorkTime;
        }
        int ret = Math.round((1 - (workTime / maxWorkTime)) * 100);
        tvFilterRemind.setText(String.valueOf(ret));
        filterProgress.setProgress(100 - ret);
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AirPurifier_Bluetooth.ACTION_AIR_PURIFIER_SENSOR_CHANGED);
        filter.addAction(AirPurifier_Bluetooth.ACTION_AIR_PURIFIER_STATUS_CHANGED);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_CONNECTING);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_DISCONNECTED);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_CONNECTED);
        this.registerReceiver(airMonitor, filter);
    }

    @Override
    protected void onStop() {
        try {
            this.unregisterReceiver(airMonitor);
        } catch (Exception ex) {

        }
        super.onStop();
    }


    class AirPurifierMonitor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
//            if (intent.getAction().equals(BaseDeviceIO.ACTION_DEVICE_CONNECTED)
//                    || intent.getAction().equals(BaseDeviceIO.ACTION_DEVICE_CONNECTING)
//                    || intent.getAction().equals(BaseDeviceIO.ACTION_DEVICE_DISCONNECTED)) {
//            }
//            showFilterStatus();
//            refreshPM25();
            LCLogUtils.E(TAG,"AirPurifierMonitor:"+intent.getAction());
            refreshUIData();
        }
    }
}
