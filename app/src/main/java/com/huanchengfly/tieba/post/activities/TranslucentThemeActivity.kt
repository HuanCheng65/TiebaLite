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
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.widget.Toolbar
import androidx.core.text.HtmlCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.gyf.immersionbar.ImmersionBar
import com.huanchengfly.tieba.post.BaseApplication
import com.huanchengfly.tieba.post.BaseApplication.Companion.translucentBackground
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.ThemeColorAdapter
import com.huanchengfly.tieba.post.components.MyImageEngine
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager
import com.huanchengfly.tieba.post.components.transformations.BlurTransformation
import com.huanchengfly.tieba.post.interfaces.OnItemClickListener
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.ColorUtils
import com.huanchengfly.tieba.post.utils.ImageUtil
import com.huanchengfly.tieba.post.utils.PermissionUtil
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.jrummyapps.android.colorpicker.ColorPickerDialog
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener
import com.yalantis.ucrop.UCrop
import com.yanzhenjie.permission.Action
import com.yanzhenjie.permission.runtime.Permission
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import java.io.File

class TranslucentThemeActivity : BaseActivity(), View.OnClickListener, OnSeekBarChangeListener, ColorPickerDialogListener {
    private var mUri: Uri? = null
    private var alpha = 0
    private var blur = 0
    private var mPalette: Palette? = null

    @BindView(R.id.select_color)
    lateinit var mSelectColor: View
    private var mAdapter: ThemeColorAdapter? = null

    @BindView(R.id.progress)
    lateinit var mProgress: View
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == Activity.RESULT_OK) {
            val sourceUri = Matisse.obtainResult(data)[0]
            Glide.with(this)
                    .asDrawable()
                    .load(sourceUri)
                    .into(object : CustomTarget<Drawable>() {
                        override fun onLoadCleared(placeholder: Drawable?) {}

                        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                            val bitmap = ImageUtil.drawableToBitmap(resource)
                            val file = ImageUtil.bitmapToFile(bitmap, File(cacheDir, "origin_background.jpg"))
                            val sourceFileUri = Uri.fromFile(file)
                            val destUri = Uri.fromFile(File(cacheDir, "cropped_background.jpg"))
                            val height = BaseApplication.ScreenInfo.EXACT_SCREEN_HEIGHT.toFloat()
                            val width = BaseApplication.ScreenInfo.EXACT_SCREEN_WIDTH.toFloat()
                            val uCropOptions = UCrop.Options()
                            uCropOptions.setShowCropFrame(true)
                            uCropOptions.setShowCropGrid(true)
                            uCropOptions.setStatusBarColor(ColorUtils.getDarkerColor(ThemeUtils.getColorByAttr(this@TranslucentThemeActivity, R.attr.colorPrimary)))
                            uCropOptions.setToolbarColor(ThemeUtils.getColorByAttr(this@TranslucentThemeActivity, R.attr.colorPrimary))
                            uCropOptions.setToolbarWidgetColor(ThemeUtils.getColorByAttr(this@TranslucentThemeActivity, R.attr.colorTextOnPrimary))
                            uCropOptions.setActiveWidgetColor(ThemeUtils.getColorByAttr(this@TranslucentThemeActivity, R.attr.colorAccent))
                            uCropOptions.setActiveControlsWidgetColor(ThemeUtils.getColorByAttr(this@TranslucentThemeActivity, R.attr.colorAccent))
                            uCropOptions.setLogoColor(ThemeUtils.getColorByAttr(this@TranslucentThemeActivity, R.attr.colorPrimary))
                            uCropOptions.setCompressionFormat(Bitmap.CompressFormat.JPEG)
                            UCrop.of(sourceFileUri, destUri)
                                    .withAspectRatio(width / height, 1f)
                                    .withOptions(uCropOptions)
                                    .start(this@TranslucentThemeActivity)
                        }
                    })
        } else if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            mUri = UCrop.getOutput(data!!)
            invalidateOptionsMenu()
            refreshBackground()
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            cropError!!.printStackTrace()
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
                        findViewById(R.id.background).backgroundTintList = null
                        findViewById(R.id.background).background = BitmapDrawable(resources, bitmap)
                        mPalette = Palette.from(bitmap).generate()
                        mAdapter!!.setPalette(mPalette)
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
        setSupportActionBar(findViewById(R.id.toolbar) as Toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle(R.string.title_dialog_translucent_theme)
        (findViewById(R.id.tip) as TextView).apply {
            text = HtmlCompat.fromHtml(getString(R.string.tip_translucent_theme), HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
        (findViewById(R.id.custom_color) as Button).apply {
            setOnClickListener(this@TranslucentThemeActivity)
        }
        (findViewById(R.id.select_pic) as Button).apply {
            setOnClickListener(this@TranslucentThemeActivity)
        }
        mAdapter = ThemeColorAdapter(this)
        mAdapter!!.onItemClickListener = OnItemClickListener { _: View?, themeColor: Int, _: Int, _: Int ->
            appPreferences.translucentPrimaryColor = toString(themeColor)
            ThemeUtils.refreshUI(this)
        }
        (findViewById(R.id.select_color_recycler_view) as RecyclerView).apply {
            layoutManager = MyLinearLayoutManager(this@TranslucentThemeActivity, MyLinearLayoutManager.HORIZONTAL, false)
            adapter = mAdapter
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
        findViewById(R.id.background).setBackgroundColor(Color.BLACK)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_translucent_theme_toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("ApplySharedPref")
    override fun onColorSelected(dialogId: Int, color: Int) {
        appPreferences.translucentPrimaryColor = toString(color)
        ThemeUtils.refreshUI(this)
    }

    override fun onDialogDismissed(dialogId: Int) {}

    @SuppressLint("ApplySharedPref")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_finish -> {
                appPreferences.apply {
                    translucentBackgroundAlpha = alpha
                    translucentBackgroundBlur = blur
                }
                savePic(object : SavePicCallback<File> {
                    override fun onSuccess(t: File) {
                        ThemeUtil.getSharedPreferences(this@TranslucentThemeActivity)
                                .edit()
                                .putString(ThemeUtil.SP_THEME, ThemeUtil.THEME_TRANSLUCENT)
                                .putString(ThemeUtil.SP_OLD_THEME, ThemeUtil.THEME_TRANSLUCENT)
                                .commit()
                        toastShort(R.string.toast_save_pic_success)
                        translucentBackground = null
                        mProgress.visibility = View.GONE
                        finish()
                    }
                })
                return true
            }
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

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val finishItem = menu.findItem(R.id.menu_finish)
        finishItem.isEnabled = mUri != null
        return super.onPrepareOptionsMenu(menu)
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

    override fun onClick(v: View) {
        when (v.id) {
            R.id.select_pic -> askPermission(Action {
                Matisse.from(this)
                        .choose(MimeType.ofImage())
                        .theme(if (ThemeUtil.isNightMode(this)) R.style.Matisse_Dracula else R.style.Matisse_Zhihu)
                        .imageEngine(MyImageEngine())
                        .forResult(REQUEST_CODE_CHOOSE)
            })
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