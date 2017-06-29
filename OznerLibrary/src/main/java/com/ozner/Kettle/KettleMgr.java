package com.ozner.Kettle;

import android.content.Context;

import com.ozner.bluetooth.BluetoothIO;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.BaseDeviceManager;
import com.ozner.device.OznerDevice;

/**
 * Created by ozner_67 on 2017/6/20.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class KettleMgr extends BaseDeviceManager {
    public KettleMgr(Context context) {
        super(context);
    }

    @Override
    protected OznerDevice createDevice(String address, String type, String settings) {
        if (isMyDevice(type)) {
            return new Kettle(context(), address, type, settings);
        }else
            return null;
    }

    public static boolean isKettle(String Type) {
        if (Type == null) return false;
        return Type.trim().startsWith("DRH001");
    }


    @Override
    public boolean isMyDevice(String type) {
        return isKettle(type);
    }

    @Override
    public boolean checkIsBindMode(BaseDeviceIO io) {
        if (isMyDevice(io.getType()))
        {
            BluetoothIO bluetoothIO=(BluetoothIO)io;
            if (bluetoothIO.getScanResponseType()==0x20)
            {
                if ((bluetoothIO.getScanResponseData()!=null) && (bluetoothIO.getScanResponseData().length>1))
                {
                    return bluetoothIO.getScanResponseData()[0]!=0;
                }
            }
        }else
            return false;
        return false;
    }
}
