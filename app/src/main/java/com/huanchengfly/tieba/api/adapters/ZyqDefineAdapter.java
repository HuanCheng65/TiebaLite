package com.huanchengfly.tieba.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.huanchengfly.tieba.api.models.ForumPageBean;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ZyqDefineAdapter implements JsonDeserializer<List<ForumPageBean.ZyqDefineBean>> {
    @Override
    public List<ForumPageBean.ZyqDefineBean> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<ForumPageBean.ZyqDefineBean> zyqDefineBeans = new ArrayList<>();
        JsonObject jsonObject = json.getAsJsonObject();
        for (Map.Entry<String, JsonElement> elementEntry : jsonObject.entrySet()) {
            JsonElement jsonElement = elementEntry.getValue();
            zyqDefineBeans.add(getZyqDefineBean(elementEntry.getKey(), jsonElement.getAsString()));
        }
        return zyqDefineBeans;
    }

    private ForumPageBean.ZyqDefineBean getZyqDefineBean(String name, String link) {
        ForumPageBean.ZyqDefineBean zyqDefineBean = new ForumPageBean.ZyqDefineBean();
        zyqDefineBean.setName(name);
        zyqDefineBean.setLink(link);
        return zyqDefineBean;
    }
}
