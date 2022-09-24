package com.huanchengfly.tieba.post.api.adapters

import com.google.gson.*
import java.lang.reflect.Type

class StringToBooleanAdapter : JsonDeserializer<Boolean>, JsonSerializer<Boolean> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Boolean {
        return getNonNullString(json) == "1"
    }

    private fun getNonNullString(jsonElement: JsonElement?): String {
        return if (jsonElement != null && !jsonElement.isJsonNull) jsonElement.asString else ""
    }

    override fun serialize(
        src: Boolean?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(if (src == true) "1" else "0")
    }
}