package com.huanchengfly.tieba.post.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.huanchengfly.tieba.api.TiebaApi;
import com.huanchengfly.tieba.api.models.SearchPostBean;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.activities.base.BaseActivity;
import com.huanchengfly.tieba.post.adapters.SearchPostAdapter;
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager;
import com.huanchengfly.tieba.post.components.dividers.RecycleViewDivider;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.huanchengfly.tieba.post.utils.Util;
import com.lapism.searchview.Search;
import com.lapism.searchview.widget.SearchView;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchPostActivity extends BaseActivity implements Search.OnQueryTextListener {
    public static final String TAG = SearchPostActivity.class.getSimpleName();
    public static final String PARAM_FORUM = "forum_name";
    public static final String PARAM_KEYWORD = "keyword";
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private SearchPostAdapter searchPostAdapter;
    private String forumName;
    private String keyword;
    private int page;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_post);
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background));
        Util.setStatusBarTransparent(this);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);
        getWindow().setBackgroundDrawableResource(R.drawable.bg_trans);
        Intent intent = getIntent();
        forumName = intent.getStringExtra(PARAM_FORUM);
        if (forumName == null) {
            finish();
        }
        findView();
        initView();
        keyword = intent.getStringExtra(PARAM_KEYWORD);
        if (keyword != null) {
            searchView.setText(keyword);
            refresh();
        }
    }

    private void findView() {
        searchView = (SearchView) findViewById(R.id.toolbar_search_view);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.search_post_refresh_layout);
        recyclerView = (RecyclerView) findViewById(R.id.search_post_recycler_view);
    }

    private void initView() {
        ThemeUtil.setThemeForSwipeRefreshLayout(refreshLayout);
        searchPostAdapter = new SearchPostAdapter(this);
        searchPostAdapter.setLoadingView(R.layout.layout_footer_loading);
        searchPostAdapter.setLoadEndView(R.layout.layout_footer_loadend);
        searchPostAdapter.setLoadFailedView(R.layout.layout_footer_load_failed);
        searchPostAdapter.setOnLoadMoreListener(this::loadMore);
        recyclerView.setLayoutManager(new MyLinearLayoutManager(this));
        recyclerView.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.VERTICAL, R.drawable.drawable_divider_8dp));
        recyclerView.setAdapter(searchPostAdapter);
        refreshLayout.setOnRefreshListener(this::refresh);
        searchView.setHint(getString(R.string.hint_search_in_ba, forumName));
        searchView.setOnQueryTextListener(this);
        searchView.setOnLogoClickListener(this::finish);
    }

    public void refresh() {
        refreshLayout.setRefreshing(true);
        page = 1;
        TiebaApi.getInstance().searchPost(keyword, forumName, false, page, 30).enqueue(new Callback<SearchPostBean>() {
            @Override
            public void onResponse(@NotNull Call<SearchPostBean> call, @NotNull Response<SearchPostBean> response) {
                SearchPostBean data = response.body();
                if (!"1".equals(data.getPage().getHasMore())) {
                    searchPostAdapter.loadEnd();
                }
                searchPostAdapter.setNewData(data.getPostList());
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(@NotNull Call<SearchPostBean> call, @NotNull Throwable t) {
                searchPostAdapter.loadFailed();
                refreshLayout.setRefreshing(false);
            }
        });
    }

    public void loadMore(boolean isReload) {
        if (!isReload) {
            page += 1;
        }
        TiebaApi.getInstance().searchPost(keyword, forumName, false, page, 30).enqueue(new Callback<SearchPostBean>() {
            @Override
            public void onResponse(@NotNull Call<SearchPostBean> call, @NotNull Response<SearchPostBean> response) {
                SearchPostBean data = response.body();
                if (!"1".equals(data.getPage().getHasMore())) {
                    searchPostAdapter.loadEnd();
                }
                searchPostAdapter.setLoadMoreData(data.getPostList());
            }

            @Override
            public void onFailure(@NotNull Call<SearchPostBean> call, @NotNull Throwable t) {
                searchPostAdapter.loadFailed();
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(CharSequence query) {
        keyword = query.toString();
        refresh();
        return true;
    }

    @Override
    public void onQueryTextChange(CharSequence newText) {
    }
}
