package com.huanchengfly.tieba.post.widgets.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.statusBarsHeight
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.utils.compose.calcStatusBarColor

@Composable
fun ActionItem(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = ExtendedTheme.colors.onTopBar
        )
    }
}

@Composable
fun BackNavigationIcon(onBackPressed: () -> Unit) {
    IconButton(onClick = onBackPressed) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_round_arrow_back),
            contentDescription = stringResource(id = R.string.button_back)
        )
    }
}

@Composable
fun Toolbar(
    title: String,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column {
        Spacer(
            modifier = Modifier
                .statusBarsHeight()
                .fillMaxWidth()
                .background(color = ExtendedTheme.colors.topBar.calcStatusBarColor())
        )
        TopAppBar(
            title = { Text(text = title, fontWeight = FontWeight.Bold) },
            actions = actions,
            navigationIcon = navigationIcon,
            backgroundColor = ExtendedTheme.colors.topBar,
            contentColor = ExtendedTheme.colors.onTopBar,
            elevation = 0.dp
        )
    }
}