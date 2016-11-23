package com.ozner.cup.Device.Cup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Cup;
import com.ozner.cup.Device.SetDeviceNameActivity;
import com.ozner.cup.R;
import com.ozner.cup.UIView.ColorPickerBaseView;
import com.ozner.cup.UIView.ColorPickerView;
import com.ozner.cup.Utils.DateFormatUtils;
import com.ozner.device.BaseDeviceIO;
import com.ozner.device.OznerDeviceManager;
import com.zcw.togglebutton.ToggleButton;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.ozner.cup.R.id.rlay_device_name;

public class SetUpCupActivity extends BaseActivity {
    private static final String TAG = "SetUpCupActivity";
    private final int SET_NAME_REQ_CODE = 0x101;//设置名字请求码

    private final int DEFAULT_WEIGHT = 55;
    private final int DEFAULT_WATER_GOAL = 2000;

    @InjectView(R.id.tv_delete_device)
    TextView tvDeleteDevice;
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.sl_root)
    ScrollView slRoot;
    @InjectView(R.id.rb_temp)
    RadioButton rbTemp;
    @InjectView(R.id.rb_tds)
    RadioButton rbTds;
    @InjectView(R.id.tv_below)
    TextView tvBelow;
    @InjectView(R.id.tv_middle)
    TextView tvMiddle;
    @InjectView(R.id.tv_hight)
    TextView tvHight;
    @InjectView(R.id.tv_device_name)
    TextView tvDeviceName;
    @InjectView(rlay_device_name)
    RelativeLayout rlayDeviceName;
    @InjectView(R.id.et_volum)
    EditText etVolum;
    @InjectView(R.id.et_weight)
    EditText etWeight;
    @InjectView(R.id.cb_cool)
    CheckBox cbCool;
    @InjectView(R.id.cb_sport)
    CheckBox cbSport;
    @InjectView(R.id.cb_hotday)
    CheckBox cbHotday;
    @InjectView(R.id.cb_period)
    CheckBox cbPeriod;
    @InjectView(R.id.tv_remind_starttime)
    TextView tvRemindStarttime;
    @InjectView(R.id.tv_remind_endtime)
    TextView tvRemindEndtime;
    @InjectView(R.id.tv_remind_interval)
    TextView tvRemindInterval;
    @InjectView(R.id.cup_colorpicker)
    ColorPickerView cupColorpicker;
    @InjectView(R.id.tb_cupRemind)
    ToggleButton tbCupRemind;

    private String mac;
    private Cup mCup;
    private int cupColor;
    private int mWeight, mWaterGoal;
    private String deviceNewName = null, deviceNewPos = null;
    private Calendar tipStartCal, tipEndCal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up_cup);
        ButterKnife.inject(this);
