package com.huanchengfly.tieba.post.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.huanchengfly.tieba.api.TiebaApi;
import com.huanchengfly.tieba.api.models.MessageListBean;
import com.huanchengfly.tieba.api.retrofit.exception.TiebaException;
import com.huanchengfly.tieba.post.MainActivity;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.adapters.MessageListAdapter;
import com.huanchengfly.tieba.post.adapters.TabViewPagerAdapter;
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager;
import com.huanchengfly.tieba.post.components.dividers.CommonDivider;
import com.huanchengfly.tieba.post.interfaces.Refreshable;
import com.huanchengfly.tieba.post.utils.DisplayUtil;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.huanchengfly.tieba.post.utils.Util;
import com.huanchengfly.tieba.widgets.theme.TintImageView;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageFragment extends BaseFragment implements View.OnClickListener, Refreshable, TabLayout.OnTabSelectedListener {
    public static final int TYPE_REPLY_ME = 0;
    public static final int TYPE_AT_ME = 1;

    public static final String TAG = MessageFragment.class.getSimpleName();

    private static final String PARAM_TYPE = "type";
    @BindView(R.id.fragment_message_tab)
    TabLayout tabLayout;
    private MessageListHelper replyMe;
    private MessageListHelper atMe;
    private int type;

    public MessageFragment() {
    }

    public static MessageFragment newInstance(int type) {
        MessageFragment fragment = new MessageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PARAM_TYPE, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onFragmentVisibleChange(boolean isVisible) {
        if (isVisible) {
            refreshIfNeed();
        }
    }

    @Override
    public void onFragmentFirstVisible() {
        refreshIfNeed();
    }

    public int getType() {
        return type;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            type = args.getInt(PARAM_TYPE, TYPE_REPLY_ME);
        }
    }

    @Override
    int getLayoutId() {
        return R.layout.fragment_message;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewPager viewPager = view.findViewById(R.id.fragment_message_vp);
        TintImageView searchBtn = view.findViewById(R.id.search_btn);
        if (getAttachContext() instanceof MainActivity) {
            searchBtn.setVisibility(View.VISIBLE);
        } else {
            searchBtn.setVisibility(View.INVISIBLE);
        }
        searchBtn.setOnClickListener(this);
        TabViewPagerAdapter viewPagerAdapter = new TabViewPagerAdapter();
        replyMe = new MessageListHelper(getAttachContext(), TYPE_REPLY_ME);
        atMe = new MessageListHelper(getAttachContext(), TYPE_AT_ME);
        viewPagerAdapter.addView(replyMe.getContentView(), getAttachContext().getString(R.string.title_reply_me));
        viewPagerAdapter.addView(atMe.getContentView(), getAttachContext().getString(R.string.title_at_me));
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(this);
        viewPager.setCurrentItem(type, false);
    }

    @Override
    public boolean hasOwnAppbar() {
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.search_btn) {
            if (getAttachContext() instanceof MainActivity) {
                ((MainActivity) getAttachContext()).openSearch();
            }
        }
    }

    @Override
    public void onRefresh() {
        switch (tabLayout.getSelectedTabPosition()) {
            case 0:
                if (isFragmentVisible()) {
                    replyMe.refresh();
                } else {
                    replyMe.dataBean = null;
                }
                break;
            case 1:
                if (isFragmentVisible()) {
                    atMe.refresh();
                } else {
                    atMe.dataBean = null;
                }
                break;
        }
    }

    private void refreshIfNeed() {
        switch (tabLayout.getSelectedTabPosition()) {
            case 0:
                if (replyMe.needLoad()) {
                    replyMe.refresh();
                }
                break;
            case 1:
                if (atMe.needLoad()) {
                    atMe.refresh();
                }
                break;
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        refreshIfNeed();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        refreshIfNeed();
    }

    class MessageListHelper {
        private View contentView;
        private SwipeRefreshLayout swipeRefreshLayout;
        private RecyclerView recyclerView;
        private MessageListAdapter adapter;
        private int type;
        private int page;
        private MessageListBean dataBean;

        MessageListHelper(Context context, int type) {
            this.type = type;
            if (this.type != TYPE_REPLY_ME && this.type != TYPE_AT_ME) {
                throw new IllegalArgumentException("参数不正确");
            }
            contentView = Util.inflate(context, R.layout.fragment_message_list);
            if (contentView == null) {
                throw new NullPointerException("引入的布局为空");
            }
            recyclerView = contentView.findViewById(R.id.fragment_message_recycler_view);
            swipeRefreshLayout = contentView.findViewById(R.id.fragment_message_refresh_layout);
            ThemeUtil.setThemeForSwipeRefreshLayout(swipeRefreshLayout);
            swipeRefreshLayout.setOnRefreshListener(this::refresh);
            recyclerView.setLayoutManager(new MyLinearLayoutManager(context));
            recyclerView.addItemDecoration(new CommonDivider(getAttachContext(), MyLinearLayoutManager.VERTICAL, R.drawable.drawable_divider_1dp, DisplayUtil.dp2px(context, 58), DisplayUtil.dp2px(context, 24)));
            adapter = new MessageListAdapter(context, type);
            adapter.setLoadingView(R.layout.layout_footer_loading);
            adapter.setLoadEndView(R.layout.layout_footer_loadend);
            adapter.setLoadFailedView(R.layout.layout_footer_load_failed);
            adapter.setOnLoadMoreListener((boolean isReload) -> {
                if (isReload) load(false);
                else loadMore();
            });
            recyclerView.setAdapter(adapter);
        }

        boolean needLoad() {
            return dataBean == null;
        }

        public int getType() {
            return type;
        }

        public View getContentView() {
            return contentView;
        }

        public void refresh() {
            swipeRefreshLayout.setRefreshing(true);
            load(true);
        }

        private void load(boolean reload) {
            if (reload) {
                page = 1;
            }
            Callback<MessageListBean> messageListBeanCallback = new Callback<MessageListBean>() {
                @Override
                public void onResponse(@NotNull Call<MessageListBean> call, @NotNull Response<MessageListBean> response) {
                    dataBean = response.body();
                    if (reload) {
                        adapter.reset();
                        adapter.setData(dataBean);
                    } else adapter.addData(dataBean);
                    swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onFailure(@NotNull Call<MessageListBean> call, @NotNull Throwable t) {
                    if (reload) {
                        if (!(t instanceof TiebaException)) {
                            Util.showNetworkErrorSnackbar(recyclerView, () -> refresh());
                            return;
                        }
                    }
                    adapter.loadFailed();
                    swipeRefreshLayout.setRefreshing(false);
                }
            };
            switch (getType()) {
                case TYPE_REPLY_ME:
                    TiebaApi.getInstance().replyMe(page).enqueue(messageListBeanCallback);
                    break;
                case TYPE_AT_ME:
                    TiebaApi.getInstance().atMe(page).enqueue(messageListBeanCallback);
                    break;
            }
        }

        private void loadMore() {
            if (dataBean.getPage().getHasMore().equals("1")) {
                page += 1;
                load(false);
            } else {
                adapter.loadEnd();
            }
        }
    }
}