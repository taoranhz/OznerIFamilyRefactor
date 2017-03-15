
package com.ozner.cup.Device.ReplenWater.RemindUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.ozner.cup.R;
import com.ozner.cup.Utils.LCLogUtils;


public class NotifyActivity extends Activity {
    private static final String TAG = "NotifyActivity";
    int reqCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);
        reqCode = getIntent().getIntExtra(RemindUtil.PARMS_REQ_CODE, 0);
        LCLogUtils.E(TAG, "reqCode:" + reqCode);
        showNotify();
    }

    private void showNotify() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        builder.setTitle(R.string.time_to_moisturizing);
        builder.setPositiveButton(R.string.ensure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });
        builder.show();
    }
}
