package com.ozner.cup.Device.ReplenWater;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.kayvannj.permission_utils.Func;
import com.github.kayvannj.permission_utils.PermissionUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Base.WebActivity;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Bean.OznerBroadcastAction;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.OznerDeviceSettings;
import com.ozner.cup.DBHelper.UserInfo;
import com.ozner.cup.HttpHelper.HttpMethods;
import com.ozner.cup.HttpHelper.OznerHttpResult;
import com.ozner.cup.HttpHelper.ProgressSubscriber;
import com.ozner.cup.R;
import com.ozner.cup.UIView.WaterReplMeterView;
import com.ozner.cup.Utils.DateUtils;
import com.ozner.cup.Utils.LCLogUtils;
import com.ozner.cup.Utils.WeChatUrlUtil;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ReplenDetailActivity extends BaseActivity {
    private static final String TAG = "ReplenDetail";
    private static final short CLICK_FACE = 0;
    private static final short CLICK_EYE = 1;
    private static final short CLICK_HAND = 2;
    private static final short CLICK_NECK = 3;

    private static final short CHECK_WEEK = 4;
    private static final short CHECK_MONTH = 5;

    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.iv_face)
    ImageView ivFace;
    @InjectView(R.id.iv_hand)
    ImageView ivHand;
    @InjectView(R.id.iv_eye)
    ImageView ivEye;
    @InjectView(R.id.iv_neck)
    ImageView ivNeck;
    @InjectView(R.id.tv_skin_humidity)
    TextView tvSkinHumidity;
    @InjectView(R.id.tv_skin_state)
    TextView tvSkinState;
    @InjectView(R.id.tv_last_value)
    TextView tvLastValue;
    @InjectView(R.id.tv_skin_average)
    TextView tvSkinAverage;
    @InjectView(R.id.tv_skin_part)
    TextView tvSkinPart;
    @InjectView(R.id.rg_time)
    RadioGroup rgTime;
    @InjectView(R.id.wrm_view)
    WaterReplMeterView wrmView;
    @InjectView(R.id.activity_replen_detail)
    LinearLayout activityReplenDetail;
    @InjectView(R.id.llay_bottom)
    LinearLayout llayBottom;
    @InjectView(R.id.tv_tips)
    TextView tvTips;

    /**
     * 各部位在数组中的位置
     * 脸部：0,眼部：1,手部：2,颈部：3
     */
    private int[][] waterMs = new int[4][31];
    private int[][] oilMs = new int[4][31];
    private int[][] waterWs = new int[4][7];
    private int[][] oilWs = new int[4][7];
    private int[] todayValues = new int[4];
    private float[] totalValues = new float[4];
    private int[] totalCounts = new int[4];
    //皮肤类型范围值，midValue,highValue同位为一组
    private int[] midValue = new int[]{32, 35, 30, 35};
    private int[] highValue = new int[]{42, 45, 38, 45};

    private short curPart = CLICK_FACE;
    private short curCheck = CHECK_WEEK;
    private OznerDeviceSettings oznerSetting;
    private PermissionUtil.PermissionRequestObject perReqResult;
    private String mac;
    private String mUserid;
    private String mUserToken;
    private UserInfo mUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replen_detail);
        ButterKnife.inject(this);
        initToolBar();
        if (UserDataPreference.isLoginEmail(this)) {
            llayBottom.setVisibility(View.GONE);
        }
        mUserToken = OznerPreference.getUserToken(this);
        mUserid = OznerPreference.GetValue(this, OznerPreference.UserId, "");
        mUserInfo = DBManager.getInstance(this).getUserInfo(mUserid);
        rgTime.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_week:
                        curCheck = CHECK_WEEK;
                        break;
                    case R.id.rb_month:
                        curCheck = CHECK_MONTH;
                        break;
                }
                showHistoryData(curPart, curCheck);
            }
        });
        try {
            mac = getIntent().getStringExtra(Contacts.PARMS_MAC);
            curPart = (short) getIntent().getIntExtra(Contacts.PARMS_CLICK_POS, CLICK_FACE);
            oznerSetting = DBManager.getInstance(this).getDeviceSettings(mUserid, mac);
            loadBuShuiFenbu();
        } catch (Exception ex) {
            LCLogUtils.E(TAG, "onCreate_Ex:" + ex.getMessage());
        }
        switchClick(curPart);
    }

    /**
     * 初始化ToolBar
     */
    private void initToolBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        title.setText(R.string.replen_detail_title);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.cup_detail_bg));
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.cup_detail_bg));
            //更改底部导航栏颜色(限有底部的手机)
