package com.ozner.cup.Device.Cup;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Base.WebActivity;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Bean.OznerBroadcastAction;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.Cup;
import com.ozner.cup.CupRecord;
import com.ozner.cup.CupRecordList;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.OznerDeviceSettings;
import com.ozner.cup.R;
import com.ozner.cup.UIView.ChartAdapter;
import com.ozner.cup.UIView.UIXVolumeChartView;
import com.ozner.device.OznerDeviceManager;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by ozner_67 on 2016/11/30.
 * 邮箱：xinde.zhang@cftcn.com
 * <p>
 * 饮水量详情
 */
public class CupVolumActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "CupVolumActivity";
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.tv_waterVolum)
    TextView tvWaterVolum;
    @InjectView(R.id.tv_tdsRankValue)
    TextView tvTdsRankValue;
    @InjectView(R.id.tv_chat_btn)
    TextView tvChatBtn;
    @InjectView(R.id.iv_volum_tip_icon)
    ImageView ivVolumTipIcon;
    @InjectView(R.id.tv_volum_tip)
    TextView tvVolumTip;
    @InjectView(R.id.rg_switch)
    RadioGroup rgSwitch;
    @InjectView(R.id.uixVolumeChart)
    UIXVolumeChartView uixVolumeChart;
    @InjectView(R.id.tv_water_know)
    TextView tvWaterKnow;
    @InjectView(R.id.tv_buy_water_purifier)
    TextView tvBuyWaterPurifier;

    private String mac = "";
    private int volumeRank = 0;
    private Cup mCup;
    private int[] dataDay, dataWeek, dataMonth;
    private ChartAdapter adapterDay, adapterWeek, adapterMonth;
    Calendar recordCal = Calendar.getInstance();
    private String mUserid;
    private int waterGoal = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cup_volum);
        ButterKnife.inject(this);
        rgSwitch.setOnCheckedChangeListener(this);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.cup_detail_bg));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.cup_detail_bg));
        }
        try {
            mUserid = UserDataPreference.GetUserData(this, UserDataPreference.UserId, "");
            mac = getIntent().getStringExtra(Contacts.PARMS_MAC);
            volumeRank = getIntent().getIntExtra(Contacts.PARMS_RANK, 0);
            Log.e(TAG, "onCreate: mac:" + mac);
            mCup = (Cup) OznerDeviceManager.Instance().getDevice(mac);
            OznerDeviceSettings oznerSetting = DBManager.getInstance(this).getDeviceSettings(mUserid, mCup.Address());
            if (oznerSetting != null) {
                if (oznerSetting.getAppData(Contacts.DEV_USER_WATER_GOAL) != null) {
                    waterGoal = Integer.parseInt((String) oznerSetting.getAppData(Contacts.DEV_USER_WATER_GOAL));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "onCreate_Ex: " + ex.getMessage());
        }
        initToolBar();
        initDataAdater();
        initRecordCal();
    }


    @Override
    protected void onResume() {
        initViewData();
        super.onResume();
    }

    /**
     * 初始化查询时间
     */
    private void initRecordCal() {
        recordCal = Calendar.getInstance();
        recordCal.set(Calendar.HOUR_OF_DAY, 0);
        recordCal.set(Calendar.MINUTE, 0);
        recordCal.set(Calendar.SECOND, 0);
        recordCal.set(Calendar.MILLISECOND, 0);
    }

    /**
     * 初始化ToolBar
     */
    private void initToolBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        title.setText(R.string.water_volum);
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
     * 初始化数据适配器
     */
    private void initDataAdater() {
        dataDay = new int[24];
        dataWeek = new int[7];
        dataMonth = new int[31];
        adapterDay = new ChartAdapter() {
            @Override
            public int count() {
                return dataDay.length;
            }

            @Override
            public int getValue(int Index) {
                return dataDay[Index];
            }

            @Override
            public int getMax() {
                return 400;
            }

            @Override
            public ViewMode getViewMode() {
                return ViewMode.Day;
            }
        };
        adapterWeek = new ChartAdapter() {
            @Override
            public int count() {
                return dataWeek.length;
            }

            @Override
            public int getValue(int Index) {
                return dataWeek[Index];
            }

            @Override
            public int getMax() {
                return 3000;
            }

            @Override
            public ViewMode getViewMode() {
                return ViewMode.Week;
            }
        };
        adapterMonth = new ChartAdapter() {
            @Override
            public int count() {
                return dataMonth.length;
            }

            @Override
            public int getValue(int Index) {
                return dataMonth[Index];
            }

            @Override
            public int getMax() {
                return 3000;
            }

            @Override
            public ViewMode getViewMode() {
                return ViewMode.Month;
            }
        };
    }

    /**
     * 初始化页面数据
     */
    private void initViewData() {
        try {
            initWterVolum();
            initDayRecord();
            initWeekRecord();
            initMonthRecord();
            showDayData();
        } catch (Exception ex) {
            Log.e(TAG, "initViewData_Ex: " + ex.getMessage());
        }
    }

    /**
     * 初始化饮水量
     */
    private void initWterVolum() {
        if (mCup != null) {
            tvTdsRankValue.setText(String.valueOf(volumeRank));
//            int waterGoal = (int) mCup.Setting().get(Contacts.DEV_USER_WATER_GOAL, -1);
//            if (-1 == waterGoal) {
//                waterGoal = 2000;
//            }
            CupRecord record = mCup.Volume().getRecordByDate(recordCal.getTime());
            if (record != null) {
                if (record.Volume < waterGoal) {
                    tvWaterVolum.setText(String.valueOf(record.Volume * 100 / waterGoal));
                } else {
                    tvWaterVolum.setText("100");
                }
            } else {
                tvWaterVolum.setText("0");
            }
        }
    }

    /**
     * 初始化饮水量日数据
     */
    private void initDayRecord() {
        if (mCup != null && mCup.Volume() != null) {
            Calendar dayCal = Calendar.getInstance();
            dayCal.set(Calendar.HOUR_OF_DAY, 0);
            dayCal.set(Calendar.MINUTE, 0);
            dayCal.set(Calendar.SECOND, 0);
            dayCal.set(Calendar.MILLISECOND, 0);
            CupRecord[] records = mCup.Volume().getRecordByDate(dayCal.getTime(), CupRecordList.QueryInterval.Hour);
            for (int i = 0; i < records.length; i++) {
                dataDay[records[i].start.getHours()] = records[i].Volume;
            }
        }
    }

    /**
     * 初始化饮水量周数据
     */
    private void initWeekRecord() {
        try {
            if (mCup != null && mCup.Volume() != null) {
                Calendar dayCal = Calendar.getInstance();
                dayCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                dayCal.set(Calendar.HOUR_OF_DAY, 0);
                dayCal.set(Calendar.MINUTE, 0);
                dayCal.set(Calendar.SECOND, 0);
                dayCal.set(Calendar.MILLISECOND, 0);
                CupRecord[] records = mCup.Volume().getRecordByDate(dayCal.getTime(), CupRecordList.QueryInterval.Day);
                for (int i = 0; i < records.length; i++) {

                    if (records[i].start.getDay() != 0) {
                        dataWeek[records[i].start.getDay() - 1] = records[i].Volume;
                    } else {
                        dataWeek[6] = records[i].Volume;
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "initWeekRecord_Ex: " + ex.getMessage());
        }
    }

    /**
     * 初始化饮水量月数据
     */
    private void initMonthRecord() {
        try {
            if (mCup != null && mCup.Volume() != null) {
                Calendar dayCal = Calendar.getInstance();
                dayCal.set(Calendar.DAY_OF_MONTH, 1);
                dayCal.set(Calendar.HOUR_OF_DAY, 0);
                dayCal.set(Calendar.MINUTE, 0);
                dayCal.set(Calendar.SECOND, 0);
                dayCal.set(Calendar.MILLISECOND, 0);

                CupRecord[] records = mCup.Volume().getRecordByDate(dayCal.getTime(), CupRecordList.QueryInterval.Day);
                for (int i = 0; i < records.length; i++) {
                    dataMonth[records[i].start.getDate() - 1] = records[i].Volume;
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "initMonthRecord_Ex: " + ex.getMessage());
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_day:
                showDayData();
                break;
            case R.id.rb_week:
                showWeekData();
                break;
            case R.id.rb_month:
                showMonthData();
                break;
        }
    }

    /**
     * 显示日数据
     */
    private void showDayData() {
        uixVolumeChart.clearTag();
        uixVolumeChart.putTag(50, "50");
        uixVolumeChart.putTag(200, "200");
        uixVolumeChart.putTag(400, "400\nml");
        uixVolumeChart.setAdapter(adapterDay);
        uixVolumeChart.startAnimation();
    }

    /**
     * 显示周数据
     */
    private void showWeekData() {
        uixVolumeChart.clearTag();
        uixVolumeChart.putTag(1000, "1000");
        uixVolumeChart.putTag(2000, "2000");
        uixVolumeChart.putTag(3000, "3000\nml");
        uixVolumeChart.setAdapter(adapterWeek);
        uixVolumeChart.startAnimation();
    }

    /**
     * 显示月数据
     */
    private void showMonthData() {
        uixVolumeChart.clearTag();
        uixVolumeChart.putTag(1000, "1000");
        uixVolumeChart.putTag(2000, "2000");
        uixVolumeChart.putTag(3000, "3000\nml");
        uixVolumeChart.setAdapter(adapterMonth);
        uixVolumeChart.startAnimation();
    }

    @OnClick({R.id.tv_chat_btn, R.id.tv_water_know, R.id.tv_buy_water_purifier})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_chat_btn:
                sendBroadcast(new Intent(OznerBroadcastAction.OBA_SWITCH_CHAT));
                this.finish();
                break;
            case R.id.tv_water_know:
                Intent knowIntent = new Intent(this, WebActivity.class);
                knowIntent.putExtra(Contacts.PARMS_URL, Contacts.waterHealthUrl);
                startActivity(knowIntent);
                break;
            case R.id.tv_buy_water_purifier:
                Intent eshopIntent = new Intent(OznerBroadcastAction.OBA_SWITCH_ESHOP);
                sendBroadcast(eshopIntent);
                this.finish();
                break;
        }
    }
}
