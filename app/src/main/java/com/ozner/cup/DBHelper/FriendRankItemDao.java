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
 * DAO for table "FRIEND_RANK_ITEM".
*/
public class FriendRankItemDao extends AbstractDao<FriendRankItem, Long> {

    public static final String TABLENAME = "FRIEND_RANK_ITEM";

    /**
     * Properties of entity FriendRankItem.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property AddId = new Property(0, long.class, "addId", true, "_id");
        public final static Property Id = new Property(1, String.class, "id", false, "ID");
        public final static Property Rank = new Property(2, String.class, "rank", false, "RANK");
        public final static Property Max = new Property(3, String.class, "max", false, "MAX");
        public final static Property Likenumaber = new Property(4, String.class, "likenumaber", false, "LIKENUMABER");
        public final static Property Userid = new Property(5, String.class, "userid", false, "USERID");
        public final static Property Vuserid = new Property(6, String.class, "vuserid", false, "VUSERID");
        public final static Property Type = new Property(7, String.class, "type", false, "TYPE");
        public final static Property Notify = new Property(8, String.class, "notify", false, "NOTIFY");
        public final static Property Notime = new Property(9, String.class, "notime", false, "NOTIME");
        public final static Property Nickname = new Property(10, String.class, "Nickname", false, "NICKNAME");
        public final static Property Icon = new Property(11, String.class, "Icon", false, "ICON");
        public final static Property Score = new Property(12, String.class, "Score", false, "SCORE");
    }


    public FriendRankItemDao(DaoConfig config) {
        super(config);
    }
    
    public FriendRankItemDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"FRIEND_RANK_ITEM\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ," + // 0: addId
                "\"ID\" TEXT," + // 1: id
                "\"RANK\" TEXT," + // 2: rank
                "\"MAX\" TEXT," + // 3: max
                "\"LIKENUMABER\" TEXT," + // 4: likenumaber
                "\"USERID\" TEXT," + // 5: userid
                "\"VUSERID\" TEXT," + // 6: vuserid
                "\"TYPE\" TEXT," + // 7: type
                "\"NOTIFY\" TEXT," + // 8: notify
                "\"NOTIME\" TEXT," + // 9: notime
                "\"NICKNAME\" TEXT," + // 10: Nickname
                "\"ICON\" TEXT," + // 11: Icon
                "\"SCORE\" TEXT);"); // 12: Score
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"FRIEND_RANK_ITEM\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, FriendRankItem entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getAddId());
 
        String id = entity.getId();
        if (id != null) {
            stmt.bindString(2, id);
        }
 
        String rank = entity.getRank();
        if (rank != null) {
            stmt.bindString(3, rank);
        }
 
        String max = entity.getMax();
        if (max != null) {
            stmt.bindString(4, max);
        }
 
        String likenumaber = entity.getLikenumaber();
        if (likenumaber != null) {
            stmt.bindString(5, likenumaber);
        }
 
        String userid = entity.getUserid();
        if (userid != null) {
            stmt.bindString(6, userid);
        }
 
        String vuserid = entity.getVuserid();
        if (vuserid != null) {
            stmt.bindString(7, vuserid);
        }
 
        String type = entity.getType();
        if (type != null) {
            stmt.bindString(8, type);
        }
 
        String notify = entity.getNotify();
        if (notify != null) {
            stmt.bindString(9, notify);
        }
 
        String notime = entity.getNotime();
        if (notime != null) {
            stmt.bindString(10, notime);
        }
 
        String Nickname = entity.getNickname();
        if (Nickname != null) {
            stmt.bindString(11, Nickname);
        }
 
        String Icon = entity.getIcon();
        if (Icon != null) {
            stmt.bindString(12, Icon);
        }
 
        String Score = entity.getScore();
        if (Score != null) {
            stmt.bindString(13, Score);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, FriendRankItem entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getAddId());
 
        String id = entity.getId();
        if (id != null) {
            stmt.bindString(2, id);
        }
 
        String rank = entity.getRank();
        if (rank != null) {
            stmt.bindString(3, rank);
        }
 
        String max = entity.getMax();
        if (max != null) {
            stmt.bindString(4, max);
        }
 
        String likenumaber = entity.getLikenumaber();
        if (likenumaber != null) {
            stmt.bindString(5, likenumaber);
        }
 
        String userid = entity.getUserid();
        if (userid != null) {
            stmt.bindString(6, userid);
        }
 
        String vuserid = entity.getVuserid();
        if (vuserid != null) {
            stmt.bindString(7, vuserid);
        }
 
        String type = entity.getType();
        if (type != null) {
            stmt.bindString(8, type);
        }
 
        String notify = entity.getNotify();
        if (notify != null) {
            stmt.bindString(9, notify);
        }
 
        String notime = entity.getNotime();
        if (notime != null) {
            stmt.bindString(10, notime);
        }
 
        String Nickname = entity.getNickname();
        if (Nickname != null) {
            stmt.bindString(11, Nickname);
        }
 
        String Icon = entity.getIcon();
        if (Icon != null) {
            stmt.bindString(12, Icon);
        }
 
        String Score = entity.getScore();
        if (Score != null) {
            stmt.bindString(13, Score);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.getLong(offset + 0);
    }    

    @Override
    public FriendRankItem readEntity(Cursor cursor, int offset) {
        FriendRankItem entity = new FriendRankItem( //
            cursor.getLong(offset + 0), // addId
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // id
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // rank
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // max
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // likenumaber
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // userid
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // vuserid
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // type
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // notify
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // notime
            cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10), // Nickname
            cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11), // Icon
            cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12) // Score
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, FriendRankItem entity, int offset) {
        entity.setAddId(cursor.getLong(offset + 0));
        entity.setId(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setRank(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setMax(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setLikenumaber(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setUserid(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setVuserid(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setType(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setNotify(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setNotime(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setNickname(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
        entity.setIcon(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setScore(cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(FriendRankItem entity, long rowId) {
        entity.setAddId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(FriendRankItem entity) {
        if(entity != null) {
            return entity.getAddId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(FriendRankItem entity) {
        throw new UnsupportedOperationException("Unsupported for entities with a non-null key");
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
