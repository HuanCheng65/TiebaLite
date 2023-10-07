package com.huanchengfly.tieba.post.ui.page.forum.threadlist

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.protos.OriginThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.post.api.models.protos.abstractText
import com.huanchengfly.tieba.post.api.models.protos.frsPage.Classify
import com.huanchengfly.tieba.post.arch.BaseComposeActivity.Companion.LocalWindowSizeClass
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.onEvent
import com.huanchengfly.tieba.post.arch.onGlobalEvent
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.compose.pullRefreshIndicator
import com.huanchengfly.tieba.post.ui.common.windowsizeclass.WindowWidthSizeClass
import com.huanchengfly.tieba.post.ui.models.ThreadItemData
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.ForumRuleDetailPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ThreadPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.UserProfilePageDestination
import com.huanchengfly.tieba.post.ui.page.forum.getSortType
import com.huanchengfly.tieba.post.ui.widgets.Chip
import com.huanchengfly.tieba.post.ui.widgets.compose.BlockTip
import com.huanchengfly.tieba.post.ui.widgets.compose.BlockableContent
import com.huanchengfly.tieba.post.ui.widgets.compose.FeedCard
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.LoadMoreLayout
import com.huanchengfly.tieba.post.ui.widgets.compose.LocalSnackbarHostState
import com.huanchengfly.tieba.post.ui.widgets.compose.MyLazyColumn
import com.huanchengfly.tieba.post.ui.widgets.compose.VerticalDivider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

private fun getFirstLoadIntent(
    context: Context,
    forumName: String,
    isGood: Boolean = false,
): ForumThreadListUiIntent {
    return if (isGood) ForumThreadListUiIntent.Refresh(forumName, -1, 0)
    else ForumThreadListUiIntent.FirstLoad(forumName, getSortType(context, forumName), null)
}

private fun getRefreshIntent(
    context: Context,
    forumName: String,
    isGood: Boolean = false,
    sortType: Int = getSortType(context, forumName),
    goodClassifyId: Int? = if (isGood) 0 else null,
): ForumThreadListUiIntent {
    return if (isGood) ForumThreadListUiIntent.Refresh(forumName, -1, goodClassifyId)
    else ForumThreadListUiIntent.Refresh(forumName, sortType, null)
}

private fun getLoadMoreIntent(
    context: Context,
    forumId: Long,
    forumName: String,
    page: Int,
    threadListIds: List<Long>,
    isGood: Boolean = false,
): ForumThreadListUiIntent {
    return if (isGood) ForumThreadListUiIntent.LoadMore(forumId, forumName, page, threadListIds, 0)
    else ForumThreadListUiIntent.LoadMore(
        forumId,
        forumName,
        page,
        threadListIds,
        getSortType(context, forumName)
    )
}

private enum class ItemType {
    Top, PlainText, SingleMedia, MultiMedia, Video
}

@Composable
private fun GoodClassifyTabs(
    goodClassifyHoldersProvider: () -> List<ImmutableHolder<Classify>>,
    selectedItem: Int?,
    onSelected: (Int) -> Unit,
) {
    val goodClassifyHolders = goodClassifyHoldersProvider()
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        goodClassifyHolders.forEach { holder ->
            val (classify) = holder
            Chip(
                text = classify.class_name,
                modifier = Modifier
                    .clip(RoundedCornerShape(100))
                    .clickable {
                        onSelected(classify.class_id)
                    },
                invertColor = selectedItem == classify.class_id
            )
        }
    }
}

@Composable
private fun TopThreadItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: String = stringResource(id = R.string.content_top),
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Chip(
            text = type,
            shape = RoundedCornerShape(3.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.subtitle2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            fontSize = 15.sp
        )
    }
}

