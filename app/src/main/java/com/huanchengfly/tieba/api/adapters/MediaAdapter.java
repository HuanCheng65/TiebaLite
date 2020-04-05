package com.huanchengfly.tieba.api.adapters;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.huanchengfly.tieba.api.models.ForumPageBean;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MediaAdapter implements JsonDeserializer<List<ForumPageBean.MediaInfoBean>> {
    @Override
    public List<ForumPageBean.MediaInfoBean> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<ForumPageBean.MediaInfoBean> mediaInfoBeans = new ArrayList<>();
        if (json.isJsonArray()) {
            JsonArray jsonArray = json.getAsJsonArray();
            for (JsonElement element : jsonArray) {
                if (element.isJsonObject()) {
                    JsonObject jsonObject = element.getAsJsonObject();
                    String type = getNonNullString(jsonObject.get("type"));
                    if (TextUtils.equals(type, "3")) {
                        mediaInfoBeans.add(new ForumPageBean.MediaInfoBean()
                                .setType(type)
                                .setBigPic(getNonNullString(jsonObject.get("big_pic")))
                                .setOriginPic(getNonNullString(jsonObject.get("origin_pic")))
                                .setSrcPic(getNonNullString(jsonObject.get("src_pic")))
                                .setPostId(getNonNullString(jsonObject.get("post_id")))
                                .setIsLongPic(getNonNullString(jsonObject.get("is_long_pic")))
                                .setShowOriginalBtn(getNonNullString(jsonObject.get("show_original_btn"))));
                    }
                }
            }
        }
        return mediaInfoBeans;
    }

    @Nullable
    private String getNonNullString(JsonElement jsonElement) {
        return jsonElement != null && !jsonElement.isJsonNull() ? jsonElement.getAsString() : null;
    }
}
