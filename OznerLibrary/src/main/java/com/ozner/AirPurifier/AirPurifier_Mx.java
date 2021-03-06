package com.ozner.AirPurifier;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OperateCallback;
import com.ozner.device.OperateCallbackProxy;
import com.ozner.oznerlibrary.R;
import com.ozner.util.ByteUtil;
import com.ozner.util.Helper;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by ozner_67 on 2017/6/12.
 * 邮箱：xinde.zhang@cftcn.com
 */

public abstract class AirPurifier_Mx extends AirPurifier {
    private static final String TAG = "AirPurifier_Mx";
    protected static final int defaultAutoUpdatePeriod = 5000;
    public static final byte CMD_SET_PROPERTY = (byte) 0x2;
    public static final byte CMD_REQUEST_PROPERTY = (byte) 0x1;
    public static final byte CMD_RECV_PROPERTY = (byte) 0x4;


    public static final byte PROPERTY_POWER = 0x00;
    public static final byte PROPERTY_SPEED = 0x01;
    public static final byte PROPERTY_LIGHT = 0x02;
    public static final byte PROPERTY_LOCK = 0x03;
    public static final byte PROPERTY_POWER_TIMER = 0x04;
    public static final byte PROPERTY_PM25 = 0x11;
    public static final byte PROPERTY_TEMPERATURE = 0x12;
    public static final byte PROPERTY_VOC = 0x13;
    public static final byte PROPERTY_LIGHT_SENSOR = 0x14;
    public static final byte PROPERTY_HUMIDITY = 0x18;
    public static final byte PROPERTY_TOTAL_CLEAN = 0x19;
    public static final byte PROPERTY_WIFI = 0x1a;

    public static final byte PROPERTY_FILTER = 0x15;
    public static final byte PROPERTY_TIME = 0x16;
    public static final byte PROPERTY_PERIOD = 0x17;
    public static final byte PROPERTY_MODEL = 0x21;

    public static final byte PROPERTY_DEVICE_TYPE = 0x22;
    public static final byte PROPERTY_MAIN_BOARD = 0x23;
    public static final byte PROPERTY_CONTROL_BOARD = 0x24;
    public static final byte PROPERTY_MESSAGES = 0x25;
    public static final byte PROPERTY_VERSION = 0x26;

    public static final int ErrorValue = 0xffff;
    protected static final int Timeout = 2000;


    public final static int FAN_SPEED_AUTO = 0;
    //    public final static int FAN_SPEED_HIGH = 1;
//    public final static int FAN_SPEED_MID = 2;
//    public final static int FAN_SPEED_LOW = 3;
    public final static int FAN_SPEED_SILENT = 4;
    public final static int FAN_SPEED_POWER = 5;

    protected final PowerTimer powerTimer = new PowerTimer();
    protected final FilterStatus filterStatus = new FilterStatus();
    protected boolean mIsOffline = true;
    protected int reqeustCount = 0;
    final AirPurifierImp airPurifierImp = new AirPurifierImp();

    Sensor sensor = new Sensor();
    AirStatus airStatus = new AirStatus();
    final HashMap<Byte, byte[]> property = new HashMap<>();

    private String getValue(int value) {
        if (value == 0xFFFF) {
            return "-";
        } else
            return String.valueOf(value);
    }

    public AirPurifier_Mx(Context context, String Address, String Type, String Setting) {
        super(context, Address, Type, Setting);
        String json = Setting().get("powerTimer", "").toString();
        powerTimer.fromJSON(json);
    }


    @Override
    protected String getDefaultName() {
        return context().getString(R.string.air_purifier_name);
    }

    @Override
    public int getTimerDelay() {
        return defaultAutoUpdatePeriod;
    }

    /**
     * 设备型号
     *
     * @return 型号
     */
    @Override
    public String Model() {
        return Setting().get("Model", "").toString();
    }

