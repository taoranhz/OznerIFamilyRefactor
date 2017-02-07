package com.ozner.cup.Device.ReplenWater;

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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.OznerDeviceSettings;
import com.ozner.cup.HttpHelper.HttpMethods;
import com.ozner.cup.HttpHelper.ProgressSubscriber;
import com.ozner.cup.R;
import com.ozner.cup.Utils.LCLogUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.functions.Action1;

public class ReplenQueryActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "ReplenQuery";
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.tv_skin_type)
    TextView tvSkinType;
    @InjectView(R.id.tv_never_test)
    TextView tvNeverTest;
    @InjectView(R.id.iv_skin_null)
    ImageView ivSkinNull;
    @InjectView(R.id.tv_skin_type_tips)
    TextView tvSkinTypeTips;
    @InjectView(R.id.tv_test_time)
    TextView tvTestTime;
    @InjectView(R.id.rg_skin_status)
    RadioGroup rgSkinStatus;
    @InjectView(R.id.iv_skin_status)
    ImageView ivSkinStatus;
    @InjectView(R.id.tv_skin_status_notice)
    TextView tvSkinStatusNotice;
    @InjectView(R.id.llay_bottom_btn)
    LinearLayout llayBottomBtn;
    @InjectView(R.id.rb_skin_dry)
    RadioButton rbSkinDry;
    @InjectView(R.id.rb_skin_oil)
    RadioButton rbSkinOil;
    @InjectView(R.id.rb_skin_neture)
    RadioButton rbSkinNeture;

    private String mac;
    private String mUserid;
    private String mUserToken;
    private OznerDeviceSettings oznerSetting;
    private int gender = 0;
    private int countTotal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replen_query);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.replen_blue_bg));
            //更改底部导航栏颜色(限有底部的手机)
//            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.colorAccent));
        }
        ButterKnife.inject(this);
        initToolBar();
        mUserToken = OznerPreference.getUserToken(this);
        mUserid = UserDataPreference.GetUserData(this, UserDataPreference.UserId, "");
        try {
            mac = getIntent().getStringExtra(Contacts.PARMS_MAC);
            oznerSetting = DBManager.getInstance(this).getDeviceSettings(mUserid, mac);
            gender = (int) oznerSetting.getAppData(Contacts.DEV_REPLEN_GENDER);
        } catch (Exception ex) {
            LCLogUtils.E(TAG, "onCreate_Ex:" + ex.getMessage());
        }

        initView();
        loadTestTimes();
        loadBuShuiFenbu();
    }

    /**
     * 初始化ToolBar
     */
    private void initToolBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        title.setText(R.string.replen_query_title);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.replen_blue_bg));
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
     * 初始化页面相关事件和数据
     */
    private void initView() {
        rgSkinStatus.setOnCheckedChangeListener(this);
        if (gender == 0) {
            loadImg(ivSkinStatus, R.drawable.img_women_query_dry);
            loadImg(ivSkinNull, R.drawable.img_women_query_null);
        } else {
            loadImg(ivSkinStatus, R.drawable.img_man_query_dry);
            loadImg(ivSkinNull, R.drawable.img_man_query_null);
        }
    }

