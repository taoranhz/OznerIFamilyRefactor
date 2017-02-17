package com.ozner.cup.Base;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.LoginWelcom.View.LoginActivity;
import com.ozner.cup.R;

/**
 * Created by ozner_67 on 2016/11/1.
 * 邮箱：xinde.zhang@cftcn.com
 */

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        initBarColor();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }
//
//    public abstract void setCustomTitle(int titleResId);
//
//    public abstract void setCustomTitle(String title);

    /**
     * 初始化导航栏颜色
     */
    private void initBarColor() {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent));
            //更改底部导航栏颜色(限有底部的手机)
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.colorAccent));
        }
    }

    /**
     * 在屏幕中间显示提示
     *
     * @param msg
     */
    public void showToastCenter(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * 在屏幕中间显示提示
     *
     * @param resId
     */
    public void showToastCenter(int resId) {
        Toast toast = Toast.makeText(this, resId, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * 获取控件宽度
     *
     * @param view
     *
     * @return
     */
    public int[] getMeasuredWidth(View view) {
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
        int[] size = new int[2];
        size[0] = view.getMeasuredWidth();
        size[1] = view.getMeasuredHeight();
        return size;
    }


    /**
     * 重新登录
     */
    public static void reLogin(final Activity activity) {
        new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_LIGHT)
                .setTitle(R.string.sign_out_tip)
                .setMessage(R.string.login_on_other_device)
                .setPositiveButton(R.string.ensure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UserDataPreference.SetUserData(activity, UserDataPreference.UserId, "");
                        OznerPreference.setUserToken(activity, "");
                        activity.startActivity(new Intent(activity.getApplicationContext(), LoginActivity.class));
                        activity.finishAffinity();
                    }
                }).show();
    }

    public int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
