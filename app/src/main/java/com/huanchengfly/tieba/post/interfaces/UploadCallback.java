package com.huanchengfly.tieba.post.interfaces;

import com.huanchengfly.tieba.post.models.PhotoInfoBean;

import java.util.List;

public interface UploadCallback {
    void onSuccess(List<PhotoInfoBean> photoInfoBeans);

    void onStart(int total);

    void onProgress(int current, int total);

    void onFailure(String error);
}
