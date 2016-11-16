package com.ozner.cup;

import android.content.Intent;

import com.ozner.cup.Bean.OznerBroadcastAction;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;

/**
 * Created by ozner_67 on 2016/11/1.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class OznerApplication extends OznerBaseApplication {
    @Override
    protected void onBindService() {
        String userid = UserDataPreference.GetUserData(getBaseContext(), UserDataPreference.UserId, "Ozner");
        String usertoken = OznerPreference.getUserToken(getBaseContext());
        getService().getDeviceManager().setOwner(userid, usertoken);
        this.sendBroadcast(new Intent(OznerBroadcastAction.OBA_Service_Init));
    }
}
