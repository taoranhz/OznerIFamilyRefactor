package com.ozner.wifi.mxchip.Fog2;

import android.content.Context;
import android.util.Log;

import com.ozner.AirPurifier.AirPurifierManager;
import com.ozner.WaterPurifier.WaterPurifierManager;
import com.ozner.device.DeviceSetting;
import com.ozner.device.OznerDeviceManager;
import com.ozner.wifi.WifiPair;
import com.ozner.wifi.mxchip.ThreadManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import io.fog.fog2sdk.MiCODevice;
import io.fogcloud.easylink.helper.EasyLinkCallBack;
import io.fogcloud.fog_mdns.helper.SearchDeviceCallBack;

/**
 * Created by ozner_67 on 2017/5/16.
 * 邮箱：xinde.zhang@cftcn.com
 * <p>
 * 庆科2.0 配对
 */

public class FogPairImp {
    private static final String TAG = "FogPairImp";
    private final String serviceName = "_easylink._tcp.local.";
    private Context mContext;
    private String ssid;
    private String pwd;
    MiCODevice micoDev;
    private boolean isPairing = false;
    private HashMap<String, FogSearchDeviceInfo> devmap;
    //    private ExecutorService mCacheExecutor = null;
    private HashMap<String, SocketCallable> socketMap;
    private FogPairType pairType;

    private WifiPair.PairHander mPairHander;

    public FogPairImp(Context context, WifiPair.PairHander hander) {
        this.mContext = context;
        devmap = new HashMap<>();
        socketMap = new HashMap<>();
        micoDev = new MiCODevice(context);
        mPairHander = hander;
//        mCacheExecutor = Executors.newSingleThreadExecutor();
    }

    public void init(String ssid, String pwd, FogPairType pairType) {
        this.ssid = ssid;
        this.pwd = pwd;
        this.pairType = pairType;
    }

    public boolean hasDevice() {
        return devmap != null && devmap.size() > 0;

    }

    public MiCODevice getMicoDev() {
        return micoDev;
    }

    public boolean isPairing() {
        return isPairing;
    }

    /**
     * 启动EasyLink，向设备发送wifi账号密码
     *
     * @return
     */
    private boolean startEasyLink() {
        devmap.clear();
        if (micoDev == null) {
            isPairing = false;
            return false;
        }

        if (isPairing) {
            isPairing = false;
            stopSearchService();
        }

        if (mPairHander != null) {
            mPairHander.sendEmptyMessage(WifiPair.PairHander.START_MX_PAIR);
        }
        isPairing = true;
        micoDev.startEasyLink(ssid, pwd, true, 40000, 20, "", "", new EasyLinkCallBack() {
            @Override
            public void onSuccess(int code, String message) {
                isPairing = true;
                Log.e(TAG, "startEasyLink_onSuccess: code:" + code + " ,msg:" + message);
                if (code == 0) {
                    if (mPairHander != null) {
                        mPairHander.sendEmptyMessage(WifiPair.PairHander.WAIT_CONNECT_WIFI);
                    }
                    isPairing = startSearchService(new StartSearchCallback());
                } else {
                    isPairing = false;
                    if (mPairHander != null) {
                        mPairHander.sendMessage(WifiPair.PairHander.FAILURE, new Exception("startEasyLink_success, " + code + "," + message));
                    }
                }
            }

            @Override
            public void onFailure(int code, String message) {
                isPairing = false;
                Log.e(TAG, "startEasyLink_onFailure: code:" + code + " , msg:" + message);
                if (mPairHander != null) {
                    mPairHander.sendMessage(WifiPair.PairHander.FAILURE, new Exception("startEasyLink_failure, " + code + "," + message));
                }
            }
        });
        return true;
    }

