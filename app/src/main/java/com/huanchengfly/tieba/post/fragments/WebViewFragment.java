package com.huanchengfly.tieba.post.fragments;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.components.dialogs.PermissionDialog;
import com.huanchengfly.tieba.post.interfaces.OnReceivedTitleListener;
import com.huanchengfly.tieba.post.interfaces.WebViewListener;
import com.huanchengfly.tieba.post.models.PermissionBean;
import com.huanchengfly.tieba.post.utils.AccountUtil;
import com.huanchengfly.tieba.post.utils.AssetUtil;
import com.huanchengfly.tieba.post.utils.DialogUtil;
import com.huanchengfly.tieba.post.utils.FileUtil;
import com.huanchengfly.tieba.post.utils.NavigationHelper;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.huanchengfly.tieba.post.utils.TiebaLiteJavaScript;
import com.huanchengfly.tieba.post.utils.Util;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.huanchengfly.tieba.post.utils.FileUtil.FILE_TYPE_DOWNLOAD;

//TODO: 代码太烂，需要重写
public class WebViewFragment extends BaseFragment implements DownloadListener {
    public static final String TAG = WebViewFragment.class.getSimpleName();
    private static final String DEFAULT_TITLE = "";
    private final static int FILE_CHOOSER_RESULT_CODE = 1;
    private String mUrl;
    private String mTitle;
    private boolean lazyLoad;
    private boolean enableSwipeRefresh;
    private boolean isSapi;
    private String activityName;
    private String tbliteJs;
    private String nightJs;
    private String aNightJs;
    private String clipboardGuardJs;
    private WebView mWebView;
    private NavigationHelper navigationHelper;
    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;
    private SwipeRefreshLayout swipeRefreshLayout;

    public WebViewFragment() {
    }

    public static WebViewFragment newInstance(String url, String activityName) {
        return newInstance(url, activityName, null);
    }

    public static WebViewFragment newInstance(String url, String activityName, String title) {
        return newInstance(url, activityName, title, false);
    }

    public static WebViewFragment newInstance(String url, String activityName, boolean lazyLoad) {
        return newInstance(url, activityName, null, lazyLoad);
    }

    public static WebViewFragment newInstance(String url, String activityName, @Nullable String title, boolean lazyLoad) {
        return newInstance(url, activityName, title, lazyLoad, true);
    }

    public static WebViewFragment newInstance(String url, String activityName, @Nullable String title, boolean lazyLoad, boolean enableSwipeRefresh) {
        return newInstance(url, activityName, title, lazyLoad, enableSwipeRefresh, false);
    }

