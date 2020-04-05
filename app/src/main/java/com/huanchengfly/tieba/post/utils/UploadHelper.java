package com.huanchengfly.tieba.post.utils;

import android.content.Context;

import com.huanchengfly.tieba.api.TiebaApi;
import com.huanchengfly.tieba.api.models.WebUploadPicBean;
import com.huanchengfly.tieba.post.interfaces.UploadCallback;
import com.huanchengfly.tieba.post.models.PhotoInfoBean;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadHelper {
    public static final String TAG = UploadHelper.class.getSimpleName();
    private Context mContext;
    private List<PhotoInfoBean> fileList;
    private List<PhotoInfoBean> uploadList;
    private UploadCallback callback;
    private int now;

    private UploadHelper(Context context) {
        this.mContext = context;
        this.now = 0;
    }

    public static UploadHelper with(Context context) {
        return new UploadHelper(context);
    }

    private void upload() {
        if (callback == null) return;
        if (now >= uploadList.size()) {
            callback.onSuccess(fileList);
            return;
        }
        PhotoInfoBean photoInfoBean = uploadList.get(now);
        if (photoInfoBean.getFileUri() == null) {
            callback.onFailure("文件对象为空");
            return;
        }
        if (photoInfoBean.getWebUploadPicBean() != null) {
            callback.onProgress(this.now + 1, uploadList.size());
            next();
            return;
        }
        TiebaApi.getInstance().webUploadPic(photoInfoBean).enqueue(new Callback<WebUploadPicBean>() {
            @Override
            public void onResponse(@NotNull Call<WebUploadPicBean> call, @NotNull Response<WebUploadPicBean> response) {
                photoInfoBean.setWebUploadPicBean(response.body());
                callback.onProgress(now + 1, uploadList.size());
                next();
            }

            @Override
            public void onFailure(@NotNull Call<WebUploadPicBean> call, @NotNull Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void start() {
        if (callback == null) return;
        if (fileList.isEmpty()) {
            callback.onFailure("文件列表为空");
            return;
        }
        uploadList = new ArrayList<>();
        for (PhotoInfoBean photoInfoBean : fileList) {
            if (photoInfoBean.getFile() != null && /*photoInfoBean.getUploadResult() == null*/ photoInfoBean.getWebUploadPicBean() == null) {
                uploadList.add(photoInfoBean);
            }
        }
        if (!uploadList.isEmpty()) {
            this.now = 0;
            callback.onStart(uploadList.size());
            upload();
        } else {
            callback.onSuccess(fileList);
        }
    }

    private void next() {
        this.now += 1;
        upload();
    }

    public UploadHelper setFileList(List<PhotoInfoBean> fileList) {
        this.fileList = fileList;
        return this;
    }

    public UploadHelper setCallback(UploadCallback callback) {
        this.callback = callback;
        return this;
    }
}
