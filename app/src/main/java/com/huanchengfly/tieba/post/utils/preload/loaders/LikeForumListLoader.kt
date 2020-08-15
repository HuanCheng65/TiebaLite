package com.huanchengfly.tieba.post.utils.preload.loaders

import com.billy.android.preloader.interfaces.DataLoader
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.ForumRecommend

class LikeForumListLoader : DataLoader<ForumRecommend> {
    override fun loadData(): ForumRecommend? {
        val call = TiebaApi.getInstance().forumRecommend()
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