    public static WebViewFragment newInstance(String url, String activityName, @Nullable String title, boolean lazyLoad, boolean enableSwipeRefresh, boolean isSapi) {
        WebViewFragment fragment = new WebViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        bundle.putString("title", title == null ? DEFAULT_TITLE : title);
        bundle.putString("activity", activityName);
        bundle.putBoolean("enableSwipeRefresh", enableSwipeRefresh);
        bundle.putBoolean("isSapi", isSapi);
        bundle.putBoolean("lazyLoad", lazyLoad);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
        Util.createSnackbar(mWebView, getAttachContext().getString(R.string.snackbar_download, fileName), Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.button_download, v -> {
                    FileUtil.downloadBySystem(getAttachContext(), FILE_TYPE_DOWNLOAD, url, fileName);
                    Toast.makeText(getAttachContext(), R.string.toast_start_download, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private boolean isEnabledLocationFunction() {
        int locationMode = 0;
        try {
            locationMode = Settings.Secure.getInt(getAttachContext().getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return locationMode != Settings.Secure.LOCATION_MODE_OFF;
    }

    @NonNull
    public WebView getWebView() {
        return mWebView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("url", mUrl);
        outState.putString("title", mTitle);
        outState.putBoolean("lazyLoad", lazyLoad);
        outState.putBoolean("enableSwipeRefresh", enableSwipeRefresh);
        outState.putBoolean("isSapi", isSapi);
        outState.putString("activity", activityName);
        mWebView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mUrl = savedInstanceState.getString("url");
            mTitle = savedInstanceState.getString("title");
            lazyLoad = savedInstanceState.getBoolean("lazyLoad", false);
            enableSwipeRefresh = savedInstanceState.getBoolean("enableSwipeRefresh", true);
            isSapi = savedInstanceState.getBoolean("isSapi", false);
            activityName = savedInstanceState.getString("activity");
            navigationHelper = NavigationHelper.newInstance(getAttachContext());
            mWebView.restoreState(savedInstanceState);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public boolean onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onBackPressed();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mWebView != null) {
            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.removeAllViews();
            mWebView.destroy();
            mWebView = null;
        }
    }

    private void initData() {
        tbliteJs = AssetUtil.getStringFromAsset(getAttachContext(), "tblite.js");
        nightJs = AssetUtil.getStringFromAsset(getAttachContext(), "night.js");
        aNightJs = AssetUtil.getStringFromAsset(getAttachContext(), "anight.js");
        clipboardGuardJs = AssetUtil.getStringFromAsset(getAttachContext(), "ClipboardGuard.js");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        Bundle bundle = getArguments();
        navigationHelper = NavigationHelper.newInstance(getAttachContext());
        if (savedInstanceState == null && bundle != null) {
            mUrl = bundle.getString("url");
            mTitle = bundle.getString("title");
            lazyLoad = bundle.getBoolean("lazyLoad", false);
            enableSwipeRefresh = bundle.getBoolean("enableSwipeRefresh", true);
            isSapi = bundle.getBoolean("isSapi", false);
            activityName = bundle.getString("activity");
        }
    }

    @Override
    protected void onFragmentFirstVisible() {
        if (lazyLoad) {
            mWebView.loadUrl(mUrl);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_web_view;
    }

    @NotNull
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = super.onCreateView(inflater, container, savedInstanceState);
        swipeRefreshLayout = contentView.findViewById(R.id.refresh);
        mWebView = contentView.findViewById(R.id.webView);
        ThemeUtil.setThemeForSwipeRefreshLayout(swipeRefreshLayout);
        swipeRefreshLayout.setEnabled(enableSwipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(() -> getWebView().reload());
        WebSettings webSettings = mWebView.getSettings();
        if (isSapi) {
            webSettings.setUserAgentString(webSettings.getUserAgentString() + " Sapi_8.7.5.1.6_Android_%E7%99%BE%E5%BA%A6%E8%B4%B4%E5%90%A7_9.9.8.40_" + Build.MODEL.replace(" ", "+") + "_" + Build.VERSION.RELEASE + "_Sapi");
        }
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setSupportZoom(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setDomStorageEnabled(true);
        String appCachePath = getAttachContext().getCacheDir().getAbsolutePath();
        webSettings.setAppCachePath(appCachePath);
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        mWebView.setWebChromeClient(new ChromeClient());
        mWebView.setWebViewClient(new Client());
        mWebView.addJavascriptInterface(new TiebaLiteJavaScript(mWebView), "TiebaLiteJsBridge");
        mWebView.setBackgroundColor(Color.TRANSPARENT);
        mWebView.setDownloadListener(this);
        CookieManager.getInstance().setAcceptCookie(true);
        if (!lazyLoad) {
            mWebView.loadUrl(mUrl);
        }
        ThemeUtil.setTranslucentThemeWebViewBackground(mWebView);
        return contentView;
    }

    private void injectJavaScript() {
        if (mWebView == null) return;
        mWebView.evaluateJavascript(clipboardGuardJs, null);
        String nowTheme = ThemeUtil.getTheme(getAttachContext());
        String url = mWebView.getUrl();
        if (url == null || nowTheme == null) {
            return;
        }
        if (mWebView.getUrl().startsWith("http")) {
            mWebView.evaluateJavascript(tbliteJs, (String value) -> {
                if (mWebView != null)
                    mWebView.evaluateJavascript("tblite.init();tblite.theme.init('" + nowTheme + "');", null);
            });
        }
        if (nowTheme.equalsIgnoreCase(ThemeUtil.THEME_AMOLED_DARK)) {
            mWebView.evaluateJavascript(aNightJs, null);
        } else if (nowTheme.equalsIgnoreCase(ThemeUtil.THEME_BLUE_DARK)) {
            mWebView.evaluateJavascript(nightJs, null);
        }
    }

    private void openImageChooserActivity() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, getAttachContext().getString(R.string.title_select_pic)), FILE_CHOOSER_RESULT_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == uploadMessage && null == uploadMessageAboveL) return;
            Uri result = data == null || resultCode != Activity.RESULT_OK ? null : data.getData();
            if (uploadMessageAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (uploadMessage != null) {
                uploadMessage.onReceiveValue(result);
                uploadMessage = null;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null)
            return;
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                String dataString = intent.getDataString();
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
    }

    private class Client extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            if (getAttachContext() instanceof WebViewListener) {
                ((WebViewListener) getAttachContext()).onPageFinished(view, url);
            }
            if (enableSwipeRefresh) swipeRefreshLayout.setRefreshing(false);
            injectJavaScript();
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            return shouldInterceptRequest(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (getAttachContext() instanceof WebViewListener) {
                ((WebViewListener) getAttachContext()).onPageStarted(view, url, favicon);
            }
            if (enableSwipeRefresh) swipeRefreshLayout.setRefreshing(true);
            if (AccountUtil.isLoggedIn(getAttachContext()) && !activityName.startsWith("LoginActivity") && !activityName.startsWith("UpdateInfoActivity")) {
                String cookieStr = AccountUtil.getBdussCookie(getAttachContext());
                CookieManager.getInstance().setCookie(url, cookieStr);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return navigationHelper.interceptWebViewRequest(view, request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return navigationHelper.interceptWebViewRequest(view, url);
        }
    }

    private class ChromeClient extends WebChromeClient {
        @SuppressLint("WrongConstant")
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            Uri uri = Uri.parse(mWebView.getUrl());
            if (uri != null && uri.getHost() != null) {
                new PermissionDialog(getAttachContext(),
                        new PermissionBean(PermissionDialog.CustomPermission.PERMISSION_LOCATION,
                                uri.getHost(),
                                getAttachContext().getString(R.string.title_ask_permission, uri.getHost(), getAttachContext().getString(R.string.permission_name_location)),
                                R.drawable.ic_round_location_on))
                        .setOnGrantedCallback(isForever -> {
                            AndPermission.with(getAttachContext())
                                    .runtime()
                                    .permission(Permission.ACCESS_COARSE_LOCATION, Permission.ACCESS_FINE_LOCATION)
                                    .onGranted((List<String> permissions) -> {
                                        if (isEnabledLocationFunction()) {
                                            callback.invoke(origin, true, isForever);
                                        } else {
                                            callback.invoke(origin, false, false);
                                        }
                                    })
                                    .onDenied((List<String> permissions) -> callback.invoke(origin, false, false))
                                    .start();
                        })
                        .setOnDeniedCallback(isForever -> callback.invoke(origin, false, false))
                        .show();
            }
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
            uploadMessageAboveL = filePathCallback;
            openImageChooserActivity();
            return true;
        }

        @Override
        public boolean onJsAlert(WebView webView, String url, String message, JsResult result) {
            DialogUtil.build(webView.getContext())
                    .setMessage(message).setPositiveButton(R.string.button_sure_default, null)
                    .setCancelable(false)
                    .create().show();
            result.confirm();
            return true;
        }

        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            if ("ClipboardGuardCopyRequest".equalsIgnoreCase(message)) {
                Uri uri = Uri.parse(mWebView.getUrl());
                if (uri != null && uri.getHost() != null) {
                    new PermissionDialog(getAttachContext(),
                            new PermissionBean(PermissionDialog.CustomPermission.PERMISSION_CLIPBOARD_COPY,
                                    uri.getHost(),
                                    getAttachContext().getString(R.string.title_ask_permission_clipboard_copy, uri.getHost()),
                                    R.drawable.ic_round_file_copy))
                            .setOnGrantedCallback(isForever -> result.confirm())
                            .setOnDeniedCallback(isForever -> result.cancel())
                            .show();
                }
            } else {
                DialogUtil.build(view.getContext())
                        .setTitle("Confirm")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> result.confirm())
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> result.cancel())
                        .create()
                        .show();
            }
            return true;
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            mTitle = title;
            injectJavaScript();
            if (getAttachContext() instanceof OnReceivedTitleListener) {
                ((OnReceivedTitleListener) getAttachContext()).onReceivedTitle(view, title, view.getUrl());
            }
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress >= 100) {
                if (enableSwipeRefresh) swipeRefreshLayout.setRefreshing(false);
            }
        }
    }
}