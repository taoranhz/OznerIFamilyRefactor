package com.ozner.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.ozner.device.BaseDeviceIO;
import com.ozner.device.DeviceNotReadyException;
import com.ozner.device.OperateCallback;
import com.ozner.util.dbg;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * 蓝牙IO接口
 * Created by zhiyongxu on 15/10/28.
 */
public class BluetoothIO extends BaseDeviceIO {
    //    /**
//     * 连接中
//     */
//    public final static int STATE_CONNECTING = BluetoothGatt.STATE_CONNECTING;
//    /**
//     * 已连接
//     */
//    public final static int STATE_CONNECTED = BluetoothGatt.STATE_CONNECTED;
//    /**
//     * 连接断开
//     */
//    public final static int STATE_DISCONNECTED = BluetoothGatt.STATE_DISCONNECTED;
//    /**
//     * 关闭中
//     */
//    public final static int STATE_DISCONNECTING = BluetoothGatt.STATE_DISCONNECTING;
    byte[] scanResponseData = null;
    int scanResponseType = 0;
    BluetoothScanResponse scanResponse;

    public BluetoothScanResponse getScanResponse() {
        return scanResponse;
    }

    BluetoothDevice device;
    BluetoothProxy bluetoothProxy;
    String Platform = "";
    long Firmware = 0;

    public BluetoothIO(Context context, BluetoothDevice device, String Model, String Platform, long Firmware) {
        super(context, Model);
        this.device = device;
        this.Firmware = Firmware;
        this.Platform = Platform;
        bluetoothProxy = new BluetoothProxy();
    }

    public static byte[] makePacket(byte opCode, byte[] data) {
        ByteBuffer buffer = ByteBuffer.allocate(data == null ? 1 : data.length + 1);
        buffer.put(opCode);
        if (data != null) {
            buffer.put(data);
        }
        return buffer.array();
    }

    public long getFirmware() {
        return Firmware;
    }
    public void setInfo(String platform,long firmware)
    {
        this.Platform=platform;
        this.Firmware=firmware;
    }

    public String getPlatform() {
        return Platform;
    }

    public void updateScanResponse(BluetoothScanResponse scanResponse) {
        this.scanResponseType = scanResponse.ScanResponseType;
        this.scanResponseData = scanResponse.ScanResponseData;

        this.scanResponse=scanResponse;
    }

    public int getScanResponseType() {
        return scanResponseType;
    }

    public byte[] getScanResponseData() {
        return scanResponseData;
    }



    @Override
    public boolean send(byte[] bytes) {
        return bluetoothProxy.postSend(bytes, null);
    }

    @Override
    public boolean send(byte[] bytes, OperateCallback<Void> callback) {
        return bluetoothProxy.postSend(bytes, callback);
    }

    /**
     * 设置一个循环发送runnable,来执行发送大数据包,比如挂件升级过程
     */
    public boolean post(BluetoothRunnable runnable) {
        return bluetoothProxy.postRunable(runnable);
    }

    @Override
    public void close() {
        bluetoothProxy.close();
    }

    @Override
    public void open() throws DeviceNotReadyException {
        bluetoothProxy.start();
    }

    @Override
    public String getAddress() {
        return device.getAddress();
    }


    @Override
    public ConnectStatus connectStatus() {
        if (bluetoothProxy.connectionState == BluetoothGatt.STATE_CONNECTING)
            return ConnectStatus.Connecting;
        if (bluetoothProxy.connectionState == BluetoothGatt.STATE_CONNECTED) {
            if (isReady())
                return ConnectStatus.Connected;
            else
                return ConnectStatus.Connecting;
        } else
            return ConnectStatus.Disconnect;
    }



    @Override
    protected void doChangeRunningMode() {
        if (getRunningMode() == RunningMode.Background) {
            //如果是后台模式,退出LOOP消息循环
            if (bluetoothProxy.mLooper != null) {
                bluetoothProxy.mLooper.quitSafely();
            }
        }
    }

