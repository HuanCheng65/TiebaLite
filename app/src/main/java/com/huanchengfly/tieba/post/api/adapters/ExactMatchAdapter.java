package com.huanchengfly.tieba.post.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.huanchengfly.tieba.post.api.models.SearchForumBean;

import java.lang.reflect.Type;

public class ExactMatchAdapter implements JsonDeserializer<SearchForumBean.ForumInfoBean> {
    @Override
    public SearchForumBean.ForumInfoBean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonArray()) {
            return null;
        }
        return context.deserialize(json, typeOfT);
    }
}
