package com.huanchengfly.tieba.post.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.gyf.immersionbar.ImmersionBar;
import com.huanchengfly.tieba.post.BaseApplication;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.adapters.ThemeColorAdapter;
import com.huanchengfly.tieba.post.components.MyImageEngine;
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager;
import com.huanchengfly.tieba.post.components.transformations.BlurTransformation;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.utils.ImageUtil;
import com.huanchengfly.tieba.post.utils.PermissionUtil;
import com.huanchengfly.tieba.post.utils.SharedPreferencesUtil;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.jrummyapps.android.colorpicker.ColorPickerDialog;
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener;
import com.yalantis.ucrop.UCrop;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.runtime.Permission;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.io.File;
import java.util.List;

import static com.huanchengfly.tieba.post.utils.ColorUtils.getDarkerColor;
import static com.huanchengfly.tieba.post.utils.ThemeUtil.SP_TRANSLUCENT_PRIMARY_COLOR;
import static com.huanchengfly.tieba.post.utils.ThemeUtil.THEME_TRANSLUCENT;

public class TranslucentThemeActivity extends BaseActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, ColorPickerDialogListener {
    public static final String TAG = TranslucentThemeActivity.class.getSimpleName();
    public static final int REQUEST_CODE_CHOOSE = 2;

    private Uri mUri;
    private int alpha;
    private int blur;
    private Palette mPalette;
    private View mSelectColor;
    private ThemeColorAdapter mAdapter;
    private View mProgress;

    public static String toString(int alpha, int red, int green, int blue) {
        String hr = Integer.toHexString(red);
        String hg = Integer.toHexString(green);
        String hb = Integer.toHexString(blue);
        String ha = Integer.toHexString(alpha);
        return "#" + fixHexString(ha) + fixHexString(hr) + fixHexString(hg) + fixHexString(hb);
    }

    public static String fixHexString(String hexString) {
        if (hexString.length() < 1) {
            hexString = "00";
        }
        if (hexString.length() == 1) {
            hexString = "0" + hexString;
        }
        if (hexString.length() > 2) {
            hexString = hexString.substring(0, 2);
        }
        return hexString;
    }

