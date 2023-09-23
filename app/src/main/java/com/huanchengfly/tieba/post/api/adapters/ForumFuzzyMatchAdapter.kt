package com.huanchengfly.tieba.post.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.huanchengfly.tieba.post.api.models.SearchForumBean
import java.lang.reflect.Type

class ForumFuzzyMatchAdapter : JsonDeserializer<List<SearchForumBean.ForumInfoBean>> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): List<SearchForumBean.ForumInfoBean> {
        val forumInfoBeans: MutableList<SearchForumBean.ForumInfoBean> = ArrayList()
        if (json.isJsonArray) {
            val jsonArray = json.asJsonArray
            for (element in jsonArray) {
                if (element.isJsonObject) {
                    forumInfoBeans.add(getForumInfoBean(element.asJsonObject))
                }
            }
        } else if (json.isJsonObject) {
            val jsonObject = json.asJsonObject
            for ((_, jsonElement) in jsonObject.entrySet()) {
                if (jsonElement.isJsonObject) {
                    forumInfoBeans.add(getForumInfoBean(jsonElement.asJsonObject))
                }
            }
        }
        return forumInfoBeans
    }

    private fun getNonNullString(jsonElement: JsonElement): String? {
        return if (jsonElement.isJsonNull) null else jsonElement.asString
    }

    private fun getForumInfoBean(jsonObject: JsonObject): SearchForumBean.ForumInfoBean {
        return SearchForumBean.ForumInfoBean(
            forumId = jsonObject["forum_id"].asLong,
            forumName = getNonNullString(jsonObject["forum_name"]),
            forumNameShow = getNonNullString(jsonObject["forum_name_show"]),
            avatar = getNonNullString(jsonObject["avatar"]),
            postNum = getNonNullString(jsonObject["post_num"]) ?: "0",
            concernNum = getNonNullString(jsonObject["concern_num"]) ?: "0",
            hasConcerned = jsonObject["has_concerned"].asInt
        )
    }
}
