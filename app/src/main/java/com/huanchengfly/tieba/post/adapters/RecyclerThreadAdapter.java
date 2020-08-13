package com.huanchengfly.tieba.post.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import com.allen.library.SuperTextView;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.huanchengfly.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.api.TiebaApi;
import com.huanchengfly.tieba.api.models.CommonResponse;
import com.huanchengfly.tieba.api.models.ThreadContentBean;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.activities.ReplyActivity;
import com.huanchengfly.tieba.post.activities.base.BaseActivity;
import com.huanchengfly.tieba.post.base.Config;
import com.huanchengfly.tieba.post.components.spans.MyImageSpan;
import com.huanchengfly.tieba.post.components.spans.MyURLSpan;
import com.huanchengfly.tieba.post.components.spans.MyUserSpan;
import com.huanchengfly.tieba.post.components.spans.RoundBackgroundColorSpan;
import com.huanchengfly.tieba.post.fragments.ConfirmDialogFragment;
import com.huanchengfly.tieba.post.fragments.FloorFragment;
import com.huanchengfly.tieba.post.fragments.MenuDialogFragment;
import com.huanchengfly.tieba.post.models.PhotoViewBean;
import com.huanchengfly.tieba.post.models.ReplyInfoBean;
import com.huanchengfly.tieba.post.utils.*;
import com.huanchengfly.tieba.widgets.ContentLayout;
import com.huanchengfly.tieba.widgets.MyImageView;
import com.huanchengfly.tieba.widgets.VideoPlayerStandard;
import com.huanchengfly.tieba.widgets.VoicePlayerView;
import com.huanchengfly.tieba.widgets.theme.TintTextView;
import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.base.MultiBaseAdapter;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.*;

import static com.huanchengfly.tieba.post.activities.PhotoViewActivity.OBJ_TYPE_THREAD_PAGE;
import static com.huanchengfly.tieba.post.utils.Util.alphaColor;

public class RecyclerThreadAdapter extends MultiBaseAdapter<ThreadContentBean.PostListItemBean> {
    public static final String TAG = "RecyclerThreadAdapter";
    public static final int TYPE_REPLY = 1000;
    public static final int TYPE_THREAD = 1001;
    private static final int TEXT_VIEW_TYPE_CONTENT = 0;
    private static final int TEXT_VIEW_TYPE_FLOOR = 1;
    private Map<String, ThreadContentBean.UserInfoBean> userInfoBeanMap;
    private NavigationHelper navigationHelper;
    private RequestOptions avatarRequestOptions;
    private LinearLayout.LayoutParams defaultLayoutParams;
    private RequestOptions defaultRequestOptions;
    private DrawableTransitionOptions transitionOptions;
    private ThreadContentBean.ThreadBean threadBean;
    private ThreadContentBean dataBean;
    private TreeMap<Integer, List<PhotoViewBean>> photoViewBeansMap;
    private int screenWidthPx;
    private boolean showForum;
    private Map<String, Boolean> blockCacheMap;
    private boolean immersive;
    private boolean seeLz;

    public RecyclerThreadAdapter(Context context) {
        super(context, null, true);
        setLoadingView(R.layout.layout_footer_loading);
        setLoadEndView(R.layout.layout_footer_loadend);
        setLoadFailedView(R.layout.layout_footer_load_failed);
        setOnMultiItemClickListener((viewHolder, postListItemBean, i, i1) -> {
            ThreadContentBean.UserInfoBean userInfoBean = userInfoBeanMap.get(postListItemBean.getAuthorId());
            mContext.startActivity(new Intent(mContext, ReplyActivity.class)
                    .putExtra("data", new ReplyInfoBean(dataBean.getThread().getId(),
                            dataBean.getForum().getId(),
                            dataBean.getForum().getName(),
                            dataBean.getAnti().getTbs(),
                            postListItemBean.getId(),
                            postListItemBean.getFloor(),
                            userInfoBean != null ? userInfoBean.getNameShow() : "",
                            dataBean.getUser().getNameShow()).setPn(dataBean.getPage().getOffset()).toString()));
        });
        showForum = true;
        userInfoBeanMap = new HashMap<>();
        avatarRequestOptions = new RequestOptions()
                .placeholder(R.drawable.bg_placeholder_circle)
                .circleCrop()
                .skipMemoryCache(true);
        navigationHelper = NavigationHelper.newInstance(mContext);
        immersive = false;
        this.screenWidthPx = Config.EXACT_SCREEN_WIDTH;
        defaultLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        defaultLayoutParams.setMargins(0, 8, 0, 8);
        defaultRequestOptions = new RequestOptions()
                .placeholder(R.drawable.bg_placeholder)
                .skipMemoryCache(true);
        transitionOptions = DrawableTransitionOptions.withCrossFade();
        photoViewBeansMap = new TreeMap<>();
        blockCacheMap = new HashMap<>();
    }

    public boolean isImmersive() {
        return immersive;
    }

    public RecyclerThreadAdapter setImmersive(boolean immersive) {
        this.immersive = immersive;
        notifyDataSetChanged();
        return this;
    }

    public boolean isShowForum() {
        return showForum;
    }

    public RecyclerThreadAdapter setShowForum(boolean showForum) {
        this.showForum = showForum;
        return this;
    }

    public boolean isSeeLz() {
        return seeLz;
    }

    public RecyclerThreadAdapter setSeeLz(boolean seeLz) {
        this.seeLz = seeLz;
        return this;
    }

