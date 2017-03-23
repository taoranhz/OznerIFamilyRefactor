package com.ozner.cup.Device.WaterPurifier;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.WaterPurifierAttr;
import com.ozner.cup.HttpHelper.HttpMethods;
import com.ozner.cup.HttpHelper.OznerHttpResult;
import com.ozner.cup.HttpHelper.ProgressSubscriber;
import com.ozner.cup.Utils.LCLogUtils;

import java.lang.ref.WeakReference;
import java.util.Date;

/**
 * Created by ozner_67 on 2016/11/16.
 * 邮箱：xinde.zhang@cftcn.com
 * <p>
 * 净水器网络信息管理
 */

public class WaterNetInfoManager {
    private static final String TAG = "WaterNetInfoManager";
    WeakReference<Context> mContext;

    public interface IWaterAttr {
        void onResult(WaterPurifierAttr attr);
    }

    public WaterNetInfoManager(Context context) {
        this.mContext = new WeakReference<Context>(context);
    }

    /**
     * 获取净水器类型信息
     *
     * @param mac
     * @param attrListener
     */
    public void getMatchineType(final String mac, final IWaterAttr attrListener) {
        Log.e(TAG, "getMatchineType: " + mac);
        HttpMethods.getInstance().getMatchineType(mac
                , new ProgressSubscriber<JsonObject>(mContext.get(), new OznerHttpResult<JsonObject>() {
                    @Override
                    public void onError(Throwable e) {
                        if (attrListener != null) {
                            attrListener.onResult(null);
                        }
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        if (jsonObject != null) {
                            LCLogUtils.E(TAG,"MachineType:"+jsonObject.toString());
                            if (jsonObject.get("state").getAsInt() > 0) {
                                WaterPurifierAttr attr = DBManager.getInstance(mContext.get()).getWaterAttr(mac);
                                if (null == attr) {
                                    attr = new WaterPurifierAttr();
                                }
                                attr.fromJsonObject(mac, jsonObject.get("data").getAsJsonObject());
                                DBManager.getInstance(mContext.get()).updateWaterAttr(attr);
                                if (attrListener != null)
                                    attrListener.onResult(attr);
                            } else {
                                if (jsonObject.get("state").getAsInt() == -10006
                                        || jsonObject.get("state").getAsInt() == -10007) {
                                    BaseActivity.reLogin((BaseActivity) mContext.get());
                                } else if (attrListener != null) {
                                    attrListener.onResult(null);
                                }
                            }
                        } else {
                            if (attrListener != null)
                                attrListener.onResult(null);
                        }
                    }
                }));
    }

    /**
     * 获取净水去滤芯信息
     *
     * @param mac
     * @param attrListener
     */
    public void getWaterFilterInfo(final String mac, final IWaterAttr attrListener) {
        HttpMethods.getInstance().getWaterFilterInfo(mac
                , new ProgressSubscriber<JsonObject>(mContext.get(), new OznerHttpResult<JsonObject>() {
                    @Override
                    public void onError(Throwable e) {
                        if (attrListener != null) {
                            attrListener.onResult(null);
                        }
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        if (jsonObject != null) {
                            if (jsonObject.get("state").getAsInt() > 0) {
                                Log.e(TAG, "getWaterFilterInfo: " + jsonObject.toString());
                                WaterPurifierAttr attr = DBManager.getInstance(mContext.get()).getWaterAttr(mac);
                                if (null == attr) {
                                    attr = new WaterPurifierAttr();
                                }
                                attr.setMac(mac);
                                String nowtime = jsonObject.get("nowtime").getAsString();
                                String time = jsonObject.get("time").getAsString();
                                Date date = new Date(nowtime);
                                attr.setFilterNowtime(date.getTime());
                                date = new Date(time);
                                attr.setFilterTime(date.getTime());
                                DBManager.getInstance(mContext.get()).updateWaterAttr(attr);
                                if (attrListener != null)
                                    attrListener.onResult(attr);
                            } else {
                                if (jsonObject.get("state").getAsInt() == -10006
                                        || jsonObject.get("state").getAsInt() == -10007) {
                                    BaseActivity.reLogin((BaseActivity) mContext.get());
                                } else if (attrListener != null) {
                                    attrListener.onResult(null);
                                }
                            }
                        } else {
                            if (attrListener != null)
                                attrListener.onResult(null);
                        }
                    }
                }));
    }
}