    /**
     * 设备类型
     *
     * @return
     */
    public String DeviceType() {
        return Setting().get("DeviceType", "").toString();
    }

    /**
     * 主板编号
     *
     * @return 编号
     */
    public String MainBoardNo() {
        return Setting().get("MainBoard", "").toString();
    }

    /**
     * 控制板编号
     *
     * @return 编号
     */
    public String ControlBoardNo() {
        return Setting().get("ControlBoard", "").toString();
    }


    public Sensor sensor() {
        return sensor;
    }

    public AirStatus airStatus() {
        return airStatus;
    }

    /**
     * 定时开关机设置
     *
     * @return 定时开关机
     */
    public PowerTimer PowerTimer() {
        return powerTimer;
    }

    @Override
    public void saveSettings() {
        Setting().put("powerTimer", powerTimer.ToJSON());
        super.saveSettings();
    }

    public boolean isOffline() {
        return mIsOffline;
    }

    protected void setOffline(boolean isOffline) {
        if (isOffline != mIsOffline) {
            mIsOffline = isOffline;
            doUpdate();
        }
    }

    protected void dbgBytes(String text, byte[] bytes) {
        String hex = text;
        for (byte b : bytes) {
            hex += String.format("%02x ", b);
        }
        Log.i(TAG, hex);
    }

    public void ResetFilter(OperateCallback<Void> cb) {
        FilterStatus filterStatus = new FilterStatus();
        filterStatus.lastTime = new Date();
        filterStatus.workTime = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        filterStatus.stopTime = calendar.getTime();
        filterStatus.maxWorkTime = 129600; //60 * 1000;
        setProperty(PROPERTY_FILTER, filterStatus.toBytes(), cb);
    }

    @Override
    public String toString() {
        if (mIsOffline) {
            return "Offline";
        } else {
            return sensor().toString() + "\n" + airStatus().toString();
        }
    }

    @Override
    public void updateSettings() {
        setProperty(PROPERTY_POWER_TIMER, powerTimer.ToBytes(), null);
        super.updateSettings();
    }

    protected int getIntValueByShort(byte property) {
        synchronized (this.property) {
            if (this.property.containsKey(property)) {
                byte[] data = this.property.get(property);
                if (data != null)
                    return ByteUtil.getShort(data, 0);
                else
                    return ErrorValue;
            } else
                return ErrorValue;
        }
    }

    protected boolean getBoolValue(byte property) {
        synchronized (this.property) {
            if (this.property.containsKey(property)) {
                byte[] data = this.property.get(property);
                if (data != null)
                    return data[0] != 0;
                else
                    return false;
            } else
                return false;
        }
    }

    protected int getIntValueByByte(byte property) {
        synchronized (this.property) {
            if (this.property.containsKey(property)) {
                byte[] data = this.property.get(property);
                if (data != null)
                    return data[0];
                else
                    return ErrorValue;
            } else
                return ErrorValue;
        }
    }

    protected int getIntValueByInt(byte property) {
        synchronized (this.property) {
            if (this.property.containsKey(property)) {
                byte[] data = this.property.get(property);
                if (data != null)
                    return ByteUtil.getInt(data, 0);
                else
                    return ErrorValue;
            } else
                return ErrorValue;
        }
    }

    /**
     * 设置属性
     *
     * @param propertyId
     * @param value
     * @param cb
     */
    protected void setProperty(byte propertyId, byte[] value, OperateCallback<Void> cb) {
        if (super.connectStatus() == BaseDeviceIO.ConnectStatus.Disconnect) {
            if (cb != null)
                cb.onFailure(null);
            return;
        }

        byte[] bytes = new byte[13 + value.length];

        bytes[0] = (byte) 0xfb;
        ByteUtil.putShort(bytes, (short) bytes.length, 1);
        bytes[3] = CMD_SET_PROPERTY;
        byte[] macs = Helper.HexString2Bytes(this.Address().replace(":", ""));
        System.arraycopy(macs, 0, bytes, 4, 6);
        bytes[12] = propertyId;
        System.arraycopy(value, 0, bytes, 13, value.length);

        sendData(bytes, cb);
    }


