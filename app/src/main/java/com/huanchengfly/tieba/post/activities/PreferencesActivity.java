package com.huanchengfly.tieba.post.activities;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.fragments.PreferencesFragment;
import com.huanchengfly.tieba.post.utils.ThemeUtil;

public class PreferencesActivity extends BaseActivity {

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
        PreferencesFragment preferencesFragment = new PreferencesFragment();
        fragmentManager.beginTransaction()
                .add(R.id.main, preferencesFragment)
                .commit();
        if (scrollToPreference != null) {
            preferencesFragment.scrollToPreference(scrollToPreference);
        }
    }
}
