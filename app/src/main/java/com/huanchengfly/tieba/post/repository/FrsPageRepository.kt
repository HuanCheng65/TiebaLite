package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.protos.frsPage.FrsPageResponse
import com.huanchengfly.tieba.post.api.models.protos.threadList.ThreadListResponse
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaUnknownException
import com.huanchengfly.tieba.post.utils.appPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

object FrsPageRepository {
    var lastHash: String = ""
    var lastResponse: FrsPageResponse? = null

    fun frsPage(
        forumName: String,
        page: Int,
        loadType: Int,
        sortType: Int,
        goodClassifyId: Int? = null,
        forceNew: Boolean = false,
    ): Flow<FrsPageResponse> {
        val hash = "${forumName}_${page}_${loadType}_${sortType}_${goodClassifyId}"
        if (!forceNew && lastResponse != null && lastHash == hash) {
            return flowOf(lastResponse!!)
        }
        lastHash = hash
        return TiebaApi.getInstance().frsPage(forumName, page, loadType, sortType, goodClassifyId)
            .map { response ->
                if (response.data_ == null) throw TiebaUnknownException
                val userList = response.data_.user_list
                val threadList = response.data_.thread_list
                    .map { threadInfo ->
                        threadInfo.copy(author = userList.find { it.id == threadInfo.authorId })
                    }
                    .filter { !App.INSTANCE.appPreferences.blockVideo || it.videoInfo == null }
                    .filter { it.ala_info == null } // 去他妈的直播
                response.copy(data_ = response.data_.copy(thread_list = threadList))
            }
            .onEach { lastResponse = it }
    }

    fun threadList(
        forumId: Long,
        forumName: String,
        page: Int,
        sortType: Int,
        threadIds: String = "",
    ): Flow<ThreadListResponse> =
        TiebaApi.getInstance()
            .threadList(forumId, forumName, page, sortType, threadIds)
            .map { response ->
                if (response.data_ == null) throw TiebaUnknownException
                val userList = response.data_.user_list
                val threadList = response.data_.thread_list
                    .map { threadInfo ->
                        threadInfo.copy(author = userList.find { it.id == threadInfo.authorId })
                    }
                    .filter { !App.INSTANCE.appPreferences.blockVideo || it.videoInfo == null }
                    .filter { it.ala_info == null } // 去他妈的直播
                response.copy(data_ = response.data_.copy(thread_list = threadList))
            }
}