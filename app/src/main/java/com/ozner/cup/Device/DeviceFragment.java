package com.ozner.cup.Device;

import com.ozner.cup.Base.BaseFragment;
import com.ozner.device.OznerDevice;


/**
 * Created by ozner_67 on 2016/11/1.
 * 邮箱：xinde.zhang@cftcn.com
 */

public abstract class DeviceFragment extends BaseFragment {
    public final static String DeviceAddress = "device_address";

    public abstract void setDevice(OznerDevice device);

    protected abstract void refreshUIData();

}
