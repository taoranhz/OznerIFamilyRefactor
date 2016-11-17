package com.ozner.cup.Device.Cup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ozner.cup.Cup;
import com.ozner.cup.Device.DeviceFragment;
import com.ozner.cup.Main.MainActivity;
import com.ozner.cup.R;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import static com.aylanetworks.aaml.mdns.NetThread.TAG;

public class CupFragment extends DeviceFragment {

    private Cup mCup;
    private int oldTdsValue;
    private CupMonitor mMonitor;


    /**
     * 实例化Fragment
     *
     * @param mac
     *
     * @return
     */
    public static DeviceFragment newInstance(String mac) {
        CupFragment fragment = new CupFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DeviceAddress, mac);
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }


    @Override
    public void setDevice(OznerDevice device) {
        if (mCup != null) {
            if (mCup.Address() != device.Address()) {
                mCup.release();
                mCup = null;
                mCup = (Cup) device;
                refreshUIData();
            }
        } else {
            mCup = (Cup) device;
            refreshUIData();
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        initAnimation();
        try {
            Bundle bundle = getArguments();
            mCup = (Cup) OznerDeviceManager.Instance().getDevice(bundle.getString(DeviceAddress));
            oldTdsValue = 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "onCreate_Ex: " + ex.getMessage());
        }
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cup, container, false);
        return view;
    }

    @Override
    public void onStart() {
        registerMonitor();
        super.onStart();
    }

    @Override
    public void onStop() {
        releaseMonitor();
        super.onStop();
    }

    @Override
    public void onResume() {
        refreshUIData();
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        try {
            if (isThisAdd())
                ((MainActivity) context).setCustomTitle(getString(R.string.smart_glass));
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "onAttach_Ex: " + ex.getMessage());
        }
        super.onAttach(context);
    }

    /**
     * 刷新UI数据
     */
    @Override
    protected void refreshUIData() {

    }

    private boolean isThisAdd() {
        return CupFragment.this.isAdded() && !CupFragment.this.isRemoving() && !CupFragment.this.isDetached();
    }

    /**
     * 注册广播监听
     */
    private void registerMonitor() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Cup.ACTION_BLUETOOTHCUP_SENSOR);
        filter.addAction(Cup.ACTION_BLUETOOTHCUP_RECORD_COMPLETE);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_CONNECTED);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_CONNECTING);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_DISCONNECTED);
        filter.addAction(OznerDeviceManager.ACTION_OZNER_MANAGER_DEVICE_CHANGE);
        getContext().registerReceiver(mMonitor, filter);
    }

    /**
     * 注销广播监听
     */
    private void releaseMonitor() {
        if (isThisAdd()) {
            getContext().unregisterReceiver(mMonitor);
        }
    }


    class CupMonitor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshUIData();
        }
    }
}
