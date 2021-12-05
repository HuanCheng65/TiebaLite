package com.huanchengfly.tieba.post.utils;

import static android.content.Context.DOWNLOAD_SERVICE;

import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.webkit.URLUtil;

import com.huanchengfly.tieba.post.R;
import com.yanzhenjie.permission.runtime.Permission;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FileUtil {
    public static final int FILE_TYPE_DOWNLOAD = 0;
    public static final int FILE_TYPE_VIDEO = 1;
    public static final int FILE_TYPE_AUDIO = 2;

    public static final String FILE_FOLDER = "TiebaLite";

    public static void deleteAllFiles(File root) {
        File[] files = root.listFiles();
        if (files != null)
            for (File f : files) {
                if (f.isDirectory()) { // 判断是否为文件夹
                    deleteAllFiles(f);
                    try {
                        f.delete();
                    } catch (Exception e) {
                    }
                } else {
                    if (f.exists()) { // 判断是否存在
                        deleteAllFiles(f);
                        try {
                            f.delete();
                        } catch (Exception e) {
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
    public static String getFilePath(Context context, String dir) {
        String directoryPath = "";
        //判断SD卡是否可用
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            directoryPath = context.getExternalFilesDir(dir).getAbsolutePath();
        } else {
            directoryPath = context.getFilesDir() + File.separator + dir;
        }
        File file = new File(directoryPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return directoryPath;
    }

    public static String getFilePathByUri(Context context, Uri uri) {
        String path;
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            path = uri.getPath();
            return path;
        }
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        path = Environment.getExternalStorageDirectory() + "/" + split[1];
                        return path;
                    }
                } else if (isDownloadsDocument(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                            Long.valueOf(id));
                    path = getDataColumn(context, contentUri, null, null);
                    return path;
                } else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    path = getDataColumn(context, contentUri, selection, selectionArgs);
                    return path;
                }
            }
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        final String column = "_data";
        final String[] projection = {column};
        try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        try (Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null)) {
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            }
        }
        return "";
    }

    public static void downloadBySystem(Context context, int fileType, String url) {
        String fileName = URLUtil.guessFileName(url, null, null);
        downloadBySystem(context, fileType, url, fileName);
    }

    private static void downloadBySystemWithPermission(Context context, int fileType, String url, String fileName) {
        // 指定下载地址
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        // 允许媒体扫描，根据下载的文件类型被加入相册、音乐等媒体库
        request.allowScanningByMediaScanner();
        // 设置通知的显示类型，下载进行时和完成后显示通知
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        // 允许该记录在下载管理界面可见
        request.setVisibleInDownloadsUi(true);
        // 允许漫游时下载
        request.setAllowedOverRoaming(false);
        // 设置下载文件保存的路径和文件名
        String directory;
        switch (fileType) {
            case FILE_TYPE_VIDEO:
                directory = Environment.DIRECTORY_MOVIES;
                break;
            case FILE_TYPE_AUDIO:
                directory = Environment.DIRECTORY_PODCASTS;
                break;
            case FILE_TYPE_DOWNLOAD:
            default:
                directory = Environment.DIRECTORY_DOWNLOADS;
                break;
        }
        request.setDestinationInExternalPublicDir(directory, FILE_FOLDER + File.separator + fileName);
        final DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        // 添加一个下载任务
        if (downloadManager != null) {
            downloadManager.enqueue(request);
        }
    }

    public static void downloadBySystem(Context context, int fileType, String url, String fileName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            downloadBySystemWithPermission(context, fileType, url, fileName);
            return;
        }
        PermissionUtil.askPermission(context,
                data -> downloadBySystemWithPermission(context, fileType, url, fileName),
                R.string.toast_without_permission_download,
                new PermissionUtil.Permission(Permission.WRITE_EXTERNAL_STORAGE, context.getString(R.string.tip_permission_storage_download)));
    }

    public static String readFile(File file) {
        if (file == null || !file.exists() || !file.canRead()) {
            return null;
        }
        try {
            InputStream is = new FileInputStream(file);
            int length = is.available();
            byte[] buffer = new byte[length];
            is.read(buffer);
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean writeFile(File file, String content, boolean append) {
        if (file == null || !file.exists() || !file.canWrite()) {
            return false;
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.flush();
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}