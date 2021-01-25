package com.huanchengfly.tieba.post.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.api.models.ForumRecommend;
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager;
import com.huanchengfly.tieba.post.components.MyViewHolder;
import com.huanchengfly.tieba.post.components.dividers.HorizontalSpacesDecoration;
import com.huanchengfly.tieba.post.interfaces.OnItemClickListener;
import com.huanchengfly.tieba.post.interfaces.OnItemLongClickListener;
import com.huanchengfly.tieba.post.models.database.TopForum;
import com.huanchengfly.tieba.post.utils.DisplayUtil;
import com.huanchengfly.tieba.post.utils.ImageUtil;
import com.huanchengfly.tieba.post.utils.SharedPreferencesUtil;
import com.huanchengfly.tieba.post.utils.ThemeUtil;

import org.litepal.LitePal;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class LikeForumListAdapter extends RecyclerView.Adapter<MyViewHolder> {
    public static final int TYPE_ERROR = -1;
    public static final int TYPE_TOP_FORUM_TITLE = 1;
    public static final int TYPE_TOP_FORUM = 2;
    public static final int TYPE_NORMAL_FORUM_TITLE = 3;
    public static final int TYPE_NORMAL_FORUM = 4;
    private boolean showTop;
    private boolean single;
    private WeakReference<Context> contextWeakReference;
    private List<ForumRecommend.LikeForum> topForums;
    private List<ForumRecommend.LikeForum> mLikeForums;
    private TopForumsAdapter topForumsAdapter;
    private OnItemClickListener<ForumRecommend.LikeForum> onItemClickListener;
    private OnItemLongClickListener<ForumRecommend.LikeForum> onItemLongClickListener;

    public LikeForumListAdapter(Context context) {
        contextWeakReference = new WeakReference<>(context);
        single = SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_SETTINGS).getBoolean("listSingle", false);
        mLikeForums = new ArrayList<>();
        topForums = new ArrayList<>();
        topForumsAdapter = new TopForumsAdapter(context);
        onItemClickListener = null;
        onItemLongClickListener = null;
        showTop = SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_APP_DATA).getBoolean("show_top_forum", true);
    }

    public Context getContext() {
        return contextWeakReference.get();
    }

    public void remove(int position) {
        if (position >= 0 && position < mLikeForums.size()) {
            mLikeForums.remove(position);
            notifyDataSetChanged();
        }
    }

    public OnItemClickListener<ForumRecommend.LikeForum> getOnItemClickListener() {
        return onItemClickListener;
    }

    public LikeForumListAdapter setOnItemClickListener(OnItemClickListener<ForumRecommend.LikeForum> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        return this;
    }

    public OnItemLongClickListener<ForumRecommend.LikeForum> getOnItemLongClickListener() {
        return onItemLongClickListener;
    }

    public LikeForumListAdapter setOnItemLongClickListener(OnItemLongClickListener<ForumRecommend.LikeForum> onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
        return this;
    }

    public boolean isHeader(int position) {
        return isForumItemTitle(position) || isTopForumItemTitle(position) || isTopForumItem(position);
    }

    @LayoutRes
    public int getItemLayoutId(int type) {
        switch (type) {
            case TYPE_TOP_FORUM_TITLE:
            case TYPE_NORMAL_FORUM_TITLE:
                return R.layout.layout_forum_list_header;
            case TYPE_TOP_FORUM:
                return R.layout.item_top_forums;
            case TYPE_NORMAL_FORUM:
                return R.layout.item_forum_list;
            default:
                return R.layout.item_empty;
        }
    }

    public boolean isShowTop() {
        return showTop;
    }

    public void setShowTop(boolean showTop) {
        this.showTop = showTop;
        SharedPreferencesUtil.get(getContext(), SharedPreferencesUtil.SP_APP_DATA).edit().putBoolean("show_top_forum", showTop).apply();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(getContext()).inflate(getItemLayoutId(viewType), parent, false));
    }

    public ForumRecommend.LikeForum getItem(int position) {
        return mLikeForums.get(position);
    }

    public int getSpanCount() {
        if (SharedPreferencesUtil.get(getContext(), SharedPreferencesUtil.SP_SETTINGS).getBoolean("listSingle", false)) {
            return 1;
        } else {
            return 2;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case TYPE_TOP_FORUM_TITLE:
                holder.setText(R.id.forum_title_text, R.string.title_top_forum);
                ImageView imageView = holder.getView(R.id.forum_title_icon);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(isShowTop() ? R.drawable.ic_keyboard_arrow_down : R.drawable.ic_round_keyboard_arrow_right);
                holder.setItemOnClickListener(v -> setShowTop(!isShowTop()));
                break;
            case TYPE_NORMAL_FORUM_TITLE:
                holder.setText(R.id.forum_title_text, R.string.forum_list_title);
                holder.setVisibility(R.id.forum_title_icon, View.GONE);
                break;
            case TYPE_TOP_FORUM:
                topForumsAdapter.setOnItemClickListener(getOnItemClickListener());
                topForumsAdapter.setOnItemLongClickListener(getOnItemLongClickListener());
                RecyclerView topForumsView = holder.getView(R.id.forum_top_forums);
                topForumsView.setLayoutManager(new MyLinearLayoutManager(getContext(), MyLinearLayoutManager.HORIZONTAL, false));
                topForumsView.setAdapter(topForumsAdapter);
                topForumsView.setHasFixedSize(false);
                for (int i = 0; i < topForumsView.getItemDecorationCount(); i++) {
                    topForumsView.removeItemDecorationAt(i);
                }
                int dp16 = DisplayUtil.dp2px(getContext(), 16);
                int dp8 = DisplayUtil.dp2px(getContext(), 8);
                topForumsView.addItemDecoration(new HorizontalSpacesDecoration(dp8, dp8, dp16, dp16));
                break;
            case TYPE_NORMAL_FORUM:
                int realPosition = position - getTopForumItemCount() - 1;
                int padding = DisplayUtil.dp2px(getContext(), 18);
                int dp12 = DisplayUtil.dp2px(getContext(), 12);
                if (getSpanCount() == 1) {
                    holder.itemView.setPaddingRelative(padding, dp12, padding, dp12);
                } else if (realPosition % getSpanCount() == 0) {
                    holder.itemView.setPaddingRelative(padding, dp12, (int) (padding / 1.5), dp12);
                } else {
                    holder.itemView.setPaddingRelative((int) (padding / 1.5), dp12, padding, dp12);
                }
                if (realPosition >= 0) {
                    ForumRecommend.LikeForum likeForum = mLikeForums.get(realPosition);
                    holder.setItemOnClickListener(v -> {
                        if (getOnItemClickListener() != null) {
                            getOnItemClickListener().onClick(holder.itemView, likeForum, position, viewType);
                        }
                    });
                    holder.setItemOnLongClickListener(v -> {
                        if (getOnItemLongClickListener() != null) {
                            return getOnItemLongClickListener().onLongClick(holder.itemView, likeForum, position, viewType);
                        }
                        return false;
                    });
                    ImageView avatarView = holder.getView(R.id.forum_list_item_avatar);
                    if (isSingle()) {
                        avatarView.setVisibility(View.VISIBLE);
                        ImageUtil.load(avatarView, ImageUtil.LOAD_TYPE_AVATAR, likeForum.getAvatar());
                    } else {
                        avatarView.setVisibility(View.GONE);
                        Glide.with(getContext())
                                .clear(avatarView);
                    }
                    ThemeUtil.setChipThemeByLevel(likeForum.getLevelId(),
                            holder.getView(R.id.forum_list_item_status),
                            holder.getView(R.id.forum_list_item_level),
                            holder.getView(R.id.forum_list_item_sign_status));
                    holder.setText(R.id.forum_list_item_name, likeForum.getForumName());
                    holder.setText(R.id.forum_list_item_level, likeForum.getLevelId());
                    holder.setVisibility(R.id.forum_list_item_sign_status, "1".equals(likeForum.isSign()) ? View.VISIBLE : View.GONE);
                    holder.getView(R.id.forum_list_item_status).setMinimumWidth(DisplayUtil.dp2px(getContext(), "1".equals(likeForum.isSign()) ? 50 : 32));
                }
                break;
        }
    }

    private boolean isTopForumItemTitle(int position) {
        return position < getTopForumItemCount() && position == 0;
    }

    private boolean isTopForumItem(int position) {
        return position < getTopForumItemCount() && position > 0;
    }

    private boolean isForumItemTitle(int position) {
        return position == getTopForumItemCount();
    }

    private boolean isForumItem(int position) {
        return position > getTopForumItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (isTopForumItemTitle(position)) {
            return TYPE_TOP_FORUM_TITLE;
        } else if (isTopForumItem(position)) {
            return TYPE_TOP_FORUM;
        } else if (isForumItemTitle(position)) {
            return TYPE_NORMAL_FORUM_TITLE;
        } else if (isForumItem(position)) {
            return TYPE_NORMAL_FORUM;
        }
        return TYPE_ERROR;
    }

    @Override
    public int getItemCount() {
        return getTopForumItemCount() + getForumItemCount();
    }

    public int getTopForumItemCount() {
        if (topForums.size() <= 0) {
            return 0;
        }
        if (!isShowTop()) {
            return 1;
        }
        return 2;
    }

    public int getForumItemCount() {
        if (mLikeForums.size() <= 0) {
            return 0;
        }
        return mLikeForums.size() + 1;
    }

    public void setData(List<ForumRecommend.LikeForum> likeForums) {
        if (likeForums == null) likeForums = new ArrayList<>();
        List<ForumRecommend.LikeForum> normal = new ArrayList<>();
        this.topForums = new ArrayList<>();
        List<TopForum> topForums = LitePal.findAll(TopForum.class);
        List<String> topIdList = new ArrayList<>();
        List<ForumRecommend.LikeForum> topList = new ArrayList<>();
        for (TopForum topForum : topForums) {
            topIdList.add(topForum.getForumId());
        }
        for (ForumRecommend.LikeForum likeForum : likeForums) {
            if (topIdList.contains(likeForum.getForumId())) {
                topList.add(likeForum);
                if (SharedPreferencesUtil.get(getContext(), SharedPreferencesUtil.SP_SETTINGS).getBoolean("show_top_forum_in_normal_list", true)) {
                    normal.add(likeForum);
                }
            } else {
                normal.add(likeForum);
            }
        }
        this.topForums = topList;
        this.mLikeForums = normal;
        topForumsAdapter.setData(this.topForums);
        notifyDataSetChanged();
    }

    public boolean isSingle() {
        return single;
    }

    public LikeForumListAdapter setSingle(boolean single) {
        this.single = single;
        notifyDataSetChanged();
        return this;
    }
}
