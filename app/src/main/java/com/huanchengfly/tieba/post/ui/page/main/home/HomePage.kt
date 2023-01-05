package com.huanchengfly.tieba.post.ui.page.main.home

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.accompanist.placeholder.placeholder
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.ForumActivity
import com.huanchengfly.tieba.post.activities.NewSearchActivity
import com.huanchengfly.tieba.post.api.ForumSortType
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.getInt
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.widgets.Chip
import com.huanchengfly.tieba.post.ui.widgets.compose.AccountNavIconIfCompact
import com.huanchengfly.tieba.post.ui.widgets.compose.ActionItem
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.ConfirmDialog
import com.huanchengfly.tieba.post.ui.widgets.compose.LongClickMenu
import com.huanchengfly.tieba.post.ui.widgets.compose.Toolbar
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberDialogState
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberMenuState
import com.huanchengfly.tieba.post.utils.AccountUtil.LocalAccount
import com.huanchengfly.tieba.post.utils.ImageUtil
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.TiebaUtil
import com.huanchengfly.tieba.post.utils.appPreferences
import com.huanchengfly.tieba.post.utils.preload.PreloadUtil
import com.huanchengfly.tieba.post.utils.preload.loaders.ForumLoader

private fun getSortType(context: Context, forumName: String): ForumSortType {
    val defaultSortType = context.appPreferences.defaultSortType!!.toInt()
    return ForumSortType.valueOf(
        ThemeUtil.dataStore.getInt(forumName + "_sort_type", defaultSortType)
    )
}

private fun getGridCells(context: Context, listSingle: Boolean = context.appPreferences.listSingle): GridCells {
    return if (listSingle) {
        GridCells.Fixed(1)
    } else {
        GridCells.Adaptive(180.dp)
    }
}

@Preview("SearchBoxPreview")
@Composable
fun SearchBoxPreview() {
    SearchBox(
        backgroundColor = Color(0xFFF8F8F8),
        contentColor = Color(0xFFBFBFBF),
        onClick =  {}
    )
}

