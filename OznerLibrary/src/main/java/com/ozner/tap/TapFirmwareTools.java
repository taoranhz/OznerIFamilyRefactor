package com.ozner.tap;

import com.ozner.device.FirmwareTools;
import com.ozner.util.ByteUtil;
import com.ozner.util.dbg;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TapFirmwareTools extends FirmwareTools {
    static final byte opCode_GetFirmwareSum = (byte) 0xc5;
    static final byte opCode_GetFirmwareSumRet = (byte) 0xc5;

    public TapFirmwareTools() {
        super();
    }

    @Override
    protected void loadFile(String path) throws Exception {
        File file = new File(path);
        byte[] key = {0x23, 0x23, 0x24, 0x24, 0x40, 0x40, 0x2a, 0x2a, 0x54, 0x61, 0x70, 0x00};
        Size = (int) file.length();
        if (Size > 31 * 1024) Size = 31 * 1024;

        if ((Size % 128) != 0) {
            Size = (Size / 128) * 128 + 128;
        }

        FileInputStream fs = new FileInputStream(file);
        try {
            bytes = new byte[Size];
            fs.read(bytes, 0, Size);
            int v_pos = 0;
            int myLoc1 = 0;
            int myLoc2 = 0;

            boolean ver = false;

            for (int i = 0; i < bytes.length - key.length; i++) {
                ver = false;
                for (int x = 0; x < key.length; x++) {
                    if (key[x] == bytes[i + x]) {
                        ver = true;
                    } else {
                        ver = false;
                        break;
                    }
                }
                if (ver) {
                    v_pos = i;
                    break;
                }
            }
            for (int i = 0; i < bytes.length - 6; i++) {
                if ((bytes[i] == 0x12) && (bytes[i + 1] == 0x34) && (bytes[i + 2] == 0x56)
                        && (bytes[i + 3] == 0x65) && (bytes[i + 4] == 0x43) && (bytes[i + 5] == 0x21)) {
                    if (myLoc1 == 0)
                        myLoc1 = i;
                    else
                        myLoc2 = i;
                }
            }

            if (!ver) {
                throw new FirmwareException("错误的文件");
            } else {
                int ver_pos = ByteUtil.getInt(bytes, v_pos + 16);
                if ((ver_pos < 0) || (ver_pos > bytes.length)) throw new FirmwareException("错误的文件");

                int day_pos = ByteUtil.getInt(bytes, v_pos + 20);
                if ((day_pos < 0) || (day_pos > bytes.length)) throw new FirmwareException("错误的文件");

                int time_pos = ByteUtil.getInt(bytes, v_pos + 24);
                if ((time_pos < 0) || (time_pos > bytes.length))
                    throw new FirmwareException("错误的文件");

                String verS = new String(bytes, ver_pos, 3, Charset.forName("US-ASCII"));
                String dayS = new String(bytes, day_pos, 11, Charset.forName("US-ASCII"));
                String timeS = new String(bytes, time_pos, 8, Charset.forName("US-ASCII"));

                if (verS == "") throw new FirmwareException("错误的文件");
                if (dayS == "") throw new FirmwareException("错误的文件");
                if (timeS == "") throw new FirmwareException("错误的文件");
                Platform = verS.substring(0, 3);
                if (!(Platform.equals("T01") || Platform.equals("T02") || (Platform.equals("T03")))) {
                    throw new FirmwareException("错误的文件");
                }

                try {
                    SimpleDateFormat df = new SimpleDateFormat("MMM dd yyyy HH:mm:ss", Locale.US);
                    Date date = df.parse(dayS + " " + timeS);
                    Firmware = date.getTime();
                } catch (Exception e) {
                    dbg.e(e.toString());
                }
            }
            String Address = getAddress();
            if (myLoc1 != 0) {
                bytes[myLoc1 + 5] = (byte) Integer.parseInt(Address.substring(0, 2), 16);
                bytes[myLoc1 + 4] = (byte) Integer.parseInt(Address.substring(3, 5), 16);
                bytes[myLoc1 + 3] = (byte) Integer.parseInt(Address.substring(6, 8), 16);
                bytes[myLoc1 + 2] = (byte) Integer.parseInt(Address.substring(9, 11), 16);
                bytes[myLoc1 + 1] = (byte) Integer.parseInt(Address.substring(12, 14), 16);
                bytes[myLoc1] = (byte) Integer.parseInt(Address.substring(15, 17), 16);
            }
            if (myLoc2 != 0) {
                bytes[myLoc2 + 5] = bytes[myLoc1];
                bytes[myLoc2 + 4] = bytes[myLoc1 + 1];
                bytes[myLoc2 + 3] = bytes[myLoc1 + 2];
                bytes[myLoc2 + 2] = bytes[myLoc1 + 3];
                bytes[myLoc2 + 1] = bytes[myLoc1 + 4];
                bytes[myLoc2] = bytes[myLoc1 + 5];
            }

            long temp = 0;

            Checksum = 0;
            int len = Size / 4;
            for (int i = 0; i < len; i++) {
                temp += ByteUtil.getUInt(bytes, i * 4);
            }
            long TempMask = 0x1FFFFFFFFL;
            TempMask -= 0x100000000L;
            Checksum = (int) (temp & TempMask);
        } finally {
            fs.close();
        }
    }

    @Override
    protected boolean startFirmwareUpdate() throws InterruptedException {
        try {
            onFirmwareUpdateStart();
            if (bytes.length > 31 * 1024) {
                onFirmwareFail();
                return false;
            }

//            if (!getPlatform().equals(firmware.Platform))
//            {
//                onFirmwareFail();
//                return false;
//            }

            if (Firmware == deviceIO.getFirmware()) {
                onFirmwareFail();
                return false;
            }


            byte[] data = new byte[20];
            data[0] = (byte) 0x89;

            for (int i = 0; i < Size; i += 8) {
                int p = i + 0x17c00;
                ByteUtil.putInt(data, p, 1);
                System.arraycopy(bytes, i, data, 5, 8);
                if (!deviceIO.send(data)) {
                    onFirmwareFail();
                    return false;
                } else {
                    onFirmwarePosition(i, Size);
                }
            }

            Thread.sleep(1000);
            byte[] checkSum = new byte[5];
            checkSum[0] = opCode_GetFirmwareSum;
            ByteUtil.putInt(checkSum, Size, 1);
            if (deviceIO.send(checkSum)) {
                Thread.sleep(200);
                checkSum = deviceIO.getLastRecvPacket();
                if (checkSum[0] == opCode_GetFirmwareSumRet) {
                    long sum = ByteUtil.getUInt(checkSum, 1);
                    if (sum == Checksum) {

                        byte[] update = new byte[5];
                        update[0]=(byte)0xc3;
                        ByteUtil.putInt(update, Size, 1);
                        if (deviceIO.send(update)) {
                            onFirmwareComplete();
                            Thread.sleep(5000);
                            return true;
                        } else {
                            onFirmwareFail();
                            return false;
                        }
                    } else {
                        onFirmwareFail();
                        return false;
                    }

                } else {
                    onFirmwareFail();
                    return false;
                }
            } else {
                onFirmwareFail();
                return false;
            }


        } catch (Exception e) {
            onFirmwareFail();
            return false;
        }
    }


}
