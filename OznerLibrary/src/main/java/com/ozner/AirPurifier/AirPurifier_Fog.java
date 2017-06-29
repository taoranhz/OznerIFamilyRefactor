package com.ozner.AirPurifier;

import android.content.Context;

import com.ozner.device.BaseDeviceIO;
import com.ozner.util.Convert;
import com.ozner.wifi.mxchip.Fog2.FogIO;

/**
 * Created by ozner_67 on 2017/6/12.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class AirPurifier_Fog extends AirPurifier_Mx {
    private static final String TAG = "AirPurifier_Fog";

//    final FogAirPurifierImp airPurifierImp = new FogAirPurifierImp();

    public AirPurifier_Fog(Context context, String Address, String Type, String Setting) {
        super(context, Address, Type, Setting);
        String json = Setting().get("powerTimer", "").toString();
        powerTimer.fromJSON(json);
    }

    @Override
    public Class<?> getIOType() {
        return FogIO.class;
    }

    @Override
    protected void doSetDeviceIO(BaseDeviceIO oldIO, BaseDeviceIO newIO) {
        if (oldIO != null) {
            oldIO.setOnTransmissionsCallback(null);
            oldIO.unRegisterStatusCallback(airPurifierImp);
            oldIO.setOnInitCallback(null);
            mIsOffline = true;
            doUpdate();
        }
        if (newIO != null) {
            FogIO io = (FogIO) newIO;
//            io.setSecureCode(SecureCode);
            io.setOnTransmissionsCallback(airPurifierImp);
            io.registerStatusCallback(airPurifierImp);
            io.setOnInitCallback(airPurifierImp);
        } else {
            setOffline(true);
        }
    }

    @Override
    protected byte[] handlerOrgData(byte[] data) {
        try {
            String dataStr = new String(data);
//            Log.e(TAG, "onPublish: " + dataStr);
            return Convert.StringToByteArray(dataStr);
        } catch (Exception ex) {
            return null;
        }
    }

//    class FogAirPurifierImp implements
//            BaseDeviceIO.OnTransmissionsCallback
//            , BaseDeviceIO.StatusCallback
//            , BaseDeviceIO.OnInitCallback {
//
//        @Override
//        public void onConnected(BaseDeviceIO io) {
//
//        }
//
//        @Override
//        public void onDisconnected(BaseDeviceIO io) {
//
//        }
//
//        @Override
//        public void onReady(BaseDeviceIO io) {
//
//        }
//
//        @Override
//        public void onIOSend(byte[] bytes) {
//
//        }
//
//        @Override
//        public void onIORecv(byte[] bytes) {
//
//        }
//
//        private boolean send(byte[] data, OperateCallback<Void> cb) {
//            if (IO() != null) {
//                reqeustCount++;
//                if (reqeustCount >= 3) {
//                    setOffline(true);
//                }
//
//
//                //Respone=false;
//                return IO().send(data, cb);
//            } else
//                return false;
//        }
//
//        @Override
//        public boolean onIOInit() {
//            return doInit();
//        }
//    }
}
