package com.ozner.yiquan.Device.Cup;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ozner.yiquan.Base.BaseActivity;
import com.ozner.yiquan.Base.WebActivity;
import com.ozner.yiquan.Bean.Contacts;
import com.ozner.yiquan.Bean.OznerBroadcastAction;
import com.ozner.yiquan.Cup;
import com.ozner.yiquan.CupRecord;
import com.ozner.yiquan.CupRecordList;
import com.ozner.yiquan.R;
import com.ozner.yiquan.UIView.ChartAdapter;
import com.ozner.yiquan.UIView.TDSChartView;
import com.ozner.yiquan.UIView.UIXWaterDetailProgress;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OznerDeviceManager;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by ozner_67 on 2016/11/30.
 * 邮箱：xinde.zhang@cftcn.com
 * <p>
 * 水温详情
 */
public class CupTempActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "CupTempActivity";

    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.tv_TempState)
    TextView tvTempState;
    @InjectView(R.id.tv_chat_btn)
    TextView tvChatBtn;
    @InjectView(R.id.iv_temp_tip_icon)
    ImageView ivTempTipIcon;
    @InjectView(R.id.tv_temp_tip)
    TextView tvTempTip;
    @InjectView(R.id.tv_chartTitle)
    TextView tvChartTitle;
    @InjectView(R.id.rb_day)
    RadioButton rbDay;
    @InjectView(R.id.rb_week)
    RadioButton rbWeek;
    @InjectView(R.id.rb_month)
    RadioButton rbMonth;
    @InjectView(R.id.rg_switch)
    RadioGroup rgSwitch;
    @InjectView(R.id.iv_left_btn)
    ImageView ivLeftBtn;
    @InjectView(R.id.uixWaterDetailProgress)
    UIXWaterDetailProgress uixWaterDetailProgress;
    @InjectView(R.id.iv_right_btn)
    ImageView ivRightBtn;
    @InjectView(R.id.llay_waterDetail)
    LinearLayout llayWaterDetail;
    @InjectView(R.id.tdsChartView)
    TDSChartView tdsChartView;
    @InjectView(R.id.tv_tapHealthPre)
    TextView tvTapHealthPre;
    @InjectView(R.id.tv_tapGenericPre)
    TextView tvTapGenericPre;
    @InjectView(R.id.tv_tapBadPre)
    TextView tvTapBadPre;
    @InjectView(R.id.llay_legend)
    LinearLayout llayLegend;
    @InjectView(R.id.llay_tdsChart)
    LinearLayout llayTdsChart;
    @InjectView(R.id.tv_water_know)
    TextView tvWaterKnow;
    @InjectView(R.id.tv_buy_water_purifier)
    TextView tvBuyWaterPurifier;

    private String mac = "";
    private Cup mCup;
    private CupMonitor mMonitor;
    private ChartAdapter adapterDay, adapterWeek, adapterMonth;
    private int[] dataDay;
    private int[] dataWeek;
    private int[] dataMonth;
    private boolean isProgress = true;
    private int chartIndex = 0;//日图表
    private int dayCool, dayMid, dayHot, dayTotal;
    private int weekCool, weekMid, weekHot, weekTotal;
    private int monthCool, monthMid, monthHot, monthTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cup_temp);
        ButterKnife.inject(this);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.cup_detail_bg));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.cup_detail_bg));
        }
        initToolBar();
        rgSwitch.setOnCheckedChangeListener(this);
        initTag();
        initDataAdapter();

        mMonitor = new CupMonitor();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Cup.ACTION_BLUETOOTHCUP_SENSOR);
        filter.addAction(Cup.ACTION_BLUETOOTHCUP_RECORD_COMPLETE);
        this.registerReceiver(mMonitor, filter);
        try {
            mac = getIntent().getStringExtra(Contacts.PARMS_MAC);
            Log.e(TAG, "onCreate: mac:" + mac);
            mCup = (Cup) OznerDeviceManager.Instance().getDevice(mac);
            initViewData();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "onCreate_Ex: " + ex.getMessage());
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

    /**
     * 初始化ToolBar
     */
    private void initToolBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        title.setText(R.string.water_temp_no_dot);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.cup_detail_bg));
    }

    /**
     * 初始化Progress标签
     */
    private void initTag() {
        uixWaterDetailProgress.setGoodTextId(R.string.temp_cool);
        uixWaterDetailProgress.setMidTextId(R.string.temp_moderation);
        uixWaterDetailProgress.setBadTextId(R.string.temp_hot);
        tdsChartView.clearTag();
        tdsChartView.putTag(CupRecord.Temperature_Low_Value, getString(R.string.temp_cool));
        tdsChartView.putTag(CupRecord.Temperature_High_Value, getString(R.string.temp_moderation));
        tdsChartView.putTag(100, getString(R.string.temp_hot));
    }

    /**
     * 初始化数据适配器
     */
    private void initDataAdapter() {
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
                return 100;
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
                return 100;
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
                return 100;
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
        initDayData();
        initWeekData();
        initMonthData();
        refreshTempState();

        showDayProgress();
    }

    /**
     * 初始化日数据
     */
    private void initDayData() {
        try {
            if (mCup != null && mCup.Volume() != null) {
                Calendar dayCal = Calendar.getInstance();
                dayCal.set(Calendar.HOUR_OF_DAY, 0);
                dayCal.set(Calendar.MINUTE, 0);
                dayCal.set(Calendar.SECOND, 0);
                dayCal.set(Calendar.MILLISECOND, 0);
                CupRecord dayRecord = mCup.Volume().getRecordByDate(dayCal.getTime());
                dayCool = dayRecord.Temperature_Low;
                dayMid = dayRecord.Temperature_Mid;
                dayHot = dayRecord.Temperature_High;
                dayTotal = dayRecord.Count;

                CupRecord[] records = mCup.Volume().getRecordByDate(dayCal.getTime(), CupRecordList.QueryInterval.Hour);
                for (int i = 0; i < records.length; i++) {
                    /**
                     * 这里为了让折线图显示明显，对50-80范围内的数据进行矫正，提高在折线图上的高度
                     */
                    if (records[i].Temperature_MAX >= 50 && records[i].Temperature_MAX <= 80) {
                        dataDay[records[i].start.getHours()] = records[i].Temperature_MAX + 20;
                    } else {
                        dataDay[records[i].start.getHours()] = records[i].Temperature_MAX;
                    }
                }

                Log.e(TAG, "initDayData: good:" + dayCool + " ,soso:" + dayMid + " , bad:" + dayHot + " , count:" + dayTotal);
            }
        } catch (Exception ex) {
            Log.e(TAG, "initDayData_Ex: " + ex.getMessage());
        }
    }

    /**
     * 初始化周数据
     */
    private void initWeekData() {
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
                        if (records[i].Temperature_MAX >= 50 && records[i].Temperature_MAX <= 80) {
                            dataWeek[records[i].start.getDay() - 1] = records[i].Temperature_MAX + 20;
                        } else {
                            dataWeek[records[i].start.getDay() - 1] = records[i].Temperature_MAX;
                        }
                    } else {
                        if (records[i].Temperature_MAX >= 50 && records[i].Temperature_MAX <= 80) {
                            dataWeek[6] = records[i].Temperature_MAX + 20;
                        } else {
                            dataWeek[6] = records[i].Temperature_MAX;
                        }
                    }

                    weekCool += records[i].Temperature_Low;
                    weekMid += records[i].Temperature_Mid;
                    weekHot += records[i].Temperature_High;
                    weekTotal += records[i].Count;
                }

                Log.e(TAG, "initWeekData: good:" + weekCool + " ,soso:" + weekMid + " , bad:" + weekHot + " , count:" + weekTotal);
            }
        } catch (Exception ex) {
            Log.e(TAG, "initWeekData_Ex: " + ex.getMessage());
        }
    }

    /**
     * 初始化月数据
     */
    private void initMonthData() {
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
                    if (records[i].Temperature_MAX >= 50 && records[i].Temperature_MAX <= 80) {
                        dataMonth[records[i].start.getDate() - 1] = records[i].Temperature_MAX + 20;
                    } else {
                        dataMonth[records[i].start.getDate() - 1] = records[i].Temperature_MAX;
                    }
                    monthCool += records[i].Temperature_Low;
                    monthMid += records[i].Temperature_Mid;
                    monthHot += records[i].Temperature_High;
                    monthTotal += records[i].Count;
                }

                Log.e(TAG, "initMonthData: good:" + monthCool + " ,soso:" + monthMid + " , bad:" + monthHot + " , count:" + monthTotal);

            }
        } catch (Exception ex) {
            Log.e(TAG, "initMonthData_Ex: " + ex.getMessage());
        }
    }


    /**
     * 刷新水温状态
     */
    private void refreshTempState() {
        if (mCup != null && mCup.connectStatus() == BaseDeviceIO.ConnectStatus.Connected) {
            int temp = mCup.Sensor().TemperatureFix;
            if (temp > 0 && temp <= CupRecord.Temperature_Low_Value) {
                tvTempState.setText(R.string.temp_cool);
                ivTempTipIcon.setImageResource(R.drawable.face_soso);
                tvTempTip.setText(R.string.temp_cool_tips);
            } else if (temp > CupRecord.Temperature_Low_Value && temp <= CupRecord.Temperature_High_Value) {
                tvTempState.setText(R.string.temp_moderation);
                ivTempTipIcon.setImageResource(R.drawable.face_good);
                tvTempTip.setText(R.string.temp_mid_tips);
            } else if (temp > CupRecord.Temperature_High_Value) {
                tvTempState.setText(R.string.temp_hot);
                ivTempTipIcon.setImageResource(R.drawable.face_bad);
                tvTempTip.setText(R.string.temp_hot_tips);
            }
        } else {
            tvTempState.setText(R.string.state_null);
        }
    }

    /**
     * 显示圆形图表
     */
    private void showDayProgress() {
        tvChartTitle.setText(R.string.temp_exp);
        llayTdsChart.setVisibility(View.GONE);
        llayWaterDetail.setVisibility(View.VISIBLE);
        if (dayTotal > 0) {
            int hotPre = dayHot * 100 / dayTotal;
            int midPre = dayMid * 100 / dayTotal;

            uixWaterDetailProgress.set_good_progress(100 - hotPre - midPre);
            uixWaterDetailProgress.set_normal_progress(midPre);
            uixWaterDetailProgress.set_bad_progress(hotPre);
            uixWaterDetailProgress.startAnimation();
        }
    }

    /**
     * 显示圆形图表
     */
    private void showWeekProgress() {
        tvChartTitle.setText(R.string.temp_exp);
        llayTdsChart.setVisibility(View.GONE);
        llayWaterDetail.setVisibility(View.VISIBLE);
        if (weekTotal > 0) {
            int hotPre = weekHot * 100 / weekTotal;
            int midPre = weekMid * 100 / weekTotal;

            uixWaterDetailProgress.set_good_progress(100 - hotPre - midPre);
            uixWaterDetailProgress.set_normal_progress(midPre);
            uixWaterDetailProgress.set_bad_progress(hotPre);
            uixWaterDetailProgress.startAnimation();
        }
    }

    /**
     * 显示圆形图表
     */
    private void showMonthProgress() {
        tvChartTitle.setText(R.string.temp_exp);
        llayTdsChart.setVisibility(View.GONE);
        llayWaterDetail.setVisibility(View.VISIBLE);
        if (monthTotal > 0) {
            int hotPre = monthHot * 100 / monthTotal;
            int midPre = monthMid * 100 / monthTotal;

            uixWaterDetailProgress.set_good_progress(100 - hotPre - midPre);
            uixWaterDetailProgress.set_normal_progress(midPre);
            uixWaterDetailProgress.set_bad_progress(hotPre);
            uixWaterDetailProgress.startAnimation();
        }
    }

    /**
     * 显示折线图表
     */
    private void showDayChart() {
        tvChartTitle.setText(R.string.day_temp_exp);
        llayWaterDetail.setVisibility(View.GONE);
        llayTdsChart.setVisibility(View.VISIBLE);
        tdsChartView.setAdapter(adapterDay);
        if (dayTotal > 0) {
            int hotPre = dayHot * 100 / dayTotal;
            int midPre = dayMid * 100 / dayTotal;
            tvTapBadPre.setText(String.format(getString(R.string.temp_hot) + "(%d%%)", hotPre));
            tvTapGenericPre.setText(String.format(getString(R.string.temp_moderation) + "(%d%%)", midPre));
            tvTapHealthPre.setText(String.format(getString(R.string.temp_cool) + "(%d%%)", 100 - midPre - hotPre));
        } else {
            tvTapBadPre.setText(R.string.temp_hot);
            tvTapGenericPre.setText(R.string.temp_moderation);
            tvTapHealthPre.setText(R.string.temp_cool);
        }
    }

    /**
     * 显示折线图表
     */
    private void showWeekChart() {
        tvChartTitle.setText(R.string.week_temp_exp);
        llayWaterDetail.setVisibility(View.GONE);
        llayTdsChart.setVisibility(View.VISIBLE);
        tdsChartView.setAdapter(adapterWeek);
        if (weekTotal > 0) {
            int hotPre = weekHot * 100 / weekTotal;
            int midPre = weekMid * 100 / weekTotal;
            tvTapBadPre.setText(String.format(getString(R.string.temp_hot) + "(%d%%)", hotPre));
            tvTapGenericPre.setText(String.format(getString(R.string.temp_moderation) + "(%d%%)", midPre));
            tvTapHealthPre.setText(String.format(getString(R.string.temp_cool) + "(%d%%)", 100 - midPre - hotPre));
        } else {
            tvTapBadPre.setText(R.string.temp_hot);
            tvTapGenericPre.setText(R.string.temp_moderation);
            tvTapHealthPre.setText(R.string.temp_cool);
        }
    }

    /**
     * 显示折线图表
     */
    private void showMonthChart() {
        tvChartTitle.setText(R.string.month_temp_exp);
        llayWaterDetail.setVisibility(View.GONE);
        llayTdsChart.setVisibility(View.VISIBLE);
        tdsChartView.setAdapter(adapterMonth);
        if (monthTotal > 0) {
            int hotPre = monthHot * 100 / monthTotal;
            int midPre = monthMid * 100 / monthTotal;
            tvTapBadPre.setText(String.format(getString(R.string.temp_hot) + "(%d%%)", hotPre));
            tvTapGenericPre.setText(String.format(getString(R.string.temp_moderation) + "(%d%%)", midPre));
            tvTapHealthPre.setText(String.format(getString(R.string.temp_cool) + "(%d%%)", 100 - midPre - hotPre));
        } else {
            tvTapBadPre.setText(R.string.temp_hot);
            tvTapGenericPre.setText(R.string.temp_moderation);
            tvTapHealthPre.setText(R.string.temp_cool);
        }
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_day:
                chartIndex = 0;
                if (isProgress) {
                    showDayProgress();
                } else {
                    showDayChart();
                }
                break;
            case R.id.rb_week:
                chartIndex = 1;
                if (isProgress) {
                    showWeekProgress();
                } else {
                    showWeekChart();
                }
                break;
            case R.id.rb_month:
                chartIndex = 2;
                if (isProgress) {
                    showMonthProgress();
                } else {
                    showMonthChart();
                }
                break;
        }
    }

    @OnClick({R.id.iv_left_btn, R.id.uixWaterDetailProgress, R.id.iv_right_btn,
            R.id.tdsChartView, R.id.tv_water_know, R.id.tv_buy_water_purifier,R.id.tv_chat_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_left_btn:
                if (chartIndex > 0) {
                    chartIndex--;
                } else {
                    chartIndex = 2;
                }
                checkSwitch(chartIndex);
                break;
            case R.id.uixWaterDetailProgress:
                isProgress = false;
                switchChartView(chartIndex);
                break;
            case R.id.iv_right_btn:
                if (chartIndex < 2) {
                    chartIndex++;
                } else {
                    chartIndex = 0;
                }
                checkSwitch(chartIndex);
                break;
            case R.id.tdsChartView:
                isProgress = true;
                switchProgressView(chartIndex);
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
            case R.id.tv_chat_btn:
                sendBroadcast(new Intent(OznerBroadcastAction.OBA_SWITCH_CHAT));
                this.finish();
                break;
        }
    }

    /**
     * 切换图表
     *
     * @param index
     */
    private void checkSwitch(int index) {
        switch (index) {
            case 0:
                rgSwitch.check(R.id.rb_day);
                break;
            case 1:
                rgSwitch.check(R.id.rb_week);
                break;
            case 2:
                rgSwitch.check(R.id.rb_month);
                break;
            default:
                rgSwitch.check(R.id.rb_day);
                break;
        }
    }


    /**
     * 切换百分比日周月数据
     *
     * @param index
     */
    private void switchProgressView(int index) {
        switch (index) {
            case 0:
                showDayProgress();
                break;
            case 1:
                showWeekProgress();
                break;
            case 2:
                showMonthProgress();
                break;
        }
    }

    /**
     * 切换图表日周月数据
     *
     * @param index
     */
    private void switchChartView(int index) {
        switch (index) {
            case 0:
                showDayChart();
                break;
            case 1:
                showWeekChart();
                break;
            case 2:
                showMonthChart();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(mMonitor);
        super.onDestroy();
    }

    class CupMonitor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshTempState();
        }
    }
}
