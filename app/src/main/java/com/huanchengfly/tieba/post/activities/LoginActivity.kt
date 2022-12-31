package com.huanchengfly.tieba.post.activities

import android.content.Intent
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
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity(), WebViewListener {
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

    override fun onPageFinished(view: WebView, url: String) {
        val cookieManager = CookieManager.getInstance()
        val cookies = cookieManager.getCookie(url)
        if (cookies != null) {
            val bdussSplit = cookies.split("BDUSS=")
            val sTokenSplit = cookies.split("STOKEN=")
            if (bdussSplit.size > 1 && sTokenSplit.size > 1) {
                val bduss = bdussSplit[1].split(";")[0]
                val sToken = sTokenSplit[1].split(";")[0]
                if (url.startsWith("https://tieba.baidu.com/index/tbwise/") || url.startsWith("https://tiebac.baidu.com/index/tbwise/")) {
                    val snackBar = Util.createSnackbar(view, "请稍后…", Snackbar.LENGTH_INDEFINITE)
                    snackBar.show()
                    launch {
                        AccountUtil.fetchAccountFlow(bduss, sToken, cookies)
                            .flowOn(Dispatchers.IO)
                            .catch { e ->
                                snackBar.setText("登录失败 ${e.getErrorMessage()}")
                                view.loadUrl("https://wappass.baidu.com/passport?login&u=https%3A%2F%2Ftieba.baidu.com%2Findex%2Ftbwise%2Fmine")
                                handler.postDelayed({ snackBar.dismiss() }, 1500)
                            }
                            .flowOn(Dispatchers.Main)
                            .collect { account ->
                                AccountUtil.newAccount(account.uid, account) {
                                    if (it) {
                                        AccountUtil.switchUser(this@LoginActivity, account.id)
                                        snackBar.setText("登录成功，即将跳转")
                                        handler.postDelayed({
                                            snackBar.dismiss()
                                            finish()
                                            startActivity(
                                                Intent(
                                                    this@LoginActivity,
                                                    UpdateInfoActivity::class.java
                                                )
                                            )
                                        }, 1500)
                                    } else {
                                        snackBar.setText("登录失败 未知错误")
                                    }
                                }
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