package com.ozner.yiquan;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

import com.ozner.yiquan.Bean.OznerBroadcastAction;
import com.ozner.yiquan.Command.OznerPreference;
import com.ozner.yiquan.Command.UserDataPreference;
import com.ozner.yiquan.Utils.LCLogUtils;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by ozner_67 on 2016/11/1.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class OznerApplication extends OznerBaseApplication {
    @Override
    public void onCreate() {
        LCLogUtils.init(getApplicationContext());
//        CrashReport.initCrashReport(getApplicationContext(), "注册时申请的APPID", false);
        CrashReport.initCrashReport(getApplicationContext(),"d2df1ab42b",false);
        super.onCreate();
    }

    @Override
    protected void onBindService() {
        String userid = UserDataPreference.GetUserData(getBaseContext(), UserDataPreference.UserId, "Ozner");
        String usertoken = OznerPreference.getUserToken(getBaseContext());
        getService().getDeviceManager().setOwner(userid, usertoken);
        this.sendBroadcast(new Intent(OznerBroadcastAction.OBA_Service_Init));
    }

    public static void callSeviceChat(Context context) {
        if (isSimCardReady(context)) {
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:4008209667"));
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            context.startActivity(callIntent);
        } else {
            android.app.AlertDialog tipDialog = new android.app.AlertDialog.Builder(context).setMessage(context.getString(R.string.Chat_SimCardTips))
                    .setPositiveButton(context.getString(R.string.ensure), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create();
            tipDialog.setCanceledOnTouchOutside(false);
            tipDialog.show();
        }
    }

    //检查SIM卡状态
    private static boolean isSimCardReady(Context context) {
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        if (TelephonyManager.SIM_STATE_READY == tm.getSimState()) {
            return true;
        } else {
            return false;
        }
    }
}