    public static String toString(@ColorInt int color) {
        return toString(Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            Uri sourceUri = Matisse.obtainResult(data).get(0);
            Glide.with(this)
                    .asDrawable()
                    .load(sourceUri)
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            Bitmap bitmap = ImageUtil.drawableToBitmap(resource);
                            File file = ImageUtil.bitmapToFile(bitmap, new File(getCacheDir(), "origin_background.jpg"));
                            Uri sourceUri = Uri.fromFile(file);
                            Uri destUri = Uri.fromFile(new File(getCacheDir(), "cropped_background.jpg"));
                            float height = (float) BaseApplication.ScreenInfo.EXACT_SCREEN_HEIGHT;
                            float width = (float) BaseApplication.ScreenInfo.EXACT_SCREEN_WIDTH;
                            UCrop.Options uCropOptions = new UCrop.Options();
                            uCropOptions.setShowCropFrame(true);
                            uCropOptions.setShowCropGrid(true);
                            uCropOptions.setStatusBarColor(getDarkerColor(ThemeUtils.getColorByAttr(TranslucentThemeActivity.this, R.attr.colorPrimary)));
                            uCropOptions.setToolbarColor(ThemeUtils.getColorByAttr(TranslucentThemeActivity.this, R.attr.colorPrimary));
                            uCropOptions.setToolbarWidgetColor(ThemeUtils.getColorByAttr(TranslucentThemeActivity.this, R.attr.colorTextOnPrimary));
                            uCropOptions.setActiveWidgetColor(ThemeUtils.getColorByAttr(TranslucentThemeActivity.this, R.attr.colorAccent));
                            uCropOptions.setActiveControlsWidgetColor(ThemeUtils.getColorByAttr(TranslucentThemeActivity.this, R.attr.colorAccent));
                            uCropOptions.setLogoColor(ThemeUtils.getColorByAttr(TranslucentThemeActivity.this, R.attr.colorPrimary));
                            uCropOptions.setCompressionFormat(Bitmap.CompressFormat.JPEG);
                            UCrop.of(sourceUri, destUri)
                                    .withAspectRatio(width / height, 1)
                                    .withOptions(uCropOptions)
                                    .start(TranslucentThemeActivity.this);
                        }
                    });
        } else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            mUri = UCrop.getOutput(data);
            invalidateOptionsMenu();
            refreshBackground();
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            cropError.printStackTrace();
        }
    }

    private void refreshBackground() {
        mProgress.setVisibility(View.VISIBLE);
        if (mUri == null) {
            findViewById(R.id.background).setBackgroundColor(Color.BLACK);
            mProgress.setVisibility(View.GONE);
            return;
        }
        RequestOptions bgOptions = RequestOptions.centerCropTransform()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE);
        if (blur > 0) {
            bgOptions = bgOptions.transform(new BlurTransformation(blur));
        }
        Glide.with(this)
                .asDrawable()
                .load(mUri)
                .apply(bgOptions)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        resource.setAlpha(alpha);
                        Bitmap bitmap = ImageUtil.drawableToBitmap(resource);
                        findViewById(R.id.background).setBackgroundTintList(null);
                        findViewById(R.id.background).setBackground(new BitmapDrawable(getResources(), bitmap));
                        mPalette = Palette.from(bitmap).generate();
                        mAdapter.setPalette(mPalette);
                        mSelectColor.setVisibility(View.VISIBLE);
                        mProgress.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void refreshStatusBarColor() {
        ImmersionBar.with(this)
                .transparentBar()
                .init();
    }

    @SuppressLint({"ApplySharedPref", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translucent_theme);
        TextView tip = (TextView) findViewById(R.id.tip);
        Spanned spanned;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            spanned = Html.fromHtml(getString(R.string.tip_translucent_theme), Html.FROM_HTML_MODE_LEGACY);
        } else {
            spanned = Html.fromHtml(getString(R.string.tip_translucent_theme));
        }
        tip.setText(spanned);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_dialog_translucent_theme);
        }
        alpha = SharedPreferencesUtil.get(this, SharedPreferencesUtil.SP_SETTINGS).getInt("translucent_background_alpha", 255);
        blur = SharedPreferencesUtil.get(this, SharedPreferencesUtil.SP_SETTINGS).getInt("translucent_background_blur", 0);
        Button customColorBtn = (Button) findViewById(R.id.custom_color);
        mSelectColor = findViewById(R.id.select_color);
        Button selectPicBtn = (Button) findViewById(R.id.select_pic);
        mProgress = findViewById(R.id.progress);
        mProgress.setOnTouchListener((view, event) -> true);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.select_color_recycler_view);
        mAdapter = new ThemeColorAdapter(this);
        mAdapter.setOnItemClickListener((itemView, themeColor, position, viewType) -> {
            SharedPreferencesUtil.get(this, SharedPreferencesUtil.SP_SETTINGS)
                    .edit()
                    .putString(SP_TRANSLUCENT_PRIMARY_COLOR, toString(themeColor))
                    .commit();
            ThemeUtils.refreshUI(this);
        });
        recyclerView.setLayoutManager(new MyLinearLayoutManager(this, MyLinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(mAdapter);
        SeekBar alphaSeekBar = (SeekBar) findViewById(R.id.alpha);
        alphaSeekBar.setProgress(alpha);
        SeekBar blueSeekBar = (SeekBar) findViewById(R.id.blur);
        blueSeekBar.setProgress(blur);
        alphaSeekBar.setOnSeekBarChangeListener(this);
        blueSeekBar.setOnSeekBarChangeListener(this);
        selectPicBtn.setOnClickListener(this);
        customColorBtn.setOnClickListener(this);
        findViewById(R.id.background).setBackgroundColor(Color.BLACK);
        mProgress.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_translucent_theme_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void onColorSelected(int dialogId, int color) {
        SharedPreferencesUtil.get(this, SharedPreferencesUtil.SP_SETTINGS)
                .edit()
                .putString(SP_TRANSLUCENT_PRIMARY_COLOR, toString(color))
                .commit();
        ThemeUtils.refreshUI(this);
    }

    @Override
    public void onDialogDismissed(int dialogId) {
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_finish:
                SharedPreferencesUtil.put(this, SharedPreferencesUtil.SP_SETTINGS, "translucent_background_alpha", alpha);
                SharedPreferencesUtil.put(this, SharedPreferencesUtil.SP_SETTINGS, "translucent_background_blur", blur);
                /*
                saveOriginPic(file -> savePic(file1 -> {
                    ThemeUtil.getSharedPreferences(this)
                            .edit()
                            .putString(ThemeUtil.SP_THEME, THEME_TRANSLUCENT)
                            .putString(ThemeUtil.SP_OLD_THEME, THEME_TRANSLUCENT)
                            .commit();
                    Toast.makeText(TranslucentThemeActivity.this, R.string.toast_save_pic_success, Toast.LENGTH_SHORT).show();
                    mProgress.setVisibility(View.GONE);
                    finish();
                }));
                */
                savePic(file -> {
                    ThemeUtil.getSharedPreferences(this)
                            .edit()
                            .putString(ThemeUtil.SP_THEME, THEME_TRANSLUCENT)
                            .putString(ThemeUtil.SP_OLD_THEME, THEME_TRANSLUCENT)
                            .commit();
                    Toast.makeText(TranslucentThemeActivity.this, R.string.toast_save_pic_success, Toast.LENGTH_SHORT).show();
                    BaseApplication.setTranslucentBackground(null);
                    mProgress.setVisibility(View.GONE);
                    finish();
                });
                return true;
            case R.id.select_color:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void savePic(SavePicCallback<File> callback) {
        mProgress.setVisibility(View.VISIBLE);
        RequestOptions bgOptions = RequestOptions.centerCropTransform()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE);
        if (blur > 0) {
            bgOptions = bgOptions.transform(new BlurTransformation(blur));
        }
        Glide.with(this)
                .asDrawable()
                .load(mUri)
                .apply(bgOptions)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        resource.setAlpha(alpha);
                        Bitmap bitmap = ImageUtil.drawableToBitmap(resource);
                        File file = ImageUtil.compressImage(bitmap, new File(getFilesDir(), "background.jpg"));
                        mPalette = Palette.from(bitmap).generate();
                        SharedPreferencesUtil.put(TranslucentThemeActivity.this,
                                SharedPreferencesUtil.SP_SETTINGS,
                                ThemeUtil.SP_TRANSLUCENT_THEME_BACKGROUND_PATH,
                                file.getAbsolutePath());
                        ThemeUtils.refreshUI(TranslucentThemeActivity.this, TranslucentThemeActivity.this);
                        callback.onSuccess(file);
                    }
                });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem finishItem = menu.findItem(R.id.menu_finish);
        finishItem.setEnabled(mUri != null);
        return super.onPrepareOptionsMenu(menu);
    }

    private void saveOriginPic(SavePicCallback<File> callback) {
        mProgress.setVisibility(View.VISIBLE);
        RequestOptions bgOptions = RequestOptions.centerCropTransform()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE);
        Glide.with(this)
                .asFile()
                .load(mUri)
                .apply(bgOptions)
                .into(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                        File file = new File(getFilesDir(), "origin_background_file.jpg");
                        ImageUtil.copyFile(resource, file);
                        SharedPreferencesUtil.put(TranslucentThemeActivity.this,
                                SharedPreferencesUtil.SP_SETTINGS,
                                "translucent_background_origin_path",
                                file.getAbsolutePath());
                        callback.onSuccess(file);
                    }
                });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.alpha:
                alpha = seekBar.getProgress();
                break;
            case R.id.blur:
                blur = seekBar.getProgress();
                break;
        }
        refreshBackground();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_pic:
                askPermission(data -> Matisse.from(this)
                        .choose(MimeType.ofImage())
                        .theme(ThemeUtil.isNightMode(this) ? R.style.Matisse_Dracula : R.style.Matisse_Zhihu)
                        .imageEngine(new MyImageEngine())
                        .forResult(REQUEST_CODE_CHOOSE));
                break;
            case R.id.custom_color:
                ColorPickerDialog primaryColorPicker = ColorPickerDialog.newBuilder()
                        .setDialogTitle(R.string.title_color_picker_primary)
                        .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                        .setShowAlphaSlider(true)
                        .setDialogId(0)
                        .setAllowPresets(false)
                        .setColor(ThemeUtils.getColorById(this, R.color.default_color_primary))
                        .create();
                primaryColorPicker.setColorPickerDialogListener(this);
                primaryColorPicker.show(getFragmentManager(), "ColorPicker_TranslucentThemePrimaryColor");
                break;
        }
    }

    private void askPermission(Action<List<String>> granted) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            PermissionUtil.askPermission(this, granted, R.string.toast_no_permission_insert_photo,
                    new PermissionUtil.Permission(Permission.Group.STORAGE, getString(R.string.tip_permission_storage)));
        } else {
            PermissionUtil.askPermission(this, granted, R.string.toast_no_permission_insert_photo,
                    new PermissionUtil.Permission(Permission.READ_EXTERNAL_STORAGE, getString(R.string.tip_permission_storage)));
        }
    }

    public interface SavePicCallback<T> {
        void onSuccess(T t);
    }
}
