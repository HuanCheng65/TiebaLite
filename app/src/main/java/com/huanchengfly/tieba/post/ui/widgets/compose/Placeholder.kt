package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme

@Composable
fun EmptyPlaceholder(
    modifier: Modifier = Modifier,
    emptyText: String = stringResource(id = R.string.tip_empty),
    actions: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = emptyText, style = MaterialTheme.typography.body1, color = ExtendedTheme.colors.textSecondary)
        actions()
    }
}