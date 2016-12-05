package com.ozner.cup.Device;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.WaterPurifierAttr;
import com.ozner.cup.Device.WaterPurifier.WaterNetInfoManager;
import com.ozner.cup.R;
import com.ozner.cup.UIView.FilterProgressView;
import com.ozner.cup.UIView.UIZGridView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.ozner.cup.R.id.uiz_moreProject;

public class FilterStatusActivity extends BaseActivity implements AdapterView.OnItemClickListener, IFilterStatusView {
    private static final String TAG = "FilterStatusActivity";
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
    private int deviceType = TYPE_WATER_FILTER;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_status);
        ButterKnife.inject(this);
        tvBuyWaterPurifier.setText(R.string.buy_filter);
        uizMoreProject.setOnItemClickListener(this);
        initToolBar();
        initStaticData();

        try {
            mac = getIntent().getStringExtra(Contacts.PARMS_MAC);
            deviceType = getIntent().getIntExtra(PARMS_DEVICE_TYPE, TYPE_WATER_FILTER);
            if (TYPE_WATER_FILTER == deviceType) {
                initWaterPurifierInfo();
            } else if (TYPE_TAP_FILTER == deviceType) {
                Log.e(TAG, "onCreate: 水探头");
                initTapInfo();
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
        // TODO: 2016/12/2 隐藏该隐藏的信息
        initWaterPurifierFilter(mac);
    }

    /**
     * 初始化水探头滤芯信息
     */
    private void initTapInfo(){

    }

    /**
     * 初始化净水器滤芯状态
     *
     * @param mac
     */
    private void initWaterPurifierFilter(String mac) {
        try {
            purifierAttr = DBManager.getInstance(this).getWaterAttr(mac);
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
                updateFilterInfoUI(attr);

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
                // TODO: 2016/12/2  
                showToastCenter("咨询");
                break;
            case R.id.tv_buy_water_purifier:
                // TODO: 2016/12/2  
                showToastCenter("购买滤芯");
                break;
            case R.id.tv_scancode_btn:
                // TODO: 2016/12/2  
                showToastCenter("扫描二维码");
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO: 2016/12/2
        showToastCenter("点击了：" + projectStr[position]);
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
}
