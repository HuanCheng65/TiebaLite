package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.protos.addPost.AddPostResponse
import com.huanchengfly.tieba.post.arch.GlobalEvent
import com.huanchengfly.tieba.post.arch.emitGlobalEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

object AddPostRepository {
    fun addPost(
        content: String,
        forumId: String,
        forumName: String,
        threadId: String,
        tbs: String? = null,
        nameShow: String? = null,
        postId: String? = null,
        subPostId: String? = null,
        replyUserId: String? = null
    ): Flow<AddPostResponse> =
        TiebaApi.getInstance()
            .addPostFlow(
                content,
                forumId,
                forumName,
                threadId,
                tbs,
                nameShow,
                postId,
                subPostId,
                replyUserId
            )
            .onEach {
                emitGlobalEvent(GlobalEvent.ReplySuccess(threadId, postId, subPostId))
            }
}