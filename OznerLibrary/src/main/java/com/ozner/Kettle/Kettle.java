package com.ozner.Kettle;

import android.content.Context;
import android.util.Log;

import com.ozner.bluetooth.BluetoothIO;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.DeviceSetting;
import com.ozner.device.OperateCallback;
import com.ozner.device.OznerDevice;
import com.ozner.oznerlibrary.R;
import com.ozner.util.ByteUtil;

/**
 * Created by ozner_67 on 2017/6/19.
 * 邮箱：xinde.zhang@cftcn.com
 * 电热壶
 */

public class Kettle extends OznerDevice {
    private static final String TAG = "Kettle";

    /**
     * 收到传感器数据
     */
    public final static String ACTION_BLUETOOTH_KETTLE_SENSOR = "com.ozner.kettle.bluetooth.sensor";

    private static final int defaultAutoUpdatePeriod = 5000;
    private static final byte opCode_ReadStatus = 0x20;//获取设备状态，APP->设备
    private static final byte opCode_ReadStatusRet = 0x21;//设备状态返回，设备->APP
    private static final byte opCode_SendSetting = 0x33;//设置，APP->设备
    private static final byte opCode_SendWorkMode = 0x34;//加热模式,APP->设备；0待机，1加热，2保温

//    private static final byte opCode_SendFirmware = (byte) 0xC1;//发送固件,APP->设备
//    private static final byte opCode_ClearUpdate = (byte) 0xC2;//清除固件升级区域,APP->设备
//    private static final byte opCode_UpdateFirmware = (byte) 0xC3;//升级固件,APP->设备

    private final KettleStatus mStatus = new KettleStatus();

    private KettleImp kettleImp = new KettleImp();

    public Kettle(Context context, String Address, String Type, String Setting) {
        super(context, Address, Type, Setting);
    }

    @Override
    public int getTimerDelay() {
        return defaultAutoUpdatePeriod;
    }

    @Override
    protected String getDefaultName() {
        return context().getString(R.string.kettle_name);
    }

    @Override
    public Class<?> getIOType() {
        return BluetoothIO.class;
    }

    /**
     * 设备状态
     *
     * @return
     */
    public KettleStatus status() {
        return mStatus;
    }

    @Override
    protected void doSetDeviceIO(BaseDeviceIO oldIO, BaseDeviceIO newIO) {
        if (oldIO != null) {
            oldIO.setOnInitCallback(null);
            oldIO.unRegisterStatusCallback(kettleImp);
            oldIO.setOnTransmissionsCallback(null);
            oldIO.setCheckTransmissionsCompleteCallback(null);
//            firmwareTools.bind(null);
        }
        if (newIO != null) {
            newIO.setOnTransmissionsCallback(kettleImp);
            newIO.setOnInitCallback(kettleImp);
            newIO.registerStatusCallback(kettleImp);
            newIO.setCheckTransmissionsCompleteCallback(kettleImp);
//            firmwareTools.bind((BluetoothIO) newIO);
        }
    }

    @Override
    protected DeviceSetting initSetting(String Setting) {
        DeviceSetting setting = new KettleSetting();
        setting.load(Setting);
        return setting;
    }

    @Override
    public void updateSettings() {
        if (IO() != null && IO().isReady()) {
            kettleImp.sendSetting(null);
        }
    }

    public KettleSetting getSetting() {
        return (KettleSetting) Setting();
    }

    /**
     * 设置保温时间
     *
     * @param minute
     * @param cb
     *
     * @return
     */
    public boolean setPreservationTime(int minute, OperateCallback<Void> cb) {
        if (IO() != null && IO().isReady()) {
            getSetting().preservationTime(minute);
            return kettleImp.sendSetting(cb);
        } else {
            if (cb != null) {
                cb.onFailure(null);
            }
            return false;
        }
    }

    /**
     * 设置保温温度
     *
     * @param temperature
     * @param cb
     *
     * @return
     */
    public boolean setPreservationTemperature(int temperature, OperateCallback<Void> cb) {
        if (IO() != null && IO().isReady()) {
            getSetting().preservationTemperature(temperature);
            return kettleImp.sendSetting(cb);
        } else {
            if (cb != null) {
                cb.onFailure(null);
            }
            return false;
        }
    }

    /**
     * 设置保温模式
     *
     * @param mode
     * @param cb
     *
     * @return
     */
    public boolean setPreservationMode(PreservationMode mode, OperateCallback<Void> cb) {
        if (IO() != null && IO().isReady()) {
            getSetting().preservationMode(mode);
            return kettleImp.sendSetting(cb);
        } else {
            if (cb != null) {
                cb.onFailure(null);
            }
            return false;
        }
    }

    /**
     * 设置预约分钟
     *
     * @param minute
     * @param cb
     *
     * @return
     */
    public boolean setAdvanceMinute(int minute, OperateCallback<Void> cb) {
        if (IO() != null && IO().isReady()) {
            getSetting().reservationTime(minute);
            return kettleImp.sendSetting(cb);
        } else {
            if (cb != null) {
                cb.onFailure(null);
            }
            return false;
        }
    }