//        cupColorpicker = (ColorPickerView) findViewById(R.id.cup_colorpicker);
//        tbCupRemind = (ToggleButton) findViewById(R.id.tb_cupRemind);
        tipStartCal = Calendar.getInstance();
        tipEndCal = Calendar.getInstance();
        initToolBar();
        initColorPicker();
        try {
            mac = getIntent().getStringExtra(Contacts.PARMS_MAC);
            Log.e(TAG, "onCreate: mac:" + mac);
            mCup = (Cup) OznerDeviceManager.Instance().getDevice(mac);
            initViewData();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "onCreate_Ex: " + ex.getMessage());
        }
    }

    /**
     * 初始化颜色选择器
     */
    private void initColorPicker() {
        cupColorpicker.setOnColorChangedListener(new ColorPickerBaseView.OnColorChangedListener() {
            @Override
            public void colorChanged(final int color) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCup != null && mCup.connectStatus().equals(BaseDeviceIO.ConnectStatus.Connected)) {
                            mCup.changeHaloColor(color);
                            cupColor = color;
                        }
                    }
                });
            }
        });

        cupColorpicker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    slRoot.requestDisallowInterceptTouchEvent(false);
                } else {
                    slRoot.requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }
        });

    }

    /**
     * 初始化ToolBar
     */
    private void initToolBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        title.setText(R.string.my_cup);
        toolbar.setNavigationIcon(R.drawable.back);
    }

    /**
     * 初始化Ui数据
     */
    private void initViewData() {
        if (mCup != null) {
            //初始化设备名字
            deviceNewName = mCup.getName();
            StringBuffer deviceNameBuf = new StringBuffer();
            deviceNameBuf.append(mCup.getName());
            String usePos = (String) mCup.Setting().get(Contacts.DEV_USE_POS, "");
            if (usePos != null && !usePos.isEmpty()) {
                deviceNameBuf.append("(");
                deviceNameBuf.append(usePos);
                deviceNameBuf.append(")");
            }
            tvDeviceName.setText(deviceNameBuf.toString());

            //初始化用户体重
            mWeight = (int) mCup.Setting().get(Contacts.DEV_USER_WEIGHT, -1);
            if (-1 == mWeight) {
                mWeight = DEFAULT_WEIGHT;
            }
            etWeight.setText(String.valueOf(mWeight));

            //初始化饮水目标
            mWaterGoal = (int) mCup.Setting().get(Contacts.DEV_USER_WATER_GOAL, -1);
            if (-1 == mWaterGoal) {
                mWaterGoal = DEFAULT_WATER_GOAL;
            }
            etVolum.setText(String.valueOf(mWaterGoal));

            initRemind();
        }
    }


    /**
     * 初始化提醒信息
     */
    private void initRemind() {
        if (mCup != null) {
            //初始化提醒间隔
            tvRemindInterval.setText(String.valueOf(mCup.Setting().remindInterval()));

            //初始化饮水提醒时间
            int startHour = mCup.Setting().remindStart() / 3600;
            int endHour = mCup.Setting().remindEnd() / 3600;
            tipStartCal.set(Calendar.HOUR_OF_DAY, startHour);
            tipStartCal.set(Calendar.MINUTE, 0);
            tipStartCal.set(Calendar.SECOND, 0);
            tipEndCal.set(Calendar.HOUR_OF_DAY, endHour);
            tipEndCal.set(Calendar.MINUTE, 0);
            tipEndCal.set(Calendar.SECOND, 0);
            tvRemindStarttime.setText(DateFormatUtils.hourMinFormt(tipStartCal.getTime()));
            tvRemindEndtime.setText(DateFormatUtils.hourMinFormt(tipEndCal.getTime()));

            //初始化灯带颜色
            cupColorpicker.SetCenterColor(mCup.Setting().haloColor());

            //初始化水杯提醒功能
            if (mCup.Setting().RemindEnable()) {
                tbCupRemind.setToggleOn();
            } else {
                tbCupRemind.setToggleOff();
            }

            //设置开关监听
            tbCupRemind.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
                @Override
                public void onToggle(final boolean on) {
                    Log.e(TAG, "onToggle: " + on);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (on != mCup.Setting().RemindEnable()) {
                                // TODO: 2016/11/23 开关切换事件

                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(0, 0, 0, getString(R.string.save));

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
                saveSettings();
                break;
        }
        return true;
    }

    /**
     * 保存设置
     */
    private void saveSettings() {

    }


    @OnClick({R.id.tv_delete_device, R.id.rb_temp, R.id.rb_tds, R.id.rlay_about_cup, R.id.tv_remind_endtime, R.id.tv_remind_interval
            , R.id.rlay_device_name})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rlay_device_name:
                Intent setNameIntent = new Intent(this, SetDeviceNameActivity.class);
                setNameIntent.putExtra(Contacts.PARMS_MAC, mac);
                startActivityForResult(setNameIntent, SET_NAME_REQ_CODE);
                break;
            case R.id.tv_delete_device:
                new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT).setMessage(R.string.delete_this_device)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mCup != null) {
                                    OznerDeviceManager.Instance().remove(mCup);
                                    setResult(RESULT_OK);
                                    SetUpCupActivity.this.finish();
                                }
                            }
                        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
                break;
            case R.id.rb_temp:
                checkTemp(true);
                checkTds(false);
                break;
            case R.id.rb_tds:
                checkTemp(false);
                checkTds(true);
                break;
            case R.id.rlay_about_cup:
                break;
            case R.id.tv_remind_endtime:
                break;
            case R.id.tv_remind_interval:
                break;
        }
    }

    /**
     * 选中温度提示
     *
     * @param isCheck
     */
    private void checkTemp(boolean isCheck) {
        rbTemp.setChecked(isCheck);
        if (isCheck) {
            tvBelow.setText(R.string.temp_blow);
            tvMiddle.setText(R.string.temp_middle);
            tvHight.setText(R.string.temp_hot);
        }
    }

    /**
     * 选中tds提示
     *
     * @param isCheck
     */
    private void checkTds(boolean isCheck) {
        rbTds.setChecked(isCheck);
        if (isCheck) {
            tvBelow.setText(R.string.tds_blow);
            tvMiddle.setText(R.string.tds_middle);
            tvHight.setText(R.string.tds_high);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SET_NAME_REQ_CODE:
                    try {
                        deviceNewName = data.getStringExtra(Contacts.RESULT_NAME);
                        deviceNewPos = data.getStringExtra(Contacts.RESULT_POS);
                        Log.e(TAG, "onActivityResult: newName:" + deviceNewName + ",newPos:" + deviceNewPos);
                        if (deviceNewPos != null && !deviceNewPos.isEmpty())
                            tvDeviceName.setText(deviceNewName + "(" + deviceNewPos + ")");
                        else
                            tvDeviceName.setText(deviceNewName);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Log.e(TAG, "onActivityResult_Ex: " + ex.getMessage());
                    }
                    break;
            }
        }
    }

}
