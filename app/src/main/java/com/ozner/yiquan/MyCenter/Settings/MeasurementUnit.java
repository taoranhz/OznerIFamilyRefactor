package com.ozner.yiquan.MyCenter.Settings;

/**
 * Created by xinde on 2015/12/10.
 */
//计量单位
public class MeasurementUnit {
    //一盎司 —> 克
    public static final float OZ_TO_G = 28.35f;

    //温度单位
    public static class TempUnit {
        public static final int CENTIGRADE = 0;//摄氏度
        public static final int FAHRENHEIT = 1;//华氏度 fahrenheit
    }

    //水量单位
    public static class VolumUnit {
        public static final int ML = 0;//毫升
        public static final int DL = 1;//分升
        public static final int OZ = 2;//盎司
    }
}
