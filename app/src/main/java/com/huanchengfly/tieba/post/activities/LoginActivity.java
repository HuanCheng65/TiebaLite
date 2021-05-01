package com.huanchengfly.tieba.post.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.api.interfaces.CommonCallback;
import com.huanchengfly.tieba.post.fragments.WebViewFragment;
import com.huanchengfly.tieba.post.interfaces.WebViewListener;
import com.huanchengfly.tieba.post.models.MyInfoBean;
import com.huanchengfly.tieba.post.models.database.Account;
import com.huanchengfly.tieba.post.utils.AccountUtil;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.huanchengfly.tieba.post.utils.Util;

public class LoginActivity extends BaseActivity implements WebViewListener {
    public static final String TAG = LoginActivity.class.getSimpleName();

    private static Handler handler = new Handler();
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background));
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_login);
        }
        if (savedInstanceState == null) {
            WebViewFragment mWebViewFragment = WebViewFragment.newInstance("https://wappass.baidu.com/passport?login&u=https%3A%2F%2Ftieba.baidu.com%2Findex%2Ftbwise%2Fmine", "LoginActivity");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main, mWebViewFragment, "WebViewFragment")
                    .commit();
        }
    }

    @Override
    public void setTitle(String newTitle) {
        toolbar.setTitle(newTitle);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(url);
        Log.i(TAG, "onPageFinished: " + cookies);
        if (cookies != null) {
            String[] bdussSplit = cookies.split("BDUSS=");
            if (bdussSplit.length > 1) {
                String bduss = bdussSplit[1].split(";")[0];
                Log.i(TAG, "onPageFinished: " + bduss);
                if (url.startsWith("https://tieba.baidu.com/index/tbwise/") || url.startsWith("https://tiebac.baidu.com/index/tbwise/")) {
                    Snackbar snackbar = Util.createSnackbar(view, "请稍后…", Snackbar.LENGTH_INDEFINITE);
                    snackbar.show();
                    AccountUtil.updateUserInfoByBduss(bduss, new CommonCallback<MyInfoBean>() {
                        @Override
                        public void onSuccess(MyInfoBean data) {
                            Account account = AccountUtil.getLoginInfo(LoginActivity.this);
                            if (account == null) {
                                account = AccountUtil.getAccountInfoByBduss(bduss);
                            }
                            if (account == null) {
                                account = AccountUtil.getAccountInfoByUid(String.valueOf(data.getData().getUid()));
                            }
                            if (account != null) {
                                AccountUtil.switchUser(LoginActivity.this, account.getId());
                                snackbar.setText("登录成功，即将跳转");
                                handler.postDelayed(() -> {
                                    snackbar.dismiss();
                                    finish();
                                    startActivity(new Intent(LoginActivity.this, UpdateInfoActivity.class));
                                }, 1500);
                            } else {
                                snackbar.setText("登录失败 未知错误");
                            }
                        }

                        @Override
                        public void onFailure(int code, String error) {
                            snackbar.setText("登录失败，无法获取用户信息 " + error);
                            view.loadUrl("https://wappass.baidu.com/passport?login&u=https%3A%2F%2Ftieba.baidu.com%2Findex%2Ftbwise%2Fmine");
                            handler.postDelayed(snackbar::dismiss, 1500);
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
    }
}
