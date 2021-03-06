package com.ozner.cup.DBHelper;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "USER_INFO".
*/
public class UserInfoDao extends AbstractDao<UserInfo, String> {

    public static final String TABLENAME = "USER_INFO";

    /**
     * Properties of entity UserInfo.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property UserId = new Property(0, String.class, "userId", true, "USER_ID");
        public final static Property Mobile = new Property(1, String.class, "mobile", false, "MOBILE");
        public final static Property Nickname = new Property(2, String.class, "nickname", false, "NICKNAME");
        public final static Property Headimg = new Property(3, String.class, "headimg", false, "HEADIMG");
        public final static Property GradeName = new Property(4, String.class, "GradeName", false, "GRADE_NAME");
        public final static Property Score = new Property(5, String.class, "Score", false, "SCORE");
        public final static Property Status = new Property(6, int.class, "Status", false, "STATUS");
        public final static Property DeviceId = new Property(7, String.class, "deviceId", false, "DEVICE_ID");
        public final static Property ChannelId = new Property(8, String.class, "channelId", false, "CHANNEL_ID");
        public final static Property Sex = new Property(9, String.class, "sex", false, "SEX");
        public final static Property Email = new Property(10, String.class, "email", false, "EMAIL");
    }


    public UserInfoDao(DaoConfig config) {
        super(config);
    }
    
    public UserInfoDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"USER_INFO\" (" + //
                "\"USER_ID\" TEXT PRIMARY KEY NOT NULL ," + // 0: userId
                "\"MOBILE\" TEXT," + // 1: mobile
                "\"NICKNAME\" TEXT," + // 2: nickname
                "\"HEADIMG\" TEXT," + // 3: headimg
                "\"GRADE_NAME\" TEXT," + // 4: GradeName
                "\"SCORE\" TEXT," + // 5: Score
                "\"STATUS\" INTEGER NOT NULL ," + // 6: Status
                "\"DEVICE_ID\" TEXT," + // 7: deviceId
                "\"CHANNEL_ID\" TEXT," + // 8: channelId
                "\"SEX\" TEXT," + // 9: sex
                "\"EMAIL\" TEXT);"); // 10: email
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"USER_INFO\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, UserInfo entity) {
        stmt.clearBindings();
 
        String userId = entity.getUserId();
        if (userId != null) {
            stmt.bindString(1, userId);
        }
 
        String mobile = entity.getMobile();
        if (mobile != null) {
            stmt.bindString(2, mobile);
        }
 
        String nickname = entity.getNickname();
        if (nickname != null) {
            stmt.bindString(3, nickname);
        }
 
        String headimg = entity.getHeadimg();
        if (headimg != null) {
            stmt.bindString(4, headimg);
        }
 
        String GradeName = entity.getGradeName();
        if (GradeName != null) {
            stmt.bindString(5, GradeName);
        }
 
        String Score = entity.getScore();
        if (Score != null) {
            stmt.bindString(6, Score);
        }
        stmt.bindLong(7, entity.getStatus());
 
        String deviceId = entity.getDeviceId();
        if (deviceId != null) {
            stmt.bindString(8, deviceId);
        }
 
        String channelId = entity.getChannelId();
        if (channelId != null) {
            stmt.bindString(9, channelId);
        }
 
        String sex = entity.getSex();
        if (sex != null) {
            stmt.bindString(10, sex);
        }
 
        String email = entity.getEmail();
        if (email != null) {
            stmt.bindString(11, email);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, UserInfo entity) {
        stmt.clearBindings();
 
        String userId = entity.getUserId();
        if (userId != null) {
            stmt.bindString(1, userId);
        }
 
        String mobile = entity.getMobile();
        if (mobile != null) {
            stmt.bindString(2, mobile);
        }
 
        String nickname = entity.getNickname();
        if (nickname != null) {
            stmt.bindString(3, nickname);
        }
 
        String headimg = entity.getHeadimg();
        if (headimg != null) {
            stmt.bindString(4, headimg);
        }
 
        String GradeName = entity.getGradeName();
        if (GradeName != null) {
            stmt.bindString(5, GradeName);
        }
 
        String Score = entity.getScore();
        if (Score != null) {
            stmt.bindString(6, Score);
        }
        stmt.bindLong(7, entity.getStatus());
 
        String deviceId = entity.getDeviceId();
        if (deviceId != null) {
            stmt.bindString(8, deviceId);
        }
 
        String channelId = entity.getChannelId();
        if (channelId != null) {
            stmt.bindString(9, channelId);
        }
 
        String sex = entity.getSex();
        if (sex != null) {
            stmt.bindString(10, sex);
        }
 
        String email = entity.getEmail();
        if (email != null) {
            stmt.bindString(11, email);
        }
    }

    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }    

    @Override
    public UserInfo readEntity(Cursor cursor, int offset) {
        UserInfo entity = new UserInfo( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // userId
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // mobile
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // nickname
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // headimg
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // GradeName
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // Score
            cursor.getInt(offset + 6), // Status
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // deviceId
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // channelId
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // sex
            cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10) // email
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, UserInfo entity, int offset) {
        entity.setUserId(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setMobile(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setNickname(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setHeadimg(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setGradeName(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setScore(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setStatus(cursor.getInt(offset + 6));
        entity.setDeviceId(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setChannelId(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setSex(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setEmail(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
     }
    
    @Override
    protected final String updateKeyAfterInsert(UserInfo entity, long rowId) {
        return entity.getUserId();
    }
    
    @Override
    public String getKey(UserInfo entity) {
        if(entity != null) {
            return entity.getUserId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(UserInfo entity) {
        return entity.getUserId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
