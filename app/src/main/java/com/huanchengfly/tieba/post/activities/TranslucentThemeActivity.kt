package com.huanchengfly.tieba.post.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.ColorInt
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.gyf.immersionbar.ImmersionBar
import com.huanchengfly.tieba.post.*
import com.huanchengfly.tieba.post.BaseApplication.Companion.translucentBackground
import com.huanchengfly.tieba.post.adapters.TranslucentThemeColorAdapter
import com.huanchengfly.tieba.post.adapters.WallpaperAdapter
import com.huanchengfly.tieba.post.api.LiteApi
import com.huanchengfly.tieba.post.api.retrofit.doIfSuccess
import com.huanchengfly.tieba.post.components.MyImageEngine
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager
import com.huanchengfly.tieba.post.components.dividers.HorizontalSpacesDecoration
import com.huanchengfly.tieba.post.components.transformations.BlurTransformation
import com.huanchengfly.tieba.post.interfaces.OnItemClickListener
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.*
import com.huanchengfly.tieba.post.utils.ThemeUtil.TRANSLUCENT_THEME_DARK
import com.huanchengfly.tieba.post.utils.ThemeUtil.TRANSLUCENT_THEME_LIGHT
import com.huanchengfly.tieba.post.widgets.theme.TintMaterialButton
import com.jrummyapps.android.colorpicker.ColorPickerDialog
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener
import com.yalantis.ucrop.UCrop
import com.yanzhenjie.permission.Action
import com.yanzhenjie.permission.runtime.Permission
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.File

class TranslucentThemeActivity : BaseActivity(), View.OnClickListener, OnSeekBarChangeListener, ColorPickerDialogListener {
    private var mUri: Uri? = null
    private var alpha = 0
    private var blur = 0
    private var mPalette: Palette? = null

    @BindView(R.id.select_color)
    lateinit var mSelectColor: View

    @BindView(R.id.recommend_wallpapers)
    lateinit var recommendWallpapers: View

    @BindView(R.id.wallpapers_rv)
    lateinit var recommendWallpapersRv: RecyclerView

    @BindView(R.id.progress)
    lateinit var mProgress: View

    @BindView(R.id.dark_color)
    lateinit var darkColorBtn: TintMaterialButton

    @BindView(R.id.light_color)
    lateinit var lightColorBtn: TintMaterialButton

    @BindView(R.id.button_back)
    lateinit var backBtn: View

    @BindView(R.id.bottom_sheet)
    lateinit var bottomSheet: LinearLayout

    @BindView(R.id.button_finish)
    lateinit var finishBtn: View

    @BindView(R.id.mask)
    lateinit var maskView: View

    @BindView(R.id.experimental_tip)
    lateinit var experimentalTipView: View

    @BindView(R.id.color_theme)
    lateinit var colorTheme: ViewGroup

    var wallpapers: List<String>? = null
        set(value) {
            field = value
            refreshWallpapers()
        }
    private val wallpaperAdapter: WallpaperAdapter by lazy { WallpaperAdapter(this) }

    private val mTranslucentThemeColorAdapter: TranslucentThemeColorAdapter by lazy {
        TranslucentThemeColorAdapter(
            this
        )
    }

