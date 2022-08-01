package com.huanchengfly.tieba.post.fragments;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.piasy.biv.loader.ImageLoader;
import com.github.piasy.biv.view.BigImageView;
import com.github.piasy.biv.view.GlideImageViewFactory;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.components.CircleProgressIndicator;
import com.huanchengfly.tieba.post.models.PhotoViewBean;
import com.huanchengfly.tieba.post.utils.DialogUtil;
import com.huanchengfly.tieba.post.utils.ImageUtil;

import java.io.File;

import butterknife.BindView;

public class PhotoViewFragment extends BaseFragment {
    public static final String TAG = PhotoViewFragment.class.getSimpleName();
    private static final String ARG_INFO = "info";
    @BindView(R.id.big_image_view)
    public BigImageView bigImageView;
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
        loadByBigImageView();
    }

    private void showBottomBar(boolean autoHide) {
        if (getAttachContext() instanceof OnChangeBottomBarVisibilityListener) {
            ((OnChangeBottomBarVisibilityListener) getAttachContext()).onShow(autoHide);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void loadByBigImageView() {
        if (!canLoad()) return;
        bigImageView.setVisibility(View.VISIBLE);
        bigImageView.setImageViewFactory(new GlideImageViewFactory());
        bigImageView.setProgressIndicator(new CircleProgressIndicator());
        bigImageView.showImage(Uri.parse(photoViewBean.getOriginUrl()));
        bigImageView.setOnTouchListener((view, event) -> {
            showBottomBar(true);
            return false;
        });
        bigImageView.setOnLongClickListener(v -> {
            showBottomBar(false);
            openDialog(photoViewBean);
            return true;
        });
        bigImageView.setOnClickListener(view -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
        bigImageView.setImageLoaderCallback(new ImageLoader.Callback() {
            @Override
            public void onCacheHit(int imageType, File image) {
            }

            @Override
            public void onCacheMiss(int imageType, File image) {
            }

            @Override
            public void onStart() {
            }

            @Override
            public void onProgress(int progress) {
            }

            @Override
            public void onFinish() {
            }

            @Override
            public void onSuccess(File image) {
                final SubsamplingScaleImageView view = bigImageView.getSSIV();

                if (view != null) {
                    view.setMinimumDpi(80);

                    view.setOnImageEventListener(new SubsamplingScaleImageView.OnImageEventListener() {
                        @Override
                        public void onReady() {
                        }

                        @Override
                        public void onImageLoaded() {
                            view.setDoubleTapZoomDpi(80);
                            view.setDoubleTapZoomDuration(200);
                            view.setDoubleTapZoomStyle(SubsamplingScaleImageView.ZOOM_FOCUS_FIXED);
                            view.setQuickScaleEnabled(false);
                        }

                        @Override
                        public void onPreviewLoadError(Exception e) {
                        }

                        @Override
                        public void onImageLoadError(Exception e) {
                        }

                        @Override
                        public void onTileLoadError(Exception e) {
                        }

                        @Override
                        public void onPreviewReleased() {
                        }
                    });
                }
            }

            @Override
            public void onFail(Exception error) {
                Toast.makeText(getAttachContext(), R.string.toast_load_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
