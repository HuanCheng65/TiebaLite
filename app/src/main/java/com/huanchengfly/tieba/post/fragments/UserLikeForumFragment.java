package com.huanchengfly.tieba.post.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.activities.ForumActivity;
import com.huanchengfly.tieba.post.adapters.UserLikeForumAdapter;
import com.huanchengfly.tieba.post.adapters.base.OnItemChildClickListener;
import com.huanchengfly.tieba.post.api.TiebaApi;
import com.huanchengfly.tieba.post.api.models.UserLikeForumBean;
import com.huanchengfly.tieba.post.components.dividers.StaggeredDividerItemDecoration;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserLikeForumFragment extends BaseFragment {
    public static final String PARAM_UID = "uid";
    @BindView(R.id.refresh)
    public SmartRefreshLayout refreshLayout;
    @BindView(R.id.user_post_reclcyer_view)
    public RecyclerView recyclerView;
    private String uid;
    private int page;
    private UserLikeForumAdapter userLikeForumAdapter;

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

    @Override
    public void onViewCreated(@NonNull View contentView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(contentView, savedInstanceState);
        refreshLayout.setNestedScrollingEnabled(true);
        ThemeUtil.setThemeForSmartRefreshLayout(refreshLayout);
        refreshLayout.setOnRefreshListener(refreshLayout -> refresh());
        refreshLayout.setOnLoadMoreListener(refreshLayout -> load());
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.addItemDecoration(new StaggeredDividerItemDecoration(getAttachContext(), 16));
        userLikeForumAdapter = new UserLikeForumAdapter(getAttachContext());
        userLikeForumAdapter.setOnItemChildClickListener(
                R.id.forum_item_card,
                (OnItemChildClickListener<UserLikeForumBean.ForumBean>)
                        (viewHolder, forumBean, position) ->
                                ForumActivity.launch(getAttachContext(), forumBean.getName())
        );
        recyclerView.setAdapter(userLikeForumAdapter);
    }

    public void refresh() {
        page = 1;
        userLikeForumAdapter.reset();
        TiebaApi.getInstance()
                .userLikeForum(uid, page)
                .enqueue(new Callback<UserLikeForumBean>() {
                    @Override
                    public void onResponse(@NotNull Call<UserLikeForumBean> call, @NotNull Response<UserLikeForumBean> response) {
                        UserLikeForumBean data = response.body();
                        userLikeForumBean = data;
                        if (data.getForumList() != null) {
                            userLikeForumAdapter.setData(data.getForumList().getForumList());
                            if ("0".equals(data.getHasMore())) {
                                //emptyTipView.setText(R.string.tip_empty);
                                refreshLayout.finishRefreshWithNoMoreData();
                            } else {
                                refreshLayout.finishRefresh();
                            }
                        } else {
                            //emptyTipView.setText(R.string.tip_user_hide);
                            refreshLayout.finishRefreshWithNoMoreData();
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<UserLikeForumBean> call, @NotNull Throwable t) {
                        Toast.makeText(getAttachContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                        refreshLayout.finishRefresh(false);
                    }
                });
    }

    public void load() {
        TiebaApi.getInstance()
                .userLikeForum(uid, page + 1)
                .enqueue(new Callback<UserLikeForumBean>() {
                    @Override
                    public void onResponse(@NotNull Call<UserLikeForumBean> call, @NotNull Response<UserLikeForumBean> response) {
                        page += 1;
                        UserLikeForumBean data = response.body();
                        userLikeForumBean = data;
                        if (data.getForumList() != null) {
                            userLikeForumAdapter.insert(data.getForumList().getForumList());
                            if ("0".equals(data.getHasMore())) {
                                //emptyTipView.setText(R.string.tip_empty);
                                refreshLayout.finishLoadMoreWithNoMoreData();
                            } else {
                                refreshLayout.finishLoadMore();
                            }
                        } else {
                            //emptyTipView.setText(R.string.tip_user_hide);
                            refreshLayout.finishLoadMoreWithNoMoreData();
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<UserLikeForumBean> call, @NotNull Throwable t) {
                        Toast.makeText(getAttachContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                        refreshLayout.finishLoadMore(false);
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