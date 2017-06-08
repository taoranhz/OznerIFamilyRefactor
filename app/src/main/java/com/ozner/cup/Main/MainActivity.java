package com.ozner.cup.Main;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BadgeItem;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.github.kayvannj.permission_utils.Func;
import com.github.kayvannj.permission_utils.PermissionUtil;
import com.google.gson.JsonObject;
import com.ozner.AirPurifier.AirPurifierManager;
import com.ozner.WaterPurifier.WaterPurifierManager;
import com.ozner.WaterReplenishmentMeter.WaterReplenishmentMeterMgr;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Bean.OznerBroadcastAction;
import com.ozner.cup.Bean.RankType;
import com.ozner.cup.Chat.EaseChatFragment;
import com.ozner.cup.Command.CenterNotification;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.CupManager;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.OznerDeviceSettings;
import com.ozner.cup.DBHelper.UserInfo;
import com.ozner.cup.Device.AirPurifier.AirDeskPurifierFragment;
import com.ozner.cup.Device.AirPurifier.AirVerPurifierFragment;
import com.ozner.cup.Device.Cup.CupFragment;
import com.ozner.cup.Device.DeviceFragment;
import com.ozner.cup.Device.NoDeviceFragment;
import com.ozner.cup.Device.ROWaterPurifier.ROWaterPurifierFragment;
import com.ozner.cup.Device.ReplenWater.ReplenWaterFragment;
import com.ozner.cup.Device.Tap.TapFragment;
import com.ozner.cup.Device.WaterPurifier.WPContainerFragment;
import com.ozner.cup.EShop.EShopFragment;
import com.ozner.cup.HttpHelper.HttpMethods;
import com.ozner.cup.LoginWelcom.View.LoginActivity;
import com.ozner.cup.MyCenter.MyCenterFragment;
import com.ozner.cup.MyCenter.MyFriend.FriendInfoManager;
import com.ozner.cup.MyCenter.MyFriend.bean.VerifyMessageItem;
import com.ozner.cup.MyCenter.OznerUpdateManager;
import com.ozner.cup.R;
import com.ozner.cup.Utils.LCLogUtils;
import com.ozner.cup.Utils.MobileInfoUtil;
import com.ozner.cup.Utils.WeChatUrlUtil;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;
import com.ozner.tap.TapManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.jpush.android.api.JPushInterface;
import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import cn.udesk.config.UdeskBaseInfo;
import cn.udesk.config.UdeskConfig;
import cn.udesk.presenter.ChatActivityPresenter;
import rx.Subscriber;
import udesk.core.UdeskCallBack;

import static cn.udesk.config.UdeskBaseInfo.sdkToken;

public class MainActivity extends BaseActivity implements BottomNavigationBar.OnTabSelectedListener, ILeftMenu {
    private static final String TAG = "MainActivity";
    private static final String NoDeviceTag = "nodevicetag";
    private String UDESK_DOMAIN = "ozner.udesk.cn";
    private String AppId = "f633b561471be762";
    private String UDESK_SECRETKEY = "4ddf84becfd2320bca9f183136574c0f";
    //    @InjectView(R.id.title)
//    TextView customTitle;
//    @InjectView(R.id.toolbar)
//    Toolbar toolbar;
    @InjectView(R.id.bn_bootom_nav_bar)
    BottomNavigationBar bnBootomNavBar;
    @InjectView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @InjectView(R.id.llay_bottom)
    LinearLayout llayBottom;

    MainReceiver mainMonitor;
    UserInfo userInfo;
    UserInfoManager userInfoManager;

