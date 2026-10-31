package com.huanchengfly.tieba.post.utils

import android.net.Uri
import com.huanchengfly.tieba.post.ui.page.destinations.ForumPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ThreadPageDestination
import com.ramcosta.composedestinations.spec.Direction

fun isTiebaHost(host: String): Boolean =
    host == "wapp.baidu.com" || host.contains("tieba.baidu.com") || host == "tiebac.baidu.com"

/**
 * Maps a Tieba web URL or a Tieba app link to the native page showing the same content, or
 * returns null if there is no native counterpart.
 */
fun Uri.toTiebaDirection(): Direction? {
    // Some links are written as "scheme://tieba.baidu.com//kw=..."
    val uri = Uri.parse(toString().replace("://tieba.baidu.com//", "://tieba.baidu.com/?"))
    val scheme = uri.scheme?.lowercase() ?: return null
    val host = uri.host?.lowercase().orEmpty()
    val path = uri.path?.lowercase().orEmpty()

    fun forum(vararg keys: String) = keys.firstNotNullOfOrNull { uri.getQueryParameter(it) }
        ?.takeIf { it.isNotEmpty() }
        ?.let { ForumPageDestination(it) }

    fun thread(key: String) = uri.getQueryParameter(key)?.toLongOrNull()
        ?.let { ThreadPageDestination(it) }

    return when (scheme) {
        "http", "https" -> when {
            !isTiebaHost(host) -> null
            path == "/f" || path == "/mo/q/m" -> thread("kz") ?: forum("kw", "word")
            path.startsWith("/p/") -> path.substring(3).toLongOrNull()?.let { ThreadPageDestination(it) }
            else -> null
        }

        "tbfrs" -> forum("kw")
        "tbpb" -> thread("tid")
        "com.baidu.tieba" -> when {
            host != "unidispatch" -> null
            path == "/frs" -> forum("kw")
            path == "/pb" -> thread("tid")
            else -> null
        }

        else -> null
    }
}
