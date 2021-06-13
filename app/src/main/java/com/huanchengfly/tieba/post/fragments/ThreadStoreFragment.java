package com.huanchengfly.tieba.post.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.adapters.ThreadStoreAdapter;
import com.huanchengfly.tieba.post.api.TiebaApi;
import com.huanchengfly.tieba.post.api.models.CommonResponse;
import com.huanchengfly.tieba.post.api.models.ThreadStoreBean;
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager;
import com.huanchengfly.tieba.post.models.database.Account;
import com.huanchengfly.tieba.post.utils.AccountUtil;
import com.huanchengfly.tieba.post.utils.NavigationHelper;
import com.huanchengfly.tieba.post.utils.SharedPreferencesUtil;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.huanchengfly.tieba.post.utils.Util;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThreadStoreFragment extends BaseFragment {

    @BindView(R.id.thread_store_recycler_view)
    public RecyclerView recyclerView;

    @BindView(R.id.thread_store_refresh_layout)
    public SmartRefreshLayout refreshLayout;

    public NavigationHelper navigationHelper;
    private ThreadStoreAdapter threadStoreAdapter;
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
        threadStoreAdapter = new ThreadStoreAdapter(getAttachContext());
        threadStoreAdapter.setOnItemClickListener((viewHolder, threadStoreInfo, i) -> {
            Map<String, String> map = new HashMap<>();
            map.put("tid", threadStoreInfo.getThreadId());
            map.put("pid", threadStoreInfo.getMarkPid());
            map.put("seeLz", SharedPreferencesUtil.get(getAttachContext(), SharedPreferencesUtil.SP_SETTINGS).getBoolean("collect_thread_see_lz", true) ? "1" : "0");
            map.put("from", "collect");
            map.put("max_pid", threadStoreInfo.getMaxPid());
            navigationHelper.navigationByData(NavigationHelper.ACTION_THREAD, map);
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_thread_store;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ThemeUtil.setThemeForSmartRefreshLayout(refreshLayout);
        refreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                loadMore();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refresh();
            }
        });
        recyclerView.setLayoutManager(new MyLinearLayoutManager(getAttachContext()));
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
                ThreadStoreBean.ThreadStoreInfo threadStoreInfo = threadStoreAdapter.getItem(position);
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
    }

    @Override
    protected void onFragmentFirstVisible() {
        refreshLayout.autoRefresh();
    }

    private void refresh() {
        page = 0;
        TiebaApi.getInstance()
                .threadStore(page, 20)
                .enqueue(new Callback<ThreadStoreBean>() {
                    @Override
                    public void onResponse(@NotNull Call<ThreadStoreBean> call, @NotNull Response<ThreadStoreBean> response) {
                        ThreadStoreBean data = response.body();
                        refreshLayout.finishRefresh();
                        refreshLayout.setNoMoreData(!hasMore);
                        List<ThreadStoreBean.ThreadStoreInfo> storeInfoList = data.getStoreThread();
                        if (storeInfoList == null) {
                            return;
                        }
                        threadStoreAdapter.reset();
                        threadStoreAdapter.setData(storeInfoList);
                        hasMore = storeInfoList.size() > 0;
                    }

                    @Override
                    public void onFailure(@NotNull Call<ThreadStoreBean> call, @NotNull Throwable t) {
                        refreshLayout.finishRefresh(false);
                    }
                });
    }

    private void loadMore() {
        if (!hasMore) {
            return;
        }
        TiebaApi.getInstance()
                .threadStore(page + 1, 20)
                .enqueue(new Callback<ThreadStoreBean>() {
                    @Override
                    public void onResponse(@NotNull Call<ThreadStoreBean> call, @NotNull Response<ThreadStoreBean> response) {
                        page += 1;
                        ThreadStoreBean data = response.body();
                        List<ThreadStoreBean.ThreadStoreInfo> storeInfoList = data.getStoreThread();
                        if (storeInfoList == null) {
                            return;
                        }
                        threadStoreAdapter.insert(storeInfoList);
                        refreshLayout.finishLoadMore();
                        refreshLayout.setNoMoreData(!hasMore);
                        hasMore = storeInfoList.size() > 0;
                    }

                    @Override
                    public void onFailure(@NotNull Call<ThreadStoreBean> call, @NotNull Throwable t) {
                        refreshLayout.finishLoadMore(false);
                        Toast.makeText(getAttachContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
