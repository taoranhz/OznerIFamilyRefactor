package com.ozner.WaterPurifier;

import android.content.Context;

import com.ozner.device.BaseDeviceIO;
import com.ozner.wifi.mxchip.MXChipIO;

/**
 * Created by zhiyongxu on 16/5/15.
 */
public class WaterPurifier_MXChip extends WaterPurifier_Mx {
    private static String SecureCode = "16a21bd6";
//    final WaterPurifierImp waterPurifierImp = new WaterPurifierImp();

    public WaterPurifier_MXChip(Context context, String Address, String Model, String Setting) {
        super(context, Address, Model, Setting);
    }

    @Override
    protected byte[] handlerOrgData(byte[] data) {
        return data;
    }

    @Override
    public Class<?> getIOType() {
        return MXChipIO.class;
    }

    @Override
    protected void doSetDeviceIO(BaseDeviceIO oldIO, BaseDeviceIO newIO) {
        if (oldIO != null) {
            oldIO.setOnTransmissionsCallback(null);
            oldIO.unRegisterStatusCallback(waterPurifierImp);
            oldIO.setOnInitCallback(null);
        }
        if (newIO != null) {
            MXChipIO io = (MXChipIO) newIO;
            io.setSecureCode(SecureCode);
            io.setOnTransmissionsCallback(waterPurifierImp);
            io.registerStatusCallback(waterPurifierImp);
            io.setOnInitCallback(waterPurifierImp);
        }
        super.doSetDeviceIO(oldIO,newIO);
    }


}
