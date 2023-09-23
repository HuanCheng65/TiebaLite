package com.huanchengfly.tieba.post.ui.models

import androidx.compose.runtime.Immutable
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.personalized.ThreadPersonalized
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.utils.BlockManager.shouldBlock
import com.huanchengfly.tieba.post.utils.appPreferences

@Immutable
data class ThreadItemData(
    val thread: ImmutableHolder<ThreadInfo>,
    val blocked: Boolean = thread.get { shouldBlock() },
    val personalized: ImmutableHolder<ThreadPersonalized>? = null,
    val hidden: Boolean = blocked && App.INSTANCE.appPreferences.hideBlockedContent,
)