package com.ozner.WaterPurifier;

import android.util.Log;

/**
 * Created by ozner_67 on 2017/6/21.
 * 邮箱：xinde.zhang@cftcn.com
 * <p>
 * 厨上式水芯片设置二
 */

public class WaterSettingExtra {
    private static final String TAG = "WaterSettingExtra";
    private boolean isLoaded = false;
    /**
     * 加热水温
     */
    public int heatTemperature;
    /**
     * 自动开关机
     */
    public boolean isAutoPower;

    /**
     * 开机时间，整点，24小时制
     */
    public int openPowerPoint;

    /**
     * 关机时间，整点
     */
    public int closePowerPoint;
    /**
     * 自动加热
     */
    public boolean isAutoHeating;
    /**
     * 加热开启时间
     */
    public int openHeatPoint;
    /**
     * 加热关闭时间
     */
    public int closeHeatPoint;
    /**
     * 制冷开启
     */
    public boolean isOpenCool;


    public void fromBytes(byte[] data) {
        try {
            this.heatTemperature = data[0];
            this.isAutoPower = data[1] != 0 ? true : false;
            this.openPowerPoint = data[2];
            this.closePowerPoint = data[3];
            this.isAutoHeating = data[4] != 0 ? true : false;
            this.openHeatPoint = data[5];
            this.closeHeatPoint = data[6];
            this.isOpenCool = data[7] != 0 ? true : false;
            isLoaded = true;
        } catch (Exception ex) {
            Log.e(TAG, "WaterSettingExtra_fromBytes_Ex: " + ex.getMessage());
        }
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void reset() {
        isLoaded = false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("加热温度:").append(heatTemperature).append("\n");
        sb.append("自动开关机:").append(isAutoPower).append(" ,开机:")
                .append(openPowerPoint).append(" ,关机:").append(closePowerPoint).append("\n");
        sb.append("自动加热:").append(isAutoHeating).append(" ,加热:")
                .append(openHeatPoint).append(" ,停止:").append(closeHeatPoint).append("\n");
        sb.append("制冷:").append(isOpenCool);
        return sb.toString();
    }
}
