package com.ozner.device;

import android.content.Context;

import com.ozner.bluetooth.BluetoothIOMgr;
import com.ozner.wifi.mxchip.Fog2.FogIOManager;
import com.ozner.wifi.mxchip.MXChipIOManager;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by xzyxd on 2015/11/2.
 */
public class IOManagerList extends IOManager {
    BluetoothIOMgr bluetoothIOMgr;
    MXChipIOManager mxChipIOManager;
    FogIOManager fogIOManager;

    public IOManagerList(Context context) {
        super(context);
        bluetoothIOMgr = new BluetoothIOMgr(context);
        mxChipIOManager = new MXChipIOManager(context);
        fogIOManager = new FogIOManager(context);
    }

    public BluetoothIOMgr bluetoothIOMgr() {
        return bluetoothIOMgr;
    }

    public MXChipIOManager mxChipIOManager() {
        return mxChipIOManager;
    }

    public FogIOManager fogIOManager() {
        return fogIOManager;
    }


    @Override
    public void Start(String user, String token) {
        bluetoothIOMgr.Start(user, token);
        mxChipIOManager.Start(user, token);
        fogIOManager.Start(user, token);
    }

    @Override
    public void Stop() {
        bluetoothIOMgr.Stop();
        mxChipIOManager.Stop();
        fogIOManager.Stop();
    }

    @Override
    public void closeAll() {
        bluetoothIOMgr.closeAll();
        mxChipIOManager.closeAll();
        fogIOManager.closeAll();
    }

    @Override
    public void setIoManagerCallback(IOManagerCallback ioManagerCallback) {
        bluetoothIOMgr.setIoManagerCallback(ioManagerCallback);
        mxChipIOManager.setIoManagerCallback(ioManagerCallback);
        fogIOManager.setIoManagerCallback(ioManagerCallback);
    }

    @Override
    public void removeDevice(BaseDeviceIO io) {
        if (bluetoothIOMgr.isMyIO(io)) {
            bluetoothIOMgr.removeDevice(io);
        } else if (mxChipIOManager.isMyIO(io)) {
            mxChipIOManager.removeDevice(io);
        } else if (fogIOManager().isMyIO(io)) {
            fogIOManager.removeDevice(io);
        }

        super.removeDevice(io);
    }

    @Override
    public BaseDeviceIO getAvailableDevice(String address) {
        BaseDeviceIO io = null;
        if ((io = bluetoothIOMgr.getAvailableDevice(address)) != null) {
            return io;
        }
        if ((io = mxChipIOManager.getAvailableDevice(address)) != null) {
            return io;
        }
        if((io = fogIOManager.getAvailableDevice(address))!=null){
            return io;
        }
        return io;
    }

    @Override
    public BaseDeviceIO[] getAvailableDevices() {
        ArrayList<BaseDeviceIO> list = new ArrayList<>();

        Collections.addAll(list, bluetoothIOMgr.getAvailableDevices());
        Collections.addAll(list, mxChipIOManager.getAvailableDevices());
        Collections.addAll(list,fogIOManager.getAvailableDevices());

        return list.toArray(new BaseDeviceIO[list.size()]);
    }

}
