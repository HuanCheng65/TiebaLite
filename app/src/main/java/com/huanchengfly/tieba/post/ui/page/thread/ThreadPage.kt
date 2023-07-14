package com.huanchengfly.tieba.post.ui.page.thread

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChromeReaderMode
import androidx.compose.material.icons.rounded.AlignVerticalTop
import androidx.compose.material.icons.rounded.ChromeReaderMode
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Face6
import androidx.compose.material.icons.rounded.FaceRetouchingOff
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.UserActivity
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.booleanToString
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.models.protos.SimpleForum
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.post.api.renders
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.onEvent
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.arch.wrapImmutable
import com.huanchengfly.tieba.post.models.ThreadHistoryInfoBean
import com.huanchengfly.tieba.post.models.database.History
import com.huanchengfly.tieba.post.toJson
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.ui.common.PbContentRender
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.page.ProvideNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.ForumPageDestination
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.Button
import com.huanchengfly.tieba.post.ui.widgets.compose.Card
import com.huanchengfly.tieba.post.ui.widgets.compose.ConfirmDialog
import com.huanchengfly.tieba.post.ui.widgets.compose.EmoticonText
import com.huanchengfly.tieba.post.ui.widgets.compose.HorizontalDivider
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.ListMenuItem
import com.huanchengfly.tieba.post.ui.widgets.compose.LoadMoreLayout
import com.huanchengfly.tieba.post.ui.widgets.compose.MyScaffold
import com.huanchengfly.tieba.post.ui.widgets.compose.PromptDialog
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.ui.widgets.compose.TextWithMinWidth
import com.huanchengfly.tieba.post.ui.widgets.compose.TitleCentredToolbar
import com.huanchengfly.tieba.post.ui.widgets.compose.UserHeader
import com.huanchengfly.tieba.post.ui.widgets.compose.VerticalDivider
import com.huanchengfly.tieba.post.ui.widgets.compose.VerticalGrid
import com.huanchengfly.tieba.post.ui.widgets.compose.buildChipInlineContent
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberDialogState
import com.huanchengfly.tieba.post.ui.widgets.compose.states.StateScreen
import com.huanchengfly.tieba.post.utils.DateTimeUtils.getRelativeTimeString
import com.huanchengfly.tieba.post.utils.HistoryUtil
import com.huanchengfly.tieba.post.utils.StringUtil
import com.huanchengfly.tieba.post.utils.StringUtil.getShortNumString
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.TiebaUtil
import com.huanchengfly.tieba.post.utils.Util.getIconColorByLevel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.math.max

private fun getDescText(
    time: Long?,
    floor: Int,
    ipAddress: String?
): String {
    val texts = mutableListOf<String>()
    if (time != null) texts.add(getRelativeTimeString(App.INSTANCE, time))
    if (floor > 1) texts.add(App.INSTANCE.getString(R.string.tip_post_floor, floor))
    if (!ipAddress.isNullOrEmpty()) texts.add(
        App.INSTANCE.getString(
            R.string.text_ip_location,
            "$ipAddress"
        )
    )
    return texts.joinToString(" ")
}

