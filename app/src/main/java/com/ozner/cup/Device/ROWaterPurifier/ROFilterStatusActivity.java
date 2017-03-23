package com.ozner.cup.Device.ROWaterPurifier;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.github.kayvannj.permission_utils.PermissionUtil;
import com.google.gson.JsonObject;
import com.ozner.WaterPurifier.WaterPurifier_RO_BLE;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Base.WebActivity;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Bean.OznerBroadcastAction;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.UserInfo;
import com.ozner.cup.DBHelper.WaterPurifierAttr;
import com.ozner.cup.Device.WaterPurifier.WaterNetInfoManager;
import com.ozner.cup.HttpHelper.ApiException;
import com.ozner.cup.HttpHelper.HttpMethods;
import com.ozner.cup.HttpHelper.OznerHttpResult;
import com.ozner.cup.HttpHelper.ProgressSubscriber;
import com.ozner.cup.R;
import com.ozner.cup.UIView.UIZGridView;
import com.ozner.cup.Utils.LCLogUtils;
import com.ozner.cup.Utils.WeChatUrlUtil;
import com.ozner.device.OperateCallback;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.ozner.cup.R.id.uiz_moreProject;

public class ROFilterStatusActivity extends BaseActivity implements AdapterView.OnItemClickListener{
    private static final String TAG = "ROFilterStatusActivity";
    private final static int SCANNIN_GREQUEST_CODE = 0x03;
    public static final int TYPE_WATER_FILTER = 0;
    public static final int TYPE_TAP_FILTER = 1;
    public static final String PARMS_DEVICE_TYPE = "parms_device_type";

    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
//    @InjectView(R.id.tv_remainTime)
//    TextView tvRemainTime;
//    @InjectView(R.id.tv_remainPre)
//    TextView tvRemainPre;
//    @InjectView(R.id.filter_progress)
//    FilterProgressView filterProgress;
//    @InjectView(R.id.textView)
//    TextView textView;
    @InjectView(R.id.tds_health_buy_layout)
    LinearLayout tvBuyWaterPurifier;
    @InjectView(R.id.ll_en_no)
    LinearLayout llEnNo;
    @InjectView(R.id.tds_health_know_layout)
    LinearLayout tvChatBtn;
    @InjectView(R.id.llay_scanCode)
    LinearLayout llayScanCode;
    @InjectView(R.id.uiz_onzeService)
    UIZGridView uizOnzeService;
    @InjectView(R.id.llay_moreService)
    LinearLayout llayMoreService;
    @InjectView(uiz_moreProject)
    UIZGridView uizMoreProject;
    @InjectView(R.id.tv_rolxa)
    TextView tv_rolxa;
    @InjectView(R.id.tv_rolxb)
    TextView tv_rolxb;
    @InjectView(R.id.tv_rolxc)
    TextView tv_rolxc;
    @InjectView(R.id.tv_ro_filter)
    TextView tv_ro_filter;
    @InjectView(R.id.tv_ro_filterRest)
    TextView tv_ro_filterRest;

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
    private String deviceType = "";
    private ProgressDialog progressDialog;
    private UserInfo userInfo;
    SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private PermissionUtil.PermissionRequestObject perReqResult;
    private OznerDevice device;
    private String waterUrl="";
    private String fit_a,fit_b,fit_c;
    private Timer timer;
    //RO文字呼吸灯
    private int index = 0;
    private boolean isOpen = true;
    private WaterPurifier_RO_BLE roWaterPurifier;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ro_filter_status);
        ButterKnife.inject(this);
