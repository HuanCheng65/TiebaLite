package com.huanchengfly.tieba.post.ui.page.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.LocationManager
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.JsResult
import android.webkit.ValueCallback
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
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
import androidx.core.content.getSystemService
import androidx.core.location.LocationManagerCompat
import androidx.core.net.toUri
import com.hjq.permissions.Permission
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.arch.GlobalEvent
import com.huanchengfly.tieba.post.arch.onGlobalEvent
import com.huanchengfly.tieba.post.components.dialogs.PermissionDialog
import com.huanchengfly.tieba.post.models.PermissionBean
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.ui.page.destinations.ForumPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ThreadPageDestination
import com.huanchengfly.tieba.post.ui.widgets.compose.AccompanistWebChromeClient
import com.huanchengfly.tieba.post.ui.widgets.compose.AccompanistWebViewClient
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.ClickMenu
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.LoadingState
import com.huanchengfly.tieba.post.ui.widgets.compose.MyScaffold
import com.huanchengfly.tieba.post.ui.widgets.compose.Toolbar
import com.huanchengfly.tieba.post.ui.widgets.compose.WebView
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberMenuState
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberSaveableWebViewState
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberWebViewNavigator
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.DialogUtil
import com.huanchengfly.tieba.post.utils.PermissionUtils
import com.huanchengfly.tieba.post.utils.PermissionUtils.PermissionData
import com.huanchengfly.tieba.post.utils.TiebaUtil
import com.huanchengfly.tieba.post.utils.appPreferences
import com.huanchengfly.tieba.post.utils.compose.launchActivityForResult
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.UUID

@SuppressLint("SetJavaScriptEnabled")
@Destination
@Composable
fun WebViewPage(
    initialUrl: String,
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
        webViewNavigator.loadUrl(initialUrl)
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
                                    val url = webViewState.webView?.url ?: initialUrl
                                    TiebaUtil.copyText(context, url)
                                    dismiss()
                                }
                            ) {
                                Text(text = stringResource(id = R.string.title_copy_link))
                            }
                            DropdownMenuItem(
                                onClick = {
                                    val uri = (webViewState.webView?.url ?: initialUrl).toUri()
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            uri
                                        )
                                    )
                                    dismiss()
                                }
                            ) {
                                Text(text = stringResource(id = R.string.title_open_in_browser))
                            }
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
                client = remember(navigator) { MyWebViewClient(navigator) },
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

fun isTiebaHost(host: String): Boolean {
    return host == "wapp.baidu.com" ||
            host.contains("tieba.baidu.com") ||
            host == "tiebac.baidu.com"
}

fun isInternalHost(host: String): Boolean {
    return isTiebaHost(host) ||
            host.contains("wappass.baidu.com") ||
            host.contains("ufosdk.baidu.com") ||
            host.contains("m.help.baidu.com")
}

open class MyWebViewClient(
    protected val nativeNavigator: DestinationsNavigator? = null,
) : AccompanistWebViewClient() {
    val context: Context
        get() = state.webView?.context ?: App.INSTANCE

    private fun interceptWebViewRequest(
        webView: WebView,
        request: WebResourceRequest,
    ): Boolean {
        val newUri = request.url
        val scheme = newUri.scheme?.lowercase() ?: return false
        val host = newUri.host?.lowercase() ?: return false
        val path = newUri.path?.lowercase() ?: return false
        val isHttp = scheme.startsWith("http")
        val isTieba = host == "wapp.baidu.com" ||
                host.contains("tieba.baidu.com") ||
                host == "tiebac.baidu.com"
        val isInternal = isTieba ||
                host.contains("wappass.baidu.com") ||
                host.contains("ufosdk.baidu.com") ||
                host.contains("m.help.baidu.com")
        return when {
            isHttp && isTieba -> {
                if (path == "/f" || path == "/mo/q/m") {
                    val forumName =
                        newUri.getQueryParameter("kw") ?: newUri.getQueryParameter("word")
                    val threadId = newUri.getQueryParameter("kz")?.toLongOrNull()
                    if (threadId != null) {
                        nativeNavigator?.navigate(
                            ThreadPageDestination(threadId)
                        )
                        true
                    } else if (forumName != null) {
                        nativeNavigator?.navigate(
                            ForumPageDestination(forumName)
                        )
                        true
                    } else false
                } else if (path.startsWith("/p/")) {
                    val threadId = path.substring(3).toLongOrNull()
                    if (threadId != null) {
                        nativeNavigator?.navigate(
                            ThreadPageDestination(threadId)
                        )
                        true
                    } else false
                } else false
            }

            isHttp && !isInternal -> {
                if (context.appPreferences.useWebView)
                    false
                else {
                    if (context.appPreferences.useCustomTabs) {
                        runCatching {
                            CustomTabsIntent.Builder()
                                .setShowTitle(true)
                                .setDefaultColorSchemeParams(
                                    CustomTabColorSchemeParams.Builder()
                                        .setToolbarColor(
                                            ThemeUtils.getColorByAttr(
                                                context,
                                                R.attr.colorToolbar
                                            )
                                        )
                                        .setNavigationBarColor(
                                            ThemeUtils.getColorByAttr(
                                                context,
                                                R.attr.colorNavBar
                                            )
                                        )
                                        .setNavigationBarDividerColor(
                                            ThemeUtils.getColorByAttr(
                                                context,
                                                R.attr.colorDivider
                                            )
                                        )
                                        .build()
                                )
                                .build()
                                .launchUrl(context, newUri)
                        }.onFailure {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    newUri
                                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            )
                        }
                    } else {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                newUri
                            ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        )
                    }
                    true
                }
            }

            !isHttp -> {
                val currentUri = webView.url?.toUri()
                val currentHost = currentUri?.host
                if (currentHost != null) {
                    launchThirdPartyApp(newUri, currentHost)
                }
                true
            }

            else -> false
        }
    }

    private fun launchThirdPartyApp(
        intent: Intent,
        host: String,
    ) {
        val resolves = context.packageManager.queryIntentActivities(intent, 0)
        if (resolves.isEmpty()) return
        val appName = if (resolves.size == 1) {
            resolves[0].loadLabel(context.packageManager)
        } else {
            context.getString(R.string.name_multi_app)
        }
        val scheme = intent.scheme ?: ""
        PermissionDialog(
            context,
            PermissionBean(
                PermissionDialog.CustomPermission.PERMISSION_START_APP,
                "${host}_${scheme}",
                context.getString(R.string.title_start_app_permission, host, appName),
                R.drawable.ic_round_exit_to_app
            )
        )
            .setOnGrantedCallback { context.startActivity(intent) }
            .show()
    }

    private fun launchThirdPartyApp(
        uri: Uri,
        host: String,
    ) {
        if (uri.scheme.equals("intent", ignoreCase = true)) {
            launchThirdPartyApp(
                Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME)
                    .addCategory(Intent.CATEGORY_BROWSABLE), host
            )
        } else {
            launchThirdPartyApp(
                Intent(
                    Intent.ACTION_VIEW,
                    uri
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP), host
            )
        }
    }

    open fun injectCookies(url: String) {
        val cookieStr = CookieManager.getInstance().getCookie(url) ?: ""
        val cookies = AccountUtil.parseCookie(cookieStr)
        val BDUSS = cookies["BDUSS"]
        val currentAccountBDUSS = AccountUtil.getBduss()
        if (currentAccountBDUSS != null && BDUSS != currentAccountBDUSS) {
            CookieManager.getInstance()
                .setCookie(url, AccountUtil.getBdussCookie(currentAccountBDUSS))
        }
    }

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        injectCookies(url ?: "")
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest?): Boolean {
        if (request == null) return false
        return interceptWebViewRequest(view, request)
    }
}

