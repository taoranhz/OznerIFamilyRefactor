package com.ozner.yiquan.Device.AirPurifier;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.ozner.yiquan.Base.BaseActivity;
import com.ozner.yiquan.Device.AirPurifier.bean.NetWeather;
import com.ozner.yiquan.HttpHelper.HttpMethods;
import com.ozner.yiquan.HttpHelper.OznerHttpResult;
import com.ozner.yiquan.HttpHelper.ProgressSubscriber;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by ozner_67 on 2016/12/5.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class AirPurifierPresenter {
    private static final String TAG = "AirPurifierPresenter";
    private WeakReference<Context> mContext;

    interface NetWeatherResult {
        void onResult(NetWeather weather);
    }

    public AirPurifierPresenter(Context context) {
        this.mContext = new WeakReference<Context>(context);
    }

    /**
     * 获取室外天气信息
     */
    public void getWeatherOutSide(final NetWeatherResult result) {
        HttpMethods.getInstance().getWeatherOutSide(new ProgressSubscriber<JsonObject>(mContext.get()
                , new OznerHttpResult<JsonObject>() {
            @Override
            public void onError(Throwable e) {
                if (result != null)
                    result.onResult(null);
            }

            @Override
            public void onNext(JsonObject jsonObject) {
//                Log.e(TAG, "getWeatherOutSide_result: " + jsonObject.toString());
                try {
                    if (jsonObject.get("state").getAsInt() > 0) {
                        NetWeather weather = new NetWeather();
//                        Log.e(TAG, "weatherform: " + jsonObject.get("weatherform").getAsString());
                        weather.setWeatherform(jsonObject.get("weatherform").getAsString());

                        JSONObject dataJson = new JSONObject(jsonObject.get("data").getAsString());
                        JSONArray array = dataJson.getJSONArray("HeWeather data service 3.0");
//                        Log.e(TAG, "call: data:" + array.length());
//                        JsonPrimitive array = data.get("HeWeather data service 3.0").getAsJsonPrimitive();

                        if (array.length() > 0) {
                            JSONObject firstElement = (JSONObject) array.get(0);
                            if (firstElement.get("now") != null) {
                                JSONObject now = (JSONObject) firstElement.get("now");
//                                Log.e(TAG, "hum: " + String.valueOf(now.get("hum")));
                                weather.setHum(String.valueOf(now.get("hum")));
                                weather.setTmp(String.valueOf(now.get("tmp")));
                            }

                            if (firstElement.get("basic") != null) {
//                                Log.e(TAG, "city: " + ((JSONObject) firstElement.get("basic")).get("city"));
                                weather.setCity(((JSONObject) firstElement.get("basic")).get("city").toString());
                                JSONObject update = ((JSONObject) firstElement.get("basic")).getJSONObject("update");
                                if (update != null) {
                                    weather.setUpdateLocTime(update.getString("loc"));
                                    weather.setUpdateUtcTime(update.getString("utc"));
                                }
                            }
                            if (firstElement.get("aqi") != null) {
                                JSONObject city = (JSONObject) ((JSONObject) firstElement.get("aqi")).get("city");
//                                Log.e(TAG, "pm25: " + String.valueOf(city.get("pm25")).toString());
                                weather.setAqi(city.get("aqi").toString());
                                weather.setCo(city.get("co").toString());
                                weather.setNo2(city.get("no2").toString());
                                weather.setO3(city.get("o3").toString());
                                weather.setPm10(city.get("pm10").toString());
                                weather.setPm25(city.get("pm25").toString());
                                weather.setSo2(city.get("so2").toString());
                                weather.setQlty(city.get("qlty").toString());
                            }
                        }
                        if (result != null)
                            result.onResult(weather);
                    } else {
                        if (jsonObject.get("state").getAsInt() == -10006
                                || jsonObject.get("state").getAsInt() == -10007) {
                            BaseActivity.reLogin((BaseActivity) mContext.get());
                        } else if (result != null)
                            result.onResult(null);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "getWeatherOutSide_Ex: " + ex.getMessage());
                    if (result != null)
                        result.onResult(null);
                }
            }
        }));
    }
}
