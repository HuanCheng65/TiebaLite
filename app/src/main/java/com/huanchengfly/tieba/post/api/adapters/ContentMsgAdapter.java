package com.huanchengfly.tieba.post.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.huanchengfly.tieba.post.api.models.ThreadContentBean;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ContentMsgAdapter implements JsonDeserializer<List<ThreadContentBean.ContentBean>> {
    @Override
    public List<ThreadContentBean.ContentBean> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<ThreadContentBean.ContentBean> list = new ArrayList<>();
        if(json.isJsonArray()){
            json.getAsJsonArray().forEach(element->{
                if(element.isJsonObject()){
                    list.add(context.deserialize(element,ThreadContentBean.ContentBean.class));
                }
            });
        }

        return list;
    }
}
