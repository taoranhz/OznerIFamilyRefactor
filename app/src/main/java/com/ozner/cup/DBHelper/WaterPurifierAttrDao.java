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
 * DAO for table "WATER_PURIFIER_ATTR".
*/
public class WaterPurifierAttrDao extends AbstractDao<WaterPurifierAttr, String> {

    public static final String TABLENAME = "WATER_PURIFIER_ATTR";

    /**
     * Properties of entity WaterPurifierAttr.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Mac = new Property(0, String.class, "Mac", true, "MAC");
        public final static Property DeviceType = new Property(1, String.class, "DeviceType", false, "DEVICE_TYPE");
        public final static Property Attr = new Property(2, String.class, "Attr", false, "ATTR");
        public final static Property Disabled = new Property(3, boolean.class, "Disabled", false, "DISABLED");
        public final static Property CreateTime = new Property(4, long.class, "CreateTime", false, "CREATE_TIME");
        public final static Property ModifyTime = new Property(5, long.class, "ModifyTime", false, "MODIFY_TIME");
        public final static Property IsShowDueDay = new Property(6, boolean.class, "IsShowDueDay", false, "IS_SHOW_DUE_DAY");
        public final static Property Smlinkurl = new Property(7, String.class, "smlinkurl", false, "SMLINKURL");
        public final static Property Buylinkurl = new Property(8, String.class, "buylinkurl", false, "BUYLINKURL");
        public final static Property Tips = new Property(9, String.class, "tips", false, "TIPS");
        public final static Property Days = new Property(10, int.class, "days", false, "DAYS");
        public final static Property Boolshow = new Property(11, boolean.class, "boolshow", false, "BOOLSHOW");
        public final static Property HasCool = new Property(12, boolean.class, "hasCool", false, "HAS_COOL");
        public final static Property HasHot = new Property(13, boolean.class, "hasHot", false, "HAS_HOT");
        public final static Property FilterTime = new Property(14, long.class, "filterTime", false, "FILTER_TIME");
        public final static Property FilterNowtime = new Property(15, long.class, "filterNowtime", false, "FILTER_NOWTIME");
    }


    public WaterPurifierAttrDao(DaoConfig config) {
        super(config);
    }
    
    public WaterPurifierAttrDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"WATER_PURIFIER_ATTR\" (" + //
                "\"MAC\" TEXT PRIMARY KEY NOT NULL ," + // 0: Mac
                "\"DEVICE_TYPE\" TEXT," + // 1: DeviceType
                "\"ATTR\" TEXT," + // 2: Attr
                "\"DISABLED\" INTEGER NOT NULL ," + // 3: Disabled
                "\"CREATE_TIME\" INTEGER NOT NULL ," + // 4: CreateTime
                "\"MODIFY_TIME\" INTEGER NOT NULL ," + // 5: ModifyTime
                "\"IS_SHOW_DUE_DAY\" INTEGER NOT NULL ," + // 6: IsShowDueDay
                "\"SMLINKURL\" TEXT," + // 7: smlinkurl
                "\"BUYLINKURL\" TEXT," + // 8: buylinkurl
                "\"TIPS\" TEXT," + // 9: tips
                "\"DAYS\" INTEGER NOT NULL ," + // 10: days
                "\"BOOLSHOW\" INTEGER NOT NULL ," + // 11: boolshow
                "\"HAS_COOL\" INTEGER NOT NULL ," + // 12: hasCool
                "\"HAS_HOT\" INTEGER NOT NULL ," + // 13: hasHot
                "\"FILTER_TIME\" INTEGER NOT NULL ," + // 14: filterTime
                "\"FILTER_NOWTIME\" INTEGER NOT NULL );"); // 15: filterNowtime
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"WATER_PURIFIER_ATTR\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, WaterPurifierAttr entity) {
        stmt.clearBindings();
 
        String Mac = entity.getMac();
        if (Mac != null) {
            stmt.bindString(1, Mac);
        }
 
        String DeviceType = entity.getDeviceType();
        if (DeviceType != null) {
            stmt.bindString(2, DeviceType);
        }
 
        String Attr = entity.getAttr();
        if (Attr != null) {
            stmt.bindString(3, Attr);
        }
        stmt.bindLong(4, entity.getDisabled() ? 1L: 0L);
        stmt.bindLong(5, entity.getCreateTime());
        stmt.bindLong(6, entity.getModifyTime());
        stmt.bindLong(7, entity.getIsShowDueDay() ? 1L: 0L);
 
        String smlinkurl = entity.getSmlinkurl();
        if (smlinkurl != null) {
            stmt.bindString(8, smlinkurl);
        }
 
        String buylinkurl = entity.getBuylinkurl();
        if (buylinkurl != null) {
            stmt.bindString(9, buylinkurl);
        }
 
        String tips = entity.getTips();
        if (tips != null) {
            stmt.bindString(10, tips);
        }
        stmt.bindLong(11, entity.getDays());
        stmt.bindLong(12, entity.getBoolshow() ? 1L: 0L);
        stmt.bindLong(13, entity.getHasCool() ? 1L: 0L);
        stmt.bindLong(14, entity.getHasHot() ? 1L: 0L);
        stmt.bindLong(15, entity.getFilterTime());
        stmt.bindLong(16, entity.getFilterNowtime());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, WaterPurifierAttr entity) {
        stmt.clearBindings();
 
        String Mac = entity.getMac();
        if (Mac != null) {
            stmt.bindString(1, Mac);
        }
 
        String DeviceType = entity.getDeviceType();
        if (DeviceType != null) {
            stmt.bindString(2, DeviceType);
        }
 
        String Attr = entity.getAttr();
        if (Attr != null) {
            stmt.bindString(3, Attr);
        }
        stmt.bindLong(4, entity.getDisabled() ? 1L: 0L);
        stmt.bindLong(5, entity.getCreateTime());
        stmt.bindLong(6, entity.getModifyTime());
        stmt.bindLong(7, entity.getIsShowDueDay() ? 1L: 0L);
 
        String smlinkurl = entity.getSmlinkurl();
        if (smlinkurl != null) {
            stmt.bindString(8, smlinkurl);
        }
 
        String buylinkurl = entity.getBuylinkurl();
        if (buylinkurl != null) {
            stmt.bindString(9, buylinkurl);
        }
 
        String tips = entity.getTips();
        if (tips != null) {
            stmt.bindString(10, tips);
        }
        stmt.bindLong(11, entity.getDays());
        stmt.bindLong(12, entity.getBoolshow() ? 1L: 0L);
        stmt.bindLong(13, entity.getHasCool() ? 1L: 0L);
        stmt.bindLong(14, entity.getHasHot() ? 1L: 0L);
        stmt.bindLong(15, entity.getFilterTime());
        stmt.bindLong(16, entity.getFilterNowtime());
    }

    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }    

    @Override
    public WaterPurifierAttr readEntity(Cursor cursor, int offset) {
        WaterPurifierAttr entity = new WaterPurifierAttr( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // Mac
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // DeviceType
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // Attr
            cursor.getShort(offset + 3) != 0, // Disabled
            cursor.getLong(offset + 4), // CreateTime
            cursor.getLong(offset + 5), // ModifyTime
            cursor.getShort(offset + 6) != 0, // IsShowDueDay
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // smlinkurl
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // buylinkurl
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // tips
            cursor.getInt(offset + 10), // days
            cursor.getShort(offset + 11) != 0, // boolshow
            cursor.getShort(offset + 12) != 0, // hasCool
            cursor.getShort(offset + 13) != 0, // hasHot
            cursor.getLong(offset + 14), // filterTime
            cursor.getLong(offset + 15) // filterNowtime
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, WaterPurifierAttr entity, int offset) {
        entity.setMac(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setDeviceType(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setAttr(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setDisabled(cursor.getShort(offset + 3) != 0);
        entity.setCreateTime(cursor.getLong(offset + 4));
        entity.setModifyTime(cursor.getLong(offset + 5));
        entity.setIsShowDueDay(cursor.getShort(offset + 6) != 0);
        entity.setSmlinkurl(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setBuylinkurl(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setTips(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setDays(cursor.getInt(offset + 10));
        entity.setBoolshow(cursor.getShort(offset + 11) != 0);
        entity.setHasCool(cursor.getShort(offset + 12) != 0);
        entity.setHasHot(cursor.getShort(offset + 13) != 0);
        entity.setFilterTime(cursor.getLong(offset + 14));
        entity.setFilterNowtime(cursor.getLong(offset + 15));
     }
    
    @Override
    protected final String updateKeyAfterInsert(WaterPurifierAttr entity, long rowId) {
        return entity.getMac();
    }
    
    @Override
    public String getKey(WaterPurifierAttr entity) {
        if(entity != null) {
            return entity.getMac();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(WaterPurifierAttr entity) {
        return entity.getMac() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
