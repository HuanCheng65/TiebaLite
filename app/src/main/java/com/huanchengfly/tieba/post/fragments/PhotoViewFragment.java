package com.huanchengfly.tieba.post.fragments;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.ImageViewState;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.huanchengfly.tieba.post.BaseApplication;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.models.PhotoViewBean;
import com.huanchengfly.tieba.post.utils.DialogUtil;
import com.huanchengfly.tieba.post.utils.ImageUtil;
import com.huanchengfly.tieba.post.widgets.MyPhotoView;

import butterknife.BindView;

public class PhotoViewFragment extends BaseFragment {
    public static final String TAG = PhotoViewFragment.class.getSimpleName();
    private static final String ARG_INFO = "info";
    private final RequestOptions requestOptions = new RequestOptions().skipMemoryCache(true);
    @BindView(R.id.photo_view)
    public MyPhotoView myPhotoView;
    @BindView(R.id.scale_image_view)
    public SubsamplingScaleImageView scaleImageView;
    @BindView(R.id.progressbar)
    public ContentLoadingProgressBar progressBar;
    private PhotoViewBean photoViewBean;

    public PhotoViewFragment() {
    }

    public static PhotoViewFragment newInstance(PhotoViewBean photoViewBean) {
        PhotoViewFragment fragment = new PhotoViewFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_INFO, photoViewBean);
        fragment.setArguments(args);
        return fragment;
    }

    public PhotoViewBean getPhotoViewBean() {
        return photoViewBean;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            photoViewBean = getArguments().getParcelable(ARG_INFO);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_photo_view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (!canLoad()) return;
        if (photoViewBean.isLongPic()) {
            loadBySubsamplingScaleImageView();
        } else {
            loadByMyPhotoView();
        }
    }

    private void showBottomBar(boolean autoHide) {
        if (getAttachContext() instanceof OnChangeBottomBarVisibilityListener) {
            ((OnChangeBottomBarVisibilityListener) getAttachContext()).onShow(autoHide);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void loadByMyPhotoView() {
        if (!canLoad()) return;
        myPhotoView.setVisibility(View.VISIBLE);
        scaleImageView.setVisibility(View.GONE);
        myPhotoView.enable();
        myPhotoView.setOnDispatchTouchEvent(event -> {
            showBottomBar(true);
        });
        myPhotoView.setOnLongClickListener(v -> {
            showBottomBar(false);
            openDialog(photoViewBean);
            return true;
        });
        myPhotoView.setOnClickListener(view -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
        Glide.with(this)
                .load(photoViewBean.getOriginUrl())
                .apply(requestOptions)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Toast.makeText(getAttachContext(), R.string.toast_load_failed, Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .apply(requestOptions)
                .into(myPhotoView);
        myPhotoView.setOnPhotoErrorListener(e -> loadBySubsamplingScaleImageView());
    }

    private void loadBySubsamplingScaleImageView() {
        if (!canLoad()) return;
        myPhotoView.setVisibility(View.GONE);
        scaleImageView.setVisibility(View.VISIBLE);
        scaleImageView.setOnLongClickListener(v -> {
            showBottomBar(false);
            openDialog(photoViewBean);
            return true;
        });
        scaleImageView.setOnClickListener(view -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
        Glide.with(this)
                .asBitmap()
                .load(photoViewBean.getOriginUrl())
                .apply(requestOptions)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        Toast.makeText(getAttachContext(), R.string.toast_load_failed, Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(new CustomViewTarget<SubsamplingScaleImageView, Bitmap>(scaleImageView) {
                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    }

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        scaleImageView.setImage(ImageSource.bitmap(resource), new ImageViewState(getInitImageScale(resource), new PointF(0, 0), 0));
                    }

                    @Override
                    protected void onResourceCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    private float getInitImageScale(Bitmap bitmap) {
        int width = BaseApplication.ScreenInfo.EXACT_SCREEN_WIDTH;
        int height = BaseApplication.ScreenInfo.EXACT_SCREEN_HEIGHT;
        float scale = 1.0f;
        if (bitmap == null) return scale;
        // 拿到图片的宽和高
        int dw = bitmap.getWidth();
        int dh = bitmap.getHeight();
        //图片宽度大于屏幕，但高度小于屏幕，则缩小图片至填满屏幕宽
        if (dw > width && dh <= height) {
            scale = width * 1.0f / dw;
        }
        //图片宽度小于屏幕，但高度大于屏幕，则放大图片至填满屏幕宽
        if (dw <= width && dh > height) {
            scale = width * 1.0f / dw;
        }
        //图片高度和宽度都小于屏幕，则放大图片至填满屏幕宽
        if (dw < width && dh < height) {
            scale = width * 1.0f / dw;
        }
        //图片高度和宽度都大于屏幕，则缩小图片至填满屏幕宽
        if (dw > width && dh > height) {
            scale = width * 1.0f / dw;
        }
        return scale;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Glide.with(this).clear(myPhotoView);
        Glide.with(this).clear(scaleImageView);
    }

    private boolean canLoad() {
        return getActivity() != null && !getActivity().isDestroyed() && !getActivity().isFinishing();
    }

    private void openDialog(PhotoViewBean photoViewBean) {
        if (!canLoad()) {
            return;
        }
        final String[] strArray = new String[]{getAttachContext().getString(R.string.menu_save_photo)};
        DialogUtil.build(getAttachContext())
                .setItems(strArray, (DialogInterface dialog, int which) -> {
                    switch (which) {
                        case 0:
                            ImageUtil.download(getAttachContext(), photoViewBean.getOriginUrl(), photoViewBean.isGif());
                            break;
                    }
                })
                .create()
                .show();
    }

    public interface OnChangeBottomBarVisibilityListener {
        void onShow(boolean autoHide);

        void onHide();
    }
}
