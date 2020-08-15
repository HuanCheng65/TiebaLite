package com.huanchengfly.tieba.post.api.adapters;

import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.huanchengfly.tieba.post.api.models.SearchUserBean;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserFuzzyMatchAdapter implements JsonDeserializer<List<SearchUserBean.UserBean>> {
    @Override
    public List<SearchUserBean.UserBean> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<SearchUserBean.UserBean> userBeans = new ArrayList<>();
        if (json.isJsonArray()) {
            JsonArray jsonArray = json.getAsJsonArray();
            for (JsonElement element : jsonArray) {
                if (element.isJsonObject()) {
                    userBeans.add(getUserBean(element.getAsJsonObject()));
                }
            }
        } else if (json.isJsonObject()) {
            JsonObject jsonObject = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> elementEntry : jsonObject.entrySet()) {
                JsonElement jsonElement = elementEntry.getValue();
                if (jsonElement.isJsonObject()) {
                    userBeans.add(getUserBean(jsonElement.getAsJsonObject()));
                }
            }
        }
        return userBeans;
    }

    @Nullable
    private String getNonNullString(JsonElement jsonElement) {
        return jsonElement.isJsonNull() ? null : jsonElement.getAsString();
    }

    private SearchUserBean.UserBean getUserBean(JsonObject jsonObject) {
        return new SearchUserBean.UserBean()
                .setId(getNonNullString(jsonObject.get("id")))
                .setIntro(getNonNullString(jsonObject.get("intro")))
                .setUserNickname(getNonNullString(jsonObject.get("user_nickname")))
                .setName(getNonNullString(jsonObject.get("name")))
                .setPortrait(getNonNullString(jsonObject.get("portrait")))
                .setFansNum(getNonNullString(jsonObject.get("fans_num")))
                .setHasConcerned(jsonObject.get("has_concerned").getAsInt());
    }
}
