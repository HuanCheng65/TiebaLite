package com.huanchengfly.tieba.post.activities;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.fragments.SettingsFragment;
import com.huanchengfly.tieba.post.utils.ThemeUtil;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_settings);
        }
        String scrollToPreference = getIntent().getStringExtra("scroll_to_preference");
        FragmentManager fragmentManager = getSupportFragmentManager();
        SettingsFragment settingsFragment = new SettingsFragment();
        fragmentManager.beginTransaction()
                .add(R.id.main, settingsFragment)
                .commit();
        if (scrollToPreference != null) {
            settingsFragment.scrollToPreference(scrollToPreference);
        }
    }
}
