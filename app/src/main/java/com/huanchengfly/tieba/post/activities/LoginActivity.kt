package com.huanchengfly.tieba.post.activities

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.snackbar.Snackbar
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.fragments.WebViewFragment
import com.huanchengfly.tieba.post.interfaces.WebViewListener
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.ClientUtils
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity(), WebViewListener {
    private var isLoadingAccount = false

    private var toolbar: Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background))
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setTitle(R.string.title_activity_login)
        }
        if (savedInstanceState == null) {
            val mWebViewFragment = WebViewFragment.newInstance(
                "https://wappass.baidu.com/passport?login&u=https%3A%2F%2Ftieba.baidu.com%2Findex%2Ftbwise%2Fmine",
                "LoginActivity"
            )
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, mWebViewFragment, "WebViewFragment")
                .commit()
        }
    }

    override fun setTitle(newTitle: String?) {
        toolbar!!.title = newTitle
    }

    // 从 Cookies 字符串解析出每个 Cookie
    fun parseCookies(cookies: String): Map<String, String> {
        val cookieMap = mutableMapOf<String, String>()
        cookies.split(";").forEach {
            val cookie = it.trim()
            val cookieSplit = cookie.split("=").toMutableList()
            if (cookieSplit.size > 1) {
                val name = cookieSplit.removeFirst()
                cookieMap[name] = cookieSplit.joinToString("=")
            }
        }
        return cookieMap
    }

    override fun onPageFinished(view: WebView, url: String) {
        if (isLoadingAccount) {
            return
        }
        val cookieManager = CookieManager.getInstance()
        val cookiesStr = cookieManager.getCookie(url) ?: ""
        val cookies = parseCookies(cookiesStr)
        val bduss = cookies["BDUSS"]
        val sToken = cookies["STOKEN"]
        val baiduId = cookies["BAIDUID"]
        if (url.startsWith("https://tieba.baidu.com/index/tbwise/") || url.startsWith("https://tiebac.baidu.com/index/tbwise/")) {
            if (bduss == null || sToken == null) {
                return
            }
            if (!baiduId.isNullOrEmpty() && ClientUtils.baiduId.isNullOrEmpty()) launch {
                ClientUtils.saveBaiduId(
                    this@LoginActivity,
                    baiduId
                )
            }
            val snackBar = Util.createSnackbar(view, "请稍后…", Snackbar.LENGTH_INDEFINITE)
            isLoadingAccount = true
            snackBar.show()
            launch {
                AccountUtil.fetchAccountFlow(bduss, sToken, cookiesStr)
                    .flowOn(Dispatchers.IO)
                    .catch { e ->
                        snackBar.setText("登录失败 ${e.getErrorMessage()}")
                        isLoadingAccount = false
                        view.loadUrl("https://wappass.baidu.com/passport?login&u=https%3A%2F%2Ftieba.baidu.com%2Findex%2Ftbwise%2Fmine")
                        handler.postDelayed({ snackBar.dismiss() }, 1500)
                    }
                    .flowOn(Dispatchers.Main)
                    .collect { account ->
                        AccountUtil.newAccount(account.uid, account) {
                            isLoadingAccount = false
                            if (it) {
                                AccountUtil.switchUser(this@LoginActivity, account.id)
                                snackBar.setText("登录成功，即将跳转")
                            } else {
                                snackBar.setText("登录失败 未知错误")
                                view.loadUrl("https://wappass.baidu.com/passport?login&u=https%3A%2F%2Ftieba.baidu.com%2Findex%2Ftbwise%2Fmine")
                                handler.postDelayed({ snackBar.dismiss() }, 1500)
                            }
                        }
                    }
            }
        }
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {}

    companion object {
        val TAG: String = LoginActivity::class.java.simpleName
        private val handler = Handler()
    }
}