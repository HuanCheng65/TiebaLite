package com.huanchengfly.tieba.post.api.retrofit.body

import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.internal.toImmutableList
import okio.Buffer
import okio.BufferedSink
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import java.io.IOException
import java.util.*

@Suppress("NAME_SHADOWING")
class MyMultipartBody internal constructor(
    private val boundaryByteString: ByteString,
    @get:JvmName("type") val type: MediaType,
    @get:JvmName("parts") val parts: List<Part>
) : RequestBody() {
    private val contentType: MediaType = "$type; boundary=$boundary".toMediaType()
    private var contentLength = -1L

    @get:JvmName("boundary")
    val boundary: String
        get() = boundaryByteString.utf8()

    /** The number of parts in this multipart body. */
    @get:JvmName("size")
    val size: Int
        get() = parts.size

    fun part(index: Int): Part = parts[index]

    /** A combination of [type] and [boundaryByteString]. */
    override fun contentType(): MediaType = contentType

    @Throws(IOException::class)
    override fun contentLength(): Long {
        var result = contentLength
        if (result == -1L) {
            result = writeOrCountBytes(null, true)
            contentLength = result
        }
        return result
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        writeOrCountBytes(sink, false)
    }

    /**
     * Either writes this request to [sink] or measures its content length. We have one method do
     * double-duty to make sure the counting and content are consistent, particularly when it comes
     * to awkward operations like measuring the encoded length of header strings, or the
     * length-in-digits of an encoded integer.
     */
    @Throws(IOException::class)
    private fun writeOrCountBytes(
        sink: BufferedSink?,
        countBytes: Boolean
    ): Long {
        var sink = sink
        var byteCount = 0L

        var byteCountBuffer: Buffer? = null
        if (countBytes) {
            byteCountBuffer = Buffer()
            sink = byteCountBuffer
        }

        for (part in parts) {
            val headers = part.headers
            val body = part.body

            sink!!.write(DASHDASH)
            sink.write(boundaryByteString)
            sink.write(CRLF)

            if (headers != null) {
                for (h in 0 until headers.size) {
                    sink.writeUtf8(headers.name(h))
                        .write(COLONSPACE)
                        .writeUtf8(headers.value(h))
                        .write(CRLF)
                }
            }

            val contentType = body.contentType()
            if (contentType != null) {
                sink.writeUtf8("Content-Type: ")
                    .writeUtf8(contentType.toString())
                    .write(CRLF)
            }

            val contentLength = body.contentLength()
            if (countBytes) {
                // We can't measure the body's size without the sizes of its components.
                byteCountBuffer!!.clear()
                return -1L
            }

            sink.write(CRLF)

            if (countBytes) {
                byteCount += contentLength
            } else {
                body.writeTo(sink)
            }

            sink.write(CRLF)
        }

        sink!!.write(DASHDASH)
        sink.write(boundaryByteString)
        sink.write(DASHDASH)
        sink.write(CRLF)

        if (countBytes) {
            byteCount += byteCountBuffer!!.size
            byteCountBuffer.clear()
        }

        return byteCount
    }

    class Part private constructor(
        @get:JvmName("headers") val headers: Headers?,
        @get:JvmName("body") val body: RequestBody
    ) {
        companion object {
            @JvmStatic
            fun create(body: RequestBody): Part = create(null, body)

            @JvmStatic
            fun create(headers: Headers?, body: RequestBody): Part {
                require(headers?.get("Content-Type") == null) { "Unexpected header: Content-Type" }
                require(headers?.get("Content-Length") == null) { "Unexpected header: Content-Length" }
                return Part(headers, body)
            }

            @JvmStatic
            fun createFormData(name: String, value: String): Part =
                createFormData(name, null, value.toRequestBody())

            @JvmStatic
            fun createFormData(name: String, filename: String?, body: RequestBody): Part {
                val disposition = buildString {
                    append("form-data; name=")
                    appendQuotedString(name)

                    if (filename != null) {
                        append("; filename=")
                        appendQuotedString(filename)
                    }
                }

                val headers = Headers.Builder()
                    .addUnsafeNonAscii("Content-Disposition", disposition)
                    .build()

                return create(headers, body)
            }
        }
    }

    class Builder @JvmOverloads constructor(boundary: String = UUID.randomUUID().toString()) {
        private val boundary: ByteString = boundary.encodeUtf8()
        private var type = MIXED
        private val parts = mutableListOf<Part>()

        /**
         * Set the MIME type. Expected values for `type` are [MIXED] (the default), [ALTERNATIVE],
         * [DIGEST], [PARALLEL] and [FORM].
         */
        fun setType(type: MediaType) = apply {
            require(type.type == "multipart") { "multipart != $type" }
            this.type = type
        }

        /** Add a part to the body. */
        fun addPart(body: RequestBody) = apply {
            addPart(Part.create(body))
        }

        /** Add a part to the body. */
        fun addPart(headers: Headers?, body: RequestBody) = apply {
            addPart(Part.create(headers, body))
        }

        /** Add a form data part to the body. */
        fun addFormDataPart(name: String, value: String) = apply {
            addPart(Part.createFormData(name, value))
        }

        /** Add a form data part to the body. */
        fun addFormDataPart(name: String, filename: String?, body: RequestBody) = apply {
            addPart(Part.createFormData(name, filename, body))
        }

        /** Add a part to the body. */
        fun addPart(part: Part) = apply {
            parts += part
        }

        /** Assemble the specified parts into a request body. */
        fun build(): MyMultipartBody {
            check(parts.isNotEmpty()) { "Multipart body must have at least one part." }
            return MyMultipartBody(boundary, type, parts.toImmutableList())
        }
    }

    companion object {
        /**
         * The "mixed" subtype of "multipart" is intended for use when the body parts are independent
         * and need to be bundled in a particular order. Any "multipart" subtypes that an implementation
         * does not recognize must be treated as being of subtype "mixed".
         */
        @JvmField
        val MIXED = "multipart/mixed".toMediaType()

        /**
         * The "multipart/alternative" type is syntactically identical to "multipart/mixed", but the
         * semantics are different. In particular, each of the body parts is an "alternative" version of
         * the same information.
         */
        @JvmField
        val ALTERNATIVE = "multipart/alternative".toMediaType()

        /**
         * This type is syntactically identical to "multipart/mixed", but the semantics are different.
         * In particular, in a digest, the default `Content-Type` value for a body part is changed from
         * "text/plain" to "message/rfc822".
         */
        @JvmField
        val DIGEST = "multipart/digest".toMediaType()

        /**
         * This type is syntactically identical to "multipart/mixed", but the semantics are different.
         * In particular, in a parallel entity, the order of body parts is not significant.
         */
        @JvmField
        val PARALLEL = "multipart/parallel".toMediaType()

        /**
         * The media-type multipart/form-data follows the rules of all multipart MIME data streams as
         * outlined in RFC 2046. In forms, there are a series of fields to be supplied by the user who
         * fills out the form. Each field has a name. Within a given form, the names are unique.
         */
        @JvmField
        val FORM = "multipart/form-data".toMediaType()

        private val COLONSPACE = byteArrayOf(':'.code.toByte(), ' '.code.toByte())
        private val CRLF = byteArrayOf('\r'.code.toByte(), '\n'.code.toByte())
        private val DASHDASH = byteArrayOf('-'.code.toByte(), '-'.code.toByte())

        /**
         * Appends a quoted-string to a StringBuilder.
         *
         * RFC 2388 is rather vague about how one should escape special characters in form-data
         * parameters, and as it turns out Firefox and Chrome actually do rather different things, and
         * both say in their comments that they're not really sure what the right approach is. We go
         * with Chrome's behavior (which also experimentally seems to match what IE does), but if you
         * actually want to have a good chance of things working, please avoid double-quotes, newlines,
         * percent signs, and the like in your field names.
         */
        internal fun StringBuilder.appendQuotedString(key: String) {
            append('"')
            for (element in key) {
                when (element) {
                    '\n' -> append("%0A")
                    '\r' -> append("%0D")
                    '"' -> append("%22")
                    else -> append(element)
                }
            }
            append('"')
        }
    }
}
