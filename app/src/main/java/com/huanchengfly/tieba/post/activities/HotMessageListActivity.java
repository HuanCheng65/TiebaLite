package com.huanchengfly.tieba.post.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.adapters.HotMessageListAdapter;
import com.huanchengfly.tieba.post.api.TiebaApi;
import com.huanchengfly.tieba.post.api.models.web.HotMessageListBean;
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager;
import com.huanchengfly.tieba.post.components.dividers.CommonDivider;
import com.huanchengfly.tieba.post.utils.DisplayUtil;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HotMessageListActivity extends BaseActivity {
    private Toolbar toolbar;
    private AppBarLayout appBarLayout;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;

    private HotMessageListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_message_list);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_hot_message);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        LinearLayoutManager layoutManager = new MyLinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new CommonDivider(this, LinearLayoutManager.VERTICAL, R.drawable.drawable_divider_1dp, DisplayUtil.dp2px(this, 48)));
        adapter = new HotMessageListAdapter(this);
        adapter.setLoadFailedView(R.layout.layout_footer_load_failed);
        adapter.setOnLoadMoreListener(isReload -> refresh());
        recyclerView.setAdapter(adapter);
        refreshLayout.setOnRefreshListener(this::refresh);
        refresh();
    }

    private void refresh() {
        refreshLayout.setRefreshing(true);
        TiebaApi.getInstance().hotMessageList().enqueue(new Callback<HotMessageListBean>() {
            @Override
            public void onResponse(@NotNull Call<HotMessageListBean> call, @NotNull Response<HotMessageListBean> response) {
                adapter.setNewData(response.body().getData().getList().getRet());
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(@NotNull Call<HotMessageListBean> call, @NotNull Throwable t) {
                adapter.loadFailed();
                refreshLayout.setRefreshing(false);
                Toast.makeText(HotMessageListActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
