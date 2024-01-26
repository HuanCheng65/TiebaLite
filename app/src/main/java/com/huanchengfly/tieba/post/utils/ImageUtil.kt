package com.huanchengfly.tieba.post.utils

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Base64
import android.view.MenuItem
import android.view.View
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.github.panpf.sketch.request.DownloadRequest
import com.github.panpf.sketch.request.DownloadResult
import com.github.panpf.sketch.request.execute
import com.huanchengfly.tieba.post.App.Companion.INSTANCE
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.PhotoViewActivity.Companion.launch
import com.huanchengfly.tieba.post.components.transformations.RadiusTransformation
import com.huanchengfly.tieba.post.dpToPx
import com.huanchengfly.tieba.post.models.PhotoViewBean
import com.huanchengfly.tieba.post.utils.PermissionUtils.PermissionData
import com.huanchengfly.tieba.post.utils.PermissionUtils.askPermission
import com.huanchengfly.tieba.post.utils.ThemeUtil.isNightMode
import com.zhihu.matisse.MimeType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.channels.FileChannel

object ImageUtil {
    /**
     * 智能省流
     */
    const val SETTINGS_SMART_ORIGIN = 0

    /**
     * 智能无图
     */
    const val SETTINGS_SMART_LOAD = 1

    /**
     * 始终高质量
     */
    const val SETTINGS_ALL_ORIGIN = 2

