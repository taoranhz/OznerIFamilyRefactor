package com.ozner.yiquan.Device.AirPurifier.bean;

/**
 * Created by ozner_67 on 2016/12/5.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class NetWeather {
    private String weatherfrom;
    private String city;//城市
    private String aqi; //空气质量指数
    private String co;//一氧化碳1小时平均值(ug/m³)
    private String no2; //二氧化氮1小时平均值(ug/m³)
    private String o3;//臭氧1小时平均值(ug/m³)
    private String pm10; //PM10 1小时平均值(ug/m³)
    private String pm25; //PM2.5 1小时平均值(ug/m³)
    private String qlty; //空气质量类别
    private String so2; //二氧化硫1小时平均值(ug/m³)
    private String hum; //相对湿度（%）
    private String tmp; //温度
    private String updateLocTime="";//更新时间（本地）
    private String updateUtcTime="";//更新时间（UTC）

    public String getWeatherform() {
        return weatherfrom;
    }

    public void setWeatherform(String weatherform) {
        this.weatherfrom = weatherform;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAqi() {
        return aqi;
    }

    public void setAqi(String aqi) {
        this.aqi = aqi;
    }

    public String getCo() {
        return co;
    }

    public void setCo(String co) {
        this.co = co;
    }

    public String getNo2() {
        return no2;
    }

    public void setNo2(String no2) {
        this.no2 = no2;
    }

    public String getO3() {
        return o3;
    }

    public void setO3(String o3) {
        this.o3 = o3;
    }

    public String getPm10() {
        return pm10;
    }

    public void setPm10(String pm10) {
        this.pm10 = pm10;
    }

    public String getPm25() {
        return pm25;
    }

    public void setPm25(String pm25) {
        this.pm25 = pm25;
    }

    public String getQlty() {
        return qlty;
    }

    public void setQlty(String qlty) {
        this.qlty = qlty;
    }

    public String getSo2() {
        return so2;
    }

    public void setSo2(String so2) {
        this.so2 = so2;
    }

    public String getHum() {
        return hum;
    }

    public void setHum(String hum) {
        this.hum = hum;
    }

    public String getTmp() {
        return tmp;
    }

    public void setTmp(String tmp) {
        this.tmp = tmp;
    }

    public String getUpdateLocTime() {
        return updateLocTime;
    }

    public void setUpdateLocTime(String updateLocTime) {
        this.updateLocTime = updateLocTime;
    }

    public String getUpdateUtcTime() {
        return updateUtcTime;
    }

    public void setUpdateUtcTime(String updateUtcTime) {
        this.updateUtcTime = updateUtcTime;
    }

    @Override
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("weatherfrom:");
        str.append(weatherfrom);
        str.append(" , city:");
        str.append(city);
        str.append(" , aqi:");
        str.append(aqi);
        str.append(" , pm25:");
        str.append(pm25);
        str.append(" , hum:");
        str.append(hum);
        str.append(" , tmp");
        str.append(tmp);
        str.append(" ,updateLocTime:");
        str.append(updateLocTime);
        str.append(" ,updateUtcTime:");
        str.append(updateUtcTime);

        return str.toString();
    }
}
