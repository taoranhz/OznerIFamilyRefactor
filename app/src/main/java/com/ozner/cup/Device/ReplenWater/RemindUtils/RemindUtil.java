package com.ozner.cup.Device.ReplenWater.RemindUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Calendar;

/**
 * Created by ozner_67 on 2017/2/14.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class RemindUtil {
    private static final String TAG = "RemindUtil";
    public static final String INTERVAL_MILLIS = "interval_millis";
    public static final String PARMS_REQ_CODE = "parms_req_code";
    public static final String ALARM_ACTION = "ozner.alarm.action";
    public static final String ALARM_CATEGORY = "ozner.alarm.category";
    private WeakReference<Context> weakContext;
    private AlarmManager alarmManager;

    public RemindUtil(Context context) {
        Log.e(TAG, "RemindUtil: init");
        this.weakContext = new WeakReference<Context>(context);
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * 开始闹铃提醒
     *
     * @param reqCode
     * @param startCal
     * @param interval
     */
    public void startRemind(int reqCode, Calendar startCal, long interval) {
        Log.e(TAG, "startRemind: reqCode:" + reqCode+" ,interval:"+interval);
        try {
            Calendar curCal = Calendar.getInstance();
            startCal.set(Calendar.SECOND, 0);
            startCal.set(Calendar.MILLISECOND, 0);

            if (curCal.getTimeInMillis() > startCal.getTimeInMillis()) {
                startCal.set(Calendar.DAY_OF_MONTH, curCal.get(Calendar.DAY_OF_MONTH));
                startCal.add(Calendar.DAY_OF_MONTH, 1);
            }
            Intent intent = new Intent();
            intent.addCategory(ALARM_CATEGORY);
            intent.setAction(ALARM_ACTION);
            intent.putExtra(PARMS_REQ_CODE, reqCode);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Log.e(TAG, "startRemind: interval");
                intent.putExtra(INTERVAL_MILLIS, interval);
            }
            PendingIntent pi = PendingIntent.getBroadcast(weakContext.get(), reqCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, startCal.getTimeInMillis(), pi);
            } else {
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startCal.getTimeInMillis(), interval, pi);
            }
        } catch (Exception ex) {
            Log.e(TAG, "startRemind_Ex: " + ex.getMessage());
            Toast.makeText(weakContext.get(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 取消闹铃
     *
     * @param reqCode
     */
    public void stopRemind(int reqCode) {
        try {
            Intent intent = new Intent(weakContext.get(),AlarmReceiver.class);
            intent.addCategory(ALARM_CATEGORY);
            intent.setAction(ALARM_ACTION);
            PendingIntent pi = PendingIntent.getBroadcast(weakContext.get(), reqCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.cancel(pi);
            Log.e(TAG, "stopRemind:取消了提醒！");
        } catch (Exception ex) {
            Log.e(TAG, "stopRemind_Ex: " + ex.getMessage());
        }
    }
}