    private fun launchUCrop(sourceUri: Uri) {
        mProgress.visibility = View.VISIBLE
        Glide.with(this)
            .asBitmap()
            .load(sourceUri)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {}

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    mProgress.visibility = View.GONE
                    val file =
                        ImageUtil.bitmapToFile(resource, File(cacheDir, "origin_background.jpg"))
                    val sourceFileUri = Uri.fromFile(file)
                    val destUri = Uri.fromFile(File(filesDir, "cropped_background.jpg"))
                    val height = BaseApplication.ScreenInfo.EXACT_SCREEN_HEIGHT.toFloat()
                    val width = BaseApplication.ScreenInfo.EXACT_SCREEN_WIDTH.toFloat()
                    UCrop.of(sourceFileUri, destUri)
                        .withAspectRatio(width / height, 1f)
                        .withOptions(UCrop.Options().apply {
                            setShowCropFrame(true)
                            setShowCropGrid(true)
                            setStatusBarColor(
                                ColorUtils.getDarkerColor(
                                    ThemeUtils.getColorByAttr(
                                        this@TranslucentThemeActivity,
                                        R.attr.colorPrimary
                                    )
                                )
                            )
                            setToolbarColor(
                                ThemeUtils.getColorByAttr(
                                    this@TranslucentThemeActivity,
                                    R.attr.colorPrimary
                                )
                            )
                            setToolbarWidgetColor(
                                ThemeUtils.getColorByAttr(
                                    this@TranslucentThemeActivity,
                                    R.attr.colorTextOnPrimary
                                )
                            )
                            setActiveControlsWidgetColor(
                                ThemeUtils.getColorByAttr(
                                    this@TranslucentThemeActivity,
                                    R.attr.colorAccent
                                )
                            )
                            setLogoColor(
                                ThemeUtils.getColorByAttr(
                                    this@TranslucentThemeActivity,
                                    R.attr.colorPrimary
                                )
                            )
                            setCompressionFormat(Bitmap.CompressFormat.JPEG)
                        })
                        .start(this@TranslucentThemeActivity)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    mProgress.visibility = View.GONE
                    toastShort(R.string.text_load_failed)
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == Activity.RESULT_OK) {
            val sourceUri = Matisse.obtainResult(data)[0]
            launchUCrop(sourceUri)
        } else if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            mUri = UCrop.getOutput(data!!)
            invalidateFinishBtn()
            refreshBackground()
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            cropError!!.printStackTrace()
        }
    }

    private fun refreshWallpapers() {
        if (wallpapers.isNullOrEmpty()) {
            recommendWallpapers.visibility = View.GONE
        } else {
            recommendWallpapers.visibility = View.VISIBLE
            wallpaperAdapter.setData(wallpapers)
        }
    }

    private fun refreshBackground() {
        mProgress.visibility = View.VISIBLE
        if (mUri == null) {
            findViewById(R.id.background).setBackgroundColor(Color.BLACK)
            mProgress.visibility = View.GONE
            return
        }
        var bgOptions = RequestOptions.centerCropTransform()
            .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
        if (blur > 0) {
            bgOptions = bgOptions.transform(BlurTransformation(blur))
        }
        Glide.with(this)
                .asDrawable()
                .load(mUri)
                .apply(bgOptions)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        resource.alpha = alpha
                        val bitmap = ImageUtil.drawableToBitmap(resource)
                        findViewById(R.id.background).background = BitmapDrawable(resources, bitmap)
                        mPalette = Palette.from(bitmap).generate()
                        mTranslucentThemeColorAdapter.setPalette(mPalette)
                        mSelectColor.visibility = View.VISIBLE
                        mProgress.visibility = View.GONE
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
    }

    override fun refreshStatusBarColor() {
        ImmersionBar.with(this)
                .transparentBar()
                .init()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_translucent_theme
    }

    @SuppressLint("ApplySharedPref", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        experimentalTipView.setOnClickListener {
            showDialog {
                setTitle(R.string.title_translucent_theme_experimental_feature)
                setMessage(
                    HtmlCompat.fromHtml(
                        getString(R.string.tip_translucent_theme),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                )
                setNegativeButton(R.string.btn_close, null)
            }
        }
        listOf(
            findViewById(R.id.custom_color),
            findViewById(R.id.select_pic),
            darkColorBtn,
            lightColorBtn,
            backBtn,
            finishBtn
        ).forEach {
            it.setOnClickListener(this@TranslucentThemeActivity)
        }
        wallpapers =
            CacheUtil.getCache(this, "recommend_wallpapers", List::class.java) as List<String>?
        colorTheme.enableChangingLayoutTransition()
        wallpaperAdapter.setOnItemClickListener { _, item, _ ->
            launchUCrop(Uri.parse(item))
        }
        recommendWallpapersRv.addItemDecoration(
            HorizontalSpacesDecoration(
                0,
                0,
                16.dpToPx(),
                16.dpToPx(),
                false
            )
        )
        recommendWallpapersRv.adapter = wallpaperAdapter
        recommendWallpapersRv.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mTranslucentThemeColorAdapter.onItemClickListener =
            OnItemClickListener { _: View?, themeColor: Int, _: Int, _: Int ->
                appPreferences.translucentPrimaryColor = toString(themeColor)
                ThemeUtils.refreshUI(this)
            }
        (findViewById(R.id.select_color_recycler_view) as RecyclerView).apply {
            addItemDecoration(HorizontalSpacesDecoration(0, 0, 12.dpToPx(), 12.dpToPx(), false))
            layoutManager = MyLinearLayoutManager(
                this@TranslucentThemeActivity,
                MyLinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = mTranslucentThemeColorAdapter
        }
        alpha = appPreferences.translucentBackgroundAlpha
        blur = appPreferences.translucentBackgroundBlur
        (findViewById(R.id.alpha) as SeekBar).apply {
            progress = this@TranslucentThemeActivity.alpha
            setOnSeekBarChangeListener(this@TranslucentThemeActivity)
        }
        (findViewById(R.id.blur) as SeekBar).apply {
            progress = this@TranslucentThemeActivity.blur
            setOnSeekBarChangeListener(this@TranslucentThemeActivity)
        }
        mProgress.setOnTouchListener { _: View?, _: MotionEvent? -> true }
        mProgress.visibility = View.GONE
        val file = File(filesDir, "cropped_background.jpg")
        if (file.exists()) {
            mUri = Uri.fromFile(file)
            invalidateFinishBtn()
        }
        val bottomSheetBehavior =
            (bottomSheet.layoutParams as CoordinatorLayout.LayoutParams).behavior as BottomSheetBehavior
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                maskView.alpha = slideOffset
                maskView.visibility = if (slideOffset < 0.01f) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }

        })
        maskView.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        refreshBackground()
        refreshTheme()
        fetchWallpapers()
    }

    private fun fetchWallpapers() {
        launch(IO + job) {
            LiteApi.instance
                .wallpapersAsync()
                .doIfSuccess {
                    CacheUtil.putCache(this@TranslucentThemeActivity, "recommend_wallpapers", it)
                    wallpapers = it
                }
        }
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        appPreferences.translucentPrimaryColor = toString(color)
        ThemeUtils.refreshUI(this)
    }

    override fun onDialogDismissed(dialogId: Int) {}

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.select_color -> return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun savePic(callback: SavePicCallback<File>) {
        mProgress.visibility = View.VISIBLE
        var bgOptions = RequestOptions.centerCropTransform()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
        if (blur > 0) {
            bgOptions = bgOptions.transform(BlurTransformation(blur))
        }
        Glide.with(this)
                .asDrawable()
                .load(mUri)
                .apply(bgOptions)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        resource.alpha = alpha
                        val bitmap = ImageUtil.drawableToBitmap(resource)
                        val file = ImageUtil.compressImage(bitmap, File(filesDir, "background.jpg"))
                        mPalette = Palette.from(bitmap).generate()
                        appPreferences.translucentThemeBackgroundPath = file.absolutePath
                        ThemeUtils.refreshUI(this@TranslucentThemeActivity, this@TranslucentThemeActivity)
                        callback.onSuccess(file)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
    }

    private fun invalidateFinishBtn() {
        if (mUri != null) {
            finishBtn.visibility = View.VISIBLE
        } else {
            finishBtn.visibility = View.GONE
        }
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
    override fun onStartTrackingTouch(seekBar: SeekBar) {}
    override fun onStopTrackingTouch(seekBar: SeekBar) {
        when (seekBar.id) {
            R.id.alpha -> alpha = seekBar.progress
            R.id.blur -> blur = seekBar.progress
        }
        refreshBackground()
    }

    private fun refreshTheme() {
        when (appPreferences.translucentBackgroundTheme) {
            TRANSLUCENT_THEME_DARK -> {
                darkColorBtn.setBackgroundTintResId(R.color.default_color_accent)
                darkColorBtn.setTextColorResId(R.color.white)
                darkColorBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        ContextCompat.getDrawable(this, R.drawable.ic_round_check_circle), null, null, null
                )
                lightColorBtn.setBackgroundTintResId(R.color.color_divider)
                lightColorBtn.setTextColorResId(R.color.color_text_secondary)
                lightColorBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
            }
            TRANSLUCENT_THEME_LIGHT -> {
                darkColorBtn.setBackgroundTintResId(R.color.color_divider)
                darkColorBtn.setTextColorResId(R.color.color_text_secondary)
                darkColorBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
                lightColorBtn.setBackgroundTintResId(R.color.default_color_accent)
                lightColorBtn.setTextColorResId(R.color.white)
                lightColorBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        ContextCompat.getDrawable(this, R.drawable.ic_round_check_circle), null, null, null
                )
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_finish -> {
                appPreferences.apply {
                    translucentBackgroundAlpha = alpha
                    translucentBackgroundBlur = blur
                }
                savePic(object : SavePicCallback<File> {
                    override fun onSuccess(t: File) {
                        appPreferences.theme = ThemeUtil.THEME_TRANSLUCENT
                        toastShort(R.string.toast_save_pic_success)
                        translucentBackground = null
                        mProgress.visibility = View.GONE
                        finish()
                    }
                })
            }
            R.id.button_back -> {
                finish()
            }
            R.id.select_pic -> askPermission {
                Matisse.from(this)
                        .choose(MimeType.ofImage())
                        .theme(if (ThemeUtil.isNightMode(this)) R.style.Matisse_Dracula else R.style.Matisse_Zhihu)
                        .imageEngine(MyImageEngine())
                        .forResult(REQUEST_CODE_CHOOSE)
            }
            R.id.custom_color -> {
                val primaryColorPicker = ColorPickerDialog.newBuilder()
                        .setDialogTitle(R.string.title_color_picker_primary)
                        .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                        .setShowAlphaSlider(true)
                        .setDialogId(0)
                        .setAllowPresets(false)
                        .setColor(ThemeUtils.getColorById(this, R.color.default_color_primary))
                        .create()
                primaryColorPicker.setColorPickerDialogListener(this)
                primaryColorPicker.show(fragmentManager, "ColorPicker_TranslucentThemePrimaryColor")
            }
            R.id.dark_color -> {
                appPreferences.translucentBackgroundTheme = TRANSLUCENT_THEME_DARK
                refreshTheme()
            }
            R.id.light_color -> {
                appPreferences.translucentBackgroundTheme = TRANSLUCENT_THEME_LIGHT
                refreshTheme()
            }
        }
    }

    private fun askPermission(granted: Action<List<String?>>) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            PermissionUtil.askPermission(this, granted, R.string.toast_no_permission_insert_photo,
                    PermissionUtil.Permission(Permission.Group.STORAGE, getString(R.string.tip_permission_storage)))
        } else {
            PermissionUtil.askPermission(this, granted, R.string.toast_no_permission_insert_photo,
                    PermissionUtil.Permission(Permission.READ_EXTERNAL_STORAGE, getString(R.string.tip_permission_storage)))
        }
    }

    interface SavePicCallback<T> {
        fun onSuccess(t: T)
    }

    companion object {
        val TAG = TranslucentThemeActivity::class.java.simpleName
        const val REQUEST_CODE_CHOOSE = 2
        fun toString(alpha: Int, red: Int, green: Int, blue: Int): String {
            val hr = Integer.toHexString(red)
            val hg = Integer.toHexString(green)
            val hb = Integer.toHexString(blue)
            val ha = Integer.toHexString(alpha)
            return "#" + fixHexString(ha) + fixHexString(hr) + fixHexString(hg) + fixHexString(hb)
        }

        private fun fixHexString(string: String): String {
            var hexStr = string
            if (hexStr.isEmpty()) {
                hexStr = "00"
            }
            if (hexStr.length == 1) {
                hexStr = "0$hexStr"
            }
            if (hexStr.length > 2) {
                hexStr = hexStr.substring(0, 2)
            }
            return hexStr
        }

        fun toString(@ColorInt color: Int): String {
            return toString(Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color))
        }
    }
}