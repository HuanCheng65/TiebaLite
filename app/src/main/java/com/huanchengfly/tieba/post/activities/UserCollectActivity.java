package com.huanchengfly.tieba.post.activities;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.activities.base.BaseActivity;
import com.huanchengfly.tieba.post.utils.ThemeUtil;

public class UserCollectActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_collect);
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_my_collect);
        }
    }
}
