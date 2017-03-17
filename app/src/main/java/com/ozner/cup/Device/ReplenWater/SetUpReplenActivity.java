package com.ozner.cup.Device.ReplenWater;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ozner.WaterReplenishmentMeter.WaterReplenishmentMeter;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Base.WebActivity;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.OznerDeviceSettings;
import com.ozner.cup.Device.ReplenWater.ReplenSettings.SetGenderActivity;
import com.ozner.cup.Device.ReplenWater.ReplenSettings.SetRemindTimeActivity;
import com.ozner.cup.R;
import com.ozner.device.OznerDeviceManager;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class SetUpReplenActivity extends BaseActivity {
    private static final String TAG = "SetUpReplenActivity";
    private final int REQUEST_CODE_GENDER = 1;


    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.tv_device_name)
    TextView tvDeviceName;
    @InjectView(R.id.tv_gender)
    TextView tvGender;
    private String mac = "";
    private String mUserid;
    private int gender = 0;
    private WaterReplenishmentMeter replenWater;
    private OznerDeviceSettings oznerSetting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up_replen);
        ButterKnife.inject(this);
        initToolBar();
        mUserid = OznerPreference.GetValue(this, OznerPreference.UserId, "");
        try {
            mac = getIntent().getStringExtra(Contacts.PARMS_MAC);
            replenWater = (WaterReplenishmentMeter) OznerDeviceManager.Instance().getDevice(mac);
            oznerSetting = DBManager.getInstance(this).getDeviceSettings(mUserid, mac);
            gender = (int) oznerSetting.getAppData(Contacts.DEV_REPLEN_GENDER);
            showGender(gender);
            if (oznerSetting != null) {
                tvDeviceName.setText(oznerSetting.getName());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "onCreate_Ex: " + ex.getMessage());
        }
    }

    @Override
    protected void onResume() {
        try{
            oznerSetting = DBManager.getInstance(this).getDeviceSettings(mUserid,mac);
        }catch (Exception ex){

        }
        super.onResume();
    }

    /**
     * 显示性别
     *
     * @param genderValue
     */
    private void showGender(int genderValue) {
        if (genderValue == 0) {
            tvGender.setText(R.string.women);
        } else if (genderValue == 1) {
            tvGender.setText(R.string.man);
        } else {
            tvGender.setText(R.string.secrecy);
        }
    }

    /**
     * 初始化ToolBar
     */
    private void initToolBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        title.setText(R.string.water_replen_meter);
        toolbar.setNavigationIcon(R.drawable.back);
//        title.setTextColor(ContextCompat.getColor(this, R.color.white));
//        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
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
        if (oznerSetting != null) {
            if (tvDeviceName.getText().length() > 0) {
                oznerSetting.setName(tvDeviceName.getText().toString().trim());
            } else {
                showToastCenter(R.string.input_device_name);
            }
            oznerSetting.setAppData(Contacts.DEV_REPLEN_GENDER, gender);
            DBManager.getInstance(this).updateDeviceSettings(oznerSetting);
        }
        setResult(RESULT_OK);
        this.finish();
    }

    @OnClick({R.id.rlay_device_name, R.id.rlay_gender, R.id.rlay_replen_remind, R.id.rlay_buy_essence,
            R.id.rlay_about_replen, R.id.tv_delete_device})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rlay_device_name:
                View alertView = LayoutInflater.from(this).inflate(R.layout.add_pos_view, null);
                final EditText tvNewName = (EditText) alertView.findViewById(R.id.et_addPos);
                tvNewName.setHint(R.string.input_replen_name);
                if (oznerSetting != null)
                    tvNewName.setText(oznerSetting.getName());
                else
                    tvNewName.setText("");
                new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT)
                        .setView(alertView)
                        .setTitle(R.string.change_deivce_name)
                        .setPositiveButton(R.string.ensure, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (tvNewName.getText().toString().trim().length() > 0) {
                                    tvDeviceName.setText(tvNewName.getText().toString().trim());
                                }
                            }
                        }).setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
                break;
            case R.id.rlay_gender:
                Intent genderIntent = new Intent(this, SetGenderActivity.class);
                genderIntent.putExtra(Contacts.DEV_REPLEN_GENDER, gender);
                startActivityForResult(genderIntent, REQUEST_CODE_GENDER);
                break;
            case R.id.rlay_replen_remind:
                Intent remindIntent = new Intent(this, SetRemindTimeActivity.class);
                remindIntent.putExtra(Contacts.PARMS_MAC, mac);
                startActivity(remindIntent);
                break;
            case R.id.rlay_buy_essence:
                break;
            case R.id.rlay_about_replen:
                Intent aboutIntent = new Intent(this, WebActivity.class);
                aboutIntent.putExtra(Contacts.PARMS_URL, Contacts.aboutWRM);
                startActivity(aboutIntent);
                break;
            case R.id.tv_delete_device:
                delectDevice();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_GENDER:
                    gender = data.getIntExtra(Contacts.DEV_REPLEN_GENDER, gender);
                    showGender(gender);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 删除设备
     */
    private void delectDevice() {
        new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT).setMessage(R.string.delete_this_device)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (replenWater != null) {
                            DBManager.getInstance(SetUpReplenActivity.this).deleteDeviceSettings(mUserid, replenWater.Address());
                            OznerDeviceManager.Instance().remove(replenWater);
                            setResult(RESULT_OK);
                            SetUpReplenActivity.this.finish();
                        }
                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show();
    }
}
