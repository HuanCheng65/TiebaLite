package com.huanchengfly.tieba.post.utils;

import static com.huanchengfly.tieba.post.utils.FileUtil.FILE_FOLDER;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.huanchengfly.tieba.post.BaseApplication;
import com.huanchengfly.tieba.post.ExtensionsKt;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.activities.PhotoViewActivity;
import com.huanchengfly.tieba.post.components.transformations.RadiusTransformation;
import com.huanchengfly.tieba.post.models.PhotoViewBean;
import com.yanzhenjie.permission.runtime.Permission;
import com.zhihu.matisse.MimeType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ImageUtil {
    /**
     * 智能省流
     */
    public static final int SETTINGS_SMART_ORIGIN = 0;
    /**
     * 智能无图
     */
    public static final int SETTINGS_SMART_LOAD = 1;
    /**
     * 始终高质量
     */
    public static final int SETTINGS_ALL_ORIGIN = 2;
    /**
     * 始终无图
     */
    public static final int SETTINGS_ALL_NO = 3;

    public static final int LOAD_TYPE_SMALL_PIC = 0;
    public static final int LOAD_TYPE_AVATAR = 1;
    public static final int LOAD_TYPE_NO_RADIUS = 2;
    public static final int LOAD_TYPE_ALWAYS_ROUND = 3;
    public static final String TAG = "ImageUtil";

    public static File compressImage(Bitmap bitmap, File output, int maxSize) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        while (baos.toByteArray().length / 1024 > maxSize && quality > 0) {  //循环判断如果压缩后图片是否大于20kb,大于继续压缩 友盟缩略图要求不大于18kb
            baos.reset();//重置baos即清空baos
            quality -= 5;//每次都减少5
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);//这里压缩options%，把压缩后的数据存放到baos中
        }
        try {
            FileOutputStream fos = new FileOutputStream(output);
            try {
                fos.write(baos.toByteArray());
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return output;
    }

    public static File bitmapToFile(Bitmap bitmap, File output) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        try {
            FileOutputStream fos = new FileOutputStream(output);
            try {
                fos.write(baos.toByteArray());
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return output;
    }

    public static File compressImage(Bitmap bitmap, File output) {
        return compressImage(bitmap, output, 100);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static boolean copyFile(FileInputStream src, FileOutputStream dest) {
        boolean result = false;
        if ((src == null) || (dest == null)) {
            return result;
        }

        FileChannel srcChannel = null;
        FileChannel dstChannel = null;

        try {
            srcChannel = src.getChannel();
            dstChannel = dest.getChannel();
            srcChannel.transferTo(0, srcChannel.size(), dstChannel);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
            return result;
        }
        try {
            srcChannel.close();
            dstChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean copyFile(File src, File dest) {
        boolean result = false;
        if ((src == null) || (dest == null)) {
            return result;
        }
        if (dest.exists()) {
            dest.delete();
        }
        try {
            dest.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileChannel srcChannel = null;
        FileChannel dstChannel = null;

        try {
            srcChannel = new FileInputStream(src).getChannel();
            dstChannel = new FileOutputStream(dest).getChannel();
            srcChannel.transferTo(0, srcChannel.size(), dstChannel);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
            return result;
        }
        try {
            srcChannel.close();
            dstChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static void changeBrightness(ImageView imageView, int brightness) {
        ColorMatrix cMatrix = new ColorMatrix();
        cMatrix.set(new float[]{1, 0, 0, 0, brightness, 0, 1, 0, 0, brightness, // 改变亮度
                0, 0, 1, 0, brightness, 0, 0, 0, 1, 0});
        imageView.setColorFilter(new ColorMatrixColorFilter(cMatrix));
    }

    @SuppressLint("StaticFieldLeak")
    public static void download(Context context, String url, boolean gif) {
        download(context, url, gif, false, null);
    }

    @SuppressLint("StaticFieldLeak")
    public static void download(Context context, String url, boolean gif, boolean forShare, @Nullable ShareTaskCallback taskCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            downloadAboveQ(context, url, forShare, taskCallback);
            return;
        }
        PermissionUtil.askPermission(context,
                data -> downloadBelowQ(context, url, forShare, taskCallback),
                R.string.toast_no_permission_save_photo,
                new PermissionUtil.Permission(Permission.Group.STORAGE, context.getString(R.string.tip_permission_storage)));
    }

    private static void downloadAboveQ(Context context, String url, boolean forShare, @Nullable ShareTaskCallback taskCallback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return;
        }
        new DownloadAsyncTask(context, url, file -> {
            String fileName = URLUtil.guessFileName(url, null, MimeType.JPEG.toString());
            String relativePath = Environment.DIRECTORY_PICTURES + File.separator + FILE_FOLDER;
            if (forShare) {
                relativePath += File.separator + "shareTemp";
            }
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.RELATIVE_PATH, relativePath);
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, MimeType.JPEG.toString());
            values.put(MediaStore.Images.Media.DESCRIPTION, fileName);
            Uri uri = null;
            ContentResolver cr = context.getContentResolver();
            try {
                uri = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri == null) {
                    return;
                }
                ParcelFileDescriptor descriptor = cr.openFileDescriptor(uri, "w");
                FileOutputStream outputStream = new FileOutputStream(descriptor.getFileDescriptor());
                FileInputStream inputStream = new FileInputStream(file);
                copyFile(inputStream, outputStream);
            } catch (Exception e) {
                e.printStackTrace();
                if (uri != null) {
                    cr.delete(uri, null, null);
                }
                return;
            }
            if (!forShare)
                Toast.makeText(context, context.getString(R.string.toast_photo_saved, relativePath), Toast.LENGTH_SHORT).show();
            else if (taskCallback != null)
                taskCallback.onGetUri(uri);
        }).execute();
    }

    private static void downloadAboveQ(Context context, String url) {
        downloadAboveQ(context, url, false, null);
    }

    private static void downloadBelowQ(Context context, String url, boolean forShare, @Nullable ShareTaskCallback taskCallback) {
        new DownloadAsyncTask(context, url, file -> {
            File pictureFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsoluteFile();
            File appDir;
            if (forShare) {
                appDir = new File(pictureFolder, FILE_FOLDER + File.separator + "shareTemp");
            } else {
                appDir = new File(pictureFolder, FILE_FOLDER);
            }
            if (appDir.exists() || appDir.mkdirs()) {
                if (forShare) {
                    File nomedia = new File(appDir, ".nomedia");
                    if (!nomedia.exists()) {
                        try {
                            nomedia.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                String fileName = URLUtil.guessFileName(url, null, MimeType.JPEG.toString());
                File destFile = new File(appDir, fileName);
                if (destFile.exists()) {
                    return;
                }
                copyFile(file, destFile);
                if (!forShare) {
                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(destFile.getPath()))));
                    Toast.makeText(context, context.getString(R.string.toast_photo_saved, destFile.getPath()), Toast.LENGTH_SHORT).show();
                } else if (taskCallback != null) {
                    taskCallback.onGetUri(FileProvider.getUriForFile(context, context.getPackageName() + ".share.FileProvider", destFile));
                }
            }
        }).execute();
    }

    @SuppressLint("StaticFieldLeak")
    private static void downloadBelowQ(Context context, String url) {
        downloadBelowQ(context, url, false, null);
    }

    public static String getPicId(String picUrl) {
        String fileName = URLUtil.guessFileName(picUrl, null, MimeType.JPEG.toString());
        return fileName.replace(".jpg", "");
    }

    public static void initImageView(ImageView view, List<PhotoViewBean> photoViewBeans, int position, String forumName, String forumId, String threadId, boolean seeLz, String objType) {
        view.setOnClickListener(v -> {
            Object tag = view.getTag(R.id.image_load_tag);
            if (tag != null) {
                boolean loaded = (boolean) tag;
                if (loaded) {
                    PhotoViewActivity.launch(v.getContext(), photoViewBeans.toArray(new PhotoViewBean[0]), position, forumName, forumId, threadId, seeLz, objType);
                } else {
                    load(view, LOAD_TYPE_SMALL_PIC, photoViewBeans.get(position).getUrl(), true);
                }
            }
        });
        view.setOnLongClickListener(v -> {
            PopupMenu popupMenu = PopupUtil.create(view);
            popupMenu.getMenuInflater().inflate(R.menu.menu_image_long_click, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.menu_save_image:
                        download(view.getContext(), photoViewBeans.get(position).getOriginUrl(), false);
                        return true;
                }
                return false;
            });
            popupMenu.show();
            return true;
        });
    }

    public static void initImageView(ImageView view, List<PhotoViewBean> photoViewBeans, int position) {
        view.setOnClickListener(v -> {
            Object tag = view.getTag(R.id.image_load_tag);
            if (tag != null) {
                boolean loaded = (boolean) tag;
                if (loaded) {
                    PhotoViewActivity.launch(v.getContext(), photoViewBeans, position);
                } else {
                    load(view, LOAD_TYPE_SMALL_PIC, photoViewBeans.get(position).getUrl(), true);
                }
            }
        });
        view.setOnLongClickListener(v -> {
            PopupMenu popupMenu = PopupUtil.create(view);
            popupMenu.getMenuInflater().inflate(R.menu.menu_image_long_click, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.menu_save_image:
                        download(view.getContext(), photoViewBeans.get(position).getOriginUrl(), false);
                        return true;
                }
                return false;
            });
            popupMenu.show();
            return true;
        });
    }

    public static void initImageView(ImageView view, PhotoViewBean photoViewBean) {
        List<PhotoViewBean> photoViewBeans = new ArrayList<>();
        photoViewBeans.add(photoViewBean);
        initImageView(view, photoViewBeans, 0);
    }

    public static String getNonNullString(String... strings) {
        for (String url : strings) {
            if (!TextUtils.isEmpty(url)) {
                return url;
            }
        }
        return null;
    }

    public static int getRadiusPx(Context context) {
        return DisplayUtil.dp2px(context, getRadiusDp(context));
    }

    public static int getRadiusDp(Context context) {
        return SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_SETTINGS).getInt("radius", 8);
    }

    public static void load(ImageView imageView, @LoadType int type, String url) {
        load(imageView, type, url, false);
    }

    public static void clear(ImageView imageView) {
        Glide.with(imageView).clear(imageView);
    }

    private static Drawable getPlaceHolder(Context context, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        int color = ThemeUtil.isNightMode(context) ? context.getResources().getColor(R.color.color_place_holder_night) : context.getResources().getColor(R.color.color_place_holder);
        drawable.setColor(color);
        drawable.setCornerRadius(DisplayUtil.dp2px(context, radius));
        return drawable;
    }

    public static void load(ImageView imageView, @LoadType int type, String url, boolean skipNetworkCheck) {
        if (!Util.canLoadGlide(imageView.getContext())) {
            return;
        }
        int radius = SharedPreferencesUtil.get(imageView.getContext(), SharedPreferencesUtil.SP_SETTINGS).getInt("radius", 8);
        RequestBuilder<Drawable> requestBuilder;
        if (skipNetworkCheck ||
                type == LOAD_TYPE_AVATAR ||
                getImageLoadSettings() == SETTINGS_ALL_ORIGIN ||
                getImageLoadSettings() == SETTINGS_SMART_ORIGIN ||
                (getImageLoadSettings() == SETTINGS_SMART_LOAD && NetworkUtil.isWifiConnected(imageView.getContext()))) {
            imageView.setTag(R.id.image_load_tag, true);
            requestBuilder = Glide.with(imageView).load(url);
        } else {
            imageView.setTag(R.id.image_load_tag, false);
            requestBuilder = Glide.with(imageView).load(getPlaceHolder(imageView.getContext(), type == LOAD_TYPE_SMALL_PIC ? radius : 0));
        }
        if (ThemeUtil.isNightMode(imageView.getContext())) {
            changeBrightness(imageView, -35);
        } else {
            imageView.clearColorFilter();
        }
        switch (type) {
            case LOAD_TYPE_SMALL_PIC:
                requestBuilder.apply(RequestOptions.bitmapTransform(new RadiusTransformation(radius))
                        .placeholder(getPlaceHolder(imageView.getContext(), radius))
                        .skipMemoryCache(true));
                break;
            case LOAD_TYPE_AVATAR:
                requestBuilder.apply(RequestOptions.bitmapTransform(new RadiusTransformation(6))
                        .placeholder(getPlaceHolder(imageView.getContext(), 6))
                        .skipMemoryCache(true));
                break;
            case LOAD_TYPE_NO_RADIUS:
                requestBuilder.apply(new RequestOptions()
                        .placeholder(getPlaceHolder(imageView.getContext(), 0))
                        .skipMemoryCache(true));
                break;
            case LOAD_TYPE_ALWAYS_ROUND:
                requestBuilder.apply(new RequestOptions()
                        .circleCrop()
                        .placeholder(getPlaceHolder(imageView.getContext(), ExtensionsKt.dpToPx(100)))
                        .skipMemoryCache(true));
                break;
        }
        requestBuilder.transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
    }

    /**
     * 获取要加载的图片 Url
     *
     * @param isSmallPic   加载的是否为缩略图
     * @param originUrl    原图 Url
     * @param smallPicUrls 缩略图 Url，按照画质从好到差排序
     * @return 要加载的图片 Url
     */
    public static String getUrl(Context context, boolean isSmallPic, @NonNull String originUrl, @NonNull String... smallPicUrls) {
        List<String> urls = Arrays.asList(smallPicUrls);
        if (isSmallPic) {
            if (needReverse(context)) {
                Collections.reverse(urls);
            }
            for (String url : urls) {
                if (!TextUtils.isEmpty(url)) {
                    return url;
                }
            }
        }
        return originUrl;
    }

    private static boolean needReverse(Context context) {
        if (getImageLoadSettings() == SETTINGS_SMART_ORIGIN && NetworkUtil.isWifiConnected(context)) {
            return false;
        }
        return getImageLoadSettings() != SETTINGS_ALL_ORIGIN;
    }

    @ImageLoadSettings
    private static int getImageLoadSettings() {
        return Integer.parseInt(SharedPreferencesUtil.get(BaseApplication.getInstance(), SharedPreferencesUtil.SP_SETTINGS).getString("image_load_type", String.valueOf(SETTINGS_SMART_ORIGIN)));
    }

    public static String imageToBase64(InputStream is) {
        if (is == null) {
            return null;
        }
        byte[] data = null;
        String result = null;
        try {
            //创建一个字符流大小的数组。
            data = new byte[is.available()];
            //写入数组
            is.read(data);
            //用默认的编码格式进行编码
            result = Base64.encodeToString(data, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return result;
    }

    public static String imageToBase64(File file) {
        if (file == null) {
            return null;
        }
        String result = null;
        try {
            InputStream is = new FileInputStream(file);
            result = imageToBase64(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public interface ShareTaskCallback {
        void onGetUri(Uri uri);
    }

    @IntDef({LOAD_TYPE_SMALL_PIC, LOAD_TYPE_AVATAR, LOAD_TYPE_NO_RADIUS, LOAD_TYPE_ALWAYS_ROUND})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LoadType {
    }

    @IntDef({SETTINGS_SMART_ORIGIN, SETTINGS_SMART_LOAD, SETTINGS_ALL_ORIGIN, SETTINGS_ALL_NO})
    public @interface ImageLoadSettings {
    }

    public static class DownloadAsyncTask extends AsyncTask<Void, Integer, File> {
        private WeakReference<Context> contextWeakReference;
        private TaskCallback callback;
        private String url;

        public DownloadAsyncTask(Context context, String url, TaskCallback callback) {
            this.contextWeakReference = new WeakReference<>(context);
            this.url = url;
            this.callback = callback;
        }

        public TaskCallback getCallback() {
            return callback;
        }

        public String getUrl() {
            return url;
        }

        private Context getContext() {
            return contextWeakReference.get();
        }

        @Override
        protected File doInBackground(Void... voids) {
            File file = null;
            try {
                FutureTarget<File> future = Glide.with(getContext())
                        .asFile()
                        .load(getUrl())
                        .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
                file = future.get();
                return file;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return file;
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            getCallback().onPostExecute(file);
        }

        public interface TaskCallback {
            void onPostExecute(File file);
        }
    }
}
