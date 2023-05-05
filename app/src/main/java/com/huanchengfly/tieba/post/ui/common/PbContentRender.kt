package com.huanchengfly.tieba.post.ui.common

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.post.arch.BaseComposeActivity.Companion.LocalWindowSizeClass
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.models.protos.PhotoViewData
import com.huanchengfly.tieba.post.ui.common.windowsizeclass.WindowWidthSizeClass
import com.huanchengfly.tieba.post.ui.widgets.compose.EmoticonText
import com.huanchengfly.tieba.post.ui.widgets.compose.NetworkImage
import com.huanchengfly.tieba.post.utils.appPreferences

interface PbContentRender {
    @Composable
    fun Render()
}

data class TextContentRender(
    val text: AnnotatedString
) : PbContentRender {
    constructor(text: String) : this(AnnotatedString(text))

    @Composable
    override fun Render() {
        EmoticonText(text = text, fontSize = 15.sp, style = MaterialTheme.typography.body1)
    }

    operator fun plus(text: String): TextContentRender {
        return TextContentRender(this.text + AnnotatedString(text))
    }

    operator fun plus(text: AnnotatedString): TextContentRender {
        return TextContentRender(this.text + text)
    }
}

data class PicContentRender(
    val picUrl: String,
    val originUrl: String,
    val showOriginBtn: Boolean,
    val originSize: Int,
    val width: Int,
    val height: Int,
    val picId: String,
    val photoViewData: ImmutableHolder<PhotoViewData>? = null
) : PbContentRender {
    @Composable
    override fun Render() {
        val widthFraction =
            if (LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Compact) 1f else 0.5f
        val context = LocalContext.current

        NetworkImage(
            imageUri = picUrl,
            contentDescription = null,
            modifier = Modifier
                .clip(RoundedCornerShape(context.appPreferences.radius.dp))
                .fillMaxWidth(widthFraction)
                .aspectRatio(width * 1f / height),
            photoViewData = photoViewData,
            contentScale = ContentScale.Crop
        )
    }
}