//        tvBuyWaterPurifier.setText(R.string.buy_filter);
        tv_ro_filter.setVisibility(View.GONE);
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
                device = OznerDeviceManager.Instance().getDevice(mac);
                deviceType = device.Type();
                roWaterPurifier=(WaterPurifier_RO_BLE) device;
                if ("Ozner RO".equals(deviceType)) {
                    //RO水机滤芯状态
                    fit_a=getIntent().getStringExtra("Fit_a");
                    fit_b=getIntent().getStringExtra("Fit_b");
                    fit_c=getIntent().getStringExtra("Fit_c");
                    initWaterPurifierInfo();
                }
            } else {
                new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT)
                        .setMessage(R.string.device_address_err)
                        .setPositiveButton(R.string.ensure, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                ROFilterStatusActivity.this.finish();
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

        if(fit_a!=null&&!(Integer.parseInt(fit_a)<0)){
            tv_rolxa.setText(fit_a+"%");
        }else{
            tv_rolxa.setText(getString(R.string.state_null));
        }
        if(fit_b!=null&&!(Integer.parseInt(fit_b)<0)){
            tv_rolxb.setText(fit_b+"%");
        }else{
            tv_rolxb.setText(getString(R.string.state_null));
        }
        if(fit_c!=null&&!(Integer.parseInt(fit_c)<0)){
            tv_rolxc.setText(fit_c+"%");
        }else{
            tv_rolxc.setText(getString(R.string.state_null));
        }
        //复位键的显示
        if((Integer.parseInt(fit_a) ==0) || (Integer.parseInt(fit_b)==0)||(Integer.parseInt(fit_c)==0)){
            tv_ro_filterRest.setVisibility(View.VISIBLE);
        }else{
            tv_ro_filterRest.setVisibility(View.INVISIBLE);
        }
//文字呼吸灯
        try {
            if((Integer.parseInt(fit_a) >=0) || (Integer.parseInt(fit_b)>0)||(Integer.parseInt(fit_c)>=0)){
                if ((Integer.parseInt(fit_a) < 30) || (Integer.parseInt(fit_b)<30)||(Integer.parseInt(fit_c)<30)) {
//                timer();
                    tv_ro_filter.setVisibility(View.GONE);
                }
            }else{
                tv_ro_filter.setVisibility(View.GONE);
            }
        }catch(Exception e){
            e.getStackTrace();
        }
    }

    private void timer() {
        timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (isOpen) {
                    if (index == 2) {
                        index = 0;
                    }
                    index++;
                    Message message = new Message();
                    message.what = index;
                    handler.sendMessage(message);
                }
            }
        };
        timer.schedule(task, 0, 1000); // 延时0ms后执行，1000ms执行一次
    }

    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    tv_ro_filter.clearAnimation();
                    tv_ro_filter.setAnimation(getLoad());
                    break;
                case 2:
                    tv_ro_filter.clearAnimation();
                    tv_ro_filter.setAnimation(getOut());
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private Animation getLoad() {
        return AnimationUtils.loadAnimation(ROFilterStatusActivity.this,
                R.anim.push_in);
    }

    private Animation getOut() {
        return AnimationUtils.loadAnimation(ROFilterStatusActivity.this,
                R.anim.push_out);
    }



    /**
     * 初始化更多产品和安心服务数据
     */
    private void initStaticData() {
//        filterProgress.setThumb(R.drawable.filter_status_thumb);
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

    @OnClick({R.id.tds_health_buy_layout, R.id.tds_health_know_layout, R.id.tv_ro_filterRest})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tds_health_know_layout:
//                // TODO: 2016/12/2
//                showToastCenter("咨询");
                sendBroadcast(new Intent(OznerBroadcastAction.OBA_SWITCH_CHAT));
                this.finish();
                break;
            case R.id.tds_health_buy_layout:
                if (userInfo != null && userInfo.getMobile() != null && !userInfo.getMobile().isEmpty()) {
                        waterUrl=WeChatUrlUtil.getMallUrl(userInfo.getMobile(),OznerPreference.getUserToken(this), "zh", "zh");
                        startWebActivity(waterUrl);

                } else {
                    showToastCenter(R.string.userinfo_miss);
                }
                break;
            case R.id.tv_ro_filterRest:
                new AlertDialog.Builder(ROFilterStatusActivity.this)
                        .setMessage(getString(R.string.rofilter_need_change))
                        .setPositiveButton(getString(R.string.buy_filter), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (userInfo != null && userInfo.getMobile() != null && !userInfo.getMobile().isEmpty()) {
                                    waterUrl=WeChatUrlUtil.getMallUrl(userInfo.getMobile(),OznerPreference.getUserToken(ROFilterStatusActivity.this), "zh", "zh");
                                    startWebActivity(waterUrl);

                                } else {
                                    showToastCenter(R.string.userinfo_miss);
                                }
                                dialog.dismiss();
                            }
                        }).setNegativeButton(getString(R.string.I_got_it), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e("trfilter", "复位" );
                        //判断ro水机是否为空
                        if(roWaterPurifier!=null) {
                            if (roWaterPurifier.isEnableFilterReset()) {
//                                tv_ro_filterRest.setVisibility(View.VISIBLE);
                                //复位
                                roWaterPurifier.resetFilter(new OperateCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void var1) {
                                        Log.e("trfilter", "复位成功" );
                                        Toast.makeText(ROFilterStatusActivity.this,getString(R.string.rofilter_success)+roWaterPurifier.filterInfo.Filter_A_Percentage,Toast.LENGTH_SHORT).show();
                                        tv_ro_filterRest.setVisibility(View.INVISIBLE);
                                        tv_rolxa.setText(roWaterPurifier.filterInfo.Filter_A_Percentage+"");
                                        tv_rolxb.setText(roWaterPurifier.filterInfo.Filter_B_Percentage+"");
                                        tv_rolxc.setText(roWaterPurifier.filterInfo.Filter_C_Percentage+"");
                                    }

                                    @Override
                                    public void onFailure(Throwable var1) {
                                        Log.e("trfilter", "复位失败" );
                                        Toast.makeText(ROFilterStatusActivity.this,getString(R.string.rofilter_fail),Toast.LENGTH_SHORT).show();
                                        tv_ro_filterRest.setVisibility(View.VISIBLE);
                                        tv_rolxa.setText(fit_a+"");
                                        tv_rolxb.setText(fit_b+"");
                                        tv_rolxc.setText(fit_c+"");
                                    }
                                });
                            } else {
                                tv_ro_filterRest.setVisibility(View.INVISIBLE);
                            }
                        }
                        dialog.cancel();
                    }
                }).show();
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
                            , OznerPreference.getUserToken(ROFilterStatusActivity.this), "zh", "zh");
                    startWebActivity(tapUrl);
                } else {
                    showToastCenter(R.string.userinfo_miss);
                }
                break;
            case 1:
                if (userInfo != null && userInfo.getMobile() != null && !userInfo.getMobile().isEmpty()) {
                    String goldUrl = WeChatUrlUtil.formatFilterGoldSpringUrl(userInfo.getMobile()
                            , OznerPreference.getUserToken(ROFilterStatusActivity.this), "zh", "zh");
                    startWebActivity(goldUrl);
                } else {
                    showToastCenter(R.string.userinfo_miss);
                }
                break;
            case 2:
                if (userInfo != null && userInfo.getMobile() != null && !userInfo.getMobile().isEmpty()) {
                    String cupUrl = WeChatUrlUtil.formatFilterCupUrl(userInfo.getMobile()
                            , OznerPreference.getUserToken(ROFilterStatusActivity.this), "zh", "zh");
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
        Intent filterIntent = new Intent(ROFilterStatusActivity.this, WebActivity.class);
        filterIntent.putExtra(Contacts.PARMS_URL, url);
        startActivity(filterIntent);
    }