@Composable
private fun PostAgreeBtn(
    hasAgreed: Boolean,
    agreeNum: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedColor by animateColorAsState(
        targetValue = if (hasAgreed) ExtendedTheme.colors.primary else ExtendedTheme.colors.textSecondary,
        label = "postAgreeBtnColor"
    )
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = ExtendedTheme.colors.background,
            contentColor = animatedColor
        ),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (hasAgreed) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = stringResource(id = R.string.title_agree),
                tint = animatedColor,
                modifier = Modifier.size(16.dp)
            )
            if (agreeNum > 0) {
                Text(
                    text = agreeNum.getShortNumString(),
                    color = animatedColor,
                    style = MaterialTheme.typography.caption,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BottomBarAgreeBtn(
    hasAgreed: Boolean,
    agreeNum: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (hasAgreed) ExtendedTheme.colors.primary else ExtendedTheme.colors.textSecondary
    val animatedColor by animateColorAsState(color, label = "agreeBtnColor")

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(0),
        contentPadding = PaddingValues(horizontal = 4.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = ExtendedTheme.colors.bottomBar,
            contentColor = animatedColor
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterVertically),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (hasAgreed) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = stringResource(id = R.string.title_agree),
                tint = animatedColor
            )
            if (agreeNum > 0) {
                Text(
                    text = agreeNum.getShortNumString(),
                    style = MaterialTheme.typography.caption,
                    color = animatedColor,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun BottomBarPlaceholder() {
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .background(ExtendedTheme.colors.bottomBar)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(ExtendedTheme.colors.bottomBarSurface)
                .padding(8.dp),
        ) {
            Text(
                text = stringResource(id = R.string.tip_reply_thread),
                style = MaterialTheme.typography.caption,
                color = ExtendedTheme.colors.onBottomBarSurface,
            )
        }

        BottomBarAgreeBtn(
            hasAgreed = false,
            agreeNum = 1,
            onClick = {},
            modifier = Modifier.fillMaxHeight()
        )

        Box(
            modifier = Modifier.fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = stringResource(id = R.string.btn_more),
                tint = ExtendedTheme.colors.textSecondary,
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ToggleButton(
    text: @Composable (() -> Unit),
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    backgroundColor: Color = ExtendedTheme.colors.chip,
    contentColor: Color = ExtendedTheme.colors.text,
    selectedBackgroundColor: Color = if (ThemeUtil.isNightMode()) MaterialTheme.colors.secondary.copy(
        alpha = 0.3f
    ) else MaterialTheme.colors.secondary,
    selectedContentColor: Color = if (ThemeUtil.isNightMode()) MaterialTheme.colors.secondary else MaterialTheme.colors.onSecondary,
) {
    val animatedColor by animateColorAsState(
        if (checked) selectedContentColor else contentColor,
        label = "toggleBtnColor"
    )
    val animatedBackgroundColor by animateColorAsState(
        if (checked) selectedBackgroundColor else backgroundColor,
        label = "toggleBtnBackgroundColor"
    )

    Surface(
        onClick = onClick,
        modifier = modifier,
        enabled = true,
        shape = RoundedCornerShape(6.dp),
        color = animatedBackgroundColor,
        contentColor = animatedColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (icon != null) {
                    icon()
                }
                ProvideTextStyle(
                    value = MaterialTheme.typography.subtitle1.copy(
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                    )
                ) {
                    text()
                }
            }
        }
    }
}

object ThreadPageFrom {
    const val FROM_FORUM = "forum"

    // 收藏
    const val FROM_STORE = "store_thread"
    const val FROM_PERSONALIZED = "personalized"
    const val FROM_HISTORY = "history"
}

@Serializable
sealed interface ThreadPageExtra

@Serializable
object ThreadPageNoExtra : ThreadPageExtra

@Serializable
data class ThreadPageFromStoreExtra(
    val maxPid: Long,
    val maxFloor: Int
) : ThreadPageExtra

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Destination
@Composable
fun ThreadPage(
    threadId: Long,
    navigator: DestinationsNavigator,
    forumId: Long? = null,
    postId: Long = 0,
    seeLz: Boolean = false,
    sortType: Int = 0,
    from: String = "",
    extra: ThreadPageExtra = ThreadPageNoExtra,
    threadInfo: ThreadInfo? = null,
    viewModel: ThreadViewModel = pageViewModel()
) {
    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(
            ThreadUiIntent.Init(
                threadId,
                forumId,
                postId,
                threadInfo,
                seeLz,
                sortType
            )
        )
        viewModel.send(
            ThreadUiIntent.Load(
                threadId,
                page = 0,
                postId = postId,
                forumId = forumId,
                seeLz = seeLz,
                sortType = sortType,
                from = from
            )
        )
        viewModel.initialized = true
    }
    val scaffoldState = rememberScaffoldState()
    val data by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadUiState::data,
        initial = persistentListOf()
    )
    val contentRenders by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadUiState::contentRenders,
        initial = persistentListOf()
    )
    val author by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadUiState::author,
        initial = null
    )
    val thread by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadUiState::threadInfo,
        initial = null
    )
    val firstPost by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadUiState::firstPost,
        initial = null
    )
    val forum by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadUiState::forum,
        initial = null
    )
    val user by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadUiState::user,
        initial = wrapImmutable(User())
    )
    val anti by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadUiState::anti,
        initial = null
    )
    val firstPostContentRenders by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadUiState::firstPostContentRenders,
        initial = persistentListOf()
    )
    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadUiState::isRefreshing,
        initial = false
    )
    val isLoadingMore by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadUiState::isLoadingMore,
        initial = false
    )
    val hasMore by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadUiState::hasMore,
        initial = true
    )
    val nextPagePostId by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadUiState::nextPagePostId,
        initial = 0L
    )
    val hasPrevious by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadUiState::hasPrevious,
        initial = true
    )
    val currentPageMin by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadUiState::currentPageMin,
        initial = 0
    )
    val currentPageMax by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadUiState::currentPageMax,
        initial = 0
    )
    val totalPage by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadUiState::totalPage,
        initial = 0
    )
    val isSeeLz by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadUiState::seeLz,
        initial = seeLz
    )
    val curSortType by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadUiState::sortType,
        initial = sortType
    )

    val isImmersiveMode by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadUiState::isImmersiveMode,
        initial = false
    )

    val showFirstPostContent by remember {
        derivedStateOf { firstPostContentRenders.isNotEmpty() && author != null }
    }
    val isEmpty by remember {
        derivedStateOf { data.isEmpty() && !showFirstPostContent }
    }
    val isCollected = remember(thread) {
        thread?.get { collectStatus != 0 } == true
    }
    val hasThreadAgreed = remember(thread) {
        thread?.get { agree?.hasAgree == 1 } == true
    }
    val threadTitle = remember(thread) {
        thread?.get { title } ?: ""
    }
    val lazyListState = rememberLazyListState()
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val getLastVisibilityPost = {
        data.firstOrNull { (post) ->
            lazyListState.layoutInfo.visibleItemsInfo.lastOrNull { info -> info.key is Long }?.key as Long? == post.get { id }
        }?.post ?: firstPost
    }
    val lastVisibilityPost by remember {
        derivedStateOf {
            data.firstOrNull { (post) ->
                lazyListState.layoutInfo.visibleItemsInfo.lastOrNull { info -> info.key is Long }?.key as Long? == post.get { id }
            }?.post ?: firstPost
        }
    }
    val lastVisibilityPostId = remember(lastVisibilityPost) {
        lastVisibilityPost?.get { id } ?: 0L
    }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val openBottomSheet = {
        coroutineScope.launch {
            bottomSheetState.show()
        }
    }
    val closeBottomSheet = {
        coroutineScope.launch {
            bottomSheetState.hide()
        }
    }

    BackHandler(enabled = bottomSheetState.isVisible) {
        closeBottomSheet()
    }

    viewModel.onEvent<ThreadUiEvent.ScrollToFirstReply> {
        lazyListState.animateScrollToItem(3)
    }
    viewModel.onEvent<ThreadUiEvent.AddFavoriteSuccess> {
        scaffoldState.snackbarHostState.showSnackbar(
            context.getString(R.string.message_add_favorite_success, it.floor)
        )
    }
    viewModel.onEvent<ThreadUiEvent.RemoveFavoriteSuccess> {
        scaffoldState.snackbarHostState.showSnackbar(
            context.getString(R.string.message_remove_favorite_success)
        )
    }

    val updateCollectMarkDialogState = rememberDialogState()
    var readFloorBeforeBack by remember {
        mutableStateOf(1)
    }
    ConfirmDialog(
        dialogState = updateCollectMarkDialogState,
        onConfirm = {
            coroutineScope.launch {
                val readPostId = getLastVisibilityPost()?.get { id } ?: 0L
                navigator.navigateUp()
                if (readPostId != 0L) {
                    TiebaApi.getInstance()
                        .addStoreFlow(threadId, postId)
                        .catch {
                            context.toastShort(
                                R.string.message_update_collect_mark_failed,
                                it.getErrorMessage()
                            )
                        }
                        .collect {
                            context.toastShort(R.string.message_update_collect_mark_success)
                        }
                }
            }
        },
        onCancel = {
            navigator.navigateUp()
        }
    ) {
        Text(text = stringResource(R.string.message_update_collect_mark, readFloorBeforeBack))
    }
    BackHandler(enabled = isCollected && !bottomSheetState.isVisible) {
        readFloorBeforeBack = getLastVisibilityPost()?.get { floor } ?: 0
        if (readFloorBeforeBack != 0) {
            updateCollectMarkDialogState.show()
        } else {
            navigator.navigateUp()
        }
    }

    val jumpToPageDialogState = rememberDialogState()
    PromptDialog(
        onConfirm = {
            viewModel.send(
                ThreadUiIntent.Load(
                    threadId = threadId,
                    forumId = forum?.get { id } ?: forumId,
                    page = it.toInt(),
                    seeLz = isSeeLz,
                    sortType = curSortType
                )
            )
        },
        dialogState = jumpToPageDialogState,
        onValueChange = { newVal, _ -> "^[0-9]*$".toRegex().matches(newVal) },
        title = { Text(text = stringResource(id = R.string.title_jump_page)) },
        content = {
            Text(
                text = stringResource(
                    id = R.string.tip_jump_page,
                    currentPageMax,
                    totalPage
                )
            )
        }
    )

    LaunchedEffect(Unit) {
        if (from == ThreadPageFrom.FROM_STORE && extra is ThreadPageFromStoreExtra && extra.maxPid != postId) {
            val result = scaffoldState.snackbarHostState.showSnackbar(
                context.getString(R.string.message_store_thread_update, extra.maxFloor),
                context.getString(R.string.button_load_new),
                SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.send(
                    ThreadUiIntent.Load(
                        threadId,
                        page = 0,
                        postId = extra.maxPid,
                        forumId = forumId,
                        seeLz = seeLz,
                        sortType = sortType
                    )
                )
            }
        }
    }

    var savedHistory by remember { mutableStateOf(false) }
    DisposableEffect(threadId, threadTitle, author, lastVisibilityPostId) {
        val saveHistory = {
            if (threadTitle.isNotBlank()) {
                HistoryUtil.saveHistory(
                    History(
                        title = threadTitle,
                        data = threadId.toString(),
                        type = HistoryUtil.TYPE_THREAD,
                        extras = ThreadHistoryInfoBean(
                            pid = lastVisibilityPostId.toString(),
                            isSeeLz = seeLz
                        ).toJson(),
                        avatar = StringUtil.getAvatarUrl(author?.get { portrait }),
                        username = author?.get { nameShow }
                    ),
                    async = true
                )
                savedHistory = true
            }
        }

        if (!savedHistory || lastVisibilityPostId != 0L) {
            saveHistory()
        }

        onDispose {
            saveHistory()
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            viewModel.send(
                ThreadUiIntent.LoadFirstPage(
                    threadId,
                    forumId,
                    isSeeLz,
                    curSortType
                )
            )
        }
    )

    ProvideNavigator(navigator = navigator) {
        StateScreen(
            isEmpty = isEmpty,
            isError = false,
            isLoading = isRefreshing,
            onReload = {
                viewModel.send(
                    ThreadUiIntent.Load(
                        threadId,
                        page = 0,
                        postId = postId,
                        forumId = forumId,
                        seeLz = seeLz,
                        sortType = sortType
                    )
                )
            }
        ) {
            Column {
                ModalBottomSheetLayout(
                    sheetState = bottomSheetState,
                    sheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                    sheetBackgroundColor = ExtendedTheme.colors.background,
                    sheetContent = {
                        ThreadMenu(
                            isSeeLz = isSeeLz,
                            isCollected = isCollected,
                            isImmersiveMode = isImmersiveMode,
                            isDesc = curSortType == ThreadSortType.SORT_TYPE_DESC,
                            onSeeLzClick = {
                                viewModel.send(
                                    ThreadUiIntent.LoadFirstPage(
                                        threadId,
                                        forumId,
                                        !isSeeLz,
                                        curSortType
                                    )
                                )
                                closeBottomSheet()
                            },
                            onCollectClick = {
                                if (isCollected) {
                                    val fid = forum?.get { id } ?: forumId
                                    val tbs = anti?.get { tbs }
                                    if (fid != null) {
                                        viewModel.send(
                                            ThreadUiIntent.RemoveFavorite(
                                                threadId,
                                                fid,
                                                tbs
                                            )
                                        )
                                    }
                                } else {
                                    val readItem = getLastVisibilityPost()
                                    if (readItem != null) {
                                        viewModel.send(
                                            ThreadUiIntent.AddFavorite(
                                                threadId,
                                                readItem.get { id },
                                                readItem.get { floor }
                                            )
                                        )
                                    }
                                }
                                closeBottomSheet()
                            },
                            onImmersiveModeClick = {
                                if (!isImmersiveMode && !isSeeLz) {
                                    viewModel.send(
                                        ThreadUiIntent.LoadFirstPage(
                                            threadId,
                                            forumId,
                                            true,
                                            curSortType
                                        )
                                    )
                                }
                                viewModel.send(ThreadUiIntent.ToggleImmersiveMode(!isImmersiveMode))
                                closeBottomSheet()
                            },
                            onDescClick = {
                                viewModel.send(
                                    ThreadUiIntent.LoadFirstPage(
                                        threadId,
                                        forumId,
                                        isSeeLz,
                                        if (curSortType != ThreadSortType.SORT_TYPE_DESC) ThreadSortType.SORT_TYPE_DESC else ThreadSortType.SORT_TYPE_DEFAULT
                                    )
                                )
                                closeBottomSheet()
                            },
                            onJumpPageClick = {
                                closeBottomSheet()
                                jumpToPageDialogState.show()
                            },
                            onShareClick = {
                                TiebaUtil.shareText(
                                    context,
                                    "https://tieba.baidu.com/p/$threadId",
                                    threadTitle
                                )
                            },
                            onCopyLinkClick = {
                                TiebaUtil.copyText(
                                    context,
                                    "https://tieba.baidu.com/p/$threadId?see_lz=${isSeeLz.booleanToString()}"
                                )
                            },
                            onReportClick = {
                                TiebaUtil.reportPost(
                                    context,
                                    thread?.get { postId }.toString()
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .defaultMinSize(minHeight = 1.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    MyScaffold(
                        scaffoldState = scaffoldState,
                        topBar = {
                            TopBar(
                                forum = forum,
                                onBack = { navigator.navigateUp() },
                                onForumClick = {
                                    val forumName = forum?.get { name }
                                    if (forumName != null) navigator.navigate(
                                        ForumPageDestination(
                                            forumName
                                        )
                                    )
                                },
                                modifier = Modifier.statusBarsPadding()
                            )
                        },
                    ) {
                        Box(
                            modifier = Modifier.pullRefresh(
                                state = pullRefreshState,
                                enabled = hasPrevious
                            )
                        ) {
                            LoadMoreLayout(
                                isLoading = isLoadingMore,
                                loadEnd = !hasMore,
                                onLoadMore = {
                                    viewModel.send(
                                        ThreadUiIntent.LoadMore(
                                            threadId = threadId,
                                            page = if (curSortType == ThreadSortType.SORT_TYPE_DESC) totalPage - currentPageMax
                                            else currentPageMax + 1,
                                            forumId = forumId,
                                            postId = nextPagePostId,
                                            seeLz = isSeeLz,
                                            sortType = curSortType,
                                            postIds = data.map { it.post.get { id } }
                                        )
                                    )
                                }
                            ) {
                                LazyColumn(
                                    state = lazyListState,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    item(key = "FirstPost") {
                                        if (showFirstPostContent && firstPost != null) {
                                            Column {
                                                PostCard(
                                                    postHolder = firstPost!!,
                                                    contentRenders = firstPostContentRenders,
                                                    immersiveMode = isImmersiveMode,
                                                )

                                                VerticalDivider(
                                                    modifier = Modifier
                                                        .padding(horizontal = 16.dp)
                                                        .padding(bottom = 8.dp),
                                                    thickness = 2.dp
                                                )
                                            }
                                        }
                                    }
                                    stickyHeader(key = "ThreadHeader") {
                                        Row(
                                            modifier = Modifier
                                                .background(MaterialTheme.colors.background)
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = stringResource(
                                                    R.string.title_thread_header,
                                                    "${thread?.get { replyNum } ?: 0}"),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ExtendedTheme.colors.text,
                                                modifier = Modifier.padding(horizontal = 8.dp),
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(
                                                text = stringResource(R.string.text_all),
                                                modifier = Modifier
                                                    .padding(horizontal = 8.dp)
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = null,
                                                        enabled = isSeeLz
                                                    ) {
                                                        if (isSeeLz) {
                                                            viewModel.send(
                                                                ThreadUiIntent.LoadFirstPage(
                                                                    threadId = threadId,
                                                                    forumId = forumId,
                                                                    seeLz = false,
                                                                    sortType = curSortType
                                                                )
                                                            )
                                                        }
                                                    },
                                                fontSize = 13.sp,
                                                fontWeight = if (!isSeeLz) FontWeight.SemiBold else FontWeight.Normal,
                                                color = if (!isSeeLz) ExtendedTheme.colors.text else ExtendedTheme.colors.textSecondary,
                                            )
                                            HorizontalDivider()
                                            Text(
                                                text = stringResource(R.string.title_see_lz),
                                                modifier = Modifier
                                                    .padding(horizontal = 8.dp)
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = null,
                                                        enabled = !isSeeLz
                                                    ) {
                                                        if (!isSeeLz) {
                                                            viewModel.send(
                                                                ThreadUiIntent.LoadFirstPage(
                                                                    threadId = threadId,
                                                                    forumId = forumId,
                                                                    seeLz = true,
                                                                    sortType = curSortType
                                                                )
                                                            )
                                                        }
                                                    },
                                                fontSize = 13.sp,
                                                fontWeight = if (isSeeLz) FontWeight.SemiBold else FontWeight.Normal,
                                                color = if (isSeeLz) ExtendedTheme.colors.text else ExtendedTheme.colors.textSecondary,
                                            )
                                        }
                                    }
                                    item(key = "LoadPreviousBtn") {
                                        if (hasPrevious) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        viewModel.send(
                                                            ThreadUiIntent.LoadPrevious(
                                                                threadId,
                                                                max(currentPageMax - 1, 1),
                                                                forumId,
                                                                postId = data
                                                                    .first()
                                                                    .post
                                                                    .get { id },
                                                                seeLz = isSeeLz,
                                                                sortType = curSortType,
                                                                postIds = data.map { it.post.get { id } }
                                                            )
                                                        )
                                                    }
                                                    .padding(8.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.AlignVerticalTop,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Text(
                                                    text = stringResource(id = R.string.btn_load_previous),
                                                    color = ExtendedTheme.colors.text,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }
                                    itemsIndexed(
                                        items = data,
                                        key = { _, (item) -> item.get { id } }
                                    ) { index, (item, blocked) ->
                                        PostCard(
                                            postHolder = item,
                                            contentRenders = contentRenders[index],
                                            threadAuthorId = author?.get { id } ?: 0L,
                                            blocked = blocked,
                                            immersiveMode = isImmersiveMode,
                                            onAgree = {
                                                val postHasAgreed =
                                                    item.get { agree?.hasAgree == 1 }
                                                viewModel.send(
                                                    ThreadUiIntent.AgreePost(
                                                        threadId = threadId,
                                                        postId = item.get { id },
                                                        agree = !postHasAgreed
                                                    )
                                                )
                                            }
                                        )
                                    }
                                    if (data.isEmpty()) {
                                        item(key = "EmptyReplyTip") {
                                            if (!isRefreshing) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                                ) {
                                                    val composition by rememberLottieComposition(
                                                        LottieCompositionSpec.RawRes(R.raw.lottie_empty_box)
                                                    )
                                                    LottieAnimation(
                                                        composition = composition,
                                                        iterations = LottieConstants.IterateForever,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .aspectRatio(2f)
                                                    )
                                                    Text(
                                                        text = stringResource(id = R.string.title_empty),
                                                        style = MaterialTheme.typography.h6.copy(
                                                            color = ExtendedTheme.colors.text,
                                                            fontWeight = FontWeight.Bold,
                                                            textAlign = TextAlign.Center
                                                        )
                                                    )
                                                    Button(onClick = { reload() }) {
                                                        Text(text = stringResource(id = R.string.btn_refresh))
                                                    }
                                                }
                                            }
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

                BottomBar(
                    user = user,
                    onClickReply = { /*TODO*/ },
                    onAgree = {
                        val firstPostId =
                            thread?.get { firstPostId }.takeIf { it != 0L } ?: firstPost?.get { id }
                            ?: 0L
                        if (firstPostId != 0L) viewModel.send(
                            ThreadUiIntent.AgreeThread(
                                threadId,
                                firstPostId,
                                !hasThreadAgreed
                            )
                        )
                    },
                    onClickMore = {
                        if (bottomSheetState.isVisible) {
                            closeBottomSheet()
                        } else {
                            openBottomSheet()
                        }
                    },
                    hasAgreed = hasThreadAgreed,
                    agreeNum = thread?.get { agree?.diffAgreeNum } ?: 0L,
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    forum: ImmutableHolder<SimpleForum>?,
    onBack: () -> Unit,
    onForumClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TitleCentredToolbar(
        title = {
            forum?.let {
                if (forum.get { name }.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .height(IntrinsicSize.Min)
                            .clip(RoundedCornerShape(100))
                            .background(ExtendedTheme.colors.chip)
                            .clickable(onClick = onForumClick)
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Avatar(
                            data = forum.get { avatar },
                            contentDescription = it.get { name },
                            modifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(1f)
                        )

                        Text(
                            text = stringResource(id = R.string.title_forum, it.get { name }),
                            fontSize = 14.sp,
                            color = ExtendedTheme.colors.text,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        },
        navigationIcon = {
            BackNavigationIcon(onBack)
        },
        modifier = modifier
    )
}

@Composable
private fun BottomBar(
    user: ImmutableHolder<User>,
    onClickReply: () -> Unit,
    onAgree: () -> Unit,
    onClickMore: () -> Unit,
    modifier: Modifier = Modifier,
    hasAgreed: Boolean = false,
    agreeNum: Long = 0,
) {
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .background(ExtendedTheme.colors.bottomBar)
            .padding(horizontal = 16.dp)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (user.get { is_login } == 1) {
            Avatar(
                data = StringUtil.getAvatarUrl(user.get { portrait }),
                size = Sizes.Tiny,
                contentDescription = user.get { name },
                modifier = Modifier
                    .padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ExtendedTheme.colors.bottomBarSurface)
                    .clickable(onClick = onClickReply)
                    .padding(8.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.tip_reply_thread),
                    style = MaterialTheme.typography.caption,
                    color = ExtendedTheme.colors.onBottomBarSurface,
                )
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        BottomBarAgreeBtn(
            hasAgreed = hasAgreed,
            agreeNum = agreeNum,
            onClick = onAgree,
            modifier = Modifier.fillMaxHeight()
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .clickable(onClick = onClickMore)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = stringResource(id = R.string.btn_more),
                tint = ExtendedTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
fun PostCard(
    postHolder: ImmutableHolder<Post>,
    contentRenders: ImmutableList<PbContentRender>,
    threadAuthorId: Long = 0L,
    blocked: Boolean = false,
    immersiveMode: Boolean = false,
    onAgree: () -> Unit = {},
    onReply: () -> Unit = {},
) {
    val (post) = postHolder
    if (blocked && !immersiveMode) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(ExtendedTheme.colors.floorCard)
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.tip_blocked_post, postHolder.get { floor }),
                style = MaterialTheme.typography.caption,
                color = ExtendedTheme.colors.textSecondary
            )
        }
        return
    }
    val hasPadding = remember(key1 = postHolder, key2 = immersiveMode) {
        postHolder.get { floor > 1 } && !immersiveMode
    }
    val paddingModifier = Modifier.padding(start = if (hasPadding) Sizes.Small + 8.dp else 0.dp)
    val context = LocalContext.current
    val accentColor = ExtendedTheme.colors.accent
    val author = postHolder.get { author!! }
    val showTitle by remember(key1 = post.title, key2 = post.floor) {
        derivedStateOf { post.title.isNotBlank() && post.floor <= 1 }
    }
    val hasAgreed by remember(key1 = post.agree?.hasAgree) {
        derivedStateOf { post.agree?.hasAgree == 1 }
    }
    val agreeNum by remember(key1 = post.agree?.diffAgreeNum) {
        derivedStateOf { post.agree?.diffAgreeNum ?: 0L }
    }
    val subPosts = remember(key1 = post.sub_post_list) {
        post.sub_post_list?.sub_post_list?.toImmutableList() ?: persistentListOf()
    }
    val subPostContents = remember(key1 = subPosts, key2 = accentColor) {
        subPosts.map { subPostList ->
            val userNameString = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(
                        StringUtil.getUsernameAnnotatedString(
                            context,
                            subPostList.author?.name ?: "",
                            subPostList.author?.nameShow
                        )
                    )
                    append(": ")
                }
            }
            val contentStrings = subPostList.content.renders.map { it.toAnnotationString() }

            userNameString + contentStrings.reduce { acc, annotatedString -> acc + annotatedString }
        }
    }
    Card(
        header = {
            if (!immersiveMode) {
                UserHeader(
                    avatar = {
                        Avatar(
                            data = StringUtil.getAvatarUrl(author.portrait),
                            size = Sizes.Small,
                            contentDescription = null
                        )
                    },
                    name = {
                        UserNameText(
                            userName = StringUtil.getUsernameAnnotatedString(
                                LocalContext.current,
                                author.name,
                                author.nameShow
                            ),
                            userLevel = author.level_id,
                            isLz = author.id == threadAuthorId
                        )
                    },
                    desc = {
                        Text(text = getDescText(post.time.toLong(), post.floor, author.ip_address))
                    },
                    onClick = {
                        UserActivity.launch(context, author.id.toString())
                    }
                ) {
                    if (post.floor > 1) {
                        PostAgreeBtn(
                            hasAgreed = hasAgreed,
                            agreeNum = agreeNum,
                            onClick = onAgree
                        )
                    }
                }
            }
        },
        content = {
            SelectionContainer {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = paddingModifier
                ) {
                    if (showTitle) {
                        Text(
                            text = post.title,
                            style = MaterialTheme.typography.subtitle1,
                            fontSize = 15.sp
                        )
                    }

                    contentRenders.forEach { it.Render() }
                }
            }

            if (post.sub_post_number > 0 && !immersiveMode) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(paddingModifier)
                        .clip(RoundedCornerShape(6.dp))
                        .background(ExtendedTheme.colors.floorCard)
                        .padding(vertical = 12.dp)
                ) {
                    subPostContents.forEach {
                        EmoticonText(
                            text = it,
                            modifier = Modifier.padding(horizontal = 12.dp),
                            color = ExtendedTheme.colors.text,
                            fontSize = 13.sp,
                            style = MaterialTheme.typography.body2,
                            emoticonSize = 0.9f
                        )
                    }

                    if (post.sub_post_number > subPostContents.size) {
                        Text(
                            text = stringResource(
                                id = R.string.open_all_sub_posts,
                                post.sub_post_number
                            ),
                            style = MaterialTheme.typography.caption,
                            fontSize = 13.sp,
                            color = ExtendedTheme.colors.primary,
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun UserNameText(
    userName: AnnotatedString,
    userLevel: Int,
    modifier: Modifier = Modifier,
    isLz: Boolean = false,
    bawuType: String? = null,
) {
    val text = buildAnnotatedString {
        append(userName)
        append(" ")
        appendInlineContent("Level", alternateText = "$userLevel")
        if (!bawuType.isNullOrBlank()) {
            append(" ")
            appendInlineContent("Bawu", alternateText = bawuType)
        }
        if (isLz) {
            append(" ")
            appendInlineContent("Lz")
        }
    }
    Text(
        text = text,
        inlineContent = mapOf(
            "Level" to buildChipInlineContent(
                "18",
                color = Color(getIconColorByLevel("$userLevel")),
                backgroundColor = Color(getIconColorByLevel("$userLevel")).copy(alpha = 0.25f)
            ),
            "Bawu" to buildChipInlineContent(
                bawuType ?: "",
                color = ExtendedTheme.colors.accent,
                backgroundColor = ExtendedTheme.colors.accent.copy(alpha = 0.25f)
            ),
            "Lz" to buildChipInlineContent(stringResource(id = R.string.tip_lz)),
        ),
        modifier = modifier
    )
}

@Composable
private fun ThreadMenu(
    isSeeLz: Boolean,
    isCollected: Boolean,
    isImmersiveMode: Boolean,
    isDesc: Boolean,
    onSeeLzClick: () -> Unit,
    onCollectClick: () -> Unit,
    onImmersiveModeClick: () -> Unit,
    onDescClick: () -> Unit,
    onJumpPageClick: () -> Unit,
    onShareClick: () -> Unit,
    onCopyLinkClick: () -> Unit,
    onReportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(4.dp)
                .fillMaxWidth(0.25f)
                .clip(RoundedCornerShape(100))
                .background(ExtendedTheme.colors.chip)
        )
        VerticalGrid(
            column = 2,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            rowModifier = Modifier.height(IntrinsicSize.Min),
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            item {
                ToggleButton(
                    text = {
                        TextWithMinWidth(
                            text = stringResource(id = R.string.title_see_lz),
                            minLength = 4
                        )
                    },
                    checked = isSeeLz,
                    onClick = onSeeLzClick,
                    icon = {
                        Icon(
                            imageVector = if (isSeeLz) Icons.Rounded.Face6 else Icons.Rounded.FaceRetouchingOff,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            item {
                ToggleButton(
                    text = {
                        TextWithMinWidth(
                            text = stringResource(
                                id = if (isCollected) R.string.title_collected else R.string.title_uncollected
                            ),
                            minLength = 4
                        )
                    },
                    checked = isCollected,
                    onClick = onCollectClick,
                    icon = {
                        Icon(
                            imageVector = if (isCollected) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            item {
                ToggleButton(
                    text = {
                        TextWithMinWidth(
                            text = stringResource(
                                id = R.string.title_pure_read
                            ),
                            minLength = 4
                        )
                    },
                    checked = isImmersiveMode,
                    onClick = onImmersiveModeClick,
                    icon = {
                        Icon(
                            imageVector = if (isImmersiveMode) Icons.Rounded.ChromeReaderMode else Icons.Outlined.ChromeReaderMode,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            item {
                ToggleButton(
                    text = {
                        TextWithMinWidth(
                            text = stringResource(
                                id = R.string.title_sort
                            ),
                            minLength = 4
                        )
                    },
                    checked = isDesc,
                    onClick = onDescClick,
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Sort,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Column {
            ListMenuItem(
                icon = Icons.Rounded.RocketLaunch,
                text = stringResource(id = R.string.title_jump_page),
                iconColor = ExtendedTheme.colors.text,
                onClick = onJumpPageClick,
                modifier = Modifier.fillMaxWidth(),
            )
            ListMenuItem(
                icon = Icons.Rounded.Share,
                text = stringResource(id = R.string.title_share),
                iconColor = ExtendedTheme.colors.text,
                onClick = onShareClick,
                modifier = Modifier.fillMaxWidth(),
            )
            ListMenuItem(
                icon = Icons.Rounded.ContentCopy,
                text = stringResource(id = R.string.title_copy_link),
                iconColor = ExtendedTheme.colors.text,
                onClick = onCopyLinkClick,
                modifier = Modifier.fillMaxWidth(),
            )
            ListMenuItem(
                icon = Icons.Rounded.Report,
                text = stringResource(id = R.string.title_report),
                iconColor = ExtendedTheme.colors.text,
                onClick = onReportClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