    public interface BluetoothRunnable {
        void run();
    }

    class AsyncObject {
        public byte[] data;
        public OperateCallback<Void> callback;

        public AsyncObject(byte[] data, OperateCallback<Void> callback) {
            this.data = data;
            this.callback = callback;
        }

    }



    private class BluetoothProxy extends BluetoothGattCallback implements Runnable {
        final static int MSG_SendData = 0x1000;
        final static int MSG_Runnable = 0x2000;
        private static final int ServiceId = 0xFFF0;
        final UUID Characteristic_Input = BluetoothHelper.GetUUID(0xFFF2);
        final UUID Characteristic_Output = BluetoothHelper.GetUUID(0xFFF1);
        final UUID GATT_CLIENT_CHAR_CFG_UUID = BluetoothHelper.GetUUID(0x2902);

        Thread thread = null;
        BluetoothGattCharacteristic mInput = null;
        BluetoothGattCharacteristic mOutput = null;
        BluetoothGattService mService = null;
        BluetoothGatt mGatt = null;
        Looper mLooper;
        MessageHandler mHandler;

        private int connectionState = BluetoothGatt.STATE_DISCONNECTED;
        private int lastStatus = BluetoothGatt.GATT_FAILURE;



        private boolean checkStatus() {
            return (connectionState == BluetoothGatt.STATE_CONNECTED) && (lastStatus == BluetoothGatt.GATT_SUCCESS);
        }


        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            try {
                connectionState = newState;
                lastStatus = status;
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    doConnected();
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    doDisconnected();
                    close();
                }
                setObject();
                super.onConnectionStateChange(gatt, status, newState);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            lastStatus = status;
            mService = gatt.getService(BluetoothHelper.GetUUID(ServiceId));
            if (mService != null) {
                mInput = mService.getCharacteristic(Characteristic_Input);
                mOutput = mService.getCharacteristic(Characteristic_Output);

                mInput.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            }
            setObject();
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (descriptor.getUuid().equals(GATT_CLIENT_CHAR_CFG_UUID)) {
                lastStatus = status;
                setObject();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            doSend(characteristic.getValue());
            lastStatus = status;
            setObject();

        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            doRecv(characteristic.getValue());
            super.onCharacteristicChanged(gatt, characteristic);
        }


        private boolean connect() throws InterruptedException {
            dbg.i("开始连接:%s", device.getAddress());
            if (mGatt.connect()) {
                waitObject(10000);
                if (connectionState != BluetoothGatt.STATE_CONNECTED) {
                    return false;
                }
            } else {
                return false;
            }
            return true;
        }

        private boolean discoverServices() throws InterruptedException {
            dbg.i("开始发现:%s", device.getAddress());

            if (mGatt.discoverServices()) {
                waitObject(10000);
                if ((mInput == null) || (mOutput == null) || (!checkStatus())) {
                    return false;
                }
            } else {
                return false;
            }
            return true;
        }

        private boolean setNotification() throws InterruptedException {
            dbg.i("开始设置通知:%s", device.getAddress());
            BluetoothGattDescriptor desc = mOutput.getDescriptor(GATT_CLIENT_CHAR_CFG_UUID);
            if (desc != null) {
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mGatt.setCharacteristicNotification(mOutput, true);
                if (!mGatt.writeDescriptor(desc)) {
                    return false;
                }
                waitObject(10000);
                if (!checkStatus()) {
                    return false;
                }
            } else {
                return false;
            }
            return true;
        }


        public boolean isRuning() {
            return thread != null && thread.isAlive();
        }

        public void start() throws DeviceNotReadyException {
            if (BluetoothSynchronizedObject.hashBluetoothBusy(device.getAddress()))
                throw new DeviceNotReadyException();
            if (isRuning()) {
                throw new DeviceNotReadyException();
            }
            thread = new Thread(this);
            thread.start();
        }

