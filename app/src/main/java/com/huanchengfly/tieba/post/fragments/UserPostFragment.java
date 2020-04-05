package com.huanchengfly.tieba.post.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.huanchengfly.tieba.api.TiebaApi;
import com.huanchengfly.tieba.api.models.UserPostBean;
import com.huanchengfly.tieba.post.FloorActivity;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ThreadActivity;
import com.huanchengfly.tieba.post.adapters.UserPostAdapter;
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager;
import com.huanchengfly.tieba.post.components.dividers.CommonDivider;
import com.huanchengfly.tieba.post.utils.NavigationHelper;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.interfaces.OnMultiItemClickListeners;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserPostFragment extends BaseFragment {
    public static final String PARAM_UID = "uid";
    public static final String PARAM_IS_THREAD = "is_thread";
    public static final String TAG = UserPostFragment.class.getSimpleName();

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private UserPostAdapter userPostAdapter;

    private View emptyView;
    private TextView emptyTipView;

    private UserPostBean userPostBean;

    private String uid;
    private boolean isThread;
    private int page;

    public UserPostFragment() {
    }

    public static UserPostFragment newInstance(String uid) {
        return newInstance(uid, true);
    }

    public static UserPostFragment newInstance(String uid, boolean isThread) {
        UserPostFragment fragment = new UserPostFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_UID, uid);
        args.putBoolean(PARAM_IS_THREAD, isThread);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            uid = args.getString(PARAM_UID, null);
            isThread = args.getBoolean(PARAM_IS_THREAD, true);
        }
    }

    @Override
    int getLayoutId() {
        return R.layout.fragment_user_post;
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
        recyclerView.addItemDecoration(new CommonDivider(getAttachContext(), MyLinearLayoutManager.VERTICAL, R.drawable.drawable_divider_8dp));
        userPostAdapter = new UserPostAdapter(getAttachContext());
        emptyView = View.inflate(getAttachContext(), R.layout.layout_empty_view, null);
        emptyTipView = emptyView.findViewById(R.id.empty_tip);
        userPostAdapter.setOnMultiItemClickListener((viewHolder, postBean, position, viewType) -> {
            switch (viewType) {
                case UserPostAdapter.TYPE_THREAD:
                    getAttachContext().startActivity(new Intent(getAttachContext(), ThreadActivity.class)
                            .putExtra("tid", postBean.getThreadId()));
                    break;
                case UserPostAdapter.TYPE_REPLY:
                    if ("0".equals(postBean.getPostType())) {
                        getAttachContext().startActivity(new Intent(getAttachContext(), FloorActivity.class)
                                .putExtra("tid", postBean.getThreadId())
                                .putExtra("pid", postBean.getPostId()));
                    } else {
                        getAttachContext().startActivity(new Intent(getAttachContext(), FloorActivity.class)
                                .putExtra("tid", postBean.getThreadId())
                                .putExtra("spid", postBean.getPostId()));
                    }
                    break;
            }
        });
        userPostAdapter.setLoadingView(R.layout.layout_footer_loading);
        userPostAdapter.setEmptyView(emptyView);
        userPostAdapter.setLoadEndView(R.layout.layout_footer_loadend);
        userPostAdapter.setLoadFailedView(R.layout.layout_footer_load_failed);
        userPostAdapter.setOnLoadMoreListener(this::load);
        recyclerView.setLayoutManager(new MyLinearLayoutManager(getAttachContext()));
        recyclerView.setAdapter(userPostAdapter);
        return contentView;
    }

    public void load(boolean isReload) {
        if (!isReload) {
            page += 1;
        }
        TiebaApi.getInstance()
                .userPost(uid, page, isThread)
                .enqueue(new Callback<UserPostBean>() {
                    @Override
                    public void onResponse(@NotNull Call<UserPostBean> call, @NotNull Response<UserPostBean> response) {
                        UserPostBean data = response.body();
                        userPostBean = data;
                        if ("0".equals(data.getHidePost())) {
                            userPostAdapter.setLoadMoreData(data.getPostList());
                            if (data.getPostList().size() <= 0) {
                                emptyTipView.setText(R.string.tip_empty);
                                userPostAdapter.loadEnd();
                            }
                        } else {
                            emptyTipView.setText(R.string.tip_user_hide);
                            userPostAdapter.loadEnd();
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<UserPostBean> call, @NotNull Throwable t) {
                        Toast.makeText(getAttachContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                        userPostAdapter.loadFailed();
                    }
                });
    }

    public void refresh() {
        page = 1;
        userPostAdapter.reset();
        refreshLayout.setRefreshing(true);
        TiebaApi.getInstance()
                .userPost(uid, page, isThread)
                .enqueue(new Callback<UserPostBean>() {
                    @Override
                    public void onResponse(@NotNull Call<UserPostBean> call, @NotNull Response<UserPostBean> response) {
                        UserPostBean data = response.body();
                        userPostBean = data;
                        if ("0".equals(data.getHidePost())) {
                            userPostAdapter.setNewData(data.getPostList());
                            if (data.getPostList().size() <= 0) {
                                emptyTipView.setText(R.string.tip_empty);
                            }
                        } else {
                            emptyTipView.setText(R.string.tip_user_hide);
                            userPostAdapter.loadEnd();
                        }
                        refreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(@NotNull Call<UserPostBean> call, @NotNull Throwable t) {
                        Toast.makeText(getAttachContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                        refreshLayout.setRefreshing(false);
                    }
                });
    }

    @Override
    protected void onFragmentFirstVisible() {
        refresh();
    }

    @Override
    protected void onFragmentVisibleChange(boolean isVisible) {
        if (isVisible && userPostBean == null) {
            refresh();
        }
    }
}