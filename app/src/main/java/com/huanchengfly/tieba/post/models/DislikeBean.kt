package com.huanchengfly.tieba.post.models

import com.google.gson.annotations.SerializedName

data class DislikeBean(
    @field:SerializedName("tid") var threadId: String,
    @field:SerializedName("dislike_ids") var dislikeIds: String,
    @field:SerializedName("fid") var forumId: String?,
    @field:SerializedName("click_time") var clickTime: Long,
    var extra: String
) : BaseBean()