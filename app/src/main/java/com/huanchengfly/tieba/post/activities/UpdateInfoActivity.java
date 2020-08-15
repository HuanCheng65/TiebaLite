package com.huanchengfly.tieba.post.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.CookieManager;
import android.webkit.WebView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.fragments.WebViewFragment;
import com.huanchengfly.tieba.post.interfaces.WebViewListener;
import com.huanchengfly.tieba.post.utils.AccountUtil;
import com.huanchengfly.tieba.post.utils.DialogUtil;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.huanchengfly.tieba.post.utils.Util;

public class UpdateInfoActivity extends BaseActivity implements WebViewListener {
    public static final int ACTION_UPDATE_LOGIN_INFO = 0;
    private static final String EXTRA_ACTION = "action";
    private static Handler handler = new Handler();
    private Toolbar toolbar;
    private int action;
    private WebViewFragment mWebViewFragment;

    public static Intent newIntent(Context context, int action) {
        return new Intent(context, UpdateInfoActivity.class).putExtra(EXTRA_ACTION, action);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background));
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        action = getIntent().getIntExtra(EXTRA_ACTION, ACTION_UPDATE_LOGIN_INFO);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (action == ACTION_UPDATE_LOGIN_INFO) {
                actionBar.setTitle(R.string.title_update_stoken);
            } else {
                finish();
            }
        }
        if (savedInstanceState == null) {
            mWebViewFragment = WebViewFragment.newInstance("https://wappass.baidu.com/passport?login&u=https%3A%2F%2Ftieba.baidu.com%2Findex%2Ftbwise%2Fmine", "UpdateInfoActivity", false);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main, mWebViewFragment, "WebViewFragment")
                    .commit();
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (action == ACTION_UPDATE_LOGIN_INFO) {
            updateLoginInfo(url);
        }
    }

    private void updateLoginInfo(String url) {
        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(url);
        if (cookies != null && AccountUtil.updateLoginInfo(cookies)) {
            Snackbar snackbar = Util.createSnackbar(mWebViewFragment.getWebView(), "更新成功，即将跳转", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
            handler.postDelayed(() -> {
                snackbar.dismiss();
                finish();
            }, 1500);
        } else {
            DialogUtil.build(this)
                    .setTitle("出现问题")
                    .setMessage("看起来您还没有登录或登录已失效，请先登录")
                    .setPositiveButton(R.string.button_sure_default, (dialog, which) -> {
                        finish();
                        startActivity(new Intent(this, LoginActivity.class));
                    })
                    .create()
                    .show();
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
    }
}