    //以设备类型来保存相应的Fragment
    private HashMap<String, DeviceFragment> devFragmentMap;
    private EShopFragment shopFragment;
    private EaseChatFragment chatFragment;
    private MyCenterFragment myCenterFragment;
    public Boolean isExit = false;
    private int curBottomIndex = 0;
    private String mUserid;
    private BadgeItem centerBadge;
    private PermissionUtil.PermissionRequestObject perReqResult;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        devFragmentMap = new HashMap<>();
        setDefaultFragment();
        initNavBar();
//        refreshBottomBadge(0);
        initBroadCastFilter();
        mUserid = OznerPreference.GetValue(this, OznerPreference.UserId, null);
        if (mUserid != null && !mUserid.isEmpty()) {
            Log.e(TAG, "onCreate: mUserid:" + mUserid + ",usertoken:" + OznerPreference.getUserToken(this));
            userInfo = DBManager.getInstance(this).getUserInfo(mUserid);
            userInfoManager = new UserInfoManager(this);
            userInfoManager.loadUserInfo(null);
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            this.finish();
        }
//        new OznerUpdateManager(this, false).checkUpdate();
        //启动百度云推送
        PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY, getString(R.string.Baidu_Push_ApiKey));
        checkUserVerifyMsg();
        //隐藏底部菜单
//        hideBottomNav();
        OznerDeviceManager.Instance().setOwner(mUserid, OznerPreference.getUserToken(this));
//        //检查位置权限
//        checkPosPer();

        LCLogUtils.E(TAG, "邮箱登录方式:" + UserDataPreference.isLoginEmail(this));
        if (UserDataPreference.isLoginEmail(this)) {
            initLoginEmail();
        }

        checkUpdate();
        initChat();
        LCLogUtils.I("ozner", "oldDeviceSize:" + OznerDeviceManager.Instance().getDevices().length);
    }

    private void initChat() {
        UdeskSDKManager.getInstance().initApiKey(this, UDESK_DOMAIN, UDESK_SECRETKEY, AppId);
        Map<String, String> info = new HashMap<String, String>();
//        if (TextUtils.isEmpty(OznerPreference.getUserToken(this))) {
//            sdkToken = UUID.randomUUID().toString();
//        }
        info.put(UdeskConst.UdeskUserInfo.USER_SDK_TOKEN, OznerPreference.getUserToken(this));
        info.put(UdeskConst.UdeskUserInfo.NICK_NAME, userInfo.getNickname());

        if (!TextUtils.isEmpty(userInfo.getMobile()))
            info.put(UdeskConst.UdeskUserInfo.CELLPHONE, userInfo.getMobile());
        if (!TextUtils.isEmpty(userInfo.getEmail()))
            info.put(UdeskConst.UdeskUserInfo.EMAIL, userInfo.getEmail());
        UdeskSDKManager.getInstance().setUserInfo(
                getApplicationContext(), OznerPreference.getUserToken(this), info);
//        UdeskSDKManager.getInstance().setUpdateUserinfo(info);
//        UdeskSDKManager.getInstance().setSdkPushStatus(true);

        UdeskSDKManager.getInstance().setCustomerUrl(userInfo.getHeadimg());

//        try {
//            ChatActivityPresenter.class.newInstance().onCreateCustomer();
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }

    }

