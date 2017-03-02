package com.ozner.cup;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.ozner.application.OznerBLEService;

/**
 * Created by ozner_67 on 2016/11/1.
 * 邮箱：xinde.zhang@cftcn.com
 */

public abstract class OznerBaseApplication extends Application {
    OznerBLEService.OznerBLEBinder localService = null;
    ServiceConnection mServiceConnection = null;

    public OznerBLEService.OznerBLEBinder getService() {
        return localService;
    }

    /**
     * 服务初始化完成时调用的方法
     */
    protected abstract void onBindService();

    @Override
    public void onCreate() {
        super.onCreate();
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                try {
                    localService = (OznerBLEService.OznerBLEBinder) service;
                    onBindService();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                localService = null;
            }
        };

        Intent intent = new Intent(this, OznerBLEService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    public void onTerminate() {
        unbindService(mServiceConnection);
        super.onTerminate();
    }

}
