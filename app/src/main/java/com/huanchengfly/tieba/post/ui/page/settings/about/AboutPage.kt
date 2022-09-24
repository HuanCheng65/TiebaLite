package com.huanchengfly.tieba.post.ui.page.settings.about

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.accompanist.insets.ui.Scaffold
import com.huanchengfly.tieba.post.BuildConfig
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.TitleCentredToolbar
import com.huanchengfly.tieba.post.utils.launchUrl
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@Composable
fun AboutPage(
    navigator: DestinationsNavigator,
) {
    Scaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            TitleCentredToolbar(
                title = stringResource(id = R.string.title_about),
                navigationIcon = {
                    BackNavigationIcon(onBackPressed = { navigator.navigateUp() })
                }
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.align(Alignment.Center)) {
                    AsyncImage(
                        model = R.mipmap.ic_launcher_new,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                val context = LocalContext.current
                TextButton(
                    shape = RoundedCornerShape(100),
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = ExtendedTheme.colors.text.copy(
                            alpha = 0.1f
                        ),
                        contentColor = ExtendedTheme.colors.text
                    ),
                    onClick = {
                        launchUrl(context, "https://github.com/HuanCheng65/TiebaLite")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(id = R.string.source_code), modifier = Modifier.padding(vertical = 4.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.tip_about, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.caption,
                    color = ExtendedTheme.colors.textSecondary
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}