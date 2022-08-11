package com.huanchengfly.tieba.post.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.widget.Toolbar
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.urlDecode
import com.huanchengfly.tieba.post.fragments.WebViewFragment
import com.huanchengfly.tieba.post.fromJson
import com.huanchengfly.tieba.post.interfaces.OnOverrideUrlLoadingListener
import com.huanchengfly.tieba.post.interfaces.OnReceivedTitleListener
import com.huanchengfly.tieba.post.models.ModifyNicknameResult
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.ThemeUtil.setTranslucentThemeBackground
import com.huanchengfly.tieba.post.utils.TiebaUtil.copyText
import com.huanchengfly.tieba.post.utils.TiebaUtil.shareText

class WebViewActivity : BaseActivity(), OnReceivedTitleListener, OnOverrideUrlLoadingListener {
    private var mWebViewFragment: WebViewFragment? = null
    private var toolbar: Toolbar? = null
    private var mUrl: String? = null

    private var isModifyNickname = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isModifyNickname = intent.getBooleanExtra("is_modify_nickname", false)
        setContentView(R.layout.activity_webview)
        setTranslucentThemeBackground(findViewById(R.id.background))
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        val intent = intent
        val title = intent.getStringExtra("title")
        if (title != null) {
            setTitle(title)
        }
        mUrl = intent.getStringExtra(EXTRA_URL).also { url ->
            if (url != null) {
                mWebViewFragment = WebViewFragment.newInstance(
                    url,
                    TAG,
                    title,
                    false,
                    true,
                    url.contains(DOMAIN_SAPI)
                ).also {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main, it, TAG)
                        .commit()
                }
            } else {
                finish()
            }
        }
    }

    override fun setTitle(newTitle: String?) {
        toolbar!!.title = newTitle
    }

    override fun setSubTitle(newTitle: String?) {
        toolbar!!.subtitle = newTitle
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_open_in_browser -> {
                val uri =
                    Uri.parse(if (mWebViewFragment!!.webView.url == null) mUrl else mWebViewFragment!!.webView.url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            R.id.menu_copy_link -> copyText(this, mWebViewFragment!!.webView.url)
            R.id.menu_refresh -> mWebViewFragment!!.webView.reload()
            R.id.menu_share -> shareText(this, mWebViewFragment!!.webView.url!!)
            R.id.menu_exit -> finish()
            else -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!isModifyNickname) menuInflater.inflate(R.menu.menu_webview_toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onReceivedTitle(view: WebView, title: String, url: String) {
        setTitle(title)
        val uri = Uri.parse(url)
        val host = uri.host
        if (host != null && host.contains(DOMAIN_TIEBA)) {
            setSubTitle(null)
        } else {
            setSubTitle(host)
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView, urlString: String?): Boolean {
        if (urlString == null) {
            return false
        }
        val uri = Uri.parse(urlString)
        if (uri.scheme == "modifyname") {
            val result = urlString.split("=")[1].urlDecode().fromJson<ModifyNicknameResult>()
            Log.i(TAG, "shouldOverrideUrlLoading $result")
            if (result.isClose == 1) {
                val intent = Intent().apply {
                    putExtra("result", result)
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
            return true
        }
        return false
    }

    companion object {
        val TAG: String = WebViewActivity::class.java.simpleName
        const val DOMAIN_SAPI = "wappass.baidu.com"
        const val EXTRA_URL = "url"
        private const val DOMAIN_TIEBA = "tieba.baidu.com"

        fun launch(context: Context, urlString: String?) {
            var url = urlString ?: return
            val uri = Uri.parse(url)
            if (uri.path == "/mo/q/checkurl") {
                url = uri.getQueryParameter("url").toString()
                url = url.replace("http://https://", "https://")
            }
            context.startActivity(newIntent(context, url))
        }

        fun newIntent(context: Context, url: String?): Intent {
            return Intent(context, WebViewActivity::class.java).putExtra(EXTRA_URL, url)
        }
    }

    class ModifyNicknameResultContract : ActivityResultContract<Nothing?, ModifyNicknameResult>() {
        override fun createIntent(context: Context, input: Nothing?): Intent =
            newIntent(
                context,
                "https://tieba.baidu.com/n/interact/modifyname${if (ThemeUtil.isNightMode()) "?isNightModel=1" else ""}"
            ).putExtra("is_modify_nickname", true)
                .putExtra("title", context.getString(R.string.title_modify_nickname))

        override fun parseResult(resultCode: Int, intent: Intent?): ModifyNicknameResult {
            val data = intent?.getParcelableExtra<ModifyNicknameResult>("result")
            return if (resultCode == Activity.RESULT_OK && data != null) data
            else ModifyNicknameResult(isClose = 0, nickname = null)
        }
    }
}