package com.ozner.wifi;

import android.content.Context;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OznerDeviceManager;
import com.ozner.util.Helper;
import com.ozner.util.HttpUtil;
import com.ozner.wifi.mxchip.Fog2.FogPairImp;
import com.ozner.wifi.mxchip.Fog2.FogPairType;
import com.ozner.wifi.mxchip.MXChipIO;
import com.ozner.wifi.mxchip.Pair.ConfigurationDevice;
import com.ozner.wifi.mxchip.Pair.EasyLinkSender;
import com.ozner.wifi.mxchip.Pair.FTC;
import com.ozner.wifi.mxchip.Pair.FTC_Listener;
import com.ozner.wifi.mxchip.ThreadManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import io.fogcloud.fog_mdns.helper.SearchDeviceCallBack;

/**
 * Created by zhiyongxu on 16/5/12.
 */
public class WifiPair {
    private static final String TAG = "WifiPair";

    public interface WifiPairCallback {
        /**
         * 开始查找AYLA设备
         */
        void onStartPairAyla();

        /**
         * 开始配对庆科设备
         */
        void onStartPariMxChip();


        /**
         * 开始发送WIFI设置信息，AYLA 找到设备时掉用
         */
        void onSendConfiguration();

        /**
         * 开始激活注册设备
         */
        void onActivateDevice();

        /**
         * 等待设备连接WIFI
         */
        void onWaitConnectWifi();

        /**
         * 配网完成
         *
         * @param io 配对好的设备IO接口
         */
        void onPairComplete(BaseDeviceIO io);

        /**
         * 配网失败
         *
         * @param e 失败的异常
         */
        void onPairFailure(Exception e);
    }

    /**
     * 已经有一个配网过程正在运行
     */
    public class PairRunningException extends Exception {
    }

    /**
     * 无线设备没打开或无连接
     */
    public static class WifiStatusException extends Exception {
    }

    public static class NullSSIDException extends Exception {
    }

    public static class TimeoutException extends Exception {
    }

    public static class UnknownException extends Exception {
    }

    private WifiPairCallback callback = null;
    private String ssid = null;
    private String password = null;
    private Context context;

    private int runPairCount = 0;
    private Date startRunTime;
    private ThreadHandler runHandler = null;
    private int errorCount = 0;
    Object waitObject = new Object();
    private PairHander pairHander;
    private FogPairImp fogPairImp;
    private boolean isAcitving = false;
    private boolean isComplete = false;

    private void wait(int time) throws InterruptedException {
        synchronized (waitObject) {
            waitObject.wait(time);
        }
    }

    private void set() {
        synchronized (waitObject) {
            waitObject.notify();
        }
    }


    public WifiPair(Context context, WifiPairCallback callback)
            throws NullSSIDException {
        pairHander = new PairHander();
        this.context = context;
        this.callback = callback;
        this.fogPairImp = new FogPairImp(context, pairHander);
    }

    private void doPairFailure(Exception ex) {

        callback.onPairFailure(ex);
        stop();
    }

    private void doComplete(BaseDeviceIO io) {
        callback.onPairComplete(io);
        stop();
    }


    class MXChipPairImp implements FTC_Listener, Runnable {//, JmdnsListener {
        /**
         * 默认1分钟配网超时
         */
        final static int ConfigurationTimeout = 30000;
        final static int MDNSTimeout = 60000;
        String deviceMAC = "";


        ConfigurationDevice device = null;


        @Override
        public void onFTCfinished(String jsonString) {
            device = ConfigurationDevice.loadByFTCJson(jsonString);
            set();
        }

        @Override
        public void isSmallMTU(int MTU) {

        }

        private String getToken() {
            return String.valueOf(System.currentTimeMillis());
        }

        private String ActiveDevice() throws IOException {
            com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
            String url = "http://" + device.localIP + ":" + device.localPort + "/dev-activate";
            jsonObject.put("login_id", device.loginId);
            jsonObject.put("dev_passwd", device.devPasswd);
            jsonObject.put("user_token", getToken());
            String retString = HttpUtil.postJSON(url, jsonObject.toJSONString(), "US-ASCII");
            com.alibaba.fastjson.JSONObject ret = (com.alibaba.fastjson.JSONObject) JSON.parse(retString);
            return ret.getString("device_id");
        }

        private String Authorize() throws IOException {
            com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
            String url = "http://" + device.localIP + ":" + device.localPort + "/dev-authorize";
            jsonObject.put("login_id", device.loginId);
            jsonObject.put("dev_passwd", device.devPasswd);
            jsonObject.put("user_token", getToken());
            String retString = HttpUtil.postJSON(url, jsonObject.toJSONString(), "US-ASCII");
            com.alibaba.fastjson.JSONObject ret = (com.alibaba.fastjson.JSONObject) JSON.parse(retString);
            return ret.getString("device_id");
        }

