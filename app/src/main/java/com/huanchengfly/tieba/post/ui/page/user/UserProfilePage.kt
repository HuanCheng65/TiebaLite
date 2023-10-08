package com.huanchengfly.tieba.post.ui.page.user

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.NoAccounts
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.post.arch.GlobalEvent
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.emitGlobalEvent
import com.huanchengfly.tieba.post.arch.getOrNull
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.models.database.Block
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.page.ProvideNavigator
import com.huanchengfly.tieba.post.ui.page.editprofile.view.EditProfileActivity
import com.huanchengfly.tieba.post.ui.page.user.likeforum.UserLikeForumPage
import com.huanchengfly.tieba.post.ui.page.user.post.UserPostPage
import com.huanchengfly.tieba.post.ui.widgets.Chip
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.Button
import com.huanchengfly.tieba.post.ui.widgets.compose.ClickMenu
import com.huanchengfly.tieba.post.ui.widgets.compose.ErrorScreen
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoadHorizontalPager
import com.huanchengfly.tieba.post.ui.widgets.compose.MyScaffold
import com.huanchengfly.tieba.post.ui.widgets.compose.PagerTabIndicator
import com.huanchengfly.tieba.post.ui.widgets.compose.ProvideContentColor
import com.huanchengfly.tieba.post.ui.widgets.compose.PullToRefreshLayout
import com.huanchengfly.tieba.post.ui.widgets.compose.ScrollableTabRow
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.ui.widgets.compose.Toolbar
import com.huanchengfly.tieba.post.ui.widgets.compose.UserHeader
import com.huanchengfly.tieba.post.ui.widgets.compose.states.StateScreen
import com.huanchengfly.tieba.post.utils.AccountUtil.LocalAccount
import com.huanchengfly.tieba.post.utils.BlockManager
import com.huanchengfly.tieba.post.utils.StringUtil
import com.huanchengfly.tieba.post.utils.StringUtil.getShortNumString
import com.huanchengfly.tieba.post.utils.TiebaUtil
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalFoundationApi::class)
@Destination
@Composable
fun UserProfilePage(
    uid: Long,
    navigator: DestinationsNavigator,
    viewModel: UserProfileViewModel = pageViewModel(),
) {
    val account = LocalAccount.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    val isSelf = remember(account, uid) {
        account?.uid == uid.toString()
    }

    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(UserProfileUiIntent.Refresh(uid))
        viewModel.initialized = true
    }

    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = UserProfileUiState::isRefreshing,
        initial = true
    )
    val error by viewModel.uiState.collectPartialAsState(
        prop1 = UserProfileUiState::error,
        initial = null
    )
    val user by viewModel.uiState.collectPartialAsState(
        prop1 = UserProfileUiState::user,
        initial = null
    )
    val disableButton by viewModel.uiState.collectPartialAsState(
        prop1 = UserProfileUiState::disableButton,
        initial = false
    )

    val isError by remember {
        derivedStateOf { error != null }
    }
    val isEmpty by remember {
        derivedStateOf { user == null }
    }

    var heightOffset by rememberSaveable { mutableFloatStateOf(0f) }
    var headerHeight by rememberSaveable {
        mutableFloatStateOf(
            with(density) {
                (96.dp + 16.dp).toPx()
            }
        )
    }

    val isShowHeaderArea by remember {
        derivedStateOf {
            heightOffset.absoluteValue < headerHeight
        }
    }

    ProvideNavigator(navigator = navigator) {
        StateScreen(
            modifier = Modifier.fillMaxSize(),
            isEmpty = isEmpty,
            isError = isError,
            isLoading = isRefreshing,
            onReload = { viewModel.send(UserProfileUiIntent.Refresh(uid)) },
            errorScreen = { ErrorScreen(error = error.getOrNull()) }
        ) {
            MyScaffold(
                topBar = {
                    Toolbar(
                        title = {
                            user?.let {
                                AnimatedVisibility(
                                    visible = !isShowHeaderArea,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    ToolbarUserTitle(user = it)
                                }
                            }
                        },
                        navigationIcon = {
                            BackNavigationIcon {
                                navigator.navigateUp()
                            }
                        },
                        actions = {
                            user.takeUnless { isSelf }?.let {
                                ClickMenu(
                                    menuContent = {
                                        DropdownMenuItem(
                                            onClick = {
                                                BlockManager.addBlockAsync(
                                                    Block(
                                                        category = Block.CATEGORY_BLACK_LIST,
                                                        type = Block.TYPE_USER,
                                                        username = it.get { name },
                                                        uid = it.get { id }.toString()
                                                    )
                                                ) {
                                                    if (it) context.toastShort(R.string.toast_add_success)
                                                }
                                            }
                                        ) {
                                            Text(text = stringResource(id = R.string.menu_add_user_to_black_list))
                                        }
                                        DropdownMenuItem(
                                            onClick = {
                                                BlockManager.addBlockAsync(
                                                    Block(
                                                        category = Block.CATEGORY_WHITE_LIST,
                                                        type = Block.TYPE_USER,
                                                        username = it.get { name },
                                                        uid = it.get { id }.toString()
                                                    )
                                                ) {
                                                    if (it) context.toastShort(R.string.toast_add_success)
                                                }
                                            }
                                        ) {
                                            Text(text = stringResource(id = R.string.menu_add_user_to_white_list))
                                        }
                                    },
                                    triggerShape = CircleShape
                                ) {
                                    Box(
                                        modifier = Modifier.size(48.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.NoAccounts,
                                            contentDescription = stringResource(id = R.string.btn_block)
                                        )
                                    }
                                }
                            }
                        },
                    )
                }
            ) { paddingValues ->
                var isFakeRefreshing by remember { mutableStateOf(false) }

                LaunchedEffect(isFakeRefreshing) {
                    if (isFakeRefreshing) {
                        delay(1000)
                        isFakeRefreshing = false
                    }
                }

                PullToRefreshLayout(
                    refreshing = isFakeRefreshing,
                    onRefresh = {
                        coroutineScope.emitGlobalEvent(GlobalEvent.Refresh(key = "user_profile"))
                        isFakeRefreshing = true
                    }
                ) {
                    val headerNestedScrollConnection = remember {
                        object : NestedScrollConnection {
                            override fun onPreScroll(
                                available: Offset,
                                source: NestedScrollSource,
                            ): Offset {
                                if (available.y < 0) {
                                    val prevHeightOffset = heightOffset
                                    heightOffset = max(heightOffset + available.y, -headerHeight)
                                    if (prevHeightOffset != heightOffset) {
                                        return available.copy(x = 0f)
                                    }
                                }

                                return Offset.Zero
                            }

                            override fun onPostScroll(
                                consumed: Offset,
                                available: Offset,
                                source: NestedScrollSource,
                            ): Offset {
                                if (available.y > 0f) {
                                    // Adjust the height offset in case the consumed delta Y is less than what was
                                    // recorded as available delta Y in the pre-scroll.
                                    val prevHeightOffset = heightOffset
                                    heightOffset = min(heightOffset + available.y, 0f)
                                    if (prevHeightOffset != heightOffset) {
                                        return available.copy(x = 0f)
                                    }
                                }

                                return Offset.Zero
                            }
                        }
                    }

                    ProvideContentColor(color = ExtendedTheme.colors.text) {
                        Column(
                            modifier = Modifier
                                .padding(paddingValues)
                                .nestedScroll(headerNestedScrollConnection)
                        ) {
                            user?.let { holder ->
                                val pages = remember {
                                    listOfNotNull(
                                        UserProfilePageData(
                                            id = "threads",
                                            title = {
                                                stringResource(
                                                    id = R.string.title_profile_threads_tab,
                                                    it.get { thread_num }.getShortNumString()
                                                )
                                            },
                                            content = {
                                                UserPostPage(
                                                    uid = it.get { id },
                                                    isThread = true
                                                )
                                            }
                                        ),
                                        UserProfilePageData(
                                            id = "posts",
                                            title = {
                                                stringResource(
                                                    id = R.string.title_profile_posts_tab,
                                                    it.get { post_num }.getShortNumString()
                                                )
                                            },
                                            content = {
                                                UserPostPage(
                                                    uid = uid,
                                                    isThread = false
                                                )
                                            }
                                        ).takeIf { isSelf },
                                        UserProfilePageData(
                                            id = "concern_forums",
                                            title = {
                                                stringResource(
                                                    id = R.string.title_profile_concern_forums_tab,
                                                    it.get { my_like_num }.toString()
                                                )
                                            },
                                            content = {
                                                UserLikeForumPage(uid = it.get { id })
                                            }
                                        ),
                                    ).toImmutableList()
                                }
                                val pagerState = rememberPagerState { pages.size }

                                val containerHeight by remember {
                                    derivedStateOf {
                                        with(density) {
                                            (headerHeight + heightOffset).toDp()
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .height(containerHeight)
                                        .clipToBounds()
                                ) {
                                    UserProfileDetail(
                                        user = holder,
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                            .wrapContentHeight(
                                                align = Alignment.Bottom,
                                                unbounded = true
                                            )
                                            .onSizeChanged {
                                                headerHeight = it.height.toFloat()
                                            },
                                        showBtn = account != null,
                                        isSelf = isSelf,
                                        onBtnClick = {
                                            if (disableButton || account == null) {
                                                return@UserProfileDetail
                                            }
                                            if (isSelf) {
                                                context.goToActivity<EditProfileActivity>()
                                            } else if (holder.get { has_concerned } == 0) {
                                                viewModel.send(
                                                    UserProfileUiIntent.Follow(
                                                        holder.get { portrait },
                                                        account.tbs,
                                                    )
                                                )
                                            } else {
                                                viewModel.send(
                                                    UserProfileUiIntent.Unfollow(
                                                        holder.get { portrait },
                                                        account.tbs,
                                                    )
                                                )
                                            }
                                        },
                                        onCopyIdClick = {
                                            TiebaUtil.copyText(
                                                context,
                                                holder.get { id }.toString()
                                            )
                                        }
                                    )
                                }

                                UserProfileTabRow(
                                    user = holder,
                                    pages = pages,
                                    pagerState = pagerState,
//                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )

                                LazyLoadHorizontalPager(
                                    state = pagerState,
                                    key = { pages[it].id },
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    pages[it].content(holder)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Immutable
data class UserProfilePageData(
    val id: String,
    val title: @Composable (ImmutableHolder<User>) -> String,
    val content: @Composable (ImmutableHolder<User>) -> Unit,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UserProfileTabRow(
    user: ImmutableHolder<User>,
    pages: ImmutableList<UserProfilePageData>,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    val tabTextStyle = MaterialTheme.typography.button.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        letterSpacing = 0.sp
    )

    ScrollableTabRow(
        selectedTabIndex = pagerState.currentPage,
        indicator = { tabPositions ->
            PagerTabIndicator(
                pagerState = pagerState,
                tabPositions = tabPositions
            )
        },
        divider = {},
        backgroundColor = Color.Transparent,
        contentColor = ExtendedTheme.colors.primary,
        edgePadding = 0.dp,
        modifier = modifier.wrapContentWidth(align = Alignment.Start),
    ) {
        pages.fastForEachIndexed { i, pageData ->
            Tab(
                selected = pagerState.currentPage == i,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(i)
                    }
                },
                selectedContentColor = ExtendedTheme.colors.primary,
                unselectedContentColor = ExtendedTheme.colors.textSecondary,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(48.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(text = pageData.title(user), style = tabTextStyle)
                }
            }
        }
    }
}

@Composable
private fun ToolbarUserTitle(
    user: ImmutableHolder<User>,
    modifier: Modifier = Modifier,
) {
    UserHeader(
        avatar = {
            Avatar(
                data = StringUtil.getAvatarUrl(user.get { portrait }),
                size = Sizes.Small,
                contentDescription = null
            )
        },
        name = {
            Text(
                text = StringUtil.getUsernameAnnotatedString(
                    LocalContext.current,
                    user.get { name },
                    user.get { nameShow },
                    LocalContentColor.current
                )
            )
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserProfileDetail(
    user: ImmutableHolder<User>,
    modifier: Modifier = Modifier,
    showBtn: Boolean = true,
    isSelf: Boolean = false,
    onBtnClick: () -> Unit = {},
    onCopyIdClick: () -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Avatar(
                data = StringUtil.getBigAvatarUrl(user.get { portrait }),
                size = 96.dp,
                contentDescription = null,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            if (showBtn) {
                Button(
                    onClick = onBtnClick,
                    colors = if (user.get { has_concerned } == 0 || isSelf) {
                        ButtonDefaults.buttonColors()
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    },
                    border = if (user.get { has_concerned } == 0 || isSelf) {
                        null
                    } else {
                        ButtonDefaults.outlinedBorder
                    },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isSelf) {
                            Icon(imageVector = Icons.Rounded.Edit, contentDescription = null)
                            Text(text = stringResource(id = R.string.menu_edit_info))
                        } else if (user.get { has_concerned } == 0) {
                            Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                            Text(text = stringResource(id = R.string.button_follow))
                        } else {
                            Text(text = stringResource(id = R.string.button_unfollow))
                        }
                    }
                }
            }
        }
        Text(
            text = StringUtil.getUsernameAnnotatedString(
                LocalContext.current,
                user.get { name },
                user.get { nameShow },
                LocalContentColor.current
            ),
            style = MaterialTheme.typography.h6,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Bold
        )
        ProvideTextStyle(value = MaterialTheme.typography.body2) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.text_stat_follow),
                        color = ExtendedTheme.colors.textSecondary
                    )
                    Text(
                        text = user.get { concern_num }.getShortNumString(),
                        color = ExtendedTheme.colors.text,
                        fontWeight = FontWeight.Bold
                    )
                }
                HorizontalDivider(modifier = Modifier.fillMaxHeight())
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.text_stat_fans),
                        color = ExtendedTheme.colors.textSecondary
                    )
                    Text(
                        text = user.get { fans_num }.getShortNumString(),
                        color = ExtendedTheme.colors.text,
                        fontWeight = FontWeight.Bold
                    )
                }
                HorizontalDivider(modifier = Modifier.fillMaxHeight())
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.text_stat_agrees),
                        color = ExtendedTheme.colors.textSecondary
                    )
                    Text(
                        text = user.get { total_agree_num }.getShortNumString(),
                        color = ExtendedTheme.colors.text,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Text(
            text = user.get { intro }.takeIf { it.isNotEmpty() }
                ?: stringResource(id = R.string.tip_no_intro),
            style = MaterialTheme.typography.body2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        user.getNullableImmutable { bazhu_grade }?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Verified,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = ExtendedTheme.colors.primary,
                )
                Text(
                    text = it.get { desc },
                    style = MaterialTheme.typography.body2,
                    color = ExtendedTheme.colors.primary,
                )
            }
        } ?: user.getNullableImmutable { new_god_data }
            ?.takeIf { it.get { status } != 0 }
            ?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Verified,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = ExtendedTheme.colors.primary,
                    )
                    Text(
                        text = stringResource(id = R.string.text_god_verify, it.get { field_name }),
                        style = MaterialTheme.typography.body2,
                        color = ExtendedTheme.colors.primary,
                    )
                }
            }
        val sexEmoji = when (user.get { sex }) {
            1 -> "♂"
            2 -> "♀"
            else -> "?"
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Chip(text = sexEmoji, invertColor = true)
            Chip(
                text = stringResource(
                    id = R.string.text_profile_user_id,
                    user.get { id }.toString()
                ),
                appendIcon = {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = null
                    )
                },
                onClick = onCopyIdClick
            )
            if (user.get { ip_address }.isNotEmpty()) {
                Chip(
                    text = stringResource(
                        id = R.string.text_profile_ip_location,
                        user.get { ip_address }
                    ),
                )
            }
            Chip(
                text = stringResource(
                    id = R.string.text_profile_tb_age,
                    user.get { tb_age }
                )
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun HorizontalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .background(ExtendedTheme.colors.divider)
            .width(1.dp)
            .then(modifier)
    )
}