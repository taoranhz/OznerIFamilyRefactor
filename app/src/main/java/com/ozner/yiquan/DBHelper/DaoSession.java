package com.ozner.yiquan.DBHelper;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.ozner.yiquan.DBHelper.EMMessage;
import com.ozner.yiquan.DBHelper.FriendRankItem;
import com.ozner.yiquan.DBHelper.OznerDeviceSettings;
import com.ozner.yiquan.DBHelper.UserInfo;
import com.ozner.yiquan.DBHelper.WaterPurifierAttr;

import com.ozner.yiquan.DBHelper.EMMessageDao;
import com.ozner.yiquan.DBHelper.FriendRankItemDao;
import com.ozner.yiquan.DBHelper.OznerDeviceSettingsDao;
import com.ozner.yiquan.DBHelper.UserInfoDao;
import com.ozner.yiquan.DBHelper.WaterPurifierAttrDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig eMMessageDaoConfig;
    private final DaoConfig friendRankItemDaoConfig;
    private final DaoConfig oznerDeviceSettingsDaoConfig;
    private final DaoConfig userInfoDaoConfig;
    private final DaoConfig waterPurifierAttrDaoConfig;

    private final EMMessageDao eMMessageDao;
    private final FriendRankItemDao friendRankItemDao;
    private final OznerDeviceSettingsDao oznerDeviceSettingsDao;
    private final UserInfoDao userInfoDao;
    private final WaterPurifierAttrDao waterPurifierAttrDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        eMMessageDaoConfig = daoConfigMap.get(EMMessageDao.class).clone();
        eMMessageDaoConfig.initIdentityScope(type);

        friendRankItemDaoConfig = daoConfigMap.get(FriendRankItemDao.class).clone();
        friendRankItemDaoConfig.initIdentityScope(type);

        oznerDeviceSettingsDaoConfig = daoConfigMap.get(OznerDeviceSettingsDao.class).clone();
        oznerDeviceSettingsDaoConfig.initIdentityScope(type);

        userInfoDaoConfig = daoConfigMap.get(UserInfoDao.class).clone();
        userInfoDaoConfig.initIdentityScope(type);

        waterPurifierAttrDaoConfig = daoConfigMap.get(WaterPurifierAttrDao.class).clone();
        waterPurifierAttrDaoConfig.initIdentityScope(type);

        eMMessageDao = new EMMessageDao(eMMessageDaoConfig, this);
        friendRankItemDao = new FriendRankItemDao(friendRankItemDaoConfig, this);
        oznerDeviceSettingsDao = new OznerDeviceSettingsDao(oznerDeviceSettingsDaoConfig, this);
        userInfoDao = new UserInfoDao(userInfoDaoConfig, this);
        waterPurifierAttrDao = new WaterPurifierAttrDao(waterPurifierAttrDaoConfig, this);

        registerDao(EMMessage.class, eMMessageDao);
        registerDao(FriendRankItem.class, friendRankItemDao);
        registerDao(OznerDeviceSettings.class, oznerDeviceSettingsDao);
        registerDao(UserInfo.class, userInfoDao);
        registerDao(WaterPurifierAttr.class, waterPurifierAttrDao);
    }
    
    public void clear() {
        eMMessageDaoConfig.clearIdentityScope();
        friendRankItemDaoConfig.clearIdentityScope();
        oznerDeviceSettingsDaoConfig.clearIdentityScope();
        userInfoDaoConfig.clearIdentityScope();
        waterPurifierAttrDaoConfig.clearIdentityScope();
    }

    public EMMessageDao getEMMessageDao() {
        return eMMessageDao;
    }

    public FriendRankItemDao getFriendRankItemDao() {
        return friendRankItemDao;
    }

    public OznerDeviceSettingsDao getOznerDeviceSettingsDao() {
        return oznerDeviceSettingsDao;
    }

    public UserInfoDao getUserInfoDao() {
        return userInfoDao;
    }

    public WaterPurifierAttrDao getWaterPurifierAttrDao() {
        return waterPurifierAttrDao;
    }

}