//    /**
//     * 加载数据
//     */
//    private void showData() {
//
//    }

    @Override
    protected void onResume() {
        tvTestTime.setText(String.valueOf(countTotal));
        super.onResume();
    }

    @OnClick(R.id.tv_buy_essence_btn)
    public void onClick() {
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int skinStatusResId;
        switch (checkedId) {
            case R.id.rb_skin_dry:
                tvSkinStatusNotice.setText(R.string.replen_skin_dry_notice);
                if (gender == 0) {
                    skinStatusResId = R.drawable.img_women_query_dry;
                } else {
                    skinStatusResId = R.drawable.img_man_query_dry;
                }
                break;
            case R.id.rb_skin_oil:
                tvSkinStatusNotice.setText(R.string.replen_skin_oil_notice);
                if (gender == 0) {
                    skinStatusResId = R.drawable.img_women_query_oil;
                } else {
                    skinStatusResId = R.drawable.img_man_query_oil;
                }
                break;
            case R.id.rb_skin_neture:
                tvSkinStatusNotice.setText(R.string.replen_skin_neture_notice);
                if (gender == 0) {
                    skinStatusResId = R.drawable.img_women_query_neutral;
                } else {
                    skinStatusResId = R.drawable.img_man_query_neture;
                }
                break;
            default:
                tvSkinStatusNotice.setText(R.string.replen_skin_dry_notice);
                if (gender == 0) {
                    skinStatusResId = R.drawable.img_women_query_dry;
                } else {
                    skinStatusResId = R.drawable.img_man_query_dry;
                }
                break;
        }
        loadImg(ivSkinStatus, skinStatusResId);
    }

    /**
     * 加载图片
     *
     * @param imageView
     * @param resId
     */
    private void loadImg(ImageView imageView, int resId) {
        imageView.setImageDrawable(ContextCompat.getDrawable(this, resId));
//        Glide.with(ReplenQueryActivity.this)
//                .load(resId)
//                .asBitmap()
//                .error(errid)
//                .fitCenter()
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .into(imageView);
    }

    /**
     * 刷新皮肤状态
     *
     * @param oilTotal  检测总值
     * @param timeTotal 总检测次数
     */
    private void refreshSkinStatus(float oilTotal, int timeTotal) {
        try {
            float oilAverage = 0;
            if (timeTotal > 0) {
                oilAverage = oilTotal / timeTotal;
                tvNeverTest.setText(R.string.replen_times_tips);
            } else {
                tvNeverTest.setText(R.string.replen_never_test);
            }
//            tvTestTime.setText(String.valueOf(timeTotal));

            if (oilAverage > 0 && oilAverage <= 12) {
                //干性皮肤
                tvSkinTypeTips.setText(R.string.replen_skin_dry_notice);
                tvSkinTypeTips.setText(R.string.replen_skin_dry);
                rbSkinDry.setVisibility(View.GONE);
                rbSkinOil.setVisibility(View.VISIBLE);
                rbSkinNeture.setVisibility(View.VISIBLE);
                rgSkinStatus.check(R.id.rb_skin_oil);
                if (gender == 0) {
                    loadImg(ivSkinNull, R.drawable.img_women_query_dry);
                } else {
                    loadImg(ivSkinNull, R.drawable.img_man_query_dry);
                }
            } else if (oilAverage > 12 && oilAverage <= 20) {
                //中性皮肤
                tvSkinTypeTips.setText(R.string.replen_skin_neture_notice);
                tvSkinType.setText(R.string.replen_skin_neture);
                rbSkinNeture.setVisibility(View.GONE);
                rbSkinDry.setVisibility(View.VISIBLE);
                rbSkinOil.setVisibility(View.VISIBLE);
                rgSkinStatus.check(R.id.rb_skin_dry);
                if (gender == 0) {
                    loadImg(ivSkinNull, R.drawable.img_women_query_neutral);
                } else {
                    loadImg(ivSkinNull, R.drawable.img_man_query_neture);
                }
            } else if (oilAverage > 20) {
                //油性皮肤
                tvSkinTypeTips.setText(R.string.replen_skin_oil_notice);
                tvSkinType.setText(R.string.replen_skin_oil);
                rbSkinOil.setVisibility(View.GONE);
                rbSkinDry.setVisibility(View.VISIBLE);
                rbSkinNeture.setVisibility(View.VISIBLE);
                rgSkinStatus.check(R.id.rb_skin_dry);
                if (gender == 0) {
                    loadImg(ivSkinNull, R.drawable.img_women_query_oil);
                } else {
                    loadImg(ivSkinNull, R.drawable.img_man_query_oil);
                }
            }
        } catch (Exception ex) {
            LCLogUtils.E(TAG, "refreshSkinStatus_Ex:" + ex.getMessage());
        }
    }

    /**
     * 获取总检测次数
     */
    private void loadTestTimes() {
        HttpMethods.getInstance().getTimesCountBuShui(mUserToken, mac,
                new ProgressSubscriber<JsonObject>(this, new Action1<JsonObject>() {
                    @Override
                    public void call(JsonObject jsonObject) {
                        LCLogUtils.E(TAG, "loadTestTimes:" + jsonObject.toString());
                        if (jsonObject != null) {
                            if (jsonObject.get("state").getAsInt() > 0) {
                                countTotal = 0;
                                JsonArray dataArray = jsonObject.getAsJsonArray("data");
                                if (!dataArray.isJsonNull() && dataArray.size() > 0) {
                                    for (JsonElement element : dataArray) {
                                        countTotal += element.getAsJsonObject().get("times").getAsInt();
                                    }
                                }
                                try {
                                    tvTestTime.setText(String.valueOf(countTotal));
                                } catch (Exception ex) {
                                    LCLogUtils.E(TAG, "loadTestTimes_Ex:" + ex.getMessage());
                                }
                            }
                        }
                    }
                }));
    }

    /**
     * 加载补水仪历史检测数据
     * 目前只获取脸部数据
     */
    private void loadBuShuiFenbu() {
        LCLogUtils.E(TAG, "开始加载历史检测数据");
        HttpMethods.getInstance().getBuShuiFenBu(mUserToken, mac, ReplenFenBuAction.FaceSkinValue,
                new ProgressSubscriber<JsonObject>(this, new Action1<JsonObject>() {
                    @Override
                    public void call(JsonObject jsonObject) {
                        if (jsonObject != null) {
                            Log.e(TAG, "loadBuShuiFenbu: " + jsonObject.toString());
                            int state = jsonObject.get("state").getAsInt();
                            if (state > 0) {
                                JsonObject faceData = jsonObject.getAsJsonObject("data").getAsJsonObject("FaceSkinValue");
                                if (!faceData.isJsonNull()) {
                                    JsonArray faceMonthArray = faceData.getAsJsonArray("monty");
                                    if (!faceMonthArray.isJsonNull()) {
                                        int arraySize = faceMonthArray.size();
                                        float oilTotal = 0;
                                        int timeTotal = 0;
                                        /**
                                         * 皮肤肤质只计算脸部数据
                                         *
                                         *数组中是每天测试的平均值以及每天测试的次数，测试的时候会上传当前测试的油性值，服务器计算平均值，并和当天测试次数一起存储；
                                         *所以计算月数据的时候需要把每天的值统计，然后求平均
                                         */
                                        for (int i = 0; i < arraySize; i++) {
                                            float oil = faceMonthArray.get(i).getAsJsonObject().get("ynumber").getAsFloat();
                                            int time = faceMonthArray.get(i).getAsJsonObject().get("times").getAsInt();
                                            oilTotal += oil * time;
                                            timeTotal += time;
                                        }

                                        refreshSkinStatus(oilTotal, timeTotal);
                                    }
                                }
                            }
                        } else {
                            LCLogUtils.E(TAG, "结果为空");
                        }
                    }
                }));
    }
}
