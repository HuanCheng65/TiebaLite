package com.huanchengfly.tieba.post.activities;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.huanchengfly.tieba.post.utils.NavigationHelper;

import java.util.HashMap;
import java.util.Map;

public class JumpActivity extends AppCompatActivity {
    public static final String ACTION_JUMP = "com.huanchengfly.tieba.post.ACTION_JUMP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = getIntent().getData();
        String action = getIntent().getAction();
        NavigationHelper navigationHelper = NavigationHelper.newInstance(this);
        if (uri != null) {
            String url = uri.toString();
            Uri newUri = Uri.parse(url.replace("://tieba.baidu.com//", "://tieba.baidu.com/?"));
            if (newUri.getScheme().equalsIgnoreCase("http") || newUri.getScheme().equalsIgnoreCase("https")) {
                navigationHelper.navigationByData(NavigationHelper.ACTION_URL, newUri.toString());
            } else if (newUri.getScheme().equals("tbfrs")) {
                navigationHelper.navigationByData(NavigationHelper.ACTION_FORUM, newUri.getQueryParameter("kw"));
            } else if (newUri.getScheme().equals("tbpb")) {
                Map<String, String> map = new HashMap<>();
                map.put("tid", newUri.getQueryParameter("tid"));
                navigationHelper.navigationByData(NavigationHelper.ACTION_THREAD, map);
            }
        }
        this.finish();
    }
}