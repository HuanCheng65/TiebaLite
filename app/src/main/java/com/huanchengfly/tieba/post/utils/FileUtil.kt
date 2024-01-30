package com.huanchengfly.tieba.post.utils

import android.app.DownloadManager
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.webkit.URLUtil
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.utils.PermissionUtils.PermissionData
import com.huanchengfly.tieba.post.utils.PermissionUtils.askPermission
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.Arrays

object FileUtil {
    const val FILE_TYPE_DOWNLOAD = 0
    const val FILE_TYPE_VIDEO = 1
    const val FILE_TYPE_AUDIO = 2
    const val FILE_FOLDER = "TiebaLite"
    fun deleteAllFiles(root: File) {
        val files = root.listFiles()
        if (files != null) for (f in files) {
            if (f.isDirectory) { // 判断是否为文件夹
                deleteAllFiles(f)
                try {
                    f.delete()
                } catch (e: Exception) {
                }
            } else {
                if (f.exists()) { // 判断是否存在
                    deleteAllFiles(f)
                    try {
                        f.delete()
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }

    /**
     * @param context 上下文对象
     * @param dir     存储目录
     * @return
     */
    fun getFilePath(context: Context, dir: String): String {
        var directoryPath = ""
        //判断SD卡是否可用
        directoryPath = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            context.getExternalFilesDir(dir)!!.absolutePath
        } else {
            context.filesDir.toString() + File.separator + dir
        }
        val file = File(directoryPath)
        if (!file.exists()) {
            file.mkdirs()
        }
        return directoryPath
    }

    fun getFilePathByUri(context: Context, uri: Uri): String? {
        val path: String?
        if (ContentResolver.SCHEME_FILE == uri.scheme) {
            path = uri.path
            return path
        }
        if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split =
                        docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        path = Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                        return path
                    }
                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                    path = getDataColumn(context, contentUri, null, null)
                    return path
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split =
                        docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])
                    path = getDataColumn(context, contentUri, selection, selectionArgs)
                    return path
                }
            }
        }
        return null
    }

    private fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?,
    ): String? {
        val column = "_data"
        val projection = arrayOf(column)
        context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            .use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    val column_index = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(column_index)
                }
            }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    @JvmStatic
    fun getRealPathFromUri(context: Context, contentUri: Uri?): String {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        context.contentResolver.query(contentUri!!, proj, null, null, null).use { cursor ->
            if (cursor != null) {
                val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()
                return cursor.getString(column_index)
            }
        }
        return ""
    }

    fun downloadBySystem(context: Context, fileType: Int, url: String?) {
        val fileName = URLUtil.guessFileName(url, null, null)
        downloadBySystem(context, fileType, url, fileName)
    }

    private fun downloadBySystemWithPermission(
        context: Context,
        fileType: Int,
        url: String?,
        fileName: String,
    ) {
        // 指定下载地址
        val request = DownloadManager.Request(Uri.parse(url))
        // 允许媒体扫描，根据下载的文件类型被加入相册、音乐等媒体库
        request.allowScanningByMediaScanner()
        // 设置通知的显示类型，下载进行时和完成后显示通知
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        // 允许该记录在下载管理界面可见
        request.setVisibleInDownloadsUi(true)
        // 允许漫游时下载
        request.setAllowedOverRoaming(false)
        // 设置下载文件保存的路径和文件名
        val directory: String
        directory = when (fileType) {
            FILE_TYPE_VIDEO -> Environment.DIRECTORY_MOVIES
            FILE_TYPE_AUDIO -> Environment.DIRECTORY_PODCASTS
            FILE_TYPE_DOWNLOAD -> Environment.DIRECTORY_DOWNLOADS
            else -> Environment.DIRECTORY_DOWNLOADS
        }
        request.setDestinationInExternalPublicDir(
            directory,
            FILE_FOLDER + File.separator + fileName
        )
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        // 添加一个下载任务
        downloadManager.enqueue(request)
    }

    fun downloadBySystem(context: Context, fileType: Int, url: String?, fileName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            downloadBySystemWithPermission(context, fileType, url, fileName)
            return
        }
        askPermission(
            context,
            PermissionData(
                Arrays.asList(
                    PermissionUtils.READ_EXTERNAL_STORAGE,
                    PermissionUtils.WRITE_EXTERNAL_STORAGE
                ),
                context.getString(R.string.tip_permission_storage_download)
            )
        ) {
            downloadBySystemWithPermission(context, fileType, url, fileName)
            null
        }
    }

    @JvmStatic
    fun readFile(file: File?): String? {
        if (file == null || !file.exists() || !file.canRead()) {
            return null
        }
        try {
            val `is`: InputStream = FileInputStream(file)
            val length = `is`.available()
            val buffer = ByteArray(length)
            `is`.read(buffer)
            return String(buffer, StandardCharsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    @JvmStatic
    fun writeFile(file: File?, content: String, append: Boolean): Boolean {
        if (file == null || !file.exists() || !file.canWrite()) {
            return false
        }
        try {
            val fos = FileOutputStream(file)
            fos.write(content.toByteArray())
            fos.flush()
            fos.close()
            return true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    fun writeFile(file: File, inputStream: InputStream): Boolean {
        if (!file.exists() || !file.canWrite()) {
            return false
        }
        try {
            val fos = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var byteCount: Int
            while (inputStream.read(buffer).also { byteCount = it } != -1) {
                fos.write(buffer, 0, byteCount)
            }
            fos.flush()
            fos.close()
            inputStream.close()
            return true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    //修改文件扩展名
    fun changeFileExtension(fileName: String, newExtension: String): String {
        if (TextUtils.isEmpty(fileName)) {
            return fileName
        }
        val index = fileName.lastIndexOf(".")
        return if (index == -1) {
            fileName + newExtension
        } else fileName.substring(0, index) + newExtension
    }
}