package com.huanchengfly.tieba.post.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.activities.base.BaseActivity;
import com.huanchengfly.tieba.post.adapters.ThemeAdapter;
import com.huanchengfly.tieba.post.utils.SharedPreferencesUtil;
import com.huanchengfly.tieba.post.utils.ThemeUtil;

import java.util.Arrays;
import java.util.List;

import static com.huanchengfly.tieba.post.utils.ThemeUtil.SP_TRANSLUCENT_THEME_BACKGROUND_PATH;
import static com.huanchengfly.tieba.post.utils.ThemeUtil.THEME_TRANSLUCENT;

public class ThemeActivity extends BaseActivity {
    public static final String TAG = "ThemeActivity";

    private long lastClickTimestamp = 0;
    private int clickTimes = 0;

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);
        mRecyclerView = (RecyclerView) findViewById(R.id.theme_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ThemeAdapter themeAdapter = new ThemeAdapter(this);
        mRecyclerView.setAdapter(themeAdapter);
        if (mRecyclerView.getItemAnimator() != null) {
            mRecyclerView.getItemAnimator().setAddDuration(0);
            mRecyclerView.getItemAnimator().setChangeDuration(0);
            mRecyclerView.getItemAnimator().setMoveDuration(0);
            mRecyclerView.getItemAnimator().setRemoveDuration(0);
            ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        /*
        toolbar.setOnClickListener(v -> {
            if (System.currentTimeMillis() - lastClickTimestamp < 2000) {
                clickTimes += 1;
            } else {
                clickTimes = 0;
            }
            if (clickTimes >= 7) {
                clickTimes = 0;
                startActivity(new Intent(this, TranslucentThemeActivity.class));
                Toast.makeText(this, "\uD83D\uDC23", Toast.LENGTH_SHORT).show();
                finish();
            } else if (clickTimes >= 2) {
                Toast.makeText(this, "\uD83E\uDD5A", Toast.LENGTH_SHORT).show();
            }
            lastClickTimestamp = System.currentTimeMillis();
        });
        */
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_theme);
        }
        List<String> values = Arrays.asList(getResources().getStringArray(R.array.theme_values));
        themeAdapter.setOnItemClickListener((itemView, str, position, viewType) -> {
            String backgroundFilePath = SharedPreferencesUtil.get(this, SharedPreferencesUtil.SP_SETTINGS)
                    .getString(SP_TRANSLUCENT_THEME_BACKGROUND_PATH, null);
            if (values.get(position).equals(THEME_TRANSLUCENT) && backgroundFilePath == null) {
                startActivity(new Intent(this, TranslucentThemeActivity.class));
            }
            setTheme(values.get(position));
        });
        mRecyclerView.setAdapter(themeAdapter);
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background));
    }

    @SuppressLint("ApplySharedPref")
    private void setTheme(String theme) {
        ThemeUtil.getSharedPreferences(ThemeActivity.this).edit().putString(ThemeUtil.SP_THEME, theme).commit();
        if (!theme.contains("dark")) {
            ThemeUtil.getSharedPreferences(ThemeActivity.this).edit().putString(ThemeUtil.SP_OLD_THEME, theme).commit();
        }
        refreshUIIfNeed();
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background));
    }
}