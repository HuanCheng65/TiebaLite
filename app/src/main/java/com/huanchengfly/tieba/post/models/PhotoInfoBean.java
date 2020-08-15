package com.huanchengfly.tieba.post.models;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.huanchengfly.tieba.post.api.models.UploadResultBean;
import com.huanchengfly.tieba.post.api.models.WebUploadPicBean;
import com.huanchengfly.tieba.post.utils.FileUtil;

import java.io.File;

public class PhotoInfoBean {
    public static final String TAG = "PhotoInfoBean";
    private String filePath;
    private Uri fileUri;
    private File file;
    private WebUploadPicBean webUploadPicBean;
    private UploadResultBean uploadResult;

    public PhotoInfoBean(Context context, Uri fileUri) {
        this(context, fileUri, null);
    }

    public PhotoInfoBean(Context context, Uri fileUri, UploadResultBean uploadResult) {
        this.fileUri = fileUri;
        this.uploadResult = uploadResult;
        try {
            this.file = new File(FileUtil.getRealPathFromUri(context, fileUri));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public WebUploadPicBean getWebUploadPicBean() {
        return webUploadPicBean;
    }

    public void setWebUploadPicBean(WebUploadPicBean webUploadPicBean) {
        this.webUploadPicBean = webUploadPicBean;
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public PhotoInfoBean setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
        return this;
    }

    @Nullable
    public File getFile() {
        return file;
    }

    public UploadResultBean getUploadResult() {
        return uploadResult;
    }

    public PhotoInfoBean setUploadResult(UploadResultBean uploadResult) {
        this.uploadResult = uploadResult;
        return this;
    }
}
