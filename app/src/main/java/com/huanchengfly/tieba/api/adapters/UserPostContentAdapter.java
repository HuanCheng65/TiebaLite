package com.huanchengfly.tieba.api.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.huanchengfly.tieba.api.models.UserPostBean;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UserPostContentAdapter implements JsonDeserializer<List<UserPostBean.ContentBean>> {
    @Override
    public List<UserPostBean.ContentBean> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<UserPostBean.ContentBean> list = new ArrayList<>();
        if (json.isJsonArray()) {
            JsonArray jsonArray = json.getAsJsonArray();
            for (JsonElement jsonElement : jsonArray) {
                if (jsonElement.isJsonObject()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    UserPostBean.ContentBean contentBean = new UserPostBean.ContentBean()
                            .setCreateTime(getString(jsonObject.get("create_time")))
                            .setPostId(getString(jsonObject.get("post_id")));
                    List<UserPostBean.PostContentBean> postContentBeans = new ArrayList<>();
                    JsonElement contentElement = jsonObject.get("post_content");
                    if (contentElement.isJsonArray()) {
                        JsonArray contentArray = contentElement.getAsJsonArray();
                        for (JsonElement jsonElement1 : contentArray) {
                            if (jsonElement1.isJsonObject()) {
                                JsonObject postContentObject = jsonElement1.getAsJsonObject();
                                postContentBeans.add(new UserPostBean.PostContentBean()
                                        .setType(getNonNullString(postContentObject.get("type"), "0"))
                                        .setText(getNonNullString(postContentObject.get("text"), "")));
                            }
                        }
                    }
                    contentBean.setPostContent(postContentBeans);
                    list.add(contentBean);
                }
            }
        }
        return list;
    }

    @Nullable
    private String getString(JsonElement jsonElement) {
        return jsonElement != null && !jsonElement.isJsonNull() ? jsonElement.getAsString() : null;
    }

    @NonNull
    private String getNonNullString(JsonElement jsonElement, String defValue) {
        return jsonElement != null && !jsonElement.isJsonNull() ? jsonElement.getAsString() : defValue;
    }
}
