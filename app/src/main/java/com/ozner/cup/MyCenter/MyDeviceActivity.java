package com.ozner.cup.MyCenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.ozner.AirPurifier.AirPurifierManager;
import com.ozner.WaterPurifier.WaterPurifierManager;
import com.ozner.WaterReplenishmentMeter.WaterReplenishmentMeterMgr;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Base.CommonAdapter;
import com.ozner.cup.Base.CommonViewHolder;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Bean.OznerBroadcastAction;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.CupManager;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.OznerDeviceSettings;
import com.ozner.cup.Main.Bean.LeftMenuDeviceItem;
import com.ozner.cup.R;
import com.ozner.cup.Utils.LCLogUtils;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OznerDeviceManager;
import com.ozner.tap.TapManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MyDeviceActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "MyDeviceActivity";

    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.gv_device)
    GridView gvDevice;
    @InjectView(R.id.tv_nodevice)
    TextView tvNodevice;

    private DeviceAdapter mAdapter;
    private DeviceMonitor mMonitor;
    //    List<OznerDevice> deviceList;
    List<LeftMenuDeviceItem> mDeviceList;
    private String mUserid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_device);
        ButterKnife.inject(this);
        mDeviceList = new ArrayList<>();
        mUserid = UserDataPreference.GetUserData(this, UserDataPreference.UserId, "");
        initToolBar();
        mAdapter = new DeviceAdapter(this, R.layout.my_device_item);
        gvDevice.setEmptyView(tvNodevice);
        gvDevice.setAdapter(mAdapter);
        gvDevice.setOnItemClickListener(this);
        refreshDeviceData();
        initBroadCastFilter();
    }

    private void initBroadCastFilter() {
        mMonitor = new DeviceMonitor();
        IntentFilter filter = new IntentFilter();
        filter.addAction(OznerDeviceManager.ACTION_OZNER_MANAGER_DEVICE_ADD);
        filter.addAction(OznerDeviceManager.ACTION_OZNER_MANAGER_DEVICE_REMOVE);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_CONNECTED);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_CONNECTING);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_DISCONNECTED);
        registerReceiver(mMonitor, filter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mMonitor);
        super.onDestroy();
    }

    private void initToolBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        title.setText(R.string.my_device);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setBackgroundColor(Color.WHITE);
        title.setTextColor(Color.BLACK);
    }

    private void refreshDeviceData() {
//        OznerDevice[] devices = OznerDeviceManager.Instance().getDevices();
//        deviceList = Arrays.asList(devices);
//        mAdapter.loadData(deviceList);

        if (OznerDeviceManager.Instance() == null) {
            return;
        }
        if (OznerDeviceManager.Instance().getDevices() == null) {
            return;
        }

        try {
            List<OznerDeviceSettings> oznerSettings = DBManager.getInstance(this).getDeviceSettingList(mUserid);

            mDeviceList.clear();
            int settingCount = oznerSettings.size();
            for (int i = 0; i < settingCount; i++) {
                LeftMenuDeviceItem item = new LeftMenuDeviceItem();
                item.setName(oznerSettings.get(i).getName());
                item.setUsePos(oznerSettings.get(i).getDevicePosition());
                item.setMac(oznerSettings.get(i).getMac());
                item.setType(oznerSettings.get(i).getDevcieType());
                item.setDevice(OznerDeviceManager.Instance().getDevice(oznerSettings.get(i).getMac()));
                mDeviceList.add(item);
            }
            mAdapter.loadData(mDeviceList);
        } catch (Exception ex) {
            LCLogUtils.E(TAG, "refreshDeviceData_Ex:" + ex.getMessage());
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            if (mDeviceList != null && !mDeviceList.isEmpty()) {
                Intent deviceIntent = new Intent(OznerBroadcastAction.OBA_CenterDeviceSelect);
                deviceIntent.putExtra(Contacts.PARMS_MAC, mDeviceList.get(position).getMac());
                sendBroadcast(deviceIntent);
                this.finish();
            } else {
                showToastCenter(R.string.device_unexsit);
            }
        } catch (Exception ex) {
            LCLogUtils.E(TAG, "onItemClick_Ex:" + ex.getMessage());
            showToastCenter(ex.getMessage());
        }
    }

    class DeviceMonitor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshDeviceData();
        }
    }

    class DeviceAdapter extends CommonAdapter<LeftMenuDeviceItem> {

        public DeviceAdapter(Context context, int itemLayoutId) {
            super(context, itemLayoutId);
        }

        @Override
        public void convert(CommonViewHolder holder, LeftMenuDeviceItem item, int position) {
            holder.setText(R.id.tv_deviceName, item.getName());

//            if (item.getDevice() == null) {
//                DBManager.getInstance(mContext).deleteDeviceSettings(mUserid, item.getMac());
//                refreshDeviceData();
//            } else {
                if (CupManager.IsCup(item.getType())) {
                    if (item.getDevice().connectStatus().equals(BaseDeviceIO.ConnectStatus.Connected)) {
                        holder.setImageResource(R.id.iv_deviceIcon, R.mipmap.icon_cup_on);
                    } else {
                        holder.setImageResource(R.id.iv_deviceIcon, R.drawable.my_center_cup_gray);
                    }
                } else if (TapManager.IsTap(item.getType())) {
                    if (item.getDevice().connectStatus().equals(BaseDeviceIO.ConnectStatus.Connected)) {
                        holder.setImageResource(R.id.iv_deviceIcon, R.mipmap.icon_tap_on);
                    } else {
                        holder.setImageResource(R.id.iv_deviceIcon, R.drawable.my_center_tap_gray);
                    }
                } else if (WaterPurifierManager.IsWaterPurifier(item.getType())) {
                    if (item.getDevice().connectStatus().equals(BaseDeviceIO.ConnectStatus.Connected)) {
                        holder.setImageResource(R.id.iv_deviceIcon, R.mipmap.icon_water_purifier_on);
                    } else {
                        holder.setImageResource(R.id.iv_deviceIcon, R.drawable.my_center_purifier_gray);
                    }
                } else if (AirPurifierManager.IsBluetoothAirPurifier(item.getType())) {
                    if (item.getDevice().connectStatus().equals(BaseDeviceIO.ConnectStatus.Connected)) {
                        holder.setImageResource(R.id.iv_deviceIcon, R.mipmap.icon_air_purifier_desk_on);
                    } else {
                        holder.setImageResource(R.id.iv_deviceIcon, R.drawable.my_center_air_desk_gray);
                    }
                } else if (AirPurifierManager.IsWifiAirPurifier(item.getType())) {
                    if (item.getDevice().connectStatus().equals(BaseDeviceIO.ConnectStatus.Connected)) {
                        holder.setImageResource(R.id.iv_deviceIcon, R.mipmap.icon_air_purifier_ver_on);
                    } else {
                        holder.setImageResource(R.id.iv_deviceIcon, R.drawable.my_center_air_ver_gray);
                    }
                } else if (WaterReplenishmentMeterMgr.IsWaterReplenishmentMeter(item.getType())) {
                    if (item.getDevice().connectStatus().equals(BaseDeviceIO.ConnectStatus.Connected)) {
                        holder.setImageResource(R.id.iv_deviceIcon, R.mipmap.icon_replen_on);
                    } else {
                        holder.setImageResource(R.id.iv_deviceIcon, R.drawable.my_center_wrm_gray);
                    }
                }
//            }
        }
    }
}
