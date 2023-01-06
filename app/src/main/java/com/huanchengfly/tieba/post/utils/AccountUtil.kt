package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.content.Intent
import android.webkit.CookieManager
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
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

    val LocalAccount = staticCompositionLocalOf<Account?> { null }
    val AllAccounts = staticCompositionLocalOf<List<Account>> { emptyList() }

    @Composable
    fun LocalAccountProvider(content: @Composable () -> Unit) {
        val account by mutableCurrentAccountState
        val allAccounts by mutableAllAccountsState
        CompositionLocalProvider(
            LocalAccount provides account,
            AllAccounts provides allAccounts
        ) {
            content()
        }
    }

    @set:Synchronized
    private var mutableCurrentAccountState: MutableState<Account?> = mutableStateOf(null)

    private var mutableAllAccountsState: MutableState<List<Account>> = mutableStateOf(emptyList())

    val currentAccount
        get() = mutableCurrentAccountState.value

    val allAccounts: List<Account>
        get() = mutableAllAccountsState.value

    fun init(context: Context) {
        val account = runCatching {
            val loginUser =
                context.getSharedPreferences("accountData", Context.MODE_PRIVATE).getInt("now", -1)
            if (loginUser == -1) {
                null
            } else getAccountInfo(loginUser)
        }.getOrNull()
        mutableCurrentAccountState.value = account
        mutableAllAccountsState.value = findAll(Account::class.java)
    }

    @JvmStatic
    fun getLoginInfo(): Account? {
        return currentAccount
    }

    fun newAccount(uid: String, account: Account, callback: (Boolean) -> Unit) {
        account.saveOrUpdateAsync("uid = ?", uid).listen {
            mutableAllAccountsState.value = findAll(Account::class.java)
            callback(it)
        }
    }

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
    fun isLoggedIn(): Boolean {
        return getLoginInfo() != null
    }

    @JvmStatic
    fun switchUser(context: Context, id: Int): Boolean {
        context.sendBroadcast(Intent().setAction(ACTION_SWITCH_ACCOUNT))
        val account = runCatching { getAccountInfo(id) }.getOrNull() ?: return false
        mutableCurrentAccountState.value = account
        return context.getSharedPreferences("accountData", Context.MODE_PRIVATE).edit()
            .putInt("now", id).commit()
    }

    fun fetchAccountFlow(): Flow<Account> {
        return TiebaApi.getInstance()
            .initNickNameFlow()
            .zip(TiebaApi.getInstance().loginFlow()) { initNickNameBean, loginBean ->
                getLoginInfo()!!.apply {
                    uid = loginBean.user.id
                    name = loginBean.user.name
                    nameShow = initNickNameBean.userInfo.nameShow
                    portrait = loginBean.user.portrait
                    tbs = loginBean.anti.tbs
                    saveOrUpdate("uid = ?", loginBean.user.id)
                    mutableAllAccountsState.value = findAll(Account::class.java)
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
                    mutableAllAccountsState.value = findAll(Account::class.java)
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
                getAccountInfoByUid(loginBean.user.id) ?: Account(
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
        val bdussSplit = cookie.split("BDUSS=")
        val sTokenSplit = cookie.split("STOKEN=")
        if (bdussSplit.size > 1 && sTokenSplit.size > 1) {
            val bduss = bdussSplit[1].split(";")[0]
            val sToken = sTokenSplit[1].split(";")[0]
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
        var account = getLoginInfo() ?: return
        account.delete()
        CookieManager.getInstance().removeAllCookies(null)
        if (accounts.size > 1) {
            accounts = allAccounts
            account = accounts[0]
            switchUser(context, account.id)
            Toast.makeText(context, "退出登录成功，已切换至账号 " + account.nameShow, Toast.LENGTH_SHORT).show()
            return
        }
        mutableCurrentAccountState.value = null
        context.getSharedPreferences("accountData", Context.MODE_PRIVATE).edit().clear().commit()
        Toast.makeText(context, R.string.toast_exit_account_success, Toast.LENGTH_SHORT).show()
    }

    fun getSToken(): String? {
        val account = getLoginInfo()
        return account?.sToken
    }

    fun getCookie(): String? {
        val account = getLoginInfo()
        return account?.cookie
    }

    fun getUid(): String? {
        val account = getLoginInfo()
        return account?.uid
    }

    fun getBduss(): String? {
        val account = getLoginInfo()
        return account?.bduss
    }

    @JvmStatic
    fun getBdussCookie(): String? {
        val bduss = getBduss()
        return if (bduss != null) {
            getBdussCookie(bduss)
        } else null
    }

    fun getBdussCookie(bduss: String): String {
        return "BDUSS=$bduss; path=/; domain=.baidu.com; httponly"
    }
}