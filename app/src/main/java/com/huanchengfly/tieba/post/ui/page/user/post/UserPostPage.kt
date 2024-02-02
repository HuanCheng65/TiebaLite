package com.huanchengfly.tieba.post.ui.page.user.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.protos.PostInfoList
import com.huanchengfly.tieba.post.arch.GlobalEvent
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.getOrNull
import com.huanchengfly.tieba.post.arch.onGlobalEvent
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.compose.pullRefreshIndicator
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.ForumPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.SubPostsPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ThreadPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.UserProfilePageDestination
import com.huanchengfly.tieba.post.ui.widgets.compose.Button
import com.huanchengfly.tieba.post.ui.widgets.compose.Card
import com.huanchengfly.tieba.post.ui.widgets.compose.Container
import com.huanchengfly.tieba.post.ui.widgets.compose.ErrorScreen
import com.huanchengfly.tieba.post.ui.widgets.compose.FeedCard
import com.huanchengfly.tieba.post.ui.widgets.compose.FeedCardPlaceholder
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.LoadMoreLayout
import com.huanchengfly.tieba.post.ui.widgets.compose.MyLazyColumn
import com.huanchengfly.tieba.post.ui.widgets.compose.TipScreen
import com.huanchengfly.tieba.post.ui.widgets.compose.UserHeader
import com.huanchengfly.tieba.post.ui.widgets.compose.states.StateScreen
import com.huanchengfly.tieba.post.utils.DateTimeUtils
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UserPostPage(
    uid: Long,
    isThread: Boolean = true,
    fluid: Boolean = false,
    enablePullRefresh: Boolean = false,
    viewModel: UserPostViewModel = pageViewModel(key = if (isThread) "user_thread_$uid" else "user_post_$uid"),
) {
    val navigator = LocalNavigator.current

    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(UserPostUiIntent.Refresh(uid, isThread))
        viewModel.initialized = true
    }

    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = UserPostUiState::isRefreshing,
        initial = true
    )
    val isLoadingMore by viewModel.uiState.collectPartialAsState(
        prop1 = UserPostUiState::isLoadingMore,
        initial = false
    )
    val error by viewModel.uiState.collectPartialAsState(
        prop1 = UserPostUiState::error,
        initial = null
    )
    val currentPage by viewModel.uiState.collectPartialAsState(
        prop1 = UserPostUiState::currentPage,
        initial = 1
    )
    val hasMore by viewModel.uiState.collectPartialAsState(
        prop1 = UserPostUiState::hasMore,
        initial = false
    )
    val posts by viewModel.uiState.collectPartialAsState(
        prop1 = UserPostUiState::posts,
        initial = persistentListOf()
    )
    val hidePost by viewModel.uiState.collectPartialAsState(
        prop1 = UserPostUiState::hidePost,
        initial = false
    )

    val isEmpty by remember {
        derivedStateOf { posts.isEmpty() }
    }
    val isError by remember {
        derivedStateOf { error != null }
    }

    onGlobalEvent<GlobalEvent.Refresh>(
        filter = { it.key == "user_profile" }
    ) {
        viewModel.send(UserPostUiIntent.Refresh(uid, isThread))
    }

    StateScreen(
        modifier = Modifier.fillMaxSize(),
        isEmpty = isEmpty,
        isError = isError,
        isLoading = isRefreshing,
        onReload = {
            viewModel.send(UserPostUiIntent.Refresh(uid, isThread))
        },
        loadingScreen = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column {
                    repeat(4) {
                        FeedCardPlaceholder()
                    }
                }
            }
        },
        errorScreen = { ErrorScreen(error = error.getOrNull()) },
        emptyScreen = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                if (hidePost) {
                    TipScreen(
                        title = { Text(text = stringResource(id = R.string.title_user_hide_post)) },
                        image = {
                            val composition by rememberLottieComposition(
                                LottieCompositionSpec.RawRes(
                                    R.raw.lottie_hide
                                )
                            )
                            LottieAnimation(
                                composition = composition,
                                iterations = LottieConstants.IterateForever,
                                modifier = Modifier
                                    .padding(vertical = 16.dp)
                                    .fillMaxWidth()
                                    .aspectRatio(2.5f)
                            )
                        },
                        scrollable = false,
                    )
                } else {
                    TipScreen(
                        title = { Text(text = stringResource(id = R.string.title_empty)) },
                        image = {
                            val composition by rememberLottieComposition(
                                LottieCompositionSpec.RawRes(
                                    R.raw.lottie_empty_box
                                )
                            )
                            LottieAnimation(
                                composition = composition,
                                iterations = LottieConstants.IterateForever,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(2f)
                            )
                        },
                        actions = {
                            if (canReload) {
                                Button(onClick = { reload() }) {
                                    Text(text = stringResource(id = R.string.btn_refresh))
                                }
                            }
                        },
                        scrollable = false,
                    )
                }
            }
        },
    ) {
        val pullRefreshState = rememberPullRefreshState(
            refreshing = isRefreshing,
            onRefresh = ::reload
        )

        val lazyListState = rememberLazyListState()

        val pullRefreshModifier =
            if (enablePullRefresh) Modifier.pullRefresh(pullRefreshState) else Modifier

        Box(modifier = pullRefreshModifier) {
            LoadMoreLayout(
                isLoading = isLoadingMore,
                onLoadMore = {
                    viewModel.send(UserPostUiIntent.LoadMore(uid, isThread, currentPage))
                },
                loadEnd = !hasMore,
                lazyListState = lazyListState
            ) {
                UserPostList(
                    data = posts,
                    fluid = fluid,
                    lazyListState = lazyListState,
                    onClickItem = { threadId, postId, isSubPost ->
                        if (postId == null) {
                            navigator.navigate(ThreadPageDestination(threadId))
                        } else {
                            if (isSubPost) {
                                navigator.navigate(
                                    SubPostsPageDestination(
                                        threadId = threadId,
                                        subPostId = postId,
                                        loadFromSubPost = true
                                    )
                                )
                            } else {
                                navigator.navigate(
                                    ThreadPageDestination(
                                        threadId,
                                        postId = postId,
                                        scrollToReply = true
                                    )
                                )
                            }
                        }
                    },
                    onAgreeItem = {
                        viewModel.send(
                            UserPostUiIntent.Agree(
                                it.thread_id,
                                it.post_id,
                                it.agree?.hasAgree ?: 0
                            )
                        )
                    },
                    onClickReply = {
                        navigator.navigate(
                            ThreadPageDestination(
                                it.thread_id,
                                it.forum_id,
                                scrollToReply = true
                            )
                        )
                    },
                    onClickUser = {
                        navigator.navigate(UserProfilePageDestination(it))
                    },
                    onClickForum = {
                        navigator.navigate(ForumPageDestination(it))
                    },
                    onClickOriginThread = {
                        navigator.navigate(ThreadPageDestination(it))
                    },
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
}

@Composable
private fun UserPostList(
    data: ImmutableList<PostListItemData>,
    fluid: Boolean = false,
    lazyListState: LazyListState = rememberLazyListState(),
    onClickItem: (threadId: Long, postId: Long?, isSubPost: Boolean) -> Unit = { _, _, _ -> },
    onAgreeItem: (PostInfoList) -> Unit = {},
    onClickReply: (PostInfoList) -> Unit = {},
    onClickUser: (id: Long) -> Unit = {},
    onClickForum: (name: String) -> Unit = {},
    onClickOriginThread: (threadId: Long) -> Unit = {},
) {
    MyLazyColumn(state = lazyListState) {
        items(
            items = data,
            key = {
                "${it.data.get { thread_id }}_${it.data.get { post_id }}"
            }
        ) { itemData ->
            Container(fluid = fluid) {
                UserPostItem(
                    post = itemData,
                    onClick = onClickItem,
                    onAgree = onAgreeItem,
                    onClickReply = onClickReply,
                    onClickUser = onClickUser,
                    onClickForum = onClickForum,
                    onClickOriginThread = onClickOriginThread,
                )
            }
        }
    }
}

@Composable
fun UserPostItem(
    post: PostListItemData,
    onAgree: (PostInfoList) -> Unit,
    modifier: Modifier = Modifier,
    onClick: (threadId: Long, postId: Long?, isSubPost: Boolean) -> Unit = { _, _, _ -> },
    onClickReply: (PostInfoList) -> Unit = {},
    onClickUser: (id: Long) -> Unit = {},
    onClickForum: (name: String) -> Unit = {},
    onClickOriginThread: (threadId: Long) -> Unit = {},
) {
    val item = post.data
    if (post.isThread) {
        FeedCard(
            item = item,
            onClick = { onClick(it.thread_id, null, false) },
            onAgree = onAgree,
            modifier = modifier,
            onClickReply = onClickReply,
            onClickUser = onClickUser,
            onClickForum = onClickForum,
            onClickOriginThread = { onClickOriginThread(it.tid.toLong()) },
        )
    } else {
        Card(
            header = {
                UserHeader(
                    nameProvider = { item.get { user_name } },
                    nameShowProvider = { item.get { name_show } },
                    portraitProvider = { item.get { user_portrait } },
                    onClick = {
                        onClickUser(item.get { user_id })
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            },
            content = {
                Column {
                    post.contents.fastForEach {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onClick(
                                        item.get { thread_id },
                                        it.postId,
                                        it.isSubPost
                                    )
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = it.contentText,
                                style = MaterialTheme.typography.body1,
                                color = ExtendedTheme.colors.text,
                            )

                            Text(
                                text = DateTimeUtils.getRelativeTimeString(
                                    LocalContext.current,
                                    it.createTime
                                ),
                                style = MaterialTheme.typography.caption,
                                color = ExtendedTheme.colors.textSecondary,
                            )
                        }
                    }
                }

                Text(
                    text = item.get { title },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(ExtendedTheme.colors.floorCard)
                        .clickable {
                            onClickOriginThread(item.get { thread_id })
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.body2,
                )
            },
            modifier = modifier,
            contentPadding = PaddingValues(0.dp),
        )
    }
}