    public void setData(ThreadContentBean data) {
        threadBean = data.getThread();
        dataBean = data;
        setUser(data.getUserList());
        setPic(data.getPostList());
        List<ThreadContentBean.PostListItemBean> postListItemBeans = new ArrayList<>();
        for (ThreadContentBean.PostListItemBean postListItemBean : data.getPostList()) {
            if (!needBlock(postListItemBean)) {
                postListItemBeans.add(postListItemBean);
            }
        }
        setNewData(postListItemBeans);
    }

    private void refreshForumView(ThreadContentBean.ForumInfoBean forumInfoBean, SuperTextView forumView, View dividerView) {
        if (forumView == null || dividerView == null || forumInfoBean == null) {
            return;
        }
        if (!showForum || !mContext.getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("showShortcutInThread", true) || "0".equals(forumInfoBean.isExists()) || forumInfoBean.getName().isEmpty()) {
            forumView.setVisibility(View.GONE);
            dividerView.setVisibility(View.GONE);
            return;
        }
        forumView.getLeftTextView().getPaint().setFakeBoldText(true);
        forumView.setVisibility(View.VISIBLE);
        forumView.setOnSuperTextViewClickListener((SuperTextView superTextView) -> {
            navigationHelper.navigationByData(NavigationHelper.ACTION_FORUM, forumInfoBean.getName());
        });
        forumView.setLeftString(forumInfoBean.getName());
        ImageUtil.load(forumView.getLeftIconIV(), ImageUtil.LOAD_TYPE_AVATAR, forumInfoBean.getAvatar());
    }

    public void addData(@NonNull ThreadContentBean data) {
        threadBean = data.getThread();
        dataBean = data;
        addUser(data.getUserList());
        addPic(data.getPostList());
        List<ThreadContentBean.PostListItemBean> postListItemBeans = new ArrayList<>();
        for (ThreadContentBean.PostListItemBean postListItemBean : data.getPostList()) {
            if (!needBlock(postListItemBean)) {
                postListItemBeans.add(postListItemBean);
            }
        }
        setLoadMoreData(postListItemBeans);
    }

    private void setUser(List<ThreadContentBean.UserInfoBean> userInfoBeans) {
        userInfoBeanMap = new HashMap<>();
        addUser(userInfoBeans);
    }

    private void addUser(List<ThreadContentBean.UserInfoBean> userInfoBeans) {
        for (ThreadContentBean.UserInfoBean userInfoBean : userInfoBeans) {
            if (userInfoBeanMap.get(userInfoBean.getId()) == null) {
                userInfoBeanMap.put(userInfoBean.getId(), userInfoBean);
            }
        }
    }

    private void setPic(List<ThreadContentBean.PostListItemBean> postListItemBeans) {
        photoViewBeansMap = new TreeMap<Integer, List<PhotoViewBean>>();
        addPic(postListItemBeans);
    }

    private void addPic(List<ThreadContentBean.PostListItemBean> postListItemBeans) {
        if (postListItemBeans != null) {
            for (ThreadContentBean.PostListItemBean postListItemBean : postListItemBeans) {
                List<PhotoViewBean> photoViewBeans = new ArrayList<>();
                for (ThreadContentBean.ContentBean contentBean : postListItemBean.getContent()) {
                    String url = ImageUtil.getUrl(mContext, true, contentBean.getOriginSrc(), contentBean.getBigCdnSrc(), contentBean.getCdnSrcActive(), contentBean.getCdnSrc());
                    if (TextUtils.isEmpty(url)) {
                        continue;
                    }
                    if (contentBean.getType().equals("3")) {
                        photoViewBeans.add(new PhotoViewBean(url,
                                ImageUtil.getNonNullString(contentBean.getOriginSrc(), contentBean.getBigCdnSrc(), contentBean.getCdnSrcActive(), contentBean.getCdnSrc()),
                                "1".equals(contentBean.isLongPic())));
                    }/* else if (contentBean.getType().equals("20")) {
                        photoViewBeans.add(new PhotoViewBean(contentBean.getSrc(), contentBean.getSrc(), false));
                    }
                   */
                }
                photoViewBeansMap.put(Integer.valueOf(postListItemBean.getFloor()), photoViewBeans);
            }
        }
    }

    private List<PhotoViewBean> getPhotoViewBeans() {
        List<PhotoViewBean> photoViewBeans = new ArrayList<>();
        for (int key : photoViewBeansMap.keySet()) {
            if (photoViewBeansMap.get(key) != null)
                photoViewBeans.addAll(photoViewBeansMap.get(key));
        }
        return photoViewBeans;
    }

