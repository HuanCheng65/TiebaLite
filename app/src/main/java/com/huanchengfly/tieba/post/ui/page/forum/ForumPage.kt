package com.huanchengfly.tieba.post.ui.page.forum

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.VerticalAlignTop
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.germainkevin.collapsingtopbar.CollapsingTopBar
import com.germainkevin.collapsingtopbar.CollapsingTopBarDefaults
import com.germainkevin.collapsingtopbar.rememberCollapsingTopBarScrollBehavior
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.material.placeholder
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.SearchPostActivity
import com.huanchengfly.tieba.post.api.models.protos.frsPage.ForumInfo
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.onEvent
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.dataStore
import com.huanchengfly.tieba.post.getInt
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.models.database.History
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.ProvideNavigator
import com.huanchengfly.tieba.post.ui.page.forum.threadlist.ForumThreadListPage
import com.huanchengfly.tieba.post.ui.page.forum.threadlist.ForumThreadListUiEvent
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.AvatarPlaceholder
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.Button
import com.huanchengfly.tieba.post.ui.widgets.compose.ClickMenu
import com.huanchengfly.tieba.post.ui.widgets.compose.ConfirmDialog
import com.huanchengfly.tieba.post.ui.widgets.compose.FeedCardPlaceholder
import com.huanchengfly.tieba.post.ui.widgets.compose.HorizontalDivider
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.MenuScope
import com.huanchengfly.tieba.post.ui.widgets.compose.MyScaffold
import com.huanchengfly.tieba.post.ui.widgets.compose.PagerTabIndicator
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.ui.widgets.compose.TopAppBarContainer
import com.huanchengfly.tieba.post.ui.widgets.compose.picker.ListSinglePicker
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberDialogState
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberMenuState
import com.huanchengfly.tieba.post.ui.widgets.compose.states.StateScreen
import com.huanchengfly.tieba.post.utils.AccountUtil.LocalAccount
import com.huanchengfly.tieba.post.utils.HistoryUtil
import com.huanchengfly.tieba.post.utils.StringUtil.getShortNumString
import com.huanchengfly.tieba.post.utils.TiebaUtil
import com.huanchengfly.tieba.post.utils.appPreferences
import com.huanchengfly.tieba.post.utils.requestPinShortcut
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

fun getSortType(
    context: Context,
    forumName: String
): Int {
    val defaultSortType = context.appPreferences.defaultSortType?.toIntOrNull() ?: 0
    return context.dataStore.getInt("${forumName}_sort_type", defaultSortType)
}

suspend fun setSortType(
    context: Context,
    forumName: String,
    sortType: Int
) {
    context.dataStore.edit {
        it[intPreferencesKey("${forumName}_sort_type")] = sortType
    }
}

@Composable
private fun ForumHeaderPlaceholder(
    forumName: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarPlaceholder(size = Sizes.Large)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.title_forum, forumName),
                    style = MaterialTheme.typography.h6,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (LocalAccount.current != null) {
                Button(
                    onClick = {},
                    elevation = null,
                    shape = RoundedCornerShape(100),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = ExtendedTheme.colors.accent,
                        contentColor = ExtendedTheme.colors.onAccent
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                    modifier = Modifier.placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.fade(),
                    )
                ) {
                    Text(text = stringResource(id = R.string.button_sign_in), fontSize = 13.sp)
                }
            }
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .placeholder(
                    visible = true,
                    highlight = PlaceholderHighlight.fade(),
                )
                .padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatCardItem(
                statNum = 0,
                statText = stringResource(id = R.string.text_stat_follow)
            )
        }
    }
}

