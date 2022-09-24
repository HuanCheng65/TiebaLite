package com.huanchengfly.tieba.post.ui.widgets.compose

import android.os.Parcelable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.github.panpf.sketch.compose.AsyncImage
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.models.protos.PhotoViewData
import com.huanchengfly.tieba.post.ui.page.photoview.PhotoViewActivity
import com.huanchengfly.tieba.post.ui.page.photoview.PhotoViewActivity.Companion.EXTRA_PHOTO_VIEW_DATA

@Composable
fun NetworkImage(
    imageUri: String,
    contentDescription: String?,
    photoViewData: PhotoViewData? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val context = LocalContext.current
    val clickableModifier = if (photoViewData != null) {
        Modifier.clickable(
            indication = null,
            interactionSource = remember {
                MutableInteractionSource()
            }
        ) {
            context.goToActivity<PhotoViewActivity> {
                putExtra(EXTRA_PHOTO_VIEW_DATA, photoViewData as Parcelable)
            }
        }
    } else Modifier
    AsyncImage(
        imageUri = imageUri,
        contentDescription = contentDescription,
        modifier = modifier.then(clickableModifier),
        contentScale = contentScale,
    )
}