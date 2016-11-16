package com.ozner.cup.Device;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ozner.cup.Device.AddDevice.AddDeviceActivity;
import com.ozner.cup.Main.MainActivity;
import com.ozner.cup.R;
import com.ozner.device.OznerDevice;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


/**
 *
 */
public class NoDeviceFragment extends DeviceFragment {

    @InjectView(R.id.tv_add_device)
    TextView tvAddDevice;
    @InjectView(R.id.iv_top_bg)
    ImageView ivTopBg;
    @InjectView(R.id.iv_top_logo)
    ImageView ivTopLogo;

    public NoDeviceFragment() {
        // Required empty public constructor
    }

    /**
     * 实例化Fragment
     *
     * @return
     */
    public static DeviceFragment newInstance() {
        NoDeviceFragment fragment = new NoDeviceFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_no_device, container, false);
        ButterKnife.inject(this, view);

        Glide.with(this).load(R.drawable.tds_detail_bg).asBitmap().into(ivTopBg);
        Glide.with(this).load(R.drawable.logo).asBitmap().into(ivTopLogo);
        return view;
    }


    @Override
    public void setDevice(OznerDevice device) {

    }

    @Override
    protected void refreshUIData() {

    }

    @Override
    public void onAttach(Context context) {
        ((MainActivity) context).setCustomTitle(getString(R.string.add_device));
        super.onAttach(context);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @OnClick(R.id.tv_add_device)
    public void onClick() {
        startActivity(new Intent(getContext(), AddDeviceActivity.class));
    }
}
