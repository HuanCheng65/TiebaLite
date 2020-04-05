package com.huanchengfly.tieba.api.adapters;

import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.huanchengfly.tieba.api.models.SearchForumBean;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ForumFuzzyMatchAdapter implements JsonDeserializer<List<SearchForumBean.ForumInfoBean>> {
    @Override
    public List<SearchForumBean.ForumInfoBean> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<SearchForumBean.ForumInfoBean> forumInfoBeans = new ArrayList<>();
        if (json.isJsonArray()) {
            JsonArray jsonArray = json.getAsJsonArray();
            for (JsonElement element : jsonArray) {
                if (element.isJsonObject()) {
                    forumInfoBeans.add(getForumInfoBean(element.getAsJsonObject()));
                }
            }
        } else if (json.isJsonObject()) {
            JsonObject jsonObject = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> elementEntry : jsonObject.entrySet()) {
                JsonElement jsonElement = elementEntry.getValue();
                if (jsonElement.isJsonObject()) {
                    forumInfoBeans.add(getForumInfoBean(jsonElement.getAsJsonObject()));
                }
            }
        }
        return forumInfoBeans;
    }

    @Nullable
    private String getNonNullString(JsonElement jsonElement) {
        return jsonElement.isJsonNull() ? null : jsonElement.getAsString();
    }

    private SearchForumBean.ForumInfoBean getForumInfoBean(JsonObject jsonObject) {
        return new SearchForumBean.ForumInfoBean()
                .setForumId(jsonObject.get("forum_id").getAsInt())
                .setForumName(getNonNullString(jsonObject.get("forum_name")))
                .setForumNameShow(getNonNullString(jsonObject.get("forum_name_show")))
                .setAvatar(getNonNullString(jsonObject.get("avatar")))
                .setPostNum(getNonNullString(jsonObject.get("post_num")))
                .setConcernNum(getNonNullString(jsonObject.get("concern_num")))
                .setHasConcerned(jsonObject.get("has_concerned").getAsInt());
    }
}
