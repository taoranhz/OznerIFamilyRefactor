package com.ozner.WaterPurifier;

import android.content.Context;
import android.text.format.Time;
import android.util.Log;

import com.ozner.bluetooth.BluetoothIO;
import com.ozner.bluetooth.BluetoothScanResponse;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OperateCallback;
import com.ozner.util.ByteUtil;
import com.ozner.util.dbg;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

/**
 * Created by zhiyongxu on 16/9/12.
 */
public class WaterPurifier_RO_BLE extends WaterPurifier {
    private static final int defaultAutoUpdatePeriod = 1000;
    private static final byte opCode_request_info = (byte) 0x20;
    private static final byte opCode_reset = (byte) 0xa0;
    private static final byte opCode_set_setting = (byte) 0x40;

    private static final byte opCode_respone_setting = (byte) 0x21;
    private static final byte opCode_respone_water = (byte) 0x22;
    private static final byte opCode_respone_filter = (byte) 0x23;
    private static final byte opCode_respone_filterHis1 = (byte) 0x11;
    private static final byte opCode_respone_filterHis2 = (byte) 0x12;


    private static final byte param_request_settinginfo = 1;
    private static final byte param_request_water_info = 2;
    private static final byte param_request_filter_info = 3;
    private static final byte param_request_filter_history_info = 4;
    public SettingInfo settingInfo = new SettingInfo();
    public WaterInfo waterInfo = new WaterInfo();
    public FilterInfo filterInfo = new FilterInfo();

    public class SettingInfo {
        /**
         * RTC时间
         */
        public Date rtc;
        /**
         * 臭氧工作时间
         */
        public int Ozone_WorkTime;
        /**
         * 臭氧工作间隔
         */
        public int Ozone_Interval;

        //到期日
        public Date ExpireTime = new Date(70, 1, 1, 0, 0, 0);
        //滤芯激活状态
        public boolean isFilterActivate;
        //计时激活状态
        public boolean isTimerActivate;

        public void fromBytes(byte[] bytes) {
            this.rtc = new Date(bytes[0] + 2000 - 1900, bytes[1] - 1, bytes[2], bytes[3],
                    bytes[4], bytes[5]);
            this.Ozone_Interval = bytes[6];
            this.Ozone_WorkTime = bytes[7];
            try {
                this.ExpireTime = new Date(bytes[8] + 2000 - 1900, bytes[9] - 1, bytes[10], bytes[11], bytes[12], bytes[13]);
                Log.e("trtime",this.ExpireTime+"");
                this.isFilterActivate = bytes[14] == (byte) 0x16 || bytes[14] == (byte) 0x88 ? true : false;
                this.isTimerActivate = bytes[15] == (byte) 0x88 ? true : false;
            } catch (Exception ex) {
//                Log.e(TAG, "fromBytes_ex: " + ex.getMessage());
                //设置1970
                this.ExpireTime = new Date(70, 0, 1, 0, 0, 0);
            }
        }

