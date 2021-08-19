package com.huanchengfly.tieba.post.utils.preload.loaders

import com.billy.android.preloader.interfaces.DataLoader
import com.huanchengfly.tieba.post.api.ForumSortType
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.ForumPageBean

class ForumLoader constructor(
    private val forumName: String,
    private val page: Int,
    private val sortType: ForumSortType = ForumSortType.REPLY_TIME
) : DataLoader<ForumPageBean> {
    override fun loadData(): ForumPageBean? {
        val call = TiebaApi.getInstance().forumPage(
            forumName = forumName,
            page = page,
            sortType = sortType
        )
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                return response.body()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

}