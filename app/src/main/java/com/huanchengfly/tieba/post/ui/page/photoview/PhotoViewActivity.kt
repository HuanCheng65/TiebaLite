package com.huanchengfly.tieba.post.ui.page.photoview

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.displayImage
import com.github.panpf.sketch.zoom.ReadModeDecider
import com.github.panpf.sketch.zoom.SketchZoomImageView
import com.github.panpf.sketch.zoom.internal.Edge
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
        sketch: Sketch,
        imageWidth: Int,
        imageHeight: Int,
        viewWidth: Int,
        viewHeight: Int
    ): Boolean {
        val imageAspectRatio = imageHeight.toFloat() / imageWidth
        val viewAspectRatio = viewHeight.toFloat() / viewWidth
        return imageAspectRatio >= viewAspectRatio * 1.25f
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

    private fun getItemIndex(hasPrev: Boolean, pageIndex: Int): Int {
        return if (hasPrev) pageIndex - 1 else pageIndex
    }

    private fun getPageIndex(hasPrev: Boolean, itemIndex: Int): Int {
        return if (hasPrev) itemIndex + 1 else itemIndex
    }

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
        val initialItemIndex by viewModel.uiState.collectPartialAsState(
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
        val pageCount by derivedStateOf {
            items.size + if (hasPrev) 1 else 0 + if (hasNext) 1 else 0
        }
        Log.i("PhotoView", "pageCount = $pageCount, itemsCount = ${items.size}, hasPrev = $hasPrev, hasNext = $hasNext")
        val pagerState = rememberPagerState(initialPage = getPageIndex(hasPrev, initialItemIndex))
        val coroutineScope = rememberCoroutineScope()
        Surface(color = Color.Black) {
            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    count = pageCount,
                    state = pagerState,
                    key = {
                        if (hasPrev && it == 0) {
                            "PrevLoader"
                        } else {
                            items[getItemIndex(hasPrev, it)].originUrl
                        }
                    }
                ) {
                    if (hasPrev && it == 0) {
                        return@HorizontalPager
                    }
                    val item = items[getItemIndex(hasPrev, it)]
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
//                            coroutineScope.launch {
//                                pagerState.animateScrollBy(-dx)
//                            }
                        }
                    )
                }
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
                        .padding(horizontal = 16.dp)
                        .align(Alignment.BottomCenter),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val itemIndex = getItemIndex(hasPrev, pagerState.currentPage)
                    if (totalAmount > 1) {
                        val picIndex = items[itemIndex].overallIndex ?: (itemIndex + 1)
                        Text(text = "$picIndex / $totalAmount", modifier = Modifier.weight(1f))
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    IconButton(onClick = {
                        toastShort(R.string.toast_preparing_share_pic)
                        ImageUtil.download(
                            this@PhotoViewActivity,
                            items[itemIndex].originUrl,
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
                            }.let { Intent.createChooser(it, getString(R.string.title_share_pic)) }
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
                    IconButton(onClick = { ImageUtil.download(this@PhotoViewActivity, items[itemIndex].originUrl) }) {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = stringResource(id = R.string.desc_download_pic)
                        )
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