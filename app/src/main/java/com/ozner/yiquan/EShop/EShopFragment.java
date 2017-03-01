package com.ozner.yiquan.EShop;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ozner.yiquan.Base.BaseFragment;
import com.ozner.yiquan.Bean.Contacts;
import com.ozner.yiquan.Main.MainActivity;
import com.ozner.yiquan.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by ozner_67 on 2016/11/1.
 * 邮箱：xinde.zhang@cftcn.com
 */

public class EShopFragment extends BaseFragment {
    private static final String TAG = "EShopFragment";
    @InjectView(R.id.wv_webView)
    WebView wvWebView;

    WebSettings settings;
    @InjectView(R.id.pb_progress)
    ProgressBar pbProgress;
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    private String url = "http://www.ozner.net";

    public EShopFragment() {
        // Required empty public constructor
    }

    /**
     * 实例化Fragment
     *
     * @param bundle
     *
     * @return
     */
    public static EShopFragment newInstance(Bundle bundle) {
        EShopFragment fragment = new EShopFragment();
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Bundle bundle = getArguments();
            url = bundle.getString(Contacts.PARMS_URL);
        } catch (Exception ex) {
            Log.e(TAG, "onCreate_Ex: " + ex.getMessage());
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).initActionBarToggle(toolbar);
        initSetting();

        if (null != url && "" != url) {
            wvWebView.loadUrl(url);
            wvWebView.setWebChromeClient(webChromeClient);
            wvWebView.setWebViewClient(webViewClient);
        } else {
            new AlertDialog.Builder(getContext(), AlertDialog.THEME_HOLO_LIGHT).setMessage(getString(R.string.url_null))
                    .setPositiveButton(getString(R.string.ensure), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * 页面是否可以后退
     *
     * @return
     */
    public boolean isWebCanGoBack() {
        return wvWebView.canGoBack();
    }

    /**
     * web页面后退
     */
    public void goBack() {
        if (isWebCanGoBack()) {
            wvWebView.goBack();
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
//        settings.setBuiltInZoomControls(true);//设置支持缩放
        settings.setLoadsImagesAutomatically(true);//支持自动加载图片
        settings.setNeedInitialFocus(true);//调用requestFocus时为webview设置节点
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_eshop, container, false);
        ButterKnife.inject(this, view);
        toolbar.setTitle("");
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        return view;
    }


    /**
     * 设置toolbar背景色
     *
     * @param resId
     */
    protected void setToolbarColor(int resId) {
        if (isAdded()) {
            toolbar.setBackgroundColor(ContextCompat.getColor(getContext(), resId));
        }
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
    }

    @Override
    public void onResume() {
        try {
            setBarColor(R.color.colorAccent);
            setToolbarColor(R.color.colorAccent);
            title.setText(R.string.ozner_eshop);
        } catch (Exception ex) {

        }
        super.onResume();
    }

    /**
     * 设置状态栏颜色
     */
    protected void setBarColor(int resId) {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = ((MainActivity) getActivity()).getWindow();
            //更改状态栏颜色
            window.setStatusBarColor(ContextCompat.getColor(getContext(), resId));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
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
            if (EShopFragment.this.isAdded()) {
                pbProgress.setVisibility(View.VISIBLE);
            }
            settings.setBlockNetworkImage(true);
//            pb_progress.setVisibility(View.VISIBLE);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (EShopFragment.this.isAdded()) {
                pbProgress.setVisibility(View.GONE);
            }
            settings.setBlockNetworkImage(false);
//            pb_progress.setVisibility(View.GONE);
            super.onPageFinished(view, url);
        }
    };

    WebChromeClient webChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (EShopFragment.this.isAdded()) {
                pbProgress.setProgress(newProgress);
            }
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String webTitle) {
            if (webTitle != null && webTitle != "" && isAdded()) {
                title.setText(webTitle);
            }
            super.onReceivedTitle(view, webTitle);
        }
    };
}
