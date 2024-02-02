package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch


/**
 * Class holding onto state needed for [ScrollableTabRow]
 */
private class ScrollableTabData(
    private val scrollState: ScrollState,
    private val coroutineScope: CoroutineScope,
) {
    private var selectedTab: Int? = null

    fun onLaidOut(
        density: Density,
        edgeOffset: Int,
        tabPositions: List<TabPosition>,
        selectedTab: Int,
    ) {
        // Animate if the new tab is different from the old tab, or this is called for the first
        // time (i.e selectedTab is `null`).
        if (this.selectedTab != selectedTab) {
            this.selectedTab = selectedTab
            tabPositions.getOrNull(selectedTab)?.let {
                // Scrolls to the tab with [tabPosition], trying to place it in the center of the
                // screen or as close to the center as possible.
                val calculatedOffset = it.calculateTabOffset(density, edgeOffset, tabPositions)
                if (scrollState.value != calculatedOffset) {
                    coroutineScope.launch {
                        scrollState.animateScrollTo(
                            calculatedOffset,
                            animationSpec = ScrollableTabRowScrollSpec
                        )
                    }
                }
            }
        }
    }

    /**
     * @return the offset required to horizontally center the tab inside this TabRow.
     * If the tab is at the start / end, and there is not enough space to fully centre the tab, this
     * will just clamp to the min / max position given the max width.
     */
    private fun TabPosition.calculateTabOffset(
        density: Density,
        edgeOffset: Int,
        tabPositions: List<TabPosition>,
    ): Int = with(density) {
        val totalTabRowWidth = tabPositions.last().right.roundToPx() + edgeOffset
        val visibleWidth = totalTabRowWidth - scrollState.maxValue
        val tabOffset = left.roundToPx()
        val scrollerCenter = visibleWidth / 2
        val tabWidth = width.roundToPx()
        val centeredTabOffset = tabOffset - (scrollerCenter - tabWidth / 2)
        // How much space we have to scroll. If the visible width is <= to the total width, then
        // we have no space to scroll as everything is always visible.
        val availableSpace = (totalTabRowWidth - visibleWidth).coerceAtLeast(0)
        return centeredTabOffset.coerceIn(0, availableSpace)
    }
}


/**
 * [AnimationSpec] used when scrolling to a tab that is not fully visible.
 */
private val ScrollableTabRowScrollSpec: AnimationSpec<Float> = tween(
    durationMillis = 250,
    easing = FastOutSlowInEasing
)

@Composable
@UiComposable
fun TabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    contentColor: Color = contentColorFor(backgroundColor),
    indicator: @Composable @UiComposable
        (tabPositions: List<TabPosition>) -> Unit = @Composable { tabPositions ->
        TabRowDefaults.Indicator(
            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex])
        )
    },
    divider: @Composable @UiComposable () -> Unit =
        @Composable {
            TabRowDefaults.Divider()
        },
    tabs: @Composable @UiComposable () -> Unit,
) {
    Surface(
        modifier = modifier.selectableGroup(),
        color = backgroundColor,
        contentColor = contentColor
    ) {
        SubcomposeLayout(Modifier.fillMaxWidth()) { constraints ->
            val tabRowWidth = constraints.maxWidth
            val tabMeasurables = subcompose(TabSlots.Tabs, tabs)
            val tabCount = tabMeasurables.size
            val tabWidth = (tabRowWidth / tabCount)
            val tabPlaceables = tabMeasurables.map {
                it.measure(constraints.copy(minWidth = tabWidth, maxWidth = tabWidth))
            }

            val tabRowHeight = tabPlaceables.maxByOrNull { it.height }?.height ?: 0

            val tabPositions = List(tabCount) { index ->
                TabPosition(tabWidth.toDp() * index, tabWidth.toDp())
            }

            layout(tabRowWidth, tabRowHeight) {
                tabPlaceables.forEachIndexed { index, placeable ->
                    placeable.placeRelative(index * tabWidth, 0)
                }

                subcompose(TabSlots.Divider, divider).forEach {
                    val placeable = it.measure(constraints.copy(minHeight = 0))
                    placeable.placeRelative(0, tabRowHeight - placeable.height)
                }

                subcompose(TabSlots.Indicator) {
                    indicator(tabPositions)
                }.forEach {
                    it.measure(Constraints.fixed(tabRowWidth, tabRowHeight)).placeRelative(0, 0)
                }
            }
        }
    }
}

