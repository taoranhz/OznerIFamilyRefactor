package com.ozner.cup.Device;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.HttpHelper.ApiException;
import com.ozner.cup.HttpHelper.HttpMethods;
import com.ozner.cup.HttpHelper.OznerHttpResult;
import com.ozner.cup.HttpHelper.ProgressSubscriber;

import java.lang.ref.WeakReference;

/**
 * Created by ozner_67 on 2016/12/8.
 * 邮箱：xinde.zhang@cftcn.com
 * <p>
 * 上传TDS获取排名
 */

public class TDSSensorManager {
    private static final String TAG = "TDSSensorManager";
    private WeakReference<Context> mContext;

    public interface TDSListener {
        void onSuccess(int result);

        void onFail(String msg);
    }

    public TDSSensorManager(Context context) {
        mContext = new WeakReference<Context>(context);
    }

    /**
     * 上传TDS获取排名
     *
     * @param mac
     * @param deviceType
     * @param tds
     * @param beforetds
     * @param dsn
     * @param listener
     */
    public void updateTds(String mac, String deviceType, String tds, String beforetds, String dsn, final TDSListener listener) {
        HttpMethods.getInstance().updateTDSSensor(OznerPreference.getUserToken(mContext.get())
                , mac, deviceType, tds, beforetds, dsn
                , new ProgressSubscriber<JsonObject>(mContext.get(), new OznerHttpResult<JsonObject>() {
                    @Override
                    public void onError(Throwable e) {
                        if (listener != null) {
                            listener.onFail(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        Log.e(TAG, "updateTds: " + jsonObject.toString());
                        if (jsonObject != null) {
                            if (jsonObject.get("state").getAsInt() > 0) {
                                int rank = 0, total = 1, per = 0;
                                if (!jsonObject.get("rank").isJsonNull()) {
                                    rank = jsonObject.get("rank").getAsInt();
                                }
                                if (!jsonObject.get("total").isJsonNull()) {
                                    total = jsonObject.get("total").getAsInt();
                                }

                                if (total > 0) {
                                    per = (total - rank) * 100 / total;
                                }

                                if (listener != null) {
                                    listener.onSuccess(per);
                                }
                            } else {
                                if (jsonObject.get("state").getAsInt() == -10006
                                        || jsonObject.get("state").getAsInt() == -10007) {
                                    BaseActivity.reLogin((BaseActivity) mContext.get());
                                } else if (listener != null) {
                                    listener.onFail(mContext.get().getString(ApiException.getErrResId(jsonObject.get("state").getAsInt())));
                                }
                            }
                        } else {
                            if (listener != null) {
                                listener.onFail("result is null");
                            }
                        }
                    }
                }));
    }


    /**
     * 获取朋友圈TDS排名
     *
     * @param rankType
     * @param listener
     */
    public void getTdsFriendRank(String rankType, final TDSListener listener) {
        HttpMethods.getInstance().getTdsFriendRank(OznerPreference.getUserToken(mContext.get()), rankType
                , new ProgressSubscriber<JsonObject>(mContext.get(), new OznerHttpResult<JsonObject>() {
                    @Override
                    public void onError(Throwable e) {
                        if (listener != null) {
                            listener.onFail(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        Log.e(TAG, "getTdsFriendRank: " + jsonObject.toString());
                        try {
                            if (jsonObject != null) {
                                if (jsonObject.get("state").getAsInt() > 0) {
                                    int rank = 0;
                                    String userid = OznerPreference.GetValue(mContext.get(), OznerPreference.UserId, null);
                                    JsonArray array = jsonObject.get("data").getAsJsonArray();
                                    for (JsonElement element : array) {
                                        if (userid.equals(element.getAsJsonObject().get("userid").getAsString())) {
                                            rank = element.getAsJsonObject().get("rank").getAsInt();
                                        }
                                    }
                                    if (listener != null) {
                                        listener.onSuccess(rank);
                                    }
                                } else {
                                    if (jsonObject.get("state").getAsInt() == -10006
                                            || jsonObject.get("state").getAsInt() == -10007) {
                                        BaseActivity.reLogin((BaseActivity) mContext.get());
                                    } else if (listener != null) {
                                        listener.onFail(mContext.get().getString(ApiException.getErrResId(jsonObject.get("state").getAsInt())));
                                    }
                                }
                            } else {
                                if (listener != null) {
                                    listener.onFail("result is null");
                                }
                            }
                        } catch (Exception ex) {
                            Log.e(TAG, "getTdsFriendRank_Ex: " + ex.getMessage());
                            if (listener != null) {
                                listener.onFail(ex.getMessage());
                            }
                        }
                    }
                }));
    }

    /**
     * 获取朋友圈饮水量排名
     *
     * @param listener
     */
    public void getVolumeFriendRank(final TDSListener listener) {
        HttpMethods.getInstance().getVolumeFriendRank(OznerPreference.getUserToken(mContext.get())
                , new ProgressSubscriber<JsonObject>(mContext.get(), new OznerHttpResult<JsonObject>() {
                    @Override
                    public void onError(Throwable e) {
                        if (listener != null) {
                            listener.onFail(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        Log.e(TAG, "getTdsFriendRank: " + jsonObject.toString());
                        try {
                            if (jsonObject != null) {
                                if (jsonObject.get("state").getAsInt() > 0) {
                                    int rank = 0;
                                    // TODO: 2016/12/8 处理返回数据
                                    if (listener != null) {
                                        listener.onSuccess(rank);
                                    }
                                } else {
                                    if (jsonObject.get("state").getAsInt() == -10006
                                            || jsonObject.get("state").getAsInt() == -10007) {
                                        BaseActivity.reLogin((BaseActivity) mContext.get());
                                    } else if (listener != null) {
                                        listener.onFail(mContext.get().getString(ApiException.getErrResId(jsonObject.get("state").getAsInt())));
                                    }
                                }
                            } else {
                                if (listener != null) {
                                    listener.onFail("result is null");
                                }
                            }
                        } catch (Exception ex) {
                            Log.e(TAG, "getTdsFriendRank_Ex: " + ex.getMessage());
                            if (listener != null) {
                                listener.onFail(ex.getMessage());
                            }
                        }
                    }
                }));
    }


    /**
     * 更新当天饮水量获取好友内排名
     *
     * @param mac
     * @param type
     * @param volume
     * @param listener
     */
    public void updateVolumeSensor(String mac, String type, String volume, final TDSListener listener) {
        HttpMethods.getInstance().updateVolumeSensor(OznerPreference.getUserToken(mContext.get()), mac, type, volume,
                new ProgressSubscriber<JsonObject>(mContext.get(), new OznerHttpResult<JsonObject>() {
                    @Override
                    public void onError(Throwable e) {
                        if (listener != null) {
                            listener.onFail(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        Log.e(TAG, "updateVolumeSensor: " + jsonObject.toString());
                        try {
                            if (jsonObject != null) {
                                if (jsonObject.get("state").getAsInt() > 0) {
                                    if (listener != null) {
                                        listener.onSuccess(jsonObject.get("state").getAsInt());
                                    }
                                } else {
                                    if (jsonObject.get("state").getAsInt() == -10006
                                            || jsonObject.get("state").getAsInt() == -10007) {
                                        BaseActivity.reLogin((BaseActivity) mContext.get());
                                    } else if (listener != null) {
                                        listener.onFail(mContext.get().getString(ApiException.getErrResId(jsonObject.get("state").getAsInt())));
                                    }
                                }
                            } else {
                                if (listener != null) {
                                    listener.onFail("result is null");
                                }
                            }
                        } catch (Exception ex) {
                            Log.e(TAG, "getTdsFriendRank_Ex: " + ex.getMessage());
                            if (listener != null) {
                                listener.onFail(ex.getMessage());
                            }
                        }
                    }
                }));
    }
}
