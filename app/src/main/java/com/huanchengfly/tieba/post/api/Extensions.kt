package com.huanchengfly.tieba.post.api

import okhttp3.FormBody
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder

fun String.urlEncode(): String {
    return try {
        URLEncoder.encode(this, "UTF-8")
    } catch (e: UnsupportedEncodingException) {
        e.printStackTrace()
        this
    }
}

fun String.urlDecode(): String {
    return try {
        URLDecoder.decode(this, "UTF-8")
    } catch (e: UnsupportedEncodingException) {
        e.printStackTrace()
        this
    }
}

fun FormBody.containsEncodedName(name: String): Boolean {
    repeat(size) {
        if (encodedName(it) == name) return true
    }
    return false
}

inline fun FormBody.forEach(block: (String, String) -> Unit) {
    repeat(size) {
        block(encodedName(it), encodedValue(it))
    }
}

fun FormBody.raw() =
    StringBuilder().apply {
        repeat(size) {
            if (it != 0) append('&')
            append(encodedName(it))
            append('=')
            append(encodedValue(it))
        }
    }.toString()

fun FormBody.sortedEncodedRaw(separator: Boolean = true): String {
    val nameAndValue = ArrayList<String>()
    repeat(size) {
        nameAndValue.add("${encodedName(it)}=${encodedValue(it)}")
    }
    if (separator) return nameAndValue.sorted().joinToString(separator = "&")
    else return nameAndValue.sorted().joinToString(separator = "")
}

fun FormBody.sortedRaw(separator: Boolean = true): String {
    val nameAndValue = ArrayList<String>()
    repeat(size) {
        nameAndValue.add("${name(it)}=${value(it)}")
    }
    if (separator) return nameAndValue.sorted().joinToString(separator = "&")
    else return nameAndValue.sorted().joinToString(separator = "")
}

fun FormBody.Builder.addAllEncoded(formBody: FormBody): FormBody.Builder {
    with(formBody) {
        repeat(size) {
            addEncoded(encodedName(it), encodedValue(it))
        }
    }
    return this
}

internal typealias ParamExpression = Pair<String, () -> String?>

internal inline fun Array<out ParamExpression>.forEachNonNull(action: (String, String) -> Unit) {
    forEach { (name, valueExpression) ->
        val value = valueExpression()
        if (value != null) {
            action(name, value)
        }
    }
}