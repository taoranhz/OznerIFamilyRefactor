package com.ozner.cup.Device.WaterPurifier;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ozner.WaterPurifier.WaterPurifier;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Base.WebActivity;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Device.SetDeviceNameActivity;
import com.ozner.cup.R;
import com.ozner.device.OznerDeviceManager;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class SetupWaterActivity extends BaseActivity {
    private static final String TAG = "SetupWater";
    private final int SET_NAME_REQ_CODE = 0x101;//设置名字请求码
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.tv_device_name)
    TextView tvDeviceName;
    @InjectView(R.id.rlay_device_name)
    RelativeLayout rlayDeviceName;
    @InjectView(R.id.tv_delete_device)
    TextView tvDeleteDevice;
    @InjectView(R.id.tv_mac)
    TextView tvMac;

    private String deviceNewName = null, deviceNewPos = null;
    private WaterPurifier mWaterPurifier;
    private String mac = "";
    private String url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_water);
        ButterKnife.inject(this);
        initToolBar();

        try {
            mac = getIntent().getStringExtra(Contacts.PARMS_MAC);
//            url = getIntent().getStringExtra(Contacts.PARMS_URL);
            Log.e(TAG, "onCreate: mac:" + mac);
            mWaterPurifier = (WaterPurifier) OznerDeviceManager.Instance().getDevice(mac);
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
        title.setText(R.string.my_water_purifier);
        toolbar.setNavigationIcon(R.drawable.back);
    }


    /**
     * 初始化页面数据
     */
    private void initViewData() {
        if (mWaterPurifier != null) {
            tvMac.setText(mWaterPurifier.Address());
//            title.setText(deviceNewName);
            StringBuffer deviceNameBuf = new StringBuffer();
            deviceNameBuf.append(mWaterPurifier.getName());
            String usePos = (String) mWaterPurifier.Setting().get(Contacts.DEV_USE_POS, "");
            if (usePos != null && !usePos.isEmpty()) {
                deviceNameBuf.append("(");
                deviceNameBuf.append(usePos);
                deviceNameBuf.append(")");
            }
            tvDeviceName.setText(deviceNameBuf.toString());
        }
    }

    @OnClick({R.id.rlay_device_name, R.id.rlay_about_purifier, R.id.tv_delete_device})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rlay_device_name:
                Intent setNameIntent = new Intent(this, SetDeviceNameActivity.class);
                setNameIntent.putExtra(Contacts.PARMS_MAC, mac);
                startActivityForResult(setNameIntent, SET_NAME_REQ_CODE);
                break;
            case R.id.rlay_about_purifier:
                if (mWaterPurifier != null) {
                    Intent webIntent = new Intent(this, WebActivity.class);
                    webIntent.putExtra(Contacts.PARMS_URL, Contacts.aboutWaterPurifier);
                    startActivity(webIntent);
                } else {
                    showToastCenter(R.string.Not_found_device);
                }
                break;
            case R.id.tv_delete_device:
                new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT).setMessage(R.string.delete_this_device)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mWaterPurifier != null) {
                                    OznerDeviceManager.Instance().remove(mWaterPurifier);
                                    setResult(RESULT_OK);
                                    SetupWaterActivity.this.finish();
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
        // TODO: 2016/11/9 处理保存事件
        if (mWaterPurifier != null) {
            if (deviceNewName != null && deviceNewName.trim().length() > 0) {
                mWaterPurifier.Setting().name(deviceNewName);
            } else {
                Toast toast = Toast.makeText(SetupWaterActivity.this, getString(R.string.input_device_name), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
            if (deviceNewPos != null) {
                mWaterPurifier.Setting().put(Contacts.DEV_USE_POS, deviceNewPos);
            }
            mWaterPurifier.updateSettings();
            this.finish();
        } else {
            Toast toast = Toast.makeText(SetupWaterActivity.this, getString(R.string.Not_found_device), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
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
}
