package com.huanchengfly.tieba.post.ui.page

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.ui.common.theme.compose.TonalPalette
import com.huanchengfly.tieba.post.ui.common.theme.compose.dynamicTonalPalette
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.collections.immutable.toImmutableList
import kotlin.reflect.full.declaredMemberProperties

@Destination
@Composable
fun MonetTestPage() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        Text(text = "Only available on Android 12+")
        return
    }
    val context = LocalContext.current
    val tonalPalette = remember { dynamicTonalPalette(context) }
    val properties = remember {
        TonalPalette::class.declaredMemberProperties.sortedBy { it.name }.toImmutableList()
    }
    LazyColumn {
        items(properties) {
            // 如果类型是 Color 就显示
            if (it.returnType.classifier == Color::class) {
                val color = it.getter.call(tonalPalette) as Color
                Row {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(color = color)
                    )
                    Text(text = it.name)
                }
            }
        }
    }
}