//            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.colorAccent));
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

    @OnClick({R.id.llay_face, R.id.llay_hand, R.id.llay_eye, R.id.llay_neck,
            R.id.tv_skin_oil, R.id.tv_skin_water, R.id.tv_chat_btn, R.id.tv_btn_buy})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.llay_face:
                if (CLICK_FACE != curPart)
                    switchClick(CLICK_FACE);
                break;
            case R.id.llay_hand:
                if (CLICK_HAND != curPart)
                    switchClick(CLICK_HAND);
                break;
            case R.id.llay_eye:
                if (CLICK_EYE != curPart)
                    switchClick(CLICK_EYE);
                break;
            case R.id.llay_neck:
                if (CLICK_NECK != curPart)
                    switchClick(CLICK_NECK);
                break;

            case R.id.tv_skin_oil:
                startActivity(new Intent(this, ReplenOilIntroduceActivity.class));
                break;
            case R.id.tv_skin_water:
                startActivity(new Intent(this, ReplenWaterIntroduceActivity.class));
                break;
            case R.id.tv_chat_btn:
                sendBroadcast(new Intent(OznerBroadcastAction.OBA_SWITCH_CHAT));
                this.finish();
//                takePhone();
                break;
            case R.id.tv_btn_buy:
                if (!UserDataPreference.isLoginEmail(this)) {
                    if (mUserInfo != null) {
                        Intent shopIntent = new Intent(this, WebActivity.class);
                        String shopUrl = WeChatUrlUtil.formatUrl(Contacts.buyReplenWaterUrl, mUserInfo.getMobile(), mUserToken, "zh", "zh");
                        shopIntent.putExtra(Contacts.PARMS_URL, shopUrl);
                        startActivity(shopIntent);
                    } else {
                        showToastCenter(R.string.userinfo_miss);
                    }
                }
                break;
        }
    }

    /**
     * 拨打客服电话
     */
    private void takePhone() {
        perReqResult = PermissionUtil.with(this).request(Manifest.permission.CALL_PHONE)
                .onAllGranted(new Func() {
                    @Override
                    protected void call() {
                        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:4008209667"));
                        if (getPackageManager().checkPermission(Manifest.permission.CALL_PHONE, getPackageName()) == PackageManager.PERMISSION_GRANTED)
                            startActivity(callIntent);
                    }
                }).onAnyDenied(new Func() {
                    @Override
                    protected void call() {
                        showToastCenter(R.string.permission_call_phone_denied);
                    }
                }).ask(1);
    }

    /**
     * 切换点击位置
     *
     * @param parts
     */
    private void switchClick(short parts) {
        curPart = parts;
        double lastValue = 0;
        switch (parts) {
            case CLICK_FACE:
                ivFace.setSelected(true);
                ivHand.setSelected(false);
                ivEye.setSelected(false);
                ivNeck.setSelected(false);
                tvSkinPart.setText(R.string.part_face);
                showSkinStatus(CLICK_FACE);
                try {
                    lastValue = Double.parseDouble(String.valueOf(oznerSetting.getAppData(Contacts.DEV_REPLEN_FACE_LAST_MOIS)));
                } catch (Exception ex) {
                    lastValue = 0f;
                }
                break;
            case CLICK_EYE:
                ivFace.setSelected(false);
                ivHand.setSelected(false);
                ivEye.setSelected(true);
                ivNeck.setSelected(false);
                tvSkinPart.setText(R.string.part_eye);
                showSkinStatus(CLICK_EYE);
                try {
                    lastValue = Double.parseDouble(String.valueOf(oznerSetting.getAppData(Contacts.DEV_REPLEN_EYE_LAST_MOIS)));
                } catch (Exception ex) {
                    lastValue = 0f;
                }
                break;
            case CLICK_HAND:
                ivFace.setSelected(false);
                ivHand.setSelected(true);
                ivEye.setSelected(false);
                ivNeck.setSelected(false);
                tvSkinPart.setText(R.string.part_hand);
                showSkinStatus(CLICK_HAND);
                try {
                    lastValue = Double.parseDouble(String.valueOf(oznerSetting.getAppData(Contacts.DEV_REPLEN_HAND_LAST_MOIS)));
                } catch (Exception ex) {
                    lastValue = 0f;
                }
                break;

            case CLICK_NECK:
                ivFace.setSelected(false);
                ivHand.setSelected(false);
                ivEye.setSelected(false);
                ivNeck.setSelected(true);
                tvSkinPart.setText(R.string.part_neck);
                showSkinStatus(CLICK_NECK);
                try {
                    lastValue = Double.parseDouble(String.valueOf(oznerSetting.getAppData(Contacts.DEV_REPLEN_NECK_LAST_MOIS)));
                } catch (Exception ex) {
                    lastValue = 0f;
                }
                break;
        }
//        if (lastValue > 0.01f) {
        tvLastValue.setText(String.format("%.1f%%", lastValue));
//        } else {
//            tvLastValue.setText(R.string.state_null);
//        }
        showHistoryData(curPart, curCheck);
    }

    /**
     * 显示历史数据
     *
     * @param click_part
     * @param check_time
     */
    private void showHistoryData(short click_part, short check_time) {
        LCLogUtils.E(TAG, "clickPart:" + click_part + " , checkTime:" + check_time);
        switch (click_part) {
            case CLICK_FACE:
                if (CHECK_WEEK == check_time) {
                    wrmView.setWeekData(oilWs[CLICK_FACE], waterWs[CLICK_FACE]);
                } else if (CHECK_MONTH == check_time) {
                    wrmView.setMonthData(oilMs[CLICK_FACE], waterMs[CLICK_FACE]);
                }
                showSkinStatus(CLICK_FACE);
                break;
            case CLICK_EYE:
                if (CHECK_WEEK == check_time) {
                    wrmView.setWeekData(oilWs[CLICK_EYE], waterWs[CLICK_EYE]);
                } else if (CHECK_MONTH == check_time) {
                    wrmView.setMonthData(oilMs[CLICK_EYE], waterMs[CLICK_EYE]);
                }
                showSkinStatus(CLICK_EYE);
                break;
            case CLICK_HAND:
                if (CHECK_WEEK == check_time) {
                    wrmView.setWeekData(oilWs[CLICK_HAND], waterWs[CLICK_HAND]);
                } else if (CHECK_MONTH == check_time) {
                    wrmView.setMonthData(oilMs[CLICK_HAND], waterMs[CLICK_HAND]);
                }
                showSkinStatus(CLICK_HAND);
                break;
            case CLICK_NECK:
                if (CHECK_WEEK == check_time) {
                    wrmView.setWeekData(oilWs[CLICK_NECK], waterWs[CLICK_NECK]);
                } else if (CHECK_MONTH == check_time) {
                    wrmView.setMonthData(oilMs[CLICK_NECK], waterMs[CLICK_NECK]);
                }
                showSkinStatus(CLICK_NECK);
                break;
        }
    }

    /**
     * 显示某一部位的数据
     *
     * @param index
     */
    private void showSkinStatus(int index) {

        if (totalCounts[index] > 0) {
            tvSkinAverage.setText(String.format(getString(R.string.replen_average_value), totalValues[index] / totalCounts[index], totalCounts[index]));
            if (todayValues[index] <= 0) {
                todayValues[index] = (int) (totalValues[index] / totalCounts[index]);
            }
        } else {
            try {
                double totalValue = 0;
                int count = 0;
                switch (index) {
                    case CLICK_FACE:
                        totalValue = (double) oznerSetting.getAppData(Contacts.DEV_REPLEN_FACE_MOIS_TOTAL);
                        count = (int) oznerSetting.getAppData(Contacts.DEV_REPLEN_FACE_TEST_COUNT);
                        break;
                    case CLICK_EYE:
                        totalValue = (double) oznerSetting.getAppData(Contacts.DEV_REPLEN_EYE_MOIS_TOTAL);
                        count = (int) oznerSetting.getAppData(Contacts.DEV_REPLEN_EYE_TEST_COUNT);
                        break;
                    case CLICK_HAND:
                        totalValue = (double) oznerSetting.getAppData(Contacts.DEV_REPLEN_HAND_MOIS_TOTAL);
                        count = (int) oznerSetting.getAppData(Contacts.DEV_REPLEN_HAND_TEST_COUNT);
                        break;
                    case CLICK_NECK:
                        totalValue = (double) oznerSetting.getAppData(Contacts.DEV_REPLEN_NECK_MOIS_TOTAL);
                        count = (int) oznerSetting.getAppData(Contacts.DEV_REPLEN_NECK_TEST_COUNT);
                        break;
                }
                if (count > 0) {
                    if (todayValues[index] <= 0) {
                        todayValues[index] = (int) totalValue / count;
                    }
                    tvSkinAverage.setText(String.format(getString(R.string.replen_average_value), totalValue / count, count));
                } else {
                    tvSkinAverage.setText(String.format(getString(R.string.replen_average_value), 0f, 0));
                }

            } catch (Exception ex) {
                tvSkinAverage.setText(String.format(getString(R.string.replen_average_value), 0f, 0));
            }
        }

        tvSkinHumidity.setText(String.valueOf(todayValues[index]));
        if (todayValues[index] > 0 && todayValues[index] <= midValue[index]) {
            tvSkinState.setText(R.string.dry);
            tvTips.setVisibility(View.VISIBLE);
        } else if (todayValues[index] > midValue[index] && todayValues[index] <= highValue[index]) {
            tvSkinState.setText(R.string.normal);
            tvTips.setVisibility(View.VISIBLE);
        } else if (todayValues[index] > highValue[index]) {
            tvSkinState.setText(R.string.wetness);
            tvTips.setVisibility(View.GONE);
        } else {
            tvSkinState.setText(R.string.state_null);
            tvTips.setVisibility(View.GONE);
        }
    }


    /**
     * 加载补水仪历史检测数据
     */
    private void loadBuShuiFenbu() {
        LCLogUtils.E(TAG, "开始加载历史检测数据");
        HttpMethods.getInstance().getBuShuiFenBu(mUserToken, mac, "",
                new ProgressSubscriber<JsonObject>(this, new OznerHttpResult<JsonObject>() {
                    @Override
                    public void onError(Throwable e) {
                        LCLogUtils.E(TAG, "loadBuShuiFenbu_onError:" + e.getMessage());
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        if (jsonObject != null) {
                            Log.e(TAG, "loadBuShuiFenbu: " + jsonObject.toString());
                            int state = jsonObject.get("state").getAsInt();
                            if (state > 0) {
                                try {
                                    JsonObject dataJson = jsonObject.getAsJsonObject("data");
                                    if (!dataJson.isJsonNull()) {
                                        handleBuShuiFenBuData(dataJson.getAsJsonObject(ReplenFenBuAction.FaceSkinValue), CLICK_FACE);
                                        handleBuShuiFenBuData(dataJson.getAsJsonObject(ReplenFenBuAction.EyesSkinValue), CLICK_EYE);
                                        handleBuShuiFenBuData(dataJson.getAsJsonObject(ReplenFenBuAction.HandSkinValue), CLICK_HAND);
                                        handleBuShuiFenBuData(dataJson.getAsJsonObject(ReplenFenBuAction.NeckSkinValue), CLICK_NECK);
                                    }
                                    //显示获取到的数据
                                    if (!isDestroyed()) {
                                        showHistoryData(curPart, curCheck);
                                    }
                                } catch (Exception ex) {
                                    LCLogUtils.E(TAG, "loadBuShuiFenbu_Ex:" + ex.getMessage());
                                }
                            } else {
                                if (state == -10006 || state == -10007) {
                                    BaseActivity.reLogin(ReplenDetailActivity.this);
                                }
                            }
                        } else {
                            LCLogUtils.E(TAG, "结果为空");
                        }
                    }
                }));
    }

    /**
     * 处理补水分布接口返回的数据
     *
     * @param dataObject 不同部位的数据对象
     * @param index      检测部位序号
     *                   脸部：0
     *                   眼部：1
     *                   手部：2
     *                   颈部：3
     */
    private void handleBuShuiFenBuData(JsonObject dataObject, int index) {
        if (index > 3 || index < 0)
            return;
        LCLogUtils.E(TAG, "index:" + index);
        try {
            Calendar todayCal = Calendar.getInstance();
            Calendar tempCal = Calendar.getInstance();
            //脸部数据
//            JsonObject dataObject = dataJson.getAsJsonObject(action);
            //周数据
            JsonArray weekArray = dataObject.getAsJsonArray("week");
            if (!weekArray.isJsonNull() && weekArray.isJsonArray() && weekArray.size() > 0) {
                int weekSize = weekArray.size();
                for (int i = 0; i < weekSize; i++) {
                    JsonObject itemObject = weekArray.get(i).getAsJsonObject();
                    tempCal.setTimeInMillis(DateUtils.formatDateFromString(itemObject.get("updatetime").getAsString()));
                    //获取今日水分值
                    if (todayCal.getTime().equals(tempCal.getTime())) {
                        String snumber = itemObject.get("snumber").getAsString();
                        todayValues[index] = Integer.parseInt(snumber.substring(0, snumber.indexOf(".")));
                    }
                    int weekday = tempCal.get(Calendar.DAY_OF_WEEK);
                    //DAY_OF_WEEK 获取的值是从周日（1）开始，需要调整为周一（0）开始
                    if (weekday == 0) {
                        waterWs[index][weekday + 5] = (int) (itemObject.get("snumber").getAsFloat());
                        oilWs[index][weekday + 5] = (int) (itemObject.get("ynumber").getAsFloat());
                    } else {
                        waterWs[index][weekday - 2] = (int) (itemObject.get("snumber").getAsFloat());
                        oilWs[index][weekday - 2] = (int) (itemObject.get("ynumber").getAsFloat());
                    }
                }
            }
            //月数据
            JsonArray monthArray = dataObject.getAsJsonArray("monty");
            if (!monthArray.isJsonNull() && monthArray.isJsonArray() && monthArray.size() > 0) {
                int monthSize = monthArray.size();
                for (int i = 0; i < monthSize; i++) {
                    JsonObject itemObject = monthArray.get(i).getAsJsonObject();
                    tempCal.setTimeInMillis(DateUtils.formatDateFromString(itemObject.get("updatetime").getAsString()));
                    waterMs[index][tempCal.get(Calendar.DAY_OF_MONTH) - 1] = (int) (itemObject.get("snumber").getAsFloat() + 0.5f);
                    oilMs[index][tempCal.get(Calendar.DAY_OF_MONTH) - 1] = (int) (itemObject.get("ynumber").getAsFloat() + 0.5f);
                    int times = itemObject.get("times").getAsInt();
                    totalValues[index] += times * (itemObject.get("snumber").getAsFloat());
                    totalCounts[index] += times;
                }
            }
        } catch (Exception ex) {
            LCLogUtils.E(TAG, "handleFenBuData_Ex:" + index + " ,err:" + ex.getMessage());
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (perReqResult != null) {
            perReqResult.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
