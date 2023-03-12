package com.huanchengfly.tieba.post.ui.page.threadstore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.ThreadActivity
import com.huanchengfly.tieba.post.activities.UserActivity
import com.huanchengfly.tieba.post.api.models.ThreadStoreBean
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.onEvent
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.dpToPxFloat
import com.huanchengfly.tieba.post.pxToSp
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.ErrorScreen
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.LoadMoreLayout
import com.huanchengfly.tieba.post.ui.widgets.compose.LongClickMenu
import com.huanchengfly.tieba.post.ui.widgets.compose.MyScaffold
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.ui.widgets.compose.TitleCentredToolbar
import com.huanchengfly.tieba.post.ui.widgets.compose.UserHeader
import com.huanchengfly.tieba.post.ui.widgets.compose.states.StateScreen
import com.huanchengfly.tieba.post.utils.StringUtil
import com.huanchengfly.tieba.post.utils.StringUtil.getUsernameAnnotatedString
import com.huanchengfly.tieba.post.utils.appPreferences
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

private val UpdateTipTextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 10.sp)

@OptIn(ExperimentalMaterialApi::class, ExperimentalTextApi::class)
@Destination(
    deepLinks = [
        DeepLink(uriPattern = "tblite://favorite")
    ]
)
@Composable
fun ThreadStorePage(
    navigator: DestinationsNavigator,
    viewModel: ThreadStoreViewModel = pageViewModel()
) {
    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(ThreadStoreUiIntent.Refresh)
        viewModel.initialized = true
    }
    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadStoreUiState::isRefreshing,
        initial = false
    )
    val isLoadingMore by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadStoreUiState::isLoadingMore,
        initial = false
    )
    val hasMore by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadStoreUiState::hasMore,
        initial = true
    )
    val currentPage by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadStoreUiState::currentPage,
        initial = 0
    )
    val data by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadStoreUiState::data,
        initial = emptyList()
    )
    val error by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadStoreUiState::error,
        initial = null
    )
    val isError by remember { derivedStateOf { error != null } }

    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    viewModel.onEvent<ThreadStoreUiEvent.Delete.Failure> {
        scaffoldState.snackbarHostState.showSnackbar(
            context.getString(
                R.string.delete_store_failure,
                it.errorMsg
            )
        )
    }
    viewModel.onEvent<ThreadStoreUiEvent.Delete.Success> {
        scaffoldState.snackbarHostState.showSnackbar(context.getString(R.string.delete_store_success))
    }
    MyScaffold(
        backgroundColor = Color.Transparent,
        scaffoldState = scaffoldState,
        topBar = {
            TitleCentredToolbar(
                title = stringResource(id = R.string.title_my_collect),
                navigationIcon = {
                    BackNavigationIcon(onBackPressed = { navigator.navigateUp() })
                }
            )
        }
    ) { contentPaddings ->
        val textMeasurer = rememberTextMeasurer()

        StateScreen(
            isEmpty = data.isEmpty(),
            isError = isError,
            isLoading = isRefreshing,
            modifier = Modifier.padding(contentPaddings),
            onReload = {
                viewModel.send(ThreadStoreUiIntent.Refresh)
            },
            errorScreen = {
                error?.let {
                    val (e) = it
                    ErrorScreen(error = e)
                }
            }
        ) {
            val pullRefreshState = rememberPullRefreshState(
                refreshing = isRefreshing,
                onRefresh = { viewModel.send(ThreadStoreUiIntent.Refresh) }
            )

            Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
                LoadMoreLayout(
                    isLoading = isLoadingMore,
                    onLoadMore = { viewModel.send(ThreadStoreUiIntent.LoadMore(currentPage + 1)) },
                    loadEnd = !hasMore
                ) {
                    LazyColumn {
                        items(
                            items = data,
                            key = { it.threadId }
                        ) { info ->
                            LongClickMenu(
                                menuContent = {
                                    DropdownMenuItem(onClick = {
                                        viewModel.send(
                                            ThreadStoreUiIntent.Delete(
                                                info.threadId
                                            )
                                        )
                                    }) {
                                        Text(text = stringResource(id = R.string.title_collect_on))
                                    }
                                },
                                onClick = {
                                    ThreadActivity.launch(
                                        context,
                                        info.threadId,
                                        info.markPid,
                                        context.appPreferences.collectThreadSeeLz,
                                        "collect",
                                        info.maxPid
                                    )
                                }
                            ) {
                                StoreItem(
                                    info = info,
                                    onUserClick = {
                                        info.author.lzUid?.let {
                                            UserActivity.launch(
                                                context,
                                                it,
                                                StringUtil.getAvatarUrl(info.author.userPortrait)
                                            )
                                        }
                                    },
                                    textMeasurer = textMeasurer
                                )
                            }
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun StoreItem(
    info: ThreadStoreBean.ThreadStoreInfo,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier,
    textMeasurer: TextMeasurer = rememberTextMeasurer()
) {
    val hasUpdate = info.count != "0" && info.postNo != "0"
    var width = 0
    var height = 0
    Column(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        UserHeader(
            avatar = {
                Avatar(
                    data = StringUtil.getAvatarUrl(info.author.userPortrait),
                    size = Sizes.Small,
                    contentDescription = null
                )
            },
            name = {
                Text(
                    text = getUsernameAnnotatedString(
                        LocalContext.current,
                        info.author.name ?: "",
                        info.author.nameShow,
                        LocalContentColor.current
                    )
                )
            },
            onClick = onUserClick,
        )
        val title = buildAnnotatedString {
            append(info.title)
            if (hasUpdate) {
                append(" ")
                appendInlineContent("Update", info.postNo)
            }
        }
        val updateTip = stringResource(
            id = R.string.tip_thread_store_update,
            info.postNo
        )
        if (hasUpdate) {
            val result = textMeasurer.measure(
                AnnotatedString(updateTip),
                style = UpdateTipTextStyle
            )
            width =
                result.size.width.pxToSp() + 12F.dpToPxFloat().pxToSp() * 2 + 1
            height = result.size.height.pxToSp() + 4F.dpToPxFloat().pxToSp() * 2
        }
        Text(
            text = title,
            fontSize = 15.sp,
            color = ExtendedTheme.colors.text,
            inlineContent = mapOf(
                "Update" to InlineTextContent(
                    placeholder = Placeholder(
                        width = width.sp,
                        height = height.sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                    ),
                    children = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = ExtendedTheme.colors.chip,
                                    shape = RoundedCornerShape(3.dp)
                                )
                                .padding(vertical = 4.dp, horizontal = 12.dp)
                        ) {
                            Text(
                                text = updateTip,
                                style = UpdateTipTextStyle,
                                color = ExtendedTheme.colors.onChip,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                )
            )
        )
    }
}