    protected void requestProperty(HashSet<Byte> propertys, OperateCallback<Void> cb) {
        if (super.connectStatus() == BaseDeviceIO.ConnectStatus.Disconnect) {
            if (cb != null)
                cb.onFailure(null);
        }

        byte[] bytes = new byte[14 + propertys.size()];
        bytes[0] = (byte) 0xfb;
        ByteUtil.putShort(bytes, (short) bytes.length, 1);
        bytes[3] = CMD_REQUEST_PROPERTY;
        byte[] macs = Helper.HexString2Bytes(this.Address().replace(":", ""));
        System.arraycopy(macs, 0, bytes, 4, 6);

        bytes[12] = (byte) propertys.size();
        int p = 13;
        for (Byte id : propertys) {
            bytes[p] = id;
            p++;
            Log.i(TAG, String.format("request property:%02x", id));
        }


        sendData(bytes, cb);
    }

    /**
     * 发送命令,根据IO类型不同用不同的发送方式
     *
     * @param data
     * @param cb
     */
    protected void sendData(byte[] data, OperateCallback<Void> cb) {
        airPurifierImp.send(data, new OperateCallbackProxy<Void>(cb) {
            @Override
            public void onSuccess(Void var1) {
//                try {
//                    Thread.sleep(200);
//
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                super.onSuccess(var1);
            }
        });
    }

    /**
     * 1.0设备和2.0设备数据预处理
     *
     * @param data
     *
     * @return
     */
    protected abstract byte[] handlerOrgData(byte[] data);


    protected void setNowTime() {
        byte[] time = new byte[4];
        ByteUtil.putInt(time, (int) (System.currentTimeMillis() / 1000), 0);
        setProperty(PROPERTY_TIME, time, null);
    }

    private void setAutoReflash(short period, HashSet<Byte> propertys, OperateCallback<Void> cb) {
        byte[] bytes = new byte[3 + propertys.size()];
        ByteUtil.putShort(bytes, period, 0);
        bytes[2] = (byte) propertys.size();
        int i = 3;
        for (Byte p : propertys) {
            bytes[i] = p;
            i++;
        }
        setProperty(PROPERTY_PERIOD, bytes, cb);
    }

    protected void doTime() {
        Log.i(TAG, "doTime Begin");
        HashSet<Byte> list = new HashSet<>();
        list.add(PROPERTY_FILTER);
        list.add(PROPERTY_PM25);
        list.add(PROPERTY_LIGHT_SENSOR);
        list.add(PROPERTY_TEMPERATURE);
        list.add(PROPERTY_VOC);
        list.add(PROPERTY_HUMIDITY);
        list.add(PROPERTY_POWER);
        list.add(PROPERTY_SPEED);
        list.add(PROPERTY_LIGHT);
        list.add(PROPERTY_LOCK);
        list.add(PROPERTY_WIFI);
        list.add(PROPERTY_TOTAL_CLEAN);
        requestProperty(list, null);
        Log.i(TAG, "doTime End");
    }

    @Override
    protected void doTimer() {
        doTime();
    }

