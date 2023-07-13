package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

interface GridScope {
    fun item(
        span: Int = 1,
        content: @Composable () -> Unit
    )
}

fun <T> GridScope.items(
    items: List<T>,
    span: (T) -> Int = { 1 },
    content: @Composable (T) -> Unit
) {
    items.forEach {
        item(span(it)) {
            content(it)
        }
    }
}

fun <T> GridScope.itemsIndexed(
    items: List<T>,
    span: (index: Int, item: T) -> Int = { _, _ -> 1 },
    content: @Composable (index: Int, item: T) -> Unit
) {
    items.forEachIndexed { index, item ->
        item(span(index, item)) {
            content(index, item)
        }
    }
}

internal class GridScopeImpl : GridScope {
    class Item(
        val span: Int = 1,
        val content: @Composable () -> Unit
    )

    val items = mutableListOf<Item>()

    override fun item(span: Int, content: @Composable () -> Unit) {
        items.add(Item(span, content))
    }
}

@Stable
class GridCounter(
    private val initialValue: Int
) {
    var mutableValue: Int = initialValue

    val value: Int
        get() = mutableValue

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GridCounter

        if (initialValue != other.initialValue) return false

        return true
    }

    override fun hashCode(): Int {
        return initialValue
    }
}

private fun calcRows(column: Int, items: List<GridScopeImpl.Item>): List<List<GridScopeImpl.Item>> {
    val rows = mutableListOf<List<GridScopeImpl.Item>>()
    val allItems = items.toMutableList()
    while (allItems.isNotEmpty()) {
        var columnCount = column
        val row = mutableListOf<GridScopeImpl.Item>()
        while (allItems.isNotEmpty() && columnCount > 0) {
            val item = allItems[0]
            check(item.span <= columnCount || item.span <= column)
            if (item.span > columnCount) {
                break
            }
            columnCount -= item.span
            row.add(item)
            allItems.removeAt(0)
        }
        if (allItems.isEmpty() && columnCount > 0) {
            row.add(GridScopeImpl.Item(columnCount) { Spacer(modifier = Modifier.fillMaxWidth()) })
        }
        rows.add(row)
    }
    return rows
}

@Composable
fun VerticalGrid(
    column: Int,
    modifier: Modifier = Modifier,
    rowModifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: GridScope.() -> Unit
) {
    val gridScope = GridScopeImpl().apply(content)
    Column(modifier = modifier, verticalArrangement = verticalArrangement) {
        val rows = calcRows(column, gridScope.items)
        rows.forEach {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = horizontalArrangement,
                modifier = rowModifier
            ) {
                it.forEach {
                    Box(
                        modifier = Modifier.weight(it.span.toFloat()),
                        contentAlignment = Alignment.Center,
                    ) {
                        it.content()
                    }
                }
            }
        }
    }
}