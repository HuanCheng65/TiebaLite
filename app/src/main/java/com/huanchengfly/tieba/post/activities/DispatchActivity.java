package com.huanchengfly.tieba.post.activities;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.huanchengfly.tieba.post.utils.NavigationHelper;

import java.util.HashMap;
import java.util.Map;

public class DispatchActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = getIntent().getData();
        NavigationHelper navigationHelper = NavigationHelper.newInstance(this);
        if (uri != null) {
            String url = uri.toString();
            Uri newUri = Uri.parse(url.replace("://tieba.baidu.com//", "://tieba.baidu.com/?"));
            if ("http".equalsIgnoreCase(newUri.getScheme()) || "https".equalsIgnoreCase(newUri.getScheme())) {
                navigationHelper.navigationByData(NavigationHelper.ACTION_URL, newUri.toString());
            } else if (newUri.getScheme().equals("tbfrs")) {
                navigationHelper.navigationByData(NavigationHelper.ACTION_FORUM, newUri.getQueryParameter("kw"));
            } else if (newUri.getScheme().equals("tbpb")) {
                Map<String, String> map = new HashMap<>();
                map.put("tid", newUri.getQueryParameter("tid"));
                navigationHelper.navigationByData(NavigationHelper.ACTION_THREAD, map);
            } else if (newUri.getScheme().equals("com.baidu.tieba") && "unidispatch".equals(newUri.getHost())) {
                if ("/frs".equals(newUri.getPath())) {
                    navigationHelper.navigationByData(NavigationHelper.ACTION_FORUM, newUri.getQueryParameter("kw"));
                } else if ("/pb".equals(newUri.getPath())) {
                    Map<String, String> map = new HashMap<>();
                    map.put("tid", newUri.getQueryParameter("tid"));
                    navigationHelper.navigationByData(NavigationHelper.ACTION_THREAD, map);
                }
            }
        }
        finish();
    }
}