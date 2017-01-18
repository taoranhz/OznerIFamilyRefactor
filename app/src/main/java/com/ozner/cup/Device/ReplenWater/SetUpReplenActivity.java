package com.ozner.cup.Device.ReplenWater;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ozner.WaterReplenishmentMeter.WaterReplenishmentMeter;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.OznerDeviceSettings;
import com.ozner.cup.R;
import com.ozner.cup.Utils.LCLogUtils;
import com.ozner.device.OznerDeviceManager;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class SetUpReplenActivity extends BaseActivity {
    private static final String TAG = "SetUpReplenActivity";
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
    private WaterReplenishmentMeter replenWater;
    private OznerDeviceSettings oznerSetting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up_replen);
        ButterKnife.inject(this);
        initToolBar();
        mUserid = UserDataPreference.GetUserData(this, UserDataPreference.UserId, "");
        try {
            mac = getIntent().getStringExtra(Contacts.PARMS_MAC);
            replenWater = (WaterReplenishmentMeter) OznerDeviceManager.Instance().getDevice(mac);
            oznerSetting = DBManager.getInstance(this).getDeviceSettings(mUserid, mac);
            if (oznerSetting != null) {
                LCLogUtils.E(TAG, "oznerSetting:" + oznerSetting.toString());
            }
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

    }

    @OnClick({R.id.rlay_device_name, R.id.rlay_gender, R.id.rlay_replen_remind, R.id.rlay_buy_essence,
            R.id.rlay_about_replen, R.id.tv_delete_device})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rlay_device_name:
                break;
            case R.id.rlay_gender:
                break;
            case R.id.rlay_replen_remind:
                break;
            case R.id.rlay_buy_essence:
                break;
            case R.id.rlay_about_replen:
                break;
            case R.id.tv_delete_device:
                delectDevice();
                break;
        }
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
