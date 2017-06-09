package com.ozner.cup.Device.AirPurifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ozner.AirPurifier.AirPurifier_MXChip;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Base.WebActivity;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Bean.OznerBroadcastAction;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.UserInfo;
import com.ozner.cup.R;
import com.ozner.cup.UIView.FilterProgressView;
import com.ozner.cup.Utils.LCLogUtils;
import com.ozner.cup.Utils.WeChatUrlUtil;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OznerDeviceManager;

import java.util.Calendar;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.udesk.UdeskSDKManager;

public class AirVerFilterActivity extends BaseActivity {
    private static final String TAG = "AirVerFilterActivity";

    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.tv_pmQuestion)
    TextView tvPmQuestion;
    @InjectView(R.id.tv_pmValue)
    TextView tvPmValue;
    @InjectView(R.id.tv_vocQuestion)
    TextView tvVocQuestion;
    @InjectView(R.id.tv_vocValue)
    TextView tvVocValue;
    @InjectView(R.id.tv_cleanValue)
    TextView tvCleanValue;
    @InjectView(R.id.tv_filter_remind)
    TextView tvFilterRemind;
    @InjectView(R.id.filterProgress)
    FilterProgressView filterProgress;
    @InjectView(R.id.llay_bottom)
    LinearLayout llayBottom;

    private UserInfo userInfo;
    AirPurifierMonitor airMonitor;
    private AirPurifier_MXChip mAirPurifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_ver_filter);
        ButterKnife.inject(this);
        initToolBar();
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.cup_detail_bg));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.cup_detail_bg));
        }
        filterProgress.setThumb(R.drawable.filter_status_thumb);
        if (UserDataPreference.isLoginEmail(this)) {
            llayBottom.setVisibility(View.GONE);
        }

        String userid = OznerPreference.GetValue(this, OznerPreference.UserId, null);
        if (userid != null && !userid.isEmpty()) {
            userInfo = DBManager.getInstance(this).getUserInfo(userid);
        }

        try {
            String mac = getIntent().getStringExtra(Contacts.PARMS_MAC);
            Log.e(TAG, "onCreate: mac:" + mac);
            mAirPurifier = (AirPurifier_MXChip) OznerDeviceManager.Instance().getDevice(mac);
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
        refreshPM25();
        refreshVoc();
        refeshClean();
        refreshFilter();
    }

    /**
     * 刷新PM2.5
     */
    private void refreshPM25() {
        if (mAirPurifier != null) {
            int pm25 = mAirPurifier.sensor().PM25();
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
     * 刷新VOC
     */
    private void refreshVoc() {
        if (mAirPurifier != null) {
            if (mAirPurifier.sensor().PM25() != 65535) {
                switch (mAirPurifier.sensor().VOC()) {
                    case -1:
                        tvVocValue.setText(R.string.in_test);
                        break;
                    case 0:
                        tvVocValue.setText(R.string.excellent);
                        break;
                    case 1:
                        tvVocValue.setText(R.string.good);
                        break;
                    case 2:
                        tvVocValue.setText(R.string.ordinary);
                        break;
                    case 3:
                        tvVocValue.setText(R.string.bads);
                        break;
                    default:
                        tvVocValue.setText(R.string.state_null);
                        break;
                }
            } else {
                tvVocValue.setText(R.string.state_null);
            }
        }
    }

    /**
     * 刷新净化量
     */
    private void refeshClean() {
        if (mAirPurifier != null) {
            if (mAirPurifier.sensor().TotalClean() > 0 && mAirPurifier.sensor().TotalClean() != 65535) {
                int clean = mAirPurifier.sensor().TotalClean() / 1000;
                Log.e(TAG, "refeshClean: " + clean);
                tvCleanValue.setText(String.valueOf(clean));
            } else {
                tvCleanValue.setText("0");
            }
        }
    }

    /**
     * 刷新滤芯状态
     */
    private void refreshFilter() {
        if (mAirPurifier != null) {
            int lvXin = 0;
            if (mAirPurifier.Type().equals("580c2783")) {//君融的空净产品单独处理
                filterProgress.setShowTime(false);
                int workTime = mAirPurifier.sensor().FilterStatus().workTime;
                int maxTime = mAirPurifier.sensor().FilterStatus().maxWorkTime;
                Log.e(TAG, "refreshFilter: workTime:" + workTime + " ,maxTime:" + maxTime);
                float lvXinTemp = 0;
                if (maxTime > 0)
                    lvXinTemp = 1 - (float) workTime / maxTime;
                lvXinTemp = Math.min(1, lvXinTemp);
                lvXinTemp = Math.max(0, lvXinTemp);
                LCLogUtils.E(TAG,"滤芯状态："+lvXinTemp);
                lvXin = (int) (lvXinTemp * 100);
                filterProgress.update((100 - lvXin) * filterProgress.getWarranty() / 100);
            } else {
                filterProgress.setShowTime(true);
                Date proDate = mAirPurifier.sensor().FilterStatus().lastTime;
                Date stopDate = mAirPurifier.sensor().FilterStatus().stopTime;
                long proMill = proDate.getTime();
                long stopMill = stopDate.getTime();
                long currentMill = Calendar.getInstance().getTimeInMillis();
                long totalTime = (stopMill - proMill) / (24 * 3600 * 1000);
                long useTime = (currentMill - proMill) / (24 * 3600 * 1000);
                try {
                    lvXin = Math.round((totalTime - useTime) * 100 / totalTime);
                    if (lvXin < 0 || lvXin > 100) {
                        lvXin = 0;
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "refreshFilter_Ex: " + ex.getMessage());
                }
                filterProgress.initTime(proDate, stopDate);
                filterProgress.update(Calendar.getInstance().getTime());
            }
            tvFilterRemind.setText(String.valueOf(lvXin));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AirPurifier_MXChip.ACTION_AIR_PURIFIER_SENSOR_CHANGED);
        filter.addAction(AirPurifier_MXChip.ACTION_AIR_PURIFIER_STATUS_CHANGED);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick({R.id.tv_pmQuestion, R.id.tv_vocQuestion, R.id.tv_buy_filter_btn, R.id.tv_chatbtn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_pmQuestion:
                Intent pmIntent = new Intent(this, PMIntroduceActivity.class);
                startActivity(pmIntent);
                break;
            case R.id.tv_vocQuestion:
                Intent vocIntnet = new Intent(this, VOCIntroduceActivity.class);
                startActivity(vocIntnet);
                break;
            case R.id.tv_buy_filter_btn:
                if (userInfo != null && userInfo.getMobile() != null && !userInfo.getMobile().isEmpty()) {
                    String shopUrl = WeChatUrlUtil.formatKjShopUrl(userInfo.getMobile(), OznerPreference.getUserToken(this), "zh", "zh");
                    Intent shopIntent = new Intent(this, WebActivity.class);
                    shopIntent.putExtra(Contacts.PARMS_URL, shopUrl);
                    startActivity(shopIntent);
                } else {
                    showToastCenter(R.string.userinfo_miss);
                }
                break;
            case R.id.tv_chatbtn:
                UdeskSDKManager.getInstance().toLanuchChatAcitvity(this);
//                sendBroadcast(new Intent(OznerBroadcastAction.OBA_SWITCH_CHAT));
//                this.finish();
                break;
        }
    }

    class AirPurifierMonitor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BaseDeviceIO.ACTION_DEVICE_CONNECTED)
                    || intent.getAction().equals(BaseDeviceIO.ACTION_DEVICE_CONNECTING)
                    || intent.getAction().equals(BaseDeviceIO.ACTION_DEVICE_DISCONNECTED)) {
                refreshFilter();
            }
            refreshPM25();
            refreshVoc();
            refeshClean();
        }
    }
}
