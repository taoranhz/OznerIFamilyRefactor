package com.ozner.cup.DBHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.query.QueryBuilder;

/**
 * Created by ozner_67 on 2016/11/10.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class DBManager {
    private static final String dbName = "ozner_cup_db";
    private static DBManager mInstance;
    private DaoMaster.DevOpenHelper openHelper;
    private Context mContext;

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
        QueryBuilder<WaterPurifierAttr> qb = waterDao.queryBuilder();
        if (qb.where(WaterPurifierAttrDao.Properties.Mac.eq(attr.getMac())).count() > 0) {
            waterDao.update(attr);
        } else {
            waterDao.insert(attr);
        }
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


    public WaterPurifierAttr getWaterAttr(String mac) {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        WaterPurifierAttrDao tapDao = daoSession.getWaterPurifierAttrDao();
        QueryBuilder<WaterPurifierAttr> qb = tapDao.queryBuilder();
        return qb.where(WaterPurifierAttrDao.Properties.Mac.eq(mac)).unique();
    }
}
