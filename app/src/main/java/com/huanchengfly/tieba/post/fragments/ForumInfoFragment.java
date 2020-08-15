package com.huanchengfly.tieba.post.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.api.ForumSortType;
import com.huanchengfly.tieba.post.api.TiebaApi;
import com.huanchengfly.tieba.post.api.caster.ForumBeanCaster;
import com.huanchengfly.tieba.post.api.models.ForumPageBean;
import com.huanchengfly.tieba.post.api.models.web.ForumBean;
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaException;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.adapters.ZyqFriendAdapter;
import com.huanchengfly.tieba.post.components.spans.MyImageSpan;
import com.huanchengfly.tieba.post.components.spans.MyURLSpan;
import com.huanchengfly.tieba.post.components.spans.MyUserSpan;
import com.huanchengfly.tieba.post.interfaces.Refreshable;
import com.huanchengfly.tieba.post.interfaces.ScrollTopable;
import com.huanchengfly.tieba.post.utils.AnimUtil;
import com.huanchengfly.tieba.post.utils.DisplayUtil;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.huanchengfly.tieba.post.utils.Util;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.huanchengfly.tieba.post.fragments.ForumFragment.PARAM_FORUM_NAME;

public class ForumInfoFragment extends BaseFragment implements Refreshable, ScrollTopable {
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.slogan)
    TextView slogan;
    @BindView(R.id.hot)
    TextView hot;
    @BindView(R.id.managers)
    TextView managers;
    @BindView(R.id.zyqtitle)
    TextView zyqTitle;
    @BindView(R.id.zyqdefine)
    TextView zyqDefine;
    @BindView(R.id.scroll_view)
    NestedScrollView mScrollView;
    @BindView(R.id.friend_links)
    View mFriendLinksView;
    @BindView(R.id.friend_forums)
    View mFriendForumsView;
    @BindView(R.id.managers_view)
    View mManagersView;
    @BindView(R.id.friend_forums_view)
    RecyclerView friendForumsRecyclerView;
    private String forumName;
    private ForumPageBean mDataBean;
    private View content;
    private SwipeRefreshLayout mRefreshLayout;

    public ForumInfoFragment() {
    }

    public static ForumInfoFragment newInstance(String forumName) {
        Bundle args = new Bundle();
        args.putString(PARAM_FORUM_NAME, forumName);
        ForumInfoFragment fragment = new ForumInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (savedInstanceState == null && bundle != null) {
            forumName = bundle.getString(PARAM_FORUM_NAME);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(PARAM_FORUM_NAME, forumName);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            forumName = savedInstanceState.getString(PARAM_FORUM_NAME);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    int getLayoutId() {
        return R.layout.fragment_forum_info;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRefreshLayout = (SwipeRefreshLayout) view;
        ThemeUtil.setThemeForSwipeRefreshLayout(mRefreshLayout);
        mRefreshLayout.setOnRefreshListener(this::refresh);
        content = view.findViewById(R.id.content);
        content.setVisibility(View.GONE);
        managers.setMovementMethod(LinkMovementMethod.getInstance());
        friendForumsRecyclerView.setLayoutManager(new LinearLayoutManager(getAttachContext()));
        zyqDefine.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected void onFragmentFirstVisible() {
        if (mDataBean == null) {
            refresh();
        }
    }

    @Override
    protected void onFragmentVisibleChange(boolean isVisible) {
        if (isVisible && mDataBean == null) {
            refresh();
        }
    }

    private CharSequence getLinkContent(CharSequence name, String link) {
        String linkIconText = "[链接]";
        String s = " ";
        int start = 0;
        int end = start + s.length() + linkIconText.length() + name.length();
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        Bitmap bitmap = Util.getBitmapFromVectorDrawable(getAttachContext(), R.drawable.ic_link);
        int size = DisplayUtil.sp2px(getAttachContext(), 14);
        int color = ThemeUtils.getColorByAttr(getAttachContext(), R.attr.colorAccent);
        bitmap = Bitmap.createScaledBitmap(bitmap, size, size, true);
        bitmap = Util.tintBitmap(bitmap, color);
        spannableStringBuilder.append(linkIconText, new MyImageSpan(getAttachContext(), bitmap), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append(s);
        spannableStringBuilder.append(name);
        spannableStringBuilder.setSpan(new MyURLSpan(getAttachContext(), link), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableStringBuilder;
    }

    private CharSequence getUserContent(CharSequence username, String uid) {
        String linkIconText = "[用户]";
        String s = " ";
        int start = 0;
        int end = start + s.length() + linkIconText.length() + username.length();
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        Bitmap bitmap = Util.getBitmapFromVectorDrawable(getAttachContext(), R.drawable.ic_round_account_circle);
        int size = DisplayUtil.sp2px(getAttachContext(), 14);
        int color = ThemeUtils.getColorByAttr(getAttachContext(), R.attr.colorAccent);
        bitmap = Bitmap.createScaledBitmap(bitmap, size, size, true);
        bitmap = Util.tintBitmap(bitmap, color);
        spannableStringBuilder.append(linkIconText, new MyImageSpan(getAttachContext(), bitmap), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append(s);
        spannableStringBuilder.append(username);
        spannableStringBuilder.setSpan(new MyUserSpan(getAttachContext(), uid), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableStringBuilder;
    }

    private void refresh() {
        mRefreshLayout.setRefreshing(true);
        TiebaApi.getInstance()
                .webForumPage(forumName, 1, null, ForumSortType.REPLY_TIME, 30)
                .enqueue(new Callback<ForumBean>() {
                    @Override
                    public void onResponse(Call<ForumBean> call, Response<ForumBean> response) {
                        ForumPageBean data = new ForumBeanCaster().cast(response.body());
                        if (getAttachContext() instanceof ForumFragment.OnRefreshedListener) {
                            ((ForumFragment.OnRefreshedListener) getAttachContext()).onSuccess(data);
                        }
                        mRefreshLayout.setRefreshing(false);
                        AnimUtil.alphaIn(content).start();
                        mDataBean = data;
                        title.setText(getAttachContext().getString(R.string.title_forum, data.getForum().getName()));
                        slogan.setText(data.getForum().getSlogan());
                        hot.setText(getAttachContext().getString(R.string.forum_hot, data.getForum().getMemberNum(), data.getForum().getPostNum()));
                        if (data.getForum().getZyqDefine() != null && data.getForum().getZyqDefine().size() > 0) {
                            mFriendLinksView.setVisibility(View.VISIBLE);
                            zyqTitle.setText(data.getForum().getZyqTitle());
                            SpannableStringBuilder friendLinkBuilder = new SpannableStringBuilder();
                            for (ForumPageBean.ZyqDefineBean zyqDefineBean : data.getForum().getZyqDefine()) {
                                friendLinkBuilder.append(getLinkContent(zyqDefineBean.getName(), zyqDefineBean.getLink()));
                                if (data.getForum().getZyqDefine().indexOf(zyqDefineBean) < data.getForum().getZyqDefine().size() - 1)
                                    friendLinkBuilder.append("\n");
                            }
                            zyqDefine.setText(friendLinkBuilder);
                        } else {
                            mFriendLinksView.setVisibility(View.GONE);
                        }
                        if (data.getForum().getZyqFriend() != null && data.getForum().getZyqFriend().size() > 0) {
                            mFriendForumsView.setVisibility(View.VISIBLE);
                            friendForumsRecyclerView.setAdapter(new ZyqFriendAdapter(getAttachContext(), data.getForum().getZyqFriend()));
                        } else {
                            mFriendForumsView.setVisibility(View.GONE);
                        }
                        if (data.getForum().getManagers() != null && data.getForum().getManagers().size() > 0) {
                            mManagersView.setVisibility(View.VISIBLE);
                            SpannableStringBuilder managersBuilder = new SpannableStringBuilder();
                            for (ForumPageBean.ManagerBean managerBean : data.getForum().getManagers()) {
                                managersBuilder.append(getUserContent(managerBean.getName(), managerBean.getId()));
                                if (data.getForum().getManagers().indexOf(managerBean) < data.getForum().getManagers().size() - 1)
                                    managersBuilder.append("\n");
                            }
                            managers.setText(managersBuilder);
                        } else {
                            mManagersView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ForumBean> call, @NotNull Throwable t) {
                        int code = t instanceof TiebaException ? ((TiebaException) t).getCode() : -1;
                        String error = t.getMessage();
                        if (getAttachContext() instanceof ForumFragment.OnRefreshedListener) {
                            ((ForumFragment.OnRefreshedListener) getAttachContext()).onFailure(code, error);
                        }
                        mRefreshLayout.setRefreshing(false);
                        if (code == 0) {
                            Util.showNetworkErrorSnackbar(content, () -> refresh());
                            return;
                        }
                        Toast.makeText(getAttachContext(), getString(R.string.toast_error, code, error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    @Override
    public void scrollToTop() {
        mScrollView.scrollTo(0, 0);
    }
}
