package com.ozner.cup.Device.Tap.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ozner.cup.R;

/**
 * Created by ozner_67 on 2016/11/8.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class FoundDeviceHolder extends RecyclerView.ViewHolder {
    public ImageView iv_deviceIcon;
    public TextView tv_deviceName;
    public RadioButton rb_check;
    public LinearLayout llay_root;

    public FoundDeviceHolder(View itemView) {
        super(itemView);
        this.iv_deviceIcon = (ImageView) itemView.findViewById(R.id.iv_device_Icon);
        this.tv_deviceName = (TextView) itemView.findViewById(R.id.tv_device_Name);
        this.rb_check = (RadioButton) itemView.findViewById(R.id.rb_check);
        this.llay_root = (LinearLayout) itemView.findViewById(R.id.llay_root);
    }
}