@Composable
private fun ThreadList(
    state: LazyListState,
    items: ImmutableList<ThreadItemData>,
    isGood: Boolean,
    goodClassifyId: Int?,
    goodClassifyHoldersProvider: () -> List<ImmutableHolder<Classify>>,
    onItemClicked: (ThreadInfo) -> Unit,
    onItemReplyClicked: (ThreadInfo) -> Unit,
    onAgree: (ThreadInfo) -> Unit,
    onClassifySelected: (Int) -> Unit,
    forumRuleTitle: String? = null,
    onOpenForumRule: (() -> Unit)? = null,
    onOriginThreadClicked: (OriginThreadInfo) -> Unit = {},
    onUserClicked: (User) -> Unit = {},
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val itemFraction = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Expanded -> 0.5f
        else -> 1f
    }
    MyLazyColumn(
        state = state,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = WindowInsets.navigationBars.asPaddingValues()
    ) {
        if (isGood) {
            item(key = "GoodClassifyHeader") {
                GoodClassifyTabs(
                    goodClassifyHoldersProvider = goodClassifyHoldersProvider,
                    selectedItem = goodClassifyId,
                    onSelected = onClassifySelected
                )
            }
        }
        if (!forumRuleTitle.isNullOrEmpty()) {
            item(key = "ForumRule") {
                TopThreadItem(
                    title = forumRuleTitle,
                    onClick = {
                        onOpenForumRule?.invoke()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    type = stringResource(id = R.string.desc_forum_rule)
                )
            }
        }
        itemsIndexed(
            items = items,
            key = { index, (holder) ->
                val (item) = holder
                "${index}_${item.id}"
            },
            contentType = { _, (holder) ->
                val (item) = holder
                if (item.isTop == 1) ItemType.Top
                else {
                    if (item.media.isNotEmpty())
                        if (item.media.size == 1) ItemType.SingleMedia else ItemType.MultiMedia
                    else if (item.videoInfo != null)
                        ItemType.Video
                    else ItemType.PlainText
                }
            }
        ) { index, (holder, blocked) ->
            BlockableContent(
                blocked = blocked,
                blockedTip = { BlockTip(text = { Text(text = stringResource(id = R.string.tip_blocked_thread)) }) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
            ) {
                val (item) = holder
                Column(
                    modifier = Modifier.fillMaxWidth(itemFraction)
                ) {
                    if (item.isTop == 1) {
                        val title = item.title.takeUnless { it.isBlank() } ?: item.abstractText
                        TopThreadItem(
                            title = title,
                            onClick = { onItemClicked(item) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        if (index > 0) {
                            if (items[index - 1].thread.get { isTop } == 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            VerticalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                        FeedCard(
                            item = holder,
                            onClick = onItemClicked,
                            onClickReply = onItemReplyClicked,
                            onAgree = onAgree,
                            onClickOriginThread = onOriginThreadClicked,
                            onClickUser = onUserClicked
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ForumThreadListPage(
    forumId: Long,
    forumName: String,
    isGood: Boolean = false,
    viewModel: ForumThreadListViewModel = if (isGood) pageViewModel<GoodThreadListViewModel>() else pageViewModel<LatestThreadListViewModel>()
) {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val snackbarHostState = LocalSnackbarHostState.current

    val lazyListState = rememberLazyListState()

    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(getFirstLoadIntent(context, forumName, isGood))
        viewModel.initialized = true
    }
    onGlobalEvent<ForumThreadListUiEvent.Refresh>(
        filter = { it.isGood == isGood },
    ) {
        viewModel.send(getRefreshIntent(context, forumName, isGood, it.sortType))
    }
    onGlobalEvent<ForumThreadListUiEvent.BackToTop>(
        filter = { it.isGood == isGood },
    ) {
        lazyListState.animateScrollToItem(0)
    }
    viewModel.onEvent<ForumThreadListUiEvent.AgreeFail> {
        val snackbarResult = snackbarHostState.showSnackbar(
            message = context.getString(
                R.string.snackbar_agree_fail,
                it.errorCode,
                it.errorMsg
            ),
            actionLabel = context.getString(R.string.button_retry)
        )

        if (snackbarResult == SnackbarResult.ActionPerformed) {
            viewModel.send(
                ForumThreadListUiIntent.Agree(
                    it.threadId,
                    it.postId,
                    it.hasAgree
                )
            )
        }
    }
    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = ForumThreadListUiState::isRefreshing,
        initial = false
    )
    val isLoadingMore by viewModel.uiState.collectPartialAsState(
        prop1 = ForumThreadListUiState::isLoadingMore,
        initial = false
    )
    val hasMore by viewModel.uiState.collectPartialAsState(
        prop1 = ForumThreadListUiState::hasMore,
        initial = true
    )
    val currentPage by viewModel.uiState.collectPartialAsState(
        prop1 = ForumThreadListUiState::currentPage,
        initial = 1
    )
    val forumRuleTitle by viewModel.uiState.collectPartialAsState(
        prop1 = ForumThreadListUiState::forumRuleTitle,
        initial = null
    )
    val threadList by viewModel.uiState.collectPartialAsState(
        prop1 = ForumThreadListUiState::threadList,
        initial = persistentListOf()
    )
    val threadListIds by viewModel.uiState.collectPartialAsState(
        prop1 = ForumThreadListUiState::threadListIds,
        initial = persistentListOf()
    )
    val goodClassifyId by viewModel.uiState.collectPartialAsState(
        prop1 = ForumThreadListUiState::goodClassifyId,
        initial = null
    )
    val goodClassifies by viewModel.uiState.collectPartialAsState(
        prop1 = ForumThreadListUiState::goodClassifies,
        initial = persistentListOf()
    )
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.send(getRefreshIntent(context, forumName, isGood)) }
    )
    Box {
        LoadMoreLayout(
            isLoading = isLoadingMore,
            onLoadMore = {
                viewModel.send(
                    getLoadMoreIntent(
                        context,
                        forumId,
                        forumName,
                        currentPage,
                        threadListIds,
                        isGood
                    )
                )
            },
            loadEnd = !hasMore,
            lazyListState = lazyListState,
            isEmpty = threadList.isEmpty(),
        ) {
            ThreadList(
                state = lazyListState,
                items = threadList,
                isGood = isGood,
                goodClassifyId = goodClassifyId,
                goodClassifyHoldersProvider = { goodClassifies },
                onItemClicked = {
                    navigator.navigate(
                        ThreadPageDestination(
                            it.threadId,
                            forumId = it.forumId,
                            threadInfo = it
                        )
                    )
                },
                onAgree = {
                    viewModel.send(
                        ForumThreadListUiIntent.Agree(
                            it.threadId,
                            it.firstPostId,
                            it.agree?.hasAgree ?: 0
                        )
                    )
                },
                onItemReplyClicked = {
                    navigator.navigate(
                        ThreadPageDestination(
                            it.threadId,
                            forumId = it.forumId,
                            scrollToReply = true
                        )
                    )
                },
                onClassifySelected = {
                    viewModel.send(
                        getRefreshIntent(
                            context,
                            forumName,
                            true,
                            goodClassifyId = it
                        )
                    )
                },
                forumRuleTitle = forumRuleTitle,
                onOpenForumRule = {
                    navigator.navigate(ForumRuleDetailPageDestination(forumId))
                },
                onOriginThreadClicked = {
                    navigator.navigate(
                        ThreadPageDestination(
                            threadId = it.tid.toLong(),
                            forumId = it.fid,
                        )
                    )
                },
                onUserClicked = { navigator.navigate(UserProfilePageDestination(it.id)) }
            )
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = ExtendedTheme.colors.pullRefreshIndicator,
            contentColor = ExtendedTheme.colors.primary,
        )
    }
}