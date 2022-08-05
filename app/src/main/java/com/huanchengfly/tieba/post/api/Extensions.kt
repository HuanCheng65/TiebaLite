package com.huanchengfly.tieba.post.api

import com.huanchengfly.tieba.post.api.retrofit.body.MyMultipartBody
import okhttp3.FormBody
import okio.Buffer
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
    return if (separator) nameAndValue.sorted().joinToString(separator = "&")
    else nameAndValue.sorted().joinToString(separator = "")
}

fun FormBody.sortedRaw(separator: Boolean = true): String {
    val nameAndValue = ArrayList<String>()
    repeat(size) {
        nameAndValue.add("${name(it)}=${value(it)}")
    }
    return if (separator) nameAndValue.sorted().joinToString(separator = "&")
    else nameAndValue.sorted().joinToString(separator = "")
}

fun FormBody.Builder.addAllEncoded(formBody: FormBody): FormBody.Builder {
    with(formBody) {
        repeat(size) {
            addEncoded(encodedName(it), encodedValue(it))
        }
    }
    return this
}

fun MyMultipartBody.Builder.addAllParts(myMultipartBody: MyMultipartBody): MyMultipartBody.Builder {
    repeat(myMultipartBody.size) {
        val part = myMultipartBody.part(it)
        addPart(part)
    }
    return this
}

fun MyMultipartBody.contains(name: String): Boolean {
    repeat(size) {
        if (part(it).name() == name) return true
    }
    return false
}

fun MyMultipartBody.Part.contentDisposition(): Map<String, String> {
    val headersMap = mutableMapOf<String, String>()
    headers?.toString()?.split(";")?.forEach {
        val header = it.trim().split("=").toMutableList()
        if (header.size >= 2) {
            val name = header.removeFirst().trim()
            val value = header.joinToString("=").trim().trim('"')
            headersMap[name] = value
        }
    }
    return headersMap
}

fun MyMultipartBody.Part.name(): String? {
    return contentDisposition()["name"]
}

fun MyMultipartBody.Part.fileName(): String? {
    return contentDisposition()["filename"]
}

fun MyMultipartBody.newBuilder(): MyMultipartBody.Builder =
    MyMultipartBody.Builder(boundary).setType(type)

fun MyMultipartBody.sort(): MyMultipartBody {
    val builder = newBuilder()
    var fileParts = mutableListOf<MyMultipartBody.Part>()
    parts.forEach {
        if (it.fileName() != null) {
            fileParts.add(it)
        }
    }
    parts.filterNot { it in fileParts }.sortedBy { it.name() }.forEach { builder.addPart(it) }
    if (fileParts.isNotEmpty()) fileParts.sortedBy { it.fileName() }.forEach { builder.addPart(it) }
    return builder.build()
}

fun MyMultipartBody.Part.readString(): String {
    val buffer = Buffer()
    body.writeTo(buffer)
    return buffer.readUtf8()
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