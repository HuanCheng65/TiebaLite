package com.huanchengfly.tieba.post.utils

import com.huanchengfly.tieba.post.api.models.MessageListBean
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.models.protos.SubPostList
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.abstractText
import com.huanchengfly.tieba.post.api.models.protos.plainText
import com.huanchengfly.tieba.post.api.models.protos.resolvedAuthorId
import com.huanchengfly.tieba.post.models.database.Block
import com.huanchengfly.tieba.post.models.database.Block.Companion.getKeywords
import org.litepal.LitePal
import org.litepal.extension.delete
import org.litepal.extension.findAllAsync

object BlockManager {
    private val blockList: MutableList<Block> = mutableListOf()

    val blackList: List<Block>
        get() = blockList.filter { it.category == Block.CATEGORY_BLACK_LIST }

    val whiteList: List<Block>
        get() = blockList.filter { it.category == Block.CATEGORY_WHITE_LIST }

    fun addBlock(block: Block) {
        block.save()
        blockList.add(block)
    }

    fun addBlockAsync(
        block: Block,
        callback: ((Boolean) -> Unit)? = null,
    ) {
        block.saveAsync()
            .listen {
                callback?.invoke(it)
                blockList.add(block)
            }
    }

    fun removeBlock(id: Long) {
        LitePal.delete<Block>(id)
        blockList.removeAll { it.id == id }
    }

    fun init() {
        LitePal.findAllAsync<Block>().listen { blocks ->
            blockList.addAll(blocks)
        }
    }

    fun shouldBlock(content: String): Boolean {
        return blackList.any { block ->
            block.type == Block.TYPE_KEYWORD
                    && block.getKeywords().all { content.contains(it) }
        } && whiteList.none { block ->
            block.type == Block.TYPE_KEYWORD
                    && block.getKeywords().all { content.contains(it) }
        }
    }

    fun shouldBlock(userId: Long = 0L, userName: String? = null): Boolean {
        return blackList.any { it.matchesUser(userId, userName) }
                && whiteList.none { it.matchesUser(userId, userName) }
    }

    // A missing id (0) or blank name must never match, otherwise one rule saved without a name
    // would hide every user whose name the server left empty.
    private fun Block.matchesUser(userId: Long, userName: String?): Boolean =
        type == Block.TYPE_USER && (
                (userId != 0L && uid == userId.toString()) ||
                        (!userName.isNullOrBlank() && username == userName)
                )

    fun ThreadInfo.shouldBlock(): Boolean =
        shouldBlock(title) || shouldBlock(abstractText) || shouldBlock(authorId, author?.name)

    fun Post.shouldBlock(): Boolean =
        shouldBlock(content.plainText) || shouldBlock(resolvedAuthorId, author?.name)

    fun SubPostList.shouldBlock(): Boolean =
        shouldBlock(content.plainText) || shouldBlock(resolvedAuthorId, author?.name)

    fun MessageListBean.MessageInfoBean.shouldBlock(): Boolean =
        shouldBlock(content.orEmpty()) || shouldBlock(
            this.replyer?.id?.toLongOrNull() ?: -1,
            this.replyer?.name.orEmpty()
        )
}