    /**
     * 启动设备搜索
     *
     * @return
     */
    public boolean startSearchService(SearchDeviceCallBack callBack) {
        if (micoDev == null) {
            isPairing = false;
            return false;
        }
        if (callBack == null) {
            return false;
        }
        micoDev.startSearchDevices(serviceName, callBack);
        return true;
    }

//    public boolean startSearchService() {
//        if (micoDev == null) {
//            isPairing = false;
//            return false;
//        }
//        micoDev.startSearchDevices(serviceName, new SearchDeviceCallBack() {
//            @Override
//            public void onSuccess(int code, String message) {
//                isPairing = true;
//                Log.e(TAG, "SearchDevices_onSuccess: code:" + code + " , msg:" + message);
//            }
//
//            @Override
//            public void onFailure(int code, String message) {
//                isPairing = false;
//                Log.e(TAG, "SearchService_onFailure: code:" + code + " , msg:" + message);
//                if (mPairHander != null) {
//                    mPairHander.sendMessage(WifiPair.PairHander.FAILURE, new Exception("searchDevice_Failure ," + code + "," + message));
//                }
//            }
//
//            @Override
//            public void onDevicesFind(int code, JSONArray deviceStatus) {
//                Log.e(TAG, "SearchService_onDevicesFind: code:" + code + " ,deviceStatus:" + deviceStatus.toString());
//                if (deviceStatus != null && deviceStatus.length() > 0) {
//
//                    for (int i = 0; i < deviceStatus.length(); i++) {
//                        try {
//                            JSONObject obj = deviceStatus.getJSONObject(i);
//                            String mac = obj.getString("MAC");
//                            if (!devmap.containsKey(mac)) {
//                                FogSearchDeviceInfo fogDev = new FogSearchDeviceInfo();
//                                fogDev.fromJSONObject(obj);
//                                devmap.put(mac, fogDev);
//                            }
//                        } catch (Exception ex) {
//                            Log.e(TAG, "startSearchDevices_onDevicesFind_ex: " + ex.getMessage());
//                        }
//                    }
//
//                    if (devmap.size() > 0) {
//                        for (FogSearchDeviceInfo dev : devmap.values()) {
//                            if(OznerDeviceManager.Instance().getDevice(dev.MAC)!=null){
//                                continue;
//                            }
//                            if (dev.deviceId == null && dev.Port.equals("8002")) {
//                                if (mPairHander != null) {
//                                    mPairHander.sendEmptyMessage(WifiPair.PairHander.ACTIVATE_DEVICE);
//                                }
////
//                                //激活设备
//                                dev.deviceId = activeDevice(dev);
//                                if (dev.deviceId != null) {
////                                    isPairing = false;
//                                    stopSearchService();
//                                }
//                                Log.e(TAG, "onDevicesFind: deviceId:" + dev.deviceId);
//                            }
//                            if (mPairHander != null && dev.Port.equals("8002")) {
//                                if (dev.deviceId != null) {
//                                    DeviceSetting setting = new DeviceSetting();
//                                    setting.deviceId(dev.deviceId);
//                                    Log.e(TAG, "onDevicesFind:deviceSetting:" + setting.toString());
//                                    FogIO deviceIO = OznerDeviceManager.Instance().ioManagerList().fogIOManager()
//                                            .createFogDevice(dev.MAC, dev.Firmware_Rev.substring(0, dev.Firmware_Rev.indexOf("@")), setting.toString());
//                                    mPairHander.sendMessage(WifiPair.PairHander.COMPLETE, deviceIO);
//                                } else {
//                                    mPairHander.sendMessage(WifiPair.PairHander.FAILURE, new Exception("device id is null when active"));
//                                }
//                            }
//
//                        }
//                    }
//                }
//            }
//        });
//        return true;
//    }

    public void start() {
        startEasyLink();
    }

//    @Override
//    public void run() {
//        startEasyLink();
//        while (isPairing) ;
//    }

    public boolean stop() {
//        if (!mCacheExecutor.isShutdown())
//            mCacheExecutor.shutdown();
        return stopSearchService();
    }


