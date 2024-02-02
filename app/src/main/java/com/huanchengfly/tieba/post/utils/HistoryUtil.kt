package com.huanchengfly.tieba.post.utils

import com.huanchengfly.tieba.post.models.database.History
import com.huanchengfly.tieba.post.utils.extension.findFlow
import kotlinx.coroutines.flow.Flow
import org.litepal.LitePal
import org.litepal.crud.async.FindMultiExecutor
import org.litepal.extension.deleteAll
import org.litepal.extension.find
import org.litepal.extension.findAsync
import org.litepal.extension.findFirstAsync

object HistoryUtil {
    const val PAGE_SIZE = 100
    const val TYPE_FORUM = 1
    const val TYPE_THREAD = 2
    fun deleteAll() {
        LitePal.deleteAll<History>()
    }

    @JvmOverloads
    fun saveHistory(history: History, async: Boolean = true) {
        if (async) {
            saveOrUpdateAsync(history)
        } else {
            saveOrUpdate(history)
        }
    }

    val all: List<History>
        get() = LitePal.order("timestamp desc, count desc").limit(100).find<History>()

    fun getAll(type: Int): List<History> {
        return LitePal.order("timestamp desc, count desc").where("type = ?", type.toString())
            .limit(PAGE_SIZE)
            .find<History>()
    }

    fun getAllAsync(type: Int): FindMultiExecutor<History> {
        return LitePal.order("timestamp desc, count desc").where("type = ?", type.toString())
            .limit(PAGE_SIZE)
            .findAsync<History>()
    }

    fun getFlow(
        type: Int,
        page: Int
    ): Flow<List<History>> {
        return LitePal.where("type = ?", "$type")
            .order("timestamp desc, count desc")
            .limit(PAGE_SIZE)
            .offset(page * 100)
            .findFlow()
    }

    private fun update(history: History): Boolean {
        val historyBean = LitePal.where("data = ?", history.data).findFirst(
            History::class.java
        )
        if (historyBean != null) {
            historyBean.copy(
                timestamp = System.currentTimeMillis(),
                title = history.title,
                extras = history.extras,
                avatar = history.avatar,
                username = history.username,
                count = historyBean.count + 1
            ).update(historyBean.id)
            return true
        }
        return false
    }

    private fun updateAsync(
        history: History,
        callback: ((Boolean) -> Unit)? = null,
    ) {
        LitePal.where("data = ?", history.data).findFirstAsync<History?>()
            .listen {
                if (it == null) {
                    callback?.invoke(false)
                } else {
                    history.copy(
                        timestamp = System.currentTimeMillis(),
                        count = it.count + 1
                    ).updateAsync(it.id).listen {
                        callback?.invoke(true)
                    }
                }
            }
    }

    private fun saveOrUpdate(history: History, async: Boolean = false) {
        if (update(history)) {
            return
        }
        val saveHistory = history.copy(count = 1, timestamp = System.currentTimeMillis())
        if (async) {
            saveHistory.saveAsync().listen(null)
        } else {
            saveHistory.save()
        }
    }

    private fun saveOrUpdateAsync(
        history: History,
        callback: ((Boolean) -> Unit)? = null
    ) {
        updateAsync(history) { success ->
            if (!success) {
                history.copy(count = 1, timestamp = System.currentTimeMillis())
                    .saveAsync()
                    .listen {
                        callback?.invoke(it)
                    }
            }
        }
    }
}