        @Override
        public String toString() {
            if (rtc == null) return "";
            return String.format("设备时间:%s\n臭氧工作时间:%d 间隔:%d\n 到期日:%s \n滤芯激活:%s 计时激活:%s",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(rtc),
                    Ozone_WorkTime, Ozone_Interval, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ExpireTime)
                    , String.valueOf(isFilterActivate), String.valueOf(isTimerActivate));
        }
    }

    public class WaterInfo {
        public int TDS1;
        public int TDS2;
        public int TDS1_RAW;
        public int TDS2_RAW;
        public int TDS_Temperature;
        /**
         * 过滤水量
         */
        public int FilterVolume;

        public void fromBytes(byte[] bytes) {
            this.TDS1 = ByteUtil.getShort(bytes, 0);
            this.TDS2 = ByteUtil.getShort(bytes, 2);
            this.TDS1_RAW = ByteUtil.getShort(bytes, 4);
            this.TDS2_RAW = ByteUtil.getShort(bytes, 6);
            this.TDS_Temperature = ByteUtil.getShort(bytes, 8);
            this.FilterVolume = ByteUtil.getInt(bytes, 10);
        }

        @Override
        public String toString() {
            return String.format("TDS1:%d(%d) TDS2:%d(%d) 温度:%d 过滤水量:%d", TDS1, TDS1_RAW, TDS2, TDS2_RAW, TDS_Temperature, FilterVolume);
        }
    }

    public class FilterInfo {
        public int Filter_A_Time;
        public int Filter_B_Time;
        public int Filter_C_Time;

        public int Filter_A_Percentage;
        public int Filter_B_Percentage;
        public int Filter_C_Percentage;

        public void fromBytes(byte[] bytes) {
            Filter_A_Time = (int) ByteUtil.getUInt(bytes, 0);
            Filter_B_Time = (int) ByteUtil.getUInt(bytes, 4);
            Filter_C_Time = (int) ByteUtil.getUInt(bytes, 8);
            Filter_A_Percentage = bytes[12];
            Filter_B_Percentage = bytes[13];
            Filter_C_Percentage = bytes[14];
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("滤芯 A:%d %d%%\n", Filter_A_Time, Filter_A_Percentage));
            sb.append(String.format("滤芯 B:%d %d%%\n", Filter_B_Time, Filter_B_Percentage));
            sb.append(String.format("滤芯 C:%d %d%%\n", Filter_C_Time, Filter_C_Percentage));
            return sb.toString();
        }
    }

    private WaterPurifierIMP waterPurifierIMP = new WaterPurifierIMP();
    public static final int BLE_RO_ScanResponseType = 0x11;

    public WaterPurifier_RO_BLE(Context context, String Address, String Model, String Setting) {
        super(context, Address, Model, Setting);
    }

    @Override
    public Class<?> getIOType() {
        return BluetoothIO.class;
    }

    @Override
    protected void doSetDeviceIO(BaseDeviceIO oldIO, BaseDeviceIO newIO) {
        if (oldIO != null) {
            oldIO.setOnInitCallback(null);
            oldIO.unRegisterStatusCallback(waterPurifierIMP);
            oldIO.setOnTransmissionsCallback(null);
            oldIO.setCheckTransmissionsCompleteCallback(null);
            //firmwareTools.bind(null);
        }
        if (newIO != null) {
            newIO.setOnTransmissionsCallback(waterPurifierIMP);
            newIO.setOnInitCallback(waterPurifierIMP);
            newIO.registerStatusCallback(waterPurifierIMP);
            newIO.setCheckTransmissionsCompleteCallback(waterPurifierIMP);
            //firmwareTools.bind((BluetoothIO) newIO);
        }
    }

    int requestCount = 0;

    @Override
    protected void updateStatus(OperateCallback<Void> cb) {

        if ((requestCount % 2) == 0) {
            waterPurifierIMP.requestFilterInfo();
        } else
            waterPurifierIMP.requestWaterInfo();
        requestCount++;

    }

    /**
     * 激活设备
     *
     * @param Ozone_Interval 臭氧工作间隔时间，单位：小时
     * @param Ozone_WorkTime 臭氧工作时间,单位：分钟
     * @param resetFilter    滤芯复位，false：无操作，true：复位
     * @param cb             回调
     */
    @Override
    public void setActivate(ImpTime impTime, int Ozone_Interval, int Ozone_WorkTime, boolean resetFilter, OperateCallback<Void> cb) {
        if (waterPurifierIMP != null) {
            waterPurifierIMP.setActivate(impTime, Ozone_Interval, Ozone_WorkTime, resetFilter, cb);
        } else {
            cb.onFailure(new UnsupportedOperationException());
        }
    }

    private ScheduledExecutorService mScheduledPool = Executors.newScheduledThreadPool(2);


    public void addMonth(int month, ISettingCallback cb) {
        if (waterPurifierIMP != null) {
            waterPurifierIMP.addMonth(month, cb);
        } else {
            cb.onResult(false);
        }
    }




    /**
     * 设置设置信息回调
     */
    public interface ISettingCallback {
        void onResult(boolean success);
    }








