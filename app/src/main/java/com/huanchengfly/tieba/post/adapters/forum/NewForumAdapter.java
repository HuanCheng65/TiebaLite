package com.huanchengfly.tieba.post.adapters.forum;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.gridlayout.widget.GridLayout;

import com.alibaba.android.vlayout.layout.LinearLayoutHelper;
import com.bumptech.glide.Glide;
import com.huanchengfly.tieba.post.BaseApplication;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.activities.ThreadActivity;
import com.huanchengfly.tieba.post.adapters.base.BaseMultiTypeDelegateAdapter;
import com.huanchengfly.tieba.post.api.models.ForumPageBean;
import com.huanchengfly.tieba.post.components.MyViewHolder;
import com.huanchengfly.tieba.post.models.PhotoViewBean;
import com.huanchengfly.tieba.post.utils.BlockUtil;
import com.huanchengfly.tieba.post.utils.DateTimeUtils;
import com.huanchengfly.tieba.post.utils.DisplayUtil;
import com.huanchengfly.tieba.post.utils.ImageUtil;
import com.huanchengfly.tieba.post.utils.NavigationHelper;
import com.huanchengfly.tieba.post.utils.SharedPreferencesUtil;
import com.huanchengfly.tieba.post.utils.StringUtil;
import com.huanchengfly.tieba.post.utils.Util;
import com.huanchengfly.tieba.post.utils.preload.PreloadUtil;
import com.huanchengfly.tieba.post.utils.preload.loaders.ThreadContentLoader;
import com.huanchengfly.tieba.post.widgets.MarkedImageView;
import com.huanchengfly.tieba.post.widgets.VideoPlayerStandard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.jzvd.Jzvd;

import static com.huanchengfly.tieba.post.activities.PhotoViewActivity.OBJ_TYPE_FORUM_PAGE;

public class NewForumAdapter extends BaseMultiTypeDelegateAdapter<ForumPageBean.ThreadBean> {
    public static final String TAG = NewForumAdapter.class.getSimpleName();
    public static final int TYPE_THREAD_COMMON = 11;
    public static final int TYPE_THREAD_SINGLE_PIC = 12;
    public static final int TYPE_THREAD_MULTI_PIC = 13;
    public static final int TYPE_THREAD_VIDEO = 14;
    private ForumPageBean data;
    private Map<String, ForumPageBean.UserBean> userBeanMap;
    private List<Long> ids;

    public NewForumAdapter(Context context) {
        super(context, new LinearLayoutHelper());
        ids = new ArrayList<>();
        userBeanMap = new HashMap<>();
    }

    @Override
    public long getItemId(int position) {
        if (position > 1) {
            int p = position - 2;
            if (p < getItemCount()) {
                return Long.parseLong(Objects.requireNonNull(getItem(p).getId()));
            }
        }
        return position;
    }

    private int getMaxWidth() {
        return BaseApplication.ScreenInfo.EXACT_SCREEN_WIDTH - DisplayUtil.dp2px(getContext(), 40);
    }

