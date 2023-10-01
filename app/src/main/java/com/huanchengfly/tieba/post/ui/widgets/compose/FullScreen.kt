package com.huanchengfly.tieba.post.ui.widgets.compose

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.findViewTreeOnBackPressedDispatcherOwner
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.huanchengfly.tieba.post.R
import java.util.UUID

@Composable
fun FullScreen(content: @Composable () -> Unit) {
    val view = LocalView.current
    val parentComposition = rememberCompositionContext()
    val currentContent by rememberUpdatedState(content)
    val id = rememberSaveable { UUID.randomUUID() }

    val fullScreenLayout = remember {
        FullScreenLayout(
            view,
            id
        ).apply {
            setContent(parentComposition) {
                currentContent()
            }
        }
    }

    DisposableEffect(fullScreenLayout) {
        fullScreenLayout.show()
        onDispose { fullScreenLayout.dismiss() }
    }
}

@SuppressLint("ViewConstructor")
private class FullScreenLayout(
    private val composeView: View,
    uniqueId: UUID
) : AbstractComposeView(composeView.context) {

    private val windowManager =
        composeView.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val params = createLayoutParams()

    override var shouldCreateCompositionOnAttachedToWindow: Boolean = false
        private set

    init {
        id = android.R.id.content

        setViewTreeLifecycleOwner(composeView.findViewTreeLifecycleOwner())
        setViewTreeViewModelStoreOwner(composeView.findViewTreeViewModelStoreOwner())
        setViewTreeSavedStateRegistryOwner(composeView.findViewTreeSavedStateRegistryOwner())
        composeView.findViewTreeOnBackPressedDispatcherOwner()
            ?.let { setViewTreeOnBackPressedDispatcherOwner(it) }

        setTag(R.id.compose_view_saveable_id_tag, "CustomLayout:$uniqueId")
    }

    private var content: @Composable () -> Unit by mutableStateOf({})

    @Composable
    override fun Content() {
        content()
    }

    fun setContent(parent: CompositionContext, content: @Composable () -> Unit) {
        setParentCompositionContext(parent)
        this.content = content
        shouldCreateCompositionOnAttachedToWindow = true
    }

    private fun createLayoutParams(): WindowManager.LayoutParams =
        WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
            token = composeView.applicationWindowToken
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags = flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
                blurBehindRadius = 64
            }
        }

    fun show() {
        windowManager.addView(this, params)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_BACK) {
            if (event.action == KeyEvent.ACTION_DOWN) {
                findViewTreeOnBackPressedDispatcherOwner()?.onBackPressedDispatcher?.onBackPressed()
            }
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    fun dismiss() {
        disposeComposition()
        setViewTreeLifecycleOwner(null)
        setViewTreeViewModelStoreOwner(null)
        setViewTreeSavedStateRegistryOwner(null)
        windowManager.removeViewImmediate(this)
    }
}
