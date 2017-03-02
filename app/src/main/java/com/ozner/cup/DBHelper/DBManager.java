package com.ozner.cup.DBHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ozner.cup.Utils.LCLogUtils;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ozner_67 on 2016/11/10.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class DBManager {
    private static final String TAG = "DBManager";
    private static final String dbName = "ozner_cup_db";
    private static DBManager mInstance;
    private DaoMaster.DevOpenHelper openHelper;
    private Context mContext;

    public interface DBRxListener {
        void onSuccess();

        void onFail();
    }

    public DBManager(Context context) {
        this.mContext = context;
        openHelper = new DaoMaster.DevOpenHelper(context, dbName, null);
    }

    /**
     * 获取单例引用
     *
     * @param context
     *
     * @return
     */
    public static DBManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (DBManager.class) {
                if (mInstance == null) {
                    mInstance = new DBManager(context);
                }
            }
        }
        return mInstance;
    }

    /**
     * 获取可读数据库
     */
    private SQLiteDatabase getReadableDatabase() {
        if (openHelper == null) {
            openHelper = new DaoMaster.DevOpenHelper(mContext, dbName, null);
        }
        return openHelper.getReadableDatabase();
    }

    /**
     * 获取可写数据库
     */
    private SQLiteDatabase getWritableDatabase() {
        if (openHelper == null) {
            openHelper = new DaoMaster.DevOpenHelper(mContext, dbName, null);
        }
        SQLiteDatabase db = openHelper.getWritableDatabase();
        return db;
    }

    /**
     * 更新插入净水器属性
     *
     * @param attr
     */
    public void updateWaterAttr(WaterPurifierAttr attr) {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        WaterPurifierAttrDao waterDao = daoSession.getWaterPurifierAttrDao();
        waterDao.insertOrReplace(attr);
//        QueryBuilder<WaterPurifierAttr> qb = waterDao.queryBuilder();
//        if (qb.where(WaterPurifierAttrDao.Properties.Mac.eq(attr.getMac())).count() > 0) {
//            waterDao.update(attr);
//        } else {
//            waterDao.insert(attr);
//        }
    }

    /**
     * 删除净水器属性记录
     *
     * @param attr
     */
    public void deleteWaterAttr(WaterPurifierAttr attr) {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        WaterPurifierAttrDao tapDao = daoSession.getWaterPurifierAttrDao();
        QueryBuilder<WaterPurifierAttr> qurResQb = tapDao.queryBuilder().where(WaterPurifierAttrDao.Properties.Mac.eq(attr.getMac()));
        if (qurResQb.count() > 0) {
            tapDao.delete(qurResQb.unique());
        }
    }


    /**
     * 获取净水器属性记录
     *
     * @param mac
     *
     * @return
     */
    public WaterPurifierAttr getWaterAttr(String mac) {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        WaterPurifierAttrDao tapDao = daoSession.getWaterPurifierAttrDao();
        QueryBuilder<WaterPurifierAttr> qb = tapDao.queryBuilder();
        return qb.where(WaterPurifierAttrDao.Properties.Mac.eq(mac)).unique();
    }


    /**
     * 更新插入用户信息
     *
     * @param attr
     */
    public void updateUserInfo(UserInfo attr) {
        if (attr != null) {
            DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
            DaoSession daoSession = daoMaster.newSession();
            UserInfoDao userInfoDao = daoSession.getUserInfoDao();
//            QueryBuilder<UserInfo> qb = userInfoDao.queryBuilder();
            userInfoDao.insertOrReplace(attr);
//            if (qb.where(UserInfoDao.Properties.UserId.eq(attr.getUserId())).count() > 0) {
//                userInfoDao.update(attr);
//            } else {
//                userInfoDao.insert(attr);
//            }
        }
    }

    /**
     * 删除净水器属性记录
     *
     * @param userInfo
     */
    public void deleteUserInfo(UserInfo userInfo) {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        UserInfoDao userInfoDao = daoSession.getUserInfoDao();
        QueryBuilder<UserInfo> qurResQb = userInfoDao.queryBuilder().where(UserInfoDao.Properties.UserId.eq(userInfo.getUserId()));
        if (qurResQb.count() > 0) {
            userInfoDao.delete(qurResQb.unique());
        }
    }

    /**
     * 获取净水器属性记录
     *
     * @param userid
     *
     * @return
     */
    public UserInfo getUserInfo(String userid) {
        if (userid != null && !userid.isEmpty()) {
            DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
            DaoSession daoSession = daoMaster.newSession();
            UserInfoDao tapDao = daoSession.getUserInfoDao();
            QueryBuilder<UserInfo> qb = tapDao.queryBuilder();
            return qb.where(UserInfoDao.Properties.UserId.eq(userid)).unique();
        } else {
            return null;
        }
    }

    /**
     * 获取消息记录
     *
     * @param userid
     * @param startTime
     * @param endTime
     *
     * @return
     */
    public List<EMMessage> getChatMessageList(String userid, long startTime, long endTime) {
        try {
            DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
            DaoSession daoSession = daoMaster.newSession();
            EMMessageDao msgDao = daoSession.getEMMessageDao();
            QueryBuilder<EMMessage> qb = msgDao.queryBuilder();
            return qb.where(qb.and(EMMessageDao.Properties.Userid.eq(userid)
                    , EMMessageDao.Properties.Time.gt(startTime)
                    , EMMessageDao.Properties.Time.lt(endTime)))
                    .orderAsc(EMMessageDao.Properties.Time)
                    .list();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 获取全部消息记录
     *
     * @param userid
     *
     * @return
     */
    public List<EMMessage> getAllChatMessage(String userid) {
        try {
            DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
            DaoSession daoSession = daoMaster.newSession();
            EMMessageDao msgDao = daoSession.getEMMessageDao();
            QueryBuilder<EMMessage> qb = msgDao.queryBuilder();
            return qb.where(EMMessageDao.Properties.Userid.eq(userid))
                    .orderAsc(EMMessageDao.Properties.Time)
                    .list();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 获取指定消息
     *
     * @param userid
     * @param msgTime
     *
     * @return
     */
    public EMMessage getChatMessage(String userid, long msgTime) {
        try {
            DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
            DaoSession daoSession = daoMaster.newSession();
            EMMessageDao msgDao = daoSession.getEMMessageDao();
            QueryBuilder<EMMessage> qb = msgDao.queryBuilder();
            return qb.where(qb.and(EMMessageDao.Properties.Userid.eq(userid), EMMessageDao.Properties.Time.eq(msgTime))).unique();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 更新插入消息
     *
     * @param msg
     */
    public void updateEMMessage(EMMessage msg) {
        try {
            if (msg != null) {
                DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
                DaoSession daoSession = daoMaster.newSession();
                EMMessageDao msgDao = daoSession.getEMMessageDao();
                QueryBuilder<EMMessage> qb = msgDao.queryBuilder();
                msgDao.insertOrReplace(msg);
//                if (qb.where(qb.and(EMMessageDao.Properties.Userid.eq(msg.getUserid()), EMMessageDao.Properties.Time.eq(msg.getTime()))).count() > 0) {
//                    msgDao.update(msg);
//                } else {
//                    msgDao.insert(msg);
//                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "updateEMMessage_Ex: " + ex.getMessage());
        }
    }

    /**
     * 删除信息
     *
     * @param msg
     */
    public void deleteEMMessage(EMMessage msg) {
        try {
            DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
            DaoSession daoSession = daoMaster.newSession();
            EMMessageDao msgDao = daoSession.getEMMessageDao();
            QueryBuilder<EMMessage> qurResQb = msgDao.queryBuilder();
            qurResQb = qurResQb.where(qurResQb.and(EMMessageDao.Properties.Userid.eq(msg.getUserid()), EMMessageDao.Properties.Time.eq(msg.getTime())));
            if (qurResQb.count() > 0) {
                msgDao.delete(qurResQb.unique());
            }
        } catch (Exception ex) {
            Log.e(TAG, "deleteEMMessage_Ex: " + ex.getMessage());
        }
    }

    /**
     * 清空消息记录
     *
     * @param userid
     */
    public void clearEMMessage(String userid) {
        try {
            DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
            DaoSession daoSession = daoMaster.newSession();
            final EMMessageDao msgDao = daoSession.getEMMessageDao();
//            QueryBuilder<EMMessage> qurResQb = msgDao.queryBuilder().where(UserInfoDao.Properties.UserId.eq(userid)).list();
            List<EMMessage> msgList = msgDao.queryBuilder().where(UserInfoDao.Properties.UserId.eq(userid)).list();
            msgDao.deleteInTx(msgList);
//            if (qurResQb.count() > 0) {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        msgDao.deleteAll();
//                    }
//                }).start();
//            }
        } catch (Exception ex) {
            Log.e(TAG, "clearEMMessage_Ex: " + ex.getMessage());
        }
    }


    /**
     * 插入未读排名列表
     *
     * @param rankList
     */
    public void insertFriendRank(final List<FriendRankItem> rankList, final DBRxListener listener) {
        try {
            if (rankList == null || rankList.isEmpty()) {
                return;
            }

            DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
            DaoSession daoSession = daoMaster.newSession();
            final FriendRankItemDao rankDao = daoSession.getFriendRankItemDao();
            rankDao.getSession().runInTx(new Runnable() {
                @Override
                public void run() {
                    for (FriendRankItem item : rankList) {
                        rankDao.insertOrReplace(item);
                    }
                    if (listener != null) {
                        listener.onSuccess();
                    }
                }
            });
        } catch (Exception ex) {
            Log.e(TAG, "updateFriendRank_Ex: " + ex.getMessage());
            if (listener != null) {
                listener.onFail();
            }
        }
    }

    /**
     * 获取排名列表
     *
     * @param userid
     *
     * @return
     */
    public List<FriendRankItem> getFriendRankList(String userid) {
        if (userid == null || userid.isEmpty()) {
            return new ArrayList<>();
        }
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        FriendRankItemDao rankDao = daoSession.getFriendRankItemDao();
        return rankDao.queryBuilder().where(FriendRankItemDao.Properties.Userid.eq(userid))
                .orderDesc(FriendRankItemDao.Properties.Notime).list();
    }

    /**
     * 删除指定排名数据
     *
     * @param item
     */
    public void deleteFriendRank(FriendRankItem item) {
        if (item == null) {
            return;
        }
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        FriendRankItemDao rankDao = daoSession.getFriendRankItemDao();
        rankDao.delete(item);
    }

    /**
     * 保存设备设置
     *
     * @param settings
     */
    public void updateDeviceSettings(OznerDeviceSettings settings) {
        if (settings == null) {
            return;
        }

        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        OznerDeviceSettingsDao msgDao = daoSession.getOznerDeviceSettingsDao();
        QueryBuilder<OznerDeviceSettings> qb = msgDao.queryBuilder();
        msgDao.insertOrReplace(settings);
    }

    public List<OznerDeviceSettings> getDeviceSettingList(String userid) {
        try {
            DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
            DaoSession daoSession = daoMaster.newSession();
            OznerDeviceSettingsDao settingsDao = daoSession.getOznerDeviceSettingsDao();
            QueryBuilder<OznerDeviceSettings> qb = settingsDao.queryBuilder();
            return qb.where(OznerDeviceSettingsDao.Properties.UserId.eq(userid)).orderAsc(OznerDeviceSettingsDao.Properties.CreateTime).list();
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    /**
     * 获取指定设备的设置
     *
     * @param userid
     * @param mac
     *
     * @return
     */
    public OznerDeviceSettings getDeviceSettings(String userid, String mac) {
        try {
            DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
            DaoSession daoSession = daoMaster.newSession();
            OznerDeviceSettingsDao settingsDao = daoSession.getOznerDeviceSettingsDao();
            QueryBuilder<OznerDeviceSettings> qb = settingsDao.queryBuilder();
            return qb.where(qb.and(OznerDeviceSettingsDao.Properties.UserId.eq(userid),
                    OznerDeviceSettingsDao.Properties.Mac.eq(mac))).uniqueOrThrow();
        } catch (Exception ex) {
            ex.printStackTrace();
            LCLogUtils.E(TAG, "getDeviceSettings_Ex:" + ex.getMessage());
            return null;
        }
    }

    /**
     * 删除设备设置
     *
     * @param userid
     * @param mac
     */
    public void deleteDeviceSettings(String userid, String mac) {
        try {
            DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
            DaoSession daoSession = daoMaster.newSession();
            OznerDeviceSettingsDao settingsDao = daoSession.getOznerDeviceSettingsDao();
            QueryBuilder<OznerDeviceSettings> qb = settingsDao.queryBuilder();
            OznerDeviceSettings settings = qb.where(qb.and(OznerDeviceSettingsDao.Properties.UserId.eq(userid),
                    OznerDeviceSettingsDao.Properties.Mac.eq(mac))).uniqueOrThrow();
            if (settings != null) {
                settingsDao.delete(settings);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            LCLogUtils.E(TAG, "deleteDeviceSettings_Ex:" + ex.getMessage());
        }
    }

}
