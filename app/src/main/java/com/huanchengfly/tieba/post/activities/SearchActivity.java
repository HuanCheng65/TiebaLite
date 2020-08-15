package com.huanchengfly.tieba.post.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.android.material.tabs.TabLayout;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.adapters.FragmentTabViewPagerAdapter;
import com.huanchengfly.tieba.post.fragments.SearchForumFragment;
import com.huanchengfly.tieba.post.fragments.SearchThreadFragment;
import com.huanchengfly.tieba.post.fragments.SearchUserFragment;
import com.huanchengfly.tieba.post.models.database.SearchHistory;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.huanchengfly.tieba.post.widgets.MyViewPager;
import com.lapism.searchview.Search;
import com.lapism.searchview.widget.SearchView;

public class SearchActivity extends BaseActivity implements Search.OnQueryTextListener {
    public static final String TAG = SearchActivity.class.getSimpleName();
    public static final String EXTRA_KEYWORD = "keyword";

    private String mKeyword;
    private TabLayout mTabLayout;
    private SearchForumFragment searchForumFragment;
    private SearchThreadFragment searchThreadFragment;
    private SearchUserFragment searchUserFragment;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background));
        Intent intent = getIntent();
        mKeyword = intent.getStringExtra(EXTRA_KEYWORD);
        mSearchView = (SearchView) findViewById(R.id.toolbar_search_view);
        mSearchView.setTheme(ThemeUtil.isNightMode(this) ? Search.Theme.DARK : Search.Theme.LIGHT);
        mSearchView.setShadow(false);
        mSearchView.setBackgroundColor(ThemeUtils.getColorByAttr(this, R.attr.colorToolbar));
        mSearchView.setClearColor(ThemeUtils.getColorByAttr(this, R.attr.colorToolbarItem));
        mSearchView.setLogoColor(ThemeUtils.getColorByAttr(this, R.attr.colorToolbarItem));
        mSearchView.setTextColor(ThemeUtils.getColorByAttr(this, R.attr.colorToolbarItem));
        mSearchView.setHintColor(ThemeUtils.getColorByAttr(this, R.attr.color_toolbar_item_secondary));
        mTabLayout = (TabLayout) findViewById(R.id.tab);
        MyViewPager mViewPager = (MyViewPager) findViewById(R.id.view_pager);
        mViewPager.setOffscreenPageLimit(3);
        mSearchView.setQuery(mKeyword, false);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnLogoClickListener(this::finish);
        FragmentTabViewPagerAdapter mAdapter = new FragmentTabViewPagerAdapter(getSupportFragmentManager());
        searchForumFragment = SearchForumFragment.newInstance(mKeyword);
        searchThreadFragment = SearchThreadFragment.newInstance(mKeyword);
        searchUserFragment = SearchUserFragment.newInstance(mKeyword);
        mAdapter.addFragment(searchForumFragment, "吧");
        mAdapter.addFragment(searchThreadFragment, "贴");
        mAdapter.addFragment(searchUserFragment, "人");
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public boolean onQueryTextSubmit(CharSequence query) {
        setKeyword(query.toString());
        new SearchHistory(query.toString())
                .saveOrUpdate("content = ?", query.toString());
        return true;
    }

    private void setKeyword(String keyword) {
        if (TextUtils.equals(keyword, mKeyword)) {
            return;
        }
        mKeyword = keyword;
        searchForumFragment.setKeyword(keyword, mTabLayout.getSelectedTabPosition() == 0);
        searchThreadFragment.setKeyword(keyword, mTabLayout.getSelectedTabPosition() == 1);
        searchUserFragment.setKeyword(keyword, mTabLayout.getSelectedTabPosition() == 2);
    }

    @Override
    public void onQueryTextChange(CharSequence newText) {
    }
}