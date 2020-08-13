package com.huanchengfly.tieba.post.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.huanchengfly.tieba.api.models.ForumPageBean;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ThreadActivity;
import com.huanchengfly.tieba.post.base.Config;
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager;
import com.huanchengfly.tieba.post.components.dividers.SpacesItemDecoration;
import com.huanchengfly.tieba.post.interfaces.OnSwitchListener;
import com.huanchengfly.tieba.post.models.PhotoViewBean;
import com.huanchengfly.tieba.post.utils.*;
import com.huanchengfly.tieba.post.utils.preload.PreloadUtil;
import com.huanchengfly.tieba.post.utils.preload.loaders.ThreadContentLoader;
import com.huanchengfly.tieba.widgets.MarkedImageView;
import com.huanchengfly.tieba.widgets.VideoPlayerStandard;
import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.base.MultiBaseAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.huanchengfly.tieba.post.activities.PhotoViewActivity.OBJ_TYPE_FORUM_PAGE;

public class ForumAdapter extends MultiBaseAdapter<ForumPageBean.ThreadBean> {
    public static final String TAG = ForumAdapter.class.getSimpleName();
    public static final int TYPE_THREAD_TOP = 10;
    public static final int TYPE_THREAD_COMMON = 11;
    public static final int TYPE_THREAD_SINGLE_PIC = 12;
    public static final int TYPE_THREAD_MULTI_PIC = 13;
    public static final int TYPE_THREAD_VIDEO = 14;
    private ForumPageBean data;
    private Map<String, ForumPageBean.UserBean> userBeanMap;
    private GoodClassifyAdapter goodClassifyAdapter;
    private List<Long> ids;
    private boolean good;

    public ForumAdapter(Context context, boolean isGood) {
        super(context, null, true);
        ids = new ArrayList<>();
        userBeanMap = new HashMap<>();
        good = isGood;
        if (isGood) {
            View goodView = Util.inflate(mContext, R.layout.layout_header_forum_good);
            if (goodView != null) {
                addHeaderView(goodView);
                goodClassifyAdapter = new GoodClassifyAdapter(mContext);
                RecyclerView goodClassifyView = goodView.findViewById(R.id.forum_good_classify);
                goodClassifyView.setLayoutManager(new MyLinearLayoutManager(mContext, MyLinearLayoutManager.HORIZONTAL, false));
                goodClassifyView.addItemDecoration(new SpacesItemDecoration(DisplayUtil.dp2px(mContext, 8)));
                goodClassifyView.setAdapter(goodClassifyAdapter);
                refreshGood();
            }
        }
    }

    public void setOnSwitchListener(OnSwitchListener onSwitchListener) {
        if (good) goodClassifyAdapter.setOnSwitchListener(onSwitchListener);
    }

    @Override
    public long getItemId(int position) {
        if (position > 1) {
            int p = position - 2;
            if (p < getDataCount()) {
                return Long.valueOf(getData(p).getId());
            }
        }
        return position;
    }