    /**
     * 设置煮沸温度
     *
     * @param temperature
     * @param cb
     *
     * @return
     */
    public boolean setBolingTemperature(int temperature, OperateCallback<Void> cb) {
        if (IO() != null && IO().isReady()) {
            getSetting().boilingTemperature(temperature);
            return kettleImp.sendSetting(cb);
        } else {
            if (cb != null) {
                cb.onFailure(null);
            }
            return false;
        }
    }

    /**
     * 预约使能
     *
     * @param advance
     * @param cb
     *
     * @return
     */
    public boolean enableAdvance(boolean advance, OperateCallback<Void> cb) {
        if (IO() != null && IO().isReady()) {
            getSetting().reservationEnable(advance);
            return kettleImp.sendSetting(cb);
        } else {
            if (cb != null) {
                cb.onFailure(null);
            }
            return false;
        }
    }

    /**
     * 设备进入待机模式
     *
     * @return
     */
    public boolean setIdle(OperateCallback<Void> cb) {
        byte[] datas = new byte[2];
        datas[1] = 0;
        return kettleImp.send(opCode_SendWorkMode, datas, cb);

    }

    /**
     * 设备进入加入模式
     *
     * @return
     */
    public boolean setHeating(OperateCallback<Void> cb) {
        byte[] datas = new byte[2];
        datas[1] = 1;
        return kettleImp.send(opCode_SendWorkMode, datas, cb);
    }

    /**
     * 设置进入保温模式
     *
     * @return
     */
    public boolean setPreservation(OperateCallback<Void> cb) {
        byte[] datas = new byte[2];
        datas[1] = 2;
        return kettleImp.send(opCode_SendWorkMode, datas, cb);
    }

    @Override
    public String toString() {
        return mStatus.toString();
    }

    @Override
    protected void doTimer() {
        kettleImp.doTimer();
    }

    public static boolean isBindMode(BaseDeviceIO io){
        if(!KettleMgr.isKettle(io.getType())) return false;
        BluetoothIO bluetoothIO=(BluetoothIO)io;
        if (bluetoothIO.getScanResponseType()==0x20)
        {
            if ((bluetoothIO.getScanResponseData()!=null) && (bluetoothIO.getScanResponseData().length>1))
            {
                return bluetoothIO.getScanResponseData()[0]!=0;
            }
        }
        return false;
    }

    class KettleImp implements
            BluetoothIO.OnInitCallback,
            BluetoothIO.OnTransmissionsCallback,
            BluetoothIO.StatusCallback,
            BluetoothIO.CheckTransmissionsCompleteCallback {

        private boolean send(byte opCode, byte[] data, OperateCallback<Void> cb) {
            return IO() != null && IO().send(BluetoothIO.makePacket(opCode, data), cb);
        }

        public void doTimer() {
            if (IO() == null) return;
            requestStatus();
        }

        /**
         * 请求传感器
         */
        private boolean requestStatus() {
            return send(opCode_ReadStatus, null, null);
        }

        @Override
        public void onConnected(BaseDeviceIO io) {
            mStatus.reset();
        }

        @Override
        public void onDisconnected(BaseDeviceIO io) {
            mStatus.reset();
        }

        @Override
        public void onReady(BaseDeviceIO io) {
            mStatus.reset();
            requestStatus();
        }

        @Override
        public boolean CheckTransmissionsComplete(BaseDeviceIO io) {
            return false;
        }

        @Override
        public void onIOSend(byte[] bytes) {

        }

        public boolean sendSetting(OperateCallback<Void> cb) {
            KettleSetting setting = (KettleSetting) Setting();
            if (setting == null) {
                return false;
            }
            byte bytes[] = new byte[8];
            bytes[0] = (byte) setting.preservationTemperature();
            ByteUtil.putShort(bytes, (short) setting.preservationTime(), 1);
            bytes[3] = (byte) setting.boilingTemperature();
            if (setting.preservationMode() == PreservationMode.Boiling) {
                bytes[4] = 0;
            } else {
                bytes[4] = 1;
            }
            bytes[5] = (byte) (setting.reservationEnable() ? 1 : 0);
            ByteUtil.putShort(bytes, (short) setting.reservationTime(), 6);
            return this.send(opCode_SendSetting, bytes, cb);
        }


        @Override
        public void onIORecv(byte[] bytes) {
            if (bytes == null) return;
            if (bytes.length < 1) return;
            byte opCode = bytes[0];
//            byte[] data = null;
//            if (bytes.length > 1)
//                data = Arrays.copyOfRange(bytes, 1, bytes.length);

            switch (opCode) {
                case opCode_ReadStatusRet:
//                    synchronized (this) {
                    mStatus.load(bytes);
                    Log.e(TAG, "onIORecv: sensor:" + mStatus.toString());
//                    }
//                    Intent intent = new Intent(ACTION_BLUETOOTH_KETTLE_SENSOR);
//                    intent.putExtra("Address",IO().getAddress());
//                    intent.putExtra("Sensor",data);
//                    context().sendBroadcast(intent);
                    doUpdate();
                    break;
            }
        }

        @Override
        public boolean onIOInit() {
           return requestStatus();
//            return true;
        }
    }
}
