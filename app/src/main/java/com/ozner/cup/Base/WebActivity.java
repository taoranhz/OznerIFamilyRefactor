package com.ozner.cup.Base;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.R;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class WebActivity extends BaseActivity {
    private static final String TAG = "WebActivity";
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.wv_webView)
    WebView wvWebView;
    @InjectView(R.id.pb_progress)
    ProgressBar pbProgress;

    private String url = "";
    WebSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        ButterKnife.inject(this);
        initToolBar();
        initSetting();
        try {
            url = getIntent().getStringExtra(Contacts.PARMS_URL);
            Log.e(TAG, "onCreate: url:" + url);
            if (null != url && !url.isEmpty()) {
                wvWebView.loadUrl(url);
                wvWebView.setWebChromeClient(webChromeClient);
                wvWebView.setWebViewClient(webViewClient);
            } else {
                new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT)
                        .setMessage(getString(R.string.url_null))
                        .setPositiveButton(getString(R.string.ensure), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        } catch (Exception ex) {
            Log.e(TAG, "onCreate_Ex: " + ex.getMessage());
        }
    }

    /**
     * 初始化ToolBar
     */
    private void initToolBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (wvWebView != null && wvWebView.canGoBack()) {
            wvWebView.goBack();
        } else {
            this.finish();
        }
    }

    /**
     * 初始化Web设置
     */
    private void initSetting() {
        settings = wvWebView.getSettings();
//        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);//优先使用缓存
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);//优先不使用缓存
        settings.setJavaScriptEnabled(true);//javascript 支持
        settings.setUseWideViewPort(true);//将图片调整到适合webview的大小
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);//支持页面重新布局
        settings.setLoadWithOverviewMode(true);//缩放至屏幕大小
        settings.setBuiltInZoomControls(true);//设置支持缩放
        settings.setLoadsImagesAutomatically(true);//支持自动加载图片
        settings.setNeedInitialFocus(true);//调用requestFocus时为webview设置节点
    }

    WebViewClient webViewClient = new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return super.shouldOverrideKeyEvent(view, event);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            pbProgress.setVisibility(View.VISIBLE);
            settings.setBlockNetworkImage(true);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            pbProgress.setVisibility(View.GONE);
            settings.setBlockNetworkImage(false);
            super.onPageFinished(view, url);
        }
    };

    WebChromeClient webChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            pbProgress.setProgress(newProgress);
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String titleStr) {
            try {
                if (titleStr != null && !titleStr.isEmpty()) {
                    title.setText(titleStr);
                }
            } catch (Exception ex) {
                Log.e(TAG, "onReceivedTitle_Ex: " + ex.getMessage());
            }
            super.onReceivedTitle(view, titleStr);
        }
    };

}
