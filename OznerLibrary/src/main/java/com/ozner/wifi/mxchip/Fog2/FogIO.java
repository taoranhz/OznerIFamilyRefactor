package com.ozner.wifi.mxchip.Fog2;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.ozner.device.BaseDeviceIO;
import com.ozner.device.DeviceNotReadyException;
import com.ozner.device.OperateCallback;
import com.ozner.util.Convert;
import com.ozner.util.Helper;
import com.ozner.util.HttpUtil;
import com.ozner.wifi.ThreadHandler;
import com.ozner.wifi.mxchip.IMQTTListener;
import com.ozner.wifi.mxchip.MQTTProxyFog;
import com.ozner.wifi.mxchip.MXRunnable;
import com.ozner.wifi.mxchip.SMQTTProxy;

import org.fusesource.mqtt.client.Callback;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by ozner_67 on 2017/5/22.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class FogIO extends BaseDeviceIO {
    private static final String TAG = "FogIO";
    final FogIOImp mFogIoImp = new FogIOImp();
    //    FogSearchDeviceInfo deviceInfo;
    MQTTProxyFog proxy;
    String address = "";
    String deviceId = "";
    String mTopic;
    private ExecutorService mCacheExecutor = null;

    public FogIO(Context context, MQTTProxyFog proxy, String type, String mac, String deviceid) {
        super(context, type);
        mCacheExecutor = Executors.newCachedThreadPool();
        this.proxy = proxy;
//        this.deviceInfo = deviceInfo;
        this.address = mac;
        this.deviceId = deviceid;
        Log.e(TAG, "deviceID:" + deviceid);
        //设置订阅消息主题
        mTopic = "d2c_hz/" + deviceId + "/status";
        proxy.registerListener(mFogIoImp);
    }

    public void setSecureCode(String noneUse) {

    }


    @Override
    public boolean send(byte[] bytes) {
        return mFogIoImp.postSend(bytes, null);
    }

    @Override
    public boolean send(byte[] bytes, OperateCallback<Void> callback) {
        return mFogIoImp.postSend(bytes, callback);
    }

    @Override
    public void close() {
        mFogIoImp.close();
    }

    @Override
    public void open() throws DeviceNotReadyException {
        if (Helper.StringIsNullOrEmpty(mTopic))
            throw new DeviceNotReadyException();
        mFogIoImp.start();
    }

    public boolean post(MXRunnable runnable){
        return mFogIoImp.postRunable(runnable);
    }

    @Override
    public ConnectStatus connectStatus() {
        if (proxy.isConnected()) {
            if (isReady()) {
                return ConnectStatus.Connected;
            } else {
                return ConnectStatus.Connecting;
            }
        } else {
            return ConnectStatus.Disconnect;
        }
    }

    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public String getAddress() {
        return address;
    }


    class FogIOImp implements IMQTTListener, Runnable {
        final static int MSG_SendData = 0x1000;
        final static int MSG_Runnable = 0x2000;
        final static int Timeout = 10000;
        Thread thread = null;
        Looper looper = null;
        MessageHandler handler = null;
        private boolean succeed = false;

        public FogIOImp() {
            handler = new MessageHandler();
        }

        private boolean send(final byte[] data) throws InterruptedException {
            if (data == null) return false;
            try {
                Future<Boolean> result = mCacheExecutor.submit(new SendCallable(data));
                return result.get();
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }

        public boolean isRuning() {
            return thread != null && thread.isAlive();
        }

        public void start() throws DeviceNotReadyException {
            if (isRuning()) {
                throw new DeviceNotReadyException();
            }
            thread = new Thread(this);
            thread.start();
        }

        public void close() {
            if (isRuning()) {

                proxy.unregisterListener(mFogIoImp);
                setObject();
                if (looper != null)
                    looper.quit();
                thread.interrupt();
            }
        }

        @Override
        public void onConnected(SMQTTProxy proxy) {

        }

        @Override
        public void onDisconnected(SMQTTProxy proxy) {
            close();
        }

        @Override
        public void onPublish(SMQTTProxy proxy, String topic, byte[] data) {
            if (topic.equals(mTopic)) {
                doRecv(data);
            }
        }

        @Override
        public void run() {
            try {
                doConnecting();
                if (!proxy.isConnected()) return;
                try {
                    succeed = true;
                    proxy.subscribe(mTopic, new Callback<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            succeed = true;
                            setObject();
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            succeed = false;
                            setObject();
                        }
                    });
                    waitObject(10000);
                    if (!succeed) return;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }

                if (!doInit()) {
                    return;
                }
                doReady();
                Looper.prepare();
                Looper.loop();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                proxy.unsubscribe(mTopic);
                doDisconnected();
            }

        }

        /**
         * 设置一个循环发送runnable,来执行发送大数据包,比如挂件升级过程
         */
        public boolean postRunable(MXRunnable runnable) {
            if (handler == null) return false;
            Message message = new Message();
            message.what = MSG_Runnable;
            message.obj = runnable;
            return handler.sendMessage(message);
        }

        public boolean postSend(byte[] data, OperateCallback<Void> callback) {
            if (Thread.currentThread().getId() == thread.getId()) {
                try {
                    if (send(data)) {
                        if (callback != null) {
                            callback.onSuccess(null);
                        }
                        return true;
                    } else {
                        if (callback != null) {
                            callback.onFailure(null);
                        }
                        return false;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailure(e);
                    return false;
                }
            } else {
                if (handler != null) {
                    Message message = new Message();
                    message.what = MSG_SendData;
                    message.obj = new AsyncObject(data, callback);
                    return handler.sendMessage(message);
                } else {
                    return false;
                }
            }
        }

        class SendCallable implements Callable<Boolean> {
            private byte[] data;

            public SendCallable(byte[] sendData) {
                this.data = sendData;
            }

            @Override
            public Boolean call() throws Exception {

                String url = "https://v2.fogcloud.io/enduser/sendCommandHz/";
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("username=bing.zhao@cftcn.com")
                        .append("&password=l5201314&deviceid=")
                        .append(deviceId)
                        .append("&payload=")
                        .append(Convert.ByteArrayToHexString(data).toLowerCase());
//                String parms = "username=bing.zhao@cftcn.com&password=l5201314&deviceid=" + deviceInfo.deviceId + "&payload=" + Convert.ByteArrayToHexString(data).toLowerCase();
                String res = HttpUtil.doPost(url, stringBuffer.toString());//res:{"meta":{"message":"Send command successfully","code":0},"data":{}}
                if (res != null) {
                    try {
                        JSONObject object = new JSONObject(res);
                        if (object.getJSONObject("meta").getInt("code") == 0) {
                            succeed = true;
                        } else {
                            succeed = false;
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                        succeed = false;
                    }
                } else {
                    succeed = false;
                }
                return succeed;
            }
        }


        class MessageHandler extends ThreadHandler {
            @Override
            public void handleMessage(Message msg) {
                try {
                    switch (msg.what) {
                        case MSG_SendData:
                            AsyncObject object = (AsyncObject) msg.obj;
                            try {
                                if (send(object.data)) {
                                    if (object.callback != null)
                                        object.callback.onSuccess(null);
                                } else {
                                    if (object.callback != null)
                                        object.callback.onFailure(null);
                                }
                            } catch (Exception e) {
                                if (object.callback != null)
                                    object.callback.onFailure(e);
                                throw e;
                            }

                            break;
                        case MSG_Runnable:
                            MXRunnable runnable = (MXRunnable) msg.obj;
                            runnable.run();
                            break;
                    }
                    Thread.sleep(100);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                super.handleMessage(msg);
            }
        }

        class AsyncObject {
            public byte[] data;
            public OperateCallback<Void> callback;

            public AsyncObject(byte[] data, OperateCallback<Void> callback) {
                this.data = data;
                this.callback = callback;
            }

        }
    }

}
