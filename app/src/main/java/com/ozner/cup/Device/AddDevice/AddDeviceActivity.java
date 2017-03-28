package com.ozner.cup.Device.AddDevice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Base.CommonAdapter;
import com.ozner.cup.Base.CommonViewHolder;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Bean.RankType;
import com.ozner.cup.Device.AddDevice.bean.AddDeviceListBean;
import com.ozner.cup.Device.AirPurifier.MatchDeskAirActivity;
import com.ozner.cup.Device.AirPurifier.MatchVerAirActivity;
import com.ozner.cup.Device.Cup.MatchCupActivity;
import com.ozner.cup.Device.ROWaterPurifier.MatchROWaterPuriferActivity;
import com.ozner.cup.Device.ReplenWater.MatchReplenActivity;
import com.ozner.cup.Device.Tap.MatchTapActivity;
import com.ozner.cup.Device.WaterPurifier.MatchWaterPuriferActivity;
import com.ozner.cup.R;

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
    private int[] deviceNames = new int[]{
            R.string.smart_glass,
            R.string.water_probe,
            R.string.water_tdspen,

            R.string.water_purifier,
            R.string.air_purifier_ver,

            R.string.air_purifier_desk,
            R.string.water_replen_meter,

            R.string.water_ropurifier,
    };
    private int[] connectTypes = new int[]{
            R.string.bluetooth_connection,
            R.string.bluetooth_connection,
            R.string.bluetooth_connection,
            R.string.wifi_connection,
            R.string.wifi_connection,
            R.string.bluetooth_connection,
            R.string.bluetooth_connection,
            R.string.bluetooth_connection,
    };
    private int[] devIconRes = new int[]{
            R.drawable.device_icon_cup,
            R.drawable.device_icon_tap,
            R.drawable.device_icon_tdspen,
            R.drawable.device_icon_water,
            R.drawable.device_icon_air_ver,
            R.drawable.device_icon_air_desk,
            R.drawable.device_icon_replen,
            R.drawable.device_icon_rowater,
    };
    private int[] conIconRes = new int[]{
            R.mipmap.connect_bluetooth_on,
            R.mipmap.connect_bluetooth_on,
            R.mipmap.connect_bluetooth_on,
            R.mipmap.connect_wifi_on,
            R.mipmap.connect_wifi_on,
            R.mipmap.connect_bluetooth_on,
            R.mipmap.connect_bluetooth_on,
            R.mipmap.connect_bluetooth_on
    };

    private AddDeviceAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        ButterKnife.inject(this);
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
                break;
            case R.drawable.device_icon_tap:
                Intent tapIntent = new Intent(this,MatchTapActivity.class);
                tapIntent.putExtra(Contacts.PARMS_RANK_TYPE, RankType.TapType);
                startActivity(tapIntent);
                break;
            case R.drawable.device_icon_tdspen:
                Intent tdsPenIntent = new Intent(this,MatchTapActivity.class);
                tdsPenIntent.putExtra(Contacts.PARMS_RANK_TYPE, RankType.TdsPenType);
                startActivity(tdsPenIntent);
                break;
            case R.drawable.device_icon_water:
                startActivity(new Intent(this, MatchWaterPuriferActivity.class));
                break;
            case R.drawable.device_icon_air_ver:
                startActivity(new Intent(this, MatchVerAirActivity.class));
                break;
            case R.drawable.device_icon_air_desk:
                startActivity(new Intent(this, MatchDeskAirActivity.class));
                break;
            case R.drawable.device_icon_replen:
                startActivity(new Intent(this, MatchReplenActivity.class));
                break;
            case R.drawable.device_icon_rowater:
                startActivity(new Intent(this, MatchROWaterPuriferActivity.class));
                break;
        }

        this.finish();
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