        @Override
        public void run() {
            try {
                Log.e(TAG, "mx_run: 开始");
                device = null;
                deviceMAC = null;
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = wifiManager.getConnectionInfo();

                if (info.getSupplicantState() != SupplicantState.COMPLETED) {
                    doPairFailure(new WifiStatusException());
                    return;
                }
                pairHander.sendEmptyMessage(PairHander.START_MX_PAIR);

                FTC ftc = new FTC(context, this);
                try {
                    ftc.startListen();
                    EasyLinkSender sender = new EasyLinkSender();
                    sender.setSettings(ssid.trim(), password, info.getIpAddress());
                    Date t = new Date();

                    while (device == null) {
                        sender.send_easylink_v3();
                        Thread.sleep(100);
                        sender.send_easylink_v2();
                        Thread.sleep(100);
                        Date now = new Date();
                        if ((now.getTime() - t.getTime()) > ConfigurationTimeout) {
                            break;
                        }
                    }
                    sender.close();
                } finally {
                    ftc.stop();
                }

                if (device == null) {
                    runNext();
                    return;
                }

                if ((device.activated) && (!Helper.StringIsNullOrEmpty(device.activeDeviceID))) {
                    int p = device.activeDeviceID.indexOf("/");
                    if (p > 0) {
                        String tmp = device.activeDeviceID.substring(p + 1).toUpperCase();
                        if (tmp.length() == 12) {
                            String mac = tmp.substring(0, 2) + ":" +
                                    tmp.substring(2, 4) + ":" +
                                    tmp.substring(4, 6) + ":" +
                                    tmp.substring(6, 8) + ":" +
                                    tmp.substring(8, 10) + ":" +
                                    tmp.substring(10, 12);

                            String type = device.activeDeviceID.substring(0, p);
                            if (Helper.StringIsNullOrEmpty(type)) {
                                doPairFailure(new UnknownException());
                                return;
                            }
                            device.Type = type;
                            MXChipIO io = OznerDeviceManager.Instance().ioManagerList().mxChipIOManager().
                                    createMXChipDevice(mac, device.Type);
                            if (io != null) {
                                pairHander.sendMessage(PairHander.COMPLETE, io);
                            } else {
                                pairHander.sendMessage(PairHander.FAILURE, new Exception(""));
                            }
                            return;
                        }
                    }
                }
                pairHander.sendEmptyMessage(PairHander.WAIT_CONNECT_WIFI);

                Log.e(TAG, "run: 启动MDNS服务");
                fogPairImp.startSearchService(new SearchCallback());
                WifiPair.this.wait(MDNSTimeout);
                fogPairImp.getMicoDev().stopSearchDevices(null);

                Log.e(TAG, "run: deviceMac:" + deviceMAC);
                if (Helper.StringIsNullOrEmpty(deviceMAC)) {
                    doPairFailure(new TimeoutException());
                    return;
                }
                Log.e(TAG, "run: 正在激活");
                pairHander.sendEmptyMessage(PairHander.ACTIVATE_DEVICE);
                String deviceId = "";
                errorCount = 0;
                while (errorCount < 3) {
                    Log.e(TAG, "run: 1.0重新激活次数：" + errorCount);
                    try {
                        deviceId = ActiveDevice();
                    } catch (FileNotFoundException fe) {
                        Log.e(TAG, "run: 激活_ex:" + fe.getMessage());
                        Thread.sleep(1000);
                        errorCount++;
                        continue;
                    }
                    break;
                }

                if (Helper.StringIsNullOrEmpty(deviceId)) {
                    doPairFailure(new UnknownException());
                    return;
                }
                int p = deviceId.indexOf('/');
                if (p < 0) {
                    doPairFailure(new UnknownException());
                } else {
                    String type = deviceId.substring(0, p);
                    if (Helper.StringIsNullOrEmpty(type)) {
                        doPairFailure(new UnknownException());
                        return;
                    }
                    device.Type = type;
                }
                //Authorize();
//                if (Helper.StringIsNullOrEmpty(deviceId)) {
//                    doPairFailure(null);
//                }
                MXChipIO io = OznerDeviceManager.Instance().ioManagerList().mxChipIOManager().
                        createMXChipDevice(deviceMAC, device.Type);
                io.name = device.name;
                if (io != null) {
//                    doComplete(io);
                    pairHander.sendMessage(PairHander.COMPLETE, io);
                } else {
                    pairHander.sendMessage(PairHander.FAILURE, new Exception(""));
//                    doPairFailure(null);
                }
            } catch (Exception e) {
                pairHander.sendMessage(PairHander.FAILURE, e);
//                doPairFailure(e);
            } finally {
                Log.e(TAG, "mx_run: 结束");
            }
        }

        class SearchCallback extends SearchDeviceCallBack {

