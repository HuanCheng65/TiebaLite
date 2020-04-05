package com.huanchengfly.tieba.post.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.huanchengfly.tieba.api.TiebaApi;
import com.huanchengfly.tieba.api.models.CommonResponse;
import com.huanchengfly.tieba.api.models.ThreadStoreBean;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.adapters.ThreadStoreAdapter;
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager;
import com.huanchengfly.tieba.post.components.dividers.RecycleViewDivider;
import com.huanchengfly.tieba.post.models.database.Account;
import com.huanchengfly.tieba.post.utils.AccountUtil;
import com.huanchengfly.tieba.post.utils.NavigationHelper;
import com.huanchengfly.tieba.post.utils.SharedPreferencesUtil;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.huanchengfly.tieba.post.utils.Util;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThreadStoreFragment extends BaseFragment {

    public NavigationHelper navigationHelper;
    private RecyclerView recyclerView;
    private ThreadStoreAdapter threadStoreAdapter;
    private SwipeRefreshLayout refreshLayout;
    private int page = 0;
    private boolean hasMore = true;
    private String tbs;

    public ThreadStoreFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navigationHelper = NavigationHelper.newInstance(getAttachContext());
        Account account = AccountUtil.getLoginInfo(getAttachContext());
        if (account != null) tbs = account.getTbs();
    }

    @Override
    int getLayoutId() {
        return R.layout.fragment_thread_store;
    }

    @NotNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView = super.onCreateView(inflater, container, savedInstanceState);
        refreshLayout = contentView.findViewById(R.id.thread_store_refresh_layout);
        refreshLayout.setOnRefreshListener(this::refresh);
        ThemeUtil.setThemeForSwipeRefreshLayout(refreshLayout);
        recyclerView = contentView.findViewById(R.id.thread_store_recycler_view);
        recyclerView.setLayoutManager(new MyLinearLayoutManager(getAttachContext()));
        recyclerView.addItemDecoration(new RecycleViewDivider(getAttachContext(), LinearLayoutManager.VERTICAL, R.drawable.drawable_divider_1dp));
        threadStoreAdapter = new ThreadStoreAdapter(getAttachContext());
        threadStoreAdapter.setLoadingView(R.layout.layout_footer_loading);
        threadStoreAdapter.setLoadEndView(R.layout.layout_footer_loadend);
        threadStoreAdapter.setLoadFailedView(R.layout.layout_footer_load_failed);
        threadStoreAdapter.setOnLoadMoreListener(this::loadMore);
        threadStoreAdapter.setOnItemClickListener((viewHolder, threadStoreInfo, i) -> {
            Map<String, String> map = new HashMap<>();
            map.put("tid", threadStoreInfo.getThreadId());
            map.put("pid", threadStoreInfo.getMarkPid());
            map.put("seeLz", SharedPreferencesUtil.get(getAttachContext(), SharedPreferencesUtil.SP_SETTINGS).getBoolean("collect_thread_see_lz", true) ? "1" : "0");
            map.put("from", "collect");
            map.put("max_pid", threadStoreInfo.getMaxPid());
            navigationHelper.navigationByData(NavigationHelper.ACTION_THREAD, map);
        });
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    viewHolder.itemView.setBackgroundColor(Util.getColorByAttr(getAttachContext(), R.attr.colorControlHighlight, R.color.transparent));
                }
            }

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int dragFlags = 0, swiped = ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT;
                return makeMovementFlags(dragFlags, swiped);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                ThreadStoreBean.ThreadStoreInfo threadStoreInfo = threadStoreAdapter.getData(position);
                threadStoreAdapter.remove(position);
                Util.createSnackbar(recyclerView, R.string.toast_deleted, Snackbar.LENGTH_LONG)
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                if (event != DISMISS_EVENT_ACTION) {
                                    TiebaApi.getInstance()
                                            .removeStore(threadStoreInfo.getThreadId(), tbs)
                                            .enqueue(new Callback<CommonResponse>() {
                                                @Override
                                                public void onResponse(@NotNull Call<CommonResponse> call, @NotNull Response<CommonResponse> response) {
                                                }

                                                @Override
                                                public void onFailure(@NotNull Call<CommonResponse> call, @NotNull Throwable t) {
                                                    Toast.makeText(getAttachContext(), getAttachContext().getString(R.string.toast_delete_error, t.getMessage()), Toast.LENGTH_SHORT).show();
                                                    threadStoreAdapter.insert(threadStoreInfo, position);
                                                }
                                            });
                                }
                            }
                        }).setAction(R.string.button_undo, mView -> threadStoreAdapter.insert(threadStoreInfo, position)).show();
            }
        });
        mItemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(threadStoreAdapter);
        return contentView;
    }

    @Override
    protected void onFragmentFirstVisible() {
        refresh();
    }

    private void refresh() {
        refreshLayout.setRefreshing(true);
        page = 0;
        TiebaApi.getInstance()
                .threadStore(page, 20)
                .enqueue(new Callback<ThreadStoreBean>() {
                    @Override
                    public void onResponse(@NotNull Call<ThreadStoreBean> call, @NotNull Response<ThreadStoreBean> response) {
                        ThreadStoreBean data = response.body();
                        refreshLayout.setRefreshing(false);
                        List<ThreadStoreBean.ThreadStoreInfo> storeInfoList = data.getStoreThread();
                        if (storeInfoList == null) {
                            return;
                        }
                        threadStoreAdapter.reset();
                        threadStoreAdapter.setNewData(storeInfoList);
                        hasMore = storeInfoList.size() > 0;
                        if (!hasMore) {
                            threadStoreAdapter.loadEnd();
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ThreadStoreBean> call, @NotNull Throwable t) {
                        refreshLayout.setRefreshing(false);
                    }
                });
    }

    private void loadMore(boolean isReload) {
        if (!isReload) {
            page += 1;
        }
        if (!hasMore) {
            return;
        }
        TiebaApi.getInstance()
                .threadStore(page, 20)
                .enqueue(new Callback<ThreadStoreBean>() {
                    @Override
                    public void onResponse(@NotNull Call<ThreadStoreBean> call, @NotNull Response<ThreadStoreBean> response) {
                        ThreadStoreBean data = response.body();
                        List<ThreadStoreBean.ThreadStoreInfo> storeInfoList = data.getStoreThread();
                        if (storeInfoList == null) {
                            return;
                        }
                        threadStoreAdapter.setLoadMoreData(storeInfoList);
                        hasMore = storeInfoList.size() > 0;
                        if (!hasMore) {
                            threadStoreAdapter.loadEnd();
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ThreadStoreBean> call, @NotNull Throwable t) {
                        threadStoreAdapter.loadFailed();
                        Toast.makeText(getAttachContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