@Composable
private fun ForumHeader(
    forum: ForumInfo,
    onBtnClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(
                data = forum.avatar,
                size = Sizes.Large,
                contentDescription = null
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.title_forum, forum.name),
                    style = MaterialTheme.typography.h6,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                AnimatedVisibility(visible = forum.is_like == 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LinearProgressIndicator(
                            progress = max(
                                0F,
                                min(
                                    1F,
                                    forum.cur_score * 1.0F / (max(1.0F, forum.levelup_score * 1.0F))
                                )
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(100))
                                .height(8.dp),
                            color = ExtendedTheme.colors.accent,
                            backgroundColor = ExtendedTheme.colors.accent.copy(alpha = 0.25f)
                        )
                        Text(
                            text = stringResource(
                                id = R.string.tip_forum_header_liked,
                                forum.user_level.toString(),
                                forum.level_name
                            ),
                            style = MaterialTheme.typography.caption,
                            color = ExtendedTheme.colors.textSecondary,
                            fontSize = 10.sp,
                        )
                    }
                }
            }
            val btnEnabled =
                (forum.is_like != 1) || (forum.sign_in_info?.user_info?.is_sign_in != 1)
            if (LocalAccount.current != null) {
                Button(
                    onClick = onBtnClick,
                    elevation = null,
                    shape = RoundedCornerShape(100),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = ExtendedTheme.colors.accent,
                        contentColor = ExtendedTheme.colors.onAccent
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                    enabled = btnEnabled
                ) {
                    val text = when {
                        forum.is_like != 1 -> stringResource(id = R.string.button_follow)
                        forum.sign_in_info?.user_info?.is_sign_in == 1 -> stringResource(
                            id = R.string.button_signed_in,
                            forum.sign_in_info.user_info.cont_sign_num
                        )

                        else -> stringResource(id = R.string.button_sign_in)
                    }
                    Text(text = text, fontSize = 13.sp)
                }
            }
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color = ExtendedTheme.colors.chip)
                .padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatCardItem(
                statNum = forum.member_num,
                statText = stringResource(id = R.string.text_stat_follow)
            )
            HorizontalDivider(color = Color(if (ExtendedTheme.colors.isNightMode) 0xFF808080 else 0xFFDEDEDE))
            StatCardItem(
                statNum = forum.thread_num,
                statText = stringResource(id = R.string.text_stat_threads)
            )
            HorizontalDivider(color = Color(if (ExtendedTheme.colors.isNightMode) 0xFF808080 else 0xFFDEDEDE))
            StatCardItem(
                statNum = forum.post_num,
                statText = stringResource(id = R.string.title_stat_posts_num)
            )
        }
    }
}

private fun shareForum(context: Context, forumName: String) {
    TiebaUtil.shareText(
        context,
        "https://tieba.baidu.com/f?kw=$forumName",
        context.getString(R.string.title_forum, forumName)
    )
}

private suspend fun sendToDesktop(
    context: Context,
    forum: ForumInfo,
    onSuccess: () -> Unit = {},
    onFailure: (String) -> Unit = {}
) {
    requestPinShortcut(
        context,
        "forum_${forum.id}",
        forum.avatar,
        context.getString(R.string.title_forum, forum.name),
        Intent(Intent.ACTION_VIEW).setData(Uri.parse("tblite://forum/${forum.name}")),
        onSuccess = onSuccess,
        onFailure = onFailure
    )
}

