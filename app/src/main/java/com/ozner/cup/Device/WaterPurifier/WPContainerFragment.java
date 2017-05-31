package com.ozner.cup.Device.WaterPurifier;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.OznerDeviceSettings;
import com.ozner.cup.DBHelper.WaterPurifierAttr;
import com.ozner.cup.Device.DeviceFragment;
import com.ozner.cup.Main.MainActivity;
import com.ozner.cup.R;
import com.ozner.device.OznerDevice;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by ozner_67 on 2017/05/25.
 * 邮箱：xinde.zhang@cftcn.com
 * <p>
 * 水机子类型的容器
 * 继承DeviceFragment，替换MainActivity的Fragment
 * 在这里获取水机实例
 * 请求网络，获取水机类型，根据类型加载不同的界面fragment
 */
public class WPContainerFragment extends DeviceFragment {
    private static final String TAG = "WPContainerFragment";
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    //    private WaterPurifier mWaterPurifer;
    private String mUserid;
    private OznerDeviceSettings oznerSetting;
    private WaterPurifierAttr purifierAttr;
    private WaterNetInfoManager waterNetInfoManager;
    private String mac;
    private DeviceFragment waterFragment;
    private ProgressDialog progressDialog;


    /**
     * 实例化Fragment
     *
     * @param mac
     *
     * @return
     */
    public static WPContainerFragment newInstance(String mac) {
        Log.e(TAG, "newInstance: ");
        WPContainerFragment fragment = new WPContainerFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DeviceAddress, mac);
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate: ");
        mUserid = OznerPreference.GetValue(getContext(), OznerPreference.UserId, "");
        try {
            Bundle bundle = getArguments();
            mac = bundle.getString(DeviceAddress);
            oznerSetting = DBManager.getInstance(getContext()).getDeviceSettings(mUserid, mac);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "onCreate_Ex: " + ex.getMessage());
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView: ");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wpcontainer, container, false);
        ButterKnife.inject(this, view);
        toolbar.setTitle("");
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.e(TAG, "onActivityCreated: ");
        ((MainActivity) getActivity()).initActionBarToggle(toolbar);
        initWaterAttrInfo(oznerSetting.getMac());

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void setDevice(OznerDevice device) {
        Log.e(TAG, "setDevice: ");
        oznerSetting = DBManager.getInstance(getContext()).getDeviceSettings(mUserid, device.Address());
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume: ");
        try {
            setBarColor(R.color.cup_detail_bg);
            setToolbarColor(R.color.cup_detail_bg);
            if (oznerSetting != null) {
                title.setText(oznerSetting.getName());
            } else {
                title.setText(R.string.water_purifier);
            }
        } catch (Exception ex) {

        }
        super.onResume();
    }


    /**
     * 初始化净水器属性信息
     *
     * @param mac
     */
    private void initWaterAttrInfo(String mac) {
        Log.e(TAG, "initWaterAttrInfo: ");
        try {
            purifierAttr = DBManager.getInstance(getContext()).getWaterAttr(mac);
            if (null == waterNetInfoManager) {
                waterNetInfoManager = new WaterNetInfoManager(getContext());
            }
            //获取设备属性
            if (purifierAttr != null && purifierAttr.getDeviceType() != null && !purifierAttr.getDeviceType().isEmpty()) {
                Log.e(TAG, "initWaterAttrInfo: " + purifierAttr.getDeviceType() + " ,hasHot:" + purifierAttr.getHasHot() + " ,hasCool:" + purifierAttr.getHasCool());
                loadDetailView(purifierAttr);
                // TODO: 2017/5/25 根据机型加载相应页面
            } else {
                startLoadingView();
                waterNetInfoManager.getMatchineType(mac, new WaterNetInfoManager.IWaterAttr() {
                    @Override
                    public void onResult(WaterPurifierAttr attr) {
                        loadDetailView(attr);
                    }
                });
            }
        } catch (Exception ex) {
            Log.e(TAG, "initWaterAttrInfo_Ex: " + ex.getMessage());
            loadDetailView(null);
        }
    }


    /**
     * 设置toolbar背景色
     *
     * @param resId
     */
    protected void setToolbarColor(int resId) {
        if (isAdded())
            toolbar.setBackgroundColor(ContextCompat.getColor(getContext(), resId));
    }

    @Override
    protected void refreshUIData() {

    }

    /**
     * 开始加载页面动画
     */
    private void startLoadingView() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("正在加载页面");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
//        Dialog dialog = new Dialog(getContext());
//        dialog.setContentView();
    }

    /**
     * 结束加载页面动画
     */
    private void stopLoadingView() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
        }
    }

    @Override
    public void onAttach(Context context) {
        Log.e(TAG, "onAttach: ");
        super.onAttach(context);
    }

    /**
     * 根据设备类型加载相应界面
     *
     * @param attr
     */
    private void loadDetailView(WaterPurifierAttr attr) {
        Log.e(TAG, "loadDetailView: ");
        stopLoadingView();
//        loadOrgView();
        if (attr != null) {
            if (attr.getDeviceType() != null && !attr.getDeviceType().isEmpty()) {
                // TODO: 2017/5/25 根据设备类型加载相应的布局
                loadEspecialView(attr.getDeviceType());
            } else {
                loadOrgView();
            }
        } else {
            loadOrgView();
        }
    }

    /**
     * 加载特殊类型设备界面
     *
     * @param deviceType
     */
    private void loadEspecialView(String deviceType) {
        switch (deviceType) {
            case "A2B3(SF)":
                Log.e(TAG, "loadEspecialView: A2B3(SF)");
//                loadOrgView();
//                loadA8View();
                loadLGView();
                break;
        }
    }

    /**
     * 加载LG类型设备
     */
    private void loadLGView(){
        Log.e(TAG, "loadLGView: ");
        waterFragment = WPLGFragment.newInstance(mac);
        FragmentTransaction transcation = getChildFragmentManager().beginTransaction();
        transcation.replace(R.id.fg_waterPurifier, waterFragment);
        transcation.commitAllowingStateLoss();
    }

    /**
     * 加载A8类型的设备
     */
    private void loadA8View() {
        Log.e(TAG, "loadA8View: ");
        waterFragment = WPA8Fragment.newInstance(mac);
        FragmentTransaction transcation = getChildFragmentManager().beginTransaction();
        transcation.replace(R.id.fg_waterPurifier, waterFragment);
        transcation.commitAllowingStateLoss();
    }

    /**
     * 加载默认界面，原始布局
     */
    private void loadOrgView() {
        Log.e(TAG, "loadOrgView: ");
        waterFragment = WPNormalFragment.newInstance(mac);
        FragmentTransaction transcation = getChildFragmentManager().beginTransaction();
        transcation.replace(R.id.fg_waterPurifier, waterFragment);
        transcation.commitAllowingStateLoss();
    }
}
