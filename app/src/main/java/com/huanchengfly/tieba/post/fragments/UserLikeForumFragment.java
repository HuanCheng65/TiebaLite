package com.huanchengfly.tieba.post.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.adapters.UserLikeForumAdapter;
import com.huanchengfly.tieba.post.api.TiebaApi;
import com.huanchengfly.tieba.post.api.models.UserLikeForumBean;
import com.huanchengfly.tieba.post.components.dividers.StaggeredDividerItemDecoration;
import com.huanchengfly.tieba.post.utils.NavigationHelper;
import com.huanchengfly.tieba.post.utils.ThemeUtil;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserLikeForumFragment extends BaseFragment {
    public static final String PARAM_UID = "uid";
    private String uid;
    private int page;

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private UserLikeForumAdapter userLikeForumAdapter;

    private View emptyView;
    private TextView emptyTipView;

    private UserLikeForumBean userLikeForumBean;

    public UserLikeForumFragment() {
    }

    public static UserLikeForumFragment newInstance(String uid) {
        UserLikeForumFragment fragment = new UserLikeForumFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_UID, uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            uid = args.getString(PARAM_UID, null);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_user_like_forum;
    }

    @NotNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView = super.onCreateView(inflater, container, savedInstanceState);
        refreshLayout = contentView.findViewById(R.id.refresh);
        refreshLayout.setNestedScrollingEnabled(true);
        ThemeUtil.setThemeForSwipeRefreshLayout(refreshLayout);
        refreshLayout.setOnRefreshListener(this::refresh);
        recyclerView = contentView.findViewById(R.id.user_post_reclcyer_view);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.addItemDecoration(new StaggeredDividerItemDecoration(getAttachContext(), 16));
        userLikeForumAdapter = new UserLikeForumAdapter(getAttachContext());
        emptyView = View.inflate(getAttachContext(), R.layout.layout_empty_view, null);
        emptyTipView = emptyView.findViewById(R.id.empty_tip);
        NavigationHelper navigationHelper = NavigationHelper.newInstance(getAttachContext());
        userLikeForumAdapter.setOnItemChildClickListener(R.id.forum_item_card, (viewHolder, forumBean, position) -> navigationHelper.navigationByData(NavigationHelper.ACTION_FORUM, forumBean.getName()));
        userLikeForumAdapter.setLoadingView(R.layout.layout_footer_loading);
        userLikeForumAdapter.setEmptyView(emptyView);
        userLikeForumAdapter.setLoadEndView(R.layout.layout_footer_loadend);
        userLikeForumAdapter.setLoadFailedView(R.layout.layout_footer_load_failed);
        userLikeForumAdapter.setOnLoadMoreListener(this::load);
        recyclerView.setAdapter(userLikeForumAdapter);
        return contentView;
    }

    public void refresh() {
        page = 1;
        userLikeForumAdapter.reset();
        refreshLayout.setRefreshing(true);
        TiebaApi.getInstance()
                .userLikeForum(uid, page)
                .enqueue(new Callback<UserLikeForumBean>() {
                    @Override
                    public void onResponse(@NotNull Call<UserLikeForumBean> call, @NotNull Response<UserLikeForumBean> response) {
                        UserLikeForumBean data = response.body();
                        userLikeForumBean = data;
                        if (data.getForumList() != null) {
                            userLikeForumAdapter.setNewData(data.getForumList().getForumList());
                            if ("0".equals(data.getHasMore())) {
                                emptyTipView.setText(R.string.tip_empty);
                                userLikeForumAdapter.loadEnd();
                            }
                        } else {
                            emptyTipView.setText(R.string.tip_user_hide);
                            userLikeForumAdapter.loadEnd();
                        }
                        refreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(@NotNull Call<UserLikeForumBean> call, @NotNull Throwable t) {
                        Toast.makeText(getAttachContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                        refreshLayout.setRefreshing(false);
                    }
                });
    }

    public void load(boolean isReload) {
        if (!isReload) {
            page += 1;
        }
        TiebaApi.getInstance()
                .userLikeForum(uid, page)
                .enqueue(new Callback<UserLikeForumBean>() {
                    @Override
                    public void onResponse(@NotNull Call<UserLikeForumBean> call, @NotNull Response<UserLikeForumBean> response) {
                        UserLikeForumBean data = response.body();
                        userLikeForumBean = data;
                        if (data.getForumList() != null) {
                            userLikeForumAdapter.setLoadMoreData(data.getForumList().getForumList());
                            if ("0".equals(data.getHasMore())) {
                                emptyTipView.setText(R.string.tip_empty);
                                userLikeForumAdapter.loadEnd();
                            }
                        } else {
                            emptyTipView.setText(R.string.tip_user_hide);
                            userLikeForumAdapter.loadEnd();
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<UserLikeForumBean> call, @NotNull Throwable t) {
                        Toast.makeText(getAttachContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                        userLikeForumAdapter.loadFailed();
                    }
                });
    }

    @Override
    protected void onFragmentFirstVisible() {
        refresh();
    }

    @Override
    protected void onFragmentVisibleChange(boolean isVisible) {
        if (isVisible && userLikeForumBean == null) {
            refresh();
        }
    }
}