@OptIn(ExperimentalPagerApi::class)
@Destination(
    deepLinks = [
        DeepLink(uriPattern = "tblite://forum/{forumName}")
    ]
)
@Composable
fun ForumPage(
    forumName: String,
    viewModel: ForumViewModel = pageViewModel(),
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(ForumUiIntent.Load(forumName, getSortType(context, forumName)))
        viewModel.initialized = true
    }

    val scaffoldState = rememberScaffoldState()
    val snackbarHostState = scaffoldState.snackbarHostState
    LaunchedEffect(null) {
        onEvent<ForumUiEvent.SignIn.Success>(viewModel) {
            snackbarHostState.showSnackbar(
                message = context.getString(
                    R.string.toast_sign_success,
                    "${it.signBonusPoint}",
                    "${it.userSignRank}"
                )
            )
        }
        onEvent<ForumUiEvent.SignIn.Failure>(viewModel) {
            snackbarHostState.showSnackbar(
                message = context.getString(
                    R.string.toast_sign_failed,
                    it.errorMsg
                )
            )
        }
        onEvent<ForumUiEvent.Like.Success>(viewModel) {
            snackbarHostState.showSnackbar(
                message = context.getString(
                    R.string.toast_like_success,
                    it.memberSum,
                )
            )
        }
        onEvent<ForumUiEvent.Like.Failure>(viewModel) {
            snackbarHostState.showSnackbar(
                message = context.getString(
                    R.string.toast_like_failed,
                    it.errorMsg
                )
            )
        }
        onEvent<ForumUiEvent.Unlike.Success>(viewModel) {
            snackbarHostState.showSnackbar(
                message = context.getString(
                    R.string.toast_unlike_success
                )
            )
        }
        onEvent<ForumUiEvent.Unlike.Failure>(viewModel) {
            snackbarHostState.showSnackbar(
                message = context.getString(
                    R.string.toast_unlike_failed,
                    it.errorMsg
                )
            )
        }
    }

    val isLoading by viewModel.uiState.collectPartialAsState(
        prop1 = ForumUiState::isLoading,
        initial = false
    )
    val isError by viewModel.uiState.collectPartialAsState(
        prop1 = ForumUiState::isError,
        initial = false
    )
    val forumInfo by viewModel.uiState.collectPartialAsState(
        prop1 = ForumUiState::forum,
        initial = null
    )
    val tbs by viewModel.uiState.collectPartialAsState(prop1 = ForumUiState::tbs, initial = null)

    val account = LocalAccount.current
    val forum = forumInfo
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    val lazyListStates = listOf(rememberLazyListState(), rememberLazyListState())
    val scrollBehavior = rememberCollapsingTopBarScrollBehavior(
        expandedTopBarMaxHeight = 228.dp,
        scrollableState = lazyListStates[pagerState.currentPage]
    )

    val eventFlows = remember {
        listOf(
            MutableSharedFlow<ForumThreadListUiEvent>(),
            MutableSharedFlow<ForumThreadListUiEvent>()
        )
    }

    val unlikeDialogState = rememberDialogState()

    if (forum != null) {
        LaunchedEffect(forum) {
            HistoryUtil.writeHistory(
                History()
                    .setTitle(context.getString(R.string.title_forum, forumName))
                    .setTimestamp(System.currentTimeMillis())
                    .setAvatar(forum.avatar)
                    .setType(HistoryUtil.TYPE_FORUM)
                    .setData(forumName),
                true
            )
        }
    }

    if (account != null && forum != null) {
        ConfirmDialog(
            dialogState = unlikeDialogState,
            modifier = Modifier,
            onConfirm = {
                viewModel.send(
                    ForumUiIntent.Unlike(forum.id, forumName, tbs ?: account.tbs)
                )
            },
            title = {
                Text(
                    text = stringResource(
                        id = R.string.title_dialog_unfollow_forum,
                        forumName
                    )
                )
            }
        )
    }

    ProvideNavigator(navigator = navigator) {
        StateScreen(
            isEmpty = forum == null,
            isError = isError,
            isLoading = isLoading,
            onReload = {
                viewModel.send(
                    ForumUiIntent.Load(
                        forumName,
                        getSortType(context, forumName)
                    )
                )
            },
            loadingScreen = {
                LoadingPlaceholder(forumName)
            }
        ) {
            MyScaffold(
                scaffoldState = scaffoldState,
                backgroundColor = Color.Transparent,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    TopAppBarContainer(
                        topBar = {
                            Box {
                                CollapsingTopBar(
                                    navigationIcon = {
                                        IconButton(
                                            onClick = {},
                                            enabled = false,
                                            modifier = Modifier.alpha(0f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.ArrowBack,
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    title = {
                                        Text(
                                            text = stringResource(
                                                id = R.string.title_forum,
                                                forumName
                                            ),
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.h6
                                        )
                                    },
                                    expandedTitle = {
                                        if (forum != null) {
                                            ForumHeader(
                                                forum = forum,
                                                onBtnClick = {
                                                    when {
                                                        forum.is_like != 1 -> viewModel.send(
                                                            ForumUiIntent.Like(
                                                                forum.id,
                                                                forum.name,
                                                                tbs ?: account!!.tbs
                                                            )
                                                        )

                                                        forum.sign_in_info?.user_info?.is_sign_in != 1 -> {
                                                            viewModel.send(
                                                                ForumUiIntent.SignIn(
                                                                    forum.id,
                                                                    forum.name,
                                                                    tbs ?: account!!.tbs
                                                                )
                                                            )
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.padding(top = 60.dp)
                                            )
                                        }
                                    },
                                    scrollBehavior = scrollBehavior,
                                    colors = CollapsingTopBarDefaults.colors(
                                        backgroundColor = ExtendedTheme.colors.topBar,
                                        contentColor = ExtendedTheme.colors.onTopBar
                                    )
                                )

                                Toolbar(
                                    forumName = forumName,
                                    menuContent = {
                                        DropdownMenuItem(
                                            onClick = {
                                                shareForum(context, forumName)
                                                dismiss()
                                            }
                                        ) {
                                            Text(text = stringResource(id = R.string.title_share))
                                        }
                                        DropdownMenuItem(
                                            onClick = {
                                                if (forum != null) {
                                                    coroutineScope.launch {
                                                        sendToDesktop(
                                                            context,
                                                            forum,
                                                            onSuccess = {
                                                                coroutineScope.launch {
                                                                    snackbarHostState.showSnackbar(
                                                                        message = context.getString(
                                                                            R.string.toast_send_to_desktop_success
                                                                        )
                                                                    )
                                                                }
                                                            },
                                                            onFailure = {
                                                                coroutineScope.launch {
                                                                    snackbarHostState.showSnackbar(
                                                                        message = context.getString(
                                                                            R.string.toast_send_to_desktop_failed,
                                                                            it
                                                                        )
                                                                    )
                                                                }
                                                            }
                                                        )
                                                    }
                                                }
                                                dismiss()
                                            }
                                        ) {
                                            Text(text = stringResource(id = R.string.title_send_to_desktop))
                                        }
                                        DropdownMenuItem(
                                            onClick = {
                                                unlikeDialogState.show()
                                                dismiss()
                                            }
                                        ) {
                                            Text(text = stringResource(id = R.string.title_unfollow))
                                        }
                                    }
                                )
                            }
                        }
                    ) {
                        TabRow(
                            selectedTabIndex = pagerState.currentPage,
                            indicator = { tabPositions ->
                                PagerTabIndicator(
                                    pagerState = pagerState,
                                    tabPositions = tabPositions
                                )
                            },
                            divider = {},
                            backgroundColor = ExtendedTheme.colors.topBar,
                            contentColor = ExtendedTheme.colors.accent,
                            modifier = Modifier
                                .width(120.dp * 2)
                                .align(Alignment.Start)
                        ) {
                            val menuState = rememberMenuState()
                            val interactionSource = remember { MutableInteractionSource() }
                            var currentSortType by remember {
                                mutableStateOf(
                                    getSortType(
                                        context,
                                        forumName
                                    )
                                )
                            }
                            LaunchedEffect(null) {
                                launch {
                                    interactionSource.interactions
                                        .filterIsInstance<PressInteraction.Press>()
                                        .collect {
                                            menuState.offset = it.pressPosition
                                        }
                                }
                            }
                            ClickMenu(
                                menuState = menuState,
                                menuContent = {
                                    ListSinglePicker(
                                        itemTitles = listOf(
                                            stringResource(id = R.string.title_sort_by_reply),
                                            stringResource(id = R.string.title_sort_by_send)
                                        ),
                                        itemValues = listOf(0, 1),
                                        selectedPosition = currentSortType,
                                        onItemSelected = { _, _, value, changed ->
                                            if (changed) {
                                                coroutineScope.launch {
                                                    setSortType(context, forumName, value)
                                                }
                                                coroutineScope.launch {
                                                    eventFlows[pagerState.currentPage].emit(
                                                        ForumThreadListUiEvent.Refresh(value)
                                                    )
                                                }
                                                currentSortType = value
                                            }
                                            menuState.expanded = false
                                        }
                                    )
                                }
                            ) {
                                val rotate by animateFloatAsState(targetValue = if (menuState.expanded) 180f else 0f)
                                Tab(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.offset(x = 12.dp)
                                        ) {
                                            Text(
                                                text = stringResource(id = R.string.tab_forum_1),
                                                fontSize = 13.sp
                                            )
                                            Icon(
                                                imageVector = Icons.Rounded.ArrowDropDown,
                                                contentDescription = stringResource(id = R.string.sort_menu),
                                                modifier = Modifier.rotate(rotate)
                                            )
                                        }
                                    },
                                    selected = pagerState.currentPage == 0,
                                    onClick = {
                                        if (pagerState.currentPage != 0) {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(0)
                                            }
                                        } else {
                                            menuState.expanded = true
                                        }
                                    },
                                    selectedContentColor = ExtendedTheme.colors.accent,
                                    unselectedContentColor = ExtendedTheme.colors.onTopBarSecondary,
                                    interactionSource = interactionSource
                                )
                            }
                            Tab(
                                text = {
                                    Text(
                                        text = stringResource(id = R.string.tab_forum_good),
                                        fontSize = 13.sp
                                    )
                                },
                                selected = pagerState.currentPage == 1,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(1)
                                    }
                                },
                                selectedContentColor = ExtendedTheme.colors.accent,
                                unselectedContentColor = ExtendedTheme.colors.onTopBarSecondary
                            )
                        }
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            when (context.appPreferences.forumFabFunction) {
                                "refresh" -> {
                                    coroutineScope.launch {
                                        eventFlows[pagerState.currentPage].emit(
                                            ForumThreadListUiEvent.BackToTop
                                        )
                                        eventFlows[pagerState.currentPage].emit(
                                            ForumThreadListUiEvent.Refresh(
                                                getSortType(
                                                    context,
                                                    forumName
                                                )
                                            )
                                        )
                                    }
                                }

                                "back_to_top" -> {
                                    coroutineScope.launch {
                                        eventFlows[pagerState.currentPage].emit(
                                            ForumThreadListUiEvent.BackToTop
                                        )
                                    }
                                }

                                else -> {

                                }
                            }
                        },
                        backgroundColor = ExtendedTheme.colors.background,
                        contentColor = ExtendedTheme.colors.accent
                    ) {
                        Icon(
                            imageVector = when (context.appPreferences.forumFabFunction) {
                                "refresh" -> Icons.Rounded.Refresh
                                "back_to_top" -> Icons.Rounded.VerticalAlignTop
                                else -> Icons.Rounded.Add
                            },
                            contentDescription = null
                        )
                    }
                }
            ) { contentPadding ->
                Column(modifier = Modifier.padding(contentPadding)) {
                    if (forum != null) {
                        HorizontalPager(
                            count = 2,
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.Top,
                            userScrollEnabled = true,
                        ) {
                            ForumThreadListPage(
                                forumId = forum.id,
                                forumName = forum.name,
                                eventFlow = eventFlows[it],
                                isGood = it == 1,
                                lazyListState = lazyListStates[it]
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingPlaceholder(
    forumName: String
) {
    val context = LocalContext.current
    val scrollBehavior = rememberCollapsingTopBarScrollBehavior(
        expandedTopBarMaxHeight = 228.dp
    )

    MyScaffold(
        backgroundColor = Color.Transparent,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBarContainer(
                topBar = {
                    Box {
                        CollapsingTopBar(
                            navigationIcon = { BackNavigationIconPlaceholder() },
                            title = {
                                Text(
                                    text = stringResource(id = R.string.title_forum, forumName),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.h6
                                )
                            },
                            expandedTitle = {
                                ForumHeaderPlaceholder(
                                    forumName = forumName,
                                    modifier = Modifier.padding(top = 60.dp)
                                )
                            },
                            scrollBehavior = scrollBehavior,
                            colors = CollapsingTopBarDefaults.colors(
                                backgroundColor = ExtendedTheme.colors.topBar,
                                contentColor = ExtendedTheme.colors.onTopBar
                            )
                        )

                        Toolbar(
                            forumName = forumName,
                            menuContent = {
                                DropdownMenuItem(
                                    onClick = {
                                        shareForum(context, forumName)
                                        dismiss()
                                    }
                                ) {
                                    Text(text = stringResource(id = R.string.title_share))
                                }
                            }
                        )
                    }
                }
            ) {
                Row(modifier = Modifier.height(48.dp)) {
                    repeat(2) {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.tab_forum_1),
                                fontSize = 13.sp,
                                modifier = Modifier.placeholder(
                                    visible = true,
                                    highlight = PlaceholderHighlight.fade(),
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            repeat(4) {
                FeedCardPlaceholder()
            }
        }
    }
}

@Composable
private fun BackNavigationIconPlaceholder() {
    IconButton(
        onClick = {},
        enabled = false,
        modifier = Modifier.alpha(0f)
    ) {
        Icon(
            imageVector = Icons.Rounded.ArrowBack,
            contentDescription = null
        )
    }
}

@Composable
private fun Toolbar(
    forumName: String,
    menuContent: @Composable (MenuScope.() -> Unit)? = null,
) {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .padding(all = 4.dp)
    ) {
        BackNavigationIcon(onBackPressed = { navigator.navigateUp() })
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = {
            context.goToActivity<SearchPostActivity> {
                putExtra(SearchPostActivity.PARAM_FORUM, forumName)
            }
        }) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = stringResource(id = R.string.btn_search_in_forum)
            )
        }
        if (menuContent != null) {
            val menuState = rememberMenuState()
            ClickMenu(
                menuState = menuState,
                menuContent = menuContent,
                triggerShape = CircleShape
            ) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = stringResource(id = R.string.btn_more)
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.StatCardItem(
    statNum: Int,
    statText: String
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = statNum.getShortNumString(),
            fontSize = 20.sp,
            fontFamily = FontFamily(
                Typeface.createFromAsset(
                    LocalContext.current.assets,
                    "bebas.ttf"
                )
            ),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = statText,
            fontSize = 12.sp,
            color = ExtendedTheme.colors.textSecondary
        )
    }
}