        public void close() {
            if (isRuning()) {
                setObject();
                if (mLooper != null)
                    mLooper.quit();
            }
        }

        /**
         * 主运行循环
         */
        @Override
        public void run() {
            if (BluetoothSynchronizedObject.hashBluetoothBusy(device.getAddress())) return;
            mGatt = device.connectGatt(context(), false, this);
            if (mGatt == null) return;
            try {
                BluetoothSynchronizedObject.Busy(device.getAddress());
                synchronized (BluetoothSynchronizedObject.getLockObject()) {
                    doConnecting();
                    if (connect()) {
                        dbg.i("连接成功:%s", device.getAddress());
                    } else {
                        dbg.w("连接失败:%s", device.getAddress());
                        return;
                    }
                    if (discoverServices()) {
                        dbg.i("发现成功:%s", device.getAddress());
                    } else {
                        dbg.w("发现失败:%s", device.getAddress());
                        return;
                    }

                    if (setNotification()) {
                        dbg.i("通知设置成功:%s", device.getAddress());
                    } else {
                        dbg.w("通知设置失败:%s", device.getAddress());
                        return;
                    }
                    Thread.sleep(100);
                    if (!doInit()) {
                        return;
                    }
                }
                dbg.i("初始化成功:%s", device.getAddress());

                if (getRunningMode() == RunningMode.Foreground) {
                    //连接完成以后建立一个HANDLE来接受发送的数据
                    Looper.prepare();
                    mLooper = Looper.myLooper();
                    mHandler = new MessageHandler(mLooper);
                    doReady();
                    Looper.loop();
                } else {
                    doReady();
                    while (true)
                    {
                        //每次等待5秒
                        Thread.sleep(5000);
                        if (doCheckTransmissionsComplete())
                            break;
                    }
                }

            } catch (Exception e) {
                dbg.i("线程错误:" + getAddress());
                e.printStackTrace();
            } finally {
                dbg.i("连接关闭:" + getAddress());
                BluetoothSynchronizedObject.Idle(device.getAddress());
                if (mGatt != null) {
                    mGatt.disconnect();
                    mGatt.close();
                    mLooper = null;
                    mHandler = null;
                    //mGatt = null;
                }
                doDisconnected();

            }
        }

        protected boolean send(byte[] data) throws InterruptedException {
            if (data == null) return false;
            if (mGatt == null) return false;
            mInput.setValue(data);
            synchronized (BluetoothSynchronizedObject.getLockObject()) {
                if (!mGatt.writeCharacteristic(mInput)) {
                    return false;
                }
            }
            waitObject(10000);
            //Thread.sleep(100);
            return checkStatus();
        }

        /**
         * 设置一个循环发送runnable,来执行发送大数据包,比如挂件升级过程
         */
        public boolean postRunable(BluetoothRunnable runnable) {
            if (mHandler == null) return false;
            Message message = new Message();
            message.what = MSG_Runnable;
            message.obj = runnable;
            return mHandler.sendMessage(message);
        }

        public boolean postSend(byte[] data, OperateCallback<Void> callback) {

            if ((thread!=null) && (Thread.currentThread().getId() == thread.getId()))  {
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
                if (mHandler != null) {
                    Message message = new Message();
                    message.what = MSG_SendData;
                    message.obj = new AsyncObject(data, callback);
                    return mHandler.sendMessage(message);
                } else
                    return false;
            }

        }

        private class MessageHandler extends Handler {
            public MessageHandler(Looper looper) {
                super(looper);
            }

            @Override
            public void handleMessage(Message msg) {
                try {
                    if (msg.what == MSG_SendData) {
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


                    } else if (msg.what == MSG_Runnable) {
                        BluetoothRunnable runable = (BluetoothRunnable) msg.obj;
                        runable.run();
                    }

                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                super.handleMessage(msg);
            }
        }


    }
}
