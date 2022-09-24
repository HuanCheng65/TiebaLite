package com.huanchengfly.tieba.post

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.systemuicontroller.SystemUiController
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.BaseComposeActivity
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.page.NavGraphs
import com.huanchengfly.tieba.post.ui.page.destinations.MainPageDestination
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.PermissionUtils
import com.huanchengfly.tieba.post.utils.TiebaUtil
import com.huanchengfly.tieba.post.utils.requestPermission
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.navigation.dependency
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivityV2 : BaseComposeActivity() {
    private val handler = Handler(Looper.getMainLooper())

    private fun fetchAccount() {
        lifecycleScope.launch(Dispatchers.Main) {
            if (AccountUtil.isLoggedIn()) {
                AccountUtil.fetchAccountFlow()
                    .flowOn(Dispatchers.IO)
                    .catch { e ->
                        toastShort(e.getErrorMessage())
                        e.printStackTrace()
                    }
                    .collect()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && AccountUtil.isLoggedIn()) {
            requestPermission {
                permissions = listOf(PermissionUtils.POST_NOTIFICATIONS)
                description = getString(R.string.desc_permission_post_notifications)
            }
        }
    }

    private fun initAutoSign() {
        runCatching {
            TiebaUtil.initAutoSign(this)
        }
    }

    override fun onStart() {
        super.onStart()
        handler.postDelayed({
            requestNotificationPermission()
        }, 100)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateContent(systemUiController: SystemUiController) {
        super.onCreateContent(systemUiController)
        fetchAccount()
        initAutoSign()
    }

    @OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialNavigationApi::class)
    @Composable
    override fun createContent() {
        Surface(
            color = ExtendedTheme.colors.background
        ) {
            val engine = rememberAnimatedNavHostEngine(
                navHostContentAlignment = Alignment.TopStart,
                rootDefaultAnimations = RootNavGraphDefaultAnimations(
                    enterTransition = {
                        slideIntoContainer(AnimatedContentScope.SlideDirection.Start, initialOffset = { it })
                    },
                    exitTransition = {
                        slideOutOfContainer(AnimatedContentScope.SlideDirection.End, targetOffset = { -it })
                    },
                    popEnterTransition = {
                        slideIntoContainer(AnimatedContentScope.SlideDirection.Start, initialOffset = { -it })
                    },
                    popExitTransition = {
                        slideOutOfContainer(AnimatedContentScope.SlideDirection.End, targetOffset = { it })
                    },
                ),
            )
            DestinationsNavHost(
                navGraph = NavGraphs.root,
                engine = engine,
                dependenciesContainerBuilder = {
                    dependency(MainPageDestination) { this@MainActivityV2 }
                }
            )
        }
    }
}