    private boolean stopSearchService() {
        if (micoDev == null)
            return false;
        micoDev.stopSearchDevices(new SearchDeviceCallBack() {
            @Override
            public void onSuccess(int code, String message) {
                Log.e(TAG, "stopSearchDevices_onSuccess: code:" + code + " , msg:" + message);
                isPairing = false;
                micoDev.stopEasyLink(new EasyLinkCallBack() {
                    @Override
                    public void onSuccess(int code, String message) {
                        isPairing = false;
                    }

                    @Override
                    public void onFailure(int code, String message) {
                    }
                });
            }

            @Override
            public void onFailure(int code, String message) {
                Log.e(TAG, "stopSearchDevices_onFailure: code:" + code + " , msg:" + message);
//                isPairing = false;
                super.onFailure(code, message);
            }

            @Override
            public void onDevicesFind(int code, JSONArray deviceStatus) {
                super.onDevicesFind(code, deviceStatus);
            }
        });
        return true;
    }

    /**
     * 激活设备
     *
     * @param fogDevice
     */
    private String activeDevice(FogSearchDeviceInfo fogDevice) {
        try {
//            new SocketThread(fogDevice).start();
            if (!socketMap.containsKey(fogDevice.MAC)) {
                SocketCallable ra = new SocketCallable(fogDevice);
                socketMap.put(fogDevice.MAC, ra);
//                Future fu = mCacheExecutor.submit(ra);
                Future fu = ThreadManager.getInstance().submit(ra);
                socketMap.remove(fogDevice.MAC);
                return fu != null ? (String) fu.get() : null;
            }
        } catch (Exception ex) {
            Log.e(TAG, "activeDevice_Ex: " + ex.getMessage());
        }
        return null;
    }

    /**
     * 搜索设备回调
     */
    class StartSearchCallback extends SearchDeviceCallBack {
        @Override
        public void onSuccess(int code, String message) {
            isPairing = true;
            Log.e(TAG, "SearchDevices_onSuccess: code:" + code + " , msg:" + message);
        }

        @Override
        public void onFailure(int code, String message) {
            isPairing = false;
            Log.e(TAG, "SearchService_onFailure: code:" + code + " , msg:" + message);
            if (mPairHander != null) {
                mPairHander.sendMessage(WifiPair.PairHander.FAILURE, new Exception("searchDevice_Failure ," + code + "," + message));
            }
        }

        @Override
        public void onDevicesFind(int code, JSONArray deviceStatus) {
            Log.e(TAG, "SearchService_onDevicesFind: code:" + code + " ,deviceStatus:" + deviceStatus.toString());
            Log.e(TAG, "SearchService_onDevicesFind: isPairing:" + isPairing);
            if (!isPairing) return;
            if (deviceStatus != null && deviceStatus.length() > 0) {

                for (int i = 0; i < deviceStatus.length(); i++) {
                    try {
                        JSONObject obj = deviceStatus.getJSONObject(i);
                        String mac = obj.getString("MAC");
                        if (!devmap.containsKey(mac)) {
                            FogSearchDeviceInfo fogDev = new FogSearchDeviceInfo();
                            fogDev.fromJSONObject(obj);
                            devmap.put(mac, fogDev);
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "startSearchDevices_onDevicesFind_ex: " + ex.getMessage());
                    }
                }

                if (devmap.size() > 0) {
                    for (FogSearchDeviceInfo dev : devmap.values()) {
                        if (OznerDeviceManager.Instance().getDevice(dev.MAC) != null) {
                            continue;
                        }

                        if (dev.deviceId == null && dev.Port.equals("8002")) {
                            if (pairType == FogPairType.FOG_AIR) {
                                if (!AirPurifierManager.isFogDevice(dev.FogProductId)) {
                                    continue;
                                }
                            } else if (pairType == FogPairType.FOG_WATER) {
                                if (!WaterPurifierManager.isFogDevice(dev.FogProductId)) {
                                    continue;
                                }
                            }

                            if (mPairHander != null) {
                                mPairHander.sendEmptyMessage(WifiPair.PairHander.ACTIVATE_DEVICE);
                            }
                            //激活设备
                            dev.deviceId = activeDevice(dev);
                            if (dev.deviceId != null) {
                                stopSearchService();
                            }
                            Log.e(TAG, "onDevicesFind: deviceId:" + dev.deviceId);
                        }
                        if (mPairHander != null && dev.Port.equals("8002")) {
                            if (dev.deviceId != null) {
                                DeviceSetting setting = new DeviceSetting();
                                setting.deviceId(dev.deviceId);
                                FogIO deviceIO = OznerDeviceManager.Instance().ioManagerList().fogIOManager()
                                        .createFogDevice(dev.MAC, dev.FogProductId, setting.toString());
//                                        .createFogDevice(dev.MAC, dev.Firmware_Rev.substring(0, dev.Firmware_Rev.indexOf("@")), setting.toString());
                                if (deviceIO != null) {
                                    deviceIO.name = dev.Name.replace(" Module#", "(") + ")";
                                    mPairHander.sendMessage(WifiPair.PairHander.COMPLETE, deviceIO);
                                } else {
                                    mPairHander.sendMessage(WifiPair.PairHander.FAILURE, new Exception("Create deviceIO fail"));
                                }
                            } else {
                                mPairHander.sendMessage(WifiPair.PairHander.FAILURE, new Exception("device id is null when active"));
                            }
                        }

                    }
                }
            }
        }
    }


