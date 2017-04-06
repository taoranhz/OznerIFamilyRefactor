package com.ozner.cup.Device.AirPurifier;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ozner.AirPurifier.AirPurifier;
import com.ozner.AirPurifier.AirPurifier_MXChip;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Base.WebActivity;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.OznerDeviceSettings;
import com.ozner.cup.Device.SetDeviceNameActivity;
import com.ozner.cup.R;
import com.ozner.device.OznerDeviceManager;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * 台式空净和立式空净共同的设置页面
 */
public class SetUpAirVerActivity extends BaseActivity {
    private static final String TAG = "SetUpAirVer";
    private final int SET_NAME_REQ_CODE = 0x101;//设置名字请求码
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.tv_device_name)
    TextView tvDeviceName;
    @InjectView(R.id.rlay_introduct)
    RelativeLayout rlayIntroduct;
    @InjectView(R.id.llay_faq)
    RelativeLayout llayFaq;
    private String mac = "";
    private AirPurifier mAirPurifier;
    private String deviceNewName = null, deviceNewPos = null;
    private String mUserid;
    private OznerDeviceSettings oznerSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up_air_ver);
        ButterKnife.inject(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initToolBar();
        mUserid = OznerPreference.GetValue(this, OznerPreference.UserId, "");
        try {
            mac = getIntent().getStringExtra(Contacts.PARMS_MAC);
            Log.e(TAG, "onCreate: mac:" + mac);
            mAirPurifier = (AirPurifier) OznerDeviceManager.Instance().getDevice(mac);
            oznerSetting = DBManager.getInstance(this).getDeviceSettings(mUserid, mac);
            initViewData();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "onCreate_Ex: " + ex.getMessage());
        }

        if(UserDataPreference.isLoginEmail(this)){
            llayFaq.setVisibility(View.GONE);
        }
        if(isLanguageEn()){
            rlayIntroduct.setVisibility(View.GONE);
        }
    }


    /**
     * 初始化ToolBar
     */
    private void initToolBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        title.setText(R.string.my_air_purifier);
        toolbar.setNavigationIcon(R.drawable.back);
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
                new AlertDialog.Builder(SetUpAirVerActivity.this).setTitle("").setMessage(getString(R.string.save_device))
                        .setPositiveButton(getString(R.string.yes), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SetUpAirVerActivity.this.finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                finish();
                            }
                        }).show();
                break;
            case 0:
                new AlertDialog.Builder(SetUpAirVerActivity.this).setTitle("").setMessage(getString(R.string.save_device))
                        .setPositiveButton(getString(R.string.yes), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                saveSettings();
                                SetUpAirVerActivity.this.finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                finish();
                            }
                        }).show();
                break;
        }
        return true;
    }

    /**
     * 初始化页面数据
     */
    private void initViewData() {
        if (mAirPurifier != null) {
            deviceNewName = mAirPurifier.getName();
            if (oznerSetting != null) {
                deviceNewName = oznerSetting.getName();
                deviceNewPos = oznerSetting.getDevicePosition();
            }

            StringBuffer deviceNameBuf = new StringBuffer();
            deviceNameBuf.append(deviceNewName);
//            String usePos = (String) mAirPurifier.Setting().get(Contacts.DEV_USE_POS, "");
            if (deviceNewPos != null && !deviceNewPos.isEmpty()) {
                deviceNameBuf.append("(");
                deviceNameBuf.append(deviceNewPos);
                deviceNameBuf.append(")");
            }
            tvDeviceName.setText(deviceNameBuf.toString());
        } else {
            Log.e(TAG, "initViewData: mAirPurifier 为空");
        }
    }

    @OnClick({R.id.rlay_device_name, R.id.rlay_introduct, R.id.llay_faq, R.id.tv_delete_device})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rlay_device_name:
                Intent setNameIntent = new Intent(this, SetDeviceNameActivity.class);
                setNameIntent.putExtra(Contacts.PARMS_MAC, mac);
                startActivityForResult(setNameIntent, SET_NAME_REQ_CODE);
                break;
            case R.id.rlay_introduct:
                if (mAirPurifier != null) {
                    Intent aboutIntent = new Intent(this, WebActivity.class);
                    if (mAirPurifier instanceof AirPurifier_MXChip) {
                        aboutIntent.putExtra(Contacts.PARMS_URL, Contacts.aboutAirVer);
                    } else {
                        aboutIntent.putExtra(Contacts.PARMS_URL, Contacts.aboutAirDesk);
                    }
                    startActivity(aboutIntent);
                } else {
                    showToastCenter(R.string.Not_found_device);
                }
                break;
            case R.id.llay_faq:
                if (mAirPurifier != null) {
                    Intent faqIntent = new Intent(this, WebActivity.class);
                    faqIntent.putExtra(Contacts.PARMS_URL, Contacts.air_faq);
                    startActivity(faqIntent);
                } else {
                    showToastCenter(R.string.Not_found_device);
                }
                break;
            case R.id.tv_delete_device:
                new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT).setMessage(R.string.delete_this_device)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mAirPurifier != null) {
                                    DBManager.getInstance(SetUpAirVerActivity.this).deleteDeviceSettings(mUserid, mAirPurifier.Address());
                                    OznerDeviceManager.Instance().remove(mAirPurifier);
                                    setResult(RESULT_OK);
                                    SetUpAirVerActivity.this.finish();
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

    /**
     * 保存设置
     */
    private void saveSettings() {
        if (mAirPurifier != null) {
            if (deviceNewName != null && !deviceNewName.isEmpty()) {
                mAirPurifier.Setting().name(deviceNewName);
            } else {
                showToastCenter(R.string.input_device_name);
                return;
            }
//            if (deviceNewPos != null) {
//                mAirPurifier.Setting().put(Contacts.DEV_USE_POS, deviceNewPos);
//            }
            mAirPurifier.updateSettings();

            if (oznerSetting == null) {
                oznerSetting = new OznerDeviceSettings();
                oznerSetting.setUserId(mUserid);
                oznerSetting.setCreateTime(String.valueOf(new Date().getTime()));
            }
            oznerSetting.setName(deviceNewName);
            oznerSetting.setDevcieType(mAirPurifier.Type());
            oznerSetting.setStatus(0);
            oznerSetting.setMac(mAirPurifier.Address());
            oznerSetting.setDevicePosition(deviceNewPos);
            DBManager.getInstance(this).updateDeviceSettings(oznerSetting);
            this.finish();
        } else {
            showToastCenter(R.string.Not_found_device);
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
