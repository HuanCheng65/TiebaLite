package com.huanchengfly.utils;

import android.content.Context;
import android.webkit.WebResourceResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AssetUtil {
    public static String TYPE_CSS = "text/css";
    public static String TYPE_JS = "application/javascript";
    public static String TYPE_FONT_WOFF = "application/x-font-woff";

    public static WebResourceResponse getResponseFromAssets(Context context, String filename, String mimeType) {
        if (filename.equals("")) {
            return null;
        }
        InputStream is = null;
        try {
            is = context.getAssets().open(filename);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new WebResourceResponse(mimeType, "utf-8", is);
    }

    public static WebResourceResponse getEmptyResponse() {
        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
    }

    public static String getStringFromAsset(Context context, String file) {
        if (context == null) {
            return "";
        }
        try {
            InputStream is = context.getAssets().open(file);
            int length = is.available();
            byte[] buffer = new byte[length];
            is.read(buffer);
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