    class SocketCallable implements Callable<String> {
        FogSearchDeviceInfo devInfo;
        BufferedReader reader = null;
        BufferedWriter writer = null;
        Socket socket;
        int retryCount = 0;

        public SocketCallable(FogSearchDeviceInfo dev) {
            this.devInfo = dev;
        }

        @Override
        public String call() throws Exception {
            String deviceId = null;
            try {
                Log.e(TAG, "SocketCallable_call: 开始激活");
                do {
                    retryCount++;
                    Log.e(TAG, "run: 重试次数：" + retryCount);
                    socket = new Socket(devInfo.IP, Integer.parseInt(devInfo.Port));
                    Log.e(TAG, "call: " + socket.toString());
                    socket.setSoTimeout(10000);
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"));
                    if (socket.isClosed()) {
                        Log.e(TAG, "SocketThread_run: socket 关闭");
                        continue;
                    }
                    if (!socket.isConnected()) {
                        Log.e(TAG, "SocketThread_run: socket 断开连接 ");
                        continue;
                    }
                    if (socket.isOutputShutdown()) {
                        Log.e(TAG, "SocketThread_run: socket outputshutdown!!!!");
                        continue;
                    }

                    String writeData = "POST / HTTP/1.1\r\n\r\n{\"getvercode\":\"\"}\r\n";
                    writer.write(writeData);
                    writer.flush();

                    if (socket.isInputShutdown()) {
                        Log.e(TAG, "SocketThread_run: socket inputshutdown!!!! ");
                        continue;
                    }
                    String line = reader.readLine();
                    Log.e(TAG, "run: line:" + line);
                    StringBuffer content = new StringBuffer();
                    String deviceInfo = "";
                    while (line != null) {
                        content.append(line);
                        content.append("\n");
                        line = reader.readLine();
                        if (line != null && line.length() > 0 && line.contains("deviceid")) {
                            deviceInfo = line;
                        }
                        Log.e(TAG, "run: line:" + line);
                    }

                    Log.e(TAG, "content: " + content);
                    JSONObject jsonObj = new JSONObject(deviceInfo);
                    deviceId = jsonObj.getString("deviceid");
                    String devicepw = jsonObj.getString("devicepw");
                    String vercode = jsonObj.getString("vercode");
                    try {
                        if (socket != null && !socket.isClosed()) {
                            socket.close();
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "关闭异常: " + ex.getMessage());
                    }
                } while (deviceId == null && retryCount < 3);

            } catch (Exception ex) {
                Log.e(TAG, "SocketThread_run_Ex: " + ex.getMessage());
            } finally {
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "关闭异常: " + ex.getMessage());
                }
            }
            return deviceId;
        }
    }
}