            @Override
            public void onDevicesFind(int code, JSONArray deviceStatus) {
                Log.e(TAG, "onDevicesFind: isComplete:" + isComplete);
                Log.e(TAG, "SearchCallback_onDevicesFind: " + deviceStatus);
                if (isComplete) return;
                for (int i = 0; i < deviceStatus.length(); i++) {
                    try {
                        JSONObject object = deviceStatus.getJSONObject(i);
                        if (object.getString("Port").equals("8080")) {
                            deviceMAC = object.getString("MAC");
                            device.localIP = object.getString("IP");
                            set();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Log.e(TAG, "SearchCallback_onDevicesFind_Ex: " + ex.getMessage());
                    }
                }
                super.onDevicesFind(code, deviceStatus);
            }
        }
    }


    public void pair(String ssid, String password, FogPairType pairType) throws PairRunningException {
        Log.e(TAG, "pair: ssid:" + ssid + ",password:" + password);
        errorCount = 0;
        isComplete = false;
        isAcitving = false;
        this.ssid = ssid;
        this.password = password;
        startRunTime = new Date();

        fogPairImp.init(ssid, password, pairType);

        runNext();
    }

    private void runNext() {

        Date now = new Date();
        if ((now.getTime() - startRunTime.getTime()) > 2 * 60 * 1000) {
            if (fogPairImp.isPairing()) {
                fogPairImp.stop();
            }
            doPairFailure(new TimeoutException());
            return;
        }

        Log.e(TAG, "runNext:isPairing: " + fogPairImp.isPairing());
        Log.e(TAG, "runNext: isAcitiving:" + isAcitving);
        Log.e(TAG, "runNext: isComplete:" + isComplete);
        if (!isComplete) {
            if (!isAcitving) {
                Log.e(TAG, "runNext: 1.0配网");
                ThreadManager.getInstance().execute(new MXChipPairImp());
            }
            if (!fogPairImp.isPairing()) {
                Log.e(TAG, "runNext: 2.0配网");
                fogPairImp.start();
            }
        }

    }

    public void stop() {

        Log.e(TAG, "stop: 停止配网");
        boolean stopSucc = fogPairImp.stop();
        Log.e(TAG, "fogPairImp_stop: " + stopSucc);
        ThreadManager.getInstance().shutdownNow();
    }

    Object obj = new Object();

    /**
     * 处理配网进度回调
     */
    public class PairHander extends ThreadHandler {
        public static final int START_AYLA_PAIR = 1;//开始ayla配网
        public static final int START_MX_PAIR = 2;//开始庆科配网，包括1.0和2.0
        public static final int SEND_CONFIG = 3;//发送配置信息
        public static final int WAIT_CONNECT_WIFI = 4;//等待连接WIFI
        public static final int ACTIVATE_DEVICE = 5;//激活设备
        public static final int COMPLETE = 6;//配网成功
        public static final int FAILURE = 7;//配网失败

        /**
         * 内部发送信息
         *
         * @param what
         * @param obj
         */
        public void sendMessage(int what, Object obj) {
            Message msg = this.obtainMessage();
            msg.what = what;
            msg.obj = obj;
            msg.sendToTarget();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_AYLA_PAIR:
                    if (callback != null) {
                        callback.onStartPairAyla();
                    }
                    break;
                case START_MX_PAIR:
                    Log.e(TAG, "handleMessage: 开始配网");
                    if (callback != null) {
                        callback.onStartPariMxChip();
                    }
                    break;
                case SEND_CONFIG:
                    Log.e(TAG, "handleMessage: 发送设置");
                    if (callback != null) {
                        callback.onSendConfiguration();
                    }
                    break;
                case WAIT_CONNECT_WIFI:
                    Log.e(TAG, "handleMessage: 等待连接");
                    if (callback != null) {
                        callback.onWaitConnectWifi();
                    }
                    break;
                case ACTIVATE_DEVICE:
                    Log.e(TAG, "handleMessage: 激活设备");
                    synchronized (obj) {
                        if (!isAcitving) {
                            isAcitving = true;
                            if (callback != null) {
                                callback.onActivateDevice();
                            }
                        }
                    }
                    break;
                case COMPLETE:
                    synchronized (obj) {
                        if (!isComplete) {
                            isAcitving = false;
                            isComplete = true;
                            Log.e(TAG, "handleMessage: 配网成功");
                            try {
                                BaseDeviceIO io = (BaseDeviceIO) msg.obj;
                                if (callback != null) {
                                    callback.onPairComplete(io);
                                }
                            } catch (Exception ex) {
                                if (callback != null) {
                                    callback.onPairComplete(null);
                                }
                            }
                        }
                        stop();
                    }
                    break;
                case FAILURE:
                    synchronized (obj) {
                        if (!isComplete) {
                            isComplete = true;
                            isAcitving = false;
                            Log.e(TAG, "handleMessage: 配网失败");
                            Exception ex = (Exception) msg.obj;
                            if (callback != null) {
                                callback.onPairFailure(ex);
                            }
                        }
                        stop();
                    }
                    break;
            }
        }
    }

}
