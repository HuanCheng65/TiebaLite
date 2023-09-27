package com.huanchengfly.tieba.post.ui.page.subposts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.UserActivity
import com.huanchengfly.tieba.post.api.models.protos.SubPostList
import com.huanchengfly.tieba.post.api.models.protos.bawuType
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.onEvent
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.arch.wrapImmutable
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.ProvideNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.ReplyPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ThreadPageDestination
import com.huanchengfly.tieba.post.ui.page.thread.PostAgreeBtn
import com.huanchengfly.tieba.post.ui.page.thread.PostCard
import com.huanchengfly.tieba.post.ui.page.thread.UserNameText
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.BlockTip
import com.huanchengfly.tieba.post.ui.widgets.compose.BlockableContent
import com.huanchengfly.tieba.post.ui.widgets.compose.Card
import com.huanchengfly.tieba.post.ui.widgets.compose.ConfirmDialog
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.LoadMoreLayout
import com.huanchengfly.tieba.post.ui.widgets.compose.LongClickMenu
import com.huanchengfly.tieba.post.ui.widgets.compose.MyLazyColumn
import com.huanchengfly.tieba.post.ui.widgets.compose.MyScaffold
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.ui.widgets.compose.TitleCentredToolbar
import com.huanchengfly.tieba.post.ui.widgets.compose.UserHeader
import com.huanchengfly.tieba.post.ui.widgets.compose.VerticalDivider
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberDialogState
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberMenuState
import com.huanchengfly.tieba.post.ui.widgets.compose.states.StateScreen
import com.huanchengfly.tieba.post.utils.AccountUtil.LocalAccount
import com.huanchengfly.tieba.post.utils.DateTimeUtils
import com.huanchengfly.tieba.post.utils.StringUtil
import com.huanchengfly.tieba.post.utils.TiebaUtil
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DestinationStyleBottomSheet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay

@Destination
@Composable
fun SubPostsPage(
    navigator: DestinationsNavigator,
    threadId: Long,
    forumId: Long = 0L,
    postId: Long = 0L,
    subPostId: Long = 0L,
    loadFromSubPost: Boolean = false,
    viewModel: SubPostsViewModel = pageViewModel()
) {
    ProvideNavigator(navigator) {
        SubPostsContent(
            viewModel = viewModel,
            forumId = forumId,
            threadId = threadId,
            postId = postId,
            subPostId = subPostId,
            loadFromSubPost = loadFromSubPost
        )
    }
}