    protected boolean doInit() {
        try {
            mIsOffline = true;
            try {
                setNowTime();
                waitObject(Timeout);
                HashSet<Byte> list = new HashSet<>();
                list.add(PROPERTY_FILTER);
                list.add(PROPERTY_MODEL);
                list.add(PROPERTY_DEVICE_TYPE);
                list.add(PROPERTY_CONTROL_BOARD);
                list.add(PROPERTY_MAIN_BOARD);
                //list.add(PROPERTY_POWER_TIMER);
                list.add(PROPERTY_VERSION);
                requestProperty(list, null);
                waitObject(Timeout);
                Log.i(TAG, "OnInit Complete");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public class AirStatus {

        public int Version() {
            return getIntValueByShort(PROPERTY_VERSION);
        }

        public boolean Power() {
            return getBoolValue(PROPERTY_POWER);
        }

        public void setPower(boolean power, OperateCallback<Void> cb) {
            setProperty(PROPERTY_POWER, new byte[]{power ? (byte) 1 : (byte) 0}, cb);
        }

        /**
         * 获取风扇速度
         *
         * @return FAN_SPEED_AUTO..FAN_SPEED_POWER
         */
        public int speed() {
            return getIntValueByByte(PROPERTY_SPEED);
        }

        /**
         * 设置风扇速度
         *
         * @param value FAN_SPEED_AUTO..FAN_SPEED_POWER
         * @param cb    设置回调
         */
        public void setSpeed(int value, OperateCallback<Void> cb) {

            setProperty(PROPERTY_SPEED, new byte[]{(byte) value}, cb);
        }

        public int Light() {
            return getIntValueByByte(PROPERTY_LIGHT);
        }

        public boolean Lock() {
            return getBoolValue(PROPERTY_LOCK);
        }

        public void setLock(boolean lock, OperateCallback<Void> cb) {
            setProperty(PROPERTY_LOCK, new byte[]{lock ? (byte) 1 : (byte) 0}, cb);
        }

        /**
         * WIFI信号强度
         *
         * @return 0-100%
         */
        public int Wifi() {
            return getIntValueByByte(PROPERTY_WIFI);
        }


        @Override
        public String toString() {
            return String.format("Power:%s Speed:%s Light:%s Lock:%s Wifi:%s%%",
                    String.valueOf(Power()), getValue(speed()), getValue(Light()), String.valueOf(Lock()), getValue(Wifi()));
        }
    }

    public class Sensor {
        /**
         * 湿度
         *
         * @return 湿度%
         */
        public int Humidity() {
            return getIntValueByShort(PROPERTY_HUMIDITY);
        }

        /**
         * PM2.5
         *
         * @return pm25
         */
        public int PM25() {
            return getIntValueByShort(PROPERTY_PM25);
        }

        /**
         * 环境温度
         *
         * @return 温度
         */
        public int Temperature() {
            return getIntValueByShort(PROPERTY_TEMPERATURE);
        }

        /**
         * VOC
         *
         * @return voc
         */
        public int VOC() {
            return getIntValueByShort(PROPERTY_VOC);
        }

        /**
         * 环境光亮度
         *
         * @return 亮度
         */
        public int Light() {
            return getIntValueByShort(PROPERTY_LIGHT_SENSOR);
        }


        /**
         * 总净化量
         *
         * @return
         */
        public int TotalClean() {
            return getIntValueByInt(PROPERTY_TOTAL_CLEAN);
        }

        /**
         * 滤芯状态,如果没收到返回NULL
         *
         * @return 没收到状态时返回NULL
         */
        public FilterStatus FilterStatus() {
            return filterStatus;
        }

        @Override
        public String toString() {
            return String.format("PM2.5:%s VOC:%s Light:%s\nTemperature:%s Humidity:%s%% TotalClean:%s",
                    getValue(PM25()), getValue(VOC()), getValue(Light()),
                    getValue(Temperature()), getValue(Humidity()), getValue(TotalClean()));
        }
    }


    class AirPurifierImp implements
            BaseDeviceIO.OnTransmissionsCallback,
            BaseDeviceIO.StatusCallback,
            BaseDeviceIO.OnInitCallback {

        @Override
        public void onConnected(BaseDeviceIO io) {

        }

        @Override
        public void onDisconnected(BaseDeviceIO io) {

        }

        @Override
        public void onReady(BaseDeviceIO io) {

        }

        @Override
        public void onIOSend(byte[] bytes) {

        }

        private boolean send(byte[] data, OperateCallback<Void> cb) {
            if (IO() != null) {
                reqeustCount++;
                if (reqeustCount >= 3) {
                    setOffline(true);
                }


                //Respone=false;
                return IO().send(data, cb);
            } else
                return false;
        }

        @Override
        public void onIORecv(byte[] orgData) {
            byte[] bytes = handlerOrgData(orgData);
            if ((bytes == null) || (bytes.length <= 0)) {
                return;
            }
            dbgBytes("recv:", bytes);

            reqeustCount = 0;
            setOffline(false);
            try {
                if (bytes[0] != (byte) 0xFA) return;
                int len = ByteUtil.getShort(bytes, 1);
                if (len <= 0) return;
                byte cmd = bytes[3];
                switch (cmd) {
                    case CMD_RECV_PROPERTY:
                        int count = bytes[12];
                        Log.i(TAG, String.format("recv property count:%d", count));

                        int p = 13;
                        HashMap<Byte, byte[]> set = new HashMap<>();
                        for (int i = 0; i < count; i++) {
                            if (p >= bytes.length) break;
                            byte id = bytes[p];
                            p++;

                            byte size = bytes[p];
                            p++;

                            byte[] data = new byte[size];

                            if (p >= bytes.length) return;
                            if (p + size > bytes.length) return;

                            System.arraycopy(bytes, p, data, 0, size);

                            dbgBytes(String.format("property id:%02x size:%d ", id, (byte) size), data);
                            p += size;

                            set.put(id, data);
                        }
                        synchronized (property) {
                            for (Byte id : set.keySet()) {
                                property.put(id, set.get(id));
                                if (id == PROPERTY_POWER) {
                                    Log.i("power", "1");
                                }
                            }
                        }
                        for (Byte id : set.keySet()) {
                            switch (id) {
                                case PROPERTY_POWER_TIMER:
                                    powerTimer.fromByBytes(set.get(id));
                                    Setting().put("powerTimer", powerTimer.ToJSON());
                                case PROPERTY_POWER:
                                case PROPERTY_LIGHT:
                                case PROPERTY_LOCK:
                                case PROPERTY_SPEED: {
                                    Intent intent = new Intent(ACTION_AIR_PURIFIER_STATUS_CHANGED);
                                    intent.putExtra(Extra_Address, Address());
                                    context().sendBroadcast(intent);
                                    doUpdate();
                                    break;
                                }

                                case PROPERTY_FILTER: {
                                    filterStatus.fromBytes(set.get(id));
                                    //两个上次更换时间在2000年以前,直接重置
                                    if (filterStatus.lastTime.getTime() <= 946684800) {
                                        ResetFilter(null);
                                    }
                                }
                                case PROPERTY_PM25:
                                case PROPERTY_TEMPERATURE:
                                case PROPERTY_VOC:
                                case PROPERTY_HUMIDITY:
                                case PROPERTY_TOTAL_CLEAN:
                                case PROPERTY_LIGHT_SENSOR: {
                                    Intent intent = new Intent(ACTION_AIR_PURIFIER_SENSOR_CHANGED);
                                    intent.putExtra(Extra_Address, Address());
                                    context().sendBroadcast(intent);
                                    doUpdate();
                                    break;
                                }
                                case PROPERTY_MODEL:
                                    Setting().put("Model", new String(set.get(id), "US-ASCII"));
                                    break;

                                case PROPERTY_DEVICE_TYPE:
                                    Setting().put("DeviceType", new String(set.get(id), "US-ASCII"));
                                    break;

                                case PROPERTY_MAIN_BOARD:
                                    Setting().put("MainBoard", new String(set.get(id), "US-ASCII"));
                                    break;
                                case PROPERTY_CONTROL_BOARD:
                                    Setting().put("ControlBoard", new String(set.get(id), "US-ASCII"));
                                    break;


                            }
                        }

                        setObject();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean onIOInit() {
            return doInit();
        }
    }
}