/**
 * <a href="https://material.io/components/tabs#scrollable-tabs" class="external" target="_blank">Material Design scrollable tabs</a>.
 *
 * When a set of tabs cannot fit on screen, use scrollable tabs. Scrollable tabs can use longer text
 * labels and a larger number of tabs. They are best used for browsing on touch interfaces.
 *
 * ![Scrollable tabs image](https://developer.android.com/images/reference/androidx/compose/material/scrollable-tabs.png)
 *
 * A ScrollableTabRow contains a row of [Tab]s, and displays an indicator underneath the currently
 * selected tab. A ScrollableTabRow places its tabs offset from the starting edge, and allows
 * scrolling to tabs that are placed off screen. For a fixed tab row that does not allow
 * scrolling, and evenly places its tabs, see [TabRow].
 *
 * @param selectedTabIndex the index of the currently selected tab
 * @param modifier optional [Modifier] for this ScrollableTabRow
 * @param backgroundColor The background color for the ScrollableTabRow. Use [Color.Transparent] to
 * have no color.
 * @param contentColor The preferred content color provided by this ScrollableTabRow to its
 * children. Defaults to either the matching content color for [backgroundColor], or if
 * [backgroundColor] is not a color from the theme, this will keep the same value set above this
 * ScrollableTabRow.
 * @param edgePadding the padding between the starting and ending edge of ScrollableTabRow, and
 * the tabs inside the ScrollableTabRow. This padding helps inform the user that this tab row can
 * be scrolled, unlike a [TabRow].
 * @param indicator the indicator that represents which tab is currently selected. By default this
 * will be a [TabRowDefaults.Indicator], using a [TabRowDefaults.tabIndicatorOffset]
 * modifier to animate its position. Note that this indicator will be forced to fill up the
 * entire ScrollableTabRow, so you should use [TabRowDefaults.tabIndicatorOffset] or similar to
 * animate the actual drawn indicator inside this space, and provide an offset from the start.
 * @param divider the divider displayed at the bottom of the ScrollableTabRow. This provides a layer
 * of separation between the ScrollableTabRow and the content displayed underneath.
 * @param tabs the tabs inside this ScrollableTabRow. Typically this will be multiple [Tab]s. Each
 * element inside this lambda will be measured and placed evenly across the TabRow, each taking
 * up equal space.
 */
