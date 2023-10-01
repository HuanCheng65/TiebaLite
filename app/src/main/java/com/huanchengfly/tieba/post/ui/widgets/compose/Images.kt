package com.huanchengfly.tieba.post.ui.widgets.compose

import android.content.Context
import android.os.Parcelable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.github.panpf.sketch.compose.AsyncImage
import com.github.panpf.sketch.request.Depth
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.transform.MaskTransformation
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.models.protos.PhotoViewData
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.page.photoview.PhotoViewActivity
import com.huanchengfly.tieba.post.ui.page.photoview.PhotoViewActivity.Companion.EXTRA_PHOTO_VIEW_DATA
import com.huanchengfly.tieba.post.utils.ImageUtil
import com.huanchengfly.tieba.post.utils.NetworkUtil
import com.huanchengfly.tieba.post.utils.appPreferences

fun shouldLoadImage(context: Context, skipNetworkCheck: Boolean): Boolean {
    val imageLoadSettings =
        context.appPreferences.imageLoadType?.toIntOrNull() ?: ImageUtil.SETTINGS_SMART_ORIGIN
    return skipNetworkCheck
            || imageLoadSettings == ImageUtil.SETTINGS_SMART_ORIGIN
            || imageLoadSettings == ImageUtil.SETTINGS_ALL_ORIGIN
            || (imageLoadSettings == ImageUtil.SETTINGS_SMART_LOAD && NetworkUtil.isWifiConnected(
        context
    ))
}

@Composable
fun NetworkImage(
    imageUri: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    photoViewData: ImmutableHolder<PhotoViewData>? = null,
    contentScale: ContentScale = ContentScale.Fit,
    skipNetworkCheck: Boolean = false,
) {
    val context = LocalContext.current
    var shouldLoad by remember { mutableStateOf(shouldLoadImage(context, skipNetworkCheck)) }
    val enableClick = remember(photoViewData, shouldLoad) { photoViewData != null || !shouldLoad }

    val colorMask =
        if (ExtendedTheme.colors.isNightMode && context.appPreferences.imageDarkenWhenNightMode) {
            MaskTransformation(0x35000000)
        } else null

    val request = remember(imageUri, shouldLoad, colorMask) {
        DisplayRequest(context, imageUri) {
            placeholder(ImageUtil.getPlaceHolder(context, 0))
            crossfade()
            if (!shouldLoad) {
                depth(Depth.LOCAL)
            }
            if (colorMask != null) {
                transformations(colorMask)
            }
        }
    }

    LongClickMenu(
        enabled = enableClick,
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = {
            if (!shouldLoad) {
                shouldLoad = true
            } else if (photoViewData != null) {
                context.goToActivity<PhotoViewActivity> {
                    putExtra(EXTRA_PHOTO_VIEW_DATA, photoViewData.get() as Parcelable)
                }
            }
        },
        menuContent = {
            DropdownMenuItem(
                onClick = {
                    ImageUtil.download(
                        context,
                        photoViewData?.get { this.data_?.originUrl } ?: imageUri)
                }
            ) {
                Text(text = stringResource(id = R.string.title_save_image))
            }
        },
        modifier = modifier
            .width(IntrinsicSize.Min)
            .height(IntrinsicSize.Min),
    ) {
        AsyncImage(
            request = request,
            modifier = Modifier.fillMaxSize(),
            contentDescription = contentDescription,
            contentScale = contentScale,
        )
    }
}

@Composable
fun NetworkImage(
    imageUriProvider: () -> String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    photoViewDataProvider: (() -> ImmutableHolder<PhotoViewData>)? = null,
    contentScale: ContentScale = ContentScale.Fit,
    skipNetworkCheck: Boolean = false,
) {
    val imageUri by rememberUpdatedState(newValue = imageUriProvider())
    val photoViewData by rememberUpdatedState(newValue = photoViewDataProvider?.invoke())

    NetworkImage(
        imageUri = imageUri,
        contentDescription = contentDescription,
        modifier = modifier,
        photoViewData = photoViewData,
        contentScale = contentScale,
        skipNetworkCheck = skipNetworkCheck,
    )
}