    private View getContentView(ThreadContentBean.PostListItemBean subPostListItemBean, ThreadContentBean.PostListItemBean postListItemBean) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        ThreadContentBean.UserInfoBean userInfoBean = userInfoBeanMap.get(subPostListItemBean.getAuthorId());
        if (userInfoBean != null) {
            builder.append(userInfoBean.getNameShow(), new MyUserSpan(mContext, userInfoBean.getId()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (threadBean.getAuthor() != null && userInfoBean.getId() != null && userInfoBean.getId().equals(threadBean.getAuthor().getId())) {
                builder.append(" ");
                int start = builder.length();
                builder.append("楼主", new RoundBackgroundColorSpan(mContext,
                        alphaColor(ThemeUtils.getColorByAttr(mContext, R.attr.colorAccent), 30),
                        ThemeUtils.getColorByAttr(mContext, R.attr.colorAccent),
                        DisplayUtil.dp2px(mContext, 10)), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                //builder.setSpan(new TextAppearanceSpan("serif", Typeface.BOLD, DisplayUtil.dp2px(mContext, 10), ColorStateList.valueOf(Color.WHITE), ColorStateList.valueOf(Color.WHITE)), start, start + "楼主".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.append(" ");
            }
            builder.append(":");
        }
        if (subPostListItemBean.getContent().size() >= 1 && "10".equals(subPostListItemBean.getContent().get(0).getType())) {
            String voiceUrl = "http://c.tieba.baidu.com/c/p/voice?voice_md5=" + subPostListItemBean.getContent().get(0).getVoiceMD5() + "&play_from=pb_voice_play";
            RelativeLayout container = new RelativeLayout(mContext);
            container.setLayoutParams(defaultLayoutParams);
            container.setPadding(DisplayUtil.dp2px(mContext, 8),
                    1,
                    DisplayUtil.dp2px(mContext, 8),
                    1);
            container.setBackground(Util.getDrawableByAttr(mContext, R.attr.selectableItemBackground));
            container.setOnClickListener(view -> mContext.startActivity(new Intent(mContext, ReplyActivity.class)
                    .putExtra("data", new ReplyInfoBean(dataBean.getThread().getId(),
                            dataBean.getForum().getId(),
                            dataBean.getForum().getName(),
                            dataBean.getAnti().getTbs(),
                            postListItemBean.getId(),
                            subPostListItemBean.getId(),
                            postListItemBean.getFloor(),
                            userInfoBean != null ? userInfoBean.getNameShow() : "",
                            dataBean.getUser().getNameShow()).setPn(dataBean.getPage().getOffset()).toString())));
            container.setOnLongClickListener(v -> {
                showMenu(postListItemBean, subPostListItemBean, getAllData().indexOf(postListItemBean), postListItemBean.getSubPostList().getSubPostList().indexOf(subPostListItemBean));
                return true;
            });
            View.inflate(mContext, R.layout.layout_floor_audio, container);
            TextView mTextView = container.findViewById(R.id.floor_user);
            VoicePlayerView mVoicePlayerView = container.findViewById(R.id.floor_audio);
            mVoicePlayerView.setMini(true);
            mTextView.setText(builder);
            mVoicePlayerView.setDuration(Integer.valueOf(subPostListItemBean.getContent().get(0).getDuringTime()));
            mVoicePlayerView.setUrl(voiceUrl);
            return container;
        }
        TextView textView = createTextView(TEXT_VIEW_TYPE_FLOOR);
        textView.setLayoutParams(defaultLayoutParams);
        for (ThreadContentBean.ContentBean contentBean : subPostListItemBean.getContent()) {
            switch (contentBean.getType()) {
                case "0":
                    if (BlockUtil.needBlock(contentBean.getText()) || BlockUtil.needBlock(userInfoBean)) {
                        textView.setVisibility(View.GONE);
                    }
                    builder.append(contentBean.getText());
                    break;
                case "1":
                    builder.append(contentBean.getText(), new MyURLSpan(mContext, contentBean.getLink()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    break;
                case "2":
                    String emojiText = "#(" + contentBean.getC() + ")";
                    builder.append(emojiText);
                    break;
                case "4":
                    builder.append(contentBean.getText());
                    break;
                default:
                    break;
            }
        }
        textView.setText(StringUtil.getEmotionContent(EmotionUtil.EMOTION_ALL_TYPE, textView, builder));
        textView.setPadding(DisplayUtil.dp2px(mContext, 8),
                1,
                DisplayUtil.dp2px(mContext, 8),
                1);
        textView.setBackground(Util.getDrawableByAttr(mContext, R.attr.selectableItemBackground));
        textView.setOnClickListener(view -> mContext.startActivity(new Intent(mContext, ReplyActivity.class)
                .putExtra("data", new ReplyInfoBean(dataBean.getThread().getId(),
                        dataBean.getForum().getId(),
                        dataBean.getForum().getName(),
                        dataBean.getAnti().getTbs(),
                        postListItemBean.getId(),
                        subPostListItemBean.getId(),
                        postListItemBean.getFloor(),
                        userInfoBean != null ? userInfoBean.getNameShow() : "",
                        dataBean.getUser().getNameShow()).setPn(dataBean.getPage().getOffset()).toString())));
        textView.setOnLongClickListener(v -> {
            showMenu(postListItemBean, subPostListItemBean, getAllData().indexOf(postListItemBean), postListItemBean.getSubPostList().getSubPostList().indexOf(subPostListItemBean));
            return true;
        });
        return textView;
    }

    private void initFloorView(ViewHolder holder, ThreadContentBean.PostListItemBean bean) {
        TextView more = holder.getView(R.id.thread_list_item_content_floor_more);
        ContentLayout contentLayout = holder.getView(R.id.thread_list_item_content_floor);
        contentLayout.removeAllViews();
        if (bean.getSubPostList().getSubPostList() != null && bean.getSubPostList().getSubPostList().size() > 0) {
            holder.setVisibility(R.id.thread_list_item_content_floor_card, View.VISIBLE);
            int count = Integer.valueOf(bean.getSubPostNumber());
            List<ThreadContentBean.PostListItemBean> postListItemBeans = bean.getSubPostList().getSubPostList();
            List<ThreadContentBean.PostListItemBean> subPostList = postListItemBeans;
            List<View> views = new ArrayList<>();
            if (postListItemBeans.size() > 3) {
                subPostList = subPostList.subList(0, 3);
                holder.setVisibility(R.id.thread_list_item_content_floor_more, View.VISIBLE);
            } else {
                holder.setVisibility(R.id.thread_list_item_content_floor_more, View.GONE);
            }
            more.setText(mContext.getString(R.string.tip_floor_more_count, String.valueOf(count - subPostList.size())));
            for (ThreadContentBean.PostListItemBean postListItemBean : subPostList) {
                views.add(getContentView(postListItemBean, bean));
            }
            more.setOnClickListener(view -> {
                try {
                    if (postListItemBeans.size() < Integer.parseInt(bean.getSubPostNumber())) {
                        FloorFragment.newInstance(threadBean.getId(), bean.getSubPostList().getPid(), "", true).show(((BaseActivity) mContext).getSupportFragmentManager(), threadBean.getId() + "_Floor");
                    } else {
                        contentLayout.removeAllViews();
                        List<View> newViews = new ArrayList<>();
                        for (ThreadContentBean.PostListItemBean postListItemBean : postListItemBeans) {
                            newViews.add(getContentView(postListItemBean, bean));
                        }
                        contentLayout.addViews(newViews);
                        more.setVisibility(View.GONE);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            });
            contentLayout.addViews(views);
        } else {
            holder.setVisibility(R.id.thread_list_item_content_floor_card, View.GONE);
        }
    }

    private boolean canLoadGlide() {
        return Util.canLoadGlide(mContext);
    }

    private int getMaxWidth(String floor) {
        int maxWidth = screenWidthPx - DisplayUtil.dp2px(mContext, 28 + 38);
        if (isImmersive() || "1".equals(floor)) {
            maxWidth = screenWidthPx - DisplayUtil.dp2px(mContext, 28 + 4);
        }
        return maxWidth;
    }

    private void showMenu(ThreadContentBean.PostListItemBean postListItemBean, ThreadContentBean.PostListItemBean subPostListItemBean, int position, int subPosition) {
        ThreadContentBean.UserInfoBean userInfoBean = userInfoBeanMap.get(subPostListItemBean.getAuthorId());
        MenuDialogFragment.newInstance(R.menu.menu_thread_item, null)
                .setOnNavigationItemSelectedListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.menu_reply:
                            String replyData = new ReplyInfoBean(dataBean.getThread().getId(),
                                    dataBean.getForum().getId(),
                                    dataBean.getForum().getName(),
                                    dataBean.getAnti().getTbs(),
                                    postListItemBean.getId(),
                                    subPostListItemBean.getId(),
                                    postListItemBean.getFloor(),
                                    userInfoBean != null ? userInfoBean.getNameShow() : "",
                                    dataBean.getUser().getNameShow()).setPn(dataBean.getPage().getOffset()).toString();
                            mContext.startActivity(new Intent(mContext, ReplyActivity.class)
                                    .putExtra("data", replyData));
                            return true;
                        case R.id.menu_report:
                            navigationHelper.navigationByData(NavigationHelper.ACTION_URL, mContext.getString(R.string.url_post_report, dataBean.getForum().getId(), dataBean.getThread().getId(), subPostListItemBean.getId()));
                            return true;
                        case R.id.menu_copy:
                            StringBuilder stringBuilder = new StringBuilder();
                            for (ThreadContentBean.ContentBean contentBean : subPostListItemBean.getContent()) {
                                switch (contentBean.getType()) {
                                    case "2":
                                        contentBean.setText("#(" + contentBean.getC() + ")");
                                        break;
                                    case "3":
                                    case "20":
                                        contentBean.setText("[图片]\n");
                                        break;
                                    case "10":
                                        contentBean.setText("[语音]\n");
                                        break;
                                }
                                if (contentBean.getText() != null) {
                                    stringBuilder.append(contentBean.getText());
                                }
                            }
                            Util.showCopyDialog((BaseActivity) mContext, stringBuilder.toString(), subPostListItemBean.getId());
                            return true;
                        case R.id.menu_delete:
                            if (TextUtils.equals(AccountUtil.getUid(mContext), subPostListItemBean.getAuthorId())) {
                                ConfirmDialogFragment.newInstance(mContext.getString(R.string.title_dialog_del_post))
                                        .setOnConfirmListener(() -> {
                                            TiebaApi.getInstance()
                                                    .delPost(dataBean.getForum().getId(), dataBean.getForum().getName(), dataBean.getThread().getId(), subPostListItemBean.getId(), dataBean.getAnti().getTbs(), true, true)
                                                    .enqueue(new Callback<CommonResponse>() {
                                                        @Override
                                                        public void onResponse(@NotNull Call<CommonResponse> call, @NotNull Response<CommonResponse> response) {
                                                            Toast.makeText(mContext, R.string.toast_success, Toast.LENGTH_SHORT).show();
                                                            postListItemBean.getSubPostList().getSubPostList().remove(subPosition);
                                                            notifyItemChanged(position);
                                                        }

                                                        @Override
                                                        public void onFailure(@NotNull Call<CommonResponse> call, @NotNull Throwable t) {
                                                            Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        })
                                        .show(((BaseActivity) mContext).getSupportFragmentManager(), subPostListItemBean.getId() + "_Confirm");
                            }
                            return true;
                    }
                    return false;
                })
                .setInitMenuCallback(menu -> {
                    if (TextUtils.equals(AccountUtil.getUid(mContext), subPostListItemBean.getAuthorId())) {
                        menu.findItem(R.id.menu_delete).setVisible(true);
                    }
                })
                .show(((BaseActivity) mContext).getSupportFragmentManager(), subPostListItemBean.getId() + "_" + postListItemBean.getId() + "_Menu");
    }

    private void showMenu(ThreadContentBean.PostListItemBean postListItemBean, int position) {
        ThreadContentBean.UserInfoBean userInfoBean = userInfoBeanMap.get(postListItemBean.getAuthorId());
        MenuDialogFragment.newInstance(R.menu.menu_thread_item, null)
                .setOnNavigationItemSelectedListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.menu_reply:
                            mContext.startActivity(new Intent(mContext, ReplyActivity.class)
                                    .putExtra("data", new ReplyInfoBean(dataBean.getThread().getId(),
                                            dataBean.getForum().getId(),
                                            dataBean.getForum().getName(),
                                            dataBean.getAnti().getTbs(),
                                            postListItemBean.getId(),
                                            postListItemBean.getFloor(),
                                            userInfoBean != null ? userInfoBean.getNameShow() : "",
                                            dataBean.getUser().getNameShow()).setPn(dataBean.getPage().getOffset()).toString()));
                            return true;
                        case R.id.menu_report:
                            navigationHelper.navigationByData(NavigationHelper.ACTION_URL, mContext.getString(R.string.url_post_report, dataBean.getForum().getId(), dataBean.getThread().getId(), postListItemBean.getId()));
                            return true;
                        case R.id.menu_copy:
                            StringBuilder stringBuilder = new StringBuilder();
                            for (ThreadContentBean.ContentBean contentBean : postListItemBean.getContent()) {
                                switch (contentBean.getType()) {
                                    case "2":
                                        contentBean.setText("#(" + contentBean.getC() + ")");
                                        break;
                                    case "3":
                                    case "20":
                                        contentBean.setText("[图片]\n");
                                        break;
                                    case "10":
                                        contentBean.setText("[语音]\n");
                                        break;
                                }
                                if (contentBean.getText() != null) {
                                    stringBuilder.append(contentBean.getText());
                                }
                            }
                            Util.showCopyDialog((BaseActivity) mContext, stringBuilder.toString(), postListItemBean.getId());
                            return true;
                        case R.id.menu_delete:
                            if (TextUtils.equals(dataBean.getUser().getId(), postListItemBean.getAuthorId()) || TextUtils.equals(dataBean.getUser().getId(), dataBean.getThread().getAuthor().getId())) {
                                ConfirmDialogFragment.newInstance(mContext.getString(R.string.title_dialog_del_post))
                                        .setOnConfirmListener(() -> {
                                            TiebaApi.getInstance()
                                                    .delPost(dataBean.getForum().getId(), dataBean.getForum().getName(), dataBean.getThread().getId(), postListItemBean.getId(), dataBean.getAnti().getTbs(), TextUtils.equals(dataBean.getUser().getId(), postListItemBean.getAuthorId()), false)
                                                    .enqueue(new Callback<CommonResponse>() {
                                                        @Override
                                                        public void onResponse(@NotNull Call<CommonResponse> call, @NotNull Response<CommonResponse> response) {
                                                            Toast.makeText(mContext, R.string.toast_success, Toast.LENGTH_SHORT).show();
                                                            remove(position);
                                                        }

                                                        @Override
                                                        public void onFailure(@NotNull Call<CommonResponse> call, @NotNull Throwable t) {
                                                            Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        })
                                        .show(((BaseActivity) mContext).getSupportFragmentManager(), postListItemBean.getId() + "_Delete_Confirm");
                            }
                            return true;
                    }
                    return false;
                })
                .setInitMenuCallback(menu -> {
                    if (TextUtils.equals(dataBean.getUser().getId(), postListItemBean.getAuthorId()) || TextUtils.equals(dataBean.getUser().getId(), dataBean.getThread().getAuthor().getId())) {
                        menu.findItem(R.id.menu_delete).setVisible(true);
                    }
                })
                .show(((BaseActivity) mContext).getSupportFragmentManager(), postListItemBean.getId() + "_Menu");
    }

    @Override
    protected void convert(ViewHolder holder, ThreadContentBean.PostListItemBean data, int position, int type) {
        if (type == TYPE_THREAD) {
            SuperTextView forumView = holder.getView(R.id.forum_bar);
            View dividerView = holder.getView(R.id.forum_bar_divider);
            refreshForumView(dataBean.getForum(), forumView, dividerView);
        }
        ThreadContentBean.UserInfoBean userInfoBean = userInfoBeanMap.get(data.getAuthorId());
        if (dataBean != null && dataBean.getThread() != null && dataBean.getThread().getAuthor() != null && data.getAuthorId().equals(dataBean.getThread().getAuthor().getId())) {
            holder.setVisibility(R.id.thread_list_item_user_lz_tip, View.VISIBLE);
        } else {
            holder.setVisibility(R.id.thread_list_item_user_lz_tip, View.GONE);
        }
        holder.getConvertView().setOnLongClickListener(view -> {
            showMenu(data, position);
            return true;
        });
        holder.setText(R.id.thread_list_item_user_name, userInfoBean == null ? data.getAuthorId() : StringUtil.getUsernameString(mContext, userInfoBean.getName(), userInfoBean.getNameShow()));
        holder.setText(R.id.thread_list_item_user_time, mContext.getString(R.string.tip_thread_item, data.getFloor(), String.valueOf(DateUtils.getRelativeTimeSpanString(Long.valueOf(data.getTime()) * 1000L))));
        holder.setText(R.id.thread_list_item_content_title, data.getTitle());
        holder.setOnClickListener(R.id.thread_list_item_reply, view -> showMenu(data, position));
        if (data.getFloor().equals("1"))
            holder.setVisibility(R.id.thread_list_item_reply, View.GONE);
        else
            holder.setVisibility(R.id.thread_list_item_reply, View.VISIBLE);
        if (userInfoBean != null) {
            String levelId = userInfoBean.getLevelId() == null || TextUtils.isEmpty(userInfoBean.getLevelId()) ? "?" : userInfoBean.getLevelId();
            ThemeUtil.setChipThemeByLevel(levelId,
                    holder.getView(R.id.thread_list_item_user_status),
                    holder.getView(R.id.thread_list_item_user_level),
                    holder.getView(R.id.thread_list_item_user_lz_tip));
            holder.setText(R.id.thread_list_item_user_level, levelId);
            holder.setOnClickListener(R.id.thread_list_item_user_avatar, view -> {
                NavigationHelper.toUserSpaceWithAnim(mContext, userInfoBean.getId(), StringUtil.getAvatarUrl(userInfoBean.getPortrait()), view);
            });
            ImageUtil.load(holder.getView(R.id.thread_list_item_user_avatar), ImageUtil.LOAD_TYPE_AVATAR, userInfoBean.getPortrait());
        }
        if (!data.getFloor().equals("1"))
            holder.setVisibility(R.id.thread_list_item_content_title, View.GONE);
        else
            holder.setVisibility(R.id.thread_list_item_content_title, View.VISIBLE);
        initContentView(holder, data);
        initFloorView(holder, data);
        if (isImmersive()) {
            holder.getView(R.id.thread_list_item_content).setPadding(DisplayUtil.dp2px(mContext, 4), 0, DisplayUtil.dp2px(mContext, 4), 0);
            holder.setVisibility(R.id.thread_list_item_user, View.GONE);
            holder.setVisibility(R.id.thread_list_item_content_floor_card, View.GONE);
        } else {
            if (type == TYPE_THREAD) {
                holder.getView(R.id.thread_list_item_content).setPadding(DisplayUtil.dp2px(mContext, 4), 0, DisplayUtil.dp2px(mContext, 4), 0);
            } else {
                if (holder.getView(R.id.thread_list_item_content).getLayoutDirection() == View.LAYOUT_DIRECTION_LTR) {
                    holder.getView(R.id.thread_list_item_content).setPadding(DisplayUtil.dp2px(mContext, 38), 0, DisplayUtil.dp2px(mContext, 4), 0);
                } else {
                    holder.getView(R.id.thread_list_item_content).setPadding(DisplayUtil.dp2px(mContext, 4), 0, DisplayUtil.dp2px(mContext, 38), 0);
                }
            }
            holder.setVisibility(R.id.thread_list_item_user, View.VISIBLE);
        }
    }

    @Override
    protected int getItemLayoutId(int type) {
        return type == TYPE_THREAD ? R.layout.item_thread_list_post : R.layout.item_thread_list;
    }

    private boolean appendTextToLastTextView(List<View> views, CharSequence newContent) {
        if (views.size() > 0) {
            View lastView = views.get(views.size() - 1);
            if (lastView instanceof TextView) {
                TextView lastTextView = (TextView) lastView;
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(lastTextView.getText());
                spannableStringBuilder.append(newContent);
                setText(lastTextView, spannableStringBuilder);
                return false;
            }
        }
        return true;
    }

    private boolean appendLinkToLastTextView(List<View> views, CharSequence newContent, String url) {
        if (views.size() > 0) {
            View lastView = views.get(views.size() - 1);
            if (lastView instanceof TextView) {
                TextView lastTextView = (TextView) lastView;
                setText(lastTextView, getLinkContent(lastTextView.getText(), newContent, url));
                return false;
            }
        }
        return true;
    }

    private CharSequence getLinkContent(CharSequence newContent, String url) {
        return getLinkContent("", newContent, url);
    }

    private CharSequence getLinkContent(CharSequence oldContent, CharSequence newContent, String url) {
        String linkIconText = "[链接]";
        String s = " ";
        int start = oldContent.length();
        int end = start + s.length() + linkIconText.length() + newContent.length();
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(oldContent);
        Bitmap bitmap = Util.getBitmapFromVectorDrawable(mContext, R.drawable.ic_link);
        int size = DisplayUtil.sp2px(mContext, 16);
        int color = ThemeUtils.getColorByAttr(mContext, R.attr.colorAccent);
        bitmap = Bitmap.createScaledBitmap(bitmap, size, size, true);
        bitmap = Util.tintBitmap(bitmap, color);
        spannableStringBuilder.append(linkIconText, new MyImageSpan(mContext, bitmap), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append(s);
        spannableStringBuilder.append(newContent);
        spannableStringBuilder.setSpan(new MyURLSpan(mContext, url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableStringBuilder;
    }

    private boolean appendUserToLastTextView(List<View> views, CharSequence newContent, String uid) {
        if (views.size() > 0) {
            View lastView = views.get(views.size() - 1);
            if (lastView instanceof TextView) {
                TextView lastTextView = (TextView) lastView;
                setText(lastTextView, getUserContent(lastTextView.getText(), newContent, uid));
                return false;
            }
        }
        return true;
    }

    private CharSequence getUserContent(CharSequence newContent, String uid) {
        return getUserContent("", newContent, uid);
    }

    private CharSequence getUserContent(CharSequence oldContent, CharSequence newContent, String uid) {
        int start = oldContent.length();
        int end = start + newContent.length();
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(oldContent);
        spannableStringBuilder.append(newContent);
        spannableStringBuilder.setSpan(new MyUserSpan(mContext, uid), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableStringBuilder;
    }

    private TextView createTextView(int type) {
        TintTextView textView = new TintTextView(mContext);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setClickable(false);
        textView.setFocusable(false);
        textView.setFocusableInTouchMode(false);
        textView.setTextIsSelectable(false);
        textView.setOnClickListener(null);
        textView.setOnLongClickListener(null);
        textView.setTintResId(R.color.default_color_text);
        textView.setLetterSpacing(0.02F);
        if (type == TEXT_VIEW_TYPE_CONTENT) {
            textView.setTextSize(16);
            if (isImmersive()) {
                textView.setLineSpacing(0.5F, 1.3F);
            } else {
                textView.setLineSpacing(0.5F, 1.2F);
            }
        }
        return textView;
    }

    private void setText(TextView textView, CharSequence content) {
        textView.setText(StringUtil.getEmotionContent(EmotionUtil.EMOTION_ALL_TYPE, textView, content));
    }

    private LinearLayout.LayoutParams getLayoutParams(ThreadContentBean.ContentBean contentBean, String floor) {
        if (!contentBean.getType().equals("3") && !contentBean.getType().equals("20") && !contentBean.getType().equals("5")) {
            return defaultLayoutParams;
        }
        float widthFloat, heightFloat;
        if (contentBean.getType().equals("3") || contentBean.getType().equals("20")) {
            String[] strings = contentBean.getBsize().split(",");
            widthFloat = Float.valueOf(strings[0]);
            heightFloat = Float.valueOf(strings[1]);
            heightFloat *= getMaxWidth(floor) / widthFloat;
            widthFloat = getMaxWidth(floor);
        } else {
            float width = Float.valueOf(contentBean.getWidth());
            widthFloat = getMaxWidth(floor);
            heightFloat = Float.valueOf(contentBean.getHeight());
            heightFloat *= widthFloat / width;
        }
        int width = Math.round(widthFloat);
        int height = Math.round(heightFloat);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        int dp16 = DisplayUtil.dp2px(mContext, 16);
        int dp4 = DisplayUtil.dp2px(mContext, 4);
        int dp2 = DisplayUtil.dp2px(mContext, 2);
        if ("1".equals(floor)) {
            layoutParams.setMargins(dp16, dp2, dp16, dp2);
        } else {
            layoutParams.setMargins(dp4, dp2, dp4, dp2);
        }
        return layoutParams;
    }

    private boolean needBlock(ThreadContentBean.PostListItemBean postListItemBean) {
        if (blockCacheMap != null && blockCacheMap.get(postListItemBean.getFloor()) != null) {
            return blockCacheMap.get(postListItemBean.getFloor());
        }
        if (postListItemBean.getAuthor() != null && BlockUtil.needBlock(postListItemBean.getAuthor())) {
            blockCacheMap.put(postListItemBean.getFloor(), true);
            return true;
        }
        ThreadContentBean.UserInfoBean userInfoBean = userInfoBeanMap.get(postListItemBean.getAuthorId());
        if (userInfoBean != null && BlockUtil.needBlock(userInfoBean.getName(), userInfoBean.getId())) {
            blockCacheMap.put(postListItemBean.getFloor(), true);
            return true;
        }
        for (ThreadContentBean.ContentBean contentBean : postListItemBean.getContent()) {
            switch (contentBean.getType()) {
                case "0":
                    if (BlockUtil.needBlock(contentBean.getText())) {
                        blockCacheMap.put(postListItemBean.getFloor(), true);
                        return true;
                    }
                    break;
            }
        }
        blockCacheMap.put(postListItemBean.getFloor(), false);
        return false;
    }

    private List<View> getContentViews(ThreadContentBean.PostListItemBean postListItemBean) {
        List<View> views = new ArrayList<>();
        for (ThreadContentBean.ContentBean contentBean : postListItemBean.getContent()) {
            switch (contentBean.getType()) {
                case "0": {
                    if (appendTextToLastTextView(views, contentBean.getText())) {
                        TextView textView = createTextView(TEXT_VIEW_TYPE_CONTENT);
                        textView.setLayoutParams(getLayoutParams(contentBean, postListItemBean.getFloor()));
                        setText(textView, contentBean.getText());
                        views.add(textView);
                    }
                }
                break;
                case "1":
                    if (appendLinkToLastTextView(views, contentBean.getText(), contentBean.getLink())) {
                        TextView textView = createTextView(TEXT_VIEW_TYPE_CONTENT);
                        textView.setLayoutParams(getLayoutParams(contentBean, postListItemBean.getFloor()));
                        setText(textView, getLinkContent(contentBean.getText(), contentBean.getLink()));
                        views.add(textView);
                    }
                    break;
                case "2":
                    String emojiText = "#(" + contentBean.getC() + ")";
                    if (appendTextToLastTextView(views, emojiText)) {
                        TextView textView = createTextView(TEXT_VIEW_TYPE_CONTENT);
                        textView.setLayoutParams(getLayoutParams(contentBean, postListItemBean.getFloor()));
                        setText(textView, emojiText);
                        views.add(textView);
                    }
                    break;
                case "3":
                    String url = ImageUtil.getUrl(mContext, true, contentBean.getOriginSrc(), contentBean.getBigCdnSrc(), contentBean.getCdnSrcActive(), contentBean.getCdnSrc());
                    if (TextUtils.isEmpty(url)) {
                        break;
                    }
                    MyImageView imageView = new MyImageView(mContext);
                    imageView.setLayoutParams(getLayoutParams(contentBean, postListItemBean.getFloor()));
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    ImageUtil.load(imageView, ImageUtil.LOAD_TYPE_SMALL_PIC, url);
                    List<PhotoViewBean> photoViewBeans = getPhotoViewBeans();
                    for (PhotoViewBean photoViewBean : photoViewBeans) {
                        if (TextUtils.equals(photoViewBean.getOriginUrl(), contentBean.getOriginSrc())) {
                            ImageUtil.initImageView(imageView,
                                    photoViewBeans,
                                    photoViewBeans.indexOf(photoViewBean),
                                    dataBean.getForum().getName(),
                                    dataBean.getForum().getId(),
                                    dataBean.getThread().getId(),
                                    isSeeLz(),
                                    OBJ_TYPE_THREAD_PAGE);
                            break;
                        }
                    }
                    views.add(imageView);
                    break;
                case "4":
                    if (appendUserToLastTextView(views, contentBean.getText(), contentBean.getUid())) {
                        TextView textView = createTextView(TEXT_VIEW_TYPE_CONTENT);
                        textView.setLayoutParams(getLayoutParams(contentBean, postListItemBean.getFloor()));
                        setText(textView, getUserContent(contentBean.getText(), contentBean.getUid()));
                        views.add(textView);
                    }
                    break;
                case "5":
                    if (contentBean.getSrc() != null && contentBean.getWidth() != null && contentBean.getHeight() != null) {
                        if (contentBean.getLink() != null) {
                            VideoPlayerStandard videoPlayerStandard = new VideoPlayerStandard(mContext);
                            videoPlayerStandard.setUp(contentBean.getLink(), "");
                            videoPlayerStandard.setLayoutParams(getLayoutParams(contentBean, postListItemBean.getFloor()));
                            videoPlayerStandard.setId(R.id.video_player);
                            ImageUtil.load(videoPlayerStandard.posterImageView, ImageUtil.LOAD_TYPE_SMALL_PIC, contentBean.getSrc(), true);
                            views.add(videoPlayerStandard);
                        } else {
                            MyImageView videoImageView = new MyImageView(mContext);
                            videoImageView.setLayoutParams(getLayoutParams(contentBean, postListItemBean.getFloor()));
                            videoImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            ImageUtil.load(videoImageView, ImageUtil.LOAD_TYPE_SMALL_PIC, contentBean.getSrc(), true);
                            videoImageView.setOnClickListener(view -> {
                                navigationHelper.navigationByData(NavigationHelper.ACTION_URL, contentBean.getText());
                            });
                            views.add(videoImageView);
                        }
                    } else {
                        if (appendLinkToLastTextView(views, "[视频] " + contentBean.getText(), contentBean.getText())) {
                            TextView textView = createTextView(TEXT_VIEW_TYPE_CONTENT);
                            textView.setLayoutParams(defaultLayoutParams);
                            setText(textView, getLinkContent("[视频] " + contentBean.getText(), contentBean.getText()));
                            views.add(textView);
                        }
                    }
                    break;
                case "10":
                    String voiceUrl = "http://c.tieba.baidu.com/c/p/voice?voice_md5=" + contentBean.getVoiceMD5() + "&play_from=pb_voice_play";
                    Log.i(TAG, "getContentViews: " + contentBean.getDuringTime());
                    VoicePlayerView voicePlayerView = new VoicePlayerView(mContext);
                    voicePlayerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    //voicePlayerView.setMini(false);
                    voicePlayerView.setDuration(Integer.valueOf(contentBean.getDuringTime()));
                    voicePlayerView.setUrl(voiceUrl);
                    views.add(voicePlayerView);
                    break;
                case "20":
                    MyImageView memeImageView = new MyImageView(mContext);
                    memeImageView.setLayoutParams(getLayoutParams(contentBean, postListItemBean.getFloor()));
                    memeImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    ImageUtil.load(memeImageView, ImageUtil.LOAD_TYPE_SMALL_PIC, contentBean.getSrc());
                    ImageUtil.initImageView(memeImageView, new PhotoViewBean(contentBean.getSrc(), contentBean.getSrc(), false));
                    views.add(memeImageView);
                    break;
                default:
                    break;
            }
        }
        return views;
    }

    private void initContentView(ViewHolder viewHolder, ThreadContentBean.PostListItemBean postListItemBean) {
        ContentLayout contentLayout = viewHolder.getView(R.id.thread_list_item_content_content);
        contentLayout.removeAllViews();
        contentLayout.addViews(getContentViews(postListItemBean));
    }

    @Override
    protected int getViewType(int i, ThreadContentBean.PostListItemBean postListItemBean) {
        if ("1".equals(postListItemBean.getFloor())) {
            return TYPE_THREAD;
        }
        return TYPE_REPLY;
    }
}