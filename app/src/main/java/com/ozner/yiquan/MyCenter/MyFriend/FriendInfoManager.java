package com.ozner.yiquan.MyCenter.MyFriend;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.ozner.yiquan.Base.BaseActivity;
import com.ozner.yiquan.Command.OznerPreference;
import com.ozner.yiquan.DBHelper.DBManager;
import com.ozner.yiquan.DBHelper.FriendRankItem;
import com.ozner.yiquan.HttpHelper.ApiException;
import com.ozner.yiquan.HttpHelper.HttpMethods;
import com.ozner.yiquan.HttpHelper.OznerHttpResult;
import com.ozner.yiquan.HttpHelper.ProgressSubscriber;
import com.ozner.yiquan.MyCenter.MyFriend.bean.CenterRankItem;
import com.ozner.yiquan.MyCenter.MyFriend.bean.FriendItem;
import com.ozner.yiquan.MyCenter.MyFriend.bean.LeaveMessageItem;
import com.ozner.yiquan.MyCenter.MyFriend.bean.LikeMeItem;
import com.ozner.yiquan.MyCenter.MyFriend.bean.VerifyMessageItem;
import com.ozner.yiquan.R;
import com.ozner.yiquan.Utils.LCLogUtils;

import java.lang.ref.WeakReference;
import java.util.List;

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
                new ProgressSubscriber<JsonObject>(mContext.get(), new OznerHttpResult<JsonObject>() {
                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        try {
                            if (jsonObject != null) {
                                if (jsonObject.get("state").getAsInt() > 0) {
//                                    String data = jsonObject.get("data").getAsString();
//                                    List<FriendRankItem> resultList = JSON.parseArray(data, FriendRankItem.class);
                                    JsonArray array = jsonObject.getAsJsonArray("data");
                                    LCLogUtils.E(TAG, "loadFriendRank:" + array.toString());
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
                                } else {
                                    if (jsonObject.get("state").getAsInt() == -10006
                                            || jsonObject.get("state").getAsInt() == -10007) {
                                        BaseActivity.reLogin((BaseActivity) mContext.get());
                                    }
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
                        new OznerHttpResult<JsonObject>() {
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
                                            JsonArray array = jsonObject.get("data").getAsJsonArray();
                                            LCLogUtils.E(TAG, "result:" + array.toString());
                                            List<CenterRankItem> result = new Gson().fromJson(array, new TypeToken<List<CenterRankItem>>() {
                                            }.getType());
//                                            List<CenterRankItem> result = JSON.parseArray(array, CenterRankItem.class);
                                            if (listener != null) {
                                                listener.onSuccess(result);
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
                        new OznerHttpResult<JsonObject>() {
                            @Override
                            public void onError(Throwable e) {
                                if (listener != null) {
                                    listener.onFail(e.getMessage());
                                }
                            }

                            @Override
                            public void onNext(JsonObject jsonObject) {
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
                        mContext.get().getString(R.string.submiting),
                        false,
                        new OznerHttpResult<JsonObject>() {
                            @Override
                            public void onError(Throwable e) {
                                if (listener != null) {
                                    listener.onFail(e.getMessage());
                                }
                            }

                            @Override
                            public void onNext(JsonObject jsonObject) {
                                Log.e(TAG, "getWhoLikeMe: " + jsonObject.toString());
                                try {
                                    if (jsonObject != null) {
                                        if (jsonObject.get("state").getAsInt() > 0) {
                                            if (listener != null) {
                                                listener.onSuccess(position);
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

    public interface FriendListListener {
        void onSuccess(List<FriendItem> result);

        void onFail(String msg);
    }

    /**
     * 获取好友列表
     *
     * @param listener
     */
    public void getFriendList(final FriendListListener listener) {
        HttpMethods.getInstance().getFriendList(OznerPreference.getUserToken(mContext.get()),
                new ProgressSubscriber<JsonObject>(mContext.get(),
                        mContext.get().getString(R.string.data_loading),
                        false,
                        new OznerHttpResult<JsonObject>() {
                            @Override
                            public void onError(Throwable e) {
                                if (listener != null) {
                                    listener.onFail(e.getMessage());
                                }
                            }

                            @Override
                            public void onNext(JsonObject jsonObject) {
                                try {
                                    if (jsonObject != null) {
                                        LCLogUtils.E(TAG, "getFriendList:" + jsonObject.toString());
                                        if (jsonObject.get("state").getAsInt() > 0) {
                                            if (listener != null) {
                                                JsonArray array = jsonObject.getAsJsonArray("friendlist");
                                                List<FriendItem> result = new Gson().fromJson(array, new TypeToken<List<FriendItem>>() {
                                                }.getType());
                                                listener.onSuccess(result);
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
                                    ex.printStackTrace();
                                    if (listener != null) {
                                        listener.onFail(ex.getMessage());
                                    }
                                }
                            }
                        }));
    }

    public interface HistoryMsgListener {
        void onSuccess(List<LeaveMessageItem> result);

        void onFail(String msg);
    }

    /**
     * 获取历史留言
     */
    public void getHistoryMessage(String otherUserid, final HistoryMsgListener listener) {
        HttpMethods.getInstance().getHistoryMessage(OznerPreference.getUserToken(mContext.get()), otherUserid,
                new ProgressSubscriber<JsonObject>(mContext.get(),
                        mContext.get().getString(R.string.data_loading),
                        false,
                        new OznerHttpResult<JsonObject>() {
                            @Override
                            public void onError(Throwable e) {
                                if (listener != null) {
                                    listener.onFail(e.getMessage());
                                }
                            }

                            @Override
                            public void onNext(JsonObject jsonObject) {
                                try {
                                    if (jsonObject != null) {
                                        LCLogUtils.E(TAG, "getFriendList:" + jsonObject.toString());
                                        if (jsonObject.get("state").getAsInt() > 0) {
                                            if (listener != null) {
                                                JsonArray array = jsonObject.getAsJsonArray("data");
                                                List<LeaveMessageItem> result = new Gson().fromJson(array, new TypeToken<List<LeaveMessageItem>>() {
                                                }.getType());
                                                listener.onSuccess(result);
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
                                    ex.printStackTrace();
                                    if (listener != null) {
                                        listener.onFail(ex.getMessage());
                                    }
                                }
                            }
                        }
                ));
    }

    public interface LeaveMsgListener {
        void onSuccess();

        void onFail(String msg);
    }

    /**
     * 留言
     *
     * @param otherUserid
     * @param msg
     * @param listener
     */
    public void leaveMessage(String otherUserid, String msg, final LeaveMsgListener listener) {
        HttpMethods.getInstance().leaveMessage(OznerPreference.getUserToken(mContext.get()), otherUserid, msg,
                new ProgressSubscriber<JsonObject>(mContext.get(),
                        mContext.get().getString(R.string.sending),
                        false,
                        new OznerHttpResult<JsonObject>() {
                            @Override
                            public void onError(Throwable e) {
                                if (listener != null) {
                                    listener.onFail(e.getMessage());
                                }
                            }

                            @Override
                            public void onNext(JsonObject jsonObject) {
                                try {
                                    if (jsonObject != null) {
                                        if (jsonObject.get("state").getAsInt() > 0) {
                                            if (listener != null) {
                                                listener.onSuccess();
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
                                    ex.printStackTrace();
                                    if (listener != null) {
                                        listener.onFail(ex.getMessage());
                                    }
                                }
                            }
                        }));
    }

    public interface LoadVerifyListener {
        void onSuccess(List<VerifyMessageItem> result);

        void onFail(String msg);
    }

    /**
     * 获取验证消息列表
     *
     * @param listener
     */
    public void getVerifyMessage(final LoadVerifyListener listener) {
        HttpMethods.getInstance().getVerifyMessage(OznerPreference.getUserToken(mContext.get()),
                new ProgressSubscriber<JsonObject>(mContext.get(),
                        mContext.get().getString(R.string.data_loading), false, new OznerHttpResult<JsonObject>() {
                    @Override
                    public void onError(Throwable e) {
                        if (listener != null) {
                            listener.onFail(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        try {
                            if (jsonObject != null) {
                                if (jsonObject.get("state").getAsInt() > 0) {
                                    JsonArray array = jsonObject.getAsJsonArray("msglist");
                                    List<VerifyMessageItem> result = new Gson().fromJson(array, new TypeToken<List<VerifyMessageItem>>() {
                                    }.getType());
                                    if (listener != null) {
                                        listener.onSuccess(result);
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
                            ex.printStackTrace();
                            if (listener != null) {
                                listener.onFail(ex.getMessage());
                            }
                        }
                    }
                }));
    }

    /**
     * 获取验证消息列表
     *
     * @param listener
     */
    public void getVerifyMessageNoDialog(final LoadVerifyListener listener) {
        HttpMethods.getInstance().getVerifyMessage(OznerPreference.getUserToken(mContext.get()),
                new ProgressSubscriber<JsonObject>(mContext.get(), new OznerHttpResult<JsonObject>() {
                    @Override
                    public void onError(Throwable e) {
                        if (listener != null) {
                            listener.onFail(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        try {
                            if (jsonObject != null) {
                                if (jsonObject.get("state").getAsInt() > 0) {
                                    JsonArray array = jsonObject.getAsJsonArray("msglist");
                                    List<VerifyMessageItem> result = new Gson().fromJson(array, new TypeToken<List<VerifyMessageItem>>() {
                                    }.getType());
                                    if (listener != null) {
                                        listener.onSuccess(result);
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
                            ex.printStackTrace();
                            if (listener != null) {
                                listener.onFail(ex.getMessage());
                            }
                        }
                    }
                }));
    }

    /**
     * 接受验证请求
     *
     * @param id
     * @param listener
     */
    public void acceptUserVerify(String id, final LeaveMsgListener listener) {
        HttpMethods.getInstance().acceptUserVerify(OznerPreference.getUserToken(mContext.get()),
                id,
                new ProgressSubscriber<JsonObject>(mContext.get(),
                        mContext.get().getString(R.string.submiting),
                        false, new OznerHttpResult<JsonObject>() {
                    @Override
                    public void onError(Throwable e) {
                        if (listener != null) {
                            listener.onFail(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        try {
                            if (jsonObject != null) {
                                if (jsonObject.get("state").getAsInt() > 0) {
                                    if (listener != null) {
                                        listener.onSuccess();
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
                            ex.printStackTrace();
                            if (listener != null) {
                                listener.onFail(ex.getMessage());
                            }
                        }
                    }
                }));
    }
}
