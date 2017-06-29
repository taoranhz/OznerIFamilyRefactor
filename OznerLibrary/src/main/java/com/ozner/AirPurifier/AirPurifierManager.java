package com.ozner.AirPurifier;

import android.content.Context;

import com.ozner.bluetooth.BluetoothIO;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.BaseDeviceManager;
import com.ozner.device.DeviceSetting;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by xzyxd on 2015/11/7.
 */
public class AirPurifierManager extends BaseDeviceManager {

    private static Set<String> airFogSet = new HashSet<>();
    private static Set<String> airMxchipSet = new HashSet<>();
    private static Set<String> airBluetoothSet = new HashSet<>();
    static  {
        //wifi 2.0 空净
        airFogSet.add("e137b6e0-2668-11e7-9d95-00163e103941");
        airFogSet.add("10c347a8-562f-11e7-9baf-00163e120d98");
        //wifi 1.0 空净
        airMxchipSet.add("FOG_HAOZE_AIR");
        airMxchipSet.add("580c2783");
        //蓝牙空净
        airBluetoothSet.add("FLT001");
    }

    public AirPurifierManager(Context context) {
        super(context);
    }

    @Override
    public boolean checkIsBindMode(BaseDeviceIO io) {
        if (isBluetoothAirPurifier(io.getType()))
        {
            BluetoothIO bluetoothIO=(BluetoothIO)io;
            if (bluetoothIO.getScanResponseType()==0x20)
            {
                if ((bluetoothIO.getScanResponseData()!=null) && (bluetoothIO.getScanResponseData().length>1))
                {
                    return bluetoothIO.getScanResponseData()[0]!=0;
                }

            }
        }
        return false;
    }

    @Override
    public boolean isMyDevice(String type) {
        return isBluetoothAirPurifier(type) || isMxchipDeivce(type) || isFogDevice(type);
    }
    @Override
    protected OznerDevice createDevice(String address, String type, String settings) {
        if (isMyDevice(type)) {
            if (isBluetoothAirPurifier(type)) {
                return new AirPurifier_Bluetooth(context(), address, type, settings);
            } else if (isMxchipDeivce(type)) {
                OznerDevice airPurifier = new AirPurifier_MXChip(context(), address, type, settings);
                OznerDeviceManager.Instance().ioManagerList().mxChipIOManager()
                        .createMXChipDevice(airPurifier.Address(), airPurifier.Type());
                return airPurifier;
            }else if (isFogDevice(type)){
                OznerDevice fogAirPurifier = new AirPurifier_Fog(context(), address, type, settings);
                DeviceSetting setting = new DeviceSetting();
                setting.load(settings);
                OznerDeviceManager.Instance().ioManagerList().fogIOManager().createFogDevice(address, type, setting.toString());
                return fogAirPurifier;
            }
            return null;
        }
        else
            return null;
    }
    /**
     * 2.0wifi设备
     *
     * @param productID
     *
     * @return
     */
    public static boolean isFogDevice(String productID) {
        return airFogSet.contains(productID);
    }


    /**
     * 是否是空气净化器
     *
     * @param productID
     *
     * @return
     */
    public static boolean isAirPurifer(String productID) {
        return isBluetoothAirPurifier(productID) || isFogDevice(productID) || isMxchipDeivce(productID);
    }

    public static boolean isBluetoothAirPurifier(String productID) {
        return airBluetoothSet.contains(productID);
    }


    public static boolean isWifiAirPurifier(String Type) {
        if (Type == null) return false;

        return isFogDevice(Type)||isMxchipDeivce(Type);
    }

    /**
     * 1.0wifi设备
     *
     * @param productID
     *
     * @return
     */
    public static boolean isMxchipDeivce(String productID) {
        return airMxchipSet.contains(productID);
    }

}
