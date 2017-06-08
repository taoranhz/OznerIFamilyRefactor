package com.ozner.cup;

import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;

import com.ozner.cup.Bean.OznerBroadcastAction;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Utils.LCLogUtils;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by ozner_67 on 2016/11/1.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class OznerApplication extends OznerBaseApplication {
    @Override
    public void onCreate() {
        LCLogUtils.init(getApplicationContext());
        CrashReport.initCrashReport(getApplicationContext(), "900033413", false);
        super.onCreate();
    }

    @Override
    protected void onBindService() {
        String userid = OznerPreference.GetValue(getBaseContext(), OznerPreference.UserId, "Ozner");
        String usertoken = OznerPreference.getUserToken(getBaseContext());
        getService().getDeviceManager().setOwner(userid, usertoken);
        this.sendBroadcast(new Intent(OznerBroadcastAction.OBA_Service_Init));
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
