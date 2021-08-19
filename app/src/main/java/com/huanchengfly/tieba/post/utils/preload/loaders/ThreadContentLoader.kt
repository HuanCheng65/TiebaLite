package com.huanchengfly.tieba.post.utils.preload.loaders

import com.billy.android.preloader.interfaces.DataLoader
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.ThreadContentBean

class ThreadContentLoader(
    private val threadId: String,
    private val page: Int = 1,
    private val lz: Boolean = false
) : DataLoader<ThreadContentBean> {
    override fun loadData(): ThreadContentBean? {
        val call =
            TiebaApi.getInstance().threadContent(threadId = threadId, page = page, seeLz = lz)
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