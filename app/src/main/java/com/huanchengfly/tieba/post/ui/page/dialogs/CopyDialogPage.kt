package com.huanchengfly.tieba.post.ui.page.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.widgets.compose.Button
import com.huanchengfly.tieba.post.ui.widgets.compose.TitleCentredToolbar
import com.huanchengfly.tieba.post.utils.TiebaUtil
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DestinationStyle

@Destination
@Composable
fun CopyTextPage(
    text: String,
    navigator: DestinationsNavigator,
) {
    val context = LocalContext.current
    CopyTextPageContent(
        text = text,
        onCopy = {
            TiebaUtil.copyText(context, it)
        },
        onCancel = {
            navigator.navigateUp()
        }
    )
}

object CopyTextDialogStyle : DestinationStyle.Dialog {
    override val properties: DialogProperties
        get() = DialogProperties(
            usePlatformDefaultWidth = false,
        )

}

@Destination(
    style = CopyTextDialogStyle::class
)
@Composable
fun CopyTextDialogPage(
    text: String,
    navigator: DestinationsNavigator,
) {
    val context = LocalContext.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(color = ExtendedTheme.colors.windowBackground)
    ) {
        CopyTextPageContent(
            text = text,
            onCopy = {
                TiebaUtil.copyText(context, it)
            },
            onCancel = {
                navigator.navigateUp()
            }
        )
    }
}

@Composable
private fun CopyTextPageContent(
    text: String,
    onCopy: (String) -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = ExtendedTheme.colors.windowBackground)
            .systemBarsPadding()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TitleCentredToolbar(
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(id = R.string.title_copy),
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(id = R.string.tip_copy_text),
                        style = MaterialTheme.typography.caption
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(id = R.string.btn_close)
                    )
                }
            }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SelectionContainer {
                    Text(
                        text = text,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.body1
                    )
                }
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onCopy(text)
                    onCancel()
                }
            ) {
                Text(text = stringResource(id = R.string.btn_copy_all))
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onCancel()
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = ExtendedTheme.colors.text.copy(alpha = 0.1f),
                    contentColor = ExtendedTheme.colors.text
                )
            ) {
                Text(text = stringResource(id = R.string.btn_close))
            }
        }
    }
}