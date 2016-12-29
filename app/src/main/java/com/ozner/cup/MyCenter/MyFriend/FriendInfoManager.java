package com.ozner.cup.MyCenter.MyFriend;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.FriendRankItem;
import com.ozner.cup.HttpHelper.ApiException;
import com.ozner.cup.HttpHelper.HttpMethods;
import com.ozner.cup.HttpHelper.ProgressSubscriber;
import com.ozner.cup.R;
import com.ozner.cup.Utils.LCLogUtils;

import java.lang.ref.WeakReference;
import java.util.List;

import rx.functions.Action1;

/**
 * Created by ozner_67 on 2016/12/26.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class FriendInfoManager implements IFriendInfoManager {
    private static final String TAG = "FriendInfoManager";
    private WeakReference<Context> mContext;
    private IRankView rankView;

    public FriendInfoManager(Context context, IRankView rankView) {
        this.rankView = rankView;
        this.mContext = new WeakReference<Context>(context);
    }


    public interface RankListener {
        void onSuccess(List<CenterRankItem> result);

        void onFail(String msg);
    }

    public void loadFriendRank() {
        HttpMethods.getInstance().getRankNotify(OznerPreference.getUserToken(mContext.get()),
                new ProgressSubscriber<JsonObject>(mContext.get(), new Action1<JsonObject>() {
                    @Override
                    public void call(JsonObject jsonObject) {
                        try {
                            if (jsonObject != null) {
                                if (jsonObject.get("state").getAsInt() > 0) {
//                                    String data = jsonObject.get("data").getAsString();
//                                    List<FriendRankItem> resultList = JSON.parseArray(data, FriendRankItem.class);
                                    JsonArray array = jsonObject.getAsJsonArray("data");
                                    List<FriendRankItem> resultList = new Gson().fromJson(array, new TypeToken<List<FriendRankItem>>() {
                                    }.getType());
                                    DBManager.getInstance(mContext.get()).insertFriendRank(resultList, new DBManager.DBRxListener() {
                                        @Override
                                        public void onSuccess() {
                                            new Handler().post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (rankView != null) {
                                                        rankView.refreshRankData();
                                                    }
                                                }
                                            });
                                        }

                                        @Override
                                        public void onFail() {

                                        }
                                    });

                                }
                            } else {

                            }
                        } catch (Exception ex) {
                            Log.e(TAG, "loadFriendRank_Ex: " + ex.getMessage());
                        }
                    }
                }));
    }

    /**
     * 获取朋友圈排名列表
     *
     * @param rankType
     * @param listener
     */
    public void getTdsFriendRank(String rankType, final RankListener listener) {
        HttpMethods.getInstance().getTdsFriendRank(OznerPreference.getUserToken(mContext.get()), rankType
                , new ProgressSubscriber<JsonObject>(mContext.get(),
                        mContext.get().getString(R.string.data_loading),
                        false,
                        new Action1<JsonObject>() {
                            @Override
                            public void call(JsonObject jsonObject) {
                                Log.e(TAG, "getTdsFriendRank: " + jsonObject.toString());
                                try {
                                    if (jsonObject != null) {
                                        if (jsonObject.get("state").getAsInt() > 0) {
                                            JsonArray array = jsonObject.get("data").getAsJsonArray();
                                            LCLogUtils.E(TAG,"result:"+array.toString());
                                            List<CenterRankItem> result = new Gson().fromJson(array, new TypeToken<List<CenterRankItem>>() {
                                            }.getType());
//                                            List<CenterRankItem> result = JSON.parseArray(array, CenterRankItem.class);
                                            if (listener != null) {
                                                listener.onSuccess(result);
                                            }
                                        } else {
                                            if (listener != null) {
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

    public interface LikeMeListener {
        void onSuccess(List<LikeMeItem> result);

        void onFail(String msg);
    }

    /**
     * 获取赞我的人
     *
     * @param type
     * @param listener
     */
    public void getWhoLikeMe(String type, final LikeMeListener listener) {
        HttpMethods.getInstance().getWhoLikeMe(OznerPreference.getUserToken(mContext.get()), type
                , new ProgressSubscriber<JsonObject>(mContext.get(),
                        mContext.get().getString(R.string.data_loading),
                        false,
                        new Action1<JsonObject>() {
                            @Override
                            public void call(JsonObject jsonObject) {
                                Log.e(TAG, "getWhoLikeMe: " + jsonObject.toString());
                                try {
                                    if (jsonObject != null) {
                                        if (jsonObject.get("state").getAsInt() > 0) {
                                            JsonArray array = jsonObject.get("data").getAsJsonArray();
                                            List<LikeMeItem> result = new Gson().fromJson(array, new TypeToken<List<LikeMeItem>>() {
                                            }.getType());
                                            if (listener != null) {
                                                listener.onSuccess(result);
                                            }
                                        } else {
                                            if (listener != null) {
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

    public interface LikeOhterListener {
        void onSuccess(int pos);

        void onFail(String msg);
    }

    /**
     * 对其他用户点赞
     *
     * @param likeuserid
     * @param type
     * @param position
     * @param listener
     */
    public void likeOhterUser(String likeuserid, String type, final int position, final LikeOhterListener listener) {
        HttpMethods.getInstance().likeOtherUser(OznerPreference.getUserToken(mContext.get()), likeuserid, type
                , new ProgressSubscriber<JsonObject>(mContext.get(),
                        mContext.get().getString(R.string.data_loading),
                        false,
                        new Action1<JsonObject>() {
                            @Override
                            public void call(JsonObject jsonObject) {
                                Log.e(TAG, "getWhoLikeMe: " + jsonObject.toString());
                                try {
                                    if (jsonObject != null) {
                                        if (jsonObject.get("state").getAsInt() > 0) {
                                            if (listener != null) {
                                                listener.onSuccess(position);
                                            }
                                        } else {
                                            if (listener != null) {
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
