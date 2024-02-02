package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.content.Intent
import android.webkit.CookieManager
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.LoginBean
import com.huanchengfly.tieba.post.arch.GlobalEvent
import com.huanchengfly.tieba.post.arch.emitGlobalEvent
import com.huanchengfly.tieba.post.models.database.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import org.litepal.LitePal
import org.litepal.LitePal.findAll
import org.litepal.LitePal.where
import org.litepal.extension.findAllAsync
import org.litepal.extension.findFirst
import java.util.UUID

@Stable
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

    @JvmStatic
    fun <T> getAccountInfo(getter: Account.() -> T): T? {
        return currentAccount?.getter()
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
        return where("uid = ?", uid).findFirst<Account>()
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
    fun switchAccount(context: Context, id: Int): Boolean {
        context.sendBroadcast(Intent().setAction(ACTION_SWITCH_ACCOUNT))
        val account = runCatching { getAccountInfo(id) }.getOrNull() ?: return false
        mutableCurrentAccountState.value = account
        GlobalScope.launch {
            emitGlobalEvent(GlobalEvent.AccountSwitched)
        }
        return context.getSharedPreferences("accountData", Context.MODE_PRIVATE).edit()
            .putInt("now", id).commit()
    }

    private fun updateAccount(
        account: Account,
        loginBean: LoginBean,
    ) {
        account.apply {
            uid = loginBean.user.id
            name = loginBean.user.name
            portrait = loginBean.user.portrait
            tbs = loginBean.anti.tbs
            if (uuid.isNullOrBlank()) uuid = UUID.randomUUID().toString()
        }
    }

    fun fetchAccountFlow(account: Account = getLoginInfo()!!): Flow<Account> {
        return fetchAccountFlow(account.bduss, account.sToken, account.cookie)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun fetchAccountFlow(
        bduss: String,
        sToken: String,
        cookie: String? = null
    ): Flow<Account> {
        return TiebaApi.getInstance()
            .loginFlow(bduss, sToken)
            .zip(TiebaApi.getInstance().initNickNameFlow(bduss, sToken)) { loginBean, _ ->
                getAccountInfoByUid(loginBean.user.id)?.apply {
                    this.bduss = bduss
                    this.sToken = sToken
                    this.cookie = cookie ?: getBdussCookie(bduss)
                    updateAccount(this, loginBean)
                } ?: Account(
                    loginBean.user.id,
                    loginBean.user.name,
                    bduss,
                    loginBean.anti.tbs,
                    loginBean.user.portrait,
                    sToken,
                    cookie ?: getBdussCookie(bduss),
                )
            }
            .zip(SofireUtils.fetchZid()) { account, zid ->
                account.apply { this.zid = zid }
            }
            .flatMapConcat { account ->
                TiebaApi.getInstance()
                    .getUserInfoFlow(account.uid.toLong(), account.bduss, account.sToken)
                    .map { checkNotNull(it.data_?.user) }
                    .map { user ->
                        account.apply {
                            nameShow = user.nameShow
                            portrait = user.portrait
                        }
                    }
                    .catch {
                        emit(account)
                    }
            }
            .onEach { account ->
                account.saveOrUpdateAsync("uid = ?", account.uid)
                    .listen {
                        LitePal.findAllAsync<Account>()
                            .listen {
                                mutableAllAccountsState.value = it
                            }
                    }
            }
            .flowOn(Dispatchers.IO)
    }

    fun parseCookie(cookie: String): Map<String, String> {
        return cookie
            .split(";")
            .map { it.trim().split("=") }
            .filter { it.size > 1 }
            .associate { it.first() to it.drop(1).joinToString("=") }
    }

    @JvmStatic
    fun updateLoginInfo(cookie: String): Boolean {
        val cookies = parseCookie(cookie).mapKeys { it.key.uppercase() }
        val bduss = cookies["BDUSS"]
        val sToken = cookies["STOKEN"]
        if (bduss != null && sToken != null) {
            val account = getAccountInfoByBduss(bduss)
            account.apply {
                this.sToken = sToken
                this.cookie = cookie
            }.update(account.id.toLong())
            return true
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
            switchAccount(context, account.id)
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
        return "BDUSS=$bduss; Path=/; Max-Age=315360000; Domain=.baidu.com; Httponly"
    }
}