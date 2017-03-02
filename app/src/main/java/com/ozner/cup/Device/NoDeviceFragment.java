package com.ozner.cup.Device;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
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
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

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
        toolbar.setTitle("");
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        Glide.with(this).load(R.drawable.tds_detail_bg).asBitmap().into(ivTopBg);
        Glide.with(this).load(R.drawable.logo).asBitmap().into(ivTopLogo);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).initActionBarToggle(toolbar);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void setDevice(OznerDevice device) {

    }

    @Override
    protected void refreshUIData() {

    }

    @Override
    public void onResume() {
        try {
            setBarColor(R.color.cup_detail_bg);
//            setToolbarColor(R.color.cup_detail_bg);
            toolbar.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.cup_detail_bg));
//            ((MainActivity) getActivity()).setCustomTitle(getString(R.string.add_device));
            title.setText(R.string.add_device);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        System.gc();
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @OnClick(R.id.tv_add_device)
    public void onClick() {
        startActivity(new Intent(getContext(), AddDeviceActivity.class));
//        ((MainActivity)getActivity()).refreshBottomBadge(3);
//        ((MainActivity)getActivity()).hideBottomNav();
    }
}
