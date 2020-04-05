package com.huanchengfly.tieba.post.fragments;


import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.huanchengfly.tieba.api.TiebaApi;
import com.huanchengfly.tieba.api.models.SearchForumBean;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.adapters.SearchForumAdapter;
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager;
import com.huanchengfly.tieba.post.components.dividers.SearchDivider;
import com.huanchengfly.tieba.post.utils.ThemeUtil;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchForumFragment extends BaseFragment {
    public static final String TAG = "SearchForumFragment";

    public static final String ARG_KEYWORD = "keyword";
    @BindView(R.id.fragment_search_refresh_layout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.fragment_search_recycler_view)
    RecyclerView recyclerView;
    private String keyword;
    private SearchForumAdapter mAdapter;

    private SearchForumBean.DataBean mData;

    public SearchForumFragment() {
    }

    public static SearchForumFragment newInstance(String keyword) {
        SearchForumFragment forumFragment = new SearchForumFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_KEYWORD, keyword);
        forumFragment.setArguments(bundle);
        return forumFragment;
    }

    public void setKeyword(String keyword, boolean refresh) {
        this.keyword = keyword;
        if (refresh) {
            refresh();
        } else {
            this.mData = null;
            mAdapter.reset();
        }
    }

    @Override
    protected void onFragmentVisibleChange(boolean isVisible) {
        if (mData == null && isVisible) {
            refresh();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            keyword = getArguments().getString(ARG_KEYWORD);
        }
    }

    @Override
    int getLayoutId() {
        return R.layout.fragment_search;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager(new MyLinearLayoutManager(getAttachContext()));
        recyclerView.addItemDecoration(new SearchDivider(getAttachContext()));
        mAdapter = new SearchForumAdapter(getAttachContext());
        mAdapter.setLoadEndView(R.layout.layout_footer_loadend);
        mAdapter.setLoadFailedView(R.layout.layout_footer_load_failed);
        recyclerView.setAdapter(mAdapter);
        refreshLayout.setOnRefreshListener(this::refresh);
        ThemeUtil.setThemeForSwipeRefreshLayout(refreshLayout);
    }

    private void setRefreshing(boolean refreshing) {
        if (refreshLayout != null) refreshLayout.setRefreshing(refreshing);
    }

    private void refresh() {
        setRefreshing(true);
        TiebaApi.getInstance().searchForum(keyword).enqueue(new Callback<SearchForumBean>() {
            @Override
            public void onResponse(@NotNull Call<SearchForumBean> call, @NotNull Response<SearchForumBean> response) {
                mData = response.body().getData();
                mAdapter.setData(mData);
                setRefreshing(false);
                mAdapter.loadEnd();
            }

            @Override
            public void onFailure(@NotNull Call<SearchForumBean> call, @NotNull Throwable t) {
                setRefreshing(false);
                Toast.makeText(getAttachContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onFragmentFirstVisible() {
        refresh();
    }
}