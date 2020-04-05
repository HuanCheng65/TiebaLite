package com.huanchengfly.tieba.post.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.huanchengfly.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.api.TiebaApi;
import com.huanchengfly.tieba.api.models.PicPageBean;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.activities.base.BaseActivity;
import com.huanchengfly.tieba.post.adapters.PhotoViewAdapter;
import com.huanchengfly.tieba.post.base.Config;
import com.huanchengfly.tieba.post.fragments.PhotoViewFragment;
import com.huanchengfly.tieba.post.models.PhotoViewBean;
import com.huanchengfly.tieba.post.utils.AnimUtil;
import com.huanchengfly.tieba.post.utils.ImageUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoViewActivity extends BaseActivity implements PhotoViewFragment.OnChangeBottomBarVisibilityListener, Toolbar.OnMenuItemClickListener {
    public static final String TAG = PhotoViewActivity.class.getSimpleName();
    public static final String EXTRA_BEANS = "beans";
    public static final String EXTRA_POSITION = "position";
    public static final String EXTRA_FORUM_ID = "forum_id";
    public static final String EXTRA_FORUM_NAME = "forum_name";
    public static final String EXTRA_THREAD_ID = "thread_id";
    public static final String EXTRA_SEE_LZ = "see_lz";
    public static final String EXTRA_IS_FRS = "is_frs";
    public static final String EXTRA_OBJ_TYPE = "obj_type";
    public static final int DEFAULT_HIDE_DELAY = 3000;
    public static final String OBJ_TYPE_THREAD_PAGE = "pb";
    public static final String OBJ_TYPE_FORUM_PAGE = "frs";
    private static Handler handler = new Handler();
    private List<PhotoViewBean> photoViewBeans;
    private int startPosition;
    private ProgressBar progressBar;
    private TextView mCounter;
    private BottomAppBar mAppBar;
    private int lastIndex;
    private boolean loadFinished = false;
    private String amount;
    private PhotoViewAdapter mAdapter;
    private String forumName;
    private String forumId;
    private String threadId;
    private boolean seeLz;
    private boolean isFrs;
    private String objType;
    private ViewPager2 mViewPager;
    private Runnable autoHideRunnable = this::onHide;
    private boolean mLoading;

    public static void launch(@NonNull Context context, PhotoViewBean photoViewBean) {
        launch(context, new PhotoViewBean[]{photoViewBean});
    }

    public static void launch(@NonNull Context context, List<PhotoViewBean> photoViewBeanList) {
        launch(context, photoViewBeanList.toArray(new PhotoViewBean[0]), 0);
    }

    public static void launch(@NonNull Context context, List<PhotoViewBean> photoViewBeanList, int position) {
        launch(context, photoViewBeanList.toArray(new PhotoViewBean[0]), position);
    }

    public static void launch(@NonNull Context context, PhotoViewBean[] photoViewBeans) {
        launch(context, photoViewBeans, 0);
    }

    public static void launch(@NonNull Context context, PhotoViewBean[] photoViewBeans, int position) {
        context.startActivity(new Intent(context, PhotoViewActivity.class)
                .putExtra(EXTRA_BEANS, photoViewBeans)
                .putExtra(EXTRA_POSITION, position)
                .putExtra(EXTRA_IS_FRS, false));
    }

    public static void launch(@NonNull Context context,
                              PhotoViewBean[] photoViewBeans,
                              int position,
                              String forumName,
                              String forumId,
                              String threadId,
                              boolean seeLz,
                              String objType) {
        context.startActivity(new Intent(context, PhotoViewActivity.class)
                .putExtra(EXTRA_BEANS, photoViewBeans)
                .putExtra(EXTRA_POSITION, position)
                .putExtra(EXTRA_IS_FRS, true)
                .putExtra(EXTRA_FORUM_NAME, forumName)
                .putExtra(EXTRA_FORUM_ID, forumId)
                .putExtra(EXTRA_THREAD_ID, threadId)
                .putExtra(EXTRA_SEE_LZ, seeLz)
                .putExtra(EXTRA_OBJ_TYPE, objType));
    }

    private void loadMore() {
        if (loadFinished) {
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (mLoading) {
            return;
        }
        mLoading = true;
        progressBar.setVisibility(View.VISIBLE);
        PhotoViewBean lastBean = photoViewBeans.get(photoViewBeans.size() - 1);
        TiebaApi.getInstance().picPage(
                forumId,
                forumName,
                threadId,
                seeLz,
                ImageUtil.getPicId(lastBean.getOriginUrl()),
                String.valueOf(photoViewBeans.size()),
                objType,
                false
        ).enqueue(new Callback<PicPageBean>() {
            @Override
            public void onResponse(@NotNull Call<PicPageBean> call, @NotNull Response<PicPageBean> response) {
                PicPageBean data = response.body();
                mLoading = false;
                progressBar.setVisibility(View.GONE);
                amount = data.getPicAmount();
                updateCounter(mViewPager.getCurrentItem());
                List<PicPageBean.PicBean> picBeans = new ArrayList<>();
                List<PicPageBean.ImgInfoBean> imgInfoBeans = new ArrayList<>();
                if (data.getPicList().size() > 0) {
                    int index = Integer.valueOf(data.getPicList().get(data.getPicList().size() - 1).getOverAllIndex());
                    loadFinished = index >= Integer.valueOf(amount);
                    for (PicPageBean.PicBean picBean : data.getPicList()) {
                        picBeans.add(picBean);
                        imgInfoBeans.add(picBean.getImg().getOriginal());
                    }
                    lastIndex = Integer.parseInt(picBeans.get(0).getOverAllIndex());
                    for (PhotoViewBean photoViewBean : photoViewBeans) {
                        int ind = lastIndex - (photoViewBeans.size() - 1 - photoViewBeans.indexOf(photoViewBean));
                        photoViewBean.setIndex(String.valueOf(ind));
                    }
                    picBeans.remove(0);
                    imgInfoBeans.remove(0);
                    List<PhotoViewBean> beans = new ArrayList<>();
                    for (PicPageBean.ImgInfoBean imgInfoBean : imgInfoBeans) {
                        PhotoViewBean photoViewBean = new PhotoViewBean(imgInfoBean.getBigCdnSrc(),
                                imgInfoBean.getOriginalSrc(),
                                Integer.parseInt(imgInfoBean.getHeight()) > Config.EXACT_SCREEN_HEIGHT,
                                picBeans.get(imgInfoBeans.indexOf(imgInfoBean)).getOverAllIndex(),
                                TextUtils.equals(imgInfoBean.getFormat(), "2"));
                        beans.add(photoViewBean);
                    }
                    mAdapter.insert(beans);
                    photoViewBeans = mAdapter.getData();
                    mAdapter.notifyDataSetChanged();
                    updateCounter(mViewPager.getCurrentItem());
                } else {
                    loadFinished = true;
                }
            }

            @Override
            public void onFailure(@NotNull Call<PicPageBean> call, @NotNull Throwable t) {
                mLoading = false;
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void loadFrs() {
        forumName = getIntent().getStringExtra(EXTRA_FORUM_NAME);
        forumId = getIntent().getStringExtra(EXTRA_FORUM_ID);
        threadId = getIntent().getStringExtra(EXTRA_THREAD_ID);
        seeLz = getIntent().getBooleanExtra(EXTRA_SEE_LZ, false);
        objType = getIntent().getStringExtra(EXTRA_OBJ_TYPE);
        loadMore();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        mViewPager = (ViewPager2) findViewById(R.id.view_pager);
        mCounter = (TextView) findViewById(R.id.counter);
        mAppBar = (BottomAppBar) findViewById(R.id.bottom_app_bar);
        mAppBar.setOnMenuItemClickListener(this);
        Drawable overflowIcon = mAppBar.getOverflowIcon();
        if (overflowIcon != null) {
            mAppBar.setOverflowIcon(ThemeUtils.tintDrawable(overflowIcon, Color.WHITE));
        }
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        isFrs = getIntent().getBooleanExtra(EXTRA_IS_FRS, false);
        photoViewBeans = new ArrayList<>();
        startPosition = getIntent().getIntExtra(EXTRA_POSITION, 0);
        Parcelable[] parcelables = getIntent().getParcelableArrayExtra(EXTRA_BEANS);
        for (Parcelable parcelable : parcelables) {
            photoViewBeans.add((PhotoViewBean) parcelable);
        }
        amount = String.valueOf(photoViewBeans.size());
        mAdapter = new PhotoViewAdapter(this, photoViewBeans);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(startPosition, false);
        updateCounter();
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                onShow(true);
            }

            @Override
            public void onPageSelected(int position) {
                updateCounter(position);
                onShow(true);
                if (!mLoading && isFrs && position >= photoViewBeans.size() - 1) {
                    loadMore();
                }
            }
        });
        if (isFrs) {
            progressBar.setVisibility(View.VISIBLE);
            loadFrs();
        }
    }

    @Override
    public boolean isNeedImmersionBar() {
        return false;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_fade_in, R.anim.anim_fade_out);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void updateCounter() {
        updateCounter(startPosition);
    }

    private void updateCounter(int position) {
        onShow(true);
        if (photoViewBeans.size() <= 1) {
            Log.i(TAG, "updateCounter: null");
            mCounter.setText(null);
        } else if (isFrs && lastIndex > 0) {
            String index = photoViewBeans.get(position).getIndex();
            mCounter.setText(getString(R.string.tip_position, index == null ? position + 1 + "" : index, amount));
        } else {
            mCounter.setText(getString(R.string.tip_position, position + 1 + "", amount));
        }
    }

    @Override
    public void onShow(boolean autoHide) {
        handler.removeCallbacks(autoHideRunnable);
        if (mAppBar.getVisibility() == View.VISIBLE) {
            if (autoHide) {
                handler.postDelayed(autoHideRunnable, DEFAULT_HIDE_DELAY);
            }
            return;
        }
        AnimUtil.alphaIn(mAppBar)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAppBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation, boolean isReverse) {
                        if (autoHide) {
                            handler.postDelayed(autoHideRunnable, DEFAULT_HIDE_DELAY);
                        }
                    }
                })
                .start();
    }

    @Override
    public void onHide() {
        if (mAppBar.getVisibility() == View.GONE || mViewPager.getOrientation() != ViewPager2.ORIENTATION_HORIZONTAL) {
            return;
        }
        AnimUtil.alphaOut(mAppBar)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAppBar.setVisibility(View.GONE);
                    }
                })
                .start();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_toggle_orientation:
                item.setIcon(mViewPager.getOrientation() == ViewPager2.ORIENTATION_HORIZONTAL ?
                        R.drawable.ic_round_view_day_white :
                        R.drawable.ic_round_view_carousel_white);
                item.setTitle(mViewPager.getOrientation() == ViewPager2.ORIENTATION_HORIZONTAL ?
                        R.string.title_comic_mode_on :
                        R.string.title_comic_mode);
                Toast.makeText(this, mViewPager.getOrientation() == ViewPager2.ORIENTATION_HORIZONTAL ?
                        R.string.toast_comic_mode_on :
                        R.string.toast_comic_mode_off, Toast.LENGTH_SHORT).show();
                mViewPager.setOrientation(mViewPager.getOrientation() == ViewPager2.ORIENTATION_HORIZONTAL ?
                        ViewPager2.ORIENTATION_VERTICAL :
                        ViewPager2.ORIENTATION_HORIZONTAL);
                return true;
            case R.id.menu_save_image:
                ImageUtil.download(this, mAdapter.getBean(mViewPager.getCurrentItem()).getOriginUrl(), mAdapter.getBean(mViewPager.getCurrentItem()).isGif());
                return true;
            case R.id.menu_share:
                Toast.makeText(this, R.string.toast_preparing_share_pic, Toast.LENGTH_SHORT).show();
                ImageUtil.download(this, mAdapter.getBean(mViewPager.getCurrentItem()).getOriginUrl(), mAdapter.getBean(mViewPager.getCurrentItem()).isGif(), true, uri -> {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent.putExtra(Intent.EXTRA_STREAM, uri);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } else {
                        intent.putExtra(Intent.EXTRA_STREAM, uri);
                    }
                    intent.setType(Intent.normalizeMimeType("image/jpeg"));
                    Intent chooser = Intent.createChooser(intent, getString(R.string.title_share_pic));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(chooser);
                    }
                });
                return true;
        }
        return false;
    }
}