    /**
     * 始终无图
     */
    const val SETTINGS_ALL_NO = 3
    const val LOAD_TYPE_SMALL_PIC = 0
    const val LOAD_TYPE_AVATAR = 1
    const val LOAD_TYPE_NO_RADIUS = 2
    const val LOAD_TYPE_ALWAYS_ROUND = 3
    const val TAG = "ImageUtil"
    private fun isGifFile(file: File?): Boolean {
        if (file == null) return false
        try {
            return FileInputStream(file).use { isGifFile(it) }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return false
    }

    //判断是否为GIF文件
    private fun isGifFile(inputStream: InputStream?): Boolean {
        if (inputStream == null) return false
        val bytes = ByteArray(4)
        try {
            inputStream.read(bytes)
            val str = String(bytes)
            return str.equals("GIF8", ignoreCase = true)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    fun compressImage(
        bitmap: Bitmap,
        quality: Int = 100
    ): ByteArray {
        val baos = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.JPEG, quality, baos)
        return baos.use { it.toByteArray() }
    }

    @JvmOverloads
    fun compressImage(
        bitmap: Bitmap,
        output: File,
        maxSizeKb: Int = 100,
        initialQuality: Int = 100
    ): File {
        val baos = ByteArrayOutputStream()
        var quality = initialQuality
        bitmap.compress(CompressFormat.JPEG, quality, baos) //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        while (baos.toByteArray().size / 1024 > maxSizeKb && quality > 0) {  //循环判断如果压缩后图片是否大于设置的最大值,大于继续压缩
            baos.reset() //重置baos即清空baos
            quality -= 5 //每次都减少5
            bitmap.compress(CompressFormat.JPEG, quality, baos) //这里压缩options%，把压缩后的数据存放到baos中
        }
        try {
            val fos = FileOutputStream(output)
            try {
                fos.write(baos.toByteArray())
                fos.flush()
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return output
    }

    @JvmOverloads
    fun bitmapToFile(
        bitmap: Bitmap,
        output: File,
        format: CompressFormat = CompressFormat.JPEG
    ): File {
        val baos = ByteArrayOutputStream()
        bitmap.compress(format, 100, baos)
        try {
            val fos = FileOutputStream(output)
            try {
                fos.write(baos.toByteArray())
                fos.flush()
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return output
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth, drawable.intrinsicHeight,
            if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
    }

    fun copyFile(src: FileInputStream?, dest: FileOutputStream?): Boolean {
        if (src == null || dest == null) {
            return false
        }
        val srcChannel: FileChannel?
        val dstChannel: FileChannel?
        try {
            srcChannel = src.channel
            dstChannel = dest.channel
            srcChannel.transferTo(0, srcChannel.size(), dstChannel)
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        try {
            srcChannel.close()
            dstChannel.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return true
    }

    fun copyFile(src: File?, dest: File?): Boolean {
        if (src == null || dest == null) {
            return false
        }
        if (dest.exists()) {
            dest.delete()
        }
        try {
            dest.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val srcChannel: FileChannel?
        val dstChannel: FileChannel?
        try {
            srcChannel = FileInputStream(src).channel
            dstChannel = FileOutputStream(dest).channel
            srcChannel.transferTo(0, srcChannel.size(), dstChannel)
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        try {
            srcChannel.close()
            dstChannel.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return true
    }

    private fun changeBrightness(imageView: ImageView, brightness: Int) {
        val cMatrix = ColorMatrix()
        cMatrix.set(
            floatArrayOf(
                1f, 0f, 0f, 0f, brightness.toFloat(), 0f, 1f, 0f, 0f, brightness.toFloat(),  // 改变亮度
                0f, 0f, 1f, 0f, brightness.toFloat(), 0f, 0f, 0f, 1f, 0f
            )
        )
        imageView.colorFilter = ColorMatrixColorFilter(cMatrix)
    }

    @SuppressLint("StaticFieldLeak")
    fun download(context: Context, url: String?) {
        download(context, url, false, null)
    }

    private fun downloadForShare(context: Context, url: String?, taskCallback: ShareTaskCallback) {
        if (url == null) return
        CoroutineScope(Dispatchers.IO).launch {
            val downloadResult = DownloadRequest(context, url).execute()
            if (downloadResult is DownloadResult.Success) {
                val inputStream = downloadResult.data.data.newInputStream()
                val pictureFolder = File(context.cacheDir, ".shareTemp")
                if (pictureFolder.exists() || pictureFolder.mkdirs()) {
                    val fileName = "share_" + System.currentTimeMillis()
                    val destFile = File(pictureFolder, fileName)
                    if (!destFile.exists()) {
                        withContext(Dispatchers.IO) {
                            destFile.createNewFile()
                        }
                    }
                    inputStream.use { input ->
                        if (destFile.canWrite()) {
                            destFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                    val shareUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        FileProvider.getUriForFile(
                            context,
                            context.packageName + ".share.FileProvider",
                            destFile
                        )
                    } else {
                        Uri.fromFile(destFile)
                    }
                    withContext(Dispatchers.Main) {
                        taskCallback.onGetUri(shareUri)
                    }
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    fun download(
        context: Context,
        url: String?,
        forShare: Boolean,
        taskCallback: ShareTaskCallback?
    ) {
        if (forShare) {
            if (taskCallback != null) downloadForShare(context, url, taskCallback)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            downloadAboveQ(context, url)
            return
        }
        askPermission(
            context,
            PermissionData(
                listOf(
                    PermissionUtils.READ_EXTERNAL_STORAGE,
                    PermissionUtils.WRITE_EXTERNAL_STORAGE
                ),
                context.getString(R.string.tip_permission_storage)
            ),
            R.string.toast_no_permission_save_photo
        ) {
            downloadBelowQ(context, url)
        }
    }

    private fun downloadAboveQ(context: Context, url: String?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            val downloadResult = DownloadRequest(context, url).execute()
            if (downloadResult is DownloadResult.Success) {
                var mimeType = MimeType.JPEG.toString()
                var fileName = URLUtil.guessFileName(url, null, mimeType)
                downloadResult.data.data.newInputStream().use { inputStream ->
                    if (isGifFile(inputStream)) {
                        mimeType = MimeType.GIF.toString()
                        fileName = FileUtil.changeFileExtension(fileName, ".gif")
                    }
                }
                downloadResult.data.data.newInputStream().use { inputStream ->
                    val relativePath =
                        Environment.DIRECTORY_PICTURES + File.separator + FileUtil.FILE_FOLDER
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.RELATIVE_PATH, relativePath)
                        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                        put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                        put(MediaStore.Images.Media.DESCRIPTION, fileName)
                    }
                    val cr = context.contentResolver
                    val uri: Uri = runCatching {
                        cr.insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            values
                        )
                    }.getOrNull() ?: return@launch
                    try {
                        cr.openFileDescriptor(uri, "w")?.use {
                            FileOutputStream(it.fileDescriptor).use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.toast_photo_saved, relativePath),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        cr.delete(uri, null, null)
                    }
                }
            }
        }
    }

    private fun downloadBelowQ(context: Context, url: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            val downloadResult = DownloadRequest(context, url).execute()
            if (downloadResult is DownloadResult.Success) {
                var fileName = URLUtil.guessFileName(url, null, MimeType.JPEG.toString())
                downloadResult.data.data.newInputStream().use { inputStream ->
                    if (isGifFile(inputStream)) {
                        fileName = FileUtil.changeFileExtension(fileName, ".gif")
                    }
                }
                downloadResult.data.data.newInputStream().use { inputStream ->
                    val pictureFolder =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val appDir = File(pictureFolder, FileUtil.FILE_FOLDER)
                    val dirExists =
                        withContext(Dispatchers.IO) { appDir.exists() || appDir.mkdirs() }
                    if (dirExists) {
                        val destFile = File(appDir, fileName)
                        if (!destFile.exists()) {
                            withContext(Dispatchers.IO) {
                                destFile.createNewFile()
                            }
                        }
                        destFile.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                        context.sendBroadcast(
                            Intent(
                                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                Uri.fromFile(File(destFile.path))
                            )
                        )
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.toast_photo_saved, destFile.path),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun checkGifFile(file: File) {
        if (isGifFile(file)) {
            val gifFile = File(file.parentFile, FileUtil.changeFileExtension(file.name, ".gif"))
            if (gifFile.exists()) {
                file.delete()
            } else {
                file.renameTo(gifFile)
            }
        }
    }

    fun getPicId(picUrl: String?): String {
        val fileName = URLUtil.guessFileName(picUrl, null, MimeType.JPEG.toString())
        return fileName.replace(".jpg", "")
    }

    @JvmStatic
    fun initImageView(
        view: ImageView,
        photoViewBeans: List<PhotoViewBean?>,
        position: Int,
        forumName: String?,
        forumId: String?,
        threadId: String?,
        seeLz: Boolean,
        objType: String?
    ) {
        view.setOnClickListener { v: View ->
            val tag = view.getTag(R.id.image_load_tag)
            if (tag != null) {
                val loaded = tag as Boolean
                if (loaded) {
                    launch(
                        v.context,
                        photoViewBeans.toTypedArray(),
                        position,
                        forumName,
                        forumId,
                        threadId,
                        seeLz,
                        objType
                    )
                } else {
                    load(view, LOAD_TYPE_SMALL_PIC, photoViewBeans[position]!!.url, true)
                }
            }
        }
        view.setOnLongClickListener {
            val popupMenu = PopupUtil.create(view)
            popupMenu.menuInflater.inflate(R.menu.menu_image_long_click, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                if (item.itemId == R.id.menu_save_image) {
                    download(view.context, photoViewBeans[position]!!.originUrl)
                    return@setOnMenuItemClickListener true
                }
                false
            }
            popupMenu.show()
            true
        }
    }

    @JvmStatic
    fun initImageView(view: ImageView, photoViewBeans: List<PhotoViewBean>, position: Int) {
        view.setOnClickListener { v: View ->
            val tag = view.getTag(R.id.image_load_tag)
            if (tag != null) {
                val loaded = tag as Boolean
                if (loaded) {
                    launch(v.context, photoViewBeans, position)
                } else {
                    load(view, LOAD_TYPE_SMALL_PIC, photoViewBeans[position].url, true)
                }
            }
        }
        view.setOnLongClickListener {
            val popupMenu = PopupUtil.create(view)
            popupMenu.menuInflater.inflate(R.menu.menu_image_long_click, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                if (item.itemId == R.id.menu_save_image) {
                    download(view.context, photoViewBeans[position].originUrl)
                    return@setOnMenuItemClickListener true
                }
                false
            }
            popupMenu.show()
            true
        }
    }

    fun initImageView(view: ImageView, photoViewBean: PhotoViewBean) {
        val photoViewBeans: MutableList<PhotoViewBean> = ArrayList()
        photoViewBeans.add(photoViewBean)
        initImageView(view, photoViewBeans, 0)
    }

    @JvmStatic
    fun getNonNullString(vararg strings: String?): String? {
        for (url in strings) {
            if (!TextUtils.isEmpty(url)) {
                return url
            }
        }
        return null
    }

    @JvmStatic
    fun getRadiusPx(context: Context): Int {
        return DisplayUtil.dp2px(context, getRadiusDp(context).toFloat())
    }

    private fun getRadiusDp(context: Context): Int {
        return context.appPreferences.radius
    }

    fun clear(imageView: ImageView?) {
        Glide.with(imageView!!).clear(imageView)
    }

    fun getPlaceHolder(context: Context, radius: Int): Drawable {
        val drawable = GradientDrawable()
        val colorResId =
            if (isNightMode()) R.color.color_place_holder_night else R.color.color_place_holder
        val color = ContextCompat.getColor(context, colorResId)
        drawable.setColor(color)
        drawable.cornerRadius =
            DisplayUtil.dp2px(context, radius.toFloat()).toFloat()
        return drawable
    }

    @SuppressLint("CheckResult")
    fun load(
        imageView: ImageView,
        @LoadType type: Int,
        url: String?,
        skipNetworkCheck: Boolean,
        noTransition: Boolean
    ) {
        if (!Util.canLoadGlide(imageView.context)) {
            return
        }
        val radius = getRadiusDp(imageView.context)
        val requestBuilder =
            if (skipNetworkCheck ||
                type == LOAD_TYPE_AVATAR ||
                imageLoadSettings == SETTINGS_ALL_ORIGIN ||
                imageLoadSettings == SETTINGS_SMART_ORIGIN ||
                (imageLoadSettings == SETTINGS_SMART_LOAD && NetworkUtil.isWifiConnected(imageView.context))
            ) {
                imageView.setTag(R.id.image_load_tag, true)
                Glide.with(imageView).load(url)
            } else {
                imageView.setTag(R.id.image_load_tag, false)
                Glide.with(imageView).load(
                    getPlaceHolder(
                        imageView.context,
                        if (type == LOAD_TYPE_SMALL_PIC) radius else 0
                    )
                )
            }
        if (isNightMode()) {
            changeBrightness(imageView, -35)
        } else {
            imageView.clearColorFilter()
        }
        when (type) {
            LOAD_TYPE_SMALL_PIC -> requestBuilder.apply(
                RequestOptions.bitmapTransform(RadiusTransformation(radius))
                    .placeholder(getPlaceHolder(imageView.context, radius))
                    .skipMemoryCache(true)
            )

            LOAD_TYPE_AVATAR -> requestBuilder.apply(
                RequestOptions.bitmapTransform(RadiusTransformation(6))
                    .placeholder(getPlaceHolder(imageView.context, 6))
                    .skipMemoryCache(true)
            )

            LOAD_TYPE_NO_RADIUS -> requestBuilder.apply(
                RequestOptions()
                    .placeholder(getPlaceHolder(imageView.context, 0))
                    .skipMemoryCache(true)
            )

            LOAD_TYPE_ALWAYS_ROUND -> requestBuilder.apply(
                RequestOptions()
                    .circleCrop()
                    .placeholder(getPlaceHolder(imageView.context, 100.dpToPx()))
                    .skipMemoryCache(true)
            )
        }
        if (!noTransition) {
            requestBuilder.transition(DrawableTransitionOptions.withCrossFade())
        }
        requestBuilder.into(imageView)
    }

    @JvmStatic
    @JvmOverloads
    fun load(
        imageView: ImageView,
        @LoadType type: Int,
        url: String?,
        skipNetworkCheck: Boolean = false
    ) {
        load(imageView, type, url, skipNetworkCheck, false)
    }

    /**
     * 获取要加载的图片 Url
     *
     * @param isSmallPic   加载的是否为缩略图
     * @param originUrl    原图 Url
     * @param smallPicUrls 缩略图 Url，按照画质从好到差排序
     * @return 要加载的图片 Url
     */
    @JvmStatic
    fun getUrl(
        context: Context,
        isSmallPic: Boolean,
        originUrl: String,
        vararg smallPicUrls: String?
    ): String {
        val urls = mutableListOf(*smallPicUrls)
        if (isSmallPic) {
            if (needReverse(context)) {
                urls.reverse()
            }
            return urls.firstOrNull { !it.isNullOrEmpty() } ?: originUrl
        }
        return originUrl
    }

    private fun needReverse(context: Context): Boolean {
        return if (imageLoadSettings == SETTINGS_SMART_ORIGIN &&
            NetworkUtil.isWifiConnected(context)
        ) false
        else imageLoadSettings != SETTINGS_ALL_ORIGIN
    }

    @get:ImageLoadSettings
    private val imageLoadSettings: Int
        get() = INSTANCE.appPreferences.imageLoadType!!.toInt()

    fun imageToBase64(inputStream: InputStream?): String? {
        if (inputStream == null) {
            return null
        }
        return runCatching {
            inputStream.use {
                Base64.encodeToString(inputStream.readBytes(), Base64.DEFAULT)
            }
        }.getOrNull()
    }

    fun imageToBase64(file: File?): String? {
        if (file == null) {
            return null
        }
        var result: String? = null
        try {
            val `is`: InputStream = FileInputStream(file)
            result = imageToBase64(`is`)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }

    interface ShareTaskCallback {
        fun onGetUri(uri: Uri)
    }

    @IntDef(LOAD_TYPE_SMALL_PIC, LOAD_TYPE_AVATAR, LOAD_TYPE_NO_RADIUS, LOAD_TYPE_ALWAYS_ROUND)
    @Retention(AnnotationRetention.SOURCE)
    annotation class LoadType

    @IntDef(SETTINGS_SMART_ORIGIN, SETTINGS_SMART_LOAD, SETTINGS_ALL_ORIGIN, SETTINGS_ALL_NO)
    annotation class ImageLoadSettings

}

fun ImageUtil.download(
    context: Context,
    url: String?,
    forShare: Boolean,
    taskCallback: (Uri) -> Unit
) {
    download(context, url, forShare, object : ImageUtil.ShareTaskCallback {
        override fun onGetUri(uri: Uri) {
            taskCallback(uri)
        }
    })
}
