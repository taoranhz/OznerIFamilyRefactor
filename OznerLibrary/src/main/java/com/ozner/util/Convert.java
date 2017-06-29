package com.ozner.util;

/**
 * Created by ozner_67 on 2017/3/28.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class Convert {
    private static final String TAG = "Convert";

    public static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static byte toByte(char c) {
        byte b = (byte) "0123456789abcdef".indexOf(c);
        return b;
    }

    public static byte[] StringToByteArray(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toLowerCase().toCharArray();
        for(int i=0;i<len;i++){
            int pos = i*2;
            result[i] = (byte)(toByte(achar[pos])<<4 | toByte(achar[pos+1]));
        }
        return result;
    }
}
