package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.protos.OriginThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.pbPage.PbPageResponse
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaException
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaUnknownException
import com.huanchengfly.tieba.post.ui.page.thread.ThreadPageFrom
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object EmptyDataException : TiebaException("data is empty!") {
    override val code: Int
        get() = -2
}

object PbPageRepository {
    const val ST_TYPE_MENTION = "mention"
    const val ST_TYPE_STORE_THREAD = "store_thread"
    private val ST_TYPES = persistentListOf(ST_TYPE_MENTION, ST_TYPE_STORE_THREAD)

    fun pbPage(
        threadId: Long,
        page: Int = 1,
        postId: Long = 0,
        forumId: Long? = null,
        seeLz: Boolean = false,
        sortType: Int = 0,
        back: Boolean = false,
        from: String = "",
        lastPostId: Long? = null,
    ): Flow<PbPageResponse> =
        TiebaApi.getInstance()
            .pbPageFlow(
                threadId,
                page,
                postId = postId,
                seeLz = seeLz,
                sortType = sortType,
                back = back,
                forumId = forumId,
                stType = from.takeIf { ST_TYPES.contains(it) }.orEmpty(),
                mark = if (from == ThreadPageFrom.FROM_STORE) 1 else 0,
                lastPostId = lastPostId
            )
            .map { response ->
                if (response.data_ == null) {
                    throw TiebaUnknownException
                }
                if (response.data_.post_list.isEmpty()) {
                    throw EmptyDataException
                }
                if (
                    response.data_.page == null
                    || response.data_.thread?.author == null
                    || response.data_.forum == null
                    || response.data_.anti == null
                ) {
                    throw TiebaUnknownException
                }
                val userList = response.data_.user_list
                val postList = response.data_.post_list.map {
                    val author = it.author
                        ?: userList.first { user -> user.id == it.author_id }
                    it.copy(
                        author_id = author.id,
                        author = it.author
                            ?: userList.first { user -> user.id == it.author_id },
                        from_forum = response.data_.forum,
                        tid = response.data_.thread.id,
                        sub_post_list = it.sub_post_list?.copy(
                            sub_post_list = it.sub_post_list.sub_post_list.map { subPost ->
                                subPost.copy(
                                    author = subPost.author
                                        ?: userList.first { user -> user.id == subPost.author_id }
                                )
                            }
                        ),
                        origin_thread_info = OriginThreadInfo(
                            author = response.data_.thread.author
                        )
                    )
                }
                val firstPost = postList.firstOrNull { it.floor == 1 }
                    ?: response.data_.first_floor_post?.copy(
                        author_id = response.data_.thread.author.id,
                        author = response.data_.thread.author,
                        from_forum = response.data_.forum,
                        tid = response.data_.thread.id,
                        sub_post_list = response.data_.first_floor_post.sub_post_list?.copy(
                            sub_post_list = response.data_.first_floor_post.sub_post_list.sub_post_list.map { subPost ->
                                subPost.copy(
                                    author = subPost.author
                                        ?: userList.first { user -> user.id == subPost.author_id }
                                )
                            }
                        )
                    )

                response.copy(
                    data_ = response.data_.copy(
                        post_list = postList,
                        first_floor_post = firstPost,
                    )
                )
            }
}