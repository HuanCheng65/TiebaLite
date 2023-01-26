package com.huanchengfly.tieba.post.ui.page.settings.block

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.placeholder.material.placeholder
import com.google.gson.reflect.TypeToken
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.onEvent
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.models.database.Block
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.page.main.BottomNavigationDivider
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.LocalSnackbarHostState
import com.huanchengfly.tieba.post.ui.widgets.compose.LongClickMenu
import com.huanchengfly.tieba.post.ui.widgets.compose.MyScaffold
import com.huanchengfly.tieba.post.ui.widgets.compose.PagerTabIndicator
import com.huanchengfly.tieba.post.ui.widgets.compose.PromptDialog
import com.huanchengfly.tieba.post.ui.widgets.compose.TitleCentredToolbar
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberDialogState
import com.huanchengfly.tieba.post.ui.widgets.compose.states.StateScreen
import com.huanchengfly.tieba.post.utils.GsonUtil
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Destination
@Composable
fun BlockSettingsPage(
    viewModel: BlockSettingsViewModel = pageViewModel<BlockSettingsUiIntent, BlockSettingsViewModel>(
        listOf(BlockSettingsUiIntent.Load)
    ),
    navigator: DestinationsNavigator,
) {
    var addBlockCategory by remember { mutableStateOf(Block.CATEGORY_BLACK_LIST) }
    val dialogState = rememberDialogState()
    PromptDialog(
        dialogState = dialogState,
        onConfirm = {
            viewModel.send(BlockSettingsUiIntent.Add(category = addBlockCategory, keywords = it.split(" ")))
        },
        title = {
            Text(
                text = if (addBlockCategory == Block.CATEGORY_WHITE_LIST) stringResource(id = R.string.title_add_white)
                else stringResource(id = R.string.title_add_black)
            )
        }
    ) {
        Text(text = stringResource(id = R.string.tip_add_block))
    }

    val context = LocalContext.current
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    val blackList by viewModel.uiState.collectPartialAsState(
        prop1 = BlockSettingsUiState::blackList,
        initial = emptyList()
    )
    val whiteList by viewModel.uiState.collectPartialAsState(
        prop1 = BlockSettingsUiState::whiteList,
        initial = emptyList()
    )
    val isLoading by viewModel.uiState.collectPartialAsState(
        prop1 = BlockSettingsUiState::isLoading,
        initial = false
    )
    MyScaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            TitleCentredToolbar(
                title = stringResource(id = R.string.title_block_settings),
                navigationIcon = {
                    BackNavigationIcon(onBackPressed = { navigator.navigateUp() })
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
                    backgroundColor = Color.Transparent,
                    contentColor = ExtendedTheme.colors.onTopBar,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(84.dp * 2),
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                        text = { Text(text = stringResource(id = R.string.title_black_list)) },
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                        text = { Text(text = stringResource(id = R.string.title_white_list)) },
                    )
                }
            }
        },
        bottomBar = {
            Column {
                BottomNavigationDivider()
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                addBlockCategory = Block.CATEGORY_BLACK_LIST
                                dialogState.show()
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Outlined.Block, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = stringResource(id = R.string.title_add_black), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                addBlockCategory = Block.CATEGORY_WHITE_LIST
                                dialogState.show()
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = stringResource(id = R.string.title_add_white), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        val snackbarHostState = LocalSnackbarHostState.current
        LaunchedEffect(null) {
            onEvent<BlockSettingsUiEvent.Success>(viewModel) {
                snackbarHostState.showSnackbar(
                    when (it) {
                        is BlockSettingsUiEvent.Success.Add -> context.getString(R.string.toast_add_success)
                        is BlockSettingsUiEvent.Success.Delete -> context.getString(R.string.toast_delete_success)
                    }
                )
            }
        }
        HorizontalPager(
            count = 2,
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues,
            verticalAlignment = Alignment.Top
        ) { position ->
            val items = if (position == 0) blackList else whiteList
            StateScreen(
                isEmpty = items.isEmpty(),
                isError = false,
                isLoading = isLoading,
                onReload = { viewModel.send(BlockSettingsUiIntent.Load) },
                loadingScreen = {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(4) {
                            BlockItemPlaceholder()
                        }
                    }
                }
            ) {
                LazyColumn {
                    items(items, key = { it.id }) {
                        LongClickMenu(menuContent = {
                            DropdownMenuItem(onClick = {
                                viewModel.send(
                                    BlockSettingsUiIntent.Delete(
                                        it.id
                                    )
                                )
                            }) {
                                Text(text = stringResource(id = R.string.title_delete))
                            }
                        }) {
                            BlockItem(item = it)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BlockItemPlaceholder() {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Block,
            contentDescription = null,
            modifier = Modifier.placeholder(visible = true)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(id = R.string.title_block_settings),
            modifier = Modifier.placeholder(visible = true)
        )
    }
}

@Composable
private fun BlockItem(
    item: Block,
) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (item.type == Block.TYPE_USER) Icons.Outlined.AccountCircle else ImageVector.vectorResource(
                id = R.drawable.ic_comment_new
            ),
            contentDescription = if (item.type == Block.TYPE_USER) stringResource(
                id = R.string.block_type_user
            ) else stringResource(
                id = R.string.block_type_keywords
            )
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (item.type == Block.TYPE_USER) {
                Text(
                    text = "${item.username}",
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = "UID: ${item.uid}",
                    style = MaterialTheme.typography.caption
                )
            } else {
                val keywordsList: List<String> = runCatching {
                    GsonUtil.getGson()
                        .fromJson<List<String>>(
                            item.keywords ?: "[]",
                            object : TypeToken<List<String>>() {}.type
                        )
                }.getOrDefault(emptyList())
                Text(
                    text = keywordsList.joinToString(" "),
                    style = MaterialTheme.typography.subtitle1
                )
            }
        }
    }
}