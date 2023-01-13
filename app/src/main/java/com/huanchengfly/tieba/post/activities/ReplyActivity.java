package com.huanchengfly.tieba.post.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.adapters.InsertPhotoAdapter;
import com.huanchengfly.tieba.post.adapters.TabViewPagerAdapter;
import com.huanchengfly.tieba.post.adapters.TextWatcherAdapter;
import com.huanchengfly.tieba.post.api.TiebaApi;
import com.huanchengfly.tieba.post.api.interfaces.CommonCallback;
import com.huanchengfly.tieba.post.api.models.WebReplyResultBean;
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaException;
import com.huanchengfly.tieba.post.components.EmotionViewFactory;
import com.huanchengfly.tieba.post.components.dialogs.LoadingDialog;
import com.huanchengfly.tieba.post.interfaces.ReplyContentCallback;
import com.huanchengfly.tieba.post.interfaces.UploadCallback;
import com.huanchengfly.tieba.post.models.PhotoInfoBean;
import com.huanchengfly.tieba.post.models.ReplyInfoBean;
import com.huanchengfly.tieba.post.models.database.Draft;
import com.huanchengfly.tieba.post.utils.AssetUtil;
import com.huanchengfly.tieba.post.utils.EmotionUtil;
import com.huanchengfly.tieba.post.utils.GsonUtil;
import com.huanchengfly.tieba.post.utils.StringUtil;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.huanchengfly.tieba.post.utils.UploadHelper;
import com.huanchengfly.tieba.post.utils.Util;
import com.huanchengfly.tieba.post.widgets.edittext.widget.UndoableEditText;
import com.huanchengfly.tieba.post.widgets.theme.TintConstraintLayout;
import com.huanchengfly.tieba.post.widgets.theme.TintImageView;
import com.zhihu.matisse.Matisse;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import cn.dreamtobe.kpswitch.util.KPSwitchConflictUtil;
import cn.dreamtobe.kpswitch.util.KeyboardUtil;
import cn.dreamtobe.kpswitch.widget.KPSwitchFSPanelFrameLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReplyActivity extends BaseActivity implements View.OnClickListener {
    public static final int REQUEST_CODE_CHOOSE = 2;

    public static final String TAG = "ReplyActivity";

    @BindView(R.id.activity_reply_edit_text)
    UndoableEditText editText;
    @BindView(R.id.activity_reply_panel_root)
    KPSwitchFSPanelFrameLayout panelFrameLayout;
    @BindView(R.id.activity_reply_emotion)
    RelativeLayout emotionView;
    @BindView(R.id.activity_reply_insert_photo)
    FrameLayout insertImageView;
    @BindView(R.id.activity_reply_edit_emotion)
    TintImageView emotionBtn;
    @BindView(R.id.activity_reply_edit_insert_photo)
    TintImageView insertImageBtn;
    @BindView(R.id.activity_reply_emotion_view_pager)
    ViewPager emotionViewPager;
    @BindView(R.id.activity_reply_insert_photo_view)
    RecyclerView insertView;
    @BindView(R.id.webview_container)
    FrameLayout webViewContainer;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private ReplyInfoBean replyInfoBean;
    private LoadingDialog loadingDialog;
    private InsertPhotoAdapter insertPhotoAdapter;
    private Callback<WebReplyResultBean> mCallback;
    private MenuItem sendItem;
    private boolean replySuccess;
    private String content;

    private WebView mWebView;

    @Override
    public boolean isNeedImmersionBar() {
        return false;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_reply;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSwipeBackEnable(false);
        if (ThemeUtil.THEME_TRANSLUCENT.equals(ThemeUtil.getTheme(this))) {
            TintConstraintLayout constraintLayout = (TintConstraintLayout) findViewById(R.id.activity_reply_layout);
            constraintLayout.setBackgroundTintResId(0);
            ThemeUtil.setTranslucentBackground(constraintLayout);
        }
        Util.setStatusBarTransparent(this);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.transparent));
        getWindow().setBackgroundDrawableResource(R.drawable.bg_trans);
        initData();
        initView();
    }

    private void destroyWebView() {
        if (mWebView != null) {
            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.removeAllViews();
            mWebView.destroy();
            mWebView = null;
        }
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            destroyWebView();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        panelFrameLayout.recordKeyboardStatus(getWindow());
        if (replyInfoBean != null && !replySuccess) {
            new Draft(replyInfoBean.hash(), editText.getText() == null ? "" : editText.getText().toString())
                    .saveOrUpdate("hash = ?", replyInfoBean.hash());
        } else if (replySuccess) {
            Draft draft = LitePal.where("hash = ?", replyInfoBean.hash())
                    .findFirst(Draft.class);
            if (draft != null) {
                draft.delete();
            }
        }
    }

    private void initData() {
        Intent intent = getIntent();
        String jsonData = intent.getStringExtra("data");
        replyInfoBean = GsonUtil.getGson().fromJson(jsonData, ReplyInfoBean.class);
        Draft draft = LitePal.where("hash = ?", replyInfoBean.hash())
                .findFirst(Draft.class);
        if (draft != null) {
            content = draft.getContent();
        }
    }

    protected void initView() {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder.itemView.setBackgroundColor(Util.getColorByAttr(ReplyActivity.this, R.attr.colorControlHighlight, R.color.transparent));
                }
            }

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int dragFlags = 0, swiped = 0;
                if (viewHolder.getAdapterPosition() < insertPhotoAdapter.getItemCount() - 1) {
                    swiped = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                    if (viewHolder.getAdapterPosition() < insertPhotoAdapter.getItemCount() - 2 && viewHolder.getAdapterPosition() > 0) {
                        dragFlags = ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT;
                    } else if (viewHolder.getAdapterPosition() == insertPhotoAdapter.getItemCount() - 2) {
                        dragFlags = ItemTouchHelper.LEFT;
                    } else if (viewHolder.getAdapterPosition() == 0) {
                        dragFlags = ItemTouchHelper.RIGHT;
                    }
                }
                return makeMovementFlags(dragFlags, swiped);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int oldPosition = viewHolder.getAdapterPosition();
                int newPosition = target.getAdapterPosition();
                if (newPosition < insertPhotoAdapter.getItemCount() - 1) {
                    if (oldPosition < newPosition) {
                        for (int i = oldPosition; i < newPosition; i++) {
                            insertPhotoAdapter.swap(i, i + 1);
                        }
                    } else {
                        for (int i = oldPosition; i > newPosition; i--) {
                            insertPhotoAdapter.swap(i, i - 1);
                        }
                    }
                    insertPhotoAdapter.notifyItemMoved(oldPosition, newPosition);
                    return true;
                }
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                insertPhotoAdapter.remove(position);
            }
        });
        mItemTouchHelper.attachToRecyclerView(insertView);
        findViewById(R.id.activity_reply_root).setOnClickListener(this);
        findViewById(R.id.activity_reply_layout).setOnClickListener(this);
        toolbar.setNavigationIcon(R.drawable.ic_reply_toolbar_round_close);
        if (replyInfoBean.getPid() == null && replyInfoBean.getFloorNum() == null) {
            insertImageBtn.setVisibility(View.VISIBLE);
        } else {
            insertImageBtn.setVisibility(View.INVISIBLE);
        }
        insertPhotoAdapter = new InsertPhotoAdapter(this);
        insertView.setAdapter(insertPhotoAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        insertView.setLayoutManager(layoutManager);
        if (replyInfoBean.getReplyUser() != null) {
            editText.setHint(getString(R.string.hint_reply, replyInfoBean.getReplyUser()));
        }
        TabLayout tabLayout = (TabLayout) findViewById(R.id.activity_reply_emotion_tab);
        TabViewPagerAdapter emotionViewPagerAdapter = new TabViewPagerAdapter();
        GridView classicEmotionGridView = new GridView(this);
        GridView emojiEmotionGridView = new GridView(this);
        EmotionViewFactory.initGridView(this, EmotionUtil.EMOTION_CLASSIC_WEB_TYPE, classicEmotionGridView);
        EmotionViewFactory.initGridView(this, EmotionUtil.EMOTION_EMOJI_WEB_TYPE, emojiEmotionGridView);
        emotionViewPagerAdapter.addView(classicEmotionGridView, getString(R.string.title_emotion_classic));
        emotionViewPagerAdapter.addView(emojiEmotionGridView, getString(R.string.title_emotion_emoji));
        emotionViewPager.setAdapter(emotionViewPagerAdapter);
        tabLayout.setupWithViewPager(emotionViewPager);
        if (content != null) {
            editText.getMgr().disable();
            editText.setText(StringUtil.getEmotionContent(EmotionUtil.EMOTION_ALL_WEB_TYPE, editText, content));
            editText.getMgr().enable();
        }
        initListener();
    }

    private boolean canSend() {
        return (!(editText.getText().toString().isEmpty()) |
                insertPhotoAdapter.getFileList().size() > 0);
    }

    private boolean needUpload() {
        boolean needUpload = false;
        if (replyInfoBean.isSubFloor()) {
            return false;
        }
        for (PhotoInfoBean photoInfoBean : insertPhotoAdapter.getFileList()) {
            if (photoInfoBean.getWebUploadPicBean() == null) {
                needUpload = true;
                break;
            }
        }
        return needUpload;
    }

    private String getReplyContent() {
        StringBuilder builder = new StringBuilder();
        if (replyInfoBean.isSubFloor() && (replyInfoBean.getReplyUser() != null)) {
            builder.append("回复 ")
                    .append(replyInfoBean.getReplyUser())
                    .append(" :");
        }
        builder.append(editText.getText());
        if (getAppPreferences().getLittleTail() != null) {
            if( replyInfoBean.isSubFloor() == false || replyInfoBean.getReplyUser() == null ){
                builder.append("\n");
            }
            builder.append(getAppPreferences().getLittleTail());
        }
        return builder.toString();
    }

    private void getImageInfo(ReplyContentCallback callback) {
        StringBuilder builder = new StringBuilder();
        if (hasPhoto()) {
            if (!needUpload()) {
                for (PhotoInfoBean photoInfoBean : insertPhotoAdapter.getFileList()) {
                    if (photoInfoBean.getWebUploadPicBean() != null) {
                        builder.append(photoInfoBean.getWebUploadPicBean().getImageInfo());
                        if ((insertPhotoAdapter.getFileList().size() - 1) > insertPhotoAdapter.getFileList().indexOf(photoInfoBean)) {
                            builder.append("|");
                        }
                    }
                }
                callback.onSuccess(builder.toString());
                return;
            }
            UploadHelper.with(this)
                    .setFileList(insertPhotoAdapter.getFileList())
                    .setCallback(new UploadCallback() {
                        @Override
                        public void onSuccess(List<PhotoInfoBean> photoInfoBeans) {
                            for (PhotoInfoBean photoInfoBean : photoInfoBeans) {
                                if (photoInfoBean.getWebUploadPicBean() != null) {
                                    builder.append(photoInfoBean.getWebUploadPicBean().getImageInfo());
                                    if ((photoInfoBeans.size() - 1) > photoInfoBeans.indexOf(photoInfoBean)) {
                                        builder.append("|");
                                    }
                                }
                            }
                            Log.i(TAG, "onSuccess: " + builder.toString());
                            callback.onSuccess(builder.toString());
                        }

                        @Override
                        public void onStart(int total) {
                            callback.onStart(total);
                        }

                        @Override
                        public void onProgress(int current, int total) {
                            callback.onProgress(current, total);
                        }

                        @Override
                        public void onFailure(String error) {
                            callback.onFailure(error);
                        }
                    })
                    .start();
            return;
        }
        callback.onSuccess("");
    }

    private boolean hasPhoto() {
        return insertPhotoAdapter.getFileList() != null && insertPhotoAdapter.getFileList().size() > 0;
    }

    private void setEnabled(TintImageView imageButton, boolean enable) {
        imageButton.setClickable(enable);
        imageButton.setEnabled(enable);
    }

    private void initListener() {
        TintImageView undo = (TintImageView) findViewById(R.id.activity_reply_edit_undo);
        TintImageView redo = (TintImageView) findViewById(R.id.activity_reply_edit_redo);
        TintImageView clear = (TintImageView) findViewById(R.id.activity_reply_edit_clear);
        undo.setOnClickListener(this);
        setEnabled(undo, false);
        redo.setOnClickListener(this);
        setEnabled(redo, false);
        clear.setOnClickListener(this);
        setEnabled(clear, false);
        editText.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable editable) {
                super.afterTextChanged(editable);
                setEnabled(undo, editText.canUndo());
                setEnabled(redo, editText.canRedo());
                setEnabled(clear, !TextUtils.isEmpty(editable));
                if (sendItem != null) sendItem.setEnabled(canSend());
            }
        });
        KeyboardUtil.attach(this, panelFrameLayout);
        KPSwitchConflictUtil.attach(
                panelFrameLayout,
                editText,
                new KPSwitchConflictUtil.SubPanelAndTrigger(emotionView, emotionBtn),
                new KPSwitchConflictUtil.SubPanelAndTrigger(insertImageView, insertImageBtn)
        );
        EmotionUtil.GlobalOnItemClickManagerUtil.getInstance(this).attachToEditText(editText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reply_toolbar, menu);
        sendItem = menu.findItem(R.id.menu_send);
        sendItem.setEnabled(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void finish() {
        overridePendingTransition(R.anim.in_bottom, R.anim.out_bottom);
        super.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_send) {
            mCallback = new Callback<WebReplyResultBean>() {
                @Override
                public void onResponse(@NotNull Call<WebReplyResultBean> call, @NotNull Response<WebReplyResultBean> response) {
                    WebReplyResultBean data = response.body();
                    if (loadingDialog != null) loadingDialog.cancel();
                    Toast.makeText(ReplyActivity.this, R.string.toast_reply_success, Toast.LENGTH_SHORT).show();
                    sendBroadcast(new Intent()
                            .setAction(ThreadActivity.ACTION_REPLY_SUCCESS)
                            .putExtra("pid", replyInfoBean.getPid() != null ? replyInfoBean.getPid() : data.getData().getPid()));
                    replySuccess = true;
                    finish();
                }

                @Override
                public void onFailure(@NotNull Call<WebReplyResultBean> call, @NotNull Throwable t) {
                    if (loadingDialog != null) loadingDialog.cancel();
                    int code = t instanceof TiebaException ? ((TiebaException) t).getCode() : -1;
                    Toast.makeText(ReplyActivity.this, getString(R.string.toast_reply_failed, code, t.getMessage()), Toast.LENGTH_SHORT).show();
                }
            };
            realReply();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void getBSK(String tbs, CommonCallback<String> commonCallback) {
        mWebView = new WebView(this);
        mWebView.getSettings().setJavaScriptEnabled(true);
        webViewContainer.addView(mWebView);
        mWebView.evaluateJavascript(AssetUtil.getStringFromAsset(this, "new_bsk.js"), value -> {
            mWebView.evaluateJavascript("get_bsk_data(\"" + tbs + "\")", value1 -> {
                destroyWebView();
                if (commonCallback != null)
                    commonCallback.onSuccess(value1.replaceAll("\"", ""));
            });
        });
    }

    private void realReply(/*String code, String md5*/) {
        loadingDialog = new LoadingDialog(this);
        loadingDialog.show();
        getImageInfo(new ReplyContentCallback() {
            @Override
            public void onSuccess(String data) {
                loadingDialog.setTipText("正在提交...");
                getBSK(replyInfoBean.getTbs(), new CommonCallback<String>() {
                    @Override
                    public void onSuccess(String bsk) {
                        if (replyInfoBean.getPid() == null && replyInfoBean.getFloorNum() == null) {
                            TiebaApi.getInstance().webReply(
                                    replyInfoBean.getForumId(),
                                    replyInfoBean.getForumName(),
                                    replyInfoBean.getThreadId(),
                                    replyInfoBean.getTbs(),
                                    getReplyContent(),
                                    data,
                                    replyInfoBean.getNickName(),
                                    replyInfoBean.getPn(),
                                    bsk
                            ).enqueue(mCallback);
                        } else {
                            if (replyInfoBean.isSubFloor() && replyInfoBean.getSpid() != null) {
                                TiebaApi.getInstance().webReply(replyInfoBean.getForumId(), replyInfoBean.getForumName(),
                                        replyInfoBean.getThreadId(),
                                        replyInfoBean.getTbs(),
                                        getReplyContent(),
                                        data,
                                        replyInfoBean.getNickName(),
                                        replyInfoBean.getPid(),
                                        replyInfoBean.getSpid(),
                                        replyInfoBean.getFloorNum(),
                                        replyInfoBean.getPn(),
                                        bsk).enqueue(mCallback);
                            } else {
                                TiebaApi.getInstance().webReply(replyInfoBean.getForumId(), replyInfoBean.getForumName(),
                                        replyInfoBean.getThreadId(),
                                        replyInfoBean.getTbs(),
                                        getReplyContent(),
                                        data,
                                        replyInfoBean.getNickName(),
                                        replyInfoBean.getPid(),
                                        replyInfoBean.getFloorNum(),
                                        replyInfoBean.getPn(),
                                        bsk).enqueue(mCallback);
                            }
                        }
                    }

                    @Override
                    public void onFailure(int code, String error) {
                    }
                });
            }

            @Override
            public void onStart(int total) {
                loadingDialog.setTipText("正在上传图片...(0/" + total + ")");
            }

            @Override
            public void onProgress(int current, int total) {
                loadingDialog.setTipText("正在上传图片...(" + current + "/" + total + ")");
            }

            @Override
            public void onFailure(String error) {
                loadingDialog.cancel();
                Toast.makeText(ReplyActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            List<Uri> uriList = Matisse.obtainResult(data);
            List<PhotoInfoBean> photoInfoBeans = insertPhotoAdapter.getFileList();
            for (Uri uri : uriList) {
                PhotoInfoBean infoBean = new PhotoInfoBean(this, uri);
                photoInfoBeans.add(infoBean);
            }
            insertPhotoAdapter.setFileList(photoInfoBeans);
            sendItem.setEnabled(true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_reply_root:
                finish();
                break;
            case R.id.activity_reply_edit_undo:
                editText.undo();
                break;
            case R.id.activity_reply_edit_redo:
                editText.redo();
                break;
            case R.id.activity_reply_edit_clear:
                editText.setText(null);
                break;
        }
    }
}