package com.ozner.yiquan.MyCenter.Settings;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.ozner.yiquan.Base.BaseActivity;
import com.ozner.yiquan.Command.UserDataPreference;
import com.ozner.yiquan.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class UnitSettingsActivity extends BaseActivity {

    //    @InjectView(R.id.title)
//    TextView title;
//    @InjectView(R.id.toolbar)
//    Toolbar toolbar;
    @InjectView(R.id.iv_tempSelect1)
    ImageView ivTempSelect1;
    @InjectView(R.id.iv_tempSelect2)
    ImageView ivTempSelect2;
    @InjectView(R.id.iv_MLSelect)
    ImageView ivMLSelect;
    @InjectView(R.id.iv_DLSelect)
    ImageView ivDLSelect;
    @InjectView(R.id.iv_OZSelect)
    ImageView ivOZSelect;

    private int tempUnit, volUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unit_settings);
        ButterKnife.inject(this);
//        initToolBar();
        tempUnit = Integer.parseInt(UserDataPreference.GetUserData(this, UserDataPreference.TempUnit, "0"));
        volUnit = Integer.parseInt(UserDataPreference.GetUserData(this, UserDataPreference.VolUnit, "0"));
        selectTempUnit(tempUnit);
        selectVolumnUnit(volUnit);
    }

//    private void initToolBar() {
//        toolbar.setTitle("");
//        setSupportActionBar(toolbar);
//        title.setText(R.string.unit);
//        toolbar.setNavigationIcon(R.drawable.back);
//        toolbar.setBackgroundColor(Color.WHITE);
//        title.setTextColor(Color.BLACK);
//
//    }

    @OnClick({R.id.llay_back, R.id.llay_save, R.id.rlay_centigrade, R.id.rlay_fahrenheit, R.id.rlay_Unit_ML, R.id.rlay_Unit_DL, R.id.rlay_Unit_OZ})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.llay_back:
                this.finish();
                break;
            case R.id.llay_save:
                saveSettings();
                break;
            case R.id.rlay_centigrade:
                selectTempUnit(MeasurementUnit.TempUnit.CENTIGRADE);
                break;
            case R.id.rlay_fahrenheit:
                selectTempUnit(MeasurementUnit.TempUnit.FAHRENHEIT);
                break;
            case R.id.rlay_Unit_ML:
                selectVolumnUnit(MeasurementUnit.VolumUnit.ML);
                break;
            case R.id.rlay_Unit_DL:
                selectVolumnUnit(MeasurementUnit.VolumUnit.DL);
                break;
            case R.id.rlay_Unit_OZ:
                selectVolumnUnit(MeasurementUnit.VolumUnit.OZ);
                break;
        }
    }

    /**
     * 设置饮水温度
     *
     * @param unit
     */
    private void selectTempUnit(int unit) {
        tempUnit = unit;
        switch (unit) {
            case MeasurementUnit.TempUnit.CENTIGRADE://摄氏度
                ivTempSelect1.setSelected(true);
                ivTempSelect2.setSelected(false);
                break;
            case MeasurementUnit.TempUnit.FAHRENHEIT://华氏度
                ivTempSelect1.setSelected(false);
                ivTempSelect2.setSelected(true);
                break;
        }
    }

    /**
     * 设置饮水量单位
     *
     * @param unit
     */
    private void selectVolumnUnit(int unit) {
        volUnit = unit;
        switch (unit) {
            case MeasurementUnit.VolumUnit.ML:
                ivMLSelect.setSelected(true);
                ivDLSelect.setSelected(false);
                ivOZSelect.setSelected(false);
                break;
            case MeasurementUnit.VolumUnit.DL:
                ivMLSelect.setSelected(false);
                ivDLSelect.setSelected(true);
                ivOZSelect.setSelected(false);
                break;
            case MeasurementUnit.VolumUnit.OZ:
                ivMLSelect.setSelected(false);
                ivDLSelect.setSelected(false);
                ivOZSelect.setSelected(true);
                break;
        }
    }

    /**
     * 保存设置
     */
    private void saveSettings() {
        UserDataPreference.SetUserData(this, UserDataPreference.TempUnit, String.valueOf(tempUnit));
        UserDataPreference.SetUserData(this, UserDataPreference.VolUnit, String.valueOf(volUnit));
        showToastCenter(R.string.save_successful);
        this.finish();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuItem item = menu.add(0, 0, 0, getString(R.string.save));
//        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//
//        SpannableString spanString = new SpannableString(item.getTitle().toString());
//        spanString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, spanString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        item.setTitle(spanString);
//
////        MenuInflater inflater = getMenuInflater();
////
////        getLayoutInflater().setFactory(new LayoutInflater.Factory() {
////
////            @Override
////            public View onCreateView(String name, Context context,
////                                     AttributeSet attrs) {
////                System.out.println(name);
////                if (name.equalsIgnoreCase("com.android.internal.view.menu.IconMenuItemView")
////                        || name.equalsIgnoreCase("com.android.internal.view.menu.ActionMenuItemView")) {
////                    try {
////                        LayoutInflater f = getLayoutInflater();
////                        final View view = f.createView(name, null, attrs);
////                        System.out.println((view instanceof TextView));
////                        if (view instanceof TextView) {
////                            ((TextView) view).setTextColor(Color.BLACK);
////                        }
////                        return view;
////                    } catch (InflateException e) {
////                        e.printStackTrace();
////                    } catch (ClassNotFoundException e) {
////                        e.printStackTrace();
////                    }
////                }
////                return null;
////            }
////
////        });
////
////        inflater.inflate(R.menu.main, menu);
//
//        return super.onCreateOptionsMenu(menu);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                this.finish();
//                break;
//            case 0:
//                saveSettings();
//                break;
//        }
//        return true;
//    }
}
