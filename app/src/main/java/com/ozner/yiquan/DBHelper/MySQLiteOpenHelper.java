package com.ozner.yiquan.DBHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.greenrobot.greendao.database.Database;

/**
 * Created by ozner_67 on 2017/5/5.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class MySQLiteOpenHelper extends DaoMaster.OpenHelper {

    public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
//        super.onUpgrade(db, oldVersion, newVersion);
        Log.e("MySQLiteOpenHelper", "Upgrading schema from version " + oldVersion + " to " + newVersion);
        MigrationHelper.getInstance().migrate(db,
                EMMessageDao.class,
                FriendRankItemDao.class,
                OznerDeviceSettingsDao.class,
                UserInfoDao.class,
                WaterPurifierAttrDao.class);
    }
}
