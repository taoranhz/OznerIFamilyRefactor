package com.ozner.yiquan.Device.ReplenWater.RemindUtils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.ozner.yiquan.R;
import com.ozner.yiquan.Utils.LCLogUtils;

import java.util.Calendar;

/**
 * Created by ozner_67 on 2017/2/8.
 * 邮箱：xinde.zhang@cftcn.com
 * <p>
 * 提醒闹钟服务接收器
 */

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        long interval = intent.getLongExtra(RemindUtil.INTERVAL_MILLIS, 0);
        int reqCode = intent.getIntExtra(RemindUtil.PARMS_REQ_CODE, 1);
        LCLogUtils.E(TAG, "onReceive:提醒服务执行 interval:" + interval + " , action:" + intent.getAction());
        LCLogUtils.E(TAG, "onReceive: action equal: " + intent.getAction().equals(RemindUtil.ALARM_ACTION));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MILLISECOND, 0);
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            PendingIntent sender = PendingIntent.getBroadcast(context, reqCode,
                    intent, PendingIntent.FLAG_CANCEL_CURRENT);
            am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis() + interval, sender);
        }
        showNotify(context, reqCode);
    }

    private void showNotify(Context context, int reqCode) {
        Notification.Builder builder = new Notification.Builder(context);
        Intent intent = new Intent(context, NotifyActivity.class);
        intent.putExtra(RemindUtil.PARMS_REQ_CODE, reqCode);
        PendingIntent pi = PendingIntent.getActivity(context, reqCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        builder.setSmallIcon(R.mipmap.ozner)
                .setAutoCancel(true)
                .setContentText(context.getString(R.string.time_to_moisturizing))//消息内容
                .setContentIntent(pi)
                .setContentTitle(context.getString(R.string.replen_remind))
                .setTicker(context.getString(R.string.time_to_moisturizing))
                .setDefaults(Notification.DEFAULT_ALL);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(reqCode, builder.build());
    }
}