@Composable
@UiComposable
fun ScrollableTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    contentColor: Color = contentColorFor(backgroundColor),
    edgePadding: Dp = TabRowDefaults.ScrollableTabRowPadding,
    indicator: @Composable @UiComposable
        (tabPositions: List<TabPosition>) -> Unit = @Composable { tabPositions ->
        TabRowDefaults.Indicator(
            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex])
        )
    },
    divider: @Composable @UiComposable () -> Unit =
        @Composable {
            TabRowDefaults.Divider()
        },
    tabs: @Composable @UiComposable () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        contentColor = contentColor
    ) {
        val scrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()
        val scrollableTabData = remember(scrollState, coroutineScope) {
            ScrollableTabData(
                scrollState = scrollState,
                coroutineScope = coroutineScope
            )
        }
        SubcomposeLayout(
            Modifier
                .fillMaxWidth()
                .wrapContentSize(align = Alignment.CenterStart)
                .horizontalScroll(scrollState)
                .selectableGroup()
                .clipToBounds()
        ) { constraints ->
            val padding = edgePadding.roundToPx()
            val tabConstraints = constraints.copy()

            val tabPlaceables = subcompose(TabSlots.Tabs, tabs)
                .map { it.measure(tabConstraints) }

            var layoutWidth = padding * 2
            var layoutHeight = 0
            tabPlaceables.forEach {
                layoutWidth += it.width
                layoutHeight = maxOf(layoutHeight, it.height)
            }

            // Position the children.
            layout(layoutWidth, layoutHeight) {
                // Place the tabs
                val tabPositions = mutableListOf<TabPosition>()
                var left = padding
                tabPlaceables.forEach {
                    it.placeRelative(left, 0)
                    tabPositions.add(TabPosition(left = left.toDp(), width = it.width.toDp()))
                    left += it.width
                }

                // The divider is measured with its own height, and width equal to the total width
                // of the tab row, and then placed on top of the tabs.
                subcompose(TabSlots.Divider, divider).forEach {
                    val placeable = it.measure(
                        constraints.copy(
                            minHeight = 0,
                            minWidth = layoutWidth,
                            maxWidth = layoutWidth
                        )
                    )
                    placeable.placeRelative(0, layoutHeight - placeable.height)
                }

                // The indicator container is measured to fill the entire space occupied by the tab
                // row, and then placed on top of the divider.
                subcompose(TabSlots.Indicator) {
                    indicator(tabPositions)
                }.forEach {
                    it.measure(Constraints.fixed(layoutWidth, layoutHeight)).placeRelative(0, 0)
                }

                scrollableTabData.onLaidOut(
                    density = this@SubcomposeLayout,
                    edgeOffset = padding,
                    tabPositions = tabPositions,
                    selectedTab = selectedTabIndex
                )
            }
        }
    }
}

/**
 * [Modifier] that takes up all the available width inside the [TabRow], and then animates
 * the offset of the indicator it is applied to, depending on the [currentTabPosition].
 *
 * @param currentTabPosition [TabPosition] of the currently selected tab. This is used to
 * calculate the offset of the indicator this modifier is applied to, as well as its width.
 */
fun Modifier.tabIndicatorOffset(
    currentTabPosition: TabPosition,
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "tabIndicatorOffset"
        value = currentTabPosition
    }
) {
    val currentTabWidth by animateDpAsState(
        targetValue = currentTabPosition.width,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
    )
    val indicatorOffset by animateDpAsState(
        targetValue = currentTabPosition.left,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
    )
    fillMaxWidth()
        .wrapContentSize(Alignment.BottomStart)
        .offset(x = indicatorOffset)
        .width(currentTabWidth)
}

/**
 * Data class that contains information about a tab's position on screen, used for calculating
 * where to place the indicator that shows which tab is selected.
 *
 * @property left the left edge's x position from the start of the [TabRow]
 * @property right the right edge's x position from the start of the [TabRow]
 * @property width the width of this tab
 */
@Immutable
class TabPosition internal constructor(val left: Dp, val width: Dp) {
    val right: Dp get() = left + width

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TabPosition) return false

        if (left != other.left) return false
        return width == other.width
    }

    override fun hashCode(): Int {
        var result = left.hashCode()
        result = 31 * result + width.hashCode()
        return result
    }

    override fun toString(): String {
        return "TabPosition(left=$left, right=$right, width=$width)"
    }
}

private enum class TabSlots {
    Tabs,
    Divider,
    Indicator
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerTabIndicator(
    pagerState: PagerState,
    tabPositions: List<TabPosition>,
) {
    if (tabPositions.isNotEmpty()) {
        val tabWidth = 16.dp
        val currentPage = minOf(tabPositions.lastIndex, pagerState.currentPage)
        val currentTab = tabPositions[currentPage]
        val prevTab = tabPositions.getOrNull(currentPage - 1)
        val nextTab = tabPositions.getOrNull(currentPage + 1)
        val fraction = pagerState.currentPageOffsetFraction
        val currentTabLeft = currentTab.left + (currentTab.width / 2 - tabWidth / 2)
        val indicatorOffset = if (fraction > 0 && nextTab != null) {
            val nextTabLeft = nextTab.left + (nextTab.width / 2 - tabWidth / 2)
            lerp(currentTabLeft, nextTabLeft, fraction)
        } else if (fraction < 0 && prevTab != null) {
            val prevTabLeft = prevTab.left + (prevTab.width / 2 - tabWidth / 2)
            lerp(currentTabLeft, prevTabLeft, -fraction)
        } else {
            currentTabLeft
        }
        val animatedIndicatorOffset by animateDpAsState(targetValue = indicatorOffset)
        Box(
            Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.BottomStart)
                .offset(x = animatedIndicatorOffset, y = (-8).dp)
                .width(16.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(100))
                .background(color = LocalContentColor.current)
        )
    }
}

