package com.ozner.wifi.mxchip;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by ozner_67 on 2017/6/28.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class ThreadManager {
    private static final ThreadManager ourInstance = new ThreadManager();

    private ExecutorService mExeccutor = null;

    public static ThreadManager getInstance() {
        return ourInstance;
    }

    private ThreadManager() {
        mExeccutor = Executors.newFixedThreadPool(4);
    }

    public void execute(Runnable runnable) {
        mExeccutor.execute(runnable);
    }

    public void submit(Runnable runnable) {
        mExeccutor.submit(runnable);
    }

    public <T> Future<T> submit(Callable<T> callable) {
        return mExeccutor.submit(callable);
    }

    public void shutdown(){
        if(mExeccutor!=null&&mExeccutor.isShutdown()){
            mExeccutor.shutdown();
        }
    }

    public void shutdownNow(){
        if(mExeccutor!=null&&mExeccutor.isShutdown()){
            mExeccutor.shutdownNow();
        }
    }

    public boolean isShutdown(){
        return mExeccutor.isShutdown();
    }
}
