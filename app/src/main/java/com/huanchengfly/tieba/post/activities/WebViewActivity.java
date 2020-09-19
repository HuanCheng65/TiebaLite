package com.huanchengfly.tieba.post.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.fragments.WebViewFragment;
import com.huanchengfly.tieba.post.interfaces.OnReceivedTitleListener;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.huanchengfly.tieba.post.utils.TiebaUtil;

public class WebViewActivity extends BaseActivity implements OnReceivedTitleListener {
    public static final String TAG = WebViewActivity.class.getSimpleName();

    public static final String DOMAIN_SAPI = "wappass.baidu.com";
    public static final String EXTRA_URL = "url";
    private static final String DOMAIN_TIEBA = "tieba.baidu.com";

    private WebViewFragment mWebViewFragment;
    private Toolbar toolbar;
    private String mUrl;

    public static void launch(Context context, String url) {
        context.startActivity(newIntent(context, url));
    }

    public static Intent newIntent(Context context, String url) {
        return new Intent(context, WebViewActivity.class).putExtra(EXTRA_URL, url);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background));
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        mUrl = intent.getStringExtra(EXTRA_URL);
        mWebViewFragment = WebViewFragment.newInstance(mUrl, TAG, null, false, true, mUrl.contains(DOMAIN_SAPI));
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, mWebViewFragment, TAG)
                .commit();
    }

    @Override
    public void setTitle(String newTitle) {
        toolbar.setTitle(newTitle);
    }

    @Override
    public void setSubTitle(String newSubTitle) {
        toolbar.setSubtitle(newSubTitle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_open_in_browser:
                Uri uri = Uri.parse(mWebViewFragment.getWebView().getUrl() == null ?
                        mUrl :
                        mWebViewFragment.getWebView().getUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            case R.id.menu_copy_link:
                TiebaUtil.copyText(this, mWebViewFragment.getWebView().getUrl());
                break;
            case R.id.menu_refresh:
                mWebViewFragment.getWebView().reload();
                break;
            case R.id.menu_share:
                TiebaUtil.shareText(this, mWebViewFragment.getWebView().getUrl());
                break;
            case R.id.menu_exit:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_webview_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onReceivedTitle(WebView view, String title, String url) {
        setTitle(title);
        Uri uri = Uri.parse(url);
        String host = uri.getHost();
        if (host != null && host.contains(DOMAIN_TIEBA)) {
            setSubTitle(null);
        } else {
            setSubTitle(host);
        }
    }
}