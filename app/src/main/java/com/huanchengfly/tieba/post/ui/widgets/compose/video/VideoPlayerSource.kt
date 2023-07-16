package com.huanchengfly.tieba.post.ui.widgets.compose.video

import android.os.Parcelable
import androidx.annotation.RawRes
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
sealed class VideoPlayerSource : Parcelable {
    @Immutable
    data class Raw(@RawRes val resId: Int) : VideoPlayerSource()

    @Immutable
    data class Network(
        val url: String,
        val headers: Map<String, String> = mapOf()
    ) : VideoPlayerSource()
}
