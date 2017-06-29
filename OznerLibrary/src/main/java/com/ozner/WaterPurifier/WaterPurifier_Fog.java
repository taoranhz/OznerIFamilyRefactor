package com.ozner.WaterPurifier;

import android.content.Context;

import com.ozner.device.BaseDeviceIO;
import com.ozner.util.Convert;
import com.ozner.wifi.mxchip.Fog2.FogIO;

/**
 * Created by ozner_67 on 2017/5/23.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class WaterPurifier_Fog extends WaterPurifier_Mx {
//    private final WaterPurifierFogImp waterPurifierFogImp = new WaterPurifierFogImp();

    public WaterPurifier_Fog(Context context, String Address, String Model, String Setting) {
        super(context, Address, Model, Setting);
    }

    @Override
    protected byte[] handlerOrgData(byte[] data) {
        try {
            String dataStr = new String(data);
            return Convert.StringToByteArray(dataStr);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public Class<?> getIOType() {
        return FogIO.class;
    }

    @Override
    protected void doSetDeviceIO(BaseDeviceIO oldIO, BaseDeviceIO newIO) {
        if (oldIO != null) {
            oldIO.setOnTransmissionsCallback(null);
            oldIO.unRegisterStatusCallback(waterPurifierImp);
            oldIO.setOnInitCallback(null);
        }
        if (newIO != null) {
            FogIO io = (FogIO) newIO;
//            io.setSecureCode(SecureCode);
            io.setOnTransmissionsCallback(waterPurifierImp);
            io.registerStatusCallback(waterPurifierImp);
            io.setOnInitCallback(waterPurifierImp);
        }
        super.doSetDeviceIO(oldIO, newIO);
    }

}
