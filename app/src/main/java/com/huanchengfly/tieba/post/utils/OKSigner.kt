package com.huanchengfly.tieba.post.utils

import android.content.Context
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.SignResultBean
import com.huanchengfly.tieba.post.api.retrofit.*
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorCode
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.models.SignDataBean
import com.huanchengfly.tieba.post.models.database.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import java.lang.ref.WeakReference
import java.util.concurrent.ThreadLocalRandom
import kotlin.properties.Delegates

abstract class IOKSigner(
    val coroutineScope: CoroutineScope,
    context: Context
) {
    private val contextWeakReference: WeakReference<Context> = WeakReference(context)

    val context: Context
        get() = contextWeakReference.get()!!

    abstract suspend fun start(): Boolean

    suspend fun sign(signDataBean: SignDataBean): ApiResult<SignResultBean> {
        return TiebaApi.getInstance()
            .signAsync(signDataBean.forumName, signDataBean.tbs)
            .await()
    }

    fun getSignDelay(): Long {
        return if (context.appPreferences.oksignSlowMode) {
            ThreadLocalRandom.current().nextInt(3500, 8000).toLong()
        } else {
            2000
        }
    }
}

/*
class MultiAccountSigner(
        context: Context
) : IOKSigner(context) {
    private val accounts: MutableList<Account> = mutableListOf()

    override suspend fun start() {
        accounts.clear()
        accounts.addAll(AccountUtil.allAccounts)
    }

    interface ProgressListener {
        fun onStart(
                total: Int
        )

        fun onProgressStart(
                signDataBean: SignDataBean,
                current: Int,
                total: Int
        )

        fun onProgressFinish(
                signResultBean: SignResultBean,
                current: Int,
                total: Int
        )

        fun onFinish(
                success: Boolean,
                signedCount: Int,
                total: Int
        )

        fun onFailure(
                current: Int,
                total: Int,
                errorCode: Int,
                errorMsg: String
        )
    }
}
*/

class SingleAccountSigner(
    coroutineScope: CoroutineScope,
    context: Context,
    private val account: Account
) : IOKSigner(coroutineScope, context) {
    companion object {
        const val TAG = "SingleAccountSigner"
    }

    private val signData: MutableList<SignDataBean> = mutableListOf()
    private var position = 0
    private var successCount = 0
    private var totalCount = 0

    private var mProgressListener: ProgressListener? = null

    fun setProgressListener(listener: ProgressListener?) {
        mProgressListener = listener
    }

    override suspend fun start(): Boolean {
        var result = false
        signData.clear()
        var userName: String by Delegates.notNull()
        var tbs: String by Delegates.notNull()
        AccountUtil.updateUserInfoAsync(coroutineScope, account.bduss)
            .await()
            .fetchIfSuccess {
                userName = it.data.name
                tbs = it.data.itbTbs
                TiebaApi.getInstance().forumRecommendAsync().getData()
            }
            .doIfSuccess { forumRecommend ->
                signData.addAll(forumRecommend.likeForum.filter { it.isSign != "1" }
                    .map { SignDataBean(it.forumName, userName, tbs) })
                totalCount = signData.size
                mProgressListener?.onStart(totalCount)
                if (signData.isNotEmpty()) {
                    result = sign(0)
                } else {
                    mProgressListener?.onFinish(true, 0, 0)
                }
            }
            .doIfFailure {
                mProgressListener?.onFailure(
                    0,
                    0,
                    it.getErrorCode(),
                    it.getErrorMessage()
                )
            }
        return result
    }

    private suspend fun sign(position: Int): Boolean {
        this.position = position
        val data = signData[position]
        mProgressListener?.onProgressStart(data, position, signData.size)
        val result = sign(data)
            .doIfSuccess {
                successCount += 1
                mProgressListener?.onProgressFinish(data, it, position, totalCount)
            }
            .doIfFailure {
                mProgressListener?.onFailure(
                    position,
                    totalCount,
                    it.getErrorCode(),
                    it.getErrorMessage()
                )
            }
        return if (position < signData.size - 1) {
            delay(getSignDelay())
            sign(position + 1)
        } else {
            mProgressListener?.onFinish(successCount == totalCount, successCount, totalCount)
            result.isSuccessful
        }
    }
}

interface ProgressListener {
    fun onStart(
        total: Int
    )

    fun onProgressStart(
        signDataBean: SignDataBean,
        current: Int,
        total: Int
    )

    fun onProgressFinish(
        signDataBean: SignDataBean,
        signResultBean: SignResultBean,
        current: Int,
        total: Int
    )

    fun onFinish(
        success: Boolean,
        signedCount: Int,
        total: Int
    )

    fun onFailure(
        current: Int,
        total: Int,
        errorCode: Int,
        errorMsg: String
    )
}