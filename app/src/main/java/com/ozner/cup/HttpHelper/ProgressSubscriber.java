package com.ozner.cup.HttpHelper;

import android.content.Context;

import rx.Subscriber;

/**
 * Created by ozner_67 on 2016/11/2.
 * 邮箱：xinde.zhang@cftcn.com
 * <p>
 * 用于在Http请求开始时，自动显示一个ProgressDialog
 * 在Http请求结束时，关闭ProgressDialog
 */

public class ProgressSubscriber<JsonObject> extends Subscriber<JsonObject> implements ProgressCancelListener {
    private Context mContext;
//        private Action1<JsonObject> nextListener;
    private OznerHttpResult httpResult;
    private ProgressDialogHandler progressHandler;
    private String dialogMsg;

//    public ProgressSubscriber(Context context, String dialogMsg, boolean dialogCancelable, Action1<JsonObject> nextListener) {
//        this.nextListener = nextListener;
//        this.mContext = context;
//        this.dialogMsg = dialogMsg;
//        progressHandler = new ProgressDialogHandler(context, dialogMsg, this, dialogCancelable);
//    }

    public ProgressSubscriber(Context context, String dialogMsg, boolean dialogCancelable, OznerHttpResult httpResult) {
        this.httpResult = httpResult;
        this.mContext = context;
        this.dialogMsg = dialogMsg;
        progressHandler = new ProgressDialogHandler(context, dialogMsg, this, dialogCancelable);
    }

    /**
     * 没有加载框的网络请求
     *
     * @param context
     * @param nextListener
     */
//    public ProgressSubscriber(Context context, Action1<JsonObject> nextListener) {
//        this.nextListener = nextListener;
//        this.mContext = context;
////        progressHandler = new ProgressDialogHandler(context, dialogMsg, this, false);
//    }
    public ProgressSubscriber(Context context, OznerHttpResult nextListener) {
        this.httpResult = nextListener;
        this.mContext = context;
//        progressHandler = new ProgressDialogHandler(context, dialogMsg, this, false);
    }

    private void showProgressDialog() {
        if (progressHandler != null) {
            progressHandler.obtainMessage(ProgressDialogHandler.SHOW_PROGRESS_DIALOG).sendToTarget();
        }
    }

    private void dismissProgressDialog() {
        if (progressHandler != null) {
            progressHandler.obtainMessage(ProgressDialogHandler.DISMISS_PROGRESS_DIALOG).sendToTarget();
            progressHandler = null;
        }
    }

    @Override
    public void onStart() {
        showProgressDialog();
    }

    @Override
    public void onCompleted() {
        dismissProgressDialog();
//        Toast.makeText(mContext, "Get Top Movie Completed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(Throwable e) {
//        if (e instanceof SocketTimeoutException) {
//            Toast.makeText(mContext, mContext.getString(R.string.err_net_outline), Toast.LENGTH_SHORT).show();
//        } else if (e instanceof ConnectException) {
//            Toast.makeText(mContext, mContext.getString(R.string.err_net_outline), Toast.LENGTH_SHORT).show();
//        } else {
//            try {
//                Log.e("ProgressSubscriber", "onError: " + e.getMessage());
//                Toast toast = Toast.makeText(mContext, "error:" + mContext.getString(ApiException.getErrResId(Integer.parseInt(e.getMessage()))), Toast.LENGTH_SHORT);
//                toast.setGravity(Gravity.CENTER, 0, 0);
//                toast.show();
//            } catch (Exception ex) {
//                Log.e("ProgressSubscriber", "onError_Ex: " + ex.getMessage());
//            }
//        }

        if (httpResult != null) {
            httpResult.onError(e);
        }

        dismissProgressDialog();
    }

    @Override
    public void onNext(JsonObject t) {
        if (httpResult != null) {
            httpResult.onNext(t);
        }
//        if (nextListener != null) {
//            nextListener.call(t);
//        }
    }

    @Override
    public void onCancelProgress() {
        if (!this.isUnsubscribed()) {
            this.unsubscribe();
        }
    }
}
