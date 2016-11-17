package com.ozner.cup.Device.AirPurifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ozner.AirPurifier.AirPurifier_MXChip;
import com.ozner.cup.Device.DeviceFragment;
import com.ozner.cup.Main.MainActivity;
import com.ozner.cup.R;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;


public class AirVerPurifierFragment extends DeviceFragment {
    private static final String TAG = "AirVerPurifier";
    private AirPurifier_MXChip mVerAirPurifier;
    AirPurifierMonitor airMonitor;

    /**
     * 实例化Fragment
     *
     * @param mac
     *
     * @return
     */
    public static DeviceFragment newInstance(String mac) {
        AirVerPurifierFragment fragment = new AirVerPurifierFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DeviceAddress, mac);
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        try {
            Bundle bundle = getArguments();
            mVerAirPurifier = (AirPurifier_MXChip) OznerDeviceManager.Instance().getDevice(bundle.getString(DeviceAddress));
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "onCreate_Ex: " + ex.getMessage());
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_air_ver_purifier, container, false);
    }

    @Override
    public void setDevice(OznerDevice device) {

        Log.e(TAG, "setDevice: " + device.Type() + " ,name:" + device.getName());
        if (mVerAirPurifier != null) {
            Log.e(TAG, "setDevice: not null");
            if (mVerAirPurifier.Address() != device.Address()) {
                mVerAirPurifier.release();
                mVerAirPurifier = null;
                mVerAirPurifier = (AirPurifier_MXChip) device;
                refreshUIData();
            }
        } else {
            mVerAirPurifier = (AirPurifier_MXChip) device;
            Log.e(TAG, "setDevice: null");
            refreshUIData();
        }
    }


    /**
     * 注册广播接收器
     */
    private void registerMonitor() {
        airMonitor = new AirPurifierMonitor();
        IntentFilter filter = new IntentFilter();
        filter.addAction(OznerDeviceManager.ACTION_OZNER_MANAGER_DEVICE_CHANGE);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_CONNECTING);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_DISCONNECTED);
        filter.addAction(BaseDeviceIO.ACTION_DEVICE_CONNECTED);

        filter.addAction(AirPurifier_MXChip.ACTION_AIR_PURIFIER_SENSOR_CHANGED);
        filter.addAction(AirPurifier_MXChip.ACTION_AIR_PURIFIER_STATUS_CHANGED);
        getContext().registerReceiver(airMonitor, filter);
    }

    /**
     * 注销广播接收器
     */
    private void releaseMonitor() {
        if (isThisAdd() && airMonitor != null) {
            getContext().unregisterReceiver(airMonitor);
        }
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


    private boolean isThisAdd() {
        return AirVerPurifierFragment.this.isAdded() && !AirVerPurifierFragment.this.isRemoving() && !AirVerPurifierFragment.this.isDetached();
    }

    @Override
    public void onAttach(Context context) {
        Log.e(TAG, "onAttach: " + isThisAdd());
        try {
            if (isThisAdd())
                ((MainActivity) context).setCustomTitle(R.string.air_purifier);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "onAttach_Ex: " + ex.getMessage());
        }
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        refreshUIData();
        super.onResume();
    }

    @Override
    protected void refreshUIData() {
//        Log.e(TAG, "refreshUIData: ");
        if (mVerAirPurifier != null && isThisAdd()) {
            ((MainActivity) getActivity()).setCustomTitle(mVerAirPurifier.getName());
        }
    }




    class AirPurifierMonitor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshUIData();
        }
    }
}
