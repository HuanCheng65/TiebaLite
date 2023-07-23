package com.huanchengfly.tieba.post.ui.widgets.compose

import android.os.Parcelable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.github.panpf.sketch.compose.AsyncImage
import com.github.panpf.sketch.request.DisplayRequest
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.models.protos.PhotoViewData
import com.huanchengfly.tieba.post.ui.page.photoview.PhotoViewActivity
import com.huanchengfly.tieba.post.ui.page.photoview.PhotoViewActivity.Companion.EXTRA_PHOTO_VIEW_DATA
import com.huanchengfly.tieba.post.utils.ImageUtil

@Composable
fun NetworkImage(
    imageUri: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    photoViewData: ImmutableHolder<PhotoViewData>? = null,
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
                putExtra(EXTRA_PHOTO_VIEW_DATA, photoViewData.get() as Parcelable)
            }
        }
    } else Modifier
    val request = remember(imageUri) {
        DisplayRequest(context, imageUri) {
            placeholder(ImageUtil.getPlaceHolder(context, 0))
            crossfade()
        }
    }
    AsyncImage(
        request = request,
        contentDescription = contentDescription,
        modifier = modifier.then(clickableModifier),
        contentScale = contentScale,
    )
}

@Composable
fun NetworkImage(
    imageUriProvider: () -> String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    photoViewDataProvider: (() -> ImmutableHolder<PhotoViewData>)? = null,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val imageUri by rememberUpdatedState(newValue = imageUriProvider())
    val photoViewData by rememberUpdatedState(newValue = photoViewDataProvider?.invoke())

    NetworkImage(
        imageUri = imageUri,
        contentDescription = contentDescription,
        modifier = modifier,
        photoViewData = photoViewData,
        contentScale = contentScale,
    )
}