//    /**
//     * 检查位置权限
//     */
//    private void checkPosPer() {
//        perReqResult = PermissionUtil.with(this).request(Manifest.permission.ACCESS_COARSE_LOCATION).ask(2);
//    }

    /**
     * 检查更新
     */
    private void checkUpdate() {
        perReqResult = PermissionUtil.with(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onAllGranted(new Func() {
                    @Override
                    protected void call() {
                        new OznerUpdateManager(MainActivity.this, false).checkUpdate();
                    }
                }).onAnyDenied(new Func() {
                    @Override
                    protected void call() {
                        showToastCenter(R.string.user_deny_write_storge);
                    }
                }).ask(1);
    }


    private void initLoginEmail() {
        //隐藏底部菜单
        hideBottomNav();
    }


    /**
     * 初始化toolbar切换按钮
     *
     * @param toolbar
     */
    public void initActionBarToggle(Toolbar toolbar) {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();
    }

    /**
     * 隐藏底部菜单
     */
    public void hideBottomNav() {
        try {
//            if (!bnBootomNavBar.isHidden()) {
//                bnBootomNavBar.hide();//隐藏
//                bnBootomNavBar.hide(false);//隐藏是否启动动画，这里并不能自定义动画
//            }
            llayBottom.setVisibility(View.GONE);
        } catch (Exception ex) {
            Log.e(TAG, "hideBottomNav_Ex: " + ex.getMessage());
        }
    }

    /**
     * 显示底部菜单
     */

    public void showBottomNav() {
        try {
//            if (!bnBootomNavBar.isShown()) {
//                bnBootomNavBar.show();//显示
//                bnBootomNavBar.show(true);//隐藏是否启动动画，这里并不能自定义动画
//            }
            llayBottom.setVisibility(View.VISIBLE);
        } catch (Exception ex) {
            Log.e(TAG, "showBottomNav_Ex: " + ex.getMessage());
        }
    }

    /**
     * 初始化监听器
     */

    private void initBroadCastFilter() {
        mainMonitor = new MainReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(OznerBroadcastAction.OBA_SWITCH_ESHOP);
//        filter.addAction(OznerBroadcastAction.OBA_SWITCH_CHAT);
        filter.addAction(OznerBroadcastAction.OBA_BDBind);
        filter.addAction(OznerBroadcastAction.OBA_Login_Notify);
        filter.addAction(OznerBroadcastAction.OBA_NewFriendVF);
        filter.addAction(OznerBroadcastAction.OBA_NewRank);
        filter.addAction(OznerBroadcastAction.OBA_NewCenterMsg);
        this.registerReceiver(mainMonitor, filter);
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(mainMonitor);
        } catch (Exception ex) {

        }
        super.onDestroy();
    }

    /**
     * 初始化底部菜单
     */
    private void initNavBar() {
        bnBootomNavBar.setMode(BottomNavigationBar.MODE_FIXED);
        bnBootomNavBar.setActiveColor(R.color.colorAccent);
        centerBadge = new BadgeItem()
                .setBorderWidth(2)//Badge的Border(边界)宽度
//                .setBorderColor(ContextCompat.getColor(this, R.color.err_red))//Badge的Border颜色
                .setBackgroundColor(ContextCompat.getColor(this, R.color.err_red))//Badge背景颜色
                .setGravity(Gravity.RIGHT | Gravity.TOP)//位置，默认右上角
//                .setText(String.valueOf(getCenterMsgCount()))//显示的文本
                .setTextColor(ContextCompat.getColor(this, R.color.white));//文本颜色
//                .setAnimationDuration(2000)
//                .setHideOnSelect(true);//当选中状态时消失，非选中状态显示

        bnBootomNavBar
                .addItem(new BottomNavigationItem(R.drawable.tab_device_selector, getString(R.string.device)))
                .addItem(new BottomNavigationItem(R.drawable.tab_shop_selector, getString(R.string.eshop)))
                .addItem(new BottomNavigationItem(R.drawable.tab_msg_selector, getString(R.string.chat)))
                .addItem(new BottomNavigationItem(R.drawable.tab_my_selector, getString(R.string.mine)).setBadgeItem(centerBadge))
//                .addItem(new BottomNavigationItem(R.drawable.tab_my_selector, getString(R.string.mine)))
                .initialise();


        bnBootomNavBar.setTabSelectedListener(this);
        if (Build.VERSION.SDK_INT >= 21) {
            bnBootomNavBar.setElevation(0);
        }
        bnBootomNavBar.selectTab(0);
    }


    /**
     * 设置“我的”底部数字
     *
     * @param msgTip
     */
    public void setNewCenterMsgTip(int msgTip) {
        LCLogUtils.E(TAG, "setNewCenterMsgTip:" + msgTip);
        if (msgTip > 0) {
            centerBadge.setText("N");
            centerBadge.show();
        } else {
            centerBadge.hide();
        }
    }

    private void checkUserVerifyMsg() {
        FriendInfoManager infoManager = new FriendInfoManager(this, null);
        infoManager.getVerifyMessageNoDialog(new FriendInfoManager.LoadVerifyListener() {
            @Override
            public void onSuccess(List<VerifyMessageItem> result) {
                int waitNum = 0;
                for (VerifyMessageItem item : result) {
                    if (item.getStatus() != 2) {
                        waitNum++;
                    }
                }
                if (waitNum > 0) {
                    CenterNotification.setCenterNotify(MainActivity.this, CenterNotification.NewFriendVF);
                } else {
                    CenterNotification.resetCenterNotify(MainActivity.this, CenterNotification.DealNewFriendVF);
                }
                setNewCenterMsgTip(CenterNotification.getCenterNotifyState(MainActivity.this));
            }

            @Override
            public void onFail(String msg) {
                setNewCenterMsgTip(CenterNotification.getCenterNotifyState(MainActivity.this));
            }
        });
    }

    @Override
    protected void onResume() {
        try {
            setNewCenterMsgTip(CenterNotification.getCenterNotifyState(MainActivity.this));
        } catch (Exception ex) {

        }
        if (bnBootomNavBar.getCurrentSelectedPosition() == 2) {
            bnBootomNavBar.selectTab(0);
        }
        super.onResume();
    }

    /**
     * 设置默认页面
     */

    private void setDefaultFragment() {
        NoDeviceFragment noDeviceFragment = new NoDeviceFragment();
        devFragmentMap.put(NoDeviceTag, noDeviceFragment);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            exit();
        }
    }

    //退出程序
    private void exit() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出
            Toast.makeText(this, getString(R.string.PressToExit), Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

        } else {
            finish();
        }
    }

    public void closeLeftMenu() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    /**
     * 侧边栏设备列表点击
     *
     * @param device
     * @param mac
     */
    @Override
    public void onDeviceItemClick(OznerDevice device, String mac, boolean isAuto) {
        if (!isAuto || curBottomIndex == 0) {
            closeLeftMenu();
            UserDataPreference.SetUserData(this, UserDataPreference.SelMac, mac);//保存选中的设备mac
            Log.e(TAG, "onDeviceItemClick: " + mac);
            bnBootomNavBar.selectTab(0, true);
        }
    }

    /**
     * 根据设备，获取相应的Fragment
     *
     * @param device
     * @return
     */
    private DeviceFragment getDeviceFragment(OznerDeviceSettings device) {
        if (device != null) {
            if (CupManager.IsCup(device.getDevcieType())) {
                return CupFragment.newInstance(device.getMac());
            } else if (TapManager.IsTap(device.getDevcieType())) {
                return TapFragment.newInstance(device.getMac());
            } else if (device.getDevcieType().equals(RankType.TdsPenType)) {
                return TapFragment.newInstance(device.getMac());
            } else if (WaterPurifierManager.IsWaterPurifier(device.getDevcieType())) {
                if ("Ozner RO".equals(device.getDevcieType())) {
                    return ROWaterPurifierFragment.newInstance(device.getMac());
                } else {
//                    return WaterPurifierFragment.newInstance(device.getMac());
                    return WPContainerFragment.newInstance(device.getMac());
                }
            } else if (AirPurifierManager.IsWifiAirPurifier(device.getDevcieType())) {
                return AirVerPurifierFragment.newInstance(device.getMac());
            } else if (AirPurifierManager.IsBluetoothAirPurifier(device.getDevcieType())) {
                return AirDeskPurifierFragment.newInstance(device.getMac());
            } else if (WaterReplenishmentMeterMgr.IsWaterReplenishmentMeter(device.getDevcieType())) {
                return ReplenWaterFragment.newInstance(device.getMac());
            } else {
                return NoDeviceFragment.newInstance();
            }
        } else {
            return NoDeviceFragment.newInstance();
        }
    }

    /**
     * 显示设备
     */
    private void showDevice() {
        FragmentTransaction trans = this.getSupportFragmentManager().beginTransaction();
        OznerDevice device = ((LeftMenuFragment) (getSupportFragmentManager().findFragmentById(R.id.fg_left_menu))).getSelectedDevice();
        if (device != null) {
            Log.e(TAG, "showDevice: " + device.Address() + " , deviceType:" + device.Type());
            if (devFragmentMap.containsKey(device.Address())) {
                DeviceFragment df = devFragmentMap.get(device.Address());
                df.setDevice(device);
                trans.replace(R.id.fg_content, df);
            } else {
                OznerDeviceSettings oznerSetting = DBManager.getInstance(MainActivity.this).getDeviceSettings(mUserid, device.Address());
                if (oznerSetting != null) {
                    DeviceFragment newDef = getDeviceFragment(oznerSetting);
                    devFragmentMap.put(device.Address(), newDef);
                    trans.replace(R.id.fg_content, newDef);
                } else {
                    trans.replace(R.id.fg_content, devFragmentMap.get(NoDeviceTag));
                }

//                DeviceFragment newDef = getDeviceFragment(device);
//                devFragmentMap.put(device.Address(), newDef);
//                trans.replace(R.id.fg_content, newDef);
            }
        } else {
            Log.e(TAG, "showDevice: NoDeviceTag");
            trans.replace(R.id.fg_content, devFragmentMap.get(NoDeviceTag));
        }
        trans.commitAllowingStateLoss();
    }

    @Override
    public void onTabSelected(int position) {
        curBottomIndex = position;
        System.gc();
        Log.e(TAG, "onTabSelected() called with: " + "position = [" + position + "]");
        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        if (2 != position) {
            hideKeyboard();
        }

        if (3 != position) {
            setNewCenterMsgTip(CenterNotification.getCenterNotifyState(MainActivity.this));
        }

        switch (position) {
            case 0://设备
                showDevice();
                break;
            case 1:
                if (shopFragment == null) {
                    Bundle bundle = new Bundle();
                    userInfo = DBManager.getInstance(this).getUserInfo(mUserid);
                    String eshop_url = WeChatUrlUtil.getMallUrl(userInfo.getMobile(), OznerPreference.getUserToken(this), "zh", "zh");
                    bundle.putString(Contacts.PARMS_URL, eshop_url);
                    shopFragment = EShopFragment.newInstance(bundle);

                }
                transaction.replace(R.id.fg_content, shopFragment).commitAllowingStateLoss();

                break;
            case 2:
//                if (chatFragment == null) {
//                    chatFragment = EaseChatFragment.newInstance(null);
//                }
//                transaction.replace(R.id.fg_content, chatFragment).commitAllowingStateLoss();
                //设置在客服那边显示用户名
                UdeskSDKManager.getInstance().toLanuchChatAcitvity(this);
//                Map uInfo = new HashMap();
//                if (!TextUtils.isEmpty(userInfo.getNickname())) {
//                    uInfo.put(UdeskConst.UdeskUserInfo.NICK_NAME, userInfo.getNickname());
//                }
//                if (!TextUtils.isEmpty(userInfo.getMobile()))
//                    uInfo.put(UdeskConst.UdeskUserInfo.CELLPHONE, userInfo.getMobile());
//                if (!TextUtils.isEmpty(userInfo.getEmail()))
//                    uInfo.put(UdeskConst.UdeskUserInfo.EMAIL, userInfo.getEmail());
//                UdeskSDKManager.getInstance().setUpdateUserinfo(uInfo);

                break;
            case 3:
                if (myCenterFragment == null) {
                    myCenterFragment = MyCenterFragment.newInstance(null);
                }
                transaction.replace(R.id.fg_content, myCenterFragment).commitAllowingStateLoss();
                break;
        }

    }

    @Override
    public void onTabUnselected(int position) {
        Log.d(TAG, "onTabUnselected() called with: " + "position = [" + position + "]");
    }

    @Override
    public void onTabReselected(int position) {
        if (position == 0) {
            showDevice();
        }
    }


    /**
     * hide
     */
    protected void hideKeyboard() {
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

        }
    }

    /**
     * 转移保存设备
     */
    private void transfeSaveDevice() {

    }

    class MainReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case OznerBroadcastAction.OBA_SWITCH_ESHOP://切换到商城
                    //这里不用延时的话没有效果
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            bnBootomNavBar.selectTab(1);
                        }
                    }, 100);
                    break;
