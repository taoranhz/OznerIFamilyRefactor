package com.ozner.cup.Device;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ozner.AirPurifier.AirPurifierManager;
import com.ozner.WaterPurifier.WaterPurifierManager;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Base.CommonAdapter;
import com.ozner.cup.Base.CommonViewHolder;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.CupManager;
import com.ozner.cup.R;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;
import com.ozner.tap.TapManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SetDeviceNameActivity extends BaseActivity {
    private static final String TAG = "SetDeviceName";

    @InjectView(R.id.title)
    TextView tv_customTitle;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.et_device_name)
    EditText etDeviceName;
    @InjectView(R.id.lv_use_position)
    ListView lvUsePosition;
    @InjectView(R.id.tv_property)
    TextView tvProperty;

    OznerDevice mDevice;
    String mac;
    private String mSelPos;
    private String saveTag;
    private PositionAdapter posAdapter;
    EditText et_addPos;
    List<String> savePosList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_device_name);
        ButterKnife.inject(this);
        initToolBar();
        initPosList();
        try {
            mac = getIntent().getStringExtra(Contacts.PARMS_MAC);
            mDevice = OznerDeviceManager.Instance().getDevice(mac);

            if (mDevice != null) {
                mSelPos = (String) mDevice.Setting().get(Contacts.DEV_USE_POS, "");
                etDeviceName.setText(mDevice.getName());
                loadDevicePosData();
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
        toolbar.setNavigationIcon(R.drawable.back);
    }

    /**
     * 初始化位置列表
     */
    private void initPosList() {
        posAdapter = new PositionAdapter(this, R.layout.position_item);
        View addItem = LayoutInflater.from(this).inflate(R.layout.position_add_item, null);
        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddPosDialog();
            }
        });
        lvUsePosition.setFooterDividersEnabled(true);
        lvUsePosition.addFooterView(addItem, null, true);
        lvUsePosition.setItemsCanFocus(false);
        lvUsePosition.setAdapter(posAdapter);
    }

    /**
     * 加载设备地点
     */
    private void loadDevicePosData() {
        try {
            if (mDevice != null) {
                String[] posArray = new String[]{};
                posAdapter.clear();
                if (TapManager.IsTap(mDevice.Type())) {
                    tv_customTitle.setText(R.string.my_tap);
                    //加载默认地点
                    posArray = getResources().getStringArray(R.array.pos_tap_defalut_array);
                    for (String item : posArray) {
                        posAdapter.addData(item);
                    }
                    loadSavePos(UserDataPreference.TapPosSaveTag);
                } else if (CupManager.IsCup(mDevice.Type())) {
                    tv_customTitle.setText(R.string.my_cup);
                    tvProperty.setText(R.string.property);
                    //加载默认地点
                    posArray = getResources().getStringArray(R.array.pos_cup_defalut_array);
                    for (String item : posArray) {
                        posAdapter.addData(item);
                    }
                    loadSavePos(UserDataPreference.CupPosSaveTag);
                } else if (WaterPurifierManager.IsWaterPurifier(mDevice.Type())) {
                    tv_customTitle.setText(R.string.my_water_purifier);
                    //加载默认地点
                    posArray = getResources().getStringArray(R.array.pos_water_defalut_array);
                    for (String item : posArray) {
                        posAdapter.addData(item);
                    }
                    loadSavePos(UserDataPreference.WaterPosSaveTag);
                } else if (AirPurifierManager.IsBluetoothAirPurifier(mDevice.Type()) || AirPurifierManager.IsWifiAirPurifier(mDevice.Type())) {
                    tv_customTitle.setText(R.string.my_air_purifier);
                    //加载默认地点
                    posArray = getResources().getStringArray(R.array.pos_air_defalut_array);
                    for (String item : posArray) {
                        posAdapter.addData(item);
                    }
                    loadSavePos(UserDataPreference.AirSaveTag);
                }
//                int selPos = posAdapter.getPosition()


                if (mSelPos != null) {
                    int selPos = posAdapter.getPosition(mSelPos);
                    if (selPos >= 0) {
                        lvUsePosition.setItemChecked(selPos, true);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "loadDevicePosData_Ex: " + ex.getMessage());
        }
    }

    /**
     * 加载保存的地点
     *
     * @param posTag
     */
    private void loadSavePos(@UserDataPreference.SaveTag String posTag) {
        try {
            String savePosStr = null;
            saveTag = posTag;
            switch (posTag) {
                case UserDataPreference.TapPosSaveTag:
                    savePosStr = UserDataPreference.GetUserData(this, UserDataPreference.TapPosSaveTag, null);
                    break;
                case UserDataPreference.CupPosSaveTag:
                    savePosStr = UserDataPreference.GetUserData(this, UserDataPreference.CupPosSaveTag, null);
                    break;
                case UserDataPreference.WaterPosSaveTag:
                    savePosStr = UserDataPreference.GetUserData(this, UserDataPreference.WaterPosSaveTag, null);
                    break;
                case UserDataPreference.AirSaveTag:
                    savePosStr = UserDataPreference.GetUserData(this, UserDataPreference.AirSaveTag, null);
                    break;
            }

            if (savePosStr != null && !savePosStr.isEmpty()) {
                Log.e(TAG, "loadSavePos_savePosStr: " + savePosStr);
                savePosList = commaSplitStrToList(savePosStr);

                for (String item : savePosList) {
                    posAdapter.addData(item);
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "loadSavePos_Ex: " + ex.getMessage());
        }
    }

    /**
     * 显示添加地点对话框
     */
    private void showAddPosDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(SetDeviceNameActivity.this, AlertDialog.THEME_HOLO_LIGHT);

        final View inputView = LayoutInflater.from(SetDeviceNameActivity.this).inflate(R.layout.add_pos_view, null);
        builder.setView(inputView);
        et_addPos = (EditText) inputView.findViewById(R.id.et_addPos);

        if (mDevice != null && CupManager.IsCup(mDevice.Type())) {
            builder.setTitle(R.string.add_property);
            et_addPos.setHint(R.string.input_new_property);
        } else {
            builder.setTitle(R.string.add_pos);
            et_addPos.setHint(R.string.input_new_pos);
        }
        builder.setPositiveButton(R.string.append, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                try {
                    if (et_addPos != null && et_addPos.length() > 0) {
                        final String addStr = et_addPos.getText().toString().trim();
                        if (posAdapter.getPosition(addStr) == -1) {
                            posAdapter.addData(et_addPos.getText().toString().trim());
                            savePosList.add(addStr);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    lvUsePosition.setItemChecked(posAdapter.getCount() - 1, true);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    lvUsePosition.setItemChecked(posAdapter.getPosition(addStr), true);
                                }
                            });
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Log.e(TAG, "addPos_Ex: " + ex.getMessage());
                }
//                    }
//                });
                dialog.cancel();
            }
        });
        builder.setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    /**
     * 字符串数组转成逗号分隔的字符串
     *
     * @param org
     *
     * @return
     */
    private String listToString(List<String> org) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < org.size() - 1; i++) {
            sb.append(org.get(i));
            sb.append(",");
        }
        sb.append(org.get(org.size() - 1));

        return sb.toString();
    }

    /**
     * 逗号分隔的字符串转成列表
     * <p>
     * on 2016/11/11
     * 关于这里为什么会看似多此一举的新建一个tempList并遍历复制，而不是使用系统方法直接转换成list，
     * 因为如果用Arrays.asList()将String[]转成List<String>的话，生成list就是定长的，长度不能改变，
     * 这样它的添加和删除方法就无法使用，否则会报UnsupportedOperationException异常。
     *
     * @param org
     *
     * @return
     */
    private List<String> commaSplitStrToList(@NonNull String org) {
        String[] strArry = org.split(",");

        List<String> tempList = new ArrayList<>();
        for (String item : strArry) {
            tempList.add(item);
        }
        return tempList;
    }


    /**
     * 保存位置列表
     */
    private void saveNewPosList() {
        if (saveTag != null && !saveTag.isEmpty() && savePosList.size() > 0) {
            Log.e(TAG, "saveNewPosList_size: " + savePosList.size());
//            UserDataPreference.SetUserData(SetDeviceNameActivity.this, saveTag, Arrays.toString(savePosList.toArray()));
            UserDataPreference.SetUserData(SetDeviceNameActivity.this, saveTag, listToString(savePosList));
        }
    }

    /**
     * 确认修改
     */
    private void ensureChange() {
        if (etDeviceName.getText().toString().trim().length() > 0) {
            int selPos = lvUsePosition.getCheckedItemPosition();
            String selPosStr = "";
            if (selPos >= 0 && selPos < posAdapter.getCount()) {
                selPosStr = posAdapter.getItem(selPos);
                Log.e(TAG, "onOptionsItemSelected: " + selPosStr);
            }
            saveNewPosList();
            Intent resIntent = new Intent();
            resIntent.putExtra(Contacts.RESULT_NAME, etDeviceName.getText().toString().trim());
            resIntent.putExtra(Contacts.RESULT_POS, selPosStr);
            setResult(RESULT_OK, resIntent);
            this.finish();
        } else {
            Toast toast = Toast.makeText(SetDeviceNameActivity.this, getString(R.string.input_device_name), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
//            Toast.makeText(SetDeviceNameActivity.this, getString(R.string.input_device_name), Toast.LENGTH_SHORT).show();
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
                // TODO: 2016/11/9 处理确定事件
                ensureChange();
                break;
        }
        return true;
    }

    class PositionAdapter extends CommonAdapter<String> {

        public PositionAdapter(Context context, int itemLayoutId) {
            super(context, itemLayoutId);
        }

        /**
         * 获取item的位置
         *
         * @param item
         *
         * @return
         */
        private int getPosition(String item) {
            int count = getCount();
            int pos = -1;
            for (int i = 0; i < count; i++) {
                if (item.equals(getItem(i))) {
                    pos = i;
                    break;
                }
            }
            return pos;
        }

        @Override
        public void convert(CommonViewHolder holder, String item, int position) {
            holder.setText(R.id.ctv_device_position, item);
        }
    }
}
