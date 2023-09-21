package com.huanchengfly.tieba.post.ui.page.photoview

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.zoomimage.SketchZoomAsyncImage
import com.google.accompanist.systemuicontroller.SystemUiController
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.arch.BaseComposeActivityWithParcelable
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.models.protos.LoadPicPageData
import com.huanchengfly.tieba.post.models.protos.PhotoViewData
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.ProvideContentColor
import com.huanchengfly.tieba.post.utils.ImageUtil
import com.huanchengfly.tieba.post.utils.download
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.roundToInt

@Composable
private fun ViewPhoto(
    imageUri: String,
    modifier: Modifier = Modifier,
    onTap: (offset: Offset) -> Unit = {},
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val context = LocalContext.current
        var progress by remember { mutableFloatStateOf(0f) }
        var showProgress by remember { mutableStateOf(true) }
        val request = remember(imageUri) {
            DisplayRequest.Builder(context, imageUri)
                .listener(
                    onStart = { showProgress = true },
                    onSuccess = { _, _ -> showProgress = false },
                    onError = { _, _ -> showProgress = false },
                    onCancel = { showProgress = false }
                )
                .progressListener { _, totalLength, completedLength ->
                    progress = completedLength.toFloat() / totalLength
                }
                .build()
        }
        SketchZoomAsyncImage(
            request = request,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            onTap = onTap,
        )
        if (showProgress) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = progress,
                    color = ExtendedTheme.colors.accent,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "${(progress * 100).roundToInt()}%",
                    color = ExtendedTheme.colors.accent,
                    fontSize = 12.sp
                )
            }
        }
    }
}

class PhotoViewActivity : BaseComposeActivityWithParcelable<PhotoViewData>() {
    private val viewModel: PhotoViewViewModel by viewModels()

    override val dataExtraKey: String = EXTRA_PHOTO_VIEW_DATA

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Content(data: PhotoViewData) {
        LazyLoad(loaded = viewModel.initialized) {
            viewModel.send(PhotoViewUiIntent.Init(data))
            viewModel.initialized = true
        }
        val items by viewModel.uiState.collectPartialAsState(
            prop1 = PhotoViewUiState::data,
            initial = persistentListOf()
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
        val loadPicPageData by viewModel.uiState.collectPartialAsState(
            prop1 = PhotoViewUiState::loadPicPageData,
            initial = LoadPicPageData()
        )

        val pageCount by remember { derivedStateOf { items.size } }

        val pagerState = rememberPagerState(initialPage = initialIndex) { pageCount }

        LaunchedEffect(initialIndex) {
            if (pagerState.currentPage != initialIndex) pagerState.scrollToPage(initialIndex)
        }

        Surface(color = Color.Black) {
            if (items.isNotEmpty()) {
                LaunchedEffect(pagerState.currentPage, pageCount, loadPicPageData) {
                    loadPicPageData?.let {
                        val item = items[pagerState.currentPage]
                        if (pagerState.currentPage == 0 && hasPrev) {
                            viewModel.send(
                                PhotoViewUiIntent.LoadPrev(
                                    item.picId,
                                    item.overallIndex,
                                    it
                                )
                            )
                        } else if (pagerState.currentPage == pageCount - 1 && hasNext) {
                            viewModel.send(
                                PhotoViewUiIntent.LoadMore(
                                    item.picId,
                                    item.overallIndex,
                                    it
                                )
                            )
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    HorizontalPager(
                        state = pagerState,
                        key = { items[it].picId }
                    ) {
                        val item = items[it]
                        ViewPhoto(
                            imageUri = item.originUrl,
                            modifier = Modifier.fillMaxSize(),
                            onTap = {
                                finish()
                            },
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    ) {
                        ProvideContentColor(color = Color.White) {
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
                                    .navigationBarsPadding(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val index = pagerState.currentPage
                                if (totalAmount > 1) {
                                    val picIndex = items[index].overallIndex
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
    }

    override fun onCreateContent(systemUiController: SystemUiController) {
        systemUiController.isSystemBarsVisible = false
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