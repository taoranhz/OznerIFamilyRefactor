package com.ozner.cup.Device;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Base.WebActivity;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Bean.OznerBroadcastAction;
import com.ozner.cup.Bean.RankType;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.OznerDeviceSettings;
import com.ozner.cup.DBHelper.UserInfo;
import com.ozner.cup.DBHelper.WaterPurifierAttr;
import com.ozner.cup.Device.WaterPurifier.WaterNetInfoManager;
import com.ozner.cup.HttpHelper.ApiException;
import com.ozner.cup.HttpHelper.HttpMethods;
import com.ozner.cup.HttpHelper.OznerHttpResult;
import com.ozner.cup.HttpHelper.ProgressSubscriber;
import com.ozner.cup.QRCodeScan.activity.CaptureActivity;
import com.ozner.cup.R;
import com.ozner.cup.UIView.FilterProgressView;
import com.ozner.cup.UIView.UIZGridView;
import com.ozner.cup.Utils.LCLogUtils;
import com.ozner.cup.Utils.WeChatUrlUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.ozner.cup.Device.Tap.TapFragment.INIT_WARRANTY;
import static com.ozner.cup.R.id.uiz_moreProject;

public class FilterStatusActivity extends BaseActivity implements AdapterView.OnItemClickListener, IFilterStatusView {
    private static final String TAG = "FilterStatusActivity";
    private final static int SCANNIN_GREQUEST_CODE = 0x03;
    public static final int TYPE_WATER_FILTER = 0;
    public static final int TYPE_TAP_FILTER = 1;
    public static final String PARMS_DEVICE_TYPE = "parms_device_type";

    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.tv_remainTime)
    TextView tvRemainTime;
    @InjectView(R.id.tv_remainPre)
    TextView tvRemainPre;
    @InjectView(R.id.filter_progress)
    FilterProgressView filterProgress;
    @InjectView(R.id.textView)
    TextView textView;
    @InjectView(R.id.tv_buy_water_purifier)
    TextView tvBuyWaterPurifier;
    @InjectView(R.id.ll_en_no)
    LinearLayout llEnNo;
    @InjectView(R.id.tv_chat_btn)
    TextView tvChatBtn;
    @InjectView(R.id.llay_scanCode)
    LinearLayout llayScanCode;
    @InjectView(uiz_moreProject)
    UIZGridView uizMoreProject;
    @InjectView(R.id.uiz_onzeService)
    UIZGridView uizOnzeService;
    @InjectView(R.id.llay_moreService)
    LinearLayout llayMoreService;

    private ArrayList<HashMap<String, Object>> projectList, serviceList;
    //更多产品
    private int[] projectImgs;// = {R.drawable.filter_status_tap, R.drawable.filter_status_purifier, R.drawable.filter_status_cup};
    private String[] projectStr;// = {"浩泽智能水探头", "金色伊泉系列", "浩泽智能杯"};
    private SimpleAdapter projectAdapter, serviceAdapter;
    //浩泽服务
    private int[] serviceImgs;
    private String[] serviceUpStr;
    private String[] serviceDownStr;
    private WaterNetInfoManager waterNetInfoManager;
    private WaterPurifierAttr purifierAttr;
    private String mac = "";
    private String userid = "";
    private int deviceType = TYPE_WATER_FILTER;
    private ProgressDialog progressDialog;
    private UserInfo userInfo;
    SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_status);
        ButterKnife.inject(this);
        tvBuyWaterPurifier.setText(R.string.buy_filter);
        uizMoreProject.setOnItemClickListener(this);
        initToolBar();
        initStaticData();
        userid = UserDataPreference.GetUserData(this, UserDataPreference.UserId, null);
        if (userid != null) {
            userInfo = DBManager.getInstance(this).getUserInfo(userid);
        }
        try {
            mac = getIntent().getStringExtra(Contacts.PARMS_MAC);
            if (null != mac && "" != mac) {
                deviceType = getIntent().getIntExtra(PARMS_DEVICE_TYPE, TYPE_WATER_FILTER);
                if (TYPE_WATER_FILTER == deviceType) {
                    initWaterPurifierInfo();
                } else if (TYPE_TAP_FILTER == deviceType) {
                    Log.e(TAG, "onCreate: 水探头");
                    initTapInfo();
                }
            } else {
                new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT)
                        .setMessage(R.string.device_address_err)
                        .setPositiveButton(R.string.ensure, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                FilterStatusActivity.this.finish();
                            }
                        }).show();
            }
        } catch (Exception ex) {
            Log.e(TAG, "onCreate_Ex: " + ex.getMessage());
        }
    }


    /**
     * 初始化ToolBar
     */
    private void initToolBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        title.setText(R.string.current_filter_state);
        toolbar.setNavigationIcon(R.drawable.back);
    }

    /**
     * 初始化净水器信息
     */
    private void initWaterPurifierInfo() {
        purifierAttr = DBManager.getInstance(this).getWaterAttr(mac);
        if (purifierAttr != null) {
            if (purifierAttr.getBuylinkurl() != null) {
                if (purifierAttr.isBoolshow()) {
                    llayScanCode.setVisibility(View.VISIBLE);
                } else {
                    llayScanCode.setVisibility(View.GONE);
                }
            } else {
                llayScanCode.setVisibility(View.VISIBLE);
            }
        } else {
            llayScanCode.setVisibility(View.VISIBLE);
        }

        initWaterPurifierFilter(mac);
    }

    /**
     * 初始化水探头滤芯信息
     */
    private void initTapInfo() {
        llayMoreService.setVisibility(View.GONE);

        try {
            OznerDeviceSettings oznerSetting = DBManager.getInstance(this).getDeviceSettings(userid, mac);
            if (oznerSetting != null) {
                LCLogUtils.E(TAG, "refreshTapFilterInfo:" + oznerSetting.getSettings());
                String startTimeStr = (String) oznerSetting.getAppData(Contacts.TAP_FILTER_START_TIME);
                long updateTimeMill = (long) oznerSetting.getAppData(Contacts.TAP_FILTER_UPDATE_TIMEMILLS);
                int useDay = (int) oznerSetting.getAppData(Contacts.TAP_FILTER_USEDAY);
                if (DateUtils.isToday(updateTimeMill)) {
                    Date startTime = dataFormat.parse(startTimeStr);
                    if (useDay < INIT_WARRANTY) {
                        int ret = (INIT_WARRANTY - useDay) / INIT_WARRANTY * 100;
                        tvRemainTime.setText(String.valueOf(INIT_WARRANTY - useDay));
                        tvRemainPre.setText(String.valueOf(ret));
                    }
                    filterProgress.initTime(startTime, INIT_WARRANTY);
                    filterProgress.update(useDay);
                } else {
                    loadTapFilterFromNet();
                }
            } else {
                loadTapFilterFromNet();
            }
        } catch (Exception ex) {
            LCLogUtils.E(TAG, "initTapInfo_Ex:" + ex.getMessage());
            loadTapFilterFromNet();
        }
    }

    /**
     * 从网络加载滤芯数据
     * <p>
     * 这里获取数据，需要在设备配对时，将设备同步到服务器
     */
    private void loadTapFilterFromNet() {
        HttpMethods.getInstance().getTapFilterInfo(OznerPreference.getUserToken(FilterStatusActivity.this), mac,
                new ProgressSubscriber<JsonObject>(FilterStatusActivity.this, new OznerHttpResult<JsonObject>() {
                    @Override
                    public void onError(Throwable e) {
                        LCLogUtils.E(TAG, "loadTapFilterFromNet_onError: " + e.getMessage());
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        LCLogUtils.E(TAG, "loadTapFilterFromNet: " + jsonObject.toString());
                        try {
                            if (jsonObject != null) {
                                if (jsonObject.get("state").getAsInt() > 0) {
                                    String modifytime = jsonObject.get("modifytime").getAsString();
                                    Date startTime = dataFormat.parse(modifytime);
                                    int useDay = jsonObject.get("useday").getAsInt();
                                    if (useDay < INIT_WARRANTY) {
                                        int ret = (INIT_WARRANTY - useDay) / INIT_WARRANTY * 100;
                                        tvRemainTime.setText(String.valueOf(INIT_WARRANTY - useDay));
                                        tvRemainPre.setText(String.valueOf(ret));
                                    }
                                    filterProgress.initTime(startTime, INIT_WARRANTY);
                                    filterProgress.update(useDay);
                                    OznerDeviceSettings oznerSetting = DBManager.getInstance(FilterStatusActivity.this).getDeviceSettings(userid, mac);
                                    if (oznerSetting != null) {
                                        oznerSetting.setAppData(Contacts.TAP_FILTER_START_TIME, startTime);
                                        oznerSetting.setAppData(Contacts.TAP_FILTER_USEDAY, useDay);
                                        oznerSetting.setAppData(Contacts.TAP_FILTER_UPDATE_TIMEMILLS, System.currentTimeMillis());
                                        DBManager.getInstance(FilterStatusActivity.this).updateDeviceSettings(oznerSetting);
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            LCLogUtils.E(TAG, "loadTapFilterFromNet_Ex:" + ex.getMessage());
                        }
                    }
                }));
    }

    /**
     * 初始化净水器滤芯状态
     *
     * @param mac
     */
    private void initWaterPurifierFilter(String mac) {
        try {

            if (null == waterNetInfoManager) {
                waterNetInfoManager = new WaterNetInfoManager(this);
            }

            //获取滤芯信息
            if (purifierAttr != null && purifierAttr.getFilterNowtime() != 0) {
                Log.e(TAG, "initWaterAttrInfo_filter: " + purifierAttr.getFilterNowtime());

                long dayTimeMill = purifierAttr.getFilterNowtime() / (24 * 3600 * 1000);
                long todayTimeMill = Calendar.getInstance().getTimeInMillis() / (24 * 3600 * 1000);
                if (dayTimeMill != todayTimeMill) {
                    loadWaterFilterNet(mac);
                } else {
                    updateFilterInfoUI(purifierAttr);
                }
            } else {
                loadWaterFilterNet(mac);
            }
        } catch (Exception ex) {
            Log.e(TAG, "initWaterPurifierFilter_Ex: " + ex.getMessage());
        }
    }

    /**
     * 从网络加载水机滤芯数据
     *
     * @param mac
     */
    private void loadWaterFilterNet(String mac) {
        progressDialog = new ProgressDialog(this);//, null, getString(R.string.data_loading));
        progressDialog.setMessage(getString(R.string.data_loading));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        waterNetInfoManager.getWaterFilterInfo(mac, new WaterNetInfoManager.IWaterAttr() {
            @Override
            public void onResult(WaterPurifierAttr attr) {
                progressDialog.cancel();
                if (attr != null) {
                    updateFilterInfoUI(attr);
                }

            }
        });
    }

    /**
     * 更新净水器滤芯状态
     *
     * @param attr
     */
    private void updateFilterInfoUI(final WaterPurifierAttr attr) {
        if (attr != null) {
            Calendar nowCal = Calendar.getInstance();
            nowCal.setTimeInMillis(attr.getFilterNowtime());
            Calendar endCal = Calendar.getInstance();
            endCal.setTimeInMillis(attr.getFilterTime());
            Calendar startCal = (Calendar) endCal.clone();
            startCal.add(Calendar.YEAR, -1);

            if (startCal.getTimeInMillis() > nowCal.getTimeInMillis()) {//当前时间早于有效期一年
                float remainDay = (endCal.getTimeInMillis() - nowCal.getTimeInMillis()) / (1000.0f * 3600 * 24);
                showRemainDay((int) remainDay);
                showRemainPre(100);
                showFilterProgress(nowCal.getTime(), endCal.getTime(), nowCal.getTime());
            } else if (endCal.getTimeInMillis() > nowCal.getTimeInMillis()) {//当前时间在一年有效期内
                float remainDay = (endCal.getTimeInMillis() - nowCal.getTimeInMillis()) / (1000.0f * 3600 * 24);
                int dayOfYear = nowCal.getActualMaximum(Calendar.DAY_OF_YEAR);
                Log.e(TAG, "dayOfYear: " + dayOfYear);
                float pre = (remainDay / dayOfYear) * 100;
                showRemainDay((int) remainDay);
                showRemainPre((int) Math.ceil(pre));
                showFilterProgress(startCal.getTime(), endCal.getTime(), nowCal.getTime());
            } else {//超出有效期
                showRemainDay(0);
                showRemainPre(0);
                showFilterProgress(startCal.getTime(), endCal.getTime(), nowCal.getTime());
            }
        }
    }


    /**
     * 初始化更多产品和安心服务数据
     */
    private void initStaticData() {
        filterProgress.setThumb(R.drawable.filter_status_thumb);
        projectImgs = new int[]{R.drawable.filter_status_tap, R.drawable.filter_status_purifier, R.drawable.filter_status_cup};
        projectStr = new String[]{getString(R.string.Filter_Project1), getString(R.string.Filter_Project2), getString(R.string.Filter_Project3)};

        serviceImgs = new int[]{R.drawable.filter_status_00, R.drawable.filter_status_01,
                R.drawable.filter_status_10, R.drawable.filter_status_11,
                R.drawable.filter_status_20, R.drawable.filter_status_21,
                R.drawable.filter_status_30, R.drawable.filter_status_31
        };
        serviceUpStr = new String[]{getString(R.string.Filter_Service_up_00), getString(R.string.Filter_Service_up_01),
                getString(R.string.Filter_Service_up_10), getString(R.string.Filter_Service_up_11),
                getString(R.string.Filter_Service_up_20), getString(R.string.Filter_Service_up_21),
                getString(R.string.Filter_Service_up_30), getString(R.string.Filter_Service_up_31)
        };
        serviceDownStr = new String[]{getString(R.string.Filter_Service_down_00), getString(R.string.Filter_Service_down_01),
                getString(R.string.Filter_Service_down_10), getString(R.string.Filter_Service_down_11),
                getString(R.string.Filter_Service_down_20), "",
                "", ""
        };
        projectList = new ArrayList<HashMap<String, Object>>();
        serviceList = new ArrayList<HashMap<String, Object>>();
        loadMoreProjectData();
        loadServiceData();
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
     * 加载更多产品数据
     */
    private void loadMoreProjectData() {
        projectList.clear();
        for (int i = 0; i < projectImgs.length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("itemImg", projectImgs[i]);
            map.put("itemText", projectStr[i]);
            projectList.add(map);
        }
        projectAdapter = new SimpleAdapter(this, projectList, R.layout.more_product_item,
                new String[]{"itemImg", "itemText"}, new int[]{R.id.iv_more_product_img, R.id.tv_more_product_text});

        uizMoreProject.setAdapter(projectAdapter);
    }

    /**
     * 加载安心服务数据
     */
    private void loadServiceData() {
        serviceList.clear();
        for (int i = 0; i < serviceImgs.length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("itemImg", serviceImgs[i]);
            map.put("itemTextUp", serviceUpStr[i]);
            map.put("itemTextDown", serviceDownStr[i]);
            serviceList.add(map);
        }
        serviceAdapter = new SimpleAdapter(this, serviceList, R.layout.more_ozner_service_item,
                new String[]{"itemImg", "itemTextUp", "itemTextDown"},
                new int[]{R.id.iv_more_service_img, R.id.tv_more_service_up_text, R.id.tv_more_service_down_text});

        uizOnzeService.setAdapter(serviceAdapter);
    }

    @OnClick({R.id.tv_chat_btn, R.id.tv_buy_water_purifier, R.id.tv_scancode_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_chat_btn:
//                // TODO: 2016/12/2
//                showToastCenter("咨询");
                sendBroadcast(new Intent(OznerBroadcastAction.OBA_SWITCH_CHAT));
                this.finish();
                break;
            case R.id.tv_buy_water_purifier:
                if (userInfo != null && userInfo.getMobile() != null && !userInfo.getMobile().isEmpty()) {
                    if (TYPE_TAP_FILTER == deviceType) {
                        String tapUrl = WeChatUrlUtil.formatTapShopUrl(userInfo.getMobile()
                                , OznerPreference.getUserToken(this), "zh", "zh");
                        startWebActivity(tapUrl);
                    } else if (TYPE_WATER_FILTER == deviceType) {
                        String waterUrl = WeChatUrlUtil.formatSecurityServiceUrl(userInfo.getMobile(),
                                OznerPreference.getUserToken(this), "zh", "zh");
                        if (purifierAttr != null && purifierAttr.getBuylinkurl() != null && !purifierAttr.getBuylinkurl().isEmpty()) {
                            waterUrl = WeChatUrlUtil.formatUrl(purifierAttr.getBuylinkurl(), userInfo.getMobile()
                                    , OznerPreference.getUserToken(this), "zh", "zh");
                        }
                        startWebActivity(waterUrl);
                    }
                } else {
                    showToastCenter(R.string.userinfo_miss);
                }
                break;
            case R.id.tv_scancode_btn:
                // TODO: 2016/12/2  
//                showToastCenter("扫描二维码");
                Intent scanIntent = new Intent(FilterStatusActivity.this, CaptureActivity.class);
                startActivityForResult(scanIntent, SCANNIN_GREQUEST_CODE);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO: 2016/12/2
//        showToastCenter("点击了：" + projectStr[position]);
        switch (position) {
            case 0:
                if (userInfo != null && userInfo.getMobile() != null && !userInfo.getMobile().isEmpty()) {
                    String tapUrl = WeChatUrlUtil.formatFilterTapUrl(userInfo.getMobile()
                            , OznerPreference.getUserToken(FilterStatusActivity.this), "zh", "zh");
                    startWebActivity(tapUrl);
                } else {
                    showToastCenter(R.string.userinfo_miss);
                }
                break;
            case 1:
                if (userInfo != null && userInfo.getMobile() != null && !userInfo.getMobile().isEmpty()) {
                    String goldUrl = WeChatUrlUtil.formatFilterGoldSpringUrl(userInfo.getMobile()
                            , OznerPreference.getUserToken(FilterStatusActivity.this), "zh", "zh");
                    startWebActivity(goldUrl);
                } else {
                    showToastCenter(R.string.userinfo_miss);
                }
                break;
            case 2:
                if (userInfo != null && userInfo.getMobile() != null && !userInfo.getMobile().isEmpty()) {
                    String cupUrl = WeChatUrlUtil.formatFilterCupUrl(userInfo.getMobile()
                            , OznerPreference.getUserToken(FilterStatusActivity.this), "zh", "zh");
                    startWebActivity(cupUrl);
                } else {
                    showToastCenter(R.string.userinfo_miss);
                }
                break;
        }
    }

    /**
     * 点击更多产品，跳转到指定商城页面
     *
     * @param url
     */
    private void startWebActivity(String url) {
        Intent filterIntent = new Intent(FilterStatusActivity.this, WebActivity.class);
        filterIntent.putExtra(Contacts.PARMS_URL, url);
        startActivity(filterIntent);
    }

    @Override
    public void showRemainDay(int day) {
        tvRemainTime.setText(String.valueOf(day));
    }

    @Override
    public void showRemainPre(int pre) {
        tvRemainPre.setText(String.valueOf(pre));
    }

    @Override
    public void showFilterProgress(Date startTime, Date endTime, Date currentTime) {
        filterProgress.initTime(startTime, endTime);
        filterProgress.update(currentTime);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SCANNIN_GREQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String scanResult = bundle.getString("result");
                    if (null != scanResult && "" != scanResult) {
                        reNewFilterTime(deviceType == 0 ? RankType.WaterType : RankType.TapType, scanResult);
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 更新滤芯服务时间
     *
     * @param type
     * @param code
     */
    private void reNewFilterTime(final String type, String code) {
        HttpMethods.getInstance().reNewFilterTime(OznerPreference.getUserToken(FilterStatusActivity.this), mac, type, code,
                new ProgressSubscriber<JsonObject>(FilterStatusActivity.this, getString(R.string.loading), false, new OznerHttpResult<JsonObject>() {
                    @Override
                    public void onError(Throwable e) {
                        LCLogUtils.E(TAG, "reNewFilterTime_Ex:" + e.getMessage());
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        LCLogUtils.E(TAG, "reNewFilterTime:" + jsonObject.toString());
                        if (jsonObject != null) {
                            if (jsonObject.get("state").getAsInt() > 0) {
                                if (deviceType == TYPE_TAP_FILTER) {
                                    loadTapFilterFromNet();
                                } else {
                                    loadWaterFilterNet(mac);
                                }
                            } else {
                                showToastCenter(ApiException.getErrResId(jsonObject.get("state").getAsInt()));
                            }
                        }
                    }
                }));
    }
}
