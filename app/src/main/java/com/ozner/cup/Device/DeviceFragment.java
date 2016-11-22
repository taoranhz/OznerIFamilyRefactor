package com.ozner.cup.Device;

import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Toast;

import com.ozner.cup.Base.BaseFragment;
import com.ozner.device.OznerDevice;


/**
 * Created by ozner_67 on 2016/11/1.
 * 邮箱：xinde.zhang@cftcn.com
 */

public abstract class DeviceFragment extends BaseFragment {
    public final static String DeviceAddress = "device_address";
    protected RotateAnimation rotateAnimation;

    public abstract void setDevice(OznerDevice device);

    protected abstract void refreshUIData();

    /**
     * 初始化动画
     */
    protected void initAnimation() {
        rotateAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setRepeatCount(-1);
        LinearInterpolator li = new LinearInterpolator();
        rotateAnimation.setInterpolator(li);
        rotateAnimation.setFillAfter(false);
        rotateAnimation.setDuration(1000);
    }

    /**
     * 在中间弹出提示信息
     *
     * @param resId
     */
    public void showCenterToast(int resId) {
        Toast toast = Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


}
