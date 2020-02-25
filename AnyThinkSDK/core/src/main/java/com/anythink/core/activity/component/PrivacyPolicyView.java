package com.anythink.core.activity.component;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anythink.core.api.ATSDK;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.utils.CommonUtil;

/**
 * Created by Z on 2018/5/3.
 * GDPR view
 */

public class PrivacyPolicyView extends RelativeLayout implements View.OnClickListener {
    private static String TAG = PrivacyPolicyView.class.getSimpleName();
    ViewGroup mPolicyContentView;
    LinearLayout mLoadingView;
    LoadingView mLoadingImageView;
    TextView mLoadingTextView;

    FrameLayout mWebviewArea;
    WebView mPolicyWebView;
    CheckBox mRecommendCheckBox;
    View mAgreeView;
    TextView mRejectView;


    boolean mIsWebViewloadSuccess = true;
    boolean mIsLoading = false;

    public PrivacyPolicyView(Context context) {
        super(context);
        init();

    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(CommonUtil.getResId(getContext(), "privace_policy_layout", "layout"), this);
        mPolicyContentView = findViewById(CommonUtil.getResId(getContext(), "policy_content_view", "id"));

        mLoadingView = findViewById(CommonUtil.getResId(getContext(), "policy_loading_view", "id"));
        mLoadingImageView = new LoadingView(getContext());
        LinearLayout.LayoutParams loadingImageParam = new LinearLayout.LayoutParams(CommonUtil.dip2px(getContext(), 30), CommonUtil.dip2px(getContext(), 30));
        loadingImageParam.gravity = Gravity.CENTER_HORIZONTAL;
        mLoadingImageView.setLayoutParams(loadingImageParam);

        mLoadingTextView = new TextView(getContext());
        LinearLayout.LayoutParams loadingTextParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingTextParam.gravity = Gravity.CENTER_HORIZONTAL;
        loadingTextParam.topMargin = CommonUtil.dip2px(getContext(), 5);
        mLoadingTextView.setLayoutParams(loadingTextParam);

        mLoadingTextView.setText("Page failed to load, please try again later.");
        mLoadingTextView.setTextColor(0xff777777);
        mLoadingTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);

        mLoadingView.addView(mLoadingImageView);
        mLoadingView.addView(mLoadingTextView);


