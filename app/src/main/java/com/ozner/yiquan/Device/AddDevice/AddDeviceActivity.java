package com.ozner.yiquan.Device.AddDevice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ozner.yiquan.Base.BaseActivity;
import com.ozner.yiquan.Base.CommonAdapter;
import com.ozner.yiquan.Base.CommonViewHolder;
import com.ozner.yiquan.Device.AddDevice.bean.AddDeviceListBean;
import com.ozner.yiquan.Device.AirPurifier.MatchDeskAirActivity;
import com.ozner.yiquan.Device.AirPurifier.MatchVerAirActivity;
import com.ozner.yiquan.Device.Cup.MatchCupActivity;
import com.ozner.yiquan.Device.ReplenWater.MatchReplenActivity;
import com.ozner.yiquan.Device.Tap.MatchTapActivity;
import com.ozner.yiquan.Device.WaterPurifier.MatchWaterPuriferActivity;
import com.ozner.yiquan.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AddDeviceActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "AddDeviceActivity";

    @InjectView(R.id.title)
    TextView tv_title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.lv_add_list)
    ListView lvAddList;
    BluetoothManager bluetoothManager;
    BluetoothAdapter blueAdapter;
    //君融科技
    private int[] deviceNames = new int[]{
            R.string.water_probe,
            R.string.water_purifier,
            R.string.air_purifier,
            R.string.water_replen_meter
    };
    private int[] connectTypes = new int[]{
            R.string.bluetooth_connection,
            R.string.wifi_connection,
            R.string.wifi_connection,
            R.string.bluetooth_connection,
    };
    private int[] devIconRes = new int[]{
            R.drawable.device_icon_tap,
            R.drawable.device_icon_water,
            R.drawable.device_icon_air_ver,
            R.drawable.device_icon_replen
    };
    private int[] conIconRes = new int[]{
            R.mipmap.connect_bluetooth_on,
            R.mipmap.connect_wifi_on,
            R.mipmap.connect_wifi_on,
            R.mipmap.connect_bluetooth_on,
    };
    private AddDeviceAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        ButterKnife.inject(this);
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        blueAdapter = bluetoothManager.getAdapter();
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        tv_title.setText(R.string.add_device);
        initDeviceList();
    }

    /**
     * 初始化设备列表
     */
    private void initDeviceList() {
        mAdapter = new AddDeviceAdapter(this, R.layout.add_device_item);
        lvAddList.setAdapter(mAdapter);
        for (int i = 0; i < deviceNames.length; i++) {
            AddDeviceListBean item = new AddDeviceListBean();
            item.setDeviceType(getString(deviceNames[i]));
            item.setConnectType(getString(connectTypes[i]));
            item.setDeviceIconResId(devIconRes[i]);
            item.setConnectResId(conIconRes[i]);
            mAdapter.addData(item);
        }
        lvAddList.setOnItemClickListener(this);
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
        dealItemClick(devIconRes[position]);
    }

    /**
     * 处理添加设备列表点击事件
     *
     * @param devIconResId
     */
    private void dealItemClick(int devIconResId) {
        switch (devIconResId) {
            case R.drawable.device_icon_cup:
                startActivity(new Intent(this, MatchCupActivity.class));
                this.finish();
                break;
            case R.drawable.device_icon_tap:
                if (!blueAdapter.isEnabled()) {
                    blueAdapter.enable();
                }
                if(blueAdapter.isEnabled()){
                    startActivity(new Intent(AddDeviceActivity.this, MatchTapActivity.class));
                    this.finish();
                }
                break;
            case R.drawable.device_icon_tdspen:
                this.finish();
                break;
            case R.drawable.device_icon_water:
                startActivity(new Intent(this, MatchWaterPuriferActivity.class));
                this.finish();
                break;
            case R.drawable.device_icon_air_ver:
                startActivity(new Intent(this, MatchVerAirActivity.class));
                this.finish();
                break;
            case R.drawable.device_icon_air_desk:
                startActivity(new Intent(this, MatchDeskAirActivity.class));
                this.finish();
                break;
            case R.drawable.device_icon_replen:
                if (!blueAdapter.isEnabled()) {
                    blueAdapter.enable();
                }
                if(blueAdapter.isEnabled()){
                    startActivity(new Intent(AddDeviceActivity.this, MatchReplenActivity.class));
                    this.finish();
                }
                break;
        }

//        this.finish();
    }

    public class AddDeviceAdapter extends CommonAdapter<AddDeviceListBean> {

        public AddDeviceAdapter(Context context, int itemLayoutId) {
            super(context, itemLayoutId);
        }

        @Override
        public void convert(CommonViewHolder holder, AddDeviceListBean item, int position) {
            holder.setImageResource(R.id.iv_device_icon, item.getDeviceIconResId());
            holder.setText(R.id.tv_device_name, item.getDeviceType());
            holder.setImageResource(R.id.iv_connection_icon, item.getConnectResId());
            holder.setText(R.id.tv_connection_text, item.getConnectType());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