class MyWebChromeClient(
    context: Context,
    coroutineScope: CoroutineScope,
) : AccompanistWebChromeClient() {
    private val contextWeakReference = WeakReference(context)

    val context: Context
        get() = state.webView?.context ?: contextWeakReference.get() ?: App.INSTANCE

    private var uploadMessage: ValueCallback<Array<Uri>>? = null

    val id: String = UUID.randomUUID().toString()

    init {
        coroutineScope.onGlobalEvent<GlobalEvent.ActivityResult>(
            filter = { it.requesterId == id },
        ) {
            uploadMessage?.onReceiveValue(FileChooserParams.parseResult(it.resultCode, it.intent))
            uploadMessage = null
        }
    }

    private fun isEnabledLocationFunction(): Boolean {
        val locationManager = context.getSystemService<LocationManager>()
        return locationManager != null && LocationManagerCompat.isLocationEnabled(locationManager)
    }

    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?,
    ) {
        if (origin == null || callback == null) return
        PermissionDialog(
            context,
            PermissionBean(
                PermissionDialog.CustomPermission.PERMISSION_LOCATION,
                origin,
                context.getString(
                    R.string.title_ask_permission,
                    origin,
                    context.getString(R.string.common_permission_location)
                ),
                R.drawable.ic_round_location_on
            )
        )
            .setOnGrantedCallback { isForever: Boolean ->
                PermissionUtils.askPermission(
                    context,
                    PermissionData(
                        listOf(
                            Permission.ACCESS_COARSE_LOCATION,
                            Permission.ACCESS_FINE_LOCATION
                        ),
                        context.getString(R.string.usage_webview_location_permission)
                    ),
                    R.string.tip_no_permission,
                    {
                        if (isEnabledLocationFunction()) {
                            callback.invoke(origin, true, isForever)
                        } else {
                            callback.invoke(origin, false, false)
                        }
                    }
                ) {
                    callback.invoke(origin, false, false)
                }
            }
            .setOnDeniedCallback {
                callback.invoke(
                    origin,
                    false,
                    false
                )
            }
            .show()
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?,
    ): Boolean {
        if (webView == null || filePathCallback == null || fileChooserParams == null) return false
        uploadMessage?.onReceiveValue(null)
        uploadMessage = filePathCallback
        launchActivityForResult(id, fileChooserParams.createIntent())
        return true
    }

    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?,
    ): Boolean {
        DialogUtil.build(view?.context ?: context)
            .setMessage(message)
            .setPositiveButton(R.string.button_sure_default) { _, _ ->
                result?.confirm()
            }
            .setCancelable(false)
            .create()
            .show()
        return true
    }

    override fun onJsConfirm(
        view: WebView,
        url: String?,
        message: String?,
        result: JsResult,
    ): Boolean {
        val context = view.context ?: context
        if ("ClipboardGuardCopyRequest".equals(message, ignoreCase = true)) {
            val uri = Uri.parse(url)
            if (uri != null && uri.host != null) {
                PermissionDialog(
                    context,
                    PermissionBean(
                        PermissionDialog.CustomPermission.PERMISSION_CLIPBOARD_COPY,
                        uri.host,
                        context.getString(
                            R.string.title_ask_permission_clipboard_copy,
                            uri.host
                        ),
                        R.drawable.ic_round_file_copy
                    )
                )
                    .setOnGrantedCallback { result.confirm() }
                    .setOnDeniedCallback { result.cancel() }
                    .show()
            }
        } else {
            DialogUtil.build(view.context)
                .setTitle("Confirm")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    result.confirm()
                }
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    result.cancel()
                }
                .create()
                .show()
        }
        return true
    }
}

@Composable
fun MyWebView() {

}