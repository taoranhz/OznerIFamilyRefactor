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
import com.ozner.cup.HttpHelper.HttpMethods;
import com.ozner.cup.HttpHelper.ProgressSubscriber;

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

    public void loadFriendRank() {
        HttpMethods.getInstance().getRankNotify(OznerPreference.getUserToken(mContext.get()),
                new ProgressSubscriber<JsonObject>(mContext.get(), new Action1<JsonObject>() {
                    @Override
                    public void call(JsonObject jsonObject) {
                        try {
                            if (jsonObject != null) {
                                if (jsonObject.get("state").getAsInt() > 0) {
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
                            Log.e(TAG, "loadFriendRank_Ex: "+ex.getMessage());
                        }
                    }
                }));
    }
}
