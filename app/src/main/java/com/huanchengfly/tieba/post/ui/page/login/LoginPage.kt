package com.huanchengfly.tieba.post.ui.page.login

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.page.webview.MyWebChromeClient
import com.huanchengfly.tieba.post.ui.page.webview.MyWebViewClient
import com.huanchengfly.tieba.post.ui.page.webview.isInternalHost
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.ClickMenu
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.LoadingState
import com.huanchengfly.tieba.post.ui.widgets.compose.LocalSnackbarHostState
import com.huanchengfly.tieba.post.ui.widgets.compose.MyScaffold
import com.huanchengfly.tieba.post.ui.widgets.compose.Toolbar
import com.huanchengfly.tieba.post.ui.widgets.compose.WebView
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberMenuState
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberSaveableWebViewState
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberWebViewNavigator
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.AccountUtil.parseCookie
import com.huanchengfly.tieba.post.utils.ClientUtils
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

const val LOGIN_URL =
    "https://wappass.baidu.com/passport?login&u=https%3A%2F%2Ftieba.baidu.com%2Findex%2Ftbwise%2Fmine"

@SuppressLint("SetJavaScriptEnabled")
@Destination
@Composable
fun LoginPage(
    navigator: DestinationsNavigator,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val webViewState = rememberSaveableWebViewState()
    val webViewNavigator = rememberWebViewNavigator()
    var loaded by rememberSaveable {
        mutableStateOf(false)
    }
    var pageTitle by rememberSaveable {
        mutableStateOf("")
    }
    val displayPageTitle by remember {
        derivedStateOf {
            pageTitle.ifEmpty {
                context.getString(R.string.title_default)
            }
        }
    }
    val currentHost by remember {
        derivedStateOf {
            webViewState.lastLoadedUrl?.toUri()?.host.orEmpty().lowercase()
        }
    }
    val isExternalHost by remember {
        derivedStateOf {
            currentHost.isNotEmpty() && !isInternalHost(currentHost)
        }
    }

    DisposableEffect(Unit) {
        val job = coroutineScope.launch {
            snapshotFlow { webViewState.pageTitle }
                .filterNotNull()
                .filter { it.isNotEmpty() }
                .cancellable()
                .collect {
                    pageTitle = it
                }
        }
        onDispose {
            job.cancel()
        }
    }

    LazyLoad(loaded = loaded) {
        webViewNavigator.loadUrl(LOGIN_URL)
        loaded = true
    }

    val isLoading by remember {
        derivedStateOf {
            webViewState.loadingState is LoadingState.Loading
        }
    }

    val progress by remember {
        derivedStateOf {
            webViewState.loadingState.let {
                if (it is LoadingState.Loading) {
                    it.progress
                } else {
                    0f
                }
            }
        }
    }

    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    MyScaffold(
        topBar = {
            Toolbar(
                title = {
                    Column {
                        Text(
                            text = displayPageTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isExternalHost) {
                            Text(
                                text = currentHost,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = ExtendedTheme.colors.onTopBarSecondary,
                                style = MaterialTheme.typography.caption
                            )
                        }
                    }
                },
                navigationIcon = { BackNavigationIcon(onBackPressed = { navigator.navigateUp() }) },
                actions = {
                    val menuState = rememberMenuState()
                    ClickMenu(
                        menuContent = {
                            DropdownMenuItem(
                                onClick = {
                                    webViewNavigator.reload()
                                    dismiss()
                                }
                            ) {
                                Text(text = stringResource(id = R.string.title_refresh))
                            }
                        },
                        menuState = menuState,
                        triggerShape = CircleShape
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = stringResource(id = R.string.btn_more)
                            )
                        }
                    }
                },
            )
        }
    ) { paddingValues ->
        Box {
            val snackbarHostState = LocalSnackbarHostState.current
            WebView(
                state = webViewState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                navigator = webViewNavigator,
                onCreated = {
                    it.settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                    }
                },
                client = remember(navigator) {
                    LoginWebViewClient(
                        navigator,
                        coroutineScope,
                        snackbarHostState
                    )
                },
                chromeClient = remember { MyWebChromeClient(context, coroutineScope) }
            )

            if (isLoading) {
                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

class LoginWebViewClient(
    nativeNavigator: DestinationsNavigator? = null,
    val coroutineScope: CoroutineScope,
    val snackbarHostState: SnackbarHostState,
) : MyWebViewClient(nativeNavigator) {
    private var isLoadingAccount = false

    override fun injectCookies(url: String) {}

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        if (url == null) {
            return
        }
        if (isLoadingAccount) {
            return
        }
        val cookieStr = CookieManager.getInstance().getCookie(url) ?: return
        val cookies = parseCookie(cookieStr).mapKeys { it.key.uppercase() }
        val bduss = cookies["BDUSS"]
        val sToken = cookies["STOKEN"]
        val baiduId = cookies["BAIDUID"]
        if (url.startsWith("https://tieba.baidu.com/index/tbwise/") || url.startsWith("https://tiebac.baidu.com/index/tbwise/")) {
            if (bduss == null || sToken == null) {
                return
            }
            if (!baiduId.isNullOrEmpty() && ClientUtils.baiduId.isNullOrEmpty()) {
                coroutineScope.launch {
                    ClientUtils.saveBaiduId(context, baiduId)
                }
            }
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    context.getString(R.string.text_please_wait),
                    duration = SnackbarDuration.Indefinite
                )
            }
            coroutineScope.launch {
                AccountUtil.fetchAccountFlow(bduss, sToken, cookieStr)
                    .catch {
                        coroutineScope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            snackbarHostState.showSnackbar(
                                context.getString(
                                    R.string.text_login_failed,
                                    it.getErrorMessage()
                                ), duration = SnackbarDuration.Short
                            )
                        }
                        navigator.loadUrl(LOGIN_URL)
                        isLoadingAccount = false
                    }
                    .flowOn(Dispatchers.Main)
                    .collect { account ->
                        isLoadingAccount = false
                        AccountUtil.newAccount(account.uid, account) {
                            if (it) {
                                AccountUtil.switchAccount(context, account.id)
                                coroutineScope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    snackbarHostState.showSnackbar(
                                        context.getString(R.string.text_login_success),
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                coroutineScope.launch {
                                    delay(1500L)
                                    nativeNavigator?.navigateUp()
                                }
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    snackbarHostState.showSnackbar(
                                        context.getString(R.string.text_login_failed_default),
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                view.loadUrl(LOGIN_URL)
                            }
                        }
                    }
            }
        }
    }
}