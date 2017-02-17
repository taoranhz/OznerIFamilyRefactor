package com.ozner.cup.Device.ReplenWater.ReplenSettings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.OznerDeviceSettings;
import com.ozner.cup.Device.ReplenWater.RemindUtils.RemindUtil;
import com.ozner.cup.R;
import com.ozner.cup.Utils.DateUtils;
import com.ozner.cup.Utils.LCLogUtils;
import com.zcw.togglebutton.ToggleButton;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class SetRemindTimeActivity extends BaseActivity {
    public static final String PREFERENCES = "AlarmClock";
    private static final String TAG = "SetRemindTime";
    private final long DEFAULT_INTERVAL = 30000;//30s

    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.tv_time1)
    TextView tvTime1;
    @InjectView(R.id.tv_time2)
    TextView tvTime2;
    @InjectView(R.id.tv_time3)
    TextView tvTime3;

    @InjectView(R.id.tb_first_switch)
    ToggleButton tbFirstSwitch;
    @InjectView(R.id.tb_second_switch)
    ToggleButton tbSecondSwitch;
    @InjectView(R.id.tb_three_switch)
    ToggleButton tbThreeSwitch;

    private Calendar time1Cal;
    private Calendar time2Cal;
    private Calendar time3Cal;
    private boolean isRemind1 = false;
    private boolean isRemind2 = false;
    private boolean isRemind3 = false;
    private String mac;
    private String mUserid;
    private OznerDeviceSettings oznerSetting;
    private RemindUtil remindUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_remind_time);
        ButterKnife.inject(this);
        initToolBar();
        mUserid = UserDataPreference.GetUserData(this, UserDataPreference.UserId, "");
        remindUtil = new RemindUtil(this);
        try {
            mac = getIntent().getStringExtra(Contacts.PARMS_MAC);
            oznerSetting = DBManager.getInstance(this).getDeviceSettings(mUserid, mac);
            initTime();
        } catch (Exception ex) {
            LCLogUtils.E(TAG, "onCreate_Ex:" + ex.getMessage());
        }
        tbFirstSwitch.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                isRemind1 = on;
                if (!on) {
                    remindUtil.stopRemind(1);
                }
            }
        });
        tbSecondSwitch.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                isRemind2 = on;
                if (!on) {
                    remindUtil.stopRemind(2);
                }
            }
        });
        tbThreeSwitch.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                isRemind3 = on;
                if (!on) {
                    remindUtil.stopRemind(3);
                }
            }
        });
    }

    /**
     * 初始化ToolBar
     */
    private void initToolBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        title.setText(R.string.water_replen_meter);
        toolbar.setNavigationIcon(R.drawable.back);
    }

    /**
     * 初始化提醒时间
     */
    private void initTime() {
        try {
            if (oznerSetting != null) {
                long time1 = 0;
                long time2 = 0;
                long time3 = 0;

                try {
                    isRemind1 = (boolean) oznerSetting.getAppData(Contacts.DEV_REPLEN_IS_REMIND_1);
                    time1 = (long) oznerSetting.getAppData(Contacts.DEV_REPLEN_REMIND_TIME_1);
                } catch (Exception ex) {

                }
                try {
                    isRemind2 = (boolean) oznerSetting.getAppData(Contacts.DEV_REPLEN_IS_REMIND_2);
                    time2 = (long) oznerSetting.getAppData(Contacts.DEV_REPLEN_REMIND_TIME_2);
                } catch (Exception ex) {

                }
                try {
                    isRemind3 = (boolean) oznerSetting.getAppData(Contacts.DEV_REPLEN_IS_REMIND_3);
                    time3 = (long) oznerSetting.getAppData(Contacts.DEV_REPLEN_REMIND_TIME_3);
                } catch (Exception ex) {

                }
                if (time1 != 0) {
                    time1Cal = Calendar.getInstance();
                    time1Cal.setTimeInMillis(time1);
                    tvTime1.setText(DateUtils.hourMinFormt(time1Cal.getTime()));
                    if (isRemind1) {
                        tbFirstSwitch.setToggleOn();
                    } else {
                        tbFirstSwitch.setToggleOff();
                    }
                } else {
                    tvTime1.setText("无");
                    tbFirstSwitch.setToggleOff();
                }

                if (time2 != 0) {
                    time2Cal = Calendar.getInstance();
                    time2Cal.setTimeInMillis(time2);
                    tvTime2.setText(DateUtils.hourMinFormt(time2Cal.getTime()));
                    if (isRemind2) {
                        tbSecondSwitch.setToggleOn();
                    } else {
                        tbSecondSwitch.setToggleOff();
                    }
                } else {
                    tvTime2.setText("无");
                    tbSecondSwitch.setToggleOff();
                }

                if (time3 != 0) {
                    time3Cal = Calendar.getInstance();
                    time3Cal.setTimeInMillis(time3);
                    tvTime3.setText(DateUtils.hourMinFormt(time3Cal.getTime()));
                    if (isRemind3) {
                        tbThreeSwitch.setToggleOn();
                    } else {
                        tbThreeSwitch.setToggleOff();
                    }
                } else {
                    tvTime3.setText("无");
                    tbThreeSwitch.setToggleOff();
                }
            }
        } catch (Exception ex) {
            LCLogUtils.E(TAG, "initTime_Ex:" + ex.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(0, 0, 0, getString(R.string.ensure));

        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            case 0:
                ensureAlarm();
                break;
        }
        return true;
    }

    /**
     * 确认时间,并启动定时提醒
     */
    private void ensureAlarm() {
        Log.e(TAG, "ensureTime: 确认设置闹铃");
        try {
            if (time1Cal != null) {
                oznerSetting.setAppData(Contacts.DEV_REPLEN_REMIND_TIME_1, time1Cal.getTimeInMillis());
                oznerSetting.setAppData(Contacts.DEV_REPLEN_IS_REMIND_1, isRemind1);
            }
            if (time2Cal != null) {
                oznerSetting.setAppData(Contacts.DEV_REPLEN_REMIND_TIME_2, time2Cal.getTimeInMillis());
                oznerSetting.setAppData(Contacts.DEV_REPLEN_IS_REMIND_2, isRemind2);
            }
            if (time3Cal != null) {
                oznerSetting.setAppData(Contacts.DEV_REPLEN_REMIND_TIME_3, time3Cal.getTimeInMillis());
                oznerSetting.setAppData(Contacts.DEV_REPLEN_IS_REMIND_3, isRemind3);
            }
            DBManager.getInstance(this).updateDeviceSettings(oznerSetting);
            if (time1Cal != null && isRemind1) {
                remindUtil.startRemind(1, time1Cal, DEFAULT_INTERVAL);
            } else {
                remindUtil.stopRemind(1);
            }
            if (time2Cal != null && isRemind2) {
                remindUtil.startRemind(2, time2Cal, 50000);
            } else {
                remindUtil.stopRemind(2);
            }
            if (time3Cal != null && isRemind3) {
                remindUtil.startRemind(3, time3Cal, 70000);
            } else {
                remindUtil.stopRemind(3);
            }

        } catch (Exception ex) {
            LCLogUtils.E(TAG, "ensureTime_Ex:" + ex.getMessage());
        }
        this.finish();
    }

    @OnClick({R.id.rlay_time1, R.id.rlay_time2, R.id.rlay_time3})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rlay_time1:
                final TimePicker timePicker1 = new TimePicker(this);
                Calendar cal1 = Calendar.getInstance();
                timePicker1.setIs24HourView(true);
                timePicker1.setCurrentHour(cal1.get(Calendar.HOUR_OF_DAY));
                timePicker1.setCurrentMinute(cal1.get(Calendar.MINUTE));
                new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT).setView(timePicker1)
                        .setPositiveButton(R.string.ensure, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (time1Cal == null) {
                                    time1Cal = Calendar.getInstance();
                                }
                                time1Cal.set(Calendar.HOUR_OF_DAY, timePicker1.getCurrentHour());
                                time1Cal.set(Calendar.MINUTE, timePicker1.getCurrentMinute());
                                time1Cal.set(Calendar.SECOND, 0);
                                time1Cal.set(Calendar.MILLISECOND, 0);
                                tvTime1.setText(DateUtils.hourMinFormt(time1Cal.getTime()));
                            }
                        }).setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
                break;
            case R.id.rlay_time2:
                final TimePicker timePicker2 = new TimePicker(this);
                Calendar cal2 = Calendar.getInstance();
                timePicker2.setIs24HourView(true);
                timePicker2.setCurrentHour(cal2.get(Calendar.HOUR_OF_DAY));
                timePicker2.setCurrentMinute(cal2.get(Calendar.MINUTE));
                new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT).setView(timePicker2)
                        .setPositiveButton(R.string.ensure, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (null == time2Cal) {
                                    time2Cal = Calendar.getInstance();
                                }
                                time2Cal.set(Calendar.HOUR_OF_DAY, timePicker2.getCurrentHour());
                                time2Cal.set(Calendar.MINUTE, timePicker2.getCurrentMinute());
                                time2Cal.set(Calendar.SECOND, 0);
                                time2Cal.set(Calendar.MILLISECOND, 0);
                                tvTime2.setText(DateUtils.hourMinFormt(time2Cal.getTime()));
                            }
                        }).setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
                break;
            case R.id.rlay_time3:
                final TimePicker timePicker3 = new TimePicker(this);
               Calendar cal3 = Calendar.getInstance();
                timePicker3.setIs24HourView(true);
                timePicker3.setCurrentHour(cal3.get(Calendar.HOUR_OF_DAY));
                timePicker3.setCurrentMinute(cal3.get(Calendar.MINUTE));
                new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT).setView(timePicker3)
                        .setPositiveButton(R.string.ensure, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (null == time3Cal) {
                                    time3Cal = Calendar.getInstance();
                                }
                                time3Cal.set(Calendar.HOUR_OF_DAY, timePicker3.getCurrentHour());
                                time3Cal.set(Calendar.MINUTE, timePicker3.getCurrentMinute());
                                time3Cal.set(Calendar.SECOND, 0);
                                time3Cal.set(Calendar.MILLISECOND, 0);
                                tvTime3.setText(DateUtils.hourMinFormt(time3Cal.getTime()));
                            }
                        }).setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
                break;
        }
    }
}
