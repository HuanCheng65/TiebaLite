package com.huanchengfly.tieba.post.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.fragments.MessageFragment;
import com.huanchengfly.tieba.post.utils.ThemeUtil;

public class MessageActivity extends BaseActivity {

    public static Intent createIntent(Context context, int type) {
        return new Intent(context, MessageActivity.class)
                .putExtra("type", type);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        AppBarLayout.LayoutParams layoutParams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        layoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        toolbar.setLayoutParams(layoutParams);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_my_message);
        }
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_message_list_content,
                            MessageFragment.newInstance(getIntent().getIntExtra("type", MessageFragment.TYPE_REPLY_ME), false), MessageFragment.class.getSimpleName()).commit();
        }
    }
}