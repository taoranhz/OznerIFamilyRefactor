package com.ozner.cup.MyCenter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.ozner.cup.HttpHelper.ApiException;
import com.ozner.cup.HttpHelper.HttpMethods;
import com.ozner.cup.HttpHelper.ProgressSubscriber;
import com.ozner.cup.R;
import com.ozner.cup.Utils.LCLogUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import rx.functions.Action1;

/**
 * Created by ozner_67 on 2017/1/4.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class OznerUpdateManager {
    private static final String TAG = "OznerUpdateManager";

    private static final int DOWN_UPDATE = 5;
    private static final int DOWN_OVER = 6;
    private WeakReference<Context> mContext;
    private String folderPath;//文件下载路径
    private String downLoadUrl;//下载链接
    private int isMustUpdate;//是否必须更新
    private int netVersionCode;//最新版本号
    private String installPackagename;//下载文件名
    private boolean isShowMsg = false;
    private boolean isChecking = false;
    private Thread downLoadThread;
    private int progress;
    private boolean interceptFlag = false;
    private ProgressBar mProgress;
    private TextView tv_loadper;
    private AlertDialog mDownloadDialog;

    public OznerUpdateManager(Context context, boolean isShowToastmsg) {
        this.isShowMsg = isShowToastmsg;
        this.mContext = new WeakReference<Context>(context);
        this.folderPath = Environment.getExternalStorageDirectory() + "/Download/";
        LCLogUtils.E(TAG, "folderPaht:" + folderPath);
    }

    private void showCenterToast(String msg) {
        Toast toast = Toast.makeText(mContext.get(), msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * 检查软件更新
     */
    public void checkUpdate() {
        if (!isChecking) {
            isChecking = true;
            getNetVersion();
        } else {
            showCenterToast(mContext.get().getString(R.string.version_checking));
        }
    }

    /**
     * 获取网络版本号,并判断是否需要更新
     */
    private void getNetVersion() {
        HttpMethods.getInstance().getNewVersion("android",
                new ProgressSubscriber<JsonObject>(mContext.get(), new Action1<JsonObject>() {
                    @Override
                    public void call(JsonObject jsonObject) {
                        isChecking = false;
                        try {
                            if (jsonObject != null) {
                                if (jsonObject.get("state").getAsInt() > 0) {
                                    JsonObject data = jsonObject.get("data").getAsJsonObject();
                                    downLoadUrl = data.get("url").getAsString();
                                    isMustUpdate = data.get("mustupdata").getAsInt();
                                    netVersionCode = data.get("versioncode").getAsInt();
                                    String updateContent = data.get("updatecon").getAsString();
                                    String netVersionName = data.get("versionname").getAsString();
                                    installPackagename = downLoadUrl.substring(downLoadUrl.lastIndexOf("/") + 1);
                                    if (netVersionCode > getVersionCode(mContext.get())) {
                                        showNoticeDialog(updateContent);
                                    } else {
                                        if (isShowMsg) {
                                            showCenterToast(mContext.get().getString(R.string.latest_version));
                                        }
                                    }
                                } else {
                                    showCenterToast(mContext.get().getString(ApiException.getErrResId(jsonObject.get("state").getAsInt())));
                                }
                            } else {
                                if (isShowMsg) {
                                    showCenterToast("未获取到版本信息");
                                }
                            }
                        } catch (Exception ex) {
                            LCLogUtils.E(TAG, "getNetVersion_Ex:" + ex.getMessage());
                            if (isShowMsg) {
                                showCenterToast(ex.getMessage());
                            }
                        }
                    }
                }));
    }

    /**
     * 获取软件版本号
     *
     * @param context
     *
     * @return
     */
    private int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            // 获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            LCLogUtils.E(TAG, "updateManage:curVersion: " + versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 显示软件更新对话框
     *
     * @param updateMsg
     */
    private void showNoticeDialog(String updateMsg) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext.get(), AlertDialog.THEME_HOLO_LIGHT);
            builder.setTitle(R.string.soft_update_title);
            builder.setMessage(R.string.soft_update_info);
            builder.setPositiveButton(R.string.soft_update, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    showDownLoadDialog();
                }
            });

            builder.setNegativeButton(R.string.soft_update_later, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
        } catch (Exception ex) {
            LCLogUtils.E(TAG, "showNoticeDialog_Ex:" + ex.getMessage());
        }
    }

    /**
     * 显示下载对话框
     */
    private void showDownLoadDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext.get(), AlertDialog.THEME_HOLO_LIGHT);
            builder.setTitle(R.string.soft_updating);
            View view = LayoutInflater.from(mContext.get()).inflate(R.layout.update_dialog_progress, null);
            mProgress = (ProgressBar) view.findViewById(R.id.update_progress);
            tv_loadper = (TextView) view.findViewById(R.id.tv_loadper);
            builder.setView(view);
            mDownloadDialog = builder.create();
            mDownloadDialog.setCancelable(false);
            mDownloadDialog.setCanceledOnTouchOutside(false);
            mDownloadDialog.show();
            downloadApk();
        } catch (Exception ex) {
            LCLogUtils.E(TAG, "showDownLoadDialog_Ex:" + ex.getMessage());
        }
    }


    /*
  *下载文件
   */
    private void downloadApk() {
        Log.e(TAG, "downloadApk");
        downLoadThread = new Thread(downloadRunnable);
        downLoadThread.start();
    }


    private Runnable downloadRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                URL url = new URL(downLoadUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                int length = conn.getContentLength();
                InputStream is = conn.getInputStream();

                File file = new File(folderPath);
                if (!file.exists()) {
                    file.mkdir();
                }
                File ApkFile = new File(file, installPackagename);
                ApkFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(ApkFile);

                BufferedInputStream bis = new BufferedInputStream(is);

                int count = 0;
                byte buf[] = new byte[1024];

                do {
                    int numread = bis.read(buf);
                    count += numread;
                    progress = (int) (((float) count / length) * 100);
                    //更新进度
                    mhandler.sendEmptyMessage(DOWN_UPDATE);
                    if (numread <= 0) {
                        //下载完成通知安装
                        mhandler.sendEmptyMessage(DOWN_OVER);
                        break;
                    }
                    fos.write(buf, 0, numread);
                    fos.flush();
                }
                while (!interceptFlag);//点击取消就停止下载.

                fos.close();
                is.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DOWN_UPDATE:
                    mProgress.setProgress(progress);
                    tv_loadper.setText(progress + "%");
                    break;
                case DOWN_OVER:
                    LCLogUtils.E(TAG, "下载完成");
                    if (mDownloadDialog != null)
                        mDownloadDialog.cancel();
                    installApk();
                    break;
            }
        }
    };

    /**
     * 安装apk
     *
     * @param
     */
    private void installApk() {
        File apkfile = new File(folderPath + "/" + installPackagename);
        if (!apkfile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        Log.e("tag", "installApk: Slience:" + Uri.parse("file://" + apkfile.toString()));
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        mContext.get().startActivity(i);

    }

}
