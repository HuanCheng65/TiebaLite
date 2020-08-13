package com.huanchengfly.tieba.post.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.request.RequestOptions;
import com.huanchengfly.tieba.api.TiebaApi;
import com.huanchengfly.tieba.api.models.CommonResponse;
import com.huanchengfly.tieba.api.models.SubFloorListBean;
import com.huanchengfly.tieba.api.models.ThreadContentBean;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.activities.ReplyActivity;
import com.huanchengfly.tieba.post.activities.base.BaseActivity;
import com.huanchengfly.tieba.post.components.spans.MyURLSpan;
import com.huanchengfly.tieba.post.components.spans.MyUserSpan;
import com.huanchengfly.tieba.post.fragments.ConfirmDialogFragment;
import com.huanchengfly.tieba.post.fragments.MenuDialogFragment;
import com.huanchengfly.tieba.post.models.PhotoViewBean;
import com.huanchengfly.tieba.post.models.ReplyInfoBean;
import com.huanchengfly.tieba.post.utils.*;
import com.huanchengfly.tieba.widgets.ContentLayout;
import com.huanchengfly.tieba.widgets.VoicePlayerView;
import com.huanchengfly.tieba.widgets.theme.TintTextView;
import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.base.CommonBaseAdapter;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class RecyclerFloorAdapter extends CommonBaseAdapter<SubFloorListBean.PostInfo> {
    public static final String TAG = "RecyclerFloorAdapter";
    private static final int TEXT_VIEW_TYPE_CONTENT = 0;
    private NavigationHelper navigationHelper;
    private RequestOptions avatarRequestOptions;
    private LinearLayout.LayoutParams defaultLayoutParams;
    private RequestOptions defaultRequestOptions;
    private Float maxWidth;
    private SubFloorListBean dataBean;

    public RecyclerFloorAdapter(Context context) {
        super(context, null, true);
        setOnItemClickListener((viewHolder, postInfo, position) -> {
            int floor = Integer.parseInt(dataBean.getPost().getFloor());
            int pn = floor - (floor % 30);
            ThreadContentBean.UserInfoBean userInfoBean = postInfo.getAuthor();
            mContext.startActivity(new Intent(mContext, ReplyActivity.class)
                    .putExtra("data", new ReplyInfoBean(dataBean.getThread().getId(),
                            dataBean.getForum().getId(),
                            dataBean.getForum().getName(),
                            dataBean.getAnti().getTbs(),
                            dataBean.getPost().getId(),
                            postInfo.getId(),
                            dataBean.getPost().getFloor(),
                            userInfoBean != null ? userInfoBean.getNameShow() : "",
                            AccountUtil.getLoginInfo(mContext).getNameShow()).setPn(String.valueOf(pn)).toString()));
        });
        avatarRequestOptions = new RequestOptions()
                .placeholder(R.drawable.bg_placeholder_circle)
                .circleCrop()
                .skipMemoryCache(true);
        navigationHelper = NavigationHelper.newInstance(mContext);
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        maxWidth = (float) dm.widthPixels;
        defaultLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        defaultLayoutParams.setMargins(0, 8, 0, 8);
        defaultRequestOptions = new RequestOptions()
                .placeholder(R.drawable.bg_placeholder)
                .skipMemoryCache(true);
    }

    public void setData(SubFloorListBean data) {
        dataBean = data;
        data.getSubPostList().add(0, data.getPost());
        setNewData(data.getSubPostList());
    }

    public void addData(SubFloorListBean data) {
        dataBean = data;
        setLoadMoreData(data.getSubPostList());
    }

    private void showMenu(SubFloorListBean.PostInfo postInfo, int position) {
        ThreadContentBean.UserInfoBean userInfoBean = postInfo.getAuthor();
        MenuDialogFragment.newInstance(R.menu.menu_thread_item, null)
                .setOnNavigationItemSelectedListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.menu_reply:
                            int floor = Integer.parseInt(dataBean.getPost().getFloor());
                            int pn = floor - (floor % 30);
                            String replyData = new ReplyInfoBean(dataBean.getThread().getId(),
                                    dataBean.getForum().getId(),
                                    dataBean.getForum().getName(),
                                    dataBean.getAnti().getTbs(),
                                    dataBean.getPost().getId(),
                                    postInfo.getId(),
                                    dataBean.getPost().getFloor(),
                                    userInfoBean != null ? userInfoBean.getNameShow() : "",
                                    AccountUtil.getLoginInfo(mContext).getNameShow()).setPn(String.valueOf(pn)).toString();
                            Log.i(TAG, "convert: " + replyData);
                            mContext.startActivity(new Intent(mContext, ReplyActivity.class)
                                    .putExtra("data", replyData));
                            return true;
                        case R.id.menu_report:
                            navigationHelper.navigationByData(NavigationHelper.ACTION_URL, mContext.getString(R.string.url_post_report, dataBean.getForum().getId(), dataBean.getThread().getId(), postInfo.getId()));
                            return true;
                        case R.id.menu_copy:
                            StringBuilder stringBuilder = new StringBuilder();
                            for (ThreadContentBean.ContentBean contentBean : postInfo.getContent()) {
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
                            Util.showCopyDialog((BaseActivity) mContext, stringBuilder.toString(), postInfo.getId());
                            return true;
                        case R.id.menu_delete:
                            if (TextUtils.equals(AccountUtil.getLoginInfo(mContext).getUid(), postInfo.getAuthor().getId())) {
                                ConfirmDialogFragment.newInstance(mContext.getString(R.string.title_dialog_del_post))
                                        .setOnConfirmListener(() -> {
                                            TiebaApi.getInstance()
                                                    .delPost(dataBean.getForum().getId(), dataBean.getForum().getName(), dataBean.getThread().getId(), postInfo.getId(), dataBean.getAnti().getTbs(), true, true)
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
                                        .show(((BaseActivity) mContext).getSupportFragmentManager(), postInfo.getId() + "_Confirm");
                            }
                            return true;
                    }
                    return false;
                })
                .setInitMenuCallback(menu -> {
                    if (TextUtils.equals(AccountUtil.getLoginInfo(mContext).getUid(), postInfo.getAuthor().getId())) {
                        menu.findItem(R.id.menu_delete).setVisible(true);
                    }
                })
                .show(((BaseActivity) mContext).getSupportFragmentManager(), postInfo.getId() + "_Menu");
    }

    @Override
    protected void convert(ViewHolder holder, SubFloorListBean.PostInfo data, int position) {
        ThreadContentBean.UserInfoBean userInfoBean = data.getAuthor();
        if (dataBean != null && dataBean.getThread() != null && dataBean.getThread().getAuthor() != null && userInfoBean != null && userInfoBean.getId() != null && userInfoBean.getId().equals(dataBean.getThread().getAuthor().getId())) {
            holder.setVisibility(R.id.thread_list_item_user_lz_tip, View.VISIBLE);
        } else {
            holder.setVisibility(R.id.thread_list_item_user_lz_tip, View.GONE);
        }
        holder.getConvertView().setOnLongClickListener(v -> {
            showMenu(data, position);
            return true;
        });
        holder.setOnClickListener(R.id.thread_list_item_reply, view -> showMenu(data, position));
        holder.setText(R.id.thread_list_item_user_name, userInfoBean == null ? "" : StringUtil.getUsernameString(mContext, userInfoBean.getName(), userInfoBean.getNameShow()));
        holder.setText(R.id.thread_list_item_user_time, String.valueOf(DateUtils.getRelativeTimeSpanString(Long.valueOf(data.getTime()) * 1000L)));
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
        holder.setVisibility(R.id.thread_list_item_content_title, View.GONE);
        initContentView(holder, data);
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_thread_list;
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
        if (type == TEXT_VIEW_TYPE_CONTENT) {
            textView.setTextSize(16);
        }
        return textView;
    }

    private void setText(TextView textView, CharSequence content) {
        textView.setText(StringUtil.getEmotionContent(EmotionUtil.EMOTION_ALL_TYPE, textView, content));
    }

    private LinearLayout.LayoutParams getLayoutParams(ThreadContentBean.ContentBean contentBean) {
        if (!contentBean.getType().equals("3") && !contentBean.getType().equals("5")) {
            return defaultLayoutParams;
        }
        float widthFloat, heightFloat;
        if (contentBean.getType().equals("3") || contentBean.getType().equals("20")) {
            String[] strings = contentBean.getBsize().split(",");
            widthFloat = Float.parseFloat(strings[0]);
            heightFloat = Float.parseFloat(strings[1]);
            heightFloat *= this.maxWidth / widthFloat;
            widthFloat = this.maxWidth;
        } else {
            float width = Float.parseFloat(contentBean.getWidth());
            widthFloat = this.maxWidth;
            heightFloat = Float.parseFloat(contentBean.getHeight());
            heightFloat *= widthFloat / width;
        }
        int width = Math.round(widthFloat);
        int height = Math.round(heightFloat);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        layoutParams.setMargins(0, 8, 0, 8);
        return layoutParams;
    }

    private CharSequence getLinkContent(CharSequence newContent, String url) {
        return getLinkContent("", newContent, url);
    }

    private CharSequence getLinkContent(CharSequence oldContent, CharSequence newContent, String url) {
        int start = oldContent.length();
        int end = start + newContent.length();
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(oldContent);
        spannableStringBuilder.append(newContent);
        spannableStringBuilder.setSpan(new MyURLSpan(mContext, url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableStringBuilder;
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

    private boolean canLoadGlide() {
        if (mContext instanceof Activity) {
            return !((Activity) mContext).isDestroyed();
        }
        return false;
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

    private List<View> getContentViews(SubFloorListBean.PostInfo postListItemBean) {
        List<View> views = new ArrayList<>();
        for (ThreadContentBean.ContentBean contentBean : postListItemBean.getContent()) {
            switch (contentBean.getType()) {
                case "0": {
                    if (appendTextToLastTextView(views, contentBean.getText())) {
                        TextView textView = createTextView(TEXT_VIEW_TYPE_CONTENT);
                        textView.setLayoutParams(getLayoutParams(contentBean));
                        setText(textView, contentBean.getText());
                        views.add(textView);
                    }
                }
                break;
                case "1":
                    if (appendLinkToLastTextView(views, contentBean.getText(), contentBean.getLink())) {
                        TextView textView = createTextView(TEXT_VIEW_TYPE_CONTENT);
                        textView.setLayoutParams(getLayoutParams(contentBean));
                        setText(textView, getLinkContent(contentBean.getText(), contentBean.getLink()));
                        views.add(textView);
                    }
                    break;
                case "2":
                    String emojiText = "#(" + contentBean.getC() + ")";
                    if (appendTextToLastTextView(views, emojiText)) {
                        TextView textView = createTextView(TEXT_VIEW_TYPE_CONTENT);
                        textView.setLayoutParams(getLayoutParams(contentBean));
                        setText(textView, emojiText);
                        views.add(textView);
                    }
                    break;
                case "3":
                    ImageView imageView = new ImageView(mContext);
                    imageView.setLayoutParams(getLayoutParams(contentBean));
                    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    ImageUtil.load(imageView, ImageUtil.LOAD_TYPE_SMALL_PIC, contentBean.getSrc());
                    List<PhotoViewBean> photoViewBeans = new ArrayList<>();
                    photoViewBeans.add(new PhotoViewBean(ImageUtil.getNonNullString(contentBean.getSrc(), contentBean.getOriginSrc()),
                            ImageUtil.getNonNullString(contentBean.getOriginSrc(), contentBean.getSrc()),
                            "1".equals(contentBean.isLongPic())));
                    ImageUtil.initImageView(imageView, photoViewBeans, 0);
                    views.add(imageView);
                    break;
                case "4":
                    if (appendUserToLastTextView(views, contentBean.getText(), contentBean.getUid())) {
                        TextView textView = createTextView(TEXT_VIEW_TYPE_CONTENT);
                        textView.setLayoutParams(getLayoutParams(contentBean));
                        setText(textView, getUserContent(contentBean.getText(), contentBean.getUid()));
                        views.add(textView);
                    }
                    break;
                case "10":
                    String voiceUrl = "http://c.tieba.baidu.com/c/p/voice?voice_md5=" + contentBean.getVoiceMD5() + "&play_from=pb_voice_play";
                    VoicePlayerView voicePlayerView = new VoicePlayerView(mContext);
                    voicePlayerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    //voicePlayerView.setMini(false);
                    voicePlayerView.setDuration(Integer.valueOf(contentBean.getDuringTime()));
                    voicePlayerView.setUrl(voiceUrl);
                    views.add(voicePlayerView);
                    break;
            }
        }
        return views;
    }

    private void initContentView(ViewHolder viewHolder, SubFloorListBean.PostInfo postListItemBean) {
        ContentLayout contentLayout = viewHolder.getView(R.id.thread_list_item_content_content);
        contentLayout.removeAllViews();
        contentLayout.addViews(getContentViews(postListItemBean));
    }
}