package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.content.Intent
import android.webkit.CookieManager
import android.widget.Toast
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.models.database.Account
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.zip
import org.litepal.LitePal.findAll
import org.litepal.LitePal.where

object AccountUtil {
    const val TAG = "AccountUtil"
    const val ACTION_SWITCH_ACCOUNT = "com.huanchengfly.tieba.post.action.SWITCH_ACCOUNT"

    @JvmStatic
    fun getLoginInfo(context: Context): Account? {
        val loginUser =
            context.getSharedPreferences("accountData", Context.MODE_PRIVATE).getInt("now", -1)
        return if (loginUser == -1) {
            null
        } else getAccountInfo(loginUser)
    }

    val allAccounts: List<Account>
        get() = findAll(Account::class.java)

    private fun getAccountInfo(accountId: Int): Account {
        return where("id = ?", accountId.toString()).findFirst(Account::class.java)
    }

    @JvmStatic
    fun getAccountInfoByUid(uid: String): Account? {
        return where("uid = ?", uid).findFirst(Account::class.java)
    }

    @JvmStatic
    fun getAccountInfoByBduss(bduss: String): Account {
        return where("bduss = ?", bduss).findFirst(Account::class.java)
    }

    @JvmStatic
    fun isLoggedIn(context: Context): Boolean {
        return getLoginInfo(context) != null
    }

    @JvmStatic
    fun switchUser(context: Context, id: Int): Boolean {
        context.sendBroadcast(Intent().setAction(ACTION_SWITCH_ACCOUNT))
        return context.getSharedPreferences("accountData", Context.MODE_PRIVATE).edit()
            .putInt("now", id).commit()
    }

    fun fetchAccountFlow(context: Context): Flow<Account> {
        return TiebaApi.getInstance()
            .initNickNameFlow()
            .zip(TiebaApi.getInstance().loginFlow()) { initNickNameBean, loginBean ->
                getLoginInfo(context)!!.apply {
                    uid = loginBean.user.id
                    name = loginBean.user.name
                    nameShow = initNickNameBean.userInfo.nameShow
                    portrait = loginBean.user.portrait
                    tbs = loginBean.anti.tbs
                    saveOrUpdate("uid = ?", loginBean.user.id)
                }
            }
    }

    fun fetchAccountFlow(account: Account): Flow<Account> {
        return TiebaApi.getInstance()
            .initNickNameFlow(
                account.bduss,
                account.sToken
            )
            .zip(
                TiebaApi.getInstance().loginFlow(
                    account.bduss,
                    account.sToken
                )
            ) { initNickNameBean, loginBean ->
                account.apply {
                    uid = loginBean.user.id
                    name = loginBean.user.name
                    nameShow = initNickNameBean.userInfo.nameShow
                    portrait = loginBean.user.portrait
                    tbs = loginBean.anti.tbs
                    saveOrUpdate("uid = ?", loginBean.user.id)
                }
            }
    }

    fun fetchAccountFlow(
        bduss: String,
        sToken: String,
        cookie: String? = null
    ): Flow<Account> {
        return TiebaApi.getInstance()
            .initNickNameFlow(bduss, sToken)
            .zip(TiebaApi.getInstance().loginFlow(bduss, sToken)) { initNickNameBean, loginBean ->
                Account(
                    loginBean.user.id,
                    loginBean.user.name,
                    bduss,
                    loginBean.anti.tbs,
                    loginBean.user.portrait,
                    sToken,
                    cookie ?: getBdussCookie(bduss),
                    initNickNameBean.userInfo.nameShow,
                    "",
                    "0"
                )
            }
    }

    @JvmStatic
    fun updateLoginInfo(cookie: String): Boolean {
        val bdussSplit = cookie.split("BDUSS=").toTypedArray()
        val sTokenSplit = cookie.split("STOKEN=").toTypedArray()
        if (bdussSplit.size > 1 && sTokenSplit.size > 1) {
            val bduss = bdussSplit[1].split(";").toTypedArray()[0]
            val sToken = sTokenSplit[1].split(";").toTypedArray()[0]
            val account = getAccountInfoByBduss(bduss)
            if (account != null) {
                account.apply {
                    this.sToken = sToken
                    this.cookie = cookie
                }.update(account.id.toLong())
                return true
            }
        }
        return false
    }

    fun exit(context: Context) {
        var accounts = allAccounts
        var account = getLoginInfo(context) ?: return
        account.delete()
        CookieManager.getInstance().removeAllCookies(null)
        if (accounts.size > 1) {
            accounts = allAccounts
            account = accounts[0]
            switchUser(context, account.id)
            Toast.makeText(context, "退出登录成功，已切换至账号 " + account.nameShow, Toast.LENGTH_SHORT).show()
            return
        }
        context.getSharedPreferences("accountData", Context.MODE_PRIVATE).edit().clear().commit()
        Toast.makeText(context, R.string.toast_exit_account_success, Toast.LENGTH_SHORT).show()
    }

    fun getSToken(context: Context?): String? {
        if (context == null) return null
        val account = getLoginInfo(context)
        return account?.sToken
    }

    fun getCookie(context: Context?): String? {
        if (context == null) return null
        val account = getLoginInfo(context)
        return account?.cookie
    }

    fun getUid(context: Context?): String? {
        if (context == null) return null
        val account = getLoginInfo(context)
        return account?.uid
    }

    fun getBduss(context: Context?): String? {
        if (context == null) return null
        val account = getLoginInfo(context)
        return account?.bduss
    }

    @JvmStatic
    fun getBdussCookie(context: Context?): String? {
        if (context == null) return null
        val bduss = getBduss(context)
        return if (bduss != null) {
            getBdussCookie(bduss)
        } else null
    }

    fun getBdussCookie(bduss: String): String {
        return "BDUSS=$bduss; path=/; domain=.baidu.com; httponly"
    }
}