@Composable
fun TabIndicator(
    selectedTabIndex: Int,
    tabPositions: List<TabPosition>
) {
    if (tabPositions.isNotEmpty()) {
        val tabIndicatorWidth = 16.dp
        val currentTab = tabPositions[selectedTabIndex]
        val currentTabLeft = currentTab.left + (currentTab.width / 2 - tabIndicatorWidth / 2)
        val animatedIndicatorOffset by animateDpAsState(targetValue = currentTabLeft)
        Box(
            Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.BottomStart)
                .offset(x = animatedIndicatorOffset, y = (-8).dp)
                .width(tabIndicatorWidth)
                .height(3.dp)
                .clip(RoundedCornerShape(100))
                .background(color = LocalContentColor.current)
        )
    }
}

@Composable
fun TabClickMenu(
    selected: Boolean,
    onClick: () -> Unit,
    menuContent: @Composable MenuScope.() -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    menuState: MenuState = rememberMenuState(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    selectedContentColor: Color = LocalContentColor.current,
    unselectedContentColor: Color = selectedContentColor.copy(alpha = ContentAlpha.medium),
    content: @Composable ColumnScope.() -> Unit,
) {
    LaunchedEffect(Unit) {
        launch {
            interactionSource.interactions
                .filterIsInstance<PressInteraction.Press>()
                .collect {
                    menuState.offset = it.pressPosition
                }
        }
    }
    ClickMenu(
        menuContent = menuContent,
        menuState = menuState
    ) {
        Tab(
            selected = selected,
            onClick = {
                if (!selected) {
                    onClick()
                } else {
                    menuState.toggle()
                }
            },
            modifier = modifier,
            enabled = enabled,
            interactionSource = interactionSource,
            selectedContentColor = selectedContentColor,
            unselectedContentColor = unselectedContentColor,
            content = content
        )
    }
}

@Composable
fun TabClickMenu(
    selected: Boolean,
    onClick: () -> Unit,
    text: @Composable () -> Unit,
    menuContent: @Composable MenuScope.() -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    menuState: MenuState = rememberMenuState(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    selectedContentColor: Color = LocalContentColor.current,
    unselectedContentColor: Color = selectedContentColor.copy(alpha = ContentAlpha.medium),
) {
    TabClickMenu(
        selected = selected,
        onClick = onClick,
        menuContent = menuContent,
        modifier = modifier,
        enabled = enabled,
        menuState = menuState,
        interactionSource = interactionSource,
        selectedContentColor = selectedContentColor,
        unselectedContentColor = unselectedContentColor,
    ) {
        val rotate by animateFloatAsState(
            targetValue = if (menuState.expanded) 180f else 0f,
            label = "ArrowIndicatorRotate"
        )
        val alpha by animateFloatAsState(
            targetValue = if (selected) 1f else 0f,
            label = "ArrowIndicatorAlpha"
        )

        val tabTextStyle =
            MaterialTheme.typography.button.copy(fontSize = 13.sp, letterSpacing = 0.sp)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(48.dp)
                .padding(start = 16.dp)
        ) {
            ProvideTextStyle(value = tabTextStyle) {
                text()
            }
            Icon(
                imageVector = Icons.Rounded.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(rotate)
                    .alpha(alpha)
            )
        }
    }
}