        mLoadingView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                loadPolicyUrl(mUrl);
                try {
                    if (mPolicyWebView != null && !mIsLoading) {
                        mIsWebViewloadSuccess = true;
                        Log.d(TAG, "reload.......");
                        loadPolicyUrl(mUrl);

                    }
                } catch (Throwable e) {
                    if (Const.DEBUG) {
                        e.printStackTrace();
                    }
                }

            }
        });

        mWebviewArea = findViewById(CommonUtil.getResId(getContext(), "policy_webview_area", "id"));


        mPolicyWebView = new WebView(getContext());
        mWebviewArea.addView(mPolicyWebView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        initPolicyWebView();

        mRecommendCheckBox = findViewById(CommonUtil.getResId(getContext(), "policy_check_box", "id"));
        mAgreeView = findViewById(CommonUtil.getResId(getContext(), "policy_agree_view", "id"));
        mRejectView = findViewById(CommonUtil.getResId(getContext(), "policy_reject_view", "id"));

        mAgreeView.setOnClickListener(this);
        mRejectView.setOnClickListener(this);

        int roundRadiusBtn = CommonUtil.dip2px(getContext(), 20);
        GradientDrawable agreeBgbtn = new GradientDrawable();
        if (Const.SYSTEM == 1) {
            agreeBgbtn.setColor(0xff326df4);
        } else {
            agreeBgbtn.setColor(0xff60C185);
        }

        agreeBgbtn.setCornerRadius(roundRadiusBtn);
        mAgreeView.setBackgroundDrawable(agreeBgbtn);

        mRejectView.setText(Html.fromHtml("<u>" + "No,Thanks" + "</u>"));

    }

    private void initPolicyWebView() {
        WebSettings setting = mPolicyWebView.getSettings();
        if (setting != null) {
            setting.setAllowFileAccess(true);
            setting.setJavaScriptEnabled(false);
            setting.setAppCacheEnabled(true);
//            setting.setAppCacheMaxSize(1024 * 1024 * 1);
//            setting.setAllowFileAccess(true);
            setting.setBuiltInZoomControls(true);
            setting.setJavaScriptCanOpenWindowsAutomatically(true);
            setting.setDomStorageEnabled(true);
            setting.setSupportZoom(false);
            setting.setSavePassword(false);
            setting.setDatabaseEnabled(false);
//            setting.setLoadWithOverviewMode(true);
            setting.setRenderPriority(WebSettings.RenderPriority.HIGH);
            setting.setPluginState(WebSettings.PluginState.ON);
            setting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
            // setting.setBlockNetworkImage(true);
            // enable database

//            setting.setJavaScriptEnabled(true);
//            setting.setJavaScriptCanOpenWindowsAutomatically(true);
//            setting.setPluginState(WebSettings.PluginState.ON);
            // settings.setPluginsEnabled(true);
            setting.setAllowFileAccess(true);
            setting.setLoadWithOverviewMode(true);
            setting.setUseWideViewPort(true);

        }

        mPolicyWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d(TAG, "onPageStarted：" + url);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                try {
                    Log.d(TAG, "onPageFinished：" + url + "   mIsWebViewloadSuccess:" + mIsWebViewloadSuccess);
                    if (mUrl.equals(url)) {
                        if (mIsWebViewloadSuccess) {
                            mLoadingView.setVisibility(INVISIBLE);
                            mPolicyContentView.setVisibility(VISIBLE);

                            mLoadingView.setVisibility(GONE);
                            mLoadingImageView.clearAnimation();
                        } else {
                            mLoadingView.setVisibility(VISIBLE);
                            mLoadingImageView.clearAnimation();
                            mLoadingTextView.setVisibility(VISIBLE);
                            mPolicyContentView.setVisibility(GONE);
                        }

                        mIsLoading = false;
                        super.onPageFinished(view, url);
                    }
                } catch (Throwable e) {
                    if (Const.DEBUG) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(TAG, "shouldOverrideUrlLoading：" + url);
                if (!TextUtils.isEmpty(url)) {
                    openBrowser(getContext(), url);
                    return true;
                }
                return false;
            }


            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                mIsWebViewloadSuccess = false;
                Log.d(TAG, "onPageFinished：" + error.getErrorCode());
                super.onReceivedError(view, request, error);

            }

        });

        mPolicyWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                if (!TextUtils.isEmpty(title) && title.toLowerCase().contains("error")) {
                    mIsWebViewloadSuccess = false;
                }
                super.onReceivedTitle(view, title);
            }
        });

    }

    String mUrl;

    public void loadPolicyUrl(String url) {

        if (mIsLoading) {
            return;
        }

        mUrl = url;
        if (CommonUtil.isNetConnect(getContext())) {
            mIsWebViewloadSuccess = true;
            mLoadingView.setVisibility(VISIBLE);
            mLoadingImageView.clearAnimation();
            mLoadingImageView.startAnimation();
            mLoadingTextView.setVisibility(GONE);
            mIsLoading = true;
            if (mUrl.equals(mPolicyWebView.getUrl())) {
                mPolicyWebView.reload();
            } else {
                mPolicyWebView.loadUrl(mUrl);
            }
        } else {
            mIsWebViewloadSuccess = false;
            mLoadingView.setVisibility(VISIBLE);
            mLoadingImageView.clearAnimation();
            mLoadingTextView.setVisibility(VISIBLE);
            mPolicyContentView.setVisibility(GONE);
        }

    }

    private void openBrowser(Context context, String url) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void destory() {
        try {

            removeAllViews();

            if (mPolicyContentView != null) {
                mPolicyContentView.removeAllViews();
            }

            if (mWebviewArea != null) {
                mWebviewArea.removeView(mPolicyWebView);
                mPolicyWebView.removeAllViews();
            }

            if (mPolicyWebView != null) {
                mPolicyWebView.clearHistory();
                mPolicyWebView.clearCache(true);
//                mPolicyWebView.loadUrl("about:blank"); // clearView() should be changed to loadUrl("about:blank"), since clearView() is deprecated now
//                mPolicyWebView.freeMemory();
//                mPolicyWebView.pauseTimers();
                mPolicyWebView.destroy();
                mPolicyWebView = null; // Note that mWebView.destroy() and mWebView = null do the exact same thing
            }
        } catch (Throwable e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }

    }

    OnClickListener mClickCallbackListener;

    public void setClickCallbackListener(OnClickListener clickCallbackListener) {
        mClickCallbackListener = clickCallbackListener;
    }

    @Override
    public void onClick(View view) {
        if (view == mAgreeView) {
            ATSDK.setGDPRUploadDataLevel(getContext(), ATSDK.PERSONALIZED);
            view.setTag(ATSDK.PERSONALIZED);
        } else if (view == mRejectView) {
            ATSDK.setGDPRUploadDataLevel(getContext(), ATSDK.NONPERSONALIZED);
            view.setTag(ATSDK.NONPERSONALIZED);
        }


        mClickCallbackListener.onClick(view);
    }
}
