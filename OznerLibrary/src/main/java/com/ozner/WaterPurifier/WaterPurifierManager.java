package com.ozner.WaterPurifier;

import android.content.Context;

import com.ozner.bluetooth.BluetoothIO;
import com.ozner.bluetooth.BluetoothScanResponse;
import com.ozner.bluetooth.IBluetoothScanResponseParser;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.BaseDeviceManager;
import com.ozner.device.DeviceSetting;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by xzyxd on 2015/11/2.
 */
public class WaterPurifierManager extends BaseDeviceManager {
    private static Set<String> waterFogSet = new HashSet<>();
    private static Set<String> waterMxchipSet = new HashSet<>();
    private static Set<String> waterBluetoothSet = new HashSet<>();

    IBluetoothScanResponseParser bluetoothScanResponseParser = new IBluetoothScanResponseParser() {
        @Override
        public BluetoothScanResponse parseScanResponse(String name, byte[] Manufacturer_Specific, byte[] Service_Data) {
            ///转到RO设备里面去处理BLE广播数据，返回NULL说明不是RO设备
            return WaterPurifier_RO_BLE.parseScanResp(name,Service_Data);

        }
    };

    public WaterPurifierManager(Context context) {
        super(context);
        OznerDeviceManager.Instance().ioManagerList().bluetoothIOMgr().registerScanResponseParser(bluetoothScanResponseParser);
    }

    static {
        //wifi 1.0 净水器
        waterMxchipSet.add("MXCHIP_HAOZE_Water");
//        waterMxchipSet.add("AY001MAB1");
        waterMxchipSet.add("16a21bd6");


        //wifi 2.0 净水器
        waterFogSet.add("2821b472-5263-11e7-9baf-00163e120d98");
        waterFogSet.add("f4edba26-549a-11e7-9baf-00163e120d98");
        waterFogSet.add("67ea604c-549b-11e7-9baf-00163e120d98");
        waterFogSet.add("b5d03ee4-549b-11e7-9baf-00163e120d98");


        //蓝牙净水器
        waterBluetoothSet.add("d50cd29a-549b-11e7-9baf-00163e120d98");
        waterBluetoothSet.add("b78e2292-549a-11e7-9baf-00163e120d98");
        waterBluetoothSet.add("4295741c-549b-11e7-9baf-00163e120d98");
        waterBluetoothSet.add("934ed042-549b-11e7-9baf-00163e120d98");
        waterBluetoothSet.add("RO Comml");
        waterBluetoothSet.add("Ozner RO");

    }

//    public static boolean IsWaterPurifier(String Model) {
//        if (Model == null) return false;
//        if (Model.trim().equals("MXCHIP_HAOZE_Water"))
//        {
//            return true;
//        }
//        if (Model.trim().equals("AY001MAB1"))
//        {
//            return true;
//        }
//        if (Model.trim().equals("Ozner RO"))
//        {
//            return true;
//        }
//        if (Model.trim().equals("16a21bd6"))
//        {
//            return true;
//        }
//        if (Model.trim().equals("RO Comml")) {
//            return true;
//        }
//
//        return false;
//
//    }

    @Override
    public boolean isMyDevice(String type) {
        return isWaterPurifier(type);
    }

    @Override
    protected OznerDevice createDevice(String address, String type, String settings) {
        if (isMxchipDeivce(type)) {
            WaterPurifier waterPurifier = new WaterPurifier_MXChip(context(), address, type, settings);
            OznerDeviceManager.Instance().ioManagerList().mxChipIOManager()
                    .createMXChipDevice(waterPurifier.Address(), waterPurifier.Type());
            return waterPurifier;
        }
//        if (type.trim().equals("FOG_V2_EMW3162")) {
        //庆科2.0设备，type为2.0中的productId
        if (isFogDevice(type)) {
            try {
                WaterPurifier waterPurifier = new WaterPurifier_Fog(context(), address, type, settings);
                DeviceSetting devSetting = new DeviceSetting();
                devSetting.load(settings);
                OznerDeviceManager.Instance().ioManagerList().fogIOManager().createFogDevice(address, type, devSetting.toString());
                return waterPurifier;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }

        if (isBluetoothDevice(type)) {
            WaterPurifier waterPurifier = new WaterPurifier_RO_BLE(context(), address, type, settings);
            return waterPurifier;
        } else {
            return null;
        }
    }

    @Override
    public boolean checkIsBindMode(BaseDeviceIO io) {

        if (io instanceof BluetoothIO)
        {
            if (isBluetoothDevice(io.getType())) {
                //检查是否在配对模式
                return WaterPurifier_RO_BLE.isBindMode((BluetoothIO) io);
            }
        }
        return super.checkIsBindMode(io);
    }

    /**
     * 是否是净水器
     *
     * @param productID
     *
     * @return
     */
    public static boolean isWaterPurifier(String productID) {
        return isBluetoothDevice(productID) || isFogDevice(productID) || isMxchipDeivce(productID);
    }

    /**
     * 是否是wifi净水器
     *
     * @param productID
     *
     * @return
     */
    public static boolean isWifiWaterPurifier(String productID) {
        return isMxchipDeivce(productID) || isFogDevice(productID);
    }

    /**
     * 是否是蓝牙设备
     *
     * @param productID
     *
     * @return
     */
    public static boolean isBluetoothDevice(String productID) {
        return waterBluetoothSet.contains(productID);
    }

    /**
     * 2.0wifi设备
     *
     * @param productID
     *
     * @return
     */
    public static boolean isFogDevice(String productID) {
        return waterFogSet.contains(productID);
    }

    /**
     * 1.0wifi设备
     *
     * @param productID
     *
     * @return
     */
    public static boolean isMxchipDeivce(String productID) {
        return waterMxchipSet.contains(productID);
    }

}
