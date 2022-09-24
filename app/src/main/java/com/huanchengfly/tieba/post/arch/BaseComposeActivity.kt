package com.huanchengfly.tieba.post.arch

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.huanchengfly.tieba.post.activities.BaseActivity
import com.huanchengfly.tieba.post.ui.common.theme.compose.TiebaLiteTheme
import com.huanchengfly.tieba.post.utils.AccountUtil.LocalAccountProvider
import com.huanchengfly.tieba.post.utils.ThemeUtil

abstract class BaseComposeActivityWithParcelable<DATA : Parcelable> : BaseComposeActivityWithData<DATA>() {
    abstract val dataExtraKey: String

    override fun parseData(intent: Intent): DATA? {
        return intent.extras?.getParcelable(dataExtraKey)
    }
}

abstract class BaseComposeActivityWithData<DATA> : BaseComposeActivity() {
    var data: DATA? = null

    abstract fun parseData(intent: Intent): DATA?

    override fun onCreate(savedInstanceState: Bundle?) {
        data = parseData(intent)
        super.onCreate(savedInstanceState)
    }

    @Composable
    final override fun createContent() {
        createContent(data!!)
    }

    @Composable
    abstract fun createContent(data: DATA)
}

abstract class BaseComposeActivity : BaseActivity() {
    override val isNeedImmersionBar: Boolean = false
    override val isNeedFixBg: Boolean = false
    override val isNeedSetTheme: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            TiebaLiteTheme {
                val systemUiController = rememberSystemUiController()
                SideEffect {
                    systemUiController.apply {
                        setStatusBarColor(
                            Color.Transparent,
                            darkIcons = ThemeUtil.isStatusBarFontDark()
                        )
                        setNavigationBarColor(
                            Color.Transparent,
                            darkIcons = ThemeUtil.isNavigationBarFontDark()
                        )
                    }
                }

                LaunchedEffect(key1 = "onCreateContent") {
                    onCreateContent(systemUiController)
                }

                LocalAccountProvider {
                    createContent()
                }
            }
        }
    }

    /**
     * 在创建内容前执行
     *
     * @param systemUiController SystemUiController
     */
    open fun onCreateContent(
        systemUiController: SystemUiController
    ) {}

    @Composable
    abstract fun createContent()

    fun handleCommonEvent(event: CommonUiEvent) {
        when (event) {
            is CommonUiEvent.Toast -> {
                Toast.makeText(this, event.message, event.length).show()
            }
        }
    }
}



sealed interface CommonUiEvent : UiEvent {
    data class Toast(
        val message: CharSequence,
        val length: Int = android.widget.Toast.LENGTH_SHORT
    ) : CommonUiEvent
}