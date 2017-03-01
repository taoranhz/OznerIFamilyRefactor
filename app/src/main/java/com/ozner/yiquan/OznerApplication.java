package com.ozner.yiquan;

import android.content.Intent;

import com.ozner.yiquan.Bean.OznerBroadcastAction;
import com.ozner.yiquan.Command.OznerPreference;
import com.ozner.yiquan.Command.UserDataPreference;
import com.ozner.yiquan.Utils.LCLogUtils;

/**
 * Created by ozner_67 on 2016/11/1.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class OznerApplication extends OznerBaseApplication {
    @Override
    public void onCreate() {
        LCLogUtils.init(getApplicationContext());
        super.onCreate();
    }

    @Override
    protected void onBindService() {
        String userid = UserDataPreference.GetUserData(getBaseContext(), UserDataPreference.UserId, "Ozner");
        String usertoken = OznerPreference.getUserToken(getBaseContext());
        getService().getDeviceManager().setOwner(userid, usertoken);
        this.sendBroadcast(new Intent(OznerBroadcastAction.OBA_Service_Init));
    }
}
