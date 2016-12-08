package com.ozner.cup.Device.Cup;

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

import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Base.WebActivity;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Bean.OznerBroadcastAction;
import com.ozner.cup.Cup;
import com.ozner.cup.CupRecord;
import com.ozner.cup.CupRecordList;
import com.ozner.cup.R;
import com.ozner.cup.UIView.ChartAdapter;
import com.ozner.cup.UIView.TDSChartView;
import com.ozner.cup.UIView.UIXWaterDetailProgress;
import com.ozner.device.OznerDeviceManager;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class CupTDSActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "CupTDSActivity";
    private static final int TextSize = 40;
    private static final int NumSize = 45;

    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.tv_tdsValue)
    TextView tvTdsValue;
    @InjectView(R.id.tv_tdsRankValue)
    TextView tvTdsRankValue;
    @InjectView(R.id.tv_chat_btn)
    TextView tvChatBtn;
    @InjectView(R.id.iv_tds_tip_icon)
    ImageView ivTdsTipIcon;
    @InjectView(R.id.tv_tds_tip)
    TextView tvTdsTip;
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
    private int dayGood, daySoSo, dayBad, dayCount;
    private int weekGood, weekSoSo, weekBad, weekCount;
    private int monthGood, monthSoSo, monthBad, monthCount;
    private boolean isProgress = true;
    private int chartIndex = 0;//日图表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cup_tds);
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


    /**
     * 初始化ToolBar
     */
    private void initToolBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        title.setText(R.string.tdsTitle);
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
                return 200;
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
                return 200;
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
                return 200;
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
        refreshTdsState();

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
                dayGood = dayRecord.TDS_Good;
                daySoSo = dayRecord.TDS_Mid;
                dayBad = dayRecord.TDS_Bad;
                dayCount = dayRecord.Count;
                Log.e(TAG, "initDayData: good:" + dayGood + " ,soso:" + daySoSo + " , bad:" + dayBad + " , count:" + dayCount);

                CupRecord[] records = mCup.Volume().getRecordByDate(dayCal.getTime(), CupRecordList.QueryInterval.Hour);
                for (int i = 0; i < records.length; i++) {
                    dataDay[records[i].start.getHours()] = records[i].TDS_High;
                }
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
                        dataWeek[records[i].start.getDay() - 1] = records[i].TDS_High;
                    } else {
                        dataWeek[6] = records[i].TDS_High;
                    }

                    weekGood += records[i].TDS_Good;
                    weekSoSo += records[i].TDS_Mid;
                    weekBad += records[i].TDS_Bad;
                    weekCount += records[i].Count;
                }
                Log.e(TAG, "initWeekData: good:" + weekGood + " ,soso:" + weekSoSo + " , bad:" + weekBad + " , count:" + weekCount);

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
                    dataMonth[records[i].start.getDate() - 1] = records[i].TDS_High;
                    monthGood += records[i].TDS_Good;
                    monthSoSo += records[i].TDS_Mid;
                    monthBad += records[i].TDS_Bad;
                    monthCount += records[i].Count;
                }

                Log.e(TAG, "initMonthData: good:" + monthGood + " ,soso:" + monthSoSo + " , bad:" + monthBad + " , count:" + monthCount);

            }
        } catch (Exception ex) {
            Log.e(TAG, "initMonthData_Ex: " + ex.getMessage());
        }
    }


    /**
     * 显示圆形图表
     */
    private void showDayProgress() {
        tvChartTitle.setText(R.string.tds_exp);
        llayTdsChart.setVisibility(View.GONE);
        llayWaterDetail.setVisibility(View.VISIBLE);
        if (dayCount > 0) {
            int badPre = dayBad * 100 / dayCount;
            int sosoPre = daySoSo * 100 / dayCount;

            uixWaterDetailProgress.set_good_progress(100 - badPre - sosoPre);
            uixWaterDetailProgress.set_normal_progress(sosoPre);
            uixWaterDetailProgress.set_bad_progress(badPre);
            uixWaterDetailProgress.startAnimation();
        }
    }

    /**
     * 显示圆形图表
     */
    private void showWeekProgress() {
        tvChartTitle.setText(R.string.tds_exp);
        llayTdsChart.setVisibility(View.GONE);
        llayWaterDetail.setVisibility(View.VISIBLE);
        if (weekCount > 0) {
            int badPre = weekBad * 100 / weekCount;
            int sosoPre = weekSoSo * 100 / weekCount;

            uixWaterDetailProgress.set_good_progress(100 - badPre - sosoPre);
            uixWaterDetailProgress.set_normal_progress(sosoPre);
            uixWaterDetailProgress.set_bad_progress(badPre);
            uixWaterDetailProgress.startAnimation();
        }
    }

    /**
     * 显示圆形图表
     */
    private void showMonthProgress() {
        tvChartTitle.setText(R.string.tds_exp);
        llayTdsChart.setVisibility(View.GONE);
        llayWaterDetail.setVisibility(View.VISIBLE);
        if (monthCount > 0) {
            int badPre = monthBad * 100 / monthCount;
            int sosoPre = monthSoSo * 100 / monthCount;

            uixWaterDetailProgress.set_good_progress(100 - badPre - sosoPre);
            uixWaterDetailProgress.set_normal_progress(sosoPre);
            uixWaterDetailProgress.set_bad_progress(badPre);
            uixWaterDetailProgress.startAnimation();
        }
    }

    /**
     * 显示折线图表
     */
    private void showDayChart() {
        tvChartTitle.setText(R.string.day_tds_exp);
        llayWaterDetail.setVisibility(View.GONE);
        llayTdsChart.setVisibility(View.VISIBLE);
        tdsChartView.setAdapter(adapterDay);
        if (dayCount > 0) {
            int badPre = dayBad * 100 / dayCount;
            int sosoPre = daySoSo * 100 / dayCount;
            tvTapBadPre.setText(String.format(getString(R.string.bad) + "(%d%%)", badPre));
            tvTapGenericPre.setText(String.format(getString(R.string.soso) + "(%d%%)", sosoPre));
            tvTapHealthPre.setText(String.format(getString(R.string.health) + "(%d%%)", 100 - badPre - sosoPre));
        } else {
            tvTapBadPre.setText(R.string.bad);
            tvTapGenericPre.setText(R.string.soso);
            tvTapHealthPre.setText(R.string.health);
        }
    }

    /**
     * 显示折线图表
     */
    private void showWeekChart() {
        tvChartTitle.setText(R.string.week_tds_exp);
        llayWaterDetail.setVisibility(View.GONE);
        llayTdsChart.setVisibility(View.VISIBLE);
        tdsChartView.setAdapter(adapterWeek);
        if (weekCount > 0) {
            int badPre = weekBad * 100 / weekCount;
            int sosoPre = weekSoSo * 100 / weekCount;
            tvTapBadPre.setText(String.format(getString(R.string.bad) + "(%d%%)", badPre));
            tvTapGenericPre.setText(String.format(getString(R.string.soso) + "(%d%%)", sosoPre));
            tvTapHealthPre.setText(String.format(getString(R.string.health) + "(%d%%)", 100 - badPre - sosoPre));
        } else {
            tvTapBadPre.setText(R.string.bad);
            tvTapGenericPre.setText(R.string.soso);
            tvTapHealthPre.setText(R.string.health);
        }
    }

    /**
     * 显示折线图表
     */
    private void showMonthChart() {
        tvChartTitle.setText(R.string.month_tds_exp);
        llayWaterDetail.setVisibility(View.GONE);
        llayTdsChart.setVisibility(View.VISIBLE);
        tdsChartView.setAdapter(adapterMonth);
        if (monthCount > 0) {
            int badPre = monthBad * 100 / monthCount;
            int sosoPre = monthSoSo * 100 / monthCount;
            tvTapBadPre.setText(String.format(getString(R.string.bad) + "(%d%%)", badPre));
            tvTapGenericPre.setText(String.format(getString(R.string.soso) + "(%d%%)", sosoPre));
            tvTapHealthPre.setText(String.format(getString(R.string.health) + "(%d%%)", 100 - badPre - sosoPre));
        } else {
            tvTapBadPre.setText(R.string.bad);
            tvTapGenericPre.setText(R.string.soso);
            tvTapHealthPre.setText(R.string.health);
        }
    }

    /**
     * 刷新Tds值
     */
    private void refreshTdsState() {
        if (mCup != null) {
            int tds = mCup.Sensor().TDSFix;
            if (tds > 0 && tds < 5000) {
                tvTdsValue.setTextSize(NumSize);
                tvTdsValue.setText(String.valueOf(mCup.Sensor().TDSFix));
                showTdsStateTips(mCup.Sensor().TDSFix);
            } else {
                tvTdsValue.setText(R.string.state_null);
                tvTdsValue.setTextSize(TextSize);
            }
        }
    }

    /**
     * 根据tds显示状态
     */
    private void showTdsStateTips(int thenTds) {
        if (thenTds == 0) {
            ivTdsTipIcon.setVisibility(View.GONE);
            tvTdsTip.setText(R.string.tds_good_tips);
        } else if (thenTds > 0 && thenTds <= CupRecord.TDS_Good_Value) {
            ivTdsTipIcon.setImageResource(R.drawable.face_good);
            tvTdsTip.setText(R.string.tds_good_tips);
        } else if (thenTds > CupRecord.TDS_Good_Value && thenTds < CupRecord.TDS_Bad_Value) {
            ivTdsTipIcon.setImageResource(R.drawable.face_soso);
            tvTdsTip.setText(R.string.tds_normal_tips);
        } else if (thenTds > CupRecord.TDS_Bad_Value) {
            ivTdsTipIcon.setImageResource(R.drawable.face_bad);
            tvTdsTip.setText(R.string.tds_bad_tips);
        }
    }

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(mMonitor);
        super.onDestroy();
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


    class CupMonitor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshTdsState();
        }
    }
}
