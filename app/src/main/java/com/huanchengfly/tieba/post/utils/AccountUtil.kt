package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import android.webkit.CookieManager
import android.widget.Toast
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.Error
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.interfaces.CommonCallback
import com.huanchengfly.tieba.post.api.retrofit.ApiResult
import com.huanchengfly.tieba.post.api.retrofit.doIfSuccess
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaException
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaLocalException
import com.huanchengfly.tieba.post.api.retrofit.isSuccessful
import com.huanchengfly.tieba.post.models.MyInfoBean
import com.huanchengfly.tieba.post.models.database.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.litepal.LitePal.findAll
import org.litepal.LitePal.where
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    fun getAccountInfo(accountId: Int): Account {
        return where("id = ?", accountId.toString()).findFirst(Account::class.java)
    }

    @JvmStatic
    fun getAccountInfoByUid(uid: String): Account {
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

    fun newAccount(context: Context, account: Account, needSwitch: Boolean): Boolean {
        return if (account.save()) {
            if (needSwitch) {
                switchUser(context, account.id)
            } else true
        } else false
    }

    @JvmStatic
    fun switchUser(context: Context, id: Int): Boolean {
        context.sendBroadcast(Intent().setAction(ACTION_SWITCH_ACCOUNT))
        return context.getSharedPreferences("accountData", Context.MODE_PRIVATE).edit()
            .putInt("now", id).commit()
    }

    fun updateUserInfo(context: Context, commonCallback: CommonCallback<MyInfoBean>) {
        val account = getLoginInfo(context)
        if (account == null) {
            commonCallback.onFailure(Error.ERROR_NOT_LOGGED_IN, "未登录")
            return
        }
        updateUserInfoByBduss(account.bduss, commonCallback)
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
                account.setsToken(sToken)
                    .setCookie(cookie)
                    .update(account.id.toLong())
                return true
            }
        }
        return false
    }

    suspend fun updateUserInfoAsync(
        coroutineScope: CoroutineScope,
        bduss: String
    ): Deferred<ApiResult<MyInfoBean>> {
        return coroutineScope.async {
            var result = TiebaApi.getInstance()
                .myInfoAsync(getBdussCookie(bduss))
                .await()
            Log.i("AccountUtil", "updateUserInfo finish success:${result.isSuccessful}")
            result.doIfSuccess {
                if (!it.data.isLogin()) {
                    result = ApiResult.Failure(
                        TiebaLocalException(
                            Error.ERROR_LOGGED_IN_EXPIRED,
                            "登录已过期"
                        )
                    )
                }
                val userId = it.data.getUid().toString()
                Account().setBduss(bduss)
                    .setPortrait(it.data.getAvatarUrl())
                    .setUid(userId)
                    .setTbs(it.data.getTbs())
                    .setItbTbs(it.data.getItbTbs())
                    .setName(it.data.getName())
                    .setNameShow(it.data.getShowName())
                    .saveOrUpdate("uid = ? OR bduss = ?", userId, bduss)
            }
            result
        }
    }

    @JvmStatic
    fun updateUserInfoByBduss(bduss: String, commonCallback: CommonCallback<MyInfoBean>?) {
        TiebaApi.getInstance().myInfo(getBdussCookie(bduss)).enqueue(object : Callback<MyInfoBean> {
            override fun onResponse(call: Call<MyInfoBean>, response: Response<MyInfoBean>) {
                val myInfoBean = response.body()
                if (myInfoBean == null) {
                    commonCallback?.onFailure(Error.ERROR_UNKNOWN, "未知错误")
                    return
                }
                if (!myInfoBean.data.isLogin()) {
                    commonCallback?.onFailure(Error.ERROR_LOGGED_IN_EXPIRED, "登录已过期")
                    return
                }
                val userId = myInfoBean.data.getUid().toString()
                Account().setBduss(bduss)
                    .setPortrait(myInfoBean.data.getAvatarUrl())
                    .setUid(userId)
                    .setTbs(myInfoBean.data.getTbs())
                    .setItbTbs(myInfoBean.data.getItbTbs())
                    .setName(myInfoBean.data.getName())
                    .setNameShow(myInfoBean.data.getShowName())
                    .saveOrUpdate("uid = ? OR bduss = ?", userId, bduss)
                commonCallback?.onSuccess(myInfoBean)
            }

            override fun onFailure(call: Call<MyInfoBean>, t: Throwable) {
                if (commonCallback != null) {
                    if (t is TiebaException) {
                        commonCallback.onFailure(t.code, t.message)
                    } else {
                        commonCallback.onFailure(-1, t.message)
                    }
                }
            }
        })
    }

    fun exit(context: Context) {
        var accounts = allAccounts
        var account = getLoginInfo(context)
        if (account == null) return
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
        return account?.getsToken()
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