package com.ozner.cup.Device.AirPurifier;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.ozner.cup.R;

import java.lang.ref.WeakReference;

/**
 * Created by ozner_67 on 2016/11/18.
 * 邮箱：xinde.zhang@cftcn.com
 * <p>
 * 空气净化器模式设置弹出窗口
 */

public class AirModePopWindow extends PopupWindow {
    private static final String TAG = "AirModePopWindow";
    private WeakReference<Context> mContext;
    private IModeClick listener;


    interface IModeClick {
        void onModeClick(int mode);
    }

    public AirModePopWindow(Context context) {
        super(context);
        mContext = new WeakReference<Context>(context);
        initView();
    }

    private void initView() {
        View rootView = LayoutInflater.from(mContext.get()).inflate(R.layout.air_mode_pop_layout, null);
        setContentView(rootView);
        setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setOutsideTouchable(true);

        rootView.findViewById(R.id.rlay_AutoSwitch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: Auto");
            }
        });
        rootView.findViewById(R.id.rlay_StrongSwitch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: Strong");
            }
        });
        rootView.findViewById(R.id.rlay_SlientSwitch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: Slient");
            }
        });
    }


}
