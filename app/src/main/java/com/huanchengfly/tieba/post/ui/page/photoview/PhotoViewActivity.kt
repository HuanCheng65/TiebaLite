package com.huanchengfly.tieba.post.ui.page.photoview

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.panpf.sketch.displayImage
import com.github.panpf.sketch.zoom.Edge
import com.github.panpf.sketch.zoom.ReadModeDecider
import com.github.panpf.sketch.zoom.SketchZoomImageView
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.arch.BaseComposeActivityWithParcelable
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.models.protos.PhotoViewData
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.utils.ImageUtil
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

object MyReadModeDecider : ReadModeDecider {
    override fun should(
        imageWidth: Int,
        imageHeight: Int,
        viewWidth: Int,
        viewHeight: Int
    ): Boolean {
        val imageAspectRatio = imageHeight.toFloat() / imageWidth
        val viewAspectRatio = viewHeight.toFloat() / viewWidth
        return if (viewAspectRatio > 1f) {
            imageAspectRatio >= viewAspectRatio * 1.25f
        } else {
            imageAspectRatio >= (1f / viewAspectRatio) * 3f
        }
    }
}

@Composable
private fun ViewPhoto(
    imageUri: String,
    modifier: Modifier = Modifier,
    onDrag: ((dx: Float, dy: Float, isAtEdge: Boolean) -> Unit)? = null
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        var progress by remember { mutableStateOf(0f) }
        var showProgress by remember { mutableStateOf(true) }
        AndroidView(
            factory = {
                SketchZoomImageView(it).apply {
                    readModeEnabled = true
                    readModeDecider = MyReadModeDecider
                    allowParentInterceptOnEdge = true
                    addOnViewDragListener { dx, dy ->
                        val isAtEdge = horScrollEdge != Edge.NONE
                        onDrag?.invoke(dx, dy, isAtEdge)
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = {
                it.displayImage(imageUri) {
                    listener(
                        onStart = { showProgress = true },
                        onSuccess = { _, _ -> showProgress = false },
                        onError = { _, _ -> showProgress = false },
                        onCancel = { showProgress = false }
                    )
                    progressListener { _, totalLength: Long, completedLength: Long ->
                        progress = (completedLength.toDouble() / totalLength).toFloat()
                    }
                }
            }
        )
        if (showProgress) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = progress,
                    color = ExtendedTheme.colors.primary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "${(progress * 100).roundToInt()}%",
                    color = ExtendedTheme.colors.primary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

class PhotoViewActivity : BaseComposeActivityWithParcelable<PhotoViewData>() {
    private val viewModel: PhotoViewViewModel by viewModels()

    override val dataExtraKey: String = EXTRA_PHOTO_VIEW_DATA

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    override fun createContent(data: PhotoViewData) {
        LazyLoad(loaded = viewModel.initialized) {
            viewModel.send(PhotoViewUiIntent.Init(data))
            viewModel.initialized = true
        }
        val items by viewModel.uiState.collectPartialAsState(
            prop1 = PhotoViewUiState::data,
            initial = emptyList()
        )
        val initialIndex by viewModel.uiState.collectPartialAsState(
            prop1 = PhotoViewUiState::initialIndex,
            initial = 0
        )
        val totalAmount by viewModel.uiState.collectPartialAsState(
            prop1 = PhotoViewUiState::totalAmount,
            initial = 0
        )
        val hasPrev by viewModel.uiState.collectPartialAsState(
            prop1 = PhotoViewUiState::hasPrev,
            initial = false
        )
        val hasNext by viewModel.uiState.collectPartialAsState(
            prop1 = PhotoViewUiState::hasNext,
            initial = false
        )
        val pageCount = items.size
        Surface(color = Color.Black) {
            if (items.isNotEmpty()) {
                val coroutineScope = rememberCoroutineScope()
                val pagerState = rememberPagerState(initialPage = initialIndex)
                LaunchedEffect(initialIndex) {
                    if (pagerState.currentPage != initialIndex) pagerState.scrollToPage(initialIndex)
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    HorizontalPager(
                        count = pageCount,
                        state = pagerState,
                        key = {
                            items[it].originUrl
                        }
                    ) {
                        val item = items[it]
                        ViewPhoto(
                            imageUri = item.originUrl,
                            modifier = Modifier.fillMaxSize(),
                            onDrag = { dx, dy, isAtEdge ->
                                if (abs(dy) < 15 && abs(dx) > 25 && isAtEdge) {
                                    val prevPage = it - 1
                                    val nextPage = it + 1
                                    if (dx > 0 && prevPage >= 0) {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(prevPage)
                                        }
                                    } else if (dx < 0 && nextPage < items.size) {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(nextPage)
                                        }
                                    }
                                }
                            }
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Black.copy(alpha = 0.0f),
                                            Color.Black.copy(alpha = 0.5f)
                                        )
                                    )
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val index = pagerState.currentPage
                            if (totalAmount > 1) {
                                val picIndex = items[index].overallIndex ?: (index + 1)
                                Text(
                                    text = "$picIndex / $totalAmount",
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                            IconButton(onClick = {
                                toastShort(R.string.toast_preparing_share_pic)
                                ImageUtil.download(
                                    this@PhotoViewActivity,
                                    items[index].originUrl,
                                    true
                                ) { uri: Uri ->
                                    val chooser = Intent(Intent.ACTION_SEND).apply {
                                        type = Intent.normalizeMimeType("image/jpeg")
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        } else {
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                        }
                                    }.let {
                                        Intent.createChooser(
                                            it,
                                            getString(R.string.title_share_pic)
                                        )
                                    }
                                    runCatching {
                                        startActivity(chooser)
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = stringResource(id = R.string.title_share_pic)
                                )
                            }
                            IconButton(onClick = {
                                ImageUtil.download(
                                    this@PhotoViewActivity,
                                    items[index].originUrl
                                )
                            }) {
                                Icon(
                                    imageVector = Icons.Rounded.Download,
                                    contentDescription = stringResource(id = R.string.desc_download_pic)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return try {
            super.dispatchTouchEvent(ev)
        } catch (e: RuntimeException) {
            e.printStackTrace()
            true
        }
    }

    companion object {
        const val EXTRA_PHOTO_VIEW_DATA = "photo_view_data"
    }
}