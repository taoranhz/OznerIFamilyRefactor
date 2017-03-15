package com.ozner.cup.Main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ozner.AirPurifier.AirPurifierManager;
import com.ozner.WaterPurifier.WaterPurifierManager;
import com.ozner.WaterReplenishmentMeter.WaterReplenishmentMeterMgr;
import com.ozner.cup.Base.BaseFragment;
import com.ozner.cup.Base.CommonAdapter;
import com.ozner.cup.Base.CommonViewHolder;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Bean.OznerBroadcastAction;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.CupManager;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.OznerDeviceSettings;
import com.ozner.cup.Device.AddDevice.AddDeviceActivity;
import com.ozner.cup.Main.Bean.LeftMenuDeviceItem;
import com.ozner.cup.R;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;
import com.ozner.tap.TapManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class LeftMenuFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    private static final String TAG = "LeftMenuFragment";
    @InjectView(R.id.lv_myDevice)
    ListView lvMyDevice;
    @InjectView(R.id.ib_addDevice)
    ImageButton ibAddDevice;
    @InjectView(R.id.llay_hasDevice)
    LinearLayout llayHasDevice;
    @InjectView(R.id.iv_left_center)
    ImageView ivLeftCenter;
    @InjectView(R.id.llay_hasNoDevice)
    LinearLayout llayHasNoDevice;
    @InjectView(R.id.llay_root)
    LinearLayout llayRoot;

    private LeftMonitor mMonitor;
    private LeftMenuAdapter mLeftAdapter;
    private List<LeftMenuDeviceItem> leftDeviceList;
    private String mUserid;

    public LeftMenuFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserid = UserDataPreference.GetUserData(getContext(), UserDataPreference.UserId, "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_left_menu, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        leftDeviceList = new ArrayList<>();
        mLeftAdapter = new LeftMenuAdapter(getContext(), R.layout.left_menu_item);
        lvMyDevice.setAdapter(mLeftAdapter);
        lvMyDevice.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvMyDevice.setOnItemClickListener(this);

        initBroadCastFilter();
    }


    /**
     * 初始化设备列表
     */
    private void initDataList() {

        loadDeviceList();
        showDatalist(mLeftAdapter.getCount() > 0);
    }

    /**
     * 选择设备
     *
     * @param position
     */
    public void selectDevice(int position, boolean isAuto) {
//        if (!mLeftAdapter.getItem(position).Address().equals(UserDataPreference.GetUserData(getContext(), UserDataPreference.SelMac, ""))) {
//            UserDataPreference.SetUserData(getContext(), UserDataPreference.SelMac, mLeftAdapter.getItem(position).Address());
        mLeftAdapter.setSelectPosition(position);
        if (position >= 0 && position < lvMyDevice.getCount()) {
            lvMyDevice.setItemChecked(position, true);
            ((MainActivity) getActivity()).onDeviceItemClick(mLeftAdapter.getItem(position).getDevice(), mLeftAdapter.getItem(position).getMac(), isAuto);
        }
//        }
    }

    public OznerDevice getSelectedDevice() {
        if (mLeftAdapter != null && mLeftAdapter.getCount() > 0) {
            return mLeftAdapter.getSelectedItem().getDevice();
        } else {
            return null;
        }
    }

    /**
     * 初始化广播接收器
     */
    private void initBroadCastFilter() {
        mMonitor = new LeftMonitor();
        IntentFilter filter = new IntentFilter();
        filter.addAction(OznerBroadcastAction.OBA_CenterDeviceSelect);
        filter.addAction(OznerBroadcastAction.OBA_Service_Init);
        filter.addAction(OznerDeviceManager.ACTION_OZNER_MANAGER_DEVICE_ADD);
        filter.addAction(OznerDeviceManager.ACTION_OZNER_MANAGER_DEVICE_CHANGE);
        filter.addAction(OznerDeviceManager.ACTION_OZNER_MANAGER_DEVICE_REMOVE);
        filter.addAction(OznerDeviceManager.ACTION_OZNER_MANAGER_OWNER_CHANGE);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_CONNECTED);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_CONNECTING);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_DISCONNECTED);
        getActivity().registerReceiver(mMonitor, filter);
    }

    /**
     * 切换侧边栏菜单显示内容
     *
     * @param hasData
     */
    private void showDatalist(boolean hasData) {
        if (hasData) {
            //有设备
            llayHasNoDevice.setVisibility(View.GONE);
            llayHasDevice.setVisibility(View.VISIBLE);
            llayRoot.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.left_menu_data_bg));
        } else {
            //无设备
            llayHasDevice.setVisibility(View.GONE);
            llayHasNoDevice.setVisibility(View.VISIBLE);
            llayRoot.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.theme_blue));
            Glide.with(this).load(R.drawable.left_center).asBitmap().into(ivLeftCenter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initDataList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        getActivity().unregisterReceiver(mMonitor);
    }

    @OnClick(R.id.ib_addDevice)
    public void onClick() {
        startActivity(new Intent(getContext(), AddDeviceActivity.class));
        ((MainActivity) getActivity()).closeLeftMenu();
    }

    /**
     * 加载已配对设备
     */
    private void loadDeviceList() {
        if (OznerDeviceManager.Instance() == null) {
            return;
        }
        if (OznerDeviceManager.Instance().getDevices() == null) {
            return;
        }

        mLeftAdapter.clear();
        List<OznerDeviceSettings> oznerSettings = DBManager.getInstance(getContext()).getDeviceSettingList(mUserid);
//        for (OznerDevice device : OznerDeviceManager.Instance().getDevices()) {
//            mLeftAdapter.addData(device);
//        }
        leftDeviceList.clear();
        int settingCount = oznerSettings.size();
        for (int i = 0; i < settingCount; i++) {
            LeftMenuDeviceItem item = new LeftMenuDeviceItem();
            item.setName(oznerSettings.get(i).getName());
            item.setUsePos(oznerSettings.get(i).getDevicePosition());
            item.setMac(oznerSettings.get(i).getMac());
            item.setType(oznerSettings.get(i).getDevcieType());
            item.setDevice(OznerDeviceManager.Instance().getDevice(oznerSettings.get(i).getMac()));
            leftDeviceList.add(item);
        }
        mLeftAdapter.loadData(leftDeviceList);
        if (mLeftAdapter.getCount() > 0) {
            //设置侧边栏默认选中
            String selMac = UserDataPreference.GetUserData(getContext(), UserDataPreference.SelMac, null);
            int selPos = mLeftAdapter.getSelectItemPos(selMac);
            if (selPos >= 0) {
                selectDevice(selPos, true);
            } else {
                if (mLeftAdapter.getCount() > 0) {
                    selectDevice(0, true);
                } else {
                    selectDevice(-1, true);
                    ((MainActivity) getActivity()).onDeviceItemClick(null, null, true);
                }
            }
        } else {
            ((MainActivity) getActivity()).onDeviceItemClick(null, null, true);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectDevice(position, false);
    }

    public class LeftMonitor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "LeftMonitor_onReceive: " + intent.getAction());
            if (intent.getAction().equals(OznerDeviceManager.ACTION_OZNER_MANAGER_DEVICE_ADD)
                    || intent.getAction().equals(OznerDeviceManager.ACTION_OZNER_MANAGER_DEVICE_CHANGE)
                    || intent.getAction().equals(OznerDeviceManager.ACTION_OZNER_MANAGER_OWNER_CHANGE)
                    || intent.getAction().equals(OznerDeviceManager.ACTION_OZNER_MANAGER_DEVICE_REMOVE)) {
                initDataList();
            } else if (intent.getAction().endsWith(OznerBroadcastAction.OBA_CenterDeviceSelect)) {
                String selMac = intent.getStringExtra(Contacts.PARMS_MAC);
                int selPos = mLeftAdapter.getSelectItemPos(selMac);
                if (selPos >= 0) {
                    selectDevice(selPos, false);
                } else {
                    Toast.makeText(context, R.string.device_unexsit, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public class LeftMenuAdapter extends CommonAdapter<LeftMenuDeviceItem> {
        private int selPos = -1;
        private String selMac = "";
        private boolean isMacSel = false;

        public LeftMenuAdapter(Context context, int itemLayoutId) {
            super(context, itemLayoutId);
        }

        public int getSelectItemPos(String mac) {
            if (mac != null) {
                int count = getCount();
                int pos = -1;
                for (int i = 0; i < count; i++) {
                    if (mac.equals(getItem(i).getMac())) {
                        pos = i;
                        break;
                    }
                }
                return pos;
            } else {
                return -1;
            }
        }

        /**
         * 按位置选择
         *
         * @param position
         */
        public void setSelectPosition(int position) {
            if (position >= 0 && position < getCount()) {
                this.selPos = position;
            } else if (getCount() > 0) {
                selPos = 0;
            } else {
                selPos = -1;
            }
            isMacSel = false;
            this.notifyDataSetChanged();
        }

        /**
         * 按设备mac选择
         *
         * @param mac
         */
        public void setSelectMac(String mac) {
            this.selMac = mac;
            isMacSel = true;
            this.notifyDataSetChanged();
        }

        public int getSelectPosition() {
            if (getCount() == 0) {
                selPos = -1;
            }
            return this.selPos;
        }

        public LeftMenuDeviceItem getSelectedItem() {
            if (selPos >= 0) {
                return this.getItem(selPos);
            } else {
                return null;
            }
        }

        /**
         * 设置选中时的icon
         *
         * @param holder
         * @param isSelected
         * @param onResId
         * @param offResid
         */
        private void setItemSelected(CommonViewHolder holder, boolean isSelected, int onResId, int offResid) {
            if (isSelected) {
                holder.setImageResource(R.id.iv_deviceIcon, onResId);
            } else {
                holder.setImageResource(R.id.iv_deviceIcon, offResid);
            }
        }

        @Override
        public void convert(CommonViewHolder holder, LeftMenuDeviceItem item, int position) {
            //设置设备名称
            holder.setText(R.id.tv_deviceName, item.getName());

            //设置设备连接状态
            if (item.getDevice() != null) {
                if (item.getDevice().connectStatus() == BaseDeviceIO.ConnectStatus.Connected) {
                    holder.setText(R.id.tv_connectState, R.string.connected);
                } else if (item.getDevice().connectStatus() == BaseDeviceIO.ConnectStatus.Connecting) {
                    holder.setText(R.id.tv_connectState, R.string.connecting);
                } else if (item.getDevice().connectStatus() == BaseDeviceIO.ConnectStatus.Disconnect) {
                    holder.setText(R.id.tv_connectState, R.string.disconnect);
                }
            } else {
                holder.setText(R.id.tv_connectState, R.string.disconnect);
                DBManager.getInstance(mContext).deleteDeviceSettings(mUserid, item.getMac());
                loadDeviceList();
            }
            String usePos = item.getUsePos();//(String) item.Setting().get(Contacts.DEV_USE_POS, "");
            if (usePos != null && usePos.trim().length() > 0) {
                holder.setText(R.id.tv_deviceDesc, usePos);
            }

            //设置设备网络类型和是否选中
            String deviceType = item.getType();
            boolean isSelected = false;
            if (isMacSel) {
                selPos = -1;
                if (selMac == item.getMac()) {
                    selPos = position;
                }
            }

            if (selPos == position) {
                isSelected = true;
            } else {
                isSelected = false;
            }

            int typeResId = R.mipmap.connect_bluetooth_on;

            if (CupManager.IsCup(deviceType)) {
                // TODO: 2016/11/4 水杯
                typeResId = R.mipmap.connect_bluetooth_on;
                setItemSelected(holder, isSelected, R.mipmap.icon_cup_on, R.mipmap.icon_cup_off);
            } else if (TapManager.IsTap(deviceType)) {
                // TODO: 2016/11/4 水探头
                typeResId = R.mipmap.connect_bluetooth_on;
                setItemSelected(holder, isSelected, R.mipmap.icon_tap_on, R.mipmap.icon_tap_off);
            } else if (WaterPurifierManager.IsWaterPurifier(deviceType)) {
                if("Ozner RO".equals(deviceType)){
                    typeResId = R.mipmap.connect_bluetooth_on;
                    setItemSelected(holder, isSelected, R.mipmap.icon_water_purifier_on, R.mipmap.icon_water_purifier_off);
                }else {
                    // TODO: 2016/11/4 水机
                    typeResId = R.mipmap.connect_wifi_on;
                    setItemSelected(holder, isSelected, R.mipmap.icon_water_purifier_on, R.mipmap.icon_water_purifier_off);
                }
            } else if (AirPurifierManager.IsBluetoothAirPurifier(deviceType)) {
                // TODO: 2016/11/4 蓝牙空净
                typeResId = R.mipmap.connect_bluetooth_on;
                setItemSelected(holder, isSelected, R.mipmap.icon_air_purifier_desk_on, R.mipmap.icon_air_purifier_desk_off);
            } else if (AirPurifierManager.IsWifiAirPurifier(deviceType)) {
                // TODO: 2016/11/4 WiFi空净
                typeResId = R.mipmap.connect_wifi_on;
                setItemSelected(holder, isSelected, R.mipmap.icon_air_purifier_ver_on, R.mipmap.icon_air_purifier_ver_off);
            } else if (WaterReplenishmentMeterMgr.IsWaterReplenishmentMeter(deviceType)) {
                // TODO: 2016/11/4 补水仪
                typeResId = R.mipmap.connect_bluetooth_on;
                setItemSelected(holder, isSelected, R.mipmap.icon_replen_on, R.mipmap.icon_replen_off);
            }

            holder.setImageResource(R.id.iv_deviceType, typeResId);
        }
    }

}