    private int getGridHeight() {
        return (BaseApplication.ScreenInfo.EXACT_SCREEN_WIDTH - DisplayUtil.dp2px(getContext(), 70)) / 3;
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

    public void setData(ForumPageBean data) {
        if (data.getThreadList() == null) {
            Toast.makeText(getContext(), R.string.toast_cannot_view, Toast.LENGTH_SHORT).show();
            return;
        }
        this.data = data;
        ids = new ArrayList<>();
        List<ForumPageBean.ThreadBean> threadBeans = new ArrayList<>();
        for (ForumPageBean.ThreadBean threadBean : data.getThreadList()) {
            long id = Long.parseLong(threadBean.getId());
            if (!ids.contains(id) && !needBlock(threadBean) && "0".equals(threadBean.isTop())) {
                ids.add(id);
                threadBeans.add(threadBean);
            }
        }
        setData(threadBeans);
        addUser(data.getUserList());
    }

    public void addData(ForumPageBean data) {
        if (data.getThreadList() == null) {
            Toast.makeText(getContext(), R.string.toast_cannot_view, Toast.LENGTH_SHORT).show();
            return;
        }
        this.data = data;
        addUser(data.getUserList());
        List<ForumPageBean.ThreadBean> threadBeans = new ArrayList<>();
        for (ForumPageBean.ThreadBean threadBean : data.getThreadList()) {
            long id = Long.parseLong(threadBean.getId());
            if (!ids.contains(id) && !needBlock(threadBean) && "0".equals(threadBean.isTop())) {
                ids.add(id);
                threadBeans.add(threadBean);
            }
        }
        insert(threadBeans);
    }

    private void addUser(List<ForumPageBean.UserBean> data) {
        for (ForumPageBean.UserBean userBean : data) {
            if (userBeanMap.get(userBean.getId()) == null) {
                userBeanMap.put(userBean.getId(), userBean);
            }
        }
    }

    private void setListenerForImageView(List<ForumPageBean.MediaInfoBean> mediaInfoBeans, ImageView imageView, int position, ForumPageBean.ThreadBean threadBean) {
        List<PhotoViewBean> photoViewBeans = new ArrayList<>();
        for (ForumPageBean.MediaInfoBean media : mediaInfoBeans) {
            photoViewBeans.add(new PhotoViewBean(ImageUtil.getNonNullString(media.getBigPic(), media.getSrcPic(), media.getOriginPic()),
                    ImageUtil.getNonNullString(media.getOriginPic(), media.getSrcPic(), media.getBigPic()),
                    "1".equals(media.isLongPic())));
        }
        ImageUtil.initImageView(imageView,
                photoViewBeans,
                position,
                data.getForum().getName(),
                data.getForum().getId(),
                threadBean.getId(),
                true,
                OBJ_TYPE_FORUM_PAGE);
    }

    private void load(ForumPageBean.MediaInfoBean mediaInfoBean, ImageView imageView) {
        imageView.setVisibility(View.VISIBLE);
        String url = ImageUtil.getUrl(getContext(), true, mediaInfoBean.getOriginPic(), mediaInfoBean.getSrcPic());
        if ("3".equals(mediaInfoBean.getType())) {
            ImageUtil.load(imageView, ImageUtil.LOAD_TYPE_NO_RADIUS, url);
        }
    }

    private void startActivity(ForumPageBean.ThreadBean threadBean) {
        PreloadUtil.startActivityWithPreload(getContext(),
                new Intent(getContext(), ThreadActivity.class)
                        .putExtra("tid", threadBean.getTid())
                        .putExtra("from", ThreadActivity.FROM_FORUM),
                new ThreadContentLoader(threadBean.getTid(), 1, false));
    }

    @Override
    protected void convert(MyViewHolder viewHolder, ForumPageBean.ThreadBean threadBean, int position, int type) {
        viewHolder.setText(R.id.forum_item_comment_count_text, threadBean.getReplyNum());
        viewHolder.setText(R.id.forum_item_agree_count_text, threadBean.getAgreeNum());
        if ("1".equals(threadBean.isGood())) {
            viewHolder.setVisibility(R.id.forum_item_good_tip, View.VISIBLE);
        } else {
            viewHolder.setVisibility(R.id.forum_item_good_tip, View.GONE);
        }
        viewHolder.setOnClickListener(R.id.forum_item, view -> startActivity(threadBean));
        String title = "1".equals(threadBean.isNoTitle()) ? null : threadBean.getTitle();
        String text = (!TextUtils.isEmpty(threadBean.getAbstractString())
        ) ? threadBean.getAbstractString() : null;
        if (title != null && text != null) {
            viewHolder.setVisibility(R.id.forum_item_title_holder, View.VISIBLE);
            viewHolder.setText(R.id.forum_item_title, title);
            viewHolder.setVisibility(R.id.forum_item_content_text, View.VISIBLE);
            viewHolder.setText(R.id.forum_item_content_text, text);
        } else if (title == null && text != null) {
            viewHolder.setVisibility(R.id.forum_item_title_holder, View.VISIBLE);
            viewHolder.setText(R.id.forum_item_title, text);
            viewHolder.setVisibility(R.id.forum_item_content_text, View.GONE);
            viewHolder.setText(R.id.forum_item_content_text, null);
        } else if (title != null) {
            viewHolder.setVisibility(R.id.forum_item_title_holder, View.VISIBLE);
            viewHolder.setText(R.id.forum_item_title, title);
            viewHolder.setVisibility(R.id.forum_item_content_text, View.GONE);
            viewHolder.setText(R.id.forum_item_content_text, null);
        } else {
            viewHolder.setVisibility(R.id.forum_item_title_holder, View.GONE);
            viewHolder.setText(R.id.forum_item_title, null);
            viewHolder.setVisibility(R.id.forum_item_content_text, View.GONE);
            viewHolder.setText(R.id.forum_item_content_text, null);
        }
        ForumPageBean.UserBean userBean = userBeanMap.get(threadBean.getAuthorId());
        if (userBean != null) {
            viewHolder.setOnClickListener(R.id.forum_item_user_avatar, v -> {
                NavigationHelper.toUserSpaceWithAnim(getContext(), userBean.getId(), userBean.getPortrait(), v);
            });
            viewHolder.setText(R.id.forum_item_user_name, StringUtil.getUsernameString(getContext(), userBean.getName(), userBean.getNameShow()));
            viewHolder.setText(R.id.forum_item_user_time, DateTimeUtils.getRelativeTimeString(getContext(), threadBean.getLastTimeInt()));
            ImageUtil.load(viewHolder.getView(R.id.forum_item_user_avatar), ImageUtil.LOAD_TYPE_AVATAR, userBean.getPortrait());
        }
        switch (type) {
            case TYPE_THREAD_SINGLE_PIC:
                if (Util.canLoadGlide(getContext()) && "3".equals(threadBean.getMedia().get(0).getType())) {
                    ImageView imageView = viewHolder.getView(R.id.forum_item_content_pic);
                    imageView.setLayoutParams(getLayoutParams((RelativeLayout.LayoutParams) imageView.getLayoutParams()));
                    setListenerForImageView(threadBean.getMedia(), imageView, 0, threadBean);
                    ForumPageBean.MediaInfoBean mediaInfoBean = threadBean.getMedia().get(0);
                    ImageUtil.load(
                            imageView,
                            ImageUtil.LOAD_TYPE_SMALL_PIC,
                            ImageUtil.getUrl(
                                    getContext(),
                                    true,
                                    mediaInfoBean.getOriginPic(),
                                    mediaInfoBean.getSrcPic(),
                                    mediaInfoBean.getBigPic()
                            )
                    );
                }
                break;
            case TYPE_THREAD_MULTI_PIC:
                GridLayout gridLayout = viewHolder.getView(R.id.forum_item_content_pics);
                CardView cardView = viewHolder.getView(R.id.forum_item_content_pics_card);
                cardView.setRadius(DisplayUtil.dp2px(getContext(), SharedPreferencesUtil.get(getContext(), SharedPreferencesUtil.SP_SETTINGS).getInt("radius", 8)));
                MarkedImageView firstImageView = viewHolder.getView(R.id.forum_item_content_pic_1);
                MarkedImageView secondImageView = viewHolder.getView(R.id.forum_item_content_pic_2);
                MarkedImageView thirdImageView = viewHolder.getView(R.id.forum_item_content_pic_3);
                gridLayout.setLayoutParams(getGridLayoutParams(gridLayout.getLayoutParams()));
                int size = threadBean.getMedia().size();
                if (size >= 1) {
                    setListenerForImageView(threadBean.getMedia(), firstImageView, 0, threadBean);
                    ForumPageBean.MediaInfoBean firstMedia = threadBean.getMedia().get(0);
                    load(firstMedia, firstImageView);
                } else {
                    firstImageView.setVisibility(View.GONE);
                    Glide.with(getContext())
                            .clear(firstImageView);
                }
                if (size >= 2) {
                    setListenerForImageView(threadBean.getMedia(), secondImageView, 1, threadBean);
                    ForumPageBean.MediaInfoBean secondMedia = threadBean.getMedia().get(1);
                    load(secondMedia, secondImageView);
                } else {
                    secondImageView.setVisibility(View.GONE);
                    Glide.with(getContext())
                            .clear(secondImageView);
                }
                if (size >= 3) {
                    setListenerForImageView(threadBean.getMedia(), thirdImageView, 2, threadBean);
                    ForumPageBean.MediaInfoBean thirdMedia = threadBean.getMedia().get(2);
                    load(thirdMedia, thirdImageView);
                } else {
                    thirdImageView.setVisibility(View.GONE);
                    Glide.with(getContext())
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
    }

    private boolean needBlock(ForumPageBean.ThreadBean threadBean) {
        if (!TextUtils.isEmpty(threadBean.getTitle()) && BlockUtil.needBlock(threadBean.getTitle())) {
            return true;
        }
        ForumPageBean.UserBean userBean = userBeanMap.get(threadBean.getAuthorId());
        if (BlockUtil.needBlock(userBean != null ? userBean.getName() : null, threadBean.getAuthorId())) {
            return true;
        }
        return !(TextUtils.isEmpty(threadBean.getAbstractString())) && BlockUtil.needBlock(threadBean.getAbstractString());
    }

    private String getAbstracts(List<ForumPageBean.AbstractBean> abstractBeans) {
        StringBuilder stringBuilder = new StringBuilder();
        for (ForumPageBean.AbstractBean abstractBean : abstractBeans) {
            if ("0".equals(abstractBean.getType())) {
                stringBuilder.append(abstractBean.getText());
            }
        }
        return stringBuilder.toString();
    }

    @Override
    protected int getItemLayoutId(int type) {
        switch (type) {
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
    protected int getViewType(int position, ForumPageBean.ThreadBean threadBean) {
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

    @Override
    public void onViewDetachedFromWindow(@NonNull MyViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder.getItemViewType() == TYPE_THREAD_VIDEO) {
            VideoPlayerStandard videoPlayerStandard = holder.getViewOrNull(R.id.forum_item_content_video);
            if (videoPlayerStandard != null && Jzvd.CURRENT_JZVD != null &&
                    videoPlayerStandard.jzDataSource.containsTheUrl(Jzvd.CURRENT_JZVD.jzDataSource.getCurrentUrl())) {
                if (Jzvd.CURRENT_JZVD != null && Jzvd.CURRENT_JZVD.screen != Jzvd.SCREEN_FULLSCREEN) {
                    Jzvd.releaseAllVideos();
                }
            }
        }
    }
}