@Destination(
    style = DestinationStyleBottomSheet::class
)
@Composable
fun SubPostsSheetPage(
    navigator: DestinationsNavigator,
    threadId: Long,
    forumId: Long = 0L,
    postId: Long = 0L,
    subPostId: Long = 0L,
    loadFromSubPost: Boolean = false,
    viewModel: SubPostsViewModel = pageViewModel()
) {
    ProvideNavigator(navigator) {
        SubPostsContent(
            viewModel = viewModel,
            forumId = forumId,
            threadId = threadId,
            postId = postId,
            subPostId = subPostId,
            loadFromSubPost = loadFromSubPost,
            isSheet = true
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SubPostsContent(
    viewModel: SubPostsViewModel,
    forumId: Long,
    threadId: Long,
    postId: Long,
    subPostId: Long = 0L,
    loadFromSubPost: Boolean = false,
    isSheet: Boolean = false
) {
    val navigator = LocalNavigator.current
    val account = LocalAccount.current

    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(
            SubPostsUiIntent.Load(
                forumId,
                threadId,
                postId,
                subPostId.takeIf { loadFromSubPost } ?: 0L
            )
        )
    }

    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = SubPostsUiState::isRefreshing,
        initial = false
    )
    val isLoading by viewModel.uiState.collectPartialAsState(
        prop1 = SubPostsUiState::isLoading,
        initial = false
    )
    val anti by viewModel.uiState.collectPartialAsState(
        prop1 = SubPostsUiState::anti,
        initial = null
    )
    val forum by viewModel.uiState.collectPartialAsState(
        prop1 = SubPostsUiState::forum,
        initial = null
    )
    val thread by viewModel.uiState.collectPartialAsState(
        prop1 = SubPostsUiState::thread,
        initial = null
    )
    val post by viewModel.uiState.collectPartialAsState(
        prop1 = SubPostsUiState::post,
        initial = null
    )
    val postContentRenders by viewModel.uiState.collectPartialAsState(
        prop1 = SubPostsUiState::postContentRenders,
        initial = persistentListOf()
    )
    val subPosts by viewModel.uiState.collectPartialAsState(
        prop1 = SubPostsUiState::subPosts,
        initial = persistentListOf()
    )
    val currentPage by viewModel.uiState.collectPartialAsState(
        prop1 = SubPostsUiState::currentPage,
        initial = 1
    )
    val totalCount by viewModel.uiState.collectPartialAsState(
        prop1 = SubPostsUiState::totalCount,
        initial = 0
    )
    val hasMore by viewModel.uiState.collectPartialAsState(
        prop1 = SubPostsUiState::hasMore,
        initial = true
    )

    val lazyListState = rememberLazyListState()

    viewModel.onEvent<SubPostsUiEvent.ScrollToSubPosts> {
        delay(20)
        lazyListState.scrollToItem(2 + subPosts.indexOfFirst { it.id == subPostId })
    }

    val confirmDeleteDialogState = rememberDialogState()
    var deleteSubPost by remember { mutableStateOf<ImmutableHolder<SubPostList>?>(null) }
    ConfirmDialog(
        dialogState = confirmDeleteDialogState,
        onConfirm = {
            if (deleteSubPost == null) {
                val isSelfPost = post?.get { author_id } == account?.uid?.toLongOrNull()
                viewModel.send(
                    SubPostsUiIntent.DeletePost(
                        forumId = forumId,
                        forumName = forum?.get { name }.orEmpty(),
                        threadId = threadId,
                        postId = postId,
                        deleteMyPost = isSelfPost,
                        tbs = anti?.get { tbs },
                    )
                )
            } else {
                val isSelfSubPost =
                    deleteSubPost!!.get { author_id } == account?.uid?.toLongOrNull()
                viewModel.send(
                    SubPostsUiIntent.DeletePost(
                        forumId = forumId,
                        forumName = forum?.get { name }.orEmpty(),
                        threadId = threadId,
                        postId = postId,
                        subPostId = deleteSubPost!!.get { id },
                        deleteMyPost = isSelfSubPost,
                        tbs = anti?.get { tbs },
                    )
                )
            }
        }
    ) {
        Text(
            text = stringResource(
                id = R.string.message_confirm_delete,
                if (deleteSubPost == null && post != null) stringResource(
                    id = R.string.tip_post_floor,
                    post!!.get { floor })
                else stringResource(id = R.string.this_reply)
            )
        )
    }

    StateScreen(
        isEmpty = subPosts.isEmpty(),
        isError = false,
        isLoading = isRefreshing
    ) {
        MyScaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TitleCentredToolbar(
                    title = {
                        Text(text = post?.let {
                            stringResource(
                                id = R.string.title_sub_posts,
                                it.get { floor })
                        } ?: stringResource(id = R.string.title_sub_posts_default),
                            fontWeight = FontWeight.Bold, style = MaterialTheme.typography.h6)
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.navigateUp() }) {
                            Icon(
                                imageVector = if (isSheet) Icons.Rounded.Close else Icons.Rounded.ArrowBack,
                                contentDescription = stringResource(id = R.string.btn_close)
                            )
                        }
                    },
                    actions = {
                        if (!isSheet) {
                            IconButton(onClick = {
                                navigator.navigate(
                                    ThreadPageDestination(
                                        forumId = forumId,
                                        threadId = threadId,
                                        postId = postId
                                    )
                                )
                            }) {
                                Icon(
                                    imageVector = Icons.Rounded.OpenInBrowser,
                                    contentDescription = stringResource(id = R.string.btn_open_origin_thread)
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = {
                if (account != null) {
                    Surface(
                        elevation = 16.dp,
                        color = ExtendedTheme.colors.bottomBar,
                        contentColor = ExtendedTheme.colors.text,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Avatar(
                                data = StringUtil.getAvatarUrl(account.portrait),
                                size = Sizes.Tiny,
                                contentDescription = account.name,
                            )
                            Row(
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ExtendedTheme.colors.bottomBarSurface)
                                    .clickable {
                                        val fid = forum?.get { id } ?: forumId
                                        val forumName = forum?.get { name }
                                        if (!forumName.isNullOrEmpty()) {
                                            navigator.navigate(
                                                ReplyPageDestination(
                                                    forumId = fid,
                                                    forumName = forumName,
                                                    threadId = threadId,
                                                    postId = postId,
                                                )
                                            )
                                        }
                                    }
                                    .padding(8.dp),
                            ) {
                                Text(
                                    text = stringResource(id = R.string.tip_reply_thread),
                                    style = MaterialTheme.typography.caption,
                                    color = ExtendedTheme.colors.onBottomBarSurface,
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            LoadMoreLayout(
                isLoading = isLoading,
                onLoadMore = {
                    viewModel.send(
                        SubPostsUiIntent.LoadMore(
                            forumId,
                            threadId,
                            postId,
//                            subPostId,
                            page = currentPage + 1,
                        )
                    )
                },
                loadEnd = !hasMore,
                lazyListState = lazyListState,
                isEmpty = post == null && subPosts.isEmpty(),
            ) {
                MyLazyColumn(
                    modifier = Modifier.padding(paddingValues),
                    state = lazyListState
                ) {
                    item(key = "Post$postId") {
                        post?.let {
                            Column {
                                PostCard(
                                    postHolder = it,
                                    contentRenders = postContentRenders,
                                    canDelete = { it.author_id == account?.uid?.toLongOrNull() },
                                    showSubPosts = false,
                                    onAgree = {
                                        val hasAgreed = it.get { agree?.hasAgree != 0 }
                                        viewModel.send(
                                            SubPostsUiIntent.Agree(
                                                forumId,
                                                threadId,
                                                postId,
                                                agree = !hasAgreed
                                            )
                                        )
                                    },
                                    onReplyClick = {
                                        navigator.navigate(
                                            ReplyPageDestination(
                                                forumId = forumId,
                                                forumName = forum?.get { name } ?: "",
                                                threadId = threadId,
                                                postId = postId,
                                                replyUserId = it.author?.id ?: it.author_id,
                                                replyUserName = it.author?.nameShow.takeIf { name -> !name.isNullOrEmpty() }
                                                    ?: it.author?.name,
                                                replyUserPortrait = it.author?.portrait,
                                            )
                                        )
                                    },
                                ) {
                                    deleteSubPost = null
                                    confirmDeleteDialogState.show()
                                }
                                VerticalDivider(thickness = 2.dp)
                            }
                        }
                    }
                    stickyHeader(key = "SubPostsHeader") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ExtendedTheme.colors.background)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    id = R.string.title_sub_posts_header,
                                    totalCount
                                ),
                                style = MaterialTheme.typography.subtitle1
                            )
                        }
                    }
                    itemsIndexed(
                        items = subPosts,
                        key = { _, subPost -> subPost.id }
                    ) { index, item ->
                        SubPostItem(
                            item = item,
                            canDelete = { it.author_id == account?.uid?.toLongOrNull() },
                            threadAuthorId = thread?.get { author?.id },
                            onAgree = {
                                val hasAgreed = it.agree?.hasAgree != 0
                                viewModel.send(
                                    SubPostsUiIntent.Agree(
                                        forumId,
                                        threadId,
                                        postId,
                                        subPostId = it.id,
                                        agree = !hasAgreed
                                    )
                                )
                            },
                            onReplyClick = {
                                navigator.navigate(
                                    ReplyPageDestination(
                                        forumId = forumId,
                                        forumName = forum?.get { name } ?: "",
                                        threadId = threadId,
                                        postId = postId,
                                        subPostId = it.id,
                                        replyUserId = it.author?.id ?: it.author_id,
                                        replyUserName = it.author?.nameShow.takeIf { name -> !name.isNullOrEmpty() }
                                            ?: it.author?.name,
                                        replyUserPortrait = it.author?.portrait,
                                    )
                                )
                            },
                            onMenuDeleteClick = {
                                deleteSubPost = it.wrapImmutable()
                                confirmDeleteDialogState.show()
                            },
                        )
                    }
                }
            }
        }
    }
}

private fun getDescText(
    time: Long?,
    ipAddress: String?
): String {
    val texts = mutableListOf<String>()
    if (time != null) texts.add(DateTimeUtils.getRelativeTimeString(App.INSTANCE, time))
    if (!ipAddress.isNullOrEmpty()) texts.add(
        App.INSTANCE.getString(
            R.string.text_ip_location,
            "$ipAddress"
        )
    )
    return texts.joinToString(" ")
}

@Composable
private fun SubPostItem(
    item: SubPostItemData,
    threadAuthorId: Long? = null,
    canDelete: (SubPostList) -> Boolean = { false },
    onAgree: (SubPostList) -> Unit = {},
    onReplyClick: (SubPostList) -> Unit = {},
    onMenuDeleteClick: ((SubPostList) -> Unit)? = null,
) {
    val (subPost, contentRenders, blocked) = item
    val context = LocalContext.current
    val author = remember(subPost) { subPost.get { author }?.wrapImmutable() }
    val hasAgreed = remember(subPost) {
        subPost.get { agree?.hasAgree == 1 }
    }
    val agreeNum = remember(subPost) {
        subPost.get { agree?.diffAgreeNum ?: 0L }
    }
    val menuState = rememberMenuState()
    BlockableContent(
        blocked = blocked,
        blockedTip = { BlockTip(text = { Text(text = stringResource(id = R.string.tip_blocked_sub_post)) }) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        LongClickMenu(
            menuState = menuState,
            indication = null,
            menuContent = {
                DropdownMenuItem(
                    onClick = {
                        onReplyClick(subPost.get())
                        menuState.expanded = false
                    }
                ) {
                    Text(text = stringResource(id = R.string.btn_reply))
                }
                DropdownMenuItem(
                    onClick = {
                        TiebaUtil.copyText(
                            context,
                            contentRenders.joinToString("\n") { it.toString() })
                        menuState.expanded = false
                    }
                ) {
                    Text(text = stringResource(id = R.string.menu_copy))
                }
                DropdownMenuItem(
                    onClick = {
                        TiebaUtil.reportPost(context, subPost.get { id }.toString())
                        menuState.expanded = false
                    }
                ) {
                    Text(text = stringResource(id = R.string.title_report))
                }
                if (canDelete(subPost.get()) && onMenuDeleteClick != null) {
                    DropdownMenuItem(
                        onClick = {
                            onMenuDeleteClick(subPost.get())
                            menuState.expanded = false
                        }
                    ) {
                        Text(text = stringResource(id = R.string.title_delete))
                    }
                }
            },
            onClick = { onReplyClick(subPost.get()) }
        ) {
            Card(
                header = {
                    if (author != null) {
                        UserHeader(
                            avatar = {
                                Avatar(
                                    data = StringUtil.getAvatarUrl(author.get { portrait }),
                                    size = Sizes.Small,
                                    contentDescription = null
                                )
                            },
                            name = {
                                UserNameText(
                                    userName = StringUtil.getUsernameAnnotatedString(
                                        LocalContext.current,
                                        author.get { name },
                                        author.get { nameShow }
                                    ),
                                    userLevel = author.get { level_id },
                                    isLz = author.get { id } == threadAuthorId,
                                    bawuType = author.get { bawuType },
                                )
                            },
                            desc = {
                                Text(
                                    text = getDescText(
                                        subPost.get { time }.toLong(),
                                        author.get { ip_address })
                                )
                            },
                            onClick = {
                                UserActivity.launch(context, author.get { id }.toString())
                            }
                        ) {
                            PostAgreeBtn(
                                hasAgreed = hasAgreed,
                                agreeNum = agreeNum,
                                onClick = { onAgree(subPost.get()) }
                            )
                        }
                    }
                },
                content = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .padding(start = Sizes.Small + 8.dp)
                            .fillMaxWidth()
                    ) {
                        contentRenders.fastForEach { it.Render() }
                    }
                }
            )
        }
    }
}