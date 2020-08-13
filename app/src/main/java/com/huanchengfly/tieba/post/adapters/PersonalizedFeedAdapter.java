package com.huanchengfly.tieba.post.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.gridlayout.widget.GridLayout;
import com.bumptech.glide.Glide;
import com.huanchengfly.tieba.api.models.ForumPageBean;
import com.huanchengfly.tieba.api.models.PersonalizedBean;
import com.huanchengfly.tieba.hotmessage.activities.HotMessageListActivity;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.base.Config;
import com.huanchengfly.tieba.post.components.dialogs.DislikeDialog;
import com.huanchengfly.tieba.post.models.PhotoViewBean;
import com.huanchengfly.tieba.post.utils.*;
import com.huanchengfly.tieba.widgets.MarkedImageView;
import com.huanchengfly.tieba.widgets.VideoPlayerStandard;
import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.base.MultiBaseAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonalizedFeedAdapter extends MultiBaseAdapter<PersonalizedBean.ThreadBean> {
    public static final int TYPE_THREAD_COMMON = 11;
    public static final int TYPE_THREAD_SINGLE_PIC = 12;
    public static final int TYPE_THREAD_MULTI_PIC = 13;
    public static final int TYPE_THREAD_VIDEO = 14;

    private NavigationHelper navigationHelper;
    private PersonalizedBean personalizedBean;

    private int refreshPosition;
    private OnRefreshListener onRefreshListener;

    public PersonalizedFeedAdapter(Context context) {
        super(context, null, true);
        refreshPosition = -1;
        setLoadingView(R.layout.layout_footer_loading);
        setLoadEndView(R.layout.layout_footer_loadend);
        setLoadFailedView(R.layout.layout_footer_load_failed);
        View header = Util.inflate(context, R.layout.header_feed);
        View hotMessage = header.findViewById(R.id.hot_message);
        hotMessage.setOnClickListener(v -> context.startActivity(new Intent(context, HotMessageListActivity.class)));
        addHeaderView(header);
        navigationHelper = NavigationHelper.newInstance(context);
    }

    public OnRefreshListener getOnRefreshListener() {
        return onRefreshListener;
    }

    public PersonalizedFeedAdapter setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
        return this;
    }

    public int getRefreshPosition() {
        return refreshPosition;
    }

    public void setRefreshPosition(int refreshPosition) {
        this.refreshPosition = refreshPosition;
    }

    private int getMaxWidth() {
        return Config.EXACT_SCREEN_WIDTH - DisplayUtil.dp2px(mContext, 40);
    }

    private int getGridHeight() {
        return (Config.EXACT_SCREEN_WIDTH - DisplayUtil.dp2px(mContext, 70)) / 3;
    }

    private RelativeLayout.LayoutParams getLayoutParams(RelativeLayout.LayoutParams layoutParams) {
        layoutParams.width = getMaxWidth();
        layoutParams.height = (int) (getMaxWidth() * 0.5625);
        return layoutParams;
    }

    private ViewGroup.LayoutParams getGridLayoutParams(ViewGroup.LayoutParams layoutParams) {
        layoutParams.height = getGridHeight();
        return layoutParams;
    }

    private void setListenerForImageView(List<ForumPageBean.MediaInfoBean> mediaInfoBeans, ImageView imageView, int position) {
        List<PhotoViewBean> photoViewBeans = new ArrayList<>();
        for (ForumPageBean.MediaInfoBean media : mediaInfoBeans) {
            photoViewBeans.add(new PhotoViewBean(ImageUtil.getNonNullString(media.getSrcPic(), media.getOriginPic()),
                    ImageUtil.getNonNullString(media.getOriginPic(), media.getSrcPic()),
                    "1".equals(media.getShowOriginalBtn())));
        }
        ImageUtil.initImageView(imageView, photoViewBeans, position);
    }

    public void setData(PersonalizedBean personalizedBean) {
        this.personalizedBean = personalizedBean;
    }

    private void load(ForumPageBean.MediaInfoBean mediaInfoBean, ImageView imageView) {
        imageView.setVisibility(View.VISIBLE);
        String url = ImageUtil.getUrl(mContext, true, mediaInfoBean.getOriginPic(), mediaInfoBean.getSrcPic());
        if ("3".equals(mediaInfoBean.getType())) {
            ImageUtil.load(imageView, ImageUtil.LOAD_TYPE_NO_RADIUS, url);
        }
    }

    @Override
    protected void convert(ViewHolder viewHolder, PersonalizedBean.ThreadBean threadBean, int position, int viewType) {
        View refreshTip = viewHolder.getView(R.id.feed_refresh_tip);
        if (position == getRefreshPosition()) {
            refreshTip.setVisibility(View.VISIBLE);
            refreshTip.setOnClickListener(v -> {
                if (getOnRefreshListener() != null) {
                    getOnRefreshListener().onRefresh();
                }
            });
        } else {
            refreshTip.setVisibility(View.GONE);
        }
        viewHolder.setText(R.id.forum_item_comment_count_text, threadBean.getReplyNum());
        viewHolder.setVisibility(R.id.dislike, View.VISIBLE);
        if (threadBean.getThreadPersonalizedBean() == null) {
            viewHolder.setVisibility(R.id.dislike, View.INVISIBLE);
        } else {
            viewHolder.setVisibility(R.id.dislike, View.VISIBLE);
        }
        viewHolder.setOnClickListener(R.id.dislike, v -> {
            if (threadBean.getThreadPersonalizedBean() == null || threadBean.getThreadPersonalizedBean().getDislikeResource() == null) {
                if (position <= refreshPosition && refreshPosition > -1) {
                    refreshPosition -= 1;
                    if (position == (refreshPosition + 1)) {
                        notifyItemChanged(position);
                    }
                    if (refreshPosition > -1) {
                        notifyItemChanged(refreshPosition);
                    }
                }
                remove(position);
            } else {
                DislikeDialog dislikeDialog = new DislikeDialog(mContext, threadBean.getThreadPersonalizedBean(), threadBean.getForumId());
                dislikeDialog.setOnSubmitListener(() -> {
                    if (position <= refreshPosition && refreshPosition > -1) {
                        refreshPosition -= 1;
                        if (position == (refreshPosition + 1)) {
                            notifyItemChanged(position);
                        }
                        if (refreshPosition > -1) {
                            notifyItemChanged(refreshPosition);
                        }
                    }
                    remove(position);
                });
                dislikeDialog.show();
            }
        });
        if ("1".equals(threadBean.isGood())) {
            viewHolder.setVisibility(R.id.forum_item_good_tip, View.VISIBLE);
        } else {
            viewHolder.setVisibility(R.id.forum_item_good_tip, View.GONE);
        }
        viewHolder.setOnClickListener(R.id.forum_item, view -> {
            Map<String, String> map = new HashMap<>();
            map.put("tid", threadBean.getTid());
            navigationHelper.navigationByData(NavigationHelper.ACTION_THREAD, map);
        });
        if ("1".equals(threadBean.isNoTitle())) {
            viewHolder.setVisibility(R.id.forum_item_title_holder, View.GONE);
        } else {
            viewHolder.setVisibility(R.id.forum_item_title_holder, View.VISIBLE);
            viewHolder.setText(R.id.forum_item_title, threadBean.getTitle());
        }
        TextView textView = viewHolder.getView(R.id.forum_item_content_text);
        if (threadBean.getAbstractBeans().size() > 0 && "0".equals(threadBean.getAbstractBeans().get(0).getType())) {
            if (TextUtils.isEmpty(threadBean.getAbstractBeans().get(0).getText())) {
                textView.setText(null);
                textView.setVisibility(View.GONE);
            } else {
                textView.setVisibility(View.VISIBLE);
                textView.setText(threadBean.getAbstractBeans().get(0).getText());
            }
        } else {
            textView.setText(null);
            textView.setVisibility(View.GONE);
        }
        PersonalizedBean.AuthorBean authorBean = threadBean.getAuthor();
        if (authorBean != null) {
            viewHolder.setOnClickListener(R.id.forum_item_user_avatar, v -> NavigationHelper.toUserSpaceWithAnim(mContext, authorBean.getId(), authorBean.getPortrait(), v));
            viewHolder.setText(R.id.forum_item_user_name, authorBean.getNameShow());
            viewHolder.setText(R.id.forum_item_user_time, String.valueOf(DateUtils.getRelativeTimeSpanString(Long.valueOf(threadBean.getLastTimeInt()) * 1000L)));
            ImageUtil.load(viewHolder.getView(R.id.forum_item_user_avatar), ImageUtil.LOAD_TYPE_AVATAR, authorBean.getPortrait());
        }
        switch (viewType) {
            case TYPE_THREAD_SINGLE_PIC:
                if (Util.canLoadGlide(mContext) && "3".equals(threadBean.getMedia().get(0).getType())) {
                    ImageView imageView = viewHolder.getView(R.id.forum_item_content_pic);
                    imageView.setLayoutParams(getLayoutParams((RelativeLayout.LayoutParams) imageView.getLayoutParams()));
                    setListenerForImageView(threadBean.getMedia(), imageView, 0);
                    ForumPageBean.MediaInfoBean mediaInfoBean = threadBean.getMedia().get(0);
                    ImageUtil.load(imageView, ImageUtil.LOAD_TYPE_SMALL_PIC, ImageUtil.getUrl(mContext, true, mediaInfoBean.getOriginPic(), mediaInfoBean.getSrcPic()));
                }
                break;
            case TYPE_THREAD_MULTI_PIC:
                GridLayout gridLayout = viewHolder.getView(R.id.forum_item_content_pics);
                CardView cardView = viewHolder.getView(R.id.forum_item_content_pics_card);
                cardView.setRadius(DisplayUtil.dp2px(mContext, SharedPreferencesUtil.get(mContext, SharedPreferencesUtil.SP_SETTINGS).getInt("radius", 8)));
                MarkedImageView firstImageView = viewHolder.getView(R.id.forum_item_content_pic_1);
                MarkedImageView secondImageView = viewHolder.getView(R.id.forum_item_content_pic_2);
                MarkedImageView thirdImageView = viewHolder.getView(R.id.forum_item_content_pic_3);
                gridLayout.setLayoutParams(getGridLayoutParams(gridLayout.getLayoutParams()));
                int size = threadBean.getMedia().size();
                if (size >= 1) {
                    setListenerForImageView(threadBean.getMedia(), firstImageView, 0);
                    ForumPageBean.MediaInfoBean firstMedia = threadBean.getMedia().get(0);
                    load(firstMedia, firstImageView);
                } else {
                    firstImageView.setVisibility(View.GONE);
                    Glide.with(mContext)
                            .clear(firstImageView);
                }
                if (size >= 2) {
                    setListenerForImageView(threadBean.getMedia(), secondImageView, 1);
                    ForumPageBean.MediaInfoBean secondMedia = threadBean.getMedia().get(1);
                    load(secondMedia, secondImageView);
                } else {
                    secondImageView.setVisibility(View.GONE);
                    Glide.with(mContext)
                            .clear(secondImageView);
                }
                if (size >= 3) {
                    setListenerForImageView(threadBean.getMedia(), thirdImageView, 2);
                    ForumPageBean.MediaInfoBean thirdMedia = threadBean.getMedia().get(2);
                    load(thirdMedia, thirdImageView);
                } else {
                    thirdImageView.setVisibility(View.GONE);
                    Glide.with(mContext)
                            .clear(thirdImageView);
                }
                if (size > 3) {
                    viewHolder.setVisibility(R.id.forum_item_content_pic_badge, View.VISIBLE);
                    viewHolder.setText(R.id.forum_item_content_pic_badge_text, String.valueOf(size));
                } else {
                    viewHolder.setVisibility(R.id.forum_item_content_pic_badge, View.GONE);
                }
                break;
            case TYPE_THREAD_VIDEO:
                if (threadBean.getVideoInfo() == null) {
                    break;
                }
                VideoPlayerStandard videoPlayerStandard = viewHolder.getView(R.id.forum_item_content_video);
                videoPlayerStandard.setLayoutParams(getLayoutParams((RelativeLayout.LayoutParams) videoPlayerStandard.getLayoutParams()));
                videoPlayerStandard.setUp(threadBean.getVideoInfo().getVideoUrl(), "");
                ImageUtil.load(videoPlayerStandard.posterImageView, ImageUtil.LOAD_TYPE_SMALL_PIC, threadBean.getVideoInfo().getThumbnailUrl(), true);
                break;
        }
        if (!TextUtils.isEmpty(threadBean.getForumName()))
            viewHolder.setText(R.id.forum_item_forum_name, mContext.getString(R.string.tip_forum_name, threadBean.getForumName()));
        else
            viewHolder.setText(R.id.forum_item_forum_name, "");
    }

    @Override
    protected int getItemLayoutId(int viewType) {
        switch (viewType) {
            case TYPE_THREAD_COMMON:
                return R.layout.item_forum_thread_common;
            case TYPE_THREAD_VIDEO:
                return R.layout.item_forum_thread_video;
            case TYPE_THREAD_SINGLE_PIC:
                return R.layout.item_forum_thread_single_pic;
            case TYPE_THREAD_MULTI_PIC:
                return R.layout.item_forum_thread_multi_pic;
        }
        return R.layout.item_forum_thread_common;
    }

    @Override
    protected int getViewType(int position, PersonalizedBean.ThreadBean threadBean) {
        if (threadBean.getVideoInfo() != null) {
            return TYPE_THREAD_VIDEO;
        }
        if (threadBean.getMedia() == null) {
            return TYPE_THREAD_COMMON;
        }
        if (threadBean.getMedia().size() == 1) {
            return TYPE_THREAD_SINGLE_PIC;
        }
        if (threadBean.getMedia().size() > 1) {
            return TYPE_THREAD_MULTI_PIC;
        }
        return TYPE_THREAD_COMMON;
    }

    public interface OnRefreshListener {
        void onRefresh();
    }
}
