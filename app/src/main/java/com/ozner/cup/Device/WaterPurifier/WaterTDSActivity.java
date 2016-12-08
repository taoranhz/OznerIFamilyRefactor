package com.ozner.cup.Device.WaterPurifier;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ozner.WaterPurifier.WaterPurifier;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Base.WebActivity;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Bean.OznerBroadcastAction;
import com.ozner.cup.CupRecord;
import com.ozner.cup.Device.RankType;
import com.ozner.cup.Device.TDSSensorManager;
import com.ozner.cup.R;
import com.ozner.cup.UIView.UIZPurifierExpView;
import com.ozner.device.OznerDeviceManager;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.ozner.cup.R.id.tv_spec;

public class WaterTDSActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "WaterTDSActivity";
    private static final int TextSize = 23;
    private static final int NumSize = 40;


    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(tv_spec)
    TextView tvSpec;
    @InjectView(R.id.tv_preValue)
    TextView tvPreValue;
    @InjectView(R.id.tv_afterValue)
    TextView tvAfterValue;
    @InjectView(R.id.friend_rank)
    TextView friendRank;
    @InjectView(R.id.tv_tdsRankValue)
    TextView tvTdsRankValue;
    @InjectView(R.id.iv_tds_tip_icon)
    ImageView ivTdsTipIcon;
    @InjectView(R.id.tv_tds_tip)
    TextView tvTdsTip;
    @InjectView(R.id.rb_week)
    RadioButton rbWeek;
    @InjectView(R.id.rb_month)
    RadioButton rbMonth;
    @InjectView(R.id.uiz_purifierExp)
    UIZPurifierExpView uizPurifierExp;
    @InjectView(R.id.tv_water_know)
    TextView tvWaterKnow;
    @InjectView(R.id.tv_buy_water_purifier)
    TextView tvBuyWaterPurifier;
    @InjectView(R.id.rg_switch)
    RadioGroup rgSwitch;

    Calendar curCal;
    private String mac = "";
    private WaterPurifier mWaterPurifier;
    private int oldPreValue, oldThenValue;
    int[] mon_predata, mon_afterdata, week_predata, week_afterdata;
    private TDSSensorManager tdsSensorManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_tds);
        ButterKnife.inject(this);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.cup_detail_bg));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.cup_detail_bg));
        }
        initToolBar();
        initDefaultData();
        rgSwitch.setOnCheckedChangeListener(this);

        try {
            mac = getIntent().getStringExtra(Contacts.PARMS_MAC);
            Log.e(TAG, "onCreate: mac:" + mac);
            mWaterPurifier = (WaterPurifier) OznerDeviceManager.Instance().getDevice(mac);
            refreshUIData();
            initWeekBtnChecked();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "onCreate_Ex: " + ex.getMessage());
        }
        loadTdsFriendRank();
    }

    /**
     * 获取朋友圈tds排名
     */
    private void loadTdsFriendRank() {
        tdsSensorManager = new TDSSensorManager(this);
        tdsSensorManager.getTdsFriendRank(RankType.WaterType, new TDSSensorManager.TDSListener() {
            @Override
            public void onSuccess(int result) {
                tvTdsRankValue.setText(String.valueOf(result));
            }

            @Override
            public void onFail(String msg) {
                Log.e(TAG, "getTdsFriendRank_onFail: " + msg);
            }
        });
    }

    /**
     * 初始化数组
     */
    private void initDefaultData() {
        curCal = Calendar.getInstance();
        mon_predata = new int[31];
        mon_afterdata = new int[31];
        week_predata = new int[7];
        week_afterdata = new int[7];
    }

    /**
     * 初始化周数据默认选中
     */
    private void initWeekBtnChecked() {
        try {
            rbWeek.setChecked(true);
            rbMonth.setChecked(false);
            refreshWeekData();
            refreshMonthData();
            uizPurifierExp.setWeekData(week_predata, week_afterdata);
        } catch (Exception ex) {
            Log.e(TAG, "initWeekBtnChecked_Ex: " + ex.getMessage());
        }
    }

    /**
     * 初始化ToolBar
     */
    private void initToolBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        title.setText(R.string.tdsTitle);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.cup_detail_bg));
        toolbar.setNavigationIcon(R.drawable.back);
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
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        try {
            switch (checkedId) {
                case R.id.rb_week:
                    uizPurifierExp.setWeekData(week_predata, week_afterdata);
                    break;
                case R.id.rb_month:
                    uizPurifierExp.setMonthData(mon_predata, mon_afterdata);
                    break;
            }
        } catch (Exception ex) {
            Log.e(TAG, "onCheckedChanged_Ex: " + ex.getMessage());
        }
    }


    /**
     * 刷新UI数据
     */
    private void refreshUIData() {
        refreshTdsState();
    }

    /**
     * 刷新化周数据
     */
    private void refreshWeekData() {

        //本地数据，需要从网络获取数据
        for (int i = 0; i < week_predata.length; i++) {
            int curDayOfWeek = curCal.get(Calendar.DAY_OF_WEEK);
            if (curDayOfWeek == 1) {
                if (mWaterPurifier.sensor().TDS1() > mWaterPurifier.sensor().TDS2()) {
                    week_predata[6] = mWaterPurifier.sensor().TDS1();
                    week_afterdata[6] = mWaterPurifier.sensor().TDS2();
                } else {
                    week_predata[6] = mWaterPurifier.sensor().TDS2();
                    week_afterdata[6] = mWaterPurifier.sensor().TDS1();
                }
            } else if ((curDayOfWeek - 2) == i) {
                if (mWaterPurifier.sensor().TDS1() > mWaterPurifier.sensor().TDS2()) {
                    week_predata[i] = mWaterPurifier.sensor().TDS1();
                    week_afterdata[i] = mWaterPurifier.sensor().TDS2();
                } else {
                    week_predata[i] = mWaterPurifier.sensor().TDS2();
                    week_afterdata[i] = mWaterPurifier.sensor().TDS1();
                }
            }
        }
    }

    /**
     * 刷新化月数据
     */
    private void refreshMonthData() {

        //本地数据，需要从网络获取数据
        for (int i = 0; i < mon_afterdata.length; i++) {
            int curDayOfMonth = curCal.get(Calendar.DAY_OF_MONTH);
            if (i == curDayOfMonth - 1) {
                if (mWaterPurifier.sensor().TDS1() > mWaterPurifier.sensor().TDS2()) {
                    mon_predata[i] = mWaterPurifier.sensor().TDS1();
                    mon_afterdata[i] = mWaterPurifier.sensor().TDS2();
                } else {
                    mon_predata[i] = mWaterPurifier.sensor().TDS2();
                    mon_afterdata[i] = mWaterPurifier.sensor().TDS1();
                }
            }
        }
    }


    /**
     * 显示TDS值
     */
    private void refreshTdsState() {
        if (mWaterPurifier != null) {
            int tdsPre, tdsThen;
            //获取净化前后的TDS值
            if (mWaterPurifier.sensor().TDS1() > 0 && mWaterPurifier.sensor().TDS2() > 0
                    && mWaterPurifier.sensor().TDS1() != 65535 && mWaterPurifier.sensor().TDS2() != 65535) {
                //TDS值比较大的作为净化前的值
                if (mWaterPurifier.sensor().TDS1() > mWaterPurifier.sensor().TDS2()) {
                    tdsPre = mWaterPurifier.sensor().TDS1();
                    tdsThen = mWaterPurifier.sensor().TDS2();
                } else {
                    tdsPre = mWaterPurifier.sensor().TDS2();
                    tdsThen = mWaterPurifier.sensor().TDS1();
                }
            } else {
                //有任何一个不大于0或者有任何一个为65535，就全部置为0
                tdsPre = tdsThen = 0;
            }

            //只有当数据和上次不一样时才更新刷新
            if (oldPreValue != tdsPre || oldThenValue != tdsThen) {

                oldPreValue = tdsPre;
                oldThenValue = tdsThen;
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(tvSpec.getLayoutParams());
                //净化前后的值都不为0，并且都不为65535
                if (tdsPre != 0) {
                    tvPreValue.setText(String.valueOf(tdsPre));
                    tvAfterValue.setText(String.valueOf(tdsThen));
                    tvPreValue.setTextSize(NumSize);
                    tvAfterValue.setTextSize(NumSize);

                    lp.topMargin = dip2px(this, 10);
                    tvSpec.setLayoutParams(lp);
                } else {
                    tvPreValue.setText(R.string.state_null);
                    tvAfterValue.setText(R.string.state_null);
                    tvPreValue.setTextSize(TextSize);
                    tvAfterValue.setTextSize(TextSize);
                    lp.topMargin = dip2px(this, 0);
                    tvSpec.setLayoutParams(lp);
                }

                showTdsStateTips(tdsThen);
            }
        }
    }

    /**
     * 根据净化后的tds显示状态
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

    @OnClick({R.id.tv_water_know, R.id.tv_buy_water_purifier, R.id.tv_chat_btn})
    public void onClick(View view) {
        switch (view.getId()) {
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
}