@Composable
fun SearchBox(
    modifier: Modifier = Modifier,
    backgroundColor: Color = ExtendedTheme.colors.topBarSurface,
    contentColor: Color = ExtendedTheme.colors.onTopBarSurface,
    onClick: () -> Unit,
) {
    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Surface(
            color = backgroundColor,
            contentColor = contentColor,
            shape = RoundedCornerShape(6.dp),
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
        ) {
            Row(
                verticalAlignment = CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    modifier = Modifier
                        .align(CenterVertically)
                        .size(24.dp),
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(id = R.string.hint_search),
                    modifier = Modifier.align(CenterVertically),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun Header(
    text: String,
    invert: Boolean = false,
    modifier: Modifier = Modifier
) {
    Chip(text = text, invertColor = invert, modifier = modifier)
}

@Composable
private fun ForumItemPlaceholder(
    showAvatar: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (showAvatar) {
            Image(
                painter = rememberDrawablePainter(
                    drawable = ImageUtil.getPlaceHolder(
                        LocalContext.current,
                        0
                    )
                ),
                contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(40.dp)
                    .align(CenterVertically)
                    .placeholder(visible = true, color = ExtendedTheme.colors.chip),
            )
            Spacer(modifier = Modifier.width(14.dp))
        }
        Text(
            color = ExtendedTheme.colors.text,
            text = "",
            modifier = Modifier
                .weight(1f)
                .align(CenterVertically)
                .placeholder(visible = true, color = ExtendedTheme.colors.chip),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .width(54.dp)
                .background(
                    color = ExtendedTheme.colors.chip,
                    shape = RoundedCornerShape(3.dp)
                )
                .padding(vertical = 4.dp)
                .align(CenterVertically)
                .placeholder(visible = true, color = ExtendedTheme.colors.chip)
        ) {
            Text(
                text = "0",
                color = ExtendedTheme.colors.onChip,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Center)
            )
        }
    }
}

@Composable
private fun ForumItem(
    viewModel: HomeViewModel,
    item: HomeUiState.Forum,
    showAvatar: Boolean,
    isTopForum: Boolean = false
) {
    val context = LocalContext.current
    val menuState = rememberMenuState()
    var willUnfollow by remember {
        mutableStateOf(false)
    }
    if (willUnfollow) {
        val dialogState = rememberDialogState()

        ConfirmDialog(
            dialogState = dialogState,
            modifier = Modifier,
            onConfirm = { viewModel.send(HomeUiIntent.Unfollow(item.forumId, item.forumName)) },
            onCancel = {
                willUnfollow = false
            },
            title = { Text(text = stringResource(id = R.string.title_dialog_unfollow_forum, item.forumName)) }
        )

        LaunchedEffect(key1 = "launchUnfollowDialog") {
            dialogState.show = true
        }
    }
    LongClickMenu(
        menuState = menuState,
        menuContent = {
            if (isTopForum) {
                DropdownMenuItem(
                    onClick = {
                        viewModel.send(HomeUiIntent.TopForums.Delete(item.forumId))
                        menuState.expanded = false
                    }
                ) {
                    Text(text = stringResource(id = R.string.menu_top_del))
                }
            } else {
                DropdownMenuItem(
                    onClick = {
                        viewModel.send(HomeUiIntent.TopForums.Add(item))
                        menuState.expanded = false
                    }
                ) {
                    Text(text = stringResource(id = R.string.menu_top))
                }
            }
            DropdownMenuItem(
                onClick = {
                    TiebaUtil.copyText(context, item.forumName)
                    menuState.expanded = false
                }
            ) {
                Text(text = stringResource(id = R.string.title_copy_forum_name))
            }
            DropdownMenuItem(
                onClick = {
                    willUnfollow = true
                    menuState.expanded = false
                }
            ) {
                Text(text = stringResource(id = R.string.button_unfollow))
            }
        },
        onClick = {
            PreloadUtil.startActivityWithPreload(
                context,
                Intent(
                    context,
                    ForumActivity::class.java
                ).putExtra(ForumActivity.EXTRA_FORUM_NAME, item.forumName),
                ForumLoader(
                    item.forumName,
                    1,
                    getSortType(context, item.forumName)
                )
            )
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(vertical = 12.dp)
                .animateContentSize(),
        ) {
            AnimatedVisibility(visible = showAvatar) {
                Row {
                    Avatar(data = item.avatar, size = 40.dp, contentDescription = null)
                    Spacer(modifier = Modifier.width(14.dp))
                }
            }
            Text(
                color = ExtendedTheme.colors.text,
                text = item.forumName,
                modifier = Modifier
                    .weight(1f)
                    .align(CenterVertically),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(54.dp)
                    .background(
                        color = ExtendedTheme.colors.chip,
                        shape = RoundedCornerShape(3.dp)
                    )
                    .padding(vertical = 4.dp)
                    .align(CenterVertically)
            ) {
                Row(
                    modifier = Modifier.align(Center),
                ) {
                    Text(
                        text = "Lv.${item.levelId}",
                        color = ExtendedTheme.colors.onChip,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(CenterVertically)
                    )
                    if (item.isSign) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = stringResource(id = R.string.tip_signed),
                            modifier = Modifier
                                .size(12.dp)
                                .align(CenterVertically),
                            tint = ExtendedTheme.colors.onChip
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomePage(
    viewModel: HomeViewModel = pageViewModel<HomeUiIntent, HomeViewModel>(
        listOf(
            HomeUiIntent.Refresh
        )
    ),
) {
    LocalAccount.current
    val context = LocalContext.current
    val isLoading by viewModel.uiState.collectPartialAsState(
        prop1 = HomeUiState::isLoading,
        initial = true
    )
    val forums by viewModel.uiState.collectPartialAsState(
        prop1 = HomeUiState::forums,
        initial = emptyList()
    )
    val topForums by viewModel.uiState.collectPartialAsState(
        prop1 = HomeUiState::topForums,
        initial = emptyList()
    )
    var listSingle by remember { mutableStateOf(context.appPreferences.listSingle) }
    val gridCells by remember { derivedStateOf { getGridCells(context, listSingle) } }
    Scaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            Toolbar(
                title = stringResource(id = R.string.title_main),
                navigationIcon = AccountNavIconIfCompact(),
                actions = {
                    ActionItem(
                        icon = ImageVector.vectorResource(id = R.drawable.ic_oksign),
                        contentDescription = stringResource(id = R.string.title_oksign)
                    ) {
                        TiebaUtil.startSign(context)
                    }
                    ActionItem(
                        icon = Icons.Outlined.ViewAgenda,
                        contentDescription = stringResource(id = R.string.title_switch_list_single)
                    ) {
                        context.appPreferences.listSingle = !listSingle
                        listSingle = !listSingle
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize(),
    ) { contentPaddings ->
        val pullRefreshState = rememberPullRefreshState(
            refreshing = isLoading,
            onRefresh = { viewModel.send(HomeUiIntent.Refresh) })
        Box(modifier = Modifier
            .padding(contentPaddings)
            .pullRefresh(pullRefreshState)) {
            val gridState = rememberLazyGridState()
            LazyVerticalGrid(
                state = gridState,
                columns = gridCells,
                contentPadding = PaddingValues(bottom = 12.dp),
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                item(key = "SearchBox", span = { GridItemSpan(maxLineSpan) }) {
                    SearchBox(modifier = Modifier.padding(bottom = 12.dp)) {
                        context.goToActivity<NewSearchActivity>()
                    }
                }
                if (!isLoading) {
                    if (topForums.isNotEmpty()) {
                        item(key = "TopForumHeader", span = { GridItemSpan(maxLineSpan) }) {
                            Column {
                                Header(
                                    text = stringResource(id = R.string.title_top_forum),
                                    invert = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        items(count = topForums.size, key = { "Top${topForums[it].forumId}" }) {
                            val item = topForums[it]
                            ForumItem(viewModel, item, listSingle, true)
                        }
                        item(
                            key = "Spacer",
                            span = { GridItemSpan(maxLineSpan) }) {
                            Spacer(
                                modifier = Modifier.height(
                                    16.dp
                                )
                            )
                        }
                        item(key = "ForumHeader", span = { GridItemSpan(maxLineSpan) }) {
                            Column {
                                Header(text = stringResource(id = R.string.forum_list_title))
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                    items(count = forums.size, key = { forums[it].forumId }) {
                        val item = forums[it]
                        ForumItem(viewModel, item, listSingle)
                    }
                } else {
                    item(key = "TopForumHeaderPlaceholder", span = { GridItemSpan(maxLineSpan) }) {
                        Column {
                            Header(
                                text = stringResource(id = R.string.title_top_forum),
                                invert = true,
                                modifier = Modifier.placeholder(visible = true, color = ExtendedTheme.colors.chip)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    items(6, key = { "TopPlaceholder$it" }) {
                        ForumItemPlaceholder(listSingle)
                    }
                    item(
                        key = "Spacer",
                        span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(
                            modifier = Modifier.height(
                                16.dp
                            )
                        )
                    }
                    item(key = "ForumHeaderPlaceholder", span = { GridItemSpan(maxLineSpan) }) {
                        Column {
                            Header(
                                text = stringResource(id = R.string.forum_list_title),
                                invert = true,
                                modifier = Modifier.placeholder(
                                    visible = true,
                                    color = ExtendedTheme.colors.chip
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    items(12, key = { "Placeholder$it" }) {
                        ForumItemPlaceholder(listSingle)
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}