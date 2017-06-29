package com.ozner.WaterPurifier;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OperateCallback;
import com.ozner.device.OperateCallbackProxy;
import com.ozner.util.ByteUtil;
import com.ozner.util.Convert;
import com.ozner.util.Helper;
import com.ozner.wifi.mxchip.Pair.CRC8;

/**
 * Created by ozner_67 on 2017/5/23.
 * 邮箱：xinde.zhang@cftcn.com
 */

public abstract class WaterPurifier_Mx extends WaterPurifier {

    protected static final byte GroupCode_DeviceToApp = (byte) 0xFB;
    protected static final byte GroupCode_AppToDevice = (byte) 0xFA;
    protected static final byte GroupCode_DevceToServer = (byte) 0xFC;

    protected static final byte Opcode_RequestStatus = (byte) 0x01;
    protected static final byte Opcode_RespondStatus = (byte) 0x01;
    protected static final byte Opcode_ChangeStatus = (byte) 0x02;
    protected static final byte Opcode_DeviceInfo = (byte) 0x03;
    final WaterPurifierStatusPacket statusPacket = new WaterPurifierStatusPacket();

//    private static String SecureCode = "16a21bd6";

    final WaterPurifierImp waterPurifierImp = new WaterPurifierImp();


    public WaterPurifier_Mx(Context context, String Address, String Model, String Setting) {
        super(context, Address, Model, Setting);
    }

    public static byte[] MakeWoodyBytes(byte Group, byte OpCode, String Address, byte[] payload) {
        int len = 10 + (payload == null ? 3 : payload.length + 3);
        byte[] bytes = new byte[len];
        bytes[0] = Group;
        ByteUtil.putShort(bytes, (short) len, 1);
        bytes[3] = OpCode;

        byte[] macs = Helper.HexString2Bytes(Address.replace(":", ""));
        System.arraycopy(macs, 0, bytes, 4, 6);

        bytes[10] = 0;//保留数据
        bytes[11] = 0;//保留数据
        if (payload != null)
            System.arraycopy(payload, 0, bytes, 12, payload.length);

        bytes[len - 1] = CRC8.calcCrc8(bytes, 0, bytes.length - 1);
        return bytes;
    }


    private void setStatusPacket(OperateCallback<Void> cb) {
        if (super.connectStatus() != BaseDeviceIO.ConnectStatus.Connected) {
            if (cb != null)
                cb.onFailure(null);
            return;
        }
        IO().send(MakeWoodyBytes(GroupCode_AppToDevice, Opcode_ChangeStatus, Address(),
                statusPacket.toBytes()),
                new OperateCallbackProxy<Void>(cb) {
                    @Override
                    public void onFailure(Throwable var1) { //失败时重新更新状态
                        //updateStatus(null, null);
                        super.onFailure(var1);
                    }

                    @Override
                    public void onSuccess(Void var1) {
                        updateStatus(null);
                        super.onSuccess(var1);
                    }
                });

    }

    int requestCount = 0;

    @Override
    protected void updateStatus(OperateCallback<Void> cb) {
        if (IO() == null) {
            if (cb != null)
                cb.onFailure(null);
        } else {
            requestCount++;
            if (requestCount >= 3) {
                setOffline(true);
            }
            IO().send(MakeWoodyBytes(GroupCode_AppToDevice, Opcode_RequestStatus, Address(), null), cb);
        }
    }


    @Override
    protected int getTDS1() {
        return statusPacket.TDS1;
    }

    @Override
    protected int getTDS2() {
        return statusPacket.TDS2;
    }

    @Override
    protected int getTemperature() {
        return statusPacket.Temperature;
    }

    @Override
    protected boolean getPower() {
        return statusPacket.Power;
    }

    @Override
    protected void setPower(boolean Power, OperateCallback<Void> cb) {
        if (IO() == null) {
            cb.onFailure(null);
        }
        statusPacket.Power = Power;
        setStatusPacket(cb);
    }

    @Override
    protected boolean getHot() {
        return statusPacket.Hot;
    }

    @Override
    protected void setHot(boolean Hot, OperateCallback<Void> cb) {
        if (IO() == null) {
            cb.onFailure(null);
        }
        statusPacket.Hot = Hot;
        setStatusPacket(cb);
    }

    @Override
    protected boolean getCool() {
        return statusPacket.Cool;
    }

    @Override
    protected void setCool(boolean Cool, OperateCallback<Void> cb) {
        if (IO() == null) {
            cb.onFailure(null);
        }
        statusPacket.Cool = Cool;
        setStatusPacket(cb);
    }

    @Override
    protected boolean getSterilization() {
        return statusPacket.Sterilization;
    }

    @Override
    protected void setSterilization(boolean Sterilization, OperateCallback<Void> cb) {
        if (IO() == null) {
            cb.onFailure(null);
        }
        statusPacket.Sterilization = Sterilization;
        setStatusPacket(cb);
    }

    /**
     * 1.0设备和2.0设备数据预处理
     *
     * @param data
     *
     * @return
     */
    protected abstract byte[] handlerOrgData(byte[] data);


    class WaterPurifierImp implements
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
            updateStatus(null);
        }


        @Override
        public void onIOSend(byte[] bytes) {

        }


        @Override
        public void onIORecv(byte[] orgData) {
            Log.e("lingchen", "onIORecv: " + Convert.ByteArrayToHexString(orgData));

            byte[] bytes = handlerOrgData(orgData);
            if ((bytes == null) || (bytes.length <= 0)) {
                return;
            }

            if (isOffline) {
                setOffline(false);
            }
            requestCount = 0;

            if ((bytes != null) && (bytes.length > 10)) {
                byte group = bytes[0];
                byte opCode = bytes[3];
                switch (group) {
                    case GroupCode_DeviceToApp:
                        switch (opCode) {
                            case Opcode_RespondStatus:
                                requestCount = 0;

                                statusPacket.fromBytes(bytes);
                                Intent intent = new Intent(ACTION_WATER_PURIFIER_STATUS_CHANGE);
                                intent.putExtra(Extra_Address, Address());
                                context().sendBroadcast(intent);
                                isOffline = false;
                                break;
                            case Opcode_DeviceInfo:
                                info.fromBytes(bytes);
                                setObject();
                                isOffline = false;
                                break;
                        }
                        break;

                }

            }
        }

        @Override
        public boolean onIOInit() {
            try {
                isOffline = true;
                IO().send(MakeWoodyBytes(GroupCode_AppToDevice, Opcode_RequestStatus, Address(), null), null);
                waitObject(5000);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }


    }

}
