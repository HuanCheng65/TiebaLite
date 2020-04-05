package com.huanchengfly.tieba.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class ErrorMsgAdapter implements JsonDeserializer<String> {
    @Override
    public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive()) {
            return json.getAsString();
        } else if (json.isJsonObject()) {
            return json.getAsJsonObject().get("errmsg").getAsString();
        }
        return null;
    }
}