//    @Override
//    public void addMonth(int month, OperateCallback<Void> cb) {
//        if (waterPurifierIMP != null) {
//            waterPurifierIMP.addMonty(month, cb);
//        } else {
//            cb.onFailure(new UnsupportedOperationException());
//        }
//    }




    @Override
    protected int getTDS1() {
        return waterInfo.TDS1;
    }

    @Override
    protected int getTDS2() {
        return waterInfo.TDS2;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(settingInfo.toString() + "\n");
        sb.append(waterInfo.toString() + "\n");
        sb.append(filterInfo.toString() + "\n");
        return sb.toString();
    }

    public static class ImpTime {
        public int year;
        public int month;
        public int day;
        public int hour;
        public int min;
        public int second;
    }

    /**
     * 请求设置信息
     */
    public boolean requestSettingInfo() {
        if (waterPurifierIMP != null) {
            waterPurifierIMP.requestSettingInfo();
            return true;
        }
        return false;
    }




    class WaterPurifierIMP implements
            BluetoothIO.OnInitCallback,
            BluetoothIO.OnTransmissionsCallback,
            BluetoothIO.StatusCallback,
            BluetoothIO.CheckTransmissionsCompleteCallback {
        private byte calcSum(byte[] data, int size) {
            byte sum = 0;
            for (int i = 0; i < size; i++)
                sum += data[i];
            return sum;
        }

        private void requestSettingInfo() {
            if (IO() != null) {
                byte[] bytes = new byte[3];
                bytes[0] = opCode_request_info;
                bytes[1] = 1;
                bytes[2] = calcSum(bytes, 2);
                IO().send(bytes);
                dbg.i("请求设置信息");
            }
        }

        public void addMonth(int month, ISettingCallback callback) {
            if (IO() != null) {
                byte[] bytes = new byte[19];
                bytes[0] = opCode_set_setting;
                //同步时间
                Time time = new Time();
                time.setToNow();
                bytes[1] = (byte) (time.year - 2000);
                bytes[2] = (byte) (time.month + 1);
                bytes[3] = (byte) time.monthDay;
                bytes[4] = (byte) time.hour;
                bytes[5] = (byte) time.minute;
                bytes[6] = (byte) time.second;

                //臭氧工作间隔时间和工作时间
                bytes[7] = (byte) settingInfo.Ozone_Interval;
                bytes[8] = (byte) settingInfo.Ozone_WorkTime;
                bytes[9] = 0;
                //到期日
                final Calendar cal = Calendar.getInstance();
                Date orgTime = settingInfo.ExpireTime;
                if (settingInfo.ExpireTime.getYear() < 100) {
                    if (callback != null) {
                        callback.onResult(false);
                    }
                }

                //判断充水起始日
                if (orgTime.getTime() > cal.getTimeInMillis()) {
                    cal.setTime(orgTime);
                }
                cal.add(Calendar.DAY_OF_MONTH, month);

                bytes[10] = (byte) (cal.get(Calendar.YEAR) - 2000);
                bytes[11] = (byte) (cal.get(Calendar.MONTH) + 1);
                bytes[12] = (byte) (cal.get(Calendar.DAY_OF_MONTH));
                bytes[13] = (byte) (cal.get(Calendar.HOUR_OF_DAY));
                bytes[14] = (byte) (cal.get(Calendar.MINUTE));
                bytes[15] = (byte) (cal.get(Calendar.SECOND));

                //激活：0x1688,0x8816表示激活，其他未激活；0x1688：滤芯，0x8816：滤芯+计时
                bytes[16] = (byte) 0x16;
                bytes[17] = (byte) 0x88;

                bytes[18] = calcSum(bytes, 18);

                IO().send(bytes, null);

                requestSettingInfo();
                try {
                    int count = 5;//循环5次

                    ScheduledFuture<Boolean> result;
                    do {
                        count--;
                        final Date tempTim = settingInfo.ExpireTime;
                        result = mScheduledPool.schedule(new Callable<Boolean>() {
                            @Override
                            public Boolean call() throws Exception {
                                //检查是否和预期的时间一致，一致证明成功，不一致证明失败
                                if ((cal.get(Calendar.YEAR) - 1900) != tempTim.getYear() ||
                                        cal.get(Calendar.MONTH) + 0 != tempTim.getMonth() ||
                                        cal.get(Calendar.DAY_OF_MONTH) + 0 != tempTim.getDate()) {
                                    return false;
                                } else {
                                    return true;
                                }
                            }
                        }, 2, TimeUnit.SECONDS);
                        if (result != null && !result.get()) {
                            requestSettingInfo();
                        }
                    }
                    while (count > 0 && result != null && !result.get());
                    if (result != null) {
                        if (callback != null) {
                            callback.onResult(result.get());
                        }
                    } else {
                        if (callback != null) {
                            callback.onResult(false);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Log.e(TAG, "addMonth_Ex: " + ex.getMessage());
                    if (callback != null) {
                        callback.onResult(false);
                    }
                }
            } else {
                if (callback != null) {
                    callback.onResult(false);
                }
            }
        }
//        public boolean addMonty(int month, OperateCallback<Void> cb) {
//            if (IO() != null) {
//                Log.e("tr","month:"+month);
//                byte[] bytes = new byte[19];
//                bytes[0] = opCode_set_setting;
//                //同步时间
//                Time time = new Time();
//                time.setToNow();
//                bytes[1] = (byte) (time.year - 2000);
//                bytes[2] = (byte) (time.month + 1);
//                bytes[3] = (byte) time.monthDay;
//                bytes[4] = (byte) time.hour;
//                bytes[5] = (byte) time.minute;
//                bytes[6] = (byte) time.second;
//
//                //臭氧工作间隔时间和工作时间
//                bytes[7] = (byte) settingInfo.Ozone_Interval;
//                bytes[8] = (byte) settingInfo.Ozone_WorkTime;
//
////                //滤芯复位
////                if (settingInfo.isFilterActivate)
////                    bytes[9] = 1;
////                else
//                bytes[9] = 0;
//
//
//                //到期日
//                Calendar cal = Calendar.getInstance();
//
//                Date orgTime = settingInfo.ExpireTime;
//                Log.e("tr","orgTime:"+orgTime.toLocaleString());
//                Log.e("tr","orgYear:"+settingInfo.ExpireTime.getYear());
//                //settingInfo.ExpireTime.getYear方法是获取1900到如今的间隔年数
//                if(settingInfo.ExpireTime.getYear() < 100){
//                    if(cb!=null){
//                        cb.onFailure(null);
//                    }
//                    return false;
//                }
//
//                //判断充水起始日
//                if (orgTime.getTime() > cal.getTimeInMillis()) {
//                    cal.setTime(orgTime);
//                }
//
////                cal.add(Calendar.YEAR, impTime.year);
////                cal.add(Calendar.MONTH, month);
//                  cal.add(Calendar.DAY_OF_MONTH,month);
////                cal.add(Calendar.DAY_OF_MONTH, impTime.day);
////                cal.add(Calendar.HOUR_OF_DAY, impTime.hour);
////                cal.add(Calendar.MINUTE, impTime.min);
////                cal.add(Calendar.SECOND, impTime.second);
//                Log.e("tr","newTime:"+cal.getTime().toLocaleString());
//
//                bytes[10] = (byte) (cal.get(Calendar.YEAR) - 2000);
//                bytes[11] = (byte) (cal.get(Calendar.MONTH) + 1);
//                bytes[12] = (byte) (cal.get(Calendar.DAY_OF_MONTH));
//                bytes[13] = (byte) (cal.get(Calendar.HOUR_OF_DAY));
//                bytes[14] = (byte) (cal.get(Calendar.MINUTE));
//                bytes[15] = (byte) (cal.get(Calendar.SECOND));
//
//                //激活：0x1688,0x8816表示激活，其他未激活；0x1688：滤芯，0x8816：滤芯+计时
//                bytes[16] = (byte) 0x16;
//                bytes[17] = (byte) 0x88;
//
//                bytes[18] = calcSum(bytes, 18);
////                Log.e(TAG, "setActivate: 发送激活信息");
//                boolean success = IO().send(bytes,cb);
//                if(success){
//                    requestSettingInfo();
//                }
//                return success;
//
//            } else{
//                if (cb!=null){
//                    cb.onFailure(null);
//                }
//                return false;
//            }
//        }

        public boolean setActivate(ImpTime impTime, int Ozone_Interval, int Ozone_WorkTime, boolean resetFilter, OperateCallback<Void> cb) {
            if (IO() != null) {
                byte[] bytes = new byte[19];
                bytes[0] = opCode_set_setting;
                //同步时间
                Time time = new Time();
                time.setToNow();
                bytes[1] = (byte) (time.year - 2000);
                bytes[2] = (byte) (time.month + 1);
                bytes[3] = (byte) time.monthDay;
                bytes[4] = (byte) time.hour;
                bytes[5] = (byte) time.minute;
                bytes[6] = (byte) time.second;

                //臭氧工作间隔时间和工作时间
                bytes[7] = (byte) Ozone_Interval;
                bytes[8] = (byte) Ozone_WorkTime;

                //滤芯复位
                if (resetFilter)
                    bytes[9] = 1;
                else
                    bytes[9] = 0;


                //到期日
                Calendar cal = Calendar.getInstance();

                Date orgTime = settingInfo.ExpireTime;

                //判断充水起始日
                if (orgTime.getTime() > cal.getTimeInMillis()) {
                    cal.setTime(orgTime);
                }

                cal.add(Calendar.YEAR, impTime.year);
                cal.add(Calendar.MONTH, impTime.month);
                cal.add(Calendar.DAY_OF_MONTH, impTime.day);
                cal.add(Calendar.HOUR_OF_DAY, impTime.hour);
                cal.add(Calendar.MINUTE, impTime.min);
                cal.add(Calendar.SECOND, impTime.second);

                bytes[10] = (byte) (cal.get(Calendar.YEAR) - 2000);
                bytes[11] = (byte) (cal.get(Calendar.MONTH) + 1);
                bytes[12] = (byte) (cal.get(Calendar.DAY_OF_MONTH));
                bytes[13] = (byte) (cal.get(Calendar.HOUR_OF_DAY));
                bytes[14] = (byte) (cal.get(Calendar.MINUTE));
                bytes[15] = (byte) (cal.get(Calendar.SECOND));

                //激活：0x1688,0x8816表示激活，其他未激活；0x1688：滤芯，0x8816：滤芯+计时
                bytes[16] = (byte) 0x16;
                bytes[17] = (byte) 0x88;

                bytes[18] = calcSum(bytes, 18);
//                Log.e(TAG, "setActivate: 发送激活信息");
                return IO().send(bytes, cb);

            } else
                return false;
        }

        public boolean updateSetting(int Ozone_Interval, int Ozone_WorkTime, boolean resetFilter, OperateCallback<Void> cb) {
            if (IO() != null) {
                byte[] bytes = new byte[11];
                bytes[0] = opCode_set_setting;
                Time time = new Time();
                time.setToNow();

                bytes[1] = (byte) (time.year - 2000);
                bytes[2] = (byte) (time.month + 1);
                bytes[3] = (byte) time.monthDay;
                bytes[4] = (byte) time.hour;
                bytes[5] = (byte) time.minute;
                bytes[6] = (byte) time.second;

                bytes[7] = (byte) Ozone_Interval;
                bytes[8] = (byte) Ozone_WorkTime;
                if (resetFilter)
                    bytes[9] = 1;
                else
                    bytes[9] = 0;

                bytes[10] = calcSum(bytes, 10);
                dbg.i("发送设置信息");
                return IO().send(bytes, cb);
            } else
                return false;

        }

        private void requestWaterInfo() {
            if (IO() != null) {
                byte[] bytes = new byte[3];
                bytes[0] = opCode_request_info;
                bytes[1] = 2;
                bytes[2] = calcSum(bytes, 2);
                IO().send(bytes);
                dbg.i("请求水质信息");
            }
        }

        private void requestFilterInfo() {
            if (IO() != null) {
                byte[] bytes = new byte[3];
                bytes[0] = opCode_request_info;
                bytes[1] = 3;
                bytes[2] = calcSum(bytes, 2);
                IO().send(bytes);
                dbg.i("请求滤芯信息");
            }
        }


        private void requestFilterHisInfo() {
            if (IO() != null) {
                byte[] bytes = new byte[3];
                bytes[0] = opCode_request_info;
                bytes[1] = 4;
                bytes[2] = calcSum(bytes, 2);
                IO().send(bytes);
                dbg.i("请求滤芯历史信息");
            }
        }


        private void reset() {
            if (IO() != null) {
                byte[] bytes = new byte[2];
                bytes[0] = (byte) 0xa0;
                bytes[1] = calcSum(bytes, 2);
                IO().send(bytes);
                dbg.i("重启设备");
            }
        }


        @Override
        public boolean CheckTransmissionsComplete(BaseDeviceIO io) {
            return true;
        }

        @Override
        public boolean onIOInit() {
            return true;
        }

        @Override
        public void onIOSend(byte[] bytes) {

        }

        @Override
        public void onIORecv(byte[] bytes) {
            if (bytes == null) return;
            if (bytes.length <= 0) return;
            byte opCode = bytes[0];
            if (bytes[bytes.length - 1] != calcSum(bytes, bytes.length - 1))
                return;
            byte[] data = null;
            if (bytes.length > 2)
                data = Arrays.copyOfRange(bytes, 1, bytes.length - 1);

            switch (opCode) {
                case opCode_respone_setting:
                    settingInfo.fromBytes(data);
                    break;
                case opCode_respone_water:
                    waterInfo.fromBytes(data);
                    break;
                case opCode_respone_filter:
                    filterInfo.fromBytes(data);
                    break;
                case opCode_respone_filterHis1:
                    break;
                case opCode_respone_filterHis2:
                    break;
            }
            doUpdate();
        }

        @Override
        public void onConnected(BaseDeviceIO io) {
            BluetoothIO bluetoothIO = (BluetoothIO) io;
            if (((BluetoothIO) io).getScanResponse() != null) {
                info.ControlBoard = "";
                info.MainBoard = bluetoothIO.getScanResponse().MainbroadPlatform;
                info.Model = bluetoothIO.getScanResponse().Model;
            }
        }

        @Override
        public void onDisconnected(BaseDeviceIO io) {

        }

        @Override
        public void onReady(BaseDeviceIO io) {
            requestSettingInfo();

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            requestFilterHisInfo();

        }


    }

    /**
     * 返回是否允许滤芯重置
     *
     * @return
     */
    public boolean isEnableFilterReset() {
        return true;
    }

    /**
     * 重置滤芯时间
     *
     * @return
     */
    public boolean resetFilter(OperateCallback<Void> cb) {
        if (settingInfo.Ozone_Interval <= 0) return false;
        if (settingInfo.Ozone_WorkTime <= 0) return false;
        return waterPurifierIMP.updateSetting(settingInfo.Ozone_Interval, settingInfo.Ozone_WorkTime,
                true, cb);
    }

    public static BluetoothScanResponse parseScanResp(String name, byte[] Service_Data) {
        if (name.equals("Ozner RO")) {
            BluetoothScanResponse rep = new BluetoothScanResponse();
            if (Service_Data != null) {
                if (Service_Data.length < 25) return null;
                rep.Platform = new String(Service_Data, 0, 3);
                rep.Firmware = new Date(Service_Data[3] + 2000 - 1900, Service_Data[4] - 1,
                        Service_Data[5], Service_Data[6],
                        Service_Data[7], Service_Data[8]);

                rep.Model = "Ozner RO";
                rep.MainbroadPlatform = new String(Service_Data, 9, 3);
                rep.MainbroadFirmware = new Date(Service_Data[12] + 2000 - 1900, Service_Data[13] - 1,
                        Service_Data[14], Service_Data[15],
                        Service_Data[16], Service_Data[17]);
                rep.Firmware = rep.MainbroadFirmware;
                rep.CustomDataLength = 8;
                rep.ScanResponseType = WaterPurifier_RO_BLE.BLE_RO_ScanResponseType;
                rep.ScanResponseData = Arrays.copyOfRange(Service_Data, 18, 25);
            }
            return rep;
        } else
            return null;
    }


    /**
     * 判断设备是否处于配对状态
     *
     * @param io 设备接口
     *
     * @return true=配对状态
     */
    public static boolean isBindMode(BluetoothIO io) {
        //return true;
        byte[] data = io.getScanResponseData();
        if ((io.getScanResponseType() == BLE_RO_ScanResponseType) && (data != null)) {
            return data[0] != 0;
        }
        return false;
    }



}
