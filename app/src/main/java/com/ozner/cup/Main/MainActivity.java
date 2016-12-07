package com.ozner.cup.Main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.ozner.AirPurifier.AirPurifierManager;
import com.ozner.WaterPurifier.WaterPurifierManager;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Bean.OznerBroadcastAction;
import com.ozner.cup.Chat.ChatFragment;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.CupManager;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.UserInfo;
import com.ozner.cup.Device.AirPurifier.AirDeskPurifierFragment;
import com.ozner.cup.Device.AirPurifier.AirVerPurifierFragment;
import com.ozner.cup.Device.Cup.CupFragment;
import com.ozner.cup.Device.DeviceFragment;
import com.ozner.cup.Device.NoDeviceFragment;
import com.ozner.cup.Device.Tap.TapFragment;
import com.ozner.cup.Device.WaterPurifier.WaterPurifierFragment;
import com.ozner.cup.EShop.EShopFragment;
import com.ozner.cup.LoginWelcom.View.LoginActivity;
import com.ozner.cup.MyCenter.MyCenterFragment;
import com.ozner.cup.R;
import com.ozner.cup.Utils.WeChatUrlUtil;
import com.ozner.device.OznerDevice;
import com.ozner.tap.TapManager;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends BaseActivity implements BottomNavigationBar.OnTabSelectedListener, ILeftMenu {
    private static final String TAG = "MainActivity";
    private static final String NoDeviceTag = "nodevicetag";
    @InjectView(R.id.title)
    TextView customTitle;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.bn_bootom_nav_bar)
    BottomNavigationBar bnBootomNavBar;
    @InjectView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    MainReceiver mainMonitor;
    UserInfo userInfo;
    UserInfoManager userInfoManager;
    //以设备类型来保存相应的Fragment
    private HashMap<String, DeviceFragment> devFragmentMap;
    private EShopFragment shopFragment;
    private ChatFragment chatFragment;
    private MyCenterFragment myCenterFragment;
    public Boolean isExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        devFragmentMap = new HashMap<>();
        setDefaultFragment();
        initNavBar();
        setCustomTitle(R.string.device);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();
        initBroadCastFilter();
        String userid = UserDataPreference.GetUserData(this, UserDataPreference.UserId, null);
        if (userid != null && !userid.isEmpty()) {
            Log.e(TAG, "onCreate: userid:" + userid);
            userInfo = DBManager.getInstance(this).getUserInfo(userid);
            userInfoManager = new UserInfoManager(this);
            userInfoManager.loadUserInfo(null);
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            this.finish();
        }

    }

    /**
     * 初始化监听器
     */
    private void initBroadCastFilter() {
        mainMonitor = new MainReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(OznerBroadcastAction.OBA_SWITCH_ESHOP);
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
     * 设置主页的标题
     *
     * @param title
     */
    public void setCustomTitle(String title) {
        try {
            if (!isDestroyed())
                customTitle.setText(title);
        } catch (Exception ex) {

        }
    }

    /**
     * 设置主页的标题
     *
     * @param resId
     */
    public void setCustomTitle(int resId) {
        try {
            if (!isDestroyed())
                customTitle.setText(resId);
        } catch (Exception ex) {

        }
    }

    /**
     * 设置toolbar背景色
     *
     * @param resId
     */
    public void setToolBarColor(int resId) {
        if (!isDestroyed())
            toolbar.setBackgroundColor(ContextCompat.getColor(this, resId));
    }

    /**
     * 初始化底部菜单
     */
    private void initNavBar() {
        bnBootomNavBar.setMode(BottomNavigationBar.MODE_FIXED);
        bnBootomNavBar.setActiveColor(R.color.colorAccent);
        bnBootomNavBar
                .addItem(new BottomNavigationItem(R.drawable.tab_device_selector, getString(R.string.device)))
                .addItem(new BottomNavigationItem(R.drawable.tab_shop_selector, getString(R.string.eshop)))
                .addItem(new BottomNavigationItem(R.drawable.tab_msg_selector, getString(R.string.chat)))
                .addItem(new BottomNavigationItem(R.drawable.tab_my_selector, getString(R.string.mine)))
                .initialise();
        bnBootomNavBar.setTabSelectedListener(this);
        if (Build.VERSION.SDK_INT >= 21) {
            bnBootomNavBar.setElevation(0);
        } else {
            bnBootomNavBar.selectTab(0);
        }
    }

    /**
     * 设置默认页面
     */
    private void setDefaultFragment() {
        FragmentManager fm = getSupportFragmentManager();
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
    public void onDeviceItemClick(OznerDevice device, String mac) {
        closeLeftMenu();
        UserDataPreference.SetUserData(this, UserDataPreference.SelMac, mac);//保存选中的设备mac
        Log.e(TAG, "onDeviceItemClick: " + mac);
        bnBootomNavBar.selectTab(0, true);
    }

    /**
     * 根据设备，获取相应的Fragment
     *
     * @param device
     *
     * @return
     */
    private DeviceFragment getDeviceFragment(OznerDevice device) {
        if (device != null) {
            if (CupManager.IsCup(device.Type())) {
                return CupFragment.newInstance(device.Address());
            } else if (TapManager.IsTap(device.Type())) {
                return TapFragment.newInstance(device.Address());
            } else if (WaterPurifierManager.IsWaterPurifier(device.Type())) {
                return WaterPurifierFragment.newInstance(device.Address());
            } else if (AirPurifierManager.IsWifiAirPurifier(device.Type())) {
                return AirVerPurifierFragment.newInstance(device.Address());
            } else if (AirPurifierManager.IsBluetoothAirPurifier(device.Type())) {
                return AirDeskPurifierFragment.newInstance(device.Address());
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
            if (devFragmentMap.containsKey(device.Type())) {
                DeviceFragment df = devFragmentMap.get(device.Type());
                trans.replace(R.id.fg_content, df);
                df.setDevice(device);
            } else {
                DeviceFragment newDef = getDeviceFragment(device);
                devFragmentMap.put(device.Type(), newDef);
                trans.replace(R.id.fg_content, newDef);
            }
        } else {
            Log.e(TAG, "showDevice: NoDeviceTag");
            trans.replace(R.id.fg_content, devFragmentMap.get(NoDeviceTag));
        }
        trans.commitAllowingStateLoss();
    }

    @Override
    public void onTabSelected(int position) {
        System.gc();
        Log.d(TAG, "onTabSelected() called with: " + "position = [" + position + "]");
        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        switch (position) {
            case 0://设备
                showDevice();
                break;
            case 1:
                if (shopFragment == null) {
                    Bundle bundle = new Bundle();
                    String eshop_url = WeChatUrlUtil.getMallUrl("18655591525", OznerPreference.getUserToken(this), "zh", "zh");
                    bundle.putString(Contacts.PARMS_URL, eshop_url);
                    shopFragment = EShopFragment.newInstance(bundle);

                }
                transaction.replace(R.id.fg_content, shopFragment).commitAllowingStateLoss();

                break;
            case 2:
                if (chatFragment == null) {
                    chatFragment = ChatFragment.newInstance(null);
                }
                transaction.replace(R.id.fg_content, chatFragment).commitAllowingStateLoss();
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

    class MainReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case OznerBroadcastAction.OBA_SWITCH_ESHOP:
                    //这里不用延时的话没有效果
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            bnBootomNavBar.selectTab(1);
                        }
                    }, 300);
                    break;
            }
        }
    }
}
