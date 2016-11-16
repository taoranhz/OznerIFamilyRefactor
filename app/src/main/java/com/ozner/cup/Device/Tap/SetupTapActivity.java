package com.ozner.cup.Device.Tap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Device.SetDeviceNameActivity;
import com.ozner.cup.R;
import com.ozner.device.OznerDeviceManager;
import com.ozner.tap.Tap;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class SetupTapActivity extends BaseActivity {
    public static final String PARMS_MAC = "parms_mac";
    private static final String TAG = "SetupTapActivity";
    private final int SET_NAME_REQ_CODE = 0x101;//设置名字请求码
    @InjectView(R.id.title)
    TextView tv_customTitle;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.tv_device_name)
    TextView tvDeviceName;
    @InjectView(R.id.rlay_device_name)
    RelativeLayout rlayDeviceName;
    @InjectView(R.id.iv_dete_icon_first)
    ImageView ivDeteIconFirst;
    @InjectView(R.id.tv_first_desc)
    TextView tvFirstDesc;
    @InjectView(R.id.tv_first_time)
    TextView tvFirstTime;
    @InjectView(R.id.llay_first_time)
    LinearLayout llayFirstTime;
    @InjectView(R.id.iv_dete_icon_second)
    ImageView ivDeteIconSecond;
    @InjectView(R.id.tv_secont_desc)
    TextView tvSecontDesc;
    @InjectView(R.id.tv_second_time)
    TextView tvSecondTime;
    @InjectView(R.id.llay_second_time)
    LinearLayout llaySecondTime;
    @InjectView(R.id.tv_delete_device)
    TextView tvDeleteDevice;
    private String mac = "";
    private String deviceNewName = null, deviceNewPos = null;
    private Tap mTap;
    private Date time1, time2;
    private SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_tap);
        ButterKnife.inject(this);
        initToolBar();

        try {
            mac = getIntent().getStringExtra(PARMS_MAC);
            Log.e(TAG, "onCreate: mac:" + mac);
            mTap = (Tap) OznerDeviceManager.Instance().getDevice(mac);
            initViewData();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "onCreate_Ex: " + ex.getMessage());
        }
    }

    /**
     * 初始化ToolBar
     */
    private void initToolBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        tv_customTitle.setText(R.string.my_tap);
        toolbar.setNavigationIcon(R.drawable.back);
    }

    /**
     * 初始化页面数据
     */
    private void initViewData() {
        if (mTap != null) {
            //初始化设备名字
            deviceNewName = mTap.getName();
//            tv_customTitle.setText(deviceNewName);
            StringBuffer deviceNameBuf = new StringBuffer();
            deviceNameBuf.append(mTap.getName());
            String usePos = (String) mTap.Setting().get(Contacts.DEV_USE_POS, "");
            if (usePos != null && !usePos.isEmpty()) {
                deviceNameBuf.append("(");
                deviceNameBuf.append(usePos);
                deviceNameBuf.append(")");
            }
            tvDeviceName.setText(deviceNameBuf.toString());
            //初始化检测时间
            time1 = new Date(0, 0, 0, mTap.Setting().DetectTime1() / 3600,
                    mTap.Setting().DetectTime1() % 3600 / 60);
            if (time1.getHours() == 0 && time1.getMinutes() == 0) {
                time1.setHours(10);
                time1.setMinutes(0);
            }
            tvFirstTime.setText(fmt.format(time1));

            time2 = new Date(0, 0, 0, mTap.Setting().DetectTime2() / 3600, mTap
                    .Setting().DetectTime2() % 3600 / 60);
            if (time2.getHours() == 0 && time2.getMinutes() == 0) {
                time2.setHours(18);
                time2.setMinutes(0);
            }
            tvSecondTime.setText(fmt.format(time2));
        } else {
            Log.e(TAG, "initViewData: mTap为空");
        }
    }

    /**
     * 保存设置
     */
    private void saveSettings() {
        // TODO: 2016/11/9 处理保存事件
        if (mTap != null) {
            if (deviceNewName != null && deviceNewName.trim().length() > 0) {
                mTap.Setting().name(deviceNewName);
            } else {
                Toast toast = Toast.makeText(SetupTapActivity.this, getString(R.string.input_device_name), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
            if (deviceNewPos != null) {
                mTap.Setting().put(Contacts.DEV_USE_POS, deviceNewPos);
            }

            mTap.Setting().isDetectTime1(true);
            mTap.Setting().DetectTime1(time1.getHours() * 3600 + time1.getMinutes() * 60);
            mTap.Setting().isDetectTime2(true);
            mTap.Setting().DetectTime2(time2.getHours() * 3600 + time2.getMinutes() * 60);
            mTap.updateSettings();
            this.finish();
        } else {
            Toast toast = Toast.makeText(SetupTapActivity.this, getString(R.string.Not_found_device), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
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

    @OnClick({R.id.rlay_device_name, R.id.llay_first_time, R.id.llay_second_time, R.id.tv_delete_device})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rlay_device_name:
                Intent setNameIntent = new Intent(this, SetDeviceNameActivity.class);
                setNameIntent.putExtra(PARMS_MAC, mac);
                startActivityForResult(setNameIntent, SET_NAME_REQ_CODE);
                break;
            case R.id.llay_first_time:
                tvFirstDesc.setTextColor(ContextCompat.getColor(this, R.color.faq_text_blue));
                ivDeteIconFirst.setVisibility(View.VISIBLE);
                ivDeteIconSecond.setVisibility(View.INVISIBLE);
                tvSecontDesc.setTextColor(ContextCompat.getColor(this, R.color.light_black));
                final TimePicker picker = new TimePicker(this);
                picker.setIs24HourView(true);

                picker.setCurrentHour(time1.getHours());
                picker.setCurrentMinute(time1.getMinutes());
                new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT).setView(picker)
                        .setPositiveButton(getString(R.string.ensure), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
                                time1 = new Date(0, 0, 0, picker
                                        .getCurrentHour(), picker
                                        .getCurrentMinute());
                                tvFirstTime.setText(fmt.format(time1));
                            }
                        }).show();
                break;
            case R.id.llay_second_time:

                tvSecontDesc.setTextColor(ContextCompat.getColor(this, R.color.faq_text_blue));
                ivDeteIconSecond.setVisibility(View.VISIBLE);
                ivDeteIconFirst.setVisibility(View.INVISIBLE);
                tvFirstDesc.setTextColor(ContextCompat.getColor(this, R.color.light_black));
                final TimePicker pickerSecond = new TimePicker(this);
                pickerSecond.setIs24HourView(true);

                pickerSecond.setCurrentHour(time2.getHours());
                pickerSecond.setCurrentMinute(time2.getMinutes());
                new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT).setView(pickerSecond)
                        .setPositiveButton(getString(R.string.ensure), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                time2 = new Date(0, 0, 0, pickerSecond
                                        .getCurrentHour(), pickerSecond
                                        .getCurrentMinute());
                                tvSecondTime.setText(fmt.format(time2));
                            }
                        }).show();
                break;
            case R.id.tv_delete_device:
                new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT).setMessage(R.string.delete_this_device)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mTap != null) {
                                    OznerDeviceManager.Instance().remove(mTap);
                                    setResult(RESULT_OK);
                                    SetupTapActivity.this.finish();
                                }
                            }
                        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
                break;
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