//                case OznerBroadcastAction.OBA_SWITCH_CHAT://切换到咨询
//                    Log.e(TAG, "onReceive: 切换到咨询");
//                    //这里不用延时的话没有效果
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            bnBootomNavBar.selectTab(2);
//                        }
//                    }, 100);
//                    break;
                case OznerBroadcastAction.OBA_BDBind:
                    String usertoken = OznerPreference.getUserToken(MainActivity.this);
                    String deviceid = OznerPreference.GetValue(MainActivity.this, OznerPreference.BDDeivceID, "");
                    HttpMethods.getInstance().updateUserInfoBD(usertoken, deviceid, new Subscriber<JsonObject>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "onError: " + e.getMessage());
                        }

                        @Override
                        public void onNext(JsonObject jsonObject) {
                            Log.e(TAG, "onNext: " + jsonObject.toString());
                            int state = jsonObject.get("state").getAsInt();
                            if (state > 0) {
                                Log.e(TAG, "onNext: success");
                                OznerPreference.SetValue(MainActivity.this, OznerPreference.ISBDBind, String.valueOf(true));
                            } else {
                                if (jsonObject.get("state").getAsInt() == -10006
                                        || jsonObject.get("state").getAsInt() == -10007) {
                                    BaseActivity.reLogin(MainActivity.this);
                                } else {
                                    if (!UserDataPreference.isLoginEmail(MainActivity.this))
                                        showToastCenter(R.string.bd_bind_fail);
//                                    Toast.makeText(MainActivity.this, "百度推送绑定失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                    break;
                case OznerBroadcastAction.OBA_Login_Notify://登录通知
                    String loginUserid = intent.getStringExtra(Contacts.PARMS_LOGIN_USERID);
                    String loginToken = intent.getStringExtra(Contacts.PARMS_LOGIN_TOKEN);
                    String miei = intent.getStringExtra(Contacts.PARMS_LOGIN_MIEI);
                    if (miei != null
                            && loginToken != null
                            && loginUserid != null
                            && !miei.equals(MobileInfoUtil.getImie(MainActivity.this))
                            && loginUserid.equals(OznerPreference.GetValue(MainActivity.this, OznerPreference.UserId, null))
                            && !loginToken.endsWith(OznerPreference.getUserToken(MainActivity.this))) {
                        BaseActivity.reLogin(MainActivity.this);
                    }
                    break;
                case OznerBroadcastAction.OBA_NewFriendVF:
                    CenterNotification.setCenterNotify(getBaseContext(), CenterNotification.NewFriendVF);
                    setNewCenterMsgTip(CenterNotification.getCenterNotifyState(MainActivity.this));
                    break;
                case OznerBroadcastAction.OBA_NewCenterMsg:
                    CenterNotification.setCenterNotify(getBaseContext(), CenterNotification.NewMessage);
                    setNewCenterMsgTip(CenterNotification.getCenterNotifyState(MainActivity.this));
                    break;
                case OznerBroadcastAction.OBA_NewRank:
                    CenterNotification.setCenterNotify(getBaseContext(), CenterNotification.NewRank);
                    setNewCenterMsgTip(CenterNotification.getCenterNotifyState(MainActivity.this));
                    break;
                case OznerBroadcastAction.OBA_NewFriend:
                    CenterNotification.setCenterNotify(getBaseContext(), CenterNotification.NewFriend);
                    setNewCenterMsgTip(CenterNotification.getCenterNotifyState(MainActivity.this));
                    break;
            }
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