//    @Override
//    public void showRemainDay(int day) {
//        tvRemainTime.setText(String.valueOf(day));
//    }

//    @Override
//    public void showRemainPre(int pre) {
//        tvRemainPre.setText(String.valueOf(pre));
//    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SCANNIN_GREQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String scanResult = bundle.getString("result");
                    if (null != scanResult && "" != scanResult) {
//                        reNewFilterTime(deviceType == 0 ? RankType.WaterType : RankType.TapType, scanResult);
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
        HttpMethods.getInstance().reNewFilterTime(OznerPreference.getUserToken(ROFilterStatusActivity.this), mac, type, code,
                new ProgressSubscriber<JsonObject>(ROFilterStatusActivity.this, getString(R.string.loading), false, new OznerHttpResult<JsonObject>() {
                    @Override
                    public void onError(Throwable e) {
                        LCLogUtils.E(TAG, "reNewFilterTime_Ex:" + e.getMessage());
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        LCLogUtils.E(TAG, "reNewFilterTime:" + jsonObject.toString());
                        if (jsonObject != null) {
                            if (jsonObject.get("state").getAsInt() > 0) {
//                                if (deviceType == TYPE_TAP_FILTER) {
////                                    loadTapFilterFromNet();
//                                } else {
////                                    loadWaterFilterNet(mac);
//                                }
                            } else {
                                showToastCenter(ApiException.getErrResId(jsonObject.get("state").getAsInt()));
                            }
                        }
                    }
                }));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (perReqResult != null) {
            perReqResult.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
