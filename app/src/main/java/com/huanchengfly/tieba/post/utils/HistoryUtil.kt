package com.huanchengfly.tieba.post.utils

import com.huanchengfly.tieba.post.models.database.History
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.litepal.LitePal.deleteAll
import org.litepal.LitePal.order
import org.litepal.LitePal.where
import org.litepal.crud.async.FindMultiExecutor
import org.litepal.extension.find

object HistoryUtil {
    const val PAGE_SIZE = 100
    const val TYPE_FORUM = 1
    const val TYPE_THREAD = 2
    fun deleteAll() {
        deleteAll(History::class.java)
    }

    @JvmOverloads
    fun writeHistory(history: History, async: Boolean = false) {
        add(history, async)
    }

    val all: List<History>
        get() = order("timestamp desc, count desc").limit(100).find(
            History::class.java
        )

    fun getAll(type: Int): List<History> {
        return order("timestamp desc, count desc").where("type = ?", type.toString())
            .limit(PAGE_SIZE)
            .find(
                History::class.java
            )
    }

    fun getAllAsync(type: Int): FindMultiExecutor<History> {
        return order("timestamp desc, count desc").where("type = ?", type.toString())
            .limit(PAGE_SIZE)
            .findAsync(
                History::class.java
            )
    }

    fun getFlow(
        type: Int,
        page: Int
    ): Flow<List<History>> {
        return flow {
            delay(100)
            emit(
                where("type = ?", "$type")
                    .order("timestamp desc, count desc")
                    .limit(PAGE_SIZE)
                    .offset(page * 100)
                    .find<History>()
            )
        }.flowOn(Dispatchers.IO)
    }

    private fun update(history: History): Boolean {
        val historyBean = where("data = ?", history.data).findFirst(
            History::class.java
        )
        if (historyBean != null) {
            historyBean.setTimestamp(System.currentTimeMillis())
                .setTitle(history.title)
                .setExtras(history.extras)
                .setAvatar(history.avatar)
                .setUsername(history.username)
                .setCount(historyBean.count + 1)
                .update(historyBean.id.toLong())
            return true
        }
        return false
    }

    private fun add(history: History, async: Boolean = false) {
        if (update(history)) {
            return
        }
        history.setCount(1).timestamp = System.currentTimeMillis()
        if (async) {
            history.saveAsync().listen(null)
        } else {
            history.save()
        }
    }
}