    private void refreshGood() {
        if (data == null || !good) {
            return;
        }
        goodClassifyAdapter.setData(data.getForum().getGoodClassify());
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

    public void setData(ForumPageBean data) {
        if (data.getThreadList() == null) {
            Toast.makeText(mContext, R.string.toast_cannot_view, Toast.LENGTH_SHORT).show();
            return;
        }
        this.data = data;
        ids = new ArrayList<>();
        List<ForumPageBean.ThreadBean> threadBeans = new ArrayList<>();
        for (ForumPageBean.ThreadBean threadBean : data.getThreadList()) {
            long id = Long.valueOf(threadBean.getId());
            if (!ids.contains(id) && !needBlock(threadBean)) {
                ids.add(id);
                threadBeans.add(threadBean);
            }
        }
        setNewData(threadBeans);
        addUser(data.getUserList());
        refreshGood();
    }

    public void addData(ForumPageBean data) {
        if (data.getThreadList() == null) {
            Toast.makeText(mContext, R.string.toast_cannot_view, Toast.LENGTH_SHORT).show();
            return;
        }
        this.data = data;
        addUser(data.getUserList());
        List<ForumPageBean.ThreadBean> threadBeans = new ArrayList<>();
        for (ForumPageBean.ThreadBean threadBean : data.getThreadList()) {
            long id = Long.valueOf(threadBean.getId());
            if (!ids.contains(id) && !needBlock(threadBean)) {
                ids.add(id);
                threadBeans.add(threadBean);
            }
        }
        setLoadMoreData(threadBeans);
        refreshGood();
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
        String url = ImageUtil.getUrl(mContext, true, mediaInfoBean.getOriginPic(), mediaInfoBean.getSrcPic());
        if ("3".equals(mediaInfoBean.getType())) {
            ImageUtil.load(imageView, ImageUtil.LOAD_TYPE_NO_RADIUS, url);
        }
    }

    private void startActivity(ForumPageBean.ThreadBean threadBean) {
        PreloadUtil.startActivityWithPreload(mContext,
                new Intent(mContext, ThreadActivity.class)
                        .putExtra("tid", threadBean.getTid())
                        .putExtra("from", ThreadActivity.FROM_FORUM),
                new ThreadContentLoader(threadBean.getTid(), 1, false));
    }

    @Override
    protected void convert(ViewHolder viewHolder, ForumPageBean.ThreadBean threadBean, int position, int type) {
        if (type == TYPE_THREAD_TOP) {
            viewHolder.setOnClickListener(R.id.forum_item_top, view -> startActivity(threadBean));
            viewHolder.setText(R.id.forum_item_top_title, threadBean.getTitle());
            return;
        }
        viewHolder.setText(R.id.forum_item_comment_count_text, threadBean.getReplyNum());
        if ("1".equals(threadBean.isGood())) {
            viewHolder.setVisibility(R.id.forum_item_good_tip, View.VISIBLE);
        } else {
            viewHolder.setVisibility(R.id.forum_item_good_tip, View.GONE);
        }
        viewHolder.setOnClickListener(R.id.forum_item, view -> startActivity(threadBean));
        if ("1".equals(threadBean.isNoTitle())) {
            viewHolder.setVisibility(R.id.forum_item_title_holder, View.GONE);
        } else {
            viewHolder.setVisibility(R.id.forum_item_title_holder, View.VISIBLE);
            viewHolder.setText(R.id.forum_item_title, threadBean.getTitle());
        }
        TextView textView = viewHolder.getView(R.id.forum_item_content_text);
        if (TextUtils.isEmpty(threadBean.getAbstractString())) {
            textView.setText(null);
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(threadBean.getAbstractString());
            textView.setVisibility(View.VISIBLE);
        }
        ForumPageBean.UserBean userBean = userBeanMap.get(threadBean.getAuthorId());
        if (userBean != null) {
            viewHolder.setOnClickListener(R.id.forum_item_user_avatar, v -> {
                NavigationHelper.toUserSpaceWithAnim(mContext, userBean.getId(), userBean.getPortrait(), v);
            });
            viewHolder.setText(R.id.forum_item_user_name, StringUtil.getUsernameString(mContext, userBean.getName(), userBean.getNameShow()));
            viewHolder.setText(R.id.forum_item_user_time, Util.getTime(threadBean.getLastTimeInt()));
            ImageUtil.load(viewHolder.getView(R.id.forum_item_user_avatar), ImageUtil.LOAD_TYPE_AVATAR, userBean.getPortrait());
        }
        switch (type) {
            case TYPE_THREAD_SINGLE_PIC:
                if (Util.canLoadGlide(mContext) && "3".equals(threadBean.getMedia().get(0).getType())) {
                    MarkedImageView imageView = viewHolder.getView(R.id.forum_item_content_pic);
                    imageView.setLayoutParams(getLayoutParams((RelativeLayout.LayoutParams) imageView.getLayoutParams()));
                    setListenerForImageView(threadBean.getMedia(), imageView, 0, threadBean);
                    ForumPageBean.MediaInfoBean mediaInfoBean = threadBean.getMedia().get(0);
                    if ("1".equals(mediaInfoBean.isGif())) {
                        imageView.setMarkText("GIF");
                        imageView.setMarkVisible(true);
                    } else {
                        imageView.setMarkText("");
                        imageView.setMarkVisible(false);
                    }
                    ImageUtil.load(imageView, ImageUtil.LOAD_TYPE_SMALL_PIC, ImageUtil.getUrl(mContext, true, mediaInfoBean.getOriginPic(), mediaInfoBean.getSrcPic(), mediaInfoBean.getBigPic()));
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
                    setListenerForImageView(threadBean.getMedia(), firstImageView, 0, threadBean);
                    ForumPageBean.MediaInfoBean firstMedia = threadBean.getMedia().get(0);
                    load(firstMedia, firstImageView);
                } else {
                    firstImageView.setVisibility(View.GONE);
                    Glide.with(mContext)
                            .clear(firstImageView);
                }
                if (size >= 2) {
                    setListenerForImageView(threadBean.getMedia(), secondImageView, 1, threadBean);
                    ForumPageBean.MediaInfoBean secondMedia = threadBean.getMedia().get(1);
                    load(secondMedia, secondImageView);
                } else {
                    secondImageView.setVisibility(View.GONE);
                    Glide.with(mContext)
                            .clear(secondImageView);
                }
                if (size >= 3) {
                    setListenerForImageView(threadBean.getMedia(), thirdImageView, 2, threadBean);
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
            case TYPE_THREAD_TOP:
                return R.layout.item_forum_thread_top;
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
        if ("1".equals(threadBean.isTop())) {
            return TYPE_THREAD_TOP;
        }
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
}
