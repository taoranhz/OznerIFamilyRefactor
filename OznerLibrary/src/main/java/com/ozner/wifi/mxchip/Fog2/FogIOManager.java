package com.ozner.wifi.mxchip.Fog2;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.ozner.device.BaseDeviceIO;
import com.ozner.device.DeviceSetting;
import com.ozner.device.IOManager;
import com.ozner.device.OznerDeviceManager;
import com.ozner.wifi.mxchip.IMQTTListener;
import com.ozner.wifi.mxchip.MQTTProxyFog;
import com.ozner.wifi.mxchip.SMQTTProxy;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ozner_67 on 2017/5/23.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class FogIOManager extends IOManager {
    private static final String TAG = "FogIOManager";
    HashMap<String, String[]> listenDeviceList = new HashMap<>();

    final MQTTProxyImp mqttProxyImp = new MQTTProxyImp();
    MQTTProxyFog proxy;

    public FogIOManager(Context context) {
        super(context);
        proxy = new MQTTProxyFog(context);
        proxy.registerListener(mqttProxyImp);
    }


    public FogIO createFogDevice(String address, String type, String setting) throws ClassCastException {
        DeviceSetting devSetting = new DeviceSetting();
        devSetting.load(setting);
        synchronized (listenDeviceList) {
            if (!listenDeviceList.containsKey(address)) {
                listenDeviceList.put(address, new String[]{type, devSetting.deviceId()});
            }
//            if (proxy.isConnected()) {
                Log.e(TAG, "createFogDevice: proxy:"+proxy.isConnected());

                FogIO io = new FogIO(context(), proxy, type, address, devSetting.deviceId());
                doAvailable(io);
                return io;
//            } else {
//                Log.e(TAG, "createFogDevice: proxy未连接");
//                return null;
//            }
        }
    }

    @Override
    protected void doUnavailable(BaseDeviceIO io) {
        super.doUnavailable(io);
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == delayedAvailableMessage) {
                    String address = msg.obj.toString();
                    synchronized (listenDeviceList) {
                        if (listenDeviceList.containsKey(address)) {
                            String[] typeAndDevID = listenDeviceList.get(address);
                            if (OznerDeviceManager.Instance().hashDevice(address)) {
                                DeviceSetting devSetting = new DeviceSetting();
                                devSetting.deviceId(typeAndDevID[1]);
                                Log.e(TAG, "fog_doUn_handleMessage: " + devSetting.deviceId());
                                FogIO io = createFogDevice(address, typeAndDevID[0], devSetting.toString());
                                if (io != null)
                                    doAvailable(io);
                            }
                        }
                    }
                }
            }
        };

        if (!OznerDeviceManager.Instance().hashDevice(io.getAddress())) {
            listenDeviceList.remove(io.getAddress());
        } else {
            if (proxy.isConnected()) {
                Message m = new Message();
                m.what = delayedAvailableMessage;
                m.obj = io.getAddress();
                handler.sendMessageDelayed(m, 5000); //如果IO被关闭了,MQTT还是连接中的情况下,重新激活IO
            }
        }
        super.doUnavailable(io);
    }

    @Override
    public void Start(String user, String token) {
        Log.e(TAG, "Start: ");
        proxy.start();
    }

    @Override
    public void Stop() {
        Log.e(TAG, "Stop: ");
        proxy.stop();
    }

    final static int delayedAvailableMessage = 0x1000;

    class MQTTProxyImp implements IMQTTListener {

        @Override
        public void onConnected(SMQTTProxy proxy) {
            Log.e("FogIOManager", "onConnected: ");
            ArrayList<String> list;
            synchronized (listenDeviceList) {
                list = new ArrayList<>(listenDeviceList.keySet());
            }

            for (String address : list) {
                FogIO io = new FogIO(context(), (MQTTProxyFog) proxy, listenDeviceList.get(address)[0], address, listenDeviceList.get(address)[1]);
                doAvailable(io);
            }

        }

        @Override
        public void onDisconnected(SMQTTProxy proxy) {
            ArrayList<String> list;
            synchronized (listenDeviceList) {
                list = new ArrayList<>(listenDeviceList.keySet());
            }

            for (String address : list) {
                Log.e(TAG, "MQTTProxyImp_onDisconnected: " + address);
                FogIO io = new FogIO(context(), (MQTTProxyFog) proxy, listenDeviceList.get(address)[0], address, listenDeviceList.get(address)[1]);
                if (io != null)
                    doUnavailable(io);
            }
        }

        @Override
        public void onPublish(SMQTTProxy proxy, String topic, byte[] data) {

        }
    }

}
